 package org.xbrlapi.aspects.alt;
 
 import java.net.URI;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.utilities.XBRLException;
 
 public class LocationAspectValue extends AspectValueImpl implements AspectValue {
 
     /**
      * 
      */
     private static final long serialVersionUID = -9147415332504512016L;
 
     protected final static Logger logger = Logger.getLogger(LocationAspectValue.class);
 
     /**
      * The index of the fact.
      */
     private String factIndex = null;
     
     /** 
      * The index of the parent fact - null for facts
      * that are children of the containing XBRL instance.
      */
     private String parentFactIndex = null;
 
     /**
      * Missing aspect value constructor
      */
     public LocationAspectValue() {
         super();
     }
     
     /**
      * @param index The index of the fact.
      * @throws XBRLException if the parameter is null.
      */
     public LocationAspectValue(String index) throws XBRLException {
         super();
         if (index == null) throw new XBRLException("The fact index must not be null.");
         factIndex = index;
     }
     
     /**
      * @param parentFactIndex String equal to the index of the parent fact.
      * Only use this constructor for facts that are children of tuple facts.
      * @param index The index of the fact.
      * @throws XBRLException if a parameter is null.
      */
     public LocationAspectValue(String parentFactIndex, String index) throws XBRLException {
         this(index);
         if (parentFactIndex == null) throw new XBRLException("The parent fact index must not be null.");
         factIndex = index;
         this.parentFactIndex = parentFactIndex;
     }    
 
     /**
      * @see AspectValue#getId()
      */
     public String getId() {
        if (this.isMissing()) return "missing";
         if (this.isRootLocation()) return "report";
         return factIndex;
     }
 
     /**
      * @see AspectHandler#getAspectId()
      */
     public URI getAspectId() {
         return LocationAspect.ID;
     }
     
     /**
      * There is no such thing as a missing location aspect value.
      * @see AspectValue#isMissing()
      */
     public boolean isMissing() {
        return (getFactIndex() == null);
     }
     
     /**
      * @return the index of the fact fragment that this is a location for.
      */
     public String getFactIndex() {
         return this.factIndex;
     }
     
     /**
      * @return true if this location is for a fact that is a child of an 
      * XBRL instance and false if this location is for a fact that is a child
      * of a tuple fact.
      */
     public boolean isRootLocation() {
         return (parentFactIndex == null);
     }
     
     /**
      * @return the index of the parent tuple fact for facts that are
      * children of tuples.
      * @throws XBRLException if the fact is a child of an XBRL instance
      * rather than a tuple fact.
      */
     public String getParentFactIndex() throws XBRLException {
         if (parentFactIndex == null) throw new XBRLException("This is the location of a root fact and has no parent tuple");
         return parentFactIndex;
     }
     
 }
