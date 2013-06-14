package jp.upset.horoscope;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
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

	private TabView mTab1, mTab2, mTab3, mTab4;
	private WebView mWebView;
	private ProgressDialog mProgress;
	private AdView adView;
	private boolean needCleanHistory = false;
	private static String[] urls = Preference.Urls;
	// private Map<String, String> headers;
	private String param = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initGCM();

		LinearLayout layout = (LinearLayout) this
				.findViewById(R.id.linearLayout1);
		layout.getLayoutParams().height = getDisplayWidth() / 5;

		mTab1 = (TabView) this.findViewById(R.id.tab1);
		mTab2 = (TabView) this.findViewById(R.id.tab2);
		mTab3 = (TabView) this.findViewById(R.id.tab3);
		mTab4 = (TabView) this.findViewById(R.id.tab4);
		mWebView = (WebView) this.findViewById(R.id.webView1);
		mProgress = new ProgressDialog(this);
		mProgress.setMax(100);

		mTab1.setSelected(true);

		mTab1.setOnClickListener(this);
		mTab2.setOnClickListener(this);
		mTab3.setOnClickListener(this);
		mTab4.setOnClickListener(this);

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

		mWebView.getSettings().setGeolocationDatabasePath("/data/data/jp.upset.horoscope");
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
		loadUrl(urls[0]);

		mTab3.setCount(Preference.getMessageCount(this));

		IntentFilter filter = new IntentFilter();
		filter.addAction(MSG_ACTION);
		filter.addAction(PUSH_ACTION);
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
		mTab1.setSelected(v.getId() == R.id.tab1);
		mTab2.setSelected(v.getId() == R.id.tab2);
		mTab3.setSelected(v.getId() == R.id.tab3);
		mTab4.setSelected(v.getId() == R.id.tab4);
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
				mTab3.setCount(msg_count);
				mTab3.invalidate();
			} else {
				String reg_id = intent.getStringExtra("reg_id");
				if (!reg_id.equals("")) {
					registerPush(reg_id);
				}
			}

		}

	};

}
