 package com.kg6.schlafdoedel.speechrecognition;
 
 import java.lang.ref.WeakReference;
 import java.util.ArrayList;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.Service;
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.CountDownTimer;
 import android.os.Handler;
 import android.os.IBinder;
 import android.os.Message;
 import android.os.Messenger;
 import android.os.RemoteException;
 import android.speech.RecognitionListener;
 import android.speech.RecognizerIntent;
 import android.speech.SpeechRecognizer;
 import android.util.Log;
 
 import com.kg6.schlafdoedel.Configuration;
 
 public class SpeechRecognition extends Service {
 	private final int COUNTDOWN_TIMER_OFFSET = 5000;
 	private final int COUNTDOWN_TIMER_DELAY = 5000;
 	
 	private final int ACTIVATION_TIMEOUT = 10000;
 	
 	private final Messenger MESSAGE_SERVER = new Messenger(new IncomingHandler(this));
 	
 	private AudioManager audioManager;
 	private SpeechRecognizer speechRecognizer;
 	private Intent speechRecognizerIntent;
 
 	private volatile boolean isListening;
 	private volatile boolean isCountDownEnabled;
 	private volatile boolean isServiceEnabled;
 	
 	private long lastActivationTime;
 
 	@Override
 	public void onCreate() {
 		super.onCreate();
 
 		this.audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
 
 		this.speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
 		this.speechRecognizer.setRecognitionListener(new SpeechRecognitionListener());
 		
 		this.speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 		this.speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 		this.speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
 		
 		this.isListening = false;
 		this.isCountDownEnabled = false;
 		this.isServiceEnabled = true;
 		
 		this.lastActivationTime = 0;
 
 		try {
 			MESSAGE_SERVER.send(Message.obtain(null, Configuration.SPEECH_RECOGNITION_START));
 		} catch (RemoteException e) {
 			Log.e("SpeechRecognition.java", "Unable to initialize speech recognition", e);
 		}
 	}
 	
 	@SuppressLint("DefaultLocale")
 	private boolean manageSpokenTerm(String term) {
 		term = term.toLowerCase(Locale.getDefault());
 		
 		if(term.contains(Configuration.SPEECH_RECOGNITION_ACTIVATION_PHRASE)) {
 			sendBroadcast(Configuration.SPEECH_RECOGNITION_COMMAND_ACTIVATED);
 			
 			this.lastActivationTime = System.currentTimeMillis();
 			
 			return true;
 		}
 		
 		if(this.lastActivationTime > System.currentTimeMillis() - ACTIVATION_TIMEOUT) {
 			String[] availableCommands = new String[] {
 				Configuration.SPEECH_RECOGNITION_COMMAND_WEATHER,
 				Configuration.SPEECH_RECOGNITION_COMMAND_NEWS,
 				Configuration.SPEECH_RECOGNITION_COMMAND_SLEEP,
 			};
 			
 			for(String command : availableCommands) {
 				if(term.contains(command)) {
 					sendBroadcast(command);
 					
 					this.lastActivationTime = 0;
 					
 					sendBroadcast(Configuration.SPEECH_RECOGNITION_COMMAND_DEACTIVATED);
 					
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	private void sendBroadcast(String command) {
 		try {
 			Intent intent = new Intent(Configuration.SPEECH_RECOGNITION_BROADCAST);
 			intent.putExtra("command", command);
 			
 			sendBroadcast(intent);
 		} catch (Exception e) {
 			Log.e("SpeechRecognition.java", "Unable to send broadcast of speech recognition command", e);
 		}
 	}
 	
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		
 		this.isServiceEnabled = false;
 
 		if(this.isCountDownEnabled) {
 			this.speechDeactivatedCountdown.cancel();
 		}
 		
 		if(this.speechRecognizer != null) {
 			this.speechRecognizer.cancel();
 			this.speechRecognizer.destroy();
 		}
 	}
 	
 	@Override
 	public IBinder onBind(Intent intent) {
 		return null;
 	}
 
 	private static class IncomingHandler extends Handler {
 		private WeakReference<SpeechRecognition> speechReconitionReference;
 
 		IncomingHandler(SpeechRecognition target) {
 			this.speechReconitionReference = new WeakReference<SpeechRecognition>(target);
 		}
 
 		@Override
 		public void handleMessage(Message msg) {
 			final SpeechRecognition speechRecognition = this.speechReconitionReference.get();
 			
 			if(!speechRecognition.isServiceEnabled) {
 				return;
 			}
 
 			switch (msg.what) {
 				case Configuration.SPEECH_RECOGNITION_START:
 					if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 						speechRecognition.audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
 					}
 					
 					if(!speechRecognition.isListening) {
 						speechRecognition.speechRecognizer.startListening(speechRecognition.speechRecognizerIntent);
 						
 						speechRecognition.isListening = true;
 						
 						Log.v("SpeechRecognition.java", "Start listening to voice recognition");
 					}
 	
 					break;
 				case Configuration.SPEECH_RECOGNITION_STOP:
 					speechRecognition.speechRecognizer.stopListening();
 					speechRecognition.speechRecognizer.cancel();
 					
 					speechRecognition.isListening = false;
 	
 					Log.v("SpeechRecognition.java", "Stop listening to voice recognition");
 	
 					break;
 				default:
 					Log.v("SpeechRecognition.java", String.format("Unknown message command %s", msg.what));
 					
 					break;
 			}
 		}
 	}
 
 	protected class SpeechRecognitionListener implements RecognitionListener {
 
 		@Override
 		public void onBeginningOfSpeech() {			
 			if(isServiceEnabled && isCountDownEnabled) {
 				isCountDownEnabled = false;
 				
 				speechDeactivatedCountdown.cancel();
 			}
 		}
 
 		@Override
 		public void onError(int error) {
 			if(!isServiceEnabled) {
 				return;
 			}
 			
 			if(isCountDownEnabled) {
 				isCountDownEnabled = false;
 				
 				speechDeactivatedCountdown.cancel();
 			}
 			
 			isListening = false;
 			
 			switch(error) {
 				case SpeechRecognizer.ERROR_AUDIO:
 					Log.v("SpeechRecognition.java", "Audio recording error.");
 					break;
 				case SpeechRecognizer.ERROR_CLIENT:
 					Log.v("SpeechRecognition.java", "Other client side errors.");
 					break;
 				case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
 					Log.v("SpeechRecognition.java", "Insufficient permissions.");
 					break;
 				case SpeechRecognizer.ERROR_NETWORK:
 					Log.v("SpeechRecognition.java", "Other network related errors.");
 					break;
 				case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
 					Log.v("SpeechRecognition.java", "Network operation timed out.");
 					break;
 				case SpeechRecognizer.ERROR_NO_MATCH:
 					Log.v("SpeechRecognition.java", "No recognition result matched.");
 					break;
 				case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
 					Log.v("SpeechRecognition.java", "RecognitionService busy.");
 					break;
 				case SpeechRecognizer.ERROR_SERVER:
 					Log.v("SpeechRecognition.java", "Server sends error status.");
 					break;
 				case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
 					Log.v("SpeechRecognition.java", "No speech input.");
 					break;
 			}
 			
 			try {
 				MESSAGE_SERVER.send(Message.obtain(null, Configuration.SPEECH_RECOGNITION_START));
 			} catch (RemoteException e) {

 			}
 		}
 
 		@Override
 		public void onReadyForSpeech(Bundle params) {
 			if(isServiceEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 				isCountDownEnabled = true;
 				
 				speechDeactivatedCountdown.start();
 				
 				audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
 			}
 		}
 
 		@Override
 		public void onResults(Bundle results) {
 			if(!isServiceEnabled) {
 				return;
 			}
 			
 			ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
 
 			for (int i = 0; i < matches.size(); i++) {
 				if(manageSpokenTerm(matches.get(i))) {
 					break;
 				}
 			}
 			
 			speechRecognizer.stopListening();
 			speechRecognizer.cancel();
 		}
 		
 		@Override
 		public void onEvent(int eventType, Bundle params) {
 			//Not needed here
 		}
 
 		@Override
 		public void onPartialResults(Bundle partialResults) {
 			//Not needed here
 		}
 		
 		@Override
 		public void onBufferReceived(byte[] buffer) {
 			//Not needed here
 		}
 
 		@Override
 		public void onEndOfSpeech() {
 			//Not needed here
 		}
 
 		@Override
 		public void onRmsChanged(float rmsdB) {
 			//Not needed here
 		}
 	}
 	
 	private CountDownTimer speechDeactivatedCountdown = new CountDownTimer(COUNTDOWN_TIMER_OFFSET, COUNTDOWN_TIMER_DELAY) {
 
 		@Override
 		public void onTick(long millisUntilFinished) {
 			//Not needed here
 		}
 
 		@Override
 		public void onFinish() {
 			if(!isServiceEnabled) {
 				return;
 			}
 			
 			if(lastActivationTime > 0 && lastActivationTime < System.currentTimeMillis() - ACTIVATION_TIMEOUT) {
 				lastActivationTime = 0;
 				
 				sendBroadcast(Configuration.SPEECH_RECOGNITION_COMMAND_DEACTIVATED);
 			}
 			
 			isCountDownEnabled = false;
 			
 			try {
 				MESSAGE_SERVER.send(Message.obtain(null, Configuration.SPEECH_RECOGNITION_STOP));
 				MESSAGE_SERVER.send(Message.obtain(null, Configuration.SPEECH_RECOGNITION_START));
 			} catch (RemoteException e) {
 
 			}
 		}
 	};
 }
