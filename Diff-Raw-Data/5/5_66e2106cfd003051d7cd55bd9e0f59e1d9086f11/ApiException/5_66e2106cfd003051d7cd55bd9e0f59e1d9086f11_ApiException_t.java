 package org.dedeler.template.exception;
 
import java.util.Locale;

 import org.dedeler.template.context.MessageHelper;
 
 public class ApiException extends RuntimeException {
 
 	private static final long serialVersionUID = 1L;
 	private ErrorCode errorCode;
 	private String message;
 
 	public ApiException(ErrorCode errorCode) {
 		this.errorCode = errorCode;
		this.message = MessageHelper.getMessage(errorCode.toString(), Locale.ENGLISH);
 	}
 
 	public ErrorCode getErrorCode() {
 		return errorCode;
 	}
 
 	public void setErrorCode(ErrorCode errorCode) {
 		this.errorCode = errorCode;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public void setMessage(String message) {
 		this.message = message;
 	}
 
 }
