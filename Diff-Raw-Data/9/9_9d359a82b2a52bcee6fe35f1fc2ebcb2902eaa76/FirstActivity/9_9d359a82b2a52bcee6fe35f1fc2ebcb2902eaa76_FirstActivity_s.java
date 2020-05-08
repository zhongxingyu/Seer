 package com.tw;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class FirstActivity extends Activity
 {
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.first_layout);
 
         TextView content =
                 (TextView) findViewById(R.id.content_view);
         content.setText("Android Series Chennai - First Activity");
         Button launchButton = (Button) findViewById(R.id.launch_activity);
 
         launchButton.setOnClickListener(new Button.OnClickListener() {
             @Override
             public void onClick(View view) {
//                Toast.makeText(FirstActivity.this, "You clicked on the view", Toast.LENGTH_SHORT).show();
                 Intent intent = new Intent(FirstActivity.this, SecondActivity.class);
                 intent.putExtra("content", "Second Activity started - click to view contact");
                 startActivity(intent);
             }
         });
 
     }
 }
