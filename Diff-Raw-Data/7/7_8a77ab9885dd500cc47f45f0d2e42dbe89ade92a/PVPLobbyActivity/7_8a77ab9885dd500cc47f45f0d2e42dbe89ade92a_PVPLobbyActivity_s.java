 package edu.rhit.petitjam_coblebj.roboarena;
 
 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.Button;
 import android.widget.CheckBox;
 import android.widget.EditText;
 import android.widget.Toast;
 
 import com.firebase.client.DataSnapshot;
 import com.firebase.client.Firebase;
 import com.firebase.client.ValueEventListener;
 
 /**
  * 
  * <p>When this class starts the LockerRoomActivity, a couple extras are put in place:</p>
  * 
  * 	<p>KEY_GAME_ID - the name of this game in Firebase<br/>
  * 	   KEY_PLAYER_ID - the id of the LOCAL player ("player_creator" or "player_joiner")</p>
  * 
  * @author petitjam
  */
 public class PVPLobbyActivity extends Activity {
 
 	private static String GAME_SETUP = "GameSetup";
 
 	private Firebase mRef;
 
 	private EditText mCreateRoomName;
 	private EditText mCreateRoomPassword;
 	private EditText mJoinRoomName;
 	private EditText mJoinRoomPassword;
 
 	private CheckBox mAllowSpectators;
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_pvp_lobby);
 
 		mRef = new Firebase(getString(R.string.roboarena_firebase_games));
 
 		// EditTexts
 		mCreateRoomName = (EditText)findViewById(R.id.create_room_name);
 		mCreateRoomPassword = (EditText)findViewById(R.id.create_room_password);
 		mJoinRoomName = (EditText)findViewById(R.id.join_room_name);
 		mJoinRoomPassword = (EditText)findViewById(R.id.join_room_password);
 
 		// Allow Specs Check
 		mAllowSpectators = (CheckBox)findViewById(R.id.allow_spectators_check);
 
 		// Buttons
 		Button join_button = (Button)findViewById(R.id.join_player_game_button);
 		Button create_button = (Button)findViewById(R.id.create_player_game_button);
 
 		/* ****************************************
 		 * 
 		 * BIG IMPORTANT NOTE TO REMEMBER
 		 * 
 		 * When we create the game, we are "player_creator"
 		 * 
 		 * When we join, "player_joiner"
 		 * 
 		 * ****************************************
 		 */
 
 		join_button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				final String gameName = mJoinRoomName.getText().toString();
 				final String gamePassword = mJoinRoomPassword.getText().toString();
 				
 				if (gameName.isEmpty()) {
 					Toast.makeText(PVPLobbyActivity.this, R.string.error_game_name_empty, Toast.LENGTH_SHORT).show();
 					return;
 				}
 
 				final Firebase gameRef = mRef.child(gameName);
 				
 				Log.d("RA", "clicked join");
 
 				gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
 					@Override
 					public void onDataChange(DataSnapshot snap) {
 						
 						Log.d("RA", "checking if game exists");
 
 						if (snap.getValue() == null) {
 							Toast.makeText(PVPLobbyActivity.this, R.string.error_game_does_not_exist,
 									Toast.LENGTH_SHORT).show();
 							mJoinRoomName.setText("");
 							mJoinRoomPassword.setText("");
 							return;
 						}
 
 						final Firebase passwordRef = gameRef.child(getString(R.string.fb_game_info)).child(
 								getString(R.string.fb_game_info_password));
 
 						passwordRef.addListenerForSingleValueEvent(new ValueEventListener() {
 							@Override
 							public void onDataChange(DataSnapshot snap) {
 								
 								Log.d("RA", "checking if password correct");
 								
 								String password = (String)snap.getValue();
 
 								if (!password.equals(gamePassword)) {
 									Toast.makeText(PVPLobbyActivity.this, R.string.error_game_password_no_match,
 											Toast.LENGTH_SHORT).show();
 									mJoinRoomPassword.setText("");
 									return;
 								}
 								
 								Log.d("RA", "this is where we go to the locker room");
 
 								gameRef.child(getString(R.string.fb_game_player_joiner))
 										.child(getString(R.string.fb_game_player_is_connected)).setValue(Boolean.TRUE);
 
 								Intent lockerRoomIntent = new Intent(PVPLobbyActivity.this, LockerRoomActivity.class);
 
 								lockerRoomIntent.putExtra(ArenaActivity.KEY_GAME_ID, gameRef.getName());
 								lockerRoomIntent.putExtra(ArenaActivity.KEY_PLAYER_ID,
 										getString(R.string.fb_game_player_joiner));
 
 								Log.d(MainMenuActivity.RA, "Starting locker room by JOIN game button");
 								startActivity(lockerRoomIntent);
 							}
 
 							@Override
 							public void onCancelled() {}
 						});
 					}
 
 					@Override
 					public void onCancelled() {
 
 					}
 				});
 			}
 		});
 
 		create_button.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View v) {
 
 				final String gameName = mCreateRoomName.getText().toString();
 				final String gamePassword = mCreateRoomPassword.getText().toString();
 				final boolean allowSpectators = mAllowSpectators.isChecked();
 
 				Log.d(GAME_SETUP, "gameName: " + gameName);
 				Log.d(GAME_SETUP, "gamePass: " + gamePassword);
 
 				if (gameName.isEmpty()) {
 					Toast.makeText(PVPLobbyActivity.this, R.string.error_game_name_empty, Toast.LENGTH_SHORT).show();
 					return;
 				}
 
 				final Firebase gameRef = mRef.child(gameName);
 
 				gameRef.addListenerForSingleValueEvent(new ValueEventListener() {
 					@Override
 					public void onDataChange(DataSnapshot snap) {
 						if (snap.getValue() != null) {
 							Toast.makeText(PVPLobbyActivity.this, R.string.error_game_name_exists, Toast.LENGTH_SHORT)
 									.show();
 							mCreateRoomName.setText("");
 							mCreateRoomPassword.setText("");
 							return;
 						}
 
 						String gameRefName = createNewGame(gameName, gamePassword, allowSpectators);
 
 						Intent lockerRoomIntent = new Intent(PVPLobbyActivity.this, LockerRoomActivity.class);
 						lockerRoomIntent.putExtra(ArenaActivity.KEY_PLAYER_ID, getString(R.string.fb_game_player_creator));
 						lockerRoomIntent.putExtra(ArenaActivity.KEY_GAME_ID, gameRefName);
 
 						Log.d(MainMenuActivity.RA, "Starting locker room by CREATE game button");
 						startActivity(lockerRoomIntent);
 					}
 
 					@Override
 					public void onCancelled() {}
 				});
 			}
 		});
 	}
 
 	private String createNewGame(String gameName, String gamePassword, boolean allowSpectators) {
 		// Create new game
 		Firebase gameRef = mRef.child(gameName);
 		Log.d(GAME_SETUP, "New game created at " + gameRef.getName());
 
 		// Create info object for the game
 		Firebase infoRef = gameRef.child(getString(R.string.fb_game_info));
 		infoRef.child(getString(R.string.fb_game_info_game_running)).setValue(Boolean.FALSE);
 		infoRef.child(getString(R.string.fb_game_info_allow_spectators)).setValue(allowSpectators);
 		infoRef.child(getString(R.string.fb_game_info_password)).setValue(gamePassword);
 		Log.d(GAME_SETUP, "Info object created at " + infoRef.getName());
 
 		createPlayer(gameRef, getString(R.string.fb_game_player_creator));
 		createPlayer(gameRef, getString(R.string.fb_game_player_joiner));
 
 		return gameRef.getName();
 	}
 
 	private void createPlayer(Firebase gameRef, String playerId) {
 		// Make the player
 		Firebase playerRef = gameRef.child(playerId);
 
 		// Create player stats / statuses
 		playerRef.child(getString(R.string.fb_game_player_health)).setValue(Integer.valueOf(100));
 		playerRef.child(getString(R.string.fb_game_player_is_ready)).setValue(Boolean.FALSE);
 		playerRef.child(getString(R.string.fb_game_player_is_connected)).setValue(Boolean.FALSE);
 		playerRef.child(getString(R.string.fb_game_player_left_actions_allowed)).setValue(Boolean.FALSE);
 		playerRef.child(getString(R.string.fb_game_player_right_actions_allowed)).setValue(Boolean.FALSE);
 		playerRef.child(getString(R.string.fb_game_player_blocking)).setValue(Boolean.FALSE);
 
 		// Set all action counters to 0
 		playerRef.child(getString(R.string.fb_game_player_block)).setValue(Integer.valueOf(0));
 		playerRef.child(getString(R.string.fb_game_player_left_jab)).setValue(Integer.valueOf(0));
 		playerRef.child(getString(R.string.fb_game_player_left_hook)).setValue(Integer.valueOf(0));
 		playerRef.child(getString(R.string.fb_game_player_left_uppercut)).setValue(Integer.valueOf(0));
 		playerRef.child(getString(R.string.fb_game_player_right_jab)).setValue(Integer.valueOf(0));
 		playerRef.child(getString(R.string.fb_game_player_right_hook)).setValue(Integer.valueOf(0));
 		playerRef.child(getString(R.string.fb_game_player_right_uppercut)).setValue(Integer.valueOf(0));
 
 		Log.d(GAME_SETUP, "New player " + playerId + " created at " + playerRef.getName());
 	}
 
 }
