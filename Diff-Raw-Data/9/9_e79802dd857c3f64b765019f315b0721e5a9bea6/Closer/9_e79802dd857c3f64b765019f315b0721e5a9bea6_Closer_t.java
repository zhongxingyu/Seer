 package hochberger.utilities.files;
 
 import java.io.Closeable;
 import java.io.IOException;
import java.util.Scanner;
 
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

	public static void close(Scanner closeable) {
		if (null == closeable) {
			return;
		}
		closeable.close();
	}
 }
