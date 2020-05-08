 package aeroport.sgbag.kernel;
 
import java.util.LinkedList;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
 
 @EqualsAndHashCode
 public abstract class ElementCircuit {
 
 	@Getter
 	protected LinkedList<Chariot> listeChariot;
 
 	public abstract Boolean update();
 
 	public ElementCircuit() {
 		super();
 		listeChariot = new LinkedList<Chariot>();
 	}
 
 	public Boolean registerChariot(Chariot chariot) {
 		int oldSize = listeChariot.size();
 
 		listeChariot.addFirst(chariot);
 
 		return (listeChariot.size() == oldSize + 1);
 	}
 
 	public Boolean unregisterChariot() {
 		int oldSize = listeChariot.size();
 
 		listeChariot.removeLast();
 
 		return (listeChariot.size() == oldSize - 1);
 	}
 
 	public Boolean hasChariot() {
 		return (listeChariot.size() > 0);
 	}
 
 }
