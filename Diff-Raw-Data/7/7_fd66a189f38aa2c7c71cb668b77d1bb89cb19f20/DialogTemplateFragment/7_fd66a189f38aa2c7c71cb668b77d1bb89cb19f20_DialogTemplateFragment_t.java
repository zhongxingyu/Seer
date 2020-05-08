 package com.example.judgecompanion.dialogs.abstracts;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.content.res.Configuration;
 import android.support.v4.app.DialogFragment;
 import android.view.LayoutInflater;
 import android.view.View;
 
 public abstract class DialogTemplateFragment extends DialogFragment {
 		protected View theView = null;
 		int xmlIDPortrait = 0;
 		int xmlIDLandscape = 0;
 		
 		public DialogTemplateFragment(int layoutport, int layoutland)
 		{
 			xmlIDPortrait = layoutport;
 			xmlIDLandscape = layoutland;
 		}
 		
 		@Override 
 	    public Dialog onCreateDialog(Bundle savedInstanceState) {
 			if(savedInstanceState != null)
 			{
 				xmlIDPortrait = savedInstanceState.getInt("port", 0);
 				xmlIDLandscape = savedInstanceState.getInt("land", 0);
 			}
 			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 			// Get the layout inflater
 	        LayoutInflater inflater = getActivity().getLayoutInflater();
 
 	        // If no xml ID is given, throw an exception
 	        if (xmlIDPortrait == 0 || xmlIDLandscape == 0)
 	        	throw new NullPointerException();
 	        // Save view for later use
 	        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
 	        	theView = inflater.inflate(xmlIDLandscape, null);
 	        else
 	        	theView = inflater.inflate(xmlIDPortrait, null);
 	        
 	     // Inflate and set the layout for the dialog
 	        // Pass null as the parent view because its going in the dialog layout
 	        builder.setView(theView)
 	              .setPositiveButton("OK", new
 	                 DialogInterface.OnClickListener() {
 	                      public void onClick(DialogInterface dialog, int id) {
 	                          // Send the positive button event back to the host
 	                          // activity
 	                          confirm();
 	                      }             
 	                  })
 	              .setNegativeButton("Cancel", new
 	                 DialogInterface.OnClickListener() {
 	                     public void onClick(DialogInterface dialog, int id) {
 	                       // Do nothing, and dialog framework will cancel the dialog
 	                     }
 	                  });
 	        buildData();
 	           return builder.create();
 		}
 		
 		public interface DialogTemplateListener {
 	         public void onDialogPositiveClick(DialogFragment dialog, 
 	                                           String result);
 	     }
 	 
 	     // Use this instance of the interface to deliver action events
 	     DialogTemplateListener mListener;
 
 	     // Override the Fragment.onAttach() method to instantiate the
 	     // DialogTemplateListener
 	     @Override
 	     public void onAttach(Activity activity) {
 	         super.onAttach(activity);
 	         // Verify that the host activity implements the callback interface
 	         try {
 	             // Instantiate the NoticeDialogListener so we can send events to
 	             // the host
 	             mListener = (DialogTemplateListener) activity;
 	         } catch (ClassCastException e) {
 	             // The activity doesn't implement the interface, throw exception
 	             throw new ClassCastException(activity.toString()
 	                     + " must implement DialogTemplateListener");
 	         }
 	     }
 	     
 	     @Override
	     public void onPause()
	     {
	    	 super.onPause();
	    	 this.dismiss();
	     }
	     
	     @Override
 	     public void onSaveInstanceState(Bundle savedInstanceState)
 	     {
 	    	 savedInstanceState.putInt("port", xmlIDPortrait);
 	    	 savedInstanceState.putInt("land", xmlIDLandscape);
 	     }
 
 		public abstract void confirm();
 		public abstract void buildData();
 }
