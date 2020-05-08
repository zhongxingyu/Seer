 package com.memelab.jandig;
 
 import android.os.Bundle;
 import android.util.Log;
 import edu.dhbw.andar.ARToolkit;
 import edu.dhbw.andar.AndARActivity;
 import edu.dhbw.andar.exceptions.AndARException;
 import edu.dhbw.andar.interfaces.OpenGLRenderer;
 //import edu.dhbw.andar.sample.CustomObject;
 import edu.dhbw.andar.sample.CustomRenderer;
 //import edu.dhbw.andar.util.IO;
 
 
 import java.io.BufferedReader;
 //import java.io.BufferedWriter;
 import java.io.File;
 //import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.OutputStream;
 //import java.io.FileReader;
 //import java.io.FileWriter;
 import java.io.IOException;
 //import java.io.InputStreamReader;
 import java.io.InputStream;
 //import java.net.URI;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 //import java.util.Date;
 import java.util.Arrays;
 
 //import android.app.ProgressDialog;
 import android.content.ContentValues;
 //import android.content.ContentResolver;
 //import android.content.Context;
 //import android.content.Intent;
 //import android.content.res.Resources;
 import android.graphics.Bitmap;
 //import android.graphics.Bitmap.CompressFormat;
 import android.net.Uri;
 import android.os.AsyncTask;
 //import android.os.Debug;
 import android.provider.MediaStore;
 //import android.view.Menu;
 //import android.view.MenuItem;
 import android.view.MotionEvent;
 //import android.view.SurfaceHolder;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.Toast;
 //import edu.dhbw.andarmodelviewer.R;
 //import edu.dhbw.andobjviewer.graphics.LightingRenderer;
 import edu.dhbw.andobjviewer.graphics.Model3D;
 import edu.dhbw.andobjviewer.models.Model;
 import edu.dhbw.andobjviewer.parser.ObjParser;
 //import edu.dhbw.andobjviewer.parser.ParseException;
 //import edu.dhbw.andobjviewer.parser.Util;
 import edu.dhbw.andobjviewer.util.AssetsFileUtil;
 import edu.dhbw.andobjviewer.util.BaseFileUtil;
 //import edu.dhbw.andobjviewer.util.SDCardFileUtil;
 
 
 /**
  * Example of an application that makes use of the AndAR toolkit.
  * @author Tobi
  *
  */
 public class JandigActivity extends AndARActivity {
 
 	/* tgh: making it compatible with augmentedModelViewer */
 	public static final boolean DEBUG = false;
 
 	private static final boolean TESTANDO = false;
 
 	//CustomObject someObject;
 	ARToolkit artoolkit;
 
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		getSurfaceView().setOnTouchListener(new TouchEventHandler());
 
 		if(TESTANDO){
 			createFromCustomObjects(savedInstanceState);
 		}
 		else{
 			//createFromAssets(savedInstanceState);
 			createFromDirsInAssets(savedInstanceState);
 		}
 		Toast.makeText(JandigActivity.this, "Jandig 0.3. Toque na tela para tirar uma foto!", Toast.LENGTH_LONG).show();
 	}
 
 	// function to register CustomObject objects onto the ARToolkit
 	private void createFromCustomObjects(Bundle savedInstanceState){
 		OpenGLRenderer renderer = new JandigRenderer();// optional, may be set to null
 		super.setNonARRenderer(renderer);// or might be omited
 
 		artoolkit = super.getArtoolkit();
 
 		/* tgh: associate different color cubes to different .patt files */
 		try {
 			//register a object for each marker type
 			//CustomObject myObject;
 			//myObject = new CustomObject("test0", "ap0_16x16.patt", 80.0, new double[]{0,0}, new float[]{255,0,0});
 			//artoolkit.registerARObject(myObject);
 			//myObject = new CustomObject("test1", "ap1_16x16.patt", 80.0, new double[]{0,0}, new float[]{0,255,0});
 			//artoolkit.registerARObject(myObject);
 			//myObject = new CustomObject("test2", "ap2_16x16.patt", 80.0, new double[]{0,0}, new float[]{0,0,255});
 			//artoolkit.registerARObject(myObject);
 
 			ImageObject myImgObject;
 
 			InputStream ins = getAssets().open("Images/ap0_16x16.png");
 			myImgObject = new ImageObject("test0", "ap0_16x16.patt", ins, 50.0, new double[]{0,0});
 			artoolkit.registerARObject(myImgObject);
 
 			ins = getAssets().open("Images/ap1_16x16.png");
 			myImgObject = new ImageObject("test0", "ap1_16x16.patt", ins, 50.0, new double[]{0,0});
 			artoolkit.registerARObject(myImgObject);
 
 			ins = getAssets().open("Images/ap2_16x16.png");
 			myImgObject = new ImageObject("test0", "ap2_16x16.patt", ins, 50.0, new double[]{0,0});
 			artoolkit.registerARObject(myImgObject);
 
 		} 
 		catch (AndARException ex){
 			// handle the exception, that means: show the user what happened
 			System.out.println("");
 		}
 		catch(Exception e) {
 			System.out.println("");
 		}
 	}
 
 
 	// function to register objects in the assets directory onto the ARToolkit
 	private void createFromAssets(Bundle savedInstanceState) {
 	 
 		OpenGLRenderer renderer = new CustomRenderer(); // optional, may be set to null
 		super.setNonARRenderer(renderer); // or might be omited
 
 		/* tgh: Model3D... wooohhooooooo!! */
 		Model model;
 		Model3D model3d;
 		ImageObject imageobject;
 
 		artoolkit = super.getArtoolkit();
 
 
 		/* tgh: get every file in assets/objModels that ends in .obj 
 		 *      and every file in assets/Images, and associate them with the correct .patt */
 		try{
 			/* 3D MODEL FILES*/
 			// get every obj file in objModels directory, create a Model3D object, 
 			//   bind it to a pattern and register it in the toolkit 
 			final String[] allObjFilesInAssets = getAssets().list("objModels");
 			for(int i=0; i<allObjFilesInAssets.length; i++) {
 				if(allObjFilesInAssets[i].endsWith(".obj")){
 					String baseName = allObjFilesInAssets[i].substring(0,allObjFilesInAssets[i].lastIndexOf("."));
 					String modelFileName = allObjFilesInAssets[i];
 					BaseFileUtil fileUtil = new AssetsFileUtil(getAssets());
 					fileUtil.setBaseFolder("objModels/");
 					ObjParser parser = new ObjParser(fileUtil);
 					if(fileUtil != null) {
 						BufferedReader fileReader = fileUtil.getReaderFromName(modelFileName);
 						if(fileReader != null) {
 							model = parser.parse(modelFileName, fileReader);
 							/* add a check here.... */
 							if(Arrays.asList(getAssets().list("")).contains(baseName+".patt")){
 								model3d = new Model3D(baseName, model,baseName+".patt", 80.0);
 								artoolkit.registerARObject(model3d);
 							}
 							else{
 								// default
 								model3d = new Model3D(model);
 								artoolkit.registerARObject(model3d);								
 							}
 							System.out.println("---created Model3D for: "+ allObjFilesInAssets[i]);
 						}
 					}
 				}
 			}
 
 			/* IMAGE FILES*/
 			// grab every image file in assets/Images
 			final String[] allImgFilesInAssets = getAssets().list("Images");
 			for(int i=0; i<allImgFilesInAssets.length; i++) {
 				// maybe add a check for .gif/.GIF/.Gif, .bmp/.BMP/.Bmp, .jpg/.jpeg/.JPG/.JPEG/.Jpg/.Jpeg and .png/.PNG/.Png
 				//if(allImgFilesInAssets[i].endsWith(".obj")) {
 				String baseName = allImgFilesInAssets[i].substring(0,allImgFilesInAssets[i].lastIndexOf("."));
 				String ImageFileName = allImgFilesInAssets[i];
 
 				InputStream ins = getAssets().open("Images/"+ImageFileName);
 
 				if(Arrays.asList(getAssets().list("")).contains(baseName+".patt")){
 					imageobject = new ImageObject(baseName, baseName+".patt", ins, 48.0);
 					artoolkit.registerARObject(imageobject);
 				}
 				else{
 					// default pattern
 					imageobject = new ImageObject(baseName, ins, 48.0);
 					artoolkit.registerARObject(imageobject);
 				}
 				System.out.println("----cretaed ImageObject for: "+ allImgFilesInAssets[i]);
 			}
 		}
 		catch(Exception e){
 			// handle the exception, that means: show the user what happened
 			System.out.println("Some exception somewhere was thrown");
 			System.out.println("string: "+e.toString());
 			System.out.println("message: "+e.getMessage());
 			if(e.getCause() != null) {
 				System.out.println("cause msg"+e.getCause().getMessage());			
 			}
 		}
 		startPreview();
 	} // createFromAssets
 
 
 	private void createFromDirsInAssets(Bundle savedInstanceState){
 		OpenGLRenderer renderer = new CustomRenderer(); // optional, may be set to null
 		super.setNonARRenderer(renderer); // or might be omited
 
 		/* tgh: Model3D... wooohhooooooo!! */
 		Model model;
 		Model3D model3d;
 
 		artoolkit = super.getArtoolkit();
 
 		// tgh: grab every directory in assets
 		//      inside each dir, look for a .obj, or a .png/.gif/.bmp/.jpg
 		//      if any of these exist, find a .patt of any name...
 		try{
 			// for every thing in assets
 			final String[] allFilesInAssets = getAssets().list("");
 			for(int i=0; i<allFilesInAssets.length; i++) {
 				String patFileName = null;
 				String objFileName = null;
 				String imgFileName = null;
 
 				// for all files inside directories in assets
 				final String[] allFilesInDir = getAssets().list(allFilesInAssets[i]);
 				for(int j=0; j<allFilesInDir.length; j++) {
 					// detect if there is a .obj
 					if(allFilesInDir[j].endsWith(".obj")){
 						objFileName = new String(allFilesInDir[j]);
 					}
 
 					// detect an image
 					else if(allFilesInDir[j].endsWith(".bmp") || allFilesInDir[j].endsWith(".gif") || allFilesInDir[j].endsWith(".jpg") || allFilesInDir[j].endsWith(".png")){
 						imgFileName = new String(allFilesInDir[j]);
 					}
 
 					// detect pattern file
 					else if(allFilesInDir[j].endsWith(".patt")){
 						patFileName = new String(allFilesInDir[j]);
 					}
				}
 
 				// if there is an obj file and a pat file --> it's a 3D .obj
 				if((objFileName!=null) && (patFileName!=null)) {
 					BaseFileUtil fileUtil = new AssetsFileUtil(getAssets());
 					fileUtil.setBaseFolder(allFilesInAssets[i]+File.separator);
 					ObjParser parser = new ObjParser(fileUtil);
 					BufferedReader fileReader = fileUtil.getReaderFromName(objFileName);
 					model = parser.parse(objFileName, fileReader);
 					model3d = new Model3D(allFilesInAssets[i], model, new String(allFilesInAssets[i]+File.separator+patFileName), 80.0);
 
 
 					// hack to overcome the copying procedure in ARToolkit.registerARObject()
 					InputStream in = getAssets().open(allFilesInAssets[i]+File.separator+patFileName);					
 					File bf = new File(getFilesDir().getAbsolutePath()+File.separator+allFilesInAssets[i]);
 					if(!bf.exists()){
 						bf.mkdir();
 					}
 					OutputStream out = new FileOutputStream(new File(bf,patFileName));
 					this.bCopy(in, out);
 
 					artoolkit.registerARObject(model3d);
 
 					System.out.println("----Created Model3D for: "+ allFilesInAssets[i]+" with "+objFileName+" and "+patFileName);
 				}
 
 				// if there is an image and a pat file, and no obj
 				//     kind of redundant, but sometimes obj models have a png...
 				else if((imgFileName!=null) && (patFileName!=null) && (objFileName == null)) {
 					InputStream ins = getAssets().open(allFilesInAssets[i]+File.separator+imgFileName);					
 					ImageObject imageobject = new ImageObject(allFilesInAssets[i], new String(allFilesInAssets[i]+File.separator+patFileName), ins, 48.0);
 
 					// hack to overcome the copying procedure in ARToolkit.registerARObject()
 					InputStream in = getAssets().open(allFilesInAssets[i]+File.separator+patFileName);					
 					File bf = new File(getFilesDir().getAbsolutePath()+File.separator+allFilesInAssets[i]);
 					if(!bf.exists()){
 						bf.mkdir();
 					}
 					OutputStream out = new FileOutputStream(new File(bf,patFileName));
 					this.bCopy(in, out);
 
 					artoolkit.registerARObject(imageobject);
 
 					System.out.println("----Created ImageObject for: "+ allFilesInAssets[i]+" with "+imgFileName+" and "+patFileName);
 				}
			}
		}
 		catch(Exception e){
 
 		}
 
 	}
 
 	private void bCopy(InputStream in, OutputStream out ) throws IOException { 
 		byte[] buffer = new byte[ 0xFFFF ]; 
 		for ( int len; (len = in.read(buffer)) != -1; ) 
 			out.write( buffer, 0, len ); 
 	}
 
 
 
 	/**
 	 * Inform the user about exceptions that occurred in background threads.
 	 * This exception is rather severe and can not be recovered from.
 	 * TODO Inform the user and shut down the application.
 	 */
 	@Override
 	public void uncaughtException(Thread thread, Throwable ex) {
 		Log.e("AndAR EXCEPTION", ex.getMessage());
 		finish();
 	}
 
 
 	///////////////
 	class TouchEventHandler implements OnTouchListener {
 		@Override
 		public boolean onTouch(View v, MotionEvent event) {
 			if(event.getAction() == MotionEvent.ACTION_UP) {
 				// some pressure thresholding...
 				if(event.getPressure() > 0.10){
 					new TakeAsyncScreenshot().execute();
 				}
 			}
 			return true;
 		}
 	}
 
 	class TakeAsyncScreenshot extends AsyncTask<Void, Void, Void> {
 		private String errorMsg = null;
 
 		private File imgDir  = new File("/sdcard/Jandig/");
 		private File imgFile = null;
 
 		private Calendar calendar = Calendar.getInstance();
 		private SimpleDateFormat sdfD = new SimpleDateFormat("yyyyMMdd");
 		private SimpleDateFormat sdfT = new SimpleDateFormat("HHmmss");
 
 		@Override
 		protected Void doInBackground(Void... params) {
 			Bitmap bm = takeScreenshot();
 
 			try {
 				String date = new String(sdfD.format(calendar.getTime()));
 				String time = new String(sdfT.format(calendar.getTime()));
 
 				String fname = new String("Jandig_"+date+"_"+time+".jpg");
 				imgDir.mkdirs();
 				imgFile = new File(imgDir,fname);
 
 				// write into media folder
 				ContentValues v = new ContentValues();
 				long dateTaken = calendar.getTimeInMillis()/1000;
 				v.put(MediaStore.Images.Media.TITLE, fname);
 				v.put(MediaStore.Images.Media.PICASA_ID, fname);
 				v.put(MediaStore.Images.Media.DISPLAY_NAME, "Jandig");
 				v.put(MediaStore.Images.Media.DESCRIPTION, "Jandig");
 				v.put(MediaStore.Images.Media.DATE_ADDED, dateTaken);
 				v.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
 				v.put(MediaStore.Images.Media.DATE_MODIFIED, dateTaken);
 				v.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
 				v.put(MediaStore.Images.Media.ORIENTATION, 0);
 				v.put(MediaStore.Images.Media.DATA, imgFile.getAbsolutePath());
 
 				Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, v);
 				OutputStream outStream = getContentResolver().openOutputStream(uri);
 
 				bm.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
 				outStream.flush();
 				outStream.close();
 			} 
 			catch (FileNotFoundException e) {
 				errorMsg = e.getMessage();
 				e.printStackTrace();
 			} 
 			catch (IOException e) {
 				errorMsg = e.getMessage();
 				e.printStackTrace();
 			}	
 			return null;
 		}
 
 		protected void onPostExecute(Void result) {
 			if((errorMsg == null)&&(imgFile != null)){
 				//Toast.makeText(JandigActivity.this, "Imagem salva em: "+imgFile.getAbsolutePath(), Toast.LENGTH_SHORT ).show();
 				Toast.makeText(JandigActivity.this, "Imagem salva", Toast.LENGTH_SHORT ).show();
 			}
 			else {
 				Toast.makeText(JandigActivity.this, "Erro salvando imagem: "+errorMsg, Toast.LENGTH_SHORT ).show();
 			}
 		}
 	} // class TakeAsyncScreenshot
 
 }
 
 
