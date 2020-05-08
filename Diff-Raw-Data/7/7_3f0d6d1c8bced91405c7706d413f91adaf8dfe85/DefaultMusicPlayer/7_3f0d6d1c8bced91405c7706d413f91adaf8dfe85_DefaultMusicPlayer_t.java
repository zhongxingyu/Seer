 package sps.audio;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.files.FileHandle;
import sps.core.Logger;
 
 public class DefaultMusicPlayer extends MusicPlayer {
     private final Music mainTheme;
 
     public DefaultMusicPlayer() {
        FileHandle mainThemeFh = new FileHandle("assets/music/MainTheme.mp3");
        Logger.info(mainThemeFh.file().getAbsolutePath());
        mainTheme = Gdx.audio.newMusic(mainThemeFh);
         mainTheme.setLooping(true);
     }
 
     @Override
     public void start() {
         mainTheme.play();
     }
 
 
     @Override
     public void stop() {
         mainTheme.stop();
     }
 }
