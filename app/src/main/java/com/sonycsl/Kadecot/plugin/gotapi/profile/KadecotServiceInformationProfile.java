/*
KadecotServiceInformationProfile
Copyright (c) 2015 Sony Computer Science Labs, Inc.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */
package com.sonycsl.Kadecot.plugin.gotapi.profile;

import org.deviceconnect.android.profile.DConnectProfileProvider;
import org.deviceconnect.android.profile.ServiceInformationProfile;

/**
 * JUnit用テストデバイスプラグイン、Service Informationプロファイル.
 */
public class KadecotServiceInformationProfile extends ServiceInformationProfile {

    /**
     * コンストラクタ.
     * @param provider プロファイルプロバイダ
     */
    public KadecotServiceInformationProfile(final DConnectProfileProvider provider) {
        super(provider);
    }

    @Override
    protected ConnectState getWifiState(final String serviceId) {
        return ConnectState.OFF;
    }

    @Override
    protected ConnectState getBluetoothState(final String serviceId) {
        return ConnectState.OFF;
    }

    @Override
    protected ConnectState getNFCState(final String serviceId) {
        return ConnectState.OFF;
    }

    @Override
    protected ConnectState getBLEState(final String serviceId) {
        return ConnectState.OFF;
    }
}
