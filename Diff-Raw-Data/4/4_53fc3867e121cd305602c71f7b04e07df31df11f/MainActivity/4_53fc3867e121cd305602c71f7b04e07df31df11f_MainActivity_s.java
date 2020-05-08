 package tw.iccl.ipps;
 
 import android.app.NotificationManager;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 import tw.iccl.option.Setting;
 import tw.iccl.service.BackgroundService;
 import tw.iccl.view.Device;
 import tw.iccl.view.Sensor;
 
 import static tw.iccl.service.BackgroundService.haveBackgroundService;
 
 public class MainActivity extends SherlockActivity {
     public final static boolean D = true;
     public final static String TAG = "MainActivity";
 
     private Sensor mSensor = null;
     private Device mDevice = null;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
     @Override
     protected void onStart() {
         super.onStart();
         onReceiver();
 
         cancelNotificationAll();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
 
        mSensor = new Sensor(this);
        mDevice = new Device(this);
     }
 
     @Override
     protected void onDestroy() {
         super.onDestroy();
 
         if(mSensor != null) mSensor.DisableReceiver();
         if(mDevice != null) mDevice.DisableReceiver();
     }
 
     private void cancelNotificationAll() {
         ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
     }
 
     private void onReceiver() {
         if(haveBackgroundService) {
             if(D) Log.e(TAG, "enalbe BGService");
             Intent mIntent = new Intent(this, BackgroundService.class);
             startService(mIntent);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         MenuInflater inflater = getSupportMenuInflater();
         inflater.inflate(R.menu.main_menu, menu);
         return super.onCreateOptionsMenu(menu);
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch(item.getItemId()) {
             case R.id.settings:
                 startActivity(new Intent().setClass(this , Setting.class));
                 break;
         }
         return super.onOptionsItemSelected(item);
     }
 }
