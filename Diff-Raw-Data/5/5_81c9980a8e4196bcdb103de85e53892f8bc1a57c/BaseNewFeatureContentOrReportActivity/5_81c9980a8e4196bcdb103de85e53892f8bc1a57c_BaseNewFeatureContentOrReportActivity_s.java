 package uk.co.jarofgreen.cityoutdoors.UI;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import uk.co.jarofgreen.cityoutdoors.R;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.location.Location;
 import android.location.LocationListener;
 import android.location.LocationManager;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.provider.Settings;
 import android.util.Log;
 import android.view.View;
 import android.widget.ImageView;
 /**
  * 
  * @author James Baster  <james@jarofgreen.co.uk>
  * @copyright City of Edinburgh Council & James Baster
  * @license Open Source under the 3-clause BSD License
  * @url https://github.com/City-Outdoors/City-Outdoors-Android
  */
 public class BaseNewFeatureContentOrReportActivity extends BaseActivity  {
 
 	protected int featureID = -1;
 	protected float lat = 0;
 	protected float lng = 0;
 	
 	protected static final int ACTION_TAKE_PHOTO = 1;
 	protected static final int ACTION_SELECT_PHOTO = 2;
 	
 	protected static final String JPEG_FILE_PREFIX = "IMG_";
 	protected static final String JPEG_FILE_SUFFIX = ".jpg";
 	
 	protected String photoFileName;
 	protected boolean hasPhoto = false;
 	
 	protected LocationManager locationManager;
 	
 	protected boolean hasPosition() {
 		return (featureID != -1 || lat != 0 || lng != 0);
 	}
 	
     protected void promptForPosition() {
         
     	if (!hasPosition()) {
 
     		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
     		final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
 
         	if (!gpsEnabled) {
         		
         		AlertDialog.Builder builder = new AlertDialog.Builder(this);
         		builder.setMessage("Please turn on GPS so we can get your current position.")
 	        		.setCancelable(false)
 	        		.setPositiveButton("OK", new DialogInterface.OnClickListener() {
 	        			public void onClick(DialogInterface dialog, int id) {
 	        				startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
 	
 	        			}
 	        		});
         		AlertDialog alert = builder.create();
         		alert.show();
         	}
         	
         }
         
         
     }
     
     @Override
     public void onResume() {
     	super.onResume();
     	if (!hasPosition()) locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
     }
 
     @Override
     public void onStop() {
     	if (locationManager != null) locationManager.removeUpdates(locationListener);
     	super.onStop();
     }
 
 
     protected File getAlbumDir() {
 		File storageDir = null;
 
 		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
 			
 			/**if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
 				storageDir = new File(
 						  Environment.getExternalStoragePublicDirectory(
 						    Environment.DIRECTORY_PICTURES
 						  ), 
 						  getString(R.string.photo_album_name)
 						);
 			} else { **/
 				storageDir =new File (
 						Environment.getExternalStorageDirectory()
 						+ "/dcim/"
 						+ getString(R.string.photo_album_name)
 				);
 			//}
 
 			if (storageDir != null) {
 				if (! storageDir.mkdirs()) {
 					if (! storageDir.exists()){
 						Log.d("CameraSample", "failed to create directory");
 						return null;
 					}
 				}
 			}
 			
 		} else {
 			Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
 		}
 		
 		return storageDir;
 	}
     
 
     public void onClickSelectPhoto(View v) {
     	Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
     	startActivityForResult(i, ACTION_SELECT_PHOTO);
     }
     
     public void onClickTakePhoto(View v) {
     	Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     	
     	
     	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
 		String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
 		File albumF = getAlbumDir();
 		File imageF;
 		try {
 			imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
 		} catch (IOException e) {
 			// TODO 
 			e.printStackTrace();
 			return;
 		}
 		Log.d("PHOTO","Should Save in "+imageF.getAbsolutePath());
 		photoFileName = imageF.getAbsolutePath();
     	takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageF));
     	
     	startActivityForResult(takePictureIntent, ACTION_TAKE_PHOTO);
     }
     
     protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
     	if (requestCode == ACTION_TAKE_PHOTO && resultCode == RESULT_OK) {
     		Log.d("PHOTO","Got Photo Back (Taken)");
     		
     		hasPhoto = true;
     		
     		// set preview image
     		//Bundle extras = intent.getExtras();
     		//ImageView mImageView = (ImageView)findViewById(R.id.photo_preview);
     		//mImageView.setImageBitmap((Bitmap) extras.get("data"));
 
     	} else if (requestCode == ACTION_SELECT_PHOTO && resultCode == RESULT_OK) {
     		Log.d("PHOTO","Got Photo Back (selected)");
     		
     		Uri selectedImage = intent.getData();
             String[] filePathColumn = { MediaStore.Images.Media.DATA };
     
             Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
             cursor.moveToFirst();
     
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             photoFileName = cursor.getString(columnIndex);
             cursor.close();
             
             hasPhoto = true;
             
     		
     	}
     	
     	
     	if (photoFileName != null) {
 			Bitmap bitmap = new BitmapDrawable(photoFileName).getBitmap();
 			//Log.i("OLDWIDTH",Integer.toString(bitmap.getWidth()));
 			//Log.i("OLDHEIGHT",Integer.toString(bitmap.getHeight()));
 			
 			float scale = Math.max(1.0f,Math.max((float)bitmap.getWidth()/200f, (float)bitmap.getHeight()/200f));
 			//Log.i("SCALE", Float.toString(scale));
 			
 			int scaleWidth = (int)(bitmap.getWidth() / scale);
 			int scaleHeight = (int)(bitmap.getHeight() / scale);
 			//Log.i("NEWWIDTH",Integer.toString(scaleWidth));
 			//Log.i("NEWHEIGHT",Integer.toString(scaleHeight));
 
 			Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, scaleWidth, scaleHeight, true);
 			
 			ImageView ivPhoto = (ImageView)findViewById(R.id.photo_preview);
 			ivPhoto.setImageBitmap(newBitmap);
 			ivPhoto.setVisibility(View.VISIBLE);
     	}
     }
     
     protected final LocationListener locationListener = new LocationListener() {
     	public void onLocationChanged(Location location) {
 	    	lat = (float)location.getLatitude();
 	    	lng = (float)location.getLongitude();
     	}
 
     	public void onStatusChanged(String provider, int status, Bundle extras) {}
 
     	public void onProviderEnabled(String provider) {}
 
     	public void onProviderDisabled(String provider) {
     	}
     };    
     
 	
 }
