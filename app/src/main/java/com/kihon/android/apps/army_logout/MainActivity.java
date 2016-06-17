package com.kihon.android.apps.army_logout;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;
import com.github.OrangeGangsters.circularbarpager.library.CircularBarPager;
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
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
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

public class MainActivity extends AppCompatActivity implements OnStartDragListener, ActionMode.Callback {

    public static final String PREF = "ARMY_LOGOUT_PREF";
    public static final String PREF_LOGINDATE = "ARMY_LOGOUT_LoginDate";
    public static final String PREF_SERVICEDAY = "ARMY_LOGOUT_ServiceDay";
    public static final String PREF_DELETEDAY = "ARMY_LOGOUT_DeleteDay";
    public static final String PREF_CONNECT_FB = "ARMY_LOGOUT_ConnectFB";
    //FIXME 待修正
    public static final String PREF_LOGINMILLIS = "ARMY_LOGOUT_LoginMillis";
    public static final String PREF_LOGOUTMILLIS = "ARMY_LOGOUT_LogoutMillis";
    public static final String PREF_SERVICERANGE = "ARMY_LOGOUT_ServiceTime";
    public static final String PREF_USERNAME = "ARMY_LOGOUT_Username";
    public static final String PREF_WIDGETBGCOLOR = "ARMY_LOGOUT_WidgetBackgroundColor";
    public static final String PREF_WIDGETTITLECOLOR = "ARMY_LOGOUT_WidgetTitleColor";
    public static final String PREF_WIDGETSUBTITLECOLOR = "ARMY_LOGOUT_WidgetSubtitleColor";
    public static final String PREF_CUSTOM_SERVICERANGE_YEAR = "ARMY_LOGOUT_CustomYear";
    public static final String PREF_CUSTOM_SERVICERANGE_MONTH = "ARMY_LOGOUT_CustomMonth";
    public static final String PREF_CUSTOM_SERVICERANGE_DAY = "ARMY_LOGOUT_CustomDay";
    public final static int RANGE_DEFAULT_ONE_YEAR = 0;
    public final static int RANGE_FOUR_MONTH = 1;
    public final static int RANGE_ONE_YEAR_FIFTEEN = 2;
    public final static int RANGE_CUSTOM = 3;
    static final int ID_SCREENDIALOG = 1;
    private static final String TAG = "MainActivity";
    private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
    private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
    private final static Calendar mLoginDateCalendar = Calendar.getInstance();
    /**
     * The animation time in milliseconds that we take to display the steps taken
     */
    private static final int BAR_ANIMATION_TIME = 1000;
    public static boolean NETWORK_CONNECTED = false;
    //private int total_day = 365;
    protected static float sLoginPercent;
    protected static int sLogoutYear = 0;
    protected static int sLogoutDay = 0;
    static boolean login_yet = false;
    private static Calendar mLogoutDateCalendar = Calendar.getInstance();
    private static String sLoginDate = null;
    //	private static String LOGOUT_TIME = null;
    private static String sDeleteDays = null;
    private static String USER_FB_NAME = "弟兄";
    private static int sIntServiceRange = 0;
    private static boolean service_year = true;
    /**
     * 自訂役期
     */

    private static String[] sCustomServiceRangeArray = new String[]{"4", "0", "0"};
    protected String mFacebookCountTimeText;
    @BindView(R.id.until_logout_days_text)
    TextView mTvUntilLogoutDays;
    @BindView(R.id.until_logout_title_text)
    TextView mTvUntilLogoutTitle;
    //	public static int LOGIN_PASS_DAY = 0;
    @BindView(R.id.welcome_user_text)
    TextView mTvUsername;
    @BindView(R.id.welcome_user_text2)
    TextView mTvStatus;
    @BindView(R.id.login_date_btn)
    Button mBtnLoginDate;
    /*    @BindView(R.id.login_progressBar)
        ProgressBar mProgressBarLogin;*/
    @BindView(R.id.progressBar_connect_facebook)
    ProgressBar mProgressBarFbConnect;
    /*    @BindView(R.id.login_Percent)
        TextView mTvLoginPercent;*/
    //    private static int sWidgetColors = -1;
//	@BindView(R.id.delete_day_edittext)
//	EditText mEtDeleteDay;
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
    /**
     *
     */

