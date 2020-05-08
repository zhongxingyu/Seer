 package de.aidger.model.validators;
 
 import java.util.Date;
 import java.text.MessageFormat;
 
 import static de.aidger.utils.Translation._;
 import de.aidger.model.AbstractModel;
 
 /**
  * Validates the range of two dates in the specified model class.
  *
  * @author aidGer Team
  */
 public class DateRangeValidator extends Validator {
 
     /**
      * Initializes the DataRangeValidator class.
      * 
      * @param model
      *            The model to validate
      * @param from
      *            The from date
      * @param to
      *            The to date
      */
     public DateRangeValidator(AbstractModel model, String from, String to) {
         super(model, new String[] { from, to });
         message = _("is an incorrect date range");
     }
 
     /**
      * Validate the Date Range
      *
      * @return True if the date range validates
      */
     @Override
     public boolean validate() {
         Date from = (Date)getValueOf(members[0]);
         Date to = (Date)getValueOf(members[1]);
         if (!validate(from, to)) {
             if (model != null) {
                 model.addError(MessageFormat.format(
                         _("The date range {0} and {1} is incorrect."),
                         (Object[])members));
             }
             return false;
         }
         return true;
     }
 
     /**
      * Validate the range of the given dates.
      *
      * Note: Only used for testing purposes.
      *
      * @param from
      *            The from date
      * @param to
      *            The to date
      * @return True if the date range validates
      */
     public static boolean validate(Date from, Date to) {
        if (from.equals(to) || from.after(to)) {
             return false;
         }
         return true;
     }
 
     /**
      * Empty overridden function
      */
     @Override
     public boolean validateVar(Object o) {
         return false;
     }
 
 }
