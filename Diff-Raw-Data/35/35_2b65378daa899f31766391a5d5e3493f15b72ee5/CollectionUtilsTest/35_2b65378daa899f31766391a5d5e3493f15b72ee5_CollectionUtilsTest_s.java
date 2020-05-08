 package org.nohope.typetools.collection;
 
 import org.junit.Test;
 import org.nohope.ITranslator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertSame;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">ketoth xupack</a>
  * @since 9/18/12 5:34 PM
  */
 public class CollectionUtilsTest {
     @Test
     public void fillListTest() {
         final Object marker = new Object();
         final List<Object> list = CollectionUtils.fillList(10, marker);
         assertEquals(10, list.size());
         for (final Object obj : list) {
             assertSame(marker, obj);
         }
     }
 
     @Test
     public void testToArray() throws Exception {
        final List<Integer> integerList = new ArrayList<>(100);
        for (int i =0; i<100; ++i ){
             integerList.add(i);
         }
 
         final String[] arr = CollectionUtils.mapArray(integerList.toArray(new Integer[integerList.size()])
                 , new ITranslator<Integer, String>() {
             @Override
             public String translate(final Integer source) {
                 return "" + source;
             }

            @Override
            public Class<String> getTargetClass() {
                return String.class;
            }
         });
 
        assertEquals(arr.length, 100);
     }
 }
