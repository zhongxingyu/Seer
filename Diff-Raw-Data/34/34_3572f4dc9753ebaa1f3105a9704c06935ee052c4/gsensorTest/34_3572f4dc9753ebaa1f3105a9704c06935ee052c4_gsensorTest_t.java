 package com.tware.anexter;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.ToggleButton;
 
 public class gsensorTest extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private ToggleButton bOff;
 	
 	private String LOG;
 	private int isChanged = 0;	
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.gsensor);
         this.setTitle("aNexter - G-Sensor 测试");
       
         bPass = (Button)findViewById(R.id.btn_pass);
         bPass.setEnabled(false);
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "PASS|");
         		i.setClass(gsensorTest.this , mp3.class);
         		startActivity(i);
        		gsensorTest.this.finish();
         	}
         });
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(gsensorTest.this , getResults.class);
         		startActivity(i);
        		gsensorTest.this.finish();
         	}
         });
         
         bOff = (ToggleButton)findViewById(R.id.btn_rotation);
         bOff.setOnClickListener(new OnClickListener(){
         	@Override
         	public void onClick(View v)
         	{
         		if (bOff.isChecked())
         		{
         			Settings.System.putInt(getContentResolver(),
         					Settings.System.ACCELEROMETER_ROTATION,
         					1);
         		}else{
         			Settings.System.putInt(getContentResolver(),
         					Settings.System.ACCELEROMETER_ROTATION,
         					0);
         		}
         	}
         });
         
         if (!bOff.isChecked())
         {
         	Settings.System.putInt(getContentResolver(),
 					Settings.System.ACCELEROMETER_ROTATION,
 					0);
         }
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setVisibility(View.INVISIBLE);
         
         LOG = this.getIntent().getStringExtra("LOG");
 //        Toast.makeText(getApplicationContext(), LOG, Toast.LENGTH_SHORT).show();
     }
 
     @Override
 	public void onConfigurationChanged(Configuration newConfig) {
 	  super.onConfigurationChanged(newConfig);
 	  if ( bOff.isChecked() && ++isChanged == 2)
 	  {
 		Intent i = new Intent();
 	  	i.putExtra("LOG", LOG + "PASS|");
 	  	i.setClass(gsensorTest.this , mp3.class);
 	  	startActivity(i);
	  	gsensorTest.this.finish();
 	  }
 	}
     
     @Override
     public void onDestroy()
     {
     	super.onDestroy();
 		Settings.System.putInt(getContentResolver(),
 				Settings.System.ACCELEROMETER_ROTATION,
 				1);
     }
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
     