    Bitmap bmScreen;
    RelativeLayout mLayout;
    Dialog screenDialog;
    ImageView bmImage;
    Button btnScreenDialog_OK;
    // TextView TextOut;

    View screen;
    EditText EditTextIn;
    /**
     *
     */
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.circleView)
    CircleProgressView mCircleView;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.menu_settings)
    FloatingActionButton mFab;
    private Button shareButton;
    private boolean pendingPublishReauthorization = false;
    private Handler mRefreshInformationHandler = new Handler();
    private ProgressDialog mProgressDialog;
    private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("TAIWAN"));
    private byte[] photo_data = null;
    /**
     * DrawerLayout
     *
     * @date 2013-11-19
     * @author kihon
     */

    private ViewGroup mContainerView;
    private Handler mCheckNetWorkStatusHandler = new Handler();
    private boolean mBeforeHoneycomb = true;
    private boolean mCustomServiceRange = false;
    private ArrayList<String> mServiceRangeArrayList = new ArrayList<>();
    private DialogFragment mCustomRangeDialog = new CustomRangeDialogFragment();
    private String mCustomServiceRangeText = null;
    private ArrayAdapter<String> mServiceDayAdapter;
    //    private int mDeleteDay;
    private String mCountTimeText;
    private Runnable mCheckNetWorkStatusRunnable = new Runnable() {

        @Override
        public void run() {
            NETWORK_CONNECTED = checkNetwork();
        }
    };
    private ServiceUtil mServiceUtil;
    private DemoView[] mDemoViews;
    private InfoAdapter mInfoAdapter;
    private List<InfoItem> mData;
    private MilitaryInfo mMilitaryInfo;
    private ItemTouchHelper mTouchHelper;
    //    private ServiceTime mServiceTime = ServiceTime.ONE_YEAR;
    private Runnable mRefreshInformationRunnable = new Runnable() {

        @Override
        public void run() {

            mServiceUtil = new ServiceUtil(mMilitaryInfo);

            if (mServiceUtil.isLoggedIn()) {
                mTvUntilLogoutTitle.setText("距離退伍還剩下");
                mTvStatus.setText("你入伍已經" + mServiceUtil.getPassedDay() + "天了嘿~ ");
            } else {
                mBreakMonthBlock.setVisibility(View.GONE);
                mTvUntilLogoutTitle.setText("距離入伍還剩下");
                mTvStatus.setText("準備好踏入陰間了嗎？");
            }

            for (int i = 0; i < mDemoViews.length; i++) {
                TextView valueInfoTextView = mDemoViews[i].mValueInfoTextview;
                TextView userBottomTextView = mDemoViews[i].mUserBottomTextview;
                switch (i) {
                    case 0:
                        userBottomTextView.setVisibility(View.GONE);
                        String valueInfoText = new DecimalFormat("#.##").format(mServiceUtil.getPercentage());
                        if (mServiceUtil.getPercentage() >= 100.0f) {
                            valueInfoTextView.setText("100%");
                        } else if (mServiceUtil.getPercentage() <= 0.0f) {
                            valueInfoTextView.setText("0%");
                        } else {
                            valueInfoTextView.setText(String.format("%s%%", valueInfoText));
                        }
                        break;
                    case 1:
                        userBottomTextView.setVisibility(View.GONE);
                        PeriodFormatter formatter = new PeriodFormatterBuilder()
                                .printZeroAlways().appendDays().appendSuffix("天")
                                .toFormatter();
                        valueInfoTextView.setText(mServiceUtil.getRemainingDayWithString(formatter));
                        formatter = new PeriodFormatterBuilder()
                                .printZeroAlways().minimumPrintedDigits(2).appendHours().appendSeparator(":")
                                .printZeroAlways().minimumPrintedDigits(2).appendMinutes().appendSeparator(":")
                                .printZeroAlways().minimumPrintedDigits(2).appendSeconds()
                                .toFormatter();
                        userBottomTextView.setText(mServiceUtil.getRemainingDayWithString(formatter));
                        break;
                }
            }

            /*for (UpdateCallbacks callbacks : mDemoViews) {
                callbacks.onUpdate(mServiceUtil.getPercentage());
            }*/

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
                mRecyclerView.getItemAnimator().setAddDuration(5000);
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
            mInfoAdapter.notifyItemRangeInserted(0, InfoItem.values().length);

            //END
            mRefreshInformationHandler.postDelayed(mRefreshInformationRunnable, 500);
        }

    };
    private CirclePageIndicator mCirclePageIndicator;
    private ViewPager mViewPager;

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
                        Log.d(TAG, year + "年" + monthOfYear + "月" + dayOfMonth + "日");
                        mMilitaryInfo.setBegin(new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0).getMillis());
                    }
                }, year, monthOfYear, dayOfMonth);
                loginDatePickerDialog.show();
                break;
            case Period:
