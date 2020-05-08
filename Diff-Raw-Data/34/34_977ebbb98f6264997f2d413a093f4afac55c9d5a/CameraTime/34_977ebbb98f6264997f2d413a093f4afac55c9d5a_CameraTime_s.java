 package com.phenology.camtim;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import android.app.Activity;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.hardware.Camera;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.VideoView;
 
 public class CameraTime extends Activity
 implements OnClickListener, Camera.PictureCallback, Runnable{
     EditText minutes;
   Button startb;
   private Camera ct_camera;
   private boolean is_ct_active; //is camera on
   private boolean is_ct_running; //are we taking pictures
   private String image_name;
   private String image_prefix;
   private int image_counter;
   private Handler handler;
   
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setContentView(R.layout.main);
     minutes = (EditText) findViewById(R.id.editText2); 
     startb = (Button) findViewById(R.id.button1);
     startb.setOnClickListener(this);
     is_ct_active = false;
     is_ct_running = false;
     image_name = "nothing.jpg";
     image_prefix = Long.toString(System.currentTimeMillis());
     image_counter = 0;
     handler = new Handler();
   }
   
   @Override
   public void onStop(){
	  super.onStop();
	  this.stopCamera();
   }
   
   @Override
   public void onDestroy(){
	  super.onDestroy();
	  this.stopCamera();
   }
 
   private long getMilies (){
     try{
       return Long.parseLong(minutes.getText().toString()) * 60 * 1000;
     }catch (NumberFormatException nfe) {return 60*60*1000;}
   } 
 
   private boolean startCamera (){
     ct_camera = Camera.open();
 
     Camera.Parameters p = ct_camera.getParameters();
     ct_camera.setParameters(p);
 
     VideoView vv = (VideoView) findViewById(R.id.videoView1);
     try{
       ct_camera.setPreviewDisplay(vv.getHolder());
     } catch (IOException ioe) {return false;}
 
     ct_camera.startPreview();
     is_ct_active = true;
 
     // Give time for camera to adjust.
     try { Thread.sleep(2000);}
     catch(InterruptedException e){Log.e("ct_cam_run", e.getMessage());}
 
     return true;
   }
 
   private void stopCamera (){
     ct_camera.stopPreview();
     ct_camera.release();
     this.is_ct_active = false;
   }
 
   /** This is the start/stop button behavior */
   public void onClick (View v){
 	if (!this.is_ct_running){
 	  this.handler.removeCallbacks(this);
 	  this.handler.post(this);
 	  startb.setText("Stop");
 	} else {
 	  this.handler.removeCallbacks(this);
 	  startb.setText("Start");
 	}
 	this.is_ct_running = !this.is_ct_running;	  
   }
 
   public void run () {
 	if (!this.is_ct_running)
		return;
 
 	/** Allows for chnage of times. */
     long milis = this.getMilies();
     handler.postDelayed(this, milis);
     
     /* construct the name */
     this.image_name = this.image_prefix + Integer.toString(this.image_counter) + ".jpg";
     
     startCamera();
       
     this.ct_camera.takePicture(null, this, this);
     try { Thread.sleep(2000);}
     catch(InterruptedException e){Log.e("ct_cam_run", e.getMessage());}
  
     stopCamera();
     this.image_counter++;
   }
   
   public void onPictureTaken(byte[] data, Camera camera) {
 	if (data == null)
		return;
	
     try {// store image bytes logic.
       File root_path = // Using sd card. level 7.
         new File(Environment.getExternalStorageDirectory().getAbsolutePath()
              +"/Android/data/"+this.getPackageName()+"/files/");
       root_path.mkdirs();
       File file_path = new File(root_path.getAbsolutePath()+"/"+this.image_name);
       BufferedOutputStream bos =
         new BufferedOutputStream( new FileOutputStream (file_path, true));
       
       BitmapFactory.Options options = new BitmapFactory.Options();
       options.inSampleSize = 5;
       Bitmap myImage =
         BitmapFactory.decodeByteArray( data, 0, data.length,options);
 
       /** Keep the image if more than 20% of pixels are non-black */
       double np = myImage.getHeight() * myImage.getWidth();
       double nnbp_counter = 0, nbp_counter = 0 ;/* Number {Non-}Black Pixels */
       for ( int row = 0 ; row < myImage.getHeight() ; row++){
    	  for ( int col = 0 ; col < myImage.getWidth() ; col++)
    		  if (myImage.getPixel(col, row) <= -1.67772E7)
    			  nbp_counter++;
    		  else
    			  nnbp_counter++;
     	  
     	  if ( nbp_counter > .8*np ) //We dont keep picture
    		  return;
     	  else if (nnbp_counter > .2*np ) //We take picture
    		  break;
       }
       
       myImage.compress(CompressFormat.JPEG, 100, bos);
       bos.flush();
       bos.close();
     }
     catch (FileNotFoundException e) { Log.e("error",e.getMessage());}
     catch (IOException e) { Log.e("error",e.getMessage());}
   }
 }
