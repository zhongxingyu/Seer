 package au.com.miskinhill.rdftemplate.selector;
 
 import org.antlr.runtime.RecognitionException;
 
 public class InvalidSelectorSyntaxException extends RuntimeException {
     
     private static final long serialVersionUID = 5805546105865617336L;
 
    public InvalidSelectorSyntaxException(Throwable cause) {
        super(cause);
    }
    
    public InvalidSelectorSyntaxException(String message) {
        super(message);
     }
 
 }
