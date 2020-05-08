 package com.example.modernhome;
 
 import java.util.ArrayList;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.concurrent.ExecutionException;
 
 import org.apache.http.HttpResponse;
 
 import android.content.Context;
 import android.content.Intent;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.os.Build;
 import android.speech.RecognizerIntent;
 import android.speech.SpeechRecognizer;
 import android.speech.tts.TextToSpeech;
 import android.util.Log;
import android.view.View;
 import android.view.WindowManager;
 
 public class Controller implements Observer {
 
 	private SpeechRecognizer _sr;
 	private ObservableRecognitionListener _speechListener;
 	private Intent _speechRecognitionIntent;
 	private MainActivity _mainView;
 	public AudioManager _audioManager;
 	public boolean _buzzWordRecognized;
 	private String _commandHttp;
 	public SoundPool _soundPool;
 	private StringMatcher _matchResults;
 	private TTS _tts;
 	public int _sound;
 	public static final int CHECK_TTS_AVAILABILITY = 4711;
 
 	public Controller(MainActivity View) {
 		if (_mainView == null) {
 			_mainView = View;
 			init();
 		}
 	}
 
 	private void say(String text) {
 		_tts.speak(text);
 	}
 
 	public void createTTS() {
 		_tts = new TTS(_mainView);
 	}
 
 	private void init() {
 		_soundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
 		_sound = _soundPool.load(_mainView, R.raw.ding, 1);
 		_buzzWordRecognized = false;
 		_speechListener = new ObservableRecognitionListener();
 		_speechListener.addObserver(this);
 		_matchResults = new StringMatcher();
 		_audioManager = (AudioManager) _mainView
 				.getSystemService(Context.AUDIO_SERVICE);
 
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		_mainView.startActivityForResult(checkIntent, CHECK_TTS_AVAILABILITY);
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
 			// turn off beep soundController
 			_audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
 		}
 		_mainView.getWindow().setFlags(
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
 				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
 
 	}
 
 	public void sendHttpRequest() {
 
 		int status = 0;
 		AsyncHttpCommunication communication = new AsyncHttpCommunication();
 		try {
 			HttpResponse response = communication.execute(_commandHttp).get();
 			status = response.getStatusLine().getStatusCode();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Log.d("HttpCode", String.valueOf(status));
 		while (!_speechListener.hasSpeechEnded()) {
 			try {
 				Thread.sleep(100, 0);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		_sr.startListening(_speechRecognitionIntent);
 	}
 
 	private void matchStrings(ArrayList<String> matches) {
 
 		if ((matches.contains("okay Zuhause")
 				|| matches.contains("okay Zuhause")
 				|| matches.contains("okay zuhause an")
 				|| matches.contains("okay zu hause") || matches
 					.contains("okay zu Hause")) && _buzzWordRecognized == false) {
 			_mainView.buzzWordRecognized();
 			while (!_speechListener.hasSpeechEnded()) {
 				try {
 					Thread.sleep(100, 0);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			_sr.startListening(_speechRecognitionIntent);
 
 		} else if (_buzzWordRecognized) {
 			String[] results = _matchResults.getCommand(matches);
 			if (results != null) {
 				if (results[1] != null) {
 					_mainView.executeText.setText(results[1]);
 					say(results[1]);
 				}
 				if (results[0] != null) {
 					_commandHttp = results[0];
 					_mainView.commandRecognized();
 				}
 			} else {
 				while (!_speechListener.hasSpeechEnded()) {
 					try {
 						Thread.sleep(100, 0);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				_sr.startListening(_speechRecognitionIntent);
 			}
 		}
 		else
 		{
 			while (!_speechListener.hasSpeechEnded()) {
 				try {
 					Thread.sleep(100, 0);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			_sr.startListening(_speechRecognitionIntent);
 		}
 	}
 
 	public void onStop() {
 		_sr.destroy();
 	}
 
 	public void onStart() {
 		if (SpeechRecognizer.isRecognitionAvailable(_mainView
 				.getApplicationContext())) {
 			_sr = SpeechRecognizer.createSpeechRecognizer(_mainView
 					.getApplicationContext());
 			_sr.setRecognitionListener(_speechListener);
 			_speechRecognitionIntent = new Intent(
 					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
 			_speechRecognitionIntent.putExtra(
 					RecognizerIntent.EXTRA_LANGUAGE_MODEL,
 					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
 			_speechRecognitionIntent
 					.putExtra(
 							RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
 							10);
 			_speechRecognitionIntent.putExtra(
 					RecognizerIntent.EXTRA_CALLING_PACKAGE,
 					"com.example.modernhome");
 		}
 		_sr.startListening(_speechRecognitionIntent);
 	}
 
 	@Override
 	public void update(Observable observable, Object data) {
 		if (data != null) {
 			if (data instanceof ArrayList<?>) {
 				@SuppressWarnings("unchecked")
 				ArrayList<String> temp = (ArrayList<String>) data;
 				matchStrings(temp);
                _mainView.ErrorTextView.setVisibility(View.INVISIBLE);
 			} else if (data instanceof String) {
                _mainView.ErrorTextView.setVisibility(View.VISIBLE);
 				String errorMessage = (String) data;
 				Log.d("ERROR", errorMessage);
 				// _buzzWordRecognized = false;
 				_mainView.ErrorTextView.setText(errorMessage);
 				if (errorMessage == "RecognitionService busy.") {
 					_sr.stopListening();
 				}
 				// if (_speechListener.hasSpeechEnded())
 				while (!_speechListener.hasSpeechEnded()) {
 					try {
 						Thread.sleep(100, 0);
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 				_sr.startListening(_speechRecognitionIntent);
 			}
 		}
 
 	}
 
 }
