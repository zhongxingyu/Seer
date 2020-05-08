 /*
  * Copyright (c) 2008 Bradley W. Kimmel
  * 
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  * 
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package ca.eandb.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.DigestOutputStream;
 import java.security.MessageDigest;
 
 import ca.eandb.util.io.NullOutputStream;
 import ca.eandb.util.io.StreamUtil;
 
 /**
  * Utility methods for working with classes.
  * @author Brad Kimmel
  */
 public final class ClassUtil {
 
 	/**
 	 * Gets an <code>InputStream</code> from which the specified class'
 	 * bytecode definition may be read.
 	 * @param cl The <code>Class</code> for which to get the stream.
 	 * @return An <code>InputStream</code> from which the specified class'
 	 * 		bytecode definition may be read.
 	 */
 	public static InputStream getClassAsStream(Class<?> cl) {
		String name = cl.getName();
		int pos = name.lastIndexOf('.');
		String resourceName = name.substring(pos + 1) + ".class";
 		return cl.getResourceAsStream(resourceName);
 	}
 
 	/**
 	 * Writes a class' bytecode to an <code>OutputStream</code>.
 	 * @param cl The <code>Class</code> to write.
 	 * @param out The <code>OutputStream</code> to write to.
 	 * @throws IOException If unable to write to <code>out</code>.
 	 */
 	public static void writeClassToStream(Class<?> cl, OutputStream out) throws IOException {
 		InputStream in = getClassAsStream(cl);
 		StreamUtil.writeStream(in, out);
 		out.flush();
 	}
 
 	/**
 	 * Computes a digest from a class' bytecode.
 	 * @param cl The <code>Class</code> for which to compute the digest.
 	 * @param digest The <code>MessageDigest</code> to update.
 	 */
 	public static void getClassDigest(Class<?> cl, MessageDigest digest) {
 		DigestOutputStream out = new DigestOutputStream(NullOutputStream.getInstance(), digest);
 		try {
 			writeClassToStream(cl, out);
 		} catch (IOException e) {
 			e.printStackTrace();
 			throw new UnexpectedException(e);
 		}
 	}
 	
 	/**
 	 * Gets the outer class (a class with no enclosing class) that contains the
 	 * given class.
 	 * @param cl The <code>Class</code> for which to find the outer class.  If
 	 * 		<code>class_</code> is an outer class, then <code>class_</code> is
 	 * 		returned.
 	 * @return The outer <code>Outer</code> class containing
 	 * 		<code>class_</code>.
 	 */
 	public static Class<?> getOuterClass(Class<?> cl) {
 		Class<?> enclosingClass;
 		while ((enclosingClass = cl.getEnclosingClass()) != null) {
 			cl = enclosingClass;
 		}
 		return cl;
 	}
 
 	/** Declared private to prevent this class from being instantiated. */
 	private ClassUtil() {}
 
 }
