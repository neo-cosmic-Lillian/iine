package jp.upset.horoscope;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.GeolocationPermissions.Callback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.google.ads.AdView;
import com.google.android.gcm.GCMRegistrar;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class MainActivity extends Activity implements OnClickListener {

	private final static String MSG_ACTION = "msg_count";
	private final static String PUSH_ACTION = "registerPush";
	private final static String LOCATION_UPDATE = "location_update";

	private TabView[] mTabs;
	private WebView mWebView;
	private ProgressDialog mProgress;
	private AdView adView;
	private boolean needCleanHistory = false;
	private static String[] urls = Preference.Urls;
	// private Map<String, String> headers;
	private String param = "";
	private LocationManager mLocationManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initGCM();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		LinearLayout layout = (LinearLayout) this
				.findViewById(R.id.linearLayout1);
		layout.getLayoutParams().height = getDisplayWidth() / 5;

		mTabs = new TabView[4];
		mTabs[0] = (TabView) this.findViewById(R.id.tab1);
		mTabs[1] = (TabView) this.findViewById(R.id.tab2);
		mTabs[2] = (TabView) this.findViewById(R.id.tab3);
		mTabs[3] = (TabView) this.findViewById(R.id.tab4);
		mWebView = (WebView) this.findViewById(R.id.webView1);
		mProgress = new ProgressDialog(this);
		mProgress.setMax(100);


		mTabs[0].setOnClickListener(this);
		mTabs[1].setOnClickListener(this);
		mTabs[2].setOnClickListener(this);
		mTabs[3].setOnClickListener(this);

		adView = (AdView) this.findViewById(R.id.adView);

		mWebView.setWebChromeClient(new WebChromeClient() {
			@Override
			public void onProgressChanged(WebView wv, int progress) {
				mProgress.setProgress(progress);
				mProgress.setMessage(progress + "%");
			}

			public void onGeolocationPermissionsShowPrompt(String origin,
					Callback callback) {
				// TODO Auto-generated method stub
				callback.invoke(origin, true, true);
			}
		});

		mWebView.getSettings().setGeolocationDatabasePath(
				"/data/data/jp.upset.horoscope");
		mWebView.getSettings().setJavaScriptEnabled(true);
		mWebView.getSettings().setGeolocationEnabled(true);
		mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
		mWebView.getSettings().setSavePassword(false);
		mWebView.getSettings().setAppCacheEnabled(true);
		mWebView.getSettings().setDatabaseEnabled(false);
		mWebView.getSettings().setDomStorageEnabled(true);
		mWebView.getSettings().setSupportZoom(false);
		mWebView.setWebViewClient(mClient);
		mWebView.getSettings()
				.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);

		if (!isNetworkConnected(this)) {
			new AlertDialog.Builder(this)
					.setTitle(R.string.no_network_connection)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									finish();
								}
							}).setNegativeButton(R.string.cancel, null)
					.create().show();
		}

		// headers = new HashMap<String, String>();
		// headers.put("Authorization",
		// Preference.getUuid(this));
		param = "?" + "Authorization=" + Preference.getUuid(this);
		
		int tab = getIntent().getIntExtra("tab", 0);
		
		loadUrl(urls[tab]);
		mTabs[tab].setSelected(true);
		// loadUrl("http://www.google.com");
		mTabs[2].setCount(Preference.getMessageCount(this));

		IntentFilter filter = new IntentFilter();
		filter.addAction(MSG_ACTION);
		filter.addAction(PUSH_ACTION);
		filter.addAction(LOCATION_UPDATE);
		this.registerReceiver(MessageReceiver, filter);

	}

	private void initGCM() {
		// TODO Auto-generated method stub
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		String regId = GCMRegistrar.getRegistrationId(this);
		if (regId.equals("")) {
			GCMRegistrar.register(this, "682751756494");
		} else {
			registerPush(regId);
			Log.i("aa", regId);
		}
	}

	private void registerPush(String regId) {
		AsyncHttpClient mHttpClient = new AsyncHttpClient();
		RequestParams param = new RequestParams();
		param.put("device_id", Preference.getUuid(this));
		param.put("registration_ids", regId);
		mHttpClient.post(urls[4], param, new AsyncHttpResponseHandler() {

			@Override
			public void onFailure(Throwable arg0, String arg1) {
				// TODO Auto-generated method stub
				super.onFailure(arg0, arg1);
			}

			@Override
			public void onSuccess(String arg0) {
				// TODO Auto-generated method stub
				super.onSuccess(arg0);
				Log.i("aa", arg0);
			}

		});
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	public int getDisplayWidth() {
		DisplayMetrics metrics = new DisplayMetrics();
		WindowManager wm = (WindowManager) this
				.getSystemService(Context.WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(metrics);
		return metrics.widthPixels;
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if (mWebView.canGoBack()) {
			mWebView.goBack();
		} else {
			new AlertDialog.Builder(this)
					.setTitle(R.string.confirm_to_exit)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									finish();
								}
							}).setNegativeButton(R.string.cancel, null)
					.create().show();
		}
	}

	@Override
	public void onDestroy() {
		adView.destroy();
		this.unregisterReceiver(MessageReceiver);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		mTabs[0].setSelected(v.getId() == R.id.tab1);
		mTabs[1].setSelected(v.getId() == R.id.tab2);
		mTabs[2].setSelected(v.getId() == R.id.tab3);
		mTabs[3].setSelected(v.getId() == R.id.tab4);
		switch (v.getId()) {
		case R.id.tab1:
			loadUrl(urls[0]);
			break;

		case R.id.tab2:
			loadUrl(urls[1]);
			break;

		case R.id.tab3:
			loadUrl(urls[2]);
			break;

		case R.id.tab4:
			loadUrl(urls[3]);
			break;

		}
		needCleanHistory = true;

	}

	private void loadUrl(String url) {
		if (url.equals(urls[0])) {
			Intent i = new Intent();
			i.setAction(LOCATION_UPDATE);
			PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, 0);
			Criteria criteria = new Criteria();
			criteria.setAccuracy(Criteria.ACCURACY_COARSE);
			criteria.setPowerRequirement(Criteria.POWER_LOW);
			criteria.setAltitudeRequired(false);
			criteria.setBearingRequired(false);
			criteria.setSpeedRequired(false);
			criteria.setCostAllowed(false);
			mLocationManager.requestSingleUpdate(
					criteria, pi);
		}
		mWebView.loadUrl(url + param);
	}

	private WebViewClient mClient = new WebViewClient() {

		@Override
		public void doUpdateVisitedHistory(WebView view, String url,
				boolean isReload) {
			// TODO Auto-generated method stub
			super.doUpdateVisitedHistory(view, url, isReload);
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			// TODO Auto-generated method stub
			loadUrl(url);
			return true;
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			// TODO Auto-generated method stub
			super.onPageStarted(view, url, favicon);
			mProgress.show();

		}

		@Override
		public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			super.onPageFinished(view, url);
			mProgress.dismiss();
			if (needCleanHistory) {
				view.clearHistory();
				needCleanHistory = false;
			}
		}

	};

	private boolean isNetworkConnected(Context context) {
		if (context != null) {
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo mNetworkInfo = mConnectivityManager
					.getActiveNetworkInfo();
			if (mNetworkInfo != null) {
				return mNetworkInfo.isAvailable();
			}
		}
		return false;
	}

	private BroadcastReceiver MessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			Log.i("aa", "onReceive " + intent.getAction());
			String action = intent.getAction();
			if (action.equals(MSG_ACTION)) {
				int msg_count = intent.getIntExtra("msg_count", 0);
				mTabs[2].setCount(msg_count);
				mTabs[2].invalidate();
			} else if (action.endsWith(PUSH_ACTION)) {
				String reg_id = intent.getStringExtra("reg_id");
				if (!reg_id.equals("")) {
					registerPush(reg_id);
				}
			} else {
				Log.i("aa", intent.toString());
				if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
					Bundle b = intent.getExtras();
					Location l = (Location) b
							.get(LocationManager.KEY_LOCATION_CHANGED);
					double lat = l.getLatitude();
					double lng = l.getLongitude();
					
					AsyncHttpClient mHttpClient = new AsyncHttpClient();
					RequestParams param = new RequestParams();
					param.put("lat", Double.toString(lat));
					param.put("lng", Double.toString(lng));
					param.put("mac", Preference.getUuid(MainActivity.this));
					
					mHttpClient.post(urls[5], param, new AsyncHttpResponseHandler() {

						@Override
						public void onFailure(Throwable arg0, String arg1) {
							// TODO Auto-generated method stub
							super.onFailure(arg0, arg1);
						}

						@Override
						public void onSuccess(String arg0) {
							// TODO Auto-generated method stub
							super.onSuccess(arg0);
							Log.i("aa", arg0);
						}

					});
				}

			}

		}

	};

}
