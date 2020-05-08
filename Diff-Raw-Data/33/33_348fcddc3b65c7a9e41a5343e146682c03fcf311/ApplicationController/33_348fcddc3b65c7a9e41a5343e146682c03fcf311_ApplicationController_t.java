 package se.chalmers.pd.playlistmanager;
 
 import android.content.Context;
import android.util.Log;
 
 public class ApplicationController implements MqttWorker.Callback, DialogFactory.Callback {
 	
	private static final String TOPIC_PLAYLIST = "/playlist";
	private static final String TAG = "ApplicationController";
 	private MqttWorker mqttWorker;
 	private Context context;
 
 	public ApplicationController(Context context) {
 		mqttWorker = new MqttWorker(this);
 		mqttWorker.start();
 		this.context = context;
 	}
 	
 	public void reconnect() {
 		mqttWorker.interrupt();
 		mqttWorker = new MqttWorker(this);
 		mqttWorker.start();
 	}
 	
 	@Override
 	public void onConnected(boolean connected) {
 		if(connected) {
			mqttWorker.subscribe(TOPIC_PLAYLIST);
			Log.d(TAG, "Now subscribing to " + TOPIC_PLAYLIST);
 		} else {
			
 			((MainActivity) context).runOnUiThread(new Runnable() {
 				@Override
 				public void run() {
 					DialogFactory.buildConnectDialog(context, ApplicationController.this).show();
 				}
 			});
 		}
 	}
 	
 	@Override
 	public void onConnectDialogAnswer(boolean result) {
 		if(result) {
 			reconnect();
 		}
 	}
 
 	@Override
 	public void onMessage(String topic, String payload) {
 		
 	}
 
 }
