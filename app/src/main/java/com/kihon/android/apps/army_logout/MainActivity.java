package com.kihon.android.apps.army_logout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.facebook.Session;
import com.github.OrangeGangsters.circularbarpager.library.CircularBarPager;
import com.github.fcannizzaro.materialtip.MaterialTip;
import com.github.fcannizzaro.materialtip.util.ButtonListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.karumi.dexter.listener.single.SnackbarOnDeniedPermissionListener;
import com.kihon.android.apps.army_logout.settings.SettingsUtils;
import com.viewpagerindicator.CirclePageIndicator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import at.grabner.circleprogress.AnimationState;
import at.grabner.circleprogress.AnimationStateChangedListener;
import at.grabner.circleprogress.CircleProgressView;
import at.grabner.circleprogress.TextMode;
import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.recyclerview.animators.holder.AnimateViewHolder;
import tourguide.tourguide.ChainTourGuide;
import tourguide.tourguide.Overlay;
import tourguide.tourguide.Pointer;
import tourguide.tourguide.Sequence;
import tourguide.tourguide.ToolTip;

public class MainActivity extends BaseAppCompatActivity
        implements OnStartDragListener, ActionMode.Callback,ColorChooserDialog.ColorCallback {

    private static final String TAG = "MainActivity";

    private static final String GA_EVENT_CATE_MAIN_LIST = "main";
    private static final String GA_EVENT_ACTION_CHANGE = "change";

    private static final int BAR_ANIMATION_TIME = 1000;

    @BindView(R.id.until_logout_days_text)
    TextView mTvUntilLogoutDays;
    @BindView(R.id.until_logout_title_text)
    TextView mTvUntilLogoutTitle;
    @BindView(R.id.welcome_user_text)
    TextView mTvUsername;
    @BindView(R.id.welcome_user_text2)
    TextView mTvStatus;
    @BindView(R.id.login_date_btn)
    Button mBtnLoginDate;
    @BindView(R.id.progressBar_connect_facebook)
    ProgressBar mProgressBarFbConnect;
    @BindView(R.id.delete_day_button)
    Button mDeleteDayButton;
    @BindView(R.id.service_day_spinner)
    Spinner mSpinnerServiceDay;
    @BindView(R.id.logout_date_textview)
    TextView mTvLogoutDate;
    @BindView(R.id.break_month_block)
    LinearLayout mBreakMonthBlock;
    @BindView(R.id.braak_month_text)
    TextView mBreakMonthTextView;
    @BindView(R.id.circularBarPager)
    CircularBarPager mCircularBarPager;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.circleView)
    CircleProgressView mCircleView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.menu_settings)
    FloatingActionButton mFab;
    @BindView(R.id.tip)
    MaterialTip mTip;

    private Handler mRefreshInformationHandler = new Handler();
    private ArrayList<String> mServiceRangeArrayList = new ArrayList<>();
    private ArrayAdapter<String> mServiceDayAdapter;
    private ServiceUtil mServiceUtil;
    private DemoView[] mDemoViews;
    private InfoAdapter mInfoAdapter;
    private List<InfoItem> mData;
    private MilitaryInfo mMilitaryInfo;
    private ItemTouchHelper mTouchHelper;
    private Runnable mRefreshInformationRunnable = new Runnable() {

        @Override
        public void run() {

            mServiceUtil = new ServiceUtil(mMilitaryInfo);

            if (!mServiceUtil.isIllegalDiscountValue()) mMilitaryInfo.setDiscount(30);

            if (mServiceUtil.isLoggedIn()) {
                mTvUntilLogoutTitle.setText("距離退伍還剩下");
                mTvStatus.setText("你入伍已經" + mServiceUtil.getPassedDay() + "天了嘿~ ");
            } else {
                mBreakMonthBlock.setVisibility(View.GONE);
                mTvUntilLogoutTitle.setText("距離入伍還剩下");
                mTvStatus.setText("準備好踏入陰間了嗎？");
            }

            mTvUntilLogoutDays.setText(mServiceUtil.getRemainingDayWithString());
//            mProgressBarLogin.setProgress((int) mServiceUtil.getPercentage());

            if (mServiceUtil.isHundredDays()) {
                mBreakMonthBlock.setVisibility(View.VISIBLE);
                mBreakMonthTextView.setText(mServiceUtil.getUntilHundredDaysRemainingDaysWithString());
            } else {
                mBreakMonthBlock.setVisibility(View.GONE);
            }

            if (mServiceUtil.getPercentage() >= 100.0f) {
                mTvStatus.setText("學長(`・ω・́)ゝ 你已經成功返陽了!");
                mTvUntilLogoutDays.setText("0天 00:00:00");
            }

            if (mInfoAdapter == null) {
                mData = new ArrayList<>();
                mInfoAdapter = new InfoAdapter(MainActivity.this, mData, MainActivity.this);
                mRecyclerView.setAdapter(mInfoAdapter);
                ItemTouchHelper.Callback callback = new InfoItemTouchHelperCallback(mInfoAdapter);
                mTouchHelper = new ItemTouchHelper(callback);
                mTouchHelper.attachToRecyclerView(mRecyclerView);
            } else {
                mData.clear();
                mInfoAdapter.notifyDataSetChanged();
            }

            mInfoAdapter.setServiceUtil(mServiceUtil);

            int[] indexes = SettingsUtils.getInfoItemIndexes();
            if (indexes == null) {
                Collections.addAll(mData, InfoItem.values());
            } else {
                for (int index : indexes) {
                    mData.add(InfoItem.values()[index]);
                }
            }
            if (!mServiceUtil.isHundredDays()) mData.remove(InfoItem.HundredDays);
            mInfoAdapter.notifyItemRangeInserted(0, InfoItem.values().length);

            //END
            mRefreshInformationHandler.postDelayed(mRefreshInformationRunnable, 500);
        }

    };
    private CirclePageIndicator mCirclePageIndicator;
    private ViewPager mViewPager;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ChainTourGuide mTourGuideHandler;
    private Sequence mSequence;
    private Menu mMenu;
    private Tracker mTracker = AppApplication.getInstance().getDefaultTracker();

    private void onSettingsSelected(int position, View v) {
        switch (mData.get(position)) {
            case LoginDate:
                DateTime dateTime = new DateTime(mMilitaryInfo.getBegin());

                int year = dateTime.getYear();
                int monthOfYear = dateTime.getMonthOfYear() - 1;
                int dayOfMonth = dateTime.getDayOfMonth();

                DatePickerDialog loginDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                        Log.d(TAG, year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                        mMilitaryInfo.setBegin(new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0).getMillis());
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("military_info")
                                .setAction("login_date")
                                .setLabel(new DateTime(mServiceUtil.getStartTimeInMillis()).toString())
                                .build());
                        if (mTourGuideHandler != null) mTourGuideHandler.next();
                    }
                }, year, monthOfYear, dayOfMonth);
                loginDatePickerDialog.show();
                break;
            case Period:
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                for (int i = 0; i < ServiceTime.values().length; i++) {
                    popupMenu.getMenu().add(0, i, 0, ServiceTime.values()[i].getDisplayText());
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        mMilitaryInfo.setPeriod(item.getItemId());
                        mRefreshInformationHandler.post(mRefreshInformationRunnable);
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("military_info")
                                .setAction("period")
                                .setLabel(ServiceTime.values()[item.getItemId()].name())
                                .build());
                        if (mTourGuideHandler != null) mTourGuideHandler.next();
                        return false;
                    }
                });
                popupMenu.show();
                break;
            case LogoutDate:
                break;
            case Discount:
                if (mMilitaryInfo.getDiscount() > 30) {
                    showInputDiscountDaysDialog();
                } else {
                    final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(MainActivity.this)
                            .minValue(0)
                            .maxValue(30)
                            .defaultValue(mMilitaryInfo.getDiscount())
                            .backgroundColor(Color.WHITE)
                            .separatorColor(Color.TRANSPARENT)
                            .textColor(Color.BLACK)
                            .textSize(20)
                            .enableFocusability(false)
                            .wrapSelectorWheel(true)
                            .build();
                    new MaterialDialog.Builder(MainActivity.this)
                            .customView(numberPicker, false)
                            .title("折抵天數")
                            .neutralText("軍校折抵")
                            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    showInputDiscountDaysDialog();
                                }
                            })
                            .positiveText(android.R.string.ok)
                            .onPositive(new MaterialDialog.SingleButtonCallback() {
                                @Override
                                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                    mDeleteDayButton.setText(String.valueOf(numberPicker.getValue()));
                                    mMilitaryInfo.setDiscount(numberPicker.getValue());
                                    mTracker.send(new HitBuilders.EventBuilder()
                                            .setCategory("military_info")
                                            .setAction("discount")
                                            .setLabel(String.valueOf(numberPicker.getValue()))
                                            .build());
                                    if (mTourGuideHandler != null) mTourGuideHandler.next();
                                }
                            })
                            .show();
                }
                break;
            case CounterTimer:
                mMilitaryInfo.switchPeriodType();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(GA_EVENT_CATE_MAIN_LIST)
                        .setAction(GA_EVENT_ACTION_CHANGE)
                        .setLabel("period_type")
                        .build());
                break;
            case CounterProgressbar:
                new ColorChooserDialog.Builder(this, R.string.color_palette)
                        .accentMode(false)
                        .preselect(SettingsUtils.getProgressBarColor())
                        .dynamicButtonColor(true)
                        .show();
                if (mTourGuideHandler != null) mTourGuideHandler.next();
                break;
            default:
                break;
        }
    }

    private void showInputDiscountDaysDialog() {
        MaterialDialog customDiscountDialog = new MaterialDialog.Builder(MainActivity.this)
                .title("折抵天數")
                .inputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_CLASS_NUMBER)
                .input("請輸入折抵天數", mMilitaryInfo.getDiscount() > 30 ? String.valueOf(mMilitaryInfo.getDiscount()) : null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Integer discount = MoreObjects.firstNonNull(Ints.tryParse(input.toString()), 0);
                        if (mServiceUtil.isIllegalDiscountValue(discount)) {
                            mMilitaryInfo.setDiscount(discount);
                            mDeleteDayButton.setText(String.valueOf(mMilitaryInfo.getDiscount()));
                            if (mTourGuideHandler != null) mTourGuideHandler.next();
                        } else {
                            Toast.makeText(MainActivity.this, "似乎不需要服役...?", Toast.LENGTH_SHORT).show();
                        }
                        mTracker.send(new HitBuilders.EventBuilder()
                                .setCategory("military_info")
                                .setAction("discount")
                                .setLabel(String.valueOf(discount))
                                .build());
                    }
                })
                .positiveText(android.R.string.ok)
                .build();
        customDiscountDialog.getInputEditText().setKeyListener(DigitsKeyListener.getInstance(false, false));
        customDiscountDialog.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFirebaseAnalytics = AppApplication.getInstance().getFirebaseAnalytics();
        FirebaseMessaging.getInstance().subscribeToTopic("global");
        System.out.println(FirebaseInstanceId.getInstance().getToken());

        setSupportActionBar(mToolbar);
        mBreakMonthBlock.setVisibility(View.GONE);

        transLegacyPref();
