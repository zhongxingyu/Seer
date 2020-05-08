 import java.io.Serializable;
 
 public class User implements Comparable<User>,Serializable{
 	private static final long serialVersionUID = -2654414564884238786L;
 	private String name;
 	
 
 	public User(String username){
 		name = username;
 	}
 	
 	@Override
 	public int compareTo(User otherUser) {
		if(otherUser == null && otherUser.name == null) return 1;
 		if(name == null) return -1;
 		
 		return name.compareTo(name);
 	}
 	
 	public String getName(){
 		return name;
 	}
 		
 
 }
