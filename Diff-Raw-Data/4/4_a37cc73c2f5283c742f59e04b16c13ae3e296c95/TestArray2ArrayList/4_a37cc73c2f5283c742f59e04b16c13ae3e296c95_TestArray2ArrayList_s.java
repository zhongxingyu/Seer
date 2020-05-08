 package net.transformatorhuis.junit.examples;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author cyberroadie
  */
 public class TestArray2ArrayList {
 
     @Test
     public void testArray() {
         String blah = "hello world lala";
        List arrayStringList = Arrays.asList(blah);
         try {
             // This won't work because this list is a view on an array
             // which immutable
             arrayStringList.remove(2);
         } catch (Exception ex) {
             assertTrue(ex instanceof UnsupportedOperationException);
         }
         List stringList = new ArrayList(arrayStringList);
         assertEquals("lala",stringList.remove(2));
 
     }
 }
