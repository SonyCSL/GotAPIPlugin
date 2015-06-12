/*
KadecotSystemProfile
Copyright (c) 2015 Sony Computer Science Labs, Inc.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package com.sonycsl.Kadecot.plugin.gotapi.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import org.deviceconnect.android.event.EventManager;
import org.deviceconnect.android.message.MessageUtils;
import org.deviceconnect.android.profile.SystemProfile;
import org.deviceconnect.message.DConnectMessage;

/**
 * JUnit用テストデバイスプラグイン、Systemプロファイル.
 */
public class KadecotSystemProfile extends SystemProfile {

    /**
     * バージョン.
     */
    public static final String VERSION = "1.0";

    @Override
    protected boolean onPutWakeup(final Intent request, final Intent response, final String pluginId) {
        // /system/device/wakeupはテスト用デバイスプラグインでは疎通確認だけを行う.
        // 正常に設定画面が開かれることの確認は、実際のデバイスプラグインのテストで行う.
        setResult(response, DConnectMessage.RESULT_OK);
        return true;
    }

    @Override
    protected boolean onDeleteEvents(final Intent request, final Intent response, final String sessionKey) {
        boolean removed = EventManager.INSTANCE.removeEvents(sessionKey);
        if (removed) {
            setResult(response, DConnectMessage.RESULT_OK);
        } else {
            MessageUtils.setUnknownError(response, "Failed to remove events.");
        }
        return true;
    }

    @Override
    protected Class<? extends Activity> getSettingPageActivity(final Intent request, final Bundle param) {
        return null; // テスト用プラグインでは実装しない
    }
}
