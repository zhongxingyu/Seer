 package com.RoboMobo;
 
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Point;
 import android.hardware.Sensor;
 import android.hardware.SensorManager;
 import android.location.LocationManager;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class ActivityMain extends Activity// implements View.OnTouchListener
 {
     public boolean flag = true;
     public SensorManager msensorManager;
     public DataExchange dataExchange;
 
     public final Handler HandlerUIUpdate = new Handler()
     {
         @Override
         public void handleMessage(Message msg)
         {
             TextView tv = (TextView) RMR.am.findViewById(R.id.tv_score);
             tv.setText("Очки: " + msg.arg1);
         }
     };
 
     public final Handler HandleBluetoothInput = new Handler()
     {
         @Override
         public void handleMessage(Message msg)
         {
             JSONObject jobj = (JSONObject) msg.obj;
 
             if (jobj.has("Map") && RMR.state == RMR.GameState.Client)
             {
                 try
                 {
                     RMR.currentMap = new Map(jobj.getJSONObject("Map").getInt("width"), jobj.getJSONObject("Map").getInt("height"));
                     for (int i = 0; i < RMR.currentMap.width; i++)
                     {
                         for (int j = 0; j < RMR.currentMap.height; j++)
                         {
                             RMR.currentMap.tiles[i][j] = (short) ((JSONArray) jobj.getJSONObject("Map").getJSONArray("Tiles").get(i)).getInt(j);
                         }
                     }
                 }
                 catch (JSONException e)
                 {
 
                 }
 
                 RMR.state = RMR.GameState.ClientIngame;
             }
 
             if (jobj.has("Player"))
             {
                 try
                 {
                     RMR.currentMap.p1.changePos(new int[]{jobj.getJSONObject("Player").getInt("X"), jobj.getJSONObject("Player").getInt("Y")});
                 }
                 catch (JSONException e)
                 {
                     e.printStackTrace();
                 }
             }
         }
     };
 
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         RMGR.init(this);
         setContentView(R.layout.main);
         LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
         RMR.gps = new GPSModule();
         mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, RMR.gps);
         //((MainSurfaceView) findViewById(R.id.view)).setOnTouchListener(this);
         RMR.compass = new CompassModule();
         msensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
 
         RMR.sw = (MainSurfaceView) findViewById(R.id.view_ingame_canvas);
         RMR.registerActivity(this);
 
         dataExchange = new DataExchange(HandleBluetoothInput);
         dataExchange.start();
     }
 
     @Override
     protected void onResume()
     {
         super.onResume();
         msensorManager.registerListener(RMR.compass, msensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
         msensorManager.registerListener(RMR.compass, msensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
     }
 
     @Override
     protected void onPause()
     {
         super.onPause();
         msensorManager.unregisterListener(RMR.compass);
     }
 
     public void fixCoord(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             if (RMR.currentMap.state != Map.MapState.Suspended)
             {
                 if (flag)
                 {
                     RMR.currentMap.fixCorner1(RMR.gps.last_latt, RMR.gps.last_long);
                     flag = false;
                     Log.wtf("fix", "1");
                 }
                 else
                 {
                     RMR.currentMap.fixCorner2(RMR.gps.last_latt, RMR.gps.last_long);
                     flag = true;
                     Log.wtf("fix", "2");
                 }
             }
         }
     }
 
     public void moveUp(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosX = RMR.currentMap.p0.posX;
             RMR.currentMap.p0.prevPosY = RMR.currentMap.p0.posY;
             RMR.currentMap.p0.posX -= 32;
         }
     }
 
     public void moveDown(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosX = RMR.currentMap.p0.posX;
             RMR.currentMap.p0.prevPosY = RMR.currentMap.p0.posY;
             RMR.currentMap.p0.posX += 32;
         }
     }
 
     public void moveRight(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosX = RMR.currentMap.p0.posX;
             RMR.currentMap.p0.prevPosY = RMR.currentMap.p0.posY;
             RMR.currentMap.p0.posY += 32;
         }
     }
 
     public void moveLeft(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosX = RMR.currentMap.p0.posX;
             RMR.currentMap.p0.prevPosY = RMR.currentMap.p0.posY;
             RMR.currentMap.p0.posY -= 32;
         }
     }
 
     public void setPlayer(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             if (RMR.currentMap.p0.posX != 16 && RMR.currentMap.p0.posY != 16)
             {
                 RMR.currentMap.p0.changePos(new int[]{16, 16});
 
                 RMR.currentMap.suspendTile = new Point(0, 0);
                 RMR.currentMap.state = Map.MapState.Game;
             }
         }
     }
 
     public void rotateUp(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosX--;
         }
     }
 
     public void rotateDown(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosX++;
         }
     }
 
     public void rotateLeft(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosY--;
         }
     }
 
     public void rotateRight(View view)
     {
         if (RMR.state == RMR.GameState.ServerIngame || RMR.state == RMR.GameState.ClientIngame || RMR.state == RMR.GameState.SingleplayerIngame)
         {
             RMR.currentMap.p0.prevPosY++;
         }
     }
 }
