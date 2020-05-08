 package org.nohope;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.nohope.test.SerializationUtils;
 
 import static com.mongodb.util.MyAsserts.assertFalse;
 import static com.mongodb.util.MyAsserts.assertTrue;
 import static org.junit.Assert.assertEquals;
 
 /**
  * @author <a href="mailto:ketoth.xupack@gmail.com">Ketoth Xupack</a>
  * @since 2013-10-11 13:56
  */
 public class SeriesElementTest {
 
     @Test
     @SuppressWarnings({"ObjectEqualsNull", "EqualsBetweenInconvertibleTypes"})
     public void serialization() {
        final DateTime date = DateTime.parse("2000-01-01T01:01:01.001");
         final SeriesElement<Integer> e = new SeriesElement<>(date, 1);
        assertEquals("{\"timestamp\":946666861001,\"value\":1}", e.toString());
         final SeriesElement<Integer> mongoCloned = SerializationUtils.assertMongoClonedEquals(e);
         final SeriesElement<Integer> javaCloned = SerializationUtils.assertJavaClonedEquals(e);
 
         assertTrue(date.isEqual(mongoCloned.getTimestamp()));
         assertTrue(date.isEqual(javaCloned.getTimestamp()));
         assertEquals(1, (int) mongoCloned.getValue());
         assertEquals(1, (int) javaCloned.getValue());
 
         assertFalse(e.equals(null));
         assertFalse(e.equals(1));
         assertTrue(e.equals(e));
 
         assertEquals(e.hashCode(), javaCloned.hashCode());
         assertEquals(e.hashCode(), mongoCloned.hashCode());
     }
 }
