package com.kihon.android.apps.army_logout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.messaging.FirebaseMessaging;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
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
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

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
        implements OnStartDragListener, ActionMode.Callback, ColorChooserDialog.ColorCallback,
        ItemClickSupport.OnItemClickListener {

    private static final String TAG = "MainActivity";

    static final String GA_EVENT_CATE_MAIN_LIST = "main";
    static final String GA_EVENT_ACTION_CHANGE = "change";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.menu_settings)
    FloatingActionButton mFab;
    @BindView(R.id.tip)
    MaterialTip mTip;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private ServiceUtil mServiceUtil;
    private InfoAdapter mInfoAdapter;
    private List<InfoItem> mData = new ArrayList<>();
    private MilitaryInfo mMilitaryInfo;
    private ItemTouchHelper mTouchHelper;
    private Runnable mRunnable;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ChainTourGuide mTourGuideHandler;
    private Sequence mSequence;
    private Tracker mTracker = AppApplication.getInstance().getDefaultTracker();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFirebaseAnalytics = AppApplication.getInstance().getFirebaseAnalytics();
        FirebaseMessaging.getInstance().subscribeToTopic("global");

        setSupportActionBar(mToolbar);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//        mRecyclerView.setItemAnimator(new FadeInAnimator(new OvershootInterpolator(1f)));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(this);

        mInfoAdapter = new InfoAdapter(MainActivity.this, mData, MainActivity.this);
        mRecyclerView.setAdapter(mInfoAdapter);
        ItemTouchHelper.Callback callback = new InfoItemTouchHelperCallback(mInfoAdapter);
        mTouchHelper = new ItemTouchHelper(callback);
        mTouchHelper.attachToRecyclerView(mRecyclerView);

        mRunnable = new Runnable() {
            @Override
            public void run() {
                mServiceUtil = new ServiceUtil(mMilitaryInfo);

                if (!mServiceUtil.isIllegalDiscountValue()) mMilitaryInfo.setDiscount(30);

                initDataSortByIndex();
                mInfoAdapter.setServiceUtil(mServiceUtil);
                mInfoAdapter.notifyDataSetChanged();

                if (SettingsUtils.isFirstRun()) {
                    SettingsUtils.firstRun();
                    if (!mServiceUtil.isHundredDays()) {
                        initTip();
                        mTip.show();
                    }
                }

                //END
                mHandler.postDelayed(this, 1000);
            }
        };

        ChangeLog cl = new ChangeLog(this);
