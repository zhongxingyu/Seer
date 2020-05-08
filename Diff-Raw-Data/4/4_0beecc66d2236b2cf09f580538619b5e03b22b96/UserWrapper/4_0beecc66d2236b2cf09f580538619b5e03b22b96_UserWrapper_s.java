 /**
  * 
  */
 package ApplicationLogic;
 
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Set;
 
 import DatabaseFrontend.Bid;
 import DatabaseFrontend.Item;
 import DatabaseFrontend.User;
 import DatabaseFrontend.Permission;
 
 public class UserWrapper {
 	private final User user;
 	private AccountControl control;
 
 	public String getEmail() {
 		if (!control.isLoggedInUserAllowed(this, Permission.ViewEmail))
 			return "";
 		return user.getEmail();
 	}
 
 	public String getContactInfo() {		
 		if (!control.isLoggedInUserAllowed(this, Permission.ViewContactInfo))
 			return "";
 		return user.getContactInfo();
 	}
 
 	public boolean setEmail(String email) {
 		if (!control.isLoggedInUserAllowed(this, Permission.EditOwnUser, Permission.EditOtherUsers))
 			return false;
 		try {
 			return user.setEmail(email);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public Set<ItemWrapper> getItemsSelling() {
 		Set<ItemWrapper> s = new HashSet<ItemWrapper>();
 
 		try {
 			for (Item i : user.getItemsSelling())
 				s.add(new ItemWrapper(i, control.p));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return s;
 	}
 
 	public Set<BidWrapper> getBidsMade() {
 		if (!control.isLoggedInUserAllowed(this, Permission.ViewBids))
 			return null;
 		Set<BidWrapper> s = new HashSet<BidWrapper>();
 		try {
 			for (Bid b : user.getBidsMade())
 				s.add(new BidWrapper(b, control.p));
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return s;
 	}
 
 	public boolean setContactInfo(String contactInfo) {
 		if (!control.isLoggedInUserAllowed(this, Permission.EditOwnUser, Permission.EditOtherUsers))
 			return false;
 		try {
 			return user.setContactInfo(contactInfo);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean setPassword(String password) {
 		if (!control.isLoggedInUserAllowed(this, Permission.EditOwnUser, Permission.EditOtherUsers))
 			return false;
 		try {
 			return user.setPasswordHash(AccountControl.hash(password, user.getSalt()));
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean deleteUser() {
 		if (!control.isLoggedInUserAllowed(this, Permission.EditOwnUser, Permission.EditOtherUsers))
 			return false;
 		try {
 			return user.deleteUser();
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean setPermission(Permission permission) {
 		if (!control.isLoggedInUserAllowed(this, Permission.EditUserPermissions, Permission.EditUserPermissions))
 			return false;
 		try {
 			return user.setPermission(permission);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	public boolean isAllowed(Permission permission) {
 		return user.getPermissions().contains(permission);
 	}
 
 	public boolean deletePermission(Permission permission, String userEmail) {
 		if (!control.isLoggedInUserAllowed(this, Permission.EditUserPermissions, Permission.EditUserPermissions))
 			return false;
 		try {
 			return user.deletePermission(permission);
 		} catch (SQLException e) {
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	UserWrapper(User user, Control control) {
 		this.user = user;
 		this.control = control.accountControl;
 	}
 
 	public boolean checkPass(String password) {
 		String hash = user.getPasswordHash();
 		return hash.equals(AccountControl.hash(password, user.getSalt()));
 	}
 }
