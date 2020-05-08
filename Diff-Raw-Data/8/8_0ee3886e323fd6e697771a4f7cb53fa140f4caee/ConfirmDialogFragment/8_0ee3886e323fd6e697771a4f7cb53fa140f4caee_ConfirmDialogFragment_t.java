 package net.analogyc.wordiary.dialogs;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.support.v4.app.DialogFragment;
 
 import net.analogyc.wordiary.R;
 
 public class ConfirmDialogFragment extends DialogFragment {
 
     /**
      * The interface for a confirm dialog listener
      */
     public interface ConfirmDialogListener {
         /**
          * Manages a positive answer
          *
          * @param id     the dialog id
          */
         public void onConfirmedClick(int id);
     }
 
     protected ConfirmDialogListener mListener;
     protected int mId;
 
     /**
      * Set the dialog id, this will be used to identify the confirmation at onConfirmedClick
      *
      * @param id the id of the dialog
      */
     public void setId(int id) {
         mId = id;
     }
 
     @Override
     public void onAttach(Activity activity) {
         super.onAttach(activity);
         // Verify that the host activity implements the callback interface
         try {
             // Instantiate the NoticeDialogListener so we can send events to the host
             mListener = (ConfirmDialogListener) activity;
         } catch (ClassCastException e) {
             // The activity doesn't implement the interface, throw exception
             throw new ClassCastException(activity.toString() + " must implement ConfirmDialogListener");
         }
     }
 
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
 
         final int callId = mId;
 
         builder.setTitle(R.string.title_confirm)
                 // Add action buttons
                 .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         mListener.onConfirmedClick(callId);
                     }
                 })
                 .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         ConfirmDialogFragment.this.getDialog().cancel();
                     }
                 });
 
         return builder.create();
     }
 }
