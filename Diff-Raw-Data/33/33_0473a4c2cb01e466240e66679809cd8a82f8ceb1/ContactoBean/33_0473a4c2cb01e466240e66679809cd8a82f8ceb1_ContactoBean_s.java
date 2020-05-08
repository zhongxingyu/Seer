 package cl.sebastian.portal.jsf;
 
 import cl.sebastian.servicio.ServicioEmail;
 import cl.sebastian.webutils.utils.FacesUtils;
 import java.io.Serializable;
 import javax.annotation.Resource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 
 /**
  *
  * @author Sebastián Salazar Molina <sebasalazar@gmail.com>
  */
 @Component
 @Scope("request")
 @Qualifier("contactoBean")
 public class ContactoBean implements Serializable {
 
     @Resource(name = "correoContacto")
     private String correoContacto = null;
     @Resource(name = "servicioEmail")
     private ServicioEmail servicioEmail = null;
     private String nombre = null;
     private String email = null;
     private String asunto = null;
     private String mensaje = null;
     private static Logger logger = LoggerFactory.getLogger(ContactoBean.class);
 
     public void enviarMail() {
         try {
             String from = nombre + " <" + email + ">";
            boolean sendMail = servicioEmail.sendMail(correoContacto, from, asunto, mensaje);
             if (sendMail) {
                 FacesUtils.infoMessage("correoEnviado");
             } else {
                 FacesUtils.errorMessage("correoNoEnviado");
             }
 
         } catch (Exception e) {
             logger.error(e.toString());
             logger.debug("Error al enviar email", e);
         }
     }
 
     public String getNombre() {
         return nombre;
     }
 
     public void setNombre(String nombre) {
         this.nombre = nombre;
     }
 
     public String getEmail() {
         return email;
     }
 
     public void setEmail(String email) {
         this.email = email;
     }
 
     public String getAsunto() {
         return asunto;
     }
 
     public void setAsunto(String asunto) {
         this.asunto = asunto;
     }
 
     public String getMensaje() {
         return mensaje;
     }
 
     public void setMensaje(String mensaje) {
         this.mensaje = mensaje;
     }
 }
