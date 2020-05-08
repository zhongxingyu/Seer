 package edu.ibs.core.service;
 
import edu.ibs.core.service.logic.CommonService;
 import edu.ibs.core.entity.User;
 import edu.ibs.core.entity.User.Role;
 import edu.ibs.core.service.logic.ApplicationContextProvider;
 
 /**
  * @date Oct 22, 2012
  *
  * @author Vadim Martos
  */
 public final class ServiceProxy {
 
	private static final UserServicable userLogic = ApplicationContextProvider.provide().getBeansOfType(CommonService.class).get("userLogic");
	private static final AdminServicable adminLogic = ApplicationContextProvider.provide().getBeansOfType(CommonService.class).get("adminLogic");
 
 	private ServiceProxy() {
 	}
 
 	public static UserServicable user(User user) throws NullPointerException, IllegalArgumentException {
 		if (user.getRole().equals(Role.USER)) {
 			return userLogic;
 		}
 		throw new IllegalArgumentException(String.format("User %s is not in role %s", user, Role.USER));
 	}
 
 	public static AdminServicable admin(User admin) throws NullPointerException, IllegalArgumentException {
 		if (admin.getRole().equals(Role.ADMIN)) {
 			return adminLogic;
 		}
 		throw new IllegalArgumentException(String.format("User %s is not in role %s", admin, Role.ADMIN));
 	}
 
 	public static User login(String email, String passwd) {
 		return userLogic.getUser(email, passwd);
 	}
 
 	public static User register(String email, String passwd) {
 		return adminLogic.create(Role.USER, email, passwd);
 	}
 }
