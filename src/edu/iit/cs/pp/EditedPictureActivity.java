package edu.iit.cs.pp;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONObject;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.usablenet.walgreen.appathon.entities.UserInfoBean;
import com.usablenet.walgreen.appathon.sdk.CustomerInfo;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContext;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContextException;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContextFactory;
import com.usablenet.walgreen.appathon.utils.Constants;

public class EditedPictureActivity extends Activity {

	private ImageView mImageView;
	private Button printBtn;

	public static WagCheckoutContext mApiContext = null;
	private SharedPreferences prefs;

	private ArrayList<File> mUploadFileArray = new ArrayList<File>();
	UserInfoBean user;

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

	private boolean initializeSdk() {
		if (mApiContext == null) {
			try {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edited_picture);

		printBtn = (Button) findViewById(R.id.btn_print);
		printBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Uri path = (Uri) getIntent().getParcelableExtra("path");
				final File pic = new File(getRealPathFromURI(path));
				new Thread(new Runnable() {

					@Override
					public void run() {

						if (mApiContext == null) {

							if (!initializeSdk()) {

								mUploadFileArray.add(pic);
								mApiContext.uploadImages(mUploadFileArray);

								mApiContext = null;
							} else {
							}

						} else {
						}

					}
				}).start();
			}
		});

		mImageView = (ImageView) findViewById(R.id.iv_photo);

		Bitmap photo = (Bitmap) this.getIntent().getParcelableExtra("data");

		mImageView.setImageBitmap(photo);
	}

	public String getRealPathFromURI(Uri contentUri) {
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, null, null, null);
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);
	}

}
