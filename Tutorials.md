# Treasure Hunter Tutorials
## Install & Start:
* after install the apk, you will find this icon on your phone 
![Icon](https://raw.githubusercontent.com/vista20sun/Treasure-Hunter/master/Screenshot/Screenshot_2018-02-21-19-13-31-646_com.miui.home.png)
* when you open this application, you should see this screen:
![main screen](https://raw.githubusercontent.com/vista20sun/Treasure-Hunter/master/Screenshot/Screenshot_2018-02-21-19-14-40-119_com.example.vista.treasurefinder.png)
in this screen, you can find only one button *"off"* are clickable, just click it to start looking for iBeacons.

## Permission & Dependent Hardware
This application needs some specific hardwere and system feaature to work. So when you use this application you need to given the application permission to access the resources  
* bluetooth: this application needs to access the bluetooth hardware to scan the beacons, so if you start search when the bluetooth are closen, you will see this screen:  
![bluetooth switch](https://raw.githubusercontent.com/vista20sun/Treasure-Hunter/master/Screenshot/Screenshot_2018-02-21-19-14-20-340_com.android.settings.png)
please allow this application to turn on bluetooth.
* get location: scanning bluetooth LE devices on android phone needs the permission to get location. when this app first run on a phone, you may see this screen.  
![location permission](https://raw.githubusercontent.com/vista20sun/Treasure-Hunter/master/Screenshot/Screenshot_2018-02-21-19-14-11-422_com.lbe.security.miui.png)
please given this application permission to access location service

## iBeacon filter & settings:
This application will looking for any iBecaon and get the rssi of the closest one, but you could set a filter to make this application just looking for a special beacon in setting screen by following steps.
* if the application are working, please stop it.
* hold the switch button for 3 seconds when the application are not working, then you will open the settings screen.
![settings](https://raw.githubusercontent.com/vista20sun/Treasure-Hunter/master/Screenshot/Screenshot_2018-02-21-19-14-46-509_com.example.vista.treasurefinder.png)
* you can pick up a *major* & *minor* id in the dropdown menu on the top of this screen as a fliter, and use the save button at right top corner to apply this change.
![dropdown menu](https://raw.githubusercontent.com/vista20sun/Treasure-Hunter/master/Screenshot/Screenshot_2018-02-21-19-14-55-637_com.example.vista.treasurefinder.png)
* you can also change blackground colors, open or closing gradual color change and flashing background in this page...

## work with this app:
when you start searching, the blackground change to read at once and start flashing(if opened), the application will also start buzzing when you turn on the searching. when you are geting close to the target beacon, the buzzing frequence will get higher, back ground will turn to blue and the Treasure chest icon will get bigger.