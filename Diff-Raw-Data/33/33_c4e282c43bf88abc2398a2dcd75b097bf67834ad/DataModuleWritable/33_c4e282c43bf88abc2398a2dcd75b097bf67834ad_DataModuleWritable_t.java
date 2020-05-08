 package uk.ac.ebi.age.model.writable;
 
 import java.util.Collection;
import java.util.Map;
 
 import uk.ac.ebi.age.AgeResolver;
 import uk.ac.ebi.age.model.DataModule;
 import uk.ac.ebi.age.model.ModuleKey;
 import uk.ac.ebi.age.model.SemanticModel;
 
import com.pri.util.Counter;

 public interface DataModuleWritable extends DataModule
 {
  void setId( String id );
  void setClusterId( String clstId );
  void setModuleKey(ModuleKey id);
 
  void setResolver(AgeResolver ageStorage);
  
  void addObject( AgeObjectWritable obj );
 
  @Override
  AgeObjectWritable getObject( String id );
  @Override
  Collection<AgeObjectWritable> getObjects();
  
  void setMasterModel(SemanticModel newModel);
  
  @Override
  Collection<? extends AgeExternalRelationWritable> getExternalRelations();
  @Override
  Collection<? extends AgeExternalObjectAttributeWritable> getExternalObjectAttributes();
 
  @Override
  Collection<? extends AttributedWritable> getAttributed( AttributedSelector sel );
 
 Map<ModuleKey,Counter> getObjectConnections();
  
  void registerExternalRelation( AgeExternalRelationWritable rel );
  
  void pack();
 }
