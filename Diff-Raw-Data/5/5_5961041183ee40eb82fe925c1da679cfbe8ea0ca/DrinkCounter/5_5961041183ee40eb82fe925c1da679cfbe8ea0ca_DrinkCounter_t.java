 package com.example.drinkingapp;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.os.Build;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.TextView;
 
 public class DrinkCounter extends Activity {
 	private int drink_count = 0;
 	// int start_color = 0xFF7b9aad;
 	int start_color = 0x884D944D;
 	int offset = 10;
 	private DatabaseHandler db;
 	private double hours;
 	private int color;
 	private double bac;
 
 	// TODO:Temporary, move to a class that makes more sense
 	private final Double CALORIES_PER_DRINK = 120.0;
 	private final Double CALORIES_PER_CHICKEN = 264.0;
 	private final Double CALORIES_PER_PIZZA = 285.0;
 
 	@SuppressLint("NewApi")
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.drink_tracking);
 
 		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
 			getActionBar().setDisplayHomeAsUpEnabled(true);
 		}
 
 		db = new DatabaseHandler(this);
 		View view = findViewById(R.id.drink_layout);
 		calculateBac();
 		calculateColor();
 		view.setBackgroundColor(color);
 		setContentView(view);
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		View view = findViewById(R.id.drink_layout);
 		calculateBac();
 		calculateColor();
 		view.setBackgroundColor(color);
 	}
 
 	private void calculateHours() {
 		Date date = new Date();
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 		SimpleDateFormat day_fmt = new SimpleDateFormat("dd", Locale.US);
 
 		int year = Integer.parseInt(year_fmt.format(date));
 		int month = Integer.parseInt(month_fmt.format(date));
 		int day = Integer.parseInt(day_fmt.format(date));
 		
 		DatabaseStore current = new DatabaseStore("","",date, "Integer");
 		
 		ArrayList<DatabaseStore> drink_count_vals = (ArrayList<DatabaseStore>) db
 				.getVarValuesForDay("drink_count", month, day, year);
 		color = start_color;
 		if (drink_count_vals != null) {
 			drink_count = drink_count_vals.size();
 			drink_count_vals = DatabaseStore.sortByTime(drink_count_vals);
 
 			// calculate the hours drinking
 			if (drink_count_vals.size() > 0) {
 				DatabaseStore start = drink_count_vals.get(0);
 				Integer start_time = start.hour * 60 + start.minute;
				Integer last_time = current.hour * 60 + current.minute - 360;
 				hours = (last_time - start_time) / 60.0;
 			}
 		}
 	}
 
 	public void doneDrinking(View view) {
 		finish();
 	}
 
 	public void calculateColor() {
 		if (bac < 0.06) {
 			color = start_color;
 		} else if (bac < 0.15) {
 			color = 0X88E68A2E;
 		} else if (bac < 0.24) {
 			color = 0X88A30000;
 		} else {
 			color = 0XCC000000;
 		}
 	}
 
 	private void calculateBac() {
 		Date date = new Date();
 		SimpleDateFormat year_fmt = new SimpleDateFormat("yyyy", Locale.US);
 		SimpleDateFormat month_fmt = new SimpleDateFormat("MM", Locale.US);
 		SimpleDateFormat day_fmt = new SimpleDateFormat("dd", Locale.US);
 
 		int year = Integer.parseInt(year_fmt.format(date));
 		int month = Integer.parseInt(month_fmt.format(date));
 		int day = Integer.parseInt(day_fmt.format(date));
 		ArrayList<DatabaseStore> drink_count_vals = (ArrayList<DatabaseStore>) db
 				.getVarValuesForDay("drink_count", month, day, year);
 		if (drink_count_vals != null) {
 			calculateHours();
 
 			// get the users gender
 			ArrayList<DatabaseStore> stored_gender = (ArrayList<DatabaseStore>) db
 					.getAllVarValue("gender");
 			// If user did not set gender use "Female" as default
 			String gender = "Female";
 			if (stored_gender != null) {
 				gender = stored_gender.get(0).value;
 			}
 
 			// fetch the users weight
 			ArrayList<DatabaseStore> stored_weight = (ArrayList<DatabaseStore>) db
 					.getAllVarValue("weight");
 			Integer weight_lbs = 120;
 			if (stored_weight != null) {
 				weight_lbs = Integer.parseInt(stored_weight.get(0).value);
 			}
 
 			double metabolism_constant = 0;
 			double gender_constant = 0;
 			double weight_kilograms = weight_lbs * 0.453592;
 
 			if (gender.equals("Male")) {
 				metabolism_constant = 0.015;
 				gender_constant = 0.58;
 			} else {
 				metabolism_constant = 0.017;
 				gender_constant = 0.49;
 			}
 
 			bac = ((0.806 * drink_count * 1.2) / (gender_constant * weight_kilograms))
 					- (metabolism_constant * hours);
 		} else {
 			bac = 0;
 		}
 	}
 
 	@SuppressLint("NewApi")
 	public void hadDrink(View view) {
 		drink_count++;
 		if (drink_count == 1){
 			db.addValueTomorrow("drank_last_night", "True");
 			db.addValueTomorrow("tracked", "True");
 		}
 		db.addDelayValue("drink_count", drink_count);
 		calculateBac();
 		db.addDelayValue("bac", String.valueOf(bac));
 		calculateColor();
 		View parent_view = findViewById(R.id.drink_layout);
 		parent_view.setBackgroundColor(color);
 
 		//calculate number of chickens that equate the number of calories
 		Double drink_cals = drink_count * CALORIES_PER_DRINK;
 		int number_chickens = (int) Math.ceil(drink_cals / CALORIES_PER_CHICKEN);
 		db.updateOrAdd("number_chickens", number_chickens);
 
 		//calculate the number of slices of pizza that equate to the 
 		//number of drinks consumed that day.
 		int number_pizza = (int) Math.ceil(drink_cals / CALORIES_PER_PIZZA);
 		db.updateOrAdd("number_pizza", number_pizza);
 		
 		TextView check = new TextView(this);
		check.setText(String.valueOf(hours));
 		((FrameLayout)parent_view).addView(check);
 	}
 }
