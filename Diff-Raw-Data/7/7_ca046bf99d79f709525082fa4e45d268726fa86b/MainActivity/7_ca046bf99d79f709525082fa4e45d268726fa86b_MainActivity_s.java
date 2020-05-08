 package br.eng.mosaic.pigeon.client.gui.menu;
 
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.view.View;
 import android.view.View.OnClickListener;
 import br.eng.mosaic.pigeon.client.gameplay.Stage;
 import br.eng.mosaic.pigeon.client.gameplay.Stage1;
 import br.eng.mosaic.pigeon.client.infra.facebook.LoginFacebook;
 import br.eng.mosaic.pigeon.client.R;
 
 public class MainActivity extends Activity {
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.main);
 
 		findViewById(R.id.start_game).setOnClickListener(
 				new OnClickListener() {
 					public void onClick(View v) {
 						startGame();
 					}
 				});
 
 		findViewById(R.id.top_five).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showTopFive();
 			}
 		});
 		
 		findViewById(R.id.high_score).setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				showHighScore();
 			}
 		});
 	}
 	
 	private void startGame() {
 		//call the dialog to set a message
		this.showDialog(Stage.DIALOG_CHOOSE_MESSAGE);
 		
 		Intent i = new Intent(this, Stage1.class);
 		startActivity(i);
 	}
 	
 	private void showTopFive() {
 		
 		Intent i = new Intent(this, TopFiveActivity.class);
 		startActivity(i);
 	}
 	
 	private void showHighScore() {
 		
 		Intent i = new Intent(this, HighScoreActivity.class);
 		startActivity(i);
 	}
     
 	private void loginFacebook() {
         Intent i = new Intent(this, LoginFacebook.class);
         startActivity(i);    	
     }	
 }
