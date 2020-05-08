 package com.jand.bombercommander.screens;
 
 import com.jand.bombercommander.GameThread;
 import com.jand.bombercommander.R;
 
 import android.os.Bundle;
 import android.view.View;
 import android.widget.Button;
 import android.widget.TextView;
 import android.app.Activity;
 
 public class MainGameActivity extends Activity {
 	
 	public static GameThread thread;
 	public static enum gameState { P1_SETUP, P2_SETUP, ANIMATION };
 	public static gameState state;
 	TextView playerText;
 	Button actionButton;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main_game);
 		
 		playerText = (TextView)findViewById(R.id.textPlayerSetup);
 		actionButton = (Button)findViewById(R.id.btnAction);
 		
 			actionButton.setOnClickListener( new View.OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				switch (state)
 				{
 				case P1_SETUP:
 					state = gameState.P2_SETUP;
 					playerText.setText( "Player 2 Setup (Top Row)" );
 					actionButton.setText( "Prepare for Battle!" );
 					break;
 				case P2_SETUP:
 					state = gameState.ANIMATION;
 					playerText.setVisibility( View.INVISIBLE );
 					actionButton.setVisibility( View.INVISIBLE );
 					break;
 				default:
 					break;
 				}
 			}
 		});
 	}
 }
