 package de.fhb.mi.paperfly;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.app.DialogFragment;
 import android.content.DialogInterface;
 import android.os.Bundle;
 
 /**
  * @author Christoph Ott
  */
 public class InfoDialog extends DialogFragment {
     @Override
     public Dialog onCreateDialog(Bundle savedInstanceState) {
         // Use the Builder class for convenient dialog construction
         AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Nimmt sich die aktuelle Liste der zur Zeit anwesenden User und schickt sie an Programme die diese Liste abspeichern koennen oder weiter verschicken koennen. (z.B. email)")
                 .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         // FIRE ZE MISSILES!
                     }
                 })
                 .setIcon(android.R.drawable.ic_dialog_info).setTitle("Info Mockup");
         // Create the AlertDialog object and return it
         return builder.create();
     }
 }
