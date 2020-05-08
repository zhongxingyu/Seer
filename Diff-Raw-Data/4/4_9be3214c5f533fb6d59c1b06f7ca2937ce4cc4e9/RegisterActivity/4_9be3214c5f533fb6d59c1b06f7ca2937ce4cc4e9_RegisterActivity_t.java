 package com.rogoapp;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
import android.accounts.AccountAuthenticatorActivity;
 
 
 //for ServerClient class
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.json.JSONObject;
 import org.json.JSONException;
 
public class RegisterActivity extends AccountAuthenticatorActivity{
     
     Button registerButton;
     EditText lastName;
     EditText firstName;
     EditText email;
     EditText password;
     
     TextView loginLink;
     
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.register);
         addListenerOnButton1();
         
         Button button = (Button) this.findViewById(R.id.link_to_login);
         button.setBackgroundColor(Color.TRANSPARENT);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main_screen, menu);
         return true;
     }
     
     public void addListenerOnButton1() {
 
         registerButton = (Button) findViewById(R.id.btnRegister);
 
         registerButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 //TODO send authentication for registration
             	List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
         		nameValuePairs.add(new BasicNameValuePair("username", "JoeyS7"));
         		nameValuePairs.add(new BasicNameValuePair("email", "joeysiracusa@gmail.com"));
         		nameValuePairs.add(new BasicNameValuePair("password", "a336f671080fbf4f2a230f313560ddf0d0c12dfcf1741e49e8722a234673037dc493caa8d291d8025f71089d63cea809cc8ae53e5b17054806837dbe4099c4ca"));
                 ServerClient sc = new ServerClient();
                 JSONObject jObj = sc.genericPostRequest("register", nameValuePairs);
                 String uid = null;
                 String status = null;
                 try{
                 	//uid = sc.getLastResponse().getString("uid");
                 	status = jObj.getString("status");
                 }catch(JSONException e){
                 	System.err.print(e);
                 }
                 System.out.println("status = " + status + ", uid = " + uid);
             }
 
         });
 
     }
     public void addListenerOnButton2() {
 
         registerButton = (Button) findViewById(R.id.link_to_login);
 
         registerButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View arg0) {
                 openLoginScreen(arg0);
             }
 
         });
 
     }
     
     public void openLoginScreen(View v){
         final Context context = this;
         Intent intent = new Intent(context, LoginActivity.class);
         startActivity(intent);
     }
     
     
     
 }
