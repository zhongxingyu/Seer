 package com.scholastic.sbam.shared.exceptions;
 
public class ServiceNotReadyException extends RuntimeException {
 	private static final long serialVersionUID = 7430665920747969378L;
 
 	public ServiceNotReadyException() {
 	  }
 
 	  public ServiceNotReadyException(String message) {
 	    super(message);
 	  }
 
 	  public ServiceNotReadyException(String message, Throwable cause) {
 	    super(message, cause);
 	  }
 
 	  public ServiceNotReadyException(Throwable cause) {
 	    super(cause);
 	  }
 
 }
