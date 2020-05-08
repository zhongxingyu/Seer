 package devhood.im.sim.config;
 
 import javax.inject.Inject;
 
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 
 import devhood.im.sim.dao.JtdsUserDao;
 import devhood.im.sim.dao.SingleFilePerUserDao;
 import devhood.im.sim.dao.SqliteUserDao;
 import devhood.im.sim.dao.interfaces.UserDao;
 
 /**
  * Spring Configuration Klasse.
  * 
  * @author flo
  *
  */
 @Configuration
 public class SimConfigurationBean {
 
 	@Inject
 	private SimConfiguration simConfiguration;
 	
 	@Inject
 	private JtdsUserDao jtdsUserDao;
 	
 	@Inject
 	private SingleFilePerUserDao singleFilePerUserDao;
 	
 	@Inject
 	private SqliteUserDao sqliteUserDao;
 	
 	
 	@Bean(name="userDao")
 	public UserDao getUserDao() {
 		UserDao userDao = jtdsUserDao;
 		if ( simConfiguration.getUserDaoToUse() != null) {
 			if ( "single".equals(simConfiguration.getUserDaoToUse()) ) {
 				userDao = singleFilePerUserDao;
 			}else if ( "sqlite".equals(simConfiguration.getUserDaoToUse()) ) {
 				userDao = sqliteUserDao;
			}else if ( "jtds".equals(simConfiguration.getUserDaoToUse()) ) {
				userDao = jtdsUserDao;
 			}
 			
 		}
 		
 		return userDao;
 	}
 }
