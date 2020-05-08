 package cc.gtank.bt;
 
 import android.annotation.TargetApi;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothDevice;
 import android.bluetooth.BluetoothHeadset;
 import android.bluetooth.BluetoothProfile;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Build;
 
 @TargetApi(Build.VERSION_CODES.HONEYCOMB)
 public class Honeycomb extends BroadcastReceiver implements BluetoothProfile.ServiceListener, Bluetooth {
 
     public static enum VOICE { DISCONNECTED, CONNECTING, CONNECTED };
     
     private VOICE state = VOICE.DISCONNECTED;
     private BluetoothHeadset bluetoothHeadset = null;
     private Context appContext = null;
     
     @Override
     public void onServiceConnected(int profile, BluetoothProfile proxy) {
         if(profile == BluetoothProfile.HEADSET) {
             bluetoothHeadset = (BluetoothHeadset)proxy;
         }
     }
 
     @Override
     public void onServiceDisconnected(int profile) {
         if(profile == BluetoothProfile.HEADSET) {
             bluetoothHeadset = null;
         }
     }
 
     @Override
     public void onReceive(Context context, Intent intent) {
         if(intent.getAction().equals(BluetoothHeadset.ACTION_AUDIO_STATE_CHANGED)) {
             Object state = intent.getExtras().get(BluetoothHeadset.EXTRA_STATE);
             if(state.equals(BluetoothHeadset.STATE_AUDIO_CONNECTING)) {
                 state = VOICE.CONNECTING;
             } else if(state.equals(BluetoothHeadset.STATE_AUDIO_CONNECTED)) {
                 state = VOICE.CONNECTED;
                 Intent btState = new Intent(BLUETOOTH_STATE);
                 btState.putExtra("bluetooth_connected", true);
                 context.sendBroadcast(btState);
             } else {
                 state = VOICE.DISCONNECTED;
                 Intent btState = new Intent(BLUETOOTH_STATE);
                 btState.putExtra("bluetooth_connected", false);
                 context.sendBroadcast(btState);
             }
         }
     }
     
     public void setContext(Context context) {
         appContext = context;
     }
     
     public boolean obtainProxy() {
         if(appContext != null) {
             BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
             return btAdapter.getProfileProxy(appContext, this, BluetoothProfile.HEADSET);
         } else {
             return false;
         }
     }
     
     public void releaseProxy() throws Exception {
         if(appContext != null) {
             BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
             btAdapter.closeProfileProxy(BluetoothProfile.HEADSET, bluetoothHeadset);
         }
     }
 
     public void startVoiceRecognition() {
         BluetoothDevice btDevice = bluetoothHeadset.getConnectedDevices().get(0);
         bluetoothHeadset.stopVoiceRecognition(btDevice);
         bluetoothHeadset.startVoiceRecognition(btDevice);
     }
     
     public void stopVoiceRecognition() {
    	if (bluetoothHeadset.getConnectedDevices().size() > 0) {
    		BluetoothDevice btDevice = bluetoothHeadset.getConnectedDevices().get(0);
            bluetoothHeadset.stopVoiceRecognition(btDevice);
    	} 
     }
 
     public boolean isAvailable() {
         return bluetoothHeadset != null && bluetoothHeadset.getConnectedDevices().size() > 0;
     }
     
     public VOICE getVoiceState() {
         return state;
     }
 
 }
