 package online.radio;
 
 
 import java.io.IOException;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.EditText;
 import android.widget.ProgressBar;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.media.AudioManager;
 import android.media.MediaPlayer;
 import android.widget.MediaController;
 
 public class Odio extends Activity implements OnClickListener {
     /** Called when the activity is first created. */
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.main);
         
         Button b = (Button)this.findViewById(R.id.btn_confirm);
         b.setOnClickListener(this);
         
     }
     
     public void onClick(View v){
     	
     	EditText var_url = (EditText)this.findViewById(R.id.txt_URL);
     	TextView var_confirm = (TextView)this.findViewById(R.id.txt_confirm);
     	ProgressBar var_PB = (ProgressBar)this.findViewById(R.id.progbar_progress);
     	
     	
     	String URL = var_url.getText().toString(); 
     	
     	MediaPlayer var_player = new MediaPlayer();
     	var_player.setAudioStreamType(AudioManager.STREAM_MUSIC);
     	
     	//Getting data from the link 
     	
     	try {
 			var_player.setDataSource(URL);
		} catch (IllegalArgumentException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
     	
     	//Preparing the media player i.e "buffering"
     	
     	try {
 			var_player.prepare();
		} catch (IllegalStateException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
 			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
     	
     	var_player.start();
     	
     	String message = "Streaming radio from: \n\n"+URL;
     	
     	var_confirm.setText(message); 
     	
     	var_PB.setVisibility(0);
     	    	  	
     }
 }
