 package edu.umich.eecs.tac.props;
 
 import se.sics.isl.transport.Transportable;
 import se.sics.isl.transport.TransportReader;
 import se.sics.isl.transport.TransportWriter;
 
 import java.io.Serializable;
 import java.text.ParseException;
 
 /**
  * The query class represents a set of information the user is revealing during a search.  A query can contain
  * a manufacturer and a component, a manufacturer, a component, or neither.
  *
  * @author Patrick Jordan
  */
 public class Query implements Transportable, Serializable {
     private static final String MANUFACTURER_KEY = "manufacturer";
     private static final String COMPONENT_KEY = "component";
     private static final String QUERY_KEY = "query";
     private static final String LOCK_KEY = "lock";
 
     /**
      * The string representing the manufacturer.
      * May be <code>null</code>.
      */
     private String manufacturer;
     /**
      * The string representing the component.
      * May be <code>null</code>.
      */
     private String component;
 
     /**
      * The locked variable designates whether or not the query is mutable.
      */
     private boolean locked;
 
     /**
      * Make the query immutable.
      */
     public void lock() {
         locked = true;
     }
 
 
     /**
      * Check whether the query is immutable
      *
      * @return <code>true</code> if the query is locked, <code>false</code> otherwise.
      */
     private boolean isLocked() {
         return locked;
     }
 
     /**
      * Before writing an attribute value, lockCheck should be called.  This method will throw an illegal state
      * exception if a write is called on a locked query.
      *
      * @throws IllegalStateException
      */
     private void lockCheck() throws IllegalStateException {
         if (isLocked()) {
             throw new IllegalStateException("locked");
         }
     }
 
     /**
      * The string representing the manufacturer.
      *
      * @return the string representing the manufacturer.  May be <code>null</code>.
      */
     public String getManufacturer() {
         return manufacturer;
     }
 
     /**
      * Set the string representing the manufacturer.
      *
      * @param manufacturer the string representing the manufacturer.  May be <code>null</code>
      *                     if the manufacturer is not included in the query.
      */
     public void setManufacturer(String manufacturer) {
         lockCheck();
         this.manufacturer = manufacturer;
     }
 
     /**
      * The string representing the component.
      *
      * @return the string representing the component.  May be <code>null</code>.
      */
     public String getComponent() {
         return component;
     }
 
     /**
      * Set the string representing the component.
      *
      * @param component the string representing the component.  May be <code>null</code>
      *                  if the component is not included in the query.
      */
     public void setComponent(String component) {
         lockCheck();
         this.component = component;
     }
 
     public String getTransportName() {
         return QUERY_KEY;
     }
 
     public void read(TransportReader reader) throws ParseException {
        if (isLocked()) {
            throw new IllegalStateException("locked");
        }

         boolean lock = false;
 
         if (reader.nextNode(QUERY_KEY, false)) {
             lock = reader.getAttributeAsInt(LOCK_KEY, 0) > 0;
             this.setManufacturer(reader.getAttribute(MANUFACTURER_KEY,null));
             this.setComponent(reader.getAttribute(COMPONENT_KEY,null));
         }
 
         if (lock) {
             lock();
         }
     }
 
     public void write(TransportWriter writer) {
         writer.node(QUERY_KEY);
         writer.attr(LOCK_KEY, isLocked() ? 1 : 0);
 
         if(getManufacturer()!=null)
             writer.attr(MANUFACTURER_KEY, getManufacturer());
         if(getComponent()!=null)
             writer.attr(COMPONENT_KEY, getComponent());
         writer.endNode(QUERY_KEY);
     }
 
 
     public String toString() {
         return String.format("(%s (%s,%s))", QUERY_KEY, getManufacturer(), getComponent());
     }
 
 
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
 
         Query query = (Query) o;
 
         if (locked != query.locked) return false;
         if (component != null ? !component.equals(query.component) : query.component != null) return false;
         if (manufacturer != null ? !manufacturer.equals(query.manufacturer) : query.manufacturer != null) return false;
 
         return true;
     }
 
     public int hashCode() {
         int result;
         result = (manufacturer != null ? manufacturer.hashCode() : 0);
         result = 31 * result + (component != null ? component.hashCode() : 0);
         result = 31 * result + (locked ? 1 : 0);
         return result;
     }
 }
