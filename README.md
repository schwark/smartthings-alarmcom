# smartthings-alarmcom
SmartThings Integration with alarm.com


Login to https://graph.api.smartthings.com/ with your smartthings username and password

Install the Smartapp as a SmartApp:
Click on My SmartApps
Click on New SmartApp
Click on From Code
Cut and Paste the code from smartapp/AlarmCom.groovy in the big text field and click on Create!

Install the device as a custom device:
Click on My Device Handlers
Click on New Device Handler
Click on From Code
Cut and Paste the code from devices/AlarmComSwitch.groovy in the big text field and click on Create!

Go to SmartThings Mobile App

Click on Automation
Click on SmartApps
Scroll to very bottom and hit Add a SmartApp
Scroll to very bottom and hit My Apps
Click on Alarm.com
Type in your alarm.com username and password and Save
Wait 10 minutes for the switches to show up in your Things

One switch is created call '''Arm Away''' and one called '''Arm Stay'''
Turning either of them on OR off will activate the corresponding Arm action
For security purposes, Disarm does not have a switch