 /*	Copyright  2012 GivDev
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
 */
 package com.Grupp01.gymapp.View.History;
 
 import java.util.LinkedList;
 
 import com.Grupp01.gymapp.MainActivity;
 import com.Grupp01.gymapp.R;
import com.Grupp01.gymapp.View.Workout.EditWorkoutActivity;
import com.Grupp01.gymapp.View.Workout.ListWorkoutActivity;
 import com.actionbarsherlock.app.SherlockListActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuItem;
 
import android.os.Bundle;
import android.content.Intent;

 
 /**
  * This activity is started when user clicks on a row in the ListHistoryActivity.
  * It will display performed exercises for the choosen workout.
  * 
  * @author GivDev
  * @version 0.1
  * @peer by Robert Blomberg
  * @date 
  *
  */
 public class ShowSingleHistoryActivity extends SherlockListActivity {
 	
 	//THis is the list with every performed exercise, will be fetched from the database.
 	private LinkedList<History> hList = new LinkedList<History>();
 	
 	//This is the adapter we use to adapt the list to the layout.
 	
 	
 	/**
 	 * Is called on start, set the correct layout from xml file and then create the list.
 	 * 
 	 */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         //Set the layout for the activity
         setContentView(R.layout.activity_show_single_history);
         
         //Get the intent and ID which was passed as Extra.
         Intent intent = getIntent();
         
         //This row is just dummy until database connection works. ID will be used to 
         //fetch exercises for performed workout with this id.
         setTitle(intent.getStringExtra("id"));
         
         //create the list.
         createList();
     }
 
 
     /**
      * Set up the layout of actionbar
      * @param menu The menu to pass the layout to.
      * @return true to set the menu to be visible.
      */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
 		//Set the layout xml 
         getSupportMenuInflater().inflate(R.menu.activity_show_single_history, menu);
         
         //Set the home button in actionbar to be clickable so the user can return to 
       	//main menu by pressing it.
         getSupportActionBar().setHomeButtonEnabled(true);
         
         //Return true, otherwise the menu will not be visible.
         return true;
     }
 	
 	
 	/**
 	 * This method will fetch data from the database in form a LinkedList, then pass
 	 * this list on to our custom adapter to build a listview out of data from database.
 	 * 
 	 */
 	private void createList() {
 		
 		//Dummy list until database connection works
 		hList.add(new History("vning 1", "10 kg"));
 		hList.add(new History("vning 2", "20 kg"));
 		hList.add(new History("vning 3", "30 kg"));
 		hList.add(new History("vning 4", "40 kg"));
 		hList.add(new History("vning 5", "50 kg"));
 		hList.add(new History("vning 6", "60 kg"));
 		hList.add(new History("vning 7", "70 kg"));
 		hList.add(new History("vning 8", "80 kg"));
 		
 		//Set up the adapter we use to create the listview.
 		HistoryAdapter hAdapter = new HistoryAdapter(this, R.layout.history_list_layout, hList);
 		setListAdapter(hAdapter);
 		
 	}
 	
 	
 	/**
      * Set up actions for buttons in actionbar
      * @param MenuItem item - The menuitem thas has been pressed
      * 
      */
     public boolean onOptionsItemSelected(MenuItem item){
     	switch (item.getItemId()){
     		//Make app icon navigate back to the applications start screen.
     		case	android.R.id.home:
     			Intent intent = new Intent(this, MainActivity.class);
     			
     			//Remove "newer" activities from the stack.
     			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
     			
     			//Start activity
     			startActivity(intent);
     			return true;
     		default:
     			return super.onOptionsItemSelected(item);
     	}
     }
 }
