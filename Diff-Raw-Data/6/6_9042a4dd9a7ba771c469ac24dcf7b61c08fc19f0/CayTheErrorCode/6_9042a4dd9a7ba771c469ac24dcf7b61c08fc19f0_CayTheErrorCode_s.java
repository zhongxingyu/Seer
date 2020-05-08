 package de.weltraumschaf.caythe.frontend.caythe;
 
 import de.weltraumschaf.caythe.frontend.ErrorCode;
 
 /**
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  * @license http://www.weltraumschaf.de/the-beer-ware-license.txt THE BEER-WARE LICENSE
  */
 public enum CayTheErrorCode implements ErrorCode {
 
     TOO_MANY_ERRORS(-102, "Too many syntax errors"),
     EMPTY_INPUT_ERROR(-103, "Empty source file given");
    
     private int status;
     private String message;
 
     CayTheErrorCode(String message) {
 	this(0, message);
     }
 
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
