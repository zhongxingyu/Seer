 package com.example.testfeedbacklib2;
 
 import com.example.FeedbackLibrary.Screenshot;
 import com.example.FeedbackLibrary.Starter;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.TextView;
 
 public class  MainActivity extends Activity {
 
     private int cnt = 0;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         menu.add("Send feedback");
         getMenuInflater().inflate(R.menu.main, menu);
         return true;
     }
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         if (item.getItemId() == 0) {
             snap(findViewById(R.id.something1));
         }
         return true;
     }
 
     public void snap(View v) {
         (new Screenshot()).takeScreenShot(v.getRootView(), getFilesDir().getAbsolutePath());
        Starter starter = new Starter(this, "abhishekkadiyan@gmail.com", "****", "abhishekkadiyan@gmail.com");
         starter.start();
     }
 
     public void change(View v) {
         TextView textView = (TextView) findViewById(R.id.testText);
         cnt++;
         String temp = "";
         for (int i = 0; i < Math.min(cnt, 5); i++)
             temp += (" (-_-) ");
         if (cnt > 5) {
             temp += "I give up!\n";
             temp += "Why so serious?\n";
             temp += "I believe whatever doesn’t kill you simply makes you… stranger.\n ";
             temp += "See, I’m a man of simple tastes. I like dynamite…and gunpowder…and gasoline! Do you know what all of these things have in common? They’re cheap!\n";
             temp += "If you’re good at something, never do it for free.\n";
 
         }
         textView.setText(temp);
     }
 
 }
