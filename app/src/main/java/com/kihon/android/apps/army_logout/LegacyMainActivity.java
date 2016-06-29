package com.kihon.android.apps.army_logout;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.common.base.MoreObjects;
import com.google.common.primitives.Ints;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.internal.MDButton;
import com.kihon.android.apps.army_logout.settings.SettingsUtils;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.joda.time.DateTime;

import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kihon on 2016/06/27.
 */
public class LegacyMainActivity extends BaseAppCompatActivity implements
        ColorChooserDialog.ColorCallback {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.welcome_user_text)
    TextView mWelcomeUserText;
    @BindView(R.id.welcome_user_text2)
    TextView mWelcomeUserText2;
    @BindView(R.id.login_date_btn)
    Button mLoginDateBtn;
    @BindView(R.id.service_day_spinner)
    NDSpinner mServiceDaySpinner;
    @BindView(R.id.delete_day_button)
    Button mDeleteDayButton;
    @BindView(R.id.textView4)
    TextView mTextView4;
    @BindView(R.id.logout_date_textview)
    TextView mLogoutDateTextview;
    @BindView(R.id.until_logout_title_text)
    TextView mUntilLogoutTitleText;
    @BindView(R.id.until_logout_days_text)
    TextView mUntilLogoutDaysText;
    @BindView(R.id.nokori_block)
    LinearLayout mCountdownBlock;
    @BindView(R.id.break_month_text)
    TextView mBreakMonthText;
    @BindView(R.id.break_month_block)
    LinearLayout mBreakMonthBlock;
    @BindView(R.id.login_progressBar)
    ProgressBar mLoginProgressBar;
    @BindView(R.id.login_percent)
    TextView mLoginPercent;
    @BindView(R.id.logout_information)
    LinearLayout mLogoutInformation;
    @BindView(R.id.container)
    NestedScrollView mContainer;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    private Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable mRunnable;
    private ServiceUtil mServiceUtil;
    private MilitaryInfo mMilitaryInfo;
    private Tracker mTracker = AppApplication.getInstance().getDefaultTracker();

    private ArrayAdapter mServiceDayAdapter;
    private List<String> mPeriodList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main_legacy);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mToolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.md_black_1000));

        mMilitaryInfo = MilitaryInfo.parse(SettingsUtils.getMilitaryInfo());

        mRunnable = new Runnable() {
            @Override
            public void run() {
                mServiceUtil = new ServiceUtil(mMilitaryInfo);

                mLoginDateBtn.setText(mServiceUtil.getLoginDateString());
                mLogoutDateTextview.setText(mServiceUtil.getRealLogoutDateString());
                mDeleteDayButton.setText(String.format(Locale.TAIWAN, "%d天", mServiceUtil.getDiscountDays()));

                if (mServiceUtil.isLoggedIn()) {
                    mUntilLogoutTitleText.setText("距離退伍還剩下");
                    mWelcomeUserText2.setText(String.format("你入伍已經%s天了嘿~ ", mServiceUtil.getPassedDay()));
                } else {
                    mBreakMonthBlock.setVisibility(View.GONE);
                    mUntilLogoutTitleText.setText("距離入伍還剩下");
                    mWelcomeUserText2.setText("準備好踏入陰間了嗎？");
                }

                mLoginProgressBar.setProgress((int) mServiceUtil.getPercentage());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mLoginProgressBar.setProgressTintList(ColorStateList.valueOf(SettingsUtils.getProgressBarColor()));
                } else {
                    mLoginProgressBar.getProgressDrawable().setColorFilter(SettingsUtils.getProgressBarColor(), PorterDuff.Mode.SRC_IN);
                }

                mLoginPercent.setText(String.format(Locale.TAIWAN, "%.2f%%", mServiceUtil
                        .getPercentage()));
                mUntilLogoutDaysText.setText(mServiceUtil.getRemainingDayWithString());

                if (mServiceUtil.isHundredDays()) {
                    mBreakMonthBlock.setVisibility(View.VISIBLE);
                    mBreakMonthText.setText(mServiceUtil.getUntilHundredDaysRemainingDaysWithString());
                } else {
                    mBreakMonthBlock.setVisibility(View.GONE);
                }

                if (mServiceUtil.getPercentage() >= 100.0f) {
                    mWelcomeUserText2.setText("學長(`・ω・́)ゝ 你已經成功返陽了!");
                    mUntilLogoutDaysText.setText("0天 00:00:00");
                }

                mWelcomeUserText.setText(SettingsUtils.getWelcomeText());

                mHandler.postDelayed(this, 500);
            }
        };

        initSpinnerList();
        initSpinner();
    }

    private void initSpinner() {
        mServiceDayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mPeriodList);
        mServiceDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mServiceDaySpinner.setAdapter(mServiceDayAdapter);
        mServiceDaySpinner.setDefaultSelection(mMilitaryInfo.getPeriod());
        mServiceDaySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                if (ServiceTime.CUSTOM == ServiceTime.values()[position]) {
                    MaterialDialog dialog = new MaterialDialog.Builder(LegacyMainActivity.this)
                            .title("自訂役期")
                            .customView(R.layout.dialog_custom_period, true)
                            .cancelListener(new DialogInterface.OnCancelListener() {
                                @Override
                                public void onCancel(DialogInterface dialog) {
                                    if (mMilitaryInfo.getPeriod() != ServiceTime.CUSTOM.ordinal()) {
                                        mServiceDaySpinner.setSelection(mMilitaryInfo.getPeriod());
                                    }
                                }
                            })
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
                                    mMilitaryInfo.setPeriod(position);
                                    mMilitaryInfo.setCustomPeriod(customPeriod);
                                    mTracker.send(new HitBuilders.EventBuilder()
                                            .setCategory("military_info")
                                            .setAction("period")
                                            .setLabel(ServiceTime.values()[position].name() + "_" + customPeriod)
                                            .build());

                                    mPeriodList.set(position, "自訂 - " + mMilitaryInfo.getCustomPeriod().toDisplayString());
                                    mServiceDayAdapter.notifyDataSetChanged();
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
                    mMilitaryInfo.setPeriod(position);
                    mTracker.send(new HitBuilders.EventBuilder()
                            .setCategory("military_info")
                            .setAction("period")
                            .setLabel(ServiceTime.values()[position].name())
                            .build());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void initSpinnerList() {
        mPeriodList = new ArrayList<>();
        for (int i = 0; i < ServiceTime.values().length; i++) {
            if (ServiceTime.values()[i] == ServiceTime.CUSTOM) {
                mPeriodList.add("自訂 - " + mMilitaryInfo.getCustomPeriod().toDisplayString());
            } else {
                mPeriodList.add(ServiceTime.values()[i].getDisplayText());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mHandler.post(mRunnable);
    }

    @Override
    protected void onPause() {
        mHandler.removeCallbacks(mRunnable);
        SettingsUtils.setMilitaryInfo(mMilitaryInfo.getJsonString());
        super.onPause();
    }

    @OnClick({R.id.welcome_user_text, R.id.login_date_btn, R.id.delete_day_button, R.id.login_progressBar, R.id.nokori_block})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.welcome_user_text:
                new MaterialDialog.Builder(this)
                        .title("換個字")
                        .input(null, null, false, new MaterialDialog.InputCallback() {
                            @Override
                            public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                                SettingsUtils.setWelcomeText(input.toString().trim());
                            }
                        })
                        .show();
                break;
            case R.id.login_date_btn:
                DateTime dateTime = new DateTime(mMilitaryInfo.getBegin());
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
                    }
                }, year, monthOfYear, dayOfMonth);
                dpd.show(getFragmentManager(), "Datepickerdialog");
                break;
            case R.id.delete_day_button:
                if (mMilitaryInfo.getDiscount() > 30) {
                    showInputDiscountDaysDialog();
                } else {
                    final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(this)
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
                    new MaterialDialog.Builder(this)
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
                                }
                            })
                            .show();
                }

                break;
            case R.id.login_progressBar:
                new ColorChooserDialog.Builder(this, R.string.color_palette)
                        .accentMode(false)
                        .preselect(SettingsUtils.getProgressBarColor())
                        .dynamicButtonColor(true)
                        .show();
                break;
            case R.id.nokori_block:
                mMilitaryInfo.switchPeriodType();
                mTracker.send(new HitBuilders.EventBuilder()
                        .setCategory(MainActivity.GA_EVENT_CATE_MAIN_LIST)
                        .setAction(MainActivity.GA_EVENT_ACTION_CHANGE)
                        .setLabel("period_type")
                        .build());
                break;
        }
    }

    private void showInputDiscountDaysDialog() {
        MaterialDialog customDiscountDialog = new MaterialDialog.Builder(this)
                .title("折抵天數")
                .inputType(InputType.TYPE_CLASS_PHONE | InputType.TYPE_CLASS_NUMBER)
                .input("請輸入折抵天數", mMilitaryInfo.getDiscount() > 30 ? String.valueOf(mMilitaryInfo.getDiscount()) : null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull MaterialDialog dialog, CharSequence input) {
                        Integer discount = MoreObjects.firstNonNull(Ints.tryParse(input.toString()), 0);
                        if (mServiceUtil.isIllegalDiscountValue(discount)) {
                            mMilitaryInfo.setDiscount(discount);
                        } else {
                            Toast.makeText(LegacyMainActivity.this, "似乎不需要服役...?", Toast.LENGTH_SHORT).show();
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
        customDiscountDialog.getInputEditText().setKeyListener(DigitsKeyListener.getInstance(false, false));
        customDiscountDialog.show();
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
}
