 package com.youpony.amuse;
 
 import java.io.ByteArrayOutputStream;
 
 import org.apache.http.HttpResponse;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.graphics.Bitmap;
 import android.util.Base64;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class SendStory extends Activity {
 
 	TextView email, name;
 	EditText emailForm,  nameForm;
 	String emailString, nameString;
 	Button send, cancel;
 	AlertDialog.Builder error;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_send_story);
 		email = (TextView) findViewById(R.id.emailTitle);
 		name = (TextView) findViewById(R.id.nameTitle);
 		email.setText("Insert email here:");
 		name.setText("insert your name here:");
 		emailForm = (EditText) findViewById(R.id.emailForm);
 		nameForm = (EditText) findViewById(R.id.nameForm);
 		
 		send = (Button) findViewById(R.id.send);
 		cancel = (Button) findViewById(R.id.cancel);
 		
 		cancel.setOnClickListener(cancelListener);
 		send.setOnClickListener(sendListener);
 		
 		
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.send_story, menu);
 		return true;
 	}
 	
 	OnClickListener cancelListener = new OnClickListener(){
 		@Override
 		public void onClick(View v) {
 			close();
 			
 		}
 	};
 	
 	
 	OnClickListener sendListener = new OnClickListener(){
 
 		@Override
 		public void onClick(View arg0) {
 			emailString = emailForm.getText().toString();
 			nameString = nameForm.getText().toString();
 			error = new AlertDialog.Builder(SendStory. this);
 			error.setTitle("Wrong email!");
 			error.setMessage("Please check your email.");
 			if(!emailString.contains("@")){
 				error.show();
 				Log.i("orrudebug", "sbagliato email");
 			}
 			else{
 				JSONObject json = new JSONObject();
 				try {
 					json.put("email", emailString);
 					json.put("name", nameString);
 					JSONArray jsonarray = new JSONArray();
 					for(int i=0; i<PageViewer.values.size(); i++){
 						if(PageViewer.values.get(i).type.equals("QR")){
 							JSONObject jsonId = new JSONObject();
 							jsonId.put("item_pk", PageViewer.values.get(i).id);
 							jsonarray.put(jsonId);
 						}
 						else{
							Bitmap bigPic = PageViewer.decodeSampledBitmapFromFile(PageViewer.values.get(i).url,10,10);//PageViewer.values.get(i).w ,PageViewer.values.get(i).h );
 							ByteArrayOutputStream baos = new ByteArrayOutputStream();  
							bigPic.compress(Bitmap.CompressFormat.JPEG, 0, baos); 
 							byte[] b = baos.toByteArray();
 							
 							String encoded = Base64.encodeToString(b, Base64.DEFAULT);
 							Log.i("orrudebug", "image: " + encoded.length());
 														
 							JSONObject jsonId = new JSONObject();
 							jsonId.put("image", encoded);
 							jsonarray.put(jsonId);
 						}
 					}
 					json.put("posts", jsonarray);
 				} catch (JSONException e) {
 					e.printStackTrace();
 				}
 				new SendJSONAsync(){
 					protected void onPostExecute(HttpResponse response){
 						if(response != null){
 							Log.i("orrudebug", "storia inviata correttamente");
 							//clear della story e reset
 							PageViewer.values.clear();
 							PageViewer.pinterestItems.clear();
 							Story.pinterestAdapter.clear();
 							Story.pinterestAdapter.notifyDataSetChanged();
 //							Story.line.setVisibility(View.INVISIBLE);
 							Story.send.setVisibility(View.INVISIBLE);
 							
 							close();
 						}
 						else{
 							Log.i("orrudebug", "storia non inviata");
 						}
 					}
 				}.execute(json);
 			}
 		}
 	};
 	
 
 	void close(){
 		SendStory.this.finish();
 		PageViewer.mViewPager.setCurrentItem(1);
 	}
 
 }
