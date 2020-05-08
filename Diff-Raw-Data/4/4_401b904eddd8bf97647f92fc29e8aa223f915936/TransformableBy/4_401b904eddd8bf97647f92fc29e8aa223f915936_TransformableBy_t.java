 package com.astound.fragments.locators;
 
 import com.astound.fragments.format.Formatter;
 import com.astound.fragments.format.transformers.StringTransformer;
 import org.openqa.selenium.By;
 import org.openqa.selenium.SearchContext;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ByIdOrName;
 import org.openqa.selenium.support.How;
 
 import java.util.List;
 
 public class TransformableBy extends By {
 
     private final How how;
 
     private final String using;
 
     private final Formatter formatter;
 
     public TransformableBy(How how, String using, Formatter formatter) {
         this.how = how;
         this.using = using;
         this.formatter = formatter;
     }
 
     public TransformableBy(How how, String using) {
         this(how, using, new Formatter());
     }
 
     public TransformableBy using(String using) {
         return new TransformableBy(how, using, formatter);
     }
 
     public TransformableBy how(How how) {
         return new TransformableBy(how, using, formatter);
     }
 
     public TransformableBy transformWith(StringTransformer transformer) {
         return new TransformableBy(how, formatter.format(using, transformer), formatter);
     }
 
     @Override public List<WebElement> findElements(SearchContext context) {
         return buildBy(how, using).findElements(context);
     }
 
    @Override public String toString() {
        return String.format("By.%s: %s", how, using);
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
 }
 
