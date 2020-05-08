 package com.sapienter.jbilling.client.util;
 
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.apache.struts.action.ActionError;
 import org.apache.struts.action.ActionErrors;
 import org.apache.struts.action.DynaActionForm;
 import org.apache.struts.validator.Resources;
 
 import com.sapienter.jbilling.common.Util;
 
 public class FormDateHelper {
 	public static final String SUFFIX_DAY = "_day";
 	public static final String SUFFIX_MONTH = "_month";
 	public static final String SUFFIX_YEAR = "_year";
 
 	private static final Logger log = Logger.getLogger(FormDateHelper.class);
 	private final DynaActionForm myForm;
 	private final HttpServletRequest myRequest;
 	
 	public FormDateHelper(DynaActionForm form, HttpServletRequest request){
 		myForm = form;
 		myRequest = request;
 	}
 
 	public Date parseDate(String prefix, String prompt, ActionErrors errorsCollector) {
         Date date = null;
         String year = (String) myForm.get(prefix + SUFFIX_YEAR);
         String month = (String) myForm.get(prefix + SUFFIX_MONTH);
         String day = (String) myForm.get(prefix + SUFFIX_DAY);
        
         // if one of the fields have been entered, all should've been
         if ((year.length() > 0 && (month.length() <= 0 || day.length() <= 0)) ||
             (month.length() > 0 && (year.length() <= 0 || day.length() <= 0)) ||
             (day.length() > 0 && (month.length() <= 0 || year.length() <= 0)) ) {
             // get the localized name of this field
             String field = Resources.getMessage(myRequest, prompt); 
             errorsCollector.add(ActionErrors.GLOBAL_ERROR,
                     new ActionError("errors.incomplete.date", field));
             return null;
         }
         if (year.length() > 0 && month.length() > 0 && day.length() > 0) {
             try {
                 date = Util.getDate(Integer.valueOf(year), 
                         Integer.valueOf(month), Integer.valueOf(day));
             } catch (Exception e) {
                 log.info("Exception when converting the fields to integer", e);
                 date = null;
             }
             
             if (date == null) {
                 // get the localized name of this field
                 String field = Resources.getMessage(myRequest, prompt); 
                 errorsCollector.add(ActionErrors.GLOBAL_ERROR,
                         new ActionError("errors.date", field));
             } 
         }
         return date;
     }
 	
     public void setFormDate(String prefix, Date date) {
         if (date != null) {
             GregorianCalendar cal = new GregorianCalendar();
             cal.setTime(date);
             myForm.set(prefix + SUFFIX_MONTH, String.valueOf(cal.get(
                     GregorianCalendar.MONTH) + 1));
             myForm.set(prefix + SUFFIX_DAY, String.valueOf(cal.get(
                     GregorianCalendar.DAY_OF_MONTH)));
             myForm.set(prefix + SUFFIX_YEAR, String.valueOf(cal.get(
                     GregorianCalendar.YEAR)));
         } else {
             myForm.set(prefix + SUFFIX_MONTH, null);
             myForm.set(prefix + SUFFIX_DAY, null);
             myForm.set(prefix + SUFFIX_YEAR, null);
         }
     }
 	
 }
