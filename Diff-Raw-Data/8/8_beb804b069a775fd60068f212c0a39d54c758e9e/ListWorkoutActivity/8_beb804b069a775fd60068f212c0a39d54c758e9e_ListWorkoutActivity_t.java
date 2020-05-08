 /*Copyright  2012 GivDev
  *
  * This file is part of Gymapp.
  *
  * Gymapp is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Gymapp is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Gymapp. If not, see <http://www.gnu.org/licenses/>.
  *
  */
 package com.Grupp01.gymapp.View.Workout;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 import com.Grupp01.gymapp.MainActivity;
 import com.Grupp01.gymapp.R;
 import com.Grupp01.gymapp.Controller.IdName;
 import com.Grupp01.gymapp.Controller.Workout.WorkoutDbHandler;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 import com.actionbarsherlock.view.MenuItem;
 
 
 /**
  * @author GivDev
  * @version 0.1
  * @peer reviewed by
  * @date 04/10/12
  *
  * Class ListWorkoutActivity will show all workout routines that the user has created,
  *
  * <p>This class i a part of the </p><i>View</i><p> package, and a part of the </p><i>Workout</i>
  * <p> Subpackage</p>
  *
  */
 public class ListWorkoutActivity extends SherlockActivity {
 
 
 	public final static String WORKOUT_NAME = "com.Grupp01.gymapp.WORKOUT.NAME";
 	public final static String WORKOUT_ID = "com.Grupp01.gymapp.WORKOUT.ID";
 	private ListView mainListView ; //This is the listview where the list of all workouts will be shown
 	private ArrayAdapter<String> listAdapter ; //Adapter used for the list
 	private List<IdName> idNameList;
 
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.list_workout);
 		createWorkoutList();
 	}
 
 
 	/**
 	 * Set up the actionbar (layout xml and title)
 	 * @param Menu the actionbar menu
 	 * @return true to make the menu visible
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getSupportMenuInflater();
 		inflater.inflate(R.menu.list_workout, menu);
 		//Set the App icon to work as a button in the actionbar
 		getSupportActionBar().setHomeButtonEnabled(true);
 		return true;
 	}
 
 
 	/**
 	 * Set up actions for buttons in actionbar
 	 * @param MenuItem item - The menuitem thas has been pressed
 	 *
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item){
 		switch (item.getItemId()){
 		//Make app icon navigate back to the applications start screen.
 		case	android.R.id.home:
 			Intent intent = new Intent(this, MainActivity.class);
 			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
 			startActivity(intent);
 			return true;
 
 
 			//when clicking "add workout" a dialog pops up with input for the name
 		case	R.id.menu_addWorkout:
 			openDialog();
 
 
 		default:
 			return super.onOptionsItemSelected(item);
 		}
 	}
 
 
 
 	/**
 	 * This method creates the menu for longclicking a workout in the list
 	 * @param menu, v & menuInfo
 	 */
 	@Override
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 
 		menu.add(Menu.NONE, 0, 0, "Edit");
 		menu.add(Menu.NONE, 1, 1, "Delete");
 
 	}
	@Override
	public void onResume()
	{
		super.onResume();
		createWorkoutList();
 
		
	}
 	/**
 	 * Is called when any of the options in the context menu is clicked.
 	 * This method will make the selected action.
 	 * @param item The context menu item that was clicked
 	 */
 	@Override
 	public boolean onContextItemSelected(android.view.MenuItem item) {
 		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
 		int menuItemIndex = item.getItemId();
 		String workoutName = mainListView.getAdapter().getItem(info.position).toString();
 		switch(menuItemIndex){
 		case 0:
 			editWorkouts(workoutName);
 			return true;
 		case 1:
 
 			deleteWorkout(workoutName);
 			return true;
 		}
 		return true;
 	}
 
 
 	/**
 	 * Is called from onCreate to build the list with all workouts and
 	 * set correct actions for onClick and also context menus for longclicking.
 	 *
 	 */
 	private void createWorkoutList() {
 		mainListView = (ListView) findViewById( R.id.ListViewWorkouts );
 		ArrayList<String> arrayWorkouts = new ArrayList<String>();
 
 
 		//Add all the strings from stringarray to the ArrayList
 
 		WorkoutDbHandler dbHandler = new WorkoutDbHandler(this);
 		dbHandler.open();
 		idNameList = dbHandler.getWorkoutsIdName();
 		dbHandler.close();
 
 		for(IdName temp: idNameList)
 		{
 			arrayWorkouts.add(temp.getName());
 
 		}
 
 		//Set the listview layout with strings in array
 		listAdapter = new ArrayAdapter<String>(this, R.layout.list_simple_row, arrayWorkouts);
 		mainListView.setAdapter(listAdapter);
 
 		//Activate longclick menu in the list
 		registerForContextMenu(mainListView);
 
 		//Set each row in the list clickable and fetch the title of the workout
 		//to be able to open correct workout in WorkoutActivity
 		mainListView.setOnItemClickListener(new OnItemClickListener() {
 			public void onItemClick(AdapterView<?> arg0, View v, int position,
 					long id) {
 
 				//Get the text label of the row that has been clicked (will be used to open the correct workout)
 				int workoutId = idNameList.get(position).getId();
 				//String workoutName = mainListView.getAdapter().getItem(position).toString();
 				startNewWorkout(workoutId);
 
 
 			}// end of onItemClick
 		});// end of setOnItemClickListener
 
 	}
 
 
 	/**
 	 * Is called with the user selects to create a new workout (clicking the "add workout" button
 	 * in ActionBar.
 	 * @param workoutName The name for the new workout.
 	 *
 	 */
 	private void startNewWorkout(int workoutId) {
 		Intent workout = new Intent(ListWorkoutActivity.this, com.Grupp01.gymapp.View.Workout.WorkoutActivity.class);
 		workout.putExtra(WORKOUT_ID, workoutId);
 		startActivity(workout);
 	}
 
 
 
 
 	/**
 	 * Is called from contextMenu when the user longclicks a workout and selects
 	 * "Delete" from the menu.
 	 * @param The name of the workout
 	 */
 	private void deleteWorkout(String workoutName) {
 
 		//Show a confirmation dialog before deleting
 		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
 		deleteDialog.setMessage("Are you sure you want to delete workout " + workoutName +"?");
 		deleteDialog.setCancelable(false);
 
 		//Set action for clicking "Yes" (the user wants to delete)
 		deleteDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 
 				Toast.makeText(ListWorkoutActivity.this, "Not implementet yet!", Toast.LENGTH_SHORT).show();
 			} //End of onclick method
 		}	//end of DialogInterface
 				);	//End of setPositiveButton
 
 		//Set action for choosing not to delete (the dialog just closes and no action is taken)
 		deleteDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
 			public void onClick(DialogInterface dialog, int id) {
 				dialog.cancel();
 			} //End of onclick method
 		}	//end of newDialogInterface
 				);	//End of setPositiveButton
 
 		//Show the dialog
 		AlertDialog alert = deleteDialog.create();
 		alert.show();
 	}
 
 
 
 	/**
 	 * Is called from contextMenu when the user longclicks a workout and selects
 	 * "Edit" from the menu. Will start EditWorkout to add/remove exercises.
 	 * @param workoutName The name of the workout
 	 */
 	private void editWorkouts(String workoutName) {
 		Intent intent = new Intent(this, com.Grupp01.gymapp.View.Workout.EditWorkoutActivity.class);
 		intent.putExtra(WORKOUT_NAME, workoutName);
 		startActivity(intent);	
 
 	}
 
 	/**
 	 * Is called from when a user clicks the "Add workout". Will open a dialog which ask
 	 * the person to write a name for the workout. When the user clicks "Add workout" in
 	 * the dialog the activity "EditWorkout" starts where you can add/remove exercises.
 	 */
 	private void openDialog()
 	{
 		//Variables for the dialog
 		final AlertDialog.Builder addWorkout_Dialog = new AlertDialog.Builder(this);
 		final EditText editText_Dialog = new EditText(this);
 		final Intent intent2 = new Intent(this, com.Grupp01.gymapp.View.Workout.EditWorkoutActivity.class);
 
 		addWorkout_Dialog.setMessage("Enter name of Workout:");
 		addWorkout_Dialog.setView(editText_Dialog);
 
 
 		//If pressing "Add Workout" on the dialog
 		addWorkout_Dialog.setPositiveButton("Add Workout", new DialogInterface.OnClickListener()
 		{
 
 			/** When the user clicked "Add workout"
 			 * go to EditWorkout
 			 * if nothing is inputed, the dialog closes and
 			 * the user is returned to ListWorkout
 			 * */
 			public void onClick(DialogInterface dialog, int whichButton)
 			{
 				String workoutName = editText_Dialog.getText().toString();
 
 				if(workoutName.trim().equals(""))
 				{
 					dialog.cancel();
 					return;
 				}
 				//Add the name of the workout to the intent so the next activity can get the name
 				newWorkoutToDatabase(workoutName);
 				intent2.putExtra(WORKOUT_NAME, workoutName);
 				startActivity(intent2);
 
 			}
 		});
 		//If pressing "Cancel" on the dialog
 		addWorkout_Dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
 		{
 
 			/** Close dialog if the user press cancel*/
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton)
 			{
 				dialog.cancel();
 			}
 		});
 		addWorkout_Dialog.show();
 	}
 
 	/**
 	 * Put the new Workout to database. Only the WorkoutName is put to database
 	 * @param workoutName
 	 */
 	private void newWorkoutToDatabase(String workoutName)
 	{
 		WorkoutDbHandler dbHandler = new WorkoutDbHandler(this);
 		dbHandler.open();
 		dbHandler.putNewWorkout(workoutName);
 		dbHandler.close();
 	}
 }
