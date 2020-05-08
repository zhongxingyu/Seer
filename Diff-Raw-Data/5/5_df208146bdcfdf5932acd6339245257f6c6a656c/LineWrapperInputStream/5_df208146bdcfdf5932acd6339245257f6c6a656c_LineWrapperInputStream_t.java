 package net.northfuse.resources;
 
 import java.io.*;
 
 /**
  * @author tylers2
  */
 public class LineWrapperInputStream extends InputStream {
 	private final InputStream is;
 
 	public LineWrapperInputStream(InputStream is, String description) throws IOException {
 		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 		String line;
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		PrintWriter writer = new PrintWriter(baos);
 		int lineNumber = 0;
 		boolean inComment = false;
 		while ((line = reader.readLine()) != null) {
 			if (inComment) {
				writer.println("   " + description + ":" + (++lineNumber) + "   " + line);
 			} else {
				writer.println("/* " + description + ":" + (++lineNumber) + " */" + line);
 			}
 			if (!inComment) {
 				int commentIndex = line.lastIndexOf("/*");
 				if (commentIndex > -1) {
 					if (commentIndex > 0) {
 						//is the comment start inside of a string?
 						if (countOccurences(line.substring(0, commentIndex), "\"") % 2 == 1) {
 							continue;
 						}
 					}
 					if (!line.substring(commentIndex).contains("*/")) {
 						inComment = true;
 					}
 				}
 			} else {
 				int commentIndex = line.lastIndexOf("*/");
 				if (commentIndex > -1) {
 					if (!line.substring(commentIndex).contains("/*")) {
 						inComment = false;
 					}
 				}
 			}
 		}
 		writer.close();
 		//get rid of the last new line
 		byte[] data = baos.toByteArray();
 		this.is = new ByteArrayInputStream(data, 0, data.length - 1);
 	}
 
 	private int countOccurences(String s, String pattern) {
 		int count = 0;
 		for (char c : s.toCharArray()) {
 			if (c == pattern.charAt(0)) {
 				count++;
 			}
 		}
 		return count;
 	}
 
 	private int doCountOccurences(String s, String pattern, int count) {
 		int index = s.indexOf(pattern);
 		if (index == -1) {
 			return count;
 		} else {
 			return doCountOccurences(s.substring(index + 1), pattern, count + 1);
 		}
 	}
 
 	@Override
 	public int read() throws IOException {
 		return is.read();
 	}
 
 	@Override
 	public int read(byte[] b) throws IOException {
 		return is.read(b);
 	}
 
 	@Override
 	public int read(byte[] b, int off, int len) throws IOException {
 		return is.read(b, off, len);
 	}
 
 	@Override
 	public long skip(long n) throws IOException {
 		return is.skip(n);
 	}
 
 	@Override
 	public int available() throws IOException {
 		return is.available();
 	}
 
 	@Override
 	public void close() throws IOException {
 		is.close();
 	}
 
 	@Override
 	public void mark(int readlimit) {
 		is.mark(readlimit);
 	}
 
 	@Override
 	public void reset() throws IOException {
 		is.reset();
 	}
 
 	@Override
 	public boolean markSupported() {
 		return is.markSupported();
 	}
 }
