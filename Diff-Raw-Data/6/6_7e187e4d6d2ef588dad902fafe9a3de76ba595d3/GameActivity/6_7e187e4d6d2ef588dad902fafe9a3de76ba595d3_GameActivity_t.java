 package com.rachum.amir.skyhiking.android;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.content.pm.ActivityInfo;
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.os.Handler;
 import android.view.View;
 import android.widget.Button;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.ScrollView;
 import android.widget.SlidingDrawer;
 import android.widget.TableLayout;
 import android.widget.TextView;
 import android.widget.ViewFlipper;
 
 import com.rachum.amir.skyhiking.Card;
 import com.rachum.amir.skyhiking.CloudLevel;
 import com.rachum.amir.skyhiking.EventHandler;
 import com.rachum.amir.skyhiking.Game;
 import com.rachum.amir.skyhiking.GameEvent;
 import com.rachum.amir.skyhiking.GameEventListener;
 import com.rachum.amir.skyhiking.Move;
 import com.rachum.amir.skyhiking.android.R;
 import com.rachum.amir.skyhiking.players.Player;
 import com.rachum.amir.skyhiking.players.RandomPlayer;
 import com.rachum.amir.skyhiking.players.RiskyPlayer;
 import com.rachum.amir.util.range.Range;
 
 public class GameActivity extends Activity implements GameEventListener {
     private LinearLayout scoreboard;
     private TextView log;
     private Handler handler;
     private Player humanPlayer;
     private CloudLevel lastKnownLevel = null;
     private Map<Player, PlayerStatusDisplay> playerStatus = 
     	new HashMap<Player, PlayerStatusDisplay>();
     private List<String> names = 
     	Arrays.asList("Phil", "Johnny", "Sharon", "Tammy", "Dan", "George",
     			"Joel", "Jeff", "Arnold", "Jack", "Gary", "Ben", "Fred",
     			"Susan", "Andy", "Evelyn", "Amy", "Donna", "Max", "Jane",
    			"Joan", "Melanie", "Phoebe", "Caroline", "Veronica",
    			"Gloria", "Maya", "Karen", "Marry Ann");
     
     /** Called when the activity is first created. */
     @Override
     public void onCreate(final Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.game);
         handler = new Handler();
         scoreboard = (LinearLayout) findViewById(R.id.scoreboard);
         log = (TextView) findViewById(R.id.log);
         final Button stay = (Button) findViewById(R.id.stay);
         final Button leave = (Button) findViewById(R.id.leave);
         final Button pay = (Button) findViewById(R.id.pay);
         final Button payWithWild = (Button) findViewById(R.id.payWithWild);
         final Button dontPay = (Button) findViewById(R.id.dontPay);
         
         stay.setEnabled(false);
         leave.setEnabled(false);
         pay.setEnabled(false);
         dontPay.setEnabled(false);
         payWithWild.setEnabled(false);
         
         Collections.shuffle(names);
         final List<Player> players = new LinkedList<Player>();
         for (final int i : new Range(5)) {
         	players.add(new RandomPlayer(names.get(i)));
         }
         Bundle bundle = getIntent().getExtras();
         String playerName = bundle.getString("playerName");
         humanPlayer = new HumanPlayer(playerName, handler, stay, leave, pay, 
         							  payWithWild, dontPay);
         players.add(humanPlayer);
         Collections.shuffle(players);
         final Game game = new Game(players);
         for (Player player : players) {
         	PlayerStatusDisplay display = new PlayerStatusDisplay(this, player);
         	scoreboard.addView(display);
         	playerStatus.put(player, display);
         }
         game.registerListener(this);
         game.start();
 	}
     
     @Override
     public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
     }
     
 	@Override
 	public void handleEvent(final GameEvent event, 
 						    final EventHandler eventHandler) {
 		final Collection<Card> cards = new LinkedList<Card>(
 				humanPlayer.getHand().getCards());
         handler.post(new Runnable() {
 			@Override
 			public void run() {
                 handleEventAux(event);
                 updateScores(event.context.players);
                 updateHand(cards);
                 updateLevelInfo(event.level);
                 eventHandler.done();
 			}
 		});
 	}
     
 	private void updateLevelInfo(CloudLevel level) {
 		if (level == null) {
 			level = lastKnownLevel;
 		} else {
 			lastKnownLevel = level;
 		}
 		LinearLayout layout = (LinearLayout) findViewById(R.id.levels_row);
 		layout.removeAllViews();
 		for (CloudLevel cloudLevel : CloudLevel.gameLevels()) {
 			Button levelImage = new Button(this);
 			levelImage.setWidth(50);
 			levelImage.setPadding(5, 5, 5, 5);
 			levelImage.setClickable(false);
 			String levelScore = ((Integer) cloudLevel.getScore()).toString();
 			String levelDice = "-";
 			if (cloudLevel.getDiceNumber() > 0) {
 				levelDice = ((Integer) cloudLevel.getDiceNumber()).toString();
 				levelDice = "(" + levelDice + ")";
 			}
 			String levelInfo = levelScore + "\n" + levelDice;
 			levelImage.setText(levelInfo);
 			if (cloudLevel.equals(level)) {
 				levelImage.setBackgroundResource(R.drawable.levelselected);
 			} else {
 				levelImage.setBackgroundResource(R.drawable.level);
 			}
 			layout.addView(levelImage);
 		}
 	}
 
 	private void sleep(int milliseconds) {
 		try {
 			Thread.sleep(milliseconds);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	private void handleEventAux(final GameEvent event){
 		switch (event.type) {
 		case DICE_ROLLED:
 			String diceRollPrint = formatDiceRoll(event.context.diceRoll);
 			log.setText(event.context.pilot + " rolled " + diceRollPrint + ".");
 			break;
 		case GAME_BEGIN:
			log.setText("Starting a new game!");
 			break;
 		case GAME_END:
 			log.setText("Game over! The winner is " + event.winner);
 			playerStatus.get(event.winner).setWon();
 			//LinearLayout buttons = (LinearLayout) findViewById(R.id.buttons);
 			ViewFlipper flipper = (ViewFlipper) findViewById(R.id.buttonsFlipper);
 			flipper.showNext();
 			break;
 		case LEVEL_BEGIN:
 			log.setText("Starting " + event.level);
 			for (Player player : event.context.players) {
 				playerStatus.get(player).unsetStatus();
 				playerStatus.get(player).setPlaying(
 						event.context.remainingPlayers.contains(player));
 			}
 			playerStatus.get(event.context.pilot).setPilot();
 			break;
 		case LEVEL_END:
 			break;
 		case MOVE:
 			playerStatus.get(event.currentPlayer).setMove(event.move);
 			break;
 		case PAY:
 			if (event.didPay) {
 				log.setText(event.context.pilot + " payed");
 				if (event.cardsPayed.contains(Card.WILD)) {
 					log.append(" with a wildcard");
 				}
 				log.append(".");
 			}
 			else {
 				log.setText("Awww... we crashed!");
 			}
 			break;
 		case ROUND_BEGIN:
 			log.setText("Starting a new round!");
 			break;
 		case ROUND_END:
 			break;
 		}
 		
 		sleep(300);
 	}
 
 	private String formatDiceRoll(Collection<Card> diceRoll) {
 		List<String> diceToPrint = new LinkedList<String>();
 		for (Card card : diceRoll) {
 			if (card != Card.NONE) {
 				diceToPrint.add(card.toString());
 			}
 		}
 		if (diceToPrint.isEmpty()) {
 			return "nothing";
 		}
 		else {
 			return StringUtils.join(diceToPrint, ", ");
 		}
 	}
 
 	private void updateScores(final List<Player> players) {
 		for (Player player : players) {
 			playerStatus.get(player).updatePlayerInfo();
 		}
 	}
     
 	private void updateHand(Collection<Card> cards) {
 		@SuppressWarnings("serial")
 		Map<Card, Integer> counterMap = new HashMap<Card, Integer>() {{
 			put(Card.GREEN, R.id.greencount);
 			put(Card.RED, R.id.redcount);
 			put(Card.PURPLE, R.id.purplecount);
 			put(Card.YELLOW, R.id.yellowcount);
 			put(Card.WILD, R.id.wildcount);
 		}};
 		
 		for (Card cardType : counterMap.keySet()) {
 			Integer cardCount = (Integer)Collections.frequency(cards, cardType);
 			((TextView) findViewById(counterMap.get(cardType)))
 				.setText(cardCount.toString());
 		}
 	}
 	
 	public void playAgain(final View view) {
 		finish();
 		Intent intent = getIntent();
 		startActivity(intent);
 	}
 }
