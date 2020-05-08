 package com.example.droidbox;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 //uses up navigation in the manifest, should be changed later so users can't go back to the queue without signing in
 public class LoginActivity extends Activity {
  
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_login);       
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_login, menu);
         return true;
     }
     
     public void KillApp(View view)
     {
     	finish();
     	//close the application...somehow
     }
     public void Authenticate(View view)
     {
     	
     	EditText pass = (EditText) findViewById(R.id.editText2);//getting the password
     	String tablePassword = pass.getText().toString();
     	
    	EditText num = (EditText) findViewById(R.id.editText0);//getting the table number
     	String tableNumber = num.getText().toString();
     	
    	EditText nick = (EditText) findViewById(R.id.editText1);//getting the table nickname
     	String nickname = nick.getText().toString();
     	
     	communicate( tablePassword, tableNumber, nickname);//passing parameters to helper method communicate
     	
     	
     	Intent intent = new Intent(this, Main.class);
     	startActivity(intent);
     	finish();//goes to main screen AND users can't "back" into this login screen again
         SplashscreenActivity.alreadyLogged = true;//set boolean to true!
     }
     
     public void communicate( String tablePassword, String tableNumber, String nickname) 
     {
     	String test = "nothing";
     	
     	JSONParser jParser = new JSONParser();
     	List<NameValuePair> params = new ArrayList<NameValuePair>();
     	
     	//adding parameters to send through JSON
     	 params.add(new BasicNameValuePair(tableNumber, tablePassword ));
     	
         // sending parameters through JSON
     	String url = "http://9.12.10.1/db-wa/getQueue.php";
         JSONObject json = jParser.makeHttpRequest(url, "POST", params, "0");
         
         Context context = getApplicationContext();
         int duration = Toast.LENGTH_SHORT;
         Toast toast = Toast.makeText(context, "authenticated", duration);
         toast.show();
     }
     
     public void onBackPressed() {
     	//overriding back button so that user can't go back to main screen.
     }
     
 }
