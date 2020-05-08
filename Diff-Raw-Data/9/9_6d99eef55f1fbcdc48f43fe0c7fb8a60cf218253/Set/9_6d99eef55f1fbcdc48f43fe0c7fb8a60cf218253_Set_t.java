 package edu.ucsf.rbvi.setsApp.internal;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.cytoscape.model.CyIdentifiable;
 
 public class Set <T extends CyIdentifiable> {
 	private String name;
 	private HashMap<Long, T> set;
 	
 	public Set(String localName) {
 		initMethod(localName);
 	}
 	
 	public Set(String localName, List<T> cyIds) {
 		initMethod(localName);
 		addList(cyIds);
 	}
 	
 	private void initMethod(String localName) {
 		name = localName;
 		set = new HashMap<Long, T>();
 	}
 	
 	public void addList(List<T> cyIds) {
 		Iterator<T> iterator = cyIds.iterator();
 		while (iterator.hasNext()) {
 			T cyId = iterator.next();
 			set.put(cyId.getSUID(), cyId);
 		}
 	}
 	
 	public void add(T cyId) {
 		set.put(cyId.getSUID(), cyId);
 	}
 	
 	public void remove(T cyId) {
 		set.remove(cyId.getSUID());
 	}
 	public Set<T> intersection(String newName, Set<T> s) {
 		Set<T> newSet = new Set<T>(newName);
 		Iterator<T> sValues = s.getElements();
 		while (sValues.hasNext()) {
 			T curValue = sValues.next();
 			if (set.containsKey(curValue.getSUID()))
 				newSet.add(curValue);
 		}
 		return newSet;
 	}
 	
 	public Set<T> union(String newName, Set<T> s) {
 		Set<T> newSet = new Set<T>(newName);
 		Iterator<T> sValues = s.getElements(), thisValue = getElements();
 		while (sValues.hasNext())
 			newSet.add(sValues.next());
 		while (thisValue.hasNext())
 			newSet.add(thisValue.next());
 		return newSet;
 	}
 	
 	public Set<T> difference(String newName, Set<T> s) {
 		Set<T> newSet = new Set<T>(newName);
 		Iterator<T> sValues = s.getElements();
 		while (sValues.hasNext()) {
 			T curValue = sValues.next();
			if (! set.containsKey(((CyIdentifiable) curValue).getSUID()))
 				newSet.add(curValue);
 		}
 		return newSet;
 	}
 	
 	public Iterator<T> getElements() {
 		return set.values().iterator();
 	}
 
 	public Set<? extends CyIdentifiable> unionGeneric(String newName,
 			Set<? extends CyIdentifiable> set2) {
 		return union(newName, (Set<T>) set2);
 	}
 
 	public Set<? extends CyIdentifiable> intersectionGeneric(String newName,
 			Set<? extends CyIdentifiable> set2) {
 		return intersection(newName, (Set<T>) set2);
 	}
 
 	public Set<? extends CyIdentifiable> differenceGeneric(String newName,
 			Set<? extends CyIdentifiable> set2) {
 		return difference(newName, (Set<T>) set2);
 	}
 	
 	
 }
