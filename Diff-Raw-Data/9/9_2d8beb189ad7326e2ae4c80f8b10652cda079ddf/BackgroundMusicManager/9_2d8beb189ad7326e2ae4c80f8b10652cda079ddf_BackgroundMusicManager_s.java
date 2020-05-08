 package starpunk.services;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.utils.Disposable;
 import javax.annotation.Nullable;
 
 /** a basic service for playing background music. One track can be playing at any one time. */
 public final class BackgroundMusicManager
   implements Disposable
 {
   @Nullable
   private MusicResource _resource;
   @Nullable
   private Music _music;
 
   private float _volume = 1f;
   private boolean _disabled;
 
   public void play( final MusicResource resource )
   {
     if( _resource == resource )
     {
       return;
     }
     stop();
 
     if( null != resource )
     {
       log( "Playing music: " + resource.getFilename() );
       _resource = resource;
       loadMusic();
     }
     else
     {
       _resource = null;
     }
   }
 
   private void loadMusic()
   {
     if( !_disabled && null != _resource )
     {
       final FileHandle musicFile = Gdx.files.internal( _resource.getFilename() );
       final Music music = Gdx.audio.newMusic( musicFile );
       music.setVolume( _volume );
       music.setLooping( true );
       music.play();
       _music = music;
     }
   }
 
   private void stop()
   {
     if( null != _music )
     {
       log( "Stopping current music" );
       _music.stop();
       _music.dispose();
       _music = null;
     }
     _resource = null;
   }
 
   /** Sets the music volume which must be inside the range [0,1]. */
   public void setVolume( final float volume )
   {
     if( volume < 0 || volume > 1f )
     {
       throw new IllegalArgumentException( "The volume must be inside the range: [0,1]" );
     }
     log( "Adjusting music volume to: " + volume );
     _volume = volume;
 
     if( null != _music )
     {
       _music.setVolume( volume );
     }
   }
 
   public void setDisabled( final boolean disabled )
   {
     log( disabled ? "Enabling background music" : "Disabling background music" );
     _disabled = disabled;
     if( disabled )
     {
       stop();
     }
     else
     {
       loadMusic();
     }
   }
 
   public void dispose()
   {
     log( "Disposing music manager" );
     stop();
   }
 
   private void log( final String message )
   {
    Gdx.app.log( BackgroundMusicManager.class.getSimpleName(), message );
   }
 }
