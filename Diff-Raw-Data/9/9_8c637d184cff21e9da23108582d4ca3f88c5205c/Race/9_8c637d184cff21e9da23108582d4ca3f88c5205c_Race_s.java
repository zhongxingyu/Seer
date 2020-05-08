 package models;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 
 import org.apache.commons.lang.NotImplementedException;
 
 import play.db.jpa.Model;
 
 
 @Entity
 public class Race extends Model {
 	
 	public static final int MAX_AVAILABLE_SLOTS = 8;
 
	@Id
	public Long id;
	
 	@OneToMany
 	public Set<Horse> horses = new HashSet<Horse>();
 	
 	public void enter(Horse horse) {
 		this.horses.add(horse);
 //		throw new NotImplementedException();
 	}
 
 	public int getAvailableSlots() {
 		return MAX_AVAILABLE_SLOTS;
 	}
 
 	public void start() {
 		throw new NotImplementedException();
 	}
 	
 }
