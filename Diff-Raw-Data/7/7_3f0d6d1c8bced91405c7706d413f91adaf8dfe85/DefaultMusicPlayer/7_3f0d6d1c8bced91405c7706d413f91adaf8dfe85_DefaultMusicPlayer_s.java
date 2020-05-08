 package sps.audio;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.audio.Music;
 
 public class DefaultMusicPlayer extends MusicPlayer {
     private final Music mainTheme;
 
     public DefaultMusicPlayer() {
        mainTheme = Gdx.audio.newMusic(Gdx.files.internal("assets/music/MainTheme.mp3"));
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
