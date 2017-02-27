/**
 *  Alarm.com Service Manager
 *
 *  Author: Schwark Satyavolu
 *
 */
definition(
    name: "Alarm.com",
    namespace: "schwark",
    author: "Schwark Satyavolu",
    description: "Allows you to connect your Alarm.com alarm system with SmartThings and control them from your Things area or Dashboard in the SmartThings Mobile app.",
    category: "SmartThings Labs",
    iconUrl: "https://images-na.ssl-images-amazon.com/images/I/71yQ11GAAiL.png",
    iconX2Url: "https://images-na.ssl-images-amazon.com/images/I/71yQ11GAAiL.png",
    singleInstance: true
)

preferences {
	input("username", "string", title:"Username", description: "Please enter your Alarm.com username", required: true, displayDuringSetup: true)
	input("password", "password", title:"Password", description: "Please enter your Alarm.com password", required: true, displayDuringSetup: true)
	input("addArmStay", "bool", title:"Do you want to add Arm Stay as a Switch?", description: "Turning this on will add one switch Arm Stay of your Alarm", required: false, displayDuringSetup: true, defaultValue: true )
	input("addArmAway", "bool", title:"Do you want to add Arm Away as a Switch?", description: "Turning this on will add one switch Arm Away of your Alarm", required: false, displayDuringSetup: true, defaultValue: true )
}

/////////////////////////////////////
def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def uninstalled() {
	log.debug("Uninstalling with settings: ${settings}")
	unschedule()

	removeChildDevices(getChildDevices())
}

/////////////////////////////////////
def updated() {
	//log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

/////////////////////////////////////
def initialize() {
	// remove location subscription aftwards
	unsubscribe()
	state.subscribe = false

	runIn(60*5, doDeviceSync)
}

def getHubId() {
	return state.hubId ? state.hubId : location.hubs[0].id
}

/////////////////////////////////////
def locationHandler(evt) {
	log.debug "$locationHandler(evt.description)"
	def description = evt.description
	def hub = evt?.hubId
	state.hubId = hub
	log.debug("location handler: event description is ${description}")
}

/////////////////////////////////////
private def parseEventMessage(Map event) {
	//handles gateway attribute events
	return event
}

private def parseEventMessage(String description) {
}


private def getCommands() {
	def COMMANDS = [
					'ARMSTAY': ['params': ['ctl00$phBody$butArmStay':'Arm Stay'], 'name': 'Arm Stay'],
					'ARMAWAY': ['params': ['ctl00$phBody$butArmAway':'Arm Away'], 'name': 'Arm Away']
				   ]

	return COMMANDS
}

private def getVarPattern(key=null) {
   def VARPATTERNS = [
   						'pda': /(?ms)pda\/([^\/]+)/
   					 ]
   return key ? VARPATTERNS[key] : VARPATTERNS
}

private def runCommand(command, browserSession=[:]) {
	def COMMANDS = getCommands()
	if(!COMMANDS[command]) return

	def params = [:]
	browserSession.state = ['pda':'']
	browserSession.vars = ['__VIEWSTATEGENERATOR':'','__EVENTTARGET':'','__EVENTARGUMENT':'','__VIEWSTATEENCRYPTED':'','__EVENTVALIDATION':'','__VIEWSTATE':'']
	params.uri = 'https://www.alarm.com/pda/'
	navigateUrl(params, browserSession)
	params.uri = 'https://www.alarm.com/pda/${pda}/default.aspx'
	navigateUrl(params, browserSession)
	params.method = 'post'
	browserSession.referer = params.uri
	params.query = [
				'ctl00$ContentPlaceHolder1$txtLogin': settings.username,
			  	'ctl00$ContentPlaceHolder1$txtPassword': settings.password
			 ]
	browserSession.vars = ['__VIEWSTATEENCRYPTED':'','__EVENTVALIDATION':'','__VIEWSTATE':'']
	navigateUrl(params, browserSession) {response, bSession, body ->
	}
	params.uri = 'https://www.alarm.com/pda/${pda}/main.aspx'
	params.method = 'post'
	browserSession.referer = params.uri
	params.query = COMMANDS[command]['params']
	navigateUrl(params, browserSession) {response, bSession, body ->
	}

	return browserSession
}

private def getPatternValue(html, browserSession, kind, variable, pattern=null) {
	if(!pattern) {
        pattern = getVarPattern(variable)
        if(!pattern) pattern = /(?ms)name="${variable}".*?value="([^"]*)"/
    }
	log.debug("looking for values with pattern ${pattern} for ${variable}")
	def value = null
	if(html) {
		if(!browserSession[kind]) browserSession[kind] = [:]
		def group = (html =~ pattern)
		if(group) {
			log.debug "found variable value ${group[0][1]} for ${variable}"
			value = group[0][1]
			browserSession[kind][variable] = value
		}
	}
	return value
}

