/**
 *  Alarm.com Switch for SmartThings
 *  Schwark Satyavolu
 *  Originally based on: Allan Klein's (@allanak) and Mike Maxwell's code
 *
 *  Usage:
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */

metadata {
	definition (name: "Alarm.com Switch", namespace: "schwark", author: "Schwark Satyavolu") {
	capability "Switch"
	command "setCommand", ["string"]
	command "runCommand"
	}

simulator {
		// TODO: define status and reply messages here
	}

tiles {
	standardTile("switch", "device.switch", width: 1, height: 1, canChangeIcon: true) {
        	state "on", label: '${name}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#79b821"
        	state "off", label: '${name}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
   		}
	}

preferences {
}

    main "switch"
    details(["switch"])
}

def updated() {
}

def runCommand() {
	parent.runCommand(state.command)
}

def on() {
	runCommand()
	sendEvent(name: "switch", value: "on")
}

def off() {
	runCommand()
	sendEvent(name: "switch", value: "off")
}

def setCommand(command) {
	state.command = command
}



