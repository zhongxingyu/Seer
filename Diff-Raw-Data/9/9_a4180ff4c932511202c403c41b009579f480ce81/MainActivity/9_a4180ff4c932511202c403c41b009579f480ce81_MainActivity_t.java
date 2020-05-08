 package com.protech.ascension.gui;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.AdapterView;
 import android.widget.ListView;
 import com.protech.ascension.R;
import com.protech.ascension.gui.chapter1.Chapter1FragmentActivity;
 
 public class MainActivity extends Activity
 {
     private static final int CHAPTER_1 = 0;
     private static final int CHAPTER_2 = 1;
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         ListView lv = (ListView) findViewById(R.id.chapter_list_view);
         lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
             public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Intent intent = null;
                 switch (position) {
                     case CHAPTER_1:
                         intent = new Intent(parent.getContext(), Chapter1FragmentActivity.class);
                         break;
                     case CHAPTER_2:
 //                        intent = new Intent(parent.getContext(), Chapter2FragmentActivity.class);
                         break;
                     default:
                 }
                 if (intent != null) {
                     startActivity(intent);
                 }
             }
         });
     }
 }
