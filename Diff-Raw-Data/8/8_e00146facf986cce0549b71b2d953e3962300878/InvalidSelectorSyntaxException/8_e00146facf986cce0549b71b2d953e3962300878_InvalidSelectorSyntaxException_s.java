 package au.com.miskinhill.rdftemplate.selector;
 
 import org.antlr.runtime.RecognitionException;
 
 public class InvalidSelectorSyntaxException extends RuntimeException {
     
     private static final long serialVersionUID = 5805546105865617336L;
 
    public InvalidSelectorSyntaxException(RecognitionException e) {
        super(e);
     }
 
 }
