 package model;
 
 import java.util.HashSet;
 import java.util.Observable;
 import java.util.Set;
 
 public class Model extends Observable {
 
 	private Set<String> players;
	private int modelID;
 
 	/**
 	 * Creates a new model.
 	 */
 	public Model() {
 		this.players = new HashSet<String>();
 		this.modelID = 1;
 	}
 
 	/**
 	 * Adds player.
 	 * 
 	 * @param p
 	 *            Player to be added.
 	 */
 	public void addPlayer(String p) {
 		if (p != null) {
 			players.add(p);
 			setChanged();
 			notifyObservers();
 			clearChanged();
 		}
 	}
 
 	/**
 	 * Sets id to that provided.
 	 * 
 	 * @param id
 	 *            The new ID.
 	 */
 	public void setID(int id) {
 		if (id > 0) {
 			modelID = id;
 			setChanged();
 			notifyObservers();
 			clearChanged();
 		}
 	}
 
 	/**
 	 * Access method for the id.
 	 * 
 	 * @return Returns the modelID.
 	 */
 	public int getID() {
 		return modelID;
 	}
 
 	/**
 	 * Access method for the player Set.
 	 * 
 	 * @return Returns the player set.
 	 */
 	public Set<String> getPlayers() {
 		return new HashSet<String>(this.players);
 	}
 
 }
