# GotAPIPlugin
GotAPI plugin for Kadecot

[GotAPI](https://device-webapi.org/) is an [open-source](https://github.com/DeviceConnect), Android-hosted device API standardized by OMA. This hybrid plugin provides [ECHONET Lite](http://www.echonet.gr.jp/) and other devices access to GotAPI server through Kadecot JSONP API.

# How to use
1. Install [Kadecot](https://play.google.com/store/apps/details?id=com.sonycsl.Kadecot) and GotAPI server (such as [Device Web API Manager by GClue.inc](https://play.google.com/store/apps/details?id=org.deviceconnect.android.manager) ) to your Android phone.
2. Install also app-debug.apk in this repository.
3. Boot Kadecot as usual, then you will find GotAPI icon within devices list, as well as other smart appliances supported by Kadecot. 

Then you are all set. GotAPI server should already recognize Kadecot and provide access to ECHONET Lite and other devices through "kadecot" profile. The arguments and reply structure is described in [Kadecot API page](http://kadecot.net/blog/2750/).

# How to access via JSONP

Obtain services suppoted by the system and plugins: 
http://[Kadecot IP]:31413/jsonp/v1/devices/1?procedure=system.get&params={}

Obtain services supported by each device:
http://[Kadecot IP]:31413/jsonp/v1/devices/11?procedure=serviceinformation.get&params={}

Operation examples for light service
GET:    http://[Kadecot IP]:31413/jsonp/v1/devices/11?procedure=light.get&params={}
POST:   http://[Kadecot IP]:31413/jsonp/v1/devices/11?procedure=light.post&params={lightId:6,color:%220000FF%22}
DELETE: http://[Kadecot IP]:31413/jsonp/v1/devices/11?procedure=light.delete&params={lightId:6}
PUT:    http://[Kadecot IP]:31413/jsonp/v1/devices/11?procedure=light.put&params={lightId:6,name=%22moe%22,color:%2200FF00%22}


# How to compile

You need to locate "DeviceConnect-Android" in the [GotAPI source repository](https://github.com/DeviceConnect/DeviceConnect-Android) at the same directory level as GotAPIPlugin.
 
# ToDo
- Currently, this plugin is an apk. However, we would like to link this to our official Kadecot release in future. A difficulty is in websocket library conflict
