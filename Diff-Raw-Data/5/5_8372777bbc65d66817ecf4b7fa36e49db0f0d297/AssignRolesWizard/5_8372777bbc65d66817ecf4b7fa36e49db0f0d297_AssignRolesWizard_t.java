 package org.eclipse.emf.emfstore.client.ui.views.emfstorebrowser.dialogs.admin;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Set;
 
import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.emfstore.client.ui.views.users.UserUiController;
 import org.eclipse.emf.emfstore.server.model.ProjectInfo;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACOrgUnit;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.ACUser;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.PermissionSet;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.Role;
 import org.eclipse.emf.emfstore.server.model.accesscontrol.RoleAssignment;
 import org.eclipse.jface.wizard.Wizard;
 
 /**
  * assign project roles for a specific project to users
  * 
  * @author boehlke
  */
 public class AssignRolesWizard extends Wizard {
 
 	private ProjectInfo projectInfo;
 	private PermissionSet permissionSet;
 	private SelectRolesPage selectRolesPage;
 
 	/**
 	 * will be filled by ChooseUsersPage
 	 */
 	private Set<ACUser> selectedUsers = new HashSet<ACUser>() {
 		private static final long serialVersionUID = 4424369725696447623L;
 
 		@Override
 		public boolean add(ACUser user) {
 			boolean add = super.add(user);
 			update();
 
 			return add;
 		};
 
 		@Override
 		public boolean remove(Object o) {
 			boolean remove = super.remove(o);
 			update();
 
 			return remove;
 		}
 
 		private void update() {
 			getContainer().updateButtons();
 			getContainer().updateMessage();
 			selectRolesPage.usersUpdated();
 		};
 	};
 	private AssignRoleSummaryPage summaryPage;
 	private ArrayList<RoleAssignmentData> addedAssignments;
 	private ArrayList<RoleAssignmentData> removedAssignments;
 	private UserUiController controller;
 
 	public AssignRolesWizard(ProjectInfo projectInfo, PermissionSet permissionSet) {
 		setWindowTitle("Assign project roles");
 		this.projectInfo = projectInfo;
 		this.permissionSet = permissionSet;
 		controller = UserUiController.getInstance();
 		controller.registerPermissionSetListener();
 	}
 
 	void update() {
 		getContainer().updateButtons();
 		getContainer().updateMessage();
 		getContainer().updateTitleBar();
 	}
 
 	@Override
 	public void addPages() {
 		addPage(new ChooseUsersPage(this));
 		selectRolesPage = new SelectRolesPage(getShell(), this);
 		addPage(selectRolesPage);
 		summaryPage = new AssignRoleSummaryPage(this);
 		addPage(summaryPage);
 	}
 
 	@Override
 	public boolean performFinish() {
		controller.assignAndRemoveRoles(EcoreUtil.copy(projectInfo.getProjectId()), addedAssignments,
			removedAssignments);
 		return true;
 	}
 
 	public ProjectInfo getProjectInfo() {
 		return projectInfo;
 	}
 
 	public PermissionSet getPermissionSet() {
 		return permissionSet;
 	}
 
 	public Set<ACUser> getSelectedUsers() {
 		return selectedUsers;
 	}
 
 	@Override
 	public boolean canFinish() {
 		if (getSelectedUsers().size() == 0) {
 			return false;
 		}
 		return roleAssignmentsChanged();
 	}
 
 	/**
 	 * calculates, if the role assignments changed
 	 * 
 	 * @return
 	 */
 	public boolean roleAssignmentsChanged() {
 		removedAssignments = new ArrayList<RoleAssignmentData>();
 		addedAssignments = new ArrayList<RoleAssignmentData>();
 
 		for (ACOrgUnit orgUnit : getSelectedUsers()) {
 			for (RoleAssignment role : orgUnit.getRoles()) {
 				if (role.getProjectId() == null || !role.getProjectId().equals(projectInfo.getProjectId())) {
 					continue;
 				}
 				if (!selectRolesPage.isSelected(orgUnit, role.getRole())) {
 					removedAssignments.add(new RoleAssignmentData(role.getRole(), orgUnit));
 				}
 			}
 
 			for (Role role : getPermissionSet().getRoles()) {
 				boolean selected = selectRolesPage.isSelected(orgUnit, role);
 				boolean hasProjectRole = orgUnit.hasProjectRole(role, projectInfo.getProjectId());
 
 				if (selected && !hasProjectRole) {
 					addedAssignments.add(new RoleAssignmentData(role, orgUnit));
 				}
 			}
 		}
 		return removedAssignments.size() > 0 || addedAssignments.size() > 0;
 	}
 
 	public Collection<RoleAssignmentData> getAddedRoles() {
 		roleAssignmentsChanged();
 		return addedAssignments;
 	}
 
 	public Collection<RoleAssignmentData> getRemovedRoles() {
 		roleAssignmentsChanged();
 		return removedAssignments;
 	}
 }
