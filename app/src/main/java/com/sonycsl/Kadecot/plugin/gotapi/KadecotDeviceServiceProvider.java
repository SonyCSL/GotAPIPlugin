/*
KadecotDeviceServiceProvider
Copyright (c) 2015 Sony Computer Science Labs, Inc.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package com.sonycsl.Kadecot.plugin.gotapi;

import android.app.Service;

import org.deviceconnect.android.message.DConnectMessageServiceProvider;

/**
 * テスト用デバイスプラグインサービスプロバイダ.
 * 
 * @param <T> KadecotDeviceService
 */
public class KadecotDeviceServiceProvider<T extends Service> extends DConnectMessageServiceProvider<Service> {
    @SuppressWarnings("unchecked")
    @Override
    protected Class<Service> getServiceClass() {
        Class<? extends Service> clazz = (Class<? extends Service>) KadecotDeviceService.class;
        return (Class<Service>) clazz;
    }
}
