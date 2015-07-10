/*
KadecotServiceDiscoveryProfile
Copyright (c) 2015 Sony Computer Science Labs, Inc.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package com.sonycsl.Kadecot.plugin.gotapi.profile;

import android.content.Intent;
import android.os.Bundle;

import com.sonycsl.Kadecot.plugin.gotapi.GotAPIClient;

import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceDiscoveryProfile;
import org.deviceconnect.message.DConnectMessage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit用テストデバイスプラグイン、ServiceDiscoveryプロファイル.
 * @author NTT DOCOMO, INC.
 */
public class KadecotServiceDiscoveryProfile extends ServiceDiscoveryProfile {

    /**
     * コンストラクタ.
     * 
     * @param provider プロファイルプロバイダ
     */
    public KadecotServiceDiscoveryProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    /**
     * テスト用サービスID.
     */
    public static final String SERVICE_ID = "kadecot";

    /**
     * 特殊文字を含むテスト用サービスID.
     */
    public static final String SERVICE_ID_SPECIAL_CHARACTERS = "!#$'()-~¥@[;+:*],._/=?&%^|`\"{}<>";

    /**
     * テスト用デバイス名: {@value} .
     */
    public static final String DEVICE_NAME = "Kadecot";

    /**
     * テスト用デバイス名: {@value} .
     */
    public static final String DEVICE_NAME_SPECIAL_CHARACTERS = "Kadecot Service ID Special Characters";


    /**
     * テスト用オンライン状態.
     */
    public static final boolean DEVICE_ONLINE = true;

    /**
     * テスト用コンフィグ.
     */
    public static final String DEVICE_CONFIG = "kadecot config";

    /**
     * セッションキーが空の場合のエラーを作成する.
     * @param response レスポンスを格納するIntent
     */
    private void createEmptySessionKey(final Intent response) {
        MessageUtils.setInvalidRequestParameterError(response);
    }

    @Override
    protected boolean onGetServices(final Intent request, final Intent response) {
        List<Bundle> services = new ArrayList<Bundle>();

        // 典型的なサービス
        Bundle service = new Bundle();
        setId(service, SERVICE_ID);
        setName(service, DEVICE_NAME);
        setType(service, NetworkType.WIFI);
        setOnline(service, DEVICE_ONLINE);
        setConfig(service, DEVICE_CONFIG);
        setScopes(service, getProfileProvider());
        services.add(service);

        String urlstr = "http://localhost:31413/jsonp/v1/devices/" ;

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
            JSONObject devices = new JSONObject(str);
            JSONArray deviceList = devices.getJSONArray("deviceList");
            for(int i = 0; i < deviceList.length(); i++) {
                JSONObject device = deviceList.getJSONObject(i);
                String protocol = device.getString("protocol");
                if(GotAPIClient.PROTOCOL_NAME.equals(protocol)) {
                    continue;
                }
                String nickname = device.getString("nickname");
                int deviceId = device.getInt("deviceId");
                boolean status = device.getBoolean("status");
                Bundle s = new Bundle();
                setId(s, Integer.toString(deviceId));
                setName(s, nickname);
                setType(s, NetworkType.WIFI);
                setOnline(s, status);
                setConfig(s, DEVICE_CONFIG);
                setScopes(s, getProfileProvider());
                services.add(s);
            }
        } catch( Exception e){
            e.printStackTrace();
        }
/*
        // サービスIDが特殊なサービス
        service = new Bundle();
        setId(service, SERVICE_ID_SPECIAL_CHARACTERS);
        setName(service, DEVICE_NAME_SPECIAL_CHARACTERS);
        setType(service, DEVICE_TYPE);
        setOnline(service, DEVICE_ONLINE);
        setConfig(service, DEVICE_CONFIG);
        setScopes(service, getProfileProvider());
        services.add(service);*/

        setResult(response, DConnectMessage.RESULT_OK);
        setServices(response, services);
        
        return true;
    }

    @Override
    protected boolean onPutOnServiceChange(final Intent request, final Intent response,
                                            final String serviceId, final String sessionKey) {
        return false ;
        /*
        if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);

            Intent message = MessageUtils.createEventIntent();
            setSessionKey(message, sessionKey);
            setServiceID(message, serviceId);
            setProfile(message, getProfileName());
            setAttribute(message, ATTRIBUTE_ON_SERVICE_CHANGE);
            
            Bundle service = new Bundle();
            setId(service, SERVICE_ID);
            setName(service, DEVICE_NAME);
            setType(service, DEVICE_TYPE);
            setOnline(service, DEVICE_ONLINE);
            setConfig(service, DEVICE_CONFIG);
            
            setNetworkService(message, service);
            
            Util.sendBroadcast(getContext(), message);
        }
        
        return true;*/
    }

    @Override
    protected boolean onDeleteOnServiceChange(final Intent request, final Intent response,
                                                final String serviceId, final String sessionKey) {
        return false ;
        /*
        if (sessionKey == null) {
            createEmptySessionKey(response);
        } else {
            setResult(response, DConnectMessage.RESULT_OK);
        }
        return true;
        */
    }
}
