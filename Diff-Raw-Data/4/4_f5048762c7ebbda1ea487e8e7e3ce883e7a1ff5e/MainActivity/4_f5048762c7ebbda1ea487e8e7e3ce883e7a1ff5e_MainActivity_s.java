 package com.example.bluetoothanalog;
 
import com.example.bluetoothserialtest.R;

 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.TextView;
 import android.app.Activity;
 import cc.arduino.btserial.BtSerial;
 
 public class MainActivity extends Activity implements OnClickListener {
 
 	public static final String LOGTAG = "BlueToothAnalog";
 	public static final String BLUETOOTH_MAC_ADDRESS = "00:06:66:42:1F:DF";
 	
 	public static final int DELIMITER = 10;  // Newline in ASCII
 	
 	BtSerial btserial;
 	
 	MyDrawingView myDrawingView;
 	Button connectButton;
 	Button readButton;
 	
 	StringBuilder sbuffer;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		connectButton = (Button) this.findViewById(R.id.connectButton);
 		connectButton.setOnClickListener(this);
 		
 		myDrawingView = (MyDrawingView) this.findViewById(R.id.myDrawingView);
 		
 		sbuffer = new StringBuilder();
 		btserial = new BtSerial(this);
 	}
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 	}
 	
 	@Override
 	protected void onPause()
 	{
 		super.onPause();
 		btserial.disconnect();
 	}
 
 	// Handlers let us interact with threads on the UI thread
 	Handler handler = new Handler() {
 		  @Override
 		  public void handleMessage(Message msg) {
 			int messageData = msg.getData().getInt("serialvalue");
 			myDrawingView.setYoverTime(messageData);
 		  }
 	};	
 	
 	public void btSerialEvent(BtSerial btserialObject) {
 		String serialValue = btserialObject.readStringUntil(DELIMITER);
 		
 		if (serialValue != null)
 		{
 			Log.v(LOGTAG,"Data: " + serialValue);
 
 			// Turn it into an int
 			try {
 				int intSerialValue = Integer.parseInt(serialValue.trim());
 
 				// Since btSerialEvent is happening in a separate thread, 
 				// we need to use a handler to send a message in order to interact with the UI thread
 				
 				Message msg = handler.obtainMessage();
 				Bundle bundle = new Bundle();
 				bundle.putInt("serialvalue", intSerialValue);
 				msg.setData(bundle);
 				handler.sendMessage(msg);
 			
 			} catch (NumberFormatException nfe) {
 				// Not a number
 				Log.v(LOGTAG,"" + serialValue + " is not a number");
 			}
 			
 		}
 	}
 
 	@Override
 	public void onClick(View clickedView) {
 		if (clickedView == connectButton) {
 			if (btserial.isConnected()) {
 				Log.v(LOGTAG, "Already Connected, Disconnecting");
 				btserial.disconnect();
 			}
 			
 			btserial.connect(BLUETOOTH_MAC_ADDRESS);
 			if (btserial.isConnected()) {
 				Log.v(LOGTAG,"Connected");
 			}
 			else {
 				Log.v(LOGTAG,"Not Connected");
 			}
 		}
 	}
 	
 	
 
 }
