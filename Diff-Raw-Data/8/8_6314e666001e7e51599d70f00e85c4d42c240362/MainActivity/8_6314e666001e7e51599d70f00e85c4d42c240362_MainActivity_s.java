 package com.example.devcon_exampl1;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class MainActivity extends Activity {
     private ImageView homeButton;
 
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
        //initFields();
     }
 
     private void initFields() {
         homeButton = (ImageView) findViewById(R.id.home_button);
         homeButton.setOnClickListener(new OnClickListener() {
 
             @Override
             public void onClick(View v) {
                 Toast toast = Toast.makeText(MainActivity.this, "Pulsado Home", Toast.LENGTH_SHORT);
                 toast.show();
             }
         });
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 Toast toast = Toast.makeText(MainActivity.this, "HOME", Toast.LENGTH_SHORT);
                 toast.show();
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
