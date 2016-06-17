package com.kihon.android.apps.army_logout;

import com.google.common.collect.ContiguousSet;
import com.google.common.collect.DiscreteDomain;
import com.google.common.collect.Range;
import com.google.common.primitives.Ints;
import com.google.gson.Gson;

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
import com.kihon.android.apps.army_logout.settings.SettingsUtils;
import com.viewpagerindicator.CirclePageIndicator;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;
import org.json.JSONException;
import org.json.JSONObject;

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
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
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
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
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

public class MainActivity extends AppCompatActivity implements OnStartDragListener {

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
    //    private ServiceTime mServiceTime = ServiceTime.ONE_YEAR;
    private Runnable mRefreshInformationRunnable = new Runnable() {

        @Override
        public void run() {

//            mServiceUtil = new ServiceUtil(mLoginDateCalendar.getTimeInMillis(), mServiceTime, mDeleteDay);
            mServiceUtil = new ServiceUtil(mMilitaryInfo);

            if (mServiceUtil.isLoggedIn()) {
                mTvUntilLogoutTitle.setText("距離退伍還剩下");
                mTvStatus.setText("你入伍已經" + mServiceUtil.getPassedDay() + "天了嘿~ ");
            } else {
                mBreakMonthBlock.setVisibility(View.GONE);
                mTvUntilLogoutTitle.setText("距離入伍還剩下");
                mTvStatus.setText("準備好踏入陰間了嗎？");
            }

           /* String percentText = new DecimalFormat("#.#").format(mServiceUtil.getPercentage());
            if (mServiceUtil.getPercentage() >= 100.0f) {
                mTvLoginPercent.setText("100%");
            } else if (mServiceUtil.getPercentage() <= 0.0f) {
                mTvLoginPercent.setText("0%");
            } else {
                mTvLoginPercent.setText(percentText + "%");
            }*/

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

//            InfoItem infoItems = new InfoItem(mServiceUtil);


           /* Object[][] infos = new Object[][]{
                    new Object[]{R.drawable.ic_date_range_black_24dp, "入伍日期", mServiceUtil.getLoginDateString()},
                    new Object[]{R.drawable.ic_access_time_black_24dp, "役期", mServiceUtil.getServiceTime().getDisplayText()},
                    new Object[]{R.drawable.ic_directions_run_black_24dp, "退伍日期", mServiceUtil.getRealLogoutDateString()},
                    new Object[]{R.drawable.ic_all_inclusive_black_24px, "折抵", String.format(Locale.TAIWAN, "%d天", mServiceUtil.getDiscountDays())},
                    new Object[]{R.drawable.ic_time_countdown_black_24dp, "距離退伍剩下", mServiceUtil.getRemainingDayWithString()},
                    new Object[]{R.drawable.ic_school_black_24dp, "退伍令下載進度", mServiceUtil.getPercentage()}};
*/

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

            //END
            mRefreshInformationHandler.postDelayed(mRefreshInformationRunnable, 500);
        }

    };
    private InfoAdapter mInfoAdapter;
    private List<InfoItem> mData;
    private MilitaryInfo mMilitaryInfo;
    private ItemTouchHelper mTouchHelper;

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

