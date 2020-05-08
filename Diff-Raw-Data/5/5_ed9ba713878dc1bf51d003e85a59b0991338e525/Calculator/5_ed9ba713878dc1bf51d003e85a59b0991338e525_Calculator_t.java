 package org.sumitbisht;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 
 public class Calculator extends Activity {
 	Button btn;
 	Button billFinder;
 	EditText billCost;
 	EditText unitCount;
 	EditText meterRent;
 	TextView unitCost;
 	EditText user1Reading;
 	EditText user2Reading;
 	EditText user3Reading;
 	TextView user1Bill;
 	TextView user2Bill;
 	TextView user3Bill;
 	
     /** Called when the activity is first created. */
   //  @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         btn = (Button) findViewById(R.id.costperunitbtn);
         billCost = (EditText) findViewById(R.id.editText);
         unitCount = (EditText) findViewById(R.id.editText1);
         meterRent = (EditText) findViewById(R.id.editText2);
         unitCost = (TextView) findViewById(R.id.unitcost);
         billFinder = (Button) findViewById(R.id.mainBillButton);
 
         user1Reading = (EditText) findViewById(R.id.usr1Reading);
         user2Reading = (EditText) findViewById(R.id.usr2Reading);
         user3Reading = (EditText) findViewById(R.id.usr3Reading);
         user1Bill = (TextView) findViewById(R.id.Usr1Bill);
         user2Bill = (TextView) findViewById(R.id.Usr2Bill);
         user3Bill = (TextView) findViewById(R.id.Usr3Bill);
         
         btn.setOnClickListener(new View.OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				unitPrice();
 			}
 		});
         billFinder.setOnClickListener( new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View arg0) {
 				perPersonBill();
 				
 			}
 		});
      //Following properties exist to set the value of the fields to null on focus.
      //TODO : write the code here!
     }
     
     /**
      * Finds out the price of a single unit of electricity.
      * Uses the total number of units consumed and the total price of the electricity bill.
      * It then deducts the fixed meter rent as specified as this is not directly counted under the unit price,
      * but is distributed evenly.
      */
 	private void unitPrice() {
 		try{
 			double billedCost = Double.parseDouble(billCost.getText().toString());
 			double unitsCnt = Double.parseDouble(unitCount.getText().toString());
 			double rent = Double.parseDouble(meterRent.getText().toString());
 		
 			billedCost = (billedCost-rent)/unitsCnt;
 			unitCost.setText("Rs. "+billedCost+"/-");
 			perPersonBill();
 		}catch(Exception ex){
 			unitCost.setText("error computing values.");
 		}
 	}
 	private void perPersonBill(){
 		try {
 			double totalBill = Double.parseDouble(billCost.getText().toString());
			double rent = Double.parseDouble(meterRent.getText().toString());
 			double unitsCnt = Double.parseDouble(unitCount.getText().toString());
 			double perUnitCost = (totalBill - rent) / unitsCnt;
 			
 			double usr1Reading = Double.parseDouble(user1Reading.getText().toString());
 			double usr2Reading = Double.parseDouble(user2Reading.getText().toString());
 			
 			double remainingReading = unitsCnt-usr1Reading-usr2Reading;
 			user3Reading.setText(""+(int)remainingReading);
			rent /=3;
 			usr1Reading = usr1Reading * perUnitCost + rent;
 			usr2Reading = usr2Reading * perUnitCost + rent;
 			remainingReading = remainingReading * perUnitCost + rent;
 			user1Bill.setText("Rs."+(int)usr1Reading+"/-");
 			user2Bill.setText("Rs."+(int)usr2Reading+"/-");
 			user3Bill.setText("Rs."+(int)remainingReading+"/-");
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 	}
 }
