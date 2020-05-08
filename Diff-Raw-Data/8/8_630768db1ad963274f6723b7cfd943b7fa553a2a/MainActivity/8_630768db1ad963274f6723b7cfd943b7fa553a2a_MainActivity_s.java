 /*
  * Dan Annis
  * Java 1 Week 1 - Audit
  * June 6 2013
  */
 package com.dannis.java1week1_audit;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 import android.view.View;
 
 public class MainActivity extends Activity {
 
 	
 	LinearLayout ll;
 	LinearLayout.LayoutParams lp;
 	TextView results;
 	TextView pricePerAppResult;
 	EditText companyField;
 	EditText amtdownloadedField;
 	int totalPrice;
 	int pricePerApp;
 	boolean groupRate;
 	
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
 
      // setting up the linear layout cause this is what we do
         ll = new LinearLayout(this);
         ll.setOrientation(LinearLayout.VERTICAL);
         
      // setting up the layout params which tells the layout to match both the width and the height of the entire screen
         lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
         ll.setLayoutParams(lp);
         
      // setting up Edit text boxes for user entry
         companyField = new EditText(this);
         companyField.setHint("Company Name");
         amtdownloadedField = new EditText(this);
         amtdownloadedField.setHint("QTY of Apps needed");
      
      // Calculation Button
         Button compute = new Button(this);
         compute.setText("Compute!");
         compute.setOnClickListener(new View.OnClickListener() {
 
         	@Override
         	public void onClick(View v) {
         		
         		//Convert the number entered(a string) to an integer that I can work with to mathmatize! 
         		int amountNeeded = Integer.parseInt(amtdownloadedField.getText().toString());	
         		//Grab the cost for the app
         		int costPerDownload = getResources().getInteger(R.integer.appPrice);
         		//Do the Math
         		totalPrice = amountNeeded*costPerDownload;
         		//In case I want to call the price of the app
         		pricePerApp = costPerDownload;
         		
        		
         		//Show the Results of the Calculation
        		results.append("You're total price will be $" + totalPrice + " at $" + pricePerApp + " per download.");
 
 
         		
         	}
         });
         
         
         
         
         
         
         
      // Create the Layout for the app
         LinearLayout form = new LinearLayout(this);
         form.setOrientation(LinearLayout.VERTICAL);
         lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         form.setLayoutParams(lp);
         
      // Adding fields to the layout
         form.addView(companyField);
         form.addView(amtdownloadedField);
         form.addView(compute);
         ll.addView(form);
         
         
      //Display the result Text
         results = new TextView(this);
         ll.addView(results);
         
         
      //Setting the ll to show
         setContentView(ll);
     }
 
     
     //Not used at this point in the class
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
     
 }
