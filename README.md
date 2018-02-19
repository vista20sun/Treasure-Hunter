# Treasure Hunder for IPRO-497 

Author: Yuyang Luo

This is a Lite-weight software to look up the closet beacon around you for the game.

app name already change to Treasure Hunter but the package name is still TreasureFinder  

## Environment:  
* Android studio 3.0.1(windows x64 version)  
* Android sdk version: 26  
* Gradle version 4.1  
* Android build tools version: 3.0.1  

## Third party Library:
* Android Beacon Library
> https://altbeacon.github.io/android-beacon-library/  
*import by gradle 'org.altbeacon:android-beacon-library:2+'*  
-------

## Upload History

* 02/07/2018: (ver 1.2)
	* First upload  
	* Use the Android Beacon Library to implements the search function  
	* make uuid fliter work for *iBeacon*  

* 02/09/2018: (ver 1.3)
	* Remove the word hints and size changable circle from UI, add a full screen size changable back ground.
	* Add filter of major & minor pair, could looking for a special beacon.
	* Add background flicker.
	* Add a setting screen (hold switch button when is off to get in), could open/close flicker, gradual color change. Could custom background colors in application.  
	* rewrite "README.md"  

* 02/13/2018: (ver1.4)
	* Adjusted sensitivity
	* Add a scalable treasure chest icon into main screen
	* Fix some bugs with flashing background
	* Change some button's color

* 02/18/2018:
	* Adjusted sensitivity to fit small room
	* add avereage RSSI calculation to make reduce disturbance of noise 

