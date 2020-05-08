 package ultraextreme.model.item;
 
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.List;
 
 import ultraextreme.model.entity.WeaponPickup;
 
 /**
  * In charge of storing and handling all pickups. (WeaponPickUpables)
  * 
  * @author Johan Gronvall
  * 
  */
 public class PickupManager {
 	private List<WeaponPickup> pickups;
 	private final PropertyChangeSupport pcs;
 
 	private static final String NEW_PICKUP = "add";
	private static final String REMOVE_PICKUP = "remove";
	
 
 	public PickupManager() {
 		pickups = new ArrayList<WeaponPickup>();
 		pcs = new PropertyChangeSupport(this);
 	}
 
 	/**
 	 * Adds a pickup to the list of pickups
 	 * 
 	 * @param pickup
 	 */
 	public void addPickup(WeaponPickup pickup) {
 		pickups.add(pickup);
 		pcs.firePropertyChange(NEW_PICKUP, null, pickup);
 
 	}
 
 	/**
 	 * Removes the assigned pickup
 	 * 
 	 * @param pickup
 	 *            item which is to be removed
 	 */
 	public void removePickUp(WeaponPickup pickup) {
		pcs.firePropertyChange(REMOVE_PICKUP, null, pickup);
 		pickups.remove(pickup);
 	}
 
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.addPropertyChangeListener(listener);
 	}
 
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		this.pcs.removePropertyChangeListener(listener);
 	}
 }
