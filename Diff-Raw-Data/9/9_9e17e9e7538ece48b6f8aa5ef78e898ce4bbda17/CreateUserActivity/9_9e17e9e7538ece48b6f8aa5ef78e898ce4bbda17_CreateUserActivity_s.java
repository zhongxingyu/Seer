 package com.example.palhunter;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Timestamp;
 import java.util.Date;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 
 public class CreateUserActivity extends Activity {
 	
 	EditText firstNameText, lastNameText;
 	Date myDate;
 	Integer userId;
 	Timestamp myTimestamp;
 	String userName = "team17";
 	String password = "palhunter1";
 	
 	
 	public final static String EXTRA_MESSAGE = "com.example.palhunter.MESSAGE";
     String httpInserUserURL = "http://hamedaan.usc.edu:8080/team17/QueryServlet?id=%d&first_name=%s&last_name=%s&created_time=%d&action=insertPeople";
     final String httpGetUserNumQuery = "http://hamedaan.usc.edu:8080/team17/QueryServlet?action=getTotalPeople";
     String firstName, lastName;
 	Handler handler; 
 	HttpClient httpClient = new DefaultHttpClient();
 	
     public void SubmitData(View view) {
     	System.out.println("submit data");
     	
     	firstNameText = (EditText)findViewById(R.id.first_name);
     	firstName = firstNameText.getText().toString().trim();
     	
     	lastNameText = (EditText)findViewById(R.id.last_name);
     	lastName = lastNameText.getText().toString().trim();
     	
     	//random user id??
     	userId = 10;
     	if (firstName.length() <= 0) {
     		Toast.makeText(this, "please input your name", Toast.LENGTH_SHORT).show();
     	} else {
     		System.out.println("got message: " + firstName + "  " + lastName);	
     		
     		final Runnable rr = new Runnable() {
 				public void run() {
 	    			HttpGet getUserNumUrl = new HttpGet(httpGetUserNumQuery);
 		    		HttpResponse response;
 		    		try {
 						response = httpClient.execute(getUserNumUrl);
						userId = User.getTotalUserNumber(response);
 		                   
 					//	System.out.println("get user number response: " + response.getEntity().getContent().toString());
 					//	userId = Integer.parseInt(response.getEntity().getContent().toString());
 						
 						response.getEntity().consumeContent();
 					} catch (Exception e) {
 						System.out.println("cant get total user number");
 						e.printStackTrace();
 					} 
 		    		long pubDate = System.currentTimeMillis();
 		    		final String url = String.format(httpInserUserURL, userId, firstName, lastName, pubDate);
 		    		System.out.println("send request:  " + url);
 		    		
 					HttpResponse responseStream;
 					HttpGet httpPost = new HttpGet(url);
 		    		try {
 						responseStream = httpClient.execute(httpPost);
 						System.out.println(EntityUtils.toString(responseStream.getEntity()));
 						responseStream.getEntity().consumeContent();
 					} catch (ClientProtocolException e) {
 				        // TODO Auto-generated catch block
 						System.out.println("can't connect to insert ppl");
 				    } catch (IOException e) {
 				        // TODO Auto-generated catch block
 				    } 
 				}
     		};
     		Thread submitDataThread = new Thread(rr);
     		submitDataThread.start();
     	}
     	
 		Intent intent = new Intent(this, MyLocation.class);
 		intent.putExtra("id", userId);
 		intent.putExtra("firstName", firstName);
 		intent.putExtra("lastName", lastName);
 		startActivity(intent);
     }
     
     public void BackToLogin(View view){
 		Intent intent = new Intent(this, LogInActivity.class);
 		startActivity(intent);
     }
     
  /*   
 	public void retrieveData(View view) {
 		final Runnable rr = new Runnable() {
 			public void run() {
 				new RetrieveDataTask().execute();
 			}
 		};
     	handler.post(rr);
     	
     }
 	*/  
 	  
 	
 	
     @SuppressLint({ "NewApi", "NewApi" })
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_create_user);
 //        getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_create_user, menu);
         return true;
     }
 
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 /*        
     	switch (item.getItemId()) {
             case android.R.id.home:
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
 */        
         return super.onOptionsItemSelected(item);
     }
 }
