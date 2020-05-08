 package btlshp.entities;
 
public class SaveGame {
 	Map savedMap;
 	int savingPlayerID;
 	
 	public SaveGame(Map gameMap, int currentPlayerID){
 		savedMap = gameMap;
 		savingPlayerID = currentPlayerID;
 	}
 	public Map getMap(){
 		return savedMap;
 	}
 	public int getSavingPlayerID(){
 		return savingPlayerID;
 	}
 }
