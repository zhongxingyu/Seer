 package HiddenText;
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.Scanner;
 
 /**
  * Author: Aleksey Alekseenko
  * Date: 02.08.13
  */
 public abstract class HiddenView {
     Properties properties;
 
     public abstract StringBuilder hide(StringBuilder string);
 
     public HiddenView() {
         properties = loadProperties();
        if (!properties.getProperty("hiddenText").equals("true") && properties.getProperty("hiddenText").equals("false")
                 && isInteger(properties.getProperty("quantityOfFirstChars"))
                && isInteger(properties.getProperty("quantityOfLastChars"))) {
             System.out.println("Properties file isn't valid");
             properties = loadProperties();
         }
     }
 
     private Properties loadProperties() {
         Properties properties = new Properties();
         try {
             System.out.println("Enter property file location");
             Scanner scanner = new Scanner(System.in);
             String fileLocation = scanner.next();
             properties.load(new FileInputStream(fileLocation));
         } catch (IOException e) {
             System.out.println("Properties didnt load");
         }
         return properties;
     }
 
     private boolean isInteger(String quantityOfChars) {
         try {
             Integer.parseInt(quantityOfChars);
         } catch (NumberFormatException e) {
             return false;
         }
         return true;
     }
 
 
 }
