 /**
  * FitnessException.java
  */
 package jmona;
 
 /**
  * This Exception is thrown when there is a problem determining the fitness of
  * an Individual.
  * 
  * @author jeff
  */
 public class FitnessException extends Exception {
 
  /**
   * Default generated serial version UID.
   */
   private static final long serialVersionUID = -926352878278367019L;
 
   /**
    * Instantiate this class by calling the default constructor of the
    * superclass.
    */
   public FitnessException() {
     super();
   }
 
   /**
    * Instantiate this Exception with the specified human-readable message.
    * 
    * @param message
    *          A human-readable message explaining the problem.
    */
   public FitnessException(final String message) {
     super(message);
   }
 
   /**
    * Instantiate this Exception with the specified human-readable message and
    * the specified cause.
    * 
    * @param message
    *          A human-readable message explaining the problem.
    * @param cause
    *          The cause of this Exception.
    */
   public FitnessException(final String message, final Throwable cause) {
     super(message, cause);
   }
 
   /**
    * Instantiate this Exception with the specified cause.
    * 
    * @param cause
    *          The cause of this Exception.
    */
   public FitnessException(final Throwable cause) {
     super(cause);
   }
 
 }
