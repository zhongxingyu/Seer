 package com.papteco.web.dbs;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.annotation.PostConstruct;
 
 import org.apache.commons.lang.StringUtils;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Component;
 
 import com.papteco.web.beans.UsersBean;
 import com.sleepycat.je.Environment;
 import com.sleepycat.je.EnvironmentConfig;
 import com.sleepycat.persist.EntityCursor;
 import com.sleepycat.persist.EntityStore;
 import com.sleepycat.persist.PrimaryIndex;
 import com.sleepycat.persist.StoreConfig;
 
 @Component
 public class UserDAO {
 
 	@Value("#{settings[datapath]}")
 	protected String datapath;
 	@Value("#{settings[sysadmin_ind]}")
 	protected boolean sysadmin_ind;
 	@Value("#{settings[sysadmin_pwd]}")
 	protected String sysadmin_pwd;
 	
 	private static PrimaryIndex<String, UsersBean> usersIndex;
 
 	@PostConstruct
 	public void init() {
 		File f = new File(datapath);
 		if(!f.exists()){
 			f.mkdirs();
 		}
 		new UserDAO(datapath);
 		if(sysadmin_ind){
 			UsersBean sysadmin = getUser("sysadmin");
 			if(sysadmin == null){
 				sysadmin = new UsersBean();
 				sysadmin.setUserName("sysadmin");
 				sysadmin.setPassword(sysadmin_pwd);
 				sysadmin.setEmail("");
 				List<String> roles = new ArrayList<String>();
 				roles.add("Administrator");
 				sysadmin.setRoles(roles);
 				saveUser(sysadmin);
 			}
 		}
 	}  
 
 	public UserDAO(String databasePath) {
 
 		// Open a transactional Berkeley DB engine environment.
 		//
 		EnvironmentConfig envConfig = new EnvironmentConfig();
 		envConfig.setAllowCreate(true);
 		envConfig.setTransactional(true);
 		Environment env = new Environment(new File(databasePath), envConfig);
 
 		// Open a transactional entity store.
 		//
 		StoreConfig storeConfig = new StoreConfig();
 		storeConfig.setAllowCreate(true);
 		storeConfig.setTransactional(true);
 		EntityStore store = new EntityStore(env, "ProjectStore", storeConfig);
 
 		usersIndex = store.getPrimaryIndex(String.class,
 				UsersBean.class);
 	}
 
 	// this is retry function
 	public static void saveUser(UsersBean user) {
 		usersIndex.put(user);
 	}
 	
 	// this is retry function
 	public static void deleteUser(UsersBean user) {
 		usersIndex.delete(user.getUserName());
 	}
 	
 	public static UsersBean getUser(String username) {
 		return usersIndex.get(username);
 	}
 
 	public static List<UsersBean> getUsersByFilter(String username, String rolekey){
 		EntityCursor<UsersBean> allUsers = usersIndex.entities();
 		List<UsersBean> result = new LinkedList<UsersBean>();
 		for (UsersBean bean : allUsers){
 			if(StringUtils.isBlank(username) && StringUtils.isBlank(rolekey)){
 				result.add(bean);
 			}else if(StringUtils.isBlank(username) && StringUtils.isNotBlank(rolekey)){
 				List<String> roles = bean.getRoles();
 				for(String role : roles){
 					if(role.contains(rolekey)){
 						result.add(bean);
 						break;
 					}
 				}
 			}else if(StringUtils.isNotBlank(username) && StringUtils.isBlank(rolekey)){
 				if(bean.getUserName().contains(username.trim())){
 					result.add(bean);
 				}
 			}else{
 				if(bean.getUserName().contains(username.trim())){
 					List<String> roles = bean.getRoles();
 					for(String role : roles){
 						if(role.contains(rolekey)){
 							result.add(bean);
 							break;
 						}
 					}
 				}
 			}
 		}
 		allUsers.close();
 		return result;
 	}
 	
 	/* mandatory constructor method */
 	public UserDAO(){
 		
 	}
 }
