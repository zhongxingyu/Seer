 package com.android.trackz;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.nio.ByteBuffer;
 import java.util.UUID;
 
 import android.app.Activity;
 import android.bluetooth.BluetoothAdapter;
 import android.bluetooth.BluetoothServerSocket;
 import android.bluetooth.BluetoothSocket;
 import android.content.BroadcastReceiver;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Looper;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class TrackzActivity extends Activity implements OnTouchListener {
 
 	private static final UUID MY_UUID = UUID
 			.fromString("2f5ffe67-81f1-4e9b-8636-370b83607639");
 	private BluetoothServerSocket srvSocket;
 	BluetoothAdapter adapter;
 	BluetoothSocket socket;
 	BroadcastReceiver receiver;
 	String NAME = "Trackz";
 	float prev_x = -1;
 	float prev_y = -1;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		adapter = BluetoothAdapter.getDefaultAdapter();
 		if (adapter == null) {
 			Toast.makeText(this, "Bluetooth Not Available!", Toast.LENGTH_SHORT)
 					.show();
 			finish();
 			return;
 		} else {
 			Toast.makeText(this, "Checking bluetooth...", Toast.LENGTH_SHORT)
 					.show();
 			checkBluetoothEnabled();
 
 			Toast.makeText(this,
 					"Bluetooth available, waiting for connections.",
 					Toast.LENGTH_SHORT).show();
 
 			ServerThread acceptThread = new ServerThread(this);
 
 			Toast.makeText(this, srvSocket.toString(), Toast.LENGTH_SHORT)
 					.show();
 
 			acceptThread.start();
 
 			// Keep listening until exception occurs or a socket is returned
 			Toast.makeText(this, "Waiting for connections...",
 					Toast.LENGTH_SHORT).show();
 		}
 	}
 
 	public void checkBluetoothEnabled() {
 		Intent discoverableIntent = new Intent(
 				BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
 		discoverableIntent.putExtra(
 				BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
 		startActivity(discoverableIntent);
 	}
 
 	public void manageConnectedSocket(BluetoothSocket a_socket) {
 		// Enable touch
 		ImageView view = (ImageView) findViewById(R.id.imageView1);
 		view.setOnTouchListener(this);
 		
		Toast.makeText(this, "GOT A FUCKING CONNECTION!", Toast.LENGTH_SHORT)
 				.show();
 		socket = a_socket;
		try {
			socket.getOutputStream().write("Hell yes!".getBytes());
		} catch (IOException e) {
			// FUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUUU
			int i = 1;
		}

 	}
 
 	public boolean onTouch(View v, MotionEvent event) {
 		float x = event.getX();
 		float y = event.getY();
 		if (event.getAction() == MotionEvent.ACTION_DOWN) {
 			prev_x = x;
 			prev_y = y;
 		}
 		else if (event.getAction() == MotionEvent.ACTION_MOVE) {
 			float delta_x = x - prev_x;
 			float delta_y = y - prev_y;
 
 			sendMotion(1, delta_x, delta_y);
 
 			prev_x = x;
 			prev_y = y;
 		}
 		return true;
 	}
 
 	public void sendMotion(int type, float x, float y) {
 		try {
 			OutputStream os = socket.getOutputStream();
 			ByteBuffer bb = ByteBuffer.allocate(12);
 			bb.putInt(type);
 			bb.putInt((int) x);
 			bb.putInt((int) y);
 			os.write(bb.array());
 			os.flush();
 		} catch (IOException e) {
 		}
 	}
 
 	private class ServerThread extends Thread {
 		Activity parent;
 
 		public ServerThread(TrackzActivity parent) {
 			this.parent = parent;
 			createServerSocket();
 		}
 
 		public void createServerSocket() {
 			try {
				// MY_UUID is the app's UUID string, also used by the client
				// code
 				srvSocket = adapter.listenUsingRfcommWithServiceRecord(NAME,
 						MY_UUID);
 			} catch (IOException e) {
 				Toast.makeText(parent, "Failed to make server socket!",
 						Toast.LENGTH_SHORT).show();
 			}
 		}
 
 		public void run() {
 			Looper.prepare();
 			BluetoothSocket socket = null;
 			// Keep listening until exception occurs or a socket is returned
 			while (true) {
 				try {
 					socket = srvSocket.accept();
 				} catch (IOException e) {
 					break;
 				}
 				// If a connection was accepted
 				if (socket != null) {
 					// Do work to manage the connection (in a separate thread)
 					manageConnectedSocket(socket);
 					try {
 						srvSocket.close();
 					} catch (IOException e) {
 						int i = 1;
 					}
 					break;
 				}
 			}
 		}
 	}
 }
