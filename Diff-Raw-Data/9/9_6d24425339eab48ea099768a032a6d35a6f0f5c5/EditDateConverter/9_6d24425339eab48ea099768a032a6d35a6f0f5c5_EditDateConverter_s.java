 package jpaoletti.jpm.struts.converter;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;

 import jpaoletti.jpm.converter.ConverterException;
 import jpaoletti.jpm.core.PMContext;
 
 /**Converter for date.<br>
  * <pre>
  * {@code
  * <converter class="jpaoletti.jpm.converter.EditDateConverter">
  *     <operationId>edit (or add)</operationId>
  *     <properties>
  *         <property name="format" value="dd/MM/yyyy" />
  *     <properties>
  * </converter>
  * }
  * </pre>
  * @author jpaoletti
  * */
 public class EditDateConverter extends EditStringConverter {
 
     @Override
     public Object build(PMContext ctx) throws ConverterException {
         try {
             String value = (String) ctx.getFieldValue();
             if (value != null && !"".equals(value.trim())) {
                 return getDateFormat().parse((String) value);
             }
         } catch (ParseException e) {
            ctx.getPresentationManager().error(e);
         }
         return null;
     }
 
     @Override
     public String visualize(PMContext ctx) throws ConverterException {
         try {
             final Date o = (Date) ctx.getFieldValue();
             return super.visualize("date_converter.jsp?format=" + normalize(javaToJavascriptDateFormat(getFormatString())) + "&value=" + getDateFormat().format(o));
         } catch (Exception e) {
             return super.visualize("date_converter.jsp?format=" + normalize(javaToJavascriptDateFormat(getFormatString())) + "&value=");
         }
     }
 
     /**
      * Return the format object of the date
      * @return The format
      */
     public DateFormat getDateFormat() {
         DateFormat df = new SimpleDateFormat(getFormatString());
         return df;
     }
 
     private String getFormatString() {
         return getConfig("format", "MM/dd/yyyy");
     }
 
     private String javaToJavascriptDateFormat(String s) {
         /*
          * The format can be combinations of the following:
          * d - day of month (no leading zero)
          * dd - day of month (two digit)
          * D - day name short
          * DD - day name long
          * m - month of year (no leading zero)
          * mm - month of year (two digit)
          * M - month name short
          * MM - month name long
          * y - year (two digit)
          * yy - year (four digit)
          */
         return s.replaceAll("yy", "y").replace('M', 'm');
     }
 }
