 package de.aidger.view.models;
 
 import de.aidger.model.models.Assistant;
 import de.aidger.view.tabs.ViewerTab.DataType;
 import de.unistuttgart.iste.se.adohive.model.IAssistant;
 
 /**
  * The UI assistant is used for prettier rendering of the model.
  * 
  * @author aidGer Team
  */
 public class UIAssistant extends Assistant implements UIModel,
         Comparable<UIAssistant> {
 
     /**
      * The total hours the assistant is employed for a course.
      */
     private double totalHours = 0.0;
 
     /**
      * Initializes the Assistant class.
      */
     public UIAssistant() {
     }
 
     /**
      * Initializes the Assistant class with the given assistant model.
      * 
      * @param a
      *            the assistant model
      */
     public UIAssistant(IAssistant a) {
         super(a);
     }
 
     /**
      * Sets the total hours for this assistant.
      * 
      * @param totalHours
      *            the total hours
      */
     public void setTotalHours(double totalHours) {
         this.totalHours = totalHours;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.model.AbstractModel#toString()
      */
     @Override
     public String toString() {
         String name = getFirstName() + " " + getLastName();
 
         if (getFirstName() == null || getLastName() == null) {
             return "";
         } else if (totalHours == 0.0) {
             return name;
         } else {
             return name + " (" + totalHours + "h)";
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see de.aidger.view.models.UIModel#getDataType()
      */
     @Override
     public DataType getDataType() {
         return DataType.Assistant;
     }
 
     /**
      * Check if two objects are equal.
      * 
      * @param o
      *            The other object
      * @return True if both are equal
      */
     @Override
     public boolean equals(Object o) {
         if (o instanceof IAssistant) {
             IAssistant a = (IAssistant) o;
             return (getFirstName() == null ? a.getFirstName() == null : (a
                 .getFirstName() == null ? false : a.getFirstName().equals(
                 getFirstName())))
                     && (getLastName() == null ? a.getLastName() == null : (a
                         .getLastName() == null ? false : a.getLastName()
                         .equals(getLastName())))
                     && (getEmail() == null ? a.getEmail() == null : (a
                         .getEmail() == null ? false : a.getEmail().equals(
                        getEmail())))
                    && (getQualification() == null ? a.getQualification() == null
                            : (a.getQualification() == null ? false : a
                                .getQualification().equals(getQualification())));
         } else {
             return false;
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see java.lang.Comparable#compareTo(java.lang.Object)
      */
     @Override
     public int compareTo(UIAssistant o) {
         return toString().compareTo(o.toString());
     }
 }
