 package gov.nih.nci.ncicb.cadsr.loader.parser;
 import com.infomata.data.CSVFormat;
 import com.infomata.data.DataFile;
 import com.infomata.data.DataRow;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.domain.Property;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.ReviewTracker;
 import gov.nih.nci.ncicb.cadsr.loader.util.LookupUtil;
 import java.io.File;
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.List;
 
 public class CSVWriter implements ElementWriter
 {
   private DataFile write;
   private File report;
   private ReviewTracker reviewTracker = ReviewTracker.getInstance();
  
   public void setOutput(String url) 
   {
     report = new File(url);
   }
   
   public void write(ElementsLists elements) 
   {
     write = DataFile.createWriter("8859_2", true);
     write.setDataFormat(new CSVFormat());
     DataRow row = null;
     if((report!=null) && (report.exists()))
 		{
 		    report.delete();
 		}
     List ocs =elements.getElements(DomainObjectFactory.newObjectClass().getClass());
 
     try
       {
         write.open(report);
         row = write.next();
         row.add("UMLClass");
         row.add("UMLEntity");
         row.add("UMLDescription");
         row.add("ConceptName");
         row.add("ConceptPreferredName");
         row.add("Classification");
         row.add("ConceptCode");
         row.add("ConceptDefinition");
         row.add("ConceptDefinitionSource");
         row.add("ModifiedDate");
         row.add("HumanVerified");
         
         for(ObjectClass oc : (List<ObjectClass>) ocs) {
 
           String className = oc.getLongName();
           className = className.substring(className.lastIndexOf(".") + 1);
 
           String [] conceptCodes = oc.getPreferredName().split(":");
           for(int i = conceptCodes.length-1; i > -1; i--) {
          row = write.next();
          
          row.add(className);
          row.add(className);
          row.add(oc.getPreferredDefinition());
          Concept con = LookupUtil.lookupConcept(conceptCodes[i]);
          if(con != null) {
          row.add(con.getLongName());
          row.add(con.getLongName());
          row.add("ObjectClass");
          row.add(con.getPreferredName());
          row.add(con.getPreferredDefinition());
          row.add(con.getDefinitionSource());
          row.addEmpty();
          row.add(reviewTracker.get(oc.getLongName())?"1":"0");
          }
           }
         
           DataElementConcept o = DomainObjectFactory.newDataElementConcept();
           List<DataElementConcept> decs = (List<DataElementConcept>) elements.getElements(o.getClass());
 
           for(DataElementConcept dec : decs) {
             if(dec.getObjectClass().getLongName()
               .equals(oc.getLongName())) {
 
             String [] propConCodes = dec.getProperty().getPreferredName().split(":");
             for(int i = propConCodes.length-1; i > -1; i--)
             {
               row = write.next();
               row.add(className);
               row.add(dec.getProperty().getLongName());
               row.addEmpty();
               
               Concept con = LookupUtil.lookupConcept(propConCodes[i]);
               row.add(con.getLongName());
               row.add(con.getLongName());
               if(i == propConCodes.length-1)
                 row.add("Property");
               else
                 row.add("PropertyQualifier" + ((propConCodes.length-1) - i));
         
               row.add(con.getPreferredName());
               row.add(con.getPreferredDefinition());
               row.add(con.getDefinitionSource());
               row.addEmpty();
               row.add(reviewTracker.get(oc.getLongName() + "." + dec.getProperty().getLongName())?"1":"0");            
             }
            }
           }          
           
         }
       }
       catch (IOException e)
       {
         e.printStackTrace();
       }
       finally 
       {
         write.close();
       }
     }
   
 }
 
