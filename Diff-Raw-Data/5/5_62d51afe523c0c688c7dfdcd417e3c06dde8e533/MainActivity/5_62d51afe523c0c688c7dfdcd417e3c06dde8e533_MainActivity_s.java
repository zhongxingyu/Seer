 package com.android.iliConnect;
 
 import java.io.File;
 import java.util.Date;
 import java.util.List;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.View;
 import android.webkit.MimeTypeMap;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.android.iliConnect.Exceptions.NetworkException;
 import com.android.iliConnect.dataproviders.DataDownloadThread;
 import com.android.iliConnect.dataproviders.FileDownloadProvider;
 import com.android.iliConnect.dataproviders.LocalDataProvider;
 import com.android.iliConnect.dataproviders.NotificationWatchThread;
 import com.android.iliConnect.dataproviders.RemoteDataProvider;
 import com.android.iliConnect.handler.AndroidNotificationBuilder;
 import com.android.iliConnect.models.Item;
 
 public class MainActivity extends Activity {
 
 	public static MainActivity instance;
 	public static Activity currentActivity;
 	public RemoteDataProvider remoteDataProvider;
 	public ProgressDialog progressDialog;
 	public static Object syncObject = new Object();
 	public LocalDataProvider localDataProvider;
 	public FileDownloadProvider download;
 	public DataDownloadThread watchThread = new DataDownloadThread();
 	public NotificationWatchThread notificationThread = new NotificationWatchThread();
 	private EditText etUrl;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		currentActivity = this;
 
 		instance = this;
 
 		localDataProvider = LocalDataProvider.getInstance();
 		localDataProvider.init(R.xml.config);
 
 		localDataProvider.init(R.xml.modification);
 
 		localDataProvider.localdata.load();
 		localDataProvider.auth = localDataProvider.localdata.Static.auth;
 		localDataProvider.settings = localDataProvider.localdata.Static.settings;
 
 		// localDataProvider.updateLocalData();
 
 		remoteDataProvider = new RemoteDataProvider();
 
 		View login = findViewById(R.id.button1);
 
 		EditText etUserID = (EditText) findViewById(R.id.editText1);
 		EditText etPassword = (EditText) findViewById(R.id.editText2);
 		etUrl = (EditText) findViewById(R.id.urlText);
 
 		if (!localDataProvider.auth.user_id.equals(""))
 			etUserID.setText(localDataProvider.auth.user_id);
 		if (!localDataProvider.auth.user_id.equals(""))
 			etPassword.setText(localDataProvider.auth.password);
 		if (!localDataProvider.auth.url_src.equals(""))
 			etUrl.setText(localDataProvider.auth.url_src);
 
 		final File remoteDataFile = new File(MainActivity.instance.getFilesDir() + "/" + localDataProvider.remoteDataFileName);
 		if (localDataProvider.auth.autologin && remoteDataFile.exists()) {
 			// falls AutoLogin true ist kann eine Anmeldung ohne Sync. durchgeführt werden
 			try {
 				autologin();
 			} catch (NetworkException e) {
 				MessageBuilder.exception_message(MainTabView.instance, e.getMessage());
 				e.printStackTrace();
 			}
 		}
 
 		login.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View v) {
 
 				EditText etUserID = (EditText) findViewById(R.id.editText1);
 				localDataProvider.auth.user_id = etUserID.getText().toString();
 				EditText etPassword = (EditText) findViewById(R.id.editText2);
 				localDataProvider.auth.password = etPassword.getText().toString();
 
 				// Prüfen, ob / an Url-Ende vorhaden und ggf. hinzufügen
 				String url = etUrl.getText().toString();
 				if (!url.endsWith("/")) {
 					url = url + "/";
 				}
 				localDataProvider.auth.url_src = url;
 				localDataProvider.auth.setLogin(true, etUserID.getText().toString(), etPassword.getText().toString(), url);
 				localDataProvider.localdata.save();
 				// Login mit Syncronisation
 				try {
 					login();
 				} catch (NetworkException e) {
 					MessageBuilder.exception_message(MainTabView.instance, e.getMessage());
 					e.printStackTrace();
 				}
 
 			}
 		});
 	}
 
 	public MainActivity getInstance() {
 		return instance;
 	}
 
 	public void showBrowserContent(String url) {
 
 		Intent openUrlIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
 		startActivity(openUrlIntent);
 
 	}
 
 	public void sync(Context context) throws NetworkException {
 		sync(context, false);
 	}
 
 	public void sync(Context context, boolean manual) throws NetworkException {
 
 		boolean wlanOnly = this.localDataProvider.settings.sync_wlanonly;
 
 		// wenn Context null ist, keine Sync-Meldung anzeigen
 		if (context != null) {
 			progressDialog = new ProgressDialog(context);
 			progressDialog.setTitle("Synchronisation");
 			progressDialog.setMessage("Bitte warten...");
 			remoteDataProvider = new RemoteDataProvider(progressDialog);
 		} else {
 			remoteDataProvider = new RemoteDataProvider();
 		}
 
 		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
 
 		NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
 		NetworkInfo mobile = connManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
 
 		if (wlanOnly == true) {
 			if (wifi == null || !wifi.isConnected()) {
 				// Fehlermeldung nur ausgeben, wenn manuelle Synchronisation
 				if (manual) {
 					throw new NetworkException("Benötigte WLAN-Verbindung nicht vorhanden.");
 				}
 			} else {
 				remoteDataProvider.execute(MainActivity.instance.localDataProvider.remoteData.getSyncUrl() + "?action=sync");
 			}
 		} else {
 			boolean showSyncError = false;
 			if ((mobile == null || !mobile.isConnected()) && (wifi == null || !wifi.isConnected())) {
 				showSyncError = true;
 			}
 
 			if (showSyncError) {
 				// Fehlermeldung nur ausgeben, wenn manuelle Synchronisation
 				if (manual) {
 					throw new NetworkException("Benötigte Datenverbindung nicht vorhanden");
 				}
 			} else {
 				remoteDataProvider.execute(MainActivity.instance.localDataProvider.remoteData.getSyncUrl() + "?action=sync");
 			}
 			NotificationWatchThread wt = new NotificationWatchThread();
 			wt.showNotificationPopups(false, false);
 		}
 	}
 
 	public void showToast(final String msg) {
 		if (msg != null && !msg.equals("")) {
 			Toast t = Toast.makeText(MainActivity.instance, msg, Toast.LENGTH_LONG);
 			t.show();
 		}
 	}
 
 	public void logout() {
 		if (MainTabView.instance != null)
 			MainTabView.instance.finish();
 
 		localDataProvider.remoteData.delete();
 
 		remoteDataProvider.cancel(true);
 		AndroidNotificationBuilder.cancelNotification();
 
 		LocalDataProvider.isAvaiable = false;
 
 		if (watchThread.doAsynchronousTask != null) {
 			watchThread.doAsynchronousTask.cancel();
 			watchThread.doAsynchronousTask = null;
 		}
 
 		if (notificationThread.doAsynchronousTask != null) {
 			notificationThread.doAsynchronousTask.cancel();
 			notificationThread.doAsynchronousTask = null;
 		}
 
 	}
 
 	public void login() throws NetworkException {
 		final File remoteDataFile = new File(MainActivity.instance.getFilesDir() + "/" + localDataProvider.remoteDataFileName);
 		MainActivity.instance.sync(MainActivity.instance, true);
 
 		new Thread(new Runnable() {
 			public void run() {
 				synchronized (syncObject) {
 					Date start = new Date();
 					long timeout = 5000;
 
 					while (!remoteDataFile.exists() || !LocalDataProvider.isAvaiable) {
 						try {
 							syncObject.wait(2000);
 						} catch (InterruptedException e) {
 
 						}
 						if (new Date().getTime() - start.getTime() > timeout)
 							break;
 					}
 					if (remoteDataFile.exists()) {
 						Intent i = new Intent(MainActivity.this, MainTabView.class);
 						
 						startActivity(i);
 						
 
 						MainActivity.instance.runOnUiThread(new Runnable() {
 							public void run() {
 								if (watchThread.doAsynchronousTask == null) {
 									watchThread.startTimer();
 								}
 							}
 						});
 					}
 				}
 			}
 		}).start();
 	}
 
 	public void autologin() throws NetworkException {
 		MainActivity.instance.localDataProvider.updateLocalData();
 
 		Intent i = new Intent(MainActivity.this, MainTabView.class);
 		startActivity(i);
 
 		MainActivity.instance.runOnUiThread(new Runnable() {
 			public void run() {
 				if (watchThread.doAsynchronousTask == null) {
 					watchThread.startTimer();
 				}
 			}
 		});
 	}
 
 	@Override
 	protected void onRestart() {
 		EditText etUserID = (EditText) findViewById(R.id.editText1);
 		EditText etPassword = (EditText) findViewById(R.id.editText2);
 		EditText etUrl = (EditText) findViewById(R.id.urlText);
 
 		// Bei Ausloggen Textfelder zurücksetzen
 		etUserID.setText("");
 		etPassword.setText("");
 		etUrl.setText(localDataProvider.auth.url_src);
 
 		// Instance von MainTabView überschreiben, da sonst IllegalStateExeption bei Update der View auftitt.
 		MainTabView.instance = null;
 		super.onRestart();
 	}
 
 	public void openFileOrDownload(final Activity instance, final Item item) {
 		// ProgessDialog für Downlaod definieren
 		// TODO Auto-generated method stub
 		// progressDialog = new ProgressDialog(MainTabView.instance);
 		// progressDialog.setTitle("SDownload");
 		// progressDialog.setMessage("Bitte warten...");
 
 		
 
 		download = new FileDownloadProvider(progressDialog);
 
 		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 
 		String dirPath = path + "/IliConnect/" + MainActivity.instance.localDataProvider.auth.user_id;
 		// IliConnect-Ordner erstellen, falls noch nicht vorhanden
 		File f = new File(dirPath);
 		if (!f.exists() && !f.isDirectory()) {
 			f.mkdirs();
 		}
 
 		final String filePath = dirPath + "/" + item.getTitle();
 		final File file = new File(filePath);
 		
 
 		if(item.changed && file.exists())
 			file.delete();
 		
 		if (!file.exists()) {
 			// Download Dialog erst anzeigen, wenn Datei auch heruntergeladen werden muss.
 			progressDialog = ProgressDialog.show(instance, "Download", "Bitte warten");
 			download.execute(new String[] { localDataProvider.auth.url_src + "repository.php?ref_id=" + item.getRef_id() + "&cmd=sendfile", filePath });
 		}
 		new Thread(new Runnable() {
 
 			public void run() {
 
 				// synchronized (syncObject) {
 
 				Date start = new Date();
 				long timeout = 5000;
 				Intent intent = null;
 				boolean fileError;
 				boolean openFileError;
 				String ext="";
 
 				while (!file.exists()) {
 					try {
 						Thread.sleep(2000);
 					} catch (InterruptedException e) {
 
 					}
 					if (new Date().getTime() - start.getTime() > timeout)
 						break;
 				}
 
 				fileError = false;
 				openFileError = false;
 				if (file.exists()) {
 
 					intent = new Intent(Intent.ACTION_VIEW);
 					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 
 					MimeTypeMap mime = MimeTypeMap.getSingleton();			
 					
 					// Funktioniert nicht zu 100%
 					//String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
 					
 					int dot = filePath.lastIndexOf(".");
 					ext = filePath.substring(dot+1, filePath.length());					
 					String mimeType = mime.getMimeTypeFromExtension(ext);
 					if (mimeType == null || mimeType.equals("")) {
 						// falls Dateiendung unbekannt
 						intent.setData(Uri.fromFile(file));
 						PackageManager pm = instance.getPackageManager();
 						List<ResolveInfo> apps = pm.queryIntentActivities(intent, pm.MATCH_DEFAULT_ONLY);
 
 						// Prüfen, ob Apps vorhanden sind die die Datei öffnen könnten
 						if (apps.size() == 0) {
 							openFileError = true;
 						}
 					} else {
 						intent.setDataAndType(Uri.fromFile(file), mimeType);	
 					}
 
 				} else {
 					fileError = true;
 				}
 				
 				// Werte für UI Thread definieren
 				final boolean downloadError = fileError;
 				final boolean appError = openFileError;
 				final Intent appIntent = intent;
 				final String extension = ext;
 				
 
				
 				MainActivity.instance.runOnUiThread(new Runnable() {
 					public void run() {
 						if (MainTabView.getInstance() != null)
 							MainTabView.getInstance().update();
 	
 						if(progressDialog != null) {
 							progressDialog.dismiss();	
 						}
 						
 						if(downloadError) {
							MessageBuilder.download_error(instance, item.getTitle());
 						}
 						if(appError) {
 							try {
 							    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q="+extension)));
 							} catch (android.content.ActivityNotFoundException anfe) {
 							    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://market.android.com/search?q="+extension)));
 							}							
 							
 						}
 						
 						if(appIntent != null && appError == false) {
 							MainActivity.instance.startActivity(Intent.createChooser(appIntent, "Datei öffnen..."));
 						}
 						
 					}
 				});
 			}
 			// }
 		}).start();
 	}
 	
 	public void iliasNotifier(final Activity instance, final Item item) {
 		// ProgessDialog für Downlaod definieren
 		// TODO Auto-generated method stub
 		// progressDialog = new ProgressDialog(MainTabView.instance);
 		// progressDialog.setTitle("SDownload");
 		// progressDialog.setMessage("Bitte warten...");
 
 		//progressDialog = ProgressDialog.show(instance, "Download", "Bitte warten");
 		if(!localDataProvider.settings.sync)
 		return;
 			
 		download = new FileDownloadProvider(null);
 
 		File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
 
 		String dirPath = path + "/IliConnect/" + MainActivity.instance.localDataProvider.auth.user_id;
 		// IliConnect-Ordner erstellen, falls noch nicht vorhanden
 		File f = new File(dirPath);
 		if (!f.exists() && !f.isDirectory()) {
 			f.mkdirs();
 		}
 
 		final String filePath = dirPath + "/.tmp";
 		final File file = new File(filePath);
 
 		if (!file.exists()) {
 			download.execute(new String[] { localDataProvider.auth.url_src + "repository.php?ref_id=" + item.getRef_id() + "&cmd=view", filePath });
 		}
 		new Thread(new Runnable() {
 
 			public void run() {
 
 				// synchronized (syncObject) {
 
 				Date start = new Date();
 				long timeout = 5000;
 
 				while (!file.exists()) {
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 
 					}
 					if (new Date().getTime() - start.getTime() > timeout)
 						break;
 				}
 
 				MainActivity.instance.runOnUiThread(new Runnable() {
 					public void run() {
 						if (MainTabView.getInstance() != null)
 							MainTabView.getInstance().update();
 					}
 				});
 			}
 			// }
 		}).start();
 		file.delete();
 	}
 
 }
