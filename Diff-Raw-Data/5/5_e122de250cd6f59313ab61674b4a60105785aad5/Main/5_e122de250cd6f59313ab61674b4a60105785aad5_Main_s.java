 package org.shokai.goldfish;
 
 import jp.co.topgate.android.nfc.TagWrapper;
 import android.app.Activity;
 import android.content.Intent;
 import android.nfc.NfcAdapter;
 import android.os.Bundle;
 import android.os.Parcelable;
 import android.util.*;
 import android.widget.TextView;
 
 public class Main extends Activity {
     private TextView textViewTag;
     
     @Override
     public void onCreate(Bundle savedInstanceState) {        
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         trace("start");
         textViewTag = (TextView)findViewById(R.id.textViewTag);
         resolveIntent(this.getIntent());
     }
 
     void resolveIntent(Intent intent) {
         String action = intent.getAction();
         trace(action);
         if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
             try{
                 Parcelable tag = intent.getParcelableExtra("android.nfc.extra.TAG");
                 TagWrapper tw = new TagWrapper(tag);
                 String id = tw.getHexIDString();
                 textViewTag.setText("TAG : "+id);
                 trace(id);
             }
             catch(Exception e){
                 trace(e);
                 textViewTag.setText("TAG error");
             }
         }
        else{
            textViewTag.setText("no TAG");
        }
     }
     
     void trace(Object message){
         Log.v("GoldFish", message.toString());
     }
 }
