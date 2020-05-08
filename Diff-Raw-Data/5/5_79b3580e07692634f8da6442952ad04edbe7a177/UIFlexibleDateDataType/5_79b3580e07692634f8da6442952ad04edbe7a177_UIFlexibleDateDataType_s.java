 package org.pillarone.riskanalytics.application.client;
 
 import com.ulcjava.base.client.datatype.DataTypeConversionException;
 import com.ulcjava.base.client.datatype.UIDataType;
 import com.ulcjava.base.shared.ErrorCodes;
 import com.ulcjava.base.shared.ErrorObject;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 public class UIFlexibleDateDataType extends UIDataType {
 
     private List<String> possibleFormats;
     private String displayFormat;
 
     @Override
     protected Object doStringToObjectConversion(String s, Object o) throws DataTypeConversionException {
         try {
             return new SimpleDateFormat(displayFormat).parse(s);
        } catch (ParseException ex) {
             //try the next format
         }
         for (String format : possibleFormats) {
             try {
                 SimpleDateFormat dateFormat = new SimpleDateFormat(format);
                 dateFormat.setLenient(false);
                 return dateFormat.parse(s);
            } catch (ParseException ex) {
                 //try the next format
             }
         }
         throw new DataTypeConversionException("Not a valid date format", new ErrorObject(ErrorCodes.ERROR_CODE_BAD_DATE_FORMAT, s));
     }
 
     @Override
     public String convertToString(Object o, boolean forEditing) {
         if (o instanceof Date) {
             Date date = (Date) o;
             return new SimpleDateFormat(displayFormat).format(date);
         }
         return o != null ? o.toString() : null;
     }
 
     public void setFormats(List<String> formats) {
         this.possibleFormats = formats;
     }
 
     public void setDisplayFormat(String displayFormat) {
         this.displayFormat = displayFormat;
     }
 }
