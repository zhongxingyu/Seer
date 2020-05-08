 package cz.datalite.helpers;
 
 import java.text.ParseException;
import org.zkoss.lang.Classes;
 
 /**
  * Třída usnadňuje konvertování hodnot typu String na různé datové typy.
  * Zatím podporované konverze jsou StringTo:
  * <ul>
  *  <li>{@link Integer} nebo int</li>
  *  <li>{@link Long} nebo long</li>
  *  <li>{@link Double} nebo double</li>
  *  <li>{@link String}</li>
  *  <li>{@link java.util.Date}</li>
  * </ul>
  * @author Karel Čemus <cemus@datalite.cz>
  */
 final public class TypeConverter {
 
     public static String dateFormat = "dd.MM.yyyy";
 
     private TypeConverter() {
     }
 
     /**
      * Převede hodnotu typu String na hodnotu definovanou parametrem Type
      * @param <T> typ, na které se bude hodnota převádět
      * @param value hodnota, která se bude konvertovat
      * @param type typ hodnoty, na kterou se bude konvertovat
      * @return zkonvertovaná hodnota
      * @throws ParseException nepovedlo se zkonvertovat - generuje se při přetypování na date
      * @throws ClassCastException nepovedlo se přetypovat
      * @throws NumberFormatException nepovedlo se přetypovat - generuje se při přetypování na číslo
      */
     public static <T> T convertTo( final String value, final Class<T> type ) throws ParseException, ClassCastException, NumberFormatException {
         if ( value == null ) {
             return null;
         }
         if ( java.util.Date.class.equals( type ) ) {
             return type.cast( new java.text.SimpleDateFormat( dateFormat, java.util.Locale.getDefault() ).parse( value ) );
         } else if ( String.class.equals( type ) ) {
             return type.cast( value );
         } else if ( Integer.class.equals( type ) || Integer.TYPE.equals( type.getClass() ) ) {
             return ( T ) Integer.valueOf( value );
         } else if ( Double.class.equals( type ) || Double.TYPE.equals( type.getClass() ) ) {
             return ( T ) Double.valueOf( value );
         } else if ( Long.class.equals( type ) || Long.TYPE.equals( type.getClass() ) ) {
             return ( T ) Long.valueOf( value );
         } else if ( Boolean.class.equals( type ) || Boolean.TYPE.equals( type.getClass() ) ) {
             return ( T ) Boolean.valueOf( value );
         } else if ( type.isEnum() ) {
             for ( T enumValue : type.getEnumConstants() ) {
                 if ( enumValue.toString().equals( value ) ) {
                     return enumValue;
                 }
             }
             return null;
         } else {
            return ( T ) Classes.coerce( type, value );
         }
     }
 
     /**
      * Volá metodu {@link TypeConverter#convertTo} ale potlačuje všechny výjimky
      * @param <T> typ, na který se bude konvertovat
      * @param value hodnota ke konverzi
      * @param type typ, na který se bude konvertovat
      * @return zkonvertovaná hodnota, v případě neúspěchu vrací <b>null</b>
      */
     public static <T> T convertToSilent( final String value, final Class<T> type ) {
         try {
             return convertTo( value, type );
         } catch ( ParseException ex ) {
             return null;
         } catch ( ClassCastException ex ) {
             return null;
         } catch ( NumberFormatException ex ) {
             return null;
         }
     }
 }
