 package uk.ac.ebi.fg.annotare2.magetab.checker;
 
 import org.junit.Test;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static uk.ac.ebi.fg.annotare2.magetab.checker.CheckPosition.NO_FILE_NAME;
 import static uk.ac.ebi.fg.annotare2.magetab.checker.CheckPosition.NO_INDEX;
 
 /**
  * @author Olga Melnichuk
  */
 public class CheckPositionTest {
 
     @Test
     public void testUndefinedPosition() {
         CheckPosition pos = CheckPosition.undefinedPosition();
         assertTrue(pos.isUndefined());
         assertEquals(NO_FILE_NAME, pos.getFileName());
         assertEquals(NO_INDEX, pos.getLine());
         assertEquals(NO_INDEX, pos.getColumn());
     }
 
     @Test
     public void testCreatePosition() {
         CheckPosition pos = CheckPosition.createPosition("file", 2, 3);
        assertFalse(pos.isUndefined());
         assertEquals("file", pos.getFileName());
         assertEquals(2, pos.getLine());
         assertEquals(3, pos.getColumn());
     }
 }
