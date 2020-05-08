 package cornell.cs3300.nosql;
 
 /**
  * Exception thrown when an error has occured on the server
  */
 public class ServerException extends Exception {
 	private static final long serialVersionUID = 1L;
 
 	public static enum ErrorType {
 		CUSTOMER_NOT_FOUND,
 		CANDY_NOT_FOUND,
 		INVALID_INPUT,
		NO_CLUSTERS,
		UNKNOWN_ERROR
 	}
 
 	private ErrorType error;
 
 	public ServerException() {
		super("Unknown error");
		this.error = ErrorType.UNKNOWN_ERROR;
 	}
 
 	public ServerException(ErrorType errorType, String message) {
 		super(message);
 		this.error = errorType;
 	}
 
 	public ServerException(ErrorType errorType, String message, Throwable cause) {
 		super(message, cause);
 		this.error = errorType;
 	}
 
 	public ErrorType getErrorType() {
 		return error;
 	}
 
 	@Override
 	public String toString() {
 		return error + " - " + super.toString();
 	}
 }
