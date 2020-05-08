 package com.gradugation;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.widget.ArrayAdapter;
 import android.widget.ListView;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import com.coordinates.MapCoordinate;
 
 import android.content.Intent;
 import android.database.sqlite.SQLiteDatabase;
 import android.util.Log;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.AdapterView;
 import android.widget.ImageView;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class ContinueActivity extends Activity {
 
 	private DbHelper dbhelper;
 	public int numberSavedGames=4;
 	private ArrayList<Character> thePlayers = new ArrayList<Character>();
     public static final String THE_PLAYERS = "com.gradugation.the_players";
 
 	private List<SavedGame> mySavedGames = new ArrayList<SavedGame>();
 
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_continue);
 		
 		
 
 		populateSavedGameList();
 		populateListView();
 		registerClickCallback();
 		
 	}
 
 	/*
 	 * replace savedGame1 string with string variable whose value is set after
 	 * saving game, get characterID in place of R.drawable.ic_launcher, get time
 	 * at moment of save
 	 */
 	private void populateSavedGameList() {
 		for(int i=0;i<numberSavedGames-1;i++){
 			mySavedGames.add(new SavedGame("saved game"+ i, R.drawable.ic_launcher,
 					"Last saved mm/dd 00:00"));
 		}
 	}
 
 	private void populateListView() {
 		ArrayAdapter<SavedGame> adapter = new MyListAdapter();
 		ListView list = (ListView) findViewById(R.id.savedGamesListView);
 		list.setAdapter(adapter);
 	}
 
 	private void registerClickCallback() {
 		
         
 		ListView list = (ListView) findViewById(R.id.savedGamesListView);
 		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
 
 			public void onItemClick(AdapterView<?> parent, View viewClicked,
 					int position, long id) {
 				LoadSavedGame(position);
 			}
 		});
 	}
 	
 	private void LoadSavedGame(int id){
 		String[] gameKey = { Integer.toString(id) };
 		int numCharacters=0;
 		//Open Database
         dbhelper = new DbHelper(this);
         SQLiteDatabase db = dbhelper.openDB();
         Log.d("TEST", "Database has been opened");
 		
         //Grab Game info
 		ArrayList gameList = dbhelper.getRow(1, gameKey);
 		
 		if (gameList.size()>0){
 			numCharacters = Integer.valueOf((String)gameList.get(2));
 
 		}else{
 			String message = "Unable to retrieve saved game.";
 			Toast.makeText(ContinueActivity.this, message, Toast.LENGTH_SHORT).show();
 		}
 		
 		
 		
 		
 		ArrayList characterList;
 
 		if (numCharacters != 0) {
 
 			// Grab each character info
 			for (int i = 0; i < (numCharacters); i++) {
 				String[] tempStringArray = { "" + (id << 2) + i }; // Game ID | Char ID = Char Index 
 				characterList = dbhelper.getRow(2, tempStringArray);
 				
 				String type = (String) characterList.get(1);
 				String name = (String) characterList.get(2);
 				// being loaded as map coordinates
 				int x = Integer.valueOf((String) characterList.get(3));
 				int y = Integer.valueOf((String) characterList.get(4));
 				MapCoordinate location = new MapCoordinate(x,y);
 				int credits = Integer.valueOf((String) characterList.get(5));
 				int coins = Integer.valueOf((String) characterList.get(6));
 				
				/// grab gameid
				int gameId = 0;
				
 				Log.d("characterstype", type);
				Character thePlayer = new Character(type.toUpperCase(), name, location.mapToSprite(), (id << 2) + i, gameId, credits, coins);
 				thePlayers.add(thePlayer);
 			}
 			// Close database
 			dbhelper.close();
 
 			// Go to main game screen
 			Intent intent = new Intent(this, MainGameScreen.class);
 			Bundle bundle = new Bundle();
 			bundle.putSerializable(THE_PLAYERS, thePlayers);
 			intent.putExtras(bundle);
 //			intent.putExtra(THE_PLAYERS, bundle);
 			startActivity(intent);
 
 		}
 		
         //ArrayList itemList = dbhelper.getRow(3, itemKey);
                 
 		//Close database
 		dbhelper.close();
 		
         
        
 	}
 
 	private class MyListAdapter extends ArrayAdapter<SavedGame> {
 		public MyListAdapter() {
 			super(ContinueActivity.this, R.layout.item_view, mySavedGames);
 
 		}
 
 		@Override
 		public View getView(int position, View convertView, ViewGroup parent) {
 			// Make sure we have a view to work with (may have been given null)
 			View itemView = convertView;
 			if (itemView == null) {
 				itemView = getLayoutInflater().inflate(R.layout.item_view,
 						parent, false);
 			}
 
 			SavedGame currentSavedGame = mySavedGames.get(position);
 
 			// Fill the view
 			ImageView imageView = (ImageView) itemView
 					.findViewById(R.id.item_icon);
 			imageView.setImageResource(currentSavedGame.getIconID());
 
 			TextView makeText = (TextView) itemView
 					.findViewById(R.id.item_txtSavedGame);
 			makeText.setText(currentSavedGame.getGameNumber());
 
 			TextView saveText = (TextView) itemView
 					.findViewById(R.id.item_txtSaveData);
 			saveText.setText(currentSavedGame.getTimeDate());
 
 			return itemView;
 		}
 	}
 	
 	public void incrementNumSaveGames(){
 		numberSavedGames++;
 	}
 	
 	public int getNumSaveGames(){
 		return numberSavedGames;
 	}
 }
