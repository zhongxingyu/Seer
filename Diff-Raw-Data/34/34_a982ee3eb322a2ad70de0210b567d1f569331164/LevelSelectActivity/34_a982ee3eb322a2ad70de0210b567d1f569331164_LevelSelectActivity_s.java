 package com.amazon.dinorama;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.media.MediaPlayer;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 import android.widget.ImageView;
 
 public class LevelSelectActivity extends Activity {
 	
 	MediaPlayer song;
 	
 	private static final int RESULT_LOAD_IMAGE = 100;
 	
 	String mode;
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.level_select_screen);
         Intent intent = getIntent();
         mode = intent.getStringExtra("game_mode");
         song = MediaPlayer.create(LevelSelectActivity.this, R.raw.level_select);
         song.setLooping(true);
         song.start();
 	}
 	
 	public void pickedLevelOne(View view){
 		LevelBitmapWrapper.bitmap = scaleDownBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.background_1), 480); //change this to R.drawable.LEVEL_NAME
 		Intent myIntent = new Intent(LevelSelectActivity.this, GameActivity.class);
 		myIntent.putExtra("game_mode", mode);
 		song.stop();
     	LevelSelectActivity.this.startActivity(myIntent);
 	}
 	public void pickedLevelTwo(View view){
 		LevelBitmapWrapper.bitmap = scaleDownBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.background_2), 480); //change this to R.drawable.LEVEL_NAME
 		Intent myIntent = new Intent(LevelSelectActivity.this, GameActivity.class);
 		myIntent.putExtra("game_mode", mode);
 		song.stop();
     	LevelSelectActivity.this.startActivity(myIntent);
 	}
 	public void pickedLevelThree(View view){
 		LevelBitmapWrapper.bitmap = scaleDownBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.background_3), 480); //change this to R.drawable.LEVEL_NAME
 		Intent myIntent = new Intent(LevelSelectActivity.this, GameActivity.class);
 		myIntent.putExtra("game_mode", mode);
 		song.stop();
     	LevelSelectActivity.this.startActivity(myIntent);
 	}
 	public void pickedLevelFour(View view){
 		LevelBitmapWrapper.bitmap = scaleDownBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.background_4), 480); //change this to R.drawable.LEVEL_NAME
 		Intent myIntent = new Intent(LevelSelectActivity.this, GameActivity.class);
 		myIntent.putExtra("game_mode", mode);
 		song.stop();
     	LevelSelectActivity.this.startActivity(myIntent);
 	}
 	public void pickedLevelFive(View view){
 		LevelBitmapWrapper.bitmap = scaleDownBitmap(BitmapFactory.decodeResource(this.getResources(), R.drawable.background_5), 480); //change this to R.drawable.LEVEL_NAME
 		Intent myIntent = new Intent(LevelSelectActivity.this, GameActivity.class);
 		myIntent.putExtra("game_mode", mode);
 		song.stop();
     	LevelSelectActivity.this.startActivity(myIntent);
 	}
 
 	
 	public void getUserPhoto(View view){
 		Intent i = new Intent( Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
         startActivityForResult(i, RESULT_LOAD_IMAGE);  
 	}
 	
 	@Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);
          
         if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
             Uri selectedImage = data.getData();
             String[] filePathColumn = { MediaStore.Images.Media.DATA };
  
             Cursor cursor = getContentResolver().query(selectedImage,
                     filePathColumn, null, null, null);
             cursor.moveToFirst();
  
             int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
             String picturePath = cursor.getString(columnIndex);
             cursor.close();
              
             ImageView imageView = (ImageView) findViewById(R.id.imgView);
             imageView.setImageBitmap(BitmapFactory.decodeFile(picturePath));
             
             LevelBitmapWrapper.bitmap = scaleDownBitmap(BitmapFactory.decodeFile(picturePath), 480);
             
             Intent myIntent = new Intent(LevelSelectActivity.this, GameActivity.class);
     		myIntent.putExtra("game_mode", mode);
     		song.stop();
         	LevelSelectActivity.this.startActivity(myIntent);
             
         }
      
      
     }
 	
 	public Bitmap scaleDownBitmap(Bitmap photo, int newHeight) {
 
 		float densityMultiplier = this.getResources().getDisplayMetrics().density;        
 
 		int h= (int) (newHeight*densityMultiplier);
 		int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));
 
 		photo=Bitmap.createScaledBitmap(photo, w, h, true);
 
 		return photo;
 	}
 	
 }
