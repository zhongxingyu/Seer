 package org.swordapp.server;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class SwordError extends Exception
 {
     private String errorUri;
     private int status = -1;
    private boolean hasBody = true;
 
     public SwordError()
     {
         super();
     }
 
    public SwordError(int status)
    {
        super();
        this.status = status;
        this.hasBody = false;
    }

     public SwordError(String errorUri)
     {
         super(errorUri);
         this.errorUri = errorUri;
     }
 
     public SwordError(String errorUri, int status)
     {
         super(errorUri);
         this.errorUri = errorUri;
         this.status = status;
     }
 
     public SwordError(String errorUri, String message)
     {
         super(message);
         this.errorUri = errorUri;
     }
 
     public SwordError(String errorUri, int status, String message)
     {
         super(message);
         this.errorUri = errorUri;
         this.status = status;
     }
 
     public SwordError(String errorUri, Throwable cause)
     {
         super(errorUri, cause);
         this.errorUri = errorUri;
     }
 
     public SwordError(String errorUri, int status, Throwable cause)
     {
         super(errorUri, cause);
         this.errorUri = errorUri;
         this.status = status;
     }
 
     public SwordError(String errorUri, String message, Throwable cause)
     {
         super(message, cause);
         this.errorUri = errorUri;
     }
 
     public SwordError(String errorUri, int status, String message, Throwable cause)
     {
         super(message, cause);
         this.errorUri = errorUri;
         this.status = status;
     }
 
     public String getErrorUri()
     {
         return errorUri;
     }
 
     public int getStatus()
     {
         return status;
     }

    public boolean hasBody()
    {
        return hasBody;
    }
 }
