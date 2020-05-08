 package edu.ucdavis.cros.roadkill;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.os.Bundle;
 import android.os.Environment;
 
 public class TakePhoto extends Activity {
 
 	protected boolean _taken;
 	protected ImageView _image;
 	public ImageButton phButton;
 	ExifInterface exif;
     File picFile;
     String strLat;
     Float fltLatC;
     static String strLatC;
     String strLatRef;
     String strLong;
     Float fltLongC;
     static String strLongC;
     String strLongRef;
     static String strDateTime;
     TextView longT;
     TextView latT;
     TextView datetimeT;
     
     protected static final String PHOTO_TAKEN	= "photo_taken";
     
 
 //	public void startCameraActivity()
 //    {
     
     public void onCreate(Bundle savedInstanceState) {
     	super.onCreate(savedInstanceState);
     	
     	
 //    	_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "CROSPic.jpg";
     	Log.i("TakePhoto", "startCameraActivity()" );
     	File file = new File( roadkill._path );
     	Uri outputFileUri = Uri.fromFile( file );
     	
     	// Start intent that will open the camera
     	Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
     	intent.putExtra( MediaStore.EXTRA_OUTPUT, outputFileUri );
     	
     	// Tells camera to return to App with a result when done
     	startActivityForResult( intent, 0 );
     	
     }
 
     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) 
     {	
     	// Log whether they took a photo or not
     	Log.i( "TakePhoto", "resultCode: " + resultCode );
     	switch( resultCode )
     	{
     		case 0:
     			Log.i( "TakePhoto", "User cancelled" );
     			break;
     			
     		case -1:
     			onPhotoTaken();
     			Intent returnIntent = new Intent();
     			setResult(RESULT_OK,returnIntent);
     			finish();
     			
     	}
     }
     
 	public boolean onPhotoTaken()
     {
     	Log.i( "RoadKillPhoto", "onPhotoTaken" );
     	
     	_taken = true;
     	
     	
     	try {
     		File picFile = new File( roadkill._path );
     		exif = new ExifInterface(picFile.getCanonicalPath());
     		
     	} catch (Exception e) {
     	// TODO: handle exception
     	}
     	
     	strLat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
     	strLatRef = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
     	strLong = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
     	strLongRef = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
     	strDateTime = exif.getAttribute(ExifInterface.TAG_DATETIME);
     	
     	//TODO : Comparison appears to fail if the GPS doesn't have a fix
 		//Set the Location from the Photo exif it it has a GPS fix
     	if(strLatRef != null){
 			if(strLatRef.equals("N")){
 				fltLatC = convertToDegree(strLat);
 			}
 			else{
 				fltLatC = 0 - convertToDegree(strLat);
 			}
 			 
 			if(strLongRef.equals("E")){
 				fltLongC = convertToDegree(strLong);
 			}
 			else{
 				fltLongC = 0 - convertToDegree(strLong);
 			}
 
 			latT = (TextView)findViewById(R.id.latVal);
 	    	longT = (TextView)findViewById(R.id.longVal);
 	    	strLatC = fltLatC.toString();
 	    	strLongC = fltLongC.toString();
 	    	

 		}

     	
     	return _taken;
     }
     public Float convertToDegree(String stringDMS){
    	 Float result = null;
    	 String[] DMS = stringDMS.split(",", 3);
 
    	 String[] stringD = DMS[0].split("/", 2);
    	    Double D0 = new Double(stringD[0]);
    	    Double D1 = new Double(stringD[1]);
    	    Double FloatD = D0/D1;
 
    	 String[] stringM = DMS[1].split("/", 2);
    	 Double M0 = new Double(stringM[0]);
    	 Double M1 = new Double(stringM[1]);
    	 Double FloatM = M0/M1;
    	  
    	 String[] stringS = DMS[2].split("/", 2);
    	 Double S0 = new Double(stringS[0]);
    	 Double S1 = new Double(stringS[1]);
    	 Double FloatS = S0/S1;
    	  
    	 result = new Float(FloatD + (FloatM/60) + (FloatS/3600));
    	  
    	 return result;
 
 
    	};
 }
