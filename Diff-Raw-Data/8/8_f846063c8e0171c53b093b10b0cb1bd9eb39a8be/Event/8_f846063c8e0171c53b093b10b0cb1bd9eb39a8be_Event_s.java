 package cz.muni.fi.publishsubscribe.matchingtree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Event {
 
	private List<Attribute<Comparable<?>>> attributes = new ArrayList<>();
 
 	public boolean addAttribute(Attribute<? extends Comparable<?>> attribute) {
		return this.attributes.add((Attribute<Comparable<?>>) attribute);
 	}
 
	public List<Attribute<Comparable<?>>> getAttributes() {
 		return attributes;
 	}
 
 }
