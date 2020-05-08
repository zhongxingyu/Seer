 package com.matoyan.conbumusubi;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Stack;
 
 import mobisocial.socialkit.Obj;
 import mobisocial.socialkit.musubi.DbFeed;
 import mobisocial.socialkit.musubi.multiplayer.FeedRenderable;
 import mobisocial.socialkit.musubi.multiplayer.TurnBasedMultiplayer;
 import mobisocial.socialkit.musubi.DbObj;
 import mobisocial.socialkit.musubi.FeedObserver;
 import mobisocial.socialkit.musubi.MemObj;
 import mobisocial.socialkit.musubi.Musubi;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.xbill.DNS.utils.base64;
 
 import com.facebook.android.AsyncFacebookRunner;
 import com.facebook.android.AsyncFacebookRunner.RequestListener;
 import com.facebook.android.DialogError;
 import com.facebook.android.Facebook;
 import com.facebook.android.Facebook.DialogListener;
 import com.facebook.android.FacebookError;
 import com.facebook.android.SessionStore;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.ContentValues;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.graphics.Matrix;
 import android.media.ExifInterface;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.os.Handler;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.provider.MediaStore.Images;
 import android.util.Base64;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Gallery;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 // post data into Facebook / Musubi
 public class PostDataActivity extends Activity implements OnClickListener{
 	
 	// UI
 	private EditText postText = null;
 	private Button launchCamera = null;
 	private Button launchGallery = null;
 	private Button submitMulti = null;
 	private Button submitFB = null;
 	private Button submitMB = null;
 	private Button getMB = null;
 
 	// Image
 	private Uri mImageUri;
 	private File mTmpFile;
 	private String uploadURL;
 	private String thumbURL;
 
 	// Server Control
 	private int serverResponseCode=0;
 	private String serverResponseMessage="";
 	
 	// Facebook
     private Facebook m_facebook;
     private AsyncFacebookRunner m_facebook_runner;
 
     // Musubi
     Musubi mMusubi;
     DbFeed mFeed;
     
     // PostData
 	private String poststr;
 	Stack mystack = new Stack();
 
 	// static
 	private static int REQCODE_CAMERA = 99;
 	private static int REQCODE_GALLERY = 98;
     
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.post);
 		
         // for camera
 // 独自のカメラ機能を実装する場合
 //        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
 //        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
 //        requestWindowFeature(Window.FEATURE_NO_TITLE);
 //        setContentView(new CameraView(this));
 
 		
         // Initial setup for musubi
         if(!Musubi.isMusubiIntent(getIntent())){
             Toast.makeText(this, "Please launch from Musubi!", Toast.LENGTH_SHORT).show();
             finish();
             return;
         }
 
 		mMusubi = Musubi.getInstance(this);
 		mFeed = mMusubi.getFeedFromIntent(getIntent());
         mFeed.registerStateObserver(mFeedObserver);
         
         // UI
 		postText = (EditText)findViewById( R.id.postText);
 		launchCamera = (Button)findViewById( R.id.launchCamera);
 		launchGallery = (Button)findViewById( R.id.launchGallery);
 		submitMulti = (Button)findViewById( R.id.submitMulti);
 		submitFB = (Button)findViewById( R.id.submitFB);
 		submitMB = (Button)findViewById( R.id.submitMB);
 		getMB = (Button)findViewById( R.id.getMB);
 		
 		// Add listener to buttons
 		launchCamera.setOnClickListener( this);
 		launchGallery.setOnClickListener( this);
 		submitMulti.setOnClickListener( this);
 		submitFB.setOnClickListener( this);
 		submitMB.setOnClickListener( this);
 		getMB.setOnClickListener( this);
 
 		// Initial setup for Facebook
 		connect_facebook ();
 
 	}
 
 	@Override
 	public void onClick(View v) {
 		int vid = v.getId();
 		String pressed="";
 		switch (vid) {
 		// Camera
 		case R.id.launchGallery:
 			pressed = "Gallery";
 			break;
 		// Multi
 		case R.id.launchCamera:
 			pressed = "Camera";
 			break;
 		// Multi
 		case R.id.submitMulti:
 			pressed = "Multi";
 			break;
 		// FB
 		case R.id.submitFB:
 			pressed = "Facebook";
 			break;
 		// MB
 		case R.id.submitMB:
 			pressed = "Musubi";
 			break;
 		case R.id.getMB:
 			pressed = "getMB";
 			break;
 		}
 
 		poststr = postText.getText().toString();
		Log.d("CombuMusubi", "Msg saved: "+poststr);
 
 		if(pressed.equals("Camera")) {
 			launchCameraApp();
 		}else if(pressed.equals("Gallery")){
 			launchGalleryApp();
 		}else if(pressed.equals("getMB")) {
 //			getMB();
 		}else{
 			showDL(pressed);
 		}
 	};
 
 	private void launchCameraApp(){
 		Intent intent = new Intent();
 		intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
 		mTmpFile = new File(Environment.getExternalStorageDirectory()+"/MusubiPictures", "img_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
 		intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTmpFile));
 		startActivityForResult(intent, REQCODE_CAMERA);
 	}
 	
 	private void launchGalleryApp(){
 		Intent intent = new Intent();
 		intent.setType("image/*");
 		intent.setAction(Intent.ACTION_PICK);
 		startActivityForResult(intent, REQCODE_GALLERY);
 	}
 
 	private void showDL(final String pressed){
 		
 		// 確認ダイアログの表示
 		AlertDialog.Builder builder = new AlertDialog.Builder( this);
 		// アイコン設定
 		builder.setIcon(android.R.drawable.ic_dialog_alert);
 		// タイトル設定
 		builder.setTitle("Are you sure you want to post data?");
 		// OKボタン設定
 		builder.setPositiveButton( android.R.string.ok, new DialogInterface.OnClickListener() {
 			@Override
 			public void onClick(DialogInterface dialog, int whichButton) {
 //				Toast toast = Toast.makeText(PostDataActivity.this, R.string.okpress, Toast.LENGTH_SHORT);
 //				toast.show();
 		        
 				// upload to Server
 				if(mTmpFile.isFile()){
 					Log.d ("ConbuMusubi", "imagePath" + mTmpFile.getAbsolutePath());
 					// uploadToMyServer(mTmpFile.getAbsolutePath());
 					try {
 						uploadToMB();
 					} catch (Exception e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 		    	  
 					Log.d ("ConbuMusubi", "serverResponseCode " + serverResponseCode);
 					Log.d ("ConbuMusubi", "serverResponseMessage " + serverResponseMessage);
 					
 					uploadURL = "http://mtrecimg.com/musubi/data/"+mTmpFile.getName();
 					thumbURL = "http://mtrecimg.com/musubi/imgresize.php?maxsize=200&fname="+mTmpFile.getName();
 				}
 		        if (pressed.equals("Facebook")){
 					postFB();
 				}else if(pressed.equals("Musubi")) {
 					postMB();
 				}else if(pressed.equals("Multi")) {
 					postMulti();
 				}
 				Log.d("CombuMusubiDBG", "POSTFIN==================================");
 			}
 		});
 		// キャンセルボタン設定
 		builder.setNegativeButton( android.R.string.cancel, null);
 		// ダイアログの表示
 		builder.show();
 	}
 
 	// Multi---------------------------------------------------------------------------------------------
 	public void postMulti(){
 	    
 		// check
 		if(!m_facebook.isSessionValid()){
 			Toast toast = Toast.makeText(PostDataActivity.this, "FB session is expired. Please login again.", Toast.LENGTH_SHORT);
 			toast.show();
 			return;
 	    }
 
 		// Musubi Post
 	    try {
 			mMusubi.getFeed().postObj(getFeedRenderableObj());
         } catch (JSONException e) {
         	Log.wtf("CombuMusubi", "Failed to post through Musubi", e);
         }
 	    Bundle params = new Bundle();
 	    params.putString("message", poststr);
 	    
 	    if(mTmpFile.isFile()){
 		    params.putString("link", uploadURL);
 //	    	params.putString("name", "Photo");
 //	    	params.putString("caption", "CaptionText");
 //	    	params.putString("description", "DescriptionText");
 	    	params.putString("picture", thumbURL);
 	    }
 
 	    // FB Post
 	    m_facebook_runner = new AsyncFacebookRunner (m_facebook);
 	    m_facebook_runner.request("me/feed", params, "POST", new RequestListener() {
 	        @Override
 			public void onMalformedURLException(MalformedURLException e, final Object state) {}
 	        @Override
 			public void onIOException(IOException e, final Object state) {}
 	        @Override
 			public void onFileNotFoundException(FileNotFoundException e, final Object state) {}
 	        @Override
 			public void onFacebookError(FacebookError e, final Object state) {}
 	        @Override
 			public void onComplete(String response, final Object state) {
 	        	Log.e("PostDataActivity",response);
 	        }
 	    }, new Object()); 
 
 	    Toast.makeText(PostDataActivity.this, "Posting to your Wall...", Toast.LENGTH_SHORT).show();       
 	}
 	
 	private Obj getFeedRenderableObj() throws JSONException {
 		String msgstr = poststr;
 	    if(mTmpFile.isFile()){
 	    	msgstr = "<img src='"+thumbURL+"' /><br />"+poststr;
 	    }
 		return FeedRenderable.fromHtml(msgstr).getObj();
 //		return FeedRenderable.fromB64Image(b64JImage); //?
 	}
 	private JSONObject getJSONObj() throws JSONException {
         JSONObject o = new JSONObject();
     	o.put("data", poststr);
     	return o;
 	}
 	
 	private void uploadToMB() throws Exception{
 
 		// for test 
 //		mTmpFile = new File(Environment.getExternalStorageDirectory()+"/MusubiPictures", "small.jpg");
 //		mTmpFile = new File(Environment.getExternalStorageDirectory()+"/MusubiPictures", "mid.jpg");
 //		mTmpFile = new File(Environment.getExternalStorageDirectory()+"/MusubiPictures", "large.jpg");
 //		mTmpFile = new File(Environment.getExternalStorageDirectory()+"/MusubiPictures", "huge.jpg");
 
 		int sqnum = 1;
 		byte[][] ba = file2bytearr(mTmpFile);
 		int totalnum = ba.length;
 		int i = 0;
 		for(byte[] ba2 : ba){
 			if(ba2 == null){
 				break;
 			}
 //        	Log.d("ConbuMusubiDBG", "Data2 --- "+Base64.encodeToString(ba2, Base64.DEFAULT));
 			JSONObject jso = new JSONObject();
 			jso.put("filename", mTmpFile.getName());
 			jso.put("sqnum", sqnum);
 			jso.put("totalnum", totalnum);
 
 //			jso.put("filedata", Base64.encodeToString(file2byte(mTmpFile), Base64.DEFAULT));
 			jso.put("filedata", Base64.encodeToString(ba2, Base64.DEFAULT));
 			
 			MemObj mbj = new MemObj("imgdata", jso);
 			mystack.push(mbj);
 			
 			Log.d("CombuMusubiDBG", "1==================================("+sqnum+"/"+totalnum+")");
 		    Handler handler = new Handler(); 
 		    handler.postDelayed(new Runnable() {
 		         public void run() {
 		        	 postMusubiFromRun();
 		         } 
 		    }, 3000 * i); 
 			Log.d("CombuMusubiDBG", "2==================================("+sqnum+"/"+totalnum+")");
 			
 			Toast.makeText(this, "Sent: "+sqnum+"/"+totalnum, Toast.LENGTH_SHORT).show();
 			sqnum++;
 			i++;
 		}
 	}
 	private void postMusubiFromRun(){
 		MemObj mbj = (MemObj)mystack.pop();
 		mMusubi.getFeed().postObj(mbj);
 	}
 	
 	private void uploadToMyServer(String pathToOurFile){
 		HttpURLConnection connection = null;
 		DataOutputStream outputStream = null;
 		DataInputStream inputStream = null;
 
 		String urlServer = "http://mtrecimg.com/musubi/upload.php";
 		String lineEnd = "\r\n";
 		String twoHyphens = "--";
 		String boundary =  "*****";
 
 		int bytesRead, bytesAvailable, bufferSize;
 		byte[] buffer;
 		int maxBufferSize = 1*1024*1024;
 
 		try
 		{
 		FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
 
 		URL url = new URL(urlServer);
 		connection = (HttpURLConnection) url.openConnection();
 
 		// Allow Inputs & Outputs
 		connection.setDoInput(true);
 		connection.setDoOutput(true);
 		connection.setUseCaches(false);
 
 		// Enable POST method
 		connection.setRequestMethod("POST");
 
 		connection.setRequestProperty("Connection", "Keep-Alive");
 		connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
 
 		outputStream = new DataOutputStream( connection.getOutputStream() );
 		outputStream.writeBytes(twoHyphens + boundary + lineEnd);
 		outputStream.writeBytes("Content-Disposition: form-data; name=\"uploadedfile\";filename=\"" + pathToOurFile +"\"" + lineEnd);
 		outputStream.writeBytes(lineEnd);
 
 		bytesAvailable = fileInputStream.available();
 		bufferSize = Math.min(bytesAvailable, maxBufferSize);
 		buffer = new byte[bufferSize];
 
 		// Read file
 		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
 
 		while (bytesRead > 0)
 		{
 		outputStream.write(buffer, 0, bufferSize);
 		bytesAvailable = fileInputStream.available();
 		bufferSize = Math.min(bytesAvailable, maxBufferSize);
 		bytesRead = fileInputStream.read(buffer, 0, bufferSize);
 		}
 
 		outputStream.writeBytes(lineEnd);
 		outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
 
 		// Responses from the server (code and message)
 		serverResponseCode = connection.getResponseCode();
 		serverResponseMessage = connection.getResponseMessage();
 
 		fileInputStream.close();
 		outputStream.flush();
 		outputStream.close();
 		}
 		catch (Exception ex)
 		{
 		//Exception handling
 		}
 	}
 
 	// Musubi---------------------------------------------------------------------------------------------
 	// get data from Musubi
 	/*
 	private void getMB(){
 		String localUname = mMusubi.getFeed().getLocalUser().getName();
     	Log.d("CombuMusubi", "localUname:".concat(localUname));
 		Set<User> RemoteUnames = mMusubi.getFeed().getRemoteUsers();
 		
     	Log.d("CombuMusubi", "RemoteUnames--->");
         Iterator<User> ite = RemoteUnames.iterator();     //実際のプログラム中に、null判定を忘れずに  
         while(ite.hasNext()) {              //ループ  
         	User usr = ite.next();        //該当オブジェクト取得 
         	Log.d("CombuMusubi", usr.getName());
         }
     	Log.d("CombuMusubi", "<---RemoteUnames");
     	
     	Uri myuri = mMusubi.getFeed().getUri();
     	Log.d("CombuMusubi", "FeedUri:".concat(myuri.toString()));
     	
     	JSONObject jsobj = mMusubi.getFeed().getLatestObj();
     	if(jsobj != null){
     		Log.d("CombuMusubi", "LatestObj:".concat(jsobj.toString()));
     	}
 	}
 	*/
 	// post message to Musubi
 	public void postMB(){
         //mDungBeetle.getFeed().setApplicationState(getApplicationState(), getSnapshotText());
         try {
 			mMusubi.getFeed().postObj(getFeedRenderableObj());
         } catch (JSONException e) {
         	Log.wtf("CombuMusubi", "Failed to post through Musubi", e);
         }
 	}
 //	private AppState getAppState() throws JSONException {
 //        JSONObject o = new JSONObject();
 //    	o.put("data", poststr);
 //    	AppState as = new AppState(o);
 //    	as.arg = "arg";
 //    	as.thumbnailText = "thumbnailText";
 //    	as.thumbnailImage = "http://yimg.dip.jp/themes/day_break/logo2.gif";
 //    	as.thumbnailHtml = "<html><header></header><body><strong>thumbnailHtml</strong></body></html>";
 //    	return as;
 //	}
 	
 	private FeedObserver mFeedObserver = new FeedObserver() {
         @Override
         public void onUpdate(Obj stateObj){
     		Log.d("CombuMusubiDBG", "RCV==================================");
 
         	JSONObject state = stateObj.getJson();
         	
 //			Toast.makeText(PostDataActivity.this, "Message Received", Toast.LENGTH_SHORT).show();
 
         	String getstr = "";
         	String fpath = "";
         	try {
         		if(state != null){
         			Log.d("ConbuMusubi", "Message Rcvd ---------: ".concat(stateObj.getType()));
         			
         			if(stateObj.getType().equals("imgdata")){
             			Toast.makeText(PostDataActivity.this, "ImageData Received!", Toast.LENGTH_SHORT).show();
         			}
         			if(!state.isNull("filename")){
         				fpath = Environment.getExternalStorageDirectory()+"/MusubiPictures/copy"+state.getString("sqnum")+"_"+state.getString("filename")+".tmp";
         				Log.d("ConbuMusubi2", state.getString("filename"));
         				Log.d("ConbuMusubi2", "sqnum:"+state.getString("sqnum"));
         				Log.d("ConbuMusubi2", "totalnum:"+state.getString("totalnum"));
         				Log.d("ConbuMusubiDBG", "Data3 --- "+state.getString("filedata"));
         				Log.d("ConbuMusubi2", fpath);
         				byte2file(Base64.decode(state.getString("filedata"), 0), fpath);
 //        				byte2file(stateObj.getRaw(), fpath);
         				
         				// try to conbine temporally files
         				int totalnum = Integer.parseInt(state.getString("totalnum"));
         				// precheck
         				for(int i=1;i<=totalnum;i++){
         					fpath = Environment.getExternalStorageDirectory()+"/MusubiPictures/copy"+i+"_"+state.getString("filename")+".tmp";
         					File rf = new File(fpath);
         					if(!rf.exists()){
         						return;
         					}
         				}
         				// if all files exist, try to conbine them and delete all.
     					fpath = Environment.getExternalStorageDirectory()+"/MusubiPictures/converted_"+state.getString("filename");
     		            FileOutputStream fos=new FileOutputStream(fpath);
         				for(int i=1;i<=totalnum;i++){
         					fpath = Environment.getExternalStorageDirectory()+"/MusubiPictures/copy"+i+"_"+state.getString("filename")+".tmp";
         					File rf = new File(fpath);
         		            fos.write(file2byte(rf));
         		            rf.delete();
         				}
     		            fos.close();
     		            
     		            // delete original file
         				fpath = Environment.getExternalStorageDirectory()+"/MusubiPictures/"+state.getString("sqnum")+state.getString("filename");
     					File rf = new File(fpath);
     					if(!rf.exists()){
     						rf.delete();
     					}
         			}
         			if(!state.isNull("data")){
         				getstr = state.getString("data");
         				Toast.makeText(PostDataActivity.this, "Message Received!: " + '"' + getstr + '"', Toast.LENGTH_SHORT).show();
         			}
         		}else{
         			
         			Log.d("ConbuMusubi", "Message Rcvd ---------: null");
         			Toast.makeText(PostDataActivity.this, "Message Received!: ... null", Toast.LENGTH_SHORT).show();
         			
         		}
 			} catch (JSONException e) {
 				getstr = "[ERROR] Failed to get data";
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
         	
         }
     };
 
 	// FB---------------------------------------------------------------------------------------------
 	// post message to Facebook
 	public void postFB(){
 	    if(!m_facebook.isSessionValid()){
 			Toast toast = Toast.makeText(PostDataActivity.this, "FB session is expired. Please login again.", Toast.LENGTH_SHORT);
 			toast.show();
 			return;
 	    }
 	    Bundle params = new Bundle();
 	    params.putString("message", poststr);
 	    if(mTmpFile.isFile()){
 		    params.putString("link", uploadURL);
 //	    	params.putString("name", "Photo");
 //	    	params.putString("caption", "CaptionText");
 //	    	params.putString("description", "DescriptionText");
 	    	params.putString("picture", uploadURL);
 	    }
 	    
 	    m_facebook_runner = new AsyncFacebookRunner (m_facebook);
 	    m_facebook_runner.request("me/feed", params, "POST", new RequestListener() {
 	        @Override
 			public void onMalformedURLException(MalformedURLException e, final Object state) {}
 	        @Override
 			public void onIOException(IOException e, final Object state) {}
 	        @Override
 			public void onFileNotFoundException(FileNotFoundException e, final Object state) {}
 	        @Override
 			public void onFacebookError(FacebookError e, final Object state) {}
 	        @Override
 			public void onComplete(String response, final Object state) {
 	        	Log.e("PostDataActivity",response);
 	        }
 	    }, new Object()); 
 
 	    Toast.makeText(PostDataActivity.this, "Posting to your Wall...", Toast.LENGTH_SHORT).show();       
 	}
 
 	
     // FB connect
     private void connect_facebook ()
     {
 //   	  Toast.makeText(PostDataActivity.this, "connecting", Toast.LENGTH_SHORT).show();       
       m_facebook = new Facebook ("177064775705380");
       SessionStore.restore(m_facebook, this);
       if(!m_facebook.isSessionValid()){
     	  m_facebook.authorize (this, new String[] {"email", "publish_stream"}, new LoginListener ());
       }
     }
     
     // save FB login token
     private void saveFBToken(String token, long tokenExpires){
     	SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
     	prefs.edit().putString("FacebookToken", token).commit();
     }
     
     // FB login
     public class LoginListener implements DialogListener
     {
       @Override
         public void onComplete (Bundle values)
         {
     	  Toast.makeText(PostDataActivity.this, "FB login Completed.", Toast.LENGTH_SHORT).show();
     	  SessionStore.save(m_facebook, PostDataActivity.this);
     	  
 //          m_facebook_runner = new AsyncFacebookRunner (m_facebook);
 //          m_facebook_runner.request ("me/friends", new FriendsRequestListener ());
         }
 
       @Override
         public void onFacebookError (FacebookError e)
         {
           Log.e ("Facebook", "authorize : Facebook Error:" + e.getMessage ());
         }
 
       @Override
         public void onError (DialogError e)
         {
           Log.e ("Facebook", "authorize : Error:" + e.getMessage ());
         }
 
       @Override
         public void onCancel () {}
     }
 
     // callback
     @Override
     public void onActivityResult (int requestCode, int resultCode, Intent data)
     {
       super.onActivityResult(requestCode, resultCode, data);
       if (requestCode == REQCODE_CAMERA) {
           // Update ImageView
           ImageView imageView = (ImageView) findViewById(R.id.imgpreview);
 //          imageView.setImageURI(mImageUri);
           
           Bitmap myBitmap = BitmapFactory.decodeFile(mTmpFile.getAbsolutePath());
           
           Matrix matrix = new Matrix();
           float rotation = rotationForImage(getBaseContext(), Uri.fromFile(mTmpFile));
           if (rotation != 0f) {
                matrix.preRotate(rotation);
           }
           Log.d("ConbuMusubi", "rotation: "+rotation);
           
           float h = myBitmap.getHeight();
           float resizedscale = 200 / h;
           matrix.postScale(resizedscale, resizedscale);
 
           Bitmap resizedBitmap = Bitmap.createBitmap(
                myBitmap, 0, 0, myBitmap.getWidth(), myBitmap.getHeight(), matrix, true);
           
           imageView.setImageBitmap(resizedBitmap);
               	  
       }else if (requestCode == REQCODE_GALLERY){
    	  if(data == null){
    		  return;
    	  }
           ImageView imageView = (ImageView) findViewById(R.id.imgpreview);
           Uri uri = data.getData();
           
           InputStream is;
           try {
         	  is = getContentResolver().openInputStream(uri);
               Bitmap myBitmap = BitmapFactory.decodeStream(is);
               imageView.setImageBitmap(myBitmap);
               
               mTmpFile = new File(getRealPathFromURI(uri));
           } catch (FileNotFoundException e) {
         	  // TODO Auto-generated catch block
         	  e.printStackTrace();
           }
           
       }else{
     	  m_facebook.authorizeCallback (requestCode, resultCode, data);
       }
     }
  	
 	// Functions---------------------------------------------------------------------------------------------
     //file -> byte[]
     private byte[][] file2bytearr(File FileData) throws Exception {
         int size;
         int cntsize;
         int i = 0;
         final int MAXFSIZE = 1024 * 100; // 100KB
         final int MAXARRNUM = (int)Math.ceil((FileData.length() / (double)MAXFSIZE));
         
         byte[][] rt = new byte[MAXARRNUM][MAXFSIZE];
         byte[] w = new byte[1024];
         FileInputStream fin=null;
         ByteArrayOutputStream out=null;
         try {
             fin=new FileInputStream(FileData);
             out=new ByteArrayOutputStream();
             cntsize = 0;
             while (true) {
                 size=fin.read(w);
 //                if (size<=0 || i>=(MAXARRNUM-1)) break;
                 if (size<=0) break;
                 if (cntsize>=MAXFSIZE){
                 	out.close();
                 	rt[i] = out.toByteArray();
 //                	Log.d("ConbuMusubiDBG", "Data1 --- "+Base64.encodeToString(out.toByteArray(), Base64.DEFAULT));
                 	out = new ByteArrayOutputStream();
                 	cntsize = 0;
                 	i++;
                 }
                 out.write(w,0,size);
                 cntsize += size;
             }
             fin.close();
             out.close();
         	rt[i] = out.toByteArray();
     		i++;
         	while(i<MAXARRNUM){
         		rt[i] = null;
         		i++;
         	}
             return rt;
         } catch (Exception e) {
             try {
                 if (fin!=null) fin.close();
                 if (out!=null) out.close();
             } catch (Exception e2) {
             }
             throw e;
         }
     }
     
     //file -> byte[]
     private byte[] file2byte(File FileData) throws Exception {
         int size;
         byte[] w = new byte[1024];
         FileInputStream fin=null;
         ByteArrayOutputStream out=null;
         try {
             fin=new FileInputStream(FileData);
             out=new ByteArrayOutputStream();
             while (true) {
                 size=fin.read(w);
                 if (size<=0) break;
                 out.write(w,0,size);
             }
             fin.close();
             out.close();
             return out.toByteArray();
         } catch (Exception e) {
             try {
                 if (fin!=null) fin.close();
                 if (out!=null) out.close();
             } catch (Exception e2) {
             }
             throw e;
         }
     }
     // byte[] -> file
     private void byte2file(byte[] w, String file) 
         throws Exception {
         FileOutputStream fos=null;
         try {
             fos=new FileOutputStream(file);
             fos.write(w);
             fos.close();
         } catch (Exception e) {
             if (fos!=null) fos.close();
             throw e;
         }
     }
     
     public static float rotationForImage(Context context, Uri uri) {
         if (uri.getScheme().equals("content")) {
         String[] projection = { Images.ImageColumns.ORIENTATION };
         Cursor c = context.getContentResolver().query(
                 uri, projection, null, null, null);
         if (c.moveToFirst()) {
             return c.getInt(0);
         }
     } else if (uri.getScheme().equals("file")) {
         try {
             ExifInterface exif = new ExifInterface(uri.getPath());
             int rotation = (int)exifOrientationToDegrees(
                     exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                             ExifInterface.ORIENTATION_NORMAL));
             return rotation;
         } catch (IOException e) {
             Log.e("ConbuMusubi", "Error checking exif", e);
         }
     }
         return 0f;
     }
 
     private static float exifOrientationToDegrees(int exifOrientation) {
 	    if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_90) {
 	        return 90;
 	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_180) {
 	        return 180;
 	    } else if (exifOrientation == ExifInterface.ORIENTATION_ROTATE_270) {
 	        return 270;
 	    }
 	    return 0;
     }
 
     private String getRealPathFromURI(Uri contentUri) {
 
         String [] proj={MediaStore.Images.Media.DATA};
         Cursor cursor = managedQuery( contentUri, proj, null, null, null);
         int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
         cursor.moveToFirst();
 
         return cursor.getString(column_index);
     }
 }
