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
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;

import com.usablenet.walgreen.appathon.entities.UserInfoBean;
import com.usablenet.walgreen.appathon.sdk.CustomerInfo;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContext;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContextException;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContextFactory;
import com.usablenet.walgreen.appathon.utils.Constants;

public class EditedPictureActivity extends Activity implements OnClickListener {

	private ImageView mImageView;
	private Button printBtn;

	public static WagCheckoutContext mApiContext = null;
	private SharedPreferences prefs;

	private ArrayList<File> mUploadFileArray = new ArrayList<File>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edited_picture);

		printBtn = (Button) findViewById(R.id.btn_print);

		printBtn.setOnClickListener(this);

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

	@Override
	public void onClick(View v) {
		Uri path = (Uri) getIntent().getParcelableExtra("path");
		Log.i(path.toString().substring(7).toString(), "PATH NAME");
		final File pic = new File(path.toString().substring(7));

		// TODO Auto-generated method stub
		if (mApiContext == null) {
			try {
				mApiContext = WagCheckoutContextFactory.createContext(getApplication());
				mApiContext.init(Constants.AFF_ID, Constants.PROD_API_KEY,null, null, null, WagCheckoutContext.EnvironmentType.DEVELOPMENT, "1.0.1");
				mApiContext.uploadImages(mUploadFileArray);
			}
			catch (WagCheckoutContextException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			mUploadFileArray.add(pic);
			mApiContext.uploadImages(mUploadFileArray);
		}
	}

}
