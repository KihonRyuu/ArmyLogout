package tw.kihon.armylogout;

import com.google.android.gms.analytics.HitBuilders;

import com.afollestad.materialdialogs.MaterialDialog;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

import org.joda.time.DateTime;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sephiroth.android.library.tooltip.Tooltip;
import tw.kihon.armylogout.settings.SettingsUtils;

/**
 * Created by kihon on 2016/09/09.
 */
public class PhotoShareActivity extends tw.kihon.armylogout.ArmyLogoutActivity {

    private static final String TAG = "PhotoShareActivity";

    @BindView(R.id.imageView)
    SubsamplingScaleImageView mScaleImageView;
    @BindView(R.id.place_textview)
    TextView mLocationText;
    @BindView(R.id.shareto_logout_date_inf)
    LinearLayout mSharetoLogoutDateInf;
    @BindView(R.id.textView_year)
    TextView mCurrentYear;
    @BindView(R.id.textView_today)
    TextView mCurrentDay;
    @BindView(R.id.share_to_logout_text)
    TextView mText1;
    @BindView(R.id.login_progressBar)
    ProgressBar mLoginProgressBar;
    @BindView(R.id.login_Percent)
    TextView mLoginPercent;
    @BindView(R.id.share_inf)
    LinearLayout mINF;
    @BindView(R.id.frameLayout)
    FrameLayout mPostScreen;

    private tw.kihon.armylogout.ServiceUtil mServiceUtil;
    private Menu mMenu;
    private MaterialDialog mDialog;

    @Override
    protected int getLayoutResource() {
        return R.layout.activity_shareto;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            *//*WindowManager.LayoutParams attrs = getWindow().getAttributes();
            attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            getWindow().setAttributes(attrs);*//*

        }*/

       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            View bottom = findViewById(R.id.appbar);
            bottom.setBackground(
                    ScrimUtil.makeCubicGradientScrimDrawable(
                            ContextCompat.getColor(this, R.color.md_grey_800), 8, Gravity.TOP));
        }*/

        Drawable background = mINF.getBackground();
        background.setAlpha(40);

        loadImage((Uri) getIntent().getParcelableExtra("Uri"));
    }

    private void loadImage(Uri uri) {
        mScaleImageView.setImage(ImageSource.uri(uri));
        mScaleImageView.setOrientation(SubsamplingScaleImageView.ORIENTATION_USE_EXIF);
        mScaleImageView.setOnImageEventListener(new SubsamplingScaleImageView.OnImageEventListener() {
            @Override
            public void onReady() {
                mMenu.findItem(R.id.action_share).setEnabled(false);
            }

            @Override
            public void onImageLoaded() {
                mMenu.findItem(R.id.action_share).setEnabled(true);
                Tooltip.make(PhotoShareActivity.this,
                        new Tooltip.Builder()
                                .anchor(mPostScreen, Tooltip.Gravity.CENTER)
                                .closePolicy(new Tooltip.ClosePolicy()
                                        .insidePolicy(true, false)
                                        .outsidePolicy(true, false), 3000)
                                .activateDelay(800)
                                .showDelay(300)
                                .text("可縮放移動")
                                .maxWidth(500)
                                .withArrow(true)
                                .withOverlay(true)
                                .floatingAnimation(Tooltip.AnimationBuilder.DEFAULT)
                                .build()
                ).show();
            }

            @Override
            public void onPreviewLoadError(Exception e) {

            }

            @Override
            public void onImageLoadError(Exception e) {

            }

            @Override
            public void onTileLoadError(Exception e) {

            }
        });
    }

    @Override
    public void run() {
        mServiceUtil = new tw.kihon.armylogout.ServiceUtil(getMilitaryInfo());

        mText1.setText(mServiceUtil.isLoggedIn() ? "天之後就脫離陰間正式退伍了!" : "天後就要進入陰間服役了!!");

        if (mServiceUtil.getRemainingYearDays().contains(" ")) {
            mCurrentYear.setVisibility(View.VISIBLE);
            mCurrentYear.setText(mServiceUtil.getRemainingYearDays().split(" ")[0]);
            mCurrentDay.setText(mServiceUtil.getRemainingYearDays().split(" ")[1]);
        } else {
            mCurrentYear.setVisibility(View.GONE);
            mCurrentDay.setText(mServiceUtil.getRemainingYearDays());
        }

        mLocationText.setText("");
        mLoginPercent.setText(String.format(Locale.TAIWAN, "%.2f%%", mServiceUtil.getPercentage()));
        mLoginProgressBar.setProgress((int) mServiceUtil.getPercentage());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mLoginProgressBar.setProgressTintList(ColorStateList.valueOf(SettingsUtils.getProgressBarColor()));
        } else {
            mLoginProgressBar.getProgressDrawable().setColorFilter(SettingsUtils.getProgressBarColor(), PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.share_photo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                photoShare();
                return true;
            case R.id.action_take_photo:
                try {
                    takePic();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_pick_photo:
                pickPic();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void photoShare() {
        try {
            String timeStamp = DateTime.now().toString("yyyyMMdd_HHmmss");
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalCacheDir();
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            mPostScreen.setDrawingCacheEnabled(true);
            mPostScreen.setDrawingCacheBackgroundColor(Color.WHITE);
            Bitmap bitmap = Bitmap.createBitmap(mPostScreen.getDrawingCache());
            mPostScreen.setDrawingCacheEnabled(false);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();

            Uri outputFileUri = Uri.fromFile(imageFile);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent, "分享至"));

            AppApplication.getInstance().getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory("feature")
                    .setAction("share")
                    .setLabel("photo")
                    .build());

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        Uri uri;
        switch (requestCode) {
            case REQUEST_SELECT_PHOTO:
                uri = data.getData();
                break;
            case REQUEST_TAKE_PHOTO:
                uri = getFileUri();
                break;
            default:
                return;
        }
        loadImage(uri);
    }

}
