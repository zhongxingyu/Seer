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
 
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.ListView;
 
 import com.Grupp01.gymapp.MainActivity;
 import com.Grupp01.gymapp.R;
 import com.actionbarsherlock.app.SherlockListActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
 
 
 
 /**
  * This activity will list all performed workouts with name and date.
  * By pressing a workout a new activity will start and show performed exercises
  * for that workout.
  * 
  * @author GivDev
  * @version 0.1
  * @peer by Robert Blomberg
  * @date 
  *
  */
 public class ListHistoryActivity extends SherlockListActivity {
 
 	//The list with History objects, will be fetched from database.
 	private LinkedList<History> hList = new LinkedList<History>();
 	
 	/**
 	 * Is called on start, set the correct layout from xml file and then create the list.
 	 * 
 	 */
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_list_history);
         createList();
         
     }
 
 	
 	/**
 	 * This method will fetch data from the database in form a LinkedList, then pass
 	 * this list on to our custom adapter to build a listview out of data from database.
 	 * 
 	 */
     private void createList() {
     	
     	//Dummy rows which will add a few entries to the list, will be fetch from database.
     	hList.add(new History("Test 1", "2009-01-01"));
 		hList.add(new History("Test 2", "2009-01-02"));
 		hList.add(new History("Test 3", "2009-01-03"));
 		hList.add(new History("Test 4", "2009-01-04"));
 		hList.add(new History("Test 5", "2009-01-05"));
 		hList.add(new History("Test 6", "2009-01-06"));
 		hList.add(new History("Test 7", "2009-01-07"));
 		hList.add(new History("Test 8", "2009-01-08"));
 		
 		//Set up the adapter we will use to adapt the layout.
 		HistoryAdapter hAdapter = new HistoryAdapter(this, R.layout.history_list_layout, hList);
 		setListAdapter(hAdapter);
 	}
 
     
     /**
      * Set up the layout of actionbar
      * @param menu The menu to pass the layout to.
      * @return true to set the menu to be visible.
      */
 	@Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	//Set the layout xml 
 		getSupportMenuInflater().inflate(R.menu.activity_list_history, menu);
 		
 		//Set the home button in actionbar to be clickable so the user can return to 
 		//main menu by pressing it.
     	getSupportActionBar().setHomeButtonEnabled(true);
     	
     	//Return true, otherwise the menu will not be visible.
         return true;
     }
 	
 	
 	/**
 	 * This method will be called when the user presses an item in the ListView of history objects.
 	 * The method will check what position was clicked, check the object in LinkedList for that position
 	 * and start a new activity which will display exercises for that History object.
 	 */
 	@Override
 	protected void onListItemClick(ListView l, View v, int position, long id){
 		//Get the history object that was clicked
 		History h = (History) getListAdapter().getItem(position);
 		
 		//Start a new activity that will display performed exercises for that history
 		Intent intent = new Intent(this, ShowSingleHistoryActivity.class);
 		
 		//To let the activity open corrent history object we pass on the database ID 
 		//to the new activity.
 		//This is just a dummy row until we use the correct history object from database.
 		intent.putExtra("id", h.getName());
 		
 		//Start activity.
 		startActivity(intent);
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
