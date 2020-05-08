 package bazooka.common.exception;
 
 public class ShootingException extends Exception {
 
   public ShootingException() {
    this(null);
   }
 
   public ShootingException(Throwable cause) {
    this(cause.getMessage(), cause);
   }
 
   public ShootingException(String message, Throwable cause) {
     super(message, cause);
   }
 }
