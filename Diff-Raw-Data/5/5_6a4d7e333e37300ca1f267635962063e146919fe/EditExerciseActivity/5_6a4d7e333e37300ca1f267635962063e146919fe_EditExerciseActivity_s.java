 /*Copyright  2012 GivDev
  * 
  * This file is part of Gymapp.
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
 
 package com.Grupp01.gymapp.View.Exercise;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 import com.Grupp01.gymapp.R;
 import com.Grupp01.gymapp.Controller.IdName;
 import com.Grupp01.gymapp.Controller.Exercise.EditExerciseDbHandler;
 import com.Grupp01.gymapp.Controller.Exercise.ExerciseData;
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
 public class EditExerciseActivity extends SherlockActivity implements AdapterView.OnItemSelectedListener {
 	private Spinner spinnerType, spinnerPMuscle, spinnerSMuscle, spinnerSport;
 	private String currentView;
 	private int exerciseId;
 	private ExerciseData exercise;
 	private ArrayList<String> listMuscles, listSports, listTrainingType;
 	private EditText comment, desc;
 	private List<IdName> idNameListMuscles, idNameListSports, idNameListTrainingType;
 	/**
 	 * Instantiates the class with necessary method calls, setting up the correct layout
 	 * and receiving the intent that started this activity
 	 */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_add_exercise);
 		getSupportActionBar().setHomeButtonEnabled(true);
 
 		//get the id for the exercise that started this activity
 		exerciseId = getIntent().getIntExtra(ListExerciseActivity.EXTRA_EXERCISE_NAME, 0);
 		//Retrieves the exercise from database using exerciseId
 		getExerciseData(); 
 
 		//Get ArrayList<String> with training types from database
 		listTrainingType = getExerciseTypesFromDb(); 
 		//initialize spinner with listener and set spinner to the first post
 		initSpinnerType(getPosById(exercise.getTypeId(), idNameListTrainingType)); 
 
 	}
 	/**
 	 * Sets up the menubar, note the use of actionbarsherlock, making it possible of using
 	 * a menubar for APIs lower than 11
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.universal_menu, menu);
 		return true;
 	}
 
 	/**
 	 * This method initializes the type of training spinner, populating them with items and adds listener
 	 * @param position used for changing the default value due to the previous selection in a spinner
 	 */
 	public void initSpinnerType(int position)
 	{	
 		//Retrieves the view from .xml-file
 		spinnerType = (Spinner) findViewById(R.id.spinner_type_of_training); 
 		//Create ArrayAdapter, setting up the layout for spinner items and adding arraylist containing items
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listTrainingType);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); 
 		//Sets the adapter to the spinner
 		spinnerType.setAdapter(adapter); 
 
 		//Sets the spinner default value to selected value
 		spinnerType.setSelection(position);
 		//Adds listener to spinner spinnterType
 		spinnerType.setOnItemSelectedListener(this); 
 		//Calling setTexts to set the EditText-fields with data from the exercise-object
 		setTexts();
 
 	}
 	/**
 	 * This method initializes the spinners for selecting primary and secondary muscles, populating them with items and adds listener
 	 * 
 	 */
 	public void initSpinnerDynamicStatic()
 	{
 		listMuscles = getMusclesFromDb(); 
 
 		spinnerPMuscle = (Spinner) findViewById(R.id.spinner_primary_muscle); 
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listMuscles);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerPMuscle.setAdapter(adapter); //Sets the adapter to the spinner
 		spinnerPMuscle.setSelection(getPosById(exercise.getPri(), idNameListMuscles));
 
 		spinnerSMuscle = (Spinner) findViewById(R.id.spinner_secondary_muscle);
 		spinnerSMuscle.setAdapter(adapter);
 		spinnerSMuscle.setSelection(getPosById(exercise.getSec(), idNameListMuscles));
 	}
 	/**
 	 * This method initializes the spinner for selecting sport, populating them with items and adds listener
 	 * 
 	 */
 	public void initSpinnerCardio()
 	{
 		listSports = getSportsFromDb();
 		spinnerSport = (Spinner) findViewById(R.id.spinner_sport); //Retrieves the view from .xml-file
 		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, listSports);
 		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		spinnerSport.setAdapter(adapter); //Sets the adapter to the spinner
 		spinnerSport.setSelection(getPosById(exercise.getSportId(),idNameListSports));
 	}
 	/**
 	 * Callback method to be invoked when an item in this view has been selected
 	 */
 	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
 		if(!listTrainingType.get(position).equals(currentView)) //conditional if to prevent infinite loop
 		{
 
 			if(listTrainingType.get(position).equals("Static"))
 			{
 				//Switches the layout to the one that is used for static and dynamic
 				setContentView(R.layout.add_exercise_static);
 				//After switching view, adds listener to spinner again
 				initSpinnerType(position);
 				//Dynamic or static was selected, setting up the spinners for primary and secondary muscle
 				initSpinnerDynamicStatic();
 				//Sets variable currentView to prevent infinite loop
 				currentView = listTrainingType.get(position);	
 			}
 
 			else if(listTrainingType.get(position).equals("Dynamic"))
 			{
 				//Switches the layout to the one that is used for static and dynamic
 				setContentView(R.layout.add_exercise_static);	
 				//After switching view, adds listener to spinner again
 				initSpinnerType(position);
 				//Dynamic or static was selected, setting up the spinners for primary and secondary muscle
 				initSpinnerDynamicStatic();
 				//Sets variable currentView to prevent infinite loop
 				currentView = listTrainingType.get(position);
 			}
 			else if (listTrainingType.get(position).equals("Cardio"))
 			{
 				//Switches the layout to the one that is used for cardio
 				setContentView(R.layout.activity_add_exercise);	
 				//After switching view, adds listener to spinner again
 				initSpinnerType(position);
 				//Setting up the spinner for selecting sport
 				initSpinnerCardio();
 				//Sets variable currentView to prevent infinite loop
 				currentView=listTrainingType.get(position);
 			}
 		}
 	}
 	/**
 	 * Sets the title for this exercise, using the name for the exercise.
 	 * Sets the editTextFields for description and note to self, using data from exercise object
 	 */
 	public void setTexts()
 	{
 		setTitle(exercise.getName());
 
 		if((exercise.getNote() != null) && (exercise.getDesc() != null))
 		{
 			//Retrieves the view from .xml-file
 			comment = (EditText) findViewById(R.id.edit_comment);
 			//Retrieves the view from .xml-file
 			desc = (EditText) findViewById(R.id.edit_description);
 
 			//Sets the text for the editText-fields
 			comment.setText(exercise.getNote());
 			desc.setText(exercise.getDesc());
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
 		//Gets and puts the added/changed data into the exercise object
 		putExerciseData();
 		//creates a EditExerciseDbHandler
 		EditExerciseDbHandler dbHandler = new EditExerciseDbHandler(this);
 		dbHandler.open();
 		//Push the changed exercise-object int the database
 		dbHandler.editExercise(exercise);
 		dbHandler.close();
 		//Kills this activity
 
 		finish();
 	}
 	/**
 	 * <p>Callback method to be invoked when the </p><i>cancel-button</i><p> has been clicked, making the activity
 	 * return the the previous</p><i> Exercise.java</i>
 	 */
 	public void cancel(View view)
 	{
 		//Kills this activity
 		finish();
 	}
 	/**
 	 * Retrives a List<IdName> of musclesfrom the database and adds the names of the muscles to an ArrayList<String>
 	 * @return ArrayList that spinners need to populate with items
 	 */
 	private ArrayList<String> getMusclesFromDb()
 	{
 		//creates a EditExerciseDbHandler
 		EditExerciseDbHandler get = new EditExerciseDbHandler(this);
 		get.open();
 		//puts the list from database into a local list
 		idNameListMuscles = get.getMuscles();
 		get.close();
 		ArrayList<String> getIdName = new ArrayList<String>();
 		//for-each idname in idNameListMuscles
 		for (IdName idname : idNameListMuscles)
 		{	
 			//adds the name if the IdName-object into the arraylist
 			getIdName.add(idname.getName());
 		}
 		return getIdName;
 	}
 	/**
 	 * Retrives a List<IdName> of sports from the database and adds the names of the muscles to an ArrayList<String>
 	 * @return ArrayList that spinners need to populate with items
 	 */
 	private ArrayList<String> getSportsFromDb()
 	{
 		EditExerciseDbHandler get = new EditExerciseDbHandler(this);
 		get.open();
 		idNameListSports = get.getSports();
 		get.close();
 		ArrayList<String> getIdName = new ArrayList<String>();
 		for (IdName idname : idNameListSports)
 		{
 			getIdName.add(idname.getName());
 		}
 		return getIdName;
 	}
 
 	/**
 	 * Retrives a List<IdName> of training types from the database and adds the names of the muscles to an ArrayList<String>
 	 * @return ArrayList that spinners need to populate with items
 	 */
 	private ArrayList<String> getExerciseTypesFromDb()
 	{
 
 		EditExerciseDbHandler get = new EditExerciseDbHandler(this);
 		get.open();
 		idNameListTrainingType = get.getExerciseTypes();
 		get.close();
 		ArrayList<String> getIdName = new ArrayList<String>();
 		for (IdName idname : idNameListTrainingType)
 		{
 			getIdName.add(idname.getName());
 		}
 		return getIdName;
 	}
 
 	/**
 	 * Method for retrieving the exercise selected from database, using the 
 	 * ID from the intent that start the activity
 	 */
 	private void getExerciseData()
 	{
 		//creates a EditExerciseDbHandler
 		EditExerciseDbHandler get = new EditExerciseDbHandler(this);
 		get.open();
 		//retrieves the exercise-object
 		exercise = get.getExerciseById(exerciseId);
 		get.close();
 	}
 
 	/**
 	 * Method for putting data that has been changed/added to this exercise object
 	 */
 	private void putExerciseData()
 	{
 		//needed, otherwhise the toString will return null
 		comment = (EditText) findViewById(R.id.edit_comment); 
 		desc = (EditText) findViewById(R.id.edit_description);
 
 		//Puts description into the exercise
 		exercise.putDesc( (String)desc.getText().toString());
 		//Puts note to self into the exercise
 		exercise.putNote( (String)comment.getText().toString());
 		//Puts the training type selected from spinner into the exercise
 		exercise.putTypeId(idNameListTrainingType.get((Integer)spinnerType.getSelectedItemPosition()).getId());
 
 		//If the spinners for primary and secondary muscle havent been drawed yet, there is no data to retrieve
 		if (!currentView.equals("Cardio"))
 		{
 			//puts the primary muscle selected in spinner into the exercise
 			exercise.putPri(idNameListMuscles.get((Integer)spinnerPMuscle.getSelectedItemPosition()).getId());
 			//puts the secondary muscle selected in spinner into the exercise
 			exercise.putSec(idNameListMuscles.get((Integer)spinnerSMuscle.getSelectedItemPosition()).getId());
 		}
 		//If the spinner for sport havent been drawed yet, there is no data to retrieve
 		else
 			//puts the sport selected in spinner into the exercise
 			exercise.putSport(idNameListSports.get((Integer)spinnerSport.getSelectedItemPosition()).getId());
 	}
 	/**
 	 * The database uses a unique id for every items in its list, this method simply retrieves the position in a 
 	 * local list that holds this id
 	 * @param id Id for the IdName-object which position we want
 	 * @param list List that holds the idname-objects 
 	 * @return Position in the list wich holds the specified idname
 	 */
 	private int getPosById(int id, List<IdName> list)
 	{
 		int pos = 0;
 		//for-each idname in list
 		for (IdName idName : list)
 		{	
 			//When id is found, return position
 			if(idName.getId()==id)
 			{
 				return pos; 
 			}
 			//Not found, increment position
 			pos++;
 		}
 		return 0;
 	}
 
 }
