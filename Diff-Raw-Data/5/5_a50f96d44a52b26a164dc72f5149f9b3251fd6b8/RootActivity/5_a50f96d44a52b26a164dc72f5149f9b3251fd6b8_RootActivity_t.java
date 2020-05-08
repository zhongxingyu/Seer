 package com.coffeeandpower;
 
 import java.text.DecimalFormat;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.Location;
 import android.os.Bundle;
 import android.os.Handler;
 import android.support.v4.app.FragmentActivity;
 import android.util.DisplayMetrics;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.SlidingDrawer;
 import android.widget.TextView;
 
 import com.coffeeandpower.activity.ActivityLoginPage;
 import com.coffeeandpower.app.R;
 import com.coffeeandpower.tab.activities.ActivityVenueFeeds;
 import com.coffeeandpower.urbanairship.IntentReceiver;
 import com.coffeeandpower.utils.Executor;
 import com.coffeeandpower.utils.Executor.ExecutorInterface;
 
 public class RootActivity extends FragmentActivity {
 
     public static final int DIALOG_MUST_BE_A_MEMBER = 30;
     public static final int DIALOG_CONTACT_EXCHANGE_ACCEPTED = 40;
 
     private AlertDialog alert;
     private SlidingDrawer mDrawer = null;
 
     private Executor mExe = null;
     private BroadcastReceiver mContactExchangeReceiver;
 
     @Override
     protected void onCreate(Bundle instance) {
         super.onCreate(instance);
         if (Constants.debugLog)
             Log.d("RootActivity", "RootActivity.onCreate()");
 
         // register running activity for intents which will be used to 
         // pass contact exchange requests/acknowledgments from push notifications
         IntentFilter filter = new IntentFilter();
         filter.addAction(IntentReceiver.ACTION_CONTACT_EXCHANGE_REQUESTED);
         filter.addAction(IntentReceiver.ACTION_CONTACT_EXCHANGE_ACCEPTED);
 
         mContactExchangeReceiver = new BroadcastReceiver() {
 
             @Override
             public void onReceive(Context context, Intent intent) {
                 if (null != intent) {
                     if (intent.getAction().equals(IntentReceiver.ACTION_CONTACT_EXCHANGE_REQUESTED)) {
                         // unpack the bundle
                         Bundle extras = intent.getExtras();
                         String msg = extras.getString(IntentReceiver.EXTRA_ALERT);
                         String id = extras.getString(IntentReceiver.EXTRA_CONTACT_REQUEST_SENDER);
                         // handle the request
                         handleContactExchangeRequest(id, msg);
                     } else if (intent.getAction().equals(IntentReceiver.ACTION_CONTACT_EXCHANGE_ACCEPTED)) {
                         // message the acceptance
                         showDialog(DIALOG_CONTACT_EXCHANGE_ACCEPTED, intent.getExtras());
                     }
                 }
             }
         };
         this.registerReceiver(mContactExchangeReceiver, filter);
 
         // Executor
         mExe = new Executor(RootActivity.this);
         mExe.setExecutorListener(new ExecutorInterface() {
             @Override
             public void onErrorReceived() {
                 // TODO any follow-up from action executor errors?
             }
 
             @Override
             public void onActionFinished(int action) {
                 // TODO any follow-up from action completion?
             }
         });
     }
 
     @Override
     protected void onDestroy() {
         if (Constants.debugLog)
             Log.d("RootActivity", "RootActivity.onDestroy()");
         if (null != mDrawer) {
             mDrawer.close();
             mDrawer = null;
         }
         unregisterReceiver(mContactExchangeReceiver);
         super.onDestroy();
     }
     
     
     @Override
     protected void onRestoreInstanceState(Bundle savedInstanceState) {
         super.onRestoreInstanceState(savedInstanceState);
         
         if (savedInstanceState.getBoolean("shouldchecklogin", false)) {
             int uid = AppCAP.getLoggedInUserId();
             if (uid != 0) {
                 AppCAP.setLoggedInUserId(uid);
                 AppCAP.setLoggedIn(true);            
             } else {
                 Intent i = new Intent();
                 i.setClass(getApplicationContext(), ActivityLoginPage.class);
                 startActivity(i);
                 this.finish();
             }
         }
     }
 
     @Override
     protected void onSaveInstanceState(Bundle outState) {
         super.onSaveInstanceState(outState);
         outState.putBoolean("shouldchecklogin", true);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         if (Constants.debugLog)
             Log.d("RootActivity", "RootActivity.onResume()");
         AppCAP.setActive(true);
 
         Intent i = getIntent();
         if(null != i.getStringExtra(IntentReceiver.EXTRA_CONTACT_ACTION_PENDING)) {
             final String msg = i.getStringExtra(IntentReceiver.EXTRA_ALERT);
             final String id = i.getStringExtra(IntentReceiver.EXTRA_CONTACT_REQUEST_SENDER);
             Handler h = new Handler();
             h.postDelayed(new Runnable() {
                 
                 @Override
                 public void run() {
                     handleContactExchangeRequest(id, msg);
                 }
             }, 3000);
         }
 
     }
 
     @Override
     protected void onPause() {
         if (Constants.debugLog)
             Log.d("RootActivity", "RootActivity.onPause()");
         AppCAP.setActive(false);
         super.onPause();
     }
 
     @Override
     protected void onStop() {
         if (Constants.debugLog)
             Log.d("RootActivity", "RootActivity.onStop()");
 
         super.onStop();
     }
 
     /**
      * Easy log
      * 
      * @param msg
      */
     public static void log(String msg) {
         if (Constants.debugLog)
             Log.d(AppCAP.TAG, msg);
     }
 
     /**
      * Get distance between points
      * 
      * @param startLat
      * @param startLng
      * @param endLat
      * @param endLng
      * @return String 100m or 5.4km
      */
     public static String getDistanceBetween(double startLat, double startLng,
             double endLat, double endLng, boolean addFarAway) {
         float[] results = new float[1];
         Location.distanceBetween(startLat, startLng, endLat, endLng, results);
 
         if (Float.isNaN(results[0])
                 || (addFarAway && (results[0] / 1000) > 500)) {
             return AppCAP.getAppContext().getString(R.string.map_distance_far); 
         }
         return formatToMetricsOrImperial(results[0]);
     }
 
     public static String formatToMetricsOrImperial(float distance) {
         DecimalFormat oneDForm = new DecimalFormat("#.#");
         String distanceS = "";
 
         if (AppCAP.isMetrics()) {
             if (distance < 100) {
                 float d = Float.valueOf(oneDForm.format(distance));
                 distanceS = d + "m";
             } else {
                 float d = Float.valueOf(oneDForm.format(distance / 1000));
                 distanceS = d + "km";
             }
         } else {
             distance = distance * 3.28f; // feets
             if (distance < 1000) {
                 float d = Float.valueOf(oneDForm.format(distance));
                 distanceS = d + "ft";
             } else {
                 float d = Float.valueOf(oneDForm.format(distance / 5280));
                 distanceS = d + "mi";
             }
         }
         return distanceS;
     }
 
     protected DisplayMetrics getDisplayMetrics() {
         DisplayMetrics metrics = new DisplayMetrics();
         getWindowManager().getDefaultDisplay().getMetrics(metrics);
         return metrics;
     }
 
     @Override
     protected Dialog onCreateDialog(int id) {
         AlertDialog.Builder builder = new AlertDialog.Builder(RootActivity.this);
 
         switch (id) {
 
         case DIALOG_MUST_BE_A_MEMBER:
            builder.setMessage("You must be a member to use this feature.")
                .setCancelable(false)
                     .setPositiveButton("LOGIN",
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int id) {
                                     Activity a = alert.getOwnerActivity();
                                     if (a != null) {
                                         Intent i = new Intent();
                                         i.setClass(a, ActivityLoginPage.class);
                                         i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                         a.startActivity(i);
                                         a.finish();
                                     }
                                     dialog.cancel();
                                 }
                             })
                     .setNegativeButton("Cancel",
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int id) {
                                     dialog.cancel();
                                 }
                             });
             alert = builder.create();
             break;
 
         default:
             alert = null;
             break;
         }
 
         return alert;
     }
 
     
     /* (non-Javadoc)
      * @see android.app.Activity#onCreateDialog(int, android.os.Bundle)
      */
     @Override
     protected Dialog onCreateDialog(int id, Bundle args) {
         AlertDialog.Builder builder = new AlertDialog.Builder(RootActivity.this);
 
         switch (id) {
         case DIALOG_CONTACT_EXCHANGE_ACCEPTED:
             String name = args.getString(IntentReceiver.EXTRA_CONTACT_REQUEST_ACCEPTED_NICK);
             builder.setMessage(name + " exchanged contact info with you.")
                     .setCancelable(false)
                     .setPositiveButton("OK",
                             new DialogInterface.OnClickListener() {
                                 public void onClick(DialogInterface dialog,
                                         int id) {
                                     dialog.cancel();
                                 }
                             });
             alert = builder.create();
             break;
         }
         return alert;
     }
 
     @Override
     protected boolean isRouteDisplayed() {
         return false;
     }
 
     public String getResStr(int id) {
         return getResources().getString(id);
     }
 
     public boolean startSmartActivity(Intent intent, String activityName) {
         if (activityName == "ActivityMap") {
             intent.putExtra("fragment", R.id.tab_fragment_area_map);
             intent.setClass(RootActivity.this, ActivityVenueFeeds.class);
             startActivity(intent);
             return true;
         } else if (activityName == "ActivityPeopleAndPlaces") {
             intent.putExtra("fragment", R.id.tab_fragment_area_places);
             intent.setClass(RootActivity.this, ActivityVenueFeeds.class);
             startActivity(intent);
             return true;
         } else if (activityName == "ActivityContacts") {
             intent.putExtra("fragment", R.id.tab_fragment_area_contacts);
             intent.setClass(RootActivity.this, ActivityVenueFeeds.class);
             startActivity(intent);
             return true;
         }
         return false;
     }
 
     /*
      * Contact Exchange
      */
     private void initContactExchangeDrawer() {
         if (null == mDrawer) {
             /*
              * initialize the sliding drawer by obtaining the top level window
              * for the application and inflating our widget view into the first
              * child of the 'decor view'. By definition in the view hierarchy,
              * this is the 'content view' provided by Android.
              */
             Window w = getWindow();
             ViewGroup root = (ViewGroup)w.getDecorView();
             // sanity check view hierarchy
             if (root.getChildCount() > 0 && root.getChildAt(0) instanceof FrameLayout) {
                 FrameLayout content = (FrameLayout)root.getChildAt(0);
                 View.inflate(this.getApplicationContext(), R.layout.widget_contact_exchange, content);
                 // the newly inflated view in our hierarchy.
                 mDrawer = (SlidingDrawer)findViewById(R.id.contact_exchange_widget);
             }
         }
     }
 
     protected void onAcceptExchangeRequest(String id) {
         mExe.acceptContactExchangeRequest(Integer.parseInt(id));
     }
 
     protected void onDeclineExchangeRequest(String id) {
         mExe.declineContactExchangeRequest(Integer.parseInt(id));
     }
 
     public void handleContactExchangeRequest(final String id, final String msg) {
 
         if(null != id && null != msg) {
             // setup the view
             initContactExchangeDrawer();
 
             // update the content
             if (null != mDrawer) {
                 // update the message
                 TextView tv = (TextView)findViewById(R.id.contact_exchange_message);
                 if (null != tv) {
                     tv.setText(msg);
                 }
 
                 // set up our buttons
                 final Button accept = (Button)findViewById(R.id.btn_contact_exchange_accept);
                 final Button decline = (Button)findViewById(R.id.btn_contact_exchange_decline);
 
                 accept.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         if (null != mDrawer) {
                             onAcceptExchangeRequest(id);
                             mDrawer.animateClose();
                         }
                     }
                 });
 
                 decline.setOnClickListener(new OnClickListener() {
                     @Override
                     public void onClick(View v) {
                         if (null != mDrawer) {
                             onDeclineExchangeRequest(id);
                             mDrawer.animateClose();
                         }
                     }
                 });
 
                 // show the view
                 mDrawer.animateOpen();
             } else {
                 Log.d("RootActivity", "Failed to initialize contact exchange drawer");
             }
         }
     }
 }