private def extractSession(response, browserSession) {
	log.debug("extracting session variables..")
	def count = 1
	def html = response.data
    //log.debug("html is ${html}")

	['vars','state'].each { kind ->
	    browserSession[kind].each() { name, value ->
	  		if('vars' == kind && name) { // use node search for vars
				def foundNode = html.find { it.@name == name }
                def foundValue = foundNode ? foundNode.@value : null
				if(foundValue) {
					browserSession[kind][name] = foundValue
					log.debug "found form value ${foundValue} for ${name}"
				}
			}
			if('state' == kind && name) {
				getPatternValue(html, browserSession, kind, name)
			}
		}
	}

	return browserSession
}

private def fillTemplate(template, map) {
	if(!map) return template
	def result = template.replaceAll(/\$\{(\w+)\}/) { k -> map[k[1]] ?: k[0] }
	return result
}

private def navigateUrl(params, browserSession, processor=null) {
	def success = { response ->
    	log.debug "Request was successful.. processing cookies"
    	log.trace("response status is ${response.status}")

    	browserSession.cookies = !browserSession.get('cookies') ? [] : browserSession.cookies
    	response.headerIterator('Set-Cookie').each {
    		log.debug "adding cookie to request: ${it}"
      		browserSession.cookies.add(it.value.split(';')[0])
    	}

    	if(response.status == 200) {
			extractSession(response, browserSession)
	    	if(processor) processor(response, browserSession)
	    }

	    return browserSession
    }

	if(params.uri) {
		params.uri = fillTemplate(params.uri, browserSession.vars + browserSession.state)
		log.debug("navigating to ${params.uri}...")
        if(!params.headers) params.headers = [:]
		params.headers['User-Agent'] = 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36'
		if(browserSession.referer) params.headers['Referer'] = browserSession.referer
		if(browserSession.cookies) {
			params.headers['Cookie'] = browserSession.cookies.join(";")
            log.debug("using cookies ${params.headers['Cookie']}")
		}
		if(browserSession.vars) {
			params.query = (params.query ? params.query : [:]) + browserSession.vars
			log.debug("using params: ${params.query}")
		}
        
		try {
			if(params.method == 'post') {
				httpPost(params, success)
			} else {
	    		httpGet(params,success)
   			}
    		browserSession.referer = params.uri		
		} catch (e) {
    			log.error "something went wrong: $e"
		}
	}

	return browserSession
}

/////////////////////////////////////
def doDeviceSync(){
	log.debug "Doing Alarm.com Device Sync!"

	if(!state.subscribe) {
		subscribe(location, null, locationHandler, [filterEvents:false])
		state.subscribe = true
	}

	createSwitches()
}


////////////////////////////////////////////
//CHILD DEVICE METHODS
/////////////////////////////////////
def parse(childDevice, description) {
	def parsedEvent = parseEventMessage(description)

	if (parsedEvent.headers && parsedEvent.body) {
		def headerString = new String(parsedEvent.headers.decodeBase64())
		def bodyString = new String(parsedEvent.body.decodeBase64())
		log.debug "parse() - ${bodyString}"
	} else {
		log.debug "parse - got something other than headers,body..."
		return []
	}
}

def createSwitches() {
	log.debug("Creating Alarm.com Switches...")
	if(state.childDevicesCreated) return

	def COMMANDS = getCommands()
	COMMANDS.each() { id, map ->
		def name = map['name']
		log.debug("processing switch ${id} with name ${name}")
		def PREFIX = "ALARMCOM"
		def hubId = getHubId()
		def alarmSwitch = addChildDevice("schwark", "Alarm.com Switch", "${PREFIX}${id}", hubId, ["name": "AlarmCom.${id}", "label": "${name}", "completedSetup": true])
		log.debug("created child device ${PREFIX}${id} with name ${name} and hub ${hubId}")
		alarmSwitch.setCommand(id)
	}
	state.childDevicesCreated = true
}

private Integer convertHexToInt(hex) {
	Integer.parseInt(hex,16)
}

private String convertHexToIP(hex) {
	[convertHexToInt(hex[0..1]),convertHexToInt(hex[2..3]),convertHexToInt(hex[4..5]),convertHexToInt(hex[6..7])].join(".")
}

private Boolean canInstallLabs()
{
	return hasAllHubsOver("000.011.00603")
}

private Boolean hasAllHubsOver(String desiredFirmware)
{
	return realHubFirmwareVersions.every { fw -> fw >= desiredFirmware }
}

private List getRealHubFirmwareVersions()
{
	return location.hubs*.firmwareVersionString.findAll { it }
}

private removeChildDevices(data) {
    data.delete.each {
        deleteChildDevice(it.deviceNetworkId)
    }
}
