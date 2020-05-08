 package uct.snapvote;
 
 import android.content.ContentResolver;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Color;
 import android.net.Uri;
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.support.v4.app.NavUtils;
 import android.view.View;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.ImageView;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 public class PreprocessActivity extends Activity {
 
     String uristr;
     Button btnProcess;
 
     CheckBox btnRed;
     CheckBox btnGreen;
     CheckBox btnBlue;
     CheckBox btnBlack;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_preprocess);
         // Show the Up button in the action bar.
         setupActionBar();
 
         //extract image url from bundle
         uristr = getIntent().getStringExtra("ImageUri");
 
         // add click event
         // bind onclick event
         btnProcess = (Button) findViewById(R.id.btnProcess);
         btnProcess.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 createAndLaunchProcess();
             }
         });
 
         btnRed = (CheckBox) findViewById(R.id.chkRedChoice);
         btnGreen = (CheckBox) findViewById(R.id.chkGreenChoice);
         btnBlue = (CheckBox) findViewById(R.id.chkBlueChoice);
         btnBlack = (CheckBox) findViewById(R.id.chkBlackChoice);
 
         //load up thumbnail - TODO: move to an asynx task to remove awkward lag
         loadThumbnail(uristr);
     }
 
     private void createAndLaunchProcess() {
         Intent intent = new Intent(this, ProcessActivity.class);
 
         intent.putExtra("ImageUri", uristr);
 
         List<Integer> selectedColours = new ArrayList<Integer>();
         if (btnRed.isChecked()) selectedColours.add(Color.RED);
         if (btnBlack.isChecked()) selectedColours.add(Color.BLACK);
         if (btnGreen.isChecked()) selectedColours.add(Color.GREEN);
         if (btnBlue.isChecked()) selectedColours.add(Color.BLUE);
 
         int[] asInts = new int[selectedColours.size()];
         for(int i=0;i<asInts.length;i++) asInts[i] = selectedColours.get(i);
 
         Log.d("uct.snapvote", "selected colours: " + asInts.length);
 
         intent.putExtra("ColourArray", asInts);
 
         startActivity(intent);
     }
 
     /**
      * Set up the {@link android.app.ActionBar}.
      */
     private void setupActionBar() {
         getActionBar().setDisplayHomeAsUpEnabled(true);
     }
 
     // TODO: threaded
     // TODO: check the width and height things, not sure if appropriate, seems to work well though
     private void loadThumbnail(String uri) {
 
         Uri contentURI = Uri.parse(uri);
         ContentResolver cr = getContentResolver();
 
         InputStream in = null;
         try {
             in = cr.openInputStream(contentURI);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
 
         // read image attributes
         BitmapFactory.Options iOptions = new BitmapFactory.Options();
         iOptions.inJustDecodeBounds = true;
         BitmapFactory.decodeStream(in,null,iOptions);
         int imageHeight = iOptions.outHeight;
         int imageWidth = iOptions.outWidth;
 
         try {
             in = cr.openInputStream(contentURI);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
 
         iOptions.inPreferredConfig = Bitmap.Config.RGB_565;
         int inSampleSize = 1;
 
         if (imageHeight > 200) {
             inSampleSize = Math.round((float)imageHeight / (float)200);
         }
 
         int expectedWidth = imageWidth / inSampleSize;
 
         if (expectedWidth > 400) {
             //if(Math.round((float)width / (float)reqWidth) > inSampleSize) // If bigger SampSize..
             inSampleSize = Math.round((float)imageWidth / (float)400);
         }
 
 
         iOptions.inSampleSize = inSampleSize;
 
         // Decode bitmap with inSampleSize set
         iOptions.inJustDecodeBounds = false;
 
         ImageView mImage = (ImageView) findViewById(R.id.imageView);
         mImage.setImageBitmap(BitmapFactory.decodeStream(in, null, iOptions));
 
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.preprocess, menu);
         return true;
     }
     
 
     @Override
     public boolean onOptionsItemSelected(MenuItem item) {
         switch (item.getItemId()) {
             case android.R.id.home:
                 // This ID represents the Home or Up button. In the case of this
                 // activity, the Up button is shown. Use NavUtils to allow users
                 // to navigate up one level in the application structure. For
                 // more details, see the Navigation pattern on Android Design:
                 //
                 // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                 //
                 NavUtils.navigateUpFromSameTask(this);
                 return true;
         }
         return super.onOptionsItemSelected(item);
     }
 
 }
