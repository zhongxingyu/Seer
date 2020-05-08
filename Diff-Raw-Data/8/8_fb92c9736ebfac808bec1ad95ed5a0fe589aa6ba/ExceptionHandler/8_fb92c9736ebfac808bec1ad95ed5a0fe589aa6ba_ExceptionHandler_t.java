 package ro.satrapu.homebudget.ui.exceptionhandler;
 
 import java.util.Iterator;
 import javax.faces.FacesException;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.faces.event.ExceptionQueuedEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Handles uncaught exceptions.
  * <br/>
  * This class is based on an article found 
  * <a href="http://javalabor.blogspot.com/2011/09/jsf-2-global-exception-handling.html">here</a>.
  * @author satrapu
  */
 public class ExceptionHandler
         extends javax.faces.context.ExceptionHandlerWrapper {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
     private javax.faces.context.ExceptionHandler wrapped;
 
     public ExceptionHandler(final javax.faces.context.ExceptionHandler wrapped) {
         this.wrapped = wrapped;
     }
 
     @Override
     public javax.faces.context.ExceptionHandler getWrapped() {
         return wrapped;
     }
 
     @Override
     public void handle() throws FacesException {
         for (final Iterator<ExceptionQueuedEvent> it = getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();) {
             Throwable throwable = it.next().getContext().getException();
             logger.error("Caught an unexpected exception", throwable);
 
             FacesContext facesContext = FacesContext.getCurrentInstance();
             ExternalContext externalContext = facesContext.getExternalContext();
            externalContext.getSessionMap().put("unexpectedErrorDetails", ExceptionPrettyPrinter.prettyPrint(throwable));
 
             try {
                 String url = externalContext.getRequestContextPath() + "/faces/errors/unexpectedError.xhtml";
                 externalContext.redirect(url);
             } catch (Exception ex) {
                 logger.error("Could not redirect user to error page", ex);
             } finally {
                 it.remove();
             }
         }
 
         getWrapped().handle();
     }
 }
