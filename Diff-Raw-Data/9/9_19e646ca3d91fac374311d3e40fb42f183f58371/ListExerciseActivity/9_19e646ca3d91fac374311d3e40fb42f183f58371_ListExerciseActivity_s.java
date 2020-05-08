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
 
 import java.util.ArrayList;
 
 import android.app.Dialog;
 import android.content.Intent;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 
 import com.Grupp01.gymapp.R;
 import com.actionbarsherlock.app.SherlockActivity;
 import com.actionbarsherlock.view.Menu;
 import com.actionbarsherlock.view.MenuInflater;
 /** 
  * @author GivDev
  * @version 0.1
  * @peer reviewed by
  * @date dd/mm/yy
 */
public class Exercise extends SherlockActivity implements OnClickListener, OnItemClickListener {
 	
 	/** Instansvariabler */
 	public final static String EXTRA_EXERCISE_NAME = "com.Grupp01.gymapp.message";
 	private Dialog dialog;
 	private ArrayList<String> listElements;
 	private ArrayAdapter<String> elementAdapter;
 
 	/**Setups the class layout and some instans variables
 	 * @param savedInstanceState
 	*/
 	@Override
     public void onCreate(Bundle savedInstanceState)
 	{
         super.onCreate(savedInstanceState);
         setContentView(R.layout.exercise);
         
         dialog = new Dialog(this);
     	dialog.setContentView(R.layout.dialog);
     	dialog.setTitle("New Exercise");
     	
         ((Button) dialog.findViewById(R.id.add_Button)).setOnClickListener(this);
     	((Button) dialog.findViewById(R.id.cancel_Button)).setOnClickListener(this);
     	
     	listElements = new ArrayList<String>();
     	
     	elementAdapter = new ArrayAdapter<String>(this, R.layout.thelist_row, listElements);
     	((ListView)findViewById(R.id.theList)).setAdapter(elementAdapter);
     	((ListView)findViewById(R.id.theList)).setOnItemClickListener(this);
     	
     	setTitle("Exercise");
         createListView();
     }
 
 	/**Setups the menu of the class
 	 * @param menu
 	 * @return true = menu shown false = menu hidden
 	*/
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
     	MenuInflater inflater = getSupportMenuInflater();
     	inflater.inflate(R.menu.ovningar, menu);
     	getSupportActionBar().setHomeButtonEnabled(true);
         return true;
     }
     
     /**Skapar en lista i listViewn
      * anvnds nu nr vi inte har koppla ihop med databasen */
     public void createListView()
     {	
     	listElements.add("ett");
     	listElements.add("tv");
     	listElements.add("tre");
     	listElements.add("fyra");
     	listElements.add("fem");
     	listElements.add("sex");
     	listElements.add("sju");
     }
 
     //lyssnar metoderna brjar hr
     /** The method listens to the home button and to add a new exercise button in the menu
 	 * @param item the item that has been clicked
 	 * @return true
 	*/     
 	@Override
 	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item)
 	{
 		if(item.getItemId() == R.id.menu_addExe)
 		{
 	    	dialog.show();
 		}
 		else if(item.getItemId() == android.R.id.home)
 		{
 			//Taget frn developer.android.com
 			Intent parentActivityIntent = new Intent(this, MainActivity.class);
             parentActivityIntent.addFlags(
                     Intent.FLAG_ACTIVITY_CLEAR_TOP |
                     Intent.FLAG_ACTIVITY_NEW_TASK);
             startActivity(parentActivityIntent);
             finish();
 
 		}
 		return true;
 	}
 
 	/** Listens to the add and cancel button in the dialog
 	 * @param view the view that is clicked
 	*/
 	@Override
 	public void onClick(View view)
 	{
 		if(view == ((Button) dialog.findViewById(R.id.add_Button)))
 		{
 			EditText name = (EditText) dialog.findViewById(R.id.exerciseName);
 			String temp = name.getText().toString();
 			if(temp.length() == 0)
 			{
 				name.setHint("Fyll i ett namn");
 				name.setHintTextColor(Color.RED);
 			}
 			else
 			{
 				Intent intent_Add_Exercise = new Intent(this, AddExercise.class);
 				intent_Add_Exercise.putExtra(EXTRA_EXERCISE_NAME, temp);
 				dialog.dismiss();
 				startActivity(intent_Add_Exercise);
 			}
 		}
 		else if(view == ((Button) dialog.findViewById(R.id.cancel_Button)))
 		{
 			dialog.dismiss();
 		}
 	}
 
 	/**Listens to the listview were all the exercises are listed
 	 * @param adapter
 	 * @param view
 	 * @param n the position of the clicked element
 	 * @param t
 	*/
 	@Override
 	public void onItemClick(AdapterView<?> adapt, View view, int n, long t)
 	{
 		//listElements.add("tta");
 		//elementAdapter.notifyDataSetChanged();
 		String exercise_Name = ((ListView)findViewById(R.id.theList)).getItemAtPosition(n).toString();
 		Intent intent_View_Exercise = new Intent(this, AddExercise.class);
 		intent_View_Exercise.putExtra(EXTRA_EXERCISE_NAME, exercise_Name);
 		startActivity(intent_View_Exercise);
 	}
 }
