 package uk.ac.ebi.age.model.impl;
 
 import java.io.Serializable;
 
 import uk.ac.ebi.age.model.AgeRelationClass;
 import uk.ac.ebi.age.model.AgeRelationClassPlug;
 import uk.ac.ebi.age.model.SemanticModel;
 import uk.ac.ebi.age.util.Plug;
 
 public class AgeRelationClassPlugPluggable implements Serializable, Plug, AgeRelationClassPlug
 {
  private String className;
  private transient AgeRelationClass ageRelationClass;
  private SemanticModel model;
  
  public AgeRelationClassPlugPluggable(AgeRelationClass relClass, SemanticModel mod)
  {
   ageRelationClass=relClass;
   className = relClass.getName();
   model=mod;
  }
 
  public void unplug()
  {
   ageRelationClass = null;
  }
  
  public boolean plug()
  {
   ageRelationClass = model.getDefinedAgeRelationClass(className);
   
   if( ageRelationClass != null )
    return true;
   
   return false;
  }
  
  public AgeRelationClass getAgeRelationClass()
  {
   if( ageRelationClass == null )
    plug();
   
   return ageRelationClass;
  }
 }
