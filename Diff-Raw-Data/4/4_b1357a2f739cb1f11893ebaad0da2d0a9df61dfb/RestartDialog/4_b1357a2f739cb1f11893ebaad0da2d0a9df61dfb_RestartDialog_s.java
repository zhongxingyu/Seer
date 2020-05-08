 package gov.cdc;
 
 import gov.cdc.R;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Toast;
 
 /**
  * Created with IntelliJ IDEA.
  * User: paul
  * Date: 9/7/12
  * Time: 11:17 AM
  * To change this template use File | Settings | File Templates.
  */
 public class RestartDialog extends Activity {
     CharSequence[] items = { "Google","Apple","Microsoft" };
     boolean[] itemsChecked = new boolean [items.length];
 
 
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.restart_dialog);
     }
 
    public void onClick(View v) {
        showDialog(0);
    }

     @Override
     protected Dialog onCreateDialog(int id) {
         switch (id) {
             case 0:
                 return new AlertDialog.Builder(this)
                 .setIcon(R.drawable.nav_button_restart)
                 .setTitle("This is a dialog with some simple text...")
                 .setPositiveButton("OK",
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton)
                         {
                             Toast.makeText(getBaseContext(),
                                 "OK clicked!", Toast.LENGTH_SHORT).show();
                         }
                     }
                 )
                 .setNegativeButton("Cancel",
                     new DialogInterface.OnClickListener() {
                         public void onClick(DialogInterface dialog, int whichButton)
                         {
                             Toast.makeText(getBaseContext(),
                                 "Cancel clicked!", Toast.LENGTH_SHORT).show();
                         }
                     }
                 )
                 .setMultiChoiceItems(items, itemsChecked,
                     new DialogInterface.OnMultiChoiceClickListener() {
                         public void onClick(DialogInterface dialog,
                         int which, boolean isChecked) {
                             Toast.makeText(getBaseContext(),
                                 items[which] + (isChecked ? " checked:":" unchecked:"),
                                 Toast.LENGTH_SHORT).show();
                         }
                     }
                 ).create();
         }
         return null;
 
     }
 }
