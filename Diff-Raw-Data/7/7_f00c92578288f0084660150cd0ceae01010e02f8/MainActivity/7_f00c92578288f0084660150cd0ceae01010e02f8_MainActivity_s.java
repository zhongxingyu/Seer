 package com.example.myfirstapp;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.net.Uri;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.content.Intent;
 import android.database.Cursor;
 import android.database.sqlite.SQLiteException;
 import android.widget.EditText;
 
 public class MainActivity extends Activity {
 
 	public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
 	
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
     }
 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
     
     /** Called when the user clicks the Send button */
     public void sendMessage(View view) {
     	Intent intent = new Intent(this, DisplayMessageActivity.class);
     	EditText editText = (EditText) findViewById(R.id.edit_message);
     	//String message = editText.getText().toString();
     	Integer rowNumReq = 0;
     	try {
     		rowNumReq = Integer.parseInt(editText.getText().toString());
     	}
     	catch (NumberFormatException ex)
     	{
     		/*rowNumReq = 0;*/
     		//action not required
     	}
     	String message = getSMS(rowNumReq);
     	//getSMS();
     	intent.putExtra(EXTRA_MESSAGE, message);
     	startActivity(intent);
     }
     
     public String getSMS(Integer rowNumReq) {
     	
     	StringBuilder smsBuilder = new StringBuilder();
         final String SMS_URI_INBOX = "content://sms/inbox";
         final String SMS_URI_ALL = "content://sms/";
          try {
              Uri uri = Uri.parse(SMS_URI_INBOX);  
              String[] projection = new String[] { "_id", "address", "person", "body", "date", "type" };
              String whereClause = "address=\"000019\"";
             Cursor cur = getContentResolver().query(uri, null/*projection*/, /*null*/whereClause, null, (rowNumReq > 0 ? " date " + "limit 0, " + rowNumReq.toString(): null));
               if (cur.moveToFirst()) {
                  int index_Address = cur.getColumnIndex("address");
                  int index_Person = cur.getColumnIndex("person");
                  int index_Body = cur.getColumnIndex("body");
                  int index_Date = cur.getColumnIndex("date");
                  int index_Type = cur.getColumnIndex("type");
                  do {
             	     String strAddress = cur.getString(index_Address);
                      int intPerson = cur.getInt(index_Person);
                      String strbody = cur.getString(index_Body);
                      long longDate = cur.getLong(index_Date);
                      int int_Type = cur.getInt(index_Type);
 
                      smsBuilder.append("[ ");
                      smsBuilder.append(strAddress + ", ");
                      smsBuilder.append(intPerson + ", ");
                      smsBuilder.append(strbody + ", ");
                     smsBuilder.append(longDate + ", ");
                      smsBuilder.append(int_Type);
                      smsBuilder.append(" ]\n\n");
                  } while (cur.moveToNext());
 
                  if (!cur.isClosed()) {
                      cur.close();
                      cur = null;
                  }
              } else {
                  smsBuilder.append("no result!");
              }
          }
          catch (SQLiteException ex) {
         	 String errMsg = ex.getMessage();
              System.out.println("SQLiteException" + ex.getMessage());
          }
          
          return smsBuilder.toString();
     }
     
 }
