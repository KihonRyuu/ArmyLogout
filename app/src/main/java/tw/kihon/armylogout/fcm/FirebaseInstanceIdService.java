package tw.kihon.armylogout.fcm;

import com.google.firebase.iid.FirebaseInstanceId;

import tw.kihon.armylogout.settings.SettingsUtils;

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
