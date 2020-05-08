 package de.weltraumschaf.caythe.frontend.caythe;
 
 import de.weltraumschaf.caythe.frontend.ErrorCode;
 
 /**
  * Error codes for Cay-The.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public enum CayTheErrorCode implements ErrorCode {
     INVALID_CHARACTER("Invalid character"),
    INVALID_NUMBER("Invalid number"),
    RANGE_INTEGER("Integer literal out of range"),
    RANGE_REAL("Real literal out of range"),
    
     // Fatal errors:
     TOO_MANY_ERRORS(-102, "Too many syntax errors"),
     EMPTY_INPUT_ERROR(-103, "Empty source file given");
 
     /**
      * The status.
      *
      * Negative numbers for fatal errors.
      */
     private int status;
     /**
      * Error message.
      */
     private String message;
 
     /**
      * Initializes status with 0.
      *
      * @param message
      */
     CayTheErrorCode(String message) {
 	this(0, message);
     }
 
     /**
      * Designated constructor.
      *
      * @param status
      * @param message
      */
     CayTheErrorCode(int status, String message) {
 	this.status  = status;
 	this.message = message;
     }
 
     @Override
     public int getStatus() {
 	return status;
     }
 
     @Override
     public String getMessage() {
         return message;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder(message);
 
         if (status != 0) {
             sb.append(" (status: ").append(status).append(")");
         }
 
         sb.append('!');
 
 	return sb.toString();
     }
 
 }
