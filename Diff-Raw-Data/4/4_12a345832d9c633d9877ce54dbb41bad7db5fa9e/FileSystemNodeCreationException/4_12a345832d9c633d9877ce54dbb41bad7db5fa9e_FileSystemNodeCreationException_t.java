 package bg.uni.sofia.fmi.xml.filesystem2html.model;
 
 /**
  * Exception thrown when incorrect situation happens during FileSystemNode
  * or one of its decendants are processing.
  *
  * @author Leni Kirilov
  */
 public class FileSystemNodeCreationException extends RuntimeException {
 
	private static final long serialVersionUID = -7553207804017159942L;

	public FileSystemNodeCreationException(String message) {
         this(message, null);
     }
 
     public FileSystemNodeCreationException(String message, Throwable t) {
         super(message, t);
     }
 }
