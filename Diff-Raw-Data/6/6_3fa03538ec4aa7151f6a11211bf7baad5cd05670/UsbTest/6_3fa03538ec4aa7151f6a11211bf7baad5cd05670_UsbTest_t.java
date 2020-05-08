 package greendrm.android.usbtest;
 
 import java.io.FileDescriptor;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import com.android.future.usb.UsbAccessory;
 import com.android.future.usb.UsbManager;
 
 import android.app.Activity;
 import android.app.PendingIntent;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.os.Bundle;
 import android.os.ParcelFileDescriptor;
 import android.util.Log;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class UsbTest extends Activity {
 	private static final boolean DEBUG = true;
 	private static final String TAG = "USB"; 
 	
	private static final String ACTION_USB_PERMISSION = "greendrm.android.usbtest.USBPERMISSION";
 	private UsbAccessory mAccessory = null;
 	private Button mBtSend = null;
 	
 	private ParcelFileDescriptor mFd = null;
 	private FileOutputStream mFout = null;
 	
 	private UsbManager mUsbManager;
 	private PendingIntent mPermissionIntent = null;
 	private boolean mPermissionRequestPending;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         if (DEBUG) Log.d(TAG, "onCreate()");
         
         mUsbManager = UsbManager.getInstance(this);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, 
        		new Intent(ACTION_USB_PERMISSION),0);
         
         IntentFilter i = new IntentFilter();
         //i.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
         i.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
         i.addAction(ACTION_USB_PERMISSION);
         registerReceiver(mUsbReceiver,i);
         
         if (getLastNonConfigurationInstance() != null) {
         	mAccessory = (UsbAccessory)getLastNonConfigurationInstance();
         	openAccessory(mAccessory);
         }
         
         mBtSend = (Button)(findViewById(R.id.btSebd));
         mBtSend.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String s = ((EditText)findViewById(R.id.editText1)).getText().toString();
 				queueWrite(s);
 			}
 		});
     }
     
     @Override
     public Object onRetainNonConfigurationInstance() {
     	if (DEBUG) Log.d(TAG, "onRetainNonConfigurationInstance()");
     	
     	if (mAccessory != null) {
     		return mAccessory;
     	}
     	else {
     		return super.onRetainNonConfigurationInstance();
     	}
     }
     
     @Override
     public void onResume() {
     	if (DEBUG) Log.d(TAG, "onResume()");
     	
     	super.onResume();
     	
     	if (mFout != null) {
     		mBtSend.setEnabled(true);
     		return;
     	}
     	
     	UsbAccessory[] accessories = mUsbManager.getAccessoryList();
     	UsbAccessory accessory = (accessories == null? null : accessories[0]);
     	
     	if (accessory != null) {
     		if (mUsbManager.hasPermission(accessory)) {
     			openAccessory(accessory);
     			mBtSend.setEnabled(true);
     		}
     		else {
     			synchronized(mUsbReceiver) {
     				if (!mPermissionRequestPending) {
     					mUsbManager.requestPermission(accessory, mPermissionIntent);
     					mPermissionRequestPending = true;
     				}
     			}
     		}
     	}
     	else {
     		Log.d(TAG, "mAccessory is null");
     	}
     	
     }
     
     @Override
     public void onPause() {
     	if (DEBUG) Log.d(TAG, "onPause()");
     	
     	super.onPause();
     	closeAccessory();
     	mBtSend.setEnabled(false);
     }
     
     @Override
     protected void onDestroy() {
     	if (DEBUG) Log.d(TAG, "onDestroy()");
     	unregisterReceiver(mUsbReceiver);
     	super.onDestroy();
     }
     
     private void openAccessory(UsbAccessory accessory) {
     	mFd = mUsbManager.openAccessory(accessory);
     	if (mFd != null) {
     		mAccessory = accessory;
     		FileDescriptor fd = mFd.getFileDescriptor();
     		mFout = new FileOutputStream(fd);
     	}
     	else {
     		Log.d(TAG, "accessory open fail");
     	}
     }
     
     private void closeAccessory() {
     	try {
     		if (mFd != null) {
     			mFout.close();
     			mFd.close();
     		}
     	}
     	catch (IOException e) {
     	}
     	finally {
     		mFd = null;
     		mFout = null;
     		mAccessory = null;
     	}
     }
     
     public void queueWrite(final String data){
     	if(mAccessory == null){
     		return;
     	}
     	new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					Log.d(TAG, "Writing length "+data.length());
 					mFout.write(new byte[]{(byte)data.length()});
 					Log.d(TAG, "Writing data: "+data);
 					mFout.write(data.getBytes());
 					Log.d(TAG,"Done writing");
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}).start();
     }
     
 	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
 		@Override
 		public void onReceive(Context context, Intent intent) {
 			String action = intent.getAction();
 			if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
 				UsbAccessory accessory = UsbManager.getAccessory(intent);
 				if (accessory != null && accessory.equals(mAccessory)) {
 					if (DEBUG) Log.d(TAG,"Dettached!");
 					closeAccessory();
 					mBtSend.setEnabled(false);
 				}
 			} else if(ACTION_USB_PERMISSION.equals(action)){
 				l("permission answered");
 				synchronized(this) {
 					UsbAccessory accessory = UsbManager.getAccessory(intent);
 					if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)){
 						openAccessory(accessory);
 						mBtSend.setEnabled(true);
 					}
 					else {
 						Log.d(TAG, "permission denied");
 					}
 					mPermissionRequestPending = false;
 				}
 			}
 			else {
 				Log.d(TAG, "Unknown action: " + action);
 			}
 		}
 	};
 	
 	private void l(String l){
 		if (DEBUG) Log.d(TAG, l);
 	}
 }
