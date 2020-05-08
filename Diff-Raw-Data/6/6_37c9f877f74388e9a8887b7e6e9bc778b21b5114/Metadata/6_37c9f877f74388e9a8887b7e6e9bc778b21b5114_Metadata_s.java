 package uk.ac.nott.mrl.homework.server.model;
 
 import java.util.Collection;
 import java.util.Map;
 
 public class Metadata
 {
 	private Map<String, String> owners;
 	private Collection<String> types;
 	
 	public Iterable<String> getTypes()
 	{
 		return types;
 	}
 	
 	public Map<String, String> getOwners()
 	{
 		return owners;
 	}
 }