    private CirclePageIndicator mCirclePageIndicator;
    private ViewPager mViewPager;

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

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

//        restorePrefs();
//        loadUserData();
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
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        ItemClickSupport.addTo(mRecyclerView).setOnItemClickListener(
                new ItemClickSupport.OnItemClickListener() {
                    @Override
                    public void onItemClicked(RecyclerView recyclerView, int position, View v) {
                        onSettingsSelected(position, v);
                    }
                }
        );

    }

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mTouchHelper.startDrag(viewHolder);
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

    public static class InfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements ItemTouchHelperAdapter, ItemTouchHelperViewHolder {

        private final LayoutInflater mLayoutInflater;
        private final List<InfoItem> mData;
        private final OnStartDragListener mDragStartListener;
        private final Context mContext;
        private ServiceUtil mServiceUtil;
        private boolean isReorder;

        public InfoAdapter(Context context, List<InfoItem> data, OnStartDragListener dragStartListener) {
            mContext = context;
            mLayoutInflater = LayoutInflater.from(context);
            mData = data;
            mDragStartListener = dragStartListener;
        }

        @Override
        public int getItemViewType(int position) {
            return mData.get(position) == InfoItem.CounterProgressbar ? 1 : 0;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == 0)
                return new ItemViewHolder(mLayoutInflater.inflate(R.layout.list_item_drink_list, parent, false));
            else
                return new ProgressbarViewHolder(mLayoutInflater.inflate(R.layout.list_item_drink_list_2, parent, false));
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
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
//            swap(SettingsUtils.getInfoItemIndexes(), fromPosition, toPosition);
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
        public void onItemDismiss(int position) {

        }

        public void onReorderMode(boolean value) {
            isReorder = value;
            notifyDataSetChanged();
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

        class ItemViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.imageView)
            ImageView icon;
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.subtitle)
            TextView subtitle;
            @BindView(R.id.handle)
            ImageView handleView;

            public ItemViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);

            }
        }

        class ProgressbarViewHolder extends RecyclerView.ViewHolder {

            @BindView(R.id.imageView)
            ImageView icon;
            @BindView(R.id.title)
            TextView title;
            @BindView(R.id.login_progressBar)
            RoundCornerProgressBar progressBar;
            @BindView(R.id.login_percent)
            TextView percent;
            @BindView(R.id.handle)
            ImageView handleView;

            public ProgressbarViewHolder(View view) {
                super(view);
                ButterKnife.bind(this, view);
            }
        }
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
        inflater.inflate(R.menu.main, menu);

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
                return true;
            case R.id.share_photo:
                CharSequence[] items = {"拍攝相片", "選擇相片"};
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        switch (item) {
                            case 0:
                                startActivity(new Intent(MainActivity.this, ShareTo.class).putExtra("openCam", true));
                                break;
                            case 1:
                                startActivity(new Intent(MainActivity.this, ShareTo.class).putExtra("openCam", false));
                                break;
                        }
                    }
                });
                builder.create();
                builder.show();
                return true;
            case R.id.share_board:

			/*
                AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);

	        	alert.setTitle("分享至Facebook");
	        	alert.setMessage("計時看板截圖+你想說的話，一併發布至你的動態時報!");

	        	// Set an EditText view to get user input
	        	final EditText input = new EditText(MainActivity.this);
	        	alert.setView(input);
	        	input.setHint(USER_FB_NAME+"，快退了嗎？");

	        	alert.setPositiveButton("送出", new DialogInterface.OnClickListener() {
	        		public void onClick(DialogInterface dialog, int whichButton) {
	        			if(NETWORK_CONNECTED){
	        				closeSoftKeyboard(input.getWindowToken());
	        				String value = input.getText().toString();
	        				// Do something with value!
	        				pDialog = new ProgressDialog(MainActivity.this);
	        				pDialog.setMessage("張貼中");
	        				pDialog.setIndeterminate(true);
	        				pDialog.setCancelable(false);// 無法利用back鍵退出
	        				pDialog.show();
	        				publishStory(value, true);
	        			}else{
	        				Toast.makeText(MainActivity.this. getApplicationContext(), "請確認網路狀態是否連線", Toast.LENGTH_LONG).show();
	        			}
	        		}
	        	});

	        	alert.setNegativeButton("取消", new DialogInterface.OnClickListener() {
	        	  public void onClick(DialogInterface dialog, int whichButton) {
	        	    // Canceled.
	        	  }
	        	});

	        	alert.show();
			 */

                screen = findViewById(R.id.logout_information);
                assert screen != null;
                screen.setDrawingCacheEnabled(true);
                bmScreen = screen.getDrawingCache();
                saveImage(bmScreen);
                screen.setDrawingCacheEnabled(false);

                File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                File image_file = new File(path + "/captured_screen.png");
                Uri outputFileUri = Uri.fromFile(image_file);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);
                shareIntent.setType("image/jpeg");
                startActivity(Intent.createChooser(shareIntent, "將截圖分享至..."));

                return true;
            case R.id.share_facebook:
                LoginFacebook();
                AlertDialog.Builder alert_non_pic = new AlertDialog.Builder(MainActivity.this);

                alert_non_pic.setTitle("分享至Facebook");
                alert_non_pic.setMessage("完全不PO圖，將計時看板的資訊轉為文字發布至動態時報");

                // Set an EditText view to get user input
                final EditText input_non_pic = new EditText(MainActivity.this);
                alert_non_pic.setView(input_non_pic);
                input_non_pic.setHint(USER_FB_NAME + "，快退了嗎？");

                alert_non_pic.setPositiveButton("送出", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (NETWORK_CONNECTED) {
                            if (checkPostPermissions()) {
                                closeSoftKeyboard(input_non_pic.getWindowToken());
                                String value = input_non_pic.getText().toString();
                                // Do something with value!
                                mProgressDialog = new ProgressDialog(MainActivity.this);
                                mProgressDialog.setMessage("張貼中");
                                mProgressDialog.setIndeterminate(true);
                                mProgressDialog.setCancelable(false);// 無法利用back鍵退出
                                mProgressDialog.show();
                                try {
                                    publishStory(value, false);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Toast.makeText(MainActivity.this.getApplicationContext(), "您必須授予程式貼文的權限後，才可張貼至動態時報", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this.getApplicationContext(), "請確認網路狀態是否連線", Toast.LENGTH_LONG).show();
                        }

                    }
                });

                alert_non_pic.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Canceled.
                    }
                });

                alert_non_pic.show();
                return true;
            //	        case R.id.share_facebook_request:
            //	        	if(NETWORK_CONNECTED){
            //	        		sendRequestDialog();
            //	        	} else {
            //	        		Toast.makeText(MainActivity.this. getApplicationContext(), "請確認網路狀態是否連線", Toast.LENGTH_LONG).show();
            //	        	}
            //
            //	        	return true;
            case R.id.action_about:
                DialogFragment dialogFragment = new AboutDialogFragment();
                dialogFragment.show(getFragmentManager(), "aboutDialog");
                return true;
            case R.id.action_changelog:
                ChangeLog cl = new ChangeLog(this);
                cl.getFullLogDialog().show();
                return true;
            case R.id.action_change_widger_bgcolor:
                startActivity(new Intent(this, WidgetColorPickerActivity.class));
                return true;
            case R.id.action_reorder:
                mInfoAdapter.onReorderMode(!mInfoAdapter.isReorder);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    public static class AboutDialogFragment extends DialogFragment {

        PackageInfo pinfo = null;

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction
            final TextView message = new TextView(getActivity());
            StringBuffer buffer = new StringBuffer();

            try {
                pinfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            buffer.append("版本:" + pinfo.versionName);
            buffer.append(getActivity().getText(R.string.about_text));

            final SpannableString s = new SpannableString(buffer.toString());
            Linkify.addLinks(s, Linkify.WEB_URLS);
            message.setText(s);
            message.setTextSize(16);
            message.setPadding(10, 10, 10, 10);
            message.setMovementMethod(LinkMovementMethod.getInstance());

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.about_app_name)
                    .setView(message)
                    .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // FIRE ZE MISSILES!
                        }
                    });
            // Create the AlertDialog object and return it
            return builder.create();
        }
    }

    public class DemoView extends LinearLayout implements UpdateCallbacks {
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

        @Override
        public void onUpdate(float percent) {
            String percentText = new DecimalFormat("#.##").format(percent);
            if (percent >= 100.0f) {
                mValueInfoTextview.setText("100%");
            } else if (percent <= 0.0f) {
                mValueInfoTextview.setText("0%");
            } else {
                mValueInfoTextview.setText(String.format("%s%%", percentText));
            }
        }
    }

	/*private void sendRequestDialog() {
        Bundle params = new Bundle();
		params.putString("message", "期待我們早日回陽間生活吧!!");

		WebDialog requestsDialog = (new WebDialog.RequestsDialogBuilder(MainActivity.this, Session.getActiveSession(), params)).setOnCompleteListener(new WebDialog.OnCompleteListener() {

			@Override
			public void onComplete(Bundle values, FacebookException error) {
				if (error != null) {
					if (error instanceof FacebookOperationCanceledException) {
						Toast.makeText(MainActivity.this.getApplicationContext(), "取消邀請", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MainActivity.this.getApplicationContext(), "網路錯誤", Toast.LENGTH_SHORT).show();
					}
				} else {
					final String requestId = values.getString("request");
					if (requestId != null) {
						Toast.makeText(MainActivity.this.getApplicationContext(), "邀請已發送", Toast.LENGTH_SHORT).show();
					} else {
						Toast.makeText(MainActivity.this.getApplicationContext(), "取消邀請", Toast.LENGTH_SHORT).show();
					}
				}
			}
		}).setMessage("載入中...").build();
		requestsDialog.show();
	}*/

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

    public static class MilitaryInfo {

        private long begin;
        private int period;
        private int discount;

        public static MilitaryInfo parse(String jsonString) {
            if (jsonString == null) {
                return new Gson().fromJson(new MilitaryInfo(DateTime.now().getMillis(), ServiceTime.ONE_YEAR, 30).getJsonString(), MilitaryInfo.class);
            } else {
                return new Gson().fromJson(jsonString, MilitaryInfo.class);
            }
        }

        public MilitaryInfo(long loginMillis, ServiceTime serviceTime, int deleteDays) {
            begin = loginMillis;
            period = serviceTime.ordinal();
            discount = deleteDays;
        }

        public String getJsonString() {
            return new Gson().toJson(this);
        }

        public long getBegin() {
            return begin;
        }

        public int getPeriod() {
            return period;
        }

        public int getDiscount() {
            return discount;
        }

        public MilitaryInfo setBegin(long begin) {
            this.begin = begin;
            return this;
        }

        public MilitaryInfo setPeriod(ServiceTime period) {
            this.period = period.ordinal();
            return this;
        }

        public MilitaryInfo setPeriod(int period) {
            this.period = period;
            return this;
        }

        public MilitaryInfo setDiscount(int discount) {
            this.discount = discount;
            return this;
        }
    }

    private class DisplayData {

        private final InfoItem mInfoItem;

        public DisplayData(InfoItem infoItem) {
            mInfoItem = infoItem;
        }

        public int getImageResource() {
            return mInfoItem.getImageRes();
        }

        public String getTitle() {
            return mInfoItem.getTitle();
        }

    }
}
