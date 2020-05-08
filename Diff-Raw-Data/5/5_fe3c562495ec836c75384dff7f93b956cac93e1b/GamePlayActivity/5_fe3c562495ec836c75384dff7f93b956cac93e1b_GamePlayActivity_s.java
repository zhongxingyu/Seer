 package rose.BoucherMercer.drunkr;
 
 import java.util.Locale;
 import java.util.Random;
 
 import android.app.Activity;
 import android.os.Bundle;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class GamePlayActivity extends Activity {
 	
 	Deck deck;
 	Random rng;
 	Game mGame;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game_play);
 		deck = new Deck();
 		rng = new Random(System.currentTimeMillis());
 		mGame = (Game)getIntent().getSerializableExtra(GameListActivity.GAME_KEY);
 		setTitle(mGame.getName());
 		((TextView)findViewById(R.id.game_instructions)).setText(mGame.getInstructions());
 		
 		StringBuilder sb = new StringBuilder();
		for(String s : mGame.getProps()) {
 			sb.append(s + "\n");
 		}
 		
 		((Button)findViewById(R.id.email_game_button)).setOnClickListener(new OnClickListener() {
 			
 			@Override
 			public void onClick(View v) {
 				Email.sendGame(GamePlayActivity.this, mGame);
 			}
 		});
 
 		((TextView)findViewById(R.id.props)).setText(sb.toString());
 		
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		super.onCreateOptionsMenu(menu);
 		getMenuInflater().inflate(R.menu.game_play, menu);
 		return true;
 	}
 	
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		int itemId = item.getItemId();
 		if (itemId == R.id.roll_dice_menu_item) {
 			rollDie();
 			return true;
 		} else if(itemId == R.id.draw_card_menu_item) {
 			String card = deck.drawRandomCard();
 			Toast.makeText(this, card, Toast.LENGTH_LONG).show();
 			return true;
 		} else if(itemId == R.id.shuffle_deck_menu_item) {
 			deck.shuffle();
 			return true;
 		} 
 		return false;
 	}
 	
 	public void rollDie() {
 		int roll = rng.nextInt(6) + 1;
 		String rollString = String.format(Locale.getDefault(), "You rolled a %d", roll);
 		
 		Toast.makeText(this, rollString, Toast.LENGTH_LONG).show();
 	}
 
 	
 	
 }
