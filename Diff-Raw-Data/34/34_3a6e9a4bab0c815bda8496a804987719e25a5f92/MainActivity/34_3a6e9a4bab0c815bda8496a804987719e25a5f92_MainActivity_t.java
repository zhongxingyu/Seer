 package com.example.imgur;
 
 import android.app.Activity;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.View;
 import android.widget.ImageView;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	private int _imageCount = 0;
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.activity_main, menu);
         return true;
     }
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_main);
 
        showNextImage();
     }
 
    private void showNextImage(){
     	new GalleryFeedTask(this, _imageCount).execute("http://imgur.com/gallery.json");
     	_imageCount++;
     }
 
     public void showErrorMessage(String errorMessage) { 
  		showTitle("Error: " + errorMessage);
     }
     
     public void showTitle(String title) {
     	TextView textView = (TextView)findViewById(R.id.titleText);
  		textView.setText(title);
     }
 
     public void showImage(Drawable image) {
     	ImageView galleryImage = (ImageView) findViewById(R.id.galleryImage);
 		galleryImage.setImageDrawable(image);
     }
 
     public void nextImage(View view) {
    	showNextImage();
     }
 }
