 package uk.ac.ebi.age.model.impl.v1;
 
 import java.io.Serializable;
 import java.lang.ref.Reference;
 
 import uk.ac.ebi.age.model.AgeAttribute;
 import uk.ac.ebi.age.model.AgeObject;
 import uk.ac.ebi.age.model.AgeObjectAttribute;
 import uk.ac.ebi.age.model.AttributeClassRef;
 import uk.ac.ebi.age.model.AttributedClass;
 import uk.ac.ebi.age.model.DataModule;
 import uk.ac.ebi.age.model.FormatException;
 import uk.ac.ebi.age.model.ResolveScope;
 import uk.ac.ebi.age.model.writable.AgeExternalObjectAttributeWritable;
 import uk.ac.ebi.age.model.writable.AgeObjectWritable;
 import uk.ac.ebi.age.model.writable.AttributedWritable;
 import uk.ac.ebi.age.util.ReferenceFactory;
 
 public class AgeExternalObjectAttributeImpl extends AgeAttributeImpl implements AgeExternalObjectAttributeWritable, Serializable
 {
  private static final long serialVersionUID = 4L;
 
  private String objId;
  private transient Reference<AgeObjectWritable> target;
  private ResolveScope tgtScope;
 
  
  protected AgeExternalObjectAttributeImpl(AttributeClassRef relClass, String id,  AttributedWritable host, ResolveScope scp) 
  {
   super(relClass, host);
   
   objId=id;
   tgtScope = scp;
  }
 
 
  @Override
  public AgeObjectWritable getValue()
  {
   AgeObjectWritable tgt = null;
   
   if( target != null )
   {
    tgt = target.get();
    
    if( tgt != null )
     return tgt;
   }
   
   DataModule dm = getMasterObject().getDataModule();
 
   if( tgtScope == ResolveScope.CLUSTER || tgtScope == ResolveScope.CASCADE_CLUSTER )
    tgt = dm.getResolver().getClusterScopeObject(objId, dm.getClusterId());
   
   if( tgt == null && ( tgtScope == ResolveScope.CASCADE_CLUSTER || tgtScope == ResolveScope.GLOBAL ) )
    tgt = dm.getResolver().getGlobalScopeObject(objId);
   
  if( tgt != null )
   target = ReferenceFactory.getReference(tgt);
  
   return tgt;
  }
 
 
  @Override
  public String getTargetObjectId()
  {
   return objId;
  }
 
 
  @Override
  public AttributedClass getAttributedClass()
  {
   return getAgeElClass();
  }
 
  @Override
  public boolean getValueAsBoolean()
  {
   return false;
  }
 
  @Override
  public int getValueAsInteger()
  {
   return 0;
  }
 
  @Override
  public double getValueAsDouble()
  {
   return 0;
  }
 
  @Override
  public void updateValue(String value) throws FormatException
  {
   objId=value;
  }
 
  @Override
  public void finalizeValue()
  {
  }
 
 
  @Override
  public void setTargetObject(AgeObjectWritable obj)
  {
   target=ReferenceFactory.getReference(obj);
   objId=(obj).getId();
  }
 
 
  
  @Override
  public void setValue(Object val)
  {
   if( val instanceof AgeObject )
   { 
    target=ReferenceFactory.getReference((AgeObjectWritable)val);
    objId=((AgeObjectWritable)val).getId();
   }
  }
 
  @Override
  public void setBooleanValue(boolean boolValue)
  {
   throw new UnsupportedOperationException();
  }
 
  @Override
  public void setIntValue(int intValue)
  {
   throw new UnsupportedOperationException();
  }
 
  @Override
  public void setDoubleValue(double doubleValue)
  {
   throw new UnsupportedOperationException();
  }
 
  
  
  @Override
  public AgeExternalObjectAttributeWritable createClone( AttributedWritable host )
  {
   AgeExternalObjectAttributeImpl clone  = new AgeExternalObjectAttributeImpl(getClassReference(), objId, host, tgtScope);
   clone.target=this.target;
   
   cloneAttributes( clone );
 
   return clone;
  }
  
  @Override
  public boolean equals( Object ob )
  {
   if( ! (ob instanceof AgeObjectAttribute) )
    return false;
   
    return objId.equals( ((AgeObjectAttribute)ob).getValue().getId() );
  }
 
 
  @Override
  public int hashCode()
  {
   return objId.hashCode();
  }
 
  
  @Override
  public int compareTo( AgeAttribute ob )
  {
   if( ! (ob instanceof AgeObjectAttribute) )
    return 1;
 
   
   return objId.compareTo( ((AgeObjectAttribute)ob).getValue().getId() );
  }
 
 
  @Override
  public ResolveScope getTargetResolveScope()
  {
   return tgtScope;
  }
 
 
  @Override
  public void setTargetResolveScope(ResolveScope scp)
  {
   tgtScope = scp;
  }
 
 @Override
 public synchronized void reset()
 {
  super.reset();
  
  target = null;
 }
 
 }
 
