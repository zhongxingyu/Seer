 package com.cse454.nel.scoring;
 
import java.util.*;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.cse454.nel.Entity;
 import com.cse454.nel.EntityMention;
 
 public class Scorer {
 
 	private Map<String, Set<String>> results;
 	private Object lock = new Object();
	
 	public Scorer() {
 		results = new HashMap<String, Set<String>>();
 	}
	
 	public void ScoreResults(String docID, Map<EntityMention, Entity> entities) {
 		// Process Results
 
 		Set<String> values = new HashSet<String>();
 		for(Entity ent : entities.values())
 		{
 			if (ent != null) {
				values.add(ent.wikiID);
 			}
 		}
		
		synchronized (lock) {	
 			results.put(docID, values);
 			// Aggregate score
 		}
 	}
 
 	public void ScoreOverall() {
 		for(Entry<String, Set<String>> entry : results.entrySet()) {
 		    String key = entry.getKey();
 		    Set<String> values = entry.getValue();
		    
 		    System.out.println(key+"\t"+StringUtils.join(values, "\t"));
 		}
 	}
 }
