 package org.bpunit.assertions;
 
import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 
 import org.bpunit.examples.SomeClass;
 import org.bpunit.utils.SeedableRandom;
 import org.junit.Test;
 
 /**
  * A test case for {@link AssertUtils}
  */
 public class AssertUtilsTest {
     @Test
     public void testSimpleClass() {
         SomeClass sc = new SomeClass();
         AssertUtils.testProperties(sc, new SeedableRandom(System.currentTimeMillis()));
 
         // Make sure all properties were addressed
         assertNotNull("myInt not checked", sc.getMyInt());
         assertNotNull("myLong not checked", sc.getMyDate());
         assertNotNull("myString not checked", sc.getMyString());
         assertNotNull("someBoolean not checked", sc.getSomeBoolean());
         assertNotNull("someOtherBoolean not checked", sc.isSomeOtherBoolean());
        assertFalse("primitiveDouble not checked", "NaN".equals(String.valueOf(sc.getMyPrimitiveDouble())));
     }
 }
