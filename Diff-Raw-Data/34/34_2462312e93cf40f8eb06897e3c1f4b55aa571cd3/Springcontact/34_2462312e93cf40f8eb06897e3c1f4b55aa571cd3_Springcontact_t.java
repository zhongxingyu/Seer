 package net.bioclipse.brunn;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
import java.util.List;
 
 import net.bioclipse.usermanager.Activator;
 import net.bioclipse.usermanager.business.IUserManager;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.Platform;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 public class Springcontact {
 	
	private final static String PLUGIN_ID = "net.bioclipse.brunn.core";
 	public static ApplicationContext CONTEXT;
 	
 	public static Object getBean(String beanName){
 		
 		if (CONTEXT == null) {
 			rebuildContext();
 		}
 		return CONTEXT.getBean(beanName);
 	}
 	
 	public static URL getPluginURL() {
 		try {
 	        return FileLocator.toFileURL(Platform.getBundle(PLUGIN_ID).getEntry("/"));
         }
         catch (IOException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
         return null;
 	}
 
 	public static void rebuildContext() {
 		try {
 			CONTEXT = new FileSystemXmlApplicationContext( getPluginURL() + 
 					                                       "src" +
 			                                               File.separator + 
 			                                               "applicationContext.xml" );
 			
 			BasicDataSource dataSource = (BasicDataSource) CONTEXT.getBean("dataSource");
 			IUserManager userManager = Activator.getDefault().getUserManager();
			List<String> accounts = userManager.getAccountIdsByAccountTypeName("BrunnAccountType");
			if (accounts.size() > 1) {
				throw new IllegalStateException("Found multiple Brunn accounts. Brunn doesn't support that in this version");
			}
			dataSource.setUrl(      userManager.getProperty(accounts.get(0), "URL") );
			dataSource.setUsername( userManager.getProperty(accounts.get(0), "Database user") );
			dataSource.setPassword( userManager.getProperty(accounts.get(0), "Database password") );
			if ( !userManager.isLoggedInWithAccountType("BrunnAccountType") ) {
 				throw new IllegalStateException("not logged in or the logged in user lacks LisAccount");
 			}
 		}
 		//not running from the lis_gui project but rather running test code
 		//TODO: this is ugly with a special case for running tests figure out something else maybee?
 		catch(NullPointerException e) {
			System.out.println("Running in tests mode. If not tunning tests this is wrong.");
			System.out.println("See Springcontact.rebuildContext()");
 			CONTEXT = new ClassPathXmlApplicationContext("applicationContext.xml");
 		}
     }
 }
