 package org.jailsframework.loaders;
 
 import junit.framework.Assert;
 import org.jailsframework.JailsProjectTestBase;
import org.jailsframework.database.IDatabase;
 import org.junit.Test;
 
 /**
  * @author <a href="mailto:sanjusoftware@gmail.com">Sanjeev Mishra</a>
  * @version $Revision: 0.1
  *          Date: Apr 4, 2010
  *          Time: 2:50:13 PM
  */
 public class DatabaseConfigurationTest extends JailsProjectTestBase {
     @Test
     public void shouldReadTheDatabasePropertiesFileAndLoadTheDatabaseConfiguration() {
        IDatabase database = DatabaseConfiguration.getInstance(project).getDatabase();
        Assert.assertEquals("root", database.getUser());
        Assert.assertEquals("com.mysql.jdbc.Driver", database.getDriver());
        Assert.assertEquals("password", database.getPassword());
        Assert.assertEquals("jails_development", database.getName());
     }
 }
