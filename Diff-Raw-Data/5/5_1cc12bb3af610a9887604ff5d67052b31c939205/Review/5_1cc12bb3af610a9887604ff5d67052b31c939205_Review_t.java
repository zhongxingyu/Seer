 package to.rcpt.chronicle;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapFactory.Options;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.View;
 import android.widget.ImageView;
 
 public class Review extends Activity {
     private static final String TAG = "Review";
 	private byte[] imageData;
 
 	// sigh. Do you suppose Scala would work in Dalvik?
     interface BitmapFactoryQuery {
     	Bitmap factoryDecode(BitmapFactory.Options options);
     };
     
 	@Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.review);
         Intent i = getIntent();
         imageData = i.getByteArrayExtra(Chronicle.IMAGE_DATA);
         final String referenceFile = i.getStringExtra(Chronicle.REFERENCE_IMAGE);
         ImageView reference = (ImageView)findViewById(R.id.reference_image);
         ImageView prospect = (ImageView)findViewById(R.id.prospective_image);
         BitmapFactoryQuery referenceBitmap = new BitmapFactoryQuery() {
 			@Override
 			public Bitmap factoryDecode(Options options) {
 				return BitmapFactory.decodeFile(referenceFile, options);
 			}
 		};
         BitmapFactoryQuery prospectBitmap = new BitmapFactoryQuery() {
 			@Override
 			public Bitmap factoryDecode(Options options) {
				return BitmapFactory.decodeByteArray(imageData, 0, imageData.length, options);
 			}
 		};
         
 		projectBitmap(reference, referenceBitmap);
		projectBitmap(prospect, prospectBitmap);
     }
 
 	private void projectBitmap(ImageView view, BitmapFactoryQuery bitmapQuery) {
         boolean portraitOrientation = true;
 		Display display = getWindowManager().getDefaultDisplay(); 
         int width = display.getWidth();
         int height = display.getHeight();
         if(portraitOrientation)
         	height /= 3;
         else
         	width /= 3;
 		BitmapFactory.Options options = new BitmapFactory.Options();
 		options.inJustDecodeBounds = true;
 		bitmapQuery.factoryDecode(options);
 		int hScale = options.outHeight / height;
 		int vScale = options.outWidth / width;
 		float aspectRatio = (float)options.outWidth / options.outHeight;
 		
 		options.inSampleSize = hScale > vScale ? hScale : vScale;
 		if (options.inSampleSize < 1)
 			options.inSampleSize = 1;
 		
 		options.inJustDecodeBounds = false;
 		int scaledWidth = (int)(aspectRatio * height);
 		Log.i(TAG, "Projecting " + options.outWidth + "x" + options.outHeight + " into " +
 				width + "x" + height + " by downsampling " + options.inSampleSize + "x and scaling to " +
 				scaledWidth + "x" + height);
 		view.setImageBitmap(Bitmap.createScaledBitmap(
 				bitmapQuery.factoryDecode(options),
 				scaledWidth, height, true));
 	}
 	
 	public void acceptImage(View v) {
 		
 	}
 }
