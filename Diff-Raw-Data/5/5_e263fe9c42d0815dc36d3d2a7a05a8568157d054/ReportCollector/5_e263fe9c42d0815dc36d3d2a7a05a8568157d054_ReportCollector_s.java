 package ee.positium.reportcollector;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.location.LocationManager;
 import android.content.Context;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.provider.Settings;
 import org.apache.cordova.*;
 
 public class ReportCollector extends DroidGap {
   private boolean loaded = false;
   private AlertDialog dialog = null;
 
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
   }
   
   @Override
   public void onResume() {
     super.onResume();
 
     // check if location is enabled
     LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
     boolean gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
     boolean nl_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
 
     if (!(gps_enabled && nl_enabled)) {
       AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
       builder.setTitle(R.string.gps_dialog_title);
       builder.setMessage(R.string.gps_dialog_msg);
       builder.setCancelable(false);
       builder.setPositiveButton(R.string.gps_dialog_ok, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int id) {
           openLocationSettings();
         }
       });
       builder.setNegativeButton(R.string.gps_dialog_quit, new DialogInterface.OnClickListener() {
         @Override
         public void onClick(DialogInterface dialog, int id) {
           finish();
         }
       });
 
       dialog = builder.create();
       dialog.show();
     } else {
       if (dialog != null) dialog.dismiss();
 
       if (!loaded) {
         loaded = true;
         super.loadUrl("file:///android_asset/www/index.html");
       }
     }
   }
 
   private void openLocationSettings() {
     Intent settings_intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
     startActivity(settings_intent);
   }
 }
