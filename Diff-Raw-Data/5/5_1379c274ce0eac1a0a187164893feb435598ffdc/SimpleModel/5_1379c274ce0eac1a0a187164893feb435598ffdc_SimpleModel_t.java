 package nl.astraeus.persistence;
 
 import java.io.Serializable;
 
 /**
  * SimpleModel
  * <p/>
  * User: rnentjes
  * Date: 7/16/11
  * Time: 1:56 PM
  */
 public abstract class SimpleModel implements Serializable, Cloneable, Comparable {
     public final static long serialVersionUID = 1L;
 
     private static volatile long nextId = 1;
 
     static {
         long time = System.currentTimeMillis();
 
         nextId = time*100000;
     }
 
     private long id;
 
     private boolean _prevayler_saved                = false;
     private long    _prevayler_last_update          = System.currentTimeMillis();
     private boolean _prevayler_selected_for_update  = false;
 
     public Long getNextId() {
         synchronized (SimpleModel.class) {
             return ++nextId;
         }
     }
 
     public SimpleModel() {
         this.id = getNextId();
     }
 
     public String getIdAsString() {
         return Long.toString(id);
     }
 
     public String getKey() {
         return getIdAsString();
     }
 
     public Long getId() {
         return id;
     }
 
     public String getGUID() {
         return this.getClass()+"-"+this.getIdAsString();
     }
 
     public String getDescription() {
         return toString();
     }
 
     public SimpleModel clone() throws CloneNotSupportedException {
         SimpleModel result = (SimpleModel)super.clone();
 
         // clone refs and lists
        //ReflectHelper.get().copyPrevaylerReferenceAndListProperties(this, result);
 
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         boolean result = true;
 
         if (obj == null || obj.getClass() != this.getClass()) {
             result = false;
         } else if (!((SimpleModel)obj).getId().equals(this.getId())) {
             result = false;
         }
 
         return result;
     }
 
     @Override
     public int hashCode() {
         return new Long(id).hashCode();
     }
 
     public int compareTo(Object o) {
         int result = -1;
         SimpleModel other = (SimpleModel)o;
 
         if (other != null) {
             result = (getId() - other.getId()) > 0 ? 1 : -1;
         }
 
         return result;
     }
 }
