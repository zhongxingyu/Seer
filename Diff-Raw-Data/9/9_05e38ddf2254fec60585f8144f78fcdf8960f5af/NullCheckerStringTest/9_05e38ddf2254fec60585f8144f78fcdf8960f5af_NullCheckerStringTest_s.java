 package jayray.net.common.nullchecker;
 
import static org.junit.Assert.*;
 
 import java.util.ArrayList;
import java.util.GregorianCalendar;
 import java.util.List;
 
import jayray.net.common.nullchecker.NullChecker;

import org.junit.Test;
 
 public class NullCheckerStringTest {
     @Test
     public void checkIsNullOnNullString() {
         String value = null;
         assertEquals(true, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnNullString() {
         String value = null;
         assertEquals(false, NullChecker.isNotNullish(value));
     }
 
     @Test
     public void checkIsNullOnBlankString() {
         String value = "";
         assertEquals(true, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnBlankString() {
         String value = "";
         assertEquals(false, NullChecker.isNotNullish(value));
     }
 
     @Test
     public void checkIsNullOnSpacedString() {
         String value = " ";
         assertEquals(true, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnSpacedString() {
         String value = " ";
         assertEquals(false, NullChecker.isNotNullish(value));
     }
 
     @Test
     public void checkIsNullOnDoubleSpacedString() {
         String value = "  ";
         assertEquals(true, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnDoubleSpacedString() {
         String value = "  ";
         assertEquals(false, NullChecker.isNotNullish(value));
     }
 
     @Test
     public void checkIsNullOnPoplatedString() {
         String value = "x";
         assertEquals(false, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnPopulatedString() {
         String value = "x";
         assertEquals(true, NullChecker.isNotNullish(value));
     }
 
     @Test
     public void checkIsNullOnNullList() {
         List<String> value = null;
         assertEquals(true, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnNullList() {
         List<String> value = null;
         assertEquals(false, NullChecker.isNotNullish(value));
     }
 
     @Test
     public void checkIsNullOnEmptyList() {
         List<String> value = new ArrayList<String>();
         assertEquals(true, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnEmptyList() {
         List<String> value = new ArrayList<String>();
         assertEquals(false, NullChecker.isNotNullish(value));
     }
 
     @Test
     public void checkIsNullOnPopulatedList() {
         List<String> value = new ArrayList<String>();
         value.add("x");
         assertEquals(false, NullChecker.isNullish(value));
     }
 
     @Test
     public void checkIsNotNullOnPopulatedList() {
         List<String> value = new ArrayList<String>();
         value.add("x");
         assertEquals(true, NullChecker.isNotNullish(value));
     }
 }
