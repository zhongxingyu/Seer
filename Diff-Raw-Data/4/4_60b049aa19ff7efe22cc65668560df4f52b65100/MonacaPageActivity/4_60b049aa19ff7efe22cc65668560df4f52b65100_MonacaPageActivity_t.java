 package mobi.monaca.framework;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import mobi.monaca.framework.bootloader.LocalFileBootloader;
 import mobi.monaca.framework.nativeui.UIBuilder;
 import mobi.monaca.framework.nativeui.UIBuilder.ResultSet;
 import mobi.monaca.framework.nativeui.UIContext;
 import mobi.monaca.framework.nativeui.UIUtil;
 import mobi.monaca.framework.nativeui.UpdateStyleQuery;
 import mobi.monaca.framework.nativeui.component.Component;
 import mobi.monaca.framework.nativeui.component.PageOrientation;
 import mobi.monaca.framework.nativeui.container.ToolbarContainer;
 import mobi.monaca.framework.nativeui.menu.MenuRepresentation;
 import mobi.monaca.framework.psedo.R;
 import mobi.monaca.framework.transition.BackgroundDrawable;
 import mobi.monaca.framework.transition.ClosePageIntent;
 import mobi.monaca.framework.transition.TransitionParams;
 import mobi.monaca.framework.util.AssetUriUtil;
 import mobi.monaca.framework.util.BenchmarkTimer;
 import mobi.monaca.framework.util.InputStreamLoader;
 import mobi.monaca.framework.util.MyLog;
 import mobi.monaca.framework.util.UrlUtil;
 import mobi.monaca.framework.view.MonacaPageGingerbreadWebViewClient;
 import mobi.monaca.framework.view.MonacaPageHoneyCombWebViewClient;
 import mobi.monaca.framework.view.MonacaWebView;
 import mobi.monaca.utils.TimeStamp;
 import mobi.monaca.utils.gcm.GCMPushDataset;
 import mobi.monaca.utils.log.LogItem;
 import mobi.monaca.utils.log.LogItem.LogLevel;
 import mobi.monaca.utils.log.LogItem.Source;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.cordova.CordovaWebView;
 import org.apache.cordova.CordovaWebViewClient;
 import org.apache.cordova.DroidGap;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import receiver.ScreenReceiver;
 import android.R.color;
 import android.annotation.TargetApi;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.os.Build.VERSION;
 import android.os.Build.VERSION_CODES;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Handler;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.Display;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewTreeObserver;
 import android.view.WindowManager;
 import android.webkit.WebView;
 import android.webkit.WebViewClient;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