//                Context wrapper = new ContextThemeWrapper(v.getContext(), v.getContext().getTheme());
                PopupMenu popupMenu = new PopupMenu(MainActivity.this, v);
                for (int i = 0; i < ServiceTime.values().length; i++) {
                    popupMenu.getMenu().add(0, i, 0, ServiceTime.values()[i].getDisplayText());
                }
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                       /* if (item.getItemId() == ServiceTime.values().length - 1) {
                            mCustomRangeDialog.show(getFragmentManager(), "custom_range_dialog");
                        } else {
                            sIntServiceRange = item.getItemId();
                            loadUserData();
                        }*/
                        mMilitaryInfo.setPeriod(item.getItemId());
//                        sIntServiceRange = item.getItemId();
//                        loadUserData();
                        return false;
                    }
                });
                popupMenu.show();
                break;
            case LogoutDate:
                break;
            case Discount:
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
                        .positiveText(android.R.string.ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                mDeleteDayButton.setText(String.valueOf(numberPicker.getValue()));
                                mMilitaryInfo.setDiscount(numberPicker.getValue());
                            }
                        })
                        .dismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialog) {
                                mDeleteDayButton.setText(String.valueOf(numberPicker.getValue()));
                                mMilitaryInfo.setDiscount(numberPicker.getValue());
                            }
                        })
                        .show();
                break;
            case CounterTimer:
                break;
            case CounterProgressbar:
                break;
            default:
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mBreakMonthBlock.setVisibility(View.GONE);

        /**
         * Data Trans Legacy
         */
        String legacyLoginDate = getSharedPreferences(PREF, Context.MODE_PRIVATE).getString(PREF_LOGINDATE, "");
        if (!legacyLoginDate.isEmpty()) {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("yyyy-MM-dd");
            long loginMillis = fmt.parseMillis(legacyLoginDate);

            SharedPreferences settings = getSharedPreferences(PREF, Context.MODE_PRIVATE);
            int deleteDays = Integer.parseInt(settings.getString(PREF_DELETEDAY, null));
            int serviceRange = settings.getInt(PREF_SERVICERANGE, 0);

            ServiceTime serviceTime;
            switch (serviceRange) {
                case RANGE_DEFAULT_ONE_YEAR:
                    serviceTime = ServiceTime.ONE_YEAR;
                    break;
                case RANGE_FOUR_MONTH:
                    serviceTime = ServiceTime.FOUR_MONTHS;
                    break;
                case RANGE_ONE_YEAR_FIFTEEN:
                    serviceTime = ServiceTime.ONE_YEAR_FIFTH_DAYS;
                    break;
                default:
                    serviceTime = ServiceTime.FOUR_YEARS;
                    break;
            }

            mMilitaryInfo = new MilitaryInfo(loginMillis, serviceTime, deleteDays);
            SettingsUtils.setMilitaryInfo(mMilitaryInfo.getJsonString());
            getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
        } else {
            mMilitaryInfo = MilitaryInfo.parse(SettingsUtils.getMilitaryInfo());
        }


        setListeners();
        initCircleView();

        ChangeLog cl = new ChangeLog(this);
        if (cl.firstRun())
            cl.getLogDialog().show();

        mViewPager = mCircularBarPager.getViewPager();
        mViewPager.setClipToPadding(true);
        mCirclePageIndicator = mCircularBarPager.getCirclePageIndicator();

        mDemoViews = new DemoView[2];
        mDemoViews[0] = new DemoView(this);
        mDemoViews[1] = new DemoView(this);

        ViewPagerAdapter adapter = new ViewPagerAdapter(this, mDemoViews);
        mViewPager.setAdapter(adapter);
        mCirclePageIndicator.setViewPager(mViewPager);
        mCirclePageIndicator.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (mCircularBarPager != null && mCircularBarPager.getCircularBar() != null) {
                    selectItem(position);
                }
            }
        });

        mCirclePageIndicator.setFillColor(0xFF888888);
