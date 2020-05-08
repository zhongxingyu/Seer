 /*
  * The localeManager class is responsible for managing the locale of the
  * application. It main functionality is to get the representing String of the
  * resourceId, according to the locale property value.
  */
 package adg.red.locale;
 
 import adg.red.models.Locale;
 import adg.red.models.ResourceDictionary;
 import adg.red.utils.ConfigManager;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * The localeManager class is responsible for managing the locale of the
  * application. It main functionality is to get the representing String of the
  * resourceId, according to the locale property value.
  * <p/>
  * @author Witt
  */
 public class LocaleManager
 {
 
     private static final List<ResourceDictionary> resourceList;
     private static Properties properties;
     private static final Locale loc;
 
     static
     {
         loc = Locale.findByName(ConfigManager.getInstance().getPropertyValue("locale"));
         resourceList = ResourceDictionary.getResourceByLocaleId(loc.getId());
        setLocale(loc.getName());
 
     }
 
     private static void setLocale(String locale)
     {
         properties = new Properties();
         try
         {
             properties.load(LocaleManager.class.getResourceAsStream("locale." + locale + ".properties"));
         }
         catch (IOException ex)
         {
             Logger.getLogger(ConfigManager.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     /**
      * The function to get the String that represent the input resourseId
      * number.
      * <p/>
      * @param resourceId the number that we want to get the represented String
      * <p/>
      * @return the String represents the resourceId number
      */
     public static String get(int resourceId)
     {
 
        String text = "No transalation";
 
         for (ResourceDictionary reDict : resourceList)
         {
             if (reDict.getResourceDictionaryPK().getResourceId() == resourceId)
             {
                 text = reDict.getTextString();
             }
         }
         //return properties.getProperty(Integer.toString(resourceId));
         return text;
     }
 }
