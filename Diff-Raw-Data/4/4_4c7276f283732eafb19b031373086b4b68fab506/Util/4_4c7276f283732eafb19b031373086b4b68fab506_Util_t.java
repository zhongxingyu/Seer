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
			String sname = cls.getName();
			sname = sname.substring(sname.lastIndexOf('.')+1);
			URL url = NOTNULL(cls.getResource(sname + ".class"));
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
