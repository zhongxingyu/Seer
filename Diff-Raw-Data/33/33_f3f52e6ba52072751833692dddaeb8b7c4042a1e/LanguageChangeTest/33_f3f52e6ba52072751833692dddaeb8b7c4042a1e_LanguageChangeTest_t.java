 package models.data;
 
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.Assert;
 
 import play.mvc.Http;
 import play.i18n.Lang;
 
 import models.EMessages;
 import models.data.Language;
 import test.ContextTest;
 
 /**
  * This class tests the changing of languages.
  * @author Felix Van der Jeugt
  */
 public class LanguageChangeTest extends ContextTest {
 
     // Set the accepted languages to just Dutch.
     @Override protected Http.Request makeRequest() {
         return new StubRequest() {
             @Override public List<Lang> acceptLanguages() {
                 return Arrays.asList(Lang.forCode("nl"));
             }
         };
     }
 
     @Test public void testDefaultLanguage() {
         Assert.assertEquals("Engels", EMessages.get("languages.en"));
     }
 
     @Test public void testSetLanguage() {
         EMessages.setLang("en");
         Assert.assertEquals("English", EMessages.get("languages.en"));
     }
 
    @Test public void testGetLangDefault() {
        Assert.assertEquals("nl", EMessages.getLang());
    }

    @Test public void testGetLangChanged() {
        EMessages.setLang("en");
        Assert.assertEquals("en", EMessages.getLang());
     }
 
 }