//        mCirclePageIndicator.setStrokeColor(0xFF000000);
//        mCirclePageIndicator.setStrokeWidth();
//        mCirclePageIndicator.setRadius();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
//        mRecyclerView.setItemAnimator(new FadeInAnimator(new OvershootInterpolator(1f)));
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

    private void setListeners() {

        mTvUsername.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                final EditText inputText = new EditText(MainActivity.this);
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle("改名");
                alertDialog.setView(inputText, 10, 15, 10, 0);
                inputText.setText(USER_FB_NAME);
                inputText.setLines(1);
                inputText.setSingleLine(true);
                alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "確定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        USER_FB_NAME = inputText.getText().toString();
                        mTvUsername.setText("YO~ " + USER_FB_NAME + "!");
                    }
                });
                alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                alertDialog.show();
            }
        });

        /**
         *  自訂役期下拉選單初始化及設定監聽器
         *
         *  TODO 長按Spinner可以自訂 或 按住自訂的Item可以設定
         */


        mServiceRangeArrayList.add("1年");
        mServiceRangeArrayList.add("4個月");
        mServiceRangeArrayList.add("1年15天");

//		serviceDayAdapter = ArrayAdapter.createFromResource(this, R.array.service_day, android.R.layout.simple_spinner_item);
        mServiceDayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, mServiceRangeArrayList);
        mSpinnerServiceDay.setAdapter(mServiceDayAdapter);
        mServiceDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if (mCustomServiceRange) {
            mServiceDayAdapter.add(calCustomServiceRange(sCustomServiceRangeArray));
            mServiceDayAdapter.notifyDataSetChanged();
        }

        if (sIntServiceRange != RANGE_DEFAULT_ONE_YEAR) {
            mSpinnerServiceDay.setSelection(sIntServiceRange);
        }

        mSpinnerServiceDay.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sIntServiceRange = position;
//                loadUserData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private boolean checkNetwork() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            boolean isConnected = activeNetwork.isConnectedOrConnecting();
            // LoginFacebook();
            mProgressBarFbConnect.setVisibility(View.GONE);
            return isConnected;
        } catch (NullPointerException e) {
            mProgressBarFbConnect.setVisibility(View.VISIBLE);
            mTvUsername.setText("請確認網路狀況是否正常");
            return false;
        } finally {
            mCheckNetWorkStatusHandler.postDelayed(mCheckNetWorkStatusRunnable, 1000);
        }

    }

    private void LoginFacebook() {

        Session.openActiveSession(this, true, new Session.StatusCallback() {
            @Override
            public void call(final Session session, SessionState state, Exception exception) {
                if (session.isOpened()) {

                    if (session != null) {
                        // Check for publish permissions
                        List<String> permissions = session.getPermissions();
                        if (!isSubsetOf(PERMISSIONS, permissions)) {
                            pendingPublishReauthorization = true;
                            Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(MainActivity.this, PERMISSIONS);
                            session.requestNewPublishPermissions(newPermissionsRequest);
                        }

                        // Make an API call to get user data and define a
                        // new callback to handle the response.
                        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
                            @Override
                            public void onCompleted(GraphUser user, Response response) {
                                // If the response is successful
                                if (session == Session.getActiveSession()) {
                                    if (user != null) {
                                        // Set the id for the ProfilePictureView
                                        // view that in turn displays the profile picture.
                                        // Set the Textview's text to the user's name.
                                        mTvUsername.setText("Hi~ " + user.getName() + "!");
                                        mProgressBarFbConnect.setVisibility(View.GONE);
                                        USER_FB_NAME = user.getName();
                                    }
                                }
                                if (response.getError() != null) {
                                    // Handle errors, will do so later.
                                }
                            }
                        });
                        request.executeAsync();
                        return;
                    }
                }
            }
        });

