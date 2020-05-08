 package gov.nih.nci.ncicb.cadsr.loader.jaas;
 
 import java.io.*;
 
 import javax.security.auth.callback.*;
 
 public class ConsoleCallbackHandler implements CallbackHandler {
 
   public ConsoleCallbackHandler() {
   }
 
   public void handle(Callback[] callbacks) 
     throws java.io.IOException, UnsupportedCallbackException {
 
     for (int i = 0; i < callbacks.length; i++) {
 
       if (callbacks[i] instanceof NameCallback) {
         System.out.print(((NameCallback)callbacks[i]).getPrompt());
        String user=(new BufferedReader(new InputStreamReader(System.in))).readLine();
         ((NameCallback)callbacks[i]).setName(user);
       } else if (callbacks[i] instanceof PasswordCallback) {
         System.out.print(((PasswordCallback)callbacks[i]).getPrompt());
        String pass=(new BufferedReader(new InputStreamReader(System.in))).readLine();
         ((PasswordCallback)callbacks[i]).setPassword(pass.toCharArray());
       } else {
         throw(new UnsupportedCallbackException(
                 callbacks[i], "Callback class not supported"));
       }
     }
   }
 }
 
