 package at.yawk.snap;
 
 import java.util.Properties;
 import java.util.Random;
 
 import nl.flotsam.xeger.Xeger;
 
 public class RegexIdGenerator implements IdGenerator {
     private final String regex;
     private final Random rng = new Random();
     
     public RegexIdGenerator(String regex) {
         this.regex = regex;
     }
     
     public RegexIdGenerator(Properties properties) {
         regex = properties.getProperty("idgen.regex.regex");
     }
     
     @Override
     public String generateId(long timeMilliSeconds, long timeSeconds) {
         String usedRegex = regex;
        usedRegex.replace("$ms", Long.toString(timeMilliSeconds));
        usedRegex.replace("$s", Long.toString(timeSeconds));
         return new Xeger(usedRegex, rng).generate();
     }
     
     @Override
     public void serializeSettings(Properties properties) {
         properties.setProperty("idgen.regex.regex", regex);
     }
 }
