 package com.techtalk4geeks.blogspot;
 
 import java.sql.Date;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.DatePickerDialog;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.Button;
 import android.widget.DatePicker;
 import android.widget.TextView;
 import android.widget.Spinner;
 import android.widget.DatePicker;
 
 public class MainActivity extends Activity
 {
 	DatePicker myDatePicker;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.setup);
 		// myDatePicker = ((DatePicker)findViewById(R.id.typeSelecter));
 		
 		Spinner spinner = ((Spinner) findViewById(R.id.typeSelecter));
 		for (String rankName : User.rank_display)
 		{
 			TextView text = new TextView(this);
 			text.setText(rankName);
 			spinner.addView(text);
 		}
 		Button doneButton = (Button) this.findViewById(R.id.done_button);
 		doneButton.setOnClickListener(new View.OnClickListener()
 		{
 			public void onClick(View v)
 			{
 				String name = ((TextView) findViewById(R.id.nameField))
 						.getText().toString();
 				String rank = ((Spinner) findViewById(R.id.typeSelecter))
 						.getSelectedItem().toString();
 				String strAge = ((TextView) findViewById(R.id.ageField))
 						.getText().toString();
 				int age = Integer.parseInt(strAge);
 				User user = new User(name, rank, age);
 				setContentView(R.layout.activity_main);
 				TextView nameView = (TextView) MainActivity.this
 						.findViewById(R.id.nameValue);
 				nameView.setText(user.getName());
 				TextView levelView = (TextView) MainActivity.this
 						.findViewById(R.id.levelValue);
 				levelView.setText(String.valueOf(user.getLevel()));
 				TextView hpView = (TextView) MainActivity.this
 						.findViewById(R.id.hpValue);
 				hpView.setText(String.valueOf(user.getHP()));
 				TextView spView = (TextView) MainActivity.this
 						.findViewById(R.id.spValue);
 				spView.setText(String.valueOf(user.getSP()));
 				TextView powView = (TextView) MainActivity.this
 						.findViewById(R.id.powValue);
 				powView.setText(String.valueOf(user.getPOW()));
 				TextView defView = (TextView) MainActivity.this
 						.findViewById(R.id.defValue);
 				defView.setText(String.valueOf(user.getDEF()));
 				TextView speedView = (TextView) MainActivity.this
 						.findViewById(R.id.speedValue);
 				speedView.setText(String.valueOf(user.getSPEED()));
 			}
 		});
 
 		// WARNING: METHOD NOT OVER!
 
 		// user.setName(view.getText().toString());
 	}
 
 	// @Override
 	// public boolean onCreateOptionsMenu(Menu menu)
 	// {
 	// // Inflate the menu; this adds items to the action bar if it is present.
 	// getMenuInflater().inflate(R.menu.main, menu);
 	// return true;
 	// }
 	//
 	// private DatePickerDialog.OnDateSetListener datePickerListener
 	// = new DatePickerDialog.OnDateSetListener() {
 	//
 	// // when dialog box is closed, below method will be called.
 	// public void onDateSet(DatePicker view, int selectedYear,
 	// int selectedMonth, int selectedDay) {
 	// int year = selectedYear;
 	// int month = selectedMonth;
 	// int day = selectedDay;
 	//
 	// // set selected date into textview
 	// tvDisplayDate.setText(new StringBuilder().append(month + 1)
 	// .append("-").append(day).append("-").append(year)
 	// .append(" "));
 	//
 	// // set selected date into datepicker also
 	// dpResult.init(year, month, day, null);
 	//
 	// }
 	// };
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu)
 	{
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item)
 	{
 		// Handle item selection
 		switch (item.getItemId())
 		{
 		case R.id.inventory:
 			setContentView(R.layout.inventory);
 			return true;
 		case R.id.map:
 			setContentView(R.layout.map);
 			return true;
		case R.id.battle:
			return true;
		case R.id.crash:
			throw new NullPointerException();
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 }
