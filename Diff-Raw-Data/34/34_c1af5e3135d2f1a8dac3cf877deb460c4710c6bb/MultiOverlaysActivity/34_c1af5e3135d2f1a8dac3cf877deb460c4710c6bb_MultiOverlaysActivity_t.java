 package com.multioverlays;
 
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import com.multioverlays.R;
 import com.multioverlays.ClearGLSurfaceView;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.graphics.Color;
import android.graphics.PixelFormat;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnBufferingUpdateListener;
 import android.media.MediaPlayer.OnCompletionListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.Surface;
 import android.view.SurfaceHolder;
 import android.view.View;
 import android.view.ViewGroup;
 import android.view.Window;
 import android.view.WindowManager;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.FrameLayout;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 import android.widget.Toast;
 
 
 public class MultiOverlaysActivity extends Activity 
 {
     private static final String TAG = "Multioverlays demo";
     private short mNumLayers = 8;
     private int LAYER_WIDTH = 1152;
     private int LAYER_HEIGHT = 768;
     private int[] mFrameViewIdArray;
     private int[] mTextViewIdArray;
 	private ClearGLSurfaceView[] mStvArray;
 
 
     public void onCreate(Bundle icicle) {
     	   	
         super.onCreate(icicle);
         
         mFrameViewIdArray = new int[mNumLayers];
         mFrameViewIdArray[0] = R.id.frameLayout1; mFrameViewIdArray[1] = R.id.frameLayout2;
         mFrameViewIdArray[2] = R.id.frameLayout3; mFrameViewIdArray[3] = R.id.frameLayout4;
         mFrameViewIdArray[4] = R.id.frameLayout5; mFrameViewIdArray[5] = R.id.frameLayout6;
         mFrameViewIdArray[6] = R.id.frameLayout7; mFrameViewIdArray[7] = R.id.frameLayout8;
 
         mTextViewIdArray = new int[mNumLayers];
         mTextViewIdArray[0] = R.id.textFPS1; mTextViewIdArray[1] = R.id.textFPS2;
         mTextViewIdArray[2] = R.id.textFPS3; mTextViewIdArray[3] = R.id.textFPS4;
         mTextViewIdArray[4] = R.id.textFPS5; mTextViewIdArray[5] = R.id.textFPS6;
         mTextViewIdArray[6] = R.id.textFPS7; mTextViewIdArray[7] = R.id.textFPS8;
 
         Window window = getWindow();
         window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
         
         Display display = getWindowManager().getDefaultDisplay(); 
         int width = display.getWidth();
         int height = display.getHeight();
         
         if(width == 1280 && height == 752)
         	setContentView(R.layout.main);
         else if(width == 864 && height == 480)
         	setContentView(R.layout.main_blaze);
         else
         {
         	// Hack but better than nothing
         	AlertDialog.Builder builder_res = new AlertDialog.Builder(this);
             builder_res.setTitle("Unhandled device resolution");
         	AlertDialog alert_res = builder_res.create();
             alert_res.show();
             if (alert_res.isShowing())
             	return;
         }
         
         
         final CharSequence[] items_res = {"640x480","1024x768", "1152x768", "1280x720", "1920x1080"};
 
         AlertDialog.Builder builder_res = new AlertDialog.Builder(this);
         builder_res.setTitle("Choose layers resolution");
         builder_res.setSingleChoiceItems(items_res, -1, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
             	if(items_res[item].toString().compareTo("640x480") == 0)
             	{
             		LAYER_WIDTH = 640;
             		LAYER_HEIGHT = 480;
             	} 
             	else if(items_res[item].toString().compareTo("1024x768") == 0)
             	{
             		LAYER_WIDTH = 1024;
             		LAYER_HEIGHT = 768;
             	}
             	else if(items_res[item].toString().compareTo("1152x768") == 0)
             	{
             		LAYER_WIDTH = 1152;
             		LAYER_HEIGHT = 768;
             	}
             	else if(items_res[item].toString().compareTo("1280x720") == 0)
             	{
             		LAYER_WIDTH = 1280;
             		LAYER_HEIGHT = 720;
             	}
             	else if(items_res[item].toString().compareTo("1920x1080") == 0)
             	{
             		LAYER_WIDTH = 1920;
             		LAYER_HEIGHT = 1080;
             	}
             	
             	RelativeLayout.LayoutParams rl1 = new RelativeLayout.LayoutParams(LAYER_WIDTH,LAYER_HEIGHT);
             	findViewById(R.id.frameLayout1).setLayoutParams(rl1);
             	
             	for (int flId = 1; flId < mNumLayers; flId++) {
             		int idprev = getFrameViewId(flId-1);
             		int id = getFrameViewId(flId);
             		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(LAYER_WIDTH,LAYER_HEIGHT);
                 	p.addRule(RelativeLayout.ALIGN_LEFT , findViewById(idprev).getId());
                 	p.addRule(RelativeLayout.ALIGN_TOP , findViewById(idprev).getId());
                 	p.setMargins(10, 10, 0, 0);
                 	findViewById(id).setLayoutParams(p);
             	}
 
             	initSurfaceViews();
     	    	((Button) findViewById(R.id.gobutton)).setVisibility(4);
     	    	((TextView) findViewById(R.id.textFPSAverage)).setVisibility(0);
     	    	((TextView) findViewById(R.id.textFPS1)).setVisibility(0);   
     	    	
     	    	((Button) findViewById(R.id.alphaUp)).bringToFront();
     	        ((Button) findViewById(R.id.alphaDown)).bringToFront();
                 dialog.dismiss();
             }
         });
         AlertDialog alert_res = builder_res.create();
         alert_res.show();
                    
         final CharSequence[] items = {"1", "2", "3", "4", "5", "6", "7", "8"};
 
         AlertDialog.Builder builder = new AlertDialog.Builder(this);
         builder.setTitle("Choose # of layers to be displayed");
         builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int item) {
             	mNumLayers = (short) Integer.parseInt((String) items[item]);
                 dialog.dismiss();
             }
         });
         AlertDialog alert = builder.create();
         alert.show();
         
         // setting TI logo
         ImageView logo = (ImageView) findViewById(R.id.imageViewLogo);
         logo.setImageResource(R.drawable.ti_logo);
         
         ((Button) findViewById(R.id.alphaUp)).setOnClickListener(mAlphaUpListener);
         ((Button) findViewById(R.id.alphaDown)).setOnClickListener(mAlphaDownListener);
         ((Button) findViewById(R.id.clearEveryFrame)).setOnClickListener(mClearEveryFrame);
         ((Button) findViewById(R.id.alignFrames)).setOnClickListener(mToggleOffsets);
     }   
     
     public int getNumLayers() { return mNumLayers; }
     public int getFrameViewId(int idx) { return mFrameViewIdArray[idx]; }
     public int getTextViewId(int idx) { return mTextViewIdArray[idx]; }
 
 	class OffsetsClickListener implements OnClickListener {
 		private MultiOverlaysActivity mActivity;
 		private boolean mOffsetsOn = true;
 
 		public OffsetsClickListener(MultiOverlaysActivity aActivity) {
 			mActivity = aActivity;
 		}
 		@Override
     	public void onClick(View v) {
     		int maxid = mActivity.getNumLayers();
     		mOffsetsOn = !mOffsetsOn;
     		for (int i = 0; i < maxid; i++) {
     			int fvId = mActivity.getFrameViewId(i);
     			FrameLayout fl = (FrameLayout) mActivity.findViewById(fvId);
     			RelativeLayout.LayoutParams p = (RelativeLayout.LayoutParams)fl.getLayoutParams();
     			if (mOffsetsOn) {
     				p.setMargins(10, 10, 0, 0);
     			} else {
     				p.setMargins(0, 0, 0, 0);
     			}
     			fl.setLayoutParams(p);
     		}
     	}
     };
     
     OnClickListener mToggleOffsets = new OffsetsClickListener(this);
 
     OnClickListener mClearEveryFrame = new OnClickListener() {
         @Override
         public void onClick(View v) {
         	
         	if (((Button) findViewById(R.id.clearEveryFrame)).getText().toString().compareTo("glClear() on") == 0)
         		((Button) findViewById(R.id.clearEveryFrame)).setText("glClear() off");
         	else
         		((Button) findViewById(R.id.clearEveryFrame)).setText("glClear() on");
         	
         	for (int flId = 0 ; flId < mNumLayers; flId++ ) {
         		FrameLayout fl = (FrameLayout)findViewById(getFrameViewId(flId));
         		((ClearGLSurfaceView)(fl).getChildAt(0)).toggleClearEveryFrame();
         	}
         }
     };
     
     OnClickListener mAlphaUpListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
         	for (int flId = 0 ; flId < mNumLayers; flId++ ) {
         		FrameLayout fl = (FrameLayout)findViewById(getFrameViewId(flId));
         		((ClearGLSurfaceView)(fl).getChildAt(0)).setAlphaUp();
         	}
         }
     };
     
     OnClickListener mAlphaDownListener = new OnClickListener() {
         @Override
         public void onClick(View v) {
         	for (int flId = 0 ; flId < mNumLayers; flId++ ) {
         		FrameLayout fl = (FrameLayout)findViewById(getFrameViewId(flId));
         		((ClearGLSurfaceView)(fl).getChildAt(0)).setAlphaDown();
         	}        	
         }
     };
     
     private static double timeFPS = 0;
     private static double prev_time = 0;
     private static double fps = 0;
     
     private static class ourRunnable implements Runnable {
     	private TextView[] mTvArray;
     	private ClearGLSurfaceView[] mStArray;
     	private int mCount;    	
     	private TextView tvAVG;
 
         public ourRunnable(int aCount, TextView[] aTvArray, TextView aTvAVG, ClearGLSurfaceView[] aStArray) {
         	tvAVG = aTvAVG; mCount = aCount; mTvArray = aTvArray; mStArray = aStArray;
         }
     	@Override
         public void run() {
         	timeFPS = System.currentTimeMillis() - prev_time;
         	prev_time = System.currentTimeMillis();
         	if(timeFPS > 0)
         		fps = 1/(timeFPS/1000);
         	Log.d("time", "time = " + timeFPS);
         	
         	int averageFPS = 0;
         	for (int i = 0; i < mCount; i++) {
         		int fps = mStArray[i].getFrameCounter();
         		mTvArray[i].setText("FPS = " + fps);
         		averageFPS += fps;
         		mStArray[i].resetFrameCounter();
         	}
         	averageFPS /= mCount;
         	tvAVG.setText("AVG FPS = " + (int)averageFPS);
         }
     }
     
     private static class UpdaterAVG extends TimerTask {
     	private TextView[] mTvArray;
     	private ClearGLSurfaceView[] mStArray;
     	private int mCount;    	
     	private TextView tvAVG;
     	private ourRunnable mOurRunnable;
 
         public UpdaterAVG(int aCount, TextView[] aTvArray, TextView aTvAVG, ClearGLSurfaceView[] aStArray) {
         	tvAVG = aTvAVG; mCount = aCount; mTvArray = aTvArray; mStArray = aStArray;
         	mOurRunnable = new ourRunnable(mCount, mTvArray, tvAVG, mStArray);
         }
         
         @Override
         public void run() {
         	tvAVG.post(mOurRunnable);
         }
     }    
     
     private void initSurfaceViews()
     {
 		int maxid = mNumLayers;
         mStvArray = new ClearGLSurfaceView[mNumLayers];
 
 		TextView []tvArray = new TextView[maxid];
 		TextView tvAVG = (TextView)findViewById(R.id.textFPSAverage);
 		for (int i = 0; i < maxid; i++) {
 			int fvId = getFrameViewId(i);
 			FrameLayout fl = (FrameLayout)findViewById(fvId);
 			ClearGLSurfaceView stv = new ClearGLSurfaceView(this);
			stv.getHolder().setFormat(PixelFormat.TRANSLUCENT);
 			fl.addView(stv);
 			mStvArray[i] = stv;
 			tvArray[i] = (TextView)findViewById(getTextViewId(i));
 		}
 
         Timer t = new Timer();
         t.scheduleAtFixedRate(new UpdaterAVG(maxid, tvArray, tvAVG, mStvArray), 0, 1000);
     }
     
     public void onPause() {
     	super.onPause();
     	if (mStvArray == null) {
     		return;
     	}
     	for (int i = 0; i < mNumLayers; i++) {
     		if (mStvArray[i] != null)
     			mStvArray[i].onPause();
     	}
     }
     
     public void onResume() {
     	super.onResume();
     	if (mStvArray == null) {
     		return;
     	}
     	for (int i = 0; i < mNumLayers; i++) {
     		if (mStvArray[i] != null)
     			mStvArray[i].onResume();
     	}
     }
 }
