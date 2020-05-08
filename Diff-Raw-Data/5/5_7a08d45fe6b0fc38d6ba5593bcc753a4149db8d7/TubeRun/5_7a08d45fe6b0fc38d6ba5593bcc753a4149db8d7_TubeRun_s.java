 package com.papagiannis.tuberun;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Date;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ProgressDialog;
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.provider.Settings.Secure;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 import android.widget.ToggleButton;
 
 import com.google.android.vending.licensing.AESObfuscator;
 import com.google.android.vending.licensing.LicenseChecker;
 import com.google.android.vending.licensing.LicenseCheckerCallback;
 import com.google.android.vending.licensing.Policy;
 import com.google.android.vending.licensing.ServerManagedPolicy;
 import com.papagiannis.tuberun.favorites.Favorite;
 import com.papagiannis.tuberun.fetchers.Observer;
 import com.papagiannis.tuberun.fetchers.OysterFetcher;
 import com.papagiannis.tuberun.stores.CredentialsStore;
 
 public class TubeRun extends Activity implements OnClickListener, Observer {
 	public static final String APPNAME = "TubeRun";
	public static final String VERSION = "1.2";
 	//Don't forget to change the Gmaps API key in full_screen_map.xml and the version/version code in manifest
 	public static final Boolean USE_LICENSING = true;
 
 	private static final String TUBE_MAP_URL = "https://www.tfl.gov.uk/assets/downloads/standard-tube-map.gif";
 	private static final String LOCAL_PATH = "standard-tube-map.gif";
 	private static final String LICENCING_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnskzkZ7GjJBChKebZfXVqdDnuqWDLNHuhhpIwL6a+g8OiNE52+LxolCAZJmOnHr3zvgdPw0vuRKrFfGjuPgVJV13nx1DKFi7LuXuK4rpmMucZ1qZf4kwbNw+iOmp6YqWT8OQ1RN94biWluZhwcee5sb16xmtJEeH2iHEKVtjheJUGebSm6mxiQO3S3LE4p9pWadPDfPmFEvw2vjVLtwyxUqBIhiMiEtOF3e6JDBE6kLndI97jZY4LXsfL7IDhiBe1pLCZrO90TQTKMzqwz8nowXqoQLvDJ78bUaCuJm7WwPPTgpZmAyL5P2bi+c5NDoJrZsntq82EL2hRnDPiP2+nwIDAQAB";
 	private static final byte[] LICENCING_SALT = new byte[] { 100, 78, 89, 45,
 			21, 45, 21, 90, 23, 45, 67, 12, 11, 54 };
 
 	private static final int DOWNLOAD_IMAGE_DIALOG = -1;
 	private static final int DOWNLOAD_IMAGE_PROGRESS_DIALOG = -2;
 	private static final int DOWNLOAD_IMAGE_FAILED_DIALOG = -3;
 	private static final int LICENCING_ERROR = -4;
 	private static final int SHOW_WELCOME = -5;
 	
 	public static final String PREFERENCES="Preferences";
 	public static final String AUTOSTART="autostart";
 	public static final Integer AUTOSTART_NONE=-1;
 	public static final String TUBEMAP_EXISTS="tubeMapDownloaded";
 	
 	TextView oysterBalance;
 	ProgressBar oysterProgress;
 	LinearLayout oysterLayout;
 	Button oysterButton;
 	Button oysterButtonActive;
 	Button logoButton;
 	ToggleButton favoritesButton;
 	Button mapsButton;
 
 	private SharedPreferences preferences;
 	private boolean tubeMapDownloaded = false;
 	private ImageDownloadTask task;
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		preferences = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
 		tubeMapDownloaded = preferences.getBoolean(TUBEMAP_EXISTS, false);
 		
 		View statusButton = findViewById(R.id.button_status);
 		statusButton.setOnClickListener(this);
 		View departuresButton = findViewById(R.id.button_departures);
 		departuresButton.setOnClickListener(this);
 		mapsButton = (Button) findViewById(R.id.button_maps);
 		mapsButton.setOnClickListener(this);
 		View nearbyButton = findViewById(R.id.button_nearby);
 		nearbyButton.setOnClickListener(this);
 		logoButton = (Button) findViewById(R.id.button_logo);
 		logoButton.setOnClickListener(this);
 		favoritesButton = (ToggleButton) findViewById(R.id.button_favorites);
 		favoritesButton.setOnClickListener(this);
 		View claimsButton = findViewById(R.id.button_claims);
 		claimsButton.setOnClickListener(this);
 		View planButton = findViewById(R.id.button_planner);
 		planButton.setOnClickListener(this);
 		oysterButton = (Button) findViewById(R.id.button_oyster);
 		oysterButton.setOnClickListener(this);
 		oysterButtonActive = (Button) findViewById(R.id.button_oyster_active);
 		oysterButtonActive.setOnClickListener(this);
 		oysterBalance = (TextView) findViewById(R.id.view_balance);
 		oysterProgress = (ProgressBar) findViewById(R.id.progressbar_balance);
 		oysterLayout = (LinearLayout) findViewById(R.id.layout_balance);
 
 		if (USE_LICENSING)
 			initializeLicencing();
 		Intent i=getIntent();
 		Boolean showMap=i.getBooleanExtra(MainMenu.SHOWMAP, false);
 		if (showMap) onNewIntent(i);
 		else {
 			setIntent(new Intent());
 			showWelcome();
 		}
 	}
 	
 	@Override
 	protected void onNewIntent(Intent intent) {
 		setIntent(intent);
 		Boolean showMap=intent.getBooleanExtra(MainMenu.SHOWMAP, false);
 		if (showMap)onClick(mapsButton);
 	};
 
 	@SuppressWarnings("deprecation")
 	private void showWelcome() {
 		String l=preferences.getString("lastNotice", "");
 		if (!l.equals(VERSION)) {
 			Editor editor = preferences.edit();
 			editor.putString("lastNotice", VERSION);
 			editor.commit();
 			copyDatabase();
 			showDialog(SHOW_WELCOME);
 		}
 		else jump();
 	}
 	
 	private void jump() {
 		SharedPreferences shPrefs = getSharedPreferences(PREFERENCES, MODE_PRIVATE);
 		int viewId = shPrefs.getInt( AUTOSTART, AUTOSTART_NONE);
 		if (viewId != AUTOSTART_NONE) {
 			onClick(findViewById(viewId));
 			finish();
 		}
 	}
 
 	@SuppressWarnings("deprecation")
 	public void onClick(View v) {
 		Intent i = null;
 		switch (v.getId()) {
 		case R.id.button_status:
 			i = new Intent(this, StatusActivity.class);
 			break;
 		case R.id.button_departures:
 			i = new Intent(this, SelectLineActivity.class);
 			i.putExtra("type", "departures");
 			break;
 		case R.id.button_maps:
 			if (tubeMapDownloaded)
 				i = getMapIntent();
 			else {
 				showDialog(DOWNLOAD_IMAGE_DIALOG);
 				return;
 			}
 			break;
 		case R.id.button_nearby:
 			i = new Intent(this, NearbyStationsActivity.class);
 			break;
 		case R.id.button_favorites:
 			favoritesButton.setChecked(!favoritesButton.isChecked()); // no
 																		// toggling
 			i = new Intent(this, FavoritesActivity.class);
 			break;
 		case R.id.button_claims:
 			i = new Intent(this, ClaimsActivity.class);
 			break;
 		case R.id.button_planner:
 			i = new Intent(this, PlanActivity.class);
 			break;
 		case R.id.button_oyster:
 		case R.id.button_oyster_active:
 			i = new Intent(this, OysterActivity.class);
 			break;
 		case R.id.button_logo:
 			i = new Intent(this, AboutActivity.class);
 			break;
 		}
 		if (i!=null) startActivity(i);
 	}
 
 	private Intent getMapIntent() {
 		Intent i;
 		i = new Intent(this, StatusMapActivity.class);
 		i.putExtra("line",
 				LinePresentation.getStringRespresentation(LineType.ALL));
 		i.putExtra("type", "maps");
 		return i;
 	}
 
 	private CredentialsStore store = CredentialsStore.getInstance();
 	private OysterFetcher fetcher;
 	private String username = "";
 
 	private void fetchBalance() {
 
 		oysterButtonActive.setVisibility(View.GONE);
 		oysterButton.setVisibility(View.VISIBLE);
 		oysterBalance.setVisibility(View.GONE);
 		oysterProgress.setVisibility(View.GONE);
 		oysterLayout.setVisibility(View.GONE);
 		ArrayList<String> credentials = store.getAll(this);
 		if (credentials.size() == 0)
 			return;
 		Date now = new Date();
 		// skip fetching oyster balance if it has been fetched before (in the
 		// last 5 min).
 		if (!username.equals("")
 				&& username.equals(credentials.get(0))
 				&& !fetcher.isErrorResult()
 				&& !fetcher.getResult().equals("")
 				&& (now.getTime() - fetcher.getUpdateTime().getTime()) / 1000 < 5 * 60) {
 			// there is a result i can reuse
 			update();
 		} else if (credentials.size() == 2) {
 			OysterFetcher newFetcher = OysterFetcher.getInstance(
 					credentials.get(0), credentials.get(1));
 			if (fetcher != newFetcher) {
 				fetcher = newFetcher;
 				fetcher.registerCallback(this);
 			}
 			oysterButtonActive.setVisibility(View.VISIBLE);
 			oysterButton.setVisibility(View.GONE);
 			oysterLayout.setVisibility(View.VISIBLE);
 			oysterProgress.setVisibility(View.VISIBLE);
 			username = credentials.get(0);
 			fetcher.update();
 		}
 	}
 
 	@Override
 	public void update() {
 		CharSequence balance = fetcher.getResult();
 		oysterBalance.setText(balance);
 		oysterButtonActive.setVisibility(View.VISIBLE);
 		oysterButton.setVisibility(View.GONE);
 		oysterLayout.setVisibility(View.VISIBLE);
 		oysterProgress.setVisibility(View.GONE);
 		oysterBalance.setVisibility(View.VISIBLE);
 	}
 
 	private Dialog wait_dialog;
 	ProgressDialog progressDialog;
 
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		Dialog result = null;
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		switch (id) {
 		case DOWNLOAD_IMAGE_DIALOG:
 			builder.setTitle("Tube Map Required")
 					.setMessage(
 							"The official Tube Map is property of TfL and is not included in this app. "
 									+ "However, TubeRun can fetch it from TfL and cache it for future use.\n\n"
 									+ "File size: ~550KB")
 					.setCancelable(true)
 					.setPositiveButton("Download",
 							new DialogInterface.OnClickListener() {
 								@SuppressWarnings("deprecation")
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dismissDialog(DOWNLOAD_IMAGE_DIALOG);
 									showDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 									fetchTubeMap();
 								}
 							})
 					.setNegativeButton("Cancel",
 							new DialogInterface.OnClickListener() {
 
 								@SuppressWarnings("deprecation")
 								@Override
 								public void onClick(DialogInterface dialog,
 										int which) {
 									dismissDialog(DOWNLOAD_IMAGE_DIALOG);
 								}
 							});
 			wait_dialog = builder.create();
 			result = wait_dialog;
 			break;
 		case DOWNLOAD_IMAGE_PROGRESS_DIALOG:
 			progressDialog = new ProgressDialog(this);
 			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progressDialog.setMessage("Downloading Tube Map");
 			progressDialog.setCancelable(true);
 			progressDialog
 					.setOnCancelListener(new DialogInterface.OnCancelListener() {
 
 						@SuppressWarnings("deprecation")
 						@Override
 						public void onCancel(DialogInterface dialog) {
 							task.cancel(true);
 							dismissDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 						}
 					});
 			wait_dialog = progressDialog;
 			result = progressDialog;
 			break;
 		case DOWNLOAD_IMAGE_FAILED_DIALOG:
 			builder.setTitle("Download failed")
 					.setMessage(
 							"Could not download the Tube Map. Please try again later. "
 									+ "Make sure that you have Internet access.")
 					.setCancelable(true)
 					.setPositiveButton("OK",
 							new DialogInterface.OnClickListener() {
 								@SuppressWarnings("deprecation")
 								public void onClick(DialogInterface dialog,
 										int id) {
 									dismissDialog(DOWNLOAD_IMAGE_FAILED_DIALOG);
 								}
 							});
 			wait_dialog = builder.create();
 			result = wait_dialog;
 			break;
 		case LICENCING_ERROR:
 			wait_dialog = getLicensingErrorDialog();
 			result = wait_dialog;
 			break;
 		case SHOW_WELCOME:
 			wait_dialog = getWelcomeDialog();
 			result = wait_dialog;
 			break;
 		}
 		return result;
 	}
 
 	@Override
 	protected void onPrepareDialog(int id, Dialog dialog) {
 		switch (id) {
 		case DOWNLOAD_IMAGE_PROGRESS_DIALOG:
 			progressDialog.setProgress(0);
 			progressDialog.setMax(100);
 		}
 	};
 
 	private void fetchTubeMap() {
 		task = new ImageDownloadTask();
 		task.execute(TUBE_MAP_URL, LOCAL_PATH);
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	protected void onPause() {
 		super.onPause();
 		if (fetcher != null) {
 			fetcher.abort();
 		}
 		if (task != null) {
 			if (progressDialog != null)
 				dismissDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 			task.cancel(true);
 		}
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		ArrayList<Favorite> favs = new ArrayList<Favorite>();
 		fetchBalance();
 		try {
 			favs = Favorite.getFavorites(this);
 		} catch (Exception e) {
 			Log.w("Main", e);
 		}
 		favoritesButton.setChecked(favs.size() > 0);
 	}
 
 	class ImageDownloadTask extends AsyncTask<String, Integer, Boolean> {
 
 		@Override
 		protected Boolean doInBackground(String... params) {
 			try {
 				URL url = new URL(params[0]);
 				HttpURLConnection urlConnection = (HttpURLConnection) url
 						.openConnection();
 				urlConnection.setRequestMethod("GET");
 				urlConnection.connect();
 
 				// File appdir = Environment.getExternalStorageDirectory();
 				// File dir = new File(appdir.getAbsoluteFile() + "/tuberun/");
 				// Boolean created = dir.mkdirs();
 				// File file = new File(dir, params[1]);
 				// FileOutputStream fileOutput = new FileOutputStream(file);
 
 				// Let's read everything to RAM first
 				InputStream inputStream = urlConnection.getInputStream();
 				int totalSize = urlConnection.getContentLength();
 				int downloadedSize = 0;
 				byte[] buffer = new byte[1024];
 				byte[] fullFile = new byte[totalSize];
 				int bufferLength = 0; // used to store a temporary size of the
 				// // buffer
 				int i = 0;
 				while ((bufferLength = inputStream.read(buffer)) > 0) {
 					if (isCancelled())
 						return false;
 					// fileOutput.write(buffer, 0, bufferLength);
 
 					int k = 0;
 					for (int j = i; j < i + bufferLength; j++) {
 						fullFile[j] = buffer[k++];
 					}
 					i += bufferLength;
 					//
 					//
 					downloadedSize += bufferLength;
 					publishProgress(downloadedSize, totalSize);
 				}
 				// fileOutput.close();
 				inputStream.close();
 
 				ContentValues v = new ContentValues();
 				v.put("map", fullFile);
 				ContentResolver r = getContentResolver();
 				r.insert(
 						Uri.parse("content://"
 								+ TubeMapContentProvider.AUTHORITY + "/map"), v);
 
 				// This is an attempt to read from the ContentProvider, it
 				// works!
 				// Cursor
 				// rrr=r.query(Uri.parse("content://"+TubeMapContentProvider.AUTHORITY+"/map"),
 				// new String[]{},"",new String[]{},"");
 				// Boolean suc=rrr.moveToFirst();
 				// if (suc) {
 				// byte[] res=rrr.getBlob(0);
 				// int iii=res.length;
 				// i=i+i;
 				// }
 
 			} catch (MalformedURLException e) {
 				e.printStackTrace();
 				return false;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 			return true;
 		}
 
 		@SuppressWarnings("deprecation")
 		@Override
 		protected void onPostExecute(Boolean result) {
 			dismissDialog(DOWNLOAD_IMAGE_PROGRESS_DIALOG);
 			if (result) {
 				tubeMapDownloaded = true;
 				Editor editor = preferences.edit();
 				editor.putBoolean("tubeMapDownloaded", tubeMapDownloaded);
 				editor.commit();
 				Intent i = getMapIntent();
 				startActivity(i);
 			} else
 				showDialog(DOWNLOAD_IMAGE_FAILED_DIALOG);
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 			int current = values[0];
 			int total = values[1];
 			// int percent=(100*current)/total;
 			progressDialog.setProgress(current);
 			progressDialog.setMax(total);
 		}
 	}
 
 	// *****************Licensing methods go here***********************
 	private LicenseCheckerCallback mLicenseCheckerCallback;
 	private LicenseChecker mChecker;
 
 	private void initializeLicencing() {
 		mLicenseCheckerCallback = new MyLicenseCheckerCallback();
 		String deviceId = Secure.getString(getContentResolver(),
 				Secure.ANDROID_ID);
 		mChecker = new LicenseChecker(this, new ServerManagedPolicy(this,
 				new AESObfuscator(LICENCING_SALT, getPackageName(), deviceId)),
 				LICENCING_PUBLIC_KEY);
 		mChecker.checkAccess(mLicenseCheckerCallback);
 	}
 
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		if (USE_LICENSING)
 			mChecker.onDestroy();
 	}
 
 	private class MyLicenseCheckerCallback implements LicenseCheckerCallback {
 		public void allow(int reason) {
 			if (isFinishing()) {
 				return;
 			}
 		}
 
 		@SuppressWarnings("deprecation")
 		public void dontAllow(int reason) {
 			if (isFinishing()) {
 				return;
 			}
 			if (reason == Policy.RETRY) {
 			} else {
 				showDialog(LICENCING_ERROR);
 			}
 		}
 
 		@Override
 		public void applicationError(int errorCode) {
 			// this is called for ERROR_NOT_MARKET_MANAGED,
 			// ERROR_INVALID_PACKAGE_NAME, ERROR_NON_MATCHING_UID
 			// see LicenseValidator.java
 			// showDialog(LICENCING_ERROR);
 		}
 	}
 
 	private Dialog getLicensingErrorDialog() {
 		AlertDialog.Builder builder = new AlertDialog.Builder(this);
 		builder.setTitle("Application Not Licensed")
 				.setMessage(
 						"Your copy of this application is unauthorised. You may obtain a licensed copy from the Google Play "
 								+ "Store.\n\n"
 								+ "Please support the developer (I have spent many nights working on this).\n\n"
 								+ "If you think that you are seeing this in error, please contact the developer.")
 				.setCancelable(false)
 				.setPositiveButton("Open Play Store",
 						new DialogInterface.OnClickListener() {
 							public void onClick(DialogInterface dialog, int id) {
 								Intent intent = new Intent(Intent.ACTION_VIEW);
 								intent.setData(Uri
 										.parse("market://details?id=com.papagiannis.tuberun"));
 								startActivity(intent);
 								finish();
 							}
 						})
 				.setNeutralButton("Contact Developer",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								final Intent emailIntent = new Intent(
 										android.content.Intent.ACTION_SEND);
 								emailIntent.setType("plain/text");
 								emailIntent.putExtra(
 										android.content.Intent.EXTRA_EMAIL,
 										new String[] { "jpapayan@gmail.com" });
 								emailIntent.putExtra(
 										android.content.Intent.EXTRA_SUBJECT,
 										TubeRun.APPNAME + " Activation Error");
 								emailIntent.putExtra(
 										android.content.Intent.EXTRA_TEXT, "");
 								startActivity(Intent.createChooser(emailIntent,
 										"Send mail via"));
 								finish();
 							}
 						})
 				.setNegativeButton("Exit Application",
 						new DialogInterface.OnClickListener() {
 							@Override
 							public void onClick(DialogInterface dialog,
 									int which) {
 								finish();
 							}
 						});
 		return builder.create();
 	}
 
 	// *****************Welcome to TubeRun operations**************************
 	
 	private Dialog getWelcomeDialog() {
 		ProgressDialog.Builder builder = new ProgressDialog.Builder(this);
 		builder.setTitle(APPNAME+" "+VERSION)
 				.setMessage("What's new:\n\n"+
						"*New UI\n\n" +
 						"*Station autocomplete in textboxes\n\n"+
 						"*Search provider for quick access to departures from the homescreen (needs manual activation)\n\n"+
 						"*Official TfL Bus API\n\n"+
 						"*Bug fixes")
 				.setCancelable(false).setPositiveButton("OK", null);
 		return builder.create();
 	}
 
 	private void copyDatabase() {
 		AsyncTask<Context, Integer, Boolean> task = new AsyncTask<Context, Integer, Boolean>() {
 
 			@Override
 			protected Boolean doInBackground(Context... params) {
 				try {
 					DatabaseHelper myDbHelper = new DatabaseHelper(params[0]);
 					myDbHelper.createDatabase();
 					myDbHelper.openDataBase();
 					myDbHelper.close();
 					wait_dialog.setCancelable(true);
 				} catch (Exception e) {
 					Log.w("CopyDatabase", e);
 					return false;
 				}
 				return true;
 			}
 		};
 		task.execute(this);
 	}
 }
