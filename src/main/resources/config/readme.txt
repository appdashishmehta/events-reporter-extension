Events Reporter Utility:
------------------------

1) The purpose of this utility is to check if the process (windows) or command (linux/unix) is running or not.

2) If those are not running, then the configured action is invoked for the command to notify the interested party about the command not running.

3) As of now the supported actions are:
- Sending an email
- Raising incident in PagerDuty via REST API
- Sending Events to Controller as CUSTOM event type with severity as defined in eventType property.

4) The required configurations and input data for Actions and Commands are to be provided in properties.xml (sample file is provided)

5) The location of properties.xml to be set in monitor.xml
linux/unix:
defalut-value = /path-to-machine-agent-install/monitors/EventsReporter/properties.xml
Windows = \path-to-machine-agent-install\monitors\EventsReporter\properties.xml

6) Required details:

- For Email Action:
  mail.smtp.host
  mail.smtp.port
  mail.smtp.auth
  mail.smtp.user (the from address)
  mail.smtp.password
  and other required values as per email service provider
- For Commands using email Action, properties = 'subject' and 'body' with corresponding values are required

- For PagerDuty Action:
  service key
  service URL
- For commands using PagerDuty action, a property name = 'description' is required

- For EventsToController Action:
  httpUrl (should NOT be changed)
- For commands using EventsToController action:
  a property name = 'summary' with corresponding value is required.
  a property name = 'eventType' with value as one of these [error, info, warning] is required.

7) Each command runs at its own frequency set in Minutes (frequencyMins in each command tag in properties.xml)
frequencyMins - This is a REQUIRED Field for every command and should be a positive numeric.
Minimum value = 1

8) Each command has its own 'enabled' setting, if its 'true' then only it will be verified if running or not.

9) When the machine agent starts, all the configured actions are disabled by default. The utility will run a verification for all actions and enable them once it is verified to be working.
If the action is not enabled, the action will not be invoked and no notification will be sent.

10) For PagerDuty Action, when a command (process) is not running, a incident ID will be created and will be reused again and again to make PagerDuty API call. Once the command (process) starts running, the incident will be resolved by calling resolve type API to PagerDuty and this incident Id will be removed from cache.
If again the command found to be not running, a new incident id will be created.


How this works:
---------------

Windows:
We get the process list running in windows and check of contains for the given process.
As the check is on processs list CONTAINS, if there are duplicate processes then it will return true as well.
For example:
Command = notepad++.exe
If the process list (task manager) has notepad++.exe running then it will not take action otherwise it will invoke action corresponding to this command.


Linux/Unix/Aix:
Command = ps - ef | grep java | grep -v grep
The program will check if the above command is running on the machine or not (this assumes only 1 process matching this is running on machine)
If its not running invoke the action.

Important:
---------
Appending 'grep - v grep' is required (for grep commands) as simple 'ps -ef | grep java' will return 'grep java' also in the process lists
whereas   'ps - ef | grep java | grep -v grep' will exclude 'grep java' and only return if the actual java process is running or not.

To run this:
------------

1) Unzip the EventsReporter.zip at <machine-agent-install>/monitors/*
2) Edit <machine-agent-install>/monitors/EventsReporter/monitor.xml to correctly provide the path of properties.xml (if the location is changed)
3) Default properties.xml is provided in <machine-agent-install>/monitors/EventsReporter/
4) Edit the properties.xml to provide the required actions/commands and other details.


5) Run machine agent
run machine agent as usual:
java -jar machineagent.jar

if 'EventsToController' action is used/set, then run machine agent as:
java -Dmetric.http.listener=true -jar machineagent.jar
