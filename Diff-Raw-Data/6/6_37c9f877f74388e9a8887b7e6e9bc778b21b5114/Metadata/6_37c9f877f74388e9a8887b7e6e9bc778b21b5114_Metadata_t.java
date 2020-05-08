 package uk.ac.nott.mrl.homework.server.model;
 
 import java.util.Collection;
 import java.util.Map;
 
import com.google.gson.annotations.Expose;

 public class Metadata
 {
	@Expose
 	private Map<String, String> owners;
	@Expose
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
