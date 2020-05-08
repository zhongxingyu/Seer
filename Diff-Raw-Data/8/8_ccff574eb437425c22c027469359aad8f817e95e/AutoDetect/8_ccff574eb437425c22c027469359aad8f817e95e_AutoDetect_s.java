 package com.group15.djhero;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.NetworkInterface;
 import java.net.Socket;
 import java.net.SocketAddress;
 import java.net.SocketException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.app.ActionBar;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.TextView;
 
 public class AutoDetect extends Activity implements OnItemClickListener {
 
 	Button button;
 	private ListView m_listview;
 	ListIpAddresses adapter;
 	TextView textView;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		MyApplication myApp = (MyApplication) getApplication();
 		// This call will result in better error messages if you
 		// try to do things in the wrong thread.
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_auto_detect);
 		m_listview = (ListView) findViewById(R.id.ip_list_view);
 		m_listview.setOnItemClickListener(this);
 		textView = (TextView) findViewById(R.id.connectedIPDisplay);
 		setTitle("Connect to DE2");
 		if (myApp.sock == null) {
 			textView.setText("Not Connected");
 
 			String connectTo = "192.168.0.113";
 			new SocketConnect().execute(connectTo);
 			myApp.availableDE2s.clear();
 			myApp.availableDE2s.add(connectTo);
 			this.adapter = new ListIpAddresses(this, myApp.availableDE2s);
 			myApp.connectedTo = myApp.availableDE2s.get(0);
 			TCPReadTimerTask tcp_task = new TCPReadTimerTask();
 			Timer tcp_timer = new Timer();
 			tcp_timer.schedule(tcp_task, 3000, 75);
 		} else {
 			adapter = new ListIpAddresses(AutoDetect.this, myApp.availableDE2s);
 			m_listview.setAdapter(adapter);
 			textView.setText("Connected to: " + myApp.connectedTo);
 		}
 		final ActionBar actionBar = getActionBar();
 		actionBar.setDisplayHomeAsUpEnabled(true);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.auto_detect, menu);
 		return true;
 	}
 
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 			case android.R.id.home:
 				super.onBackPressed();
 				return true;
 			default:
 				return super.onOptionsItemSelected(item);
 		}
 	}
 
 	// Called when the user closes a socket
 	public void closeSocket() {
 		MyApplication app = (MyApplication) getApplication();
 		Socket s = app.sock;
 		try {
 			s.getOutputStream().close();
 			s.close();
 			app.sock = null;
 			textView.setText("Not Connected");
 		} catch (IOException e) {
 			textView.setText("Error: Could not disconnect!");
 			e.printStackTrace();
 		}
 	}
 
 	public String getLocalIpAddress() {
 		try {
 			String Ip = null;
 			for (Enumeration<NetworkInterface> en = NetworkInterface
 			        .getNetworkInterfaces(); en.hasMoreElements();) {
 				NetworkInterface intf = en.nextElement();
 
 				for (Enumeration<InetAddress> enumIpAddr = intf
 				        .getInetAddresses(); enumIpAddr.hasMoreElements();) {
 					InetAddress inetAddress = enumIpAddr.nextElement();
 					if (!inetAddress.isLoopbackAddress()) {
 						Ip = inetAddress.getHostAddress().toString();
 					}
 				}
 			}
 			return Ip;
 		} catch (SocketException obj) {
 			Log.e("Error occurred during IP fetching: ", obj.toString());
 		}
 		return null;
 	}
 
 	// This is the Socket Connect asynchronous thread. Opening a socket
 	// has to be done in an Asynchronous thread in Android. Be sure you
 	// have done the Asynchronous Tread tutorial before trying to understand
 	// this code.
 
 	public class SocketConnect extends AsyncTask<String, Void, Socket> {
 
 		// The main parcel of work for this thread. Opens a socket
 		// to connect to the specified IP.
 
 		protected Socket doInBackground(String... url) {
 			Socket s = null;
 			String ip = url[0];
 			Integer port = 50002;
 
 			try {
 				s = new Socket(ip, port);
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return s;
 		}
 
 		// After executing the doInBackground method, this is
 		// automatically called, in the UI (main) thread to store
 		// the socket in this app's persistent storage
 
 		protected void onPostExecute(Socket s) {
 			MyApplication myApp = (MyApplication) AutoDetect.this
 			        .getApplication();
 			myApp.sock = s;
 			if (myApp.sock != null) {
 				textView.setText("Connected to: " + myApp.connectedTo);
 			} else {
 				textView.setText("Error: Could not connect");
 			}
 
 		}
 	}
 
 	// This is a timer Task. Be sure to work through the tutorials
 	// on Timer Tasks before trying to understand this code.
 
 	public class TCPReadTimerTask extends TimerTask {
 		public void run() {
 			MyApplication app = (MyApplication) AutoDetect.this
 			        .getApplication();
 			if (app.sock != null && app.sock.isConnected()
 			        && !app.sock.isClosed()) {
 
 				try {
 					InputStream in = app.sock.getInputStream();
 
 					// See if any bytes are available from the Middleman
 
 					int bytes_avail = in.available();
 					if (bytes_avail > 0) {
 
 						// If so, read them in and create a sring
 
 						byte buf[] = new byte[bytes_avail];
 						in.read(buf);
 
 						final String s = new String(buf, 0, bytes_avail,
 						        "US-ASCII");
 						
 						
 						MyApplication myApp = (MyApplication) AutoDetect.this
 						        .getApplication();
 
 						
 						
 						if(s.contains("djdj")){
 							myApp.djDoneLoad = true;
 						}
 						else{
 							myApp.listComplete = myApp.mainSongList.addSongs(s);
 							Log.i("DE2list", s);
 							SendMessage.sendMessage("a", myApp.sock);
 						}
 						// As explained in the tutorials, the GUI can not be
 						// updated in an asyncrhonous task. So, update the GUI
 						// using the UI thread.
 						
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	class FindDE2sOnNetwork extends AsyncTask<Void, Integer, List<String>> {
 
 		ProgressDialog progress;
 		MyApplication app = (MyApplication) AutoDetect.this.getApplication();
 
 		@Override
 		protected void onPreExecute() {
 			progress = new ProgressDialog(AutoDetect.this);
 			progress.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
 			progress.setMessage("Discovering DE2s");
 			progress.setIndeterminate(false);
 			progress.setMax(100);
 			progress.setCancelable(false);
 			progress.setProgressNumberFormat(null);
 			progress.show();
 		}
 
 		@Override
 		protected List<String> doInBackground(Void... arg0) {
 			int port = 50002;
 			List<String> result = new ArrayList<String>();
 			int count = 0;
 			String ip = getLocalIpAddress();
 			System.out.println(ip);
 			try {
 
 				for (int i = 0; i < 255; i++) {
 					// build the next IP address
 					String addr = ip.substring(0, ip.lastIndexOf('.') + 1)
 					        + (i);
 					System.out.println(addr);
 					InetAddress pingAddr = InetAddress.getByName(addr);
 					// System.out.println(ping(addr));
 					// 50ms Timeout for the "ping"
 					// System.out.println(ping(""));
 
 					publishProgress(i, count);
 					// if(pingHost(addr) == 0){
 					System.out.println("--------------Testing port " + port);
 					// Socket s = null;
 
 					SocketAddress socks = new InetSocketAddress(pingAddr, 50002);
 					Socket s = new Socket();
 					System.out.println("in count: " + count);
 					try {
 						System.out.println("in try");
 						// s = new Socket(pingAddr, 50002);
 						// s.connect(sock, 500);
 						s.connect(socks, 1000);
 						s.close();
 						result.add(pingAddr.getHostAddress());
 						count = count + 1;
 					} catch (IOException e) {
 					} catch (IllegalArgumentException e) {
 					} finally {
 
 						// System.out.println("in finally");
 						//
 						// if( s != null){
 						// try {
 						// s.close();
 						// } catch (IOException e) {
 						//
 						// }
 						// }
 					}
 
 					// }
 				}
 			} catch (UnknownHostException ex) {
 			}
 			publishProgress(100, count);
 			return result;
 		}
 
 		@Override
 		protected void onProgressUpdate(Integer... is) {
 			progress.setProgress((int) ((is[0].intValue() / 255.0) * 100));
 			if (is[1].intValue() > 0) {
 				progress.setMessage("Discovering DE2s...\nFound "
 				        + is[1].toString());
 			}
 		}
 
 		@Override
 		protected void onPostExecute(List<String> result) {
 			MyApplication myApp = (MyApplication) AutoDetect.this
 			        .getApplication();
 			progress.dismiss();
 			myApp.availableDE2s.clear();
 			myApp.availableDE2s.addAll(result);
 			adapter = new ListIpAddresses(AutoDetect.this, myApp.availableDE2s);
 			m_listview.setAdapter(adapter);
 			TCPReadTimerTask tcp_task = new TCPReadTimerTask();
 			Timer tcp_timer = new Timer();
 			tcp_timer.schedule(tcp_task, 3000, 500);
 		}
 	}
 
 	@Override
 	public void onItemClick(AdapterView<?> adapter, View arg1, int position,
 	        long arg3) {
 		MyApplication myApp = (MyApplication) AutoDetect.this.getApplication();
 		if (myApp.sock != null) {
 			if (myApp.connectedTo.equals(myApp.availableDE2s.get(position))) {
 				closeSocket();
 			} else {
 				closeSocket();
 				new SocketConnect().execute(myApp.availableDE2s.get(position));
 				this.adapter = new ListIpAddresses(this, myApp.availableDE2s);
 				myApp.connectedTo = myApp.availableDE2s.get(position);
 			}
 		} else {
 			new SocketConnect().execute(myApp.availableDE2s.get(position));
 			this.adapter = new ListIpAddresses(this, myApp.availableDE2s);
 			myApp.connectedTo = myApp.availableDE2s.get(position);
 		}
 
 	}
 
 	private static final String TAG = "Network.java";
 
 	public String pingError = null;
 
 	/**
 	 * Ping a host and return an int value of 0 or 1 or 2 0=success, 1=fail, 2=error Does not work
 	 * in Android emulator and also delay by '1' second if host not pingable In the Android emulator
 	 * only ping to 127.0.0.1 works
 	 * 
 	 * @param String
 	 *            host in dotted IP address format
 	 * @return
 	 * @throws IOException
 	 * @throws InterruptedException
 	 */
 	public int pingHost(String host) throws IOException, InterruptedException {
 		System.out.println("trying to ping");
 		Runtime runtime = Runtime.getRuntime();
 		Process proc = runtime.exec("ping -W 5000 -o " + host);
 		proc.waitFor();
 		int exit = proc.exitValue();
 		System.out.println(exit);
 		return exit;
 
 	}
 
 	public String ping(String host) throws IOException, InterruptedException {
 		StringBuffer echo = new StringBuffer();
 		Runtime runtime = Runtime.getRuntime();
 		Log.v(TAG, "About to ping using runtime.exec");
 		Process proc = runtime.exec("ping -W 300 -o " + host);
 		proc.waitFor();
 		int exit = proc.exitValue();
 		if (exit == 0) {
 			InputStreamReader reader = new InputStreamReader(
 			        proc.getInputStream());
 			BufferedReader buffer = new BufferedReader(reader);
 			String line = "";
 			while ((line = buffer.readLine()) != null) {
 				echo.append(line + "\n");
 			}
 			return getPingStats(echo.toString());
 		} else if (exit == 1) {
 			pingError = "failed, exit = 1";
 			return null;
 		} else {
 			pingError = "error, exit = 2";
 			return null;
 		}
 	}
 
 	public String getPingStats(String s) {
 		if (s.contains("0% packet loss")) {
 			int start = s.indexOf("/mdev = ");
 			int end = s.indexOf(" ms\n", start);
 			s = s.substring(start + 8, end);
 			String stats[] = s.split("/");
 			return stats[2];
 		} else if (s.contains("100% packet loss")) {
 			pingError = "100% packet loss";
 			return null;
 		} else if (s.contains("% packet loss")) {
 			pingError = "partial packet loss";
 			return null;
 		} else if (s.contains("unknown host")) {
 			pingError = "unknown host";
 			return null;
 		} else {
 			pingError = "unknown error in getPingStats";
 			return null;
 		}
 	}
 
 }
