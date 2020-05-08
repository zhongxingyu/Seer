 package com.shootthesquare.ermahgerdtranslator;
 
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Typeface;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.graphics.drawable.LayerDrawable;
 import android.net.Uri;
 import android.os.Bundle;
 import android.os.Environment;
 import android.provider.MediaStore.Images;
 import android.text.ClipboardManager;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.inputmethod.InputMethodManager;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.Toast;
 
 public class Ermahgerd extends Activity {
 	private View imageWrapperLayout;
 	private ImageView workingImage;
 	private EditText inputText;
 	private EditText outputText;
 
 	private Button newButton;
 	private Button shareButton;
 	private Button translateButton; 
 	private Button copyButton;
 
 	private Button topButton;
 	private Button bottomButton;
 	private Button splitButton;
 	private Button talkButton;
 
 	private Dialect ermahgerd = new ErmahgerdDialect();
 
 	private Bitmap yourSelectedImage;
 	private Bitmap combined;
 	
 	private LayerDrawable layerDrawable;
 	private Bitmap bgBitmap;
 	private Bitmap fgBitmap;
 
 	private static final int SELECT_PHOTO = 100;
 
 	private Resources r;
 	
 	
 	
 	@Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_ermahgerd);
 
         imageWrapperLayout = (View) findViewById(R.id.ImageWrapperLayout);
         workingImage = (ImageView)findViewById(R.id.workingImageView);
         inputText = (EditText)findViewById(R.id.inputText);
         outputText = (EditText)findViewById(R.id.outputText);
 
         newButton = (Button)findViewById(R.id.NewButton);
         shareButton = (Button)findViewById(R.id.shareButton);
         translateButton = (Button)findViewById(R.id.translateButton);
         copyButton = (Button)findViewById(R.id.copyButton);
 
         topButton = (Button)findViewById(R.id.TopButton);
         bottomButton = (Button)findViewById(R.id.bottomButton);
         splitButton = (Button)findViewById(R.id.splitButton);
         talkButton = (Button)findViewById(R.id.TalkButton);
         
         layerDrawable = (LayerDrawable) getResources().getDrawable(R.drawable.layers);
         		
         imageWrapperLayout.setOnClickListener(WorkingPictureListener);
 
         newButton.setOnClickListener(newButtonListener);
         shareButton.setOnClickListener(shareButtonListener);
         translateButton.setOnClickListener(TranslateButtonListener);
         copyButton.setOnClickListener(CopyButtonListener);
         
         topButton.setOnClickListener(topButtonListener);
         bottomButton.setOnClickListener(bottomButtonListener);
         splitButton.setOnClickListener(splitButtonListener);
         talkButton.setOnClickListener(talkButtonListener);
 	  
         r = getResources();
         bgBitmap = BitmapFactory.decodeResource(r, R.drawable.ermahgerd_original, null);
         
         
         Intent intent = getIntent();
         String action = intent.getAction();
         String type = intent.getType();
 
         if (Intent.ACTION_SEND.equals(action) && type != null) {
            if (type.startsWith("image/")) {
                 handleSendImage(intent, true); // Handle single image being sent
             }
         }
         
 	}
 
 	 private OnClickListener WorkingPictureListener = new OnClickListener() {
 		   public void onClick(View v) {
 			 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		        imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		   }
 	   };
 	
     private OnClickListener TranslateButtonListener = new OnClickListener() {
 		   public void onClick(View v) {
 			   String textEntered = inputText.getText().toString();
 			   String translatedText = ermahgerd.translate(textEntered);
 			   outputText.setText(translatedText);
 			 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 		        imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 		   }
 	   };
 
 	   
 	    private OnClickListener CopyButtonListener = new OnClickListener() {
 			   public void onClick(View v) {
 				 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 			        imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 			        if (outputText.getText().toString().length()>0) {
 
 			        	final ClipboardManager clipBoard = (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
 			        	clipBoard.setText(outputText.getText().toString());
 			        	Context context = getApplicationContext();
 						   CharSequence text = "Ermahgerd! Terxt Cerperd ter dah clerpberd!";
 						   int duration = Toast.LENGTH_SHORT;
 
 						   Toast toast = Toast.makeText(context, text, duration);
 						   toast.show();
 			        }
 
 			   }
 		   };
 	   
 
 		   private OnClickListener shareButtonListener = new OnClickListener() {
 			   public void onClick(View v) {
 					 InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 				        imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 
 				        Bitmap bitmap = getBitmapFromView(workingImage);
 				        
 				        String saved = Images.Media.insertImage(v.getContext().getContentResolver(), bitmap, "Ermahgerd!", "Created with Ermahgerd Translator for Android");
 				        Uri sdCardUri = Uri.parse("file://" + Environment.getExternalStorageDirectory());
 				        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, sdCardUri));
 
 				        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
 				        sharingIntent.setType("image/png");
 				        sharingIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(saved));
 				        startActivity(Intent.createChooser(sharingIntent, "Share image"));
 			   }
 		   };
 		   
 		   public static Bitmap getBitmapFromView(View view) {
 			    Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
 			    Canvas canvas = new Canvas(returnedBitmap);
 			    Drawable bgDrawable =view.getBackground();
 			    if (bgDrawable!=null) 
 			        bgDrawable.draw(canvas);
 			    else 
 			        canvas.drawColor(Color.WHITE);
 			    view.draw(canvas);
 			    return returnedBitmap;
 			}
 
 		   
 		   //new
 		   private OnClickListener newButtonListener = new OnClickListener() {
 				   public void onClick(View v) {
 					   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 					   imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 
 					   Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
 					   photoPickerIntent.setType("image/*");
 					   startActivityForResult(photoPickerIntent, SELECT_PHOTO);    
 
 				   }
 			   };
 
 			    private OnClickListener topButtonListener = new OnClickListener() {
 					   public void onClick(View v) {
 						   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 						   imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 						   String translatedText = outputText.getText().toString();
 						   if (translatedText.length()>0) addText(translatedText, 0);
 					   }
 				   };
 
 				    private OnClickListener bottomButtonListener = new OnClickListener() {
 						   public void onClick(View v) {
 							   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 							   imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 							   String translatedText = outputText.getText().toString();
 							   if (translatedText.length()>0) addText(translatedText, 1);							   
 						   }
 					   };
 
 					    
 					   private OnClickListener splitButtonListener = new OnClickListener() {
 							   public void onClick(View v) {
 								   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 								   imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 								   String translatedText = outputText.getText().toString();
 								   if (translatedText.length()>0) addText(translatedText, 2);							   
 							   }
 						   };
 
 						 
 				   private OnClickListener talkButtonListener = new OnClickListener() {
 					   public void onClick(View v) {
 						   InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
 						   imm.hideSoftInputFromWindow(inputText.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
 						   String translatedText = outputText.getText().toString();
 
 						   Context context = getApplicationContext();
 							   CharSequence text = "Coming soon!";
 							   int duration = Toast.LENGTH_SHORT;
 							   Toast toast = Toast.makeText(context, text, duration);
 							   toast.show();
 					   }
 				   };
 						   
 			   @Override
 			   protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
 			       super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 
 
 			       switch(requestCode) { 
 			       case SELECT_PHOTO:
 			           if(resultCode == RESULT_OK){
 			        	   handleSendImage(imageReturnedIntent, false);
 			           }
 			           
 			       }
 
 			   }
 
 			   private void handleSendImage(Intent intent, boolean external_call) {
 
 				   Uri selectedImage;				   
 				   if (external_call) selectedImage = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
 				   else selectedImage = intent.getData();
 				   			
 				   	   InputStream imageStream;
 							               
 		               try {
 					        imageStream = getContentResolver().openInputStream(selectedImage);
 					        bgBitmap.recycle();
 					        bgBitmap = BitmapFactory.decodeStream(imageStream);
 					        	        
 					        int bmWidth = bgBitmap.getWidth();
 					        int bmHeight = bgBitmap.getHeight();
 
 					        float scaleFactor = 640f/(float)((bmWidth + bmHeight)/2 + Math.abs(bmWidth-bmHeight)/2);
 				        	bmHeight *= scaleFactor;
 				        	bmWidth *= scaleFactor;
 					        
 					        bgBitmap = Bitmap.createScaledBitmap(bgBitmap, bmWidth, bmHeight, false);
 					        Drawable bgd = new BitmapDrawable(r, bgBitmap);
 
 					        Drawable[] layers = new Drawable[2];
 					        layers[0] = bgd;
 					        layers[1] = bgd;
 					        layerDrawable = new LayerDrawable(layers);
 
 							workingImage.setImageDrawable(layerDrawable);
 							   
 						} catch (FileNotFoundException e) {
 
 							outputText.setText(e.toString());
 						}
 		               
 			   }
 			   
 			   private int whereToSplit(String s) {
 				   int n = 0;
 				   String[] arr = s.split(" ");
 				   for (int i=0; n < (s.length()/2) ;i++) {
 					   n+=arr[i].length() + 1 ;
 				   }
 				   return n;
 			   }
 			   
 			   private void addText(String text, int position) {
 				   // position 0=top, 1=bottom, 2=split
 				   int numchars = 0, numlines = 1;
 				   Bitmap cs;
 				   
 				   int charLen = 37;
 				   numchars = text.length();
 
				   if (numchars>14) numlines = 2;
 	
 			        String sub1 = "";
 			        String sub2 = "";
 			        int n = numchars;
 			        if (numlines == 2) {
 			        	n = whereToSplit(text);
 			        	sub1 = text.substring(0, n);
 			        	sub2 = text.substring(n,text.length());
 			        }
 			        else if (position == 1) sub2 = text;
 			        else sub1 = text;
 				   
 				   
 				   int imgWidth = charLen * n;
 				   int imgHeight = (int) (bgBitmap.getHeight() * ((float)imgWidth/(float)bgBitmap.getWidth()));
 				   cs = Bitmap.createBitmap(imgWidth, imgHeight, Bitmap.Config.ARGB_8888);
 				   Canvas canvas = new Canvas(cs);
 
 
 			        int y1s1 = 70, y1s2 = 130, y2s1 = imgHeight-90, y2s2 = imgHeight-30;
 
 			        Paint paint = new Paint();
 			        paint.setColor(Color.WHITE);
 			        paint.setAntiAlias(true);
 			        paint.setTextSize(60);
			        paint.setTypeface(Typeface.DEFAULT_BOLD);
 			        paint.setStyle(Paint.Style.FILL_AND_STROKE);
 			        int width = cs.getWidth();
 			        
 			        int cw = ( (numlines==1)?width/numchars:width/sub1.length());
 			        			        
 		        	canvas.drawText(sub1, 10+((width/2)-cw*sub1.length()/2), (position==1?y2s1:y1s1), paint);
 		        	canvas.drawText(sub2, 10+((width/2)-cw*sub2.length()/2), (position==0?y1s2:y2s2), paint);
 
 /**		        	
 			        paint.setColor(Color.RED);
 			        paint.setTextSize(5+n);		        	
 			        String credits = "Ermahgerd Translator for Android";
 			        canvas.drawText(credits, 20, imgHeight-5, paint);
 */		        	
 		        	Drawable dbm = new BitmapDrawable(r, cs);
 		        	Bitmap bitmap = ((BitmapDrawable) dbm).getBitmap();
 		        	Drawable d = new BitmapDrawable(Bitmap.createScaledBitmap(bitmap, workingImage.getWidth(), workingImage.getHeight(), true));
 		        	
 			        Drawable[] layers = new Drawable[2];
 			        layers[0] = layerDrawable.getDrawable(0);
 			        layers[1] = d;
 			        layerDrawable = new LayerDrawable(layers);
 		        	
 		        	workingImage.setImageDrawable(layerDrawable);
 			   }
 
 	   
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_ermahgerd, menu);
         return true;
     }
 
     
 }
