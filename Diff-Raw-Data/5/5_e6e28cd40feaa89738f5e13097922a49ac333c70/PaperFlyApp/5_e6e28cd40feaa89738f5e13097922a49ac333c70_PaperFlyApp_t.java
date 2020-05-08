 package de.fhb.mi.paperfly;
 
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.app.Application;
 import android.app.Service;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.IBinder;
 import android.util.Log;
 
 import org.apache.http.client.CookieStore;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import de.fhb.mi.paperfly.dto.AccountDTO;
 import de.fhb.mi.paperfly.dto.RoomDTO;
 import de.fhb.mi.paperfly.service.BackgroundLocationService;
 import de.fhb.mi.paperfly.service.ChatService;
 import de.fhb.mi.paperfly.service.RestConsumerSingleton;
 import lombok.Getter;
 import lombok.Setter;
 
 /**
  * The application for PaperFly.
  *
  * @author Christoph Ott
  * @see android.app.Application
  */
 @Getter
 @Setter
 public class PaperFlyApp extends Application {
 
     private static final String TAG = PaperFlyApp.class.getSimpleName();
     private final Object lock = new Object();
    private String currentVisibleChatRoom = "";
    private RoomDTO actualRoom = null;
    private AccountDTO account;
    private List<AccountDTO> usersInRoom = new ArrayList<AccountDTO>();
     private CookieStore cookieStore = null;
     private ChatService chatService;
     private boolean boundChatService;
 
     private ServiceConnection connectionChatService = new ServiceConnection() {
 
         @Override
         public void onServiceConnected(ComponentName className, IBinder service) {
             // We've bound to LocalService, cast the IBinder and get LocalService instance
             ChatService.ChatServiceBinder binder = (ChatService.ChatServiceBinder) service;
             chatService = binder.getServiceInstance();
             boundChatService = true;
             Log.d(TAG, "chatService connected ...");
         }
 
         @Override
         public void onServiceDisconnected(ComponentName arg0) {
             boundChatService = false;
         }
     };
 
     /**
      * Builds a new HttpClient with the same CookieStore than the previous one.
      * This allows to follow the http session, without keeping in memory the
      * full DefaultHttpClient.
      */
     public HttpClient getHttpClient() {
         final DefaultHttpClient httpClient = new DefaultHttpClient();
         synchronized (lock) {
             if (cookieStore == null) {
                 cookieStore = httpClient.getCookieStore();
             } else {
                 httpClient.setCookieStore(cookieStore);
             }
         }
         return httpClient;
     }
 
     /**
      * Checks if the given Service is running.
      *
      * @param serviceToCheck the service to check
      *
      * @return true if the service is running, false if not
      */
     public boolean isMyServiceRunning(Class<? extends Service> serviceToCheck) {
         ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
             if (serviceToCheck.getName().equals(service.service.getClassName())) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public void onCreate() {
         super.onCreate();
 
         if (BackgroundLocationService.servicesAvailable(this)) {
             startService(new Intent(this, BackgroundLocationService.class));
         }
         startService(new Intent(this, ChatService.class));
         bindService(new Intent(this, ChatService.class), connectionChatService, Context.BIND_AUTO_CREATE);
         RestConsumerSingleton.getInstance().init(this);
     }
 
     public void disconnectChatService() {
         chatService.disconnectAfterTimeout();
     }
 
     public void unbindChatService() {
         chatService.stopTimers();
     }
 }
