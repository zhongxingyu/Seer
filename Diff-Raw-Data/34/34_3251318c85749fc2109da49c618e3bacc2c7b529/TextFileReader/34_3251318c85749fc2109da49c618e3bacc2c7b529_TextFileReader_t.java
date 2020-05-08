 /*
  * Copyright 2013 Eediom Inc.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.log.api;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
import java.io.RandomAccessFile;
 
 public class TextFileReader {
 	private String charsetName;
	private RandomAccessFile raf;
 	private ByteArrayOutputStream bos = new ByteArrayOutputStream();
 
 	private byte[] buf = new byte[80960];
 	private int bufpos = 0;
 	private int buflen = 0;
 	private long readBytes;
 	private boolean closed;
 	private final boolean needLastNewLine;
 
 	public TextFileReader(File file, long offset, String charsetName) throws IOException {
 		this(file, offset, charsetName, false);
 	}
 
 	public TextFileReader(File file, long offset, String charsetName, boolean needLastNewLine) throws IOException {
 		this.charsetName = charsetName;
		this.raf = new RandomAccessFile(file, "r");
		this.raf.seek(offset);
 		this.readBytes += offset;
 		this.needLastNewLine = needLastNewLine;
 	}
 
 	public String readLine() throws IOException {
 		if (closed)
 			throw new IOException("Already closed");
 
 		long bytes = 0;
		if (buflen < 0)
			buflen = 0;

 		bos.reset();
 		String line = null;
 		boolean lineEnd = false;
 		while (!lineEnd) {
 			if (buflen < 0) {
 				if (bos.size() == 0)
 					return null;
				else if (needLastNewLine) {
					// back file pointer to last newline
					raf.seek(readBytes);
					raf.getFD().sync();
 					return null;
				} else
 					return bos.toString(charsetName);
 			}
 
 			while (true) {
				if (bufpos >= buflen) {
					buflen = raf.read(buf, 0, buf.length);
 					if (buflen < 0)
 						break;
 
 					bufpos = 0;
 				}
 
 				boolean found = false;
 				int begin = bufpos;
 				while (bufpos < buflen) {
 					byte b = buf[bufpos++];
 					bytes++;
 					if (b == 0xa) {
 						found = true;
 						break;
 					}
 				}
 
 				int len = bufpos - begin;
 				bos.write(buf, begin, len);
 				if (found)
 					break;
 			}
 
 			if (bos.size() == 0) {
 				if (!needLastNewLine) {
 					readBytes += bytes;
 					bytes = 0;
 				}
 				return null;
 			}
 
 			line = bos.toString(charsetName);
 			lineEnd = line.endsWith("\n");
 			if (lineEnd || !needLastNewLine) {
 				readBytes += bytes;
 				bytes = 0;
 			}
 		}
 
 		int len = line.length();
 
 		if (line.endsWith("\r\n"))
 			return line.substring(0, len - 2);
 		return line.substring(0, len - 1);
 	}
 
 	public long getPosition() throws IOException {
 		if (closed)
 			throw new IOException("Already closed");
 		return readBytes;
 	}
 
 	public void close() {
 		if (closed)
 			return;
 
 		closed = true;
 		buf = null;
 		bos = null;
 
 		try {
			raf.close();
			raf = null;
 		} catch (IOException e) {
 		}
 	}
 }