//        initCircleView();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//        mRecyclerView.setItemAnimator(new FadeInAnimator(new OvershootInterpolator(1f)));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        onSettingsSelected(position, v);
                    }
                }
        );

        mFab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        ChangeLog cl = new ChangeLog(this);
//        if (cl.firstRun()) cl.getLogDialog().show();

        if (SettingsUtils.isFirstRun()) {
            SettingsUtils.firstRun();
            initTip();
        }

        mTracker.setScreenName(GA_EVENT_CATE_MAIN_LIST);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
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
            getSharedPreferences(LegacyPref.LEGACY_PREF, Context.MODE_PRIVATE).edit().clear().apply();
        } else {
            mMilitaryInfo = MilitaryInfo.parse(SettingsUtils.getMilitaryInfo());
        }
    }

    private void initTip() {
        mTip.withText("Yo!\n需要幫您介紹一下有哪些功能嗎?")
                .withPositive("觀看導覽")
                .withNegative("謝謝")
                .withIconRes(R.drawable.ic_timer)
                .withButtonListener(new ButtonListener() {

                    @Override
                    public void onPositive(MaterialTip tip) {
                        Animation animation = new AlphaAnimation(0f, 1f);
                        animation.setDuration(300);
                        animation.setInterpolator(new DecelerateInterpolator());

                        /* setup enter and exit animation */
                        Animation enterAnimation = new AlphaAnimation(0f, 1f);
                        enterAnimation.setDuration(300);
                        enterAnimation.setInterpolator(new DecelerateInterpolator());
                        enterAnimation.setFillAfter(true);

                        Animation exitAnimation = new AlphaAnimation(1f, 0f);
                        exitAnimation.setDuration(300);
                        exitAnimation.setInterpolator(new DecelerateInterpolator());
                        exitAnimation.setFillAfter(true);


                        ChainTourGuide tourGuide1 = ChainTourGuide.init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setDescription("設定你的入伍日期")
                                        .setEnterAnimation(animation)
                                        .setShadow(true)
                                )
                                .playLater(mRecyclerView.getChildAt(0));

                        ChainTourGuide tourGuide2 = ChainTourGuide.init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setDescription("選擇您的役期")
//                                        .setBackgroundColor(Color.parseColor("#c0392b"))
                                        .setEnterAnimation(animation)
                                        .setShadow(true)
                                )
                                .playLater(mRecyclerView.getChildAt(1));

                        ChainTourGuide tourGuide3 = ChainTourGuide.init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setTitle("役期折抵")
                                        .setDescription("選擇天數或手動輸入")
                                        .setEnterAnimation(animation)
                                        .setShadow(true)
                                )
                                .playLater(mRecyclerView.getChildAt(3));

                        ChainTourGuide tourGuide4 = ChainTourGuide.init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setGravity(Gravity.TOP)
                                        .setTitle("切換顯示單位")
                                        .setDescription("點擊後可以在「總天數」與「年月天」之間做切換")
                                        .setEnterAnimation(animation)
                                        .setShadow(true)
                                )
                                .playLater(mRecyclerView.getChildAt(4));

                        ChainTourGuide tourGuide5 = ChainTourGuide.init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setGravity(Gravity.TOP)
                                        .setShadow(true)
                                        .setDescription("進度條的顏色可以變更")
                                        .setEnterAnimation(animation)
                                )
                                .playLater(mRecyclerView.getChildAt(5));

                        /*ChainTourGuide tourGuide6 = ChainTourGuide.init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setShadow(true)
                                        .setDescription("自訂主畫面的排序")
                                        .setEnterAnimation(animation)
                                )
                                .setOverlay(new Overlay())
                                .playLater(findViewById(R.id.action_reorder));*/


                        mSequence = new Sequence.SequenceBuilder()
                                .add(tourGuide1, tourGuide2, tourGuide3, tourGuide4, tourGuide5)
                                .setDefaultOverlay(new Overlay()
//                                        .setEnterAnimation(enterAnimation)
//                                        .setExitAnimation(exitAnimation)
                                        .setStyle(Overlay.Style.Rectangle)
                                        .setOnClickListener(new OnClickListener(){
                                            @Override
                                            public void onClick(View v) {
                                                if (mSequence.getTourGuideArray().length == mSequence.mCurrentSequence) {
                                                    mTourGuideHandler.cleanUp();
                                                    mTourGuideHandler = null;
//                                                    Snackbar.make(findViewById(android.R.id.content),"")
                                                } else if (mTourGuideHandler != null){
                                                    mTourGuideHandler.next();
                                                }
                                            }
                                        })
                                )
                                .setDefaultPointer(new Pointer())
                                .setContinueMethod(Sequence.ContinueMethod.OverlayListener)
                                .build();

                        mTourGuideHandler = ChainTourGuide.init(MainActivity.this).playInSequence(mSequence);
                    }

                    @Override
                    public void onNegative(MaterialTip tip) {
                    }

                });

        mTip.show();
    }

    @Override
    public void onBackPressed() {
//        System.out.println(mSequence.getTourGuideArray());
        if (mTourGuideHandler != null){
            mTourGuideHandler.cleanUp();
            mTourGuideHandler = null;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mTouchHelper.startDrag(viewHolder);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.reorder, menu);
       /* MenuItem saveItem = menu.add("Restore Default").setIcon(R.drawable.ic_settings_backup_restore_white_24dp);
        MenuItemCompat.setShowAsAction(saveItem, MenuItemCompat.SHOW_AS_ACTION_IF_ROOM);*/
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        mode.setTitle("排序");
        mInfoAdapter.onReorderMode(true);
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if (item.getItemId() == R.id.action_restore_default_order) {
            mData.clear();
            SettingsUtils.setInfoItemIndexes(Ints.toArray(ContiguousSet.create(Range.closedOpen(0, InfoItem.values().length), DiscreteDomain.integers())));
            int[] indexes = SettingsUtils.getInfoItemIndexes();
            mInfoAdapter.notifyItemRangeRemoved(0, indexes.length);
            for (int i = 0; i < indexes.length; i++) {
                mData.add(InfoItem.values()[indexes[i]]);
            }
            mInfoAdapter.notifyItemRangeInserted(0, indexes.length);
            mFirebaseAnalytics.logEvent("reset_list_order", null);
        } else {
            mode.finish();
        }
        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mInfoAdapter.onReorderMode(false);
    }

    private void selectItem(int position) {
        switch (position) {
            case 0:
                mCircularBarPager.getCircularBar().animateProgress(0, (int) mServiceUtil.getPercentage(), BAR_ANIMATION_TIME);
                break;
            case 1:
                mCircularBarPager.getCircularBar().animateProgress(0, (int) mServiceUtil.getPercentage(), BAR_ANIMATION_TIME);
//                mCircularBarPager.getCircularBar().animateProgress(100, -75, BAR_ANIMATION_TIME);
//                mDemoViews[1].mValueInfoTextview.setText(mCountTimeText);
                break;
            default:
                mCircularBarPager.getCircularBar().animateProgress(0, (int) mServiceUtil.getPercentage(), BAR_ANIMATION_TIME);
                break;
        }
    }

    private void initCircleView() {
        mCircleView.setOnProgressChangedListener(new CircleProgressView.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float value) {
                Log.d(TAG, "Progress Changed: " + value);
            }
        });
        mCircleView.setShowTextWhileSpinning(true); // Show/hide text in spinning mode
        mCircleView.setText("Loading...");
        mCircleView.setOnAnimationStateChangedListener(
                new AnimationStateChangedListener() {
                    @Override
                    public void onAnimationStateChanged(AnimationState _animationState) {
                        switch (_animationState) {
                            case IDLE:
                            case ANIMATING:
                            case START_ANIMATING_AFTER_SPINNING:
                                mCircleView.setTextMode(TextMode.PERCENT); // show percent if not spinning
                                mCircleView.setUnitVisible(true);
                                break;
                            case SPINNING:
                                mCircleView.setTextMode(TextMode.TEXT); // show text while spinning
                                mCircleView.setUnitVisible(false);
                            case END_SPINNING:
                                break;
                            case END_SPINNING_START_ANIMATING:
                                break;

                        }
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRefreshInformationHandler.post(mRefreshInformationRunnable);
//        new LongOperation().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRefreshInformationHandler.removeCallbacks(mRefreshInformationRunnable);
        SettingsUtils.setMilitaryInfo(mMilitaryInfo.getJsonString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =  getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        mMenu = menu;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(this, requestCode, resultCode, data);
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
                PermissionListener snackbarPermissionListener =
                        SnackbarOnDeniedPermissionListener.Builder
                                .with(rootView, "需要存取空間的權限")
                                .withOpenSettingsButton("設定")
                                .build();

                Dexter.checkPermission(new CompositePermissionListener(listener, snackbarPermissionListener), Manifest.permission.WRITE_EXTERNAL_STORAGE);
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
            case R.id.action_reorder:
                startSupportActionMode(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void captureScreen() {
        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/PICTURES/Screenshots/" + DateTime.now().toString("yyyy-MM-dd_hh:mm:ss") + ".jpg";

            // create bitmap screen capture
            View v1 = getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

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

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, @ColorInt int selectedColor) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(GA_EVENT_CATE_MAIN_LIST)
                .setAction(GA_EVENT_ACTION_CHANGE)
                .setLabel("progressbar_color")
                .build());
        mFirebaseAnalytics.logEvent("change_progressbar_color", null);
        SettingsUtils.setProgressBarColor(selectedColor);
    }

    public enum ServiceTime {
        ONE_YEAR("一年"), FOUR_YEARS("四年"), ONE_YEAR_FIFTH_DAYS("一年十五天"), FOUR_MONTHS("四個月"),
        FOUR_MONTHS_FIVE_DAYS("四個月五天"), SIX_MONTHS("六個月"), THREE_YEARS("三年"), ONE_YEAR_SIX_MONTHS("一年六個月"), TEN_MONTHS("十個月");
//        ,CUSTOM("自訂");

        private final String mDisplayText;

        ServiceTime(String displayText) {
            mDisplayText = displayText;
        }

        public String getDisplayText() {
            return mDisplayText;
        }
    }

    public static class InfoAdapter extends RecyclerView.Adapter<InfoAdapter.BaseItemAnimateViewHolder> implements ItemTouchHelperAdapter, ItemTouchHelperViewHolder {

        private LayoutInflater mLayoutInflater;
        private List<InfoItem> mData;
        private OnStartDragListener mDragStartListener;
        private Context mContext;
        private ServiceUtil mServiceUtil;
        private boolean isReorder;

        // Allows to remember the last item shown on screen
        private int lastPosition = -1;

        public InfoAdapter(Context context, List<InfoItem> data, OnStartDragListener dragStartListener) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mData = data;
            mDragStartListener = dragStartListener;
        }

        /**
         * Swaps {@code array[i]} with {@code array[j]}.
         */
        static int[] swap(int[] array, int i, int j) {
            int temp = array[i];
            array[i] = array[j];
            array[j] = temp;
            return array;
        }

        @Override
        public int getItemViewType(int position) {
            return mData.get(position) == InfoItem.CounterProgressbar ? 1 : 0;
        }

        @Override
        public InfoAdapter.BaseItemAnimateViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0)
                return new ItemViewHolder(mLayoutInflater.inflate(R.layout.list_item_main_two_line, parent, false));
            else
                return new ProgressbarViewHolder(mLayoutInflater.inflate(R.layout.list_item_main_progress_bar, parent, false));
        }

        @Override
        public void onBindViewHolder(final InfoAdapter.BaseItemAnimateViewHolder holder, final int position) {
            InfoItem item = mData.get(position);
            if (holder instanceof ItemViewHolder) {
                ((ItemViewHolder) holder).icon.setImageResource(item.getImageRes());
                ((ItemViewHolder) holder).title.setText(item.getTitle());
                switch (item) {
                    case LoginDate:
                        ((ItemViewHolder) holder).subtitle.setText(mServiceUtil.getLoginDateString());
                        break;
                    case Period:
                        ((ItemViewHolder) holder).subtitle.setText(mServiceUtil.getServiceTime().getDisplayText());
                        break;
                    case LogoutDate:
                        ((ItemViewHolder) holder).subtitle.setText(mServiceUtil.getRealLogoutDateString());
                        break;
                    case Discount:
                        ((ItemViewHolder) holder).subtitle.setText(String.format(Locale.TAIWAN, "%d天", mServiceUtil.getDiscountDays()));
                        break;
                    case HundredDays:
                        ((ItemViewHolder) holder).subtitle.setText(mServiceUtil.getUntilHundredDaysRemainingDaysWithString());
                        break;
                    case CounterTimer:
                        ((ItemViewHolder) holder).title.setText(mServiceUtil.isLoggedIn() ? item.getTitle() : item.getTitle().replace("退", "入"));
                        ((ItemViewHolder) holder).subtitle.setText(mServiceUtil.getRemainingDayWithString());
                        break;
                    case CounterProgressbar:
                        break;
                }
                ((ItemViewHolder) holder).handleView.setVisibility(isReorder ? View.VISIBLE : View.GONE);
                ((ItemViewHolder) holder).handleView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                            mDragStartListener.onStartDrag(holder);
                        }
                        return false;
                    }
                });
            } else if (holder instanceof ProgressbarViewHolder) {
                ((ProgressbarViewHolder) holder).icon.setImageResource(item.getImageRes());
                ((ProgressbarViewHolder) holder).title.setText(item.getTitle());
                ((ProgressbarViewHolder) holder).progressBar.setProgressColor(SettingsUtils.getProgressBarColor());
                ObjectAnimator animation = ObjectAnimator.ofFloat(((ProgressbarViewHolder) holder).progressBar, "progress", mServiceUtil.getPercentage());
                animation.setDuration(300);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
//                ((ProgressbarViewHolder) holder).progressBar.setProgress(mServiceUtil.getPercentage());
                ((ProgressbarViewHolder) holder).percent.setText(String.format(Locale.TAIWAN, "%.2f%%", mServiceUtil.getPercentage()));
                ((ProgressbarViewHolder) holder).handleView.setVisibility(isReorder ? View.VISIBLE : View.GONE);
                ((ProgressbarViewHolder) holder).handleView.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                            mDragStartListener.onStartDrag(holder);
                        }
                        return false;
                    }
                });
            }

            holder.handleView.setVisibility(isReorder ? View.VISIBLE : View.GONE);
            holder.handleView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (MotionEventCompat.getActionMasked(event) == MotionEvent.ACTION_DOWN) {
                        mDragStartListener.onStartDrag(holder);
                    }
                    return false;
                }
            });
