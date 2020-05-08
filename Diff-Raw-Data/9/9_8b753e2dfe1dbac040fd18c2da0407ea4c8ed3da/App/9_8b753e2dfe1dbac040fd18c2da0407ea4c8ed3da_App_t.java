 package ru.direvius.cmp;
 
 import java.io.IOException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.security.GeneralSecurityException;
 import java.util.Date;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Hello world!
  *
  */
 public class App {
 
     private static final Logger logger = LoggerFactory.getLogger("Main");
 
     public static void main(String[] args) {
         try {
             System.setProperty("java.net.preferIPv4Stack", "true");
             Socket s = new Socket("10.0.3.70", 688);
             CMPClient cc = new CMPClient(s.getInputStream(), s.getOutputStream());
             cc.open();
            //System.out.println(new Date().getTime());
            //System.exit(0);
             cc.sendEncrypt(new CardInfoRequestBuilder(0x8782F8, 900000010409259L, new Date()).build());
             cc.receiveDecrypt();
             cc.sendEncrypt(new BonusRequestBuilder(new Date()).build());
             //cc.sendEncrypt(new PartlyReturnRequestBuilder(new Date()).build());
             cc.receiveDecrypt();
             cc.sendEncrypt(new CloseCheckRequestBuilder().build());
             cc.receiveDecrypt();
             Thread.sleep(5000);
             cc.sendEncrypt(new CardInfoRequestBuilder(0x8782F8, 900000010409259L, new Date()).build());
             cc.receiveDecrypt();
             //cc.sendEncrypt(new BonusRequestBuilder(new Date()).build());
             cc.sendEncrypt(new PartlyReturnRequestBuilder(new Date()).build());
             cc.receiveDecrypt();
             //cc.sendEncrypt(new CloseCheckRequestBuilder().build());
             //cc.receiveDecrypt();
             cc.close();
         } catch (GeneralSecurityException ex) {
             logger.error("Cryptography problem", ex);
         } catch (InterruptedException ex) {
             logger.error("Girl, interrupted", ex);
         } catch (UnknownHostException ex) {
             logger.error("Host not found while opening a connection", ex);
         } catch (IOException ex) {
             logger.error("Failed to comunicate", ex);
         } catch (Exception ex) {
             logger.error("Generic exception", ex);
         } finally {
             System.exit(0);
         }
     }
 }
