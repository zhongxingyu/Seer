 package org.xbrlapi.aspects;
 
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
import org.xbrlapi.Concept;
 import org.xbrlapi.Fact;
 import org.xbrlapi.Fragment;
 import org.xbrlapi.impl.FactImpl;
 import org.xbrlapi.impl.InstanceImpl;
 import org.xbrlapi.utilities.XBRLException;
 
 /**
  * @author Geoff Shuetrim (geoff@galexy.net)
  */
 public class LocationAspect extends BaseAspect implements Aspect {
 
     public final static String TYPE = "location";
     
     /**
      * @see Aspect#getType()
      */
     public String getType() {
         return TYPE;
     }
     
 
     
     private final static Logger logger = Logger.getLogger(LocationAspect.class);
     
     /**
      * @param aspectModel The aspect model with this aspect.
      * @throws XBRLException.
      */
     public LocationAspect(AspectModel aspectModel) throws XBRLException {
         super(aspectModel);
         initialize();
     }
     
     protected void initialize() {
         this.setTransformer(new Transformer());
     }
     
 
 
     public class Transformer extends BaseAspectValueTransformer implements AspectValueTransformer {
 
         public Transformer() {
             super();
         }
 
         /**
          * @see AspectValueTransformer#validate(AspectValue)
          */
         public void validate(AspectValue value) throws XBRLException {
             super.validate(value);
             Fragment fragment = value.getFragment();
             if (! fragment.isa(FactImpl.class) && ! fragment.isa(InstanceImpl.class)) {
                 throw new XBRLException("Fragments for location aspects must be XBRL facts or XBRL instances.  In this case is it a " + fragment.getClass().getName() + ".");
             }
         }
         
         /**
          * @see AspectValueTransformer#getIdentifier(AspectValue)
          */
         public String getIdentifier(AspectValue value) throws XBRLException {
             validate(value);
             if (hasMapId(value)) {
                 return getMapId(value);
             }
            Concept f = ((Concept) value.getFragment());
            String id = f.getTargetNamespace() + ": " + f.getName();
             setMapId(value,id);
             return id;
         }
         
         /**
          * @see AspectValueTransformer#getLabel(AspectValue)
          */
         public String getLabel(AspectValue value) throws XBRLException {
             String id = getIdentifier(value);
             return id;
         }
         
 
         
 
 
 
 
 
 
 
         
     }
 
     /**
      * @see org.xbrlapi.aspects.Aspect#getValue(org.xbrlapi.Fact)
      */
     @SuppressWarnings("unchecked")
     public AspectValue getValue(Fact fact) throws XBRLException {
         return new LocationAspectValue(this,getFragment(fact));
     }
     
     /**
      * @see Aspect#getFragmentFromStore(Fact)
      */
     public Fragment getFragmentFromStore(Fact fact) throws XBRLException {
         return fact.getParent();
     }
     
     /**
      * @see Aspect#getKey(Fact)
      */
     public String getKey(Fact fact) throws XBRLException {
         return fact.getParentIndex();
     }
 
     /**
      * Handles object inflation.
      * @param in The input object stream used to access the object's serialization.
      * @throws IOException
      * @throws ClassNotFoundException
      */
     private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
         in.defaultReadObject( );
         initialize();
     }
     
     /**
      * Handles object serialization
      * @param out The input object stream used to store the serialization of the object.
      * @throws IOException
      */
     private void writeObject(java.io.ObjectOutputStream out) throws IOException {
         out.defaultWriteObject();
     }
  
     /**
      * @see java.lang.Object#hashCode()
      */
     @Override
     public int hashCode() {
         return super.hashCode();
     }
 
     /**
      * @see java.lang.Object#equals(java.lang.Object)
      */
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (!super.equals(obj))
             return false;
         if (getClass() != obj.getClass())
             return false;
        return true;
     }
     
 }
