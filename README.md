# GotAPIPlugin
Kadecot/GotAPI hybrid plugin

[GotAPI](https://device-webapi.org/) is an [open-source](https://github.com/DeviceConnect), Android-hosted device API standardized by OMA. This hybrid plugin provides [ECHONET Lite](http://www.echonet.gr.jp/) and other devices access to GotAPI server through Kadecot JSONP API.

# How to use
1. Install [Kadecot](https://play.google.com/store/apps/details?id=com.sonycsl.Kadecot) and GotAPI server (such as [Device Web API Manager by GClue.inc](https://play.google.com/store/apps/details?id=org.deviceconnect.android.manager) ) to your Android phone.
2. Install also app-debug.apk in this repository.
3. Boot Kadecot as usual, then you will find GotAPI icon within devices list, as well as other smart appliances supported by Kadecot. 
4. In settings menu (located right-top of the main activity), check the topmost "Developer mode" box
5. Boot the GotAPI server. You will find GotAPIPlugin in "device plugins" menu.

Then you are all set. GotAPI server should already recognize Kadecot and provide access to ECHONET Lite and other devices through "kadecot" profile. The arguments and reply structure is described in [Kadecot API page](http://kadecot.net/blog/2750/).
A sample program is html/got.html. Open this file by Firefox (or other browser under a web server to enable ajax access) and input the correct IP address of the GotAPI server.

# How to compile

You need to locate "dConnectDevicePluginSDK/" and "dConnectSDK/" in the [GotAPI source repository](https://github.com/DeviceConnect/DeviceConnect-Android) at the same directory level as GotAPIPlugin.
 
# Warning
Kadecot JSONP API is open to any other peers within the same network. This may cause a security hazard.

# ToDo
- Autmated turning on the Kadecot developer mode (with simple confirmation)
- Currently, this plugin is an apk. However, we would like to link this to our official Kadecot release in future. It requires library output.
- This plugin adds kadecot functionality to GotAPI server, but not reverse. In future, we'd like to work this bidirectional.
- This plugin supports REST GET but not others. REST POST is a must.
