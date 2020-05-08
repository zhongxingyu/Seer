 package edu.cmu.hcii.peer;
 
 import java.util.Random;
 
 import edu.cmu.hcii.novo.kadarbra.R;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.BitmapShader;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Paint.Style;
 import android.graphics.PixelFormat;
 import android.graphics.PorterDuff;
 import android.graphics.Rect;
 import android.graphics.Shader;
 import android.util.AttributeSet;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 /**
  * 
  * This view draws the audio noise levels produced by a SpeechRecognizer
  * 
  * @author Gordon
  * 
  */
 public class AudioFeedbackView extends SurfaceView implements SurfaceHolder.Callback {
 	class AudioFeedbackThread extends Thread {
 		
 		/** Variables that determine how animations are drawn **/
 		private int NUMBER_OF_BARS; // number of bars drawn
		private int BAR_MAXIMUM_OFFSET = 100; // how quickly bars grow in height
 		private int BAR_MARGIN = 0; // size of margin between bars
 		private int MINIMUM_RMS_READ = 1; // minimum noise level that will be drawn
 		public boolean SHIFT = false; // if shift is true, we show the left/right moving visualizer, if false, then we show the up/down bars
 
 		
         /* Handle to the surface manager object we interact with */
         private SurfaceHolder mSurfaceHolder;
 
         /* Indicate whether the surface has been created & is ready to draw */
         private volatile boolean mRun = false;
 
         /* For keeping track of initialization state */
         private boolean init = false;
         
         /* For handling pausing of drawing*/
         private volatile boolean paused = false;
 
         private int viewWidth;	// width of view
 		public int viewHeight;	// height of view 
 	
         /** Variables that handle drawing **/
      
         /* Bitmaps for drawing microphone and audio bars*/
         private Bitmap micActive_Scaled;
         private Bitmap micInactive_Scaled;
         private Bitmap dotPattern;
     
         /* Paints */
 		private Paint pBar;			// paint of bars
 		private Paint pThreshold; 	// paint of threshold marker
 		private Paint pActive; 		// paint of active items
 		
 		/* Audio levels */
 		private int levels[];						// pixel heights of bars (if directly corresponding the noise level)
 		private int drawnLevels[];					// pixel heights of bars (when actually drawn - this is different from the above for making animations smoother)
 		public float levelThreshold = 0;			// pixel height of level threshold line
 		public float levelThresholdPercent = 0;		// percent of level threshold line
 		private int busyDrawCounter;				// for drawing each frame within the busy animation
 		private float[] drawnLevelSpeeds;			// for drawing the offsets for each frame within the rising bar animation
 		private int randomLevelIndex;				// for randomizing the bars
 
 		private long lastMessageTime;	
 		private long lastDrawTime;		// for animating threshold 
 		private long lastRandomizeTime;	
         
         public AudioFeedbackThread(SurfaceHolder surfaceHolder, Context context) {
             // get handles to some important objects
             mSurfaceHolder = surfaceHolder;
             mContext = context;
         }
         
         
         @Override
         public void run() {
             while (mRun) {
                 Canvas c = null;
                 try {
                     c = mSurfaceHolder.lockCanvas(null);
                     synchronized (mSurfaceHolder) {
                     	if (!paused && c != null && init)
                         	doDraw(c);
                     }
                 } finally {
                     // do this in a finally so that if an exception is thrown
                     // during the above, we don't leave the Surface in an
                     // inconsistent state
                     if (c != null) {
                         mSurfaceHolder.unlockCanvasAndPost(c);
                     }
                 }
             }
             return;
         }
 
         /**
          * Used to signal the thread whether it should be running or not.
          * Passing true allows the thread to run; passing false will shut it
          * down if it's already running. Calling start() after this was most
          * recently called with false will result in an immediate shutdown.
          *
          * @param b true to run, false to shut down
          */
         public void setRunning(boolean b) {
             mRun = b;
         }
         
         /**
          * Sets current state
          * @param s current state
          */
         public void setState(int s){
         	synchronized (mSurfaceHolder) {
 	        	state = s;
 	        	if (state == STATE_ACTIVE){
 	        		refreshThresholdLine();
 	        	}
         	}
         }
         
         /**
          * Gets current state
          * @return current state
          */
         public int getCurState(){
         	return state;
         }
         
         /**
          * Sets threshold to the top
          * Sets time for last received message (used to calculate y-coordinate of threshold line)
          */
         public void refreshThresholdLine(){
     		levelThreshold = viewHeight;
     		levelThresholdPercent = 1;
     		lastMessageTime = System.currentTimeMillis();
         }
         
     	/**
     	 * Sets flag for whether to render the busy state animation
     	 * 
     	 * @param busyState
     	 */
     	public void setBusy(boolean busyState){
         	synchronized (mSurfaceHolder) {
 	    		busy = busyState;
 	    		refreshThresholdLine();
 	    		busyDrawCounter = 0;
         	}
     	}
     	
     	/**
     	 * Gets busy state
     	 * @return
     	 */
     	public boolean getBusy(){
     		return busy;
     	}
         
         /**
          * All drawing is handled here.
          * @param c
          */
 		private void doDraw(Canvas c) {
 			if (c==null)
 				return;
 			//c.drawColor(Color.BLACK);
 			c.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
 			//Log.v("hello","hello");
 			
 			
 			if (busy){
 				drawMicInactive(c);
 				drawBusy(c,0.15f,0.7f,40);
 			}else{
 				if (SHIFT)
 					drawAudioBars(c);
 				else
 					drawAudioBarsVert(c);
 				drawMicInactive(c);
 			}
 			
 			if (!busy){
 				if (state == STATE_INACTIVE){
 					drawInactive(c);
 				}else if (state == STATE_ACTIVE){
 					drawMicThresholdAnimation(c);
 				}
 			}
 		}
 		
 		/**
 		 * Initializes objects needed for drawing
 		 */
 		private void init(){
 			initBitmaps();
 			initPaints();
 			initNumberOfBars();
 			initArrays();
 			
 			init = true;
 
 			for (int i = 0; i < levels.length; i++ ){
 				drawnLevelSpeeds[i] = 1;
 			}
 		}
 		
 		private void initNumberOfBars(){
 			NUMBER_OF_BARS = (viewWidth / dotPattern.getWidth());
 		}
 		
 		private void initArrays(){
 			levels = new int[NUMBER_OF_BARS];
 			drawnLevels = new int[NUMBER_OF_BARS];
 			drawnLevelSpeeds = new float[NUMBER_OF_BARS];
 			
 		}
 		
 		/**
 		 * Initializes paint
 		 */
 		private void initPaints(){
 			pBar = new Paint();
 			pBar.setColor(Color.parseColor("#a4ece8"));
 		
 			pBar.setShader(new BitmapShader(dotPattern,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT));
 			
 			pActive = new Paint();
 			pActive.setColor(Color.parseColor("#FCDA4F"));
 			pActive.setStyle(Style.FILL);
 
 			pThreshold = new Paint();
 			pThreshold.setColor(Color.parseColor("#FCDA4F"));
 			pThreshold.setStrokeWidth(2.0f);		
 		}
 		
 		/**
 		 * Initializes bitmaps
 		 */
 		private void initBitmaps(){
 			Resources res = getContext().getResources();
         	Bitmap micActive = BitmapFactory.decodeResource(res, R.drawable.mic_active);
         	micActive_Scaled = scaleImageRelativeToViewHeight(micActive,0.8f);
         	
         	Bitmap micInactive = BitmapFactory.decodeResource(res, R.drawable.mic_inactive);
         	micInactive_Scaled = scaleImageRelativeToViewHeight(micInactive,0.8f);
 		
         	dotPattern = BitmapFactory.decodeResource(res, R.drawable.mic_dot);
         	
 		}
 		
         /* Callback invoked when the surface dimensions change. */
         public void setSurfaceSize(int width, int height) {
             // synchronized to make sure these all change atomically
             synchronized (mSurfaceHolder) {
                 viewWidth = width;
                 viewHeight = height;
     			init();
 
                 Log.v(TAG,"setSurfaceSize: viewWidth,viewHeight "+width+","+height);
             }
         }
         
         /**
          * Draws the inactive microphone 
          *
          * @param c
          */
         private void drawMicInactive(Canvas c){
         	int left = viewWidth/2-micInactive_Scaled.getWidth()/2;
         	int top = viewHeight/2-micInactive_Scaled.getHeight()/2;
         	
         	int right = viewWidth/2+micInactive_Scaled.getWidth()/2;
         	int bottom = viewHeight/2+micInactive_Scaled.getHeight()/2;
         	
         	c.drawBitmap(micInactive_Scaled, left, top, null);
         }
         
         /**
          * Draws the active microphone 
          *
          * @param c
          * @param clippingProportion % of the microphone that is not filled in
          */
         private void drawMicActive(Canvas c, float clippingProportion, Paint paint){
         	int left = viewWidth/2-micActive_Scaled.getWidth()/2;
         	int top = viewHeight/2-micActive_Scaled.getHeight()/2;
         	
         	int right = viewWidth/2+micActive_Scaled.getWidth()/2;
         	int bottom = viewHeight/2+micActive_Scaled.getHeight()/2;
 
         	c.clipRect(left, top + (clippingProportion * micActive_Scaled.getHeight()), right, bottom);
         	
         	c.drawBitmap(micActive_Scaled, left, top, paint);
         }
         
         
         /**
          * Scales image relative to the view height
          * 
          * @param bm input bitmap
          * @param scale height of returned bitmap relative to view height
          * @return scaled bitmap
          */
         private Bitmap scaleImageRelativeToViewHeight(Bitmap bm, float scale){
         	
         	int micWidth = bm.getWidth();
         	int micHeight = bm.getHeight();
         	
         	int scaledHeight = (int) (viewHeight * scale);
         	int scaledWidth = (int)((1.0f*micWidth)/(1.0f*micHeight) * scaledHeight);
         	
         	Bitmap scaledImg = Bitmap.createScaledBitmap(bm, scaledWidth, scaledHeight, false);
 
         	return scaledImg;
    
         }
         
         /**
          * Draws an indicator for when the view is in the inactive state
          * @param c
          */
         private void drawInactive(Canvas c){
         }
         
         /**
          * Draws the busy animation when the speech recognizer is processing speech
          * Fades in/out the fill inside the microphone
          * 
          * @param c
          * @param startAlpha starting alpha value
          * @param endAlpha ending alpha value
          * @param animationFrames duration of animation
          */
     	private void drawBusy(Canvas c, float startAlpha, float endAlpha, int animationFrames){
     		Paint p = new Paint();
     		p.setColor(Color.parseColor("#a4ece8"));
     		
     		float alpha = startAlpha;
     		if (busyDrawCounter < animationFrames/2){
     			alpha = startAlpha + (endAlpha-startAlpha)/(animationFrames) * busyDrawCounter; 
     		}else if (busyDrawCounter >= animationFrames/2){
     			alpha = endAlpha - (endAlpha-startAlpha)/(animationFrames) * (busyDrawCounter - animationFrames/2);     			
     		}
     		busyDrawCounter++;
     		if (busyDrawCounter >= animationFrames){
     			busyDrawCounter = 0;
     		}
     		    		
     		p.setAlpha((int) (255*alpha));
     		drawMicActive(c, 0, p);
     	}
     	
 
     	/**
     	 * Draws current frame for the microphone in its active state
     	 */
         private void drawMicThresholdAnimation(Canvas c){
         	levelThresholdPercent = Math.max(getThresholdLinePercent(), 0);
         	drawMicActive(c, 1f-levelThresholdPercent, pActive);
 
         	lastDrawTime = System.currentTimeMillis();
         }
     	
     	/**
     	 * Draws the threshold line
     	 * 
     	 * Not currently used.
     	 */
     	private void drawThresholdLine(Canvas c){
     		int maxDrawnLevel = getMaximumDrawnLevel();
     		
     		if (levelThreshold < maxDrawnLevel)
     			levelThreshold = maxDrawnLevel;
     		else{
     			levelThreshold = getThresholdLineHeight();
 
     		}
     		levelThreshold = Math.max(levelThreshold, 0);
     		
     		c.drawLine(0, viewHeight-levelThreshold, viewWidth, viewHeight-levelThreshold, pThreshold);
 
     		lastDrawTime = System.currentTimeMillis();
     		//Log.v("lastDrawTime, offset", lastDrawTime +", "+ levelThreshold);
     	}
     	
     	/**
     	 * Calculates the amount that the threshold visualizer (microphone) should be filled up
     	 * @return
     	 */
     	private float getThresholdLinePercent(){
     		return 1 - (((System.currentTimeMillis() - lastMessageTime) / ((float)MessageHandler.COMMANDS_TIMEOUT_DURATION)));
     	}
     	
     	/**
     	 * Calculates the distance the line should move downwards when audio level is lower than threshold line
     	 * @return
     	 */
     	private float getThresholdLineOffset(){
     		return (((System.currentTimeMillis() - lastDrawTime) / ((float)MessageHandler.COMMANDS_TIMEOUT_DURATION)) * viewHeight);
     	}
     	
     	/**
     	 * Calculates the distance the line should move downwards when audio level is lower than threshold line
     	 * @return
     	 */
     	private float getThresholdLineHeight(){
     		return viewHeight - (((System.currentTimeMillis() - lastMessageTime) / ((float)MessageHandler.COMMANDS_TIMEOUT_DURATION)) * viewHeight);
     	}
     	
 
     	/**
     	 * Get maximum drawn bar height
     	 * @return
     	 */
     	private int getMaximumDrawnLevel(){
     		int maxDrawnLevel = 0;
     		// get maximum drawnLevel
     		for (int i = 0; i < drawnLevels.length; i++){
     			maxDrawnLevel = Math.max(maxDrawnLevel, drawnLevels[i]);
     		}
     		return maxDrawnLevel;
     	}
     	
     	
     	/**
     	 * Draws the audio bars (that move from left to right)
     	 * @param c
     	 */
     	private void drawAudioBars(Canvas c){
     		int barWidth = (viewWidth - BAR_MARGIN*levels.length) / levels.length; 
     		
     		for (int i = 0; i < levels.length; i++){
     			//Rect rect = new Rect(i*50+i*BAR_MARGIN,viewHeight-drawnLevels[i],i*50+50+i*BAR_MARGIN,viewHeight);
     			Rect rect = new Rect(i*barWidth+i*BAR_MARGIN,viewHeight-drawnLevels[i],i*barWidth+barWidth+i*BAR_MARGIN,viewHeight);
 
     			c.drawRect(rect, pBar);	
     		}
     	}
     	
     	/**
     	 * Draws audio bars (that go up and down)
     	 * @param c
     	 */
     	private void drawAudioBarsVert(Canvas c){
     		if (levels != null){
 	    		int barWidth = (viewWidth - BAR_MARGIN*levels.length) / levels.length; 
 	    		
 	    		if (getCurState() == STATE_INACTIVE){
 	    			pBar.setColor(Color.GRAY);
 	    		}else if (getCurState() == STATE_ACTIVE){
 	    			pBar.setColor(Color.parseColor("#a4ece8"));
 	    		}
 	    		
 	    		for (int i = 0; i < levels.length; i++){
 	    			Rect rect;
 	    			
 	    			int drawnHeight = viewHeight-drawnLevels[i];
 	    			drawnHeight = (drawnHeight / dotPattern.getHeight()) * dotPattern.getHeight();
 	    			
 	    			rect = new Rect(i*barWidth+i*BAR_MARGIN, drawnHeight,i*barWidth+barWidth+i*BAR_MARGIN,viewHeight);
 	    			
 	    			c.drawRect(rect, pBar);	
 	    			
 	    		}
     		}
     	}
     	
     	
     	/**
     	 * Shifts audio level array by adding on the most recent audio height
     	 * @param pixel most recent audio height
     	 */
     	private void shiftAudioLevelArray(int pixel){
     		for (int i = 0; i < levels.length; i++){
     			if (i == levels.length-1){
     				levels[levels.length-1] = pixel;
     			}else{
     				levels[i] = levels[i+1];
     			}
     		}
     	
     	}
     	
     	
     	/**
     	 * Fills audio levels with the most recent pixel level value and random values based off of this pixel level
     	 */
     	private void fillAudioLevelArrayRandom(int pixel){
 			for (int i = 0; i < levels.length; i++){
 				if (randomLevelIndex == i)
 					levels[i] = pixel;
 				else
 					levels[i] = (int) (pixel * drawnLevelSpeeds[i] * 2);
 			}
 
     	}
     	
     	
     	/**
     	 * Converts pixel heights of bars to their heights when drawn
     	 */
     	private void convertAllPixelsToDrawnPixels(){
     		for (int i = 0; i < levels.length; i++){
     			drawnLevels[i] = Math.max(0, Math.min(viewHeight, convertPixelToDrawnPixel(i)));
     		}
     	}
     	
     	/**
     	 * Converts the pixel size of visualizer that corresponds exactly to audio level to 
     	 * the pixel level that is drawn so the animation is smoother
     	 * 
     	 * @param arraySlot the # visualizer being drawn (corresponds to levels and drawnLevels arrays)
     	 * @return 
     	 */
     	private int convertPixelToDrawnPixel(int arrayIndex){
     		
     		int pixelDiff = levels[arrayIndex] - drawnLevels[arrayIndex];
     		int drawnPixel = drawnLevels[arrayIndex];
     				
     		if (pixelDiff > 0){
     			if (pixelDiff > BAR_MAXIMUM_OFFSET){
     				drawnPixel = (int) (drawnLevels[arrayIndex] + BAR_MAXIMUM_OFFSET * drawnLevelSpeeds[arrayIndex]);
     			}else{
     				drawnPixel = levels[arrayIndex];
     			}
     		}else if (pixelDiff < 0){
     			if (Math.abs(pixelDiff) > BAR_MAXIMUM_OFFSET){
     				drawnPixel = (int) (drawnLevels[arrayIndex] - BAR_MAXIMUM_OFFSET * (drawnLevelSpeeds[arrayIndex] / 2));
     			}else{
     				drawnPixel = levels[arrayIndex];
     			}
     		}
     		
     		return drawnPixel;
     	}
     	
     	/**
     	 * Randomly generates bar speeds
     	 */
     	private void setBarRandomSpeeds(){
     		if (System.currentTimeMillis() - lastRandomizeTime > 200){
     		
     			Random rand = new Random();
     			randomLevelIndex = rand.nextInt(levels.length);
     		
     			for (int i = 0; i < levels.length; i++){
     				if (i == randomLevelIndex)
     					drawnLevelSpeeds[i] = 1f;
     				else{
     					int speed = rand.nextInt(10);
     					drawnLevelSpeeds[i] = speed/20.0f+.25f;
     				}
     			}
     			lastRandomizeTime = System.currentTimeMillis();
 
     		}
     		
     	}
     	
     	/**
     	 * Converts raw rms value to an audio level percentage where 100% means the loudest and 0% means no audio	
     	 * @param rms
     	 * @return audio level percentage
     	 */
     	private float convertRmsToPercent(float rms){
     		float percent;
     		
     		if (rms < MINIMUM_RMS_READ)
     			percent = 0;
     		else
     			percent = (rms - MINIMUM_RMS_READ)/(10 - MINIMUM_RMS_READ);
     		
     		percent = Math.max(Math.min(1,percent),0);
     		    		
     		return percent;
     	}
     	
     	/**
     	 * Converts an audio level percentage to pixel size
     	 * @param percent
     	 * @return pixel size of audio feedback visualizer
     	 */
     	private int convertPercentToPixel(float percent){
     		int pixel;
     		pixel = (int) (percent * viewHeight);
     		return pixel;
     	}
  
     	/**
     	 * Updates audio feedback visualization numbers
     	 * @param rms raw rms value from SpeechRecognizer
     	 */
     	private void updateAudioValues(float rms){
     		float percent = convertRmsToPercent(rms);
     		int pixel = convertPercentToPixel(percent);
     		
     		if (SHIFT){
     			shiftAudioLevelArray(pixel);
     		}else{
     			setBarRandomSpeeds();
     			fillAudioLevelArrayRandom(pixel);
     		}
     		convertAllPixelsToDrawnPixels();
     		
     		if (rms > MessageHandler.MINIMUM_REFRESH_RMS)
     			lastMessageTime = System.currentTimeMillis();
     		//drawnLevels[drawnLevels.length-1] = convertPixelToDrawnPixel(drawnLevels.length-1);
     	}
     	
     	
     	/**
     	 * Pauses drawing
     	 */
     	public void pause(){
     		synchronized (mSurfaceHolder) {
     			paused = true;
     		}
     	}
     	
     	/**
     	 * Unpauses drawing
     	 */
     	public void unpause(){
     		synchronized (mSurfaceHolder) {
     			paused = false;
     		}
     	}
 	}
 	
 	private String TAG = "AudioFeedbackView";
 	
 	/* State of audio feedback visualizer */
 	public int STATE_INACTIVE = 0;
 	public int STATE_ACTIVE = 1;
 	public boolean busy = false;
     private int state = STATE_INACTIVE;
 
     /* The thread that actually draws the animation */
     private AudioFeedbackThread thread;
     
     /* Handle to the application context, used to e.g. fetch Drawables. */
     private Context mContext;
     private SurfaceHolder mHolder;
     
     /* Whether the thread has been closed */
     private boolean threadClosed = false;
     
 	public AudioFeedbackView(Context context, AttributeSet attrs) {
 		super(context, attrs);
         // register our interest in hearing about changes to our surface
 		mHolder = getHolder();
 		mHolder.addCallback(this);
         mContext = context;
         setZOrderOnTop(true);
         mHolder.setFormat(PixelFormat.TRANSPARENT);
         // create thread only; it's started in surfaceCreated()
         thread = new AudioFeedbackThread(mHolder, mContext);
 
         setFocusable(true); // make sure we get key events
         setBackgroundColor(Color.TRANSPARENT);
 	}
 	
     /**
      * Fetches the animation thread corresponding to this LunarView.
      *
      * @return the animation thread
      */
     public AudioFeedbackThread getThread() {
         return thread;
     }
 
 	
     @Override
     protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld){
         super.onSizeChanged(xNew, yNew, xOld, yOld);
         thread.viewWidth = xNew;
         thread.viewHeight = yNew;
 
         Log.v(TAG,"w,h: "+thread.viewWidth+", "+thread.viewHeight);
     }
     
 	/**
 	 * Updates audio feedback visualization
 	 * @param rms raw rms value from SpeechRecognizer
 	 */
 	public void updateAudioFeedbackView(float rms){
 		if (thread.init)
 			thread.updateAudioValues(rms);
 	}
 
 	@Override
 	public void surfaceChanged(SurfaceHolder arg0, int format, int width, int height) {
         thread.setSurfaceSize(width, height);
 		Log.v(TAG,"surfaceChanged");
 
 	}
 
 	@Override
 	public void surfaceCreated(SurfaceHolder arg0) {
         // start the thread here so that we don't busy-wait in run()
         // waiting for the surface to be created
 		Log.v(TAG,"surfaceCreated");
 		if (threadClosed){
 			thread = new AudioFeedbackThread(mHolder, mContext);
 			
 		}
 		thread.setRunning(true);
 		thread.start();
 	}
 
 	@Override
 	public void surfaceDestroyed(SurfaceHolder arg0) {
         // we have to tell thread to shut down & wait for it to finish, or else
         // it might touch the Surface after we return and explode
 		
 		Log.v(TAG,"surfaceDestroyed");
         boolean retry = true;
         thread.setRunning(false);
         while (retry) {
             try {
                 thread.join();
                 retry = false;
                 threadClosed = true;
             } catch (InterruptedException e) {
             }
         }		
 	}
 	
 
 
 }
