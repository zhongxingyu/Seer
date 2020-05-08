 package eu.stratuslab.registration.utils;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import javax.mail.MessagingException;
 import javax.mail.internet.MimeUtility;
 
 public class Base64 {
 
     public static byte[] encode(byte[] b) {
         byte[] res = null;
 
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         OutputStream os = null;
         try {
             os = MimeUtility.encode(baos, "base64");
             os.write(b);
             res = baos.toByteArray();
         } catch (MessagingException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             closeReliably(os);
         }
         return res;
     }
 
     public static byte[] decode(byte[] b) {
         byte[] res = null;
 
         ByteArrayInputStream bais = new ByteArrayInputStream(b);
         InputStream is = null;
         try {
 
             is = MimeUtility.decode(bais, "base64");
             byte[] tmp = new byte[b.length];
             int n = is.read(tmp);
             res = new byte[n];
             System.arraycopy(tmp, 0, res, 0, n);
 
         } catch (MessagingException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             closeReliably(is);
         }
 
         return res;
     }
 
     private static void closeReliably(Closeable o) {
         if (o != null) {
             try {
                 o.close();
             } catch (IOException e) {
                 // TODO: Log this.
             }
         }
 
     }
 
 }
