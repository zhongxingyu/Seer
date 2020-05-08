 package cl.sebastian.portal.jsf;
 
 import java.io.Serializable;
 import javax.annotation.PostConstruct;
import org.primefaces.event.TabChangeEvent;
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
@Scope("session")
 @Qualifier("menuBean")
 public class MenuBean implements Serializable {
 
     private Integer activeMenuIndex = null;
     private static Logger logger = LoggerFactory.getLogger(MenuBean.class);
 
     @PostConstruct
     public void iniciar() {
         try {
            activeMenuIndex = 0;
         } catch (Exception e) {
             logger.error(e.toString());
             logger.debug("Error al iniciar PostConstruct", e);
         }
     }
 
     public Integer getActiveMenuIndex() {
         return activeMenuIndex;
     }
 
     public void setActiveMenuIndex(Integer activeMenuIndex) {
         this.activeMenuIndex = activeMenuIndex;
     }
 }
