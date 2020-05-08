 package net.homelinux.paubox;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.view.ContextMenu;
 import android.view.Menu;
 import android.view.MenuInflater;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.ContextMenu.ContextMenuInfo;
 import android.widget.Toast;
 
 public class BaseMenuActivity extends Activity {
 
 	/*******************
 	 **** CONSTANTS ****
 	 *******************/
 
 	public static final int REQUEST_CODE_PREFERENCES = RESULT_FIRST_USER + 1;
 	public static final int REQUEST_CODE_WAITING = RESULT_FIRST_USER + 2;
 	public static final int REQUEST_CODE_EDIT = RESULT_FIRST_USER + 3;
 
 	Game current_game;
 
 	// Call the preferences activity
 	protected void launchPreferencesActivity() {
 		Intent launchPreferencesIntent = new Intent(this, PreferencesActivity.class);
 		// Make it a subactivity so we know when it returns
 		startActivityForResult(launchPreferencesIntent, REQUEST_CODE_PREFERENCES);
 	}
 	protected void launchDisplayActivity() {
 		Intent myIntent = new Intent(this,ScoreDisplayActivity.class);
		myIntent.putExtra("net.homelinux.paubox.Game",current_game);
 		startActivityForResult(myIntent, REQUEST_CODE_EDIT);
 	}
 
 	protected void launchNewGameActivity() {
 		Intent myIntent = new Intent(this,NewGameActivity.class);
 		myIntent.setAction(Intent.ACTION_DELETE);
 		startActivity(myIntent);
 	}
 
 	/**
 	 * Invoked during initialization to give the Activity a chance to set up its Menu.
 	 *
 	 * @param menu the Menu to which entries may be added
 	 * @return true
 	 */
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		MenuInflater inflater = getMenuInflater();
 		inflater.inflate(R.menu.common_menu, menu);
 		return true;
 	}
 
 	public void onCreateContextMenu(ContextMenu menu, View v,
 			ContextMenuInfo menuInfo) {
 		menu.setHeaderTitle(R.string.distribution_current);
 		menu.add(Menu.NONE, Game.Us_1, 1, current_game.getPlayer_name(Game.players[Game.Us_1]));
 		menu.add(Menu.NONE, Game.Them_1, 2, current_game.getPlayer_name(Game.players[Game.Them_1]));
 		menu.add(Menu.NONE, Game.Us_2, 3, current_game.getPlayer_name(Game.players[Game.Us_2]));
 		menu.add(Menu.NONE, Game.Them_2, 4, current_game.getPlayer_name(Game.players[Game.Them_2]));
 	}
 	/**
 	 * Invoked when the user selects an item from the Menu.
 	 *
 	 * @param item the Menu entry which was selected
 	 * @return true if the Menu item was legitimate (and we consumed it), false
 	 *         otherwise
 	 */
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 		case R.id.new_game:
 			Toast.makeText(getApplicationContext(), "Lancement d'une nouvelle partie",
 			               Toast.LENGTH_SHORT).show();
 			launchNewGameActivity();
 			return true;
 		case R.id.quit:
 			this.finish();
 			return true;
 		case R.id.preferences:
 			launchPreferencesActivity();
 			return true;
 		case R.id.display_scores:
 			if (current_game == null)
 				Toast.makeText(getApplicationContext(), "Not available now",
 					Toast.LENGTH_SHORT).show();
 			else
 				launchDisplayActivity();
 			return true;
 		}
 		return false;
 	}
 
 	public boolean onContextItemSelected(MenuItem item) {
 		super.onOptionsItemSelected(item);
 		switch (item.getItemId()) {
 			case Game.Us_1:
 			case Game.Us_2:
 			case Game.Them_1:
 			case Game.Them_2:
 				current_game.setPlayer(item.getItemId());
 				return true;
 			default:
 				return false;
 		}
 	}
 }
