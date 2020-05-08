 package com.ruzulive.androidapp;
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.app.WallpaperManager;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.view.View;
 import android.view.View.OnClickListener;
 
 public class Camera extends Activity implements OnClickListener {
 
 	ImageButton ib;
 	Button b;
 	ImageView iv;
 	Intent i;
 	final static int cameraData = 0;
 	Bitmap bmp;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.photo);
 		setUpReferences();
 	}
 	
 	public void setUpReferences(){
 		ib = (ImageButton) findViewById (R.id.ibTakePic);
 		b = (Button) findViewById (R.id.bSetWallpaper);
 		iv = (ImageView) findViewById (R.id.ivReturnPic);
 		ib.setOnClickListener(this);
 		b.setOnClickListener(this);
 	}
 	
 	@Override
 	public void onClick(View v) {
 		// TODO Auto-generated method stub
 		switch(v.getId()){
 		case R.id.ibTakePic:
 			//access camera and take an image
 			i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 			startActivityForResult(i, cameraData); //start intent with image taken
 			break;
 		case R.id.bSetWallpaper:
 			try {
 				WallpaperManager.getInstance(this).setBitmap(bmp);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			break;
 		}
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		// TODO Auto-generated method stub
 		super.onActivityResult(requestCode, resultCode, data);
 		//check if operation succeeded
 		if(resultCode == RESULT_OK){
 			Bundle extras = data.getExtras(); //getting the extras that were passed into the intent
 			bmp = (Bitmap) extras.get("data"); //setup bitmap based on the extras using a key reference
 			iv.setImageBitmap(bmp); //set bg pic to imageview
 		}
 	}
 	
 	
 }
