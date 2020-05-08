 package cours.ulaval.glo4003.domain;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang.builder.EqualsBuilder;
 import org.apache.commons.lang.builder.HashCodeBuilder;
 
 public class User {
 
 	private String idul;
 	private String name;
 	private String password;
	private List<Role> role = new ArrayList<Role>();
 
 	private String emailAddress = "";
 	private static final String EMAIL_VALIDATION_REGEX = "^[a-z0-9-]+(\\.[a-z0-9-]+)*@[a-z0-9-]+(\\.[a-z0-9-]+)*\\.[a-z]{2,6}$";
 
 	private List<Notification> notifications = new ArrayList<Notification>();
 
 	public User() {
 
 	}
 
 	public User(String idul, String name, String password, Role role) {
 		this.idul = idul;
 		this.name = name;
 		this.password = password;
		this.role.add(role);
 	}
 
 	public void acceptSchedule(Schedule schedule) {
		if (role.contains(Role.ROLE_Enseignant)) {
 			schedule.addStatus(idul, ScheduleStatus.Accepted);
 		}
 	}
 
 	public void refuseSchedule(Schedule schedule) {
		if (role.contains(Role.ROLE_Enseignant)) {
 			schedule.addStatus(idul, ScheduleStatus.Refused);
 		}
 	}
 
 	public boolean validateCredentials(String password) {
 		return this.password.equals(password);
 	}
 
 	public void setIdul(String idul) {
 		this.idul = idul;
 	}
 
 	public String getIdul() {
 		return idul;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setRoles(List<Role> role) {
		this.role = role;
 	}
 
 	public String getEmailAddress() {
 		return emailAddress;
 	}
 
 	public void setEmailAddress(String emailAddress) {
 		this.emailAddress = emailAddress;
 	}
 
 	public boolean hasValidEmailAdress() {
 		return emailAddress.matches(EMAIL_VALIDATION_REGEX);
 	}
 
 	public void addRole(Role role) {
		if (!this.role.contains(role)) {
			this.role.add(role);
 		}
 	}
 
 	public List<Role> getRoles() {
		return role;
 	}
 
 	public void addNotification(Notification notification) {
 		if (!notifications.contains(notification)) {
 			notifications.add(notification);
 		}
 	}
 
 	public void removeNotification(Notification notification) {
 		notifications.remove(notification);
 	}
 
 	public List<Notification> getNotifications() {
 		return notifications;
 	}
 
 	public void setNotifications(List<Notification> notifications) {
 		this.notifications = notifications;
 	}
 
 	public boolean hasNotification() {
 		return notifications.size() > 0;
 	}
 
 	@Override
 	public int hashCode() {
 		return HashCodeBuilder.reflectionHashCode(this);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		return EqualsBuilder.reflectionEquals(this, obj);
 	}
 
 }
