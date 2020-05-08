 package org.openmrs.module.flowsheet;
 
 import org.openmrs.Concept;
 import org.openmrs.ConceptDatatype;
 import org.openmrs.ConceptName;
 import org.openmrs.Obs;
 
 import javax.servlet.jsp.jstl.core.ConditionalTagSupport;
 import java.util.Locale;
 
 public class ConceptInfo {
     private Concept concept;
     private Integer obsId;
     public ConceptInfo(Obs obs) {
         if(obs != null){
             this.concept = obs.getConcept();
             this.obsId = obs.getObsId();
         }
         else{
             concept = new Concept();
         }
     }
 
     public String getName(){
 		ConceptName shortName = concept.getShortNameInLocale(Locale.ENGLISH);
 		return shortName == null ? concept.getName().getName() : shortName.getName();
     }
 
     public String getDesc() {
         if(concept.getDescription() != null){
             return concept.getDescription().getDescription();
         }
         return null;
     }
 
     public String getDataType() {
 		return concept.getDatatype().getName();
 	}
 
 	public String getClassType() {
 		return concept.getConceptClass().getName();
 	}
 	public Numeric getNumeric() {
         if (concept.isNumeric()) {
 			return new Numeric(concept);
 		}
 		return null;
 	}
 
     public String getImageId(){
        return ConceptDatatype.COMPLEX_UUID.equalsIgnoreCase(concept.getDatatype().getUuid())?String.valueOf(obsId):null;
     }
 }
