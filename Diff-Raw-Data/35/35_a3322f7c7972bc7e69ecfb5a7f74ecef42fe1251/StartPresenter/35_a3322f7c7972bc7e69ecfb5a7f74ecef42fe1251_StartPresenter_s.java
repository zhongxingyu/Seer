 package com.example.wecharades.presenter;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import android.app.Activity;
 import android.util.Log;
 
 import com.example.wecharades.GameAdapter;
 import com.example.wecharades.SeparatedListAdapter;
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.views.StartActivity;
 import com.parse.ParseUser;
 
 /**
  * 
  * @author Alexander
  *
  */
 public class StartPresenter extends Presenter {
 	
 	private static final String TAG = "Start Presenter";
 	private StartActivity activity;
 	private Map<String, ArrayList<Game>> separatedList;
 	
 	
	public StartPresenter(Activity activity) {
 		super(activity);
		this.activity = (StartActivity) activity;
 		separatedList = new HashMap<String, ArrayList<Game>>();
 		
 		//Checks if the there is any user logged in
 		checkLogin();
 	}
 	
 	public void update(){
 		activity.setDisplayName(model.getCurrentPlayer().getName());
 	}
 	
 	/**
 	 * Check if the there is a user logged in. 
 	 * 	Will call the activity and update username if this is true
 	 * 
 	 */
 	public void checkLogin() {
 		if(getCurrentUser() == null){
 			goToLoginActivity();
 		} else{
 			model.setCurrentPlayer();//TODO should this be removed after we have created a more persistent model?
 		}
 	}
     
 	/**
 	 * 
 	 */
 	private void parseGameLists() {
 		try {
 			//TODO This is ugly and should not be here later
 			ArrayList<Game> gameList = db.getGames(model.getCurrentPlayer());
 	        for (Game g : gameList) {
 	        	if (g.isFinished())
 	        		putInList("Finished games", g);
 	        	else if (g.getCurrentPlayer().getName().toLowerCase().equals(getCurrentUser()) && !g.isFinished())
 	        		putInList("Opponent's turn", g);        
 	        	else
 	        		putInList("Your turn", g);
 	        }
 	        
 		} catch (DatabaseException e){
 			Log.d(TAG, e.getMessage());
 		}
 	}
 
 	private void putInList(String s, Game g) {
 		separatedList.put(s, new ArrayList<Game>());
 		separatedList.get(s).add(g);
 	}
 
 	/**
 	 * 
 	 * @param adapter
 	 * @return
 	 */
 	public SeparatedListAdapter setAdapter(SeparatedListAdapter adapter) {
 		parseGameLists();
 		// TODO: Sortera listan
 		for (String s : separatedList.keySet()) {
 			adapter.addSection(s, new GameAdapter(activity.getApplicationContext(), separatedList.get(s)));		
 		}
 	
 		return adapter;
 	}
 	
 	/**
 	 * Log out the current user
 	 */
 	public void logOut() {
 		ParseUser.logOut();
 		model.logOutCurrentPlayer();
 		goToLoginActivity();
 	}
 	
 }
