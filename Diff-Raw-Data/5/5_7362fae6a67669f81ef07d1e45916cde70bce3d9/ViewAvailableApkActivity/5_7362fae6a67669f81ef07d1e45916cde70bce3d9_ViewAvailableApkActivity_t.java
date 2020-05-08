 package moses.client;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Currency;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import moses.client.abstraction.ApkListRequestObserver;
 import moses.client.abstraction.ApkMethods;
 import moses.client.abstraction.apks.APKInstalled;
 import moses.client.abstraction.apks.ApkDownloadManager;
 import moses.client.abstraction.apks.ApkInstallManager;
 import moses.client.abstraction.apks.ExternalApplication;
 import moses.client.abstraction.apks.InstalledExternalApplication;
 import moses.client.userstudy.UserstudyNotificationManager;
 import android.app.Activity;
 import android.app.Dialog;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 import android.widget.Toast;
 
 /**
  * Viewing and installing apks from the server
  * 
  * @author Simon L
  */
 public class ViewAvailableApkActivity extends Activity implements ApkListRequestObserver {
 
 	private static final int REFRESH_THRESHHOLD = 6000;
 	private ListView listView;
 	private List<ExternalApplication> externalApps;
 	private Long lastListRefreshTime = null;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Activity#onCreate(android.os.Bundle)
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.availableapklist);
 
 		initControls();
 	}
 
 	public void apkInstallClickHandler(View v) {
 		int pos = listView.getPositionForView(v);
 		final ExternalApplication app = externalApps.get(pos);
 
 		final Dialog myDialog = new Dialog(this);
 		myDialog.setContentView(R.layout.view_app_info_layout);
 		myDialog.setTitle("App informations:");
 		((TextView) myDialog.findViewById(R.id.appinfodialog_name)).setText("Name: "
 			+ app.getName());
 		((TextView) myDialog.findViewById(R.id.appinfodialog_descr)).setText(""
 			+ app.getDescription());
 		((Button) myDialog.findViewById(R.id.appinfodialog_installbtn)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				Log.i("MoSes.Install", "starting install process for app " + app.toString());
 				myDialog.dismiss();
 				handleInstallApp(app);
 			}
 		});
 		((Button) myDialog.findViewById(R.id.appinfodialog_cancelbtn)).setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				myDialog.dismiss();
 			}
 		});
 
 		myDialog.setOwnerActivity(this);
 		myDialog.show();
 	}
 
 	/**
 	 * Inits the controls.
 	 */
 	private void initControls() {
 		requestExternalApplications();
 	}
 
 	private void handleInstallApp(ExternalApplication app) {
 		final ApkDownloadManager downloader = new ApkDownloadManager(app, getApplicationContext());
 		Observer observer = new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (downloader.getState() == ApkDownloadManager.State.ERROR) {
 					// TODO: error msgs/log msgs shouldve been already shown,
 					// still.. something is to be done here still
 				} else if (downloader.getState() == ApkDownloadManager.State.FINISHED) {
 					installDownloadedApk(downloader.getDownloadedApk(), downloader.getExternalApplicationResult());
 				}
 			}
 		};
 		downloader.addObserver(observer);
 		downloader.start();
 	}
 
 	private void installDownloadedApk(final File originalApk, final ExternalApplication externalAppRef) {
 		final ApkInstallManager installer = new ApkInstallManager(originalApk, externalAppRef);
 		installer.addObserver(new Observer() {
 			@Override
 			public void update(Observable observable, Object data) {
 				if (installer.getState() == ApkInstallManager.State.ERROR) {
 					// TODO:errors/log msgs shouldve been shown already by the
 					// installer; still, something is to be done here..
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_CANCELLED) {
 					// TODO:how to handle if the user cancels the installation?
 				} else if (installer.getState() == ApkInstallManager.State.INSTALLATION_COMPLETED) {
 					new APKInstalled(externalAppRef.getID());
 					try {
 						ApkInstallManager.registerInstalledApk(originalApk, externalAppRef,
 							ViewAvailableApkActivity.this.getApplicationContext(), false);
 					} catch (IOException e) {
 						Log.e(
 							"MoSeS.Install",
 							"Problems with extracting package name from apk, or problems with the InstalledExternalApplicationsManager after installing an app");
 					}
 				}
 			}
 		});
 		installer.start();
 	}
 
 	private void requestExternalApplications() {
 		lastListRefreshTime = System.currentTimeMillis();
 		ApkMethods.getExternalApplications(this);
 	}
 
 	@Override
 	public void apkListRequestFinished(List<ExternalApplication> applications) {
 		externalApps = applications;
 		populateList(applications);
 	}
 
 	@Override
 	public void apkListRequestFailed(Exception e) {
 		Toast.makeText(getApplicationContext(), "Error when loading the list of applications: " + e==null?"":e.getMessage(), Toast.LENGTH_LONG).show();
 	}
 
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
		//TODO: find better way to detect unwanted focus-lsoe-and-regain by some intent
		if(hasFocus && (lastListRefreshTime == null)?true:(System.currentTimeMillis()-lastListRefreshTime>REFRESH_THRESHHOLD)) {
 			requestExternalApplications();
 		}
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
		//TODO: find better way to detect unwanted focus-lsoe-and-regain by some intent
 		if((lastListRefreshTime == null)?true:(System.currentTimeMillis()-lastListRefreshTime>REFRESH_THRESHHOLD)) {
 			requestExternalApplications();
 		}
 	}
 	
 	private void populateList(List<ExternalApplication> applications) {
 		listView = (ListView) findViewById(R.id.availableApkListView);
 		String[] items = new String[applications.size()];
 		int counter = 0;
 		for (ExternalApplication app : applications) {
 			items[counter] = app.getName();
 			counter++;
 		}
 		
 		TextView instructionsView = (TextView) findViewById(R.id.availableApkHeaderInstructions);
 		if(instructionsView != null) {
 			if(applications.size() == 0) {
 				instructionsView.setText(R.string.availableApkList_emptyHint);
 			} else {
 				instructionsView.setText(R.string.availableApkList_defaultHint);
 			}
 		}
 		
 		List<Map<String, String>> listContent = new LinkedList<Map<String, String>>();
 		for(ExternalApplication app: applications) {
 			HashMap<String, String> rowMap = new HashMap<String, String>();
 			rowMap.put("name", app.getName());
 			rowMap.put("description", app.getDescription());
 			listContent.add(rowMap);
 
 		}
 		SimpleAdapter contentAdapter = new SimpleAdapter( 
 			this, 
 			listContent,
 			R.layout.availableabkslistitem,
 			new String[] { "name","description" },
 			new int[] { R.id.apklistitemtext, R.id.apklistitemdescription } );
 		
 //		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, R.layout.availableabkslistitem,
 //			R.id.apklistitemtext, items) {
 //		};
 		listView.setAdapter(contentAdapter);
 	}
 
 	public static String concatStacktrace(Exception e) {
 		String stackTrace = "";
 		for (int i = 0; i < e.getStackTrace().length; i++) {
 			stackTrace += e.getStackTrace()[i];
 		}
 		return stackTrace;
 	}
 
 }
