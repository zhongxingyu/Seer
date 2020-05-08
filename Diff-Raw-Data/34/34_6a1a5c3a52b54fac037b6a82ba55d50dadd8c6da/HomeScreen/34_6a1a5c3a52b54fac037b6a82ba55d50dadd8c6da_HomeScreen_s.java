 package com.bn.bobblehead;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 
 import com.bn.bobblehead.FaceSelectActivity;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 
 public class HomeScreen extends Activity {
 
 	public static final File backFil=new File(Environment.getExternalStorageDirectory(), "bobblebackg.png");
 	public static final File faceFil=new File(Environment.getExternalStorageDirectory(), "bobbleface.png");
     private static final int CAMERA_REQUEST = 1888; 
     @Override
     public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_home_screen);
         Button photoButton = (Button) this.findViewById(R.id.button1);
         photoButton.setOnClickListener(new View.OnClickListener() {
         	
         	
         	//launch camera app to get img
             public void onClick(View v) {
                // Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE); 
                 //startActivityForResult(cameraIntent, CAMERA_REQUEST); 
             	Bitmap photo = BitmapFactory.decodeResource(getResources(), R.drawable.starwars);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 photo.compress(Bitmap.CompressFormat.PNG, 100, baos); 
                 byte[] b = baos.toByteArray();
                 
                 try {
                     if(!backFil.exists()){backFil.createNewFile();}
                 	FileOutputStream out = new FileOutputStream(backFil);
                     
                     photo.compress(Bitmap.CompressFormat.PNG, 90, out);
                     
                     out.flush();
                     out.close();
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
                                 
                 Intent intent = new Intent(HomeScreen.this, FaceSelectActivity.class);
                 startActivity(intent);
                 
             }
         });
     }
     
     //this receives the camera's photo
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {  
         if (requestCode == CAMERA_REQUEST && resultCode == RESULT_OK) {  
            Bitmap backg = (Bitmap) data.getExtras().get("data"); 
             //imageView.setImageBitmap(photo);
             //ByteArrayOutputStream baos = new ByteArrayOutputStream();
             //photo.compress(Bitmap.CompressFormat.PNG, 100, baos); 
            // byte[] b = baos.toByteArray();
 
             Intent intent = new Intent(this, BobActivity.class);
             //intent.putExtra("img", b);
             startActivity(intent);
             
             
         }  
     } 
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
     	getMenuInflater().inflate(R.menu.activity_home_screen, menu);
         return true;
     }
 }
