 package model.util;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.commons.codec.binary.Base64;
 
 public class Base64Util {
 
 	public static String decode(String text) {
 		return new String(new Base64().decode(text));
 	}
 
 	public static File encodeToFile(File file) throws IOException, InterruptedException {
 		File encoded = File.createTempFile("encoded", ".base64");
 		BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(encoded));
 		RandomAccessFile f = new RandomAccessFile(file, "r");
 		byte[] b = new byte[(int)f.length()];
 		f.read(b);
 		bos.write(new Base64().encode(b));
 		f.close();
 		bos.flush();
 		bos.close();
 		return encoded;
 	}
 
 	public static File decodeUsingOS(String text) throws IOException, InterruptedException {
		byte[] decoded = new Base64().decode(text.getBytes());
        File out = File.createTempFile("encodedImage", ".jpg");
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(out));
        bos.write(decoded);
        bos.close();
        return out;	}
 
 }
