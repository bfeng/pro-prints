/*
* Copyright 2012 Walgreen Co. All rights reserved *

* Licensed under the Walgreens Developer Program and Portal Terms of Use and API License Agreement, Version 1.0 (the �Terms of Use�)
* You may not use this file except in compliance with the License.
* A copy of the License is located at https://developer.walgreens.com/page/terms-use
*
* This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
* express or implied. See the License for the specific language governing  permissions and limitations under the License.
*/
package edu.iit.cs.pp;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.usablenet.walgreen.appathon.entities.AlbumThumbnailBean;
import com.usablenet.walgreen.appathon.entities.PhotoAlbum;
import com.usablenet.walgreen.appathon.utils.Common;

public class LocalAlbumsList extends Activity implements
		OnItemClickListener, OnClickListener {

	public static String[] mAlbumNames = null;
	public static final String ALBUM_NAME = "ALBUMNAME";
	public static final String SELECTION_MODE = "SELECTION_MODE";
	ListView list = null;
	ProgressDialog mProgressDialog;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_local_album_list);

		boolean isSDCardPresent = Common.hasSDCardMounted();
		if (true == isSDCardPresent) {
			new AlbumLoaderTask().execute(null, null, null);
		} else {
			
			Common.showToast(LocalAlbumsList.this, getString(R.string.toast_sdcard_msg));
			
		}

	}
	
	private void showOnScreenMesg(String Mesg)
	{
		TextView emptyAlbumListText=(TextView)findViewById(R.id.txt_view_album_list_empty);
		emptyAlbumListText.setText(Mesg);
		emptyAlbumListText.setVisibility(View.VISIBLE);
		ListView albumList=(ListView)findViewById(R.id.list);
		albumList.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View pView) {
		int id = pView.getId();

		switch (id) {

		case R.id.btn_camera:

			boolean isSDCardPresent = Common.hasSDCardMounted();
			if (true == isSDCardPresent) {
				
			} else {
				Common.showToast(LocalAlbumsList.this, getString(R.string.toast_sdcard_msg));
			}

			break;
		}
	}

    

	private class AlbumLoaderTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			mProgressDialog = ProgressDialog.show(LocalAlbumsList.this,
					"Loading", "Please wait...", false, false);
			mProgressDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {

			    @Override
			    public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			        if (keyCode == KeyEvent.KEYCODE_SEARCH && event.getRepeatCount() == 0) {
			            return true; // Pretend we processed it
			        }
			        return false; // Any other keys are still processed as normal
			    }
			});
		}

		@Override
		protected Void doInBackground(Void... params) {
			mAlbumNames = getPhotoAlbums();
			if (mAlbumNames != null && mAlbumNames.length > 2) {
				mAlbumNames[mAlbumNames.length - 1] = "All";
			}
			return null;

		}

		@Override
		protected void onPostExecute(Void result) {
			mProgressDialog.dismiss();
			if (mAlbumNames != null && mAlbumNames.length > 0) {
				list = (ListView) findViewById(R.id.list);
				list.setOnItemClickListener(LocalAlbumsList.this);
				list.setAdapter(new AlbumAdapter());
			} else {
				showOnScreenMesg(getString(R.string.quick_print_empty_album_mesg));
			}
		}

		@Override
		protected void onCancelled() {
			mProgressDialog.dismiss();
		}
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
		if (cur != null) {
			if (cur.getCount() == PhotoAlbum.getTotalAlbumsSize())
				return mAlbumNames;
		}
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

	public class AlbumAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public AlbumAdapter() {
			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@Override
		public int getCount() {

			return mAlbumNames.length;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		AlbumListHolder holder;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				holder = new AlbumListHolder();
				convertView = mInflater.inflate(R.layout.local_albumlistrow,
						null);
				holder.lImg = (ImageView) convertView
						.findViewById(R.id.ablumicon);
				holder.lName = (TextView) convertView
						.findViewById(R.id.ablumtitle);
				holder.lCount = (TextView) convertView
						.findViewById(R.id.ablum_img_count);
			} else {
				holder = (AlbumListHolder) convertView.getTag();
			}
			PhotoAlbum album = PhotoAlbum.getPhotoAlbum(mAlbumNames[position]);

			if (mAlbumNames[position].equals("All")) {
				PhotoAlbum firstAlbum = PhotoAlbum
						.getPhotoAlbum(mAlbumNames[0]);
				if (firstAlbum != null) {
					if (firstAlbum.getAlbumThumb() != null) {
						holder.lImg.setImageBitmap(firstAlbum.getAlbumThumb());
					} else {
						holder.lImg
								.setImageResource(R.drawable.preview_not_vailable);
					}

					holder.lName.setText(mAlbumNames[position]);
					holder.lCount.setText(" ("
							+ PhotoAlbum.getTotalAlbumsSize() + ")");
				}
			} else {
				if (album.getAlbumThumb() != null) {
					holder.lImg.setImageBitmap(album.getAlbumThumb());
				} else {
					holder.lImg
							.setImageResource(R.drawable.preview_not_vailable);
				}
				holder.lName.setText(mAlbumNames[position]);
				holder.lCount.setText(" ("
						+ album.getAlbumThumbnailList().size() + ")");
			}
			convertView.setTag(holder);
			return convertView;
		}

	}

	class AlbumListHolder {
		ImageView lImg;

		TextView lName, lCount;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {

		Intent intent = new Intent(this, LocalAlbumGallery.class);
		intent.putExtra(ALBUM_NAME, "" + mAlbumNames[position]);
		startActivity(intent);

	}
	
	
}
