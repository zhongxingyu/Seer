 package uk.ac.ebi.esd.server.service;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import uk.ac.ebi.age.model.AgeAttribute;
 import uk.ac.ebi.age.model.AgeAttributeClass;
 import uk.ac.ebi.age.model.AgeClass;
 import uk.ac.ebi.age.model.AgeObject;
 import uk.ac.ebi.age.model.AgeRelation;
 import uk.ac.ebi.age.model.AgeRelationClass;
 import uk.ac.ebi.age.query.AgeQuery;
 import uk.ac.ebi.age.query.ClassNameExpression;
 import uk.ac.ebi.age.query.OrExpression;
 import uk.ac.ebi.age.query.ClassNameExpression.ClassType;
 import uk.ac.ebi.age.storage.AgeStorage;
 import uk.ac.ebi.age.storage.index.AgeIndex;
 import uk.ac.ebi.age.storage.index.TextFieldExtractor;
 import uk.ac.ebi.age.storage.index.TextValueExtractor;
 import uk.ac.ebi.esd.client.QueryService;
 import uk.ac.ebi.esd.client.query.SampleGroupReport;
 
 import com.google.gwt.user.server.rpc.RemoteServiceServlet;
 
 public class ESDServiceImpl extends RemoteServiceServlet implements QueryService
 {
  private AgeStorage storage;
  
  private AgeIndex attrTextIndex;
  
  private AgeClass sampleClass;
  private AgeClass groupClass;
  private AgeRelationClass sampleInGroupRelClass;
  
  private AgeAttributeClass desciptionAttributeClass;
  
  public ESDServiceImpl( AgeStorage stor )
  {
   storage=stor;
   
  sampleClass = storage.getSemanticModel().getAgeClass( ESDConfigManager.SAMPLE_CLASS_NAME );
  groupClass = storage.getSemanticModel().getAgeClass( ESDConfigManager.SAMPLEGROUP_CLASS_NAME );
  sampleInGroupRelClass = storage.getSemanticModel().getAgeRelationClass( ESDConfigManager.SAMPLEINGROUP_REL_CLASS_NAME );
   
  desciptionAttributeClass = storage.getSemanticModel().getAgeAttributeClass( ESDConfigManager.DESCRIPTION_ATTR_CLASS_NAME );
   
   OrExpression orExp = new OrExpression();
   
   ClassNameExpression clsExp = new ClassNameExpression();
   clsExp.setClassName( ESDConfigManager.SAMPLE_CLASS_NAME );
   clsExp.setClassType( ClassType.DEFINED );
   
   orExp.addExpression(clsExp);
   
   clsExp = new ClassNameExpression();
   clsExp.setClassName( ESDConfigManager.SAMPLEGROUP_CLASS_NAME );
   clsExp.setClassType( ClassType.DEFINED );
 
   orExp.addExpression(clsExp);
 
   
   AgeQuery q = AgeQuery.create(orExp);
   
   ArrayList<TextFieldExtractor> extr = new ArrayList<TextFieldExtractor>(2);
   
   extr.add( new TextFieldExtractor(ESDConfigManager.NAME_FIELD_NAME, new AttrNamesExtractor() ) );
   extr.add( new TextFieldExtractor(ESDConfigManager.VALUE_FIELD_NAME, new AttrValuesExtractor() ) );
   
   attrTextIndex = storage.createTextIndex(q, extr);
  }
  
  
  public String greetServer(String input)
  {
   String serverInfo = getServletContext().getServerInfo();
   String userAgent = getThreadLocalRequest().getHeader("User-Agent");
   return "Hello, " + input + "!<br><br>I am running " + serverInfo + ".<br><br>It looks like you are using:<br>" + userAgent;
  }
 
 
 
  public List<SampleGroupReport> selectSampleGroups(String value, boolean searchGrp, boolean searchAttrNm, boolean searchAttrVl)
  {
   StringBuilder sb = new StringBuilder();
   
   if( searchAttrNm )
    sb.append(ESDConfigManager.NAME_FIELD_NAME).append(":(").append(value).append(')');
 
   if( searchAttrVl )
   {
    if( sb.length() > 0 )
     sb.append(" OR ");
    
    sb.append(ESDConfigManager.VALUE_FIELD_NAME).append(":(").append(value).append(')');
   }
   
   List<AgeObject> sel = storage.queryTextIndex(attrTextIndex, sb.toString() );
   
   List<SampleGroupReport> res = new ArrayList<SampleGroupReport>();
   
   Map<AgeObject,SampleGroupReport> repMap = new HashMap<AgeObject, SampleGroupReport>();
   
   for( AgeObject obj : sel )
   {
    if( obj.getAgeElClass() == sampleClass )
    {
     AgeObject grpObj = getGroupForSample(obj);
     
     if( grpObj == null )
      continue;
     
     SampleGroupReport sgRep = repMap.get(grpObj);
     
     if( sgRep == null )
     {
      sgRep = new SampleGroupReport();
 
      sgRep.setId( grpObj.getId() );
      sgRep.setDescription( grpObj.getAttribute(desciptionAttributeClass).toString() );
      
      repMap.put(grpObj, sgRep);
     }
 
     sgRep.addMatchedSample( obj.getId() );
    }
   }
   
   
   return res;
  }
  
  private AgeObject getGroupForSample(AgeObject obj)
  {
   for(AgeRelation rel : obj.getRelations() )
   {
    if( rel.getRelationClass() == sampleInGroupRelClass && rel.getTargetObject().getAgeElClass() == groupClass )
     return rel.getTargetObject();
   }
   
   return null;
  }
 
 
  public void shutdown()
  {
  }
  
  class AttrValuesExtractor implements TextValueExtractor
  {
   StringBuilder sb = new StringBuilder();
  
   public String getValue(AgeObject obj)
   {
    sb.setLength(0);
    
    for( AgeAttribute attr : obj.getAttributes() )
    {
     Object val = attr.getValue();
     
     if( val instanceof String )
      sb.append( val ).append(' ');
    }
     
    return sb.toString();
   }
  }
  
  class AttrNamesExtractor implements TextValueExtractor
  {
   StringBuilder sb = new StringBuilder();
  
   public String getValue(AgeObject obj)
   {
    sb.setLength(0);
    
    for( AgeAttribute attr : obj.getAttributes() )
    {
     sb.append( attr.getAgeElClass().getName() ).append(' ');
    }
     
    return sb.toString();
   }
  }
 
 
 }
