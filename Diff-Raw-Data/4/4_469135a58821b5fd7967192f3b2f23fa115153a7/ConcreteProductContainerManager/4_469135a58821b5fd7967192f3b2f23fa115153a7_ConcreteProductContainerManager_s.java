 package model;
 
 import java.io.Serializable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 @SuppressWarnings("serial")
 public class ConcreteProductContainerManager extends Observable implements Serializable,
 		ProductContainerManager {
 	private final Set<StorageUnit> rootStorageUnits;
 	private final Map<String, StorageUnit> nameToStorageUnit;
 
 	/**
 	 * Constructor
 	 * 
 	 */
 	public ConcreteProductContainerManager() {
 		rootStorageUnits = new TreeSet<StorageUnit>();
 		nameToStorageUnit = new TreeMap<String, StorageUnit>();
 	}
 
 	@Override
 	public StorageUnit getRootStorageUnitByName(String productGroupName) {
 		for (StorageUnit su : rootStorageUnits) {
 			if (su.containsProductGroup(productGroupName))
 				return su;
 		}
 		return null;
 	}
 
 	/**
 	 * Gets the StorageUnit with the given name.
 	 * 
 	 * @return The StorageUnit with the given name, or null, if not found
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public StorageUnit getStorageUnitByName(String name) {
 		return nameToStorageUnit.get(name);
 	}
 
 	@Override
 	public Iterator<StorageUnit> getStorageUnitIterator() {
 		return rootStorageUnits.iterator();
 	}
 
 	@Override
 	public boolean isValidProductGroupName(String productGroupName) {
 		for (StorageUnit su : rootStorageUnits) {
 			if (su.containsProductGroup(productGroupName))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Determines whether the specified Storage Unit name is valid for adding a new Storage
 	 * Unit.
 	 * 
 	 * @param name
 	 *            The name to be tested
 	 * @return true if name is valid, false otherwise
 	 * 
 	 * @pre true
 	 * @post true
 	 */
 	@Override
 	public boolean isValidStorageUnitName(String name) {
 		if (name == null || name.equals(""))
 			return false;
 
 		// From the Data Dictionary: Must be non-empty. Must be unique among all
 		// Storage Units.
 
 		for (StorageUnit su : rootStorageUnits) {
 			if (name.equals(su.getName().toString()))
 				return false;
 		}
 
 		return true;
 	}
 
 	/**
 	 * If pc is a StorageUnit, it is added to the list of StorageUnits managed. Notifies
 	 * observers of a change.
 	 * 
 	 * @param pc
 	 *            ProductContainer to be managed
 	 * 
 	 * @pre pc != null
 	 * @post if(pc instanceof StorageUnit) getByName(pc.getName()) == pc
 	 */
 	@Override
 	public void manage(ProductContainer pc) {
 		if (pc instanceof StorageUnit) {
 			StorageUnit storageUnit = (StorageUnit) pc;
 			rootStorageUnits.add(storageUnit);
 			nameToStorageUnit.put(storageUnit.getName(), storageUnit);
 		}
 		setChanged();
 		this.notifyObservers(pc);
 	}
 
 	/**
 	 * 
 	 * @param name
 	 * @param su
 	 */
 	@Override
 	public void setStorageUnitName(String name, StorageUnit su) {
 		if (name.equals(su.getName()))
 			return;
 		if (!isValidStorageUnitName(name))
 			throw new IllegalArgumentException("Illegal storage unit name");
 		unmanage(su);
 		su.setName(name);
 		manage(su);
 	}
 
 	/**
 	 * Remove pc from set of managed objects and notify observers of a change.
 	 * 
 	 * @param pc
 	 *            ProductContainer to be unmanaged
 	 * 
 	 * @pre true
 	 * @post getByName(pc.getName()) == null
 	 */
 	@Override
 	public void unmanage(ProductContainer pc) {
 		if (pc instanceof StorageUnit) {
 			StorageUnit storageUnit = (StorageUnit) pc;
 			rootStorageUnits.remove(storageUnit);
 			nameToStorageUnit.remove(storageUnit.getName());
 		}
 		setChanged();
 		this.notifyObservers();
 	}
 }
