 package uk.ac.ebi.age.ui.client.module;
 
 import java.util.List;
 
 import uk.ac.ebi.age.ui.shared.imprint.AttributeImprint;
 import uk.ac.ebi.age.ui.shared.imprint.AttributedImprint;
 import uk.ac.ebi.age.ui.shared.imprint.ObjectId;
 import uk.ac.ebi.age.ui.shared.imprint.ObjectImprint;
 import uk.ac.ebi.age.ui.shared.imprint.ObjectValue;
 import uk.ac.ebi.age.ui.shared.imprint.Value;
 
 import com.smartgwt.client.types.Overflow;
 import com.smartgwt.client.widgets.HTMLFlow;
 
 public class ObjectImprintViewPanelHTML extends HTMLFlow
 {
  private static int count=1;
  
  public enum RefType
  {
   OBJ,
   QUAL
  }
  
  private int depth;
  
  public ObjectImprintViewPanelHTML( final AttributedImprint impr, int depth, String clickTarget )
  {
   this.depth=depth;
   
  
   setOverflow(Overflow.VISIBLE);
   setWidth100();
   setPadding(2);
   
   setContents( representAttributed(impr, 0, "objectView", "", clickTarget) );
  }
  
  private String representValue(Value value, int lvl, String pathPfx, String clickTarget)
  {
   String str = "";
   
   List<AttributeImprint> quals = value.getAttributes();
   
   if( quals != null && quals.size() ==0 )
    quals = null;
   
   if( value instanceof ObjectValue )
   {
    ObjectValue ov = (ObjectValue)value;
    
    ObjectImprint obj = ov.getObjectImprint();
    
    String ref = pathPfx+","+RefType.OBJ.ordinal();
    
    if( ov.getObjectImprint() != null && lvl < depth && quals == null && obj.getAttributes() != null && obj.getAttributes().size() <= 5 )
    {
     str+="<table class='objectValue' style='width: 100%'><tr class='firstRow'><td class='firstCell' style='text-align: center'>";
     str+=ov.getTargetObjectClass().getName()+"</td>";
     str+="<td style='padding: 0'>"+representAttributed(ov.getObjectImprint(),lvl+1, "objectEmbedded", ref, clickTarget)+"</td></tr></table>";
    }
    else
    {
     str+="<div class='valueString'><a href='javascript:linkClicked(\""+clickTarget+"\",["+ref+"])'>"+ov.getTargetObjectClass().getName()+"</a>" +
             "<br>("+ov.getTargetObjectId().getObjectId()+")</div>";
    }
   }
   else
   {
    String val = value.getStringValue();
    
    if( val.length() > 10 && val.substring(0, 5).equalsIgnoreCase("http:") )
     val = "<a target='_blank' href='"+val+"'>"+val+"</a>";
    
    str+="<div class='valueString'>"+val+"</div>";
   }
   
   return str;
  }
  
  private String representAttributed( AttributedImprint ati, int lvl, String tblClass, String pathPfx, String clickTarget )
  {
   String str = "";
 
   str += "<table style='width: 100%' class='"+tblClass+"'>";
 
   int i = -1;
 
   for(AttributeImprint at : ati.getAttributes())
   {
    i++;
 
    if(i == 0)
     str += "<tr class='firstRow'>";
    else
     str += "<tr>";
 
    str += "<td class='firstCell attrName'>" + at.getClassImprint().getName() + ":&nbsp;</td>";
 
    
    if(at.getValueCount() == 1)
    {
     Value val = at.getValues().get(0);
     
     List<AttributeImprint> quals = val.getAttributes();
     
     if( quals != null && quals.size() == 0 )
      quals = null;
     
     if( quals != null )
      str+="<td>";
     else
      str+="<td colspan='2'>";
     
     String path = pathPfx;
     
     if( path.length() > 0 )
      path+=",";
     
     path += i+",0";
     
     str += representValue(val, lvl, path, clickTarget);
     
     str+="</td>";
     
     if( quals != null )
     {
      str+="<td style='padding: 0; width: 1%'>";
      
      String ref = path+","+RefType.QUAL.ordinal();
      
      if( lvl < depth )
       str+=representAttributed(val, lvl+1, "qualifiersEmbedded", ref, clickTarget);
      else
       str+="<a href='javascript:linkClicked(\""+clickTarget+"\",["+ref+"])'><img src='AGEVisual/icons/Q.png'></a>";
 
      str+="</td>";
     }
 
    }
    else
    {
    str += "<td colspan='2' style='padding: 0'><table class='valuesTable' style='padding: 0; width: 100%; margin: 0; border-collapse: collapse'>";
 
     int valn = -1;
     for(Value v : at.getValues())
     {
      valn++;
 
      if(valn == 0)
       str += "<tr class='firstRow'>";
      else
       str += "<tr>";
      
      List<AttributeImprint> quals = v.getAttributes();
     
      if( quals != null && quals.size() == 0 )
       quals = null;
      
      if( quals != null )
       str+="<td class='firstCell'>";
      else
       str+="<td class='firstCell' colspan='2'>";
 
      String path = pathPfx;
      
      if( path.length() > 0 )
       path+=",";
 
      path = i+","+valn+","+RefType.QUAL.ordinal();
      
      str += representValue(v, lvl, path, clickTarget);
      
      str+="</td>";
      
      if( quals != null )
      {
       str+="<td style='padding: 0'>";
       
       if( lvl < depth )
        str+=representAttributed(v, lvl+1, "qualifiersEmbedded", path, clickTarget);
       else
        str+="<a href='javascript:linkClicked(\""+clickTarget+"\",["+path+"])'><img src='AGEVisual/icons/Q.png'></a>";
 
       str+="</td>";
      }
 
      str+="</tr>";
     }
 
    str += "</table></td>";
    }
 
    str += "</tr>";
   }
  
   str += "</table>";
 
   return str;
  }
 
  private String makeRepresentationString(  ObjectId objectId, ObjectImprint obj, String theme )
  {
   String repstr = "<div style='float: left' class='briefObjectRepString'>";
   
   int cCount = 0;
   
   if( obj != null )
   {
    extloop: for( AttributeImprint attr : obj.getAttributes() )
    {
     String atName = attr.getClassImprint().getName();
     
     for( Value v : attr.getValues() )
     {
      if( cCount > 200 )
       break extloop;
      
      repstr += "<b>"+atName+"</b>"; 
      
      repstr += ": "+v.getStringValue()+"; ";
      
      cCount+=atName.length()+v.getStringValue().length();
     }
     
    }
   }
   else
    repstr=objectId.toString();
   
 
   repstr += "</div><div><a class='el' href='javascript:linkClicked(&quot;"+obj.getId()+"&quot;,&quot;"+theme+"&quot;)'>more</a></div>";
   
   return repstr;
  }
 }
