 package ox.stackgame.stackmachine.exceptions;
 
 @SuppressWarnings("serial")
 public class InvalidAddressException extends StackRuntimeException {
     private final int address;
     
     public InvalidAddressException(int address, int offendingLine) {
 	super(offendingLine);
 	this.address = address;
     }
 
     public int getAddress() {
 	return address;
     }
 }
