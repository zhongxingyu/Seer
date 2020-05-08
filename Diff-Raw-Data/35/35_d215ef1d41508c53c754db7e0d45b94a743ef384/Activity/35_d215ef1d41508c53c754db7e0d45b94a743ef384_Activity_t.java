 package models;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 import javax.persistence.OneToOne;
 
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 
 @Entity
 public class Activity extends Model {
 
 	@Required
 	public String name;
 	
 	@Required
 	public String location;
 	
 	@Required
 	public Date date;
 	
 	@MaxSize(10000)
 	public String information;
 	
 	@OneToOne
 	public User creator;
 	
 	@OneToMany(mappedBy="activity", cascade=CascadeType.ALL)
 	public List<ActivityMessage> messages;
 	
 	@OneToMany(mappedBy="activity", cascade=CascadeType.ALL)
 	public List<ActivityStatus> activityStatuses;
 	
 	@ManyToMany(mappedBy="followedActivities") 
     public List<User> followsByUsers = new ArrayList<User>(); 
 	
 	public Activity(String name, String location, Date date, String information, User creator) {
 		this.name = name;
 		this.location = location;
 		this.date = date;
 		this.information = information;
 		this.creator = creator;
 	}
 	
 	public int nrOfYes() {
 		return getAllActivityStatusesForStatus(1).size();
 	}
 	
 	public int nrOfNo() {
		return getAllActivityStatusesForStatus(0).size();
 	}
 	
 	public int nrOfUnknown() {
		return getAllActivityStatusesForStatus(-1).size();
 	}
 	
 	public List<ActivityStatus> getAllYes() {
 		return getAllActivityStatusesForStatus(1);
 	}
 	
 	public List<ActivityStatus> getAllNo() {
 		return getAllActivityStatusesForStatus(0);
 	}
 	
 	public List<ActivityStatus> getAllUnknown() {
 		return getAllActivityStatusesForStatus(-1);
 	}
 	
 	private List<ActivityStatus> getAllActivityStatusesForStatus(int status) {
 		List<ActivityStatus> statuses = new ArrayList<ActivityStatus>();
 		for (ActivityStatus activityStatus : activityStatuses) {
 			if (activityStatus.status == status) {
 				statuses.add(activityStatus);
 			}
 		}
 		return statuses;
 	}
 	
 	public ActivityStatus getActivityStatusForUser(String userName) {
 		for (ActivityStatus activityStatus : activityStatuses) {
 			if (activityStatus.user.email.equals(userName)) {
 				return activityStatus;
 			}
 		}
 		return null;
 	}
 	
 	public String toString() {
 		return name;
 	}
 }
