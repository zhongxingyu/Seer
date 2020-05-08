 package com.dbstar.guodian;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.net.Socket;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 
 public class ReceiveThread extends Thread {
 	public static final String TAG = "ReceiveThread";
 
 	private Socket mSocket = null;
 	private BufferedReader mIn = null;
 	AtomicBoolean mExit;
 	Handler mClientHander;
 
 	public ReceiveThread(Socket socket, BufferedReader in, Handler handler) {
 
 		mExit = new AtomicBoolean();
 		mExit.set(false);
 
 		mSocket = socket;
 		mIn = in;
 
 		mClientHander = handler;
 	}
 
 	public void setExit() {
 		mExit.set(true);
 	}
 
 	public void run() {
 
 		while (!mExit.get()) {
 			Log.d(TAG, " receive thread run ============== !");
 
 			if (mSocket != null && mSocket.isConnected() && !mSocket.isClosed()) {
 				try {
 					String data = new String();
 					String temp = null;
 
 					Log.d(TAG, " === read start==== ");
 
 					do {
 						temp = mIn.readLine();

						if (temp == null || temp.isEmpty()) {
							break;
						}

 						Log.d(TAG,
 								" ===== read == " + temp + " size="
 										+ temp.length());
 
 						data += temp;
 
 					} while (true);
 
 					Log.d(TAG, " === read end ==== " + data);
 					Message msg = mClientHander
 							.obtainMessage(GDClient.MSG_RESPONSE);
 					msg.obj = data;
 					msg.sendToTarget();
 
 				} catch (IOException e) {
 					e.printStackTrace();
 					mSocket = null;
 					mExit.set(true);
 					Log.d(TAG, "Exit receive thread!");
 				}
 			}
 		}
 	}
 }
