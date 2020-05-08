 package org.xbrlapi.aspects.alt;
 
 import java.net.URI;
 import java.util.List;
 import java.util.Set;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 import org.xbrlapi.Fact;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.Tuple;
 import org.xbrlapi.XML;
 import org.xbrlapi.data.Store;
 import org.xbrlapi.impl.InstanceImpl;
 import org.xbrlapi.impl.TupleImpl;
 import org.xbrlapi.utilities.XBRLException;
 
public class LocationDomain extends DomainImpl implements Domain, StoreHandler {
 
    
     /**
      * 
      */
     private static final long serialVersionUID = 6991871946096405025L;
     
     protected final static Logger logger = Logger.getLogger(LocationDomain.class);
 
     public LocationDomain(Store store) throws XBRLException {
         super(store);
     }
 
     /**
      * @see Domain#getAspectId()
      */
     public URI getAspectId() { return LocationAspect.ID; }
     
     /**
      * @see Domain#getAllAspectValues()
      */
     public List<AspectValue> getAllAspectValues() throws XBRLException {
         Set<String> factIndices = getStore().queryForIndices("for $root in #roots#[@fact] return $root");
         List<AspectValue> values = new Vector<AspectValue>();
         for (String factIndex: factIndices) {
             AspectValue value = new LocationAspectValue(factIndex);
             values.add(value);
         }
         return values;
     }
 
     /**
      * @see Domain#getChildren(AspectValue)
      */
     public List<AspectValue> getChildren(AspectValue parent)
             throws XBRLException {
         List<AspectValue> result = new Vector<AspectValue>();
         LocationAspectValue aspectValue = (LocationAspectValue) parent;
         Fact fact = getStore().<Fact> getXMLResource(aspectValue.getFactIndex());
         if (fact.isa(TupleImpl.class)) {
             List<Fact> children = ((Tuple) fact).getChildFacts();
             for (Fact child: children) {
                 result.add(new LocationAspectValue(fact.getIndex(), child.getIndex()));
             }
         }
         return result;
     }
 
     /**
      * @see Domain#getDepth(AspectValue)
      */
     public int getDepth(AspectValue aspectValue) throws XBRLException {
         if (! hasParent(aspectValue)) return 0;
         int depth = 0;
         Fragment parent = getStore().<Fragment> getXMLResource(((LocationAspectValue) aspectValue).getParentFactIndex());
         while (! (parent instanceof InstanceImpl)) {
             depth++;
             parent = parent.getParent();
         }
         return depth;
     }
 
     /**
      * @see Domain#getParent(AspectValue)
      */
     public LocationAspectValue getParent(AspectValue child)
             throws XBRLException {
         if (! hasParent(child)) throw new XBRLException("The aspect value " + child.getId() + " does not have a parent aspect value.");
         Fact parent = getStore().<Fact> getXMLResource(((LocationAspectValue)child).getParentFactIndex());
         Fragment grandparent = parent.getParent();
         if (grandparent instanceof InstanceImpl) return new LocationAspectValue(parent.getIndex());
         return new LocationAspectValue(grandparent.getIndex(), parent.getIndex());
     }
 
     /**
      * @see Domain#getSize()
      */
     public long getSize() throws XBRLException {
         Set<String> factIndices = getStore().queryForIndices("for $root in #roots#[@fact] return $root");
         return factIndices.size();
     }
 
     /**
      * @see Domain#hasChildren(AspectValue)
      */
     public boolean hasChildren(AspectValue value)
             throws XBRLException {
         Fact fact = getStore().<Fact> getXMLResource(((LocationAspectValue)value).getFactIndex());
         if (! fact.isa(TupleImpl.class)) return false;
         List<Fact> children = ((Tuple) fact).getChildFacts();
         return (children.size() > 0);
     }
 
     /**
      * @see Domain#hasParent(AspectValue)
      */
     public boolean hasParent(AspectValue child) throws XBRLException {
         return (! ((LocationAspectValue)child).isRootLocation());
     }
 
     /**
      * @see Domain#isInDomain(AspectValue)
      */
     public boolean isInDomain(AspectValue candidate) {
 
         if (! (candidate instanceof LocationAspectValue)) return false;
 
         XML resource = null;
         try {
             resource = getStore().<XML>getXMLResource(((LocationAspectValue)candidate).getFactIndex());
             String factAttribute = resource.getMetaAttribute("fact");
             if (factAttribute == null) return false;
             return true;
         } catch (XBRLException e) {
             return false;
         }
 
     }
 
     /**
      * @see Domain#isFinite()
      */
     public boolean isFinite() {
         return true;
     }
 
     /**
      * Returns false.
      * @see Domain#allowsMissingValues()
      */
     public boolean allowsMissingValues() {
         return false;
     }
 
     /**
      * @param first
      *            The first aspect value
      * @param second
      *            The second aspect value
      * @return -1 if the first aspect value is less than the second, 0 if they
      *         are equal and 1 if the first aspect value is greater than the
      *         second. Any aspect values that are not in this domain
      *         are placed last in the aspect value ordering.
      *         Otherwise, the comparison is based upon the natural ordering of
      *         the aspect value IDs, treating the document part of the identifier
      *         as a string and the fragment number within the document as an integer.
      *         Missing values are ranked last among aspect values of the same type.
      */
     public int compare(AspectValue first, AspectValue second) {
         if (! (first instanceof LocationAspectValue)) {
             logger.error("Aspect values of the wrong type are being compared.");
             return 1;
         }
         if (! (second instanceof LocationAspectValue)) {
             logger.error("Aspect values of the wrong type are being compared.");
             return -1;
         }
 
 
         if (first.isMissing()) {
             if (second.isMissing()) return 0;
             return 1;
         }
         if (second.isMissing()) return -1;
 
         String[] fParts = first.getId().split("_"); 
         String[] sParts = first.getId().split("_"); 
         
         int result = fParts[0].compareTo(sParts[0]);
         if (result != 0) return result;
         return (new Integer(fParts[1])).compareTo(new Integer(sParts[1]));
     }    
 
     /**
      * @see Domain#isRoot(AspectValue)
      */
     public boolean isRoot(AspectValue aspectValue) throws XBRLException {
         return ((LocationAspectValue) aspectValue).isRootLocation();
     }
 
 }
