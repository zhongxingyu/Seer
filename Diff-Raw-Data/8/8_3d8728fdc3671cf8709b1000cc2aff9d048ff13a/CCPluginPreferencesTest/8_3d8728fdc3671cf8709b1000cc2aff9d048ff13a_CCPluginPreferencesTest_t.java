 package net.sf.commonclipse;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertThat;
 
 import java.util.regex.Pattern;
 
import org.junit.Test;

 /**
  * Tests for CCPluginPreferences.
  * @author fgiust
  * @version $Revision$ ($Author$)
  */
 public class CCPluginPreferencesTest
 {
 
     /**
      * test the conversion from an array of strings to a regexp.
      */
    @Test
     public void testRegexpConversion()
     {
         Pattern regexp = CCPluginPreferences.generateRegExp("log;test?u?;done*");
         assertThat(regexp.pattern(), equalTo("(^log$)|(^test.u.$)|(^done*$)"));
     }
 
     /**
      * test the conversion from an array of strings to a regexp.
      */
    @Test
     public void testEmptyRegexpConversion()
     {
         Pattern regexp = CCPluginPreferences.generateRegExp("");
         assertFalse(regexp.matcher("").matches());
         assertFalse(regexp.matcher("a").matches());
 
     }
 
     /**
      * test the conversion from an array of strings to a regexp.
      */
    @Test
     public void testNullRegexpConversion()
     {
         Pattern regexp = CCPluginPreferences.generateRegExp(null);
         assertFalse(regexp.matcher("").matches());
         assertFalse(regexp.matcher("a").matches());
     }
 
 }
