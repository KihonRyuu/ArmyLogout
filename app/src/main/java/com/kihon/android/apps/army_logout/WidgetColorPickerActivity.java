package com.kihon.android.apps.army_logout;

import afzkl.development.colorpickerview.view.ColorPanelView;
import afzkl.development.colorpickerview.view.ColorPickerView;
import afzkl.development.colorpickerview.view.ColorPickerView.OnColorChangedListener;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class WidgetColorPickerActivity extends ActionBarActivity implements OnClickListener, OnColorChangedListener, ActionBar.OnNavigationListener {
	
	private static final String TAG = "WidgetColorPickerActivity";
	
	private static final int BACKGROUND = 0;
	private static final int TITLE = 1;
	private static final int SUBTITLE = 2;

	private int isCurrentMode = BACKGROUND;
	
	private ColorPickerView mColorPickerView;
	private ColorPanelView mOldColorPanelView;
	private ColorPanelView mNewColorPanelView;

	private Button mOkButton;
	private Button mCancelButton;
	private OnNavigationListener mNavigationCallback;
	private TextView mTvTitle1;
	private TextView mTvTitle2;
	private TextView mTvSubtitle1;
	private TextView mTvSubtitle2;
	private FrameLayout mWidgetLayout;
	
	private int mBGColor = 0xFFFFFF;
	private int mTitleColor = 0x000000;
	private int mSubtitleColor = 0x3B5998;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_color_picker);
		init();

		getWindow().setFormat(PixelFormat.RGBA_8888);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

		/** An array of strings to populate dropdown list */
		String[] actions = new String[] { "Bookmark", "Subscribe", "Share" };

		/** Create an array adapter to populate dropdownlist */
		// ArrayAdapter<String> adapter = new
		// ArrayAdapter<String>(getBaseContext(),
		// android.R.layout.simple_spinner_item, actions);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.widget_color_picker_list_array, android.R.layout.simple_spinner_dropdown_item);
		
		/** Defining Navigation listener */
		mNavigationCallback = new OnNavigationListener() {
			// Get the same strings provided for the drop-down's ArrayAdapter
			String[] strings = getResources().getStringArray(R.array.widget_color_picker_list_array);

			@Override
			public boolean onNavigationItemSelected(int position, long itemId) {
				// Create new fragment from our own Fragment class
//				ListContentFragment newFragment = new ListContentFragment();
//				FragmentManager fm = getSupportFragmentManager();
//				FragmentTransaction ft = fm.beginTransaction();
//				// Replace whatever is in the fragment container with this
//				// fragment
//				// and give the fragment a tag name equal to the string at the
//				// position selected
//				ft.replace(R.id.fragment_container, newFragment, strings[position]);
//				// Apply changes
//				ft.commit();
				return true;
			}
		};

		/**
		 * Setting dropdown items and item navigation listener for the actionbar
		 */
		getSupportActionBar().setListNavigationCallbacks(mSpinnerAdapter, this);
		// mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

	}
	
	@Override
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		isCurrentMode = itemPosition;
		switch (isCurrentMode){
		case BACKGROUND:
			mColorPickerView.setColor(mBGColor);
			break;
		case TITLE:
			mColorPickerView.setColor(mTitleColor);
			break;
		case SUBTITLE:
			mColorPickerView.setColor(mSubtitleColor);
			break;
		}
		
		return false;
	}

	private void init() {
		// SharedPreferences prefs =
		// PreferenceManager.getDefaultSharedPreferences(this);
		// int initialColor = prefs.getInt("color_3", 0xFF000000);
		int initialColor = 0xFF000000;
		
		mWidgetLayout = (FrameLayout) findViewById(R.id.widgetMainLayout);
		mTvTitle1  = (TextView) findViewById(R.id.title1);
		mTvTitle2  = (TextView) findViewById(R.id.title2);
		mTvSubtitle1  = (TextView) findViewById(R.id.subtitle1);
		mTvSubtitle2  = (TextView) findViewById(R.id.login_Percent);

		mColorPickerView = (ColorPickerView) findViewById(R.id.color_picker_view);
		mOldColorPanelView = (ColorPanelView) findViewById(R.id.color_panel_old);
		mNewColorPanelView = (ColorPanelView) findViewById(R.id.color_panel_new);

		mOkButton = (Button) findViewById(R.id.okButton);
		mCancelButton = (Button) findViewById(R.id.cancelButton);

		((LinearLayout) mOldColorPanelView.getParent()).setPadding(Math.round(mColorPickerView.getDrawingOffset()), 0, Math.round(mColorPickerView.getDrawingOffset()), 0);

		SharedPreferences settings = getSharedPreferences(MainActivity.PREF, 0);
		mBGColor = settings.getInt(MainActivity.PREF_WIDGETBGCOLOR, 0xFFFFFFFF);
		mTitleColor = settings.getInt(MainActivity.PREF_WIDGETTITLECOLOR, 0xFF000000);
		mSubtitleColor = settings.getInt(MainActivity.PREF_WIDGETSUBTITLECOLOR, 0xFF3B5998);
		
		mColorPickerView.setOnColorChangedListener(this);
		mColorPickerView.setColor(mBGColor, true);
		mOldColorPanelView.setColor(mBGColor);

		mOkButton.setOnClickListener(this);
		mCancelButton.setOnClickListener(this);
		
		mTvTitle1.setTextColor(mTitleColor);
		mTvTitle2.setTextColor(mTitleColor);
		mTvSubtitle1.setTextColor(mSubtitleColor);
		mTvSubtitle2.setTextColor(mSubtitleColor);
	}

	@Override
	public void onColorChanged(int newColor) {
		mNewColorPanelView.setColor(mColorPickerView.getColor());
		switch (isCurrentMode){
		case BACKGROUND:
			mBGColor = mColorPickerView.getColor();
			mWidgetLayout.setBackgroundColor(mColorPickerView.getColor());
			break;
		case TITLE:
			//#000000
			mTitleColor = mColorPickerView.getColor();
			mTvTitle1.setTextColor(mColorPickerView.getColor());
			mTvTitle2.setTextColor(mColorPickerView.getColor());
			break;
		case SUBTITLE:
			//#3B5998
			mSubtitleColor = mColorPickerView.getColor();
			mTvSubtitle1.setTextColor(mColorPickerView.getColor());
			mTvSubtitle2.setTextColor(mColorPickerView.getColor());
			break;
		}
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.okButton:
			SharedPreferences settings = getSharedPreferences(MainActivity.PREF, 0);
			settings.edit()
			.putInt(MainActivity.PREF_WIDGETBGCOLOR, mBGColor)
			.putInt(MainActivity.PREF_WIDGETTITLECOLOR, mTitleColor)
			.putInt(MainActivity.PREF_WIDGETSUBTITLECOLOR, mSubtitleColor)
			.commit();
			finish();
			break;
		case R.id.cancelButton:
			finish();
			break;
		}

	}

	public static class ListContentFragment extends Fragment {
		private String mText;

		@Override
		public void onAttach(Activity activity) {
			// This is the first callback received; here we can set the text for
			// the fragment as defined by the tag specified during the fragment
			// transaction
			super.onAttach(activity);
			mText = getTag();
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
			// This is called to define the layout for the fragment;
			// we just create a TextView and set its text to be the fragment tag
			TextView text = new TextView(getActivity());
			text.setText(mText);
			return text;
		}
	}

}
