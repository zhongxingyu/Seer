 package models;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 import play.db.ebean.Model;
 
 @Entity
 @Table(name = "Sheep")
 public class Sheep extends Model {
 
 	@Id
 	public long id;
 	public long sheepId;
 	public long producerId;
 	
 	@ManyToOne
 	@JoinColumn(name="producer_id")
 	public Producer producer;
 	
 	public long rfid;
 	
 	@OneToMany(mappedBy="sheep")
 	public List<Event> events = new ArrayList<>();
 
 	public String name;
 	public double birthWeight;
 	public Date timeOfBirth;
 	public String notes;
 	public boolean attacked;
 	public Timestamp timeAdded;
 
 	public static Model.Finder<Long, Sheep> find = new Model.Finder<Long, Sheep>(Long.class, Sheep.class);
 
 	public static Sheep create(long id, long rfid, long producerId) {
 		return new Sheep();
 	}
 	
 	public static List<Sheep> findByProducerId(long id) {
 		return find.where().eq("producer_id", id).findList();
 	}
 
 	public Sheep() {
 
 	}
 
 	public Sheep(long id) {
 		this.id = id;
 	}
 
 }
