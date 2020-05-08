 package at.yawk.yxml;
 
import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Namespace used for resolving XML entities.
  */
 public class EntityNamespace implements Cloneable {
     /**
      * Default XML namespace as specified by the W3C XML conventions.
      */
    public static final EntityNamespace DEFAULT_XML_NAMESPACE = new EntityNamespace(Collections.<String, String> emptyMap());
     
     private final Map<String, String> entities;
     private boolean modifiable = true;
     
     static {
         DEFAULT_XML_NAMESPACE.putEntity("amp", "&");
         DEFAULT_XML_NAMESPACE.putEntity("lt", "<");
         DEFAULT_XML_NAMESPACE.putEntity("gt", ">");
         DEFAULT_XML_NAMESPACE.putEntity("apos", "\'");
         DEFAULT_XML_NAMESPACE.putEntity("quot", "\"");
         DEFAULT_XML_NAMESPACE.modifiable = false;
     }
     
     /**
      * Create new instance and fill it with the {@link #DEFAULT_XML_NAMESPACE}
      * values.
      */
     public EntityNamespace() {
         this(DEFAULT_XML_NAMESPACE);
     }
     
     /**
      * Create new instance and fill it with the given values.
      */
     public EntityNamespace(Map<String, String> entities) {
         this.entities = new HashMap<String, String>(entities);
     }
     
     /**
      * Copy the given {@link EntityNamespace}.
      */
     public EntityNamespace(EntityNamespace copy) {
         this(copy.entities);
     }
     
     /**
      * Return the entity with the given name from this namespace or
      * <code>null</code> if it is not found.
      */
     public String getEntity(String name) {
         return entities.get(name);
     }
     
     /**
      * Push an entity to this namespace.
      * 
      * @throws UnsupportedOperationException
      *             if this namespace is immutable (such as
      *             {@link #DEFAULT_XML_NAMESPACE}).
      */
     public void putEntity(String name, String value) {
         if (!modifiable) {
             throw new UnsupportedOperationException();
         }
         entities.put(name, value);
     }
     
     /**
      * Clone this namespace.
      * 
      * @see #EntityNamespace(EntityNamespace)
      */
     @Override
     public EntityNamespace clone() {
         return new EntityNamespace(this);
     }
 }
