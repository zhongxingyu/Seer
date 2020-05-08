 package bazooka.common.exception;
 
 public class ShootingException extends Exception {
 
   public ShootingException() {
    super();
   }
 
   public ShootingException(Throwable cause) {
    super(cause.getMessage(), cause);
   }
 
   public ShootingException(String message, Throwable cause) {
     super(message, cause);
   }
 }
