 package edu.ycp.CS320.client;
 
 import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
 
 import edu.ycp.CS320.shared.User;
 
 
 
 /**
  * @author drew
  *these are the general methods that the login service implementation will perform
  */
@RemoteServiceRelativePath("LoginServiceImpl")
 public interface LoginService extends RemoteService {
 
 	Boolean login(User user);
 
 	Boolean message(String message);
 
 	Boolean addNewUser(User user);
 
 }
