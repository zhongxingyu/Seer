 package com.github.charliecorner.jnarwhal;
 
 import com.github.charliecorner.jnarwhal.exceptions.NoValidUserAgentDeclaredException;
 import java.io.UnsupportedEncodingException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Charlie Corner
  */
 public class Main {
     
     public static void main(String[] args) throws NoValidUserAgentDeclaredException {
         Session s = Session.createNewSSLSession("jNarwhal by /u/ccorner");
 
         try {
             List<Object> errores = s.login("", "");
             
             if(errores.isEmpty()){
                System.out.println("Exito");
             } else {
                System.out.println("No hubo exito");
                 
                 for (Object str : errores) {
                     System.out.println(str);
                 }
             }
         } catch (UnsupportedEncodingException ex) {
             Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 }
