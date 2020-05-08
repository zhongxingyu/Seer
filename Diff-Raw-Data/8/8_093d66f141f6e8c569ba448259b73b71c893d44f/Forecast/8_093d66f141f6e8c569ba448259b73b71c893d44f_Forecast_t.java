 package forecast.domain;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

 public class Forecast {
 	
 	Set <Activity> activities;
 	String userName;
 	Date startDate;
 	Date endDate;
 	
	@Transient
 	public int getTotalPercentage() {
 		int total = 0;
 		for(Activity a : activities) {			
 			total += a.getDefaultPercentage();
 		}
 		return total;
 	}
 
	@OneToMany
 	public Set<Activity> getActivities() {
 		return activities;
 	}
 
 	public void setActivities(Set<Activity> activities) {
 		this.activities = activities;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	public Date getStartDate() {
 		return startDate;
 	}
 
 	public void setStartDate(Date startDate) {
 		this.startDate = startDate;
 	}
 
 	public Date getEndDate() {
 		return endDate;
 	}
 
 	public void setEndDate(Date endDate) {
 		this.endDate = endDate;
 	}
 	
 	
 	
 }
