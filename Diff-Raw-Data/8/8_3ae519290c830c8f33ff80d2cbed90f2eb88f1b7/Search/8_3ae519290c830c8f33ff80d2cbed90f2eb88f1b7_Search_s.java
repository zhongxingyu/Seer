 package DistGrep;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kyle
  * Date: 9/14/13
  * Time: 9:23 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Search {
 
     public Search() {
 
     }
 
     private static String searchKeys(String body) {
         final Pattern keyp = Pattern.compile("<key>(.+?)</key>");
         final Matcher keym = keyp.matcher(body);
         keym.find();
         return keym.group(1);
     }
 
     private static String searchValues(String body) {
         final Pattern valp = Pattern.compile("<value>(.+?)</value>");
         final Matcher valm = valp.matcher(body);
         valm.find();
         return valm.group(1);
     }
 
     public static String generateFinalCommand(String path, String body) {
         String finalcommand = null;
 
         //Check, what the user is looking for (keys, values or keys and values)
         if(body.contains("<key>") && body.contains("<value>")) {
 
             final Pattern op = Pattern.compile("<operator>(.+?)</operator>");
             final Matcher om = op.matcher(body);
             om.find();
             String operator = om.group(1);
 
             if(operator.equals("and")) {
 
                 String key = searchKeys(body);
                 String value = searchValues(body);
                finalcommand = "cat "+path +" | cut -d \",\" -f 1,2 | egrep "+key+" | egrep "+value+"";
 
             } else {
 
                 String key = searchKeys(body);
                 String value = searchValues(body);
                 finalcommand = "grep \""+key+"\\|"+value+"\" "+ path;
 
             }
 
         } else if (body.contains("<key>") && !(body.contains("<value>"))) {
 
             String key = searchKeys(body);
            finalcommand = "cat "+path +" | cut -d \",\" -f 1 | egrep "+key;
 
         } else if (!(body.contains("<key>")) && body.contains("<value>")) {
 
             String value = searchValues(body);
            finalcommand = "cat "+path +" | cut -d \",\" -f 2 | egrep "+value+"";
         }
 
         return finalcommand;
     }
 
     public static CommandExecutor runSearch(String path, String body) throws IOException, InterruptedException {
 
         String finalcommand = generateFinalCommand(path, body);
 
         if(finalcommand == null)
             throw new IOException("Failed to generate command from the client's message. ");
 
 
         CommandExecutor cex = new CommandExecutor();
         cex.execute(finalcommand);
         return cex;
 
     }
 
 }
