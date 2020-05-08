 package edu.mit.printAtMIT.view;
 
 import edu.mit.printAtMIT.R;
 import android.app.Activity;
import android.app.ListActivity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 
 /**
  * Lists all the printers from database.
  * Shows name, location, status from each printer
  * List of favorite printers on top, then list of all printers
  * 
  * Menu Item:
  *      Settings
  *      About
  *      Home
 *      Refresh
  */
public class PrinterListActivity extends ListActivity {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.printer_list);
         
         Button button01 = (Button) findViewById(R.id.button01);
         Button button02 = (Button) findViewById(R.id.button02);
 
         button01.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
             	Intent intent = new Intent(view.getContext(), MapActivity.class);
             	startActivity(intent);
             }
         });
         button02.setOnClickListener(new View.OnClickListener() {
 
             public void onClick(View view) {
             	Intent intent = new Intent(view.getContext(), PrinterInfoActivity.class);
             	startActivity(intent);
             }
         });
     }
 }
