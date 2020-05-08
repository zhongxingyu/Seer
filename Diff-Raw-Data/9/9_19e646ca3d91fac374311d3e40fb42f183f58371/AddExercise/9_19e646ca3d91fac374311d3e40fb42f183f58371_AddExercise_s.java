 /*This file is part of Gymapp.
  *
  *   Gymapp is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Gymapp is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *  along with Gymapp.  If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.Grupp01.gymapp;
 
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.Spinner;
 
 import com.Grupp01.gymapp.R;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 /** 
  * @author GivDev
  * @version 0.1
  * @peer reviewed by
  * @date 04/10/12
  *
  * This class is made exclusively for initiating the part of the GUI that gives the user 
  * a opportunity to add and change exercises
  * 
  * <p>This class i a part of the </p><i>View</i><p> package, and a part of the </p><i>Exercise</i>
  * <p> Subpackage</p> 
  *
  */
 public class AddExercise extends SherlockActivity implements AdapterView.OnItemSelectedListener {
 	private String[] items;
 	private Spinner spinnerType;
 	private String currentView;
 	/**
 	 * Instantiates the class with necessary method calls, setting up the correct layout
 	 * and receiving the intent that started this activity
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_exercise);
 		getSupportActionBar().setHomeButtonEnabled(true);
 		Intent get_Title = getIntent();
		setTitle(get_Title.getStringExtra(Exercise.EXTRA_EXERCISE_NAME));
 
 		Resources res = getResources();
 		items = res.getStringArray(R.array.trainingtype_array);//get String-array from strings.xml
 		initSpinnerType(0); //initialize spinner with listener and set spinner to static
 	}
 	/**
 	 * Sets up the menubar, note the use of actionbarsherlock, making it possible of using
 	 * a menubar for APIs lower than 11
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.activity_add_exercise, menu);
 		return true;
 	}
 
 	/**
 	 * This method initializes the spinners, populating them with items and adds listener
 	 * @param position used for changing the default value due to the previous selection in a spinner
 	 */
 	public void initSpinnerType(int position)
 	{
 		spinnerType = (Spinner) findViewById(R.id.spinner_type_of_training); //Retrieves the view from .xml-file
 		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
 				R.array.trainingtype_array, android.R.layout.simple_spinner_item);//From developer.android.com
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerType.setAdapter(adapter); //Sets the adapter to the spinner
 
 		spinnerType = (Spinner) findViewById(R.id.spinner_type_of_training);
 		spinnerType.setSelection(position); //Sets the spinner default value to selected value
 		spinnerType.setOnItemSelectedListener(this); //Adds listener to spinner spinnterType
 	}
 
 	/**
 	 * Callback method to be invoked when an item in this view has been selected
 	 */
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		if(!items[position].equals(currentView)) //conditional if to prevent infinite loop
 		{
 			if(items[position].equals("Static"))
 			{
 				setContentView(R.layout.add_exercise_static);	//Switches the layout to the selected one
 				initSpinnerType(position);	//After switching view, adds listener to spinner again
 				currentView=items[position];	//Sets currentView to prevent infinite loop
 			}
 
 			else if(items[position].equals("Dynamic"))
 			{
 				setContentView(R.layout.add_exercise_static);	
 				initSpinnerType(position);
 				currentView=items[position];
 			}
 			else if (items[position].equals("Cardio"))
 			{
 				setContentView(R.layout.activity_add_exercise);	
 				initSpinnerType(position);
 				currentView=items[position];
 			}
 		}
 	}
 	/**
 	 * Callback method to be invoked when the selection disappears from this view.
 	 */
 	public void onNothingSelected(AdapterView<?> parent)
 	{
 	}
 	/**
 	 * <p>Callback method to be invoked when the </p><i>done-button</i><p> has been clicked, making the activity
 	 * send the new exercise to the database.</p>
 	 */
 	public void done(View view)
 	{
 		//Send data to database
 	}
 	/**
 	 * <p>Callback method to be invoked when the </p><i>cancel-button</i><p> has been clicked, making the activity
 	 * return the the previous</p><i> Exercise.java</i>
 	 */
 	public void cancel(View view)
 	{
 		finish();
 	}
 }
