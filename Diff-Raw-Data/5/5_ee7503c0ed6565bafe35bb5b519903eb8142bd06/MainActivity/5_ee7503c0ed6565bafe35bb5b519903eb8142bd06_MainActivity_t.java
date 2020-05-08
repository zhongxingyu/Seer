 package se.chalmers.dryleafsoftware.androidrally;
 
 import se.chalmers.dryleafsoftware.androidrally.IO.IOHandler;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.Client;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.GameActivity;
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.content.SharedPreferences;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.LayoutInflater;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.view.ViewGroup.LayoutParams;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.AdapterView.OnItemLongClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.Button;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 import android.widget.PopupWindow;
 import android.widget.Toast;
 
 /**
  * This is the main activity, i.g. it will be the activity started when opening
  * the app on the phone.
  * 
  * @author
  * 
  */
 public class MainActivity extends Activity {
 
 	private ListView gameListView;
 	private Client client;
 	private int[] games;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 
 		this.client = Client.getInstance();
 		// Sets where to save.
 		SharedPreferences prefs = this.getSharedPreferences(
 				"androidRallyStorage", Context.MODE_PRIVATE);
 		IOHandler.setPrefs(prefs);
 
 		gameListView = (ListView) findViewById(R.id.currentGames);
 
 		// Starts the selected game when being tapped
 		gameListView.setOnItemClickListener(new OnItemClickListener() {
 
 			@Override
 			public void onItemClick(AdapterView<?> adapter, View view,
 					int position, long id) {
 				startChosenGame(view, games[position]);
 			}
 
 		});
 
 		// Deletes the selected game on longpress with a popup dialog
 		gameListView.setOnItemLongClickListener(new OnItemLongClickListener() {
 			@Override
 			public boolean onItemLongClick(AdapterView<?> adapter, View view,
 					final int position, long id) {
 				LayoutInflater layoutInflater = (LayoutInflater) getBaseContext()
 						.getSystemService(LAYOUT_INFLATER_SERVICE);
 				View popupView = layoutInflater.inflate(R.layout.delete_popup, null);
 
 				final PopupWindow popupWindow = new PopupWindow(popupView,
 						LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
 
 				Button deleteGame = (Button) popupView.findViewById(R.id.yesButton);
 				Button noButton = (Button) popupView.findViewById(R.id.noButton);
 
 				deleteGame.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						client.deleteGame(games[position]);
 						popupWindow.dismiss();
 						refreshGamesList();
 					}
 				});
 
 				noButton.setOnClickListener(new OnClickListener() {
 
 					@Override
 					public void onClick(View v) {
 						popupWindow.dismiss();
 					}
 				});
 
 				popupWindow.showAtLocation(findViewById(R.id.currentGames),
 						Gravity.CENTER, 0, 0);
 				refreshGamesList();
 				return true;
 			}
 
 		});
 		refreshGamesList();
 	}
 
 	@Override
 	protected void onResume() {
 		super.onResume();
 		refreshGamesList();
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 	@Override
 	public boolean onOptionsItemSelected(MenuItem item) {
 		switch (item.getItemId()) {
 
 		case R.id.action_refresh:
 			showToaster("Refreshing");
 			refreshGamesList();
 			return true;
 
 		case R.id.action_help:
 			Intent i = new Intent(getApplicationContext(), HelpActivity.class);
 			startActivity(i);
 			return true;
 
 		default:
 			return true;
 		}
 	}
 
 	private void refreshGamesList() {
 		games = client.getSavedGames();
 		String[] gameNames = new String[games.length];
 		for (int i = 0; i < gameNames.length; i++) {
 			if (games[i] < 0) {
				gameNames[i] = "Single player game " + Math.abs(games[i]);
 			} else {
				gameNames[i] = "Multiplayer game " + games[i];
 			}
 		}
 		ListAdapter gamesList = new ArrayAdapter<String>(
 				getApplicationContext(), android.R.layout.simple_list_item_1,
 				gameNames);
 		gameListView.setAdapter(gamesList);
 
 	}
 
 	/**
 	 * Starts the chosen game
 	 * 
 	 * @param view
 	 * @param gameID The ID of the game
 	 */
 	protected void startChosenGame(View view, int gameID) {
 		Intent i = new Intent(getApplicationContext(), GameActivity.class);
 		i.putExtra("GAME_ID", gameID);
 		startActivity(i);
 	}
 
 	/**
 	 * Shows a toaster with a message
 	 * 
 	 * @param message
 	 *            the message to show
 	 */
 	public void showToaster(CharSequence message) {
 		Context context = getApplicationContext();
 		Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
 		toast.show();
 	}
 
 	/**
 	 * Starts the activity with configuration of a new game
 	 * 
 	 * @param view
 	 */
 	public void startConfiguration(View view) {
 		Intent i = new Intent(getApplicationContext(),
 				GameConfigurationActivity.class);
 		startActivity(i);
 	}
 
 }
