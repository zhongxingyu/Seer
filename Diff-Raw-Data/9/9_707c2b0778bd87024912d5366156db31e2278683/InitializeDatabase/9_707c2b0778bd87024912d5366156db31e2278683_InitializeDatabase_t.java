 package org.jabox.applicationcontext;
 
 import org.apache.wicket.injection.web.InjectorHolder;
 import org.apache.wicket.persistence.provider.GeneralDao;
 import org.apache.wicket.spring.injection.annot.SpringBean;
 import org.jabox.model.DefaultConfiguration;
 import org.jabox.model.User;
 
 public class InitializeDatabase {
	@SpringBean()
 	protected GeneralDao _generalDao;
 
 	public InitializeDatabase() {
 		InjectorHolder.getInjector().inject(this);
 	}
 
 	/**
 	 * check if database is already populated, if not, populate
 	 */
 	public void init() {
 		if (_generalDao.getDefaultConfiguration() == null) {
 			DefaultConfiguration config = new DefaultConfiguration();
 			_generalDao.persist(config);
 			User user = new User();
 			user.setLogin("admin");
 			user.setPassword("admin");
 			_generalDao.persist(user);
 		}
 	}
 }
