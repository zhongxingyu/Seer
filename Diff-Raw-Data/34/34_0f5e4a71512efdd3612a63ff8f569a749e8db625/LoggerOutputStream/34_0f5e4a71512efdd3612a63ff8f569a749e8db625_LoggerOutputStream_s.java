 /**
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package net.sf.springderby;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 import java.nio.charset.CoderResult;
 
 import org.apache.commons.logging.Log;
 
 /**
  * {@link OutputStream} implementation that redirects to a logger.
  * 
  * @author Andreas Veithen
  */
 // TODO: strip trailing '\r' on Windows platforms
 public class LoggerOutputStream extends OutputStream {
 	private final Log log;
 	private final CharsetDecoder decoder;
 	private final ByteBuffer decoderIn = ByteBuffer.allocate(128);
 	private final CharBuffer decoderOut = CharBuffer.allocate(128);
 	private final StringBuffer lineBuffer = new StringBuffer();
 	
 	public LoggerOutputStream(Log log, Charset charset) {
 		this.log = log;
 		decoder = charset.newDecoder();
 	}
 
 	public LoggerOutputStream(Log log, String charset) {
 		this(log, Charset.forName(charset));
 	}
 
 	public void write(byte[] bytes, int offset, int length) throws IOException {
 		while (length > 0) {
 			int c = Math.min(length, decoderIn.remaining());
			decoderIn.put(bytes, offset, c).flip();
 			processInput(false);
			decoderIn.compact();
 			length -= c;
 			offset += c;
 		}
 	}
 
 	public void write(byte[] bytes) throws IOException {
 		write(bytes, 0, bytes.length);
 	}
 
 	public void write(int b) throws IOException {
 		write(new byte[] { (byte)b }, 0, 1);
 	}
 	
 	public void close() throws IOException {
 		processInput(true);
 		if (lineBuffer.length() > 0) {
 			flushLineBuffer();
 		}
 	}
 	
 	private void processInput(boolean endOfInput) throws IOException {
 		CoderResult coderResult;
 		do {
 			coderResult = decoder.decode(decoderIn, decoderOut, endOfInput);
 			if (coderResult.isError()) {
 				throw new IOException("Character set decoding error: " + coderResult);
 			}
 			int outLength = decoderOut.position();
 			char[] outArray = decoderOut.array();
 			int start = 0;
 			for (int i=0; i<outLength; i++) {
 				if (outArray[i] == '\n') {
 					lineBuffer.append(outArray, start, i-start);
 					flushLineBuffer();
 					start = i+1;
 				}
 			}
 			lineBuffer.append(outArray, start, outLength-start);
 			decoderOut.rewind();
 		} while (coderResult.isOverflow());
 	}
 	
 	private void flushLineBuffer() throws UnsupportedEncodingException {
 		log.info(lineBuffer.toString());
 		lineBuffer.setLength(0);
 	}
 }
