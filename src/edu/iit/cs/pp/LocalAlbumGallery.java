package edu.iit.cs.pp;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.usablenet.walgreen.appathon.entities.AlbumThumbnailBean;
import com.usablenet.walgreen.appathon.entities.AlertDataBean;
import com.usablenet.walgreen.appathon.entities.PhotoAlbum;
import com.usablenet.walgreen.appathon.entities.UserInfoBean;
import com.usablenet.walgreen.appathon.sdk.CustomerInfo;
import com.usablenet.walgreen.appathon.sdk.UploadProgressListener;
import com.usablenet.walgreen.appathon.sdk.UploadStatus;
import com.usablenet.walgreen.appathon.sdk.UploadStatusListener;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContext;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContextException;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContextFactory;
import com.usablenet.walgreen.appathon.utils.Alert;
import com.usablenet.walgreen.appathon.utils.Common;
import com.usablenet.walgreen.appathon.utils.Constants;

public class LocalAlbumGallery extends Activity implements OnItemClickListener,
		OnClickListener {
	public static ArrayList<String> mUploadedImagesPathList = new ArrayList<String>();
	public static Bitmap[] mThumbnails;
	private boolean[] mThumbnailsselection;
	public static String[] mArrPath;
	private ImageAdapter mImageAdapter;
	private PhotoAlbum mAlbum = null;
	private int mTotalSize = 0;
	String mAlbumName = "";
	GridView mImagegrid;
	boolean mIsGalleryLoaderActivated = false;
	private int mPrevSelectedPosition = -1;// when no image is selected
	private boolean isInMultiselectedMode = false;
	public final static String CHECKOUTURL = "CHECK_OUT_URL";
	public final static String SESSION_COOKIE = "SessionCookie";
	private Button mBtnPrint = null;
	public static WagCheckoutContext mApiContext = null;
	private ProgressDialog mProgDialog = null;
	private ProgressBar mProgressbar = null;
	volatile boolean mIsActivityInForeground = false;
	boolean mNeedToDisplayAlert = false;
	private AlertDataBean mMaintainAlertData = null;
	AlertDialog mUploadCancelAlertBox = null;
	private final String mEnablePrintTextColor = "#e01935";
	private ProgressDialog myProgressBar;
	private TextView txtUploadStatus;
	UserInfoBean user;
	private SharedPreferences prefs;

	// private volatile boolean mIsDestroyed = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		// mIsDestroyed = false;
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_local_album_gallery);
		mBtnPrint = (Button) findViewById(R.id.btn_print);
		Bundle extras = getIntent().getExtras();
		mIsActivityInForeground = true;
		if (extras != null) {
			mAlbumName = extras.getString(LocalAlbumsList.ALBUM_NAME);
			isInMultiselectedMode = true;
		}
		boolean isSDCardPresent = Common.hasSDCardMounted();
		if (true == isSDCardPresent) {
			new GalleryLoaderTask().execute(null, null, null);
		} else {

			Common.showToast(LocalAlbumGallery.this,
					getString(R.string.toast_sdcard_msg));
		}

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mIsActivityInForeground = true;
		// disable the print button on successful session complete.
		if (mUploadedImagesPathList.size() > 0) {
			enablePrintButton();
		}
		// For the Alerts which has to be displayed when app is background
		if (mNeedToDisplayAlert) {
			mNeedToDisplayAlert = false;
			if (mMaintainAlertData != null) {
				showAlert(mMaintainAlertData.getAlertTitle(),
						mMaintainAlertData.getAlertMesg());
			}
		}
		boolean isSDCardPresent = Common.hasSDCardMounted();
		if (true == isSDCardPresent) {
			if (!mIsGalleryLoaderActivated
					&& PhotoAlbum.getTotalAlbumsSize() == 0) {
				new GalleryLoaderTask().execute(null, null, null);
			} else {
				if (mImageAdapter != null) {
					// reload the images.
					mImageAdapter.notifyDataSetChanged();
				}
			}
		} else {
			Common.showToast(LocalAlbumGallery.this,
					getString(R.string.toast_sdcard_msg));
		}

	}

	@Override
	protected void onStop() {
		super.onStop();
		mIsActivityInForeground = false;
	}

	@Override
	protected void onDestroy() {
		// mIsDestroyed = true;
		super.onDestroy();
	}

	public String[] getPhotoAlbums() {
		ArrayList<String> albumNames = new ArrayList<String>();
		String[] projection = new String[] { MediaStore.Images.Media._ID,
				MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
				MediaStore.Images.Media.BUCKET_ID,
				MediaStore.Images.Media.DISPLAY_NAME,
				MediaStore.Images.Media.DATA };

		// Get the base URI for the People table in the Contacts content
		// provider.
		Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

		// Make the query.
		Cursor cur = managedQuery(images, projection, // Which columns to return
				"", // Which rows to return (all rows)
				null, // Selection arguments (none)
				"" // Ordering
		);
		PhotoAlbum.clearAll();
		if (cur != null && cur.moveToFirst()) {
			String albumName, imageName, imageURI;
			int ImageID, albumID;

			int imageIdColumn = cur.getColumnIndex(MediaStore.Images.Media._ID);
			int imageNameColumn = cur
					.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME);
			int albumIdColumn = cur
					.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
			int albumNameColumn = cur
					.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
			int dataColumnIndex = cur
					.getColumnIndex(MediaStore.Images.Media.DATA);

			do {
				// Get the field values
				ImageID = cur.getInt(imageIdColumn);
				imageName = cur.getString(imageNameColumn);
				albumID = cur.getInt(albumIdColumn);
				albumName = cur.getString(albumNameColumn);
				imageURI = cur.getString(dataColumnIndex);

				if (isValidImage(imageURI)) {
					if (albumNames.size() == 0) {
						PhotoAlbum.addToPhotoAlbumsList(PhotoAlbum.createAlbum(
								albumID, albumName, ImageID, imageName,
								imageURI, AlbumThumbnailBean.getThumb(
										getApplicationContext(), ImageID,
										imageURI)));
						albumNames.add(albumName);
					} else {
						if (!albumNames.contains(albumName)) {
							PhotoAlbum.addToPhotoAlbumsList(PhotoAlbum
									.createAlbum(albumID, albumName, ImageID,
											imageName, imageURI,
											AlbumThumbnailBean.getThumb(
													getApplicationContext(),
													ImageID, imageURI)));
							albumNames.add(albumName);

						} else {
							// If Album Already exists add Thumbnail to Album
							PhotoAlbum album = PhotoAlbum
									.getPhotoAlbum(albumName);
							album.addToAlbumThumbnailList(new AlbumThumbnailBean(
									ImageID, imageName, imageURI));
						}
					}
				}
			} while (cur.moveToNext());

		}
		if (albumNames.size() >= 2) {
			return albumNames.toArray(new String[albumNames.size() + 1]);
		}
		return albumNames.toArray(new String[albumNames.size()]);
	}

	private boolean isValidImage(String imageURI) {

		int mid = imageURI.lastIndexOf(".");
		String ext = imageURI.substring(mid + 1, imageURI.length());
		if ((ext.equalsIgnoreCase("jpg")) || (ext.equalsIgnoreCase("jpeg"))
				|| (ext.equalsIgnoreCase("png"))) {
			return true;
		}
		return false;
	}

	public void displayConfirmationAlert(final Context context, String Title,
			String Message) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
		alertbox.setTitle(Title);
		alertbox.setIcon(R.drawable.alert_arrow);
		alertbox.setMessage(Message);
		alertbox.setCancelable(false);
		alertbox.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						mIsGalleryLoaderActivated = false;
						mImageAdapter.notifyDataSetChanged();
						if (mUploadedImagesPathList.size() > 0) {
							enablePrintButton();
						}
					}
				});
		alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				mIsGalleryLoaderActivated = false;
				mImageAdapter.notifyDataSetChanged();
				// Start Html CHeck out.
				// Intent htmlFlow = new Intent(LocalAlbumGallery.this,
				// Html5CheckoutContainer.class);
				// startActivity(htmlFlow);

			}

		});
		alertbox.show();
	}

	public void printHtmlCheckoutAlert(final Context context, String Message) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
		alertbox.setIcon(R.drawable.alert_arrow);
		alertbox.setMessage(Message);
		alertbox.setCancelable(false);
		alertbox.setPositiveButton("Yes",
				new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
						mHasImageInLocalCart = false;
						mImageAdapter.notifyDataSetChanged();
						startActivity(new Intent(LocalAlbumGallery.this,
								Html5CheckoutContainer.class));

					}
				});
		alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				mImageAdapter.notifyDataSetChanged();
			}

		});
		alertbox.show();
	}

	public static void destroyApiContext() {
		try {
			if (mApiContext != null) {
				mApiContext.destroy();
			}
		} catch (WagCheckoutContextException e1) {
			if (Constants.DEBUG) {
				Log.e("LocalGallery", "destroy Error Code:" + e1.getErrorCode());
				e1.printStackTrace();
			}
		}
		mApiContext = null;
	}

	private void saveAlertContent(String Title, String Mesg) {
		if (mMaintainAlertData == null) {
			mMaintainAlertData = new AlertDataBean();
		}
		mMaintainAlertData.setAlertTitle(Title);
		mMaintainAlertData.setAlertMesg(Mesg);
	}

	private void showAlert(String Title, String Mesg) {
		if (mIsActivityInForeground) {
			mNeedToDisplayAlert = false;
			if (Mesg.equalsIgnoreCase(getString(R.string.quick_print_Confirm_Mesg))) {
				// ConfirmationAlert
				displayConfirmationAlert(LocalAlbumGallery.this, Title, Mesg);
			} else if (Title
					.equalsIgnoreCase(getString(R.string.quick_print_Max_Limit_Mesg_Title))) {
				showAlertAndNavigateToHtml(Title, Mesg);
			} else {
				// Service Unavailble Messages
				Alert.showAlert(LocalAlbumGallery.this, Title, Mesg);
			}
		} else {
			mNeedToDisplayAlert = true;
			saveAlertContent(Title, Mesg);
		}
	}

	private void showAlertAndNavigateToHtml(String Title, String Mesg) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(
				LocalAlbumGallery.this);
		alertbox.setTitle(Title);
		alertbox.setMessage(Mesg);
		alertbox.setIcon(R.drawable.alert_icon);
		alertbox.setCancelable(false);
		alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				// Start Html CHeck out.
				Intent htmlFlow = new Intent(LocalAlbumGallery.this,
						Html5CheckoutContainer.class);
				startActivity(htmlFlow);
				finish();
			}
		});
		alertbox.show();
	}

	private class GalleryLoaderTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog mProgressDialog;

		@Override
		protected void onPreExecute() {
			mIsGalleryLoaderActivated = true;
			mProgressDialog = ProgressDialog.show(LocalAlbumGallery.this,
					"Loading", "Please wait...", false, false);
			mProgressDialog
					.setOnKeyListener(new DialogInterface.OnKeyListener() {

						@Override
						public boolean onKey(DialogInterface dialog,
								int keyCode, KeyEvent event) {
							if (keyCode == KeyEvent.KEYCODE_SEARCH
									&& event.getRepeatCount() == 0) {
								return true; // Pretend we processed it
							}
							return false; // Any other keys are still processed
											// as normal
						}
					});
		}

		@Override
		protected Void doInBackground(Void... params) {
			if (PhotoAlbum.getTotalAlbumsSize() == 0) {
				LocalAlbumsList.mAlbumNames = getPhotoAlbums();
				if (LocalAlbumsList.mAlbumNames.length > 2) {
					LocalAlbumsList.mAlbumNames[LocalAlbumsList.mAlbumNames.length - 1] = "All";
				}
			}
			if (mAlbumName.equals("All")) {
				loadTotalAlbumsImageThumbs();
			} else {
				loadSelectedAlbumImageThumbs(mAlbumName);

			}
			// loadTotalAlbumsImageThumbs();
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			mProgressDialog.dismiss();
			mIsGalleryLoaderActivated = false;
			mImagegrid = (GridView) findViewById(R.id.phoneImageGrid);
			mImagegrid.setOnItemClickListener(LocalAlbumGallery.this);
			mImageAdapter = new ImageAdapter();
			mImagegrid.setAdapter(mImageAdapter);
		}

		@Override
		protected void onCancelled() {
			mProgressDialog.dismiss();
			mIsGalleryLoaderActivated = false;
		}
	}

	@Override
	public void onClick(View pView) {
		int id = pView.getId();

		switch (id) {

		case R.id.btn_camera:

			boolean isSDCardPresent = Common.hasSDCardMounted();
			if (true == isSDCardPresent) {
				cleanTheMemory();
			} else {
				Common.showToast(LocalAlbumGallery.this,
						getString(R.string.toast_sdcard_msg));
			}

			break;
		case R.id.btn_print:
			startUpload();

			break;
		}
	}

	private ArrayList<File> mUploadFileArray = new ArrayList<File>();
	private static final int MAX_IMAGES_LOAD_SIZE = 5;

	private final int DISSMISS_DIALOG = 111;

	private final int DIALOG_INITALIZE_FAILED = 112;

	private final int DIALOG_EMPTY_UPLOAD = 113;

	private final int DIALOG_NO_NETWORK = 114;

	private final int DIALOG_NO_SD_CARD = 115;

	private final int START_UPLOAD = 116;

	private final int DIALOG_MAX_UPLOAD = 117;

	private boolean mHasImageInLocalCart = false;
	private int mUploadFailureCount = 0;
	private int count = 0;

	Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			case DISSMISS_DIALOG:
				dismissDialog();
				break;
			case DIALOG_INITALIZE_FAILED:
				dismissDialog();
				showDialog("Sorry", "Initalize Failed");
				break;
			case DIALOG_EMPTY_UPLOAD:
				dismissDialog();
				showDialog("Alert", "Please select any one image");
				break;
			case DIALOG_MAX_UPLOAD:
				dismissDialog();
				showDialog("Alert", "Please do not select more than 5 images");
				break;
			case DIALOG_NO_NETWORK:
				dismissDialog();
				showDialog(
						"Connection Failed",
						"We�were�unable�to�establish�a connection. Please�check�your settings and try�again.");
				break;
			case DIALOG_NO_SD_CARD:
				dismissDialog();
				showDialog("Sorry",
						"SD Card Not Available.Please Insert SD Card");
				break;
			case START_UPLOAD:
				dismissDialog();
				uploadPhoto();
				break;
			default:
				break;
			}
		};
	};

	private void dismissDialog() {
		if (myProgressBar != null && myProgressBar.isShowing()) {
			myProgressBar.dismiss();
		}
	}

	private void showConfirmDialog(String title, String msg) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setTitle("Upload More Images");
		alertbox.setMessage("Do you want to upload more images");
		alertbox.setPositiveButton("Yes", null);
		alertbox.setNegativeButton("No", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				mHasImageInLocalCart = false;
				mImageAdapter.notifyDataSetChanged();
				startActivity(new Intent(LocalAlbumGallery.this,
						Html5CheckoutContainer.class));
			}
		});
		alertbox.show();
	}

	private void showDialog(String title, String msg) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setTitle(title);
		alertbox.setMessage(msg);
		alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {

				dialog.dismiss();
				mImageAdapter.notifyDataSetChanged();

			}
		});
		alertbox.show();
	}

	private boolean initializeSdk() {
		if (mApiContext == null) {
			try {
				mHasImageInLocalCart = false;
				mApiContext = WagCheckoutContextFactory
						.createContext(getApplication());
			} catch (WagCheckoutContextException e1) {
				e1.printStackTrace();
			}
			try {
				WagCheckoutContext.EnvironmentType environment = null;

				if (Constants.IS_PRODUCTION_DEMO) {
					environment = WagCheckoutContext.EnvironmentType.PRODUCTION;
				} else {
					environment = WagCheckoutContext.EnvironmentType.DEVELOPMENT;
				}
				CustomerInfo customerInfo = new CustomerInfo();
				user = getUser();
				if (user != null) {

					customerInfo.setFirstName(user.getFirstName());
					customerInfo.setLastName(user.getLastName());
					customerInfo.setEmail(user.email);
					customerInfo.setPhone(user.phNumber);
					if (Constants.DEBUG) {
						Log.i("First Name>>>>>>>>>> ", "" + user.getFirstName());
						Log.i("Last Name>>>>>>>>>> ", "" + user.getLastName());
						Log.i("Aff Id>>>>>>>>>> ", "" + user.getAff_Id());
						Log.i("API Key>>>>>>>>>> ", "" + user.getApiKey());
						Log.i("checkout URL >>>>>>>>>> ",
								"" + user.getCheckoutUrl());
					}

					mApiContext.init(user.getAff_Id(), user.getApiKey(),
							customerInfo, Constants.PUBLISHER_ID, null,
							environment, "3.1.1");
				}

			} catch (WagCheckoutContextException e) {

				e.printStackTrace();
				return false;
			}
			return true;
		}
		return false;
	}

	private UserInfoBean getUser() {
		UserInfoBean user = new UserInfoBean();
		if (Constants.sHasToShowSettingScreen) {
			String mJsonUser = prefs.getString("Share_userInfo", null);
			try {

				if (null != mJsonUser) {
					JSONObject j = new JSONObject(mJsonUser);
					user.fName = j.getString(SettingsMenu.FIRST_NAME);
					user.lName = j.getString(SettingsMenu.LAST_NAME);
					user.email = j.getString(SettingsMenu.EMAIL);
					user.phNumber = j.getString(SettingsMenu.PHONE_NUMBER);
					user.affId = j.getString(SettingsMenu.AFF_ID);
					user.apiKey = j.getString(SettingsMenu.APP_KEY);
					user.checkoutUrl = j.getString(SettingsMenu.CHECKOUT_URL);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return user;
	}

	private void startUpload() {
		mUploadFileArray.clear();
		if (!Common.isInternetAvailable(LocalAlbumGallery.this)) {
			enablePrintButton();
			Alert.showAlert(LocalAlbumGallery.this,
					getString(R.string.alert_InternetConnection_title),
					getString(R.string.alert_InternetConnection));
			return;
		}
		int size = mThumbnailsselection.length;
		int count = 0;
		for (int i = 0; i < size; i++) {
			if (mThumbnailsselection[i]) {
				mUploadFileArray.add(new File(mArrPath[i]));
				mThumbnailsselection[i] = false;
				count++;

			}

		}

		if (mUploadFileArray.size() == 0) {

			if (mHasImageInLocalCart) {
				printHtmlCheckoutAlert(LocalAlbumGallery.this,
						getString(R.string.quick_print_confirm_upload_mesg));
			} else {
				handler.sendEmptyMessage(DIALOG_EMPTY_UPLOAD);
			}

			return;
		}
		if (mUploadFileArray.size() > MAX_IMAGES_LOAD_SIZE) {
			handler.sendEmptyMessage(DIALOG_MAX_UPLOAD);
			return;
		}
		if (mApiContext == null) {
			myProgressBar = ProgressDialog.show(LocalAlbumGallery.this,
					"Initializing", "Please wait...", false, false);
		}

		new Thread(new Runnable() {

			@Override
			public void run() {

				doProcess();

			}
		}).start();
	}

	void doProcess() {
		if (mApiContext == null) {

			if (!initializeSdk()) {

				handler.sendEmptyMessage(DIALOG_INITALIZE_FAILED);
				mApiContext = null;
			} else {
				mUploadFailureCount = 0;
				handler.sendEmptyMessage(START_UPLOAD);
			}

		} else {
			mUploadFailureCount = 0;
			handler.sendEmptyMessage(START_UPLOAD);
		}

	}

	private void uploadPhoto() {
		count = 0;
		final int totalCount = mUploadFileArray.size();

		mProgDialog = new ProgressDialog(LocalAlbumGallery.this);
		mProgDialog.setCancelable(false);
		mProgDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true; // Pretend we processed it
				}
				return false; // Any other keys are still processed as
				// normal
			}
		});
		mProgDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

			@Override
			public void onCancel(DialogInterface dialog) {
				dialog.dismiss();

				if (Constants.DEBUG) {
					Log.d(Constants.LOG_TAG, "Canceled Called");
				}
				mImageAdapter.notifyDataSetChanged();
				try {
					if (mApiContext != null) {
						mApiContext.cancelUploads();
					}

				} catch (WagCheckoutContextException e) {

					e.printStackTrace();
				}

			}
		});
		mProgDialog.show();

		mProgDialog.setContentView(R.layout.custom_progressbar_layout);
		mProgressbar = (ProgressBar) mProgDialog
				.findViewById(R.id.progressbar1);
		mProgressbar.setMax(mUploadFileArray.size() * 100);

		txtUploadStatus = (TextView) mProgDialog
				.findViewById(R.id.text_Upload_status);
		mApiContext.setUploadProgressListener(new UploadProgressListener() {

			@Override
			public void onProgress(final double percentage, File file) {
				if (Constants.DEBUG) {
					Log.i(Constants.LOG_TAG, (int) percentage + " %");
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						txtUploadStatus.setText("Uploading photo(s) "
								+ (count + 1) + " of " + totalCount);
						mProgressbar.setProgress((count * 100)
								+ (int) percentage);
					}
				});

			}
		});

		Location loc = getCurrentLocation(LocalAlbumGallery.this);
		if (null != loc) {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG, "Latitude : " + loc.getLatitude());
				Log.i(Constants.LOG_TAG, "Longitude : " + loc.getLongitude());
			}
		}
		mApiContext.setUploadStatusListener(new UploadStatusListener() {

			@Override
			public void onSuccess(File file) {
				mHasImageInLocalCart = true;
				count++;
				// myProgressBar.setMessage("( "+(count++)+" out of "+totalCount+" )");

				if (Constants.DEBUG) {
					Log.i(Constants.LOG_TAG, "Uploaded Images : " + file);
				}
			}

			@Override
			public void onError(WagCheckoutContextException ex, File file) {
				if (Constants.DEBUG) {
					if (ex.getErrorCode() == WagCheckoutContextException.ERR_CODE_FILE_ALREADY_ADDED) {
						mUploadFailureCount++;
					}
					Log.i(Constants.LOG_TAG,
							"Uploaded ErrorCode : " + ex.getErrorCode()
									+ " MSG : " + ex.getMessage());
					if (file != null) {
						Log.i(Constants.LOG_TAG,
								"File Name : " + file.getAbsolutePath());
					}

					if (ex.getErrorCode() == WagCheckoutContextException.ERR_CODE_NO_FILES_TO_UPLOAD) {
						if (mProgDialog != null && mProgDialog.isShowing()) {

							mProgDialog.dismiss();
						}
						mImageAdapter.notifyDataSetChanged();
						showDialog("Failure",
								"There is no file to upload. All images have been uploaded already.");
					}

				}

			}

			@Override
			public void onComplete(ArrayList<UploadStatus> statusList) {

				if (Constants.DEBUG) {

					if (statusList != null) {
						for (UploadStatus status : statusList) {
							Log.i(Constants.LOG_TAG,
									" Status File : " + status.getFile()
											+ " : " + status.getStatusCode());
						}
					}
				}

				if (mProgDialog != null && mProgDialog.isShowing()) {

					mProgDialog.dismiss();
				}
				mImageAdapter.notifyDataSetChanged();
				if (mUploadFailureCount > 0) {
					showDialog("Failure", "Already uploaded image count : "
							+ mUploadFailureCount);
				} else {
					showConfirmDialog("", "");
				}

			}

			@Override
			public void onCancelUpload() {

				if (Constants.DEBUG) {
					Log.d(Constants.LOG_TAG, "Upload Canceled");
				}
				mImageAdapter.notifyDataSetChanged();
				if (mProgDialog != null && mProgDialog.isShowing()) {

					mProgDialog.dismiss();
				}

			}
		});
		mApiContext.uploadImages(mUploadFileArray);

	}

	public Location getCurrentLocation(Context context) {

		LocationManager lm;
		String bestProvider;

		try {
			lm = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);

			if (lm == null) {

				return null;
			}

			Criteria criteria = new Criteria();

			bestProvider = lm.getBestProvider(criteria, true);

			lm.requestLocationUpdates(bestProvider, 0, 0,
					new LocationListener() {

						@Override
						public void onStatusChanged(String provider,
								int status, Bundle extras) {
						}

						@Override
						public void onProviderEnabled(String provider) {
						}

						@Override
						public void onProviderDisabled(String provider) {
						}

						@Override
						public void onLocationChanged(Location location) {
						}
					});
		} catch (Exception e) {

			return null;
		}

		return lm.getLastKnownLocation(bestProvider);
	}

	private void cleanTheMemory() {
		mThumbnails = null;
		mThumbnailsselection = null;
		mArrPath = null;
		PhotoAlbum.clearAll();// Clearing all the bitmaps which are loaded.For
		// Saving the memory
		mTotalSize = 0;
		mPrevSelectedPosition = -1;
		System.gc();
	}

	/*
	 * void addPictureDialog() { Intent intent; intent = new
	 * Intent(LocalAlbumGallery.this, ImagePreview.class);
	 * intent.putExtra("IsFromLocalAlbum", true); startActivity(intent); }
	 */

	private void loadSelectedAlbumImageThumbs(String albumName) {
		mAlbum = PhotoAlbum.getPhotoAlbum(albumName);
		mTotalSize = mAlbum.getAlbumThumbnailList().size();
		mThumbnails = new Bitmap[mTotalSize];
		mArrPath = new String[mTotalSize];
		this.mThumbnailsselection = new boolean[mTotalSize];
		for (int i = 0; i < mTotalSize; i++) {
			mArrPath[i] = mAlbum.getAlbumThumbnailList().get(i).getURI();
			mThumbnails[i] = AlbumThumbnailBean.getThumb(
					getApplicationContext(), mAlbum.getAlbumThumbnailList()
							.get(i).getThumbNailID(), mArrPath[i]);

		}

	}

	private void loadTotalAlbumsImageThumbs() {
		ArrayList<PhotoAlbum> albumList = PhotoAlbum.getPhotoAlbumsList();
		if (albumList.size() > 0) {
			mTotalSize = PhotoAlbum.getTotalAlbumsSize();
			mThumbnails = new Bitmap[mTotalSize];
			mArrPath = new String[mTotalSize];
			this.mThumbnailsselection = new boolean[mTotalSize];
			int index = 0;
			for (PhotoAlbum photoAlbum : albumList) {
				for (int i = 0; i < photoAlbum.getAlbumThumbnailList().size(); i++) {
					mArrPath[index] = photoAlbum.getAlbumThumbnailList().get(i)
							.getURI();
					mThumbnails[index] = AlbumThumbnailBean.getThumb(
							getApplicationContext(), photoAlbum
									.getAlbumThumbnailList().get(i)
									.getThumbNailID(), mArrPath[index]);

					index++;
				}
			}
		}
	}

	public class ImageAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ImageAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public int getCount() {
			return mTotalSize;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = mInflater.inflate(
						R.layout.local_album_gallery_item, null);
				holder.mimageview = (ImageView) convertView
						.findViewById(R.id.thumbImage);
				holder.mcheckbox = (CheckBox) convertView
						.findViewById(R.id.itemCheckBox);
				holder.muploadedText = (TextView) convertView
						.findViewById(R.id.txt_uploaded);
				holder.mPreviewText = (TextView) convertView
						.findViewById(R.id.txt_preview);
				holder.mNotText = (TextView) convertView
						.findViewById(R.id.txt_not);
				holder.mAvailableText = (TextView) convertView
						.findViewById(R.id.txt_available);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.mcheckbox.setOnClickListener(new OnClickListener() {

				public void onClick(View v) {
					CheckBox cb = (CheckBox) v;
					FrameLayout parentLayout = (FrameLayout) v.getParent();
					ImageView imgView = (ImageView) parentLayout.getChildAt(0);
					callCheckBoxValidations(cb, imgView);
				}

			});
			holder.mcheckbox.setId(position);
			holder.mimageview.setId(position);
			if (mThumbnails[position] != null) {
				holder.mimageview.setImageBitmap(mThumbnails[position]);
				holder.mimageview.setBackgroundColor(Color.BLACK);
				disableNoPreviewText(holder);
				disableUploadedText(holder);
			} else {

				// when bitmap image is null or out of memeory
				Bitmap bmp = BitmapFactory.decodeResource(getResources(),
						R.drawable.white_preview_img);
				holder.mimageview.setImageBitmap(bmp);
				holder.mimageview.setBackgroundColor(Color
						.parseColor("#ababad"));
				holder.mimageview.setAlpha(80);
				enableNoPreviewText(holder);
				disableUploadedText(holder);
			}
			if (mThumbnailsselection[position]) {

				holder.mcheckbox.setChecked(mThumbnailsselection[position]);
				holder.mcheckbox.setVisibility(View.VISIBLE);
				holder.mimageview.setAlpha(80);
				disableUploadedText(holder);
			} else {

				holder.mcheckbox.setChecked(mThumbnailsselection[position]);
				holder.mcheckbox.setVisibility(View.GONE);
				if (mThumbnails[position] != null) {
					holder.mimageview.setAlpha(255);
					disableUploadedText(holder);
				}
			}
			if (isUploadedImage(mArrPath[position])) {
				holder.mimageview.setAlpha(80);
				holder.mimageview.setBackgroundColor(Color.BLACK);
				disableNoPreviewText(holder);
				enableUploadedText(holder);
			}
			holder.id = position;
			return convertView;
		}

	}

	private void enableUploadedText(ViewHolder holder) {
		holder.muploadedText.setVisibility(View.VISIBLE);
	}

	private void disableUploadedText(ViewHolder holder) {
		holder.muploadedText.setVisibility(View.GONE);
	}

	private void enableNoPreviewText(ViewHolder holder) {
		holder.mPreviewText.setVisibility(View.VISIBLE);
		holder.mNotText.setVisibility(View.VISIBLE);
		holder.mAvailableText.setVisibility(View.VISIBLE);
	}

	private void disableNoPreviewText(ViewHolder holder) {
		holder.mPreviewText.setVisibility(View.GONE);
		holder.mNotText.setVisibility(View.GONE);
		holder.mAvailableText.setVisibility(View.GONE);
	}

	private boolean isUploadedImage(String path) {

		return mUploadedImagesPathList.contains(path);

	}

	class ViewHolder {
		ImageView mimageview;
		CheckBox mcheckbox;
		TextView muploadedText;
		TextView mPreviewText;
		TextView mNotText;
		TextView mAvailableText;
		int id;
	}

	private void callCheckBoxValidations(CheckBox cb, ImageView imgView) {
		CheckBox PrevSelectedCheckBox = null;
		ImageView PrevSelectedImageView = null;
		int id = cb.getId();
		if (isUploadedImage(mArrPath[id])) {
			return;
		}
		if (!isInMultiselectedMode && mPrevSelectedPosition != id) {
			if (mPrevSelectedPosition != -1) {
				try {
					mThumbnailsselection[mPrevSelectedPosition] = false;
					FrameLayout parentLayout = (FrameLayout) mImagegrid
							.getChildAt(mPrevSelectedPosition
									- mImagegrid.getFirstVisiblePosition());
					PrevSelectedCheckBox = (CheckBox) parentLayout
							.getChildAt(1);
					PrevSelectedImageView = (ImageView) parentLayout
							.getChildAt(0);
					PrevSelectedCheckBox.setVisibility(View.GONE);
					// This check is for No preview images
					if (mThumbnails[mPrevSelectedPosition] != null) {
						PrevSelectedImageView.setAlpha(255);
					}
				} catch (Exception e) {
					Log.e("LocalGallery", e.getMessage() + " "
							+ mPrevSelectedPosition);
				}
			}
			mPrevSelectedPosition = id;
		} else {
			mPrevSelectedPosition = -1;
		}

		if (mThumbnailsselection[id]) {
			cb.setVisibility(View.GONE);
			cb.setChecked(false);
			// This check is for No preview images
			if (mThumbnails[id] != null) {
				imgView.setAlpha(255);
			}
			mThumbnailsselection[id] = false;

		} else {
			cb.setVisibility(View.VISIBLE);
			cb.setChecked(true);
			imgView.setAlpha(80);
			mThumbnailsselection[id] = true;
			enablePrintButton();

		}

		if (Constants.DEBUG) {
			Log.d(Constants.LOG_TAG, "previously selected image id = "
					+ mPrevSelectedPosition);
		}
	}

	private void enablePrintButton() {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mBtnPrint.setTextColor(Color.parseColor(mEnablePrintTextColor));
				mBtnPrint.setClickable(true);
			}
		});

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View view, int pPosition,
			long arg3) {
		FrameLayout parentLayout = (FrameLayout) view;
		CheckBox cb = (CheckBox) parentLayout.getChildAt(1);
		ImageView imgView = (ImageView) parentLayout.getChildAt(0);
		callCheckBoxValidations(cb, imgView);

	}

	// ---------------------- for settings option menu------------------------
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (Constants.sHasToShowSettingScreen) {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.homemenu, menu);
		}
		return super.onPrepareOptionsMenu(menu);

	}

	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.settings:
			intent = new Intent(this, SettingsMenu.class);
			startActivity(intent);
			break;
		}
		return true;
	}
	// ------------------ end settings option menu ------------------

}