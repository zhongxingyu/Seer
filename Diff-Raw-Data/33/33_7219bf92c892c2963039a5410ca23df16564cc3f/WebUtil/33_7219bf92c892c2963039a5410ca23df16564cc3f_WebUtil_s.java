 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Vista;
 
 import java.util.Properties;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 /**
  *
  * @author Sergio
  */
 public abstract class WebUtil {
     
     public static boolean enviarEmail(String to, String from, String mensaje, String asunto) {
         return enviarEmail(to, from,mensaje,asunto, null);
     }
     
     public static boolean enviarEmail(String to, String from, String mensaje, String asunto, String[][] rutas) {
         try {
             Properties props = new Properties();
             // Nombre del host de correo, es smtp.gmail.com
             props.setProperty("mail.smtp.host", "smtp.gmail.com");
             // TLS si está disponible
             props.setProperty("mail.smtp.starttls.enable", "true");
             // Puerto de gmail para envio de correos
             props.setProperty("mail.smtp.port","587");
             // Nombre del usuario
             props.setProperty("mail.smtp.user", "mirdware@gmail.com");
             // Si requiere o no usuario y password para conectarse.
             props.setProperty("mail.smtp.auth", "true");
             Session session = Session.getDefaultInstance(props);
             // Para obtener un log de salida más extenso
             session.setDebug(true);
             
             MimeMessage message = new MimeMessage(session);
             // Quien envia el correo
             message.setFrom(new InternetAddress(from));
             // A quien va dirigido
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
             message.setSubject(asunto);
             if(rutas != null) {
                 BodyPart texto = new MimeBodyPart();
                 MimeMultipart multiParte = new MimeMultipart();
                 texto.setContent(mensaje, "text/html");
                 for(int i = 0, l=rutas.length; i<l; i++) {
                     BodyPart adjunto = new MimeBodyPart();
                     adjunto.setDataHandler(new DataHandler(new FileDataSource(rutas[i][0])));
                     adjunto.setFileName(rutas[i][1]);
                     multiParte.addBodyPart(adjunto);
                 }
                 multiParte.addBodyPart(texto);
                 message.setContent(multiParte);
             } else {
                 message.setText(mensaje,"ISO-8859-1","html");
             }
             Transport t = session.getTransport("smtp");
 
             // Aqui usuario y password de gmail
             t.connect("mirdware@gmail.com","Sena2012");
             t.sendMessage(message,message.getAllRecipients());
             t.close();
             return true;
         } catch (MessagingException ex) {
             Logger.getLogger(WebUtil.class.getName()).log(Level.SEVERE, null, ex);
             return false;
         }
     }
     
     //metodo para validar correo electronio
     public static boolean isEmail(String email) {
         boolean result = true;
        try {
           InternetAddress emailAddr = new InternetAddress(email);
           emailAddr.validate();
        } catch (AddressException ex) {
           result = false;
        }
        return result;
     }
     
     public static String randomString (int longitud){
         String cadenaAleatoria = "";
         long milis = new java.util.GregorianCalendar().getTimeInMillis();
         Random r = new Random(milis);
         int i = 0;
         while ( i < longitud){
             char c = (char)r.nextInt(255);
             if ( (c >= '0' && c <='9') || (c >='A' && c <='Z') ){
                 cadenaAleatoria += c;
                 i ++;
             }
         }
         return cadenaAleatoria;
     }
 }
