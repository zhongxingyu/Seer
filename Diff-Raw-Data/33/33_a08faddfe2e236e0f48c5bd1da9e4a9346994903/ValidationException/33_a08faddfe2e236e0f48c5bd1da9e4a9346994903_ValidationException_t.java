 package dfh.cli;
 
 /**
 * Exception thrown when validation fails. The error message will be collected
 * and added to an error list.
  * <p>
  * <b>Creation date:</b> Nov 4, 2011
  * 
  * @author David Houghton
  * 
  */
 public class ValidationException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public ValidationException(String error) {
 		super(error);
 	}
 }
