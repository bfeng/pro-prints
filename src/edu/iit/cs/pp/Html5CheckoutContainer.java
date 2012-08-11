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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.HttpAuthHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.usablenet.walgreen.appathon.sdk.RemoteCart;
import com.usablenet.walgreen.appathon.sdk.WagCheckoutContextException;
import com.usablenet.walgreen.appathon.utils.Common;
import com.usablenet.walgreen.appathon.utils.Constants;

public class Html5CheckoutContainer extends Activity {

	ProgressDialog myProgressBar;

	WebView wv;

	int myProgress = 0;

	LinearLayout header;

	TextView titleText;
	private boolean mAllowBackNavigation = false;
	private Button mCancelBtn = null;
	private Button mHomeBtn = null;
	private String mUrl;
	private List<String> mSessionCookie;
	private final int CHECKOUT_LOAD_ERROR = 1111;

	private final int CHECKOUT_SHOW_TITLE = 200;
	private final int CHECKOUT_CANCEL_BUTTON_STATE = 201;
	private final int CHECKOUT_CANCEL_BUTTON_CLICKED = 202;

	public final String INTERFACE_NAME = "quickprint";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.html5checkoutcontainer);

		titleText = (TextView) findViewById(R.id.texttitle);
		wv = (WebView) findViewById(R.id.wvwebcontainer);
		mCancelBtn = (Button) findViewById(R.id.btnCancel);
		mHomeBtn = (Button) findViewById(R.id.headerhomeicon);

		// Call Cart Poster Async Task.
		if (Common.isInternetAvailable(Html5CheckoutContainer.this)) {
			proceedForCheckout();
		} else {
			displayErrorAlert(Html5CheckoutContainer.this,
					getString(R.string.alert_InternetConnection_title),
					getString(R.string.alert_InternetConnection));
		}

	}

	private void proceedForCheckout() {
		wv.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
		wv.getSettings().setSaveFormData(false);
		wv.getSettings().setSavePassword(false);
		wv.getSettings().setJavaScriptEnabled(true);
		wv.setVerticalScrollBarEnabled(false);
		wv.setHorizontalScrollBarEnabled(false);

		wv.addJavascriptInterface(new CheckoutJavaScriptInterface(),
				INTERFACE_NAME);

		myProgressBar = ProgressDialog.show(Html5CheckoutContainer.this,
				"Loading", "Please wait...");
		myProgressBar.setOnKeyListener(new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true; // Pretend we processed it
				}
				return false; // Any other keys are still processed as normal
			}
		});

		Location location = getLastKnownLocation(Html5CheckoutContainer.this);
		new postCartAsyncTask(location).execute(null, null, null);

		wv.setWebViewClient(new WenContainerWebViewClient());

		final Activity MyActivity = this;
		wv.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView view, int progress) {
				// Make the bar disappear after URL is loaded, and changes
				// string to Loading...
				MyActivity.setTitle("Loading...");
				MyActivity.setProgress(progress * 100); // Make the bar
				// disappear after URL
				// is loaded
				// Return the app name after finish loading
				Message lmsg = myHandle.obtainMessage();
				lmsg.what = progress;
				myHandle.sendMessage(lmsg);
			}
		});
	}

	public Location getLastKnownLocation(Context context) {

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

	private class postCartAsyncTask extends AsyncTask<Void, Void, Void> {
		boolean isHavingError = false;
		private Location mLocation;

		public postCartAsyncTask(Location location) {

			mLocation = location;
		}

		@Override
		protected Void doInBackground(Void... params) {
			RemoteCart cart = null;
			try {
				if (LocalAlbumGallery.mApiContext != null) {
					cart = LocalAlbumGallery.mApiContext.postCart(mLocation);
				}

				if (cart != null && cart.getErrorCode() == null) {
					LocalAlbumGallery.mApiContext.destroy();
					LocalAlbumGallery.mApiContext = null;
					LocalAlbumGallery.mUploadedImagesPathList.clear();// Clear
					// the
					// selected
					// images
					// list
					// LocalAlbumGallery.mSelectedBatchImagesList.clear();//Clearing
					// Batch selections
					mUrl = cart.getCheckoutUrl();
					mSessionCookie = cart.getCookies();

				} else {
					isHavingError = true;
				}
			} catch (WagCheckoutContextException e1) {
				if (Constants.DEBUG) {
					e1.printStackTrace();
					Log.e("WebView", "ErrCode:" + e1.getErrorCode());
				}
				isHavingError = true;

			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			if (!isHavingError) {
				CookieSyncManager.createInstance(Html5CheckoutContainer.this);
				CookieManager cookieManager = CookieManager.getInstance();
				cookieManager.removeSessionCookie();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if (mSessionCookie != null) {
					final int size = mSessionCookie.size();
					for (int i = 0; i < size; i++) {
						cookieManager.setCookie(mUrl, mSessionCookie.get(i)
								.toString());
					}
				}

				CookieSyncManager.getInstance().sync();
				if (Constants.DEBUG) {
					Log.d("WebView", "mUrl:" + mUrl);
				}
				wv.loadUrl(mUrl);
			} else {
				if (myProgressBar != null) {
					myProgressBar.dismiss();
				}

				LocalAlbumGallery.destroyApiContext();
				LocalAlbumGallery.mUploadedImagesPathList.clear();// Clear
				showAlert(Html5CheckoutContainer.this,
						getString(R.string.quick_print_UploadFailed_title),
						getString(R.string.quick_print_checkout_err_mesg));

			}
		}

		private void showAlert(Context context, String Title, String Message) {
			AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
			alertbox.setTitle(Title);
			alertbox.setMessage(Message);
			alertbox.setCancelable(false);
			alertbox.setPositiveButton("OK",
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();

						}
					});
			alertbox.show();
		}

		@Override
		protected void onCancelled() {

		}

	}

	// Following Code is For ICS devices

	/*
	 * @Override public void onSaveInstanceState(Bundle savedInstanceState) {
	 * super.onSaveInstanceState(savedInstanceState);
	 * savedInstanceState.putString("mUrl", mUrl); }
	 * 
	 * @Override public void onRestoreInstanceState(Bundle savedInstanceState) {
	 * super.onRestoreInstanceState(savedInstanceState); mUrl =
	 * savedInstanceState.getString("mUrl"); wv.loadUrl(mUrl); }
	 */

	// End
	class CheckoutJavaScriptInterface {

		public void showHTML(String html) {
			Message lmsg = myHandle.obtainMessage();
			lmsg.what = CHECKOUT_SHOW_TITLE;
			lmsg.obj = html;
			myHandle.sendMessage(lmsg);
		}

		public void checkCancelButtonState(String cancelState) {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG,
						"Html5checkoutContainer::checkCancelButtonState");
			}
			Message lmsg = myHandle.obtainMessage();
			lmsg.what = CHECKOUT_CANCEL_BUTTON_STATE;
			lmsg.obj = cancelState;
			myHandle.sendMessage(lmsg);

		}

		public void checkBackButtonState(String backButtonState) {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG,
						"Html5checkoutContainer::checkBackButtonState");
			}
			if (backButtonState.equalsIgnoreCase("true")) {
				mAllowBackNavigation = true;
			} else {
				mAllowBackNavigation = false;
			}
		}

		public void onCancel() {
			finish();
		}

		public void onCheckoutError(int errorCode, String message) {

			if (Constants.DEBUG) {
				Log.e("onCheckoutError", "errorCode:" + errorCode + " message:"
						+ message);
			}

			finish();
		}

		public void onSessionExpired() {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG,
						"Html5checkoutContainer::onSessionExpired");
			}
			navigateToPhotoLanding();
		}

		public void onCheckoutComplete() {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG,
						"Html5checkoutContainer::onCheckoutComplete");
			}
			navigateToPhotoLanding();
		}

		public void onCheckoutCancel() {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG,
						"Html5checkoutContainer::onCheckoutCancel");
			}
			Message lmsg = myHandle.obtainMessage();
			lmsg.what = CHECKOUT_CANCEL_BUTTON_CLICKED;
			myHandle.sendMessage(lmsg);
		}
	}

	private void navigateToPhotoLanding() {
		finish();

	}

	public void displayErrorAlert(final Context context, String Title,
			String Message) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
		alertbox.setTitle(Title);
		alertbox.setMessage(Message);
		alertbox.setCancelable(false);
		alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				finish();
			}
		});
		alertbox.show();
	}

	private void setCancelButtonState(String state) {
		if (state.equalsIgnoreCase("true")) {
			mHomeBtn.setVisibility(View.INVISIBLE);// For Title Alignment
			mCancelBtn.setVisibility(View.VISIBLE);
		} else {
			mCancelBtn.setVisibility(View.GONE);
			mHomeBtn.setVisibility(View.GONE);
		}
	}

	Handler myHandle = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			myProgress = msg.what;

			if (myProgress == CHECKOUT_LOAD_ERROR) {

				displayErrorAlert(Html5CheckoutContainer.this, null,
						getString(R.string.quick_print_checkout_err_mesg));

			} else if (myProgress == CHECKOUT_SHOW_TITLE) {

				titleText.setText(Html.fromHtml(msg.obj.toString()));

			} else if (myProgress == CHECKOUT_CANCEL_BUTTON_STATE) {
				setCancelButtonState(msg.obj.toString());
			} else if (CHECKOUT_CANCEL_BUTTON_CLICKED == myProgress) {
				wv.setWebViewClient(null);
				finish();
				// showAlert(Html5CheckoutContainer.this,
				// null,"Going back will remove all photos from your order. Do you wish to continue?");
			} else {
				// TODO: Error Handling..
			}
		}

	};

	private class WenContainerWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG,
						"shouldOverrideUrlLoading============> Url : " + url);
			}

			view.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onFormResubmission(WebView view, Message dontResend,
				Message resend) {
			resend.sendToTarget();
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG, "onPageFinished============> Url : "
						+ url);
			}

			if (myProgressBar.isShowing()) {
				myProgressBar.dismiss();
			}
			wv.loadUrl("javascript:window." + INTERFACE_NAME + ".showHTML"
					+ "(document.getElementById('pageHeader').innerHTML);");
			wv.loadUrl("javascript:window."
					+ INTERFACE_NAME
					+ ".checkCancelButtonState"
					+ "(document.getElementById('pageHeader').getAttribute('data-CancelBtn'));");
			wv.loadUrl("javascript:window."
					+ INTERFACE_NAME
					+ ".checkBackButtonState"
					+ "(document.getElementById('pageHeader').getAttribute('data-BackBtn'));");

			if (Constants.DEBUG) {
				Log.d(Html5CheckoutContainer.class.getSimpleName(),
						"*****loaded url: " + url);
			}
		}

		@Override
		public void onReceivedHttpAuthRequest(WebView view,
				HttpAuthHandler handler, String host, String realm) {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG,
						"---------onReceivedHttpAuthRequest--------");
			}

			// handler.proceed(Constants.USER_NAME, Constants.PASSWORD);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {

			/*
			 * if (myProgressBar == null) { myProgressBar = ProgressDialog.show(
			 * Html5CheckoutContainer.this, "Loading", "Please wait..."); } else
			 * { myProgressBar.show(); }
			 */
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG, "onPageStarted============> Url : "
						+ url);
			}

			if (myProgressBar != null) {
				myProgressBar.dismiss();
			}
			myProgressBar = ProgressDialog.show(Html5CheckoutContainer.this,
					"Loading", "Please wait...");
			myProgressBar.setOnKeyListener(new DialogInterface.OnKeyListener() {

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
		}

		@Override
		public void onReceivedError(WebView view, int errorCode,
				String description, String failingUrl) {
			if (Constants.DEBUG) {
				Log.i(Constants.LOG_TAG, "---------onReceivedError--------");
			}

			super.onReceivedError(view, errorCode, description, failingUrl);
			if (Constants.DEBUG) {
				Log.e("HTML5CHCKOUT", "onReceivedError errorCode=" + errorCode
						+ " description=" + description + " failingUrl="
						+ failingUrl);
			}
			Message lmsg = myHandle.obtainMessage();
			lmsg.what = CHECKOUT_LOAD_ERROR;
			myHandle.sendMessage(lmsg);
		}

	}

	public void showAlert(Context context, String Title, String Message) {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(context);
		alertbox.setTitle(Title);
		alertbox.setMessage(Message);
		alertbox.setCancelable(false);
		alertbox.setNegativeButton("Cancel", null);
		alertbox.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				wv.setWebViewClient(null);
				finish();
			}
		});
		alertbox.show();
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnCancel:
			showAlert(this, null,
					"All photo prints will be lost if you go back. Would you like to continue?");
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			if (mAllowBackNavigation) {
				if (wv.canGoBack()) {
					wv.goBack();

				} else {
					showAlert(this, "Alert",
							"Going back will remove all photos from your order. Do you wish to continue?");

					return true;
				}
			} else {
				return false;
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		wv.clearCache(true);
		wv.clearFormData();
		wv.clearHistory();
		wv.destroy();

	}

}
