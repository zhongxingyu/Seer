 package com.atlassian.sal.bamboo.message;
 
 import org.apache.log4j.Logger;
 import com.atlassian.sal.core.message.AbstractI18nResolver;
 import com.opensymphony.xwork.TextProvider;
 import com.opensymphony.xwork.util.LocalizedTextUtil;
 
 import java.io.Serializable;
 import java.util.*;
 
 public class BambooI18nResolver extends AbstractI18nResolver
 {
     private static final Logger log = Logger.getLogger(BambooI18nResolver.class);
     // ------------------------------------------------------------------------------------------------------- Constants
     // ------------------------------------------------------------------------------------------------- Type Properties
     // ---------------------------------------------------------------------------------------------------- Dependencies
    private final TextProvider textProvider;

    // ---------------------------------------------------------------------------------------------------- Constructors
 
     public BambooI18nResolver(TextProvider textProvider)
     {
         this.textProvider = textProvider;
     }
 
     // ----------------------------------------------------------------------------------------------- Interface Methods
     // -------------------------------------------------------------------------------------------------- Action Methods
     // -------------------------------------------------------------------------------------------------- Public Methods
     public String resolveText(String key, Serializable[] arguments)
     {
         String text = textProvider.getText(key, Arrays.asList(arguments));
         if (text != null)
         {
             return text;
         }
         else
         {
             return key;
         }
     }
 
     public Map<String, String> getAllTranslationsForPrefix(String prefix, Locale locale)
     {
         throw new UnsupportedOperationException("This application does not support this method call yet!");
     }
 
     // ------------------------------------------------------------------------------------------------- Helper Methods
     // -------------------------------------------------------------------------------------- Basic Accessors / Mutators
 }
