 package com.example.greenplanet;
 
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Bundle;
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.ActivityManager.RunningServiceInfo;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class SendActivity extends Activity {
 
 	Button Send;
     ImageView image = null;	
      
 	// 
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.send_activity); 
         
         Send = (Button) findViewById (R.id.button1);        
         image = (ImageView) findViewById (R.id.imageView1); 
         
        Bitmap bm = BitmapFactory.decodeFile(StaticContent.Path); 
         image.setImageBitmap(bm); 
                
         if (!StaticContent.isGPSlocationState) //     3    
         	Send.setText("");       
     }
     
     ////****************************    ********************///////
     
     //  - /
     public void onSend(View view)
     {
     	if (!StaticContent.isGPSlocationState) //    
     	{
 		  	Intent loc = new Intent(this,AddressActivity.class);
 	//	  	loc.putExtra(Constants.SenderId,Constants.SenderID.Act_2);
 		    startActivity(loc);
 		   	finish();   
     	}
     	else //   	
     	{
     		if (isMyServiceRunning(Constants.COMPONENT_ID.LOCATION_SERVICE))
     		{	    			
 	            StaticContent.sendBtnState = true;	          
 	            finish();
     			
 	            //        :    "  -        
     			//try {  Thread.sleep(10000);	/* wait 10 second */     }
     			//catch (InterruptedException e)  {    /*         */     }
 	            //if (isMyServiceRunning(Constants.SenderID.Loc_Ser)) {  }
     		}
     		else // the LocationService stoped - success/failure
     		{	
     			if (isLocationServiceEndedSuccessfully())
     			{
         			if (isNetworkAvailable())
         			{
             			Intent getUploadSer = new Intent(this,UploadService.class);
                 		//	getUploadSer.putExtra(Constants.SenderId,Constants.SenderID.Act_2);
                 		//	getUploadSer.putExtra(Constants.TaskName,Constants.StartUploadService);					
                 		startService(getUploadSer);	
                 		finish();
         			}
         			else // Network Disavailable
         				gotoNetworkSetting();
     			}
     			else // ServiceLocation ended in failure 
     			{
     				Toast.makeText(this, "need to add address", Toast.LENGTH_LONG).show();
     			  	Intent loc = new Intent(this,AddressActivity.class);
     				//	  	loc.putExtra(Constants.SenderId,Constants.SenderID.Act_2);
     			    startActivity(loc);
     			    finish();   
     			}
     		}    		
 			
     	}
     }
 
 	//  - 
     public void onBack(View view)
     {
     	Intent back = new Intent(this,GreenPlanet.class);
    // 	back.putExtra(Constants.SenderId,Constants.SenderID.Act_2);
     	startActivity(back);   
     	finish();      
     }
     
     ////****************************     ********************///////
 	
     //
     //   NETWORK_STATE
     private boolean isNetworkAvailable() 
     {
         ConnectivityManager conManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
         NetworkInfo Network_Enabled = conManager.getActiveNetworkInfo();
         return (Network_Enabled != null);
     }
       
     //
     private void gotoNetworkSetting()
     {
     	Intent intent = new Intent(
 			android.provider.Settings.ACTION_WIFI_SETTINGS);    		
     	startActivity(intent);    	
     }
     
     //
     private boolean isMyServiceRunning(String serviceName) 
     {
         ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
         for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) 
         {
             if (serviceName.equals(service.service.getClassName())) 
                 return true;       
         }
         return false;  
     }
      
     //
     private boolean isLocationServiceEndedSuccessfully() 
     {
     	return StaticContent.locationServiceEndedSuccessfully;
 	}
     
 }
