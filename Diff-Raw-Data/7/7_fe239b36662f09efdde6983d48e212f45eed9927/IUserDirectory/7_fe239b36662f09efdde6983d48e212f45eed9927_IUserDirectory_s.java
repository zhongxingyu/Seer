 package directory;
 
 import java.util.Collection;
 
 import entity.NewsGroupRightentity;
 import entity.Userentity;
 
 public interface IUserDirectory {
 
	public void addUser(String username,String boxname);
 
 	public boolean removeUser(int userid);
 
 	public Collection<Userentity> lookupAllUsers();
 
 	public NewsGroupRightentity lookupAUserRight(int userid);
 
 	public void updateRights(int userid);
 }
