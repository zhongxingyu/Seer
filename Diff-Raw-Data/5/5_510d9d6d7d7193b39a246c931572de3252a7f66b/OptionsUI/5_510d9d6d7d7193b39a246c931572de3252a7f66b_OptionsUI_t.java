 package com.exitjump;
 
 import java.util.HashMap;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.content.DialogInterface;
 import android.database.Cursor;
 import android.os.Looper;
 import android.util.Log;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.SeekBar;
 import android.widget.Toast;
 import android.widget.SeekBar.OnSeekBarChangeListener;
 
 import com.android.angle.AngleActivity;
 import com.android.angle.AngleObject;
 import com.android.angle.AnglePhysicObject;
 import com.android.angle.AngleSpriteLayout;
 import com.android.angle.AngleString;
 import com.android.angle.AngleUI;
 import com.exitjump.Ball.Color;
 import com.exitjump.Bonus.TypeBonus;
 import com.exitjump.R;
 
 public class OptionsUI  extends AngleUI
 {
 
 	protected HashMap<Color,HashMap<TypeBonus,AngleSpriteLayout[]>> mBallLayouts;
 	protected Ball mBall;
 
 	private MyPhysicsEngine mPhysics;
 	private AngleObject ogMenuTexts;
 	private boolean dbEmpty;
 	private AngleString strSensibility, strVolume, strVibrations, strResetScores, strExit;
 
 	protected int mSensibility;
 	protected int mVolume;
 	protected int mVibrations;
 	protected String mName;
 
 	public OptionsUI(AngleActivity activity, Background mBackGround)
 	{
 		super(activity);
 		Log.i("OptionsUI", "constructor debut");
 		if(mBackGround != null)
 			addObject(mBackGround);
 		DBOptions db = new DBOptions(mActivity);
 		db.open();
 		Cursor c = db.getOptions();
 		if (c.getCount() >= 1)
     	{
 			dbEmpty = false;
     		c.moveToFirst();
     		mSensibility = c.getInt(1);
     		mVolume = c.getInt(2);
     		mVibrations = c.getInt(3);
     	}
     	else // par default
     	{
     		dbEmpty = true;
     		mSensibility = 50;
     		mVolume = 100;
     		mVibrations = 1;
     	}
 		db.close();
 		
 		ogMenuTexts = new AngleObject();
 		
 		addObject(ogMenuTexts);
 
 		strSensibility = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "Set Sensibility", 160, 70, AngleString.aCenter));
 		strVolume = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "Set Volume", 160, 140, AngleString.aCenter));
 		String sVibrations = "Put vibrations ON";
 		if (mVibrations == 1)
 			sVibrations = "Put vibrations OFF";
 		strVibrations = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, sVibrations, 160, 210, AngleString.aCenter));
 		strResetScores = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "Reset Scores", 160, 280, AngleString.aCenter));
 		strExit = (AngleString) ogMenuTexts.addObject(new AngleString(((MainActivity)mActivity).fntGlobal, "Back home", 160, 390, AngleString.aCenter));
 		Log.i("OptionsUI", "constructor fin");
 	}
 
 	@Override
 	public boolean onTouchEvent(MotionEvent event)
 	{
 		mBall.mVelocity.mX = (event.getX()-GameUI.WIDTH/2)*((MainActivity)mActivity).mOptions.mSensibility/25;
 		
 		if (event.getAction() == MotionEvent.ACTION_DOWN)
 		{
 			float eX = event.getX();
 			float eY = event.getY();
 
 			if (strSensibility.test(eX, eY))
 				setSensibility();
 			else if (strVolume.test(eX, eY))
 				setVolume();
 			else if (strVibrations.test(eX, eY))
 				setVibrations();
 			else if (strResetScores.test(eX, eY))
 				askResetScores();
 			else if (strExit.test(eX, eY))
 				((MainActivity) mActivity).setUI(((MainActivity) mActivity).mMenu);
 
 			return true;
 		}
 		return false;
 	}
 
 	private void resetScores()
 	{
 		DBScores db = new DBScores(mActivity);
 		db.open();
 		if (db.reset())
 			Toast.makeText(mActivity, "Score reset", Toast.LENGTH_SHORT).show();
 		else
 			Toast.makeText(mActivity, "Une erreur est survenue", Toast.LENGTH_SHORT).show();
 		db.close();
 	}
 	
 	private void setSensibility()
 	{
 		new Thread() 
 		{
 			@Override 
 			public void run() 
 			{
 				Looper.prepare();
 				askParameter(1);
 				Looper.loop();
 			}
 		}.start();
 	}
 	
 	private void setVolume()
 	{
 		new Thread() 
 		{
 			@Override 
 			public void run() 
 			{
 				Looper.prepare();
 				askParameter(2);
 				Looper.loop();
 			}
 		}.start();
 	}
 	
 	public void setVibrations()
 	{
 		if (mVibrations == 1)
 		{
 			strVibrations.set("Put vibrations ON");
 			mVibrations = 0;
 		}
 		else
 		{
 			strVibrations.set("Put vibrations OFF");
 			mVibrations = 1;
 		}
 	}
 	
 	@Override
 	public void onActivate()
 	{
 		Log.i("OptionUI", "onActivate debut");
 		super.onActivate();
 		DBOptions db = new DBOptions(mActivity);
 		db.open();
 		Cursor c = db.getOptions();
 		if (c.getCount() >= 1)
     	{
 			dbEmpty = false;
     		c.moveToFirst();
     		mSensibility = c.getInt(1);
     		mVolume = c.getInt(2);
     		mVibrations = c.getInt(3);
     	}
     	else // par default
     	{
     		dbEmpty = true;
     		mSensibility = 50;
     		mVolume = 100;
     		mVibrations = 1;
     	}
 		db.close();
 		init();
 		Log.i("OptionUI", "onActivate fin");
 	}
 	
 	public void init()
 	{
 		mPhysics = ((MainActivity)mActivity).mGame.mPhysics;
 		addObject(mPhysics);
 		mBallLayouts = ((MainActivity)mActivity).mGame.mBallLayouts;
 		mBall = ((MainActivity)mActivity).mGame.mBall;
 		mPhysics.addObject(mBall);
 		
		((MainActivity)mActivity).mGame.mTypeBonus = TypeBonus.NONE;
		
		
		mBall.mPosition.set(50,400);
 		mBall.jump();
 
 		// ajoute une plateforme en bas qui prend toute la place pour le debut
 		LifePlateforme mLife = new LifePlateforme(null);
 		mPhysics.addObject(mLife); // Down wall
 		mLife.setLife(-1);
 	}
 	
 	public void askResetScores()
 	{
 		new Thread() 
 		{
 			@Override 
 			public void run() 
 			{
 				Looper.prepare();
 				AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
 				builder.setMessage(" reset scores ? ")
 				       .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   dialog.cancel();
 				           }
 				       })
 				       .setPositiveButton("Reset", new DialogInterface.OnClickListener() {
 				           public void onClick(DialogInterface dialog, int id) {
 				        	   resetScores();
 				           }
 				       });
 				AlertDialog alert = builder.create();
 				alert.show();
 				Looper.loop();
 			}
 		}.start();
 	}
 	
 	public void askParameter(int type)
 	{			
 		Dialog dialog = new Dialog(mActivity);
         dialog.setContentView(R.layout.horizontalslider);
         
         SeekBar mySeekBar=(SeekBar) dialog.findViewById(R.id.options_slider);
         
         if(type == 1) {
         	dialog.setTitle("Sensibilité :");
             mySeekBar.setProgress(mSensibility);
         } else if(type == 2) {
         	dialog.setTitle("Volume :");
             mySeekBar.setProgress(mVolume);
         }
 
         mySeekBar.setOnSeekBarChangeListener(new MySeekBarListener(type));
         
         Button buttonOK = (Button) dialog.findViewById(R.id.options_ok);        
         buttonOK.setOnClickListener(new OKListener(dialog));
         
         dialog.show();
 	}
 	
 	protected class OKListener implements OnClickListener {	 
         private Dialog dialog;
         public OKListener(Dialog dialog) {
                 this.dialog = dialog;
         }
 
         public void onClick(View v) {
         		dialog.dismiss(); 
         }
 	}
 
 	protected class MySeekBarListener implements OnSeekBarChangeListener {
 		private int mType;
 
 		public MySeekBarListener(int type) {
 			mType = type;
 		}
 		
 		public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
 			if(mType==1)
 				mSensibility = arg1;
 			else if(mType==2)
 				mVolume = arg1;
 		}
 
 		public void onStartTrackingTouch(SeekBar arg0) {
 			
 		}
 
 		public void onStopTrackingTouch(SeekBar arg0) {
 			
 		}
 	}
 	
 	@Override
 	public void onDeactivate()
 	{
 		Log.i("OptionUI", "onDeactivate debut");
 		// sauvegarde des options
 		DBOptions db = new DBOptions(mActivity);
 		db.open();
 		if (dbEmpty)
 		{
 			db.insert(mSensibility,mVolume,mVibrations,"anonyme");
 			dbEmpty = false;
 		}
 		else
 			db.replace(1, mSensibility,mVolume,mVibrations,"anonyme");
 		db.close();
 		// suppression du mur en bas
 		for (int i=1; i<20; i++)
 		{
 			if (mPhysics.childAt(i) instanceof AnglePhysicObject)
 			{
 				mPhysics.removeObject(i);
 			}
 		}
 		Log.i("OptionUI", "onDeactivate fin");
 	}
 	
 }
