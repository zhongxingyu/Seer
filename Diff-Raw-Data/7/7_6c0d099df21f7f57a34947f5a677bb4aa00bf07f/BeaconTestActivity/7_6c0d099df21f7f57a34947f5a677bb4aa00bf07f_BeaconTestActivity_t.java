 package net.antoniy.beacontest;
 
 import net.antoniy.beacon.Beacon;
 import net.antoniy.beacon.BeaconParams;
import net.antoniy.beacon.exception.BeaconException;
 import net.antoniy.beacon.impl.BeaconFactory;
 import android.app.Activity;
 import android.os.Bundle;
 
 public class BeaconTestActivity extends Activity {
	private Beacon beacon;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         BroadcastData data = new BroadcastData();
         data.setDeviceId("af69df6fdf589m");
         data.setTcpHost("192.168.1.1");
         data.setTcpPort("6754");
 
         BeaconParams beaconParams = BeaconFactory.createBeaconParams();
         beaconParams.setData(data);
         beaconParams.setDataClazz(BroadcastData.class);
         beaconParams.setSendInterval(1000);
         beaconParams.setUdpPort(10010);
         
         try {
 			beacon = BeaconFactory.createBeacon(this, beaconParams);
 		} catch (BeaconException e) {
 			e.printStackTrace();
 		}
     }
     
     @Override
     protected void onPause() {
     	beacon.stopBeacon();
     	
     	super.onPause();
     }
     
     @Override
     protected void onResume() {
     	try {
     		beacon.startBeacon();
 	    } catch (BeaconException e) {
 	    	e.printStackTrace();
 	    }
     	
     	super.onResume();
     }
     
 }
