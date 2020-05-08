 package com.feedme.activity;
 
 import android.app.Activity;
 import android.view.*;
 import com.feedme.R;
 import android.content.Intent;
 import android.os.Bundle;
 import android.widget.*;
 import com.feedme.dao.BabyDao;
 import com.feedme.model.Baby;
 
 import java.util.Iterator;
 
 
 /**
  * User: dayel.ostraco
  * Date: 1/16/12
  * Time: 12:29 PM
  */
 public class FamilyHomeActivity extends Activity {
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.family_home);
         handleButtons();
 
         final BabyDao babyDao = new BabyDao(getApplicationContext());
         Baby[] myList = babyDao.getAllBabiesAsStringArray();
 
         TableLayout tl = (TableLayout)findViewById(R.id.myTableLayout);
 
         int j = 0;
         while (j < myList.length) {
             TableRow tr = new TableRow(this);  //create new row
 
             tr.setLayoutParams(new TableRow.LayoutParams(    //set params of row
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.FILL_PARENT));
 
             Button b = new Button(this);                    //create button for child
 
             if (myList[j].getSex().equals("male")) {     //change bg color of row and button
                 tr.setBackgroundColor(0xFF7ED0FF);
                 b.setBackgroundColor(0xFF7ED0FF);
             } else {
                 tr.setBackgroundColor(0xFFFF99CC);
                 b.setBackgroundColor(0xFFFF99CC);
             }
 
             b.setText(myList[j].getName());                     //put child's name on button
 
             b.setLayoutParams(new TableRow.LayoutParams(        //set params of button
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.FILL_PARENT));
 
             //button listener for each baby
             b.setOnClickListener(new View.OnClickListener() {
                 public void onClick(View v) {
 
                 }
             });
 
             /* Add Button to row. */
             tr.addView(b);
 
             /* Add row to TableLayout. */
             tl.addView(tr,new TableLayout.LayoutParams(
                     TableRow.LayoutParams.FILL_PARENT,
                     TableRow.LayoutParams.WRAP_CONTENT));
 
             j++; //iterator
         }
 
     }
 
     public void handleButtons() {
 
         //Add Baby Button
         Button addChild = (Button) findViewById(R.id.addChild);
         addChild.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), AddChildActivity.class);
                 startActivityForResult(intent, 2);
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.menu, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
 
         switch (item.getItemId()) {
             case R.id.family:
                 break;
             case R.id.home:
                 startActivity(new Intent(FamilyHomeActivity.this,
                         HomeActivity.class));
                 break;
         }
         return true;
     }
 
 }
