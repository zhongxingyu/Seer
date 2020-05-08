 package com.yandex.sample.droidcon.library;
 
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.TextView;
 
 public class MyActivity extends Activity {
 
     public static final String IN_APP_SKU = "com.amazon.buttonclicker.blue_button";
     public static final String SUBSCRIPTION_SKU = "com.amazon.buttonclicker.subscription.1mo";
     private BroadcastReceiver receiver;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         updateState();
         receiver = new BroadcastReceiver() {
             @Override
             public void onReceive(Context context, Intent intent) {
                 System.out.println("MyActivity.onReceive");
                 updateState();
             }
         };
     }
 
     @Override
     protected void onResume() {
         System.out.println("MyActivity.onResume");
         registerReceiver(receiver, new IntentFilter(LibraryApplication.makeStateChangedBroadcast(this)));
         super.onResume();
         updateState();
     }
 
     @Override
     protected void onPause() {
         System.out.println("MyActivity.onPause");
         unregisterReceiver(receiver);
         super.onPause();
     }
 
     private void updateState() {
         final LibraryApplication application = (LibraryApplication) getApplication();
         TextView level4 = (TextView) findViewById(R.id.level3);
         final boolean state = application.getEntitlementState(IN_APP_SKU);
         if (state) {
            level4.setTextColor(getResources().getColor(R.color.text_blue_button_unlocked));
             level4.setBackgroundResource(R.drawable.btn_blue);
             level4.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
         } else {
//            level4.setTextColor(getResources().getColor(R.color.text_blue_button_locked));
//            level4.setBackgroundResource(android.R.drawable.btn_default);
//            level4.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_lock, 0, R.drawable.ic_lock_lock_right, 0);
         }
 
 
         TextView subscription = (TextView) findViewById(R.id.subscription);
 
         final int daysLeft = application.getSubscriptionDaysLeft();
         switch (daysLeft) {
             case LibraryApplication.UNLIMITED:
                 subscription.setVisibility(View.GONE);
                 break;
             case LibraryApplication.EXPIRED:
                 subscription.setVisibility(View.VISIBLE);
                 //todo: better visual indication
                 subscription.setText(R.string.days_expired);
                 break;
             default:
                 subscription.setText(getString(R.string.days_left, daysLeft));
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     public void onSubscriptionClick(View v) {
         purchaseSku(SUBSCRIPTION_SKU);
     }
 
     public void onLevel4Click(View v) {
         purchaseSku(IN_APP_SKU);
     }
 
     private void purchaseSku(String sku) {
         final LibraryApplication application = (LibraryApplication) getApplication();
         application.purchase(sku);
     }
 }
