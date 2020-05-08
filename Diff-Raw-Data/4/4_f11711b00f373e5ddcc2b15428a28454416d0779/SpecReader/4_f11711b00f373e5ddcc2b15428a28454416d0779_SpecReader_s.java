 package spec;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class SpecReader {
     private InputStream input;
     private Spec spec;
 
     public SpecReader(InputStream input) {
         this.input = input;
         this.spec = new Spec();
     }
 
     private void readCharClass(String line) {
         Pattern p = Pattern.compile("\\$([A-Z\\-]+) ((\\[.*\\])|(\\[\\^.*\\]) IN \\$([A-Z\\-]+))");
         Matcher matcher = p.matcher(line);
         matcher.matches();
 
         String charClassName = matcher.group(1);
 
         String re = null;
         if (matcher.group(3) != null) {
             re = matcher.group(2);
         } else {
            re = matcher.group(4) + "IN" + spec.getCharClass(matcher.group(5)).getRe();
         }
         spec.addCharClass(charClassName, new CharClass(re));
     }
 
     private void readTokenDef(String line) {
         Pattern p = Pattern.compile("\\$([A-Z\\-]+) (.*)");
         Matcher matcher = p.matcher(line);
         matcher.matches();
 
         String name = matcher.group(1);
         String re = matcher.group(2);
         TokenType tokenType = new TokenType(name, re, spec.getCharClasses());
 
         spec.addTokenType(tokenType);
     }
 
     public Spec specify() {
         BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
         try {
             String line;
             boolean inCharClassSection = true;
 
             while ((line = reader.readLine()) != null) {
                 if (line.equals("")) {
                     inCharClassSection = false;
                     continue;
                 }
 
                 if (inCharClassSection) {
                     readCharClass(line);
                 } else {
                     readTokenDef(line);
                 }
             }
         } catch (IOException e) {
             throw new RuntimeException();
         }
 
         return spec;
     }
 }
