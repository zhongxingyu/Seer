 package edu.mit.printAtMIT.main;
 
 import com.parse.Parse;
 
 import edu.mit.printAtMIT.R;
 import edu.mit.printAtMIT.PrintAtMITActivity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.text.InputType;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.inputmethod.EditorInfo;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 
 
 import java.util.ArrayList;
 import edu.mit.printAtMIT.list.*;
 
 /**
  * Users can reset their kerberos ID here
  * 
  * Menu Items:
  *      Home
  */
 public class SettingsActivity extends ListActivity {
 	
 	ArrayList<Item> items = new ArrayList<Item>();
 	private static final int ITEM_USERNAME = 1;
 	private static final int ITEM_INKCOLOR = 3;
 	private static final int ITEM_COPIES = 4;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         SharedPreferences userSettings = getSharedPreferences(PrintAtMITActivity.PREFS_NAME, MODE_PRIVATE);
         items.add(new SectionItem("User info"));
         items.add(new EntryItem("Change Kerberos Id", userSettings.getString(PrintAtMITActivity.USERNAME, ""), ITEM_USERNAME));
         
         items.add(new SectionItem("Printer Preferences"));
         items.add(new EntryItem("Ink Color", userSettings.getString(PrintAtMITActivity.INKCOLOR, PrintAtMITActivity.BLACKWHITE), ITEM_INKCOLOR));
         items.add(new EntryItem("Copies", ""+userSettings.getInt(PrintAtMITActivity.COPIES, 1), ITEM_COPIES));
         
         EntryAdapter adapter = new EntryAdapter(this, items);
         
         setListAdapter(adapter);
     }
     
     @Override
     public void onConfigurationChanged(Configuration newConfig){
         super.onConfigurationChanged(newConfig);
     }
     
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.settings_menu, menu);
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         // Handle item selection
         switch (item.getItemId()) {
         case R.id.home:
         	Intent intent = new Intent(findViewById(android.R.id.content).getContext(), MainMenuActivity.class);
         	startActivity(intent);
             return true;
         case R.id.about:
         	Dialog dialog = new Dialog(this);
 
 			dialog.setContentView(R.layout.about_dialog);
 			dialog.setTitle("About");
 			dialog.show();
             super.onOptionsItemSelected(item);
             return true;
         default:
             return super.onOptionsItemSelected(item);
         }
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
                                     ContextMenuInfo menuInfo) {
       super.onCreateContextMenu(menu, v, menuInfo);
       menu.setHeaderTitle("Ink Color Settings");
       MenuInflater inflater = getMenuInflater();
       inflater.inflate(R.menu.inkcolor_menu, menu);
       //checks the correct option based on saved user preference
       //default is black and white
 	    SharedPreferences userSettings = getSharedPreferences(PrintAtMITActivity.PREFS_NAME, MODE_PRIVATE);
 	    String color = userSettings.getString(PrintAtMITActivity.INKCOLOR, PrintAtMITActivity.BLACKWHITE);
 	    if (color.equals(PrintAtMITActivity.COLOR)) {
 	    	MenuItem item = (MenuItem) menu.findItem(R.id.color);
 	    	item.setChecked(true);
 	    }
 	    else {
 	    	MenuItem item = (MenuItem) menu.findItem(R.id.bw);
 	    	item.setChecked(true);
 	    }
     }
     
     @Override
     public boolean onContextItemSelected(MenuItem item)
     {
     	SharedPreferences userSettings = getSharedPreferences(PrintAtMITActivity.PREFS_NAME, MODE_PRIVATE);
     	SharedPreferences.Editor editor = userSettings.edit();
     	EntryAdapter adapter;
     	//changes user preference based on what user has selected
        switch(item.getItemId())
        {
        case R.id.bw:
     	   editor.putString(PrintAtMITActivity.INKCOLOR, PrintAtMITActivity.BLACKWHITE);
     	   items.set(ITEM_INKCOLOR, new EntryItem("Ink Color", PrintAtMITActivity.BLACKWHITE, ITEM_INKCOLOR));
     	   editor.commit();
            adapter = new EntryAdapter(this, items);
            setListAdapter(adapter);
           return true;
        case R.id.color:
           editor.putString(PrintAtMITActivity.INKCOLOR, PrintAtMITActivity.COLOR);
           items.set(ITEM_INKCOLOR, new EntryItem("Ink Color", PrintAtMITActivity.COLOR, ITEM_INKCOLOR));
           editor.commit();
           adapter = new EntryAdapter(this, items);
           setListAdapter(adapter);
           return true;
        default:
           return super.onContextItemSelected(item);
        }
     }
     
     @Override
     protected void onListItemClick(ListView l, View v, int position, long id) {
     	
     	if(!items.get(position).isSection()){
     		
     		switch(position) {
     		//popup dialog appears for username
     		//saves user-inputted username
     		case ITEM_USERNAME:
     			final Dialog dialog = new Dialog(this);
 
     			dialog.setContentView(R.layout.username_dialog);
     			dialog.setTitle("Change Kerberos Id");
     			dialog.show();
     			
     			Button saveButton = (Button) dialog.findViewById(R.id.save);
     			EditText textfield = (EditText) dialog.findViewById(R.id.change_username);
     			textfield.setImeOptions(EditorInfo.IME_ACTION_DONE);
     	        
     	        saveButton.setOnClickListener(new View.OnClickListener() {
 
     	            public void onClick(View view) {
     	            	SharedPreferences userSettings = getSharedPreferences(PrintAtMITActivity.PREFS_NAME, MODE_PRIVATE);
     	            	EditText textfield = (EditText) dialog.findViewById(R.id.change_username);
     	            	SharedPreferences.Editor editor = userSettings.edit();
     	                editor.putString(PrintAtMITActivity.USERNAME, textfield.getText().toString());
     	                editor.commit();
     	      
     	                items.set(ITEM_USERNAME, new EntryItem("Change Kerberos Id", textfield.getText().toString(), ITEM_USERNAME));
     	                EntryAdapter adapter = new EntryAdapter(view.getContext(), items);
     	                setListAdapter(adapter);
     	                dialog.dismiss();
     	            }
     	        });
     	        
         		return;
         	//context menu appears for ink color
     		case ITEM_INKCOLOR: 
     			registerForContextMenu( v ); 
     		    v.setLongClickable(false); 
     		    this.openContextMenu(v);
     		   
     			break;
     		case ITEM_COPIES:
     			final View view = v;
     			
     			final SharedPreferences userSettings = getSharedPreferences(PrintAtMITActivity.PREFS_NAME, MODE_PRIVATE);
       		  	final SharedPreferences.Editor editor = userSettings.edit();
       		  
                 final EditText copy = new EditText(this);
                 copy.setInputType(InputType.TYPE_CLASS_NUMBER);
                 
         		AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
         		builder.setMessage("Number of copies:")
           	         .setCancelable(false)
           	         .setView(copy)
           	         .setPositiveButton("Save", new DialogInterface.OnClickListener() {
           	             public void onClick(DialogInterface dialog, int id) {
           	            	 
 	        	    		 int copies = userSettings.getInt(PrintAtMITActivity.COPIES, 1);
 	        	    		 String text = copy.getText().toString();
 	        	        	 copies = (text.equals("") || text.equals("0")) ? copies : Integer.parseInt(copy.getText().toString());
 	        	             
 	        	        	 editor.putInt(PrintAtMITActivity.COPIES, copies);
 	        	          	 editor.commit();
           	       	      
            	                items.set(ITEM_COPIES, new EntryItem("Copies", "" + copies, ITEM_COPIES));
 
            	                EntryAdapter adapter = new EntryAdapter(view.getContext(), items);
            	                setListAdapter(adapter);
            	                
            	                dialog.dismiss();
           	             }
           	         })
           	         .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
           	             public void onClick(DialogInterface dialog, int id) {
           	                  dialog.cancel();
           	             }
           	         });
           	  AlertDialog alert = builder.create();
           	  alert.show();
           	  break;
     		default: Toast.makeText(this, "herp derp", Toast.LENGTH_SHORT).show(); break;
     		}
     		
     	}
     	
     	super.onListItemClick(l, v, position, id);
     }
 }