//            setAnimation(holder.itemView, position);
        }

        /**
         * Here is the key method to apply the animation
         */
        private void setAnimation(View viewToAnimate, int position) {
            // If the bound view wasn't previously displayed on screen, it's animated
            if (position > lastPosition) {
                Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in);
                viewToAnimate.startAnimation(animation);
                lastPosition = position;
            }
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }

        @Override
        public void onItemMove(int fromPosition, int toPosition) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(mData, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(mData, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
            if (SettingsUtils.getInfoItemIndexes() == null) {
                SettingsUtils.setInfoItemIndexes(Ints.toArray(ContiguousSet.create(Range.closedOpen(0, getItemCount()), DiscreteDomain.integers())));
            }
            SettingsUtils.setInfoItemIndexes(swap(SettingsUtils.getInfoItemIndexes(), fromPosition, toPosition));
            AppApplication.getInstance().getFirebaseAnalytics().logEvent("change_list_order", null);
            AppApplication.getInstance().getDefaultTracker().send(new HitBuilders.EventBuilder()
                    .setCategory(GA_EVENT_CATE_MAIN_LIST)
                    .setAction(GA_EVENT_ACTION_CHANGE)
                    .setLabel("order")
                    .build());
        }

        @Override
        public void onItemDismiss(int position) {

        }

        public void onReorderMode(boolean value) {
            isReorder = value;
//            notifyDataSetChanged();
        }

        @Override
        public void onItemSelected() {

        }

        @Override
        public void onItemClear() {

        }

        public void setServiceUtil(ServiceUtil serviceUtil) {
            mServiceUtil = serviceUtil;
        }

        class BaseItemAnimateViewHolder extends AnimateViewHolder {

            @BindView(R.id.handle)
            ImageView handleView;

            public BaseItemAnimateViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }

            @Override
            public void animateAddImpl(ViewPropertyAnimatorListener listener) {
                ViewCompat.animate(itemView)
                        .translationY(0)
                        .alpha(1)
                        .setDuration(300)
                        .setListener(listener)
                        .start();
            }

            @Override
            public void preAnimateAddImpl() {
                ViewCompat.setTranslationY(itemView, -itemView.getHeight() * 0.3f);
                ViewCompat.setAlpha(itemView, 0);
            }

            @Override
            public void animateRemoveImpl(ViewPropertyAnimatorListener listener) {
                ViewCompat.animate(itemView)
                        .translationY(-itemView.getHeight() * 0.3f)
                        .alpha(0)
                        .setDuration(300)
                        .setListener(listener)
                        .start();
            }
        }

        class ItemViewHolder extends BaseItemAnimateViewHolder {

            @BindView(R.id.imageView)
            ImageView icon;
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.subtitle)
            TextView subtitle;

            public ItemViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }

        class ProgressbarViewHolder extends BaseItemAnimateViewHolder {

            @BindView(R.id.imageView)
            ImageView icon;
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.login_progressBar)
            RoundCornerProgressBar progressBar;
            @BindView(R.id.login_percent)
            TextView percent;

            public ProgressbarViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
    }

    private static class ViewPagerAdapter extends PagerAdapter {

        private final Context mContext;
        private final DemoView[] mViews;

        public ViewPagerAdapter(Context context, DemoView... views) {
            mContext = context;
            mViews = views;
        }

        @Override
        public int getCount() {
            return mViews.length;
        }

        @Override
        public Object instantiateItem(ViewGroup collection, int position) {
            DemoView currentView = mViews[position];
            switch (position) {
                case 0:
                    currentView.mUserTopTextview.setText("退伍令下載進度");
                    break;
                case 1:
                    currentView.mUserTopTextview.setText("距離退伍還剩下");
                    break;
            }
            collection.addView(currentView);
            return currentView;
        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView((View) view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }

    public static class DemoView extends LinearLayout {
        /**
         * TAG for logging
         */
        private static final String TAG = "HomeUserView";

        @BindView(R.id.user_top_textview)
        TextView mUserTopTextview;
        @BindView(R.id.value_info_textview)
        TextView mValueInfoTextview;
        @BindView(R.id.user_bottom_textview)
        TextView mUserBottomTextview;
        @BindView(R.id.home_info_main_layout)
        LinearLayout mHomeInfoMainLayout;

        public DemoView(Context context) {
            super(context);
            initView();
        }

        private void initView() {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.view_user_info, this);
            ButterKnife.bind(this, view);
        }

    }

    public class InfoItemTouchHelperCallback extends ItemTouchHelper.Callback {

        private final ItemTouchHelperAdapter mAdapter;

        public InfoItemTouchHelperCallback(ItemTouchHelperAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return false;
        }

        @Override
        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
            switch (actionState) {
                case ItemTouchHelper.ACTION_STATE_DRAG:
                    mRefreshInformationHandler.removeCallbacks(mRefreshInformationRunnable);
                    break;
                case ItemTouchHelper.ACTION_STATE_IDLE:
                    mRefreshInformationHandler.post(mRefreshInformationRunnable);
                    if (viewHolder instanceof ItemTouchHelperViewHolder) {
                        ItemTouchHelperViewHolder itemViewHolder = (ItemTouchHelperViewHolder) viewHolder;
                        itemViewHolder.onItemSelected();
                    }
                    break;
            }

            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            int swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
            return makeMovementFlags(dragFlags, swipeFlags);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder,
                              RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

    private class LongOperation extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {

            MainActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mCircleView.setValue(0);
                    mCircleView.spin();
                }
            });

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mCircleView.setValueAnimated(mServiceUtil.getPercentage());
//            mCircleView.stopSpinning();
        }
    }

}
