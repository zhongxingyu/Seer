 package com.feedme.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import com.feedme.R;
 
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
     }
 
     public void handleButtons() {
 
         //Select Baby Button
         Button selectChild = (Button) findViewById(R.id.selectChild);
         selectChild.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), SelectChildListActivity.class);
                 startActivityForResult(intent, 1);
             }
         });
 
         //Add Baby Button
         Button addChild = (Button) findViewById(R.id.addChild);
         addChild.setOnClickListener(new View.OnClickListener() {
             public void onClick(View v) {
                 Intent intent = new Intent(v.getContext(), AddChildActivity.class);
                 startActivityForResult(intent, 2);
             }
         });
     }
 }
