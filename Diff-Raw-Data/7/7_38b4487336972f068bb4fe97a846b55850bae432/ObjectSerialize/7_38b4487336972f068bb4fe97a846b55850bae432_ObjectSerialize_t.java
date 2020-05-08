 package sandbox;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 public class ObjectSerialize {
 
     public static Object deserialize(byte[] bytes) throws Exception {
         Object result = null;
        ObjectInputStream ois =
             new ObjectInputStream(new ByteArrayInputStream(bytes));
        result = ois.readObject();
        ois.close();
         return result;
     }
 
     public static byte[] serialize(Object o) throws Exception {
         byte[] result = null;
         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         ObjectOutputStream oos = new ObjectOutputStream(baos);
         oos.writeObject(o);
         oos.flush();
         result = baos.toByteArray();
         oos.close();
         return result;
     }
 
     public static Object deepClone(Object o) throws Exception {
         return deserialize(serialize(o));
     }
 
 }
