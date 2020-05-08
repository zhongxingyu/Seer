 package churndb.utils;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.FileUtils;
 
 public class ResourceUtils {
 
 	public static String asString(String uri) {
 		try {
 			return FileUtils.readFileToString(asFile(uri));
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static File asFile(String uri) {
 		return FileUtils.toFile(getResourceURL(uri));
 	}
 
 	private static URL getResourceURL(String uri) {
 		return ResourceUtils.class.getResource(uri);
 	}
 
 	public static String asBase64(String uri) {
 		return asBase64(asFile(uri));
 	}
 
 	public static String asBase64(File file) {
 		try {
			return Base64.encodeBase64String(FileUtils.readFileToByteArray(file));
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
