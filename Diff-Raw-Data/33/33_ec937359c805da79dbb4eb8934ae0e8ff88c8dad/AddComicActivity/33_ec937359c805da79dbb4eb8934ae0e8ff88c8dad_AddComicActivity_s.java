 package com.mike.comicreeder;
 
 import android.content.Context;
 import android.content.Intent;
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 import android.annotation.TargetApi;
 import android.os.Build;
 import android.view.View;
 import android.widget.EditText;
 
 import com.parse.ParseObject;
 
 public class AddComicActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_add_comic);
         // Show the Up button in the action bar.
         setupActionBar();
     }
 
 
     public void addComic(View view) {
 
      LayoutInflater inflater = (LayoutInflater)view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      View formView = inflater.inflate(R.layout.activity_add_comic, null);

      EditText comicNameText = (EditText)formView.findViewById(R.id.comicName);
      EditText writerText = (EditText)formView.findViewById(R.id.writer);
      EditText issueNumberText = (EditText)formView.findViewById(R.id.issueNum);
      EditText publisherText = (EditText)formView.findViewById(R.id.publisher);
 
       ParseObject comic = new ParseObject("Comic");
       comic.put("comicName", comicNameText.getText().toString());
       comic.put("Writer", writerText.getText().toString());
       comic.put("issue", issueNumberText.getText().toString());
       comic.put("publisher", publisherText.getText().toString());
 
       comic.saveInBackground();
 
       Intent intent = new Intent(this, ComicReederActivity.class);
       startActivity(intent);
     }
     /**
      * Set up the {@link android.app.ActionBar}, if the API is available.
      */
     @TargetApi(Build.VERSION_CODES.HONEYCOMB)
     private void setupActionBar() {
         if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
         getActionBar().setDisplayHomeAsUpEnabled(true);
         }
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.add_comic, menu);
         return true;
     }
     
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
