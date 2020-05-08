 package com.attask.descriptiondashboard;
 
 import org.apache.commons.lang.exception.ExceptionUtils;
 
import java.util.Collection;
 import java.util.Collections;
 
 /**
  * User: Joel Johnson
  * Date: 9/26/12
  * Time: 11:56 AM
  */
 public class ErrorTable extends Table {
 	private final String message;
 	private final Throwable throwable;
 
 	public static ErrorTable createErrorTable(String message, Throwable throwable) {
 		return new ErrorTable(message, throwable);
 	}
 
 	private ErrorTable(String message, Throwable throwable) {
		super(Collections.<Header>emptyList(), Collections.<Row>emptyList(), null, Collections.<String>emptySet());
 		this.message = message;
 		this.throwable = throwable;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 	public Throwable getThrowable() {
 		return throwable;
 	}
 
 	public String getStackTrace() {
 		return ExceptionUtils.getStackTrace(throwable);
 	}
 }
