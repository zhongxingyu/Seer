 package com.turlutu;
 
 
 
 
 import android.app.AlertDialog;
 import android.app.ProgressDialog;
 import android.content.Context;
 import android.content.DialogInterface;
 import android.graphics.Typeface;
 import android.hardware.Sensor;
 import android.hardware.SensorEvent;
 import android.hardware.SensorEventListener;
 import android.hardware.SensorManager;
 import android.os.Bundle;
 import android.os.Looper;
 import android.os.Vibrator;
 import android.util.Log;
 import android.view.KeyEvent;
 import android.widget.FrameLayout;
 
 import com.android.angle.AngleActivity;
 import com.android.angle.AngleFont;
 import com.android.angle.AngleSpriteLayout;
 import com.android.angle.FPSCounter;
 import com.turlutu.Bonus.TypeBonus;
 
 
 public class MainActivity extends AngleActivity
 {
 	protected GameUI mGame;
 	protected MenuUI mMenu;
 	protected ScoresUI mScores;
 	protected OptionsUI mOptions;
 	protected InstructionsUI mInstructions;
 	protected OnLineScoresUI mScoresOnLine;
 	protected GameOverUI mGameOver;
 	private boolean loaded = false;
 	protected FrameLayout mMainLayout;
 	protected int mSensibility;
 	protected MainActivity mActivity;
 	protected ProgressDialog  dialog;
 	protected AngleFont fntGlobal, fntGlobal1;
 	protected Vibrator mVibrator;
 	private final SensorEventListener mListener = new SensorEventListener() 
 	{
 		//@Override
 		public void onAccuracyChanged(Sensor sensor, int accuracy)
 		{
 		}
 		
 		// @Override
 		public void onSensorChanged(SensorEvent event)
 		{
 			if (loaded & event.sensor.getType()==Sensor.TYPE_ACCELEROMETER)
 			{
 				if(mOptions.mBall != null)
 					mOptions.mBall.mVelocity.mX = (-mOptions.mSensibility*2*event.values[0]);
 				if (mGame.mTypeBonus == TypeBonus.CHANGEPHYSICS)
					mGame.mBall.mVelocity.mX = (mOptions.mSensibility*2*event.values[0]);
				else
					mGame.mBall.mVelocity.mX = (-mOptions.mSensibility*2*event.values[0]);
 			}
 		}
 	};
 	private SensorManager mSensorManager; 	
 
 	   
 	 @Override
 	    public boolean onKeyDown(int keyCode, KeyEvent event) 
 	    {
 	               
 	        if (keyCode == KeyEvent.KEYCODE_BACK) {
 				new Thread() 
 				{
 					@Override 
 					public void run() 
 					{
 						Looper.prepare();
 						AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
 						builder.setMessage("Do you really want to exit ?")
 						       .setCancelable(false)
 						       .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
 						           public void onClick(DialogInterface dialog, int id) {
 						        	  finish();
 						           }})
 							    .setNeutralButton("No", new DialogInterface.OnClickListener() {
 							           public void onClick(DialogInterface dialog, int id) {
 							        	   onResume();
 							           }
 						       });
 						AlertDialog alert = builder.create();
 						alert.show();
 						Looper.loop();
 					}
 				}.start();
 				onPause();
 	        	return true;
 	        }
 	           return false;
 	     }
 
 	
 	@Override
 	public void onCreate(Bundle savedInstanceState)
 	{
 		Log.i("MainActivity", "START");
 		
 		mActivity = this;
 		super.onCreate(savedInstanceState);
 		
 		new Thread() 
 		{
 			@Override 
 			public void run() 
 			{
 				Looper.prepare();
 		        dialog = ProgressDialog.show(mActivity, "", 
                         "Chargement en cours, veuillez patienter...", true);
 				Looper.loop();
 			}
 		}.start();
 		
 		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 
 		mVibrator =(Vibrator)getSystemService(Context.VIBRATOR_SERVICE);   
 		
         // a commenté dans la version finale (pour voir la fluidité du jeu)      
 		mGLSurfaceView.addObject(new FPSCounter());
 		
 		fntGlobal = new AngleFont(this.mGLSurfaceView, 25, Typeface.createFromAsset(this.getAssets(),"cafe.ttf"), 222, 0, 0, 30, 30, 30, 255);
 		// Text of instructions
 		fntGlobal1 = new AngleFont(this.mGLSurfaceView, 17, Typeface.createFromAsset(this.getAssets(),"cafe.ttf"), 222, 0, 0, 30, 30, 30, 255);
 		mMainLayout=new FrameLayout(this);
 		mMainLayout.addView(mGLSurfaceView);
 		setContentView(mMainLayout);
 
 		new Thread() 
 		{
 			@Override 
 			public void run() 
 			{
 				load();
 			}
 		}.start();
 
 		
 		Log.i("MainActivity", "FIN");
 	}
 	
 	public void load()
 	{
 		Log.i("MainActivity", "Load() start");
 		
 		AngleSpriteLayout mLayoutOptions = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.options, 0, 0, 320, 480);
 		AngleSpriteLayout mLayoutGame = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.background_jeu, 0, 0, 320, 480);
 		AngleSpriteLayout mLayoutInstruction1 = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.instruction1, 0, 0, 320, 480);
 		AngleSpriteLayout mLayoutInstruction2 = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.instruction2, 0, 0, 320, 480);
 		AngleSpriteLayout mLayoutMenu = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.background_accueil, 0, 0, 320, 480);
 		AngleSpriteLayout mLayoutScore = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.score, 0, 0, 320, 480);
 		AngleSpriteLayout mLayoutOnLineScore = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.score_online, 0, 0, 320, 480);
 		AngleSpriteLayout mLayoutGameOver = new AngleSpriteLayout(mGLSurfaceView,320,480,com.turlutu.R.drawable.game_over, 0, 0, 320, 480);
 		
 		
 		Background mBackgroundOptions = null;
 		Background mBackgroundGame = null;
 		Background mBackgroundInstruction1 = null;
 		Background mBackgroundInstruction2 = null;
 		Background mBackgroundMenu = null;
 		Background mBackgroundScore = null;
 		Background mBackgroundOnLineScore = null;
 		Background mBackgroundGameOver = null;
 		// TODO RELEASE a decommenter pour avoir des background dans les differentes UI (autre que le menu)
 		mBackgroundOptions= new Background(mLayoutOptions);
 		mBackgroundGame = new Background(mLayoutGame);
 		mBackgroundMenu = new Background(mLayoutMenu);
 		mBackgroundScore = new Background(mLayoutScore);
 		mBackgroundOnLineScore = new Background(mLayoutOnLineScore);
 		mBackgroundGameOver = new Background(mLayoutGameOver);
 		mBackgroundInstruction1 = new Background(mLayoutInstruction1);
 		mBackgroundInstruction2 = new Background(mLayoutInstruction2);
 		
 		
 		
 		mOptions=new OptionsUI(this,mBackgroundOptions);
 		mInstructions= new InstructionsUI(this,mBackgroundInstruction1);
 		mScores=new ScoresUI(this,mBackgroundScore);
 		mScoresOnLine = new OnLineScoresUI(this,mBackgroundOnLineScore);
 		mGame=new GameUI(this,mBackgroundGame);
 		mGameOver = new GameOverUI(this,mBackgroundGameOver);
 		mGame.setGravity(0f,10f);
 		mMenu=new MenuUI(this,mBackgroundMenu);
 		
 		
 		setUI(mMenu);
 		Log.i("MainActivity", "Load() fin");
 		loaded = true;
 	}
 
 	//Overload onPause and onResume to enable and disable the accelerometer
 	//Sobrecargamos onPause y onResume para activar y desactivar el aceler�metro
 	@Override
 	protected void onPause()
 	{
       mSensorManager.unregisterListener(mListener); 
       
       super.onPause();
 	}
 
 
 	@Override
 	protected void onResume()
 	{
       mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_FASTEST); 		
 
 		super.onResume();
 	}
 	
 }
