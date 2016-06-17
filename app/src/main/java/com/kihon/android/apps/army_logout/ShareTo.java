package com.kihon.android.apps.army_logout;

import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestAsyncTask;
import com.facebook.Response;
import com.facebook.Session;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.support.v4.app.NavUtils;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ShareTo extends ActionBarActivity {
	
	private static final String TAG = "ShareTo";
	
	private static final List<String> PERMISSIONS = Arrays.asList("publish_actions");
	private static final String PENDING_PUBLISH_KEY = "pendingPublishReauthorization";

	private static final int mId = 0;
	private boolean pendingPublishReauthorization = false;
	
	private byte[] photo_data = null;

	private ProgressDialog pDialog;
	
	private View mPostScreen;
	private Bitmap mPostbm;
	
	private ImageView mScreen;
	private LinearLayout mINF;
	private TextView mCurrentDay;
	private TextView mCurrentYear;
	private ProgressBar mLoginProgressBar; 
	private TextView mLoginPrecent;
	private TextView mText1;
	private TextView mLocationText;
	
	private Uri outputFileUri;
	private Intent intent =  new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
	private boolean openCam = true;
	
	private static String POST_MESSAGE = "";
	
	private File saved_image_file;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shareto);

		// findView
		mScreen = (ImageView) findViewById(R.id.imageView);
		mINF = (LinearLayout) findViewById(R.id.share_inf);
		mCurrentYear = (TextView) findViewById(R.id.textView_year);
		mCurrentDay = (TextView) findViewById(R.id.textView_today);
		mLoginProgressBar = (ProgressBar) findViewById(R.id.login_progressBar);
		mLoginPrecent = (TextView) findViewById(R.id.login_Percent);
		mText1 = (TextView) findViewById(R.id.share_to_logout_text);
		mLocationText = (TextView) findViewById(R.id.place_textview);
		
		mText1.setText(MainActivity.login_yet ? "天之後就脫離陰間正式退伍了!" :  "天後就要進入陰間服役了!!" ); 
		
		mScreen.setScaleType(ScaleType.CENTER_CROP);
		
		//get resolution
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
	
		
		//set alpha
		Drawable background = mINF.getBackground();
		background.setAlpha(40);
		
		//read current date (mCurrentDate)
		SimpleDateFormat sdf = new SimpleDateFormat("M月dd日" , Locale.TAIWAN);
		String today = sdf.format(new Date());
		
		if(MainActivity.sLogoutYear != 0){
			mCurrentYear.setVisibility(View.VISIBLE);
			mCurrentYear.setText(String.valueOf(MainActivity.sLogoutYear)+"年");	
		} else {
			mCurrentYear.setVisibility(View.GONE);
		}
		
		
		mCurrentDay.setText(String.valueOf(MainActivity.sLogoutDay));
		mLocationText.setText("");
		
		String percentText = new DecimalFormat("#.##").format(MainActivity.sLoginPercent);
		if (MainActivity.sLoginPercent >= 100) {
			mLoginPrecent.setText("100%");
		} else if (MainActivity.sLoginPercent <= 0) {
			mLoginPrecent.setText("0%");
		} else {
			mLoginPrecent.setText(percentText + "%");
		}
		
		mLoginProgressBar.setProgress((int) MainActivity.sLoginPercent);
		
		pDialog = new ProgressDialog(ShareTo.this);
		pDialog.setMessage("張貼中");
		pDialog.setIndeterminate(true);
		pDialog.setCancelable(false);// 無法利用back鍵退出
		
		openCam = getIntent().getBooleanExtra("openCam", true);
		
		if(openCam){
			takePic();
		} else {
			pickPic();
		}
		
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);
	
		/*
		Bitmap bitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/IMAG0001.jpg");
		File file = new File(Environment.getExternalStorageDirectory() + "/IMAG0001.jpg");
		Bitmap bitmap = decodeFile(file, metrics.widthPixels, metrics.heightPixels);
		
		mScreen.setImageBitmap(bitmap);
		*/
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.shareto, menu);
	    return true;
	}
	
	private void pickPic(){
		// 建立 "選擇檔案 Action" 的 Intent
		Intent intent = new Intent(Intent.ACTION_PICK);

		// 過濾檔案格式
		intent.setType("image/*");

		// 建立 "檔案選擇器" 的 Intent (第二個參數: 選擇器的標題)
		Intent destIntent = Intent.createChooser(intent, "選擇檔案");

		// 切換到檔案選擇器 (它的處理結果, 會觸發 onActivityResult 事件)
		startActivityForResult(destIntent, 1);
	}
	
	private void takePic(){
		File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		File image_file = new File(path + "/take_pic.jpg");
		outputFileUri = Uri.fromFile(image_file);

		intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
		startActivityForResult(intent, 0);
	}

	private void printPic(Uri uri) {
		//get resolution
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);

		File file = new File(openCam ? uri.getPath() : getRealPathFromURI(uri));

		Matrix matrix = new Matrix();
		float rotation = rotationForImage(this, uri);
		if (rotation != 0f) {
		     matrix.preRotate(rotation);
		}
		
		Bitmap bmp;
		if (uri.toString().startsWith("content://com.google.android.apps.photos.content")) {
			bmp = decodeFile(uri, 800, 800);
		} else {
			bmp = decodeFile(file, 800, 800); // 利用BitmapFactory去取得剛剛拍照的圖像
		}

		Bitmap resizedBitmap = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		mScreen.setImageResource(0);
		mScreen.setImageBitmap(resizedBitmap);

	}

	public static Bitmap decodeFile(File f, int WIDTH, int HIGHT) {
		try {
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(f), null, o);

			// The new size we want to scale to
			final int REQUIRED_WIDTH = WIDTH;
			final int REQUIRED_HIGHT = HIGHT;
			// Find the correct scale value. It should be the power of 2.
			int scale = 1;
			while (o.outWidth / scale / 2 >= REQUIRED_WIDTH && o.outHeight / scale / 2 >= REQUIRED_HIGHT)
				scale *= 2;

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
		} catch (FileNotFoundException e) {
		}
		return null;
	}
	
	public Bitmap decodeFile(Uri uri, int WIDTH, int HIGHT) {
		try {
			InputStream in = getContentResolver().openInputStream(uri);
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, o);
			in.close();

			// Calculate inSampleSize
			int scale = calculateInSampleSize(o, WIDTH, HIGHT);
			
			in = getContentResolver().openInputStream(uri);
			o = new BitmapFactory.Options();
			o.inSampleSize = scale;

			Bitmap bmp = BitmapFactory.decodeStream(in, null, o);
			in.close();

			return bmp;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and
			// keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case 0:
			if (resultCode == RESULT_OK) {
				printPic(outputFileUri);
			}
			break;
		case 1:
			// 有選擇檔案
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				if (uri != null) {
					printPic(uri);
				}
			}
			break;
		}
		
		if (mScreen.getDrawable() == null) 
			finish();

	}
	
	public String getRealPathFromURI(Uri contentUri) {
		if (contentUri.toString().startsWith("content://com.google.android.apps.photos.content"))
			return contentUri.toString();

		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
		int column_index = 0;
		if (cursor.getCount() != 0) {
			column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			Log.d(TAG, cursor.getString(column_index));
			return cursor.getString(column_index);
		}
		return null;
    }
	
	protected void saveImage(Bitmap bmScreen2) {
		File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
		saved_image_file = new File(path + "/captured_screen.jpg");
		if (saved_image_file.exists())
			saved_image_file.delete();
		try {
			FileOutputStream out = new FileOutputStream(saved_image_file);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			bmScreen2.compress(Bitmap.CompressFormat.JPEG, 95, out);
			bmScreen2.compress(Bitmap.CompressFormat.JPEG, 95, baos);
			photo_data = baos.toByteArray();
			out.flush();
			out.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initNotification(String MSG, String Ticker, boolean fin) {
        //Set the activity to be launch when selected
		
		Intent notificationIntent;
		
		if (!fin) {
	        notificationIntent = new Intent(getApplicationContext(), MainActivity.class);
		} else {
	        String uri = "fb://profile/";
	        notificationIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
		}

		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        

        
		NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
				.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentTitle("國軍弟兄返陽倒數計時器")
				.setContentText(MSG)
				.setTicker(Ticker)
				.setAutoCancel(true)
				.setDefaults(Notification.DEFAULT_VIBRATE)
				.setWhen(System.currentTimeMillis())
				.setContentIntent(contentIntent);
		
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(mId, mBuilder.build());
		
		
	}
	
	private Runnable mPublishStoryRunnable = new Runnable() {	
		@Override
		public void run() {
			publishStory();
//			pDialog.dismiss();
			finish();
		}
	};
	
	private void shareAction(){
		mPostScreen = (View) findViewById(R.id.shareto_framelayout);
		mPostScreen.setDrawingCacheEnabled(true);
		mPostbm = mPostScreen.getDrawingCache();
		saveImage(mPostbm);
		mPostScreen.setDrawingCacheEnabled(false);
		
//		File path = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
//		File image_file = new File(path + "/captured_screen.jpg");
		Uri shareFileUri = Uri.fromFile(saved_image_file);

		Intent shareIntent = new Intent();
		shareIntent.setAction(Intent.ACTION_SEND);
		shareIntent.putExtra(Intent.EXTRA_STREAM, shareFileUri);
		shareIntent.setType("image/jpeg");
		startActivity(Intent.createChooser(shareIntent, "選擇照片的方式"));
		
		Log.d(TAG, saved_image_file.getPath());
	}

	private void publishStory() {

		Session session = Session.getActiveSession();

		if (session != null) {

			// Check for publish permissions
			List<String> permissions = session.getPermissions();
			if (!isSubsetOf(PERMISSIONS, permissions)) {
				pendingPublishReauthorization = true;
				Session.NewPermissionsRequest newPermissionsRequest = new Session.NewPermissionsRequest(ShareTo.this, PERMISSIONS);
				session.requestNewPublishPermissions(newPermissionsRequest);
				return;
			}

			/**
			 * photo cap.
			 */

			mPostScreen = (View) findViewById(R.id.shareto_framelayout);
			mPostScreen.setDrawingCacheEnabled(true);
			mPostbm = mPostScreen.getDrawingCache();
			saveImage(mPostbm);

			initNotification("正在上傳...", "國軍返陽計時器：分享至Facebook中...", false);

			Bundle postParams = new Bundle();
			postParams.putString("message", POST_MESSAGE);
			postParams.putByteArray("picture", photo_data);

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
						Toast.makeText(ShareTo.this.getApplicationContext(), error.getErrorMessage(), Toast.LENGTH_SHORT).show();
					} else {
						// Toast.makeText(ShareTo.this.getApplicationContext(),postId, Toast.LENGTH_LONG).show();
						initNotification("張貼成功!", "國軍返陽計時器：已分享至Facebook", true);
						Toast.makeText(ShareTo.this.getApplicationContext(), "張貼成功!", Toast.LENGTH_LONG).show();
						finish();
						Log.d(TAG, postId);
					}
				}
			};

			Request request = new Request(session, "me/photos", postParams, HttpMethod.POST, callback);

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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_shareto_post:
			shareAction();
			return true;
		case R.id.action_shareto_change:
			CharSequence[] items = { getString(R.string.take_picture), getString(R.string.picture_select) };
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(which == 0){
						takePic();
					} else {
						pickPic();
					}
				}
			});
			builder.create();
			builder.show();
			return true;
		case android.R.id.home:
			NavUtils.navigateUpFromSameTask(this);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void closeSoftKeyboard(IBinder iBinder){
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(iBinder, 0);
	}
	
	public float rotationForImage(Context context, Uri uri) {
		if (uri.getScheme().equals("content")) {
			String[] projection = { Images.ImageColumns.ORIENTATION };
			Cursor c = context.getContentResolver().query(uri, projection, null, null, null);
			if (c.moveToFirst()) {
				return c.getInt(0);
			}
		} else if (uri.getScheme().equals("file")) {
			try {
				ExifInterface exif = new ExifInterface(uri.getPath());
				int rotation = (int) exifOrientationToDegrees(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL));
				return rotation;
			} catch (IOException e) {
				Log.e(TAG, "Error checking exif :" + e);
			}
		}
		return 0f;
	}

	private float exifOrientationToDegrees(int exifOrientation) {
		if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
			return 90;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
			return 180;
		} else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
			return 270;
		}
		return 0;
	}

}
