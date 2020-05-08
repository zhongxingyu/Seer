 package cs309.a1.gameboard.activities;
 
 import static cs309.a1.shared.CardGame.CRAZY_EIGHTS;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Matrix;
 import android.os.Bundle;
 import android.util.Log;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import cs309.a1.crazyeights.CrazyEightGameRules;
 import cs309.a1.crazyeights.CrazyEightsTabletGame;
 import cs309.a1.gameboard.R;
 import cs309.a1.shared.Card;
 import cs309.a1.shared.Deck;
 import cs309.a1.shared.Game;
 import cs309.a1.shared.Player;
 import cs309.a1.shared.Rules;
 import cs309.a1.shared.Util;
 import cs309.a1.shared.bluetooth.BluetoothServer;
 
 public class GameboardActivity extends Activity {
 	private static final String TAG = GameboardActivity.class.getName();
 
 	private static final int QUIT_GAME = "QUIT_GAME".hashCode();
 	private static Game game = null;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.gameboard);
 		BluetoothServer bts = BluetoothServer.getInstance(this);
 
 		int numOfConnections = bts.getConnectedDeviceCount();
 		List<Player> players = new ArrayList<Player>();
 		List<String> devices = bts.getConnectedDevices();
 
 		for (int i = 0; i < numOfConnections; i++){
 			Player p = new Player();
 			p.setId(devices.get(i));
 			p.setName("Player "+i);
 			players.add(p);
 		}
 
 		Rules rules = new CrazyEightGameRules();
 		Deck deck = new Deck(CRAZY_EIGHTS);
 		game = CrazyEightsTabletGame.getInstance(players, deck, rules);
 		game.setup();
 
 		for (int i = 0; i < players.size(); i++) {
 			if (Util.isDebugBuild()) {
 				Log.d(TAG, "Player" + i + ": " + players.get(i));
 			}
 
 			// TODO: sometimes this is giving an empty string...
 			bts.write(players.get(i), players.get(i).getId());
 		}
 
 		// If it is a debug build, show the cards face up so that we can
 		// verify that the tablet has the same cards as the player
 		if (Util.isDebugBuild()) {
 			int i = 1;
 			for (Player p : players) {
 				for (Card c : p.getCards()){
 					placeCard(i, c);
 				}
 
 				i++;
 			}
 
 			for (; i < 5; i++) {
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 				placeCard(i, new Card(5, 0, R.drawable.back_blue_1, 54));
 			}
 		} else {
 			// Otherwise just show the back of the cards for all players
 			for (int i = 1; i < 5 * 5; i++) {
 				placeCard(i % 5, new Card(5, 0, R.drawable.back_blue_1, 54));
 			}
 
 			// TODO: The discard card needs to not be hardcoded
 			placeCard(0, new Card(0, 2, R.drawable.clubs_3, 2));
 		}
 	}
 
 	@Override
 	public void onBackPressed() {
 		Intent intent = new Intent(this, QuitGameActivity.class);
 		startActivityForResult(intent, QUIT_GAME);
 	}
 
 	@Override
 	protected void onDestroy() {
 		BluetoothServer.getInstance(this).disconnect();
 
 		super.onDestroy();
 	}
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		if (requestCode == QUIT_GAME) {
 			if (resultCode == RESULT_OK) {
 				// Finish this activity
 				setResult(RESULT_OK);
 				finish();
 			}
 		}
 
 		super.onActivityResult(requestCode, resultCode, data);
 	}
 
 	void placeCard(int location, Card newCard) {
 
 		LinearLayout ll;
 		LinearLayout.LayoutParams lp;
 
 		// convert dip to pixels
 		final float dpsToPixScale = getApplicationContext().getResources().getDisplayMetrics().density;
 		int pixels = (int) (125 * dpsToPixScale + 0.5f);
 
 		// place in discard pile
 		if(location == 0) {
 			ImageView discard = (ImageView) findViewById(R.id.discardpile);
 			discard.setImageResource(newCard.getResourceId());
 		}
 
 		// if Player 1 or Player 3
 		else if(location == 1 || location == 3) {
 
 			if(location == 1) ll = (LinearLayout) findViewById(R.id.player1ll);
 			else ll = (LinearLayout) findViewById(R.id.player3ll);
 
 			lp = new LinearLayout.LayoutParams(pixels, LinearLayout.LayoutParams.WRAP_CONTENT);
 
 			ImageView toAdd = new ImageView(this);
 			toAdd.setImageResource(newCard.getResourceId());
 			toAdd.setId(newCard.getIdNum());
 			toAdd.setAdjustViewBounds(true);
 			ll.addView(toAdd, lp);
 		}
 
 		// if Player 2 or Player 4
 		else if(location == 2 || location == 4) {
 
 			if(location == 2) ll = (LinearLayout) findViewById(R.id.player2ll);
 			else ll = (LinearLayout) findViewById(R.id.player4ll);
 
 			// rotate vertical card image 90 degrees
 			Bitmap verticalCard = BitmapFactory.decodeResource(getResources(), newCard.getResourceId());
 			Matrix tempMatrix = new Matrix();
 			tempMatrix.postRotate(90);
 			Bitmap horCard = Bitmap.createBitmap(verticalCard, 0, 0, verticalCard.getWidth(), verticalCard.getHeight(), tempMatrix, true);
 
 			ImageView toAdd = new ImageView(this);
 			toAdd.setImageBitmap(horCard);
 
 			lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, pixels);
 			toAdd.setAdjustViewBounds(true);
 			ll.addView(toAdd, lp);
 		}
 
 		else {
 			ImageView draw = (ImageView) findViewById(R.id.drawpile);
 			draw.setImageResource(newCard.getResourceId());
 		}
 	}
 
 }
