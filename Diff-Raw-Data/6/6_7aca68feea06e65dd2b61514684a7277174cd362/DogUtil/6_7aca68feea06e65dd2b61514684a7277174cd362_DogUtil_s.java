 package mx.ferreyra.dogapp;
 
 import android.app.Application;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.os.Build;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.Facebook;
 import com.facebook.android.Util;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class DogUtil extends Application{	
 	public static final int DOGWELFARE = 0x01;
 	public static final int STATISTICS = 0x02;
 	public static final int LOAD_ROUTE = 0x03;
 	public static final int NEW_ROUTE = 0x04;
 
     private Integer currentUserId;
     private static DogUtil app;
 	
 	public static final String FACEBOOK_APPID = "360694843999854"; //"120875961350018"; 
 	//public static final String APP_ID = "124807174259375";
 	
 	public static final String TRACK_ID = "UA-27608007-1";
 	public Facebook mFacebook;
 	private AsyncFacebookRunner mAsyncRunner;
 	private String routeName;
 	private static String DEVICE_ID;
 	
 	public String getRouteName() {
 		return routeName;
 	}
 
 	public void setRouteName(String routeName) {
 		this.routeName = routeName;
 	}
 
 
 	private static final String[] PERMISSIONS =
 		new String[] {Utilities.PERMISSION_OFFLINE_ACCESS, Utilities.PERMISSION_USER_RELATIONSHIP, 
 		Utilities.PERMISSION_EMAIL,Utilities.PERMISSION_PUBLISH_STREAM, Utilities.PERMISSION_READ_STREAM};
 	
 	private GoogleAnalyticsTracker gTracker;
 	public static int TRACKER_VALUE = 55613156;
 	
 	public void onCreate() {		
 		super.onCreate(); 
 
 		if(app == null)
 		    app = this;
 		
 		// Loading preferences
         String prefer = getString(R.string.preferences_name);
         String userId = getString(R.string.preference_user_id);
         SharedPreferences pref = getSharedPreferences(prefer, 0);
        userId = pref.getString(userId, null);
        if(userId!=null)
            this.currentUserId = Integer.valueOf(userId);
 
         initFacebook();
 		if(gTracker == null){
 			gTracker = GoogleAnalyticsTracker.getInstance();
 			gTracker.startNewSession(TRACK_ID, this);
 			setTrackerInformation();
 		}
 	}
 
 	public void initFacebook(){
 		if (DogUtil.FACEBOOK_APPID == null) {
 			Util.showAlert(getApplicationContext(), getResources().getString(R.string.warning) , getResources().getString(R.string.invalid_facebook_id));
 		}
 
 		if(mFacebook ==null)
 			mFacebook = new Facebook(DogUtil.FACEBOOK_APPID);		
 	
 
 		if(mAsyncRunner ==null)
 			mAsyncRunner = new AsyncFacebookRunner(mFacebook);
 	}
 
 	public Facebook getFacebook(){
 		return mFacebook;
 	}
 
 	public AsyncFacebookRunner getAsyncFacebookRunner(){
 		return mAsyncRunner;
 	}
 
 	public String[] getPermissions(){
 		return PERMISSIONS;
 	}
 
 	public GoogleAnalyticsTracker getTracker(){
 		if(gTracker == null){
 			gTracker = GoogleAnalyticsTracker.getInstance();
 			gTracker.startNewSession(TRACK_ID, this);
 		}
 		return gTracker;
 	}
 
 	public void setTrackerInformation(){
 		gTracker.setCustomVar(1, "Application Details", "Android, "+getVersionName(this, DogUtil.class) +", "+
 				Build.MODEL+ ", "+android.os.Build.VERSION.RELEASE  , 1);
 	}
 
 	public static String getVersionName(Context context, Class cls) 
 	{
 		try {
 			ComponentName comp = new ComponentName(context, cls);
 			PackageInfo pinfo = context.getPackageManager().getPackageInfo(comp.getPackageName(), 0);
 			return pinfo.versionName;
 		} catch (android.content.pm.PackageManager.NameNotFoundException e) {
 			return null;
 		}
 	}
 	
 	
 	public static boolean checkNumber(String string){
 		try{
 			for (int i = 0; i < string.length(); i++) {
 				if (!Character.isDigit(string.charAt(i)))
 					return false;
 			}
 		}catch (NumberFormatException e) {
 			return false;
 		}
 
 		return true;
 	}
 	
 	public static void setDeviceID(String deviceId){
 		DEVICE_ID = deviceId;
 	}
 	
 	public static String getDeviceUniqueNumber(){
 		return DEVICE_ID;
 	}
 
     public static DogUtil getInstance() {
         return app;
     }
 
     public Integer getCurrentUserId() {
         return this.currentUserId;
     }
 
 }
