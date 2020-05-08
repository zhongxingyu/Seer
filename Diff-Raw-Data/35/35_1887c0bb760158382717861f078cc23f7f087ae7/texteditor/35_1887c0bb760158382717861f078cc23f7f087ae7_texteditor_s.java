 package org.android.liunx;
 
import static android.widget.Toast.makeText;

 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.Toast;
 
 public class texteditor extends Activity {
 	// the menus
 	private static final int MENU_OPEN = Menu.FIRST;
 	private static final int MENU_SAVE = Menu.FIRST + 1;
 	private static final int MENU_DELETE = Menu.FIRST + 2;
 	// the dialogs
 	private static final int DIALOG_OPEN = 1;
 	private static final int DIALOG_SAVE = 2;
 	private static final int DIALOG_DELETE = 4;
 	// save edit text
 	//private EditText saveContents;
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         // R.layout.save not initialized at the moment
         //saveContents = (EditText) this.findViewById(R.id.EditText01);
     }
     
     /**
      * We can do open/save/delete operations with menu 
      */
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	super.onCreateOptionsMenu(menu);
     	
     	menu.add(0, MENU_OPEN, 0, R.string.open);
     	menu.add(0, MENU_SAVE, 0, R.string.save);
     	menu.add(0, MENU_DELETE, 0, R.string.delete);
     	return true;
     }
     
     /**
      * process the menu press events
      */
     @Override 
     public boolean onOptionsItemSelected(MenuItem item) {
     	// get the id which menu is selected
     	final int id = item.getItemId();
     	boolean retval = false;
     	switch (id) {
     	case MENU_OPEN:
     		showDialog(DIALOG_OPEN);
     		retval = true;
     		break;
     	case MENU_SAVE:
     		showDialog(DIALOG_SAVE);
     		retval = true;
     		break;
     	case MENU_DELETE:
     		showDialog(DIALOG_DELETE);
     		retval =true;
     		break;
     		default:
     			break;
     	}
     	return retval;
     }
     /**
      * when howDialog, onDialogCreate will be called 
      */
     @Override
     protected Dialog onCreateDialog(int id) {
     	final View dialogview;
     	// the switch sentence
     	switch (id) {
     	case DIALOG_OPEN:
     		LayoutInflater openflater = LayoutInflater.from(this);
     		dialogview = openflater.inflate(R.layout.open, null);
     		Toast.makeText(this, "Dialog open", Toast.LENGTH_LONG).show();
     		String[] flist = this.fileList();
     		int i;
     		for (i = 0; i < flist.length; i++) {
     			Toast.makeText(this, flist[i].toString(), Toast.LENGTH_LONG).show();
     		}
     		return new AlertDialog.Builder(this)
     		.setTitle("Open the file")
     		.setIcon(R.drawable.icon)
     		.setView(dialogview)
     		.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.cancel();
 					//Toast.makeText(this, "Ok", Toast.LENGTH_LONG).show();
 					return;
 					
 				}
 			})
 			.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					
 				}
 			})
     		.create();
     	case DIALOG_SAVE:
     		LayoutInflater saveflater = LayoutInflater.from(this);
     		dialogview = saveflater.inflate(R.layout.save, null);
     		Toast.makeText(this, "Dialog save", Toast.LENGTH_LONG).show();
     		return new AlertDialog.Builder(this)
     		.setTitle("Save file")
     		.setIcon(R.drawable.icon)
     		.setView(dialogview)
     		.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// Get the input file name 
 					EditText saveview = (EditText) dialogview.findViewById(R.id.EditText01);
 					String filename = saveview.getText().toString();
 					try {
 						FileOutputStream fOut = openFileOutput(filename, MODE_WORLD_READABLE);
 						OutputStreamWriter osw = new OutputStreamWriter(fOut);
 						osw.write("Hello, World!");
 						osw.flush();
 						osw.close();
 					} catch (IOException ioe) {
 						ioe.printStackTrace();
 					}
 					
 				}
 			})
 			.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					
 				}
 			})
     		.create();
     	case DIALOG_DELETE:
     		LayoutInflater deleteflater = LayoutInflater.from(this);
     		dialogview = deleteflater.inflate(R.layout.delete, null);
     		Toast.makeText(this, "Dialog delete", Toast.LENGTH_LONG).show();
     		return new AlertDialog.Builder(this)
     		.setTitle("Delete file")
     		.setIcon(R.drawable.icon)
     		.setView(dialogview)
     		.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					dialog.cancel();
 					//Toast.makeText(this, "Ok", Toast.LENGTH_LONG).show();
 					return;
 					
 				}
 			})
 			.setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
 				
 				@Override
 				public void onClick(DialogInterface dialog, int which) {
 					// TODO Auto-generated method stub
 					
 				}
 			})
     		.create();
     	}
     	// we can not return null
     	return null;
     	
     }
 }
