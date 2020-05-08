 package uk.me.chrs.inflect;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
 
 import static uk.me.chrs.inflect.Inflect_EN.*;
 
 public class JavaPluralTest {
 
     @Test
     public void unconditionalPlurals() {
        assertEquals("aircraft", many("aircraft"));
        assertEquals("postmen", many("postman"));
        assertEquals("skies", many("sky"));
        assertEquals("faxes", many("fax"));
     }
 
     @Test
     public void conditionalPlurals() {
         assertEquals("vertices", plural(0, "vertex"));
         assertEquals("vertex", plural(1, "vertex"));
         assertEquals("vertices", plural(8, "vertex"));
         assertEquals("8 typos", count(8, "typo"));
         assertEquals("no buses", some(0, "bus"));
     }
 
 }
