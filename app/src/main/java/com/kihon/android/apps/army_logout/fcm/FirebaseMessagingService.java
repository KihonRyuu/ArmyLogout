package com.kihon.android.apps.army_logout.fcm;

import com.google.firebase.messaging.RemoteMessage;

import com.kihon.android.apps.army_logout.settings.SettingsUtils;

import java.util.Map;

/**
 * Created by kihon on 2016/06/22.
 */
public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "FirebaseMessagingService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
//        Log.d(TAG, "From: " + remoteMessage.getFrom());
//        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        Map<String, String> remoteMessageData = remoteMessage.getData();
        if (!remoteMessageData.isEmpty()) {
            if (Integer.valueOf(remoteMessageData.get("action")) == 1) {
                SettingsUtils.feedback();
            }
        }
    }
}
