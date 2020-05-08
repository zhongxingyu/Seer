 /*
  *  Copyright the original author or authors.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 package therian.operator.convert;
 
 import static org.junit.Assert.assertArrayEquals;
 
 import java.util.Arrays;
 import java.util.Iterator;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.junit.Test;
 
 import therian.TherianModule;
 import therian.TypeLiteral;
 import therian.operation.Convert;
 import therian.operator.OperatorTest;
 import therian.util.Positions;
 
 public class DefaultToArrayConverterTest extends OperatorTest {
 
     public static class Foo implements Iterable<String> {
 
         /**
          * {@inheritDoc}
          */
         @Override
         public Iterator<String> iterator() {
             return Arrays.asList(DefaultToArrayConverterTest.STRINGS).iterator();
         }
 
     }
 
     private static final String[] STRINGS = new String[] { "foo", "bar", "baz" };
 
     protected TherianModule module() {
         return TherianModule.create().withOperators(new DefaultToArrayConverter(), new DefaultToListConverter(),
             new IterableToList(), new IteratorToList());
     }
 
     @Test
     public void testStringToArrayOfString() {
         assertArrayEquals(new String[] { "foo" },
             therianContext.eval(Convert.to(String[].class, Positions.readOnly("foo"))));
     }
 
     @Test
     public void testStringToArrayOfObject() {
         assertArrayEquals(new String[] { "foo" },
             therianContext.eval(Convert.to(Object[].class, Positions.readOnly("foo"))));
     }
 
     @Test
     public void testArrayOfIntToArrayOfInteger() {
         final int[] beast = { 6, 6, 6 };
         assertArrayEquals(ArrayUtils.toObject(beast),
             therianContext.eval(Convert.to(Integer[].class, Positions.readOnly(beast))));
     }
 
     @Test
     public void testArrayOfIntToArrayOfObject() {
         final int[] beast = { 6, 6, 6 };
        assertArrayEquals(ArrayUtils.toObject(beast),
            therianContext.eval(Convert.to(Object[].class, Positions.readOnly(beast))));
     }
 
     @Test
     public void testIntegerToArrayOfInt() {
         assertArrayEquals(new int[] { 666 },
             therianContext.eval(Convert.to(int[].class, Positions.readOnly(Integer.valueOf(666)))));
     }
 
     @Test
     public void testIterableOfStringToArrayOfString() {
         assertArrayEquals(
             STRINGS,
             therianContext.eval(Convert.to(String[].class,
                 Positions.readOnly(new TypeLiteral<Iterable<String>>() {}, new Foo()))));
     }
 
     @Test
     public void testIteratorOfStringToArrayOfString() {
         assertArrayEquals(
             STRINGS,
             therianContext.eval(Convert.to(String[].class,
                 Positions.readOnly(new TypeLiteral<Iterator<String>>() {}, new Foo().iterator()))));
     }
 }
