 package com.luckywc.jossstick;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.Intent;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.ImageView;
 
 public class StartPage extends Activity {
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_start_page);
 		((ImageView)findViewById(R.id.mian_qiuqian)).setOnClickListener(new OnClickListener(){
 			@Override
 			public void onClick(View arg0) {
 				// TODO Auto-generated method stub
 				Intent i=new Intent(StartPage.this,GetLotActivity.class);
 				startActivity(i);
				StartPage.this.overridePendingTransition(R.anim.activity_open, R.anim.activity_close);
 			}			
 		});
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.start_page, menu);
 		return true;
 	}
 
 }
