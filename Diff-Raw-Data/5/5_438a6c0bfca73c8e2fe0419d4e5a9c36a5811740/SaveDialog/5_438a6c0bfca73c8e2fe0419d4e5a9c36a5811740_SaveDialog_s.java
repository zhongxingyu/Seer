 package com.werebug.randomsequencegenerator;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 import android.widget.EditText;
 
 public class SaveDialog extends DialogFragment {
 	
 	private EditText edit_name;
 	
 	public interface SaveDialogListener {
 		public void onDialogPositiveClick(DialogFragment dialog, String name);
 		public void onDialogNegativeClick(DialogFragment dialog);
 	}
 	
 	SaveDialogListener mListener;
 	
 	// Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         // Verify that the host activity implements the callback interface
         try {
             // Instantiate the NoticeDialogListener so we can send events to the host
             mListener = (SaveDialogListener) activity;
         } catch (ClassCastException e) {
             // The activity doesn't implement the interface, throw exception
             throw new ClassCastException(activity.toString()
                     + " must implement SaveDialogListener");
         }
     }
 	
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         // Build the dialog and set up the button click handlers
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
         
         edit_name = new EditText(this.getActivity());
                 
        builder.setMessage(R.string.save_dialog)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Send the negative button event back to the host activity
                        mListener.onDialogNegativeClick(SaveDialog.this);
                    }
                })
               .setPositiveButton(R.string.confirm_saving, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                 	   // Retrieving name
                 	   String name = edit_name.getText().toString();
                        // Send the positive button event back to the host activity
                        mListener.onDialogPositiveClick(SaveDialog.this, name);
                    }
                })
                .setView(edit_name);
         return builder.create();
     }
 	
 }
