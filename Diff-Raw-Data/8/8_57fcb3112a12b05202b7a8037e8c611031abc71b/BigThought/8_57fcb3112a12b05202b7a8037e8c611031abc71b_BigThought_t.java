 package com.example.bigthought;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Random;
 
 import com.example.bigthought.R.drawable;
 
 import android.app.Activity;
 import android.content.ActivityNotFoundException;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Align;
 import android.graphics.PorterDuff;
 import android.graphics.PorterDuffXfermode;
 import android.graphics.Rect;
 import android.graphics.Typeface;
 import android.graphics.drawable.Drawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore;
 import android.text.format.DateFormat;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 //import com.androidworks.R;
 
 //import com.androidworks.R;
 
 public class BigThought extends Activity {
 
 	final int RESULT_LOAD_IMAGE = 1;
 	final int PIC_CROP = 2;
 	final int CAMERA_CAPTURE = 3;
 	final int PHOTO_PICKED = 4;
 	private Uri picUri;
 	private EditText inputEditText;
 
 	// private Bitmap uploadPic;
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		try {
 			File testFolder = new File(
 					Environment
 							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
 							+ "/BigThoughtPhoto");
 			testFolder.mkdirs();
 			// VERY IMPORTANT
 			testFolder.canRead();
 		} catch (Exception e) {
 			Toast.makeText(this, "Screw it", Toast.LENGTH_LONG).show();
 		}
 
 		Button openButton = (Button) findViewById(R.id.openButton);
 		openButton.setOnClickListener(openButtonOnClickListener);
 
 		Button shareButton = (Button) findViewById(R.id.shareButton);
 		shareButton.setOnClickListener(shareButtonOnClickListener);
 
 		Button cameraButton = (Button) findViewById(R.id.cameraButton);
 		cameraButton.setOnClickListener(cameraButtonOnClickListener);
 
 		inputEditText = (EditText) findViewById(R.id.inputEditText);
 		inputEditText.setOnClickListener(inputEditTextOnClickListener);
 		// inputEditText.setText("Insert your deep thought here");
 	}
 
 	public OnClickListener openButtonOnClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			Intent i = new Intent(
 					Intent.ACTION_PICK,
 					android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
 
 			startActivityForResult(i, RESULT_LOAD_IMAGE);
 		}
 	};
 
 	public OnClickListener inputEditTextOnClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			inputEditText.setText("");
 			inputEditText.setTextColor(0xFF000000);
 		}
 	};
 
 	public OnClickListener cameraButtonOnClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 
 			// use standard intent to capture an image
 			Intent captureIntent = new Intent(
 					android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
 			// we will handle the returned data in onActivityResult
 			startActivityForResult(captureIntent, CAMERA_CAPTURE);
 
 		}
 	};
 
 	public OnClickListener shareButtonOnClickListener = new OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			// TODO Auto-generated method stub
 			share();
 		}
 	};
 
 	private void share() {
 		Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
 
 		// Bitmap sharePic;
 		String path = Environment
 				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
 				+ "/BigThoughtPhoto/toShare.png";
 		File sharePic = new File(path);
 		Uri uri = Uri.fromFile(sharePic);
 
 		sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
 				"Subject here");
 		sharingIntent.putExtra(android.content.Intent.EXTRA_STREAM, uri);
 		sharingIntent.setType("image/*");
 		startActivity(Intent.createChooser(sharingIntent, "Share via"));
 
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
		if (requestCode == RESULT_LOAD_IMAGE && resultCode==RESULT_OK) {
 			// get the Uri for the captured image
 			picUri = data.getData();
 			// carry out the crop operation
 			crop(picUri);
 		}
 		// user is returning from cropping the image
		else if (requestCode == PIC_CROP && resultCode==RESULT_OK) {
 			// get the returned data
 			Uri picUri = Uri.fromFile(getTempFile());
 			String mText = inputEditText.getText().toString();
 			// Bundle extras = data.getExtras();
 			// get the cropped bitmap
 			Bitmap thePic = null;
 			try {
 				thePic = MediaStore.Images.Media.getBitmap(
 						this.getContentResolver(), picUri);
 			} catch (FileNotFoundException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 			Bitmap bmp = postProcessing(this, thePic, mText);
 
 			// Naming by date
 			Date d = new Date();
 			CharSequence s = DateFormat
 					.format("MM-dd-yy hh:mm:ss", d.getTime());
 			String fileName = "/" + s.toString() + ".png";
 			String sharePic = "/toShare.png";
 			try {
 				File testFolder = new File(
 						Environment
 								.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
 								+ "/BigThoughtPhoto");
 				testFolder.mkdirs();
 				// VERY IMPORTANT
 				testFolder.canRead();
 				try {
 					FileOutputStream out = new FileOutputStream(
 							testFolder.getAbsolutePath() + fileName);
 					bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
 					FileOutputStream toShare = new FileOutputStream(
 							testFolder.getAbsolutePath() + sharePic);
 					bmp.compress(Bitmap.CompressFormat.PNG, 90, toShare);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 
 			// retrieve a reference to the ImageView
 
 			ImageView picView = (ImageView) findViewById(R.id.imageView1);
 			// display the returned cropped image
 			picView.setImageBitmap(bmp);
 
 		}
 
		else if (requestCode == CAMERA_CAPTURE && resultCode==RESULT_OK) {
 			// get the Uri for the captured image
 			picUri = data.getData();
 			// carry out the crop operation
 			crop(picUri);
 		}
 
 	}
 
 	private void crop(Uri picUri) {
 		try {
 			Uri tempUri = Uri.fromFile(getTempFile());
 			Intent cropIntent = new Intent("com.android.camera.action.CROP");
 			// indicate image type and Uri
 			cropIntent.setDataAndType(picUri, "image/*");
 			// set crop properties
 			cropIntent.putExtra("crop", "true");
 			// indicate aspect of desired crop
 			cropIntent.putExtra("aspectX", 1);
 			cropIntent.putExtra("aspectY", 1);
 			// indicate output X and Y
 			cropIntent.putExtra("outputX", 500);
 			cropIntent.putExtra("outputY", 500);
 			// retrieve data on return
 			// cropIntent.putExtra("return-data", true);
 			cropIntent.putExtra(MediaStore.EXTRA_OUTPUT, tempUri);
 			cropIntent.putExtra("outputFormat",
 					Bitmap.CompressFormat.JPEG.toString());
 			// End test
 			// done cropping, return out the result
 			startActivityForResult(cropIntent, PIC_CROP);
 		} catch (ActivityNotFoundException anfe) {
 			// display an error message
 			String errorMessage = "Your device sucks";
 			Toast toast = Toast
 					.makeText(this, errorMessage, Toast.LENGTH_SHORT);
 			toast.show();
 		}
 
 	}
 
 	private File getTempFile() {
 		if (isSDCARDMounted()) {
 			File f = new File(
 					Environment
 							.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
 							+ "/BigThoughtPhoto/temp.tmp");
 			try {
 				f.createNewFile();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				Toast.makeText(this, "IOerror", Toast.LENGTH_LONG).show();
 			}
 			return f;
 		} else {
 			return null;
 		}
 	}
 
 	private boolean isSDCARDMounted() {
 		String status = Environment.getExternalStorageState();
 
 		if (status.equals(Environment.MEDIA_MOUNTED))
 			return true;
 		return false;
 	}
 
 	public Bitmap postProcessing(Context mContext, Bitmap bitmap, String mText) {
 		int canvasSize = 530;
 		int margin = 15;
 		int fontSize = 30;
 		Typeface fontFormat = Typeface.create("Helvetica", Typeface.BOLD);
 		try {
 			Resources resources = mContext.getResources();
 			float scale = resources.getDisplayMetrics().density;
 
 			android.graphics.Bitmap.Config bitmapConfig = bitmap.getConfig();
 			// set default bitmap config if none
 			if (bitmapConfig == null) {
 				bitmapConfig = android.graphics.Bitmap.Config.ARGB_8888;
 			}
 			// resource bitmaps are imutable,
 			// so we need to convert it to mutable one
 
 			bitmap = bitmap.copy(bitmapConfig, true);
 			// bitmap = BitmapFactory.decodeResource(this.getResources(),
 			// R.drawable.noise);
 			bitmap = colorBlend(bitmap);
 			//bitmap=addNoise(bitmap);
 			bitmap=addVignete(bitmap);
 			// Test frame
 			Bitmap frame = Bitmap.createBitmap(canvasSize, canvasSize,
 					Bitmap.Config.ARGB_8888);
 			Canvas canvas = new Canvas(frame);
 			canvas.drawColor(getResources().getColor(R.color.light));
 			canvas.drawBitmap(bitmap, margin, margin, null);
 			// done testing frame
 
 			// Canvas canvas = new Canvas(bitmap);
 			// new antialised Paint
 			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
 			// text color - #3D3D3D
 			paint.setColor(getResources().getColor(R.color.light));
 			// text size in pixels
 			
 			paint.setAlpha(255);
 			paint.setTypeface(fontFormat);
 			paint.setTextAlign(Align.CENTER);
 			// text shadow
 			paint.setShadowLayer(1f, 0f, 1f, Color.DKGRAY);
 
 			// draw text to the Canvas center
 			Rect bounds = new Rect();
 			paint.getTextBounds(mText, 0, mText.length(), bounds);
 			Log.d("value", String.valueOf(bounds.width())+"h: "+String.valueOf(bitmap.getHeight())+"w: "+String.valueOf(bitmap.getWidth()));
 			
 			double width_factor=180.0/bounds.width();
 			double height_factor=50.0/bounds.height();
 			double factor=Math.min(width_factor, height_factor);
 			if(factor>1){
 				factor=1;
 			}
 			Log.d("code", String.valueOf(factor)+":"+ String.valueOf(paint.getTextSize()));
 			paint.setTextSize((int) (fontSize *factor));
 			int x = (bitmap.getWidth() - bounds.width()) / 6;
 			int y = (bitmap.getHeight() + bounds.height()) / 5;
 
 			canvas.drawText(mText, 265, 175, paint);
 
 			return frame;
 		} catch (Exception e) {
 			// TODO: handle exception
 
 			return null;
 		}
 
 	}
 
 	public Bitmap vintage(Bitmap source) {
 		// get image size
 		int COLOR_MIN = 0x00;
 		int COLOR_MAX = 0xFF;
 
 		int width = source.getWidth();
 		int height = source.getHeight();
 		int[] pixels = new int[width * height];
 		// get pixel array from source
 		source.getPixels(pixels, 0, width, 0, 0, width, height);
 		// a random object
 		Random random = new Random();
 
 		int index = 0;
 		// iteration through pixels
 		for (int y = 0; y < height; ++y) {
 			for (int x = 0; x < width; ++x) {
 				// get current index in 2D-matrix
 				index = y * width + x;
 				// get random color
 				int randColor = Color.rgb(random.nextInt(COLOR_MAX),
 						random.nextInt(COLOR_MAX), random.nextInt(COLOR_MAX));
 				// OR
 				pixels[index] |= randColor;
 			}
 		}
 		// output bitmap
 		Bitmap bmOut = Bitmap.createBitmap(width, height, source.getConfig());
 		bmOut.setPixels(pixels, 0, width, 0, 0, width, height);
 		return bmOut;
 	}
 
 	public Bitmap addNoise(Bitmap source) {
 		// MUST create folder drawable
 		Bitmap original = source;
         Bitmap mask = BitmapFactory.decodeResource(getResources(),drawable.noise);
         Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),Bitmap.Config.ARGB_8888);
         Canvas mCanvas = new Canvas(result);
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         //Useable mode: Overlay, multiply
         paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
         mCanvas.drawBitmap(original, 0, 0, null);
         mCanvas.drawBitmap(mask, 0, 0, paint);
         paint.setXfermode(null);
         return result;
 	}
 
 	public Bitmap addVignete(Bitmap source) {
 		Bitmap original = source;
         Bitmap mask = BitmapFactory.decodeResource(getResources(),drawable.mask);
         Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(),Bitmap.Config.ARGB_8888);
         Canvas mCanvas = new Canvas(result);
         Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
         //Useable mode: Overlay, multiply
         paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
         mCanvas.drawBitmap(original, 0, 0, null);
         mCanvas.drawBitmap(mask, 0, 0, paint);
         paint.setXfermode(null);
         return result;
 	}
 
 	public Bitmap colorBlend(Bitmap source) {
 		int[] rValue = { 52, 53, 54, 59,
 				73, 101, 134, 164,
 				186, 205, 219, 231,
 				240, 245, 249, 250, 255 };
 		int[] gValue = { 45, 51, 64, 79,
 				97, 117, 137, 155,
 				170, 182, 194, 202,
 				210, 215, 218, 250, 255 };
 		int[] bValue = { 96, 102, 108, 115,
 				124, 133, 141, 146,
 				156, 159, 166,	170,
 				172, 175, 176, 177, 255 };
 
 		for (int i = 0; i < 500; i++) {
 			for (int j = 0; j < 500; j++) {
 				int p = source.getPixel(i, j);
 				//Log.d("pointvalue", String.valueOf(p));
 				int R = (p >> 16) & 0xff;
 				int G = (p >> 8) & 0xff;
 				int B = p & 0xff;
 //				if (true) {
 //					Log.d("value", String.valueOf(R) + String.valueOf(G)
 //							+ String.valueOf(B));
 //				}
 				// red channel
 				int r = (rValue[R / 16 + 1] - rValue[R / 16]) * (R % 16) / 16
 						+ rValue[R / 16];
 				int g = (gValue[G / 16 + 1] - gValue[G / 16]) * (G % 16) / 16
 						+ gValue[G / 16];
 				int b = (bValue[B / 16 + 1] - bValue[B / 16]) * (B % 16) / 16
 						+ bValue[B / 16];
 //				if (true) {
 //					Log.d("code", String.valueOf(r) + String.valueOf(g)
 //							+ String.valueOf(b));
 //				}
 				int color = Color.argb(255, r, g, b);
 				source.setPixel(i, j, color);
 			}
 		}
 		return source;
 	}
 
 	public int transformRed(int Rxy, int x, int y) {
 
 		return Rxy;
 	}
 
 	public static Bitmap applyGaussianBlur(Bitmap src) {
 		double[][] GaussianBlurConfig = new double[][] { { 1, 2, 1 },
 				{ 2, 4, 2 }, { 1, 2, 1 } };
 		ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
 		convMatrix.applyConfig(GaussianBlurConfig);
 		convMatrix.Factor = 16;
 		convMatrix.Offset = 0;
 		return ConvolutionMatrix.computeConvolution3x3(src, convMatrix);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.big_thought, menu);
 		return true;
 	}
 
 	//
 }
