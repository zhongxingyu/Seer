 package com.itog_lab.android.sample.demokit;
 
 import com.itog_lab.android.accessory.OpenAccessory;
 import android.app.Activity;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 public class SimpleDemoKit extends Activity implements OnClickListener {
 	static final String TAG = "SimpleDemoKit";
 
 	private TextView inputLabel;
 	private TextView outputLabel;
 	private LinearLayout inputContainer;
 	private LinearLayout outputContainer;
 
 	private OpenAccessory openAccessory;
 	private InputController inputController;
 	private OutputController outputController;
 	private ADKCommandSender adkSender;
 	private ADKCommandReceiver adkReceiver;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		openAccessory = new OpenAccessory();
 		openAccessory.open(this);
		if (openAccessory.isConnected()) {
			showControls();
 			adkReceiver = new ADKCommandReceiver(openAccessory);
 			adkSender = new ADKCommandSender(openAccessory);
 			inputController = new InputController(this);
 			adkReceiver.setInputController(inputController);
 			outputController = new OutputController(this, adkSender);
 		} else {
 			hideControls();
 		}				
 	}
 
 	private void showControls() {
 		setContentView(R.layout.main);
 		inputLabel = (TextView) findViewById(R.id.inputLabel);
 		outputLabel = (TextView) findViewById(R.id.outputLabel);
 		inputContainer = (LinearLayout) findViewById(R.id.inputContainer);
 		outputContainer = (LinearLayout) findViewById(R.id.outputContainer);
 		inputLabel.setOnClickListener(this);
 		outputLabel.setOnClickListener(this);
 		
 		showTabContents(true);
 	}
 
 	private void hideControls() {
 		setContentView(R.layout.no_device);
 		if (adkReceiver != null) {
 			adkReceiver.removeInputController();
 		}
 		inputController = null;
 		outputController = null;		
 	}
 
 	private void showTabContents(Boolean showInput) {
 		if (showInput) {
 			inputContainer.setVisibility(View.VISIBLE);
 			inputLabel.setBackgroundColor(Color.DKGRAY);
 			outputContainer.setVisibility(View.GONE);
 			outputLabel.setBackgroundColor(Color.BLACK);
 		} else {
 			inputContainer.setVisibility(View.GONE);
 			inputLabel.setBackgroundColor(Color.BLACK);
 			outputContainer.setVisibility(View.VISIBLE);
 			outputLabel.setBackgroundColor(Color.DKGRAY);
 		}
 	}
 
 	public void onClick(View v) {
 		int vId = v.getId();
 		switch (vId) {
 		case R.id.inputLabel:
 			showTabContents(true);
 			break;
 		case R.id.outputLabel:
 			showTabContents(false);
 			break;
 		}
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		if (item.getTitle().equals("Simulate")) {
 			showControls();
 		} else if (item.getTitle().equals("Quit")) {
 			openAccessory.close();
 			finish();
 			System.exit(0);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		menu.add("Simulate");
 		menu.add("Quit");
 		return true;
 	}
 }
