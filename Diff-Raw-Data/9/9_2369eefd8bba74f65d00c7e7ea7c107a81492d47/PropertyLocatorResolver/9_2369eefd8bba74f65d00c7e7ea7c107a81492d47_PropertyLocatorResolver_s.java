 package com.astound.fragments.locators;
 
 import com.astound.fragments.annotation.FindByExternal;
 import org.openqa.selenium.By;
 import org.openqa.selenium.support.ByIdOrName;
 import org.openqa.selenium.support.How;
 
 import java.lang.reflect.Field;
 import java.util.Properties;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public abstract class PropertyLocatorResolver implements ILocatorResolver {
 
     private final static Pattern LOCATOR_PATTERN = Pattern
             .compile("(css|xpath|id|name|tag_name|id_or_name|class_name|link_text|partial_link_text)\\s*::\\s*(.+)", Pattern.CASE_INSENSITIVE);
 
     private final static Pattern NOT_AVAILABLE_PATTERN = Pattern.compile("(n\\a|n/a)", Pattern.CASE_INSENSITIVE);
 
     private final Properties properties;
 
     public PropertyLocatorResolver() {
         properties = loadProperties();
     }
 
     protected abstract Properties loadProperties();
 
     public final boolean isApplicable(Field field) {
         return field.isAnnotationPresent(FindByExternal.class) && getPropertyKey(field).length() > 0;
     }
 
     public final By resolve(Field field) {
         String locator = properties.getProperty(getPropertyKey(field));
 
         if (locator == null || locator.length() == 0) {
             throw new IllegalArgumentException(String.format("Value for locators property [%s] missing.", getPropertyKey(field)));
         }
 
         Matcher locatorMatcher = LOCATOR_PATTERN.matcher(locator);
 
         if (locatorMatcher.matches()) {
            return buildBy(convertHow(locatorMatcher.group(1)), convertUsing(locatorMatcher.group(2)));
         }
 
         if (NOT_AVAILABLE_PATTERN.matcher(locator).matches()) {
             return new ByNotAvailable(field);
         }
 
         throw new IllegalArgumentException(String.format("Incorrect locators syntax [%s].", locator));
     }
 
    private static String convertUsing(String string) {
        return string.trim().toLowerCase();
    }

    private static How convertHow(String string) {
         return Enum.valueOf(How.class, string.trim().toUpperCase());
     }
 
     private static By buildBy(How how, String locator) {
         switch (how) {
             case CLASS_NAME:
                 return By.className(locator);
             case CSS:
                 return By.cssSelector(locator);
             case ID:
                 return By.id(locator);
             case ID_OR_NAME:
                 return new ByIdOrName(locator);
             case LINK_TEXT:
                 return By.linkText(locator);
             case NAME:
                 return By.name(locator);
             case PARTIAL_LINK_TEXT:
                 return By.partialLinkText(locator);
             case TAG_NAME:
                 return By.tagName(locator);
             case XPATH:
                 return By.xpath(locator);
             default:
                 throw new IllegalArgumentException("Cannot determine how to locate element " + how);
         }
     }
 
     private static String getPropertyKey(Field field) {
         return field.getAnnotation(FindByExternal.class).value();
     }
 
 }
