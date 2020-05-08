 package edu.mit.printAtMIT.print;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.ListActivity;
 import android.app.ProgressDialog;
 import android.content.DialogInterface;
 import android.content.SharedPreferences;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.InputType;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.TextView;
 import android.widget.Toast;
 import edu.mit.printAtMIT.PrintAtMITActivity;
 import edu.mit.printAtMIT.R;
 import edu.mit.printAtMIT.list.ButtonItem;
 import edu.mit.printAtMIT.list.EntryAdapter;
 import edu.mit.printAtMIT.list.EntryItem;
 import edu.mit.printAtMIT.list.Item;
 import edu.mit.printAtMIT.list.SectionItem;
 
 /**
  * User selects print options:
  *      bw/color
  *      copies
  * Print button
  * 
  * Menu Items:
  *      Settings
  *      About     
  */
 
 public class PrintOptionsActivity extends ListActivity {
     PrintTask printTask;
     String fileLoc;
     String fileName;
     String queue;
     String userName;
     
     //public static Button btnStart;
     
     final String hostName = "mitprint.mit.edu";
 
     ArrayList<Item> items = new ArrayList<Item>();
 	private static final int ITEM_FILENAME = 1;
 	private static final int ITEM_INKCOLOR = 3;
 	private static final int ITEM_COPIES = 4;
 	private static final int ITEM_PRINT_BUTTON = 5;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         //setContentView(R.layout.print_options);
         // Show file chosen -- need to add options (bw/color, copies, change print name), then print
         Bundle extras = getIntent().getExtras(); 
         fileLoc = extras.getString("fileLoc");
         fileName = extras.getString("fileName");
         //Toast.makeText(getApplicationContext(), fileLoc, Toast.LENGTH_SHORT).show();
         //Toast.makeText(getApplicationContext(), fileName, Toast.LENGTH_SHORT).show();
         
         SharedPreferences userSettings = getSharedPreferences(PrintAtMITActivity.PREFS_NAME, MODE_PRIVATE);
         userName = userSettings.getString(PrintAtMITActivity.USERNAME, "");
         if (userSettings.getString(PrintAtMITActivity.INKCOLOR, PrintAtMITActivity.BLACKWHITE).equals("Color"))
         	queue = "color";
         else
         	queue = "bw";
         //Toast.makeText(getApplicationContext(), fileLoc + userName + hostName + queue, Toast.LENGTH_SHORT).show();
 
         
         /*TextView text = (TextView) findViewById(R.id.list_item_entry_title);
         text.setText(fileName);
         TextView text2 = (TextView) findViewById(R.id.list_item_entry_summary);
         text.setText(fileLoc);  */
         items.add(new SectionItem("File name"));
         items.add(new EntryItem(fileName, fileLoc, ITEM_FILENAME));
         
         items.add(new SectionItem("Printer Preferences"));
         items.add(new EntryItem("Ink Color", userSettings.getString(PrintAtMITActivity.INKCOLOR, PrintAtMITActivity.BLACKWHITE), ITEM_INKCOLOR));
         items.add(new EntryItem("Copies", ""+userSettings.getInt(PrintAtMITActivity.COPIES, 1), ITEM_COPIES));
         
         items.add(new ButtonItem("Print", ITEM_PRINT_BUTTON));
         //items.add(new EntryItem("Print", "", ITEM_PRINT_BUTTON));
         EntryAdapter adapter = new EntryAdapter(this, items);
         
         setListAdapter(adapter);
         
         //this.getLayoutInflater().inflate(R.layout.print_options, getListView());
         
         //btnStart = (Button) (this.getListView(). findViewById(R.id.printbutton));
 
         //btnStart.setOnClickListener(btnStartListener);
         
         //textStatus = (TextView)findViewById(R.id.textStatus);
     }
     
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
                                     ContextMenuInfo menuInfo) {
       super.onCreateContextMenu(menu, v, menuInfo);
      Toast.makeText(this, "" + v.getId(), Toast.LENGTH_SHORT).show();
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
     	   queue = "bw";
    	   editor.putString(PrintAtMITActivity.INKCOLOR, PrintAtMITActivity.BLACKWHITE);
     	   items.set(ITEM_INKCOLOR, new EntryItem("Ink Color", PrintAtMITActivity.BLACKWHITE, ITEM_INKCOLOR));
    	   editor.commit();
            adapter = new EntryAdapter(this, items);
            setListAdapter(adapter);
           return true;
        case R.id.color:
     	  queue = "color";
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
     		case ITEM_FILENAME:
     			final Dialog dialog = new Dialog(this);
        		Toast.makeText(this, v.toString(), Toast.LENGTH_SHORT).show();
 
     			dialog.setContentView(R.layout.about_dialog);
     			dialog.setTitle(fileName);
     			TextView filename = (TextView) dialog.findViewById(R.id.about_text);
     			filename.setText(fileLoc);
     			dialog.show();
     	        
         		return;
         	//context menu appears for ink color
     		case ITEM_INKCOLOR: 
        		Toast.makeText(this, v.toString(), Toast.LENGTH_SHORT).show();

     			registerForContextMenu( v ); 
     		    v.setLongClickable(false); 
     		    this.openContextMenu(v);
     		   
     			break;
     		case ITEM_COPIES:
     			final View view = v;
 
     			final SharedPreferences userSettings = getSharedPreferences(PrintAtMITActivity.PREFS_NAME, MODE_PRIVATE);
     		  
     			final EditText copy = new EditText(this);
     			copy.setInputType(InputType.TYPE_CLASS_NUMBER);
               
     			AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
       		  	builder.setMessage("Number of copies:")
       		  			.setCancelable(false)
 	        	        .setView(copy)
 	        	        .setPositiveButton("Save", new DialogInterface.OnClickListener() {
	        	        	public void onClick(DialogInterface dialog, int id) {
	        	        		Toast.makeText(getApplicationContext(), copy.getText(), Toast.LENGTH_SHORT).show();
	        	        		
 	        	    			int copies = userSettings.getInt(PrintAtMITActivity.COPIES, 1);
 	        	    			String text = copy.getText().toString();
 	        	        		copies = (text.equals("") || text.equals("0")) ? copies : Integer.parseInt(copy.getText().toString());
 	        	       	      
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
 
     		case ITEM_PRINT_BUTTON:
     			printTask = new PrintTask();
                 printTask.execute();
                 break;
     		default: Toast.makeText(this, "herp derp", Toast.LENGTH_SHORT).show(); break;
     		}
     		
     	}
     	
     	super.onListItemClick(l, v, position, id);
     }
     
 /*    private OnClickListener btnStartListener = new View.OnClickListener() {
         public void onClick(View v){
             //btnStart.setVisibility(View.INVISIBLE);
             printTask = new PrintTask();
             printTask.execute();
         }
     };*/
     
     public class PrintTask extends AsyncTask<Void, byte[], Boolean> {
     	private ProgressDialog dialog;
 
         @Override
         protected void onPreExecute() {
             Log.i("AsyncTask", "onPreExecute");
             dialog = ProgressDialog.show(PrintOptionsActivity.this, "", 
                     "Sending print job", true);
         }
 
         @Override
         protected Boolean doInBackground(Void... params) { //This runs on a different thread
             boolean result = false;
             Lpr lpr = new Lpr();
             try {
             	File f = new File(fileLoc);
                 lpr.printFile(f, userName, hostName, queue, fileName);
             } catch (IOException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();      
                 Log.i("AsyncTask", "doInBackground: IOException");
                 result = true;
             } catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
                 Log.i("AsyncTask", "doInBackground: Exception");
                 result = true;
             }
             return result;
         }
         
         @Override
         protected void onCancelled() {
             Log.i("AsyncTask", "Cancelled.");
             //btnStart.setVisibility(View.VISIBLE);
         }
         @Override
         protected void onPostExecute(Boolean result) {
         	dialog.dismiss();
             if (result) {
                 Toast.makeText(getApplicationContext(), "Error sending, try again", Toast.LENGTH_SHORT).show();
                 Log.i("AsyncTask", "onPostExecute: Completed with an Error.");
             } else {
                 Toast.makeText(getApplicationContext(), "Successfully sent", Toast.LENGTH_SHORT).show();
                 Log.i("AsyncTask", "onPostExecute: Completed.");
             }
             //btnStart.setVisibility(View.VISIBLE);
         }
     }
 }
