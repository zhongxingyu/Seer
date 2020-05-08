 package android.game.guessmynumber;
 
 import android.content.Context;
 import android.media.MediaPlayer;
 
 public class Music {
 
 	MediaPlayer mediaPlayer;
 	Context context;
 	
 	public Music(String what , Context context){
 		this.context = context;
 		if(what.equals("win")){
 			this.mediaPlayer = MediaPlayer.create(context, R.raw.win);
 		}
 		else if(what.equals("fail")){
 			this.mediaPlayer = MediaPlayer.create(context, R.raw.fail);
 		}
 		
 		else if(what.equals("clock")){
 			this.mediaPlayer = MediaPlayer.create(context, R.raw.clock);
 			this.mediaPlayer.setLooping(true);
 		}
 	}
 	
 	public void Stop(){
		if(this.mediaPlayer!=null && this.mediaPlayer.isPlaying()){
 			this.mediaPlayer.stop();
 			this.mediaPlayer.release();
 		}
 	}
 	
 	public void Start(){
 		this.mediaPlayer.start();
 	}
 }
