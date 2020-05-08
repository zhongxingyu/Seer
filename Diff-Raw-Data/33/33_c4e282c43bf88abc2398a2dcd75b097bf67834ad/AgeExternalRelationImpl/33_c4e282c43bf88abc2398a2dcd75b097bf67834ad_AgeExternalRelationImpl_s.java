 package uk.ac.ebi.age.model.impl.v1;
 
 import java.io.Serializable;
 import java.lang.ref.Reference;
 
 import uk.ac.ebi.age.model.AgeRelation;
 import uk.ac.ebi.age.model.AgeRelationClass;
 import uk.ac.ebi.age.model.AttributedClass;
 import uk.ac.ebi.age.model.ContextSemanticModel;
 import uk.ac.ebi.age.model.DataModule;
 import uk.ac.ebi.age.model.RelationClassRef;
 import uk.ac.ebi.age.model.ResolveScope;
 import uk.ac.ebi.age.model.writable.AgeExternalRelationWritable;
 import uk.ac.ebi.age.model.writable.AgeObjectWritable;
 import uk.ac.ebi.age.model.writable.AgeRelationWritable;
 import uk.ac.ebi.age.util.ReferenceFactory;
 
 public class AgeExternalRelationImpl extends AttributedObject implements AgeExternalRelationWritable, Serializable
 {
  private static final long serialVersionUID = 1L;
 
  private final RelationClassRef relClassRef; 
  private String objId;
  private AgeObjectWritable sourceObject;
  private transient Reference<AgeExternalRelationWritable> invRelation;
  private transient Reference<AgeObjectWritable> target;
  private boolean infered;
  private ResolveScope tgtScope;
 
  protected AgeExternalRelationImpl(RelationClassRef cRef, AgeObjectWritable srcOb, String id, ResolveScope scp)
  {
   relClassRef=cRef;
 
   objId=id;
   sourceObject=srcOb;
   tgtScope=scp;
  }
 
  @Override
  public AgeRelationClass getAgeElClass()
  {
   AgeRelationClass cls = relClassRef.getAgeElClass();
   
   if( cls == null )
    if( ! relClassRef.getPlug().plug( getSourceObject().getDataModule().getResolver() ) )
     return null;
   
   return relClassRef.getAgeElClass();
  }
  
  @Override
  public RelationClassRef getClassReference()
  {
   return relClassRef;
  }
 
  @Override
  public AgeObjectWritable getTargetObject()
  {
   AgeObjectWritable tgt = null;
   
   if( target != null )
   {
    tgt = target.get();
    
    if( tgt != null )
     return tgt;
   }
   
   DataModule dm = getSourceObject().getDataModule();
 
   if( tgtScope == ResolveScope.CLUSTER || tgtScope == ResolveScope.CASCADE_CLUSTER )
    tgt = dm.getResolver().getClusterScopeObject(objId, dm.getClusterId());
   
   if( tgt == null && ( tgtScope == ResolveScope.CASCADE_CLUSTER || tgtScope == ResolveScope.GLOBAL ) )
    tgt = dm.getResolver().getGlobalScopeObject(objId);
  
   return tgt;
  }
  
  @Override
  public AgeObjectWritable getSourceObject()
  {
   return sourceObject;
  }
 
  
  @Override
  public String getTargetObjectId()
  {
   return objId;
  }
 
  @Override
  public int getOrder()
  {
   return relClassRef.getOrder();
  }
 
 
  @Override
  public void setTargetObject(AgeObjectWritable obj)
  {
   objId=obj.getId();
   target = ReferenceFactory.getReference(obj);
  }
 
 
  @Override
  public void setInferred(boolean inf)
  {
   infered=inf;
  }
 
  @Override
  public boolean isInferred()
  {
   return infered;
  }
  
 
  @Override
  public AttributedClass getAttributedClass()
  {
   return getAgeElClass();
  }
  
  @Override
  public AgeRelationWritable createClone( AgeObjectWritable src )
  {
   AgeExternalRelationImpl clone = new AgeExternalRelationImpl(relClassRef, src, getTargetObjectId(), tgtScope);
   clone.infered = infered;
   
   cloneAttributes(clone);
   
   return clone;
  }
 
  @Override
  public AgeExternalRelationWritable getInverseRelation()
  {
   AgeExternalRelationWritable rel = null;
   
   if( invRelation != null )
   {
    rel = invRelation.get();
    
    if( rel != null)
     return rel;
   }
   
   for( AgeRelation r : getTargetObject().getRelations()  )
   {
    if( r.getAgeElClass().getInverseRelationClass() == getAgeElClass() && r.getTargetObject() == getSourceObject() )
    {
     rel = (AgeExternalRelationWritable)r;
     break;
    }
   }
   
   if( rel == null )
    return null;
   
   invRelation = ReferenceFactory.getReference(rel);
   
   return rel;
  }
 
  @Override
  public void setInverseRelation(AgeRelationWritable invRl)
  {
   throw new UnsupportedOperationException();
  }
 
  @Override
  public void setInverseRelation(AgeExternalRelationWritable invr)
  {
   invRelation= ReferenceFactory.getReference(invr);
  }
 
  @Override
  public void setSourceObject(AgeObjectWritable ageObject)
  {
   sourceObject=ageObject;
  }
 
  @Override
  public ContextSemanticModel getSemanticModel()
  {
   return sourceObject.getSemanticModel();
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
 }
 
