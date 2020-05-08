 package se.chalmers.pd.playlistmanager;
 
 import android.content.Context;
 
 public class ApplicationController implements MqttWorker.Callback, DialogFactory.Callback {
 	
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
