package edu.iit.cs.pp.image;

import edu.iit.cs.pp.R;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {

	private Context context;

	public ImageAdapter(Context applicationContext) {
		context = applicationContext;
	}

	public int getCount() {
		return thumbIds.length;
	}

	public Object getItem(int position) {
		return thumbIds[position];
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView imageView;
		if (convertView == null) {
			imageView = new ImageView(context);
			imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			imageView.setPadding(5, 5, 5, 5);
		} else {
			imageView = (ImageView) convertView;
		}

		imageView.setImageResource(thumbIds[position]);
		return imageView;
	}

	// references to our images
	private Integer[] thumbIds = { R.drawable.one, R.drawable.two,
			R.drawable.three, R.drawable.four, R.drawable.five, R.drawable.six,
			R.drawable.seven, R.drawable.eight, R.drawable.nine,
			R.drawable.one, R.drawable.two, R.drawable.three, R.drawable.four,
			R.drawable.five, R.drawable.six, R.drawable.seven,
			R.drawable.eight, R.drawable.nine };
}
