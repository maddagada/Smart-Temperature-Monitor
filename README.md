# Smart-Temperature-Humid-Controller
# Executive Summary :
The idea behind this project is tracking and sending an alert message if the temperature or humidity levels in the patient’s rooms, cold storage facilities, cold cargos and finally in our own house exceeds the certain point.
This application is very useful in many cases from general usage to industrial purpose such as in the hospitals if the patient needs to be monitored in the certain temperature conditions strictly, if the goods in the storage facility or cold cargo need to store at only certain temperature and humidity, in the houses and many other more.
It seems interesting for me because this application serves in general purpose and industrial benefits. We have number of benefits. Temperature and Humidity should be maintained properly. Human body cannot resist to very low temperature and to hot temperatures. Humidity is also equally important to be a healthy person. If there is no moisture in the air, then it dries out our skin and leads to many skin diseases and health issues. On the other hand, this application is used in business purpose like cold cargos, storage facilities to track the temperature in the cargo or room. So that they can make sure about the conditions of goods that they stored in the room/cargo are in good condition.
# Project Goals and Objectives:
	Develop a native app which is integrated to the metawear device (IoT device) to track the temperature and humidity changes.

	Sending alert message to the user about temperature and humidity level changes when they reach threshold limit to the user’s android phone.

	Alert message is configured with ringtone and LED light notification.

	Tracking the temperature and humidity in the storage facilities, patient rooms (emergency rooms) and cold cargos.

# Project Merits:
This project will mainly help in the emergency conditions in our houses. We might not know if the temperature system fails to work in one of the room in the house in midnight time. It might lead to death conditions if temperature increases to 100F. In such conditions this application will help you by giving alert message with vibration and LED light indication.

	This project is designed for multipurpose uses. This application can be used in general areas to business areas.

	This project is designed specifically to track the temperature and humidity levels in the cold storage facilities, cold cargos and in the hospital’s emergency/ICU patient rooms.

	We can also use this application to monitor humidity level in our house mainly in winter season.
# APPLICATION REQUIREMENTS
# User Stories:
1.	As someone who needs to store things only in freezing conditions, I want to know the temperature update in timely manner. So that I can act immediately before things gets destroyed.

Acceptance Criteria:

	Track the temperature levels in timely manner.

	Send an alert message when the temperature reaches threshold point with LED notification.

2.	As the patients who are in emergency/ICU condition cannot respond to the drop in the temperature or humidity levels, I want to know the update of the temperature and humidity levels in the room. So that we can make sure the patient condition and act accordingly.

Acceptance Criteria:

	Track the temperature and humidity levels in timely manner.

	Send an alert message/notification to the mobile whenever the temperature/humidity rises above the threshold level. 
# Misuser Stories:

•	As a bad competitor in storage mart/ cold cargo business, I want to get access to the other strong competitor metawear device by sending multiple request, so I can destroy functioning of the device.

Mitigation techniques:

  1. Bluetooth should be turned on only whenever we required.

  2. To get the data, we can use web services instead of Bluetooth.

•	As a misuser, I want to sniff the data, so I can change the values in the log file.

Mitigation techniques:

o	Log files should be encrypted, so that who has no authorization cannot access the files.

•	As a malicious user, I want to connect to the android device using any vulnerability in the android operating system, so I can manipulate or harm the working of the application.

Mitigation techniques:

  1. Must update the android operating system to avoid any vulnerabilities in OS.

# Architecture

![alt text](https://github.com/maddagada/Smart-Temperature-Humid-Controller/blob/master/Images/Architecture.PNG)


# Components List

# 1.	Android device:

    1.1.	Developed application (. apk) is stored in the mobile device.

    1.2.	Device Bluetooth is used to connect with the metawear device.

    1.3.	Other features of the android device are used, such as alerts/push notification.

    1.4.	An metawear sdk (metawear interface) will be downloaded into the android device to communicate with the metawear.

# 2.	Native mobile application:

Our application essentially does three main tasks

    2.1	Application connects to the metawear device to stream the data (temperature and humidity) from the android device.

    2.2	As the data streams to the log file stored on the device, application will read the .CSV file constantly and checks the values against the threshold limit. log files are stored to the android device.

    2.3	When the temperature and humidity levels reached the threshold limit, application will notify the user with alerts/push notification.

# 3.	Metawear device:

    3.1	Bluetooth feature in the android device is used to connect with the Bluetooth LE on the metawear device. 

    3.2	In this device we use BME280 humidity/pressure sensor to track the humidity level similarly, we use thermistor sensor which tracks the temperature.

    3.3	BME280 humidity/pressure and temperature sensors are connected to the Bluetooth LE. 

# SECURITY ANALYSIS:

![alt text](https://github.com/maddagada/Smart-Temperature-Humid-Controller/blob/master/Images/Security%20Analysis%20Table.PNG)
