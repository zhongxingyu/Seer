 package org.open_t.dialog;
 
 import java.beans.PropertyEditorSupport;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.codehaus.groovy.grails.web.binding.StructuredPropertyEditor;
 
 @SuppressWarnings({ "rawtypes", "unchecked" })
 public class DateTimePropertyEditor extends PropertyEditorSupport implements StructuredPropertyEditor {
 
 	SimpleDateFormat dateTimeFormat;
 
 	public DateTimePropertyEditor(String format) {
 		// format was "yyyy-MM-dd'T'HH:mm:ss"
 		this.dateTimeFormat = new SimpleDateFormat(format);
 	}
 
 	public List getRequiredFields() {
 		List requiredFields = new ArrayList();
 		requiredFields.add("date");
 		return requiredFields;
 	}
 
 	public List getOptionalFields() {
 		List optionalFields = new ArrayList();
 		optionalFields.add("time");
 		return optionalFields;
 	}
 
 	public Object assemble(Class type, Map fieldValues) throws IllegalArgumentException {
 		if (!fieldValues.containsKey("date")) {
 			throw new IllegalArgumentException("Can't populate a date/time without a date");
 		}
 
 		String date = (String) fieldValues.get("date");
 
 		try {
 			if (date.isEmpty()) {
 				throw new IllegalArgumentException("Can't populate date/time without a date");
			}            
 			String time = (String) fieldValues.get("time");
 			if (time == null || time.isEmpty()) {
 				time = "00:00";
 			}
            if (date.contains("T")) {
               date=date.split("T")[0];
            }
 			String dateTime = date + "T" + time;
 			return dateTimeFormat.parse(dateTime);
 		}
 		catch (Exception nfe) {
 			throw new IllegalArgumentException("Unable to parse structured DateTime from request for date.");
 		}
 	}
 }