//        if (cl.firstRun()) cl.getLogDialog().show();

        mTracker.setScreenName(GA_EVENT_CATE_MAIN_LIST);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    protected void onResume() {
        super.onResume();
        transLegacyPref();
        mHandler.post(mRunnable);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        SettingsUtils.setMilitaryInfo(mMilitaryInfo.getJsonString());
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private synchronized void initDataSortByIndex() {
        int[] indexes = SettingsUtils.getInfoItemIndexes();
        InfoItem[] items = InfoItem.values();
        for (int i = 0; i < indexes.length; i++) {
            items[i].setOrder(indexes[i]);
        }
        if (!mData.isEmpty()) mData.clear();
        Collections.addAll(mData, items);
        Collections.sort(mData, new Comparator<InfoItem>() {
            @Override
            public int compare(InfoItem lhs, InfoItem rhs) {
                return lhs.getOrder() > rhs.getOrder() ? 1 : -1;
            }
        });
        if (!mServiceUtil.isHundredDays()) mData.remove(InfoItem.HundredDays);
//        mInfoAdapter.notifyItemRangeInserted(0, InfoItem.values().length);
    }

    private synchronized void onItemClicked(int position, View v) {
        if (position < 0) return;
        switch (mData.get(position)) {
            case LoginDate:
                showLoginDatePicker(mMilitaryInfo.getBegin());
                break;
            case Period:
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                for (int i = 0; i < ServiceTime.values().length; i++) {
                    if (ServiceTime.values()[i] == ServiceTime.CUSTOM) {
                        popupMenu.getMenu().add(0, i, 0, "自訂 - " + mMilitaryInfo.getCustomPeriod().toDisplayString());
                    } else {
                        popupMenu.getMenu().add(0, i, 0, ServiceTime.values()[i].getDisplayText());
                    }
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(final MenuItem item) {
                        if (ServiceTime.CUSTOM == ServiceTime.values()[item.getItemId()]) {
                            MaterialDialog dialog = new MaterialDialog.Builder(MainActivity.this)
                                    .title("自訂役期")
                                    .customView(R.layout.dialog_custom_period, true)
                                    .positiveText(R.string.ok)
                                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                                        @Override
                                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                            MaterialEditText yearInput = (MaterialEditText) dialog.getCustomView().findViewById(R.id.year);
                                            MaterialEditText monthOfYearInput = (MaterialEditText) dialog.getCustomView().findViewById(R.id.monthOfYear);
                                            MaterialEditText dayOfMonthInput = (MaterialEditText) dialog.getCustomView().findViewById(R.id.dayOfMonth);

                                            int year = MoreObjects.firstNonNull(Ints.tryParse(yearInput.getText().toString().trim()), 0);
                                            int monthOfYear = MoreObjects.firstNonNull(Ints.tryParse(monthOfYearInput.getText().toString().trim()), 0);
                                            int dayOfMonth = MoreObjects.firstNonNull(Ints.tryParse(dayOfMonthInput.getText().toString().trim()), 0);

                                            CustomPeriod customPeriod = new CustomPeriod(year, monthOfYear, dayOfMonth);
                                            mMilitaryInfo.setPeriod(item.getItemId());
                                            mMilitaryInfo.setCustomPeriod(customPeriod);
                                            updateRecyclerView();
                                            mTracker.send(new HitBuilders.EventBuilder()
                                                    .setCategory("military_info")
                                                    .setAction("period")
                                                    .setLabel(ServiceTime.values()[item.getItemId()].name() + "_" + customPeriod)
                                                    .build());
                                            if (mTourGuideHandler != null) mTourGuideHandler.next();
                                        }
                                    }).build();

                            final MDButton positiveAction = dialog.getActionButton(DialogAction.POSITIVE);

                            final MaterialEditText yearInput = (MaterialEditText) dialog.getCustomView().findViewById(R.id.year);
                            final MaterialEditText monthOfYearInput = (MaterialEditText) dialog.getCustomView().findViewById(R.id.monthOfYear);
                            final MaterialEditText dayOfMonthInput = (MaterialEditText) dialog.getCustomView().findViewById(R.id.dayOfMonth);

                            yearInput.setText(String.valueOf(mMilitaryInfo.getCustomPeriod().getYear()));
                            monthOfYearInput.setText(String.valueOf(mMilitaryInfo.getCustomPeriod().getMonthOfYear()));
                            dayOfMonthInput.setText(String.valueOf(mMilitaryInfo.getCustomPeriod().getDayOfMonth()));

                            TextWatcher textWatcher = new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    int year = MoreObjects.firstNonNull(Ints.tryParse(yearInput.getText().toString().trim()), 0);
                                    int monthOfYear = MoreObjects.firstNonNull(Ints.tryParse(monthOfYearInput.getText().toString().trim()), 0);
                                    int dayOfMonth = MoreObjects.firstNonNull(Ints.tryParse(dayOfMonthInput.getText().toString().trim()), 0);
                                    positiveAction.setEnabled(!(year == 0 & monthOfYear == 0 & dayOfMonth == 0));
                                }

                                @Override
                                public void afterTextChanged(Editable s) {
                                }
                            };

                            yearInput.addTextChangedListener(textWatcher);
                            monthOfYearInput.addTextChangedListener(textWatcher);
                            dayOfMonthInput.addTextChangedListener(textWatcher);

                            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            dialog.show();
                            positiveAction.setEnabled(mMilitaryInfo.getCustomPeriod() != null);
                        } else {
                            mMilitaryInfo.setPeriod(item.getItemId());
                            updateRecyclerView();
                            mTracker.send(new HitBuilders.EventBuilder()
                                    .setCategory("military_info")
                                    .setAction("period")
                                    .setLabel(ServiceTime.values()[item.getItemId()].name())
                                    .build());
                            if (mTourGuideHandler != null) mTourGuideHandler.next();
                        }
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
                updateRecyclerView();
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

    private void updateRecyclerView() {
        mHandler.removeCallbacks(mRunnable);
        mHandler.post(mRunnable);
    }

    private void showLoginDatePicker(long instant) {
        DateTime dateTime = new DateTime(instant);
        int year = dateTime.getYear();
        int monthOfYear = dateTime.getMonthOfYear() - 1;
        int dayOfMonth = dateTime.getDayOfMonth();

        DatePickerDialog dpd = DatePickerDialog.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
                DateTime dateTime1 = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);
                mMilitaryInfo.setBegin(dateTime1.getMillis());
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory("military_info")
                        .setAction("login_date")
                        .setLabel(new DateTime(mServiceUtil.getStartTimeInMillis()).toString())
                        .build());
                if (mTourGuideHandler != null) mTourGuideHandler.next();
            }
        }, year, monthOfYear, dayOfMonth);
        dpd.show(getFragmentManager(), "Datepickerdialog");
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
        assert customDiscountDialog.getInputEditText() != null;
        customDiscountDialog.getInputEditText()
                .setKeyListener(DigitsKeyListener.getInstance(false, false));
        customDiscountDialog.show();
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

                        ChainTourGuide tourGuide1 = ChainTourGuide
                                .init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setDescription("設定你的入伍日期")
                                        .setEnterAnimation(animation)
                                        .setShadow(true))
                                .playLater(mRecyclerView.getChildAt(mInfoAdapter.findItem(InfoItem.LoginDate)));

                        ChainTourGuide tourGuide2 = ChainTourGuide
                                .init(MainActivity.this)
                                .setToolTip(new ToolTip().setDescription("選擇您的役期")
//                                        .setBackgroundColor(Color.parseColor("#c0392b"))
                                        .setEnterAnimation(animation).setShadow(true))
                                .playLater(mRecyclerView.getChildAt(mInfoAdapter.findItem(InfoItem.Period)));

                        ChainTourGuide tourGuide3 = ChainTourGuide
                                .init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setTitle("役期折抵")
                                        .setDescription("選擇天數或手動輸入")
                                        .setEnterAnimation(animation)
                                        .setShadow(true))
                                .playLater(mRecyclerView.getChildAt(mInfoAdapter.findItem(InfoItem.Discount)));

                        ChainTourGuide tourGuide4 = ChainTourGuide
                                .init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setGravity(Gravity.TOP)
                                        .setTitle("切換顯示單位")
                                        .setDescription("點擊後可以在「總天數」與「年月天」之間做切換")
                                        .setEnterAnimation(animation)
                                        .setShadow(true))
                                .playLater(mRecyclerView.getChildAt(mInfoAdapter.findItem(InfoItem.CounterTimer)));

                        ChainTourGuide tourGuide5 = ChainTourGuide
                                .init(MainActivity.this)
                                .setToolTip(new ToolTip()
                                        .setGravity(Gravity.TOP)
                                        .setShadow(true)
                                        .setDescription("進度條的顏色可以變更")
                                        .setEnterAnimation(animation))
                                .playLater(mRecyclerView.getChildAt(mInfoAdapter.findItem(InfoItem.CounterProgressbar)));

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
                                        .setOnClickListener(new OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (mSequence.getTourGuideArray().length == mSequence.mCurrentSequence) {
                                                    mTourGuideHandler.cleanUp();
                                                    mTourGuideHandler = null;
//                                                    Snackbar.make(findViewById(android.R.id.content),"")
                                                } else if (mTourGuideHandler != null) {
                                                    mTourGuideHandler.next();
                                                }
                                            }
                                        }))
                                .setDefaultPointer(new Pointer())
                                .setContinueMethod(Sequence.ContinueMethod.OverlayListener)
                                .build();

                        mTourGuideHandler = ChainTourGuide
                                .init(MainActivity.this)
                                .playInSequence(mSequence);
                    }

                    @Override
                    public void onNegative(MaterialTip tip) {
                    }

                });
        mTip.hide();
    }

    @Override
    public void onBackPressed() {
//        System.out.println(mSequence.getTourGuideArray());
        if (mTourGuideHandler != null) {
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
            mInfoAdapter.notifyItemRangeRemoved(0, mData.size());
            mData.clear();
            SettingsUtils.setInfoItemIndexes(Ints.toArray(ContiguousSet.create(Range.closedOpen(0, InfoItem
                    .values().length), DiscreteDomain.integers())));
            initDataSortByIndex();
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
            case R.id.action_legacy_mode:
                startActivity(new Intent(this, LegacyMainActivity.class));
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

            Log.d(TAG, "captureScreen: " + imageFile.toString());

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

    @Override
    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
        onItemClicked(position, v);
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
                        ((ItemViewHolder) holder).title.setText(mServiceUtil.isLoggedIn() ? item.getTitle() : item
                                .getTitle()
                                .replace("退", "入"));
                        ((ItemViewHolder) holder).subtitle.setText(mServiceUtil.getRemainingDayWithString());
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
                ObjectAnimator animation = ObjectAnimator.ofFloat(((ProgressbarViewHolder) holder).progressBar, "progress", mServiceUtil
                        .getPercentage());
                animation.setDuration(300);
                animation.setInterpolator(new DecelerateInterpolator());
                animation.start();
//                ((ProgressbarViewHolder) holder).progressBar.setProgress(mServiceUtil.getPercentage());
                ((ProgressbarViewHolder) holder).percent.setText(String.format(Locale.TAIWAN, "%.2f%%", mServiceUtil
                        .getPercentage()));
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
            SettingsUtils.setInfoItemIndexes(swap(SettingsUtils.getInfoItemIndexes(), InfoItem
                    .valueOf(mData.get(fromPosition).name())
                    .ordinal(), InfoItem.valueOf(mData.get(toPosition).name()).ordinal()));
            AppApplication.getInstance().getFirebaseAnalytics().logEvent("change_list_order", null);
            AppApplication
                    .getInstance()
                    .getDefaultTracker()
                    .send(new HitBuilders.EventBuilder()
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
            notifyItemRangeChanged(0,mData.size());
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

        public int findItem(InfoItem item) {
            for (int i = 0; i < mData.size(); i++) {
                if (mData.get(i) == item) {
                    return i;
                }
            }
            return -1;
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
                ViewCompat
                        .animate(itemView)
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
                ViewCompat
                        .animate(itemView)
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
                    mHandler.removeCallbacks(mRunnable);
                    break;
                case ItemTouchHelper.ACTION_STATE_IDLE:
                    updateRecyclerView();
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
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            mAdapter.onItemMove(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            mAdapter.onItemDismiss(viewHolder.getAdapterPosition());
        }
    }

}
