 package be.idevelop.fiber;
 
 import org.junit.Test;
 
import static junit.framework.Assert.assertEquals;
 
 public class ObjectSerializerTest {
 
     @Test
     public void canSerializeAndDeserializeFieldsOfSuperClass() {
         Fiber fiber = new Fiber();
         fiber.register(ExtendedObject.class);
 
         ExtendedObject obj = new ExtendedObject(12, "42");
         ExtendedObject result = fiber.deserialize(fiber.serialize(obj));
 
         assertEquals(obj, result);
     }
 
 }
