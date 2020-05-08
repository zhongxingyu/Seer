 package models;
 
 
 import play.i18n.Lang;
 import play.i18n.Messages;
 import play.mvc.Http.Context;
 
 /**
  * An extension of the default Messages to override default language.
  * @author Ruben Taelman
  * @author Felix Van der Jeugt
  */
 public class EMessages extends Messages {
 
     /**
      * Set the language with valid Lang code
      * @param langCode the language code
      */
     public static void setLang(String langCode) {
         Context.current().session().put("customLanguage", langCode);
     }
 
     /**
      * Translates a message. Uses `java.text.MessageFormat` internally to format the message.
      * @param key the message key
      * @param args the message arguments
      * @return
      */
     public static String get(java.lang.String key, java.lang.Object... args) {
         String lang = Context.current().session().get("customLanguage");
         if(lang != null) return Messages.get(Lang.forCode(lang), key, args);
         else             return Messages.get(key, args);
     }
 
 }
