 package com.vgershman.whocall.activity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.widget.FrameLayout;
 import android.widget.ImageButton;
 import android.widget.TextView;
 import com.vgershman.whocall.R;
 import com.vgershman.whocall.database.PhonesManager;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vkosarev
  * Date: 25.02.13
  * Time: 17:51
  * To change this template use File | Settings | File Templates.
  */
 public class NoResultActivity extends BaseActivity {
 
     private TextView searchNumber;
     private FrameLayout shareButton;
     private TextView searchCounter;
     private TextView regionInfo;
     private TextView operatorInfo;
 
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.no_result_activity);
         initView();
     }
 
     private void initView() {
         ImageButton navBack = (ImageButton)findViewById(R.id.nav_back);
         navBack.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 onBackPressed();
             }
         });
         searchNumber = (TextView)findViewById(R.id.searchNumber);
         shareButton = (FrameLayout)findViewById(R.id.shareButton);
         shareButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 Intent intent = new Intent(NoResultActivity.this,TellAboutActivity.class);
                 intent.putExtra("myself",false);
                 startActivity(intent);
             }
         });
 
         searchCounter = (TextView)findViewById(R.id.searchCounter);
         regionInfo = (TextView)findViewById(R.id.regionInfo);
         operatorInfo = (TextView)findViewById(R.id.operatorInfo);
 
     }
 
     @Override
     protected void onResume() {
         super.onResume();
         bindInfo();
         new PhonesManager(this).addPhone(getIntent().getStringExtra("phone"));
     }
 
     private void bindInfo() {
         searchNumber.setText(getIntent().getStringExtra("phone"));
         searchCounter.setText("Этот номер искали "+getIntent().getStringExtra("count")+" раз(а)");
         regionInfo.setText(getIntent().getStringExtra("region"));
         operatorInfo.setText(getIntent().getStringExtra("operator"));
     }
 
     @Override
     public void onBackPressed() {
         super.onBackPressed();
         finish();
     }
 }
