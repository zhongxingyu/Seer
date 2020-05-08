 package com.android.systemui.statusbar.quicksettings.quicktile;
 
 import android.content.BroadcastReceiver;
 import android.content.ContentResolver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.location.LocationManager;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.AnimationDrawable;
 import android.provider.Settings;
 import android.view.LayoutInflater;
 import android.net.Uri;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.View.OnLongClickListener;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 import com.android.systemui.R;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsController;
 import com.android.systemui.statusbar.quicksettings.QuickSettingsContainerView;
 
 public class GPSTile extends QuickSettingsTile {
 
     private boolean enabled = false;
     private boolean working = false;
     private boolean mEnabled = false;
 
     ContentResolver mContentResolver;
 
     public GPSTile(Context context, LayoutInflater inflater,
             QuickSettingsContainerView container, QuickSettingsController qsc) {
         super(context, inflater, container, qsc);
 
         mContentResolver = mContext.getContentResolver();
         mTileLayout = R.layout.quick_settings_tile_location;
         enabled = Settings.Secure.isLocationProviderEnabled(mContentResolver, LocationManager.GPS_PROVIDER);
 
         mOnClick = new OnClickListener() {
             @Override
             public void onClick(View v) {
                 Settings.Secure.setLocationProviderEnabled(mContentResolver, LocationManager.GPS_PROVIDER, !enabled);
             }
         };
 
         mOnLongClick = new OnLongClickListener() {
             @Override
             public boolean onLongClick(View v) {
                 startSettingsActivity(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                 return true;
             }
         };
         qsc.registerAction(LocationManager.PROVIDERS_CHANGED_ACTION, this);
         qsc.registerAction(LocationManager.GPS_ENABLED_CHANGE_ACTION, this);
         qsc.registerAction(LocationManager.GPS_FIX_CHANGE_ACTION, this);
     }
 
     @Override
     void onPostCreate() {
         applyGPSChanges();
         super.onPostCreate();
     }
 
     @Override
     public void onChangeUri(ContentResolver resolver, Uri uri) {
         applyGPSChanges();
     }
 
     void applyGPSChanges() {
         setGenericLabel();
         updateQuickSettings();
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         final String action = intent.getAction();
         enabled = Settings.Secure.isLocationProviderEnabled(mContentResolver, LocationManager.GPS_PROVIDER);
         boolean GPSenabled = intent.getBooleanExtra(LocationManager.EXTRA_GPS_ENABLED, false);
         if (action.equals(LocationManager.GPS_FIX_CHANGE_ACTION) && GPSenabled) {
             mEnabled = GPSenabled;
             working = false;
             applyGPSChanges();
         } else if (action.equals(LocationManager.GPS_ENABLED_CHANGE_ACTION) && !GPSenabled) {
             mEnabled = !GPSenabled;
             working = false;
             applyGPSChanges();
         } else {
             working = enabled;
             mEnabled = false;
             applyGPSChanges();
         }
     }
 
     @Override
     void updateQuickSettings() {
         TextView tv = (TextView) mTile.findViewById(R.id.gps_textview);
         if (tv != null) tv.setText(mLabel);
         ImageView iv = (ImageView) mTile.findViewById(R.id.gps_image);
         if (iv != null) {
            if (enabled && !mEnabled && working) {
               iv.setBackgroundResource(R.drawable.qs_gps_acquiring_anim);
               AnimationDrawable gpsAnimation = (AnimationDrawable) iv.getBackground();
               gpsAnimation.start();
            } else if (enabled && mEnabled) {
              iv.setBackgroundResource(R.drawable.ic_qs_gps_on);
            } else {
               iv.setBackgroundResource(R.drawable.ic_qs_gps_off);
            }
         }
         flipTile();
     }
 
     private void setGenericLabel() {
         // Show OFF next to the GPS label when in OFF state, ON/IN USE is indicated by the color
         String label = mContext.getString(R.string.quick_settings_gps);
         if (enabled) {
            mLabel = ((!mEnabled && working) ? mContext.getString(R.string.quick_settings_gps_search) : (label + " " + mContext.getString(R.string.quick_settings_label_disabled)));
         } else {
             mLabel = (label + " " + mContext.getString(R.string.quick_settings_label_disabled));
         }
     }
 }
