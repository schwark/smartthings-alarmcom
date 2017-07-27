# SmartThings Integration with alarm.com


* Login to the IDE with your smartthings username and password
** North American users: https://graph-na02-useast1.api.smartthings.com/430
** European users: https://graph-eu01-euwest1.api.smartthings.com/102

* Install the Smartapp as a SmartApp:
```
Click on My SmartApps
Click on New SmartApp
Click on From Code
Cut and Paste the code from smartapp/AlarmCom.groovy in the big text field and click on Create!
Click on Publish/For Me
```

* Install the device as a custom device:
```
Click on My Device Handlers
Click on New Device Handler
Click on From Code
Cut and Paste the code from devices/AlarmComSwitch.groovy in the big text field and click on Create!
Click on Publish/For Me
```

 * Activate SmartApp in SmartThings Mobile App
```
Click on Automation
Click on SmartApps
Scroll to very bottom and hit Add a SmartApp
Scroll to very bottom and hit My Apps
Click on Alarm.com
Type in your alarm.com username and password and Save
If you want a Disarm Switch also created, turn that option on - see CAUTION below.
Wait 10 minutes for the switches to show up in your Things
OPTIONAL:
Customize the silent arming (default on) option on either switch
Customize no entry delay (default off) option on either switch
```

Two switches will show up under Things: one switch is called **Arm Away** and one called **Arm Stay**. Turning either of them on OR off will activate the corresponding Arm action.

* CAUTION: 
```
For security purposes, Disarm does not have a switch by default
The smart app option allows you to create a Disarm button, but use at your own risk
It will make it easier to disarm your system, especially if you integrate that switch with Alexa
```

* Upgrade process
```
If you are upgrading from a prior version of this smartapp, follow these steps:
Remove the Switches from all automations, routines and smartapps (Alexa, Google Home for example)
Uninstall the smartapp from your SmartThings mobile app
Login to https://graph.api.smartthings.com/ with your smartthings username and password
Click on My SmartApps
Click on Alarm.com name
Delete the existing code, and Cut and Paste the code into the code box
Click Save and then Publish/For Me
Click on My Device Handlers
Click on Alarm.com Switch name
Delete the existing code, and Cut and Paste the code into the code box
Click Save and then Publish/For Me
Go to you mobile app and reinstall the smartapp, and wait 10 minutes for the switches to show up again
Re-add your switches to your automations, Alexa, etc.
```
