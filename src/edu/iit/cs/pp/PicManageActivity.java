package edu.iit.cs.pp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import edu.iit.cs.pp.image.ImageAdapter;

public class PicManageActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pic_manage);

		GridView gallery = (GridView) findViewById(R.id.gridView);
		gallery.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				Log.i("", "id:" + id);
				parent.getItemAtPosition(position);
				Intent picEdit = new Intent(v.getContext(),
						PicEditActivity.class);
				picEdit.putExtra("image",
						(Integer) parent.getItemAtPosition(position));
				startActivity(picEdit);
			}

		});
		gallery.setAdapter(new ImageAdapter(this));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_pic_manage, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		return true;
	}
}
