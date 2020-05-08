 package war.webapp.action;
 
 import org.springframework.security.context.HttpSessionContextIntegrationFilter;
 import org.springframework.security.context.SecurityContext;
 import war.webapp.model.User;
 import war.webapp.util.FacesUtils;
 
 import java.io.Serializable;
 import java.util.List;
 
 public class UserList extends BasePage implements Serializable {
     private static final long serialVersionUID = 972359310602744018L;
 
     public UserList() {
 
         setSortColumn("username");
     }
 
     public List<User> getUsers() {
         return (List<User>) sort(userManager.getUsers());
     }
 
    public List<User> getUsersByFloorheadFloor() {
        User user = (User) ((SecurityContext) FacesUtils.getSession().getAttribute(
                HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY)).getAuthentication().getPrincipal();
        String floor = user.getAddress().getHostelFloor();
        List<User> floorUsers = userManager.getUsersByFloor(floor);
        floorUsers.remove(user);
        return (List<User>) sort(floorUsers);
    }

     public String getCurrentUserFloor() {
         User user = (User) ((SecurityContext) FacesUtils.getSession().getAttribute(
                 HttpSessionContextIntegrationFilter.SPRING_SECURITY_CONTEXT_KEY)).getAuthentication().getPrincipal();
         return user.getAddress().getHostelFloor();
     }
 }
