 package org.mg8.pushr.droid;
 
 import org.mg8.pushr.droid.svc.IUploadCallback;
 import org.mg8.pushr.droid.svc.IUploadService;
 import org.mg8.pushr.droid.svc.UploadService;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.RemoteException;
 import android.util.Log;
 import android.view.View;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * Communicates with the Upload Service to show the user the current progress
  * and status.
  *
  * <p>This is also where {@code Intent}s to send a single image go, so that the
  * user can watch the upload progress.
  *
  * @author Cliff L. Biffle
  */
 public class CheckUploadStatus extends Activity {
 
   private TextView status;
   private ProgressBar progressBar;
   private IUploadService uploadService;
   private Handler mainThread;
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.upload_status);
     status = (TextView) findViewById(R.id.upload_status);
     progressBar = (ProgressBar) findViewById(R.id.upload_progress);
     mainThread = new Handler();
     bindService(new Intent(IUploadService.class.getName()),
         uploadServiceConnection, BIND_AUTO_CREATE);
     
     if (!AuthUtil.updateAuthIfNeeded(this)) {
       processSendIntent();
     }    
   }
   
   private void processSendIntent() {
     // If we've received a send intent, forward it to the service.
     Intent intent = getIntent();
     if (Intent.ACTION_SEND.equals(intent.getAction())) {
       startService(new Intent(this, UploadService.class)
           .putExtra(Intent.EXTRA_STREAM, intent.getParcelableExtra(Intent.EXTRA_STREAM)));
     }
   }
   
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if (requestCode == AuthUtil.REQUEST_CODE) {
       if (resultCode == RESULT_OK) {
         processSendIntent();
       } else {
         finish();
       }
     }
   }
 
   @Override
   protected void onDestroy() {
     super.onDestroy();
     unbindService(uploadServiceConnection);
   } 
  
   private final IUploadCallback uploadCallback = new IUploadCallback.Stub() {
     @Override public void statusUpdate(String file, final int position, final int end)
         throws RemoteException {
       final String msg;
      final int viz;
       if (file == null) {
         msg = getString(R.string.no_upload);
        viz = View.INVISIBLE;
       } else {
         msg = getString(R.string.uploading, file);
        viz = View.VISIBLE;
       }
       mainThread.post(new Runnable() {
         @Override public void run() {
           status.setText(msg);
           progressBar.setMax(end);
           progressBar.setProgress(position);
          progressBar.setVisibility(viz);
         }});
     }}; 
 
   private final ServiceConnection uploadServiceConnection = new ServiceConnection() {
     @Override public void onServiceConnected(ComponentName name, IBinder service) {
       Log.i("CheckUploadStatus", "Service bound.");
       uploadService = IUploadService.Stub.asInterface(service);
       try {
         uploadService.registerCallback(uploadCallback);
       } catch (RemoteException e) {
         // Expect a disconnect/reconnect sequence, ignore this.
         e.printStackTrace();
       }
     }
 
     @Override public void onServiceDisconnected(ComponentName name) {
       Log.i("CheckUploadStatus", "Service disconnected.");
       uploadService = null;
     }};
 
 }
