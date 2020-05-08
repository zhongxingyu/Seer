 package com.tware.anexter;
 
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ActivityNotFoundException;
 import android.content.BroadcastReceiver;
 import android.content.Context;
 import android.content.Intent;
 import android.content.IntentFilter;
 import android.content.SharedPreferences;
 import android.content.SharedPreferences.Editor;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.view.KeyEvent;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class getResults extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private String LOG;
 	private String [] strSplit = new String[200];
 	private int failNum = 0;
 	private int naNum   = 0;
 	private int batLevel = 0;
 	private Editor eLogSaver;
 	private SharedPreferences sPreferences;
 	private TextView resFail;
 	private TextView resNa;
 	
 	// for later use. reserved ...
 	private final int R_VKEY = 1;
 	private final int R_TIME = 2;
 	private final int R_BATT = 3;
 	private final int R_GENS = 4;
 	private final int R_ETH0 = 5;
 	private final int R_SDRW = 6;
 	private final int R_REC  = 7;
 	private final int R_DISP = 8;
 	private final int R_DRAW = 9;
 	private final int R_INPUT=10;
 	private final int R_WIFI =11;
 	private final int R_VIBRA=12;
 	private final int R_HDMI =13;
 	private final int R_DOCK =14;
 	private final int R_MP3	 =15;
 	private final int R_LCD  =16;
 	private final int R_SDPC =17;
 	private final int R_BLUE =18;
 	private final int R_CAM  =19;
 	private final int R_MEM	 =20;
 	private final int R_EMMC =21;
 	private final int R_MAC	 =22;
 	private final int R_MCU	 =23;
 	private final int R_IMG  =24;
 	private final int R_COMP =25;
 	private final int R_GYRO =26;
 	private final int R_SUS3 =27;
 //	private final int R_G3	 =28;  //removed @ 03/23/2012
 
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.results);
         this.setTitle("aNexter - 测试结果汇总");
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bNa = (Button)findViewById(R.id.btn_na);
         bPass = (Button)findViewById(R.id.btn_pass); 
         
         bPass.setText("上传测试记录");
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
         	public void onClick(View v)
         	{
         		//need code here.
         		try{
         			Intent i = new Intent();
         			i.putExtra("LOG", LOG + batLevel);
         			i.setClassName("com.tware.macup", "com.tware.macup.MACUpActivity");
         			startActivity(i);
         		}catch(ActivityNotFoundException e)
         		{
         			Toast.makeText(getApplicationContext(),
     					"Open MACUp Failed!, please USE above MACUp_1.4.apk",
     					Toast.LENGTH_SHORT)
     					.show();
         			return ;
         		}   
         	}
         });
         
         bFail.setVisibility(View.INVISIBLE);
         bNa.setVisibility(View.INVISIBLE);
         
         LOG = this.getIntent().getStringExtra("LOG");
 
         resFail = (TextView)findViewById(R.id.view_results);
         resNa = (TextView)findViewById(R.id.view_na);
         
     	
     	if (!LOG.equalsIgnoreCase("HISTORY"))
     	{
             showResults();
     		sPreferences = getSharedPreferences("LogSave",MODE_PRIVATE);
     		eLogSaver = sPreferences.edit();
     		eLogSaver.putString("logs", LOG);
     		eLogSaver.commit();
     	}else{
     		sPreferences = getSharedPreferences("LogSave",MODE_PRIVATE);
     		LOG = sPreferences.getString("logs", null);
     		
     		if (LOG == null || !showResults())
     		{
     			bPass.setEnabled(false);
         		new AlertDialog.Builder(getResults.this)
     				.setTitle("警告")
     				.setMessage("没有保存测试记录!")
     				.setPositiveButton("Ok", null).show();
     		}
     	}
     	
     	if (failNum != 0 )
     	{
     		bPass.setEnabled(false);
     		new AlertDialog.Builder(getResults.this)
     			.setIcon(R.drawable.icon)
 				.setTitle("警告")
 				.setMessage("请确认测试失败的项目!")
 				.setPositiveButton("Ok", null).show();			
 
     	}
     }
     
     private boolean showResults()
     {
         strSplit = LOG.split("\\|");
         if (strSplit == null)
         {
         	return false;
         }
     	for (int i =1; i< strSplit.length; i++)
     	{
     		if (strSplit[i].equals("FAIL"))
     		{
     			resFail.append("\n\n\tStep " + i + ": "+ getItemName(i));
     			failNum ++;
     		}else if (strSplit[i].equals("N/A"))
     		{
     			resNa.append("\n\n\tStep " + i + ": "+ getItemName(i));
     			naNum ++;    			
     		}   		
     	}
     	resFail.append("\n\n失败条数: " + failNum);
     	resNa.append("\n\n未测条数: " + naNum);
     	
     	return true;
     }
     
     private String getItemName(final int id)
     {
     	String strName;
     	
     	switch(id)
     	{
     		case  1: strName = "Virtual Keys"; break;
     		case  2: strName = "G-Sensor"; break;
     		case  3: strName = "Earphone/MP3/MP4"; break;
     		case  4: strName = "Sound Recorder"; break;
     		case  5: strName = "Date and Time"; break;
     		case  6: strName = "Battery Information";break;
 //    		case  5: strName = "Ethernet(USB to LAN)"; break;
     		case  7: strName = "Vibrator"; break;
     		case  8: strName = "SD R/W Test"; break;
     		case  9: strName = "LCD Brightness"; break;
     		case 10: strName = "WiFi & BT MAC"; break;
     		case 11: strName = "Draw Line"; break;
     		case 12: strName = "Input charactor"; break;
     		case 13: strName = "WiFi Connection"; break;
     		case 14: strName = "MCU"; break;
     		case 15: strName = "Image"; break;
     		case 16: strName = "U Disk"; break;
     		case 17: strName = "HDMI"; break;
 //    		case 18: strName = "Docking"; break;
     		case 18: strName = "LCD RGB"; break;
     		case 19: strName = "Connect to PC"; break;		
     		case 20: strName = "Bluetooth"; break;
     		case 21: strName = "Light Sensor"; break;    		
     		case 22: strName = "Front & Rear Camera"; break;
     		case 23: strName = "Memory Information"; break;
     		case 24: strName = "eMMC Size"; break;
     		case 25: strName = "Compass"; break;
     		case 26: strName = "Gyroscope"; break;
 //    		case 27: strName = "Suspend"; break;
 //    		case 28: strName = "3G"; break; //removed @ 03/23/2012
     		default: strName = "Unknown";break;	
     	}
     	return strName;
     }
     
 	private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
 		@Override
 		public void onReceive(Context arg0, Intent intent) {
 			String action = intent.getAction();
 			if (Intent.ACTION_BATTERY_CHANGED.equals(action)){
 				batLevel = intent.getIntExtra("level", 0);				
 				if (batLevel <= 75) bPass.setEnabled(false);					
 			}
 		}
 	};
 	
 	@Override
 	protected void onResume() {
 		super.onResume();
 		registerReceiver(mBatInfoReceiver, 
     		new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
   }
 
 	@Override
   	protected void onPause() {
 		super.onPause();
 		unregisterReceiver(mBatInfoReceiver);
   }
 
     
 	@Override
 	public boolean onKeyDown(int keyCode, KeyEvent event) {
 		if (keyCode == KeyEvent.KEYCODE_BACK) {
 			return true;
 		}
 		return super.onKeyDown(keyCode, event);
 	}
     
 	
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         super.onCreateOptionsMenu(menu);      
         menu.add(0, 1, 1, "退出");        
         return true;
     }
     
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         super.onOptionsItemSelected(item);
         switch(item.getItemId())
         {
     		case 0:
     			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
     			return true;
     		case 1:
    			getResults.this.finish();
     			return true;
         }
         return true;
     }
 
 	
 	
 }
