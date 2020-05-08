 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package resource;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.newdawn.slick.util.ResourceLoader;
 
 /**
  *
  * @author Andy
  */
 public class TextResource extends resource.Resource {
 
     String path;
     String textString;
 
     public TextResource(String path) {
         this.path = path;
     }
 
     @Override
     protected boolean load() {
         try {
             StringBuilder sb = new StringBuilder();
             InputStream is = ResourceLoader.getResourceAsStream("res/" + path);
             BufferedReader br = new BufferedReader(new InputStreamReader(is));
             String line;
             while ((line = br.readLine()) != null) {
                 sb.append(line);
                sb.append("\n");
             }
             this.textString = sb.toString();
         } catch (IOException ex) {
             return false;
         }
         return true;
     }
     
     public String getTextString() {
         return textString;
     }
 }
