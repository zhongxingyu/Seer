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

    public static String Format(Address[] ads) {

         String result = "";
        if (ads != null) {
            if (ads[0] instanceof InternetAddress) {
                result = ((InternetAddress) ads[0]).getAddress();
            } else {
                if (ads[0] instanceof NewsAddress) {
                    result = ((NewsAddress) ads[0]).getHost();
                }
             }
         }
         return result;
     }
 }
