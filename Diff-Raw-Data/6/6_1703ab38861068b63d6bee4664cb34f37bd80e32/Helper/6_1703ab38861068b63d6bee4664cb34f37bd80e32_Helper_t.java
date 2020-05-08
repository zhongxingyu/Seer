 package distributedServices;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.UnsupportedEncodingException;
 
 import microFacebook.Validate;
 
 public class Helper {
 	static public byte[] packToByteArray(Object o) {
 		ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
 		ObjectOutputStream oos;
 		try {
 			oos = new ObjectOutputStream(baos);
 			oos.writeObject(o);
 			oos.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			Validate.unexpectedCodePath();
 		}
 		return baos.toByteArray();
 	}
 	
 	@SuppressWarnings("unchecked")
 	static public <T> T unpack(byte[] data) {
 		ByteArrayInputStream bais = new ByteArrayInputStream(data); 
 		ObjectInputStream ois;
 		Object o = null;
 		try {
 			ois = new ObjectInputStream(bais);
 			o = ois.readObject();
 			ois.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return (T)o;
 	}
 	
 	static public String packToString(Object o) {
 		return byteArrayToString(packToByteArray(o));
 	}
 	
 	static public <T> T unpack(String str) {
 		return unpack(stringToByteArray(str));
 	}
 	
 	static public String byteArrayToString(byte[] data) {
 		try {
 			return new String(data, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	static public byte[] stringToByteArray(String str) {
 		try {
 			byte[] array = str.getBytes("UTF-8");
			
			//
			// Fix the binary stream prefix from the 6 'UTF-8' bytes to the 
			// two 'serializer' bytes.
			//
			
 			array[4] = -84;
 			array[5] = -19;
 			byte[] result = new byte[array.length - 4]; 
 			System.arraycopy(array, 4, result, 0, result.length);
 			return result;
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return null;
 	}
 }
