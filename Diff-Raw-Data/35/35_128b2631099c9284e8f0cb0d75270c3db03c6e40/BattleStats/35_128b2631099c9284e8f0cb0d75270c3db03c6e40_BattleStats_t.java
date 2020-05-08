 package riskyspace.logic;
 
 import java.util.List;
 
 import riskyspace.model.Fleet;
 import riskyspace.model.Player;
 
 /**
  * A class for reporting the results of a battle - the model is not altered.
  * 
  */
 public class BattleStats {
 	
 	private Player winner = null;
 	private boolean colonyDestroyed = false;
 	List<Fleet> destroyedFleets = null;
 	
 	
 	public void setDestroyedFleets(List<Fleet> destroyedFleets) {
 		this.destroyedFleets = destroyedFleets;
 	}
 
 	public List<Fleet> getDestroyedFleets() {
 		return destroyedFleets;
 	}
 
 	public Player getWinner() {
 		return winner;
 	}
 
 	public void setWinner(Player winner) {
 		this.winner = winner;
 	}
 
 	public boolean isColonyDestroyed() {
 		return colonyDestroyed;
 	}
 
 	public void setColonyDestroyed(boolean colonyDestroyed) {
 		this.colonyDestroyed = colonyDestroyed;
 	}
 
	public String getWinnerString() {
		return getWinner().toString().substring(0, 1) + getWinner().toString().substring(1).toLowerCase() + " has won the battle!";
	}
}
