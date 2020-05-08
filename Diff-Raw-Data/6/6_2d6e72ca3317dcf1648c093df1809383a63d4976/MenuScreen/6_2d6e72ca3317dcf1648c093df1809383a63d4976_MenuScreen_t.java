 package edu.chl.asciicam.activity;
 
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.app.Activity;
 import android.content.Intent;
 import android.database.Cursor;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 
 //This file is part of Asciicam.
 //
 //Asciicam is free software: you can redistribute it and/or modify
 //it under the terms of the GNU General Public License as published by
 //the Free Software Foundation, either version 3 of the License, or
 //(at your option) any later version.
 //
 //Asciicam is distributed in the hope that it will be useful,
 //but WITHOUT ANY WARRANTY; without even the implied warranty of
 //MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 //GNU General Public License for more details.
 //
 //You should have received a copy of the GNU General Public License
 //along with Asciicam.  If not, see <http://www.gnu.org/licenses/>.
 
 /**
  * This activity is the first one you see when you start the app.
  * Its a menu with the buttons, take picture, load picture and options
  * @author 
  *
  */
 public class MenuScreen extends Activity {
 	final int REQ_CODE_PICK_IMAGE= 1;
 
 	Button take_Pic_B, load_Pic_B, optionsB;
 	ImageView chosenPic;
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_menu_screen);
 		take_Pic_B = (Button) findViewById(R.id.Take_pic_B);
 		load_Pic_B = (Button) findViewById(R.id.Load_pic_B);
 		optionsB = (Button) findViewById(R.id.Options_B);
 
 		/* Setting actionListeners to the buttons */
 		take_Pic_B.setOnClickListener(new View.OnClickListener() {
 
 			/*Using the Intent class to open the phones camera */
 			public void onClick(View v) {
 				Intent i = new Intent(MenuScreen.this, CameraScreen.class);
 				startActivity(i);
 
 				//				i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 				//				startActivity ForResult(i, 0);;
 			}
 		});
 		load_Pic_B.setOnClickListener(new View.OnClickListener() {
 
 			public void onClick(View view) {
 				Intent load = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 				startActivityForResult(load, 0);
 			}
 		});
 
 	}
 
 	protected void onActivityResult(int request, int result, Intent data){
 		super.onActivityResult(request, result, data);
 
		String filePath = null;
 		switch(request){
 		case REQ_CODE_PICK_IMAGE:
 
 			if (result == RESULT_OK){
 				Uri selectedImage = data.getData();
 				String[] filePathColumn = {MediaStore.Images.Media.DATA};
 
 				Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
 				cursor.moveToFirst();
 
 				int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
				filePath = cursor.getString(columnIndex);
 				cursor.close();
 
 				Bitmap chosenPic = BitmapFactory.decodeFile(filePath);
 			}
 		}
 		//If no picture is chosen, PreviewScreen will not start (returning to MenuScreen)
 		if (result != 0){
 			Intent i = new Intent(MenuScreen.this, PreviewScreen.class);
			i.putExtra("filePath",filePath);
 			startActivity(i);
 		}
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.activity_menu_screen, menu);
 		return true;
 	}
 }
