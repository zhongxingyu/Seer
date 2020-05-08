 package models;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.db.jpa.GenericModel;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 @Entity
 public class User extends Item {
 	
 	@Email
 	@Required
 	@Column(unique=true) 
 	public String email;
 	
 	public String passwordHash;
 	
 	public String accessToken;
 	
 	@Required
     public String firstName;
 	
 	@Required
     public String lastName;
 	
 	public String mobileNumber;
     
     @OneToMany(mappedBy="owner", cascade={CascadeType.ALL})
     public List<Meeting> meetingsCreated = new ArrayList<Meeting>();
     
     @OneToMany(mappedBy="user", cascade={CascadeType.ALL})
     public List<Attendee> meetingsRelated = new ArrayList<Attendee>();
     
     @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
     @JoinTable(
         name="USER_CONNECTIONS",
         joinColumns=@JoinColumn(name="USER_1"),
         inverseJoinColumns=@JoinColumn(name="USER_2")
     )
     public List<User> userConnectionsTo = new ArrayList<User>();
     
     @ManyToMany(mappedBy="userConnectionsTo", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
     public List<User> userConnectionsFrom = new ArrayList<User>();
     
     @ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE})
     @JoinTable(
         name="USER_CONNECTION_REQUESTS",
         joinColumns=@JoinColumn(name="USER_REQUEST_1"),
         inverseJoinColumns=@JoinColumn(name="USER_REQUEST_2")
     )
     public List<User> userConnectionRequestsTo = new ArrayList<User>();
     
     @ManyToMany(mappedBy = "userConnectionRequestsTo", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
     public List<User> userConnectionRequestsFrom = new ArrayList<User>();
     
     @OneToMany(mappedBy="user")
     public List<UserLocation> locationHistory = new ArrayList<UserLocation>();
     
     @Override
 	public GenericModel delete() {
 		
 		// Remove userConnectionsTo links to this user
 		for (User user : userConnectionsFrom) {
 			user.userConnectionsTo.remove(this);
 			user.save();
 		}
 		
 		// Remove userConnectionRequest links to this user
 		for (User user : userConnectionRequestsFrom) {
 			user.userConnectionRequestsTo.remove(this);
 			user.save();
 		}
 		
 		return super.delete();
 	}
     
 }
