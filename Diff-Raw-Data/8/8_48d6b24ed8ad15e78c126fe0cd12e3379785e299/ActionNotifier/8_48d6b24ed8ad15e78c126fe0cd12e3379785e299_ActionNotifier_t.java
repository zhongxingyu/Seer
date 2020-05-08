 package game;
 
 import java.util.Observable;
 
 public class ActionNotifier extends Observable{
 
 	public void notifySubscribers() {
		setChanged();
 		notifyObservers();
 	}
 }
