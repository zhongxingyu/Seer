 package com.example.locus.dht;
 
 import java.util.Set;
 import java.util.List;
 

 import com.example.locus.entity.Result;
 import com.example.locus.entity.User;
 
 public class DHTFacade implements IDHT {
 
 	private static DHTFacade instance = null ; 
 	private chordDHT chord ; 
 	
 	private DHTFacade() { 
 		chord = new chordDHT();
 	}
 	
 	public static DHTFacade getInstance() { 
 		if (instance == null) { 
 			instance = new DHTFacade() ; 
 		}
 		return instance ; 
 	}
 	
 	
     public Result join() {
     	return chord.join(); 
     }
     
     public Result create() { 
     	return chord.create(); 
     }
 	
     public Result put(User user) {
     	return chord.put(user);
  	}
 
 	public Set<User> getUsersByKey(User user) {
 		return chord.getUsersByKey(user);
 	}
 
 	public Result delete(User user) {
 		return chord.delete(user);
 	}
 	
 	public void leave() { 
 		chord.leave();
 	}
 	
 }
