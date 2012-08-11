package edu.iit.cs.pp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

/**
 * This is the main entry point from which the app gets started. Class name is
 * generated by the Eclipse IDE, may be changed later.
 * 
 * @author Bo
 * 
 */
public class MainActivity extends Activity {

	Button connectGoogle;
	Button connectFacebook;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		connectGoogle = (Button) findViewById(R.id.button1);
		connectFacebook = (Button) findViewById(R.id.button2);

		connectGoogle.setOnClickListener(new ServiceConnector("google"));
		connectFacebook.setOnClickListener(new ServiceConnector("facebook"));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private class ServiceConnector implements View.OnClickListener {

		private String serviceName;

		public ServiceConnector(String serviceName) {
			// TODO Auto-generated constructor stub
			this.serviceName = serviceName;
		}

		public void onClick(View v) {
			// TODO Auto-generated method stub
			// Intent picManage = new Intent(v.getContext(),
			// PicManageActivity.class);
			// startActivity(picManage);

			if (serviceName.equals("google")) {
				Intent localAlbumsList = new Intent(v.getContext(),
						LocalAlbumsList.class);
				startActivity(localAlbumsList);
			} else {
				Intent intent = new Intent();

				intent.setType("image/*");
				intent.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(
						Intent.createChooser(intent, "Complete action using"),
						0);
			}

		}

	}
}
