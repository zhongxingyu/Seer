 package com.qrsphere;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.qrsphere.database.QrcodeDataOperator;
 import com.qrsphere.login.LoginProcess;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.MotionEvent;
 import android.widget.TextView;
 
 public class SplashScreen extends Activity {
 
 	protected boolean _active = true;
     protected int _splashTime = 5000;
     
     TextView tv;
     String strIntent;
     boolean isLoginOK = false;
     String info;
    
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         
 //        ImageView iv = new ImageView(this);
 //        iv.setImageResource(R.drawable.q_logo);	
 //        iv.setBackgroundColor(Color.TRANSPARENT);
         setContentView(R.layout.splash);
         tv = (TextView) findViewById(R.id.splashTextView1);
         
     	final QrcodeDataOperator qdo = new QrcodeDataOperator(this);
     	String mac = new com.qrsphere.userinfo.CollectPhoneInformation(getApplication()).getMacAddress();
     	info = qdo.withdrawUserInfo(mac);
 
     	//tv.setText(info);
         Thread splashTread = new Thread() {
 
 			@Override
             public void run() {
                 try {
 //                    int waited = 0;
 //                    while(_active && (waited < _splashTime)) {
 //                        sleep(100);
 //                        if(_active) {
 //                            waited += 100;
 //                        }
                     
 //                }
                    isLoginOK = judgeAccountInfo(info);
                 } catch(Exception e) {
                     // do nothing
                 } finally {
 
 
                     finish();
                     // start mainActivity
                     startActivity(new Intent(getIntentStr()));
                     // stop(); //android does not support stop() any more, following code could be a way to exit
 //                  shouldContinue = false;
 //                  join();
               }
           }
       };
       splashTread.start();
   }
  
   @Override
   public boolean onTouchEvent(MotionEvent event) {
       if (event.getAction() == MotionEvent.ACTION_DOWN) {
           _active = false;
       }
       return true;
   }
   
   public String getIntentStr(){
 	  
   	if(!isLoginOK)
 		strIntent =  "com.qrsphere.login.LoginActivity";
 	else
		strIntent = "com.qrsphere.MainViewActivity";
   	
   	return strIntent;
   }
   
   @SuppressWarnings("unused")
 public boolean judgeAccountInfo(String jsonStr){
 	  try {
 		if (jsonStr.length() == 0)
 			return false;
 		else{
 			JSONObject json = new JSONObject(jsonStr);
 			if (json!=null){
 				String password = json.getString("Password");
 				if (password.length()>0){
 					if (LoginProcess.Login(json))
 						return true;
 					else
 						return false;
 				}
 				else
 					return false;
 			}
 			else
 				return false;
 		}
 	} catch (JSONException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 	  
 	  return false;
   }
  
   
 }
 
