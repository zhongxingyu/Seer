 package org.otherobjects.cms.util;
 
 import groovy.lang.Binding;
 import groovy.lang.GroovyShell;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.acegisecurity.Authentication;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
 import org.acegisecurity.userdetails.UserDetails;
 import org.apache.commons.io.IOUtils;
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.dao.DaoService;
 import org.otherobjects.cms.dao.UserDao;
 import org.otherobjects.cms.model.Role;
 import org.otherobjects.cms.model.User;
 import org.otherobjects.cms.types.JcrTypeServiceImpl;
 import org.otherobjects.cms.types.TypeDef;
 import org.otherobjects.cms.types.TypeDefDao;
import org.otherobjects.cms.types.TypeService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.core.io.Resource;
 
 /**
  * Bootstrap utility that runs on site start up. Ensures all initial config data 
  * is correctly initalised.
  * 
  * <p>This curretntly does this by runnig the script in <code>bootstrap-data/setup.groovy</code>.
  * 
  * @author rich
  */
 @SuppressWarnings("unchecked")
 public class BootstrapUtils //implements ApplicationListener
 {
     private final Logger logger = LoggerFactory.getLogger(BootstrapUtils.class);
 
     private DaoService daoService;
     private Resource bootstrapScript;
     private JcrTypeServiceImpl jcrTypeService; 
 
     public void setJcrTypeService(JcrTypeServiceImpl jcrTypeService) {
 		this.jcrTypeService = jcrTypeService;
 	}
 
 	public void setBootstrapScript(Resource bootstrapScript)
     {
         this.bootstrapScript = bootstrapScript;
     }
 
     public void setDaoService(DaoService daoService)
     {
         this.daoService = daoService;
     }
 
     /**
      * Run the bootstrap script.
      */
     public void bootstrap()
     {
         // FIXME Run on first startup only
 
         try
         {
         	UserDao userDao = (UserDao) this.daoService.getDao(User.class);
             UserDetails adminUser = userDao.loadUserByUsername("admin");
 
             // Create admin user if one does not exist and then run setup script
             if (adminUser == null)
             {
                 adminUser = createUser();
 
                 // FIXME Can this be done in a cleaner way?
                 Authentication authentication = new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities());
                 SecurityContextHolder.getContext().setAuthentication(authentication);
                 
                 loadTypes();
                 
                 runScript(this.bootstrapScript.getInputStream());
                 runScript(new FileInputStream("src/main/resources/site.resources/bootstrap-data/setup.script"));
             }
             else
             {
            	// load jcr backed typeDefs
            	jcrTypeService.loadJcrBackedTypes((TypeDefDao) daoService.getDao(TypeDef.class));
             }
         }
         catch (Exception e)
         {
             this.logger.error("Could not bootstrap data.", e);
             throw new OtherObjectsException("Could not bootstrap data.", e);
         }
         finally
         {
             // Log out
             SecurityContextHolder.clearContext();
         }
     }
 
     public void loadTypes()
     {
         // load jcr backed typeDefs
         jcrTypeService.loadJcrBackedTypes((TypeDefDao) daoService.getDao(TypeDef.class));
     }
     
     protected User createUser()
     {
         Role role = new Role("ROLE_ADMIN", "Adminstrator role");
         role = (Role) this.daoService.getDao(Role.class).save(role);
         Role role2 = new Role("ROLE_USER", "User role");
         role2 = (Role) this.daoService.getDao(Role.class).save(role2);
         Role role3 = new Role("ROLE_TEST", "Test role with no function");
         role3 = (Role) this.daoService.getDao(Role.class).save(role3);
 
         List<Role> roles = new ArrayList<Role>();
         roles.add(role);
         roles.add(role2);
 
         User adminUser = new User();
         //adminUser.setId(1L);
         adminUser.setUsername("admin");
         adminUser.setFirstName("The");
         adminUser.setLastName("Administrator");
         adminUser.setPassword("d033e22ae348aeb5660fc2140aec35850c4da997");
         adminUser.setEmail("admin@mycompany.com");
         adminUser.setRoles(roles);
         adminUser.setAccountExpired(false);
         adminUser.setAccountLocked(false);
         adminUser.setEnabled(true);
         adminUser.setPasswordHint("See the command line output for the temporary admin password.");
         return (User) this.daoService.getDao(User.class).save(adminUser);
     }
 
     protected void runScript(InputStream is) throws IOException
     {
         //        BSFManager manager = new BSFManager();
         //        manager.declareBean("daoService", daoService, daoService.getClass());
         //        manager.eval("groovy", "script.groovy", 0, 0, script);
 
         Binding binding = new Binding();
         binding.setVariable("daoService", this.daoService);
         GroovyShell shell = new GroovyShell(binding);
         String script = IOUtils.toString(is);
         shell.evaluate(script);
     }
 }
