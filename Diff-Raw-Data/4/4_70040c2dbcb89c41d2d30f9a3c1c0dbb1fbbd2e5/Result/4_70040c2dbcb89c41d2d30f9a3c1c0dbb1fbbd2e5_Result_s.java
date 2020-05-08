 package about.monads.java;
 
 /**
 * Parse Result which would carry additional information
 * in a more complex example.
  */
 public interface Result {
 	String getMessage();
 	boolean isError();
 	boolean isSuccess();
 	String getValue();
 }
 
 class Success implements Result {
 	private String value;
 
 	public Success(String value) {
 		this.value = value;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public boolean isError() {
 		return false;
 	}
 
 	public boolean isSuccess() {
 		return true;
 	}
 
 	public String getMessage() {
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Success(%s)", getValue());
 	}
 }
 
 class Error implements Result {
 	private String message;
 
 	public Error(String message) {
 		this.message = message;
 	}
 
 	public boolean isError() {
 		return true;
 	}
 
 	public boolean isSuccess() {
 		return false;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public String getValue() {
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		return String.format("Error(\"%s\")", getMessage());
 	}
 }
