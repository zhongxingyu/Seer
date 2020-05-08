 package it.quadrotorcommander;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.app.Service;
 import android.content.Intent;
 import android.os.Binder;
 import android.os.IBinder;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import lcm.lcm.*;
 import lcmtypes.*;
 
 public class LcmService extends Service implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener, LCMSubscriber{
 
 	private LCM lcm;
 	private String host;
 	private TextToSpeech mTts;
 	private final IBinder mBinder = new LocalBinder();
 	
 	public class LocalBinder extends Binder {
         LcmService getService() {
             // Return this instance of LocalService so clients can call public methods
             return LcmService.this;
         }
     }
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return mBinder;
 	}
 
 	@Override
 	public void onCreate() {
 		mTts = new TextToSpeech(this, this);
 		System.out.println("onCreate");
 	}
 
 	@Override
 	public void onDestroy() {
 		lcm.close();
 		System.out.println("LCM DESTROY");
 	//code to execute when the service is shutting down
 	}
 
 	@Override
 	public void onStart(Intent intent, int startid) {
 		System.out.println("LCM ONSTART");
 	//code to execute when the service is starting up
 		try {
			//host=intent.getStringExtra("EXTRA_ADDRESS");
			host = "10.0.1.87";
         	lcm = new LCM("tcpq://"+host+":7700");
         	lcm.subscribe("SPEAK", this);
             lcm.subscribe("CONTROL", this);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}	
 	}
 	
 	public void messageReceived(LCM lcm, String channel, LCMDataInputStream ins)
     {
 		System.out.println("Received message on channel " + channel);
 		if(channel.equals("CONTROL")){
 			Intent pippo = new Intent(getBaseContext(), ChoiceActivity.class);
 			pippo.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 			startActivity(pippo);
 		}
         if(channel.equals("SPEAK")){
         	TextToSpeechMsg msg;
 	        try {
 	        	msg = new TextToSpeechMsg(ins);
 	            System.out.println("speaking");
 	        	@SuppressWarnings("deprecation")
 				int result = mTts.setOnUtteranceCompletedListener(this);
 	        	HashMap<String, String> params = new HashMap<String, String>();
 	        	params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "theUtId");
 	        	System.out.println(msg);
 	        	mTts.speak(msg.text, TextToSpeech.QUEUE_FLUSH, params); 
 	        } catch(Exception ex){
 	        	System.out.println("Error decoding message: " + ex);
 	        }
 	    }
     }
 	        
 
 	public void onInit(int status) {
 		System.out.println("LCM INIT");
 		mTts.setLanguage(Locale.US);
 	}
 
 	public void onUtteranceCompleted(String utteranceId) {
 		System.out.println("done with speech");	
 	}
 
 	
 	public void publishMessage(String channel, String msg){
 		try {	
 			lcm.publish(channel, msg);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public void publishMessage(String channel, SpeechList msg){
 		lcm.publish(channel, msg);
 	}
 	
 }
