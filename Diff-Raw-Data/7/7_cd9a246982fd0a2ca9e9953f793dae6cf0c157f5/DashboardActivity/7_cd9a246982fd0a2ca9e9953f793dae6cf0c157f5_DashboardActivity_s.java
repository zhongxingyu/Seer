 /*******************************************************************************
  * Copyright (c) 2012 sfleury.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  *
  * Contributors:
  *     sfleury - initial API and implementation
  ******************************************************************************/
 package org.gots.ui;
 
 import org.gots.R;
 import org.gots.ads.GotsAdvertisement;
 import org.gots.broadcast.BroadCastMessages;
 import org.gots.garden.GardenInterface;
 import org.gots.inapp.AppRater;
 import org.gots.weather.service.WeatherUpdateService;
 import org.gots.weather.view.WeatherView;
 import org.gots.weather.view.WeatherWidget;
 
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.animation.Animation;
 import android.view.animation.AnimationUtils;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.actionbarsherlock.app.ActionBar;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 import com.google.android.apps.analytics.GoogleAnalyticsTracker;
 
 public class DashboardActivity extends AbstractActivity implements OnClickListener {
     // GoogleAnalyticsTracker tracker;
     GotsAdvertisement adView;
 
     private WeatherWidget weatherWidget;
 
     private WeatherWidget weatherWidget2;
 
     private LinearLayout handle;
 
     private LinearLayout weatherWidgetLayout;
 
     private Intent weatherIntent;
 
     // private ImageView weatherState;
 
     private String TAG = "DashboardActivity";
 
     private MenuItem itemConnected;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         // NewRelic.withApplicationToken( "AA89617084bf906d3a0425f6cf6a382ce574b3acd8" ).start(this.getApplication());
         setContentView(R.layout.dashboard);
 
         // attach event handler to dash buttons
         findViewById(R.id.dashboard_button_hut).setOnClickListener(this);
         findViewById(R.id.dashboard_button_allotment).setOnClickListener(this);
         findViewById(R.id.dashboard_button_action).setOnClickListener(this);
         findViewById(R.id.dashboard_button_profile).setOnClickListener(this);
 
         handle = (LinearLayout) findViewById(R.id.handle);
 
         weatherWidgetLayout = (LinearLayout) findViewById(R.id.WeatherWidget);
 
         // ADMOB
         LinearLayout layout = (LinearLayout) findViewById(R.id.bannerAd);
         if (!gotsPrefs.isPremium()) {
             adView = new GotsAdvertisement(this);
             adView.getPremiumAds(layout);
 
         } else {
             layout.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_dashboard_top));
             ImageView logo = (ImageView) findViewById(R.id.idImageLogo);
             logo.setBackgroundDrawable(getResources().getDrawable(R.drawable.bt_logo_premium));
         }
         weatherIntent = new Intent(this, WeatherUpdateService.class);
 
         registerReceiver(weatherBroadcastReceiver, new IntentFilter(BroadCastMessages.WEATHER_DISPLAY_EVENT));
 
         // if (GotsPreferences.getInstance(this).getOAuthtToken() == null) {
         // Intent intent = new Intent(this, AccountList.class);
         // startActivityForResult(intent, 0);
         // }
     }
 
     private BroadcastReceiver weatherBroadcastReceiver = new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             if (BroadCastMessages.WEATHER_DISPLAY_EVENT.equals(intent.getAction())) {
 
                 updateUI(intent);
             } else if (BroadCastMessages.CONNECTION_SETTINGS_CHANGED.equals(intent.getAction())) {
                 refreshConnectionState();
 
             }
         }
 
     };
 
     protected void refreshConnectionState() {
         if (itemConnected == null) {
             return;
         }
         if (gotsPrefs.isConnectedToServer())
             itemConnected.setIcon(getResources().getDrawable(R.drawable.garden_connected));
         else
             itemConnected.setIcon(getResources().getDrawable(R.drawable.garden_disconnected));
     }
 
     private void updateUI(Intent intent) {
         boolean isError = intent.getBooleanExtra("error", true);
         Log.d(TAG, "=>" + isError);
 
         handle.removeAllViews();
         weatherWidgetLayout.removeAllViews();
 
         if (isError) {
             TextView txtError = new TextView(this);
             txtError.setText(getResources().getText(R.string.weather_citynotfound));
             txtError.setTextColor(getResources().getColor(R.color.text_color_light));
             handle.addView(txtError);
 
         } else {
             weatherWidget2 = new WeatherWidget(this, WeatherView.IMAGE);
             handle.addView(weatherWidget2);
             weatherWidget = new WeatherWidget(this, WeatherView.TEXT);
             weatherWidgetLayout.addView(weatherWidget);
         }
 
     }
 
     @Override
     public void onClick(View v) {
 
         Intent i = null;
         switch (v.getId()) {
         case R.id.dashboard_button_hut:
 
             i = new Intent(v.getContext(), HutActivity.class);
             break;
         case R.id.dashboard_button_allotment:
 
             i = new Intent(v.getContext(), MyMainGarden.class);
             break;
         case R.id.dashboard_button_action:
 
             i = new Intent(v.getContext(), ActionActivity.class);
 
             break;
         case R.id.dashboard_button_profile:
 
             i = new Intent(v.getContext(), org.gots.ui.ProfileActivity.class);
             break;
         default:
             break;
         }
         if (i != null) {
             startActivity(i);
         }
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
         unregisterReceiver(weatherBroadcastReceiver);
         stopService(weatherIntent);
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         GoogleAnalyticsTracker.getInstance().dispatch();
 
         new AsyncTask<Void, Void, GardenInterface>() {
             ActionBar bar = getSupportActionBar();
 
             @Override
             protected GardenInterface doInBackground(Void... params) {
                 return gardenManager.getCurrentGarden();
             }
 
             @Override
             protected void onPostExecute(GardenInterface currentGarden) {
                 if (currentGarden != null) {
                     bar.setTitle(currentGarden.getLocality());
                 } else {
                     bar.setTitle(gotsPrefs.getGardeningManagerAppname());
                 }
                 startService(weatherIntent);
 
                 // if (gotsPrefs.getCurrentGardenId() == -1) {
                 if (gardenManager.getCurrentGarden() == null) {
                     Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                     startActivity(intent);
                     Animation myFadeInAnimation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.tween);
                     findViewById(R.id.dashboard_button_profile).startAnimation(myFadeInAnimation);
                 } else
                     findViewById(R.id.dashboard_button_profile).clearAnimation();
 
                 refreshConnectionState();
                 super.onPostExecute(currentGarden);
             }
         }.execute();
 
     }
 
     @Override
     protected void onPause() {
         super.onPause();
 
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
 
         case R.id.help:
             // Intent browserIntent = new Intent(Intent.ACTION_VIEW,
             // Uri.parse(HelpUriBuilder.getUri(getClass().getSimpleName())));
             Intent browserIntent = new Intent(this, WebHelpActivity.class);
             browserIntent.putExtra(WebHelpActivity.URL, getClass().getSimpleName());
             startActivity(browserIntent);
 
             return true;
         case R.id.about:
             Intent aboutIntent = new Intent(this, AboutActivity.class);
             startActivity(aboutIntent);
 
             return true;
         case R.id.login:
             Intent loginIntent = new Intent(this, LoginActivity.class);
             startActivity(loginIntent);
             return true;
 
             // case R.id.settings:
             // // Intent settingsIntent = new Intent(this, SettingsActivity.class);
             // // startActivity(settingsIntent);
             // // FragmentTransaction ft = getFragmentManager().beginTransaction();
             // // ft.setCustomAnimations(android.R.animator.fade_in,
             // // android.R.animator.fade_out);
             // // ft.replace(R.id.idContent,new PreferenceActivity()).addToBackStack("back").commit();
             // return true;
 
         case R.id.connection:
             if (gotsPrefs.isConnectedToServer())
                 Toast.makeText(this, getResources().getString(R.string.login_connect_state), Toast.LENGTH_SHORT).show();
             else
                 Toast.makeText(this, getResources().getString(R.string.login_disconnect_state), Toast.LENGTH_SHORT).show();
             return true;
 
         default:
             return super.onOptionsItemSelected(item);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.menu_dashboard, menu);
         itemConnected = (MenuItem) menu.findItem(R.id.connection);
         refreshConnectionState();
         return true;
     }
 
 }
