 package org.nnc.moviediary.domain.entities;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 
 import org.nnc.moviediary.util.BaseProperties;
 
 @Entity(name = User.TABLE_NAME)
 public class User extends BaseEntity {
 	private static final long serialVersionUID = 8364239647574512618L;
 
	public static final String TABLE_NAME = BaseProperties.TABLE_PREFIX + "user";
 
 	@Column(name = "login_name", nullable = false, unique = true)
 	private String loginName;
 
 	@Column(name = "password", nullable = false)
 	private String password;
 
 	@Column(name = "display_name", unique = true)
 	private String displayName;
 
 	@Column(name = "activated", nullable = false)
 	private boolean activated;
 
 	@Column(name = "email", nullable = false, unique = true)
 	private String email;
 
 	User() {
 	}
 
 	public User(final String loginName, final String password, final String email) {
 		this.loginName = loginName;
 		this.password = password;
 		this.displayName = loginName;
 		this.activated = false;
 		this.email = email;
 	}
 
 	public String getLoginName() {
 		return loginName;
 	}
 
 	public void setLoginName(final String loginName) {
 		this.loginName = loginName;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(final String password) {
 		this.password = password;
 	}
 
 	public String getDisplayName() {
 		return displayName;
 	}
 
 	public void setDisplayName(final String displayName) {
 		this.displayName = displayName;
 	}
 
 	public boolean isActivated() {
 		return activated;
 	}
 
 	public void setActivated(final boolean activated) {
 		this.activated = activated;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(final String email) {
 		this.email = email;
 	}
 
 }
