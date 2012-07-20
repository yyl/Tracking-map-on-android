##Tracking map on Android

*Note: this is originally based on [my other repo][1], 
but it has been changed drastically therefore I think it would be easier to just create another new version.
The old one still works, but from now I will only update this version.*

###What could it do
- start the background process to periodically track user's location (using GPS)
- for every point it records, it also obtain nearby places via Google Places API
- store data and meta info into local database
- an icon on notification bar to indicate the running process and also be able to go back to the app
- start/stop function within the app
- export location data
- draw points on map based on the data

###What's new
- tracking method is more accurate
- export the data in db to "sdcard/tracking/"
- map visualization added (not real-time)

###Dependencies
- Android support library: could downloaded via Android SDK manager
- Google API Java client

###Issues
- You have to obtain your own google api key.
- the shortest update interval (for GPS) is 10s.

Feel free to fork it; have fun.

[1]: https://github.com/yyl/android-location-tracking