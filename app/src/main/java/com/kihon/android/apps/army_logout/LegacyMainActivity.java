package com.kihon.android.apps.army_logout;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by kihon on 2016/06/27.
 */
public class LegacyMainActivity extends BaseAppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.welcome_user_text)
    TextView mWelcomeUserText;
    @BindView(R.id.welcome_user_text2)
    TextView mWelcomeUserText2;
    @BindView(R.id.login_date_btn)
    Button mLoginDateBtn;
    @BindView(R.id.service_day_spinner)
    Spinner mServiceDaySpinner;
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
    LinearLayout mNokoriBlock;
    @BindView(R.id.braak_month_text)
    TextView mBraakMonthText;
    @BindView(R.id.break_month_block)
    LinearLayout mBreakMonthBlock;
    @BindView(R.id.login_progressBar)
    RoundCornerProgressBar mLoginProgressBar;
    @BindView(R.id.login_percent)
    TextView mLoginPercent;
    @BindView(R.id.logout_information)
    LinearLayout mLogoutInformation;
    @BindView(R.id.container)
    NestedScrollView mContainer;
    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_main_legacy);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.welcome_user_text, R.id.login_date_btn, R.id.delete_day_button, R.id.login_progressBar})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.welcome_user_text:
                break;
            case R.id.login_date_btn:
                break;
            case R.id.delete_day_button:
                break;
            case R.id.login_progressBar:
                break;
        }
    }
}
