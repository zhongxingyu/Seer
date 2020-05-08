 package srl.graffiti.client;
 
 /*******************************************************************************
  *  Revision History:<br>
  *  George R. Lucchese - File created
  *
  *  <p>
  *  <pre>
  *  This work is released under the BSD License:
  *  (C) 2012 Sketch Recognition Lab, Texas A&M University (hereafter SRL @ TAMU)
  *  All rights reserved.
  *
  *  Redistribution and use in source and binary forms, with or without
  *  modification, are permitted provided that the following conditions are met:
  *      * Redistributions of source code must retain the above copyright
  *        notice, this list of conditions and the following disclaimer.
  *      * Redistributions in binary form must reproduce the above copyright
  *        notice, this list of conditions and the following disclaimer in the
  *        documentation and/or other materials provided with the distribution.
  *      * Neither the name of the Sketch Recognition Lab, Texas A&M University 
  *        nor the names of its contributors may be used to endorse or promote 
  *        products derived from this software without specific prior written 
  *        permission.
  *  
  *  THIS SOFTWARE IS PROVIDED BY SRL @ TAMU ``AS IS'' AND ANY
  *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  *  DISCLAIMED. IN NO EVENT SHALL SRL @ TAMU BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  </pre>
  *  
  *******************************************************************************/
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import srl.graffiti.client.nio.Callback;
 import srl.graffiti.client.nio.GraffitiClient;
 import srl.gui.android.EditSketch;
 
 import android.os.Bundle;
 import android.accounts.Account;
 import android.accounts.AccountManager;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.support.v4.app.NavUtils;
 
 public class SampleActivity extends Activity {
 
 	Button uploadButton;
 	GraffitiClient client;
 	RelativeLayout screenLayout;
 	EditSketch sketchCanvas;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);		
 
 		sketchCanvas = new EditSketch(this.getApplicationContext());
 		sketchCanvas.setEnabled(true);
 		
 		setContentView(R.layout.activity_sample);
 
 		uploadButton = (Button) findViewById(R.id.upload_button);		
 		screenLayout = (RelativeLayout) findViewById(R.id.screen_layout);
 		
 		RelativeLayout.LayoutParams layoutParameters = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.FILL_PARENT
 				,RelativeLayout.LayoutParams.FILL_PARENT);
 		screenLayout.addView(sketchCanvas, 1, layoutParameters);
 		uploadButton.bringToFront();
 			
 	}
 
 	public void uploadImageTest(View view){
 		File imageFile = new File("/mnt/sdcard/DCIM/Camera/").listFiles()[0];
 		
 		client.uploadImage(imageFile, new Callback<String>(){
 			@Override
 			public void onCallback(String url) {
				Log.i("GraffitiClientTest", "Uploaded image. URL for image:"+url);
 			}
 		});
 	}
 	
 	public void startTest(View view) {
 		try {
 			Log.i("GraffitiClientTest", "Trying to run test");
 
 			SampleActivity.this.client = new GraffitiClient(new URL(
 					"http://192.168.1.115:8888/graffiti"), true);
 			SampleActivity.this.client = new GraffitiClient(new URL("http://192.168.1.115:8888/graffiti"), true);
 			
 			client.logIn("test@gmail.com", "something",
 					new Callback<Boolean>() {
 						@Override
 						public void onCallback(Boolean data) {
				new Callback<Boolean>() {
					@Override
					public void onCallback(Boolean data) {
						if(data){
							Log.i("GraffitiClientTest", "Login Successful");
 						}
					});
						else
							Log.i("GraffitiClientTest", "Login Failed");
				}
			});
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			Log.e("GraffitiClientTest", "Error", e);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_sample, menu);
 		return true;
 	}
 
 }
