 package net.homelinux.paubox;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.res.Resources;
 import android.os.Bundle;
 import android.os.PowerManager;
 import android.preference.PreferenceManager;
 import android.view.ContextMenu;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.LinearLayout;
 import android.widget.RadioButton;
 import android.widget.SeekBar;
 import android.widget.TextView;
 import android.widget.Toast;
 import android.graphics.PorterDuff;
 
 public class AnnounceActivity extends BaseMenuActivity {
 
 	private static final int text_players_defaults[] = {
 		R.string.name_player1, R.string.name_player2, R.string.name_player3, R.string.name_player4
 	};
 
 	/************************
 	 **** CLASS VARIABLE ****
 	 ************************/
 	LinearLayout scoreDisplayView;
 	GameView gw;
 	TextView distribution;
 
 	PowerManager.WakeLock wl;
 
 	/****************************
 	 **** PROTECTED METHODDS ****
 	 ****************************/
 	// Call the waiting activity
 	protected void launchWaitingActivity() {
 		Intent waiting_intent = new Intent(this, WaitingActivity.class);
 		waiting_intent.putExtra("net.homelinux.paubox.Game", current_game.currentDeal());
 		startActivityForResult(waiting_intent, REQUEST_CODE_WAITING);
 	}
 
 
 	@Override
 	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
 		super.onActivityResult(requestCode, resultCode, data);
 
 
 		// The preferences returned if the request code is what we had given
 		// earlier in startSubActivity
 		switch (requestCode) {
 		case REQUEST_CODE_PREFERENCES:
 			// Read a sample value they have set
 			updatePreferences();
 			break;
 		case REQUEST_CODE_WAITING:
 			if (resultCode != RESULT_CANCELED) {
 				boolean won = data.getBooleanExtra("net.homelinux.paubox.won", false);
 				int difference = data.getIntExtra("net.homelinux.paubox.difference", 0);
 				current_game.currentDeal().setWon(won);
 				current_game.currentDeal().announce_difference = difference;
 				current_game.updateResult();
 				current_game.newDeal();
 				//button_coinche.setText(CoincheButtonTextId(current_game.currentDeal().getCoinchedMultiplicator()));
 				int resultId = won ? R.string.deal_won : R.string.deal_lost;
 				Resources r = getResources();
 				String s = r.getString(R.string.deal_summary, r.getString(resultId), difference);
 				Toast.makeText(getApplicationContext(), s,
 						Toast.LENGTH_SHORT).show();
 			} else {
 				Toast.makeText(getApplicationContext(), "Cancel",
 						Toast.LENGTH_SHORT).show();
 			}
 			break;
 		case REQUEST_CODE_EDIT:
 			if (data!=null) {
 				final Game g = (Game) data.getSerializableExtra("net.homelinux.paubox.edit");
 				current_game.setAs(g);
 				current_game.recomputeScores();
 				gw.notifyDataSetChanged();
 			}
 		}	
 
 		distribution.setText("distribution : " + current_game.getPlayer_Distribution() + "\n");
 	}
 
 	// We don't update score preferences here as they are used only when creating a new game.
 	private void updatePreferences() {
 		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("wake_lock_enable", false)) {
 			PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
 			wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "AnnounceActivity");
 		} else {
 			wl = null;
 		}
 	}
 
 	static protected int BetFromItemId(int adapter_view_id) {
 		if (adapter_view_id == android.widget.AdapterView.INVALID_ROW_ID)
 			return Deal.MIN_BET;
 		else if (adapter_view_id > (Deal.MAX_BET - Deal.MIN_BET)/10)
 			return Deal.CAPOT_BET;
 		else
 			return Deal.MIN_BET + adapter_view_id * 10;
 	}
 	static protected int ItemIdFromBet(int bet) {
 		if (bet >= Deal.MIN_BET && bet <= Deal.MAX_BET) {
 			return (bet - Deal.MIN_BET)/10;
 		}
 		else if (bet <Deal.MIN_BET) {
 			return 0;
 		}
 		else return  ((Deal.MAX_BET - Deal.MIN_BET) / 10)+1;
 	}
 	/*************************
 	 **** PUBLIC METHODDS ****
 	 *************************/
 
 	@Override
 	public void onResume() {
 		super.onResume();
 		if (wl!=null) {
 			wl.acquire();
 		}
 		configureAnnounceView();
 		
 	}
 
 	@Override
 	public void onPause() {
 		super.onPause();
 		writeGame(current_game);
 		if (wl!=null) {
 			wl.release();
 		}
 	}
 
 	public void configureAnnounceView() {
 
 		distribution = (TextView) findViewById(R.id.distribution);
 		distribution.setText("distribution : " + current_game.getPlayer_Distribution());
 		registerForContextMenu(distribution);
 
 		final RadioButton radio_us = (RadioButton) findViewById(R.id.button_Us);
 		final RadioButton radio_them = (RadioButton) findViewById(R.id.button_Them);
 		final SeekBar bet_seekbar = (SeekBar) findViewById(R.id.bet_seekbar);
 		final Deal d = current_game.currentDeal();
 		final Button coinche_button = (Button) findViewById(R.id.coinche_button);
 		coinche_button.setText(resIdFromMultiplicator(d.coinchedMultiplicator));
 		Button button_go = ((Button) findViewById(R.id.button_go));
 		button_go.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						saveDeal(AnnounceActivity.this, d, radio_us, radio_them, bet_seekbar,
 								coinche_button);
 						launchWaitingActivity();
 					}});
 		gw = (GameView) findViewById(R.id.game_view);
 		gw.initGame(this.current_game, false);
 	}
 	
 	public static void saveDeal(final Activity a, final Deal d,
 			final RadioButton radio_us, final RadioButton radio_them,
 			final SeekBar bet_seekbar, final Button coincheButton) {
 		//Save the current team betting
 		if (radio_us.isChecked())
 			d.setTeam_betting(Game.Us);
 		else if (radio_them.isChecked()) {
 			d.setTeam_betting(Game.Them);
 		}
 		else {
 			Toast.makeText(a.getApplicationContext(), R.string.select_team , Toast.LENGTH_SHORT).show();
 			return;
 		}
 
 		//Save the current bet and the multiplicator
 		d.setBet(BetFromItemId(bet_seekbar.getProgress()));
 		d.setCoinchedMultiplicator(multiplicatorFromText(a,coincheButton.getText().toString()));
 	}
 	
 	public static void configureDealView(final Activity a,final Deal d) {
 		final RadioButton radio_us = (RadioButton) a.findViewById(R.id.button_Us);
 		final RadioButton radio_them = (RadioButton) a.findViewById(R.id.button_Them);
 		if (d.team_betting == Game.Us) {
 			radio_us.setChecked(true);
 		}
 		else if (d.team_betting == Game.Them){
 			radio_them.setChecked(true);
 		}
 
 		final SeekBar bet_seekbar = (SeekBar) a.findViewById(R.id.bet_seekbar);
 		final TextView progress_text = (TextView) a.findViewById(R.id.progress_text);
 		final Button coinche_button = (Button) a.findViewById(R.id.coinche_button);
 
 		final BetSeekBarListener bet_listener = new BetSeekBarListener(a, bet_seekbar, progress_text);
 		bet_seekbar.setOnSeekBarChangeListener(bet_listener);
 		bet_seekbar.setProgress(ItemIdFromBet(d.bet));
 
 		coinche_button.setText(resIdFromMultiplicator(d.coinchedMultiplicator));
 		coinche_button.getBackground().setColorFilter(coincheColorFromMultiplicator(a, d.coinchedMultiplicator), PorterDuff.Mode.MULTIPLY);
 		coinche_button.setOnClickListener(new OnClickListener() {
 			public void onClick(View v) {
 				int newMultiplicator = multiplicatorFromText(a, coinche_button.getText().toString());
 				newMultiplicator *= 2;
 				if (newMultiplicator > 4) newMultiplicator = 1;
 				coinche_button.setText(resIdFromMultiplicator(newMultiplicator));
 				coinche_button.getBackground().setColorFilter(coincheColorFromMultiplicator(a, newMultiplicator), PorterDuff.Mode.MULTIPLY);
 			}
 		});
 	}
 
 	public static int coincheColorFromMultiplicator(Activity a, int multiplicator) {
 		switch (multiplicator) {
 		case 2:
 			return a.getResources().getColor(R.color.orange);
 		case 4:
 			return a.getResources().getColor(R.color.solid_red);
 		default:
 			return a.getResources().getColor(R.color.green);
 		}
 	}
 
     private static int multiplicatorFromText(Context c, String text) {
 		if (text.equals(c.getString(R.string.uncoinched))) return 1;
 		else if (text.equals(c.getString(R.string.coinched))) return 2;
 		else if (text.equals(c.getString(R.string.overcoinched))) return 4;
 		else return -1;
     }
 	private static int resIdFromMultiplicator(int coinchedMultiplicator) {
 		switch (coinchedMultiplicator) {
 			case 1: return R.string.uncoinched;
 			case 2: return R.string.coinched;
 			case 4: return R.string.overcoinched;
 			default: return -1;
 		}
 	}
 
 	private void setDefaultNames(Game g) {
 		for (int i=0; i<4; i++)
 			g.player_names[i] = this.getResources().getString(text_players_defaults[i]);
 	}
 
 	/** Called when the activity is first created. */
 	@Override
 	public void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.announce_layout);
 		updatePreferences();
 		if (getIntent().getAction().equals(Intent.ACTION_DELETE)) {
			current_game = (Game)getIntent().getSerializableExtra("net.homelinux.paubox.Game");
 		} else {
 			try {
 				current_game = readGame();
 			}
 			catch (Exception e) {
 				//Something went wrong, start a new game
 				current_game = new Game();
 				setDefaultNames(current_game);
 			}
 		}
 		AnnounceActivity.configureDealView(this, current_game.currentDeal());
 		
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		super.onCreateContextMenu(menu, v, menuInfo);
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 		case R.id.new_game:
 			return true;
 		case R.id.quit:
 			return true;
 		case R.id.preferences:
 			return true;
 		case R.id.display_scores:
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onContextItemSelected(item);
 		switch (item.getItemId()) {
 			case Game.Us_1:
 			case Game.Us_2:
 			case Game.Them_1:
 			case Game.Them_2:
 				configureAnnounceView();
 				return true;
 			default:
 				return false;
 		}
 	}
 
 	// To be serializable
 	private static final String FILENAME = "coinchoid_current_game.ser";
 	private Game readGame() throws Exception {
 		Game game = null;
 		FileInputStream fis = openFileInput(FILENAME);
 		ObjectInputStream ois = new ObjectInputStream(fis);
 		game = (Game) ois.readObject();
 		fis.close();
 		return game;
 	}
 	private void writeGame(Game game) {
 		try {
 			FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			ObjectOutputStream oos = new ObjectOutputStream(fos);
 			oos.writeObject(game);
 			fos.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
 
