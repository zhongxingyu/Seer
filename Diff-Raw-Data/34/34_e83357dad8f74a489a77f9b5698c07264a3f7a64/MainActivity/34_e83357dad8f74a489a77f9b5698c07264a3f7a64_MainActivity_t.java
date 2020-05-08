 package com.oukasoft.ServiceBindSample;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.util.Log;
 import android.view.Gravity;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.os.IBinder;
 
 public class MainActivity extends Activity {
 
 	private final int WC = ViewGroup.LayoutParams.WRAP_CONTENT; 
 
	// レイアウト
 	LinearLayout ll;
 
     Button btnBind;
     Button btnUnBind;
     
     Button btnFunc;
    // Serviceの保存
     TestBindService mService;
     boolean         connectionStatus;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         Log.i("MainActivity", "onCreate");
         ll = new LinearLayout(this);
         ll.setGravity( Gravity.CENTER_VERTICAL);
         ll.setOrientation( LinearLayout.VERTICAL );
         
         btnBind   = new Button(this);
         btnUnBind = new Button(this);
         
         btnFunc   = new Button(this);
         
         btnBind.setOnClickListener( new ServiceOnClickListener() );
         btnUnBind.setOnClickListener( new ServiceOnClickListener() );
         btnFunc.setOnClickListener( new ServiceOnClickListener() );
         
         btnBind.setText("Service Onbind");
         btnUnBind.setText("Service Unbind");
        btnFunc.setText("Service 適当な関数");
         
         ll.addView( btnBind ,  WC );
         ll.addView( btnUnBind, WC );
         ll.addView( btnFunc , WC );
         setContentView(ll);
 
 	}
 
	// コネクション作成
 	private ServiceConnection connection = new ServiceConnection() {	
 		@Override
 		public void onServiceConnected(ComponentName className, IBinder service) {
			// サービス接続時に呼ばれる
 			Log.i("ServiceConnection", "onServiceConnected");
			// バインダーを保存
 			mService = ((TestBindService.BindServiceBinder)service).getService();
 		}
 		@Override
 		public void onServiceDisconnected(ComponentName arg0) {
			// サービス切断時に呼ばれる
 			Log.i("ServiceConnection", "onServiceDisconnected");
 			mService = null;
 		}
 	};
 
     class ServiceOnClickListener implements OnClickListener{
 
 		@Override
 		public void onClick(View view) {
 			if( view == btnBind ){
				// バインド開始
 				bindService( new Intent( MainActivity.this, TestBindService.class ) ,
 						     connection,
 						     Context.BIND_AUTO_CREATE 
 						     );
 				connectionStatus = true;
 			}else if( view == btnUnBind ){
 				if( connectionStatus ){
					// バインドされている場合、バインドを解除
 					unbindService( connection );
 					connectionStatus = false;
 				}
 			}else if( view == btnFunc ){
 				if( connectionStatus ){
					// 適当な関数を呼び出し
 					mService.TestFunction();
 				}
 			}
 		}
 
     }
 
 }
