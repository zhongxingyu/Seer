 package com.tware.anexter;
 
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.media.MediaPlayer;
 import android.os.Build;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.KeyEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.Toast;
 
 public class mp3 extends Activity {
 	private Button bPass;
 	private Button bFail;
 	private Button bNa;
 	private String LOG;
 	private Button bPlay;
 	
 	private String sdPath = "/mnt/";
 	private List<String> playList = new ArrayList<String>();
 	private MediaPlayer mPlayer = new MediaPlayer();
 	
 	/** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.mp3);
         this.setTitle("aNexter - MP3/MP4 耳机 测试");
         
         Button iFile = (Button)findViewById(R.id.btn_ifile);
         iFile.setOnClickListener(new OnClickListener(){
         	@Override
         	public void onClick(View v)
         	{
     			try{
     				if (Environment.getExternalStorageState().equals(  
                             Environment.MEDIA_MOUNTED)) 
     				{ 
     					Intent i = new Intent();	
     					i.setClassName("com.oem.iFileManager", "com.oem.iFileManager.iFileManager");
     					startActivity(i);
     					bPass.setEnabled(true);
     					bFail.setEnabled(true);
     				}else{
         				Toast.makeText(getApplicationContext(),
             					"No SD Card",
             					Toast.LENGTH_SHORT)
             					.show();		
     				}
     			}catch(ActivityNotFoundException e)
     			{
     				try{
     					Intent i = new Intent();
     					i.setClassName("com.fb.FileBrower", "com.fb.FileBrower.FileBrower");
     					startActivity(i);
         		    	bFail.setEnabled(true);
         		    	bPass.setEnabled(true);
     				}catch(ActivityNotFoundException e1){
     					Toast.makeText(getApplicationContext(),
     							"Open Filemanager Failed!",
     							Toast.LENGTH_SHORT)
     							.show();
     					return ;
     				}
     			}    			
         	}
         });
         
         bPass = (Button)findViewById(R.id.btn_pass);
         bPass.setEnabled(false);
         bPass.setOnClickListener(new OnClickListener(){
         	@Override
 			public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "PASS|");
 //        		i.setClass(mp3.this , brightness.class);  //2012-04-03: removed
         		i.setClass(mp3.this , recording.class);
         		startActivity(i);
         		if (bPlay.getText().equals("停止播放") || 
         			bPlay.getText().equals("开始播放"))
         		{
         			mPlayer.stop(); mPlayer.release();
         		}
         		mp3.this.finish();
         	}
         });
         
         LOG = this.getIntent().getStringExtra("LOG");
 //        Toast.makeText(getApplicationContext(), LOG, Toast.LENGTH_SHORT).show();
         
         bFail = (Button)findViewById(R.id.btn_fail);
         bFail.setEnabled(false);
         bFail.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
         		Intent i = new Intent();
         		i.putExtra("LOG", LOG + "FAIL|");
         		i.setClass(mp3.this , getResults.class);
         		startActivity(i);
         		if (bPlay.getText().equals("停止播放") || 
             			bPlay.getText().equals("开始播放"))
             		{
             			mPlayer.stop(); mPlayer.release();
             		}
         		mp3.this.finish();
         	}
         });
         
         bNa = (Button)findViewById(R.id.btn_na);
         bNa.setVisibility(View.INVISIBLE);
         
         bPlay = (Button)findViewById(R.id.btn_play);
         bPlay.setOnClickListener(new OnClickListener(){
         	@Override
     		public void onClick(View v)
         	{
 				
 				if (Build.VERSION.SDK_INT == 7)		sdPath = "/sdcard/";              	
 				
                 if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
                 {
                 	Toast.makeText(getApplicationContext(), 
                 			"请确认SD卡是否已插入机台！", Toast.LENGTH_SHORT).show();
                 	return ;
                 }
 				
 				if (bPlay.getText().equals("停止播放"))
                 	{ bPlay.setText("开始播放"); mPlayer.stop(); return ;}
                 
 				playList.clear();
 				
             	Toast.makeText(getApplicationContext(), "开始搜索SD卡中的音乐文件，请稍后……",
             			Toast.LENGTH_SHORT).show();
                 if (findMusic(sdPath))
                 {
                 	bPlay.setText("停止播放");
                 	Random  rand = new Random(System.currentTimeMillis());
                 	try {
                 		int id = rand.nextInt(playList.size());
                 		mPlayer.reset();
 						Toast.makeText(getApplicationContext(), 
 								"正在播放: "+ playList.get(id), 
 								Toast.LENGTH_SHORT).show();
                 		mPlayer.setDataSource(playList.get(id));
 						mPlayer.prepare();
 						mPlayer.setLooping(true);
 						int Duration = mPlayer.getDuration();
 						mPlayer.seekTo(Duration/3);
 						mPlayer.start();
 
 						bPass.setEnabled(true);    bFail.setEnabled(true);
 					} catch (IllegalArgumentException e) {
 						e.printStackTrace();
 					} catch (IllegalStateException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
                 }else
                 {
                 	Toast.makeText(getApplicationContext(), "没有找到SD卡中的音乐",
                 			Toast.LENGTH_SHORT).show();
                 }
         	}
         });	
     }
     
     public boolean findMusic(String sdPath1)
     {
     	File f = new File(sdPath1);
     	if (!f.exists()) return false;
     	    	
     	try{
     		File[] fl = f.listFiles();
     		if (fl.length == 0 )
     		{
     			return false;
     		}
     	
     		for (int i = 0; i< fl.length; i++)
     		{
     			if (fl[i].isDirectory())   findMusic(fl[i].getAbsolutePath());
     			if (fl[i].toString().toLowerCase().endsWith(".mp3"))
     			{
     			playList.add(fl[i].getAbsoluteFile().toString());
     			}
     		}
     	}
     	catch (Exception e)
     	{
     		return false;
     	}
     	
     	if (!playList.isEmpty())
     	{
     		return true;
     	}
     	else
     	{
     		return false;
     	}
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
