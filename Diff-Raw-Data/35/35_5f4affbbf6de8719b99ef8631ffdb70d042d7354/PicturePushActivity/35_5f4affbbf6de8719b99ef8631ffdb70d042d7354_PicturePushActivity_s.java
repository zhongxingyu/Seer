 package itu.perv.PicPush;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.BasicResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.gvsu.cis.masl.channelAPI.ChannelAPI;
 import edu.gvsu.cis.masl.channelAPI.ChannelAPI.ChannelException;
 
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Intent;
 import android.database.Cursor;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Handler;
 import android.provider.MediaStore;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 
 public class PicturePushActivity extends Activity {
 	private String fileName;
 	private String url;
 	private ChannelAPI channel;
 	private Handler mHandler;
 	private String pushStr;
 	private static final int SELECT_PHOTO = 100;
	private String NFCid= "123";
 	private ProgressDialog uploadDialog ;
 	private String picPath;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		// File imgFile = new File("/sdcard/DCIM/Camera/20120407_190530.jpg");
 		// if(imgFile.exists()){
 		//
 		// Bitmap myBitmap =
 		// BitmapFactory.decodeFile(imgFile.getAbsolutePath());
 		//
 		// ImageView myImage = (ImageView) findViewById(R.id.myImage);
 		// myImage.setImageBitmap(myBitmap);
 		//
 		// }
 		mHandler = new Handler();
 
 	}
 
 
 	public void startPhotoPicker(View view) {
 		Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
 		photoPickerIntent.setType("image/*");
 		startActivityForResult(photoPickerIntent, SELECT_PHOTO);
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent imageReturnedIntent) {
 		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
 
 		switch (requestCode) {
 		case SELECT_PHOTO:
 			if (resultCode == RESULT_OK) {
 //				Inspiration found on http://blog.tacticalnuclearstrike.com/2010/01/using-multipartentity-in-android-applications/
 				uploadDialog = ProgressDialog.show(PicturePushActivity.this, "", 
                         "Uploading. Please wait...", true);
 				Uri selectedImage = imageReturnedIntent.getData();
 				Log.i("myI", " " +selectedImage);
 				Log.i("myI", getPath(selectedImage) );
 				picPath = getPath(selectedImage);
 			    new Thread(){
 			    	public void run(){
 			    		mHandler.post(uploadPhoto);
 			    	}
 			    }.start(); 
 			}
 			// 
 		}
 	}
 	
 	private Runnable uploadPhoto = new Runnable() {
 		
 		@Override
 		public void run() {
 		try{
 			HttpClient httpClient = new DefaultHttpClient();
 			HttpGet get = new HttpGet(
 					"http://fluid-photos-at-itu.appspot.com/getUploadUrl");
 
 			ResponseHandler<String> resp = new BasicResponseHandler();
 			String uploadURL = httpClient.execute(get, resp);
 			
 			
 
 			 HttpPost httpPost = new HttpPost(uploadURL);
 			//
 			 	
 			 MultipartEntity entity = new MultipartEntity();
 			 entity.addPart("photos", new FileBody(new File(picPath), "image/jpeg"));
 			 entity.addPart("source", new StringBody("android"));
 			 entity.addPart("nfcid", new StringBody(NFCid));
 			 httpPost.setEntity(entity);
 			 Log.i("picupload","Skal til at starte med at uploade");
 			 
 			 HttpResponse res = httpClient.execute(httpPost);
 			 uploadDialog.dismiss();
 			 Toast.makeText(PicturePushActivity.this, "Upload done", Toast.LENGTH_LONG).show();
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 			
 		}
 	};
 	public String getPath(Uri uri) {
 	    String[] projection = { MediaStore.Images.Media.DATA };
 	    Cursor cursor = managedQuery(uri, projection, null, null, null);
 	    int column_index = cursor
 	            .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 	    cursor.moveToFirst();
 	    return cursor.getString(column_index);
 	}
 	
 	
 	public void startChannelservice(View view){
 		PhotoChannelService phChannelService = new PhotoChannelService(this,NFCid);
 	
 		try {
 			channel = new ChannelAPI("http://fluid-photos-at-itu.appspot.com", "PHOTO_RELAY", phChannelService);
 			channel.open();
 			
 		} catch (ClientProtocolException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ChannelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public void getMessage(JSONObject json){
 		try {
 			fileName = json.getString("filename");
 			url = "http://fluid-photos-at-itu.appspot.com/getPhotoById?id="+json.getString("id");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 //		pushStr = str;
 	    new Thread(){
 	    	public void run(){
 	    		mHandler.post(startDownload);
 	    	}
 	    }.start();
 	}
 	
 	public Runnable startDownload = new Runnable() {
 		
 		@Override
 		public void run() {
 			// TODO Auto-generated method stub
 			Log.i("downloadInfo", "pushstr" +pushStr);
 			Toast.makeText(PicturePushActivity.this, "Starts download", Toast.LENGTH_LONG).show();
 			Log.i("test","testDownload startes");
 			GetPic gp = new GetPic(PicturePushActivity.this, fileName, url);
 			gp.startDownload();
 		}
 	};
 }
