 package com.indexer.shorturl;
 
 import android.app.Activity;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import com.indexer.shorturl.tinyurl.tinyurl;
 
 import java.io.IOException;
 
 public class mainActivity extends Activity {
     /**
      * Called when the activity is first created.
      */
 
      String orignalurl;
    String shorturl;
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 
         TextView txtview = (TextView)findViewById(R.id.txturl);
         orignalurl = txtview.getText().toString();
 
 
         Button btnshort = (Button)findViewById(R.id.btnclick);
         btnshort.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
 
                 TextView txtshorturl = (TextView)findViewById(R.id.txturlshortcut);
                 try {
                     shorturl = tinyurl.urlShort(orignalurl);
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                txtshorturl.setText(shorturl);
 
 
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         });
 
 
 
 
     }
 
 
 }
