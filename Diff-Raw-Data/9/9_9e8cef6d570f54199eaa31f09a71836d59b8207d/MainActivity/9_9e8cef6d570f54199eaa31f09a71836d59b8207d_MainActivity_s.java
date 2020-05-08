 /*
  * project		java1project1
  * 
  * package		com.lucyhutcheson.java1project1
  * 
  * @author		Lucy Hutcheson
  * 
  * date			Jun 5, 2013
  * 
  */
 package com.lucyhutcheson.java1project1;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Context;
 import android.view.Menu;
 import android.view.View;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.TextView;
 
 
 public class MainActivity extends Activity {
 
 	// setup Variables
 	LinearLayout ll;
 	LinearLayout.LayoutParams lp;
 	EditText nameField;
 	EditText carsField;
 	TextView result;
 	TextView pricePerCar;
 	int totalTires;
 	int totalPrice;
 	boolean qualifySpecial;
 	
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         	
         // initiate new linear layout
         ll = new LinearLayout(this);
         ll.setOrientation(LinearLayout.VERTICAL);
         
         // initiate layout params
         lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
         ll.setLayoutParams(lp);
         
         // setup text view and add a title
         TextView tv = new TextView(this);
         tv.setText("Auto Tire Calculator");
         ll.addView(tv);
         
         // text fields
         carsField = new EditText(this);
         carsField.setHint("Enter Number of Cars");
         
         nameField = new EditText(this);
         nameField.setHint("Enter Your Name");
         
         qualifySpecial = false;
 
         // Calculate Button
         Button b = new Button(this);
         b.setText("Calculate Tire Price");
         b.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 
 				// Dismiss the keyboard so we can see our text
 				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
 				imm.hideSoftInputFromWindow(carsField.getWindowToken(), 0);
 				
 				// Setup variables to be used in this function
 				int numTires = getResources().getInteger(R.integer.numTires);
 				int tirePrice = getResources().getInteger(R.integer.tirePrice);
 				int numCars = Integer.parseInt(carsField.getText().toString());				
 				
 				// Calculate the total number of tires we have from the number of cars entered
 				totalTires = numTires*numCars;
 				
 				// Display a greeting
 				result.setText("Hello "+ nameField.getText().toString() + "!\r\nOur tires cost $"+tirePrice+" per tire.\r\n");
 
 				// Check if we have a car and calculate price and display message
 				if (numCars > 0)
 				{
 					if (numCars > 3){
 						qualifySpecial = true;
 					}
 					for (int i=1, j=numCars+1; i<j; i++)
 					{
 						int perCar = numTires * tirePrice;
 						pricePerCar.append("\r\nCar #"+ i + "= $" + perCar + "\r\n");
 					}
 					
 					totalPrice = totalTires*tirePrice;
 					result.append("Your total number of tires is " + totalTires + ".\r\n" + "The total price to replace all those tires is $" + totalPrice + ".\r\n");
 
 					if (qualifySpecial)
 					{
						result.append("\r\nCongratulations!! You qualify for 4 free tires!");
 					}
 					else if (!qualifySpecial)
 					{
						result.append("\r\nThank you for stopping by!! If you have more than 3 cars, you can qualify for 4 free tires!");
 					}
 				} 
 				// If we don't have any cars, display message.
 				else 
 				{
 					result.append("You haven't entered any cars.  Please try again.");
 				}
 				
 			}
 		});
         
         // Clear Button
         Button c = new Button(this);
         c.setText("Clear Text");
         c.setOnClickListener(new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 
 				// Empty result textView and pricePerCar textView
 				result.setText("");
 				pricePerCar.setText("");
 				// Empty out my fields for reuse
 				nameField.setText("");
 				carsField.setText("");	
 				// Reset boolean
 				qualifySpecial = false;
 			}
 		});
 
         // Create the Layout for our app and setup the parameters
         LinearLayout form = new LinearLayout(this);
         form.setOrientation(LinearLayout.VERTICAL);
         lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         form.setLayoutParams(lp);
         
         // Add text field and buttons to new layout
         form.addView(nameField);
         form.addView(carsField);
         form.addView(b);
         form.addView(c);
         
         // Add new form layout to original view
         ll.addView(form);
         
         // Create textViews for resulting text string and add it to the view
         // Result TextView to hold calculations
         result = new TextView(this);
         ll.addView(result);
         // PricePerCar TextView to hold price of tires per car
         pricePerCar = new TextView(this);
         ll.addView(pricePerCar);
 
         setContentView(ll);
         
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
