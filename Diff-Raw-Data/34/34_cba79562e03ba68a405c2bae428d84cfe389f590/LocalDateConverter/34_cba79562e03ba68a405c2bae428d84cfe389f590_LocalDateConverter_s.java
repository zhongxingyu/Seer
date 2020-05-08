 package fi.muni.pa165.web.converter;
 
 import java.util.Locale;
 import org.apache.wicket.util.convert.ConversionException;
 import org.apache.wicket.util.convert.IConverter;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 
 /**
  *
  * @author Oliver Pentek
  */
 public final class LocalDateConverter implements IConverter<LocalDate> {
     
     public static final LocalDateConverter INSTANCE = new LocalDateConverter();
     
    private static final String STYLE = "S-";          //pri zmene stylu musim zmenit aj style v datepickery!!!
 
     public LocalDateConverter() {
     }
 
     @Override
     public LocalDate convertToObject(String value, Locale locale) {
         LocalDate date = new LocalDate();
         // don't re-throw because we want to see the original one!
         try {
            LocalDate.parse(value, prepareFormatter(locale));
         } catch (IllegalArgumentException ex) {
             ConversionException ce = new ConversionException(value);
             String dateFormat = DateTimeFormat.patternForStyle(STYLE, locale);
             ce.setVariable("date-format", dateFormat);
             throw ce;
         }
         return date;
     }
 
     @Override
     public String convertToString(LocalDate value, Locale locale) {
         return this.prepareFormatter(locale).print(value);
     }
 
     private DateTimeFormatter prepareFormatter(Locale locale) {
         return DateTimeFormat.forStyle(STYLE).withLocale(locale);
     }
 }
