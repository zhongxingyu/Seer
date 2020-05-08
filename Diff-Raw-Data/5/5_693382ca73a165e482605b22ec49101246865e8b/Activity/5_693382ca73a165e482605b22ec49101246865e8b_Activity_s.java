 package models;
 
 import java.sql.Time;
 import java.util.Date;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Temporal;
 import javax.persistence.TemporalType;
 
 import org.hibernate.search.annotations.Field;
 import org.hibernate.search.annotations.Index;
 import org.hibernate.search.annotations.Indexed;
 
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 @Indexed
 public class Activity extends Model {
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "timestamp", nullable = false)
 	public Date timestamp;
 	
 	@Temporal(TemporalType.TIMESTAMP)
 	@Column(name = "time_end", nullable = false)
 	public Date time_end;
 	
 	@Required
 	@Field(index = Index.TOKENIZED)
 	public String name;
 	
 	@Required
 	@Field(index = Index.TOKENIZED)
 	public String title;
 	
 	@ManyToOne(targetEntity = Task.class)
 	public Task task;
 	
 	@Required
 	@ManyToOne(targetEntity = User.class)
 	public User user;
 	
 	public Time duration() {
 		long duration = time_end.getTime() - timestamp.getTime();
 		return new Time(duration);
 	}
 	
 	public String toString() {
 		return "Activity[" + id + "] {" 
 			+ timestamp + ", "
 			+ time_end + ", "
 			+ name + ", "
 			+ title + ", "
 			+ (task != null ? (task.name + ", ") : "(no task), ")
 			+ (user != null ? (user.getName()) : "(no user)")
 			+ "}"
 		;
 	}
 }
