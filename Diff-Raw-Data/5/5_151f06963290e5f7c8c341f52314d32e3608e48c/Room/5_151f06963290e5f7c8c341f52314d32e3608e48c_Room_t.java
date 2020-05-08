 package model;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeSupport;
 
 import synclogic.SyncListener;
 
 public class Room implements SyncListener {
 	private int id, capacity;
 	private String name;
 	private PropertyChangeSupport pcs;
 	
 	//constants
 	public static final String NAME_PROPERTY_CLASSTYPE = "room";
 	public static final String NAME_PROPERTY_ID = "id";
 	public static final String NAME_PROPERTY_NAME = "name";
 	public static final String NAME_PROPERTY_CAPACITY = "cap";
 	
 	/**
 	 * Create a room with the following specification.
 	 * @param id
 	 * @param name
 	 * @param capacity
 	 */
 	public Room(int id, String name, int capacity) {
 		pcs = new PropertyChangeSupport(this);
 		this.id = id;
 		this.name = name;
 		this.capacity = capacity;
 	}
 	
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		int old = getId();
 		this.id = id;
 		pcs.firePropertyChange(new PropertyChangeEvent(this, NAME_PROPERTY_ID, old, getId()));
 	}
 	public int getCapacity() {
 		return capacity;
 	}
 	public void setCapacity(int capacity) {
 		int old = getCapacity();
 		this.capacity = capacity;
 		pcs.firePropertyChange(new PropertyChangeEvent(this, NAME_PROPERTY_CAPACITY, old, getCapacity()));
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		String old = getName();
 		this.name = name;
 		pcs.firePropertyChange(new PropertyChangeEvent(this, NAME_PROPERTY_NAME, old, getName()));
 	}
 
 	@Override
 	public void fire(SaveableClass classType, Object newVersion) {
 		Room room = (Room) newVersion;
 		setCapacity(room.getCapacity());
 		setId(room.getId());
 		setName(room.getName());
 		System.out.println("Room updated!");
 	}
 
 	@Override
 	public SaveableClass getSaveableClass() {
 		return SaveableClass.Room;
 	}
 
 	@Override
 	public String getObjectID() {
 		return "" + getId();
 	}
 	
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Room)) {
 			return false;
 		}
 		Room b = (Room) obj;
		System.out.println("this.name = " + this.name);
		System.out.println("b.name = " + b.name);
 		return (this.getId() == b.getId() && this.getCapacity() == b.getCapacity()
				/*&& this.getName().equals(b.getName())*/);
 	}
 	
 	public String toString(){
 		return this.name;
 	}
 }
