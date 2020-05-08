 package com.wallpaper.sundial;
 
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Bitmap;
 import android.graphics.Point;
 import android.graphics.PixelFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.*;
 import android.graphics.drawable.*;
 import android.graphics.Shader.*;
 import android.os.Handler;
 import android.service.wallpaper.WallpaperService;
 import android.view.SurfaceHolder;
 import android.os.Bundle;
 import android.content.Context;
 import android.location.Criteria;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import java.util.List;
 import java.util.Calendar;
 import java.util.TimeZone;
 import android.util.Log;
 import android.view.Display;
 import android.view.WindowManager;
 // import java.lang.Math;
 
 public class SundialWallpaperService extends WallpaperService {
 
     @Override
     public Engine onCreateEngine() {
         return new SundialWallpaperEngine();
     }
 
     public native double[]  getSunPos(int hour, int min, int sec, double lat, double lng);
     public native double[]  getMoonPos(int hour, int min, int sec, double lat, double lng);
 
     static {
         System.loadLibrary("sundial");
     }
     
     private class SundialWallpaperEngine extends Engine {
         private boolean mVisible = false;
         private final Handler mHandler = new Handler();
         private double i = 0;
         private double aspectRatio;
         private int screenWidth;
         private int screenHeight;
         private double lat;
         private double lng;
         private int celestialOffset = 400;
 
         private Bitmap moonBitmap;
         private Bitmap sunBitmap;
         private Bitmap bgBitmap;
         private Bitmap bgBitmapSmall;
         private Bitmap moonBitmapSmall;
         private GradientDrawable bgGrad;
 
         private Paint p;
 
         private LocationManager mgr = (LocationManager) getSystemService(LOCATION_SERVICE);
         private LocationListener onLocationChange = new LocationListener() {
             public void onLocationChanged(Location location) {
                 update(location);
             }
             
             public void onProviderDisabled(String provider) {}
             
             public void onProviderEnabled(String provider) {}
             
             public void onStatusChanged(String provider, int status, Bundle extras) {}
         };
 
         private void update(Location location){
             if(location != null){
                 lat = location.getLatitude();
                 lng = location.getLongitude();
             } else {
                 lat = 37.871592;
                 lng = -122.2937;
             }
         }
 
        private Point scaleCelestialPosition(Bitmap img, double x, double y, int screenWidth, int screenHeight){
            if(y < 0.0){
                return new Point(-100, -100);
            } else {
                int newX = (int) Math.round(x / 360.0 * screenWidth);
                int newY = (int) Math.round(y / 90.0 * (screenHeight-celestialOffset));
                newY = screenHeight - newY - celestialOffset;
                newX = (int) Math.round(newX - img.getWidth()/2.0);
                newY = (int) Math.round(newY - img.getWidth()/2.0);
                return new Point(newX, newY);
            }
        }

         @Override
         public void onCreate(SurfaceHolder surfaceHolder)
         {
             update(null);
             surfaceHolder.setFormat(PixelFormat.RGBA_8888);
             BitmapFactory.Options options = new BitmapFactory.Options();
             options.inPreferredConfig = Bitmap.Config.ARGB_8888;
 
             
             int colors[] = {0xFFEEFFFF, 0xFF2F3B47};
             bgGrad = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP, colors);
 
             p = new Paint(){
                 {
                     setAntiAlias(true);
                     setFilterBitmap(true);
                     setDither(true);
                 }
             };
             moonBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.moon, options);
             sunBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sun, options);
             bgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg, options);
             moonBitmapSmall = Bitmap.createScaledBitmap(moonBitmap, 120, 120, false);
 
             mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                        3600000, 1000,
                                        onLocationChange);
         }
 
         private final Runnable mUpdateDisplay = new Runnable() {
         @Override
         public void run() {
             draw();
         }};
     
         private void draw() {
             
            SurfaceHolder holder = getSurfaceHolder();
            holder.setFormat(PixelFormat.RGBA_8888);
 
            Canvas c = null;
            try {
               c = holder.lockCanvas();
               if (c != null) {
                 screenWidth = c.getWidth();
                 screenHeight = c.getHeight();
                 aspectRatio = (double) screenWidth / (double) screenHeight;
                 double scale = screenWidth / bgBitmap.getWidth();
                 // this could have fixed-aspect ratio scaling
                 bgBitmapSmall =  Bitmap.createScaledBitmap(bgBitmap, 
                     screenWidth, 
                     screenHeight, 
                     false);
 
                 Calendar now = Calendar.getInstance(TimeZone.getDefault());
                 int sec = now.get(Calendar.SECOND);
                 int min = now.get(Calendar.MINUTE);
                 int hour = now.get(Calendar.HOUR_OF_DAY); // 24h format
                 // hour = (int) i;
                // hour = 18;
 
                 double[] sunPos = getSunPos(hour, min, sec, lat, lng);
                 double[] moonPos = getMoonPos(hour, min, sec, lat, lng);
 
                 // p.setDither(true);
                 // p.setAntiAlias(true);
                 // p.setTextSize(20);
                // double midpoint = c.getWidth() / 2.0;

                Point moon = scaleCelestialPosition(moonBitmap, moonPos[1], moonPos[0], screenWidth, screenHeight);
                Point sun = scaleCelestialPosition(sunBitmap, sunPos[1], sunPos[0], screenWidth, screenHeight);
 
                 int moonX = (int) Math.round(moonPos[1] / 360.0 * screenWidth);
                 int moonY = (int) Math.round(moonPos[0] / 90.0 * (screenHeight-celestialOffset));
                 moonY = screenHeight - moonY - celestialOffset;
 
                 int sunX = (int) Math.round(sunPos[1] / 360.0 * screenWidth);
                 int sunY = (int) Math.round(sunPos[0] / 90.0 * (screenHeight-celestialOffset));
                 sunY = screenHeight - sunY - celestialOffset;
 
                Log.v("com.wallpaper.sundial", "android log: moon alt " + moonPos[0] + " moon azi "+ moonPos[1] + "i: "+i +"\n");
                Log.v("com.wallpaper.sundial", "android log: sun alt " + sunPos[0] + " sun azi "+ sunPos[1] + "i: "+i +"\n");

 
 
                 // Drawing
                 ////////////////////////////
 
                 p.setColor(Color.BLACK);
                 c.drawRect(0, 0, c.getWidth(), c.getHeight(), p);
                 p.setColor(Color.WHITE);
 
                 bgGrad.setBounds(0,0,screenWidth,screenHeight);
                 bgGrad.draw(c);
 
                 moonX = (int) Math.round(moonX - moonBitmap.getWidth()/2.0);
                 moonY = (int) Math.round(moonY - moonBitmap.getHeight()/2.0);
                c.drawBitmap(moonBitmap, moon.x, moon.y, p);
 
                 sunX = (int) Math.round(sunX - sunBitmap.getWidth()/2.0);
                 sunY = (int) Math.round(sunY - sunBitmap.getHeight()/2.0);
                c.drawBitmap(sunBitmap, sun.x, sun.y, p);
 
                 int bgY = c.getHeight() - bgBitmapSmall.getHeight();
                 c.drawBitmap(bgBitmapSmall, 0, bgY, p);
               }
            } finally {
               if (c != null)
                  holder.unlockCanvasAndPost(c);
            }
            mHandler.removeCallbacks(mUpdateDisplay);
            if (mVisible) {
                mHandler.postDelayed(mUpdateDisplay, 100);
            }
            i += 0.5;
            if( i > 24) i = 0;
         }
         
         @Override
         public void onVisibilityChanged(boolean visible) {
             mVisible = visible;
             if (visible) {
                 draw();
             } else {
                 mHandler.removeCallbacks(mUpdateDisplay);
             }
         }
         
          @Override
           public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) {
              draw();
           }
         
         @Override
         public void onSurfaceDestroyed(SurfaceHolder holder) {
             super.onSurfaceDestroyed(holder);
             mVisible = false;
             mHandler.removeCallbacks(mUpdateDisplay);
         }
         
         @Override
         public void onDestroy() {
              super.onDestroy();
              mVisible = false;
              mHandler.removeCallbacks(mUpdateDisplay);
         }
     }    
 }
