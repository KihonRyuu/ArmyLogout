package com.kihon.android.apps.army_logout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;
import com.kihon.android.apps.army_logout.settings.SettingsUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by kihon on 2016/06/29.
 */
public abstract class ArmyLogoutActivity extends BaseAppCompatActivity
        implements Runnable, ColorChooserDialog.ColorCallback {

    private Toolbar mToolbar;
    private ProgressBar mProgressBar;
    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Tracker mTracker = AppApplication.getInstance().getDefaultTracker();

    private MilitaryInfo mMilitaryInfo;
    private FirebaseAnalytics mFirebaseAnalytics;

    @LayoutRes
    abstract protected int getLayoutResource();

    public Toolbar getToolbar() {
        return mToolbar;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResource());
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (mToolbar != null) {
            setSupportActionBar(mToolbar);
//            assert getSupportActionBar() != null;
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            /*mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mNavigationClickListener != null)
                        mNavigationClickListener.onNavigationClick(v);
                }
            });*/

            if (getOnToolbarMenuItemClickListener() != null) {
                mToolbar.setOnMenuItemClickListener(getOnToolbarMenuItemClickListener());
            }

            if (findViewById(android.R.id.progress) != null) {
                mProgressBar = (ProgressBar) findViewById(android.R.id.progress);
            }
        }

        transLegacyPref();
        mFirebaseAnalytics = AppApplication.getInstance().getFirebaseAnalytics();
        FirebaseMessaging.getInstance().subscribeToTopic("global");
    }

    public Toolbar.OnMenuItemClickListener getOnToolbarMenuItemClickListener() {
        return null;
    }

    private void transLegacyPref() {
        String legacyLoginDate = getSharedPreferences(LegacyPref.LEGACY_PREF, Context.MODE_PRIVATE).getString(LegacyPref.PREF_LOGINDATE, "");
        if (!legacyLoginDate.isEmpty()) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
            long loginMillis = fmt.parseMillis(legacyLoginDate);

            SharedPreferences settings = getSharedPreferences(LegacyPref.LEGACY_PREF, Context.MODE_PRIVATE);
            int deleteDays = Integer.parseInt(settings.getString(LegacyPref.PREF_DELETEDAY, null));
            int serviceRange = settings.getInt(LegacyPref.PREF_SERVICERANGE, 0);

            ServiceTime serviceTime;
            switch (serviceRange) {
                case LegacyPref.RANGE_DEFAULT_ONE_YEAR:
                    serviceTime = ServiceTime.ONE_YEAR;
                    break;
                case LegacyPref.RANGE_FOUR_MONTH:
                    serviceTime = ServiceTime.FOUR_MONTHS;
                    break;
                case LegacyPref.RANGE_ONE_YEAR_FIFTEEN:
                    serviceTime = ServiceTime.ONE_YEAR_FIFTH_DAYS;
                    break;
                default:
                    serviceTime = ServiceTime.FOUR_YEARS;
                    break;
            }

            mMilitaryInfo = new MilitaryInfo(loginMillis, serviceTime, deleteDays, MilitaryInfo.DayTime);
            SettingsUtils.setMilitaryInfo(mMilitaryInfo.getJsonString());
            getSharedPreferences(LegacyPref.LEGACY_PREF, Context.MODE_PRIVATE)
                    .edit()
                    .clear()
                    .apply();
        } else {
            mMilitaryInfo = MilitaryInfo.parse(SettingsUtils.getMilitaryInfo());
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(MainActivity.GA_EVENT_CATE_MAIN_LIST)
                .setAction(MainActivity.GA_EVENT_ACTION_CHANGE)
                .setLabel("progressbar_color")
                .build());
        SettingsUtils.setProgressBarColor(selectedColor);
    }

    @Override
    public abstract void run();

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                PermissionListener listener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        captureScreen();
                        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SHARE, null);
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("feature")
                                .setAction("share")
                                .setLabel("granted")
                                .build());
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("feature")
                                .setAction("share")
                                .setLabel("denied")
                                .build());
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("feature")
                                .setAction("share")
                                .setLabel("continue")
                                .build());
                    }
                };

                ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                PermissionListener snackbarPermissionListener = SnackbarOnDeniedPermissionListener.Builder
                        .with(rootView, "需要存取空間的權限")
                        .withOpenSettingsButton("設定")
                        .build();

                Dexter.checkPermission(new CompositePermissionListener(listener, snackbarPermissionListener), android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return true;
            case R.id.action_about:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("menu")
                        .setAction("show")
                        .setLabel("about")
                        .build());

                StringBuilder content = new StringBuilder()
                        .append("版本:" + BuildConfig.VERSION_NAME)
                        .append("\r\n")
                        .append(getString(R.string.about_text));

                new MaterialDialog.Builder(this)
                        .title(R.string.about_app_name)
                        .content(content)
                        .positiveText(R.string.ok)
                        .neutralText("FB粉專")
                        .neutralColorRes(R.color.com_facebook_blue)
                        .onNeutral(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mFirebaseAnalytics.logEvent("about", null);
                                OpenFacebookPage();
                            }
                        })
                        .show();

                return true;
            case R.id.action_changelog:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("menu")
                        .setAction("show")
                        .setLabel("changelog")
                        .build());
                ChangeLog cl = new ChangeLog(this);
                cl.getFullLogDialog().show();
                return true;
            case R.id.action_change_widger_bgcolor:
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("menu")
                        .setAction("start_activity")
                        .setLabel("widget_setting")
                        .build());
                startActivity(new Intent(this, WidgetSettingsActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void captureScreen() {
        try {
            String timeStamp = DateTime.now().toString("yyyyMMdd_HHmmss");
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalCacheDir();
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            Uri outputFileUri = Uri.fromFile(imageFile);
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, "將截圖分享到"));

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
    }

    protected void OpenFacebookPage() {

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("button")
                .setAction("click")
                .setLabel("facebook_page")
                .build());

        String facebookPageID = "431938510221759";
        String facebookUrl = "https://www.facebook.com/" + facebookPageID;
        String facebookUrlScheme = "fb://page/" + facebookPageID;

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrlScheme)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
        }
    }

    public MilitaryInfo getMilitaryInfo() {
        return mMilitaryInfo;
    }

    public Handler getHandler() {
        return mHandler;
    }

    public Tracker getTracker() {
        return mTracker;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.post(this);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(this);
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("view")
                .setAction("mode")
                .setLabel(SettingsUtils.getViewMode() == SettingsUtils.VIEW_MODE_RECYCLER_VIEW ? "New" : "Legacy")
                .build());
        SettingsUtils.setMilitaryInfo(mMilitaryInfo.getJsonString());
        super.onPause();
    }

}
