 package de.skuzzle.polly.sdk.exceptions;
 
 
 public class ConstraintException extends Exception {
 
     private static final long serialVersionUID = 1L;
 
     public ConstraintException() {
         super();
     }
 
    public ConstraintException(String arg0, Throwable arg1, boolean arg2,
        boolean arg3) {
        super(arg0, arg1, arg2, arg3);
    }

     public ConstraintException(String arg0, Throwable arg1) {
         super(arg0, arg1);
     }
 
     public ConstraintException(String arg0) {
         super(arg0);
     }
 
     public ConstraintException(Throwable arg0) {
         super(arg0);
     }

    
    
 }
