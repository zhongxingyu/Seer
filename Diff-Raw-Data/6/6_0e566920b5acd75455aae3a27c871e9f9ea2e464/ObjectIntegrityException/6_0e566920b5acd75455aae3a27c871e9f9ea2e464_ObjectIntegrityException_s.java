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
        super(message);
     }
 
 }
