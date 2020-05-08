 package com.android.icecave.gui;
 
 import android.app.Activity;
 import android.content.ComponentName;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.os.IBinder;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.Button;
 import android.widget.RadioButton;
 import android.widget.RadioGroup;
 import android.widget.RadioGroup.OnCheckedChangeListener;
 import com.android.icecave.R;
 import com.android.icecave.general.Consts;
 import com.android.icecave.general.EDifficulty;
 import com.android.icecave.general.MusicService;
 
 public class MainActivity extends Activity
 {
 	SharedPreferences mShared;
 	private boolean mIsBound = false;
 	private MusicService mServ;
 	private ServiceConnection mScon;
 
 	private void doBindService()
 	{
 		bindService(new Intent(this, MusicService.class), mScon, Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 
 	private void doUnbindService()
 	{
 		if (mIsBound)
 		{
 			unbindService(mScon);
 			mIsBound = false;
 		}
 	}
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_main);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		// Hide the Status Bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		final int DEFAULT_LEVEL = 0;
 
 		mShared = getSharedPreferences(Consts.PREFS_FILE_TAG, 0);
 
 		Button optionsActivity = (Button) findViewById(R.id.options_button);
 		Button gameActivity = (Button) findViewById(R.id.game_button);
 		RadioGroup levelSelect = (RadioGroup) findViewById(R.id.levelSelect);
 
 		// Load levels dynamically from EDifficulty class
 		loadLevelsToRadioGroup(levelSelect);
 
 		// Load level selection from prefs if exists
 		levelSelect.check(levelSelect.getChildAt(mShared.getInt(Consts.LEVEL_SELECT_TAG, DEFAULT_LEVEL))
 				.getId());
 
 		optionsActivity.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				startActivity(new Intent(v.getContext(), OptionsActivity.class));
 			}
 		});
 
 		gameActivity.setOnClickListener(new OnClickListener()
 		{
 			@Override
 			public void onClick(View v)
 			{
 				Intent i = new Intent(v.getContext(), GameActivity.class);
 
 				// Load selection from prefs if exists
 				i.putExtra(Consts.LEVEL_SELECT_TAG, mShared.getInt(Consts.LEVEL_SELECT_TAG, DEFAULT_LEVEL));
 				i.putExtra(Consts.SELECT_BOARD_SIZE_X,
 						mShared.getInt(Consts.SELECT_BOARD_SIZE_X, Consts.DEFAULT_BOARD_SIZE_X));
 				i.putExtra(Consts.SELECT_BOARD_SIZE_Y,
 						mShared.getInt(Consts.SELECT_BOARD_SIZE_Y, Consts.DEFAULT_BOARD_SIZE_Y));
 				startActivity(i);
 			}
 		});
 
 		levelSelect.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{
 
 			@Override
 			public void onCheckedChanged(RadioGroup group, int checkedId)
 			{
 				// Save level to prefs
 				mShared.edit()
 						.putInt(Consts.LEVEL_SELECT_TAG, group.indexOfChild(group.findViewById(checkedId)))
 						.commit();
 			}
 		});
 
 		mScon = new ServiceConnection()
 		{
 
 			public void onServiceConnected(ComponentName name, IBinder binder)
 			{
 				mServ = ((MusicService.ServiceBinder) binder).getService();
 				
 				// Initialize music for the first time when starting this activity
 				initMusic();
 			}
 
 			public void onServiceDisconnected(ComponentName name)
 			{
 				mServ = null;
 			}
 		};
 
 		// Bind music service to activity
 		doBindService();
 		Intent music = new Intent();
 		music.setClass(this, MusicService.class);
 		startService(music);
 	}
 
 	private void initMusic()
 	{
 		// Initialize music (or pause it) according to saved selection
 		if (mShared.getBoolean(Consts.MUSIC_MUTE_FLAG, false))
 		{
 			mServ.pauseMusic();
 		} else
 		{
 			mServ.startMusic();
 		}
 	}
 
 	private void loadLevelsToRadioGroup(RadioGroup levelSelect)
 	{
 		int numOfLevels = EDifficulty.values().length;
 
 		// Go over levels and add them
 		for (int i = 0; i < numOfLevels; i++)
 		{
 			RadioButton newButton = new RadioButton(this);
 			newButton.setText(EDifficulty.values()[i].name()); // An alternative to this is to set another variable (string id) in the enum
 			levelSelect.addView(newButton);
 		}
 	}
 
 	@Override
 	protected void onDestroy()
 	{
 		// Stop music
 		if (mServ != null)
 		{
 			mServ.pauseMusic();
 		}
 		
 		// Unbind activity from service
 		doUnbindService();
 		super.onDestroy();
 	}
 	
 	@Override
 	protected void onResume()
 	{
 		// Enable/disable music when resuming this activity
 		if (mServ != null)
 		{
 			initMusic();
 		}
 		
 		super.onResume();
 	}
 }
