 package org.araqne.log.api;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.channels.ByteChannel;
 
 public class ByteBufferReader {
 	private ByteChannel channel;
 
 	public ByteBufferReader(ByteChannel channel) {
 		this.channel = channel;
 		buf = ByteBuffer.allocate(32768);
 		buf.position(buf.limit());
 	}
 
 	boolean skipLF;
 	int nextChar;
 	private ByteBuffer buf = ByteBuffer.allocate(32768);
 
 	public ByteBuffer readLine() throws IOException {
 		return readLine(true);
 	}
 
 	public ByteBuffer readLine(boolean ignoreLF) throws IOException {
 		ensureOpen();
 		boolean omitLF = ignoreLF || skipLF;
 		ByteArrayOutputStream b = null;
 
 		for (;;) { // bufferloop
 			if (!buf.hasRemaining())// if (nextChar >= nChars)
 				fill();
 			if (!buf.hasRemaining()) { /* EOF */
 				if (b != null && b.size() > 0)
 					return ByteBuffer.wrap(b.toByteArray()).asReadOnlyBuffer();
 				else
 					return null;
 			}
 			boolean eol = false;
 			byte c = 0;
 			int i;
 
 			/* Skip a leftover '\n', if necessary */
 			if (omitLF && (buf.get(buf.position()) == '\n'))
 				buf.get();
 			skipLF = false;
 			omitLF = false;
 
 			// charloop
 			int oldPos = buf.position();
 			while (buf.hasRemaining()) {
 				c = buf.get();
 				if ((c == '\n') || (c == '\r')) {
 					eol = true;
 					break;
 				}
 			}
 			
 			if (eol) {
 				ByteBuffer ret = null;
 				if (b == null) {
 					ret = buf.duplicate();
 					ret.limit(buf.position() - 1);
 					ret.position(oldPos);
 					ret = ret.asReadOnlyBuffer();
 				} else {
 					b.write(buf.array(), oldPos, buf.position() - 1 - oldPos);
 					ret = ByteBuffer.wrap(b.toByteArray()).asReadOnlyBuffer();
 				}
 //				nextChar++;
 				if (c == '\r') {
 					skipLF = true;
 				}
 				return ret;
 			}
 
 			if (b == null)
 				b = new ByteArrayOutputStream(1024);
 			b.write(buf.array(), oldPos, buf.position() - oldPos);
 		}
 
 	}
 
 	private void fill() throws IOException {
 		// buf has no remaining
 		buf.limit(buf.capacity());
 		buf.position(0);
 		int read = channel.read(buf);
 		buf.flip();
 	}
 
 	private void ensureOpen() throws IOException {
 		if (channel == null)
 			throw new IOException("Stream closed");
 	}
 }
