package com.kihon.android.apps.army_logout.fcm;

import com.google.firebase.iid.FirebaseInstanceId;

import com.kihon.android.apps.army_logout.settings.SettingsUtils;

/**
 * Created by kihon on 2016/06/22.
 */
public class FirebaseInstanceIdService extends com.google.firebase.iid.FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
//        Log.d(TAG, "Refreshed token: " + refreshedToken);

        SettingsUtils.setFcmToken(refreshedToken);
//        sendRegistrationToServer(refreshedToken);
    }

}
