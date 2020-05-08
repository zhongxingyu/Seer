 package se.chalmers.dat255.sleepfighter.audio;
 
 import se.chalmers.dat255.sleepfighter.R;
 import se.chalmers.dat255.sleepfighter.model.audio.AudioConfig;
 import se.chalmers.dat255.sleepfighter.model.audio.AudioSource;
 import android.content.Context;
 
 /**
  * SilentAudioDriver is a silent driver that does nothing.
  *
  * @author Centril<twingoow@gmail.com> / Mazdak Farrokhzad.
  * @version 1.0
  * @since Sep 27, 2013
  */
 public class SilentAudioDriver implements AudioDriver {
 	private Context context;
 
 	public SilentAudioDriver() {
 	}
 
 	@Override
 	public void setSource( Context context, AudioSource source ) {
 		this.context = context;
 	}
 
 	public AudioSource getSource() {
 		return null;
 	}
 
 	@Override
 	public String printSourceName() {
		return context.getString( R.string.alarm_audiosource_summary_name_none );
 	}
 
 	@Override
 	public void play( AudioConfig config ) {
 	}
 
 	@Override
 	public boolean isPlaying() {
 		return false;
 	}
 
 	@Override
 	public void stop() {
 	}
 
 	@Override
 	public void toggle( AudioConfig config ) {
 	}
 }
