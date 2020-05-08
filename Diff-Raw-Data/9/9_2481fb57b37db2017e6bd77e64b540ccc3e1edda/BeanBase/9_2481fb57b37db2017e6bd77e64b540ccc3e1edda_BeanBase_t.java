 package cz.cvut.fel.beans;
 
 import lombok.EqualsAndHashCode;
 
import javax.annotation.PostConstruct;
 import javax.faces.application.FacesMessage;
 import javax.faces.context.ExternalContext;
 import javax.faces.context.FacesContext;
 import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedOutputStream;
 import java.io.IOException;
 import java.io.Serializable;
 
 /**
  * <p>Base class for beans providing comfort functionality</p>
  *
  * @author Karel Cemus
  */
 @EqualsAndHashCode
 public class BeanBase implements Serializable {
 
 
     /** report unsuccessful operation as a faces message */
     protected void addError( String formatMessage, Object... args ) {
 
         // create formatted message
         FacesMessage message = new FacesMessage( FacesMessage.SEVERITY_ERROR, String.format( formatMessage, args ), null );
 
         // add it to root message handler
         FacesContext.getCurrentInstance().addMessage( null, message );
     }
 
     /** add information faces message */
     protected void addInformation( String formatMessage, Object... args ) {
 
         // create formatted message
         FacesMessage message = new FacesMessage( FacesMessage.SEVERITY_INFO, String.format( formatMessage, args ), null );
 
         // add it to root message handler
         FacesContext.getCurrentInstance().addMessage( null, message );
     }
 
     /** unwrap exception to the most nested and return the original cause message */
     protected String processException( Throwable ex ) {
 
         // find the most nested exception
         while ( ex.getCause() != null ) {
             ex = ex.getCause();
         }
 
         // return the original error message
         return ex.getMessage();
     }
 
 
     /** send file to user as a text/plain file */
     protected void download( String name, int length, byte[] content ) {
         FacesContext facesContext = FacesContext.getCurrentInstance();
         ExternalContext externalContext = facesContext.getExternalContext();
         HttpServletResponse response = ( HttpServletResponse ) externalContext.getResponse();
 
         // Some JSF component library or some Filter might have set some headers in the buffer beforehand. We want to get rid of them, else it may collide.
         response.reset();
         // Check http://www.w3schools.com/media/media_mimeref.asp for all types. Use if necessary ServletContext#getMimeType() for auto-detection based on filename.
         response.setContentType( "plain/text; charset=utf-8" );
         // The Save As popup magic is done here. You can give it any filename you want, this only won't work in MSIE, it will use current request URL as filename instead.
         response.setHeader( "Content-disposition", "attachment; filename=\"" + name + "\"" );
 
         BufferedOutputStream output = null;
 
         try {
             output = new BufferedOutputStream( response.getOutputStream() );
             output.write( content, 0, length );
         } catch ( IOException e ) {
             addError( "File transfer failed. Please try it later." );
         } finally {
             if ( output != null ) {
                 try {
                     output.close();
                 } catch ( IOException ignored ) {
                     //  ignore
                 }
             }
         }
 
         // Important! Else JSF will attempt to render the response which obviously will fail since it's already written with a file and closed.
         facesContext.responseComplete();
     }

    @PostConstruct
    public void init() {

        // init session, solution for uninitialized context exception, this is a bit hack, multiple rich:calendar caused an issue
        FacesContext.getCurrentInstance().getExternalContext().getSession( true );
    }
 }
