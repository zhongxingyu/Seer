 package gov.nih.nci.caintegrator2.domain.annotation;
 
 import gov.nih.nci.caintegrator2.domain.AbstractCaIntegrator2Object;
 
 /**
  * 
  */
 public class PermissibleValue extends AbstractCaIntegrator2Object implements Comparable<PermissibleValue> {
 
     private static final long serialVersionUID = 1L;
     
     private String value;
     private String valueMeaning;
     private String valueMeaningId;
     private String valueMeaningVersion;
     
     private static final int MAX_LENGTH = 255;
     
     /**
      * @return the value
      */
     public String getValue() {
         return value;
     }
     
     /**
      * @param value the value to set
      */
     public void setValue(String value) {
         this.value = value;
     }
     
     /**
      * @return the valueMeaning
      */
     public String getValueMeaning() {
         return valueMeaning;
     }
     
     /**
      * @param valueMeaning the valueMeaning to set
      */
     public void setValueMeaning(String valueMeaning) {
        this.valueMeaning = (valueMeaning == null || valueMeaning.length() < MAX_LENGTH)
            ? valueMeaning : valueMeaning.substring(0, MAX_LENGTH - 3) + "...";
     }
     
     /**
      * @return the valueMeaningId
      */
     public String getValueMeaningId() {
         return valueMeaningId;
     }
     
     /**
      * @param valueMeaningId the valueMeaningId to set
      */
     public void setValueMeaningId(String valueMeaningId) {
         this.valueMeaningId = valueMeaningId;
     }
     
     /**
      * @return the valueMeaningVersion
      */
     public String getValueMeaningVersion() {
         return valueMeaningVersion;
     }
     
     /**
      * @param valueMeaningVersion the valueMeaningVersion to set
      */
     public void setValueMeaningVersion(String valueMeaningVersion) {
         this.valueMeaningVersion = valueMeaningVersion;
     }
     
     /**
      * {@inheritDoc}
      */
     @Override
     public String toString() {
         return value;
     }
     
     /**
      * Compare based on the Value.
      * {@inheritDoc}
      */
     public int compareTo(PermissibleValue o) {
         return getValue().compareTo(o.getValue());
     }
 }
