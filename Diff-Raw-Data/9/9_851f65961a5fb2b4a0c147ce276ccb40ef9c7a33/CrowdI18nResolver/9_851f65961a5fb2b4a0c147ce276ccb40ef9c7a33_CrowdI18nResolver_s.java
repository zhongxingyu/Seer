 package com.atlassian.sal.crowd.message;
 
 import com.atlassian.crowd.util.I18nHelper;
 import com.atlassian.sal.core.message.AbstractI18nResolver;
 
 import java.io.Serializable;
 import java.text.MessageFormat;
 import java.util.Locale;
 import java.util.Map;
 
 /**
  * Crowd i18n resolver
  *
  * @since 2.0.0
  */
 public class CrowdI18nResolver extends AbstractI18nResolver
 {
     private final I18nHelper helper;
 
    public CrowdI18nResolver(I18nHelper helper)
     {
        this.helper = helper;
     }
 
     public String resolveText(String key, Serializable[] arguments)
     {
         return MessageFormat.format(helper.getText(key), (Object[]) arguments);
     }
 
     public Map<String, String> getAllTranslationsForPrefix(final String prefix, final Locale locale)
     {
         throw new UnsupportedOperationException("This application does not support this method call yet!");
     }
 }
