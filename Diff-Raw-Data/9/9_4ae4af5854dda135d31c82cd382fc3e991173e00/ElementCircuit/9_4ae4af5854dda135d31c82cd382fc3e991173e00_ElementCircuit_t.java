 package aeroport.sgbag.kernel;
 
 import java.util.*;
 import lombok.*;
 
 @EqualsAndHashCode
 @ToString(exclude="listeChariot")
 public abstract class ElementCircuit {
 	
 	private static int counter = 0;
 	@Getter
 	final int id;
 
 	@Getter
 	protected LinkedList<Chariot> listeChariot;
 
 	public abstract Boolean update();
 
 	public ElementCircuit() {
 		super();
 		listeChariot = new LinkedList<Chariot>();
 		id = counter;
 		counter++;
 	}
 
 	public Boolean registerChariot(Chariot chariot) {
 		int oldSize = listeChariot.size();
 
 		listeChariot.addFirst(chariot);
 
 		return (listeChariot.size() == oldSize + 1);
 	}
 
 	public Boolean unregisterChariot() {
 		int oldSize = listeChariot.size();
		
		listeChariot.getLast().setPosition(0); //Et oui
 
 		listeChariot.removeLast();
 
 		return (listeChariot.size() == oldSize - 1);
 	}
 
 	public Boolean hasChariot() {
 		return (listeChariot.size() > 0);
 	}
 
 }
