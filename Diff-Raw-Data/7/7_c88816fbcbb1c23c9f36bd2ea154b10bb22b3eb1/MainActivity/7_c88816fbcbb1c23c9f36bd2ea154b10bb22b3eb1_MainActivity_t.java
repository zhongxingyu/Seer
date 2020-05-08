 package com.cloudspace.tilt;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 
 import com.cloudspace.tilt.Facebook.FacebookPoster;
 import com.cloudspace.tilt.Facebook.FacebookPoster.PendingAction;
 import com.cloudspace.tilt.ImageManip.ImageLoader;
 import com.cloudspace.tilt.ImageManip.Orientation;
 import com.cloudspace.tilt.TiltShift.TiltShift;
 import com.cloudspace.tilt.Tools.SaveAsync;
 import com.cloudspace.tilt.Tools.TiltAsync;
 import com.cloudspace.tilt.Tools.TwitterShareAsync;
 import com.cloudspace.tilt.Twitter.TwitterPoster;
 import com.cloudspace.tilt.Twitter.TwitterTools;
 import com.cloudspace.tilt.Twitter.TwitterUtils;
 import com.facebook.Session;
 import com.facebook.UiLifecycleHelper;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.ContentValues;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.net.Uri;
 import android.os.AsyncTask;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.provider.MediaStore;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.text.InputFilter;
 import android.text.format.Time;
 import android.view.LayoutInflater;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.View.OnClickListener;
 import android.view.View.OnTouchListener;
 import android.widget.EditText;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 
 public class MainActivity extends FragmentActivity implements OnClickListener,OnTouchListener {   
 	private final int GALLERY_LOAD_IMAGE = 0;
 	private final int CAMERA_LOAD_IMAGE = 1;
 	
 	public Bitmap baseImage;
 	
 	private static View top;
 	private static View bottom;
 	private static ImageView imageView1;
 	public ImageView imageView2;
 	public ImageView imageView3;
 	public ImageButton share;
 	public ImageButton save;
 	public ProgressBar progressbar;
 	
 	public Integer width;
 	private static Integer height;
 	private static Integer blurTop;
 	private static Integer blurBottom;
 	private static Integer blurStrengthTop;
 	private static Integer blurStrengthBottom;
 	public static Integer start_x;
 	
 	public Boolean isSample = false;
 	public Boolean isSaved = false;
 	public static String picturePath = null;
 	private Uri imageUri;
 	public TiltShift tilt;
 	static FacebookPoster facebookPoster;
 	public TwitterTools twitterTools;
     private SharedPreferences prefs;
     public Boolean twitterAuthed = false;
     public String statusUpdate;
     
     
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		facebookPoster = new FacebookPoster(this);
 		twitterTools = new TwitterTools(this);
 		
 		// Initialize the Facebook SDK helper
 		facebookPoster.uiHelper = new UiLifecycleHelper(this, facebookPoster.callback);
 		facebookPoster.uiHelper.onCreate(savedInstanceState);
 
 		// Check if any Facebook actions are in-progress
         if (savedInstanceState != null) {
             String name = savedInstanceState.getString(facebookPoster.PENDING_ACTION_BUNDLE_KEY);
             FacebookPoster.pendingAction = PendingAction.valueOf(name);
         }
         
         // Set the main view of the app
 		setContentView(R.layout.activity_main);
 		
 		// Define user touch areas
 	    top = (ImageView) findViewById(R.id.top);
 		bottom = (ImageView) findViewById(R.id.bottom);
 		
 		// Define touch events
 		top.setOnTouchListener(this);
 		bottom.setOnTouchListener(this);
 		
 		// Define buttons
 	    ImageButton load = (ImageButton) findViewById(R.id.load);
 	    ImageButton camera = (ImageButton) findViewById(R.id.camera);
 	    share = (ImageButton) findViewById(R.id.share);
 	    save = (ImageButton) findViewById(R.id.save);
 	    
 	    // Define onClick
 	    load.setOnClickListener(this);
 	    camera.setOnClickListener(this);
 	    share.setOnClickListener(this);
         save.setOnClickListener(this);
         save.setEnabled(false);
         share.setEnabled(false);
         
         // Define views for blur images
      	imageView1 = (ImageView) findViewById(R.id.imageView1);
      	imageView2 = (ImageView) findViewById(R.id.imageView2);
      	imageView3 = (ImageView) findViewById(R.id.imageView3);
      	
      	// Define Progressbar for Async Activites
      	progressbar = (ProgressBar) findViewById(R.id.progress);
      	progressbar.setVisibility(View.INVISIBLE);     	
 
      	// Define View for Facebook Login Fragment
      	facebookPoster.controlsContainer = (ViewGroup) findViewById(R.id.root);
 
         final FragmentManager fm = getSupportFragmentManager();
 
         // Listen for changes in the back stack so we know if a fragment got popped off because the user
         // clicked the back button.
         fm.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
             @Override
             public void onBackStackChanged() {
                 if (fm.getBackStackEntryCount() == 0) {
                     // We need to re-show our UI.
                 	facebookPoster.controlsContainer.setVisibility(View.VISIBLE);
                 }
             }
         });
 	}
 
 	@Override
 	public void onClick(View v) {
 		switch(v.getId()){
 	    	case R.id.load:
 	    		// Load picture from gallery
 	    		loadPicture();
 	    		break;
 	    		
 	        case R.id.camera:
 	        	// Launch camera and load picture
 	        	takePicture();
 	            break;
 	            
 	        case R.id.share:
 	        	// Save image in background then launch sharing if network available
 	        	isSaved = true;
 	        	SaveAsync shareSave = new SaveAsync(this);
 	        	shareSave.execute();
 	        	share();
 	        	break;
 	        	
 	        case R.id.save:
 	        	// Save tilt shifted image to gallery
 	        	if(isSaved == true){
 	        		Toast.makeText(MainActivity.this,"Image Saved",Toast.LENGTH_SHORT).show();
 	        	}
 	        	else{
 	        		SaveAsync save = new SaveAsync(this);
 		        	save.execute();
 	        	}
 		}	
 	}
 	
 	@Override
 	public boolean onTouch(View v, MotionEvent event) {
 		// Get current location of user touch
 		int y = (int) event.getY();
 		int x = (int) event.getX();
 		switch (event.getAction()) {
 			case MotionEvent.ACTION_DOWN:
 				start_x = (int) event.getX();
 			break;
 		}
 
 		// Round the touch event so that the blur effect takes a legitimate
 		// motion and not just a tap
 		x=Math.round(((int) event.getX()-start_x)/2);
 		if(x > -5 && x < 5){
 			x=0;
 		}
 
 		switch(v.getId()){
 			case R.id.top:
 				// Adjust top blur
 				adjustTopBlur(x,y);
 	    	return true;
 			
 			case R.id.bottom:
 				// Because we are working from the bottom up we need to
 				// reverse our y value and then apply the blur
 				y = y * -1;
 				adjustBottomBlur(x,y);
 			return true;
 		}
 		return true;
 	}
 	
 	public void loadPicture(){
 		// Launch the Gallery Image picker
 		Intent i = new Intent(
         Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         startActivityForResult(i, GALLERY_LOAD_IMAGE);
 	}
 	
 	private void takePicture(){
 		// Launch the camera to take and save an image with this name
 		Time today = new Time(Time.getCurrentTimezone());
 		today.setToNow();
 	    String fileName="MI_"+ today.format("%Y%m%d%H%M%S") +".jpg";
     	ContentValues values = new ContentValues();
     	values.put(MediaStore.Images.Media.TITLE, fileName);
     	imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
     	
     	Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
     	intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
     	intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
     	startActivityForResult(intent, CAMERA_LOAD_IMAGE);
 	}
 	
 	@Override
     public void onActivityResult(int requestCode, int resultCode, Intent data) {
  		super.onActivityResult(requestCode, resultCode, data);
     	if(requestCode == GALLERY_LOAD_IMAGE || requestCode == CAMERA_LOAD_IMAGE){    		
     		// Get the file path of the image to be modified
 	    	if(requestCode == GALLERY_LOAD_IMAGE && resultCode == RESULT_OK && null != data){
 	    		picturePath = ImageLoader.loadFromGallery(picturePath, data, this);
 	    	}
 	    	else if(requestCode == CAMERA_LOAD_IMAGE && resultCode == RESULT_OK){
 	    		picturePath = ImageLoader.loadFromCamera(picturePath, imageUri, this);
 	    	}
 	    	if(resultCode == RESULT_OK){
 		    	// Blank the bitmap we are loading the image to
 		    	if(baseImage != null){
 		    		baseImage = null;
 		    	}
 		    	
 		    	// Make bitmap reusable to save on memory
 		    	BitmapFactory.Options options = new BitmapFactory.Options();
 		    	options.inBitmap = baseImage;
 		    	
 		    	// Check the size of the bitmap we are loading 
 		    	BitmapFactory.Options bounds = new BitmapFactory.Options();
 		    	bounds.inJustDecodeBounds = true;
 		    	BitmapFactory.decodeFile(picturePath,bounds);
 		    	
 		    	//If the image is very large we need to sample it to save on memory
 		    	if(bounds.outWidth > 4000 || bounds.outHeight > 4000){
 		    		options.inSampleSize = 6;
 		    		isSample = true;
 		    	}
 		    	if(bounds.outWidth > 2000 || bounds.outHeight > 2000){
 		    		options.inSampleSize = 4;
 		    		isSample = true;
 		    	}
 		    	else if(bounds.outWidth > 1000 || bounds.outHeight > 1000){
 		    		options.inSampleSize = 2;
 		    		isSample = true;
 		    	}
 		    	// Rotate the image to the proper orientation
 		    	baseImage = Orientation.orientImage(BitmapFactory.decodeFile(picturePath,options),picturePath);
 		    	TiltAsync tiltAsync = new TiltAsync(this);
 		    	tiltAsync.execute();
 	    	}
     	}
     	else{
     		// Update Facebook helper
     		facebookPoster.uiHelper.onActivityResult(requestCode, resultCode, data);
     	}
  	}
 	
 	// Apply the user selected image and set margins for the touch areas
 	public void prepareViews(){
 		height = tilt.height;
 	    width = tilt.width;
 	    imageView1.setImageBitmap(tilt.contrastImage);
 	    setMargins();
 	}
 	
 	// Set top and bottom touch areas to extend the width of the image
 	private void setMargins() {
 		int margin = imageView1.getTop();
 			
 		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) top.getLayoutParams();
 		params.topMargin=margin;
 		if(tilt != null){
 			params.width=tilt.width;
 		}
 		top.setLayoutParams(params);
 			
 		RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) bottom.getLayoutParams();
 		params2.bottomMargin=margin;
 		if(tilt != null){
 			params2.width=tilt.width;
 		}
 		bottom.setLayoutParams(params2);
 	}
 	
 	// Adjust the top blur of the image based on user input
 	public void adjustTopBlur(Integer x, Integer y){
 		// Get parameters for top touch area 
 		RelativeLayout.LayoutParams paramsTop = (RelativeLayout.LayoutParams) top.getLayoutParams();
 		
 		// If user is making adjustments inside the image area apply them
 		if(y > 40 && y < height-40){
 			
 			// Adjust height of top touch area
 			paramsTop.height = y;
 			blurTop = paramsTop.height;
 			blurStrengthTop = x;
 			top.setLayoutParams(paramsTop);
 			
 			// Apply gradient
 			tilt.shiftTop(x,y+30);
 		    imageView2.setImageBitmap(tilt.topShift);
 		}
 	}
 	
 	// Adjust the bottom blur of the image based on user input
 	public void adjustBottomBlur(Integer x, Integer y){
 		// Get parameters for bottom touch area 
 		RelativeLayout.LayoutParams paramsBottom = (RelativeLayout.LayoutParams) bottom.getLayoutParams();
 		
 		// Compensate for coming up from the bottom
 		int current = paramsBottom.height;
 		current = current+y;
 		
 		
 		// If user is making adjustments inside the image area apply them
 		if(current > 50){
 			
 			// Adjust height of bottom touch area
 			paramsBottom.height = current;
 			blurBottom = paramsBottom.height;
 			blurStrengthBottom = x;
 			bottom.setLayoutParams(paramsBottom);
 			
 			// Apply gradient
 			tilt.shiftBottom(x,current);
 		    imageView3.setImageBitmap(tilt.bottomShift);
 		}
 	}
 	
 	// Launch the share dialog
 	public void share(){
 		if(isNetworkAvailable()){
 			new ShareAsync().execute();
 		}
 		else{
 			Toast.makeText(MainActivity.this,"No Network Available",Toast.LENGTH_SHORT).show();
 		}
 	}
 		 
 	// Launch email sharing
 	public void sendEmail(){
 		// Create new Intent to open the devices email programs
 		Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
 		
 		// Set email type to accept images
 	    emailIntent.setType("image/png");
 	    
 	    // Get the file path of the image to be shared
 	    Uri uri = Uri.fromFile(tilt.sharePath);
 	    
 	    // Insert the image as an attachment
 	    emailIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
 
 	    // Launch the email program
 	    startActivity(emailIntent);
 	}
 	
 	@SuppressWarnings("deprecation")
 	public void sendTweet(){
 		showDialog(2);
 	}
 	
 	@Override
 	protected void onSaveInstanceState(Bundle outState) {
 	    super.onSaveInstanceState(outState);
 	    facebookPoster.uiHelper.onSaveInstanceState(outState);
 	    outState.putString(facebookPoster.PENDING_ACTION_BUNDLE_KEY, FacebookPoster.pendingAction.name());
 
 	    // Stored the modifications to the image
 	    if(tilt != null){
 	    	if(blurTop != null){
 	    		outState.putInt("Top",Math.round(blurTop/(height/100)));
 	    	}
 			if(blurBottom != null){
 				outState.putInt("Bottom",Math.round(blurBottom/(height/100)));
 			}
 			if(blurStrengthTop != null){
 				outState.putInt("TopStrength",blurStrengthTop);
 			}
 			if(blurStrengthBottom != null){
 				outState.putInt("BottomStrength",blurStrengthBottom);
 			}
 			if(tilt.sharePath != null){
 				outState.putString("SharePath", tilt.sharePath.getPath());
 			}
 		    outState.putBoolean("Sample",isSample);
 		    outState.putBoolean("Saved",isSaved);
 		    outState.putParcelable("Base",baseImage);	    
 		}
 	}
 	
 	@Override
 	    protected void onRestoreInstanceState(Bundle savedState) {
 	    super.onRestoreInstanceState(savedState);
 	    
 	    // If there is saved data reload it
	    tilt = new TiltShift(MainActivity.this);
 	    if(savedState != null && tilt != null){
 	    	// Reload orginal image
 	    	BitmapFactory.Options options = new BitmapFactory.Options();
 	    	options.inBitmap = baseImage;
 	    	baseImage = savedState.getParcelable("Base");
 	    	isSaved = savedState.getBoolean("Saved");
 	    	save.setEnabled(true);
 	    	share.setEnabled(true);
 
 	    	// Restart and apply base adjustments
 	    	facebookPoster = new FacebookPoster(MainActivity.this);
 	    	facebookPoster.uiHelper = new UiLifecycleHelper(MainActivity.this, facebookPoster.callback);
 			twitterTools = new TwitterTools(MainActivity.this);
 	    	tilt.load(baseImage);
 	    	prepareViews();
 	    	imageView2.setImageBitmap(tilt.topShift);
 	    	imageView3.setImageBitmap(tilt.bottomShift);
 	    	if(savedState.getString("SharePath") != null){
 	    		tilt.sharePath = new File(savedState.getString("SharePath"));
 	    	}
 	    	// Apply stored adjustments to top blur
 	    	if((Integer) savedState.getInt("Top") != null){
 	    		adjustTopBlur((Integer) savedState.getInt("TopStrength"),Math.round((Integer) savedState.getInt("Top")*(height/100)));
 	    	}
 	    	// Apply stored adjustments to bottom blur
 	    	if((Integer) savedState.getInt("Bottom") != null){
 	    		adjustBottomBlur((Integer) savedState.getInt("BottomStrength"),(Integer) savedState.getInt("Bottom"));
 	    	}
 	    }
 	}
 
 	// Reset margin on focus, usually orientation change
 	@Override
 	public void onWindowFocusChanged(boolean hasFocus) {
     	super.onWindowFocusChanged(hasFocus);
     	if(hasFocus){
     		if(tilt != null){
     			setMargins();
     		}
     	}
     }
 	
 	@Override
 	protected void onResume() {
 	    super.onResume();
 	    // Update Facebook helper
 	    if(facebookPoster != null){
 	    	facebookPoster.uiHelper.onResume();
 	    }
 	}
 	
 	@Override
 	public void onPause() {
 	    super.onPause();
 	    // Update Facebook helper
 	    facebookPoster.uiHelper.onPause();
 	}
 
 	@Override
 	public void onDestroy() {
 		super.onDestroy();
 		// Update Facebook helper
 		facebookPoster.uiHelper.onDestroy();
 	}
 	
 	private class ShareAsync extends AsyncTask<Void, Integer, Void>{
 
 		@Override
 	    protected void onPreExecute(){
 	    	super.onPreExecute();
 		}
 			
 		@Override
 		protected Void doInBackground(Void... params) {
 			// Check if the user has already logged in to twitter before
 			prefs = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
 			if (TwitterUtils.isAuthenticated(prefs)) {
 				twitterAuthed = true;
 		    }
 			return null;
 		}
 			
 		@Override
 		protected void onProgressUpdate(Integer... values) {
 			super.onProgressUpdate(values);
 		}
 			
 		@Override
 		protected void onPostExecute(Void result) {
 			super.onPostExecute(result);
 			// Create Dialog options
 			String [] dialogButtons;
 		    dialogButtons = new String[4];
 		        
 		    /** Set the button names */
 		    // Check if the user has already logged in to Facebook
 		    Session session = Session.getActiveSession();
 		    boolean enableButtons = (session != null && session.isOpened());
 		    if(enableButtons == false){
 		    	dialogButtons[0]= new String("Login to Facebook");
 		    }
 		    else{
 		    	dialogButtons[0]= new String("Post to Facebook");
 		    }
 		    
 		    if (twitterAuthed == true) {
 		    	dialogButtons[1]= new String("Post to Twitter");
 		    }
 		    else{
 		    	dialogButtons[1]= new String("Login to Twitter");
 		    }
 		    
 		    dialogButtons[2]= new String("Email");
 		    dialogButtons[3]= new String("Cancel");
 		        
 		    AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
 				
 			/** Set dialog box title */
 			dialog.setTitle("Share");	
 
 			/** Set the button activities */
 			dialog.setItems(dialogButtons, new DialogInterface.OnClickListener() {
 				public void onClick(DialogInterface dialog, int which) {
 						
 					/** Facebook Button */
 					if(which==0){
 						Session session = Session.getActiveSession();
 						// If the user is not logged in to Facebook prompt them to
 			        	if (!session.isOpened() && !session.isClosed()) {
 					        session.openForRead(new Session.OpenRequest(MainActivity.this).setCallback(facebookPoster.statusCallback));
 					    } 
 			        	// If the user is logged in to Facebook begin photo sharing
 			        	else {
 					        Session.openActiveSession(MainActivity.this, true, facebookPoster.statusCallback);  
 					    }
 			        	
 				        Session session2 = Session.getActiveSession();
 					    boolean enabled = (session2 != null && session2.isOpened());
 						if(enabled == true){
 							facebookPoster.performPublish(PendingAction.POST_PHOTO);
 						}
 						dialog.dismiss();
 					}
 						
 					/** Twitter Button */
 					else if(which==1){
 						// If the user is logged in to Twitter begin photo sharing
 						if (twitterAuthed == true) {
 							sendTweet();
 							dialog.dismiss();
 						}
 						// If the user is not logged in to Twitter prompt them too
 						else{
 							sendTweet();
 							Intent i = new Intent(getApplicationContext(), TwitterPoster.class);
 							startActivity(i);
 							dialog.dismiss();
 						}
 					}
 						
 					/** Email Button */
 					else if(which == 2){
 						// Begin photo sharing
 						sendEmail();
 						dialog.dismiss();
 					}
 						
 					/** Cancel Button */
 					else if(which == 3){
 						dialog.dismiss();
 					}
 				}
 			});
 				
 			/** Display the dialog box */
 			dialog.show();	
 		}
 		 
 	 }
 	
 	@SuppressWarnings("deprecation")
 	public void postPhoto() {
 		// If the user is logged in to Facebook start sharing
         if (FacebookPoster.hasPublishPermission()) {
         	showDialog(1);
         } else {
         	FacebookPoster.pendingAction = PendingAction.POST_PHOTO;
         }
     }
 	
 	protected Dialog onCreateDialog(int id) {
 		/** Create custom layout */
 	       LayoutInflater factory = LayoutInflater.from(this);
 	       final View textEntryView = factory.inflate(R.layout.share_message, null);
 	       
 	       /** Get the edit text box and set max characters because it's twitter */
 	       final EditText edit_message = (EditText)textEntryView.findViewById(R.id.post_txt);
 	       int maxLength = 120;
 	       InputFilter[] FilterArray = new InputFilter[1];
 	       FilterArray[0] = new InputFilter.LengthFilter(maxLength);
 	       edit_message.setFilters(FilterArray);
 		try{
 			switch (id) { 
 				case 1:
 				       /** Create Dialog */
 				       return new AlertDialog.Builder(MainActivity.this)
 			               .setTitle("Status")
 			               .setView(textEntryView)
 			               .setPositiveButton("SHARE", new DialogInterface.OnClickListener() {
 			            	   public void onClick(DialogInterface dialog, int whichButton) {
 			            		   // Upload the image to Facebook
 			           	    		progressbar.setVisibility(View.VISIBLE);
 			           	    		try {
 										facebookPoster.sharePhoto(edit_message.getText().toString(),tilt.sharePath);
 									} catch (FileNotFoundException e) {
 										e.printStackTrace();
 									}
 			            	   }
 			               })
 			               .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
 			            	   public void onClick(DialogInterface dialog, int whichButton) {
 			            		   progressbar.setVisibility(View.INVISIBLE);
 			            	   }
 			               })
 			               .create(); 
 			case 2:
 			       /** Create Dialog */
 			       return new AlertDialog.Builder(MainActivity.this)
 		               .setTitle("Status")
 		               .setView(textEntryView)
 		               .setPositiveButton("SHARE", new DialogInterface.OnClickListener() {
 		            	   public void onClick(DialogInterface dialog, int whichButton) {
 		            		   // Load the image into a Twitter Status and update the users status
 		            		   statusUpdate = edit_message.getText().toString();
 		            		   TwitterShareAsync twitterAsync = new TwitterShareAsync(MainActivity.this);
 		            		   twitterAsync.execute();
 		            	   }
 		               })
 		               .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
 		            	   public void onClick(DialogInterface dialog, int whichButton) {
 		            	   }
 		               })
 		               .create();
 		}
 		}
 		catch (Exception ex) {
 			finish();
 		}
 		return null;
 	}
 	
 	// Check if there is an internet connection
 	public boolean isNetworkAvailable() {
 	    ConnectivityManager connectivityManager 
 	          = (ConnectivityManager) getSystemService(MainActivity.CONNECTIVITY_SERVICE);
 	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
 	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
 	}
}  
