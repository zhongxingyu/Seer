 package com.github.egslava.git_eclipse_test;
 
import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 
 public class MainActivity extends Activity 
                         implements OnClickListener{
 
     private Button helloButton;
     private TextView helloTextView;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         helloButton = (Button) findViewById(R.id.helloButton);
         helloTextView = (TextView) findViewById(R.id.helloTextView);
         helloButton.setOnClickListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public void onClick(View arg0){
         switch(arg0.getId()){
             case R.id.helloButton:
                 helloTextView.setText("You've clicked the button");
             break;
         }
     }
     
 }
