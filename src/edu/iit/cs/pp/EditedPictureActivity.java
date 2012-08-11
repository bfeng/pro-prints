package edu.iit.cs.pp;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;

public class EditedPictureActivity extends Activity {
	
	private ImageView mImageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edited_picture);
		
		Bitmap photo = savedInstanceState.getParcelable("data");
		mImageView.setImageBitmap(photo);
	}

}
