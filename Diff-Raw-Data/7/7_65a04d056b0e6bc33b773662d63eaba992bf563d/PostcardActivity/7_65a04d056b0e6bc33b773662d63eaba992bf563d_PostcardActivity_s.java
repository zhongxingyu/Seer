 package destinationsalinas.historictour;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.res.Configuration;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 
 public class PostcardActivity extends Activity implements OnClickListener{
 
 	private final String FACEBOOK_APP_ID = "266208693459714";
 	private final String POSTCARD_PIC_KEY = "postcard-picture";
 	private final int PICTURE_RESULT = 2387;
 	private final int SELECT_PICTURE_REQUEST = 3837;
 	Button sendButton; 
 	Button takePicture; 
 	Button selectPicture;
 	Uri pictureUri; 
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
 			setContentView(R.layout.postcard_landscape);
 		} else {
 			setContentView(R.layout.postcard);
 		}
 
 		takePicture = (Button) findViewById(R.id.takepicture_button);
 		takePicture.setOnClickListener(this);
 		sendButton = (Button) findViewById(R.id.postcard_send);
 		sendButton.setOnClickListener(this);
 		selectPicture = (Button) findViewById(R.id.selectpicture_button);
 		selectPicture.setOnClickListener(this);
 		
 		File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");     
 		Uri selectedImageUri = Uri.fromFile(photo); 
 		Bitmap b = BitmapFactory.decodeFile(photo.getAbsolutePath());
 		Bitmap smallB= Bitmap.createScaledBitmap(b, 300, 300, false);
 		ImageView pictureHolder = (ImageView) findViewById(R.id.picturetaken_imageview);
 		pictureHolder.setImageBitmap(smallB);
 		b.recycle();
 		Log.i("Example", "Screen Change");
 		
 	}
 
 	@Override 
 	protected void onSaveInstanceState(Bundle outState) {
 		byte[] data = null;       
 		if(this.findViewById(R.id.picturetaken_imageview).getDrawingCache(false) != null) {
 			File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");     
 			Uri selectedImageUri = Uri.fromFile(photo); 
 			Bitmap b = BitmapFactory.decodeFile(photo.getAbsolutePath());
 			Bitmap smallB= Bitmap.createScaledBitmap(b, 300, 300, false);
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();                       
 			smallB.compress(Bitmap.CompressFormat.JPEG, 100, baos);                       
 			data = baos.toByteArray();   
 			outState.putByteArray(POSTCARD_PIC_KEY, data);
 		}
 		Log.i("Example", "onsaved instance state");
 		super.onSaveInstanceState(outState);
 	}
 	@Override
 	public void onClick(View v) {
 		if(v.getId() == R.id.postcard_send) {
 			Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);   
 			picMessageIntent.setType("image/jpeg");  
 			picMessageIntent.putExtra(Intent.EXTRA_STREAM, pictureUri); 
 			startActivity(Intent.createChooser(picMessageIntent, "Send your picture using:")); 
 
 		} else if(v.getId() == R.id.takepicture_button) {
 			Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); 
 			 File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");     
 			 camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo)); 
 			 pictureUri = Uri.fromFile(photo);
 			this.startActivityForResult(camera, PICTURE_RESULT); 
 		} else if(v.getId() == R.id.selectpicture_button) {
 			Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);   
 			startActivityForResult(intent, SELECT_PICTURE_REQUEST);
 		}
 	}
 
 	@Override
 	public void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 		if(requestCode == PICTURE_RESULT) {
 			if (resultCode == Activity.RESULT_OK) {  
 				File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");     
 				Uri selectedImageUri = Uri.fromFile(photo); 
 				Bitmap b = BitmapFactory.decodeFile(photo.getAbsolutePath());
				Bitmap smallB= Bitmap.createScaledBitmap(b, 300, 300, false);
 				FileOutputStream fos;
 				try {
 					fos = new FileOutputStream(photo);
 					smallB.compress(Bitmap.CompressFormat.JPEG, 100, fos);
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				pictureUri = selectedImageUri;           
 				ImageView pictureHolder = (ImageView) findViewById(R.id.picturetaken_imageview);
 				pictureHolder.setImageBitmap(smallB);
 			} else if (resultCode == Activity.RESULT_CANCELED) {
 			} 
 		} else if (requestCode == SELECT_PICTURE_REQUEST) {
 			if(resultCode == Activity.RESULT_OK) {
 				File photo = new File(Environment.getExternalStorageDirectory(), "Pic.jpg");   
 				Uri selectedImageUri = data.getData(); 
 				pictureUri = selectedImageUri; 
 				String selectedImagePath = getPath(selectedImageUri);   
 				Bitmap b = BitmapFactory.decodeFile(selectedImagePath);
				Bitmap smallB= Bitmap.createScaledBitmap(b, 300, 300, false);
 				FileOutputStream fos;
 				try {
 					fos = new FileOutputStream(photo);
 					smallB.compress(Bitmap.CompressFormat.JPEG, 100, fos);
 				} catch (FileNotFoundException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				ImageView pictureHolder = (ImageView) findViewById(R.id.picturetaken_imageview);
 				pictureHolder.setImageBitmap(smallB);
 			}
 		}
 	}
 	
 
 	private String getPath(Uri uri) {         
 		String[] projection = { MediaStore.Images.Media.DATA };         
 		Cursor cursor = managedQuery(uri, projection, null, null, null);         
 		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);         
 		cursor.moveToFirst();         
 		return cursor.getString(column_index);     
 	} 
 	
 }
 
 
 
