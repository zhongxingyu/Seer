 package es.udc.cartolab.gvsig.epanet.exceptions;
 
 @SuppressWarnings("serial")
 public class InvalidNetworkError extends Exception {
 
     public InvalidNetworkError(String msg) {
 	super(msg);
     }
 
     public InvalidNetworkError(Throwable cause) {
 	super(cause);
     }
 
 }
