 package org.babblelang.engine;
 
 public class BabbleException extends RuntimeException {
     public BabbleException() {
         super();
     }
 
     public BabbleException(String message) {
         super(message);
     }
 
     public BabbleException(String message, Throwable cause) {
         super(message, cause);
     }
 
     public BabbleException(Throwable cause) {
         super(cause);
     }

    protected BabbleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
 }
