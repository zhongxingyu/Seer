 package edu.ucsf.rbvi.setsApp.internal.model;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.cytoscape.model.CyEdge;
 import org.cytoscape.model.CyIdentifiable;
 import org.cytoscape.model.CyNetwork;
 import org.cytoscape.model.CyNode;
 
 /**
  * A set object consists of a set of CyNodes or CyEdges,
  * and some metadata (name and assocaited network).  For
  * simplicity, we also track the type of Set object this is.
  */
 public class Set <T extends CyIdentifiable> {
 	private String name;
 	private CyNetwork network;
 	private Class<? extends T> setType;
 	private HashMap<Long, T> set;
 	
 	/**
  	 * This constructor returns an empty set.
  	 *
  	 * @param localName the name of the set
  	 * @param network the network this set refers to
  	 * @param setType the type of the set (CyNode.class, CyEdge.class)
  	 */
 	public Set(String localName, CyNetwork network, Class<? extends T> setType) {
 		this(localName, network, setType, null);
 	}
 	
 	/**
  	 * This constructor returns a set with the associated objects
  	 *
  	 * @param localName the name of the set
  	 * @param network the network this set refers to
  	 * @param setType the type of the set (CyNode.class, CyEdge.class)
  	 * @param cyIds the nodes or edges to add as part of this set
  	 */
 	public Set(String localName, CyNetwork network, Class<? extends T> setType, List<T> cyIds) {
 		name = localName;
 		this.network = network;
 		this.setType = setType;
 		set = new HashMap<Long, T>();
		addElements(cyIds);
 	}
 	
 	/**
  	 * Add a list of nodes or edges to an existing set.  Note that the types must
  	 * match!
  	 *
  	 * @param cyIds the list of nodes or edges to add
  	 */
 	public void addElements(List<T> cyIds) {
 		for (T cyId: cyIds) {
 			if (network.getRow(cyId) != null) {
 				set.put(cyId.getSUID(), cyId);
 			}
 		}
 	}
 
 	/**
  	 * Add a list of nodes or edges to an existing set using only their SUIDs.
  	 *
  	 * @param cyIds the list of IDs to add
  	 */
 	public void addElementsByID(List<Long> cyIds) {
 		for (Long cyId: cyIds) {
 			if (setType.equals(CyNode.class) && network.getNode(cyId) != null) {
 				set.put(cyId, (T)network.getNode(cyId));
 			} else if (network.getEdge(cyId) != null) {
 				set.put(cyId, (T)network.getEdge(cyId));
 			}
 		}
 	}
 	
 	/**
  	 * Return the list of elements in this set
  	 *
  	 * @return element list
  	 */
 	public Collection<T> getElements() {
 		return set.values();
 	}
 
 	/**
  	 * Return the name of the set
  	 *
  	 * @return the set name
  	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
  	 * Set the name of this set
  	 *
  	 * @param newName the new name for the set
  	 */
 	public void setName(String newName) {
 		name = newName;
 	}
 
 	/**
  	 * Return the type of the set.  This can not be changed
  	 *
  	 * @return the set type (CyNode.class or CyEdge.class)
  	 */
 	public Class<? extends CyIdentifiable> getType() {
 		return setType;
 	}
 
 	/**
  	 * Return the network for this set
  	 *
  	 * @return the network for the set
  	 */
 	public CyNetwork getNetwork() {
 		return network;
 	}
 
 	/**
  	 * Set the network for this set
  	 *
  	 * @param network the network for the set
  	 */
 	public void setNetwork(CyNetwork network) {
 		this.network = network;
 	}
 	
 	/**
  	 * Add a node or edge to the set
  	 *
  	 * @param cyId the node or edge to add
  	 * @return true of the addition succeeded
  	 */
 	public boolean add(CyIdentifiable cyId) {
 		if (! set.containsKey(cyId.getSUID())) {
 			set.put(cyId.getSUID(), (T)cyId);
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	/**
  	 * Remove a node or edge from the set
  	 *
  	 * @param cyId the node or edge to remove
  	 * @return true of the addition succeeded
  	 */
 	public boolean remove(CyIdentifiable cyId) {
 		if (set.containsKey(cyId.getSUID())) {
 			set.remove(cyId.getSUID());
 			return true;
 		}
 		else return false;
 	}
 	
 	/**
  	 * Test to see if the node or edge with this ID is part of the set
  	 *
  	 * @param cyId the ID to search for
  	 * @return true if the ID is part of this set, false otherwise
  	 */
 	public boolean hasCyId(Long cyId) {
 		if (set.get(cyId) != null) return true;
 		else return false;
 	}
 	
 	/**
  	 * Return a set made up from the intersection of this set and another
  	 *
  	 * @param newName the name of the intersection set
  	 * @param s the set to intersect with
  	 * @return the new set formed by intersecting this set with the argument
  	 * @throws Exception if the argument set is not in the same network or not the same type
  	 */
 	public Set<T> intersection(String newName, Set<? extends CyIdentifiable> s) throws Exception {
 		sanityCheck(s);
 		Set<T> newSet = new Set<T>(newName, network, setType);
 		Collection<T> sValues = (Collection<T>)s.getElements();
 		for (T curValue: sValues) {
 			if (set.containsKey(curValue.getSUID()))
 				newSet.add(curValue);
 		}
 		return newSet;
 	}
 	
 	/**
  	 * Return a set made up from the union of this set and another
  	 *
  	 * @param newName the name of the union set
  	 * @param s the set to join with
  	 * @return the new set formed by joining this set with the argument
  	 * @throws Exception if the argument set is not in the same network or not the same type
  	 */
 	public Set<T> union(String newName, Set<? extends CyIdentifiable> s) throws Exception {
 		sanityCheck(s);
 		Set<T> newSet = new Set<T>(newName, network, setType);
 		Collection<T> sValues = (Collection<T>)s.getElements(), thisValue = getElements();
 		for (T value: sValues)
 			newSet.add(value);
 		for (T value: thisValue)
 			newSet.add(value);
 		return newSet;
 	}
 	
 	/**
  	 * Return a set made up from the difference of this set and another
  	 *
  	 * @param newName the name of the difference set
  	 * @param s the set to test for the difference
  	 * @return the new set formed by determining the difference between this set with the argument
  	 * @throws Exception if the argument set is not in the same network or not the same type
  	 */
 	public Set<T> difference(String newName, Set<? extends CyIdentifiable> s) throws Exception {
 		sanityCheck(s);
 		Set<T> newSet = new Set<T>(newName, network, setType);
 		Collection<T> sValues = (Collection<T>)s.getElements();
 		for (T curValue: sValues) {
 			if (! set.containsKey(((CyIdentifiable) curValue).getSUID()))
 				newSet.add(curValue);
 		}
 		return newSet;
 	}
 
 	private void sanityCheck(Set<? extends CyIdentifiable> s) throws Exception {
 		if (!s.getType().equals(setType))
 			throw new Exception("Types for '"+name+"' and '"+s.getName()+"' are not the same");
 		if (!s.getNetwork().equals(network))
 			throw new Exception("Networks for '"+name+"' and '"+s.getName()+"' are not the same");
 	}
 
 }
