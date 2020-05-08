 package se.chalmers.project14.main;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 
 public class Splash extends Activity{
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.splash);
 		Thread timer = new Thread(){
 			public void run(){
 				try{
 					sleep(3000);
 				} catch (InterruptedException e){
 					e.printStackTrace();
 				}finally{
					Intent openMain = new Intent("android.support.PARENT_ACTIVITY");
 					startActivity(openMain);
 				}
 			}
 		};
 		timer.start();
 	}
 
 }
