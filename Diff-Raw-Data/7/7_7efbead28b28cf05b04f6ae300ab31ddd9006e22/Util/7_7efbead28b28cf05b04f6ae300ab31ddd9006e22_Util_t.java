 package org.jackie.test.jclassfile;
 
 import static org.jackie.utils.Assert.NOTNULL;
 import org.jackie.utils.Assert;
 import org.jackie.utils.ByteArrayDataInput;
 import org.jackie.utils.Log;
 import org.jackie.utils.IOHelper;
 import org.jackie.jclassfile.model.ClassFile;
 import org.jackie.jclassfile.constantpool.ConstantPool;
 import org.jackie.jclassfile.constantpool.Constant;
 
 import java.io.IOException;
 import java.io.DataInputStream;
 import java.io.FileOutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 
 /**
  * @author Patrik Beno
  */
 public class Util {
 
 	static public byte[] getByteCode(Class cls) {
		DataInputStream in = null;
 		try {
 			String sname = cls.getName();
 			sname = sname.substring(sname.lastIndexOf('.')+1);
 			URL url = NOTNULL(cls.getResource(sname + ".class"));
 			URLConnection con = url.openConnection();
 			byte[] bytes = new byte[con.getContentLength()];
			in = new DataInputStream(con.getInputStream());
 			in.readFully(bytes);
 			return bytes;
 		} catch (IOException e) {
 			throw Assert.unexpected(e);
		} finally {
			IOHelper.close(in);
 		}
 	}
 
 	static public void validate(String clsname, byte[] bytes) {
 		boolean ok = true;
 		try {
 			parseClassFile(bytes);
 		} catch (Exception e) {
 			e.printStackTrace();
 			ok = false;
 		}
 		try {
 			loadInClassLoader(clsname, bytes);
 		} catch (Exception e) {
 			e.printStackTrace();
 			ok = false;
 		}
 		Assert.expected(true, ok, "not ok! (%s)", clsname);
 	}
 
 	static public ClassFile parseClassFile(final byte[] bytes) {
 		try {
 			ClassFile cf = new ClassFile();
 			cf.load(new ByteArrayDataInput(bytes));
 			return cf;
 		} catch (Exception e) {
 			throw Assert.assertFailed(e);
 		}
 	}
 
 	static public void loadInClassLoader(final String name, final byte[] bytes) {
 		new ClassLoader(Thread.currentThread().getContextClassLoader()) {{
 			try {
 				Class<?> cls = defineClass(name, bytes, 0, bytes.length);
 				cls.getDeclaredFields();
 				cls.getDeclaredMethods();
 				cls.getConstructors();
 			} catch (Exception e) {
 				throw Assert.assertFailed(e);
 			}
 		}};
 	}
 
 	static public void dumpConstantPool(ConstantPool pool) {
 		Log.info("Dumping ConstantPool:");
 		for (Constant c : pool) {
 			Log.info("\t%s", c);
 		}
 	}
 
 	static public void save(byte[] bytes, String fileName) {
 		FileOutputStream out = null;
 		try {
 			out = new FileOutputStream(fileName);
 			out.write(bytes);
 		} catch (IOException e) {
 			throw Assert.notYetHandled(e);
 		} finally {
 			IOHelper.close(out);
 		}
 	}
 }
