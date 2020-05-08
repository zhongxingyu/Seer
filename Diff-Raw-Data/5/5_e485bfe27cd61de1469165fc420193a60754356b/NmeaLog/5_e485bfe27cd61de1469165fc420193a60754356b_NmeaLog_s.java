 package com.limbocat.secondmate;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.speech.tts.TextToSpeech;
 import android.speech.tts.TextToSpeech.OnInitListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 import ocss.nmea.api.NMEAClient;
 import ocss.nmea.api.NMEAEvent;
 import ocss.nmea.parser.*;
 
 public class NmeaLog extends Activity implements OnInitListener, OnClickListener {
 
 	// Following two variables are used by the Text-To-Speech engine
 	private int TTS_DATA_CHECK_CODE = 0;
 	private TextToSpeech tts;
 	private Button speakButton;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.nmealog);
 		
 		speakButton = (Button) findViewById(R.id.speak_button);
 		speakButton.setOnClickListener(this);
 		
 		Intent checkIntent = new Intent();
 		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
 		startActivityForResult(checkIntent, TTS_DATA_CHECK_CODE);
 	}
 	
 	/** needed because of the OnInitListener interface */
 	@Override
 	public void onInit(int status) {
 		if (status == TextToSpeech.SUCCESS) {
 			Toast.makeText(this, "Text-To-Speech is initialized", Toast.LENGTH_SHORT).show();
 		} else if (status == TextToSpeech.ERROR) {
 			Toast.makeText(this, "Error occured while initializing Text-To-Speech engine", Toast.LENGTH_LONG).show();
 		}
 	}
 
 	/** needed because of the OnClickListener interface */
 	@Override
 	public void onClick(View v) {
     	switch (v.getId()) {
     	case R.id.exit_button:
     		finish();
     		break;
     	case R.id.speak_button:
     		EditText inputText =  (EditText) findViewById(R.id.edittext);
     		//String text = "Please say this to me";
     		String text = inputText.getText().toString();
     		if (text != null && text.length() > 0) {
     			Toast.makeText(this, "Saying: " + text, Toast.LENGTH_LONG).show();
     			tts.speak(text, TextToSpeech.QUEUE_ADD, null);
     		}
     		break;
     	}
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == TTS_DATA_CHECK_CODE ) {
 			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
 				// success, create the TTS instance
 				tts = new TextToSpeech(this, this);
 			} else {
 				// missing data, install it
 				Intent installIntent = new Intent();
 				installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
 				startActivity(installIntent);
 			}
 		}
 	}
 
 }
