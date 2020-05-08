 package com.ruthlessgames.ArkaPong;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Message;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 import com.badlogic.gdx.backends.android.AndroidApplication;
 import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
 
 
 
 public class MainActivity extends AndroidApplication implements AndroidBridge, OnClickListener {
 	
 	GameMain GameMain;
 	View GameView;
 	View RetryView;
 	private final int SHOW_RETRY = 0;
 	private final int SHOW_MENU = 1;
 	protected Handler MyHandler = new Handler(){
 		@Override
 		public void handleMessage(Message msg){
 			switch (msg.what){
 			case SHOW_RETRY:
 				RetryView.setVisibility(View.VISIBLE);
				
 				break;
 			case SHOW_MENU:
 				break;
 			}
 		}
 	};
 	RelativeLayout MainLayout;
 	
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
         MainLayout = new RelativeLayout(this);
         requestWindowFeature(Window.FEATURE_NO_TITLE);
         getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
         getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
         
         GameMain = new GameMain(this);
         AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
         cfg.useGL20 = true;
         cfg.useWakelock = true;
         
         
         GameView = initializeForView(GameMain, cfg);
         
         LayoutInflater inflater = getLayoutInflater();
         RetryView = inflater.inflate(R.layout.gameoverlayout, MainLayout,false);
         ((Button) RetryView.findViewById(R.id.btn_retry)).setOnClickListener(this);
         
         MainLayout.addView(GameView);
        RetryView.setVisibility(View.GONE);
         
         MainLayout.addView(RetryView);
         setContentView(MainLayout);
         
     }
 
 	@Override
 	public void SayHello() {
 		// TODO Auto-generated method stub
 		Toast.makeText(this, "Boas", Toast.LENGTH_SHORT).show();
 		
 	}
 
 	@Override
 	public void ShowGameOver() {
 		// TODO Auto-generated method stub
 		
 		MyHandler.sendEmptyMessage(SHOW_RETRY);
 		
 	}
 
 	@Override
 	public void onClick(View arg0) {
 		// TODO Auto-generated method stub
 		switch (arg0.getId())
 		{
 		case R.id.btn_retry:
 		{
 			RetryView.setVisibility(View.GONE);
 			GameMain.startlvl(1);
 			break;
 		}
 		case R.id.btn_menu:
 		{
 			break;
 		}
 		}
 	}
 }
