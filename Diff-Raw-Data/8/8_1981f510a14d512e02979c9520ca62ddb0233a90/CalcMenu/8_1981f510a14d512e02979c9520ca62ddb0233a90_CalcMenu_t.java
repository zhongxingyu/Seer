 /**
  * Includes
  */
 package idig.za.net.conscalc;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.widget.Button;
 
 /**
  * @author josiah
  *
  */
 public class CalcMenu extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.calc_menu);
         
         // create a button object
         final Button buttonAddRecord = (Button) findViewById(R.id.buttonAddRecord);
         // attach a listener to the button
         buttonAddRecord.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// create an Intent object for this activity, including its destination class
 				Intent myIntent = new Intent(CalcMenu.this, AddRecord.class);
 				// run the activity
 				startActivity(myIntent);
 			}
 		});
         
         // create a button object
         final Button buttonCalculateConsumption = (Button) findViewById(R.id.buttonCalculateConsumption);
         // attach a listener to the button
        buttonCalculateConsumption.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 //				// create an Intent object for this activity, including its destination class
 //				Intent myIntent = new Intent(CalcMenu.this, CalculateConsumption.class);
 //				// run the activity
 //				startActivity(myIntent);
 			}
 		});
         
         // create a button object
         final Button buttonDeleteRecord = (Button) findViewById(R.id.buttonDeleteRecord);
         // attach a listener to the button
        buttonDeleteRecord.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 //				// create an Intent object for this activity, including its destination class
 //				Intent myIntent = new Intent(CalcMenu.this, DeleteRecord.class);
 //				// run the activity
 //				startActivity(myIntent);
 			}
 		});
         
         // create a button object
         final Button buttonEditRecord = (Button) findViewById(R.id.buttonEditRecord);
         // attach a listener to the button
        buttonEditRecord.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View v) {
 				// TODO Auto-generated method stub
 			}
 		});
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 }
