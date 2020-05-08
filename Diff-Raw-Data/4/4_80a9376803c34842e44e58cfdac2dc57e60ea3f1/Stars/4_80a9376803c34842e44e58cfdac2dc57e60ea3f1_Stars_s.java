 package com.fsck.sector25;
 
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.Typeface;
 import android.util.Log;
 
 public class Stars {
     private int width;
     private int height;
     private float stars[][] = new float[1500][3];
     private float planetPosition[][] = new float[5][3];
     private Bitmap[] planets;
 
     public Stars(Resources res) {
         for (int i = 0; i < 1500; i++) {
             stars[i][0] = (float) (Math.random() * 1.5 - 0.25);
             stars[i][1] = (float) (Math.random() * 1.5 - 0.25);
             stars[i][2] = (float) (Math.random() * 0.00006);
         }
         for (int i = 0; i < 5; i++) {
             planetPosition[i][0] = (float) (Math.random() * 1.5 - 0.25);
             planetPosition[i][1] = (float) (Math.random() * 1.5 - 0.25);
             planetPosition[i][2] = (float) (Math.random() * 0.00006);
         }
         planets = new Bitmap[5];
         //TODO: scale planets down to appear in background
         planets[0] = BitmapFactory.decodeResource(res,
                 R.drawable.planet1);
         planets[1] = BitmapFactory.decodeResource(res,
                 R.drawable.planet2);
         planets[2] = BitmapFactory.decodeResource(res,
                 R.drawable.planet3);
         planets[3] = BitmapFactory.decodeResource(res,
                 R.drawable.planet4);
         planets[4] = BitmapFactory.decodeResource(res,
                 R.drawable.planet5);
     }
 
     public void set(int width, int height){
         this.width = width;
         this.height = height;
         for(int i = 0; i < planets.length; i++){
             Log.d("sdafasd", "height: " + height + "   width: " + width);
             Log.d("sdafasd2", "bit width: " + planets[i].getWidth());
             planets[i] = Bitmap.createScaledBitmap(planets[i], (int)((float)planets[i].getWidth()/2000*(float)width),
                     (int)((float)planets[i].getWidth()/2000*(float)width), false);
         }
     }
 
     public void draw(Canvas canvas, Paint paint){
         for (int i = 0; i < 1500; i++) {
             canvas.drawPoint(stars[i][0]*width, stars[i][1]*height, paint);
         }
 
         paint.setAlpha(60);
         for (int i = 0; i < 5; i++) {
             canvas.drawBitmap(planets[i], planetPosition[i][0]*width, planetPosition[i][1]*height, paint);
         }
         paint.setAlpha(255);
     }
 
     public void move(float x, float y){
         for (int i = 0; i < 1500; i++) {
            stars[i][0] += x * stars[i][2];
            stars[i][1] += y * stars[i][2];
             if(stars[i][0] > 1.25) stars[i][0] = -0.25f;
             if(stars[i][0] < -0.25) stars[i][0] = 1.25f;
             if(stars[i][1] > 1.25) stars[i][1] =  -0.25f;
             if(stars[i][1] < -0.25) stars[i][1] = 1.25f;
         }
         for (int i = 0; i < 5; i++) {
             planetPosition[i][0] -= x * planetPosition[i][2];
             planetPosition[i][1] -= y * planetPosition[i][2];
             if(planetPosition[i][0] > 1.25) planetPosition[i][0] = -0.25f;
             if(planetPosition[i][0] < -0.25) planetPosition[i][0] = 1.25f;
             if(planetPosition[i][1] > 1.25) planetPosition[i][1] = -0.25f;
             if(planetPosition[i][1] < -0.25) planetPosition[i][1] = 1.25f;
         }
     }
 }
