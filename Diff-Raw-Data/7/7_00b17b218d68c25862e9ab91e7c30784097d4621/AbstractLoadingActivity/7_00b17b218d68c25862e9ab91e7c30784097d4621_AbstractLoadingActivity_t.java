 package uk.ac.cam.cl.passgori.app;
 
 import uk.ac.cam.cl.passgori.PasswordStoreException;
 import uk.ac.cam.cl.passgori.app.PasswordStoreService.PasswordStorageBinder;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.BroadcastReceiver;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.ServiceConnection;
 import android.os.AsyncTask;
 import android.os.IBinder;
 import android.util.Log;
 
 public abstract class AbstractLoadingActivity extends Activity {
   public ProgressDialog mLoadingDialog;
 
   protected class FailureNotification implements Runnable {
 
     private final String mMessage;
 
     public FailureNotification(String message) {
       mMessage = message;
     }
 
     public FailureNotification(Exception e) {
       mMessage = e.toString();
       e.printStackTrace();
     }
 
     @Override
     public void run() {
       Log.e(this.getClass().getCanonicalName(), mMessage);
       if (mLoadingDialog != null) {
         mLoadingDialog.dismiss();
       }
       displayError(mMessage);
     }
 
   }
 
   protected abstract void displayError(String errorMessage);
 
   protected boolean connected = false;
   protected PasswordStorageBinder binder;
   /** Defines callbacks for service binding, passed to bindService() */
   protected final ServiceConnection mConnection = new ServiceConnection() {
     IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
 
     BroadcastReceiver mReceiver = new ScreenReceiver();
 
     @Override
     public void onServiceConnected(ComponentName className, IBinder service) {
       binder = (PasswordStorageBinder) service;
       connected = true;
       if (mLoadingDialog != null) {
         mLoadingDialog.dismiss();
       }
       try {
         onConnected();
 
         registerReceiver(mReceiver, filter);
       } catch (PasswordStoreException e) {
         runOnUiThread(new FailureNotification(e));
       }
     }
 
     @Override
     public void onServiceDisconnected(ComponentName arg0) {
       binder = null;
       connected = false;
       unregisterReceiver(mReceiver);
       redirectToUnlock();
     }
    @Override
     public void finalize() throws Throwable {
       binder = null;
       connected = false;
       unregisterReceiver(mReceiver);
       mReceiver = null;
       filter = null;
     }
   };
 
   /**
    * Run on connection or when {@link #connect()} is run and already connected
    * 
    * @throws PasswordStoreException
    */
   protected abstract void onConnected() throws PasswordStoreException;
 
   /**
    * Connect to the {@link PaswordStoreService} if not already connected but if already connected
    * run {@link #onConnected()}
    */
   protected void connect() {
     Log.d(this.getClass().getCanonicalName(), "Running connect");
     if (!connected) {
       mLoadingDialog = ProgressDialog.show(this, "", "Connecting. Please wait...", true);
       new AsyncTask<Object, Object, Object>() {// don't want to do this on the UI thread
 
         @Override
         protected Object doInBackground(Object... arg0) {
           // Bind to PasswordStoreService
           Intent intent = new Intent(AbstractLoadingActivity.this, PasswordStoreService.class);
 
           if (!getApplicationContext().bindService(intent, mConnection, Context.BIND_AUTO_CREATE)) {
             runOnUiThread(new FailureNotification("Failed to create internal service"));
           }
           return null;
         }
       }.execute();
 
     } else {
       try {
         onConnected();
       } catch (PasswordStoreException e) {
         runOnUiThread(new FailureNotification(e));
       }
     }
   }
   
   protected void redirectToUnlock() {
     Intent list = new Intent(this, PassgoriUnlockActivity.class);
     startActivity(list);
 
     finish();
   }
   
   private class ScreenReceiver extends BroadcastReceiver {
     @Override
     public void onReceive(Context context, Intent intent) {
       if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
         redirectToUnlock();
       }
     }
 
   }
 }
