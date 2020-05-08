 package moses.client.service;
 
 import java.io.IOException;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import moses.client.ViewUserStudiesActivity;
 import moses.client.abstraction.HardwareAbstraction;
 import moses.client.abstraction.apks.ExternalApplication;
 import moses.client.com.C2DMReceiver;
 import moses.client.com.ReqTaskExecutor;
 import moses.client.com.NetworkJSON.BackgroundException;
 import moses.client.com.requests.RequestC2DM;
 import moses.client.com.NetworkJSON;
 import moses.client.service.helpers.CheckForNewApplications;
 import moses.client.service.helpers.Executor;
 import moses.client.service.helpers.ExecutorWithObject;
 import moses.client.service.helpers.Login;
 import moses.client.service.helpers.Logout;
 import moses.client.userstudy.UserStudyNotification;
 import moses.client.userstudy.UserstudyNotificationManager;
 import android.R;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Binder;
 import android.os.IBinder;
 import android.preference.PreferenceManager;
 import android.util.Log;
 import android.widget.Toast;
 
 /**
  * The Class MosesService.
  * 
  * @author Jaco Hofmann
  */
 public class MosesService extends android.app.Service implements
 		OnSharedPreferenceChangeListener {
 
 	private static final String MOSES_TUD_GOOGLEMAIL_COM = "moses.tud@googlemail.com";
 
 	/**
 	 * The Class LocalBinder.
 	 */
 	public class LocalBinder extends Binder {
 
 		/**
 		 * Gets the service.
 		 * 
 		 * @return the service
 		 */
 		public MosesService getService() {
 			return MosesService.this;
 		}
 	}
 
 	/**
 	 * The Class MosesSettings.
 	 */
 	public class MosesSettings {
 
 		/** The username. */
 		public String username = "";
 
 		/** The password. */
 		public String password = "";
 
 		/** The sessionid. */
 		public String sessionid = "";
 
 		/** The logged in. */
 		public boolean loggedIn = false;
 
 		public boolean loggingIn = false;
 
 		/** Saves the used filter. */
 		public JSONArray filter = new JSONArray();
 
 		public ConcurrentLinkedQueue<Executor> postLoginSuccessHook = new ConcurrentLinkedQueue<Executor>();
 
 		public ConcurrentLinkedQueue<Executor> postLoginFailureHook = new ConcurrentLinkedQueue<Executor>();
 
 		public ConcurrentLinkedQueue<Executor> loginStartHook = new ConcurrentLinkedQueue<Executor>();
 
 		public ConcurrentLinkedQueue<Executor> loginEndHook = new ConcurrentLinkedQueue<Executor>();
 
 		public ConcurrentLinkedQueue<Executor> postLogoutHook = new ConcurrentLinkedQueue<Executor>();
 
 		public String url = "http://www.da-sense.de/moses/test.php";
 
 		public ConcurrentLinkedQueue<ExecutorWithObject> changeTextFieldHook = new ConcurrentLinkedQueue<ExecutorWithObject>();
 
 	}
 
 	/** The m binder. */
 	private final IBinder mBinder = new LocalBinder();
 
 	/** The settings file. */
 	private SharedPreferences settingsFile;
 
 	/** The mset. */
 	private MosesSettings mset = new MosesSettings();
 
 	private CheckForNewApplications checkForNewApplications;
 
 	private String c2dmRegistrationId;
 
 	private boolean alreadySuccessfullySentC2DMID;
 
 	private static MosesService thisInstance = null;
 
 	public static MosesService getInstance() {
 		return thisInstance;
 	}
 
 	/**
 	 * Gets the session id.
 	 * 
 	 * @return the session id
 	 */
 	public String getSessionID() {
 		return mset.sessionid;
 	}
 
 	/**
 	 * Inits the config.
 	 */
 	private void initConfig() {
 		settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
 		mset.username = settingsFile.getString("username_pref", "");
 		mset.password = settingsFile.getString("password_pref", "");
 	}
 
 	/**
 	 * Checks if is logged in.
 	 * 
 	 * @return true, if is logged in
 	 */
 	public boolean isLoggedIn() {
 		return mset.loggedIn;
 	}
 
 	public void setFilter(JSONArray filter) {
 		mset.filter = filter;
 		settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
 		settingsFile.edit().putString("sensor_data", filter.toString()).commit();
 	}
 
 	public JSONArray getFilter() {
 		return mset.filter;
 	}
 
 	public Context getServiceContext() {
 		return this;
 	}
 
 	/**
 	 * Logged in.
 	 * 
 	 * @param sessionid
 	 *            the sessionid
 	 */
 	public void loggedIn(String sessionid) {
 		mset.loggedIn = true;
 		mset.loggingIn = false;
 		mset.sessionid = sessionid;
 		if (!alreadySuccessfullySentC2DMID && this.c2dmRegistrationId != null
 				&& !(getSessionID().equals("") || getSessionID() == null)) {
 			sendC2DMIdToServer(this.c2dmRegistrationId);
 		}
 	}
 
 	/**
 	 * Logged out.
 	 */
 	public void loggedOut() {
 		mset.loggedIn = false;
 		mset.sessionid = "";
 	}
 
 	public void registerChangeTextFieldHook(ExecutorWithObject e) {
 		if (!mset.changeTextFieldHook.contains(e))
 			mset.changeTextFieldHook.add(e);
 	}
 
 	public void unregisterChangeTextFieldHook(ExecutorWithObject e) {
 		mset.changeTextFieldHook.remove(e);
 	}
 
 	/**
 	 * Login.
 	 * 
 	 * @param e
 	 *            the e
 	 */
 	public void login() {
 		if (mset.username.equals("") || mset.password.equals("")) {
 			for (ExecutorWithObject e : mset.changeTextFieldHook) {
 				e.execute(getString(moses.client.R.string.no_username_password));
 			}
 			return;
 		}
 		if (isOnline()) {
 			if (!mset.loggedIn && !mset.loggingIn) {
 				Log.d("MoSeS.SERVICE", "Logging in...");
 				mset.loggingIn = true;
 				Login.setService(this);
 				new Login(mset.username, mset.password,
 						mset.postLoginSuccessHook, mset.postLoginFailureHook,
 						mset.loginStartHook, mset.loginEndHook);
 			}
 		} else {
 			Log.d("MoSeS.SERVICE",
 					"Tried logging in but no internet connection was present.");
 		}
 	}
 
 	/**
 	 * Logout.
 	 * 
 	 * @param e
 	 *            the e
 	 */
 	public void logout() {
 		new Logout(this, mset.postLogoutHook);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Service#onBind(android.content.Intent)
 	 */
 	@Override
 	public IBinder onBind(Intent arg0) {
 		return mBinder;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Service#onCreate()
 	 */
 	@Override
 	public void onCreate() {
 		super.onCreate();
 		thisInstance = this;
 		registerPostLoginFailureHook(new Executor() {
 
 			@Override
 			public void execute() {
 				mset.loggingIn = false;
 			}
 		});
 
 		NetworkJSON.url = mset.url;
 		PreferenceManager.getDefaultSharedPreferences(this)
 				.registerOnSharedPreferenceChangeListener(this);
 		checkForNewApplications = new CheckForNewApplications(this);
 
 		registerC2DM();
 		initConfig();
 		syncDeviceInformation();
 		new HardwareAbstraction(MosesService.this).getFilter();
 		checkForNewApplications.startChecking(true);
 		Log.d("MoSeS.SERVICE", "Service Created");
 	}
 
 	public void executeLoggedIn(Executor e) {
 		if (isLoggedIn())
 			e.execute();
 		else {
 			registerPostLoginSuccessOneTimeHook(e);
 			login();
 		}
 	}
 
 	public void registerPostLoginSuccessHook(Executor e) {
 		if (!mset.postLoginSuccessHook.contains(e))
 			mset.postLoginSuccessHook.add(e);
 	}
 
 	public void registerPostLoginSuccessOneTimeHook(final Executor e) {
 		Executor n = new Executor() {
 			@Override
 			public void execute() {
 				e.execute();
 				unregisterPostLoginSuccessHook(this);
 			}
 		};
 		mset.postLoginSuccessHook.add(n);
 	}
 
 	public void unregisterPostLoginSuccessHook(Executor e) {
 		mset.postLoginSuccessHook.remove(e);
 	}
 
 	public void registerPostLoginFailureHook(Executor e) {
 		if (!mset.postLoginFailureHook.contains(e))
 			mset.postLoginFailureHook.add(e);
 	}
 
 	public void unregisterPostLoginFailureHook(Executor e) {
 		mset.postLoginFailureHook.remove(e);
 	}
 
 	public void registerLoginStartHook(Executor e) {
 		if (!mset.loginStartHook.contains(e))
 			mset.loginStartHook.add(e);
 	}
 
 	public void unregisterLoginStartHook(Executor e) {
 		mset.loginStartHook.remove(e);
 	}
 
 	public void registerLoginEndHook(Executor e) {
 		if (!mset.loginEndHook.contains(e))
 			mset.loginEndHook.add(e);
 	}
 
 	public void unregisterLoginEndHook(Executor e) {
 		mset.loginEndHook.remove();
 	}
 
 	public void registerPostLogoutHook(Executor e) {
 		if (!mset.postLogoutHook.contains(e))
 			mset.postLogoutHook.add(e);
 	}
 
 	public void unregisterPostLogoutHook(Executor e) {
 		mset.postLogoutHook.remove(e);
 	}
 
 	private void registerC2DM() {
 		Intent registrationIntent = new Intent(
 				"com.google.android.c2dm.intent.REGISTER");
 		registrationIntent.putExtra("app",
 				PendingIntent.getBroadcast(this, 0, new Intent(), 0)); // boilerplate
 		registrationIntent.putExtra("sender", MOSES_TUD_GOOGLEMAIL_COM);
 		startService(registrationIntent);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Service#onDestroy()
 	 */
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		thisInstance = null;
 		Log.d("MoSeS.SERVICE", "Service Destroyed");
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see android.app.Service#onStart(android.content.Intent, int)
 	 */
 	@Override
 	public void onStart(Intent intent, int startId) {
 
 		super.onStart(intent, startId);
 		Log.d("MoSeS.SERVICE", "Service Started");
 	}
 
 	/**
 	 * Reload settings.
 	 */
 	public void reloadSettings() {
 		initConfig();
 	}
 
 	public void setC2DMReceiverId(String registrationId) {
 		// TODO: if the c2dm id changed, resend?
 		boolean setNewC2DMID = false;
 		if (registrationId != null) {
 			if (this.c2dmRegistrationId == null) {
 				setNewC2DMID = true;
 			} else {
 				if (!registrationId.equals(this.c2dmRegistrationId)) {
 					setNewC2DMID = true;
 				}
 			}
 		} else {
 			// TODO: unexpected error.. registrationId == null (should never
 			// occur thou)
 		}
 		if (setNewC2DMID) {
 			this.c2dmRegistrationId = registrationId;
 			if (!alreadySuccessfullySentC2DMID
 					&& !(getSessionID().equals("") || getSessionID() == null)) {
 				sendC2DMIdToServer(registrationId);
 			}
 
 		}
 	}
 
 	// TODO: make very sure that the id is really sent to the server!
 	// TODO: what if session id is yet unknown?
 	private void sendC2DMIdToServer(String registrationId) {
 		final RequestC2DM request = new RequestC2DM(
 				new ReqTaskExecutor() {
 					@Override
 					public void updateExecution(BackgroundException c) {
 					}
 
 					@Override
 					public void postExecution(String s) {
 						// request sent!
 						try {
 							JSONObject j = new JSONObject(s);
 							if (RequestC2DM.C2DMRequestAccepted(j)) {
 								alreadySuccessfullySentC2DMID = true;
 								Toast.makeText(getApplicationContext(),
 										"C2DM send request returned POSITIVE",
 										Toast.LENGTH_LONG).show();
 								Log.i("MoSeS.C2DM",
 										"synchronized c2dm id with moses server.");
 							} else {
 								Toast.makeText(getApplicationContext(),
 										"C2DM send request returned NEGATIVE",
 										Toast.LENGTH_LONG).show();
 								Log.w("MoSeS.C2DM",
 										"C2DM request returned NEGATIVE response: "
 												+ s);
 							}
 
 						} catch (JSONException e) {
 							Toast.makeText(
 									getApplicationContext(),
 									"C2DMToMosesServer returned malformed message",
 									Toast.LENGTH_LONG).show();
 							Log.e("MoSeS.C2DM",
 									"C2DMToMosesServer returned malformed message");
 						}
 					}
 
 					@Override
 					public void handleException(Exception e) {
 						// TODO: make very sure that the id is really sent to
 						// the server!
 						Toast.makeText(getApplicationContext(),
 								"sendC2DM failed: " + e.getMessage(),
 								Toast.LENGTH_LONG).show();
 					}
 				}, getSessionID(), HardwareAbstraction.extractDeviceId(),
 				registrationId);
 		executeLoggedIn(new Executor() {
 
 			@Override
 			public void execute() {
 				request.send();
 			}
 		});
 	}
 
 	/**
 	 * sends device information to the moses server
 	 * 
 	 * @param c2dmRegistrationId
 	 *            the id which identifies this device in the c2dm
 	 * @param force
 	 *            the Force is a binding, metaphysical and ubiquitous power in
 	 *            the fictional universe of the Star Wars galaxy created by
 	 *            George Lucas.
 	 */
 	private void syncDeviceInformation() {
 		new HardwareAbstraction(this).syncDeviceInformation();
 	}
 
 	public boolean isOnline() {
 		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo netInfo = cm.getActiveNetworkInfo();
 		if (netInfo != null && netInfo.isConnectedOrConnecting()) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public int onStartCommand(Intent intent, int flags, int startId) {
 		if (intent != null) {
 			String c2dmIdExtra = intent
 					.getStringExtra(C2DMReceiver.EXTRAFIELD_C2DM_ID);
 			String userStudyNotificationExtra = intent
 					.getStringExtra(C2DMReceiver.EXTRAFIELD_USERSTUDY_NOTIFICATION);
 
 			// if this startCommand was meant to notify about arrived c2dm id
 			if (c2dmIdExtra != null) {
 				setC2DMReceiverId(c2dmIdExtra);
 			}
 
 			// if this startCommand was meant to notify about user study
 			if (userStudyNotificationExtra != null) {
 				String apkId = userStudyNotificationExtra;
 				handleNeUserStudyNotificationFor(apkId);
 			}
 		}
 		return super.onStartCommand(intent, flags, startId);
 	}
 
 	private void handleNeUserStudyNotificationFor(String apkId) {
 		Log.i("MoSeS.Service", "saving user study notification to the manager");
 		if (UserstudyNotificationManager.getInstance() == null) {
 			try {
 				UserstudyNotificationManager.init(this);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		if (UserstudyNotificationManager.getInstance() != null) {
 			UserStudyNotification notification = new UserStudyNotification(
 					new ExternalApplication(apkId));
 			UserstudyNotificationManager.getInstance().addNotification(
 					notification);
 			try {
 				UserstudyNotificationManager.getInstance().saveToDisk(
 						getApplicationContext());
 			} catch (IOException e) {
 				Log.e("MoSeS", "Error when saving user study notifications");
 			}
 
 			Intent intent = new Intent(this, ViewUserStudiesActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			intent.putExtra(ViewUserStudiesActivity.EXTRA_USER_STUDY_APK_ID,
 					notification.getApplication().getID());
 			intent.putExtra("sid", getSessionID()); // TODO: passing session id:
 													// should be handled
 													// differently later
 			Log.i("MoSeS.Service",
 					"starting intent to display user study notification");
 			this.startActivity(intent);
 		} else {
 			Log.e("MoSeS.Service",
 					"cannot display user study notification because user notification manager could not be initialized.");
 		}
 	}
 	
 	private void uploadFilter() {
 		settingsFile = PreferenceManager.getDefaultSharedPreferences(this);
 		String s = settingsFile.getString("sensor_data", "[]");
 		HardwareAbstraction ha = new HardwareAbstraction(this);
 		ha.setFilter(s);
 	}
 
 	@Override
 	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
 			String key) {
 		if (key.equals("sensor_data")) {
 			Log.d("MoSeS.SERVICE", "Sensor filter changed to: "
 					+ sharedPreferences.getString("sensor_data", ""));
 			uploadFilter();
 		} else {
 			Log.d("MoSeS.SERVICE", "Settings changed - reloading them");
 			reloadSettings();
 		}
 	}
 }
