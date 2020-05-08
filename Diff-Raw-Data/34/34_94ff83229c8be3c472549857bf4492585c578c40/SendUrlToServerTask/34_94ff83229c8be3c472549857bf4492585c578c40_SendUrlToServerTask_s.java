 package net.juhonkoti.sharetobrowser;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.AsyncTask;
 import android.util.Log;
 
 public class SendUrlToServerTask extends AsyncTask<String, Void, String> {
 	private Context context;
 	public SendUrlToServerTask(Context context) {
 		this.context = context;
 	}
 	
 	protected void onPostExecute(String result) {
 		Log.d("SendUrlToServerTask", "url done. result: " + result);
 		Intent intent = new Intent(context, SendDoneActivity.class);   
		context.startActivity(intent); 
 	}
 
 	@Override
 	protected String doInBackground(String... params) {
 		String urlString = (String) params[0];
 		String target = (String) params[1];
 		Log.d("SendUrlToServerTask", "Sending url:" + urlString + " to " + target);
 
 		try {
 			String query = "http://juhonkoti.net/sharetobrowser/send.php?url=" + URLEncoder.encode(urlString, "UTF-8") + "&target=" + URLEncoder.encode(target, "UTF-8");
 			Log.d("SendUrlToServerTask", "query: " + query);
 			URL url = new URL(query);
 			URLConnection uc = url.openConnection();
 			
 		    InputStreamReader in = new InputStreamReader((InputStream) uc.getInputStream());
 		    BufferedReader buff = new BufferedReader(in);
 		    String response = "";
 		    String line;
 		    do {
 		      line = buff.readLine();
 		      if (line != null) {
 		    	  Log.v("SendUrlToServerTask", "line: " + line);
 		    	  response += line;		    	  
 		      } else {
 		    	  Log.v("SendUrlToServerTask", "line was null");
 		      }
 		      
 		    } while (line != null);
 		    in.close();
 		    
 			Log.d("SendUrlToServerTask", "response: " + response);
 			Log.d("SendUrlToServerTask", "status2: " + uc.toString());
 			return response;
 		} catch (Exception e) {
 			Log.e("SendUrlToServerTask", "Exception: " + e.toString());
 			return null;
 		}
 		
 	}
 
 }
