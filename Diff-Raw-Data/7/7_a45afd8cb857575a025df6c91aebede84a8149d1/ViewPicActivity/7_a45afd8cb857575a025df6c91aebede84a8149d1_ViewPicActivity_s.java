 package com.example.tensioncamapp.activity;
 
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import com.example.tensioncamapp.activity.R;
 import com.example.tensioncamapp.utils.Displayer;
 import com.example.tensioncamapp.utils.FileHandler;
 
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 
 /** 
  * @author Lisa Rythn Larsson, Fredrik Johansson, Martin Falk Danauskis
  * @copyright Lisa Rythn Larsson, Fredrik Johansson, Max Dubois, Martin Falk Danauskis
  * @param responeanswer: result from the analyze 
  * 
  * Copyright 2013 Fredrik Johansson, Lisa Rythn Larsson, Martin Falk Danauskis, Max Dubois
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *  */
 public class ViewPicActivity extends Activity implements View.OnClickListener {
 
 	private Button discard;
 	private Button analyze;
 	private ProgressBar progressBar;
 	
 	private static String result;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_view_pic);
 		ImageView jpgView = (ImageView) findViewById(R.id.imageView);
 		Displayer.displayImage(jpgView);
 		addListenerOnButton();
 	}
 
 
 	private void addListenerOnButton() {
 
 		this.discard = (Button) findViewById(R.id.discard_button);
 		this.analyze = (Button) findViewById(R.id.analyze_button);
 		 
 		this.discard.setOnClickListener(this);
 		this.analyze.setOnClickListener(this);
 		
 
 	}
 
 	/** Added switch-clauses to enable functionality for two buttons */
 	@Override
 	public void onClick(View v) {
 		switch (v.getId()) {
 		case R.id.discard_button:
 			this.discard.setEnabled(false);
 			Intent openCamActivity = new Intent(ViewPicActivity.this,
 					CameraActivity.class);
 			startActivity(openCamActivity);
 			break;
 		case R.id.analyze_button:
 			this.analyze.setEnabled(false);
			this.progressBar = (ProgressBar) findViewById(R.id.progressBar);
 			this.progressBar.setVisibility(0);
 			String path = FileHandler.pathToString();
 			try {
 				new SendTask().execute(path);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			break;
 		}
 	}
 
 	/** Inner class that connects to the web server and 
 	 * sends a picture as a MultipartEntity. 
 	 * The picture is analyzed and a result is returned. 
 	 * After the response has been received it directs to the next activity.
 	 *  */
 	private class SendTask extends AsyncTask<String, String, String> {
 		private static final String TAG = "SendTask";
 		private String answer;
 
 		
 		/** Method that works in the background which connects to the web server and sends a picture as a MultipartEntity to it. 
 		 * The picture is analyzed and a result is returned as a response.
 		 * @param responseanswer
 		 * @return String with number of pixels and blobs analyzed on the picture which was sent 
 		 *  */
 		@Override
 		protected String doInBackground(String... params) {
 				
 			String filePath = params[0];
 			String responseAnswer = "The server is down";
 
 
 			HttpClient httpclient = new DefaultHttpClient();
 			try {
 				HttpPost httppost = new HttpPost("http://192.168.43.79:8080/Analyse/upload");//Creates the POST request
 				FileBody pic = new FileBody(new File(filePath)); 
 				MultipartEntity requestEntity = new MultipartEntity();
 				requestEntity.addPart("file", pic);//Adds a file to the MultipartEntity
 
 				httppost.setEntity(requestEntity);//Adds the file to the POST request
 				System.out.println("executing request " + httppost.getRequestLine());
 				HttpResponse response = httpclient.execute(httppost);//Executes the POST request and stores the response
 
 				System.out.println("-------------------------------------------------------");
 				System.out.println(response.getStatusLine());
 
 				ByteArrayOutputStream outstream = new ByteArrayOutputStream();
 				response.getEntity().writeTo(outstream);
 				byte[] responseBody = outstream.toByteArray();
 
 				responseAnswer = new String(responseBody);
 				System.out.println(responseAnswer);
 
 			} catch (ClientProtocolException e) {
 				Log.e(TAG, "error in the HTTP protocol" +e.getMessage());
 			} catch (IOException e) {
 				Log.e(TAG, e.getMessage());
 			} finally {
 				try {
 					httpclient.getConnectionManager().shutdown();//Close connection to server
 				} catch (Exception ignore) {
 					Log.d(TAG, "server shutdown failed" +ignore.getMessage());
 				}
 			}
 			return responseAnswer;
 		}
 		/**
 		 * This method is executed after the "doInBackground"-method. It makes the answer available to the whole activity.
 		 * It also starts the next activity.
 		 */
 		@Override
 		protected void onPostExecute(String result) {
 			this.answer = result;
 			set(this.answer);
 			Intent openResultActivity = new Intent(ViewPicActivity.this, ResultActivity.class);
 			startActivity(openResultActivity);
 		}
 
 	}
 	
 	/** Method to set the result
 	 * Private so that the result can only be set here.
 	 */
 	static void set(String s){
 		result = s;
 	}
 	/** Method that returns the result.
 	 * Accessible from other classes. 
 	 */
 	public static String get(){
 		return result;
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		//activate buttons when activity is resumed
 		this.discard.setEnabled(true);
 		this.analyze.setEnabled(true);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.view_pic, menu);
 		return true;
 	}
 }
