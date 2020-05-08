 package net.m_kawato.tabletpos;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.math.BigDecimal;
 import java.util.Calendar;
 
 import android.os.Bundle;
 import android.os.Environment;
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Intent;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 import android.widget.TextView;
 
 public class Receipt extends Activity implements OnClickListener {
     private static final String TAG = "Receipt";
     private Globals globals;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         Log.d(TAG, "onCreate");
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_receipt);
 
         this.globals = (Globals) this.getApplication();
 
         // Build ListView for order list
         ListView orderListView = (ListView) findViewById(R.id.list);
         LayoutInflater inflater = this.getLayoutInflater();
         View header = inflater.inflate(R.layout.receipt_header, null);
         View footer = inflater.inflate(R.layout.receipt_footer, null);
         orderListView.addHeaderView(header);
         orderListView.addFooterView(footer);
 
         String[] from = {"product_name", "unit_price", "quantity", "unit_price_box", "quantity_box", "amount"};
         int[] to = {R.id.product_name, R.id.unit_price, R.id.quantity, R.id.unit_price_box, R.id.quantity_box, R.id.amount};
         SimpleAdapter orderListAdapter = new SimpleAdapter(this, globals.orderItemList, R.layout.receipt_item, from, to);
         orderListView.setAdapter(orderListAdapter);        
         
        // date, route and place
         Log.d(TAG, String.format("onCreate: selectedRoute=%s, routes.size=%s",
                 globals.selectedRoute, globals.routes.size()));
        TextView dateView = (TextView) findViewById(R.id.date);
        String dateText = DateFormat.format("dd-MM-yyyy", Calendar.getInstance()).toString();
        dateView.setText(dateText);

         TextView routeView = (TextView) findViewById(R.id.route);
         String routeCode = globals.routes.get(globals.selectedRoute);
         String routeName = globals.routeName.get(routeCode);
         routeView.setText(String.format("%s (%s)", routeName, routeCode));
         
         TextView placeView = (TextView) findViewById(R.id.place);
         String placeCode = globals.places.get(routeCode).get(globals.selectedPlace);
         String placeName = globals.placeName.get(placeCode);
         placeView.setText(String.format("%s (%s)", placeName, placeCode));
 
         // total amount, credit amount and cash amount
         TextView totalAmountView = (TextView) findViewById(R.id.total_amount);
         totalAmountView.setText(globals.totalAmount.toString() + " " + getString(R.string.currency));
 
         TextView creditAmountView = (TextView) findViewById(R.id.credit_amount);
         creditAmountView.setText(globals.creditAmount.toString() + " " + getString(R.string.currency));
 
         BigDecimal cashAmount = globals.totalAmount.subtract(globals.creditAmount);
         TextView cashAmountView = (TextView) findViewById(R.id.cash_amount);
         cashAmountView.setText(cashAmount.toString() + " " + getString(R.string.currency));
         
         // "Confirm" button
         Button btnConfirm = (Button) findViewById(R.id.btn_confirm);
         btnConfirm.setOnClickListener(this);
 
         // "Enter New Order" button (aka. go to top)
         Button btnGotoTop = (Button) findViewById(R.id.btn_gototop);
         btnGotoTop.setOnClickListener(this);
         
         // "Export" button
         Button btnExport = (Button) findViewById(R.id.btn_export);
         btnExport.setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.receipt, menu);
         return true;
     }
 
     @Override
     public void onClick(View v) {
         Intent i;
         switch (v.getId()) {
         case R.id.btn_confirm:
             i = new Intent(this, Confirm.class);
             startActivity(i);
             break;
         case R.id.btn_gototop:
             globals.initialize();
             i = new Intent(this, Order.class);
             startActivity(i);
             break;
         case R.id.btn_export:
             exportReceipt();
             break;
         }        
     }
 
     // Export receipt data to sdcard
     private void exportReceipt() {
         Log.d(TAG, "exportReceipt");
 
         String sdcardPath = Environment.getExternalStorageDirectory().getPath();
         String dirname = String.format("%s/%s", sdcardPath, Globals.SDCARD_DIRNAME);
         String filename = String.format("%s/%s", dirname, Globals.RECEIPT_FILENAME);
         String timestamp = DateFormat.format("yyyy/MM/dd kk:mm:ss", Calendar.getInstance()).toString();
         Log.d(TAG, String.format("exportReceipt: filename=%s, timestamp=%s", filename, timestamp));
 
         StringBuffer buf = new StringBuffer();
         String routeCode = globals.routes.get(globals.selectedRoute);
         String routeName = globals.routeName.get(routeCode);
         String placeCode = globals.places.get(routeCode).get(globals.selectedPlace);
         String placeName = globals.placeName.get(placeCode);
         
         buf.append(String.format("%s,%s,%s,%s,%s,%s,%s",
                 timestamp, routeName, routeCode, placeName, placeCode,
                 globals.totalAmount.toString(),
                 globals.creditAmount.toString()));
         for (Product item: globals.orderItems) {
             buf.append(String.format(",%s,%d,%d",
                     item.productName,
                     item.productId,
                     item.quantity + item.quantityBox * item.numPiecesInBox));
         }
         Log.d(TAG, String.format("exportReceipt: line=" + buf.toString()));
         
         PrintWriter writer = null;
         try {
             File dir = new File(dirname);
             if (! dir.exists()) {
                 dir.mkdirs();  
             }  
             FileOutputStream fout = new FileOutputStream(filename, true);
             writer = new PrintWriter(fout);
             writer.println(buf.toString());
             AlertDialog.Builder builder = new AlertDialog.Builder(this);
             builder.setMessage("The receipt was successfully exported to\n" + filename);
             builder.setPositiveButton(android.R.string.ok, null);
             builder.show();
         } catch (IOException e) {
             Log.e(TAG, "failed to write receipt to sdcard");
             e.printStackTrace();
         } finally {
             writer.close();
         }  
     }
 }
