 // Copyright 2009 Shun'ichi Shinohara
 
 // This file is part of ShotQuote.
 //
 // ShotQuote is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // ShotQuote is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with ShotQuote.  If not, see <http://www.gnu.org/licenses/>.
 
 package com.tumblr.shino.shotquote;
 
 import java.io.File;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.net.Uri;
 import android.os.Bundle;
 import android.provider.MediaStore;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
import android.widget.ImageView;
 import android.widget.TextView;
 
 public class ShotQuoteAggregate extends Activity 
 	implements OnClickListener{
 	private static final String TEMPORARY_CAPTURE_FILE_NAME = "/sdcard/ShotQuote/capture_image.jpg";
 	private static final int CAMERA_ACTIVITY_CAPUTURE_IMAGE = 1;
 	private static final int CAMERA_ACTIVITY_CAPUTURE_IMAGE_VIA_FILE = 2;
 	private static final int CAMERA_ACTIVITY_STILL_IMAGE = 5;
 	private static final int QUOTE_IMAGE_ACTIVITY = 10;
 	private Button imageCaptureButton;
 	private TextView quoteTextView;
 
 	private Button imageCaptureViaFileButton;
	private ImageView imageView;
 	
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
 		imageCaptureButton = (Button) findViewById(R.id.Button01);
 		imageCaptureButton.setOnClickListener(this);
 		imageCaptureViaFileButton = (Button) findViewById(R.id.Button02);
 		imageCaptureViaFileButton.setOnClickListener(this);
 		quoteTextView = (TextView) findViewById(R.id.QuoteTextView);
		imageView = (ImageView) findViewById(R.id.ImageView01);
     }
 
 	public void onClick(View target) {
 		if(target == imageCaptureButton){
 			U.debugLog(this, "the capture button was clicked", imageCaptureButton);
 			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			startActivityForResult(intent, CAMERA_ACTIVITY_CAPUTURE_IMAGE);
 		} else if(target == imageCaptureViaFileButton) {
 			U.debugLog(this, "the capture button (via file) was clicked", imageCaptureButton);
 			Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
 			intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(TEMPORARY_CAPTURE_FILE_NAME)));
 			startActivityForResult(intent, CAMERA_ACTIVITY_CAPUTURE_IMAGE_VIA_FILE);
 		}
 	}
 	
 	protected void onActivityResult(int requestCode, int resultCode,
 			Intent result) {
 		super.onActivityResult(requestCode, resultCode, result);
 		U.debugLog(this, "Result code", resultCode);
 		if (resultCode == RESULT_CANCELED) {
 			U.infoLog(this, "Activity cancelled. requestCode", requestCode);
 			return;
 		}
 		Bundle bundle = result == null ? null : result.getExtras();
 		Intent quoteImageIntent = null;
 		Bitmap bm = null;
 		switch (requestCode) {
 		case CAMERA_ACTIVITY_CAPUTURE_IMAGE:
 			quoteImageIntent = new Intent(this, com.tumblr.shino.shotquote.QuoteImageController.class);
 			U.debugLog(this, "bundle returned from camera", bundle);
 	        bm = null;
 	        // bm = BitmapFactory.decodeFile("/sdcard/ShotQuote/capture_image.jpg"); 
 			bm = (Bitmap) bundle.get("data");
 			quoteImageIntent.putExtra("data", bm);
 			startActivityForResult(quoteImageIntent, QUOTE_IMAGE_ACTIVITY);
 //			this.imageView.setImageBitmap(bm);
 			break;
 		case CAMERA_ACTIVITY_CAPUTURE_IMAGE_VIA_FILE:
 			quoteImageIntent = new Intent(this, com.tumblr.shino.shotquote.QuoteImageController.class);
 			U.debugLog(this, "bundle returned from camera", bundle);
 	        bm = null;
 	        BitmapFactory.Options options = new BitmapFactory.Options();
 	        bm = BitmapFactory.decodeFile(TEMPORARY_CAPTURE_FILE_NAME, options); 
 			quoteImageIntent.putExtra("data", bm);
 			startActivityForResult(quoteImageIntent, QUOTE_IMAGE_ACTIVITY);
 //			this.imageView.setImageBitmap(bm);
 			break;
 		case CAMERA_ACTIVITY_STILL_IMAGE:
 			quoteImageIntent = new Intent(this, com.tumblr.shino.shotquote.QuoteImageController.class);
 			U.debugLog(this, "bundle returned from camera", bundle);
 			bm = (Bitmap) bundle.get("data");
 			quoteImageIntent.putExtra("data", bm);
 			U.debugLog(this, "result from STILL_IMAGE", result);
 			U.debugLog(this, "bitmap from STILL_IMAGE", bm);
 			startActivityForResult(quoteImageIntent, QUOTE_IMAGE_ACTIVITY);
 //			this.imageView.setImageBitmap(bm);
 			break;
 		case QUOTE_IMAGE_ACTIVITY:
 			U.debugLog(this, "return from QuoteImage intent", resultCode);
 			U.debugLog(this, "return bundle from QuoteImage intent", bundle);
 			CharSequence text = (CharSequence) bundle.get("text");
 			U.debugLog(this, "text data from QuoteImage intent", text);
 			U.debugLog(this, "text data from QuoteImage intent", bundle.getCharSequence("text"));
 			quoteTextView.setText(text);
 			break;
 		default:
 			throw new RuntimeException("Illegal requestCode: " + requestCode);
 		}
 	}
 }
