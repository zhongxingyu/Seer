 package com.hastagqq.app;
 
<<<<<<< HEAD
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONObject;
=======
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.support.v4.app.FragmentActivity;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.Toast;
>>>>>>> upstream/master
 
 import com.google.android.gms.common.ConnectionResult;
 import com.google.android.gms.common.GooglePlayServicesUtil;
 import com.google.android.gms.gcm.GoogleCloudMessaging;
 import com.google.gson.Gson;
 import com.hastagqq.app.api.BasicApiResponse;
 import com.hastagqq.app.api.GetNewsApiResponse;
 import com.hastagqq.app.api.NewsApiClient;
 import com.hastagqq.app.model.DeviceInfo;
 import com.hastagqq.app.model.News;
 import com.hastagqq.app.util.Constants;
 import com.hastagqq.app.util.DBAdapter;
 import com.hastagqq.app.util.GPSTracker;
 import com.hastagqq.app.util.GsonUtil;
 import com.hastagqq.app.util.HttpUtil;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 <<<<<<< HEAD
 import android.content.Context;
 import android.content.SharedPreferences;
 import android.content.pm.PackageInfo;
 import android.content.pm.PackageManager;
 import android.database.Cursor;
 import android.location.Location;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.support.v4.app.Fragment;
 import android.app.ListActivity;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentTransaction;
 import android.support.v4.widget.SimpleCursorAdapter;
 import android.util.Log;
 import android.view.Menu;
 import android.widget.ListView;
 import android.widget.Toast;
 import java.io.IOException;
 
 public class MainActivity extends FragmentActivity implements NewsApiClient.GetCallback,
         NewsApiClient.CreateCallback {
 	private static final String TAG = MainActivity.class.getSimpleName();
     private static final String PROPERTY_APP_VERSION = "appVersion";
     private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
 
     public static final String EXTRA_MESSAGE = "message";
     public static final String PROPERTY_REG_ID = "registration_id";
 
     private String mRegId;
     private String mLocation;
     private GoogleCloudMessaging mGcm;
     
     private Fragment mNewsListFragment;
 
     private Context mContext;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
         if (!checkPlayServices()) {
             Toast.makeText(this, R.string.err_no_gcm, Toast.LENGTH_LONG).show();
             finish();
 
             return;
         } else {
             mGcm = GoogleCloudMessaging.getInstance(this);
             mContext = getApplicationContext();
             mRegId = getRegistrationId(mContext);
 
             if (mRegId.isEmpty()) {
                 registerInBackground();
             }
         }
 
         GPSTracker gpsTracker = new GPSTracker(MainActivity.this);
         mLocation = gpsTracker.getCity();
         Location location = gpsTracker.getLocation();
         
         if (location != null)
         Log.d(TAG, "::onCreate() -- " + location.getLatitude() + " - " + location.getLongitude());
         
         // NewsApiClient.createNews(new News("This is the new thing", "asdf", "ortigas", "traffic"),
         //         this);
         NewsApiClient.getNews("ortigas", this);
                 
         FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
         mNewsListFragment = new NewsListFragment();
         ft.replace(R.id.fl_fragment_container, mNewsListFragment, NewsListFragment.TAG_FRAGMENT);
         ft.commit();
 
         Log.d(TAG, "::onCreate() -- " + location.getLatitude() + " - " + location.getLongitude() + " - " + mLocation);
 
 //        NewsApiClient.createNews(new News("This is the new thing", "asdf", "Makati City", "traffic"),
 //                this);
 //        NewsApiClient.getNews("Makati City", this);
         CreateNewsFragment createNewsFragment = new CreateNewsFragment();
         Bundle args = new Bundle();
 
         args.putString(CreateNewsFragment.EXTRAS_LOCATION, mLocation);
         createNewsFragment.setArguments(args);
         getSupportFragmentManager().beginTransaction().replace(R.id.fl_fragment_container,
                 createNewsFragment).commit();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
         if (!checkPlayServices()) {
             Toast.makeText(this, R.string.err_no_gcm, Toast.LENGTH_LONG).show();
             finish();
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     private boolean checkPlayServices() {
         int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
         if (resultCode != ConnectionResult.SUCCESS) {
             if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                 GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                         PLAY_SERVICES_RESOLUTION_REQUEST).show();
             } else {
                 Log.e(TAG, "This device is not supported.");
                 finish();
             }
 
             return false;
         }
 
         return true;
     }
 
     private String getRegistrationId(Context context) {
         final SharedPreferences prefs = getGCMPreferences();
         String registrationId = prefs.getString(PROPERTY_REG_ID, "");
         if (registrationId.isEmpty()) {
             Log.i(TAG, "Registration not found.");
             return "";
         }
 
         int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
         int currentVersion = getAppVersion(context);
         if (registeredVersion != currentVersion) {
             Log.i(TAG, "App version changed.");
             return "";
         }
         return registrationId;
     }
 
     private static int getAppVersion(Context context) {
         try {
             PackageInfo packageInfo = context.getPackageManager()
                     .getPackageInfo(context.getPackageName(), 0);
             return packageInfo.versionCode;
         } catch (PackageManager.NameNotFoundException e) {
             // should never happen
             throw new RuntimeException("Could not get package name: " + e);
         }
     }
 
     private SharedPreferences getGCMPreferences() {
         return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
     }
 
     private void registerInBackground() {
         new AsyncTask<Void, Void, String>() {
             @Override
             protected String doInBackground(Void... params) {
                 String msg;
 
                 try {
                     if (mGcm == null) {
                         mGcm = GoogleCloudMessaging.getInstance(MainActivity.this);
                     }
 
                     mRegId = mGcm.register(Constants.SENDER_ID);
                     msg = "Device registered, registration ID=" + mRegId;
 
                     sendRegistrationIdToBackend();
                     storeRegistrationId(MainActivity.this, mRegId);
                 } catch (IOException ex) {
                     msg = "Error :" + ex.getMessage();
                 }
 
                 return msg;
             }
 
             @Override
             protected void onPostExecute(String msg) {
                 // TODO
             }
         }.execute(null, null, null);
     }
 
     private void sendRegistrationIdToBackend() {
         HttpClient httpClient = new DefaultHttpClient();
         HttpPost httpPost = new HttpPost(Constants.HOST + Constants.DEVICE);
         Gson defaultGsonParser = GsonUtil.getDefaultGsonParser();
         DeviceInfo deviceInfo = new DeviceInfo(mRegId, mLocation);
 
         Log.d(TAG, "::sendRegistrationIdToBackend() -- payload " + defaultGsonParser.toJson(deviceInfo));
         try {
             httpPost.setEntity(new StringEntity(defaultGsonParser.toJson(deviceInfo)));
             HttpResponse response = httpClient.execute(httpPost);
             BasicApiResponse basicApiResponse = HttpUtil.parseBasicApiResponse(response);
             Log.d(TAG, "::sendRegistrationIdToBackend() -- " + defaultGsonParser.toJson(basicApiResponse));
         } catch (IOException e) {
             Log.e(TAG, "::postData() -- ERROR: " + e.getMessage());
         }
     }
 
     private void storeRegistrationId(Context context, String regId) {
         final SharedPreferences prefs = getGCMPreferences();
         int appVersion = getAppVersion(context);
 
         SharedPreferences.Editor editor = prefs.edit();
         editor.putString(PROPERTY_REG_ID, regId);
         editor.putInt(PROPERTY_APP_VERSION, appVersion);
         editor.commit();
     }
 
     @Override
     public void onGetNewsComplete(GetNewsApiResponse apiResponse) {
         Log.d(TAG, "::onGetNewsComplete() -- START");
         
         /*DBAdapter db = new DBAdapter(MainActivity.this);
         db.open();
         for (int i = 0; i < 10; i++) {
             long id = db.inserContact("test context " + i, "test category " + i, i, "ortigas", "test title " + i);
             Log.d(TAG, "::onGetNewsComplete() -- id = " + id);
         }
         db.close();*/
         
         if (apiResponse.getNewsItems() == null) return;
         
         List<News> newss = apiResponse.getNewsItems();
         Log.d(TAG, "::onGetNewsComplete() -- newss size = " + newss.size());
         if (!newss.isEmpty()) {
             for (News news : newss) {
                 Log.d(TAG, "::onGetNewsComplete() -- location = " + news.getLocation());
             }
         }
         
         /*DBAdapter db = new DBAdapter(MainActivity.this);
         db.open();
         Cursor c = db.getAllNews();
         if (c.moveToFirst()) {
             do {
                 Log.d(TAG, "::onGetNewsComplete() -- id = " + c.getString(c.getColumnIndex(DBAdapter.KEY_ROWID)) + ", location = " + c.getString(c.getColumnIndex(DBAdapter.KEY_LOCATION)) 
                         + ", content = " + c.getString(c.getColumnIndex(DBAdapter.KEY_CONTENT)));
             } while (c.moveToNext());
         }*/
         
         
         Log.d(TAG, "::onGetNewsComplete() -- END");
     }
 
     @Override
     public void onCreateNewsComplete(BasicApiResponse apiResponse) {
         Log.d(TAG, "::onCreateNewsComplete() -- START");
         Log.d(TAG, "::onCreateNewsComplete() -- " + apiResponse);
         Log.d(TAG, "::onCreateNewsComplete() -- END");
     }
 }
