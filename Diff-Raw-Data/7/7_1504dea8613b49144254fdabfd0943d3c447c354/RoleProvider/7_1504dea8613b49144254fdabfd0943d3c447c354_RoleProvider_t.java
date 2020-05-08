 package com.tda.persistence.provider;
 
 import java.util.Enumeration;
 import java.util.Properties;
 
import org.springframework.beans.factory.annotation.Autowired;

 import com.tda.model.applicationuser.Authority;
 import com.tda.persistence.dao.AuthorityDAO;
 import com.tda.persistence.exception.NoDataFoundException;
 import com.tda.persistence.exception.SingleResultExpectedException;
 
 public class RoleProvider {
 	private Properties propertiesHolder;
 
	@Autowired
 	private AuthorityDAO authorityDAO;
 
 	public void init() {
 		loadDefaultRoles(propertiesHolder);
 
 	}
 
 	public void setPropertiesHolder(final Properties propertiesHolder) {
 		this.propertiesHolder = propertiesHolder;
 	}
 
 	private void loadDefaultRoles(final Properties properties) {
 		for (Enumeration<Object> en = properties.keys(); en.hasMoreElements();) {
 			String key = (String) en.nextElement();
 			String role = properties.getProperty(key);
 
 			Authority authority = null;
 			try {
 				authority = authorityDAO.findByAuthority(role);
 			} catch (NoDataFoundException e) {
				authority = new Authority();
				authority.setAuthority(role);
 				authorityDAO.save(authority);
 
 			} catch (SingleResultExpectedException e) {
 				// TODO we should log this as it is an anomaly in the data
 				// representation
 			}
 
 		}
 	}
 
 	public void setAuthorityDAO(AuthorityDAO authorityDAO) {
 		this.authorityDAO = authorityDAO;
 	}
 
 	public AuthorityDAO getAuthorityDAO() {
 		return authorityDAO;
 	}
 }
