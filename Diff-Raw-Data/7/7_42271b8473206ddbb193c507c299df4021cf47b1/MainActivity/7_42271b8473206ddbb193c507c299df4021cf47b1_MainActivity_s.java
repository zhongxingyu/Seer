 package com.example.echattest1;
 
 import android.content.Intent;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.WindowManager;
 import android.widget.*;
 
 import java.util.ArrayList;
 
 public class MainActivity extends Activity {
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
         hideKeyBoardOnStart();
         EditText editText = (EditText)findViewById(R.id.editText1);
         editText.setText("random video : http://www.youtube.com/watch?v=J9rBIKHJoaY ");
         editText.setAutoLinkMask(1);
     }
 
     public void previewUrl (View v) {
 
         EditText editText = (EditText)findViewById(R.id.editText1);
         String tempText = editText.getText().toString();
         LinearLayout imageLayout = (LinearLayout) findViewById(R.id.imageLayout);
         imageLayout.removeAllViewsInLayout();
 
         GetUrls getUrls = new GetUrls();
         ArrayList<ThumbnailInfo> te1 = getUrls.extract(tempText); //gets all youtube urls from text
 
         TextView textView = (TextView)findViewById(R.id.textView2);
         textView.setAutoLinkMask(1);
         textView.setText(tempText);
 
         for(final ThumbnailInfo thumbnailInfo : te1) {
             if(thumbnailInfo.imageURL.equals(""))
                 continue;
            addImage(imageLayout,thumbnailInfo.imageURL,thumbnailInfo.enteredURL);
         }
 
     }
 
     public void hideKeyBoardOnStart() {
         getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     }
 
    public void addImage(LinearLayout imageLayout,String thumbNailSource,final String url) {
         ImageView iv = new ImageView(this);
         LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
         lp.gravity = Gravity.LEFT;
 
         iv.setOnClickListener(new View.OnClickListener() {
             public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_VIEW);
                 intent.setData(Uri.parse(url));
                 startActivity(intent);
             }
         });
 
         iv.setLayoutParams(lp);
         imageLayout.addView(iv);
         new GetThumbNail(iv).execute(thumbNailSource);
         Log.e("Logcat ","here1"+" "+thumbNailSource);
//            new GetThumbNailSrcCode(iv).execute(i.url2);
         View separatingView = new View(this);
         separatingView.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,5));
         separatingView.setBackgroundColor(0);
         imageLayout.addView(separatingView);
     }
 
 }
