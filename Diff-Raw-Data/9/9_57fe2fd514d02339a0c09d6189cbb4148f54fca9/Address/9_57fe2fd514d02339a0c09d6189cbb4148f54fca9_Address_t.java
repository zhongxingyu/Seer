package panchat.addressing;
 
 import java.io.Serializable;
 import java.util.UUID;
 
 public class Address implements Serializable {
 	
 	private static final long serialVersionUID = 1L;
 	
 	UUID uuid;
 	String ip;
 }
