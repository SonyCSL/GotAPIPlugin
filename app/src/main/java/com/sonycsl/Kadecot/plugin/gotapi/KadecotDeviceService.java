/*
 KadecotDeviceService.java
Copyright (c) 2015 Sony Computer Science Labs, Inc.
Released under the MIT license
http://opensource.org/licenses/mit-license.php

 */
package com.sonycsl.Kadecot.plugin.gotapi;

import android.content.Intent;
import android.os.Bundle;

import com.sonycsl.Kadecot.plugin.gotapi.profile.DummyLightProfile;
import com.sonycsl.Kadecot.plugin.gotapi.profile.KadecotProfile;
import com.sonycsl.Kadecot.plugin.gotapi.profile.KadecotServiceDiscoveryProfile;
import com.sonycsl.Kadecot.plugin.gotapi.profile.KadecotServiceInformationProfile;
import com.sonycsl.Kadecot.plugin.gotapi.profile.KadecotSystemProfile;

import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.event.cache.db.DBCacheController;
import org.deviceconnect.android.localoauth.LocalOAuth2Main;
import org.deviceconnect.android.message.DConnectMessageService;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.android.profile.ServiceInformationProfile;
import org.deviceconnect.android.profile.SystemProfile;

import java.util.Iterator;
import java.util.logging.Logger;


public class KadecotDeviceService extends DConnectMessageService {

    /** ロガー. */
    private Logger mLogger = Logger.getLogger("dconnect.dplugin.kadecot");

    @Override
    public void onCreate() {
        super.onCreate();
        EventManager.INSTANCE.setController(new DBCacheController(this));
        LocalOAuth2Main.initialize(getApplicationContext());
        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        addProfile(new KadecotProfile());
        addProfile(new DummyLightProfile());
    }

    @Override
    public int onStartCommand(final Intent intent, final int flags, final int startId) {
        mLogger.info("onStartCommand: intent:\n" + intent);
        if (intent != null) {
            mLogger.info("onStartCommand: extras=" + toString(intent.getExtras()));
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                KadecotDeviceService.super.onStartCommand(intent, flags, startId);
            }
        }).start();

        return START_STICKY;
    }

    /*
     * JSON文字列に変換する.
     * @param bundle Bundle
     * @return JSON String
     */
    private String toString(final Bundle bundle) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        for (Iterator<String> it = bundle.keySet().iterator(); it.hasNext();) {
            String key = it.next();
            sb.append(key + ":" + bundle.get(key));
            if (it.hasNext()) {
                sb.append(", ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    protected SystemProfile getSystemProfile() {
        return new KadecotSystemProfile();
    }

    @Override
    protected ServiceInformationProfile getServiceInformationProfile() {
        return new KadecotServiceInformationProfile(this);
    }

    @Override
    protected ServiceDiscoveryProfile getServiceDiscoveryProfile() {
        return new KadecotServiceDiscoveryProfile(this);
    }
}
