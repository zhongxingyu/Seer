 package edu.upenn.cis350.project;
 
 import java.util.Calendar;
 
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.app.Dialog;
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.ViewGroup.LayoutParams;
 import android.view.ViewParent;
 import android.view.ViewStub;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.EditText;
 import android.widget.LinearLayout;
 import android.widget.Spinner;
 import android.widget.TextView;
 
 public class InfoActivity extends Activity {
 	
 	private TextView saleDisplayDate;
 	private Button changeDate;
 	
 	private int year;
 	private int month;
 	private int day;
 	
 	static final int DATE_DIALOG_ID = 999;
 	
 	private Button addVol;
 	private LinearLayout volLayout;
 	private int id;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_info);
         
         Spinner spinner = (Spinner) findViewById(R.id.school);
         
         // Create an ArrayAdapter using the string array and a default spinner layout
         ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
              R.array.school_list, android.R.layout.simple_spinner_item);
      
         // Specify the layout to use when the list of choices appears
         adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
         
         // Apply the adapter to the spinner
         spinner.setAdapter(adapter);
         
         //for the date picker
         setCurrentDateOnView();
 		addListenerOnButton();
 		
 		//for the volunteer adder
 		addListenerAddVol();
 		volLayout = (LinearLayout) findViewById(R.id.volunteer_list);  
         id = 5;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_start, menu);
         return true;
     }
      
     
  // display current date
 	public void setCurrentDateOnView() {
  
 		saleDisplayDate = (TextView) findViewById(R.id.saleDate);
  
 	    //auto populating the date
 		final Calendar c = Calendar.getInstance();
 		year = c.get(Calendar.YEAR);
 		month = c.get(Calendar.MONTH);
 		day = c.get(Calendar.DAY_OF_MONTH);
  
 		// set current date into textview
 		saleDisplayDate.setText(new StringBuilder()
 			// Month is 0 based, just add 1
 			.append(month + 1).append("-").append(day).append("-")
 			.append(year).append(" "));
  
 	}
  
 	public void addListenerOnButton() {
 		 
 		changeDate = (Button) findViewById(R.id.change_date);
  
 		changeDate.setOnClickListener(new View.OnClickListener() {
  
 			@Override
 			public void onClick(View v) {
  
 				showDialog(DATE_DIALOG_ID);
  
 			}
  
 		});
  
 	}
 	
 	@Override
 	protected Dialog onCreateDialog(int id) {
 		switch (id) {
 		case DATE_DIALOG_ID:
 		   // set date picker as current date
 		   return new DatePickerDialog(this, datePickerListener, 
                          year, month,day);
 		}
 		return null;
 	}
 	
  
 	private DatePickerDialog.OnDateSetListener datePickerListener 
     		= new DatePickerDialog.OnDateSetListener() {
 
 			// when dialog box is closed, below method will be called.
 			public void onDateSet(DatePicker view, int selectedYear,
 				int selectedMonth, int selectedDay) {
 				year = selectedYear;
 				month = selectedMonth;
 				day = selectedDay;
 
 				// set selected date into textview
 				saleDisplayDate.setText(new StringBuilder().append(month + 1)
 						.append("-").append(day).append("-").append(year)
 						.append(" "));
 			}
 		};
 	
 		//listener for button that should add an edittext for another volunteer
 		public void addListenerAddVol(){
 			addVol = (Button) findViewById(R.id.add_volunteer);
 			
 			addVol.setOnClickListener(new View.OnClickListener() {
 				 
 				@Override
 				public void onClick(View v) {
 					moreVolunteers();
 	 
 				}
 	 
 			});
 		}
 		
 		//method that actually adds another edittext for another volunteer
 		private void moreVolunteers(){
 			EditText newVol = new EditText(this);
 			newVol.setId(id);
 			id++;
 			newVol.setWidth(100);
			newVol.setHeight(70);
 			volLayout.addView(newVol);
 		}
 	
     
     public void continueToWeather(View v) {
     	//save school
     	//save date
     	//save volunteers
     	//save staff
     	
     	//Launch to weather
     	Intent i = new Intent(this, WeatherActivity.class);
     	//Save our info
     	i.putExtra("herma", "derp");
     	this.startActivity(i);
     }
 }
