 package mozart.core.validator;
 
 import java.lang.annotation.Annotation;
 
 import mozart.core.annotation.Date;
 import mozart.core.exception.Error;
 import mozart.core.exception.ErrorWrapper;
 
 import org.joda.time.format.DateTimeFormat;
 
 public class DateValidator extends Validator {
 
 	@Override
 	public void validate(ErrorWrapper errorWrapper, Annotation annot, String paramName, String value)
 	        throws Exception {
 		Date date = (Date) annot;
 		String format = date.format();
 
 		try {
 			DateTimeFormat.forPattern(format).parseDateTime(value);
 		} catch (Exception e) {
			errorWrapper.registerError(new Error(paramName, String.format(
			    "Invalid date. Should be in %s format",
			    format)));
 		}
 	}
 }
