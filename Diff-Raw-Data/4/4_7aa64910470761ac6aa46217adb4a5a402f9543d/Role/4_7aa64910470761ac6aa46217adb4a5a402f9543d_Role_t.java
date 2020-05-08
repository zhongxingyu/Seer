 package pt.ist.expenditureTrackingSystem.domain;
 
 import myorg.domain.User;
 import pt.ist.expenditureTrackingSystem.domain.organization.Person;
 import pt.ist.fenixWebFramework.services.Service;
 import dml.runtime.RelationAdapter;
 
 public class Role extends Role_Base {
 
     public static class RolePersonListener extends RelationAdapter<Role, Person> {
 
 	@Override
 	public void afterAdd(final Role role, final Person person) {
 	    super.afterAdd(role, person);
 	    if (role.getSystemRole() != null) {
 		role.getSystemRole().addUsers(person.getUser());
 	    }
 	}
 
 	@Override
 	public void afterRemove(final Role role, Person person) {
 	    if (role.getSystemRole() != null) {
 		role.getSystemRole().removeUsers(person.getUser());
 	    }
 	}
 
     }
 
     static {
 	Role.PersonRole.addListener(new RolePersonListener());
     }
 
     public Role(final RoleType type) {
 	super();
 	setRoleType(type);
 	setExpenditureTrackingSystem(ExpenditureTrackingSystem.getInstance());
 	setSystemRole(myorg.domain.groups.Role.getRole(type));
     }
 
     @Service
     public static Role createRole(final RoleType type) {
 	final Role role = new Role(type);
 	role.setSystemRole(myorg.domain.groups.Role.getRole(type));
 	return role;
     }
 
     @Service
     public static Role getRole(final RoleType roleType) {
 	for (final Role role : ExpenditureTrackingSystem.getInstance().getRoles()) {
 	    if (role.getRoleType().equals(roleType)) {
 		if (!role.hasSystemRole()) {
 		    role.setSystemRole(myorg.domain.groups.Role.getRole(roleType));
 		}
 		for (final Person person : role.getPersonSet()) {
 		    final User user = person.getUser();
		    if (user != null) {
			role.getSystemRole().addUsers(user);
		    }
 		}
 		return role;
 	    }
 	}
 	return createRole(roleType);
     }
 
 }
