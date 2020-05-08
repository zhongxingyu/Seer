 import java.util.regex.*;
 import java.io.*;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Collection;
 
 class UrlStuff {
   public static void main(String[] args) {
     final HashMap<String, String> h = new HashMap<String, String>();
     final Pattern regex = Pattern.compile("embedCode=([\\w_]+)[\\&|\\?|\\$]", Pattern.CASE_INSENSITIVE);
     String l  = null;
     Matcher m = null;
 
     // Loop over the arguments and open each file. Then run the
     // regexp against each line and save in a hash the matches
     for (int i=0; i<args.length; i++) {
       try {
         BufferedReader br = new BufferedReader(new FileReader(args[i]));  
         while ((l = br.readLine()) != null) {  
           m = regex.matcher(l);
           if (m.find()) {
             h.put(m.group(1), null);
           }
         } 
         br.close();
       } 
       catch (Exception e) {
         System.err.println("Error: " + e.getMessage());
       }
     }
 
     // Print results
     for (Iterator it = h.keySet().iterator(); it.hasNext();) {
      System.out.println(it.next());
     }
 
     System.exit(0);
   }
 }
