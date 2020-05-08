 package com.hipsterrific.app;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import com.hipsterrific.R;
 import com.hipsterrific.FaceDetector.BoundingBoxOverlaySpec;
 import com.hipsterrific.FaceDetector.Face;
 import com.hipsterrific.FaceDetector.FaceDetector;
 import com.hipsterrific.FaceDetector.ImageHelper;
 import com.hipsterrific.FaceDetector.FaceDetector.ClassifierType;
 import com.hipsterrific.FaceDetector.GlassesOverlaySpec;
 import com.hipsterrific.FaceDetector.OverlaySpec;
 
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.CursorLoader;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.graphics.Rect;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.ViewGroup.LayoutParams;
 import android.view.animation.AlphaAnimation;
 import android.widget.AbsoluteLayout;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.RelativeLayout;
 import android.widget.Toast;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.NavUtils;
 import android.support.v4.app.FragmentActivity;
 
 public class DisplayImage extends FragmentActivity {
 
 	protected Bitmap bmp;
 	FaceDetector faceDetector;
 	ArrayList<File> classifierFiles; 	
 	HashMap<FaceDetector.ClassifierType, String> classifierPaths;
 	HashMap<Button, Face> buttonMap;  
 	ArrayList<OverlaySpec> overlaySpecs;
 	private String imagePath;
 	private float orientation;
 	protected ProgressDialog progress;
 	
 	public static File getTempFileForResource(Context context, int id) throws IOException {
 		InputStream in = context.getResources().openRawResource(id);
 		
 		File file = File.createTempFile("resource", null, context.getFilesDir());
 		
 		OutputStream out = new FileOutputStream(file);
 		
 		int read = 0;
 		byte[] bytes = new byte[1024];
 	 
 		while ((read = in.read(bytes)) != -1) {
 			out.write(bytes, 0, read);
 		}
 	 
 		in.close();
 		out.close();
 		
 		return file;
 	}
 	
 	
 	private ImageView imageView;
 	LayoutInflater controlInflater = null;
 	private Uri path;
 	private RelativeLayout displayImg;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		
 		
 		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
 		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		setContentView(R.layout.activity_display_image);
 		this.displayImg = (RelativeLayout)this.findViewById(R.id.display_layout);
 		path = getIntent().getData();
 		imagePath = getRealPathFromURI(path);
 		
 		if(getIntent().hasExtra("orientation")){
 			orientation = getIntent().getExtras().getFloat("orientation");
 			Log.d("Orientation",orientation+" f");
 		}else{
 			orientation = 0;
 		}
 		
 		
 		imageView = (ImageView) findViewById(R.id.result);
 		imageView.setAdjustViewBounds(true);
 		galleryAddPic(imagePath);
 		imageView.setImageBitmap(ImageHelper.decodeSampledBitmapFromPath(imagePath, 700, 700));
 		
 		controlInflater = LayoutInflater.from(getBaseContext());
 	    View viewControl = controlInflater.inflate(R.layout.controldisplay, null);
 	    
 	    LayoutParams layoutParamsControl
 	    	= new LayoutParams(LayoutParams.MATCH_PARENT,
 	    	LayoutParams.MATCH_PARENT);
 	    
 	    this.addContentView(viewControl, layoutParamsControl);
 	    
 	    buttonMap = new HashMap<Button, Face>();
 	    
 	    progress = new ProgressDialog(this);
 	    progress.setMessage(getString(R.string.detection_string));
 	    progress.setIndeterminate(true);
 	    try {
 	    	processImage();
 	    } catch (Exception e) {
 	    	e.printStackTrace();
 	    }
 
 	}
 	
 	/*
 	 * Adds a pic to the gallery by starting the Media Scanner intent
 	 */
 	private void galleryAddPic(String img_path) {
 	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
 	    File f = new File(img_path);
 	    Uri contentUri = Uri.fromFile(f);
 	    mediaScanIntent.setData(contentUri);
 	    this.sendBroadcast(mediaScanIntent);
 	}
 	
 	private void processImage() {
 		this.classifierFiles = new ArrayList<File>();
 		this.classifierPaths = new HashMap<FaceDetector.ClassifierType, String>();
 		populateClassifierPaths();
 		
 		this.overlaySpecs = new ArrayList<OverlaySpec>();
 		populateOverlaySpecs();
 		
 		//onPostExecute is called in the main (UI) thread, therefore we are able to call doOverlay()
 		//
 		try {
 			faceDetector = new FaceDetector(imagePath,this.orientation , classifierPaths, this.progress){
 				@Override
 				  public void onPostExecute(Boolean result) {
 					Log.d("AsyncTask", "PostExecute");
 					if(result){
 						progress.dismiss();
 						addFaceButtons();
 					}else{
 						Log.d("AsyncTask", "Something went wrong!");
 					}
 				  }
 			};
 			faceDetector.execute();
 		} catch (Exception e) {
 			e.printStackTrace();
 			return;
 		}
 	}
 
 	//@TODO:: change the setX and setY to API level10 functions
 	 @SuppressLint("NewApi") protected void addFaceButtons() {
 			int viewX = displayImg.getLeft(), viewY = displayImg.getTop();
 			int originX =imageView.getDrawable().getIntrinsicWidth();
 			int originY = imageView.getDrawable().getIntrinsicHeight();
 			int imgViewX =imageView.getWidth();
 		    int imgViewY = imageView.getHeight();
 		    double resize = (double)(imgViewX) / originX;
 			
 			Log.d("faceButtons","ViewX: "+viewX+" viewY: "+viewY+" width: "+displayImg.getWidth()+" height: "+displayImg.getHeight());
 			Log.d("faceButtons","OriginX: "+originX+" OriginY: "+originY);
 			Log.d("faceButtons","ImageX: "+imgViewX+" ImageY: "+imgViewY);
 			
 			int i = 0;
 			for(Face face : faceDetector.faces){
 				Log.d("faceButtons","adding button "+ i);
 				Button b = new Button(this);			
 				b.setText(""+i);
 				b.setTextSize(12);
 				
 				b.setOnClickListener(new OnClickListener() {
 					@Override
 					public void onClick(View v) {
 						Button b = (Button)v; 
 						Face face = buttonMap.get(b);
 						Toast.makeText(getApplicationContext(), "Clicked a face in rect: "+face.rect.flattenToString(), Toast.LENGTH_LONG).show();
						
						FragmentManager fm = getSupportFragmentManager();
                            OverlayPickerDialog pickerDialog = new OverlayPickerDialog(face, overlaySpecs);
                            pickerDialog.show(fm,"overlay_picker");
 					}
 				});
 
 				b.setX((int)(face.rect.left * resize));
 				b.setY((int)(face.rect.top * resize));
 				b.setWidth((int)(face.rect.width() * resize));
 				b.setHeight((int)(face.rect.height() *resize));
 				displayImg.addView(b);
 				buttonMap.put(b, face);
 				i++;
 			}
 	}
 
 	public void rotateImager(View view){
 		imageView = (ImageView) findViewById(R.id.result);
 		BitmapDrawable drawable = (BitmapDrawable) imageView.getDrawable();
 		Bitmap myImg = drawable.getBitmap();
 		
 		Matrix matrix = new Matrix();
 		matrix.postRotate(90);
 
 		Bitmap rotated = Bitmap.createBitmap(myImg, 0, 0, myImg.getWidth(), myImg.getHeight(),
 		        matrix, true);
 
 		imageView.setImageBitmap(rotated);
 	}
 	
 	
 	
 	private void deleteClassifierFiles() {
 		if (this.classifierFiles == null) {
 			Log.i("DisplayImage","No classifier files");
 			return;
 		}
 		Log.i("DisplayImage","Deleting "+this.classifierFiles.size()+" classifier files");
 		for (File file : this.classifierFiles) {
 			file.delete();
 		}
 	}
 	
 	@Override
 	public void onDestroy(){
 		super.onDestroy();
 		deleteClassifierFiles();
 	}
 	
 	@Override
 	public void onPause(){
 		super.onPause();
 		deleteClassifierFiles();
 	}
 	
 	@Override
 	public void onBackPressed() {
 		NavUtils.navigateUpFromSameTask(this);
 	}
 	
 	private void populateOverlaySpecs() {
 		Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.raw.overlay_glasses);
 		GlassesOverlaySpec spec = new GlassesOverlaySpec(bitmap);
 		BoundingBoxOverlaySpec boxOverlaySpec = new BoundingBoxOverlaySpec();
 		
 		this.overlaySpecs.add(spec);
 		this.overlaySpecs.add(boxOverlaySpec);
 	}
 
 	private void populateClassifierPaths() {
 		HashMap<ClassifierType, Integer> classifierResources = new HashMap<ClassifierType, Integer>();
 		
 		classifierResources.put(ClassifierType.FACE,      R.raw.classifier_face);
 		classifierResources.put(ClassifierType.EYES,	  R.raw.classifier_eyes);
 		classifierResources.put(ClassifierType.NOSE,      R.raw.classifier_nose);
 		classifierResources.put(ClassifierType.MOUTH,     R.raw.classifier_mouth);
 		
 		File classifierFile = null;
 		for (HashMap.Entry<ClassifierType, Integer> entry : classifierResources.entrySet()) {
 		    ClassifierType classifierType = entry.getKey();
 		    int resourceId = entry.getValue();
 		    
 			try {
 				classifierFile = getTempFileForResource(getApplicationContext(), resourceId);
 				classifierFiles.add(classifierFile);
 				classifierPaths.put(classifierType, classifierFile.getAbsolutePath());
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	@SuppressLint("NewApi")
 	@SuppressWarnings("deprecation")
 	private String getRealPathFromURI(Uri contentUri) {
 	    String[] proj = { MediaStore.Images.Media.DATA };
 	    
 	    Cursor cursor = null;
         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
         	cursor = managedQuery(contentUri, proj, null, null, null);
         }
         else {
         	CursorLoader loader = new CursorLoader(getBaseContext(), contentUri, proj, null, null, null);
         	cursor = loader.loadInBackground();
         }
         
         if (cursor == null) {
         	return contentUri.toString();
         }
         
 	    int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
 	    cursor.moveToFirst();
 	    return cursor.getString(column_index);
 	}
 
 }
