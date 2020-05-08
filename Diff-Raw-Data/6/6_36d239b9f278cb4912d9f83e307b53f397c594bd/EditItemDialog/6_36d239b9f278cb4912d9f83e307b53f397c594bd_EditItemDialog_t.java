 package com.coffeestrike.bettershoppinglist;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.support.v4.app.Fragment;
 import android.text.Editable;
 import android.text.TextWatcher;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 
 public class EditItemDialog extends DialogFragment {
 	
 	public static final String TAG = "EditItemDialog";
 	public static final String EXTRA_ITEM = "com.coffeestrike.bettershoppinglist.EditItemDialog";
 	private Item mItem;
 
 	private void sendResult(int resultCode){
 		Fragment target = getTargetFragment();
 		if(target == null){
 			return;
 		}
 		Intent i = new Intent();
 		i.putExtra(EXTRA_ITEM, mItem);
 		target.onActivityResult(getTargetRequestCode(), resultCode, i);
 			
 	}
 	
 	public static EditItemDialog newInstance(Item item){
 		Bundle args = new Bundle();
 		args.putSerializable(EXTRA_ITEM, item);
 		
 		EditItemDialog dialog = new EditItemDialog();
 		dialog.setArguments(args);
 		return dialog;
 	}
 	
 	public Dialog onCreateDialog(Bundle savedInstanceState){
 		mItem = (Item)getArguments().getSerializable(EXTRA_ITEM);
 		View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_edit_item, null);
 		
 		EditText description = (EditText)v.findViewById(R.id.description_editText);
 		description.addTextChangedListener(new TextWatcher(){
 
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before,
 					int count) {
 				mItem.setDescription(s);
 				Log.d(TAG, "description changed");
 			}
 			
 		});
 //		description.setText(mItem.getDescription());
 		EditText quantity = (EditText)v.findViewById(R.id.qty_editText);
 		quantity.addTextChangedListener(new TextWatcher() {
 			
 			@Override
 			public void onTextChanged(CharSequence s, int start, int before, int count) {
				try {
					mItem.setQty(Integer.parseInt(s.toString()));
				} catch (NumberFormatException n) {
					mItem.setQty(1);
				}
 				
 			}
 			
 			@Override
 			public void beforeTextChanged(CharSequence s, int start, int count,
 					int after) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void afterTextChanged(Editable s) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 //		quantity.setText(mItem.getQty());
 		
 		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 		
 		return builder.setView(v).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
 			
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 				sendResult(Activity.RESULT_OK);	
 				
 			}
 		})
 		.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
 
 			@Override
 			public void onClick(DialogInterface dialog, int which) {
 							
 			}
 		}).create();
 
 	}
 	
 }
