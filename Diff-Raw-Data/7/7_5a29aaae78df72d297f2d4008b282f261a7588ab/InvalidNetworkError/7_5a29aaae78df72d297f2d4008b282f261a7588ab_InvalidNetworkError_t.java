 package es.udc.cartolab.gvsig.epanet.exceptions;
 
 @SuppressWarnings("serial")
 public class InvalidNetworkError extends Exception {
 
    private int id;

    public InvalidNetworkError(int id, String msg) {
	super(msg);
	this.id = id;
    }

     public InvalidNetworkError(String msg) {
 	super(msg);
     }
 
     public InvalidNetworkError(Throwable cause) {
 	super(cause);
     }
 
 }
