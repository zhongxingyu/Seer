 package com.brennan.dartscorecard;
 
 import java.util.ArrayList;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.util.Log;
 import android.view.Menu;
 import android.view.View;
 import android.widget.Button;
 import android.widget.RelativeLayout;
 import android.widget.TextView;
 
 import com.brennan.dartscorecard.R;
 import com.brennan.gamelogic.HammerGame;
 import com.brennan.gamelogic.Player;
 
 public class HammerGameActivity extends Activity {
 
 
 	private static final String TAG = "HammerGameActivity";
 	private TextView round_mark, dart2_text, dart3_text;
 	private RelativeLayout mRelativeLayout;
 	private ArrayList<Player> players;
 	private ArrayList<TextView> playersText;
 	private ArrayList<Button> dartOneButtons, dartTwoButtons, dartThreeButtons;
 	private int numTurns, currentPlayer;
 	private Button nextTurnButton, prevTurnButton;
 	private final Integer SELECTED_TAG = 1;
 	private final Integer UNSELECTED_TAG = 2;
 	private boolean done;
 	HammerGame game;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 
 		//Retrieve extras
 		Bundle extras = getIntent().getExtras();
 		if(extras != null){
 			players = extras.getParcelableArrayList("players");
 		}
 
 
 		setContentView(R.layout.activity_hammer_game);
 
 		mRelativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
 
 		done = false;
 		
 		//Round and multiplier text
 		round_mark = (TextView) findViewById(R.id.round_mark);
 		dart2_text = (TextView) findViewById(R.id.dart2_text);
 		dart3_text = (TextView) findViewById(R.id.dart3_text);		
 
 		//Next Turn button
 		nextTurnButton = (Button) findViewById(R.id.next_turn_button);
 		nextTurnButton.setOnClickListener(nextTurnHandler);
 
 		//Previous turn button
 		prevTurnButton = (Button) findViewById(R.id.prev_turn_button);
 		prevTurnButton.setEnabled(false);
 		prevTurnButton.setOnClickListener(prevTurnHandler);
 
 		//Dart buttons
 		//TODO: See if there is a better way to do this more programatically
 		//Possible solution: http://stackoverflow.com/questions/6663380/is-it-possible-to-write-a-for-loop-to-assign-listeners-to-many-button-with-the-s
 		dartOneButtons = new ArrayList<Button>();
 		Button b11 = (Button) findViewById(R.id.dart1times1);
 		Button b12 = (Button) findViewById(R.id.dart1times2);
 		Button b13 = (Button) findViewById(R.id.dart1times3);
 		dartOneButtons.add(b11);
 		dartOneButtons.add(b12);
 		dartOneButtons.add(b13);
 
 		dartTwoButtons = new ArrayList<Button>();
 		Button b21 = (Button) findViewById(R.id.dart2times1);
 		Button b22 = (Button) findViewById(R.id.dart2times2);
 		Button b23 = (Button) findViewById(R.id.dart2times3);
 		dartTwoButtons.add(b21);
 		dartTwoButtons.add(b22);
 		dartTwoButtons.add(b23);
 
 		dartThreeButtons = new ArrayList<Button>();
 		Button b31 = (Button) findViewById(R.id.dart3times1);
 		Button b32 = (Button) findViewById(R.id.dart3times2);
 		Button b33 = (Button) findViewById(R.id.dart3times3);
 		dartThreeButtons.add(b31);
 		dartThreeButtons.add(b32);
 		dartThreeButtons.add(b33);
 
 
 		for(int i = 0; i < 3; i++){
 			dartOneButtons.get(i).setOnClickListener(dartHandler);
 			dartTwoButtons.get(i).setOnClickListener(dartHandler);
 			dartThreeButtons.get(i).setOnClickListener(dartHandler);
 		}
 
 		//Set up players names and scores default to 0
 		TextView tv1 = (TextView) findViewById(R.id.playerOneText);
 		TextView tv2 = (TextView) findViewById(R.id.playerTwoText);
 		TextView tv3 = (TextView) findViewById(R.id.playerThreeText);
 		TextView tv4 = (TextView) findViewById(R.id.playerFourText);
 
 		playersText = new ArrayList<TextView>();
 		playersText.add(tv1);
 		playersText.add(tv2);
 		playersText.add(tv3);
 		playersText.add(tv4);
 
 		for(int i = 0; i < players.size(); i++){
 			TextView tv = playersText.get(i);
 			tv.setVisibility(View.VISIBLE);
 			//Trolling roommate
 			if(players.get(i).getName().equalsIgnoreCase("nick"))
 				tv.setText(players.get(i).getName()  + "\nScore: -1000");
 			else
 				tv.setText(players.get(i).getName()  + "\nScore: 0" ) ;
 		}
 
 		//Set up game and numTurns
 		game = new HammerGame();
 		game.addPlayers(players);
 		numTurns = 0;
 		currentPlayer = 0;
 		
 		playersText.get(0).setTextColor(getResources().getColor(R.color.light_green));
 
 	}
 
 	View.OnClickListener dartHandler = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			switch(v.getId()){
 			case R.id.dart1times1:
 			case R.id.dart1times2: 
 			case R.id.dart1times3:
 				checkDartButtons(v, dartOneButtons);
 				break;
 			case R.id.dart2times1:
 			case R.id.dart2times2:
 			case R.id.dart2times3:
 				checkDartButtons(v, dartTwoButtons);
 				break;
 			case R.id.dart3times1:
 			case R.id.dart3times2:
 			case R.id.dart3times3:
 				checkDartButtons(v, dartThreeButtons);
 				break;
 			}
 		}
 	};
 
 	//Checks if the selected button should be selected or unselected
 	public void checkDartButtons(View v, ArrayList<Button> buttons){
 		for(int i = 0; i < buttons.size(); i++){
 			if(buttons.get(i).getId() == v.getId() && buttons.get(i).getTag() != SELECTED_TAG){
 				buttons.get(i).setBackgroundColor(getResources().getColor(R.color.light_green));
 				buttons.get(i).setTag(SELECTED_TAG);
 			}else
 				clearDart(buttons.get(i));			
 		}
 	}
 
 	//Clears all darts of color and tag
 	public void clearDarts(){
 		for(int i = 0; i < dartOneButtons.size(); i++){
 			clearDart(dartOneButtons.get(i));
 			clearDart(dartTwoButtons.get(i));
 			clearDart(dartThreeButtons.get(i));
 		}
 	}
 
 	//Clears the specific dart of color and tag
 	public void clearDart(Button b){
 		b.setBackgroundColor(getResources().getColor(R.color.white));
 		b.setTag(UNSELECTED_TAG);
 	}
 
 	public void clearAllPlayerColor(){
 		for(int i = 0; i < players.size(); i++)
 			playersText.get(i).setTextColor(getResources().getColor(R.color.black));
 		
 	}
 	/*
 	 * Goes to the next turn or round
 	 */
 	View.OnClickListener nextTurnHandler = new View.OnClickListener() {
 
 		@Override
 		public void onClick(View v) {
 			currentPlayer = numTurns % players.size();
 			int nextPlayer = (numTurns+1) % players.size();
 			
 
 			Log.v(TAG, "CurrentPlayer: " + (currentPlayer + 1) + ", CurrentTurn: " + numTurns);
 			
 			prevTurnButton.setEnabled(true);
 			
 			prepareNextTurn();
 
 			if(((currentPlayer == players.size() - 1 && numTurns != 0) || players.size() == 1) //Condition for single player
 					&& game.getCurrentRound() < game.getMarks().size() - 1){ //And isn't the last round
 				game.setCurrentRound(game.getCurrentRound() + 1);				
 
 				if(!(game.getCurrentRound() >= game.getMarks().size() * players.size()))
 					setRoundText();
 			}	
 			numTurns++;
 			if(numTurns == game.getMarks().size() * players.size()){
 				nextTurnButton.setEnabled(false);
 				//numTurns--;
 				done = true;
 			}
 			
 			clearAllPlayerColor();
 			if(!done){
 				playersText.get(nextPlayer).setTextColor(getResources().getColor(R.color.light_green));
 			}
 		}		
 	};
 	
 	View.OnClickListener prevTurnHandler = new View.OnClickListener() {
 
 		//Goes to the previous round or player
 		@Override
 		public void onClick(View v) {
 
 			currentPlayer = numTurns % players.size();		
			int nextPlayer = (numTurns+1) % players.size();
 			
 			clearAllPlayerColor();
 			if(numTurns !=0)
				playersText.get(nextPlayer).setTextColor(getResources().getColor(R.color.light_green));
 			
 			Log.v(TAG, "CurrentPlayer: " + (currentPlayer + 1) + ", CurrentTurn: " + numTurns);
 		
 			preparePrevTurn();
 			
 			if(currentPlayer == 0 && !done){				
 
 				if(game.getCurrentRound() != 0)
 					game.setCurrentRound(game.getCurrentRound() - 1);
 				
 				setRoundText();
 			}	
 			
 			done = false;
 			numTurns--;
 			if(numTurns == 0)
 				prevTurnButton.setEnabled(false);
 			nextTurnButton.setEnabled(true);
 			
 		}
 	};
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.activity_hammer_game, menu);
 		return true;
 	}
 
 	public void preparePrevTurn(){
 		int prevPlayerIndex = (numTurns - 1) % players.size();
 		Player p = players.get(prevPlayerIndex);
 		p.setScore(p.getScore() - p.popRound());
 		updatePlayerScore(prevPlayerIndex);
 		clearDarts();
 	}
 	
 	//Gets the views ready for the next turn
 	public void prepareNextTurn(){
 		int tempScore = 0, multiplier2 = -1, multiplier3 = -1;
 		if(game.getCurrentRound() == game.getMarks().size() - 1){
 			multiplier2 = 3;
 			multiplier3 = 5;
 		}else{
 			multiplier2 = 2;
 			multiplier3 = 3;
 		}
 		
 		for(int i = 0; i < 3; i++){
 			
 			//Multiplier is the round multiplier, (i+1) is the row multiplier 
 			//ie the third row is all x3 darts (darts that hit a triple score)
 			if(dartOneButtons.get(i).getTag() == SELECTED_TAG)
 				tempScore += (i+1) * game.getMarks().get(game.getCurrentRound());
 			if(dartTwoButtons.get(i).getTag() == SELECTED_TAG)
 				tempScore += (i+1) * multiplier2 * game.getMarks().get(game.getCurrentRound());
 			if(dartThreeButtons.get(i).getTag() == SELECTED_TAG)
 				tempScore += (i+1) * multiplier3 * game.getMarks().get(game.getCurrentRound());
 		}
 		if(tempScore == 0){
 			tempScore = -1 * (game.getMarks().get(game.getCurrentRound()) * 3);
 		}
 		players.get(currentPlayer).pushRound(tempScore);
 		players.get(currentPlayer).addToScore(tempScore);
 		updatePlayerScore(currentPlayer);
 		clearDarts();
 	}
 
 	//Set's the round text and multiplier text
 	public void setRoundText(){
 		Integer mark = game.getMarks().get((game.getCurrentRound()));
 		if(mark == 25)
 			round_mark.setText("25");
 		else
 			round_mark.setText(mark.toString());
 		checkMultiplers();		
 		
 		Log.v(TAG, "Round: " + game.getCurrentRound());
 	}
 	
 	//Updates the player's score
 	public void updatePlayerScore(int playerIndex){
 		TextView playerName = playersText.get(playerIndex);
 		playerName.setText(players.get(playerIndex).getName() + "\nScore: " + (players.get(playerIndex).getScore() ));
 	}
 
 	//Checks to see if the multipliers need to be updated
 	public void checkMultiplers(){
 		if(game.getCurrentRound() == 7){
 			dart2_text.setText("x3");
 			dart3_text.setText("x5");
 		}else{
 			dart2_text.setText("x2");
 			dart3_text.setText("x3");
 		}
 	}
 
 }
