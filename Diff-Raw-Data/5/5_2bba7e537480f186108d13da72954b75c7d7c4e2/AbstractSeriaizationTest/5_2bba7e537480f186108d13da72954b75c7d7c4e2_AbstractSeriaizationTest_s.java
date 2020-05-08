 package lv.k2611a.domain;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.lang.reflect.Array;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.junit.Ignore;
 import org.junit.Test;
 
 import junit.framework.AssertionFailedError;
 import lv.k2611a.network.resp.CustomSerialization;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.fail;
 
 @Ignore
 public abstract class AbstractSeriaizationTest<E extends CustomSerialization> {
 
     @SuppressWarnings("unchecked")
     @Test
     public void testAll() throws InvocationTargetException, IllegalAccessException {
         boolean methodFound = false;
         for (final Method method : this.getClass().getDeclaredMethods()) {
             if (method.isAnnotationPresent(EntityFactory.class)) {
                 methodFound = true;
                 String methodName = method.getName();
                 System.out.println("Testing entity created by " + methodName);
                 mutateAndCompare(method);
             }
         }
         if (!methodFound) {
             throw new AssertionFailedError("No method annotated with @EntityFactory found");
         }
     }
 
     private E createEntity(Method method) throws IllegalAccessException, InvocationTargetException {
         return ((E) method.invoke(this));
     }
 
     private void mutateAndCompare(final Method method)
             throws IllegalAccessException, InvocationTargetException {
         E entityToMutate = createEntity(method);
         E serializableEntity = entityToMutate;
         List<byte[]> alreadySerializedEntities = new ArrayList<byte[]>();
         mutateObjectAndCompare(alreadySerializedEntities, entityToMutate, serializableEntity);
 
     }
 
     private void mutateArrayFieldAndCompare(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity)
             throws InvocationTargetException, IllegalAccessException {
         Object arrayToChange = field.get(entityToMutate);
         int arrayLength = Array.getLength(arrayToChange);
         System.out.println("Mutating array " + arrayToChange + " of length " + arrayLength);
         assertFalse("Cannot test array of length 0", arrayLength == 0);
         for (int i = 0; i < arrayLength; i++) {
             Object arrayCellToChange = Array.get(arrayToChange, i);
             System.out.println("Mutating array cell " + i + " of " + arrayToChange);
             mutateObjectAndCompare(alreadySerializedEntities, arrayCellToChange, serializableEntity);
         }
 
     }
 
     private void restoreOriginalValue(Field field, E originalEntity, E entityToMutate) throws IllegalAccessException {
         field.set(entityToMutate, deepCopy(field.get(originalEntity)));
     }
 
     private void mutateObjectAndCompare(List<byte[]> alreadySerializedEntities, Object entityToMutate, E serializableEntity)
             throws InvocationTargetException, IllegalAccessException {
         for (Field field : entityToMutate.getClass().getDeclaredFields()) {
             System.out.println("Mutating " + field.getName() + " of class " + field.getType());
             field.setAccessible(true);
             if (field.getType().equals(long.class)) {
                 mutateLongFieldAndCompare(alreadySerializedEntities, field, entityToMutate, serializableEntity);
             } else if (field.getType().equals(int.class)) {
                 mutateIntFieldAndCompare(alreadySerializedEntities, field, entityToMutate, serializableEntity);
             } else if (field.getType().equals(byte.class)) {
                 mutateByteFieldAndCompare(alreadySerializedEntities, field, entityToMutate, serializableEntity);
             } else if (field.getType().equals(short.class)) {
                 mutateShortFieldAndCompare(alreadySerializedEntities, field, entityToMutate, serializableEntity);
             } else if (field.getType().equals(boolean.class)) {
                 mutateBooleanFieldAndCompare(alreadySerializedEntities, field, entityToMutate, serializableEntity);
             } else if (field.getType().isArray()) {
                 mutateArrayFieldAndCompare(alreadySerializedEntities, field, entityToMutate, serializableEntity);
             } else if (!field.getType().isPrimitive()) {
                 mutateObjectAndCompare(alreadySerializedEntities, field.get(entityToMutate), serializableEntity);
             } else {
                 fail("Cannot mutate value of field type " + field.getType() + " - cannot test serialization");
             }
         }
     }
 
     private void mutateBooleanFieldAndCompare(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity)
             throws InvocationTargetException, IllegalAccessException {
         mutateWithSetBoolean(alreadySerializedEntities, field, entityToMutate, serializableEntity, true);
     }
 
     private void mutateIntFieldAndCompare(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity)
             throws IllegalAccessException, InvocationTargetException {
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, 1);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, 2);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, 100);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, 255);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256 * 255);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256 * 256 * 255);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, Integer.MAX_VALUE);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, Integer.MIN_VALUE);
         mutateWithSetInt(alreadySerializedEntities, field, entityToMutate, serializableEntity, -1);
     }
 
     private void mutateShortFieldAndCompare(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity)
             throws IllegalAccessException, InvocationTargetException {
         mutateWithSetShort(alreadySerializedEntities, field, entityToMutate, serializableEntity, (short) 1);
         mutateWithSetShort(alreadySerializedEntities, field, entityToMutate, serializableEntity, (short) 2);
         mutateWithSetShort(alreadySerializedEntities, field, entityToMutate, serializableEntity, (short) -1);
         mutateWithSetShort(alreadySerializedEntities, field, entityToMutate, serializableEntity, Byte.MAX_VALUE);
         mutateWithSetShort(alreadySerializedEntities, field, entityToMutate, serializableEntity, Byte.MIN_VALUE);
         mutateWithSetShort(alreadySerializedEntities, field, entityToMutate, serializableEntity, Short.MAX_VALUE);
         mutateWithSetShort(alreadySerializedEntities, field, entityToMutate, serializableEntity, Short.MIN_VALUE);
     }
 
     private void mutateByteFieldAndCompare(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity)
             throws IllegalAccessException, InvocationTargetException {
         mutateWithSetByte(alreadySerializedEntities, field, entityToMutate, serializableEntity, (byte) 1);
         mutateWithSetByte(alreadySerializedEntities, field, entityToMutate, serializableEntity, (byte) 2);
         mutateWithSetByte(alreadySerializedEntities, field, entityToMutate, serializableEntity, (byte) -1);
         mutateWithSetByte(alreadySerializedEntities, field, entityToMutate, serializableEntity, Byte.MAX_VALUE);
         mutateWithSetByte(alreadySerializedEntities, field, entityToMutate, serializableEntity, Byte.MIN_VALUE);
     }
 
 
     private void mutateLongFieldAndCompare(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity)
             throws IllegalAccessException, InvocationTargetException {
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 1);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 2);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 100);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 255);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256 * 255);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256 * 256 * 255);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256L * 256 * 256 * 255);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256L * 256 * 256 * 256 * 255);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256L * 256 * 256 * 256 * 256 * 255);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, 256L * 256 * 256 * 256 * 256 * 256 * 255);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, Integer.MIN_VALUE);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, Integer.MAX_VALUE);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, Long.MAX_VALUE);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, Long.MIN_VALUE);
         mutateWithSetLong(alreadySerializedEntities, field, entityToMutate, serializableEntity, -1);
     }
 
     private void mutateWithSetInt(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity, int newVal)
             throws IllegalAccessException, InvocationTargetException {
         field.setInt(entityToMutate, newVal);
         serializeAndCompareAssertNoEquality(serializableEntity, alreadySerializedEntities, field, Integer.toString(newVal));
     }
 
     private void mutateWithSetBoolean(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity, boolean newVal)
             throws IllegalAccessException, InvocationTargetException {
         field.setBoolean(entityToMutate, newVal);
         serializeAndCompareAssertNoEquality(serializableEntity, alreadySerializedEntities, field, Boolean.toString(newVal));
     }
 
     private void mutateWithSetByte(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity, byte newVal)
             throws IllegalAccessException, InvocationTargetException {
         field.setByte(entityToMutate, newVal);
         serializeAndCompareAssertNoEquality(serializableEntity, alreadySerializedEntities, field, Byte.toString(newVal));
     }
 
     private void mutateWithSetShort(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity, short newVal)
             throws IllegalAccessException, InvocationTargetException {
         field.setShort(entityToMutate, newVal);
         serializeAndCompareAssertNoEquality(serializableEntity, alreadySerializedEntities, field, Short.toString(newVal));
     }
 
     private void mutateWithSetLong(List<byte[]> alreadySerializedEntities, Field field, Object entityToMutate, E serializableEntity, long newVal)
             throws IllegalAccessException, InvocationTargetException {
         field.setLong(entityToMutate, newVal);
         serializeAndCompareAssertNoEquality(serializableEntity, alreadySerializedEntities, field, Long.toString(newVal));
     }
 
     private void serializeAndCompareAssertNoEquality(E entity, List<byte[]> alreadySerializedEntities, Field field, String newValue) {
         byte[] serializedNew = entity.toBytes();
         assertEquals("Entity should not change length", entity.getSize(), serializedNew.length);
         for (byte[] alreadySerializedEntity : alreadySerializedEntities) {
             assertFalse("Serialized entity cannot remain equal after field " + field.getName() + " has been changed to " + newValue, Arrays.equals(alreadySerializedEntity, serializedNew));
         }
         alreadySerializedEntities.add(serializedNew);
     }
 
     private Object deepCopy(Object object) {
         try {
             ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos);
             oos.writeObject(object);
 
             ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
             ObjectInputStream ois = new ObjectInputStream(bais);
             return ois.readObject();
         } catch (IOException e) {
             return null;
         } catch (ClassNotFoundException e) {
             return null;
         }
     }
 
}
