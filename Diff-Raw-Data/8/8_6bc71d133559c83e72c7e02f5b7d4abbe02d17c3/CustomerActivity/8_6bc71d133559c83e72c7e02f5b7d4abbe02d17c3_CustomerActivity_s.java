 package org.rcredits.pos;
 
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 /**
  * Show the name, location, and photo of the customer.
  * @intent customer: qid of customer being identified
  * @intent customerRegion: customer's region code
  * @intent json: identifying information about the customer (name, company, place), json-encoded
  */
 public class CustomerActivity extends Act {
     private final Act act = this;
     public static String customer; // qid of current customer
 
     /**
      * Get the customer's info (including photo) from the server and display it, with options for what to do next.
      * @param savedInstanceState
      */
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_customer);
 
         A.failMessage = ""; // no failure message yet
         A.lastTx = ""; // previous customer info is no longer valid
         A.balance = "";
         A.undo = "";
 
         if (!A.agentCan(A.CAN_REFUND)) findViewById(R.id.refund).setVisibility(View.INVISIBLE);
         if (!A.agentCan(A.CAN_BUY_CASH)) findViewById(R.id.cashin).setVisibility(View.GONE);
         if (!A.agentCan(A.CAN_SELL_CASH)) findViewById(R.id.cashout).setVisibility(View.GONE);
 
         String json = A.getIntentString(this.getIntent(), "json");
         customer = A.getIntentString(this.getIntent(), "customer");
         String customerRegion = A.getIntentString(this.getIntent(), "customerRegion");
 
         TextView customerName = (TextView) findViewById(R.id.customer_name);
         //customerName.setText(customer); // temporarily, while we contact the server for more info
         customerName.setText(A.jsonString(json, "name"));
 
         String company = A.jsonString(json, "company");
         TextView customerCompany = (TextView) findViewById(R.id.customer_company);
         customerCompany.setText(company);
        customerCompany.setVisibility(company == null ? View.GONE : View.VISIBLE);
 
         TextView customerPlace = (TextView) findViewById(R.id.customer_place);
         customerPlace.setText(A.jsonString(json, "place"));
 
         A.balance = A.jsonString(json, "balance"); // save in case transaction fails
 
         ImageView photo = (ImageView) findViewById(R.id.photo);
         final byte[] image = A.apiGetPhoto(act, customerRegion, customer);
         if (image != null) photo.setImageBitmap(BitmapFactory.decodeByteArray(image, 0, image.length));
     }
 
     /**
      * The user clicked on of four buttons: charge, refund, cash in, or cash out. Handle each in the Tx Activity.
      * @param v: which button was pressed.
      */
     public void onRClick(View v) {
         Intent intent = new Intent(this, TxActivity.class);
         intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
         String description = v.getId() == R.id.charge ? A.descriptions.get(0) : (String) v.getContentDescription();
         A.putIntentString(intent, "description", description);
         A.putIntentString(intent, "customer", customer);
         startActivity(intent);
     }
     
 }
