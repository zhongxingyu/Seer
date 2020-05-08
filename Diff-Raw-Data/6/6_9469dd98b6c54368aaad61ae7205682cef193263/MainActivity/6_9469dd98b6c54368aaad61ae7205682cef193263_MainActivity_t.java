 package com.example.tuts_plus_ascii_art_editor_android;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.graphics.Color;
 import android.os.Bundle;
 import android.os.Environment;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Toast;
 
 //export image file libraries
 import java.io.File;
 import java.io.FileOutputStream;
 import java.util.Date;
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 
 public class MainActivity extends Activity implements OnClickListener {
 	
 	private EditText textArea;
 	private final int COLOR_REQUEST=1;
 	private SharedPreferences asciiPrefs;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		
 		textArea = (EditText)findViewById(R.id.ascii_text);
 		Button setBtn = (Button)findViewById(R.id.set_colors_btn);
 		setBtn.setOnClickListener(this);
 		Button setImgBtn = (Button)findViewById(R.id.export_btn);
 		setImgBtn.setOnClickListener(this);
 		
 		asciiPrefs = getSharedPreferences("AsciiPicPreferences", 0);
 		String chosenColors = asciiPrefs.getString("colors", "");
 		
 		if (chosenColors.length() > 0) {
 			String[] prefColors = chosenColors.split(" ");
 			updateColors(prefColors[0], prefColors[1]);
 		}
 	}
 
 	@Override
 	public void onClick(View v) {
 		if (v.getId() == R.id.set_colors_btn) {
 			Intent colorIntent = new Intent(this, ColorChooser.class);
 			/*
 			 * We start an Intent for the Activity we created to handle 
 			 * user color scheme choice. In that Activity, we are going 
 			 * to let the user choose an option and return the result 
 			 * to the main Activity, which is why we use 
 			 * startActivityForResult.
 			 */
 			this.startActivityForResult(colorIntent, COLOR_REQUEST);
 		} else if (v.getId() == R.id.export_btn) {
 			saveImg();
 		}
 	}
 	
 	private void saveImg() {
 		// check if smartphone even have an external storage
 		String state = Environment.getExternalStorageState();
 		
 		if (Environment.MEDIA_MOUNTED.equals(state)) {
 			File picDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
			// Make sure the Pictures directory exists.
	        picDir.mkdirs();
 			/*
 			 The image is going to contain the visible content of the editable text-field, including the 
 			 background color  i.e. it will display what you can see looking at the app screen when you 
 			 export it. To achieve this we use the cache for the View:
 			 This sets up the editable text-field View to be drawable, 
 			 then saves its current appearance as a Bitmap using the drawing cache.
 			 */
 			textArea.setDrawingCacheEnabled(true);
 			textArea.buildDrawingCache(true);
 			Bitmap bitmap = textArea.getDrawingCache();
 			/*
 			 Now we need to give our file a name, so lets use the current date 
 			 and time to make each one unique, appending some informative text 
 			 and the image file extension:
 			 */
 			Date theDate = new Date();
 			String fileName = "asciipic"+theDate.getTime()+".png";
 			//Here we specify the filename, now lets create the file using it together 
 			//with the path to the Pictures directory:
 			File picFile = new File(picDir+"/"+fileName);
				Toast.makeText(getApplicationContext(), "WTF does this work? picFile: "+picFile, Toast.LENGTH_SHORT).show();
 			/* Try/catch allows the program to keep running if something goes wrong 
 			 *  with the input/ output operations. 
 			 */
 			try {
 				// create the file and pass it to an output stream:
 				picFile.createNewFile();
 				FileOutputStream picOut = new FileOutputStream(picFile);
 				// compress the bitmap and write it to the output stream
 				// saving the result as a boolean:
 				boolean worked = bitmap.compress(CompressFormat.PNG, 100, picOut);
 				//Output a message to the user depending on the result of the write operation:
 				if (worked) {
 				    Toast.makeText(getApplicationContext(), "Image saved to your device Pictures " +
 				        "directory!", Toast.LENGTH_SHORT).show();
 				} else {
 				    Toast.makeText(getApplicationContext(), "Whoops! File not saved.",
 				        Toast.LENGTH_SHORT).show();
 				}
 				// close file:
 				picOut.close();
 			}  catch (Exception e) { e.printStackTrace(); }
 			//We can free the resources being used for the drawing cache, after the catch block:
 			textArea.destroyDrawingCache();
 		} else {
 			Toast.makeText(this.getApplicationContext(), "Sorry - you don't have an external" +
 				    " storage directory available!", Toast.LENGTH_SHORT).show();
 		}
 	}
 	
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == COLOR_REQUEST) {
 			if (resultCode == RESULT_OK) {
 				String chosenTextColor = data.getStringExtra("textColor");
 				String chosenBackColor = data.getStringExtra("backColor");
 				updateColors(chosenTextColor, chosenBackColor);
 				SharedPreferences.Editor prefsEd = asciiPrefs.edit();
 				prefsEd.putString("colors", ""+chosenTextColor+" "+chosenBackColor);
 				prefsEd.commit();
 			}
 		}
 	}
 	
 	public void updateColors(String tColor, String bColor) {
 		textArea.setTextColor(Color.parseColor(tColor));
 		textArea.setBackgroundColor(Color.parseColor(bColor));
 	}
 	
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
