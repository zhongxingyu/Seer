 package fedora.server.errors;
 
 /**
  * Signals that an object (serialized or deserialized) is inappropriately
  * formed in the context that it is being examined.
  *
  * @author cwilper@cs.cornell.edu
  */
 public class ObjectIntegrityException 
         extends StorageException {
 
     /**
      * Creates an ObjectIntegrityException.
      *
      * @param message An informative message explaining what happened and
      *                (possibly) how to fix it.
      */
     public ObjectIntegrityException(String message) {
        super(null, message, null, null, null);
    }

    public ObjectIntegrityException(String a, String message, String[] b, String[] c, Throwable th) {
        super(a, message, b, c, th);
     }
 
 }
