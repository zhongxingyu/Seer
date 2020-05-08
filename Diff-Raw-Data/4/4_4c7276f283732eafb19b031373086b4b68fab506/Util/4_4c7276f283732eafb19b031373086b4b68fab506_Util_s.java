 package org.jackie.test.jclassfile;
 
 import static org.jackie.utils.Assert.NOTNULL;
 import org.jackie.utils.Assert;
 
 import java.io.IOException;
 import java.io.DataInputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * @author Patrik Beno
  */
 public class Util {
 
 	static public byte[] getByteCode(Class cls) {
 		try {
			URL url = NOTNULL(cls.getResource(cls.getSimpleName() + ".class"));
 			URLConnection con = url.openConnection();
 			byte[] bytes = new byte[con.getContentLength()];
 			DataInputStream in = new DataInputStream(con.getInputStream());
 			in.readFully(bytes);
 			return bytes;
 		} catch (IOException e) {
 			throw Assert.unexpected(e);
 		}
 	}
 
 }
