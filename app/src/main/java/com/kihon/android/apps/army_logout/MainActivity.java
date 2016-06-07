package com.kihon.android.apps.army_logout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.model.GraphUser;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
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
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SimpleTimeZone;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
	
	private static final String TAG = "MainActivity";
	
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
    
	private Button shareButton;
	
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";
	private boolean pendingPublishReauthorization = false;

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
	@BindView(R.id.login_progressBar)
	ProgressBar mProgressBarLogin;
	@BindView(R.id.progressBar_connect_facebook)
	ProgressBar mProgressBarFbConnect;
	@BindView(R.id.login_Percent)
	TextView mTvLoginPercent;
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
	
	
	//private int total_day = 365;
	protected static float sLoginPercent;
	
	private final static Calendar mLoginDateCalendar = Calendar.getInstance();
	private static Calendar mLogoutDateCalendar = Calendar.getInstance();
	
	private static String sLoginDate = null;
//	private static String LOGOUT_TIME = null;
	private static String sDeleteDays = null;
	private static String USER_FB_NAME = "弟兄";
//	public static int LOGIN_PASS_DAY = 0;
	
	private Handler mRefreshInformationHandler = new Handler();
	private ProgressDialog mProgressDialog;
	
	private SimpleDateFormat mFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss" ,new Locale("TAIWAN"));
	
	protected static int sLogoutYear = 0;
	protected static int sLogoutDay = 0;
	
    private static int sIntServiceRange = 0;
