 package cc.explain.server.utils;
 
 import org.springframework.mock.jndi.SimpleNamingContextBuilder;
 
 import javax.naming.NamingException;
import java.io.*;
 import java.util.Properties;
 
 /**
  * User: kzimnick
  * Date: 02.08.13
  * Time: 18:00
  */
 public class TestUtils {
 
     private TestUtils(){}
 
     public static void initTestJNDIFromPropertiesFile() throws IOException, NamingException {
        System.out.println(new File(".").getAbsolutePath());
         SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
         Properties prop = new Properties();
         InputStream stream = new FileInputStream("config/jndi.properties");
         prop.load(stream);
         stream.close();
 
         for(Object key : prop.keySet()){
             builder.bind((String)key, prop.get(key));
         }
         builder.activate();
     }
 
 }
