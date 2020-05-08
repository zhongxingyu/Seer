 package com.android.wifi.navigator.algorithms;
 
 /**
  * Created with IntelliJ IDEA.
  * User: linuxshaman
  * Date: 11.03.13
  * Time: 20:59
  * To change this template use File | Settings | File Templates.
  */
 public class Algorithm {
 
 
     public static Vector2D algorithm3(AlgorithmData data){
         float[][] coordsandvalue = new float[data.length][3];
         float sum;
         float a;
 
         if(data.spotsData.size() == 0) return null;
 
         for(int x = 0; x < data.width; x ++){
             for(int y = 0; y < data.length; y ++){
                 sum = 0;
                 a = 0;
                 WifiData w0Data = data.spotsData.get(0);
                 for(int i = 1; i < data.spotsData.size(); i ++){
                     WifiData wData = data.spotsData.get(i);
                     float rasstI = (float)Math.log10(rasst(x, y, wData.spot.getX(), wData.spot.getY()));
                     float rasst0 = (float)Math.log10(rasst(x, y, w0Data.spot.getX(), w0Data.spot.getY()));
                    a = (float)wData.rssi / (float)w0Data.rssi - rasstI / rasst0;
 
                     a = (float)Math.pow(a, 2);
                     sum += a;
                 }
                 coordsandvalue[y][0] = x;
                 coordsandvalue[y][1] = y;
                 coordsandvalue[y][2] = sum;
             }
         }
         double min = coordsandvalue[0][2];
         int minkey = 0;
         for(int i = 0; i < data.length;  i++){
             if(min > coordsandvalue[i][2]){
                 min = coordsandvalue[i][2];
                 minkey = i;
             }
         }
         return new Vector2D(coordsandvalue[minkey][0], coordsandvalue[minkey][1]);
     }
 
     public static Vector2D algorithm4(AlgorithmData data){
         float[][] coordsandvalue = new float[data.length][3];
         float sum;
         float a;
         float l0 = 100;
         float n = 2.f;
 
         if(data.spotsData.size() == 0) return null;
 
         for(int x = 0; x < data.width; x ++){
             for(int y = 0; y < data.length; y ++){
                 sum = 0;
                 a = 0;
                 WifiData w0Data = data.spotsData.get(0);
                 for(int i = 1; i < data.spotsData.size(); i ++){
                     WifiData wData = data.spotsData.get(i);
                     float rasstI = l0 - n * 10 *(float)Math.log10(rasst(x, y, wData.spot.getX(), wData.spot.getY()));
                     float rasst0 = l0 - n * 10 * (float)Math.log10(rasst(x, y, w0Data.spot.getX(), w0Data.spot.getY()));
                    a = (float)wData.rssi / (float)w0Data.rssi - rasstI / rasst0;
 
                     a = (float)Math.pow(a, 2);
                     sum += a;
                 }
                 coordsandvalue[y][0] = x;
                 coordsandvalue[y][1] = y;
                 coordsandvalue[y][2] = sum;
             }
         }
         double min = coordsandvalue[0][2];
         int minkey = 0;
         for(int i = 0; i < data.length;  i++){
             if(min > coordsandvalue[i][2]){
                 min = coordsandvalue[i][2];
                 minkey = i;
             }
         }
         return new Vector2D(coordsandvalue[minkey][0], coordsandvalue[minkey][1]);
     }
 
     public static Vector2D algorithm5(AlgorithmData data){
         Vector2D result = null;
         float[][] coordsandvalue = new float[data.length][3];
         float sum;
         float a;
         float l0 = 100;
         float n = 2.f;
 
         if(data.spotsData.size() == 0) return null;
 
         for(int x = 0; x < data.width; x ++){
             for(int y = 0; y < data.length; y ++){
                 sum = 0;
                 a = 0;
                 WifiData w0Data = data.spotsData.get(0);
                 for(int i = 1; i < data.spotsData.size(); i ++){
                     WifiData wData = data.spotsData.get(i);
                     float rasstI = l0 - n * 10 *(float)Math.log10(rasst(x, y, wData.spot.getX(), wData.spot.getY()));
                     float rasst0 = l0 - n * 10 * (float)Math.log10(rasst(x, y, w0Data.spot.getX(), w0Data.spot.getY()));
                    a = (float)wData.rssi / (float)w0Data.rssi - rasstI / rasst0;
 
                     a = (float)Math.pow(a, 2);
                     sum += a;
                 }
                 coordsandvalue[y][0] = x;
                 coordsandvalue[y][1] = y;
                 coordsandvalue[y][2] = sum;
             }
         }
         double min = coordsandvalue[0][2];
         int minkey = 0;
         for(int i = 0; i < data.length;  i++){
             if(min > coordsandvalue[i][2]){
                 min = coordsandvalue[i][2];
                 minkey = i;
             }
         }
         return new Vector2D(coordsandvalue[minkey][0], coordsandvalue[minkey][1]);
     }
 
 
     public static float rasst(float x1, float y1, float x2, float y2){
         float a = (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
         return (float)Math.pow(a, -2);
     }
 }
