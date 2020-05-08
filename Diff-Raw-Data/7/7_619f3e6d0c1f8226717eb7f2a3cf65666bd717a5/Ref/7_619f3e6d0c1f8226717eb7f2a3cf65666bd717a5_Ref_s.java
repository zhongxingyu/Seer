 package com.smartpark.background;
 
 import android.app.Activity;
 import android.widget.TextView;
 
import com.smartpark.bluetooth.BlueController;
 import com.smartpark.tcp.TCPController;
 
 public class Ref {
 
 	// ACOLLECTION OF REFERENCES THAT MOST OF THE CLASSES NEED TO OPERATE
 	// THIS IS INSPIERED BY THE ANDROID R.CLASS THAT HOUSES ALL REFERENCES
 	// FOR THE COMPONENTS ON THE DIFFERENT LAYOUTS.
 	// THE ALTERNATIVE WAS TO ALWAYS PASS REFERENCES TO OTHER CLASSES.
 
 //	// GLOBAL APPLICATION STATE FLAGS
 //	public static boolean bt_findIntentIsRegistered;
 //	public static boolean bt_stateIntentIsRegistered;
 //	public static boolean bt_connectionStateReceiverIsRegistered;
 //	public static boolean gpsReceiverIsRegistered;
 	
 	/*
 	 * This will hold a reference to the applicationContext. This is the main
 	 * context the application is running in. This gives great flexibility in
 	 * our program. However, with great power come great responsibility, since
 	 * everything invoked with this will not automatically be removed and will
 	 * lead to leaking if not manually removed. This is the task of the
 	 * BackOperationService.
 	 */
 	// public static Context applicationContext;
 	
 	// CONNECTION STATE INTEGERS
 	public final static int STATE_NOT_CONNECTED = -1;
 	public final static int STATE_DISCONNECTING = 0;
 	public final static int STATE_CONNECTING = 1;
 	public final static int STATE_CONNECTED = 2;
 	
 	// RESPONSES
 	public final static int RESULT_OK = 0;
 	public final static int RESULT_IO_EXCEPTION = -1;
 	public final static int RESULT_UNKNOWN_HOST_EXCEPTION = -2;
 	public final static int RESULT_EXCEPTION = -3;
 	
 	// TODO
 	// Global control-flags
 	public static int tcpState = STATE_NOT_CONNECTED;
 	public static int btState = STATE_NOT_CONNECTED;
 	
 	// Reference to the currently active activity
 	public static Activity activeActivity;
 	
 	// Global control-variable
 	public static boolean D = true;
 	
 	// TODO
 	// Objects used for Internet communication
 	public static TCPController tcpClient;
 	
 	// Reference to the background thread
 	public static BackgroundOperationThread bgThread;
 	public static boolean isBackgroundOperationThreadRunning = false;
 	
 	// TODO remove
 	public static TextView gps_text;
 	
 	// ==================================================
 	
 	// TODO remove
 	// getters and setters for Internet state
 	public static boolean tcpIsConnected() {
 		return tcpState == STATE_CONNECTED;
 	}
 	
 	public static void setTcpState(int state) {
 		tcpState = state;
 	}
 	
 	public static int getTcpState() {
 		return tcpState;
 	}
 	
 	// TODO remove
 	// getters and setters for bluetooth state
 	public static boolean btIsConnected() {
 		return btState == STATE_CONNECTED;
 	}
 	
 	public static void setbtState(int state) {
 		btState = state;
 	}
 	
 	public static int getbtState() {
 		return btState;
 	}
 }
