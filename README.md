# GotAPIPlugin
GotAPI plugin for Kadecot

[GotAPI](https://device-webapi.org/) is an [open-source](https://github.com/DeviceConnect), Android-hosted device API standardized by OMA. This hybrid plugin provides to Kadecot bridge access to GotAPI, while to GotAPI server bridge access to Kadecot.

# How to use
1. Install [Kadecot](https://play.google.com/store/apps/details?id=com.sonycsl.Kadecot) and GotAPI server (such as [Device Web API Manager by GClue.inc](https://play.google.com/store/apps/details?id=org.deviceconnect.android.manager) ) to your Android phone.
2. Boot Kadecot as usual, then you will find GotAPI and related devices icon within devices list, as well as other smart appliances supported by Kadecot. 

Then you are all set. You don't need to install this plugin by yourself, because this is now statically linked to the Kadecot's official release. GotAPI server should already recognize Kadecot and provide access to ECHONET Lite and other devices through "kadecot" profile. The arguments and reply structure is described in [Kadecot API page](http://kadecot.net/blog/2750/).

# How to access via JSONP

Terms:  
[kip] : IP address of Kadecot  
[gid] : Device ID of "GotAPI" device in Kadecot  
[did] : Device ID of specific GotAPI device in Kadecot  

+ Obtain services suppoted by the system and plugins:  
http://[kip]:31413/jsonp/v1/devices/[gid]?procedure=system.get&params={}

+ Obtain services supported by each device:  
http://[kip]:31413/jsonp/v1/devices/[did]?procedure=serviceinformation.get&params={}

+ Examples for [light profile](https://github.com/deviceconnect/DeviceConnect-JS/wiki/Light-Profile)  
 + GET:    http://[kip]:31413/jsonp/v1/devices/[did]?procedure=light.get&params={}  
 + POST:   http://[kip]:31413/jsonp/v1/devices/[did]?procedure=light.post&params={lightId:6,color:"0000FF"}  
 + DELETE: http://[kip]:31413/jsonp/v1/devices/[did]?procedure=light.delete&params={lightId:6}  
 + PUT:    http://[kip]:31413/jsonp/v1/devices/[did]?procedure=light.put&params={lightId:6,name="some light name",color:"00FF00"}  

+ Examples for Host-related profiles (Android terminal)
 + [Vibration-Profile](https://github.com/deviceconnect/DeviceConnect-JS/wiki/Vibration-Profile)  
http://[kip]:31413/jsonp/v1/devices/[did]?procedure=vibration.put&params={interface:"vibrate"}
 + [DeviceOrientation Profile](https://github.com/DeviceConnect/DeviceConnect-JS/wiki/DeviceOrientation-Profile)  
 http://[kip]:31413/jsonp/v1/devices/[did]?procedure=deviceorientation.get&params={"attribute":"ondeviceorientation"}
 + [Phone-Profile](https://github.com/deviceconnect/DeviceConnect-JS/wiki/Phone-Profile)  
  + Phone call  
http://[kip]:31413/jsonp/v1/devices/[did]?procedure=phone.post&params={interface:"call",phoneNumber:"08054137092"
 + [MediaStreamRecording-Profile](https://github.com/deviceconnect/DeviceConnect-JS/wiki/MediaStreamRecording-Profile)
  + Get available cameras  
http://[kip]:31413/jsonp/v1/devices/[did]?procedure=mediastream_recording.get&params={interface:"mediarecorder"}
  + Take photo  
http://[kip]:31413/jsonp/v1/devices/[did]?procedure=mediastream_recording.post&params={interface:"takephoto"}

Asynchronous API access is currently unsupported.

# How to update dconnect-sdk-for-android.jar

When [dConnect SDK for Android](https://github.com/DeviceConnect/DeviceConnect-Android/tree/master/dConnectSDK/dConnectSDKForAndroid) is updated,
you should make new **dconnect-sdk-for-android.jar**.
  
How to make dconnect-sdk-for-android.jar

1. Extract dconnect-sdk-for-android/build/outputs/aar/dconnect-sdk-for-android-debug.aar.
2. Rename classes.jar to dconnect-sdk-for-android.jar.
3. Move dconnect-sdk-for-android.jar to libraries/libs in this project.
