 package com.modrecipe.modrecipe.listhelpers;
 
 import java.util.ArrayList;
 
 import com.modrecipe.modrecipe.R;
 import com.modrecipe.modrecipe.objects.DataSingleton;
 import com.modrecipe.modrecipe.objects.Ingredient;
 import com.modrecipe.modrecipe.objects.ShoppingCategory;
 
 import android.app.Dialog;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 public class ListAddDialogFragment extends DialogFragment {
 	
 	ListAddDialogFragment ladf;
 	
     /** The system calls this to get the DialogFragment's layout, regardless
     of whether it's being displayed as a dialog or an embedded fragment. */
 	@Override
 	public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
 		// Inflate the layout to use as dialog or embedded fragment
 		
 		final View v = inflater.inflate(R.layout.list_dialog_add, container, false);
 
 		ladf = this;
 		
 		//EditText
 		final EditText et = (EditText) v.findViewById(R.id.itemEditText);
 		
 		//Spinner
 		String array_spinner[];
 		array_spinner=new String[7]; //8
 		array_spinner[0]="OTHER";		
 		array_spinner[1]="FRUITS & VEGETABLES";
 		array_spinner[2]="DAIRY, EGGS, & CHEESE";
 		array_spinner[3]="SPICES & BAKING";
 		array_spinner[4]="MEAT & SEAFOOD";
 		array_spinner[5]="CANNED GOODS & SOUPS";
 		array_spinner[6]="FROZEN ITEMS";
 		//array_spinner[7]="REGULAR ITEMS";
 		final Spinner s = (Spinner) v.findViewById(R.id.Spinner);
 		ArrayAdapter adapter = new ArrayAdapter(v.getContext(),
 		android.R.layout.simple_spinner_item, array_spinner);
 		s.setAdapter(adapter);	
 		
 		Button addBtn = (Button) v.findViewById(R.id.addBtn);
 		addBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// Adds Btn
 				
 				ArrayList<ShoppingCategory> sc = DataSingleton.getInstance().getUser().getShoppingList();
 				for (ShoppingCategory c : sc) {
 					if (s.getSelectedItem().toString().equals(c.getName())) {
 						Ingredient i = new Ingredient(et.getText() + "");
 						i.setCategory(c.getName());
 						c.addIngredient(i);
						DataSingleton.getInstance().checkGroupToTop(i); // TODO fix up? 
 					}
 				}
 
 				ladf.dismiss();
 				
 				// TODO fix
 				//		and add basic Data Validation
 				DataSingleton.getInstance().getListExpAdapter().notifyDataSetChanged();
 				
 			}
 			
 		});
 		
 		
 		Button cancelBtn = (Button) v.findViewById(R.id.cancelBtn);
 		cancelBtn.setOnClickListener(new OnClickListener() {
 
 			@Override
 			public void onClick(View arg0) {
 				// TODO less hacky
 				ladf.dismiss();
 			}
 			
 		});
 		
 		return v;
 	}
 
 	/** The system calls this only when creating the layout in a dialog. */
 	@Override
 	public Dialog onCreateDialog(Bundle savedInstanceState) {
 		// The only reason you might override this method when using onCreateView() is
 		// to modify any dialog characteristics. For example, the dialog includes a
 		// title by default, but your custom layout might not need it. So here you can
 		// remove the dialog title, but you must call the superclass to get the Dialog.
 		Dialog dialog = super.onCreateDialog(savedInstanceState);
 		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		return dialog;
 	}
 }
