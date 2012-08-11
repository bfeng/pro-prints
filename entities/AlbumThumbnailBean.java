/*
* Copyright 2012 Walgreen Co. All rights reserved *

* Licensed under the Walgreens Developer Program and Portal Terms of Use and API License Agreement, Version 1.0 (the “Terms of Use”)
* You may not use this file except in compliance with the License.
* A copy of the License is located at https://developer.walgreens.com/page/terms-use
*
* This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing  permissions and limitations under the License.
*/
package com.usablenet.walgreen.appathon.entities;

import com.usablenet.walgreen.appathon.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

public class AlbumThumbnailBean {
	private int imageID;
	private String imageName;
	private String URI;

	/*
	 * public void setThumb(Bitmap TumbnailImg) { Tumbnail=TumbnailImg; }
	 */
	public static Bitmap getThumb(Context context, int ImageID, String path) {
		try {
			return MediaStore.Images.Thumbnails.getThumbnail(context
					.getContentResolver(), ImageID,
					MediaStore.Images.Thumbnails.MICRO_KIND, null);// Try to
			// Genrate
			// Bitmap
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return getDecodedBitmap(context);// Decode Bitmap if it exceeds
												// memory.
	}

	private static Bitmap getDecodedBitmap(Context context) {
		try {
			
			Bitmap thumbnail = null;
			thumbnail = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.preview_not_vailable);
			
			return thumbnail;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void setThumbNailID(int ID) {
		imageID = ID;
	}

	public int getThumbNailID() {
		return imageID;
	}

	public void setThumbNailName(String Name) {
		imageName = Name;
	}

	public String getThumbNailName() {
		return imageName;
	}

	public void setURI(String uri) {
		URI = uri;
	}

	public String getURI() {
		return URI;
	}

	public AlbumThumbnailBean(int ID, String Name, String uri) {
		setThumbNailID(ID);
		setThumbNailName(Name);
		setURI(uri);
	}
}
