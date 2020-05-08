 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package EmailReader;
 
 import javax.mail.Address;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.NewsAddress;
 
 /**
  *
  * @author Roman Nort
  */
 public class MessageAddressFormatter {
    
    public static String Format(Address ad){
        
         String result = "";
        if (ad instanceof InternetAddress){
            result = ((InternetAddress)ad).getAddress();
        } else {
            if (ad instanceof NewsAddress) {
                result = ((NewsAddress)ad).getHost();
             }
         }
         return result;
     }
 }