//	    final Session session = Session.getActiveSession();

    }

    private boolean checkPostPermissions() {
        Session session = Session.getActiveSession();
        try {
            if (session != null) {

                // Check for publish permissions
                List<String> permissions = session.getPermissions();
                if (!isSubsetOf(PERMISSIONS, permissions)) {
                    pendingPublishReauthorization = true;
                    Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
                    session.requestNewPublishPermissions(newPermissionsRequest);
                    Log.d(TAG, "跑了");
                    return false;
                }
                return true;
            }
        } catch (UnsupportedOperationException e) {
            Toast.makeText(MainActivity.this.getApplicationContext(), "您必須授予程式貼文的權限後，才可張貼至動態時報", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    protected void saveImage(Bitmap bmScreen2) {

        // String fname = "Upload.png";
        File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File saved_image_file = new File(path + "/captured_screen.png");
        if (saved_image_file.exists())
            saved_image_file.delete();
        try {
            //resize

            FileOutputStream out = new FileOutputStream(saved_image_file);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bmScreen2.compress(Bitmap.CompressFormat.PNG, 100, out);
            bmScreen2.compress(Bitmap.CompressFormat.PNG, 100, baos);
            photo_data = baos.toByteArray();
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//		setCountDownTimer(LOGOUT_TIME);
        mRefreshInformationHandler.post(mRefreshInformationRunnable);
        mCheckNetWorkStatusHandler.post(mCheckNetWorkStatusRunnable);
        mCircularBarPager.getCircularBar().postDelayed(new Runnable() {
            @Override
            public void run() {
                selectItem(0);
            }
        }, 550);
        new LongOperation().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
//		mCountDownTimer.cancel();
        mRefreshInformationHandler.removeCallbacks(mRefreshInformationRunnable);
        mCheckNetWorkStatusHandler.removeCallbacks(mCheckNetWorkStatusRunnable);

        SettingsUtils.setMilitaryInfo(mMilitaryInfo.getJsonString());
//        savePrefs();
    }

    private void publishStory(String post_message, boolean takepic) throws Exception {
        String graphPath = "me/feed";
        Session session = Session.getActiveSession();

        if (session != null) {

            // Check for publish permissions
            List<String> permissions = session.getPermissions();
            if (!isSubsetOf(PERMISSIONS, permissions)) {
                pendingPublishReauthorization = true;
                Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(this, PERMISSIONS);
                session.requestNewPublishPermissions(newPermissionsRequest);
                Log.d(TAG, "跑了");
                return;
            }

            Bundle postParams = new Bundle();
            postParams.putString("message", post_message);

            /**
             * photo cap.
             */

            if (takepic) {
                screen = (View) findViewById(R.id.logout_information);
                screen.setDrawingCacheEnabled(true);
                bmScreen = screen.getDrawingCache();
                saveImage(bmScreen);
                screen.setDrawingCacheEnabled(false);
                postParams.putByteArray("picture", photo_data);
                graphPath = "me/photos";
            } else {
                postParams.putString("name", "放假時間是很珍貴的!");
                postParams.putString("caption", "請愛護服役人員");
//                postParams.putString("description", "剩下 " + mFacebookCountTimeText + "就退了，退伍令也已經載了" + mTvLoginPercent.getText() + "了");
                postParams.putString("link", "https://play.google.com/store/apps/details?id=com.kihon.android.apps.army_logout");
                postParams.putString("picture", "https://fbcdn-profile-a.akamaihd.net/hprofile-ak-frc1/s160x160/481191_436050169810593_1810179700_a.png");
            }

            Log.d(TAG, post_message);

            Request.Callback callback = new Request.Callback() {
                public void onCompleted(Response response) {
                    JSONObject graphResponse = response.getGraphObject().getInnerJSONObject();
                    String postId = null;
                    try {
                        postId = graphResponse.getString("id");
                    } catch (JSONException e) {
                        Log.i(TAG, "JSON error " + e.getMessage());
                    }
                    FacebookRequestError error = response.getError();
                    if (error != null) {
                        Toast.makeText(MainActivity.this.getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
                    } else {
//	                        Toast.makeText(MainActivity.this.getApplicationContext(), postId, Toast.LENGTH_LONG).show();
                        Toast.makeText(MainActivity.this.getApplicationContext(), "張貼成功!", Toast.LENGTH_LONG).show();
                        Log.d(TAG, postId);
                    }
                    mProgressDialog.dismiss();
                }
            };

            Request request = new Request(session, graphPath, postParams, HttpMethod.POST, callback);

            RequestAsyncTask task = new RequestAsyncTask(request);
            task.execute();
        }

    }

    private boolean isSubsetOf(Collection<String> subset, Collection<String> superset) {
        for (String string : subset) {
            if (!superset.contains(string)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

		/*
        final SharedPreferences settings = getSharedPreferences(PREF, 0);
		menu.findItem(R.id.action_connect_fb).setChecked(settings.getBoolean(PREF_CONNECT_FB, false));

		menu.findItem(R.id.action_connect_fb).setOnMenuItemClickListener(new OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(item.isChecked()){

				}else{
					LoginFacebook();
				}

				item.setChecked(!item.isChecked());
				settings.edit().putBoolean(PREF_CONNECT_FB, item.isChecked()).commit();
				return false;
			}
		});
		*/

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

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                };

                ViewGroup rootView = (ViewGroup) findViewById(android.R.id.content);
                PermissionListener snackbarPermissionListener =
                        SnackbarOnDeniedPermissionListener.Builder
                                .with(rootView, "需要存取空間的權限")
                                .withOpenSettingsButton("設定")
                                .build();

                Dexter.checkPermission(new CompositePermissionListener(listener,snackbarPermissionListener), Manifest.permission.WRITE_EXTERNAL_STORAGE);
                return true;
            case R.id.action_about:
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
                                OpenFacebookPage();
                            }
                        })
                        .show();

                return true;
            case R.id.action_changelog:
                ChangeLog cl = new ChangeLog(this);
                cl.getFullLogDialog().show();
                return true;
            case R.id.action_change_widger_bgcolor:
                startActivity(new Intent(this, WidgetColorPickerActivity.class));
                return true;
            case R.id.action_reorder:
                startSupportActionMode(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void OpenFacebookPage(){
        String facebookPageID = "431938510221759";
        String facebookUrl = "https://www.facebook.com/" + facebookPageID;
        String facebookUrlScheme = "fb://page/" + facebookPageID;

        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrlScheme)));
        } catch (Exception e) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(facebookUrl)));
        }
    }

    private void closeSoftKeyboard(IBinder iBinder) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(iBinder, 0);
    }

    private String calCustomServiceRange(String[] rangeArr) {
        StringBuilder spinnerText = new StringBuilder();
        spinnerText.append(!rangeArr[0].equals("0") ? rangeArr[0] + "年" : "");
        spinnerText.append(!rangeArr[1].equals("0") ? rangeArr[1] + "個月" : "");
        spinnerText.append(!rangeArr[2].equals("0") ? rangeArr[2] + "天" : "");
        mCustomServiceRange = !spinnerText.toString().equals("");
        return spinnerText.toString();
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

    interface UpdateCallbacks {
        void onUpdate(float percent);
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
                return new ItemViewHolder(mLayoutInflater.inflate(R.layout.list_item_drink_list, parent, false));
            else
                return new ProgressbarViewHolder(mLayoutInflater.inflate(R.layout.list_item_drink_list_2, parent, false));
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
                    case CounterTimer:
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
                ((ProgressbarViewHolder) holder).progressBar.setProgress(mServiceUtil.getPercentage());
                ((ProgressbarViewHolder) holder).percent.setText(String.format(Locale.TAIWAN, "%.1f%%", mServiceUtil.getPercentage()));
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

    @SuppressLint("ValidFragment")
    public class CustomRangeDialogFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();
            builder.setTitle("請輸入役期");
            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_custom_service_range, null))
                    // Add action buttons
                    .setPositiveButton("確定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            sCustomServiceRangeArray[0] = ((EditText) getDialog().findViewById(R.id.inputText_year)).getText().toString();
                            sCustomServiceRangeArray[1] = ((EditText) getDialog().findViewById(R.id.inputText_month)).getText().toString();
                            sCustomServiceRangeArray[2] = ((EditText) getDialog().findViewById(R.id.inputText_day)).getText().toString();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            CustomRangeDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        @Override
        public void onResume() {
            super.onResume();
            final EditText etYear = (EditText) getDialog().findViewById(R.id.inputText_year);
            final EditText etMonth = (EditText) getDialog().findViewById(R.id.inputText_month);
            final EditText etDay = (EditText) getDialog().findViewById(R.id.inputText_day);
            etYear.setFilters(new InputFilter[]{new InputFilterMinMax(0, 40), new InputFilter.LengthFilter(2)});
            etMonth.setFilters(new InputFilter[]{new InputFilterMinMax(0, 12), new InputFilter.LengthFilter(2)});
            etDay.setFilters(new InputFilter[]{new InputFilterMinMax(0, 31), new InputFilter.LengthFilter(2)});


            etYear.setText(sCustomServiceRangeArray[0]);
            etMonth.setText(sCustomServiceRangeArray[1]);
            etDay.setText(sCustomServiceRangeArray[2]);

            etYear.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String str = etYear.getText().toString().trim();
                    if (str.equals("")) {
                        etYear.setText("0");
                        etYear.selectAll();
                    }
                }
            });

            etMonth.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String str = etMonth.getText().toString().trim();
                    if (str.equals("")) {
                        etMonth.setText("0");
                        etMonth.selectAll();
                    }
                }
            });

            etDay.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String str = etDay.getText().toString().trim();
                    if (str.equals("")) {
                        etDay.setText("0");
                        etDay.selectAll();
                    }
                }
            });
            Log.d("AA", "BB");
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            sCustomServiceRangeArray[0] = String.valueOf(Integer.valueOf(sCustomServiceRangeArray[0]));
            sCustomServiceRangeArray[1] = String.valueOf(Integer.valueOf(sCustomServiceRangeArray[1]));
            sCustomServiceRangeArray[2] = String.valueOf(Integer.valueOf(sCustomServiceRangeArray[2]));

            Log.d(TAG, "Y:" + sCustomServiceRangeArray[0]);
            Log.d(TAG, "M:" + sCustomServiceRangeArray[1]);
            Log.d(TAG, "D:" + sCustomServiceRangeArray[2]);

            calCustomServiceRange(sCustomServiceRangeArray);

            if (mCustomServiceRange) {
                if (mServiceRangeArrayList.size() != RANGE_CUSTOM)
                    mServiceRangeArrayList.remove(RANGE_CUSTOM);
                mServiceDayAdapter.add(calCustomServiceRange(sCustomServiceRangeArray));
                mSpinnerServiceDay.setSelection(RANGE_CUSTOM);
            } else if (mServiceRangeArrayList.size() != RANGE_CUSTOM) {
                mServiceRangeArrayList.remove(RANGE_CUSTOM);
            }
            mServiceDayAdapter.notifyDataSetChanged();

            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//            loadUserData();

            super.onDismiss(dialog);
        }
    }

}
