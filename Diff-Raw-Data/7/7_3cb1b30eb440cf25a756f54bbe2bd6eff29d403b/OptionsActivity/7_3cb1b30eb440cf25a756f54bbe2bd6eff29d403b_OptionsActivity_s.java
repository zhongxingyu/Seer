 package com.android.icecave.gui;
 
 import android.os.IBinder;
 
 import android.content.ComponentName;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.ServiceConnection;
 import android.content.SharedPreferences;
 import android.content.pm.ActivityInfo;
 import android.media.AudioManager;
 import android.os.Bundle;
 import android.view.View;
 import android.view.Window;
 import android.view.WindowManager;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemSelectedListener;
 import android.widget.ArrayAdapter;
 import android.widget.CheckBox;
 import android.widget.CompoundButton;
 import android.widget.CompoundButton.OnCheckedChangeListener;
 import android.widget.Spinner;
 import com.android.icecave.R;
 import com.android.icecave.general.Consts;
 import com.android.icecave.general.MusicService;
 import com.android.icecave.general.PlayerThemes;
 import com.android.icecave.general.TileThemes;
 
 public class OptionsActivity extends Activity
 {
 	private boolean mIsBound = false;
 	private MusicService mServ;
 	private ServiceConnection mScon;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState)
 	{
 		super.onCreate(savedInstanceState);
 		requestWindowFeature(Window.FEATURE_NO_TITLE);
 		setContentView(R.layout.activity_options);
 		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
 		setVolumeControlStream(AudioManager.STREAM_MUSIC);
 
 		// Hide the Status Bar
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
 				WindowManager.LayoutParams.FLAG_FULLSCREEN);
 
 		Spinner backgroundThemeSelect = (Spinner) findViewById(R.id.selectBackgroundTheme);
 		Spinner playerThemeSelect = (Spinner) findViewById(R.id.selectPlayerTheme);
 		final CheckBox muteMusic = (CheckBox) findViewById(R.id.muteMusic);
 
 		final PlayerThemes playerThemes = new PlayerThemes();
 		final TileThemes tileThemes = new TileThemes();
 		final SharedPreferences shared = getSharedPreferences(Consts.PREFS_FILE_TAG, 0);
 
 		ArrayAdapter<String> tileAdapter =
 				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
 		tileAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		tileAdapter.addAll(tileThemes.getThemeNames());
 		backgroundThemeSelect.setAdapter(tileAdapter);
 
 		ArrayAdapter<String> playerAdapter =
 				new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
 		playerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
 		playerAdapter.addAll(playerThemes.getThemeNames());
 		playerThemeSelect.setAdapter(playerAdapter);
 
 		// Set initial values in spinners
 		backgroundThemeSelect.setSelection(tileThemes.getTilePositionById(shared.getInt(Consts.THEME_SELECT_TAG,
 				tileThemes.getThemeId(0))));
 		playerThemeSelect.setSelection(playerThemes.getTilePositionById(shared.getInt(Consts.PLAYER_SELECT_TAG,
 				playerThemes.getThemeId(0))));
 
 		backgroundThemeSelect.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			boolean isInitialized = false;
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 			{
 				// Avoid automatic selected of first item at creation
 				if (isInitialized)
 				{
 					// Save selected tile theme
 					shared.edit().putInt(Consts.THEME_SELECT_TAG, tileThemes.getThemeId(pos)).commit();
 				}
 
 				isInitialized = true;
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 			}
 		});
 
 		playerThemeSelect.setOnItemSelectedListener(new OnItemSelectedListener()
 		{
 			boolean isInitialized = false;
 
 			@Override
 			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id)
 			{
 				// Avoid automatic selected of first item at creation
 				if (isInitialized)
 				{
 					// Save selected player theme
 					shared.edit().putInt(Consts.PLAYER_SELECT_TAG, playerThemes.getThemeId(pos)).commit();
 				}
 
 				isInitialized = true;
 			}
 
 			@Override
 			public void onNothingSelected(AdapterView<?> arg0)
 			{
 			}
 		});
 
 		mScon = new ServiceConnection()
 		{
 
 			public void onServiceConnected(ComponentName name, IBinder binder)
 			{
 				mServ = ((MusicService.ServiceBinder) binder).getService();
 				
 				// Set music mode for first time. Can't count on checkbox because flag may not "change" from its initialized value
 				setMusicMode(shared.getBoolean(Consts.MUSIC_MUTE_FLAG, false));
 
 				// Check according to saved data
 				muteMusic.setChecked(shared.getBoolean(Consts.MUSIC_MUTE_FLAG, false));
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
 
 		muteMusic.setOnCheckedChangeListener(new OnCheckedChangeListener()
 		{			
 			@Override
 			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
 			{
 					// Save selection
 					shared.edit().putBoolean(Consts.MUSIC_MUTE_FLAG, isChecked).commit();
 
 					// Play/pause music
 					setMusicMode(isChecked);
 			}
 		});
 	}
 	
 	private void setMusicMode(boolean muteFlag) {
 		if (muteFlag)
 		{
 			mServ.pauseMusic();
 		} else
 		{
 			mServ.startMusic();
 		}
 	}
 
 	private void doBindService()
 	{
 		bindService(new Intent(this, MusicService.class), mScon, Context.BIND_AUTO_CREATE);
 		mIsBound = true;
 	}
 
 	private void doUnbindService()
 	{
 		// Unbind activity from service
 		if (mIsBound)
 		{
 			unbindService(mScon);
 			mIsBound = false;
 		}
 	}
 	
 	@Override
 	protected void onPause()
 	{
 		// Stop music
 		if (mServ != null)
 		{
 			mServ.pauseMusic();
 		}
 		
 		super.onPause();
 	}
 
 	@Override
 	protected void onDestroy()
 	{		
 		doUnbindService();
 		super.onDestroy();
 	}
 }