import android.widget.Toast;
 import android.widget.ImageView.ScaleType;
 import android.widget.LinearLayout;
 
 /**
  * This class represent a page of Monaca application.
  */
 public class MonacaPageActivity extends DroidGap {
 	public static final String TRANSITION_PARAM_NAME = "monaca.transition";
 	public static final String URL_PARAM_NAME = "monaca.url";
 	public static final String TAG = MonacaPageActivity.class.getSimpleName();
	protected static final String MONACA_READY_URL = "file:///android_asset/www/plugins/monaca.js/monaca.ready.js";
 
 	protected MonacaURI currentMonacaUri;
 
 	protected Drawable background = null;
 
 	protected HashMap<String, Component> dict;
 
 	protected Handler handler = new Handler();
 
 	protected UIBuilder.ResultSet uiBuilderResult = null;
 
 	protected int pageIndex = 0;
 
 	protected JSONObject appJson;
 
 	protected Dialog monacaSplashDialog;
 
 	private boolean isOnDestroyMonacaCalled = false;
 
 	protected BroadcastReceiver closePageReceiver = new BroadcastReceiver() {
 		public void onReceive(Context context, Intent intent) {
 			int level = intent.getIntExtra("level", 0);
 			if (pageIndex >= level) {
 				finish();
 			}
 			MyLog.d(MonacaPageActivity.this.getClass().getSimpleName(), "close intent received: " + getCurrentUriWithoutQuery());
 			MyLog.d(MonacaPageActivity.this.getClass().getSimpleName(), "page index: " + pageIndex);
 		}
 	};
 
 	protected BroadcastReceiver pushReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			if (isIndex()) {
 				GCMPushDataset p = (GCMPushDataset)intent.getExtras().get(GCMPushDataset.KEY);
 				sendPushToWebView(p);
 			}
 		}
 	};
 
 	/** If this flag is true, activity is capable of transition. */
 	protected boolean isCapableForTransition = true;
 
 	protected UIContext uiContext = null;
 
 	protected TransitionParams transitionParams;
 
 	protected JSONObject infoForJavaScript = new JSONObject();
 	protected String mCurrentHtml;
 	private ScreenReceiver mScreenReceiver;
 
 	protected GCMPushDataset pushData;
 
 	@Override
 	public void onCreate(Bundle savedInstance) {
 		registerReceiver(pushReceiver, new IntentFilter(MonacaNotificationActivity.ACTION_RECEIVED_PUSH));
 		prepare();
 
 		// initialize receiver
 		IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
 		filter.addAction(Intent.ACTION_SCREEN_OFF);
 		mScreenReceiver = new ScreenReceiver();
 		registerReceiver(mScreenReceiver, filter);
 
 		super.onCreate(savedInstance);
 		// to clear
 		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN,
 		// WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN); in DroidGap
 		// class
 		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 		MyLog.v(TAG, "MonacaApplication.getPages().size():" + MonacaApplication.getPages().size());
 
 		// currentMonacaUri is set in prepare()
 		if (MonacaApplication.getPages().size() == 1) {
 			init();
 			loadUri(currentMonacaUri.getUrlWithQuery(), false);
 		} else {
 			init();
 			loadUiFile(getCurrentUriWithoutQuery());
 			handler.postDelayed(new Runnable() {
 				@Override
 				public void run() {
 					loadUri(currentMonacaUri.getUrlWithQuery(), true);
 				}
 			}, 100);
 		}
 
 		// dirty fix for android4's strange bug
 		if (transitionParams.animationType == TransitionParams.TransitionAnimationType.MODAL) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_dialog_open_enter, mobi.monaca.framework.psedo.R.anim.monaca_dialog_open_exit);
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.TRANSIT) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_open_enter, mobi.monaca.framework.psedo.R.anim.monaca_slide_open_exit);
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.NONE) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_none, mobi.monaca.framework.psedo.R.anim.monaca_none);
 		}
 		// root.setBackgroundColor(Color.WHITE);
 
 	}
 
 
 	protected boolean isIndex() {
 		return pageIndex == MonacaApplication.getPages().size() - 1;
 	}
 
 
 	protected Drawable getSplashDrawable() throws IOException {
 		InputStream is = getResources().getAssets().open(MonacaSplashActivity.SPLASH_IMAGE_PATH);
 		return Drawable.createFromStream(is, "splash_default");
 
 	}
 
 	protected int getSplashBackgroundColor() {
 		try {
 			InputStream stream = getResources().getAssets().open("app.json");
 			byte[] buffer = new byte[stream.available()];
 			stream.read(buffer);
 			JSONObject appJson = new JSONObject(new String(buffer, "UTF-8"));
 			String backgroundColorString = appJson.getJSONObject("splash").getJSONObject("android").getString("background");
 			if (!backgroundColorString.startsWith("#")) {
 				backgroundColorString = "#" + backgroundColorString;
 			}
 			int backbroundColor = Color.parseColor(backgroundColorString);
 			return backbroundColor;
 		} catch (JSONException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (IllegalArgumentException e) {
 			MyLog.e(TAG, e.getMessage());
 		} catch (IOException e) {
 			// TODO 自動生成された catch ブロック
 			MyLog.e(TAG, e.getMessage());
 		}
 		return Color.TRANSPARENT;
 	}
 
 	public void showMonacaSplash() {
 		final MonacaPageActivity activity = this;
 
 		Runnable runnable = new Runnable() {
 			public void run() {
 
 				// Get reference to display
 				Display display = activity.getWindowManager().getDefaultDisplay();
 
 				// Create the layout for the dialog
 				FrameLayout root = new FrameLayout(activity.getActivity());
 				root.setMinimumHeight(display.getHeight());
 				root.setMinimumWidth(display.getWidth());
 				root.setBackgroundColor(activity.getSplashBackgroundColor());
 				root.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.0F));
 
 				try {
 					ImageView splashImageView;
 					splashImageView = new ImageView(MonacaPageActivity.this);
 					splashImageView.setImageDrawable(activity.getSplashDrawable());
 					splashImageView.setScaleType(ScaleType.FIT_CENTER);
 
 					root.addView(splashImageView);
 				} catch (IOException e) {
 					MyLog.e(TAG, e.getMessage());
 				}
 
 				// Create and show the dialog
 				monacaSplashDialog = new Dialog(MonacaPageActivity.this, android.R.style.Theme_Translucent_NoTitleBar);
 				// check to see if the splash screen should be full screen
 				if ((getWindow().getAttributes().flags & WindowManager.LayoutParams.FLAG_FULLSCREEN) == WindowManager.LayoutParams.FLAG_FULLSCREEN) {
 					monacaSplashDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 				}
 				monacaSplashDialog.setContentView(root);
 				monacaSplashDialog.setCancelable(false);
 				monacaSplashDialog.show();
 			}
 		};
 		this.runOnUiThread(runnable);
 	}
 
 	public void removeMonacaSplash() {
 		if (monacaSplashDialog != null && monacaSplashDialog.isShowing()) {
 			monacaSplashDialog.dismiss();
 			monacaSplashDialog = null;
 		}
 	}
 
 	@Override
 	public boolean onPrepareOptionsMenu(Menu menu) {
 		MyLog.v(TAG, "onPrepareOptionMenu()");
 		if (uiBuilderResult != null) {
 			MyLog.v(TAG, "building menu");
 
 			menu.clear();
 			MenuRepresentation menuRepresentation = MonacaApplication.findMenuRepresentation(uiBuilderResult.menuName);
 			MyLog.v(TAG, "menuRepresentation:" + menuRepresentation);
 			if (menuRepresentation != null) {
 				menuRepresentation.configureMenu(uiContext, menu);
 			}
 
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	protected void prepare() {
 		Bundle bundle = getIntent().getExtras();
 		if (bundle != null) {
 			if (bundle.getBoolean(MonacaSplashActivity.SHOWS_SPLASH_KEY, false)) {
 				showMonacaSplash();
 				getIntent().getExtras().remove(MonacaSplashActivity.SHOWS_SPLASH_KEY);
 			}
 			Serializable s = bundle.getSerializable(GCMPushDataset.KEY);
 			if (s != null) {
 				pushData = (GCMPushDataset)s;
 			}
 		}
 
 		loadParams();
 
 		MonacaApplication.addPage(this);
 		pageIndex = MonacaApplication.getPages().size() - 1;
 		registerReceiver(closePageReceiver, ClosePageIntent.createIntentFilter());
 		uiContext = new UIContext(getCurrentUriWithoutQuery(), this);
 
 		// override theme
 		if (transitionParams.animationType == TransitionParams.TransitionAnimationType.NONE) {
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.MODAL) {
 			setTheme(mobi.monaca.framework.psedo.R.style.MonacaDialogTheme);
 		} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.TRANSIT) {
 			setTheme(mobi.monaca.framework.psedo.R.style.MonacaSlideTheme);
 		} else {
 		}
 
 		try {
 			infoForJavaScript.put("display", createDisplayInfo());
 		} catch (JSONException e) {
 			throw new RuntimeException(e);
 		}
 
 		// loadBackground(getResources().getConfiguration());
 	}
 
 	/** Load background drawable from transition params and device orientation. */
 	protected void loadBackground(Configuration config) {
 		MyLog.v(TAG, "loadBackground().");
 		if (transitionParams != null && transitionParams.hasBackgroundImage()) {
 			String path = null;
 			String preferedPath = "www/" + UIContext.getPreferredPath(transitionParams.backgroundImagePath);
 			if (AssetUriUtil.existsAsset(this, preferedPath)) {
 				path = preferedPath;
 			} else {
 				path = "www/" + transitionParams.backgroundImagePath;
 			}
 
 			MyLog.v(TAG, "loadBackground(). path:" + path);
 
 			try {
 
 				Bitmap bitmap = BitmapFactory.decodeStream(LocalFileBootloader.openAsset(this.getApplicationContext(), path));
 				background = new BackgroundDrawable(bitmap, getWindowManager().getDefaultDisplay(), config.orientation);
 			} catch (Exception e) {
 				MyLog.e(TAG, e.getMessage());
 			}
 		}
 	}
 
 	/** Release background drawable. */
 	protected void unloadBackground() {
 		if (background != null) {
 			appView.setBackgroundDrawable(null);
 			root.setBackgroundDrawable(null);
 			background.setCallback(null);
 			background = null;
 			System.gc();
 		}
 	}
 
 	public void initMonaca() {
 		appView.setFocusable(true);
 		appView.setFocusableInTouchMode(true);
 
 		/*
 		 * to initialize cordova webView, MonacaWebView detects this symbol and
 		 * passes "javascript:" to CordovaWebView#loadUrlIntoView and
 		 * MonacaPageActivity supresses timeout error message caused by this
 		 */
 		this.loadUrl(MonacaWebView.INITIALIZATION_REQUEST_URL);
 
 		root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
 			@Override
 			public void onGlobalLayout() {
 				try {
 					int height = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight() - root.getHeight();
 					infoForJavaScript.put("statusbarHeight", height);
 				} catch (JSONException e) {
 					MyLog.e(getClass().getSimpleName(), "fail to get statusbar height.");
 				}
 			}
 		});
 
 		// setupBackground();
 
 		// for focus problem between native component and webView
 		appView.setOnTouchListener(new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View v, MotionEvent event) {
 				switch (event.getAction()) {
 				case MotionEvent.ACTION_DOWN:
 				case MotionEvent.ACTION_UP:
 					if (!v.hasFocus()) {
 						v.requestFocus();
 					}
 					break;
 				}
 				return false;
 			}
 		});
 
 	}
 
 	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
 	@Override
 	public void init() {
 		CordovaWebView webView = new MonacaWebView(this);
 
 		// Fix webview bug on ICS_MR1 where webview background is always white when hardware accerleration is on
 		if(VERSION.SDK_INT == VERSION_CODES.ICE_CREAM_SANDWICH_MR1){
 			webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
 		}
 		CordovaWebViewClient webViewClient = (CordovaWebViewClient) createWebViewClient(getCurrentUriWithoutQuery(), this, webView);
 		MonacaChromeClient webChromeClient = new MonacaChromeClient(this, webView);
 		this.init(webView, webViewClient, webChromeClient);
 		this.initMonaca();
 	}
 
 	/** Setup background drawable for app View and root view. */
 	public void setupBackground(Drawable background) {
 		MyLog.v(TAG, "setupBackground()");
 		appView.setBackgroundColor(0x00000000);
 		if (background != null) {
 			MyLog.v(TAG, "background != null");
 			if (appView != null) {
 				MyLog.v(TAG, "appview and background not null -> set to appview");
 				appView.setBackgroundDrawable(background);
 			}
 
 			if (root != null) {
 				root.setBackgroundDrawable(background);
 				MyLog.v(TAG, "set background to root");
 
 				if (root.getParent() == null) {
 					setContentView(root);
 				}
 			}
 		} else {
 			if (appView != null) {
 				MyLog.v(TAG, "setDefaultBackground");
 				// Default background
 				appView.setBackgroundResource(color.white);
 			}
 		}
 	}
 
 	protected void loadLayoutInformation() {
 		appView.loadUrl("javascript: window.__layout = " + infoForJavaScript.toString());
 	}
 
 	protected JSONObject createDisplayInfo() {
 		JSONObject result = new JSONObject();
 
 		DisplayMetrics metrics = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(metrics);
 
 		Display display = getWindowManager().getDefaultDisplay();
 		try {
 			result.put("width", display.getWidth());
 			result.put("height", display.getHeight());
 		} catch (JSONException e) {
 		}
 
 		return result;
 	}
 
 	protected void loadParams() {
 
 		Intent intent = getIntent();
 		transitionParams = (TransitionParams) intent.getSerializableExtra(TRANSITION_PARAM_NAME);
 
 		if (transitionParams == null) {
 			transitionParams = TransitionParams.createDefaultParams(this.getRequestedOrientation());
 		}
 
 		setCurrentUri(intent.hasExtra(URL_PARAM_NAME) ? intent.getStringExtra(URL_PARAM_NAME) : "file:///android_asset/www/index.html");
 
 		MyLog.v(TAG, "uri without query:" + getCurrentUriWithoutQuery());
 		MyLog.v(TAG, "uri with query:" + currentMonacaUri.getUrlWithQuery());
 	}
 
 	public JSONObject getInfoForJavaScript() {
 		return infoForJavaScript;
 	}
 
 	protected boolean hasOpacityBar(ResultSet resultSet) {
 		if (resultSet.top != null && ToolbarContainer.isTransparent(resultSet.top.getStyle().optDouble("opacity", 1.0))) {
 			return true;
 		}
 
 		if (resultSet.bottom != null && ToolbarContainer.isTransparent(resultSet.bottom.getStyle().optDouble("opacity", 1.0))) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/** Load local ui file */
 	public void loadUiFile(String uri) {
 		MyLog.v(TAG, "loadUiFile()");
 		String uiString = null;
 
 		try {
 			uiString = getUIFile(UrlUtil.getUIFileUrl(uri));
 		} catch (IOException e1) {
 			MyLog.d(TAG, "UI file not found");
 			return;
 		}
 
 		JSONObject uiJSON;
 		ResultSet result = null;
 		try {
 			uiJSON = new JSONObject(uiString);
 			result = new UIBuilder(uiContext, uiJSON).build();
 		} catch (JSONException e) {
 			UIUtil.reportJSONParseError(getApplicationContext(), e.getMessage());
 			return;
 		} catch (Exception e) {
 			MyLog.e(TAG, e.getMessage());
 			LogItem logItem = new LogItem(TimeStamp.getCurrentTimeStamp(), Source.SYSTEM, LogLevel.ERROR, "NativeComponent:" + e.getMessage(), "", 0);
 			MyLog.sendBloadcastDebugLog(getContext(), logItem);
 			return;
 		}
 
 		applyUiToView(result);
 	}
 
 	protected void applyScreenOrientation(PageOrientation pageOrientation){
 		switch (pageOrientation) {
 		case PORTRAIT:
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
 			break;
 
 		case LANDSCAPE:
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 			break;
 
 		case SENSOR:
 			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
 			break;
 
 		case INHERIT:
 			// no override. Do nothing.
 			break;
 		default:
 			break;
 		}
 	}
 
 	protected void applyUiToView(ResultSet result) {
 		MyLog.d(TAG, "applyUiToView()");
 
 		uiBuilderResult = result;
 		this.dict = result.dict;
 
 		setupBackground(result.pageComponent.getBackgroundDrawable());
 		applyScreenOrientation(result.pageComponent.getScreenOrientation());
 
 		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
 		params.setMargins(0, 0, 0, 0);
 
 		if (result.bottomView != null || result.topView != null) {
 			MyLog.v(TAG, "result.bottomView != null || result.topView != null");
 
 			if (hasOpacityBar(result)) {
 				MyLog.v(TAG, "hasOpacityBar");
 
 				FrameLayout frame = new FrameLayout(this);
 				LinearLayout newRoot = new LinearLayout(this);
 				newRoot.setOrientation(LinearLayout.VERTICAL);
 
 				root.removeAllViews();
 				MyLog.v(TAG, "root.removeAllViews()");
 				root.addView(frame, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
 
 				ViewGroup appViewParent = ((ViewGroup) appView.getParent());
 				if (appViewParent != null) {
 					appViewParent.removeAllViews();
 				}
 
 				frame.addView(appView, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
 				frame.addView(newRoot, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
 
 				// top bar view
 				newRoot.addView(result.topView != null ? result.topView : new FrameLayout(this), 0, params);
 
 				// center
 				newRoot.addView(new LinearLayout(this), 1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
 						LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
 
 				// bottom bar view
 				newRoot.addView(result.bottomView != null ? result.bottomView : new FrameLayout(this), 2, params);
 
 				if (result.topView != null) {
 					MyLog.v(TAG, "result.topView != null");
 					result.topView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
 					int topViewHeight = result.topView.getMeasuredHeight();
 					try {
 						infoForJavaScript.put("topViewHeight", topViewHeight);
 					} catch (JSONException e) {
 						MyLog.e(TAG, e.getMessage());
 					}
 				}
 				if (result.bottomView != null) {
 					MyLog.v(TAG, "result.bottomView != null");
 					result.bottomView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
 					int bottomViewHeight = result.bottomView.getMeasuredHeight();
 					try {
 						infoForJavaScript.put("bottomViewHeight", bottomViewHeight);
 					} catch (JSONException e) {
 						MyLog.e(TAG, e.getMessage());
 					}
 				}
 			} else {
 				MyLog.v(TAG, "noOpacityBar");
 				root.removeAllViews();
 				MyLog.v(TAG, "root.removeAllViews()");
 
 				// top bar view
 				root.addView(result.topView != null ? result.topView : new FrameLayout(this), 0, params);
 
 				// center
 				ViewGroup appViewParent = (ViewGroup) appView.getParent();
 				if (appViewParent != null) {
 					appViewParent.removeView(appView);
 				}
 
 				root.addView(appView, 1, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f));
 
 				// bottom bar view
 				root.addView(result.bottomView != null ? result.bottomView : new FrameLayout(this), 2, params);
 			}
 		} else {
 			MyLog.v(TAG, "Reverse of result.bottomView != null || result.topView != null");
 			((ViewGroup) appView.getParent()).removeView(appView);
 			root.removeAllViews();
 			MyLog.v(TAG, "root.removeAllViews()");
 			root.addView(appView);
 			this.dict = new HashMap<String, Component>();
 		}
 	}
 
 	protected String getUIFile(String path) throws IOException {
 		Reader reader;
 		InputStream stream = null;
 
 		if (path == null) {
 			return "";
 		}
 
 		MyLog.d(getClass().getSimpleName(), "ui file loading: " + path);
 
 		if (path.startsWith("file:///android_asset/")) {
 			stream = LocalFileBootloader.openAsset(this.getApplicationContext(), path.substring("file:///android_asset/".length()));
 			reader = new InputStreamReader(stream);
 
 		} else if (path.startsWith("file://")) {
 
 			path = new File(path.substring(7)).getCanonicalPath();
 			reader = new FileReader(new File(path));
 
 		} else {
 			stream = InputStreamLoader.loadAssetFile(this, path);
 			reader = new InputStreamReader(stream, "UTF-8");
 		}
 		Writer writer = new StringWriter();
 
 		char[] buffer = new char[1024];
 		try {
 			int n;
 			while ((n = reader.read(buffer)) != -1) {
 				writer.write(buffer, 0, n);
 			}
 		} finally {
 			reader.close();
 			if (stream != null) {
 				stream.close();
 			}
 		}
 		return writer.toString();
 	}
 
 	/** Retrieve a style of Native UI Framework component. */
 	public JSONObject getStyle(String componentId) {
 		if (dict.containsKey(componentId)) {
 			return dict.get(componentId).getStyle();
 		}
 
 		return null;
 	}
 
 	/** Update a style of Native UI Framework component. */
 	public void updateStyle(final UpdateStyleQuery query) {
 		List<UpdateStyleQuery> queries = new ArrayList<UpdateStyleQuery>();
 		queries.add(query);
 		updateStyleBulkily(queries);
 	}
 
 	/** Update bulkily the styles of Native UI Framework components. */
 	public void updateStyleBulkily(final List<UpdateStyleQuery> queries) {
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				MyLog.d(MonacaPageActivity.class.getSimpleName(), "updateStyleBulkily() start");
 				for (UpdateStyleQuery query : queries) {
 					for (int i = 0; i < query.ids.length(); i++) {
 						String componentId = query.ids.optString(i, "");
 
 						if (dict != null && dict.containsKey(componentId)) {
 							Component component = dict.get(componentId);
 							if (component != null) {
 								component.updateStyle(query.style);
 								MyLog.d(MonacaPageActivity.class.getSimpleName(), "updated => id: " + componentId + ", style: " + query.style.toString());
 							} else {
 								Log.e(MonacaPageActivity.class.getSimpleName(), "update fail => id: " + componentId + ", style: " + query.style.toString());
 							}
 						} else {
 							Log.e(MonacaPageActivity.class.getSimpleName(), "no such component id: " + componentId);
 						}
 					}
 				}
 				MyLog.d(MonacaPageActivity.class.getSimpleName(), "updateStyleBulkily() done");
 			}
 		});
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
 		MyLog.i(TAG, "onWindowFocusChanged()");
 		super.onWindowFocusChanged(hasFocus);
 		if (hasFocus) {
 			BenchmarkTimer.mark("visible");
 			BenchmarkTimer.finish();
 			// Debug.stopMethodTracing();
 		}
 	}
 
 	public void onPageFinished(View view, String url) {
 		// BenchmarkTimer.mark("page finish:" + url);
 		// if(!url.startsWith("about:blank")){
 		// BenchmarkTimer.finish();
 		// }
 
 		// for android4's strange bug.
 		sendJavascript("console.log(' ');");
 
 		// check if this is 404 page
 		String errorUrl = getIntent().getStringExtra("error_url");
 
 		if (errorUrl != null && url.endsWith("/404/404.html")) {
 			String backButtonText = getString(R.string.back_button_text);
 			errorUrl = UrlUtil.cutHostInUri(errorUrl);
 			MyLog.v(TAG, "error url:" + errorUrl);
 			appView.loadUrl("javascript:$('#url').html(\"" + errorUrl + "\"); $('#backButton').html('" + backButtonText + "')");
 		}
 	}
 
 	public void onPageStarted(View view, String url) {
 		ViewGroup.LayoutParams params = this.appView.getLayoutParams();
 		params.width = ViewGroup.LayoutParams.MATCH_PARENT;
 		params.height = ViewGroup.LayoutParams.MATCH_PARENT;
 		this.appView.setLayoutParams(params);
 	}
 
 	@Override
 	protected void onRestart() {
 		MyLog.i(TAG, "onRestart");
 		super.onRestart();
 		loadBackground(getResources().getConfiguration());
 		// setupBackground();
 		if (background != null) {
 			background.invalidateSelf();
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		MyLog.i(TAG, "onResume");
 		try {
 			WebView.class.getMethod("onResume").invoke(this);
 		} catch (Exception e) {
 		}
 
 		// only when screen turns on
 		if (!ScreenReceiver.wasScreenOn) {
 			// this is when onResume() is called due to a screen state change
 			System.out.println("SCREEN TURNED ON");
 		} else {
 			// this is when onResume() is called when the screen state has not
 			// changed
 			// if (appView != null && appView.callbackServer != null &&
 			// appView.pluginManager != null) {
 			if (appView != null && appView.pluginManager != null) {
 				appView.loadUrl("javascript: window.onReactivate && onReactivate();");
 			}
 		}
 
 		isCapableForTransition = true;
 
 		super.onResume();
 	}
 
 	@Override
 	protected void onPause() {
 		MyLog.i(TAG, "onPause");
 		super.onPause();
 		this.removeMonacaSplash();
 
 		if (isFinishing()) {
 			onDestroyMonacaCaller();
 		}
 	}
 
 	/**
 	 * @see MonacaPageActivity#onDestroyMonaca()
 	 */
 	private final void onDestroyMonacaCaller() {
 		//MyLog.d(TAG, "monacaOnDestroyCaller()");
 		if (!isOnDestroyMonacaCalled) {
 			// prevent from multiple calls
 			onDestroyMonaca();
 			isOnDestroyMonacaCalled = true;
 		}
 	}
 
 	/**
 	 * to call onDestroy surely, this is called in onPause or onDestroy
 	 * since Activity#finish doesn't guarantee calling onDestroy.
 	 * this should be called through onDestroyMonacaCaller
 	 * @see MonacaPageActivity#onDestroyMonacaCaller()
 	 */
 	protected void onDestroyMonaca() {
 		MyLog.d(TAG, "onDestroyMonaca");
 		unregisterReceiver(pushReceiver);
 		appView.setBackgroundDrawable(null);
 		root.setBackgroundDrawable(null);
 		this.removeMonacaSplash();
 
 		MonacaApplication.removePage(this);
 		unregisterReceiver(closePageReceiver);
 
 		if (background != null) {
 			background.setCallback(null);
 			background = null;
 		}
 
 		if (dict != null) {
 			dict.clear();
 		}
 		dict = null;
 		uiBuilderResult = null;
 		appView.setBackgroundDrawable(null);
 		root.setBackgroundDrawable(null);
 		closePageReceiver = null;
 		unregisterReceiver(mScreenReceiver);
 
 		root.removeView(appView);
 		appView.stopLoading();
 //		appView.setWebChromeClient(null);  // this caused Android 2.3.5 to crash. Null Pointer Exception
 		appView.setWebViewClient(null);
 
 		// this causes null pointer on some devices
 		// for DroidGap posts delayed message to appView
 		// unregisterForContextMenu(appView);
 		// appView.destroy();
 		// appView = null;
 	}
 
 	@Override
 	public void onDestroy() {
 		MyLog.i(TAG, "onDestroy");
 		onDestroyMonacaCaller();
 		super.onDestroy();
 	}
 
 	/** Reload current URI. */
 	public void reload() {
 		appView.stopLoading();
 		loadUri(getCurrentUriWithoutQuery(), false);
 	}
 
 	public String getCurrentHtml() {
 		return mCurrentHtml;
 	}
 
 	protected String buildCurrentUriHtml() throws IOException {
 		String html = AssetUriUtil.assetToString(this, getCurrentUriWithoutQuery());
 
 		if (UrlUtil.isMonacaUri(this, currentMonacaUri.getUrlWithQuery()) && currentMonacaUri.hasQueryParams()) {
 			html = currentMonacaUri.getQueryParamsContainingHtml(html);
 		}
 
 		return html;
 	}
 
 	/** Load current URI. */
 	public void loadUri(String uri, final boolean withoutUIFile) {
 		String currentUriWithoutQuery = getCurrentUriWithoutQuery();
 		MyLog.v(TAG, "loadUri() uri:" + currentUriWithoutQuery);
 
 		setCurrentUri(uri);
 
 		// check for 404
 		if (currentUriWithoutQuery.equalsIgnoreCase("file:///android_asset/www/404/404.html")) {
 			String failingUrl = getIntent().getStringExtra("error_url");
 			show404Page(failingUrl);
 			return;
 		}
 
 		if (!withoutUIFile) {
 			loadUiFile(getCurrentUriWithoutQuery());
 		}
 
 		try {
 			mCurrentHtml = buildCurrentUriHtml();
 			appView.loadDataWithBaseURL(getCurrentUriWithoutQuery(), mCurrentHtml, "text/html", "UTF-8", this.getCurrentUriWithoutQuery());
 
 		} catch (IOException e) {
 			MyLog.d(TAG, "Maybe Not MonacaURI : " + e.getMessage());
 			MyLog.d(TAG, "load as nomal url:" + currentUriWithoutQuery);
 			if (uri.startsWith("file://")) {
 				show404Page(uri);
 				return;
 			}
 
 			appView.setBackgroundColor(0x00000000);
 			// setupBackground();
 			loadLayoutInformation();
 
 			appView.loadUrl(currentMonacaUri.getUrlWithQuery());
 			appView.clearView();
 			appView.invalidate();
 		}
 	}
 
 	public void show404Page(String failingUrl) {
 		try {
 			InputStream is = getResources().openRawResource(R.raw.error404);
 			String html = IOUtils.toString(is);
 			html = html.replaceFirst("url_place_holder", UrlUtil.cutHostInUri(failingUrl));
 			html = html.replaceFirst("back_button_text", getString(R.string.back_button_text));
 			appView.loadDataWithBaseURL("file:///android_res/raw/error404.html", html, "text/html", "utf-8", null);
 		} catch (IOException e) {
 			MyLog.e(TAG, e.getMessage());
 		}
 	}
 
 	public void push404Page(String errorUrl) {
 		Intent intent = new Intent(this, getClass());
 		intent.putExtra(URL_PARAM_NAME, "file:///android_asset/www/404/404.html");
 		intent.putExtra("error_url", errorUrl);
 
 		TransitionParams params = TransitionParams.from(new JSONObject(), "none");
 		intent.putExtra(TRANSITION_PARAM_NAME, params);
 		startActivity(intent);
 		finish();
 	}
 
 	public void pushPageWithIntent(String url, TransitionParams params) {
 		if (isCapableForTransition) {
 			Intent intent = createIntentForNextPage(url, params);
 
 			isCapableForTransition = false;
 			startActivity(intent);
 		}
 	}
 
 	protected Intent createIntentForNextPage(String url, TransitionParams params) {
 		Intent intent = new Intent(this, getClass());
 
 		intent.putExtra(URL_PARAM_NAME, UrlUtil.getResolvedUrl(url));
 		if (params != null) {
 			intent.putExtra(TRANSITION_PARAM_NAME, params);
 		}
 		return intent;
 	}
 
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		MyLog.d(TAG, "debug - onKeyDown");
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 
 			if (uiBuilderResult != null && uiBuilderResult.eventer.hasOnTapBackButtonAction()) {
 				MyLog.d(TAG, "debug - Run backButtonEventer.onTapBackButtonAction()");
 				uiBuilderResult.eventer.onTapBackButton();
 			} else if (uiBuilderResult != null && uiBuilderResult.backButtonEventer != null) {
 				MyLog.d(TAG, "debug - Run backButtonEventer.onTap()");
 				uiBuilderResult.backButtonEventer.onTap();
 			} else {
 				MyLog.d(TAG, "debug - Run popPage()");
 				popPage();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	public void pushPageAsync(String relativePath, final TransitionParams params) {
 		final String url = getCurrentUriWithoutQuery() + "/../" + relativePath;
 		BenchmarkTimer.start();
 
 		BenchmarkTimer.mark("pushPageAsync");
 		handler.postAtFrontOfQueue(new Runnable() {
 			@Override
 			public void run() {
 				BenchmarkTimer.mark("monaca.pushPageAsync.run");
 				pushPageWithIntent(url, params);
 			}
 		});
 	}
 
 	public void popPage() {
 		int pageNum = MonacaApplication.getPages().size();
 		finish();
 
 		if (pageNum > 1) {
 			// dirty fix for android4's strange bug
 			if (transitionParams.animationType == TransitionParams.TransitionAnimationType.MODAL) {
 				overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_enter,
 						mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_exit);
 			} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.TRANSIT) {
 				overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_close_enter,
 						mobi.monaca.framework.psedo.R.anim.monaca_slide_close_exit);
 			} else if (transitionParams.animationType == TransitionParams.TransitionAnimationType.NONE) {
 				overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_none, mobi.monaca.framework.psedo.R.anim.monaca_none);
 			}
 		}
 	}
 
 	public void _popPage() {
 		int pageNum = MonacaApplication.getPages().size();
 		finish();
 
 		if (pageNum > 1) {
 			// dirty fix for android4's strange bug
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_slide_close_enter, mobi.monaca.framework.psedo.R.anim.monaca_slide_close_exit);
 		}
 	}
 
 	public void dismissPage() {
 		int pageNum = MonacaApplication.getPages().size();
 		finish();
 
 		if (pageNum > 1) {
 			overridePendingTransition(mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_enter, mobi.monaca.framework.psedo.R.anim.monaca_dialog_close_exit);
 		}
 	}
 
 	public void popPageAsync(final TransitionParams params) {
 		handler.postAtFrontOfQueue(new Runnable() {
 			@Override
 			public void run() {
 
 				if (params.animationType == TransitionParams.TransitionAnimationType.POP) {
 					_popPage();
 				} else if (params.animationType == TransitionParams.TransitionAnimationType.DISMISS) {
 					dismissPage();
 				} else {
 					_popPage();
 				}
 			}
 		});
 	}
 
 	public void goHomeAsync(JSONObject options) {
 		final String homeUrl = getHomeUrl(options);
 		MyLog.v(TAG, "homeurl:" + homeUrl);
 
 		handler.post(new Runnable() {
 			@Override
 			public void run() {
 				int numPages = MonacaApplication.getPages().size();
 				for (int i = numPages - 1; i > 0; i--) {
 					MonacaPageActivity page = MonacaApplication.getPages().get(i);
 					page.finish();
 				}
 			}
 		});
 	}
 
 	protected WebViewClient createWebViewClient(String url, MonacaPageActivity page, CordovaWebView webView) {
 		MonacaPageGingerbreadWebViewClient client = null;
 
 		if (Integer.valueOf(android.os.Build.VERSION.SDK_INT) < 11) {
 			client = new MonacaPageGingerbreadWebViewClient(url, page, webView);
 		} else {
 			client = new MonacaPageHoneyCombWebViewClient(url, page, webView);
 		}
 		return client;
 	}
 
 	protected String getHomeUrl(JSONObject options) {
 		if (options == null) {
 			return "file:///android_asset/www/index.html";
 		}
 		return options.optString("url", "").equals("") ? "file:///android_asset/www/index.html" : getCurrentUriWithoutQuery() + "/../"
 				+ options.optString("url");
 	}
 
 	@Override
 	protected void onStop() {
 		MyLog.d(TAG, "onStop");
 		super.onStop();
 		unloadBackground();
 	}
 
 	@Override
 	public void onConfigurationChanged(Configuration newConfig) {
 		super.onConfigurationChanged(newConfig);
 
 		MyLog.d(getClass().getSimpleName(), "onConfigurationChanged()");
 
 		// handling orieantation change for background image.
 		if (background != null) {
 			loadBackground(newConfig);
 			// setupBackground();
 			if (background != null) {
 				background.invalidateSelf();
 			}
 		}
 
 		uiContext.fireOnRotateListeners(newConfig.orientation);
 
 		appView.clearView();
 		appView.invalidate();
 
 		Display display = getWindowManager().getDefaultDisplay();
 		MyLog.d(getClass().getSimpleName(), "metrics width: " + display.getWidth() + ", height: " + display.getHeight());
 	}
 
 	/*
 	 * Called from WebViewClient -> can be used in DeubggerPageActivity to
 	 * publish log message
 	 */
 	public void onLoadResource(WebView view, String url) {
 		processMonacaReady(url);
 	}
 
 	protected void processMonacaReady(String url) {
 		if (pushData != null) {
 			if (url.equals(MONACA_READY_URL)) {
 				sendPushToWebView(pushData);
 				pushData = null;
 			}
 		}
 	}
 
 	protected void sendPushToWebView(GCMPushDataset pushData) {
 		appView.loadUrl("javascript:monaca.sendPush(" + pushData.getExtraJSONString() + ")");
 	}
 
 	public MonacaURI getCurrentMonacaUri() {
 		return currentMonacaUri;
 	}
 
 	public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
 	}
 
 	@Override
 	public void onReceivedError(int errorCode, String description, String failingUrl) {
 		// MyLog.d(TAG, "got error :" + Integer.toString(errorCode) + ", " +
 		// description + ", " + failingUrl);
 		if (isInitializationMessage(errorCode, description, failingUrl)) {
 			MyLog.d(TAG, "supressed initialize message");
 			return;
 		} else {
 			super.onReceivedError(errorCode, description, failingUrl);
 		}
 	}
 
 	protected boolean isInitializationMessage(int errorCode, String description, String failingUrl) {
 		return (errorCode == MonacaWebView.INITIALIZATION_ERROR_CODE && description.contains(MonacaWebView.INITIALIZATION_DESCRIPTION) && failingUrl
 				.startsWith(MonacaWebView.INITIALIZATION_MADIATOR));
 	}
 
 	public String getCurrentUriWithoutQuery() {
 		return currentMonacaUri.getUrlWithoutQuery();
 	}
 
 	/**
 	 * update uri and currentMonacaURI
 	 *
 	 * @param uri
 	 */
 	public void setCurrentUri(String uri) {
 		MyLog.v(TAG, "setCurrentUri:" + uri);
 		currentMonacaUri = new MonacaURI(uri);
 	}
 
 }
