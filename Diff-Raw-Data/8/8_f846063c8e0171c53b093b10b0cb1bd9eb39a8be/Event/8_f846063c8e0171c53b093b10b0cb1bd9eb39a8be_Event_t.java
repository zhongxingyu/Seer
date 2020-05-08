 package cz.muni.fi.publishsubscribe.matchingtree;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Event {
 
	private List<Attribute<? extends Comparable<?>>> attributes = new ArrayList<>();
 
 	public boolean addAttribute(Attribute<? extends Comparable<?>> attribute) {
		return this.attributes.add(attribute);
 	}
 
	public List<Attribute<? extends Comparable<?>>> getAttributes() {
 		return attributes;
 	}
 
 }
