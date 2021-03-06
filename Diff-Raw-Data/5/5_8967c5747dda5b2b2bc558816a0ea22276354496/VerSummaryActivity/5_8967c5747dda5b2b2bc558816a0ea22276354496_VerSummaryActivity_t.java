 package gov.nysenate.inventory.android;
 
 import java.util.ArrayList;
 import java.util.concurrent.ExecutionException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.text.Html;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ListView;
 import android.widget.ProgressBar;
 import android.widget.TabHost;
 import android.widget.TabHost.TabSpec;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class VerSummaryActivity extends SenateActivity
 {
     ArrayList<InvItem> AllScannedItems = new ArrayList<InvItem>();
     ArrayList<InvItem> missingItems = new ArrayList<InvItem>(); // Items in the location which have not been verified.
     ArrayList<InvItem> newItems = new ArrayList<InvItem>(); // Items in the location but listed elsewhere in the database.
     ArrayList<InvItem> scannedBarcodeNumbers = new ArrayList<InvItem>();
     TextView tvTotItemVSum;
     TextView tvTotScanVSum;
     TextView tvMisItems;
     TextView tvNewItems;
 
     public String res = null;
     String loc_code = null;
 
     static Button btnVerSumBack;
     static Button btnVerSumCont;
     ProgressBar progressVerSum;
     boolean positiveButtonPressed = false;
     Activity currentActivity;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_ver_summary);
         registerBaseActivityReceiver();
         currentActivity = this;
 
         // Summary Fields
 
         tvTotItemVSum = (TextView) findViewById(R.id.tvTotItemVSum);
         tvTotScanVSum = (TextView) findViewById(R.id.tvTotScanVSum);
         tvMisItems = (TextView) findViewById(R.id.tvMisItems);
         tvNewItems = (TextView) findViewById(R.id.tvNewItems);
 
         // Code for tab
 
         TabHost tabHost = (TabHost) findViewById(android.R.id.tabhost);
         tabHost.setup();
 
         TabSpec spec1 = tabHost.newTabSpec("Tab 1");
         spec1.setContent(R.id.tab1);
         spec1.setIndicator("Scanned");
 
         TabSpec spec2 = tabHost.newTabSpec("Tab 2");
         spec2.setIndicator("Unscanned");
         spec2.setContent(R.id.tab2);
 
         TabSpec spec3 = tabHost.newTabSpec("Tab 3");
         spec3.setIndicator("New/Found");
         spec3.setContent(R.id.tab3);
 
         tabHost.addTab(spec1);
         tabHost.addTab(spec2);
         tabHost.addTab(spec3);
 
         // Find the ListView resource.
         ListView ListViewTab1 = (ListView) findViewById(R.id.listView1);
         ListView ListViewTab2 = (ListView) findViewById(R.id.listView2);
         ListView ListViewTab3 = (ListView) findViewById(R.id.listView3);
 
         // Setup Buttons and Progress Bar
         this.progressVerSum = (ProgressBar) findViewById(R.id.progressVerSum);
         VerSummaryActivity.btnVerSumBack = (Button) findViewById(R.id.btnVerSumBack);
         VerSummaryActivity.btnVerSumBack.getBackground().setAlpha(255);
         VerSummaryActivity.btnVerSumCont = (Button) findViewById(R.id.btnVerSumCont);
         VerSummaryActivity.btnVerSumCont.getBackground().setAlpha(255);
 
         // get Lists from intent of previous activity
 
         AllScannedItems = this.getInvArrayListFromJSON(getIntent()
                 .getStringArrayListExtra("scannedList"));
         missingItems = this.getInvArrayListFromJSON(getIntent()
                 .getStringArrayListExtra("missingList"));
         newItems = this.getInvArrayListFromJSON(getIntent()
                 .getStringArrayListExtra("newItems"));
         scannedBarcodeNumbers = this.getInvArrayListFromJSON(getIntent()
                 .getStringArrayListExtra("scannedBarcodeNumbers"));
         loc_code = getIntent().getStringExtra("loc_code");
         String summary = getIntent().getStringExtra("summary");
         try {
             JSONObject jsonObject = new JSONObject(summary);
             try {
                 tvTotItemVSum.setText(jsonObject.getString("nutotitems"));
 
             }
             catch (Exception e2) {
                 e2.printStackTrace();
             }
             try {
                 tvTotScanVSum.setText(jsonObject.getString("nuscanitems"));
 
             }
             catch (Exception e2) {
                 e2.printStackTrace();
             }
             try {
                 tvMisItems.setText(jsonObject.getString("numissitems"));
 
             }
             catch (Exception e2) {
                 e2.printStackTrace();
             }
             try {
                 tvNewItems.setText(jsonObject.getString("nunewitems"));
 
             }
             catch (Exception e2) {
                 e2.printStackTrace();
             }
         }
         catch (JSONException e) {
 
         }
 
         // summary =
         // "{\"nutotitems\":\""+numItems+"\",\"nuscanitems\":\""+AllScannedItems.size()+"\",\"numissitems\":\""+missingItems.size()+"\",\"nunewitems\":\""+newItems.size()+"\"}";
 
         TextView locCodeView = (TextView) findViewById(R.id.textView2);
         locCodeView.setText(Verification.autoCompleteTextView1.getText());
 
         // Create ArrayAdapter using the planet list.
         // Adapter listAdapter1 = new ArrayAdapter<String>(this,
         // android.R.layout.simple_list_item_1, AllScannedItems);
         // Adapter listAdapter2 = new ArrayAdapter<String>(this,
         // android.R.layout.simple_list_item_1,missingItems );
         // Adapter listAdapter3 = new ArrayAdapter<String>(this,
         // android.R.layout.simple_list_item_1,newItems );
         InvListViewAdapter listAdapter1 = new InvListViewAdapter(this,
                 R.layout.invlist_item, AllScannedItems);
         InvListViewAdapter listAdapter2 = new InvListViewAdapter(this,
                 R.layout.invlist_item, missingItems);
         InvListViewAdapter listAdapter3 = new InvListViewAdapter(this,
                 R.layout.invlist_item, newItems);
         // Set the ArrayAdapter as the ListView's adapter.
         ListViewTab1.setAdapter(listAdapter1);
         ListViewTab2.setAdapter(listAdapter2);
         ListViewTab3.setAdapter(listAdapter3);
     }
 
     public ArrayList<InvItem> getInvArrayListFromJSON(ArrayList<String> ar) {
         ArrayList<InvItem> returnList = new ArrayList<InvItem>();
         InvItem curInvItem;
         if (ar != null) {
             for (int x = 0; x < ar.size(); x++) {
                 JSONObject jsonObject;
                 try {
                     jsonObject = new JSONObject(ar.get(x));
                     curInvItem = new InvItem();
                     try {
                         curInvItem
                                 .setNusenate(jsonObject.getString("nusenate"));
                     }
                     catch (Exception e2) {
                         e2.printStackTrace();
                     }
                     try {
                         curInvItem.setCdcategory(jsonObject
                                 .getString("cdcategory"));
                     }
                     catch (Exception e2) {
                         e2.printStackTrace();
                     }
                     try {
                         curInvItem.setType(jsonObject.getString("type"));
                     }
                     catch (Exception e2) {
                         e2.printStackTrace();
                     }
                     try {
                         curInvItem.setDecommodityf(jsonObject
                                 .getString("decommodityf"));
                     }
                     catch (Exception e2) {
                         e2.printStackTrace();
                     }
                     returnList.add(curInvItem);
 
                 }
                 catch (JSONException e) {
                     // TODO Auto-generated catch block
                     Log.i("System.err",
                             "ERROR CONVERTING FROM ARRAYLIST OF JSON" + x + ":"
                                     + ar.get(x));
                     e.printStackTrace();
                 }
             }
         }
         return returnList;
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_ver_summary, menu);
         return true;
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         positiveButtonPressed = false;                
         // Setup Buttons and Progress Bar
         this.progressVerSum = (ProgressBar) findViewById(R.id.progressVerSum);
         VerSummaryActivity.btnVerSumBack = (Button) findViewById(R.id.btnVerSumBack);
         VerSummaryActivity.btnVerSumBack.getBackground().setAlpha(255);
         VerSummaryActivity.btnVerSumCont = (Button) findViewById(R.id.btnVerSumCont);
         VerSummaryActivity.btnVerSumCont.getBackground().setAlpha(255);
     }
 
     public void backButton(View view) {
         this.onBackPressed();
         /*
          * VerSummaryActivity.btnVerSumBack.getBackground().setAlpha(45);
          * Intent intent = new Intent(this, ListtestActivity.class);
          * startActivity(intent);
          * overridePendingTransition(R.anim.in_left, R.anim.out_right);
          */
 
     }
 
     public void noServerResponse() {
         AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
 
         // set title
         alertDialogBuilder.setTitle("NO SERVER RESPONSE");
 
         // set dialog message
         alertDialogBuilder
                 .setMessage(
                         Html.fromHtml("!!ERROR: There was <font color='RED'><b>NO SERVER RESPONSE</b></font>. <br/> Please contact STS/BAC."))
                 .setCancelable(false)
                 .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                 {
                     @Override
                     public void onClick(DialogInterface dialog, int id) {
                         // if this button is clicked, just close
                         // the dialog box and do nothing
                         Context context = getApplicationContext();
 
                         CharSequence text = "No action taken due to NO SERVER RESPONSE";
                         int duration = Toast.LENGTH_SHORT;
 
                         Toast toast = Toast.makeText(context, text, duration);
                         toast.setGravity(Gravity.CENTER, 0, 0);
                         toast.show();
 
                         dialog.dismiss();
                     }
                 });
 
         // create alert dialog
         AlertDialog alertDialog = alertDialogBuilder.create();
 
         // show it
         alertDialog.show();
     }
 
     public void continueButton(View view) {
         // Since AlertDialogs are asynchronous, need logic to display one at a time.
         if (foundItemsScanned()) {
             displayFoundItemsDialog();
         }
         else if (newItemsScanned()) {
             displayNewItemsDialog();
         }
         else {
             displayVerificationDialog();
         }
     }
 
     private void displayFoundItemsDialog() {
         AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         dialogBuilder.setTitle("Warning");
         dialogBuilder.setMessage(Html.fromHtml("<font color='RED'><b>**WARNING:</font> The " + numFoundItems() +
                 " Item/s found in OTHER</b> locations will be moved to the current location: <b>" + loc_code + "</b>. <br><br>" +
                "Continue with Verification Submission (Y/N)?"));
         dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
                 if (newItemsScanned()) {
                     displayNewItemsDialog();
                 }
                 else {
                     displayVerificationDialog();
                 }
             }
         });
 
         dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
 
         AlertDialog dialog = dialogBuilder.create();
         dialog.show();
     }
 
     private void displayNewItemsDialog() {
         AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
         dialogBuilder.setTitle("Warning");
         dialogBuilder.setMessage(Html.fromHtml("<font color='RED'><b>**WARNING:</font> The " + numNewItems() +
                 " NEW Items scanned will " + "NOT be tagged to location: " + loc_code + ".</b><br><br>" +
                 "Issue information for these items must be completed via the Inventory Issue Record E/U.<br><br>" +
                "Continue with Verification Submission (Y/N)?"));
         dialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
                 displayVerificationDialog();
             }
         });
 
         dialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
 
         AlertDialog dialog = dialogBuilder.create();
         dialog.show();
     }
 
     private void displayVerificationDialog() {
         AlertDialog.Builder confirmDialog = new AlertDialog.Builder(this);
         confirmDialog.setTitle("Verification Confirmation");
         confirmDialog.setMessage("Are you sure you want to submit this verification?");
         confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
 
                 if (positiveButtonPressed) {
                     /* Context context = getApplicationContext();
                      int duration = Toast.LENGTH_SHORT;
 
                      Toast toast = Toast.makeText(context,
                              "Button was already been pressed.", Toast.LENGTH_SHORT);
                      toast.setGravity(Gravity.CENTER, 0, 0);
                      toast.show();*/
                  }
                  else {
                     positiveButtonPressed = true;
                     submitVerification();
                  }                
             }
         });
 
         confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
             @Override
             public void onClick(DialogInterface dialog, int which) {
                 dialog.dismiss();
             }
         });
 
         AlertDialog dialog = confirmDialog.create();
         dialog.show();
     }
 
     private boolean newItemsScanned() {
         boolean exist = false;
         for (InvItem item : newItems) {
             if (item.getType().equalsIgnoreCase("NEW")) {
                 exist = true;
                 break;
             }
         }
         return exist;
     }
 
     private boolean foundItemsScanned() {
         boolean exist = false;
         for (InvItem item : newItems) {
             if (!item.getType().equalsIgnoreCase("NEW")) {
                 exist = true;
                 break;
             }
         }
         return exist;
     }
 
     private int numNewItems() {
         int numNewItems = 0;
         for (InvItem item : newItems) {
             if (item.getType().equalsIgnoreCase("NEW")) {
                 numNewItems++;
             }
         }
         return numNewItems;
     }
 
     private int numFoundItems() {
         return newItems.size() - numNewItems();
     }
 
     private void submitVerification() {
         VerSummaryActivity.btnVerSumCont.getBackground().setAlpha(45);
         progressVerSum.setVisibility(View.VISIBLE);
         // new VerSummeryActivity().sendJsonString(scannedBarcodeNumbers);
         // String jsonString = null;
         String status = null;
         // JSONArray jsArray = new JSONArray(scannedBarcodeNumbers);
 
         String barcodeNum = "";
         for (int i = 0; i < scannedBarcodeNumbers.size(); i++) {
             barcodeNum += scannedBarcodeNumbers.get(i).getNusenate() + ",";
         }
 
         // Create a JSON string from the arraylist
         /*
          * WORK ON IT LATER (SENDING THE STRING AS JSON) JSONObject jo=new
          * JSONObject();// =jsArray.toJSONObject("number"); try {
          * 
          * //jo.putOpt("barcodes",scannedBarcodeNumbers.toString());
          * jsonString=jsArray.toString(); } catch (Exception e) { // TODO
          * Auto-generated catch block e.printStackTrace(); }
          */
 
         // Send it to the server
 
         // check network connection
         ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
         NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
         if (networkInfo != null && networkInfo.isConnected()) {
             // fetch data
             status = "yes";
 
             AsyncTask<String, String, String> resr1;
             try {
                 // Get the URL from the properties
                 String URL = LoginActivity.properties.get("WEBAPP_BASE_URL")
                         .toString();
                 System.out.println(URL + "/VerificationReports?loc_code="
                         + loc_code + "&barcodes=" + barcodeNum);
                 resr1 = new RequestTask().execute(URL
                         + "/VerificationReports?loc_code=" + loc_code
                         + "&barcodes=" + barcodeNum);
 
                 try {
                     res = null;
                     res = resr1.get().trim().toString();
                     if (res == null) {
                         noServerResponse();
                         return;
                     }
                 }
                 catch (NullPointerException e) {
                     noServerResponse();
                     return;
                 }
 
             }
             catch (InterruptedException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             catch (ExecutionException e) {
                 // TODO Auto-generated catch block
                 e.printStackTrace();
             }
             status = "yes1";
         }
         else {
             // display error
             status = "no";
         }
 
         // Display Toster
         Context context = getApplicationContext();
         CharSequence text = res;
         int duration = Toast.LENGTH_LONG;
         if (res == null) {
             text = "!!!ERROR: NO RESPONSE FROM SERVER";
         }
         else if (res.length() == 0) {
             text = "Database not updated";
         }
         else {
             duration = Toast.LENGTH_SHORT;
         }
 
         Toast toast = Toast.makeText(context, text, duration);
         toast.setGravity(Gravity.CENTER, 0, 0);
         toast.show();
 
         // ===================ends
         Intent intent = new Intent(this, MenuActivity.class);
         startActivity(intent);
         overridePendingTransition(R.anim.in_right, R.anim.out_left);
     }
 
 }
