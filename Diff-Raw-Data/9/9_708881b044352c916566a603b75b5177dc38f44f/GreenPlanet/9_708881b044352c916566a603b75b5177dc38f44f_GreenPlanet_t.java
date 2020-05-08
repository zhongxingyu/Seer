 package com.example.greenplanet;
 
 import java.io.File;
 import java.util.Date;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.text.format.DateFormat;
 import android.view.View;
 import android.widget.CheckBox;
 import android.app.Activity;
 import android.content.Intent;
 
 public class GreenPlanet extends Activity {
 	
 	CheckBox UseGPSBox;	
 
 	//
     @Override
     public void onCreate(Bundle savedInstanceState) 
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.green_planet);
         
         UseGPSBox = (CheckBox) findViewById (R.id.checkBox1);
     }
                 
     ////****************************    ********************///////
        
     //  - 
     public void onReport(View view)
     {   
     	if (UseGPSBox.isChecked())
     		if (isGPSAvailable())
     		{   			
     			StaticContent.isGPSlocationState = true;
     			startGPSService();
     			startCamera();
     		}
     		else
     		{	gotoGPSSetting();  }
     	else
     	{	
     		// if LocationService already run - we need to stop him
     		StaticContent.isGPSlocationState = false;
     		startCamera();
     	}
     }
     
     //  - 
     public void onExit(View view) 
     {     	
     	System.exit(RESULT_OK); 
     }
     
     ////****************************     ********************///////   
     
     //
     private boolean isGPSAvailable() 
     {
     	LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
 		boolean GPS_enabled = locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
     	return GPS_enabled;
     }
      
     //
     private void gotoGPSSetting()
     {
     	Intent intent = new Intent(
     			android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);    		
     	startActivity(intent);    	
     }
     
     //
 	private void startGPSService()
 	{		
 		Intent getLocSer = new Intent(this,LocationService.class);
 	//	getLocSer.putExtra(Constants.SenderId,Constants.SenderID.Act_1);
 	//	getLocSer.putExtra(Constants.TaskName,Constants.StartLocationService);
 	    startService(getLocSer);
 	}
     
 	//
 	private void startCamera()
 	{		    	
 		Date d = new Date();
     	CharSequence cs  = DateFormat.format("yyyy-MM-dd kk.mm.ss",d.getTime());
     	StaticContent.Path = Environment.getExternalStorageDirectory().getAbsolutePath() 
    	                    		+ "/DCIM/Camera/" + cs.toString() + ".jpg"; 
 
     	File file = new File(StaticContent.Path); 
 	    Uri Picture = Uri.fromFile(file);
 	    
     	//             
 	    Intent pic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);    
 	    pic.putExtra(MediaStore.EXTRA_OUTPUT , Picture );			 
 	    startActivityForResult(pic,Constants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
 	    
 	}
 	
 	//
     protected void onActivityResult(int requestCode, int resultCode, Intent data) 
     {    
         if (requestCode ==  Constants.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)  
         {
             if (resultCode == RESULT_OK) 
             {   
             	Date dateTime = new Date();
             	StaticContent.ImageCaptureTime = (String)DateFormat.format("kk.mm.ss",dateTime.getTime());    		  
             	StaticContent.ImageCaptureDate = (String)DateFormat.format("yyyy-MM-dd",dateTime.getTime());
         	   
             	Intent pass = new Intent(this,SendActivity.class);
             //	pass.putExtra(Constants.SenderId,Constants.SenderID.Act_1);
             	startActivity(pass);
                 finish();                                              
             }
             else if (resultCode == RESULT_CANCELED) 
             {   /* User cancelled the image capture */  }            
         }       
     }
     
 }
