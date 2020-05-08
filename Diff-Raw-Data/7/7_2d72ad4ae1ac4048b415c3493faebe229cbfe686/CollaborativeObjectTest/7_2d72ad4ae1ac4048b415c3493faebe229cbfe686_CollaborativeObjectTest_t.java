 package gx.realtime;
 
 import gx.realtime.serialize.Cloner;
 import org.junit.Test;
 
import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 public class CollaborativeObjectTest
 {
 
     private static CollaborativeString string;
 
     @Test
     public void testClone()
     {
         TestObject simpleObject = new TestObject(123);
         TestObject testObject = new TestObject(456, simpleObject);
         TestObject clone = Cloner.clone(simpleObject);
         assertEquals(simpleObject, clone);
         assertFalse(simpleObject == clone);
     }
 
 }
