 package com.room.media;
 
 import com.room.Global;
 import com.room.Options;
 import com.room.R;
 import com.room.render.RDecalSystem;
 import com.room.render.RRenderer;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import android.content.Context;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.media.SoundPool;
 import android.util.Log;
 
 public class MSoundManager
 {
 	//TBD - put these in the options
 	public static final float MASTER_VOLUME = 1.0f;
 	public static final float MUSIC_VOLUME = 1.0f;
 	public static final float SOUNDEFFCTS_VOLUME = 1.0f;
 	
 	//SINGLETON!!
 	public static MSoundManager getInstance()
 	{
 		if(instance == null)
 		{
 			instance = new MSoundManager();
 		}
 		return instance;
 	}
 	
 	static class LocationSensitiveSound
 	{
 		int resourceID;
 		float x,y;
 		float innerRadiusSquared;
 		float outerRadiusSquared;
 		MediaPlayer mediaPlayer;
 		
 		LocationSensitiveSound(int resourceID, float x, float y, float innerRadius, float outerRadius)
 		{
 			this.resourceID = resourceID;
 			this.x = x;
 			this.y = y;
 			this.innerRadiusSquared = innerRadius * innerRadius;
 			this.outerRadiusSquared = outerRadius * outerRadius;
 			this.mediaPlayer = MediaPlayer.create(Global.mainActivity, resourceID);
 			this.mediaPlayer.setLooping(true);
 			
 			try{mediaPlayer.setVolume(0, 0);}
 			catch(Exception e){}
 			
 			mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
 			{				
 				@Override
 				public void onPrepared(MediaPlayer player)
 				{
 					player.start();
 				}
 			});
 		}
 		
 		public float getVolumeAt(float px, float py)
 		{
 			float dx = px-x;
 			float dy = py-y;
 			float distSquared = dx*dx+dy*dy;
 			float volume = 0.0f;
 			if(distSquared <= innerRadiusSquared)
 			{
 				volume = 1.0f;
 			}
 			else if(distSquared <= outerRadiusSquared)
 			{
 				volume = (outerRadiusSquared - distSquared) / (outerRadiusSquared - innerRadiusSquared);
 			}
 			else
 			{
 				volume = 0;
 			}			
 			
 			return volume * MASTER_VOLUME * SOUNDEFFCTS_VOLUME;
 		}
 		
 		public void setVolume(float leftVolume, float rightVolume)
 		{
 			try{mediaPlayer.setVolume(leftVolume, rightVolume);}
 			catch(Exception e){}
 		}
 		
 		public void mute()
 		{
 			try{mediaPlayer.setVolume(0,0);}
 			catch(Exception e){}
 		}
 		
 		public void stopAndRelease()
 		{
			mediaPlayer.stop();
			mediaPlayer.release();
 		}		
 	}
 	
    public void init()
    {
 	   soundEffectsPool = new SoundPool( 20, AudioManager.STREAM_MUSIC, 0);
 	   if ( soundEffectsMap == null ) soundEffectsMap = new HashMap<Integer, Integer>();	   
 	   soundEffectsMap.put(R.raw.swords,soundEffectsPool.load(Global.mainActivity, R.raw.swords, 1));
 	   soundEffectsMap.put(R.raw.footstep01,soundEffectsPool.load(Global.mainActivity, R.raw.footstep01, 1));
 	   soundEffectsMap.put(R.raw.footstep02,soundEffectsPool.load(Global.mainActivity, R.raw.footstep02, 1));
 	   soundEffectsMap.put(R.raw.footstep03,soundEffectsPool.load(Global.mainActivity, R.raw.footstep03, 1));
 	   soundEffectsMap.put(R.raw.footstep04,soundEffectsPool.load(Global.mainActivity, R.raw.footstep04, 1));
 	   
 	   if ( locationSensitiveSounds == null ) locationSensitiveSounds = new ArrayList<LocationSensitiveSound>();
 	   
 	   locationSensitiveSounds.add(
 			   new LocationSensitiveSound
 			   (
 					   R.raw.water_drop,
 					   -18.6f,-56.6f,		//x,y
 					   10,40)	//inner, outer
 			   );
 	   
 	   locationSensitiveSounds.add(
 			   new LocationSensitiveSound
 			   (
 					   R.raw.doorknock,
 					   -20,40,		//x,y
 					   10,20)	//inner, outer
 			   );
 	   
 	   locationSensitiveSounds.add(
 			   new LocationSensitiveSound
 			   (
 					   R.raw.deadman,
 					   12,23,		//x,y
 					   0,15)	//inner, outer
 			   );	   
    }	
 	
    public void playSoundEffect(int resource)
    {
 	   if ( Options.isSoundEffectsEnabled() && soundEffectsMap != null && soundEffectsMap.containsKey(resource) )
 	   {
 		   float volume = MASTER_VOLUME * SOUNDEFFCTS_VOLUME;
            soundEffectsPool.play(soundEffectsMap.get(resource), volume, volume, 1, 0, 1f);           
        }
    }
    
    public void playTimedSound()
    {
 	   //tbd temporary disable,
 	   // 1) SoundPools are too small to work with this
 	   // 2) may have to sync with random events
 	   
 	   /*if (Options.isMusicEnabled()) {
 		   mTimer = new Timer();
 		   it = (Iterator) mSoundHashMap_BG.entrySet().iterator();
 		   mTimerTask = new TimerTask() {
 			   public void run() {
 				   if (!it.hasNext()){
 					   it = (Iterator) mSoundHashMap_BG.entrySet().iterator();
 				   }
 				   playSound((Integer)((HashMap.Entry)it.next()).getKey());
 			   }
 		   };
 		   mTimer.schedule(mTimerTask, 15000, 15000);
 	  }*/
    }
 
 	public void playMusic(int resource)
 	{				
 		//stop the music if music is disabled
 		if (!Options.isMusicEnabled())
 		{
 			if ( musicMediaPlayer != null )
 			{
 				stopAndReleaseMusic();
 			}	
 		}
 		
 		//dont play again if playing already
 		if(resource == currentMusicResourceID)
 			return;		
 		
 		//play the music if it is enabled
 		if (Options.isMusicEnabled())
 		{
 			if ( musicMediaPlayer != null )
 			{
 				stopAndReleaseMusic();
 			}
 			
 			musicMediaPlayer = MediaPlayer.create(Global.mainActivity, resource);
 			float volume = MASTER_VOLUME * MUSIC_VOLUME;
 			
 			try{musicMediaPlayer.setVolume(volume, volume);}
 			catch(Exception e){}
 			
 			musicMediaPlayer.setLooping(true);
 					   
 			musicMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()
 			{				
 				@Override
 				public void onPrepared(MediaPlayer player)
 				{
 					player.start();
 				}
 			});		
 			
 			currentMusicResourceID = resource;
 		}
 	}
    
 	public void stopMusic()
 	{				
 		//stop the music if music is disabled
 		if (!Options.isMusicEnabled())
 		{
 			if ( musicMediaPlayer != null )
 			{
 				stopAndReleaseMusic();
 			}	
 		}
 	}
 	
 	public void stopAndReleaseMusic()
 	{
 		if (musicMediaPlayer != null) {
 			musicMediaPlayer.stop();
    	       	musicMediaPlayer.release();
    	       	musicMediaPlayer = null;
    	       	currentMusicResourceID = -1;
 		}
 	}
    
    public boolean isMusicPlaying()
    {
 	   if ( musicMediaPlayer != null )
 	   {
 	       return musicMediaPlayer.isPlaying();
 	   }
 	   return false;
    }
    
    public void updateLocation(float x, float y)
    {
 	   if ( Options.isSoundEffectsEnabled() )
 	   {
 		   for(LocationSensitiveSound sound:locationSensitiveSounds)
 		   {
 			   float volume = sound.getVolumeAt(x, y);
 			   sound.setVolume(volume, volume);
 		   }
 	   }
    }
    
 
    public void muteLocationSensitiveSounds()
    {
 	   if ( Options.isSoundEffectsEnabled() )
 	   {
 		   for(LocationSensitiveSound sound:locationSensitiveSounds)
 		   {
 			   sound.mute();
 		   }		   
 	   }	
    }
    
    public void stopAllSounds()
    {
 	   stopAndReleaseMusic();	   
 	   
 	   for(LocationSensitiveSound sound:locationSensitiveSounds)
 	   {
 		   sound.stopAndRelease();
 	   }	
 	   
 	   Iterator effectsIterator = (Iterator) soundEffectsMap.entrySet().iterator();
 	   
 	   while (effectsIterator.hasNext())
 	   {   
 		   soundEffectsPool.stop((Integer)((HashMap.Entry)effectsIterator.next()).getKey());
 	   }
 	   	   
    } 
    
 	private MSoundManager()   
 	{
 		musicMediaPlayer = null;
 		soundEffectsPool = null;
 		soundEffectsMap = null;
 		locationSensitiveSounds = null;
 	}
    
    private MediaPlayer musicMediaPlayer = null;
    private int currentMusicResourceID;
 
    private SoundPool soundEffectsPool = null; //sound effects
    private HashMap<Integer, Integer> soundEffectsMap = null;
 
    private ArrayList<LocationSensitiveSound> locationSensitiveSounds = null;
    
    private static MSoundManager instance;   
 }
