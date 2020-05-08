 package org.hackystat.projectbrowser.page.validator;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.wicket.markup.html.form.Form;
 import org.apache.wicket.markup.html.form.FormComponent;
 import org.apache.wicket.markup.html.form.ListMultipleChoice;
 import org.apache.wicket.markup.html.form.validation.AbstractFormValidator;
 import org.apache.wicket.extensions.markup.html.form.DateTextField;
 import org.hackystat.sensorbase.resource.projects.ProjectUtils;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Provides a project date validator.
  * @author Philip Johnson
  */
 public class ProjectDateValidator extends AbstractFormValidator {
 
   /** For serialization. */
   private static final long serialVersionUID = 1L;
 
   /** form components to be checked. */
   private final FormComponent[] components;
 
   /**
    * Takes a Project menu and a Date field.
    * @param formComponent1 The project menu component. 
    * @param formComponent2 The Date field component.
    */
   public ProjectDateValidator(FormComponent formComponent1, FormComponent formComponent2) {
     if (formComponent1 == null) {
       throw new IllegalArgumentException("argument formComponent1 cannot be null");
     }
     if (formComponent2 == null) {
       throw new IllegalArgumentException("argument formComponent2 cannot be null");
     }
     if (!(formComponent1 instanceof ListMultipleChoice)) {
       throw new IllegalArgumentException("ProjectDateValidator not given a ListMultipleChoice");
     }
     if (!(formComponent2 instanceof DateTextField)) {
       throw new IllegalArgumentException("ProjectDateValidator not given a DateTextField");
     }
     components = new FormComponent[] { formComponent1, formComponent2 };
   }
 
   /**
    * 
    * 
    * Returns the form components.
    * @return The form components. 
    */
  //@Override
   public FormComponent[] getDependentFormComponents() {
     return components.clone();
   }
 
   /**
    * Performs the validation. 
    * @param arg0 The form to validate. 
    */
   @SuppressWarnings("unchecked")
  //@Override
   public void validate(Form arg0) {
     ListMultipleChoice projectMenu = (ListMultipleChoice)components[0]; 
     DateTextField dateField = (DateTextField)components[1];
     
     List<Project> projects = (List<Project>)projectMenu.getConvertedInput();
     Date date1 = (Date)dateField.getConvertedInput();
     XMLGregorianCalendar tomorrow = Tstamp.incrementDays(Tstamp.makeTimestamp(), 1);
     
     for (Project project : projects) {
       XMLGregorianCalendar formTime = Tstamp.makeTimestamp(date1.getTime());
       if (!ProjectUtils.isValidStartTime(project, formTime)) { //NOPMD
         error(dateField, "DateBeforeProjectStartTime");
       }
       else if (!ProjectUtils.isValidEndTime(project, formTime)) { //NOPMD
         error(dateField, "DateAfterProjectEndTime");
       }
       else if (Tstamp.greaterThan(formTime, tomorrow)) {
         error(dateField, "DateInFuture");
       }
     }
   }
 
 }
