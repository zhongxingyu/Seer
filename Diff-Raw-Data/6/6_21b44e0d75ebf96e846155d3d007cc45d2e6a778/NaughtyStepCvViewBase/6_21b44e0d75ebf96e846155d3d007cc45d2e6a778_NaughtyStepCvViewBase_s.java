 package net.arjam.naughtystep;
 
 import java.util.HashMap;
 import java.util.List;
 
 import org.opencv.core.Size;
 import org.opencv.highgui.Highgui;
 import org.opencv.highgui.VideoCapture;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.media.AudioManager;
 import android.media.SoundPool;
 import android.renderscript.Matrix2f;
 import android.util.Log;
 import android.view.SurfaceHolder;
 import android.view.SurfaceView;
 
 public abstract class NaughtyStepCvViewBase extends SurfaceView implements
         SurfaceHolder.Callback, Runnable {
     private static final String TAG = "NaughtyStep::SurfaceView";
     private static final int BORDERSIZE = 50;
     public static final int SOUND_KLAXON = 0;
     public static final int SOUND_CHEER = 1;
     public static final int SOUND_STARTBEEP = 1;
 
     private static final int STATE_NOT_STARTED = 0;
     private static final int STATE_IN_PROGRESS = 1;
     private static final int STATE_ALARM_SOUNDING = 2;
     private static final int STATE_FINISHED = 3;
     private int state = STATE_NOT_STARTED;
     
     private SoundPool mSoundPool;
     private HashMap<Integer, Integer> mSoundPoolMap;
     private SurfaceHolder mHolder;
     private VideoCapture mCamera;
     private int readyCountDown = 10;
 
 
     
     public NaughtyStepCvViewBase(Context context) {
         super(context);
         mHolder = getHolder();
         mHolder.addCallback(this);
         Log.i(TAG, "Instantiated new " + this.getClass());
         mSoundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
         mSoundPoolMap = new HashMap<Integer, Integer>();
         mSoundPoolMap.put(SOUND_KLAXON, mSoundPool.load(getContext(), R.raw.klaxon, 1));
         mSoundPoolMap.put(SOUND_CHEER, mSoundPool.load(getContext(), R.raw.cheer_8k, 1));
         mSoundPoolMap.put(SOUND_STARTBEEP, mSoundPool.load(getContext(), R.raw.beep, 1));
     }
 
     public void playSound(int sound) {
     	playSound(sound, false, false);
     }
     
     public void playSound(int sound, boolean loop, boolean stop) {
         /* Updated: The next 4 lines calculate the current volume in a scale of 0.0 to 1.0 */
         AudioManager mgr = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
         float streamVolumeCurrent = mgr.getStreamVolume(AudioManager.STREAM_MUSIC);
         float streamVolumeMax = mgr.getStreamMaxVolume(AudioManager.STREAM_MUSIC);    
         float volume = streamVolumeCurrent / streamVolumeMax;
         
         /* Play the sound with the correct volume */
        mSoundPool.play(mSoundPoolMap.get(sound), volume, volume, 1, loop?1:0, 1f);
     }
 
     
     public boolean openCamera() {
         Log.i(TAG, "openCamera");
         synchronized (this) {
             releaseCamera();
             mCamera = new VideoCapture(Highgui.CV_CAP_ANDROID + 1);
             if (!mCamera.isOpened()) {
                 mCamera.release();
                 mCamera = null;
                 Log.e(TAG, "Failed to open native camera");
                 return false;
             }
         }
         return true;
     }
 
     public void releaseCamera() {
         Log.i(TAG, "releaseCamera");
         synchronized (this) {
             if (mCamera != null) {
                 mCamera.release();
                 mCamera = null;
             }
         }
     }
 
     public void setupCamera(int width, int height) {
         Log.i(TAG, "setupCamera(" + width + ", " + height + ")");
         synchronized (this) {
             if (mCamera != null && mCamera.isOpened()) {
                 List<Size> sizes = mCamera.getSupportedPreviewSizes();
                 int mFrameWidth = width;
                 int mFrameHeight = height;
 
                 // selecting optimal camera preview size
                 {
                     double minDiff = Double.MAX_VALUE;
                     for (Size size : sizes) {
                         if (Math.abs(size.height - height) < minDiff) {
                             mFrameWidth = (int) size.width;
                             mFrameHeight = (int) size.height;
                             minDiff = Math.abs(size.height - height);
                         }
                     }
                 }
 
                 mCamera.set(Highgui.CV_CAP_PROP_FRAME_WIDTH, mFrameWidth);
                 mCamera.set(Highgui.CV_CAP_PROP_FRAME_HEIGHT, mFrameHeight);
             }
         }
 
     }
 
     public void surfaceChanged(SurfaceHolder _holder, int format, int width,
             int height) {
         Log.i(TAG, "surfaceChanged");
         setupCamera(width, height);
     }
 
     public void surfaceCreated(SurfaceHolder holder) {
         Log.i(TAG, "surfaceCreated");
         (new Thread(this)).start();
     }
 
     public void surfaceDestroyed(SurfaceHolder holder) {
         Log.i(TAG, "surfaceDestroyed");
         releaseCamera();
     }
 
     protected abstract Bitmap processFrame(VideoCapture capture);
 
     public void run() {
         Log.i(TAG, "Starting processing thread");
         Bitmap bmp = null;
         Bitmap firstBmp = null;
         Paint textPaint = new Paint();
         textPaint.setColor(0xFFFFFF);
         textPaint.setTextSize(14);
 
         int count = 0;
         while (true) {
            count+=1;
 
             synchronized (this) {
                 if (mCamera == null)
                     break;
 
                 if (!mCamera.grab()) {
                     Log.e(TAG, "mCamera.grab() failed");
                     break;
                 }
 
                 bmp = processFrame(mCamera);
             }
 
             if (bmp != null) {
                 if (firstBmp == null || count > 10) {
                     //firstBmp.recycle();
                     firstBmp = bmp;
                     count = 0;
                 }
                 Canvas canvas = mHolder.lockCanvas();
 
                 int width = bmp.getWidth();
                 int height = bmp.getHeight();
                 Integer pixErrorCount = new Integer(0);
                 for (int y = 0; y < height; y++) {
                     for (int x = 0; x < width; x++) {
                         if ((y > BORDERSIZE) && (x == BORDERSIZE)) {
                             x = bmp.getWidth()-BORDERSIZE;
                         }
                         int pixel = bmp.getPixel(x, y);
                         int lastPixel = firstBmp.getPixel(x, y);
                         int pixDiff = (android.graphics.Color.red(pixel) - android.graphics.Color
                                 .red(lastPixel)) ^ 2;
                         pixDiff += (android.graphics.Color.green(pixel) - android.graphics.Color
                                 .green(lastPixel)) ^ 2;
                         pixDiff += (android.graphics.Color.blue(pixel) - android.graphics.Color
                                 .blue(lastPixel)) ^ 2;
                         if (pixDiff > 48) {
                             bmp.setPixel(x, y, android.graphics.Color.RED);
                             pixErrorCount += 1;
                         }
                     }
                 }
 
                 if (canvas != null) {
                     canvas.drawBitmap(bmp,
                             (canvas.getWidth() - bmp.getWidth()) / 2,
                             (canvas.getHeight() - bmp.getHeight()) / 2, null);
 
                     // Finte state machine
                     switch (state) {
                     case STATE_NOT_STARTED:
                         if (pixErrorCount < 1000) {
                             // Play sound
                             readyCountDown -= 1;
                             if (readyCountDown == 0) {
 	                            playSound(SOUND_STARTBEEP);
 	                            state = STATE_IN_PROGRESS;
                             }
                             // TODO: Start clock
                         }
                         break;
                     case STATE_IN_PROGRESS:
                         if (pixErrorCount > 2000) {
                         	// Start alarm
                             // Play sound
                             playSound(SOUND_KLAXON, true, false);
                             state = STATE_ALARM_SOUNDING;
                             // TODO: Pause clock
                         }
                         // TODO: Detect time has run out and we are ready.
                         // If (time has finished) {
                         //    state = STATE_FINISHED;
                         // }
                         break;
                     case STATE_ALARM_SOUNDING:
                         if (pixErrorCount < 2000) {
                             // Stop alarm
                             playSound(SOUND_KLAXON, false, true);
                             state = STATE_IN_PROGRESS;
                             // TODO: Restart clock
                         }
                         break;
                     case STATE_FINISHED:
                         // TODO: Is this the actual way to end?
                         return;
                     }
                     mHolder.unlockCanvasAndPost(canvas);
                 }
             }
         }
 
         Log.i(TAG, "Finishing processing thread");
     }
 }
