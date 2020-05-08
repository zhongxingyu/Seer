 package com.example.java1week1;
 
 import com.example.java1week1.R.integer;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 
 	LinearLayout ll;
 	LinearLayout.LayoutParams lp;
 	EditText day;
 	Boolean isItMay = true;
 	TextView result;
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         ll = new LinearLayout(this);
         ll.setOrientation(LinearLayout.VERTICAL);
         ll.setBackgroundColor(Color.MAGENTA);
         lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
         ll.setLayoutParams(lp);
  
         //Ask question
         TextView questionText = new TextView(this);
         questionText.setText("How long til our wedding aniversary?");
         questionText.setTextSize(30);
         ll.addView(questionText);
         
         //Get answer
         TextView may = new TextView(this);
         may.setText("Today is May the ");
         may.setTextSize(20);
         
         day = new EditText(this);
         day.setHint("Day");
         day.setTextSize(20);
       
         lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
         LinearLayout form = new LinearLayout(this);
         form.setOrientation(LinearLayout.HORIZONTAL);
         form.setLayoutParams(lp);
         
         form.addView(may);
         form.addView(day);
         
         ll.addView(form);
         
         result = new TextView(this);
         result.setTextSize(20);
         
         Button b = new Button(this);
         b.setText("Tell Me!");
         b.setBackgroundColor(Color.WHITE);
         b.setLayoutParams(lp);
         ll.addView(b);
         b.setOnClickListener(new View.OnClickListener()
         {
 			
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 				
 				String test = day.getText().toString();
 				if (test.matches(""))
 				{
 					result.setText("Enter the day!");
 			        result.setLayoutParams(lp);
 			        ll.addView(result);
 				}
 				else 
 				{
 					if (isItMay == false)
 					{
 						
 						result.setText("Its not May!");
 				        result.setLayoutParams(lp);
 				        ll.addView(result);
 					}
 					else
 					{
 						int i = Integer.parseInt(day.getText().toString());
 						printResults(i);
 					}
 				}
 			}
 		});
        
         setContentView(ll);
         
        
     }
 
 private void printResults(int dayOfMonth)
 {
 	//Set Ints
 	int anni = getResources().getInteger(R.integer.anniDate);
 	int day = getResources().getInteger(R.integer.day);
 	int hour = getResources().getInteger(R.integer.hour);
 	int minute = getResources().getInteger(R.integer.minute);
 	int second = getResources().getInteger(R.integer.second);
 	
 	//Calculate times
	int dayTotal = (anni - day);
	int hourTotal = (anni - day)*hour;
	int minTotal = (anni - day)*minute;
	int secTotal = (anni - day)*second;
 	
 	int[] timeArray = {dayTotal, hourTotal, minTotal, secTotal};
 	
 	//Create text views
 	TextView dayText = new TextView(this);
 	TextView hourText = new TextView(this);
 	TextView minText = new TextView(this);
 	TextView secText = new TextView(this);
 	
 	TextView[] textArray = {dayText, hourText, minText, secText};
 	
 	//Set Strings
 	String dayString = getResources().getString(R.string.days);
 	String hourString = getResources().getString(R.string.hours);
 	String minString = getResources().getString(R.string.minutes);
 	String secString = getResources().getString(R.string.seconds);
 	String anniString = getResources().getString(R.string.tilAnni);
 	
 	String[] stringArray = {dayString, hourString, minString, secString};
 	
 	//Loop through arrays and add view for each string
 	for (int i = 0; i < timeArray.length; i++) 
 	{
 		String value = String.valueOf(timeArray[i]);
 		textArray[i].setText(value+" "+stringArray[i]+anniString);
 		textArray[i].setTextSize(20);
 		textArray[i].setLayoutParams(lp);
         ll.addView(textArray[i]);
 	}
 }
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
 }
