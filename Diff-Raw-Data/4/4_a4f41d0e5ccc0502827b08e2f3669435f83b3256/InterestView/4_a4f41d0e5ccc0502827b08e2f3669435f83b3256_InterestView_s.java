 package com.gm375.vidshare;
 
 import java.util.ArrayList;
 
 import org.haggle.Attribute;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.text.Editable;
 import android.util.Log;
 import android.view.ContextMenu;
 import android.view.Gravity;
 import android.view.KeyEvent;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ListView;
 import android.widget.Toast;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 public class InterestView extends Activity {
     public static ArrayList<String> interests = new ArrayList<String>();
     private final ArrayList<String> deletedInterests = new ArrayList<String>();
     private final ArrayList<String> addedInterests = new ArrayList<String>();
     private ArrayAdapter<String> interestAdpt = null;
     private ListView interestListView;
     private EditText entry;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         setContentView(R.layout.interest_view);
         
         entry = (EditText) findViewById(R.id.entry);
         
         entry.setOnKeyListener(new View.OnKeyListener() {
             
             @Override
             public boolean onKey(View v, int keyCode, KeyEvent event) {
                 switch (keyCode) {
                 case KeyEvent.KEYCODE_ENTER:
                     if (entry.hasFocus()) {
                         parseEntry();
                     }
                     return true;
                 }
                 return false;
             }
         });
         
         interestListView = (ListView) findViewById(R.id.interest_list);
         
         synchronized(interests) {
             interestAdpt = new ArrayAdapter<String>(this, R.layout.list_text_item, interests);
         }
         interestListView.setAdapter(interestAdpt);
         
         registerForContextMenu(interestListView);
         
         final Button addButton = (Button) findViewById(R.id.add_button);
         addButton.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 parseEntry();
             }
         });
         
         final Button cancelButton = (Button) findViewById(R.id.cancel_button);
         cancelButton.setOnClickListener(new View.OnClickListener() {
             
             @Override
             public void onClick(View v) {
                 Log.d(Vidshare.LOG_TAG, "*** Cancel button clicked ***");
                 finish();
             }
         });
         
     }
 
     @Override
     public void onCreateContextMenu(ContextMenu menu, View v,
             ContextMenuInfo menuInfo) {
         
         menu.setHeaderTitle("Interests");
         menu.add("Delete");
         menu.add("Cancel");
     }
 
     @Override
     public boolean onContextItemSelected(MenuItem item) {
         AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        
        deleteInterest(info.position);
         
         return true;
     }
 
     @Override
     protected void onPause() {
         // TODO Auto-generated method stub
         super.onPause();
     }
 
     @Override
     protected void onStart() {
         // TODO Auto-generated method stub
         super.onStart();
     }
 
     @Override
     protected void onStop() {
         // TODO Auto-generated method stub
         super.onStop();
     }
 
     @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
         switch (keyCode) {
         case KeyEvent.KEYCODE_BACK:
             
             Log.d(Vidshare.LOG_TAG, "***Back keyed, creating bundle of added and deleted interests.***");
             
             Intent i = new Intent();
             
             if (addedInterests.size() != 0)
                 i.putExtra("added", addedInterests.toArray(new String[addedInterests.size()]));
             if (deletedInterests.size() != 0)
                 i.putExtra("deleted", deletedInterests.toArray(new String[deletedInterests.size()]));
             setResult(RESULT_OK, i);
             break;
         }
         return super.onKeyDown(keyCode, event);
     }
 
     public void parseEntry() {
         
         Editable ed = entry.getText();
         String interest = ed.toString();
         
         if (interest == null || interest.length() == 0)
             return;
         
         String[] split = interest.split(" ");
         
         for (int i = 0; i < split.length; i++) {
             
             String[] split2 = split[i].split(":");
             
             // FIXME: Original app had some stuff to do with weights in here. (Weights defined as "interestname:weight")
             if (split2.length == 2) {
                 interest = split2[0];
             }
             
             interest = interest.trim();
             Log.d(Vidshare.LOG_TAG, "***Entry: "+ interest +" ***");
             
             if (hasInterest(interest)) {
                 Toast toast = Toast.makeText(getApplicationContext(), "You already have the interest '" + interest + "'", Toast.LENGTH_SHORT);
                 toast.setGravity(Gravity.TOP|Gravity.CENTER, 0, 50);
                 toast.show();
                 continue;
             }
             
             addInterestToList(interest);
             
         } // end for
         ed.clear();
         
     }
     
     public void addInterestToList(String interest) {
         if (!deletedInterests.remove(interest))
             addedInterests.add(interest);
         interestAdpt.add(interest);
         interestAdpt.notifyDataSetChanged();
     }
     
     public boolean hasInterest(String interest) {
         return interestAdpt.getPosition(interest) >= 0;
     }
     
     // Called from Vidshare in a different thread.
     public static synchronized void setInterests(Attribute[] attrs) {
         interests.clear();
         
         for (int i = 0; i < attrs.length; i++) {
             interests.add(attrs[i].getValue());
         }
     }
     
     public void deleteInterest(String interest) {
         if (!addedInterests.remove(interest))
             deletedInterests.add(interest);
         interestAdpt.remove(interest);
         interestAdpt.notifyDataSetChanged();
     }
     
     public String deleteInterest(int pos) {
         String interest = interestAdpt.getItem(pos);
         
         if (interest != null) {
             if (!addedInterests.remove(interest))
                 deletedInterests.add(interest);
             interestAdpt.remove(interest);
             interestAdpt.notifyDataSetChanged();
         }
         return interest;
     }
 
 }
