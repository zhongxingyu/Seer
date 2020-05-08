 /*
  * Copyright (C) 2013 The OmniROM Project
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 3
  * of the License, or (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package com.android.phasebeam;
 
 import android.app.Activity;
 import android.app.WallpaperManager;
 import android.app.WallpaperInfo;
 import android.app.Dialog;
 import android.service.wallpaper.IWallpaperConnection;
 import android.service.wallpaper.IWallpaperService;
 import android.service.wallpaper.IWallpaperEngine;
 import android.service.wallpaper.WallpaperSettingsActivity;
 import android.content.ServiceConnection;
 import android.content.Intent;
 import android.content.Context;
 import android.content.ComponentName;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.RemoteException;
 import android.os.IBinder;
 import android.os.ParcelFileDescriptor;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.WindowManager;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.LayoutInflater;
 import android.util.Log;
 import android.widget.TextView;
 
 public class PhaseBeamSelector extends Activity {
 
     private static final String LOG_TAG = "PhaseBeamSelector";
 
     public static final String KEY_PREFS = "phasebeam";
     public static final String KEY_HUE = "hue";
 
     private WallpaperManager mWallpaperManager;
     private WallpaperConnection mWallpaperConnection;
 
     private Intent mWallpaperIntent;
     private SharedPreferences mSharedPref;
     private float mCurrentHue;
 
     private float mInitialDownX;
     private float mDownX;
     private float mDownY;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
         setContentView(R.layout.selector);
 
         mWallpaperIntent = new Intent(this, PhaseBeamWallpaper.class);
         
         mWallpaperManager = WallpaperManager.getInstance(this);
         mWallpaperConnection = new WallpaperConnection(mWallpaperIntent);
         
         mSharedPref = getSharedPreferences(KEY_PREFS, Context.MODE_PRIVATE);
         mCurrentHue = mSharedPref.getFloat(KEY_HUE, 0.0f);
     }
     
     @Override
     public void onResume() {
         super.onResume();
         if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
             try {
                 mWallpaperConnection.mEngine.setVisibility(true);
             } catch (RemoteException e) {
                 // Ignore
             }
         }
     }
     
     @Override
     public void onPause() {
         super.onPause();
         if (mWallpaperConnection != null && mWallpaperConnection.mEngine != null) {
             try {
                 mWallpaperConnection.mEngine.setVisibility(false);
             } catch (RemoteException e) {
                 // Ignore
             }
         }
     }
 
     @Override
     public void onAttachedToWindow() {
         super.onAttachedToWindow();
 
         if (!mWallpaperConnection.connect()) {
             mWallpaperConnection = null;
         }
     }
 
     @Override
     public void onDetachedFromWindow() {
         super.onDetachedFromWindow();
         
         if (mWallpaperConnection != null) {
             mWallpaperConnection.disconnect();
         }
         mWallpaperConnection = null;
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event) {
         // TODO: make this conditional on preview mode. Right now we
         // don't get touch events in preview mode.
         switch(event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 mDownX = mInitialDownX = event.getX();
                 mDownY = event.getY();
                 return true;
 
             case MotionEvent.ACTION_UP:
                 if (Math.abs(event.getX() - mInitialDownX) < 2.0f) {
                     mCurrentHue = 0.0f;
                     updatePrefs();
                 }
                 return true;
 
             case MotionEvent.ACTION_MOVE:
                 mCurrentHue += (float)(Math.PI / 180.0f) * (mDownX - event.getX());
                 updatePrefs();
                 mDownX = event.getX();
                 mDownY = event.getY();
                 return true;
         }
 
         return super.onTouchEvent(event);
     }
     
     private void updatePrefs() {
         Editor edit = mSharedPref.edit();
         edit.putFloat(KEY_HUE, mCurrentHue);
        edit.commit();
     }
     
     class WallpaperConnection extends IWallpaperConnection.Stub implements ServiceConnection {
         final Intent mIntent;
         IWallpaperService mService;
         IWallpaperEngine mEngine;
         boolean mConnected;
 
         WallpaperConnection(Intent intent) {
             mIntent = intent;
         }
 
         public boolean connect() {
             synchronized (this) {
                 if (!bindService(mIntent, this, Context.BIND_AUTO_CREATE)) {
                     return false;
                 }
                 
                 mConnected = true;
                 return true;
             }
         }
         
         public void disconnect() {
             synchronized (this) {
                 mConnected = false;
                 if (mEngine != null) {
                     try {
                         mEngine.destroy();
                     } catch (RemoteException e) {
                         // Ignore
                     }
                     mEngine = null;
                 }
                 unbindService(this);
                 mService = null;
             }
         }
         
         public void onServiceConnected(ComponentName name, IBinder service) {
             if (mWallpaperConnection == this) {
                 mService = IWallpaperService.Stub.asInterface(service);
                 try {
                     final View view = findViewById(R.id.backgroundview);
                     final View root = view.getRootView();
                     mService.attach(this, view.getWindowToken(),
                             WindowManager.LayoutParams.TYPE_APPLICATION_MEDIA_OVERLAY,
                             true, root.getWidth(), root.getHeight());
                 } catch (RemoteException e) {
                     Log.w(LOG_TAG, "Failed attaching wallpaper; clearing", e);
                 }
             }
         }
 
         public void onServiceDisconnected(ComponentName name) {
             mService = null;
             mEngine = null;
             if (mWallpaperConnection == this) {
                 Log.w(LOG_TAG, "Wallpaper service gone: " + name);
             }
         }
         
         public void attachEngine(IWallpaperEngine engine) {
             synchronized (this) {
                 if (mConnected) {
                     mEngine = engine;
                     try {
                         engine.setVisibility(true);
                     } catch (RemoteException e) {
                         // Ignore
                     }
                 } else {
                     try {
                         engine.destroy();
                     } catch (RemoteException e) {
                         // Ignore
                     }
                 }
             }
         }
         
         public ParcelFileDescriptor setWallpaper(String name) {
             return null;
         }
 
         public void engineShown(IWallpaperEngine engine) throws RemoteException {
         }
     }
 }
