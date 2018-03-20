# Smart-Temperature Controller
# Executive Summary :
The idea behind this project is tracking and sending an alert message if the temperature levels in the cold storage facilities and cold cargos exceeds or drops the threshold point.
This application is targeted towards wholesale clubs like Sam’s club, Costco, BJ’S. These whole sale sellers store lot of their produce in subzero conditions. To keep the produce safe, they must maintain temperature is a key. For example, milk will get rotten if they are not stored in appropriate temperature conditions. Then we need to be equipped with technology that reduces the continuous monitoring of the temperatures inside the produce storage facility.
Our application can be used by wholesale clubs. keep produce safe. Instead of constantly checking for the temperatures our application will notify the user with alert message when temperature exceeds/drop the threshold limit in storage facility.

# Project Goals and Objectives:
* Develop a native app which is integrated to the metawear device (IoT device).

* Tracking the temperature in wholesale clubs. 

* Sending alert message to the user android phone when temperature exceed the threshold limit.

* Sending alert message to the user android phone when temperature drops the threshold limit.

# Project Merits:
This project will mainly help in the emergency conditions. We might not know if the temperature system fails to work in the produce storage facility/room in the midnight time. In such conditions this application will help the user by giving alert message. Then the user can act immediately before the produce gets thawed completely or rotten.

* This project is designed for multipurpose uses. This project is designed specifically to track the temperature levels in business areas like wholesale clubs. It is very useful to them because they store most part of their produce in subzero temperatures.

* Our application reduces the hassle of continuous checking or monitoring of temperature.

# APPLICATION REQUIREMENTS
# User Stories:
As a **user**, I want to **know the temperature update in timely manner**. So that **I don’t have to bother**.

**Acceptance Criteria**:

* Track the temperature levels in timely manner.

As a **user**, I want to **know through the alert message when something wrong with the temperature reading**. So that I can **act immediately before produce gets thawed or rotten**.

**Acceptance Criteria**:

* Sending an alert message to the user’s android phone when the temperature exceeds the threshold point.

* Sending an alert message to the user’s android phone when temperature drops the threshold level. 
# Misuser Stories:

As a **contending competitor** in wholesale club business, I wanted to **send multiple request to the competitor’s meta wear device to get access**, so that I can **destroy the proper functioning of the device**.

**Mitigation techniques**:

  1. Bluetooth should be turned on only whenever we required.

  2. To get the data, we can use web services instead of Bluetooth.

As a **misuser**, I want to **sniff the data**, so I can **change the values in the log file**.

**Mitigation techniques**:

  1. Log files should be encrypted, so that who has no authorization cannot access the files.

As a **malicious user**, I want to **connect to the android device using any vulnerability in the android operating system**, so I can **manipulate or harm the working of the application**.

**Mitigation techniques**:

  1. Must update the android operating system to avoid any vulnerabilities in OS.

# Architecture Design

![alt text](https://github.com/maddagada/Smart-Temperature-Humid-Controller/blob/master/Images/Modified_ArchitectureDesign.PNG)


# Components List

# 1. Android device:

    1.1.	Developed application (. apk) is stored in the mobile device.

    1.2.	Device Bluetooth is used to connect with the metawear device.

    1.3.	Other features of the android device are used, such as alerts/push notification.

    1.4.	An metawear sdk (metawear interface) will be downloaded into the android device to communicate with the metawear(IoT) device.

# 2. Metawear device:

    2.1	Bluetooth feature in the android device is used to connect with the Bluetooth LE on the metawear device. 

    2.2	In this device we use thermistor sensor to track the temperature level.

    2.3	Temperature sensor is connected to the Bluetooth LE.

# SECURITY ANALYSIS:

| Component name | Category of vulnerability | Issue Description | Mitigation |
|----------------|---------------------------|-------------------|------------|
| Bluetooth | Privilege Escalation | When a device is paired via Bluetooth, the user has absolutely access to everything on the phone. They can destroy anything on the device if they choose to. | Turning off Bluetooth whenever not in used is the best way to mitigate Bluetooth related attacks. Instead of using Bluetooth to get data, we can get data to the mobile phone using webservices. |
| Meta wear device | Resource Drain | If the user gains pairing access to the meta wear, he can own multiple communication channels and drain the battery of the meta wear device by sending multiple of requests. | Physical access and proximity to the meta wear devices must be restricted so that the attacker will have less chance of attacking the meta wear. |
| | Man-In-The-Middle | The attacker looks to interrupt, and sniff data transferred between two systems. | Encrypting the data. |
| | Tampering | The attacker tries to change the working of the device. | Meta wear must be placed or stored in a secured place. |
| | Denial of Service | The attacker tries to update the device by sending malicious firmware update. | Device firmware must be updated from the authentic or legitimate website only. |
| Android Application | Social Engineering | Applications are vulnerable to attack through some messages or pop-up alerts. | Employees must be trained on such type of attack to get more awareness. |
| Android device | Any vulnerability android OS has | There are many vulnerabilities found with the android operating system. An attacker can access the android device many ways outside of Bluetooth to gain access to our app and can-do harm to our application. | We need to make sure, we are using latest hardware and installing all required updated all the time, so that its safe against the attacks from the attacker. |
