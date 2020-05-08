 
 package pt.uac.cafeteria.model;
 
import java.util.logging.Level;
import java.util.logging.Logger;
 
 public class ApplicationException extends RuntimeException {
 
     /**
      * Creates a new instance of <code>ApplicationException</code> without detail message.
      */
     public ApplicationException() {
     }
 
     /**
      * Constructs an instance of <code>ApplicationException</code> with the specified detail message.
      * @param msg the detail message.
      */
     public ApplicationException(String msg) {
         super(msg);
     }

    public ApplicationException(String msg, Exception e) {
        super(msg);
        Logger.getLogger(e.getClass().getName()).log(Level.WARNING, null, e);
    }
 }
