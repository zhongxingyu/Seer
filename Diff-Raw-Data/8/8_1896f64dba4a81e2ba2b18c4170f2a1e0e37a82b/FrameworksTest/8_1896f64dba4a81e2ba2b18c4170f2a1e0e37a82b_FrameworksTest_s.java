 package chapter19.practice6;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.*;
 import static org.junit.Assume.*;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.BeforeClass;
import org.junit.Test;
 import org.junit.experimental.theories.DataPoints;
 import org.junit.experimental.theories.Theories;
 import org.junit.experimental.theories.Theory;
 import org.junit.runner.RunWith;
 
 @RunWith(Theories.class)
 public class FrameworksTest {
     @DataPoints
     public static ApplicationServer[] APP_SERVER_PARAMS = ApplicationServer.values();
     @DataPoints
     public static Database[] DATABASE_PARAMS = Database.values();
     static Map<String, Boolean> SUPPORTS = new HashMap<>();
 
     @BeforeClass
     public static void setUpClass() throws Exception {
         InputStream input = FrameworksTest.class.getResourceAsStream("support.tsv");
 
         try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
             String line;
             while ((line = reader.readLine()) != null) {
                 String[] params = line.split("\\t");
                 SUPPORTS.put(params[0] + "-" + params[1], Boolean.valueOf(params[2]));
             }
         }
     }
 
     @Theory
     public void testIsSupport_trueを返す(ApplicationServer applicationServer, Database database) throws Exception {
         assumeTrue(isSupport(applicationServer, database));
         String description = ", Application Server: " + applicationServer + ", Database: " + database;
         assertThat(description, Frameworks.isSupport(applicationServer, database), is(true));
     }
 
     @Theory
     public void testIsSupport_falseを返す(ApplicationServer applicationServer, Database database) throws Exception {
         assumeTrue(!isSupport(applicationServer, database));
         String description = ", Application Server: " + applicationServer + ", Database: " + database;
         assertThat(description, Frameworks.isSupport(applicationServer, database), is(false));
     }
 
     private boolean isSupport(ApplicationServer applicationServer, Database database) {
         return SUPPORTS.get(applicationServer.toString() + "-" + database.toString());
     }
 
 }
