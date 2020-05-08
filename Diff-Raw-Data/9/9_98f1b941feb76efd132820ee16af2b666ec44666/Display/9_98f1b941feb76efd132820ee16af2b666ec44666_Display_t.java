 package com.shreyaschand.MEDIC.Doctor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.Socket;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ScrollView;
 import android.widget.TextView;
 
 public class Display extends Activity implements OnClickListener {
 
 	private TextView output = null;
 	private ScrollView scroller = null;
 	private Socket socket = null;
 	private String user;
 
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.display);
 
 		findViewById(R.id.display_back_button).setOnClickListener(this);
 		output = ((TextView) findViewById(R.id.test_output));
 		scroller = (ScrollView) findViewById(R.id.display_scroller);
 		user = getIntent().getExtras().getString("com.shreyaschand.MEDIC.Doctor.user");
 	}
 
 	public void onStart() {
 		super.onStart();
 		new ConnectSocket().execute();
 	}
 
 	public void onClick(View arg0) {
 		finish();
 	}
 
 	private class ConnectSocket extends AsyncTask<Void, String, Boolean> {
 
 		protected Boolean doInBackground(Void... params) {
 			try {
 				publishProgress("Establishing connection...\n");
 				socket = new Socket("chands.dyndns-server.com", 1028);
 				publishProgress("Connected.\n");
 				return true;
 			} catch (IOException e) {return false;}
 		}
 
 		protected void onProgressUpdate(String... updates) {
 			output.append(updates[0]);
 			scroller.fullScroll(ScrollView.FOCUS_DOWN);
 		}
 
 		protected void onPostExecute(Boolean result) {
 			if (result) {
 				new SocketCommunicator().execute(socket);
 			} else {
 				output.append("Error connecting.");
 				scroller.fullScroll(ScrollView.FOCUS_DOWN);
 			}
 		}
 	}
 
 	private class SocketCommunicator extends AsyncTask<Socket, String, Boolean> {
 		protected Boolean doInBackground(Socket... socket) {
 			try {
				BufferedReader in = new BufferedReader(new InputStreamReader(socket[0].getInputStream()));
 				PrintWriter out = new PrintWriter(socket[0].getOutputStream());
 				out.println("$DOC$" + user);
 				out.flush();
//				out.close();
 				String message = in.readLine();
 				while (message != null) {
 					publishProgress(new String[] { message });
 					message = in.readLine();
 				}
			} catch (IOException e) {e.printStackTrace();return false;}
 			return true;
 		}
 
 		protected void onProgressUpdate(String... update) {
 			output.append("\n" + update[0]);
 			scroller.fullScroll(ScrollView.FOCUS_DOWN);
 		}
 		
 		protected void onPostExecute(Boolean result) {
 			if(result) {
 				output.append("\nConnection closed.");
 				scroller.fullScroll(ScrollView.FOCUS_DOWN);
 			}else {
 				output.append("\nConnection lost unexepectedly.");
 				scroller.fullScroll(ScrollView.FOCUS_DOWN);
 			}
 		}
 	}
 
 }
