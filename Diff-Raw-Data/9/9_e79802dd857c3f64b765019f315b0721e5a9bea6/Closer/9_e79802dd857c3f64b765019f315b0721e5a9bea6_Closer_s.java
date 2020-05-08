 package hochberger.utilities.files;
 
 import java.io.Closeable;
 import java.io.IOException;
 
 public class Closer {
 
 	public Closer() {
 		super();
 	}
 
 	public static void close(Closeable closeable) {
 		if (null == closeable) {
 			return;
 		}
 		try {
 			closeable.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
