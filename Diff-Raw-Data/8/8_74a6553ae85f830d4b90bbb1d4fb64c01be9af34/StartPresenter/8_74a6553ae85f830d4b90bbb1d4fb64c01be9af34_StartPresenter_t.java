 package com.example.wecharades.presenter;
 
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import android.util.Log;
 
 import com.example.wecharades.model.DatabaseException;
 import com.example.wecharades.model.Game;
 import com.example.wecharades.model.Invitation;
 import com.example.wecharades.model.Player;
 import com.example.wecharades.views.StartActivity;
 
 /**
  * 
  * @author Alexander
  *
  */
 public class StartPresenter extends Presenter {
 
 	private StartActivity activity;
 	private LinkedHashMap<String, ArrayList<Game>> listMap;
 	private final static String [] headers = {"Your turn", "Opponent's turn", "Finished games"};
 	private Map<Game, Map<Player, Integer>> score;
 
 	public StartPresenter(StartActivity activity) {
 		super(activity);
 		this.activity = activity;
 	}
 
 	public void update(){
 		String string = dc.getCurrentPlayer().getName();
 		activity.setAccountName(string);
 		listMap = new LinkedHashMap<String, ArrayList<Game>>();
 		score = new TreeMap<Game, Map<Player, Integer>>();
 		parseList();
 		setInvitationStatus();
 	}
 
 	/**
 	 * Check if the there is a user logged in. 
 	 * 	Will call the activity and update username if this is true
 	 * 
 	 */
 	public void checkLogin() {
 		if(dc.getCurrentPlayer() == null){
 			goToLoginActivity();
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private void parseList() {
 		try {
 			ArrayList<Game> gameList = dc.getGames();
 			for (String s : headers) {
 				listMap.put(s, new ArrayList<Game>());
 			}
 
 			for (Game g : gameList) {				
				score.put(g, dc.getGameScore(g));
 				if (g.isFinished())
 					listMap.get("Finished games").add(g);
 				else if (g.getCurrentPlayer().equals(dc.getCurrentPlayer()) && !g.isFinished())
 					listMap.get("Your turn").add(g);
 				else
 					listMap.get("Opponent's turn").add(g);
 			}
 
 		} catch (DatabaseException e){
 			Log.d("DATABASE ERROR",e.getMessage());
 			activity.showMessage(e.prettyPrint());
 		}
 	}
 
 	/**
 	 * 
 	 * @param adapter
 	 * @return
 	 */
 	public SeparatedListAdapter setAdapter(SeparatedListAdapter adapter) {
 		for (String s : headers) {
 			if(!listMap.get(s).isEmpty()) {
 				adapter.addSection(s, new GameAdapter(activity, listMap.get(s), dc.getCurrentPlayer(), score));		
 			}
 		}
 		return adapter;
 	}
 
 	/**
 	 * 
 	 */
 	private void setInvitationStatus() {
 		try {
 			ArrayList<Invitation> invites = dc.getInvitations();
 			activity.setInvitations(invites.size());
 		}catch (DatabaseException e){
 			activity.showMessage(e.prettyPrint());
 		}
 
 
 	}
 
 	/**
 	 * Log out the current user
 	 */
 	public void logOut() {
 		dc.logOutPlayer(activity);
 		goToLoginActivity();
 	}
 
 }
