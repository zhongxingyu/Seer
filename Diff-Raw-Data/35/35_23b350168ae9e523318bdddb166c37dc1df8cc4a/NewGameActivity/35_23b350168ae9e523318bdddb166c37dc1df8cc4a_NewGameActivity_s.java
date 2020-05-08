 package net.homelinux.paubox;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.preference.PreferenceManager;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.RadioButton;
 
 public class NewGameActivity extends BaseMenuActivity {
 
 	/*******************
 	 **** CONSTANTS ****
 	 *******************/
 
 	/************************
 	 **** CLASS VARIABLE ****
 	 ************************/
 	EditText text_players[] = new EditText[4];
 	RadioButton first_distribution[] = new RadioButton[4];
 	private static final int text_players_ids[] = {
 		R.id.name_player1, R.id.name_player2, R.id.name_player3, R.id.name_player4
 	};
 	private static final int first_distribution_ids[] = {
 		R.id.first_distribution1, R.id.first_distribution2, R.id.first_distribution3, R.id.first_distribution4
 	};
 	CheckBox shuffle_game;
 	
 	/**************************
 	 **** PRIVATE METHODS *****
 	 **************************/
 
 	/****************************
 	 **** PROTECTED METHODDS ****
 	 ****************************/
 	// Call the waiting activity
 	protected void launchAnnouceActivity() {
 		Intent annouce_intent = new Intent(this, AnnounceActivity.class);
 		annouce_intent.setAction(Intent.ACTION_DELETE);
 		annouce_intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		annouce_intent.putExtra("net.homelinux.paubox.Game", current_game);
 		startActivity(annouce_intent);
 	}
 
 
 	static void forcePreferencesCounting(Activity a, Game g) {
 		g.setLoose_160(PreferenceManager.getDefaultSharedPreferences(a).getBoolean("loose_160", false));
 		g.win_score_mode = Game.parseScoreString(PreferenceManager.getDefaultSharedPreferences(a).getString("win_score", Integer.toString(Game.SCORE_BETONLY)));
 	}
 	/*************************
 	 **** PUBLIC METHODDS ****
 	 *************************/
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.new_game_layout);
 
 		current_game = new Game();
 		forcePreferencesCounting(this, current_game);
 
 		final Button button_start = (Button) findViewById(R.id.button_new_game_go);
 		button_start.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				for (int i = 0; i<Game.players_cnt; i++) {
 					if ((text_players[i].getText().toString() != null) && (text_players[i].getText().toString().trim().length()>0))
 						current_game.setPlayer_name(text_players[i].getText().toString().trim(), Game.players[i]);
 					else
 						current_game.setPlayer_name(text_players[i].getHint().toString(), Game.players[i]);
 					if (first_distribution[i].isChecked())
 						current_game.setPlayer(i);
 				}
 				current_game.currentDeal().setShuffleDeal(shuffle_game.isChecked());
 				launchAnnouceActivity();
 			}
 		});
 
 		for (int i = 0; i < Game.players_cnt; i++) {
 			text_players[i] = (EditText) findViewById(text_players_ids[i]);
 			first_distribution[i] = (RadioButton) findViewById(first_distribution_ids[i]);
 		}
 		shuffle_game = (CheckBox) findViewById(R.id.shuffle_game);
 	}
 
 }
