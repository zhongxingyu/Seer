 package org.xmms2.server;
 
 import android.app.Notification;
 import android.app.Service;
 import android.content.Intent;
 import android.os.Build;
 import android.os.Environment;
 import android.os.IBinder;
 import android.util.Log;
 import org.xmms2.server.api11.NotificationFactoryLevel11;
 import org.xmms2.server.api8.NotificationFactoryLevel8;
 
 import java.io.*;
 
 
 /**
  * @author Eclipser
  */
 public class Server extends Service
 {
     public static final int ONGOING_NOTIFICATION = 1;
     private NotificationFactory notificationFactory;
     private Thread serverThread;
    private String pluginPath;
 
     private native void start();
     private native void quit();
 
     public IBinder onBind(Intent intent)
     {
         return null;
     }
 
     @Override
     public void onCreate()
     {
         super.onCreate();
 
         if (Build.VERSION.SDK_INT >= 11) {
             notificationFactory = new NotificationFactoryLevel11(getApplicationContext());
         } else { // min SDK version 8 in manifest
             notificationFactory = new NotificationFactoryLevel8(getApplicationContext());
         }
 
         File pluginsDirOut = new File(getFilesDir(), "/plugins/");
        pluginPath = pluginsDirOut.getAbsolutePath();
         Log.d("XMMS2", pluginsDirOut.getAbsolutePath());
         if (!pluginsDirOut.exists() && !pluginsDirOut.mkdirs()) {
             Log.e("XMMS2", ":E");
             throw new RuntimeException();
         }
 
         File files = new File(getConfigDir() + "/xmms2/plugins");
         for (File file : files.listFiles()) {
             copyFile(file, new File(pluginsDirOut, file.getName()));
         }
 
 
     }
 
     private static void copyFile(File input, File output)
     {
         Log.d("XMMS2", "Copying " + input.getAbsolutePath() + " to " + output.getAbsolutePath());
         try {
             InputStream in = new FileInputStream(input);
             OutputStream out = new FileOutputStream(output);
 
             byte[] buffer = new byte[1024];
             int read;
 
             while ((read = in.read(buffer)) != -1) {
                 out.write(buffer, 0, read);
             }
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public int onStartCommand(Intent intent, int flags, int startId)
     {
         Notification notification = notificationFactory.create();
         startForeground(ONGOING_NOTIFICATION, notification);
         serverThread = new Thread(new Runnable()
         {
             @Override
             public void run()
             {
                 start();
             }
         });
 
         serverThread.start();
         return START_STICKY;
     }
 
     @Override
     public void onDestroy()
     {
         stopForeground(true);
         quit();
         try {
             serverThread.join();
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     public static String getConfigDir()
     {
         return Environment.getExternalStorageDirectory().getAbsolutePath();
     }
 
    public String getPluginPath()
    {
        return pluginPath;
    }

     static {
         System.loadLibrary("glib-2.0");
         System.loadLibrary("gmodule-2.0");
         System.loadLibrary("gthread-2.0");
         System.loadLibrary("xmms2");
     }
 }
