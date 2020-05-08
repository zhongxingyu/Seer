 package com.kg6.schlafdoedel.speechrecognition;
 
 import java.util.HashMap;
 import java.util.Locale;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
 import android.util.Log;
 
 import com.kg6.schlafdoedel.event.EventScheduler;
 
 @SuppressWarnings("deprecation")
 public class TextToSpeechWrapper implements OnInitListener, OnUtteranceCompletedListener {
 	
 	public static void Speak(Context context, String requestKey, String text) {
 		new TextToSpeechWrapper(context, requestKey, text);
 	}
 	
 	private final String REQUEST_KEY;
 	private final String TEXT;
 	
 	private final float PREVIOUS_AUDIO_VOLUME;
 	
 	private TextToSpeech textToSpeech;
 	
 	private TextToSpeechWrapper(Context context, String requestKey, String text) {
 		REQUEST_KEY = requestKey;
 		TEXT = text;
 		
 		PREVIOUS_AUDIO_VOLUME = getMusicVolume();
 		
 		changeMusicVolume(0);
 		
 		this.textToSpeech = new TextToSpeech(context, this);
 		this.textToSpeech.setOnUtteranceCompletedListener(this);
 	}
 
 	@Override
 	public void onInit(int status) {
 		if (status == TextToSpeech.SUCCESS) {
			this.textToSpeech.setLanguage(Locale.UK);
			
			HashMap<String, String> ttsHash = new HashMap<String, String>();
	        ttsHash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_ALARM));
	        ttsHash.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, REQUEST_KEY);
 			
			this.textToSpeech.speak(TEXT, TextToSpeech.QUEUE_FLUSH, ttsHash);
 		} else {
 			Log.v("InformationRequest.java", "Unable to initialize text to speech");
 		}
 	}
 
 	@Override
 	public void onUtteranceCompleted(String utteranceId) {
 		changeMusicVolume(PREVIOUS_AUDIO_VOLUME);
 	}
 	
 	private float getMusicVolume() {
 		EventScheduler scheduler = EventScheduler.CreateInstance();
         
         if(scheduler != null) {
         	return scheduler.getEventAudioVolume();
         }
         
         return 0;
 	}
 	
 	private void changeMusicVolume(float volume) {
 		EventScheduler scheduler = EventScheduler.CreateInstance();
         
         if(scheduler != null) {
         	scheduler.setEventAudioVolume(volume);
         }
 	}
 }
