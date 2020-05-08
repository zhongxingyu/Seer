 package de.msquadrat.properties;
 
import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.Properties;
 
 import de.msquadrat.properties.source.PropertySource;
 import de.msquadrat.properties.source.PropertySources;
 
 public class Application implements Runnable {
     public static void main(String[] args) throws Exception {
         new Application(args).run();
     }
     
     public static String getName() {
         return "properties";
     }
     
     public static void die() {
         die(null);
     }
     
     public static void die(String message) {
         if (message != null)
             System.err.println(message);
         System.exit(1);
     }
     
     
     private final Properties props;
     private final ApplicationArguments args;
     
     protected Application(String[] args) throws Exception {
         this.args = new ApplicationArguments(args);
         this.props = new Properties();
     }
 
     
     public void run() {
         PropertySources sources = args.getSources();
         while (sources.peek() != null) {
             try (PropertySource source = sources.poll()) {
                 props.load(source.toReader());
             }
             catch (IOException e) {
                die("Error reading properties: " + e.getMessage());
             }
         }
         
         System.err.println(props);
     }
     
     
 }
