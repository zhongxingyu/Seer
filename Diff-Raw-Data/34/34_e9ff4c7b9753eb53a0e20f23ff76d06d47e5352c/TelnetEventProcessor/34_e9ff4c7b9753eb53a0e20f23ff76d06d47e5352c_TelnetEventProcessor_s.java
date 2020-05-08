 package model;
 
 import java.util.Observable;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TelnetEventProcessor extends Observable {
 
     private static Logger log = Logger.getLogger(TelnetEventProcessor.class.getName());
     private String string = null;
 
     public TelnetEventProcessor() {
     }
 
     private void stripAnsiColors() {
         Pattern regex = Pattern.compile("\\e\\[[0-9;]*m");
         Matcher regexMatcher = regex.matcher(string);
         string = regexMatcher.replaceAll(""); // *3 ??
     }
 
     public void parse(String string) {
         this.string = string;
         ifs();
     }
 
     //       [\w]+(?=\.) 
     private void ifs() {
         log.fine("checking..");
         if (string.contains("confusing the hell out of")) {
             Pattern pattern = Pattern.compile("[\\w]+(?=\\.)");  //(\w+)\.
             Matcher matcher = pattern.matcher(string);
             String enemy = null;
             GameData data = null;
             while (matcher.find()) {
                 enemy = matcher.group();
             }
            data = new GameData.Builder().enemy(enemy).build();
             log.fine("new data object\t\t" + data.getEnemy());
             setChanged();
             notifyObservers(data);
 
         } else if (string.contains("Enter 3-letter city code:")) {
             log.fine("found enter city code");
         } else {
         }
     }
 }
