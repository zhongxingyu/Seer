 
 package com.assurance;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 
 
 import com.Client.*;
 
 import com.library.UserFunctions;
 public class DashboardActivity extends Activity {
 	UserFunctions userFunctions;
 	Button btnLogout;
 	Button add;
 	Button view;
 	Button search;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         /**
          * Dashboard Screen for the application
          * */        
         // Check login status in database
         userFunctions = new UserFunctions();
         if(userFunctions.isUserLoggedIn(getApplicationContext())){
         	setContentView(R.layout.dashboard);
         	btnLogout = (Button) findViewById(R.id.btnLogout);
         	add = (Button) findViewById(R.id.add);
         	view = (Button) findViewById(R.id.view);
         	search = (Button) findViewById(R.id.search);
         	
         	btnLogout.setOnClickListener(new View.OnClickListener() {
     			
     			public void onClick(View arg0) {
     				// TODO Auto-generated method stub
     				userFunctions.logoutUser(getApplicationContext());
     				Intent login = new Intent(getApplicationContext(), MainActivity.class);
     	        	login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
     	        	startActivity(login);
     	        	// Closing dashboard screen
     	        	finish();
     			}
     		});
         	
         }else{
         	// user is not logged in show login screen
         	Intent login = new Intent(getApplicationContext(), MainActivity.class);
         	login.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
         	startActivity(login);
         	// Closing dashboard screen
         	finish();
         }
         
      // call add
      		add.setOnClickListener(new View.OnClickListener() {
 
                public void onClick(View v) {
                    Intent myintent2 = new Intent(DashboardActivity.this,Add.class);
                    startActivity(myintent2);
 
                 }
           }); 
         
      	   // call view
      		view.setOnClickListener(new View.OnClickListener() {
 
                public void onClick(View v) {
                    Intent myintent2 = new Intent(DashboardActivity.this,All_Clients.class);
                    startActivity(myintent2);
 
                 }
           }); 
      		
      	   // call search
      		search.setOnClickListener(new View.OnClickListener() {
 
                public void onClick(View v) {
                    Intent myintent2 = new Intent(DashboardActivity.this,Search_plate.class);
                    startActivity(myintent2);
 
                 }
           }); 
     }
 
  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.main, menu);
      return true;
  }
 
  @Override
  public boolean onOptionsItemSelected(MenuItem item)
  {
       
      switch (item.getItemId())
      {
      case R.id.PushRegistration:
       
     	 Intent myintent2 = new Intent(DashboardActivity.this,RegisterActivity.class);
          startActivity(myintent2);
 
 
 
      default:
          return super.onOptionsItemSelected(item);
      }
  }    
 
 }
