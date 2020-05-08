 package ru.peppers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import model.Driver;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import android.widget.SimpleAdapter;
 
 public class MainListActivity extends Activity {
     private ListView lv;
     public SimpleAdapter simpleAdpt;
     public List<Map<String, String>> itemsList;
     private static final String MY_TAG = "My_tag";
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         //init();
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         init();
     }
 
     private void init() {
         // Bundle bundle = getIntent().getExtras();
         // int id = bundle.getInt("id");
 
         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
         nameValuePairs.add(new BasicNameValuePair("action", "get"));
         nameValuePairs.add(new BasicNameValuePair("module", "mobile"));
         nameValuePairs.add(new BasicNameValuePair("mode", "status"));
         nameValuePairs.add(new BasicNameValuePair("object", "driver"));
 
         Document doc = PhpData.postData(this, nameValuePairs,PhpData.newURL);
         if (doc != null) {
             Node responseNode = doc.getElementsByTagName("response").item(0);
             Node errorNode = doc.getElementsByTagName("message").item(0);
 
             if (responseNode.getTextContent().equalsIgnoreCase("failure"))
                 PhpData.errorFromServer(this, errorNode);
             else {
                 try {
                     parseMainList(doc);
                 } catch (Exception e) {
                 	Log.d("My_tag", e.toString());
                     errorHandler();
                 }
             }
         }
         else{
             initMainList();
         }
     }
 
     private void errorHandler() {
         new AlertDialog.Builder(this).setTitle(this.getString(R.string.error_title))
                 .setMessage(this.getString(R.string.error_message))
                 .setNeutralButton(this.getString(R.string.close), null).show();
     }
 
     private void parseMainList(Document doc) {
         int ordersCount = Integer.valueOf(doc.getElementsByTagName("ordercount").item(0).getTextContent());
        // int carClass = Integer.valueOf(doc.getElementsByTagName("carClass").item(0).getTextContent());
         int status = 2;
         Node statusNode = doc.getElementsByTagName("status").item(1);
         if(!statusNode.getTextContent().equalsIgnoreCase(""))
         	status = Integer.valueOf(statusNode.getTextContent());
         String district = doc.getElementsByTagName("districttitle").item(0).getTextContent();
         String subdistrict = doc.getElementsByTagName("subdistricttitle").item(0).getTextContent();
 
         // Bundle bundle = getIntent().getExtras();
         // int id = bundle.getInt("id");
         Driver driver = TaxiApplication.getDriver();
         driver.setStatus(status);
         //driver.setClassAuto(0);
         driver.setOrdersCount(ordersCount);
         driver.setDistrict(district);
         driver.setSubdistrict(subdistrict);
         initMainList();
     }
 
     private void initMainList() {
         final Driver driver = TaxiApplication.getDriver();
         if (driver != null) {
             itemsList = new ArrayList<Map<String, String>>();
             itemsList.add(createItem("item", this.getString(R.string.my_orders)+" " + driver.getOrdersCount()));
             itemsList.add(createItem("item", this.getString(R.string.status)+" " + driver.getStatusString()));
             itemsList.add(createItem("item", this.getString(R.string.free_orders)));
             if (driver.getStatus() != 1) {
                 itemsList.add(createItem("item", this.getString(R.string.region)+" " + driver.getFullDisctrict()));
             }
             itemsList.add(createItem("item", this.getString(R.string.call_office)));
             itemsList.add(createItem("item", this.getString(R.string.settings)));
             itemsList.add(createItem("item", this.getString(R.string.messages)));
             itemsList.add(createItem("item", this.getString(R.string.exit)));
 
             lv = (ListView) findViewById(R.id.mainListView);
 
             simpleAdpt = new SimpleAdapter(this, itemsList, android.R.layout.simple_list_item_1,
                     new String[] { "item" }, new int[] { android.R.id.text1 });
 
             lv.setAdapter(simpleAdpt);
             lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
                 public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long index) {
                     Bundle extras = getIntent().getExtras();
                     // //int id = extras.getInt("id");
                     Intent intent;
                     switch (position) {
                         case 0:
                             intent = new Intent(MainListActivity.this, MyOrderActivity.class);
                             startActivity(intent);
                             break;
                         case 1:
                             if (driver.getStatus() != 3) {
                                 intent = new Intent(MainListActivity.this, ReportActivity.class);
                                 startActivity(intent);
                             } else {
                                 intent = new Intent(MainListActivity.this, MyOrderActivity.class);
                                 startActivity(intent);
                             }
                             break;
                         case 2:
                             intent = new Intent(MainListActivity.this, FreeOrderActivity.class);
                             startActivity(intent);
                             break;
                         case 3:
                             //TODO:
                             //if (driver.getStatus() != 1) {
                                 intent = new Intent(MainListActivity.this, DistrictActivity.class);
                                 startActivity(intent);
                                 return;
                             //}
                         default:
                             break;
                     }
                     if (driver.getStatus() != 1)
                         position--;
                     if (position == 3) {
                         List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                         nameValuePairs.add(new BasicNameValuePair("action", "calloffice"));
                         // nameValuePairs.add(new BasicNameValuePair("id", String.valueOf(id)));
 
                         Document doc = PhpData.postData(MainListActivity.this, nameValuePairs);
                         if (doc != null) {
                             Node errorNode = doc.getElementsByTagName("error").item(0);
 
                             if (Integer.parseInt(errorNode.getTextContent()) == 1)
                                 //TODO:fix
                                 errorHandler();
                             else {
                                 errorHandler();
                             }
                         }
                     }
                     if (position == 4) {
                         intent = new Intent(MainListActivity.this, SettingsActivity.class);
                         startActivity(intent);
                     }
                     if (position == 5) {
                         intent = new Intent(MainListActivity.this, MessageActivity.class);
                         startActivity(intent);
                     }
                     if (position == 6) {
                         Driver driver = TaxiApplication.getDriver();
                         if (driver.getOrdersCount() != 0) {
                             exitDialog();
                         }
                     }
                 }
 
             });
         }
     }
 
     private void exitDialog() {
         new AlertDialog.Builder(MainListActivity.this).setTitle(this.getString(R.string.orders))
                 .setMessage(this.getString(R.string.sorry_exit))
                 .setPositiveButton(this.getString(R.string.exit_action), new DialogInterface.OnClickListener() {
 
                     @Override
                     public void onClick(DialogInterface dialog, int which) {
 
                         Intent intent = new Intent(MainListActivity.this, PhpService.class);
                         stopService(intent);
                         finish();
                     }
 
                 }).setNegativeButton(this.getString(R.string.cancel), null).show();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         // Handle the back button
         if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
             // Ask the user if they want to quit
             Driver driver = TaxiApplication.getDriver();
             if (driver.getOrdersCount() != 0) {
                 exitDialog();
             }
             return true;
         } else {
             return super.onKeyDown(keyCode, event);
         }
 
     }
 
     public HashMap<String, String> createItem(String key, String name) {
         HashMap<String, String> item = new HashMap<String, String>();
         item.put(key, name);
 
         return item;
     }
 
 }
