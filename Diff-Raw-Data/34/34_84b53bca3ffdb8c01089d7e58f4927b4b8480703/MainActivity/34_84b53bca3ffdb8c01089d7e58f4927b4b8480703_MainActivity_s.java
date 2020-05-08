 package com.example.handler02;
 
 import android.os.Bundle;
 import android.os.Handler;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ProgressBar;
 import android.widget.TextView;
 
 /**
  * demonstrates the handler implementation, using Runnable
  * 
  * @author Lothar Rubusch
  */
 public class MainActivity extends Activity {
 	private int cnt=0;
 
 	private TextView tv_msg;
 	private Button bt_click;
 	private boolean toggle = false;
 
 	// handler access
 	private Handler handler = new Handler();
 
 	// runnable implementation
 	private final Runnable timed_task = new Runnable(){
 
 		@Override
 		public void run() {
 			++cnt;
 			tv_msg.setText( String.valueOf( cnt ));
			handler.postDelayed( timed_task, 500 );
 		}
 	};

	private boolean stopRunnable = false;
 	
 	// Thread using Runnable and Handler (explicit solution)
 	public Thread thread = new Thread(){
		
 		@Override
 		public void run(){
 			try{
 				while( true ){
					// wait befor starting new thread
 					sleep(500);
					if( stopRunnable ){
 						handler.removeCallbacks( timed_task );
						return;
 					}
					handler.post(timed_task);
 				}
 			}catch( InterruptedException e){
 					e.printStackTrace();
 			}
 		}
 	};
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		// init
 		this.toggle = false;
 
 		this.bt_click = (Button) findViewById( R.id.bt_click );
 		this.bt_click.setOnClickListener( new Button.OnClickListener(){
 			@Override
 			public void onClick(View v) {
				if( toggle = toggle ? false : true ){
					thread.start();
 // TODO check, is this ok for the TextView as UI element?
 //					handler.post( timed_task );
				}else{
					stopRunnable = true;
 //					handler.removeCallbacks( timed_task );
				}
 			}
 			
 		});
 		
 		this.tv_msg = (TextView) findViewById( R.id.tv_msg );
 	}
 /*
 	public void startProgress( View v ){
 		Runnable runnable = new Runnable(){
 			@Override
 			public void run(){
 				for( int idx = 0; idx <= 10; ++idx ){
 					final int value = idx;
 					try{
 						Thread.sleep( 2000 );
 					}catch( InterruptedException e ){
 						e.printStackTrace();
 					}
 					pb.post( new Runnable(){
 						@Override
 						public void run(){
 							tv_msg.setText("updating");
 							pb.setProgress( value );
 						}
 					});
 				}
 			}
 		};
 		new Thread( runnable ).start();
 	}
 //*/
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
