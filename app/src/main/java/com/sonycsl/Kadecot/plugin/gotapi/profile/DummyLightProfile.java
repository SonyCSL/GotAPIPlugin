/*
DummyLightProfile
Copyright (c) 2015 Sony Computer Science Labs, Inc.
Released under the MIT license
http://opensource.org/licenses/mit-license.php
 */

package com.sonycsl.Kadecot.plugin.gotapi.profile;

import org.deviceconnect.android.profile.DConnectProfile;

public class DummyLightProfile extends DConnectProfile {
    @Override
    public String getProfileName() {
        return "light";
    }
}
