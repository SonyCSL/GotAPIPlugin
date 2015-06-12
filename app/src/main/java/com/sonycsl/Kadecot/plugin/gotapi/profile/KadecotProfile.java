/*
KadecotProfile
Copyright (c) 2015 Sony Computer Science Labs, Inc.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package com.sonycsl.Kadecot.plugin.gotapi.profile;

import android.content.Intent;
import org.deviceconnect.android.profile.DConnectProfile;
import org.deviceconnect.message.DConnectMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class KadecotProfile extends DConnectProfile {
    @Override
    public String getProfileName() {
        return "kadecot";
    }

    @Override
    protected boolean onGetRequest(final Intent request, final Intent response) {
        //String serviceId = getServiceID(request);

        String urlstr = "http://localhost:31413/jsonp/v1/devices/" ;
        String deviceId = request.getStringExtra("deviceId") ;
        try {
            int di = Integer.parseInt(deviceId) ;
        } catch (NumberFormatException e){
            deviceId = null ;
        }
        if( deviceId != null ){
            urlstr += deviceId ;
            String procedure = request.getStringExtra("procedure") ;
            String params = request.getStringExtra("params") ;
            if( procedure != null && params != null){
                urlstr += "?procedure="+procedure+"&params="+params ;
            }
        }

       try {
            URL url = new URL(urlstr);
            HttpURLConnection urlCon = (HttpURLConnection)url.openConnection();
            urlCon.setRequestMethod("GET");
            urlCon.connect();

            InputStream in = urlCon.getInputStream();

            String str ="";
            byte[] buf = new byte[1024];

            while(true) {
                int l = in.read(buf) ;
                if( l <= 0)break;
                str += new String(buf,0,l);
            }
           response.putExtra("Response", str);
        } catch( Exception e){
            response.putExtra("Error", e.toString());
        }
        response.putExtra("url",urlstr) ;

        setResult(response, DConnectMessage.RESULT_OK);
        getContext().sendBroadcast(response);

        return true;
    }
}
