 package gov.nih.nci.ncicb.cadsr.loader.validator;
 import gov.nih.nci.ncicb.cadsr.domain.DataElement;
 import gov.nih.nci.ncicb.cadsr.domain.DataElementConcept;
 import gov.nih.nci.ncicb.cadsr.domain.DomainObjectFactory;
 import gov.nih.nci.ncicb.cadsr.domain.ObjectClass;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;
import gov.nih.nci.ncicb.cadsr.loader.util.*;
 import java.util.*;
 
 public class DuplicateValidator implements Validator 
 {
   private ElementsLists elements = ElementsLists.getInstance();
   
   private ValidationItems items = ValidationItems.getInstance();
   
   public DuplicateValidator()
   {
   }
   
   public void addProgressListener(ProgressListener l) {
 
   }
   
   public ValidationItems validate() {
     List<ObjectClass> ocs = (List<ObjectClass>)elements.getElements(DomainObjectFactory.newObjectClass().getClass());
     Map<String, ObjectClass> listed = new HashMap<String, ObjectClass>();
     Map<String, ObjectClass> prefNameList = new HashMap<String, ObjectClass>();
     Map<String, ObjectClass> deListed = new HashMap<String, ObjectClass>();
     
     if(ocs != null) {
       for(ObjectClass oc : ocs) {  
         if(oc.getPublicId() != null) {
         if(listed.containsKey(oc.getPublicId()))
           items.addItem(new ValidationError
                         (PropertyAccessor.getProperty
                           ("class.same.mapping", oc.getLongName(),(listed.get(oc.getPublicId())).getLongName()),oc));
         else
           listed.put(oc.getPublicId(), oc);
         }
        else if(!StringUtil.isEmpty(oc.getPreferredName())) {
           if(prefNameList.containsKey(oc.getPreferredName()))
             items.addItem(new ValidationError
                           (PropertyAccessor.getProperty
                             ("class.same.mapping", oc.getLongName(),(prefNameList.get(oc.getPreferredName())).getLongName()),oc));
           else
             prefNameList.put(oc.getPreferredName(), oc);
         }
       } 
     }
     
     
     List<DataElement> des = (List<DataElement>)elements.getElements(DomainObjectFactory.newDataElement().getClass());
     if(des != null && ocs != null) {
       for(ObjectClass oc : ocs) {
         Map<String, DataElement> deList = new HashMap<String, DataElement>();
         for(DataElement de : des) {
           if(de.getDataElementConcept().getObjectClass() == oc)
             if(deList.containsKey(de.getDataElementConcept().getProperty().getPreferredName()))
               items.addItem(new ValidationError
                             (PropertyAccessor.getProperty
                               ("de.same.mapping", de.getLongName(),
                                 (deList.get(de.getDataElementConcept().getProperty().getPreferredName())).getDataElementConcept().getLongName()),de));
             else
               deList.put(de.getDataElementConcept().getProperty().getPreferredName(), de);
         }
       }
     }
 
     return items;
   }
 }
