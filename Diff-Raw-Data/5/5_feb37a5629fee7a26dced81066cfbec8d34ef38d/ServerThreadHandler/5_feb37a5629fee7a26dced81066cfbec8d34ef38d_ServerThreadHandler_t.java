 package com.tabbie.android.radar;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import com.tabbie.android.radar.http.ServerRequest;
 import com.tabbie.android.radar.http.ServerResponse;
 
 import android.os.Handler;
 import android.os.Looper;
 import android.os.Message;
 import android.util.Log;
 
 public class ServerThreadHandler extends Handler {
 	public static final String TAG = "ServerThreadHandler";
 	
 	private HttpURLConnection conn;
 	
 	public ServerThreadHandler(final Looper looper) {
 		super(looper);
 	}
 	
 	@Override
 	public void handleMessage(final Message msg) {
 		super.handleMessage(msg);
 		if(!(msg.obj instanceof ServerRequest)) {
 			Log.e(TAG, "Error: Message is not a ServerRequest");
 			return;
 		}
 		final ServerRequest req = (ServerRequest) msg.obj;
 		final Handler responseHandler = req.getResponseHandler();
 		try {
 			conn = (HttpURLConnection) new URL(req.getUrl()).openConnection();
 			conn.setRequestMethod(req.getReqMethod());
 			for (final String key : req.getHttpParams().keySet()) {
 			  conn.setRequestProperty(key, req.getHttpParams().get(key));
 			}
 			if (req.hasOutput()) {
 			  conn.setDoOutput(true);
 	          OutputStream stream = conn.getOutputStream();
 	          stream.write(req.getOutput().getBytes());
 	          stream.flush();
 		    } else {
 		      conn.connect();
 		    }
 	        if (conn.getResponseCode() < 200 || conn.getResponseCode() >= 300) {
 	          // TODO I don't know what this is for #Val
 	        }
 			final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 			final StringBuilder sb = new StringBuilder();
 			
 			String y = "";
 			while ((y = reader.readLine())!=null) {
 				sb.append(y);
 			}
 			if(responseHandler!=null) {
				final Message responseMessage = Message.obtain();
				responseMessage.obj = new ServerResponse(0, sb.toString(), req.getType());
				responseHandler.sendMessage(responseMessage);
 			} else {
 				Log.i(TAG, "No response handler available");
 				return;
 			}
 		} catch (final MalformedURLException e) {
 			// TODO
 		} catch (final IOException e) {
 			// TODO
 		}
 	}
 }
