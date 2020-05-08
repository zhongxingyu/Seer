 package com.infinitiessoft.btrs.action;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.jboss.seam.ScopeType;
 import org.jboss.seam.annotations.In;
 import org.jboss.seam.annotations.Logger;
 import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Observer;
 import org.jboss.seam.annotations.Out;
 import org.jboss.seam.framework.EntityHome;
 import org.jboss.seam.log.Log;
 import org.jboss.seam.security.Identity;
 
 import com.infinitiessoft.btrs.custom.PasswordManager;
 import com.infinitiessoft.btrs.model.Report;
 import com.infinitiessoft.btrs.model.Role;
 import com.infinitiessoft.btrs.model.StatusChange;
 import com.infinitiessoft.btrs.model.User;
 import com.infinitiessoft.btrs.model.UserShared;
 
 @Name("userHome")
 public class UserHome extends EntityHome<User> {
 
 	private static final long serialVersionUID = -5368319908832175365L;
 	
 	@Logger private Log log;
 
 	@In
 	Identity identity;
 	
 	@In(required = false)
 	@Out(required = false, scope = ScopeType.SESSION)
 	User currentUser;
 	@In(required = false)
 	@Out(required = false, scope = ScopeType.SESSION)
 	UserShared currentUserShared;
 	
 	@In
 	PasswordManager passwordManager;
 	
 	@In(required = false)
 	UserSharedHome userSharedHome;
 	
 	@In(create = true)
 	UserSharedList userSharedList;
 	
 	@In(create = true)
 	UserList userList;
 	
 	@In(create = true)
 	RoleList roleList;
 	
 	public void setUserId(Long id) {
 		setId(id);
 	}
 
 	public Long getUserId() {
 		return (Long) getId();
 	}
 
 
 	public void load() {
 		if (isIdDefined()) {
 			wire();
 		}
 	}
 
 	public void wire() {
 	}
 
 	public boolean isWired() {
 		return true;
 	}
 
 	public User getDefinedInstance() {
 		return isIdDefined() ? getInstance() : null;
 	}
 
 	public List<Report> getIncomingReports() {
 		return getInstance() == null ? null : new ArrayList<Report>(
 				getInstance().getIncomingReports());
 	}
 	public List<Report> getOutgoingReports() {
 		return getInstance() == null ? null : new ArrayList<Report>(
 				getInstance().getOutgoingReports());
 	}
 	public List<StatusChange> getStatusChanges() {
 		return getInstance() == null ? null : new ArrayList<StatusChange>(
 				getInstance().getStatusChanges());
 	}
 	
 	public List<Role> getRoles() {
 		return getInstance() == null ? null : new ArrayList<Role>(getInstance().getRoles());
 	}
 	
 	@Override
 	public String persist() {
 //		log.info("Registering user #{user.username}");
 		if (userSharedHome.isIdDefined()) {
 			userSharedHome.update();
 		} else {
 			userSharedHome.persist();
 		}
 
 		User user = getInstance();
 		user.setUserSharedId(userSharedHome.getUserSharedId());
 		if (user.getRoles().isEmpty()) {
 			user.addRole(roleList.getDefaultRole());
 		}
 		return super.persist();
 	}
 
 	@Override
 	public String update() {
 		userSharedHome.update();
 		return super.update();
 	}
 	
 	@Override
 	public String remove() {
 		userSharedHome.remove();
 		return super.remove();
 	}
 	
	@Observer(Identity.EVENT_POST_AUTHENTICATE)
 	public void updateLastLoginDate() {
 		currentUserShared = userSharedList.getUserShared(identity.getPrincipal().getName());
 		currentUser = userList.getUserBySharedId(currentUserShared.getId());
 		
 		currentUser.setLastLogin(new Date());
 		setUserId(currentUser.getId());
 		
 		log.info("User #0 is authenticated", identity.getPrincipal().getName());
 		
 		super.update();
 	}
 	
 }
