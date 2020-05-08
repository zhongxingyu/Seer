 package presentationLayer;
 
 import java.io.Serializable;
 import java.security.GeneralSecurityException;
 
 import javax.ejb.EJB;
 import javax.enterprise.context.Conversation;
 import javax.enterprise.context.ConversationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import usersManagement.BusinessRole;
 import usersManagement.Role;
 import usersManagement.RolePermission;
 import usersManagement.SystemRole;
 import usersManagement.User;
 import businessLayer.Department;
 import controllerLayer.UserControllerBean;
 
 @Named("userEditor")
 @ConversationScoped
 public class UserEditPresentationBean implements Serializable {
 	private static final long serialVersionUID = -20012799818165996L;
 
 	@EJB
 	private UserControllerBean userController;
 
 	@Inject
 	private Conversation conversation;
 
 	private RolePermission newRolePermission;
 	private Department newDepartment;
 
 
 	public UserEditPresentationBean() {
 		resetNewRoleFields();
 	}
 
 
 	private void resetNewRoleFields() {
 		newRolePermission = RolePermission.ADMIN;
 		newDepartment = null;
 	}
 
 
 	public Conversation getConversation() {
 		return conversation;
 	}
 
 
 	private void close() {
 		//TODO cancellare se non serve
 	}
 
 
	public void save() {
 		try {
 			userController.save();
 		} catch (GeneralSecurityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		close();
 	}
 
 
	public void cancel() {
 		userController.cancel();
 		close();
 	}
 
 
 	public String editLoggedUser() {
 		return userController.editLoggedUser();
 	}
 
 
 	public User getUser() {
 		return userController.getCurrentUser();
 	}
 	
 	public void editUser(User user){
 		userController.setCurrentUser(user);
 	}
 
 
 	public Department getDepartmentFromRole(Role role) {
 		if (role instanceof BusinessRole) {
 			return ((BusinessRole) role).getDepartment();
 		}
 		return null;
 	}
 
 
 	public RolePermission getNewRolePermission() {
 		return newRolePermission;
 	}
 
 
 	public void setNewRolePermission(RolePermission newRolePermission) {
 		this.newRolePermission = newRolePermission;
 	}
 
 
 	public Department getNewDepartment() {
 		return newDepartment;
 	}
 
 
 	public void setNewDepartment(Department newDepartment) {
 		this.newDepartment = newDepartment;
 	}
 
 
 	public boolean newDepartmentRequired() {
 		System.out.println("User editor: newDepReq = " + !newRolePermission.equals(RolePermission.ADMIN));
 		return !newRolePermission.equals(RolePermission.ADMIN);
 	}
 
 
 	public void addRole() {
 		Role newRole;
 		if (newDepartmentRequired()) {
 			newRole = new BusinessRole(newRolePermission, newDepartment);
 		}
 		else {
 			newRole = new SystemRole(newRolePermission);
 		}
 		userController.getCurrentUser().addRole(newRole);
 		resetNewRoleFields();
 	}
 
 
 	public void removeRole(Role role) {
 		userController.getCurrentUser().removeRole(role);
 	}
 }
