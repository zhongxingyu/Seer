 package com.tware.anexter;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Vibrator;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 //import android.widget.Toast;
 
 public class vibrator extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private String LOG;
 
 
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.vibrator);
         this.setTitle("aNexter - 振动器测试");
         
         bPass = (Button)findViewById(R.id.btn_pass);
         bPass.setEnabled(false);
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "PASS|");
         		i.setClass(vibrator.this , sdcardRW.class);
         		startActivity(i);
         		vibrator.this.finish();
         	}
         });
         
         Button vibrator = (Button)findViewById(R.id.btn_vibrator);
         vibrator.setOnClickListener(new OnClickListener(){
         	@Override
         	public void onClick(View v)
         	{
     			long [] pattern = {100, 400, 100, 400};    
     			Vibrator iVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);
     	        iVibrator.vibrate(pattern, -1); 
     	        bPass.setEnabled(true);
     	        bFail.setEnabled(true);
         	}
         });
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setEnabled(false);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(vibrator.this , getResults.class);
         		startActivity(i);
         		vibrator.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "N/A|");
         		i.setClass(vibrator.this , sdcardRW.class);
         		startActivity(i);
         		vibrator.this.finish();
         	}
         });
         
         LOG = this.getIntent().getStringExtra("LOG");
 //        Toast.makeText(getApplicationContext(), LOG, Toast.LENGTH_SHORT).show();
     }
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		// TODO Auto-generated method stub
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
 }
