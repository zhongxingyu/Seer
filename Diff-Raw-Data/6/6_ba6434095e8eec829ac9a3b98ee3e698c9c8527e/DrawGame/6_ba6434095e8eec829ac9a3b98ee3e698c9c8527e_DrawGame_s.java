 package com.multimedia.slidepuzzel;
 
 import java.io.InputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 import android.hardware.Camera.Size;
 import android.net.Uri;
 import android.util.Log;
 import android.view.MotionEvent;
 
 import com.multimedia.slidepuzzel.gamelogic.Game;
 
 public class DrawGame{
 	public Size imageSize;			// Size of the camera image
 
 	private Game game;				// The game this object should draw
 	private Paint p;				// Paint for outer square lines
 	
 	private int[] rgb;				// Camera rgb data
 	private Bitmap bitmap;			// Camera rgb data put into Bitmap
 	private Uri uri;
 	private Activity activity;
 	
 	private int tileSize;			// Size of a tile (tileSize * tileSize)
 	private Rect[][] defaultRect;	// Default square positions for x,y
 	
 	private int swapX;				// Position x of tile to be swapped
 	private int swapY;				// Position y of tile to be swapped
 	private int animX;				// Shift in x direction per draw 
 	private int animY;				// Shift in y direction per draw 
 	private int anim;				// Animation counter
 	private Rect animRect;			// Animated rectangle, position will change in animation
 	
 	private float scaleFactor;		// Times the drawed image is scaled to fit the whole canvas
 	
 	private Context mContext;		// Application context
 	
 	private boolean fixedImage;		// Image is fixed or live camera image is frozen
 
 	public DrawGame(Activity activity, Game g){
 		game = g;
 		this.activity = activity;
 		// Inactive animation state
 		anim = -1;
 		swapX = -1;
 		swapY = -1;
 		fixedImage = false;
 		
 		game.getField().swapTile(game.getField().getNullX(), game.getField().getNullY() - 1);
 		//game.shuffle();
 		
 		p = new Paint();
 		mContext = activity.getApplicationContext();
 	}
 	
 	public DrawGame(Activity activity, Game g, Uri uri){
 		this(activity, g);
 		fixedImage = true;
 		this.uri = uri;
 	}
 
 	public void imageReceived(byte[] data) {
 		// Allocate the image as an array of integers if needed.
 		// Then, decode the raw image data in YUV420SP format into a red-green-blue array (rgb array)
 		
 		if(rgb == null){		
 			// Fixed image
 			if(uri != null){
 				if(bitmap == null){
 					return;
 				}
 				imageSize.width = bitmap.getWidth();
 				imageSize.height = bitmap.getHeight();
 				rgb = new int[1];
 			}else{
 				int arraySize = imageSize.width*imageSize.height;
 				rgb = new int[arraySize];
 			}
 			
 			// Calculate tile width & height by the least size of the camera image
 			tileSize = Math.min(imageSize.height, imageSize.width) / game.getSize();
 		
 			// Make default mapping rectangles
 			defaultRect = new Rect[game.getSize()][game.getSize()];
 			for(int x = 0; x < game.getSize(); x++){
 				for(int y = 0; y < game.getSize(); y++){
 					defaultRect[x][y] = new Rect(
 							(x * tileSize), (y * tileSize), 
 							((x + 1) * tileSize), ((y + 1) * tileSize)
 						);
 					
 					game.setTile(defaultRect[x][y], game.getDefaultField().getTileIdx(x, y));
 				}
 			}
 			
 		}
 		
 		// If fixed image do nothing with the received data, only apply rotation if needed.
 		if(fixedImage){
 			if(game.getRotation().rotationChanged()){
 				bitmap = game.getRotation().applyFixed(bitmap);
 			}
 			return;
 		}
 		
 		decodeYUV420SP(rgb, data);
 		
 		// Create bitmap
 		// TODO reuse bitmap
 		bitmap = Bitmap.createBitmap(rgb, imageSize.width, imageSize.height, Bitmap.Config.ARGB_8888);
 		// Apply rotation
 		bitmap = game.getRotation().apply(bitmap);
 	}
 	
 	public void getViewSize(int h, int w){
 		Log.d("FixedImage", "Received size " + w + "x" + h);
 		if(uri == null)return;
 		try{
 			InputStream image = activity.getContentResolver().openInputStream(uri);
 			
 			BitmapFactory.Options o = new BitmapFactory.Options();
 		    o.inJustDecodeBounds = true;
 		    BitmapFactory.decodeStream(image, null,  o);
 	
 		    //Find the correct scale value. It should be the power of 2.
 		    int width_tmp = o.outWidth, height_tmp = o.outHeight;
 		    int scale = 1;
 		    while(true){
 		        if(width_tmp / 2 < w || height_tmp / 2 < h){
 		            break;
 		        }
 		        width_tmp /= 2;
 		        height_tmp /= 2;
 		        scale *= 2;
 		    }
 	
 		    image.close();
 		    
 		    //Decode with inSampleSize
 		    image = activity.getContentResolver().openInputStream(uri);
 		    BitmapFactory.Options o2 = new BitmapFactory.Options();
 		    o2.inSampleSize = scale;
 		    bitmap = BitmapFactory.decodeStream(image, null, o2);
 		    
 		    image.close();
 		    Log.d("FixedImage", "Image loaded & decoded (" + bitmap + ")");
 		}catch(Exception e){
 			e.printStackTrace();
 			System.exit(1);
 		}
 	}
 
 	public void freezeCamera(){
 		fixedImage = true;
 	}
 	
 	public void unfreezeCamera(){
 		fixedImage = false;
 	}
 
 	public void draw(Canvas c){
 		// If bitmap is null the fixed image is not loaded yet
 		if(bitmap == null){
 			Log.d("FixedImage", "Bitmap unitialized");
 			return;
 		}
 		
 		if(anim > 0){
 			anim--;
 			// Move animating rectangle
 			animRect.offset(animX, animY);
 		}else if(anim == 0){	
 			// Swap the actual tile
 			game.getField().swapTile(swapX, swapY);
 			
 			// Last swap solved the puzzle
 			if(game.checkPuzzleSolved()){
 				game.getSound().playSound(game.getSound().win);
 				
 				int time = (int) (game.getGameTime().getTimeElapsed() / 1000);
 				Intent intent = new Intent().setClass(mContext, WinActivity.class);
 				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
 				intent.putExtra("time", time);
 				mContext.startActivity(intent);
 			}
 			
 			// Reset
 			anim = -1;
 			swapX = -1;
 			swapY = -1;
 			animRect = null;
 		}
 		
 		
 		// Scale
 		scaleFactor = (float) c.getHeight() / (float) (game.getSize() * tileSize);
 		c.scale(scaleFactor, scaleFactor, 0, 0);
 		
 		// Set color to black
 		p.setColor(0xFF000000);
 		// Draw the empty square black
 		c.drawRect(defaultRect[game.getField().getNullX()][game.getField().getNullY()], p);
 		
 		Rect t;
 		int tIdx;
 		for(int x = 0; x < game.getSize(); x++){
 			for(int y = 0; y < game.getSize(); y++){
 				tIdx = game.getField().getTileIdx(x, y);
 				
 				// Don't draw empty square
 				if(tIdx == 0){
 					continue;
 				}
 				
 				t = game.getTile(tIdx);
 				
 				if(x != swapX || y != swapY){
 					// Draw rectangle t of the bitmap into rectangle defaultRectangle[x][y]
 					c.drawBitmap(bitmap, t, defaultRect[x][y], null);		
 				}else if(animRect != null){
 					// Draw place from where swapped black
 					c.drawRect(defaultRect[x][y], p);
 					// Draw in animate rectangle, paint over black (sliding animation)
 					c.drawBitmap(bitmap, t, animRect, null);
 				}
 				
 				// Draw surrounding lines
 				c.drawLine((x * tileSize), (y * tileSize), ((x + 1) * tileSize), (y * tileSize), p);
 				c.drawLine((x * tileSize), ((y + 1) * tileSize), ((x + 1) * tileSize), ((y + 1) * tileSize), p);
 				
 				c.drawLine((x * tileSize), (y * tileSize), (x * tileSize), ((y + 1) * tileSize), p);
 				c.drawLine(((x + 1) * tileSize), (y * tileSize), ((x + 1) * tileSize), ((y + 1) * tileSize), p);
 				
 			}
 		}
 	}
 	
 	public void onTouchEvent(MotionEvent event){
 		if(anim == -1 && !game.isSolved()){		
 			float x =  event.getX();
 			float y =  event.getY();
 			// Apply reverse scale to get the actual x,y in the unscaled plane
 			x /= scaleFactor;
 			y /= scaleFactor;
 			
 			swapX = (int) Math.round(x);
 			swapY = (int) Math.round(y);
 			swapX /= tileSize;
 			swapY /= tileSize;
 			
 			if(game.getField().validSwap(swapX, swapY)){
 				// Animation timer
 				anim = tileSize / 20;
 				
 				animRect = new Rect(game.getTile(game.getDefaultField().getTileIdx(swapX, swapY)));
 				
 				// Shift of rectangle each draw
 				animX = game.getField().getNullX() - swapX;
 				animY = game.getField().getNullY() - swapY;
 				animX *= 20;
 				animY *= 20;
 				
 				game.getSound().playSound(game.getSound().swap);
 			}else{
 				// Reset swap x & y
 				swapX = -1;
 				swapY = -1;
 			}
 		}
 	}
 	
 	/*
 	 * Decode the incoming data (YUV format) to a red-green-blue format
 	 */
 	private void decodeYUV420SP(int[] rgb, byte[] yuv420sp) {
 		final int width = imageSize.width;
 		final int height = imageSize.height;
 		final int frameSize = width * height;
 		
 		for (int j = 0, yp = 0; j < height; j++) {
 			int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
 			for (int i = 0; i < width; i++, yp++) {
 				int y = (0xff & ((int) yuv420sp[yp])) - 16;
 				if (y < 0) y = 0;
 				if ((i & 1) == 0) {
 					v = (0xff & yuv420sp[uvp++]) - 128;
 					u = (0xff & yuv420sp[uvp++]) - 128;
 				}
 				
 				int y1192 = 1192 * y;
 				int r = (y1192 + 1634 * v);
 				int g = (y1192 - 833 * v - 400 * u);
 				int b = (y1192 + 2066 * u);
 				
 				if (r < 0) r = 0; else if (r > 262143) r = 262143;
 				if (g < 0) g = 0; else if (g > 262143) g = 262143;
 				if (b < 0) b = 0; else if (b > 262143) b = 262143;
 				
 				rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
 			}
 		}
 	}
 }
