 package com.senselessweb.cradionative.radio;
 
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import android.app.Activity;
 import android.graphics.Color;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.MediaPlayer.OnErrorListener;
 import android.media.MediaPlayer.OnPreparedListener;
 import android.net.Uri;
import android.widget.ImageButton;
 import android.widget.TextView;
 import android.widget.Toast;
 
 import com.senselessweb.cradionative.R;
 import com.senselessweb.cradionative.radio.library.GenreService;
 import com.senselessweb.cradionative.radio.library.Item;
 import com.senselessweb.cradionative.radio.library.PresetsService;
 
 public class Radio implements OnPreparedListener, OnErrorListener
 {
 	
 	private final GenreService genreService = new GenreService();
 	
 	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
 	
 	private ScheduledFuture<?> currentGenreItemTask = null;
 	
 	private final PresetsService presetsService = new PresetsService();
 	
 	private final MediaPlayer mediaPlayer = new MediaPlayer();
 
 	private final Activity activity;
 	
 	
 	private Item currentItem = null;
 	
 	public Radio(final Activity activity)
 	{
 		this.activity = activity;
 		
 		this.mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
 		this.mediaPlayer.setOnPreparedListener(this);
 		this.mediaPlayer.setOnErrorListener(this);
 	}
 	
 	public void onResume()
 	{
 		if (this.currentItem != null)
 			this.getDisplay().setText(this.currentItem.getName());
 		
 		if (this.mediaPlayer.isPlaying())
			this.getTogglePlayButton().setImageResource(R.drawable.btn_stop);
 		else if (this.currentItem != null)
			this.getTogglePlayButton().setImageResource(R.drawable.btn_play);		
 	}
 	
 	public synchronized void togglePlayback()
 	{
 		if (this.mediaPlayer.isPlaying())
 		{
 			this.mediaPlayer.stop();
			this.getTogglePlayButton().setImageResource(R.drawable.btn_play);
 		}
 		
 		else if (this.currentItem != null)
 		{
 			this.play(this.currentItem);
			this.getTogglePlayButton().setImageResource(R.drawable.btn_stop);			
 		}
 	}	
 
 	public synchronized void playPreset(final int preset)
 	{
 		final Item item = this.presetsService.getPreset(preset); 
 		if (item != null) this.play(item);
 	}
 	
 	public synchronized void nextGenre()
 	{
 		this.genreService.nextGenre();
 		this.playGenreItemAsync();
 	}
 	
 	public synchronized void previousGenre()
 	{
 		this.genreService.previousGenre();
 		this.playGenreItemAsync();
 	}
 	
 	public synchronized void nextStation()
 	{
 		this.genreService.nextStation();
 		this.playGenreItemAsync();
 	}
 	
 	public synchronized void previousStation()
 	{
 		this.genreService.previousStation();
 		this.playGenreItemAsync();
 	}
 
 	public void playGenre(String genre)
 	{
 		this.genreService.setGenre(genre);
 		this.play(this.genreService.getCurrent());
 	}
 	
 	private void playGenreItemAsync()
 	{
 		final Item current = this.genreService.getCurrentIfAvailable();
 		this.getDisplay().setText(current != null ? current.getName() : this.genreService.getCurrentGenre());
 		this.getDisplay().setTextColor(Color.rgb(255, 165, 0));
 
 		if (this.currentGenreItemTask != null)
 			this.currentGenreItemTask.cancel(false);
 		
 		this.currentGenreItemTask = this.scheduledExecutorService.schedule(new Runnable() 
 		{
 			@Override
 			public void run()
 			{
 				Radio.this.play(Radio.this.genreService.getCurrent());
 			}
 		}, 2, TimeUnit.SECONDS);
 	}
 	
 	private void play(final Item item)
 	{
 		this.currentItem = item;
 		
 		try
 		{
 			final Uri myUri = Uri.parse(item.getUrl());
 			
 			if (this.mediaPlayer.isPlaying())
 				this.mediaPlayer.stop();
 			
 			this.activity.runOnUiThread(new Runnable() 
 			{
 				@Override
 				public void run()
 				{
 					Toast.makeText(Radio.this.activity, "Buffering " + myUri, Toast.LENGTH_LONG).show();
 					Radio.this.getDisplay().setText(item.getName());
 					Radio.this.getDisplay().setTextColor(Color.rgb(255, 165, 0));
 				}
 			});
 			
 			this.mediaPlayer.reset();
 			this.mediaPlayer.setDataSource(this.activity.getApplicationContext(), myUri);
 			this.mediaPlayer.prepareAsync();
 		} 
 		catch (final Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void onPrepared(final MediaPlayer mp)
 	{
 		mp.start();
 		this.activity.runOnUiThread(new Runnable() 
 		{
 			@Override
 			public void run()
 			{
 				Radio.this.getDisplay().setTextColor(Color.WHITE);
				Radio.this.getTogglePlayButton().setImageResource(R.drawable.btn_stop);			
 			}
 		});
 	}
 
 	public void stop()
 	{
 		if (this.mediaPlayer.isPlaying())
 			this.mediaPlayer.stop();
		this.getTogglePlayButton().setImageResource(R.drawable.btn_stop);
 	}
 	
 	@Override
 	public boolean onError(MediaPlayer mp, int what, int extra)
 	{
 		Toast.makeText(this.activity, "MediaPlayer error code: " + what + ", " + extra, Toast.LENGTH_LONG).show();
 		return false;
 	}
 	
 	private TextView getDisplay()
 	{
 		return (TextView) this.activity.findViewById(R.id.display);
 	}
 	
	private ImageButton getTogglePlayButton()
 	{
		return (ImageButton) this.activity.findViewById(R.id.buttonTogglePlay);		
 	}
 }
 
 
