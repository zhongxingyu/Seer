 package org.mule.galaxy.ldap;
 
 import java.util.Collection;
 import java.util.Set;
 
 import org.acegisecurity.GrantedAuthority;
 import org.acegisecurity.userdetails.UserDetails;
 import org.apache.directory.server.ldap.LdapService;
 import org.apache.xbean.spring.context.ClassPathXmlApplicationContext;
 import org.mule.galaxy.Item;
 import org.mule.galaxy.Workspace;
 import org.mule.galaxy.impl.jcr.UserDetailsWrapper;
 import org.mule.galaxy.security.Group;
 import org.mule.galaxy.security.User;
 import org.mule.galaxy.security.UserManager;
 import org.mule.galaxy.security.ldap.LdapUserManager;
 import org.mule.galaxy.test.AbstractGalaxyTest;
 import org.springframework.context.ConfigurableApplicationContext;
 
 /**
  * Tests LDAP + Galaxy.
  */
 public class IntegratedLdapTest extends AbstractGalaxyTest {
     
     public void testUserManager() throws Exception {
 	LdapService s = (LdapService) applicationContext.getBean("ldapService");
 	s.start();
 	
         UserManager userManager = (UserManager) applicationContext.getBean("userManager");
         assertTrue(userManager instanceof LdapUserManager);
         
         LdapUserManager lUserManager = (LdapUserManager) userManager;
         
         User user = userManager.get("admin");
         assertNotNull(user);
         
         UserDetails details = lUserManager.loadUserByUsername("admin");
         GrantedAuthority[] authorities = details.getAuthorities();
         System.out.println(authorities.length);
         
         assertTrue(authorities.length > 5);
 
         Set<Group> groups = user.getGroups();
         assertNotNull(groups);
         assertEquals(1, groups.size());
         
         UserDetailsWrapper wrapper = (UserDetailsWrapper) details;
         assertEquals(1, wrapper.getUser().getGroups().size());
         
         importHelloWsdl();
         
         // do our perms work?
         Workspace w = registry.getWorkspaces().iterator().next();
         Collection<Item> artifacts = w.getItems();
         
         assertEquals(1, artifacts.size());
         
 //        List<User> users = userManager.listAll();
 //        
 //        assertEquals(1, users.size());
         
     }
 
     protected String getPassword() {
         return "secret";
     }
     
     @Override
     protected ConfigurableApplicationContext createApplicationContext(
 	    String[] locations) {
	// Apache DS uses XBea
 	return new ClassPathXmlApplicationContext(locations);
     }
 
     @Override
     protected String[] getConfigLocations() {
         return new String[] { "/META-INF/applicationContext-core.xml",
                               "/META-INF/applicationContext-acegi-security.xml",
                               "/META-INF/applicationContext-test.xml",
                               "/META-INF/applicationContext-ldap.xml",
                               "server.xml"};
     }
 
 }
