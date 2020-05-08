 package com.testgame.player;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 
 import android.graphics.Point;
 import android.util.Log;
 
 import com.testgame.OnlineGame;
 import com.testgame.mechanics.unit.AUnit;
 import com.testgame.mechanics.unit.Base;
 import com.testgame.mechanics.unit.Ditz;
 import com.testgame.mechanics.unit.Jock;
 import com.testgame.mechanics.unit.Nerd;
 
 public class ComputerPlayer extends APlayer {
 	
 	JSONArray actionsToPerform;
 	OnlineGame game;
 	
 	public ComputerPlayer(String name) {
 		super(name);
 	}
 	
 	public void startTurn(JSONArray array){
 		Log.d("Array", array.length()+"");
 		this.actionsToPerform = array;
 		this.beginTurn();
 		performNext(); // perform all of the animations
 		
 		
 		
 		
 		
 	}
 	
 	public void performNext() {
 		if(actionsToPerform.length() == 0){
 			if(game.isFirstTurn()) 
 				game.incrementCount();
			
 			this.endTurn();
 			 // this calls turn init on all the units
 			
 			game.getGameScene().activity.runOnUiThread(new Runnable() {
         	    @Override
         	    public void run() {
         	    	game.getGameScene().textMenu("Your Turn!");
           			 
         	    }
         	});
 		}
 		for (int i = 0; i < actionsToPerform.length(); i++) {
 			
 
 			if (actionsToPerform.isNull(i)) {
 				if (i == actionsToPerform.length() - 1) {
 					if(game.isFirstTurn()) 
 						game.incrementCount();
 					this.endTurn();
 					 // this calls turn init on all the units
					
 					game.getGameScene().activity.runOnUiThread(new Runnable() {
 		        	    @Override
 		        	    public void run() {
 		        	    	game.getGameScene().textMenu("Your Turn!");
 		          			 
 		        	    }
 		        	});
 				}
 				else continue; // performed this action already.
 			}
 			
 			else {
 		
 				try {
 					
 					JSONObject nextAction = actionsToPerform.getJSONObject(i);
 					String moveType = nextAction.getString("MoveType");
 					
 					int unitX = nextAction.getInt("UnitX");
 					int unitY = nextAction.getInt("UnitY");
 					
 					AUnit unit = game.gameMap.getOccupyingUnit(unitX, unitY);
 					
 					if (moveType.equals("MOVE")) {
 						Log.d("Moving", "Moving");
 						
 						int destX = nextAction.getInt("DestX");
 						int destY = nextAction.getInt("DestY");
 						int energy = nextAction.getInt("Energy");
 						
 						actionsToPerform.put(i, null); // finished action, clear it out
 						
 						unit.ComputerMove(destX, destY, energy, this);
 					}
 					
 					else if (moveType.equals("ATTACK")) {
 						
 						
 						int targetX = nextAction.getInt("OppX");
 						int targetY = nextAction.getInt("OppY");
 						int energy = nextAction.getInt("Energy");
 						int attack = nextAction.getInt("Attack");
 						
 						AUnit target = game.gameMap.getOccupyingUnit(targetX, targetY);
 						
 						actionsToPerform.put(i, null); // finished action, clear it out
 						Log.d("Target", target+"");
 						Log.d("Attack", attack+"");
 						Log.d("Unit", unit+"");
 						Log.d("Energy", energy+"");
 						Log.d("Unit2", this+"");
 						unit.ComputerAttack(target, attack, energy, this);
 					}
 					
 					return;
 					
 				} catch (JSONException e) {
 					// Failure getting the next move
 					e.printStackTrace();
 				}
 			}
 		}
 		
 	}
 	
 	public void init(JSONObject object) {
 		int nerds = 0;
 		int jocks = 0;
 		int ditz = 0;
 		
 		
 		try {
 			ditz = object.getInt("Ditzes");
 			nerds = object.getInt("Nerds");
 			jocks = object.getInt("Jocks");
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Point[] spawns;
 		
 		if(game.isFirstTurn()){
 			spawns = game.resourcesManager.getSpawn1(game.resourcesManager.mapString);
 		}
 		else{
 			spawns = game.resourcesManager.getSpawn2(game.resourcesManager.mapString);
 		}
 		
 		
 			for(Point i : spawns){
 				if(nerds > 0){
 					AUnit unit = new Nerd(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unit.init(); 
 					game.getCompPlayer().addUnit(unit);
 					nerds--;
 				}
 				else if(ditz > 0){
 					AUnit unit = new Ditz(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unit.init();
 					game.getCompPlayer().addUnit(unit);
 					ditz--;
 				}
 				else if(jocks > 0){
 					AUnit unit = new Jock(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unit.init(); 
 					game.getCompPlayer().addUnit(unit);
 					jocks--;
 				}
 				else{
 					AUnit unitbase = new Base(game.gameMap, i.x, i.y, game.getGameScene(), "red");
 					unitbase.init();
 					game.getCompPlayer().setBase(unitbase);
 				}
 			}
 			
 			
 		
 		
 		game.incrementCount();
 		if(game.isFirstTurn()) {
 			
 			game.getGameScene().activity.runOnUiThread(new Runnable() {
         	    @Override
         	    public void run() {
         	    	game.getGameScene().textMenu("Your Turn!");
           			 
         	    }
         	});
 		}
 		
 	}
 	
 	public void setGame(OnlineGame game){
 		this.game = game;
 	}
 
 }
