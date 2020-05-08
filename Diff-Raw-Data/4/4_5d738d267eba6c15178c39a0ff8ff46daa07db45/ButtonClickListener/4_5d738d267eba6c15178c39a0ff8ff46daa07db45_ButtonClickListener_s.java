 package myapp.homebase2;
 
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ButtonClickListener implements OnClickListener {
 
 	// instance
 	HomeBase2Activity context;
 
 	// Choices for type
 	final String[] typeItems = new String[]{
 			"Alphabets only",
 			"Numbers only",
 			"Mixture"
 	};//final String[] typeItems
 
 	// Choices for length
 	final String[] lengthItems = new String[]{
 			"5",
 			"7",
 			"10"
 	};//final String[] items
 
 	// Chosen item
	int chosenType = -1;
 	
 	// chosen length for "Length" button
 	int chosenLength = -1;		/* default is 0		*/
 
 	public ButtonClickListener(HomeBase2Activity context) {
 		// TODO ꂽRXgN^[EX^u
 		this.context = context;
 	}
 
 	public void onClick(View v) {
 		// TODO ꂽ\bhEX^u
 		// tag
 		 String tag	= (String)v.getTag();
 		
 		 // switching
 		 if (tag.equals("generatePassword")) {
 			showDialogPassword();
 		} else if (tag.equals("setType")) {//if (tag.equalsIgnoreCase("generatePassword"))
 			setType();
 		} else if (tag.equals("setLength")) {//if (tag.equalsIgnoreCase("generatePassword"))
 			setLength();
 		}//if (tag.equalsIgnoreCase("generatePassword"))
 
 	}
 
 	private void setLength() {
 		// define a dialog
 		 AlertDialog.Builder dialog	= new AlertDialog.Builder(context);
 		 
 		 // set title
 		 dialog.setTitle("Choose the length");
 		 
 		 // set items: types
 		 dialog.setSingleChoiceItems(lengthItems, 0, new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// TODO ꂽ\bhEX^u
 				chosenLength = whichButton;
 			}//public void onClick(DialogInterface dialog, int which)
 		 });//dialog.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener()
 		
 		// set button
 		 dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int whichButton) {
 
 				// set value to the text view
 				TextView tv = (TextView) context.findViewById(R.id.t1_LL3_tv1);
 				tv.setText(lengthItems[chosenLength]);
 				
 			}//public void onClick()
 		 });//dialog.setPositiveButton()
 		 
 		 // show dialog
 		 dialog.show();
 		
 	}//private void setLength()
 
 	/* setType()
 	 * 
 	 */
 
 	private void setType() {
 
 		// define a dialog
 		 AlertDialog.Builder dialog	= new AlertDialog.Builder(context);
 		 
 		 // set title
 		 dialog.setTitle("Choose the type");
 		 
 		 // set items: types
 		 dialog.setSingleChoiceItems(typeItems, 0, new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int whichButton) {
 				// TODO ꂽ\bhEX^u
 				chosenType = whichButton;
 			}//public void onClick(DialogInterface dialog, int which)
 		 });//dialog.setSingleChoiceItems(typeItems, 0, new DialogInterface.OnClickListener()
 		
 		// set button
 		 dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 			
 			public void onClick(DialogInterface dialog, int whichButton) {
 
 				// set value to the text view
 				TextView tv = (TextView) context.findViewById(R.id.t1_LL2_tv1);
 				tv.setText(typeItems[chosenType]);
 				
 			}//public void onClick()
 		 });//dialog.setPositiveButton()
 		 
 		 // show dialog
 		 dialog.show();
 		 
 	}//private void setType()
 
 	/**
 	 * showDialogPassword()
 	 * 
 	 */
 	// set vars
 	/*
 	 * "Alphabets only"	=> MyLib.PwdTypes.ALPHA = 0
 	 * "Numbers only"	=> MyLib.PwdTypes.NUM = 1
 	 * "Mixture"	=> MyLib.PwdTypes.ALNUM = 2
 	 */
 
 	final String[] lengths	= new String[]{
 			"5", "7", "10"
 	};
 	
 	// selected item id
 	int which = 0;
 	
 	// password length
 	int passwdLength = 5;		/* default length is 5		*/
 	
 	// method
 	private void showDialogPassword() {
 		
 		// Type items already chosen?
 		if (chosenType == -1) {
 			//debug
 			chosenType = 0;
 //			setType();
 		} 
 		
 		// Length items already chosen?
 		if (chosenLength == -1) {//if (chosenType == -1)
 			//debug
 			chosenLength = 0;
 //			setLength();
 		}//if (chosenType == -1)
 		
 		// get a  text view
 		TextView tv = (TextView) context.findViewById(R.id.t1_LL1_tv1);
 
 		// set text
 
 		
 		// debug
 		Toast.makeText(context, lengthItems[chosenLength], Toast.LENGTH_LONG).show();
 		
 //		tv.setText(MyLib.generatePassword(chosenType, chosenLength));
 //		tv.setText(MyLib.generatePassword(chosenType, Integer.parseInt(typeItems[chosenLength])));
 
 	}//private void showDialogPassword()
 
 }
