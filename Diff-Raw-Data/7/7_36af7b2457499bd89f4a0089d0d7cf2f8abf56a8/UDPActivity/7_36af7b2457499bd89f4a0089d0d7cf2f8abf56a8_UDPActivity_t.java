 package com.PsichiX.JustIDS;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.BroadcastReceiver;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class UDPActivity extends Activity {
 
 	BroadCastManager mgr = new BroadCastManager();
 	Thread thread;
 	Button button;
 	EditText text;
 	TextView textView;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_udp);
 		
 		button = (Button) findViewById(R.id.button1);
 		text = (EditText) findViewById(R.id.editText1);
 		textView = (TextView) findViewById(R.id.textView2);
 		button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				String s = text.getText().toString();
 				mgr.sendBroadcast(UDPActivity.this, s);
 			}
 			
 		});
 		
 		thread = new Thread() {
 			@Override
 			public void run() {
 				Log.i("INFO", "Starting listening for messages");
 				while (true) {
 					final String message = mgr.receiveBroadCast(UDPActivity.this);
 					Log.i("MSG", message);
 					UDPActivity.this.runOnUiThread(new Runnable() {
 						
 						@Override
 						public void run() {
 							textView.setText(message);							
 						}
 					});
 				}
 			}
 			
 		};
 		
 		thread.start();
 	}
 
 }