//    private static int sWidgetColors = -1;
    
    public final static int RANGE_DEFAULT_ONE_YEAR = 0;
    public final static int RANGE_FOUR_MONTH = 1;
    public final static int RANGE_ONE_YEAR_FIFTEEN = 2;
    public final static int RANGE_CUSTOM = 3;
    
	/**
	 *
	 */

    Bitmap bmScreen;
    RelativeLayout mLayout;
    Dialog screenDialog;
    static final int ID_SCREENDIALOG = 1;

    ImageView bmImage;
    Button btnScreenDialog_OK;
    // TextView TextOut;

    View screen;
    EditText EditTextIn;
    
    private byte[] photo_data = null;
    
    /**
     *  DrawerLayout
     *  
     *  @date 2013-11-19
     *  @author kihon
     */
    
    private ViewGroup mContainerView;
    static boolean login_yet = false;
    private static boolean service_year = true;
    private Handler mCheckNetWorkStatusHandler = new Handler();
    public static boolean NETWORK_CONNECTED = false;

    private boolean mBeforeHoneycomb = true;
    
    /**
     * 自訂役期
     */
    
	private static String[] sCustomServiceRangeArray = new String[] { "4", "0", "0" };
    private boolean mCustomServiceRange = false;
	private ArrayList<String> mServiceRangeArrayList = new ArrayList<String>();
	private DialogFragment mCustomRangeDialog = new CustomRangeDialogFragment();
	private String mCustomServiceRangeText = null;
	private ArrayAdapter<String> mServiceDayAdapter;

	protected String mFacebookCountTimeText;


	/**
	 *
	 */
	@BindView(R.id.toolbar)
	Toolbar mToolbar;

    private int mDeleteDay;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);

		setSupportActionBar(mToolbar);

        mBreakMonthBlock.setVisibility(View.GONE);

        restorePrefs();
		loadUserData();
		setListeners();
        
		ChangeLog cl = new ChangeLog(this);
		if (cl.firstRun())
			cl.getLogDialog().show();

	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {
		switch (position) {
		case 0:
			mCustomRangeDialog.show(getSupportFragmentManager(), "customRangeDialog");
			mCustomRangeDialog.setRetainInstance(true);
			break;
		case 1:
			startActivity(new Intent(this, WidgetColorPickerActivity.class));
			break;
		case 2:
			ChangeLog cl = new ChangeLog(this);
			cl.getFullLogDialog().show();
			break;
		default:
			break;
		}
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

        final MaterialNumberPicker numberPicker = new MaterialNumberPicker.Builder(MainActivity.this)
                .minValue(1)
                .maxValue(30)
                .defaultValue(mDeleteDay)
                .backgroundColor(Color.WHITE)
                .separatorColor(Color.TRANSPARENT)
                .textColor(Color.BLACK)
                .textSize(20)
                .enableFocusability(false)
                .wrapSelectorWheel(true)
                .build();

        mDeleteDayButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new MaterialDialog.Builder(MainActivity.this)
                        .customView(numberPicker,false)
                        .title("折抵天數")
                        .positiveText(android.R.string.ok)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
								mDeleteDayButton.setText(String.valueOf(mDeleteDay = numberPicker.getValue()));
                                mDeleteDay = numberPicker.getValue();
                                loadUserData();
                            }
                        })
                        .show();
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
		mServiceDayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mServiceRangeArrayList);
		mSpinnerServiceDay.setAdapter(mServiceDayAdapter);
		mServiceDayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
		if(mCustomServiceRange){
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
				loadUserData();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}

	private Runnable mCheckNetWorkStatusRunnable = new Runnable() {

		@Override
		public void run() {
			NETWORK_CONNECTED = checkNetwork();
		}
	};

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

				    if (session != null){
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
	
	private boolean checkPostPermissions(){
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
			Toast.makeText(MainActivity.this. getApplicationContext(), "您必須授予程式貼文的權限後，才可張貼至動態時報", Toast.LENGTH_LONG).show();
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
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		mCountDownTimer.cancel();
		mRefreshInformationHandler.removeCallbacks(mRefreshInformationRunnable);
		mCheckNetWorkStatusHandler.removeCallbacks(mCheckNetWorkStatusRunnable);

        savePrefs();
	}

    private void savePrefs() {
        SharedPreferences settings = getSharedPreferences(PREF, 0);
        settings.edit()
                .putString(PREF_LOGINDATE, sLoginDate)
//				.putString(PREF_SERVICEDAY, field_height.getText().toString())
                .putString(PREF_DELETEDAY, String.valueOf(mDeleteDay))
                .putString(PREF_USERNAME, USER_FB_NAME)
                .putLong(PREF_LOGINMILLIS, mLoginDateCalendar.getTimeInMillis())
                .putLong(PREF_LOGOUTMILLIS, mLogoutDateCalendar.getTimeInMillis())
//				.putInt(PREF_WIDGETBGCOLOR, sWidgetColors)
                .putInt(PREF_SERVICERANGE, mSpinnerServiceDay.getSelectedItemPosition())
                .putString(PREF_CUSTOM_SERVICERANGE_YEAR, sCustomServiceRangeArray[0])
                .putString(PREF_CUSTOM_SERVICERANGE_MONTH, sCustomServiceRangeArray[1])
                .putString(PREF_CUSTOM_SERVICERANGE_DAY, sCustomServiceRangeArray[2])
                .apply();
    }

    private Runnable mRefreshInformationRunnable = new Runnable() {
		
		private String mCountTimeText = null;
		private Calendar mCalendar = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
		
		@Override
		public void run() {
//			Log.d(TAG, "mRefreshInformationRunnable");
			
			/**
			 *  20130408 - 入伍倒數 
			 */ 
			
			long diffInMillis = 0;
			int loginPassDay = 0; 
			int currentYear = mCalendar.get(Calendar.YEAR);
			
			if (mLoginDateCalendar.getTimeInMillis() > System.currentTimeMillis()) {				
				diffInMillis = mLoginDateCalendar.getTimeInMillis() - System.currentTimeMillis();
				
				mBreakMonthBlock.setVisibility(View.GONE);
				mTvUntilLogoutTitle.setText("距離入伍還剩下");
				mTvStatus.setText("準備好踏入陰間了嗎？");
			} else {	
				long login_millis = System.currentTimeMillis() - mLoginDateCalendar.getTimeInMillis();
				Calendar currDay = Calendar.getInstance();
				currDay.setTimeInMillis(login_millis);
				loginPassDay = (int) (login_millis / 1000 / 60 / 60 / 24);
				
				diffInMillis = mLogoutDateCalendar.getTimeInMillis() - System.currentTimeMillis();				
				mTvUntilLogoutTitle.setText("距離退伍還剩下");
				mTvStatus.setText("你入伍已經"+loginPassDay+"天了嘿~ ");
			}
						
			mCalendar.setTimeInMillis(diffInMillis);
						
			StringBuffer buffer = new StringBuffer();
			
			if (mLoginDateCalendar.getTimeInMillis() <= System.currentTimeMillis()
					&& (mSpinnerServiceDay.getSelectedItemPosition() == 3) || mSpinnerServiceDay.getSelectedItemPosition() == 2) {
				if((mCalendar.get(Calendar.YEAR) - 1970) != 0){
					buffer.append((mCalendar.get(Calendar.YEAR) - 1970) + "年");
					buffer.append((mCalendar.get(Calendar.MONTH)+1) + "個月");	
					buffer.append((mCalendar.get(Calendar.DAY_OF_MONTH) - 1) + "天 ");
				}else{
					buffer.append((mCalendar.get(Calendar.DAY_OF_YEAR) - 1) + "天 ");
				}
			} else {
				buffer.append((mCalendar.get(Calendar.DAY_OF_YEAR) - 1) + "天 ");
			}
			
			mFacebookCountTimeText = buffer.toString();
			
			buffer.append((pad(mCalendar.get(Calendar.HOUR_OF_DAY)) + ":"));
			buffer.append((pad(mCalendar.get(Calendar.MINUTE)) + ":"));
			buffer.append((pad(mCalendar.get(Calendar.SECOND))));
			mCountTimeText = buffer.toString();
			

//			Log.d(TAG, "Nokori:" + diffInMillis);

			
//			LOGOUT_DAY = String.valueOf((mCalendar.get(Calendar.DAY_OF_YEAR)-1));
			sLogoutYear = (mCalendar.get(Calendar.YEAR) - 1970);
			sLogoutDay = (mCalendar.get(Calendar.DAY_OF_YEAR) - 1);

			double doubleLoginPassDay = Calendar.getInstance().getTimeInMillis() - mLoginDateCalendar.getTimeInMillis();

			sLoginPercent = (float) (( doubleLoginPassDay / (mLogoutDateCalendar.getTimeInMillis() - mLoginDateCalendar.getTimeInMillis())) * 100f);
			String percentText = new DecimalFormat("#.##").format(sLoginPercent);
			if(sLoginPercent >= 100){
				mTvLoginPercent.setText("100%");
			} else if (sLoginPercent <= 0) {
				mTvLoginPercent.setText("0%");
			} else {
				mTvLoginPercent.setText(percentText + "%");
			}
			
			
			mTvUntilLogoutDays.setText(mCountTimeText);
			mProgressBarLogin.setProgress((int) sLoginPercent);
			
			/**
			 * 20130407 - 破月BLOCK
			 **/

//			mBreakMonthBlock.setVisibility(View.GONE);
			
			if (sLogoutYear <= 0 &&sLogoutDay < 100 && sLogoutDay > 30 && diffInMillis > 0) {
				mBreakMonthBlock.setVisibility(View.VISIBLE);
				int break_month_tmp = sLogoutDay - 30;
				mBreakMonthTextView.setText(break_month_tmp + "天");
			} else {
				mBreakMonthBlock.setVisibility(View.GONE);
			}

			if (diffInMillis < 0){
				mTvStatus.setText("學長(`・ω・́)ゝ 你已經成功返陽了!");
				mTvUntilLogoutDays.setText("0天 00:00:00");
			}
	
			
			//END
			mRefreshInformationHandler.postDelayed(mRefreshInformationRunnable, 500);
		}
		
	};
	
	private static String pad(int c) {
		if (c >= 10)
			return String.valueOf(c);
		else
			return "0" + String.valueOf(c);
	}
	
    // Restore preferences
    private void restorePrefs()
    {
        SharedPreferences settings = getSharedPreferences(PREF, 0);
        String prefLoginDate = settings.getString(PREF_LOGINDATE, "");
        String prefDeleteDays = settings.getString(PREF_DELETEDAY, "");
       
        sLoginDate = prefLoginDate.equals("") ? mFormat.format(System.currentTimeMillis()) : prefLoginDate;
       
		if (!"".equals(prefDeleteDays)) {
			sDeleteDays = prefDeleteDays;
            mDeleteDay = Integer.valueOf(sDeleteDays);
            mDeleteDayButton.setText(sDeleteDays);
		} else {
			sDeleteDays = "0";
		}
        
        sIntServiceRange = settings.getInt(PREF_SERVICERANGE, 0);
        USER_FB_NAME = settings.getString(PREF_USERNAME, "弟兄");
//        sWidgetColors = settings.getInt(PREF_WIDGETBGCOLOR, -1);

		sCustomServiceRangeArray[0] = settings.getString(PREF_CUSTOM_SERVICERANGE_YEAR, "4");
		sCustomServiceRangeArray[1] = settings.getString(PREF_CUSTOM_SERVICERANGE_MONTH, "0");
		sCustomServiceRangeArray[2] = settings.getString(PREF_CUSTOM_SERVICERANGE_DAY, "0");
		
		calCustomServiceRange(sCustomServiceRangeArray);
        
        mTvUsername.setText("YO~ " + USER_FB_NAME + "!");
    }
    
    private void loadUserData(){
		try {
			mLoginDateCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd" ,new Locale("TAIWAN")).parse(sLoginDate));
			int minusDay = mDeleteDay;
			mLogoutDateCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd" ,new Locale("TAIWAN")).parse(sLoginDate));
			
			switch(sIntServiceRange){
			case RANGE_DEFAULT_ONE_YEAR:
				mLogoutDateCalendar.add(Calendar.YEAR, 1);
				break;
			case RANGE_FOUR_MONTH:
				mLogoutDateCalendar.add(Calendar.MONTH, 4);
				break;
			case RANGE_ONE_YEAR_FIFTEEN:
		        mLogoutDateCalendar.add(Calendar.YEAR, 1);
		        mLogoutDateCalendar.add(Calendar.DAY_OF_YEAR, 15);
				break;
			case RANGE_CUSTOM: //自訂役期
				mLogoutDateCalendar.add(Calendar.YEAR, Integer.valueOf(sCustomServiceRangeArray[0]));
				mLogoutDateCalendar.add(Calendar.MONTH, Integer.valueOf(sCustomServiceRangeArray[1]));
				mLogoutDateCalendar.add(Calendar.DAY_OF_MONTH, Integer.valueOf(sCustomServiceRangeArray[2]));
				break;
			default:
				break;
			}
			
			mLogoutDateCalendar.add(Calendar.DAY_OF_YEAR, -minusDay);
			
			if(mLoginDateCalendar.getTimeInMillis() < System.currentTimeMillis()){
				login_yet = true;
			} else {
				login_yet = false;
			}
			
//			mLogoutDateCalendar.setTime(new SimpleDateFormat("yyyy-MM-dd").parse(LOGOUT_TIME));
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		int year = mLoginDateCalendar.get(Calendar.YEAR);
		int month = mLoginDateCalendar.get(Calendar.MONTH)+1;
		int day = mLoginDateCalendar.get(Calendar.DAY_OF_MONTH);
        
		mBtnLoginDate.setText(year + "年" + month + "月" + day + "日");
		
		year = mLogoutDateCalendar.get(Calendar.YEAR);
		month = mLogoutDateCalendar.get(Calendar.MONTH)+1;
		day = mLogoutDateCalendar.get(Calendar.DAY_OF_MONTH);
		
		mTvLogoutDate.setText(year + "年" + month + "月" + day + "日");
//		String logout_date = year+"-"+(month)+"-"+day;
//		LOGOUT_TIME = logout_date;
    }

	private void publishStory(String post_message ,boolean takepic) throws Exception{
		String graphPath = "me/feed";
	    Session session = Session.getActiveSession();

	    if (session != null){

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
	        
	        if(takepic){
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
				postParams.putString("description", "剩下 " + mFacebookCountTimeText + "就退了，退伍令也已經載了" + mTvLoginPercent.getText() + "了");
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
	
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	    if (state.isOpened()) {
//	        shareButton.setVisibility(View.VISIBLE);
	    } else if (state.isClosed()) {
//	        shareButton.setVisibility(View.INVISIBLE);
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
	    	buffer.append("版本:"+pinfo.versionName);
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
	
	/*public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			// Use the current date as the default date in the picker
			final Calendar c = mLoginDateCalendar;
			int year = c.get(Calendar.YEAR);
			int month = c.get(Calendar.MONTH);
			int day = c.get(Calendar.DAY_OF_MONTH);

			// Create a new instance of DatePickerDialog and return it
			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		public void onDateSet(DatePicker view, int year, int month, int day) {
			// Do something with the date chosen by the user
			Log.d(TAG, year+"年"+month+"月"+day+"日");
			
			String login_date = year+"-"+(month+1)+"-"+day;
			sLoginDate = login_date;
			loadUserData();
		}
	}*/

	public void showDatePickerDialog(View v) {
//		DialogFragment newFragment = new DatePickerFragment();
//		newFragment.show(getFragmentManager(), "datePicker");
		Calendar calendar = mLoginDateCalendar;
		
		int year = calendar.get(Calendar.YEAR);
		int monthOfYear = calendar.get(Calendar.MONTH);
		int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
	
		DatePickerDialog.OnDateSetListener callBack = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				Log.d(TAG, year+"年"+monthOfYear+"月"+dayOfMonth+"日");
				
				String login_date = year+"-"+(monthOfYear+1)+"-"+dayOfMonth;
				sLoginDate = login_date;
				loadUserData();
				
			}
		};
		DatePickerDialog pickerDialog = new DatePickerDialog(this, callBack, year, monthOfYear, dayOfMonth);
		pickerDialog.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_share:
			return true;
		case R.id.share_photo:
			CharSequence[] items = {"拍攝相片","選擇相片"};
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(items , new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					switch(item){
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
			input_non_pic.setHint(USER_FB_NAME+"，快退了嗎？");

			alert_non_pic.setPositiveButton("送出", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					if(NETWORK_CONNECTED){
						if(checkPostPermissions()){
							closeSoftKeyboard(input_non_pic.getWindowToken());
							String value = input_non_pic.getText().toString();
							// Do something with value!
							mProgressDialog = new ProgressDialog(MainActivity.this);
							mProgressDialog.setMessage("張貼中");
							mProgressDialog.setIndeterminate(true);
							mProgressDialog.setCancelable(false);// 無法利用back鍵退出
							mProgressDialog.show();
							try {
								publishStory(value ,false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}else{
							Toast.makeText(MainActivity.this. getApplicationContext(), "您必須授予程式貼文的權限後，才可張貼至動態時報", Toast.LENGTH_LONG).show();
						}					
					} else {
						Toast.makeText(MainActivity.this. getApplicationContext(), "請確認網路狀態是否連線", Toast.LENGTH_LONG).show();
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
			dialogFragment.show(getSupportFragmentManager(), "aboutDialog");
			return true;
		case R.id.action_changelog:
			ChangeLog cl = new ChangeLog(this);
			cl.getFullLogDialog().show();
			return true;
		case R.id.action_change_widger_bgcolor:
			startActivity(new Intent(this, WidgetColorPickerActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void closeSoftKeyboard(IBinder iBinder){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(iBinder, 0);
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
			etYear.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 40),new InputFilter.LengthFilter(2)});
			etMonth.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 12),new InputFilter.LengthFilter(2)});
			etDay.setFilters(new InputFilter[]{ new InputFilterMinMax(0, 31),new InputFilter.LengthFilter(2)});
		

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
			loadUserData();

			super.onDismiss(dialog);
		}
	}
	
	private String calCustomServiceRange(String[] rangeArr) {
		StringBuffer spinnerText = new StringBuffer();
		spinnerText.append(!rangeArr[0].equals("0") ? rangeArr[0] + "年" : "");
		spinnerText.append(!rangeArr[1].equals("0") ? rangeArr[1] + "個月" : "");
		spinnerText.append(!rangeArr[2].equals("0") ? rangeArr[2] + "天" : "");
		mCustomServiceRange = !spinnerText.toString().equals("");
		return spinnerText.toString(); 
	}

}
