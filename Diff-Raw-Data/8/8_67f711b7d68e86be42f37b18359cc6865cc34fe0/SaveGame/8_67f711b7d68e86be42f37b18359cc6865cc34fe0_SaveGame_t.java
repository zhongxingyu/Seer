 package btlshp.entities;
 
import java.io.Serializable;

public class SaveGame implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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
