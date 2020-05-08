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
 
 package ca.eandb.util.io;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * An <code>InputStream</code> decorator that restricts the available data to
  * the specified number of bytes.
  * 
  * @author Brad Kimmel
  */
 public final class LimitedInputStream extends InputStream {
 
 	/** The <code>InputStream</code> to read from. */
 	private final InputStream inner;
 	
 	/** The number of bytes remaining in the stream. */
 	private int remaining;
 	
 	/**
 	 * Creates a new <code>LimitedInputStream</code>.
 	 * @param size The number of bytes available to be read.
 	 * @param inner The <code>InputStream</code> to read from.
 	 */
 	public LimitedInputStream(int size, InputStream inner) {
 		this.inner = inner;
 		this.remaining = size;
 	}
 	
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#read()
 	 */
 	@Override
 	public int read() throws IOException {
 		if (remaining > 0) {
 			remaining--;
 			return inner.read();
 		} else {
 			return -1;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#available()
 	 */
 	@Override
 	public int available() throws IOException {
 		return Math.min(remaining, inner.available());
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#close()
 	 */
 	@Override
 	public void close() throws IOException {
 		inner.close();
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#read(byte[], int, int)
 	 */
 	@Override
 	public int read(byte[] b, int off, int len) throws IOException {
 		int bytes = inner.read(b, off, Math.min(len, remaining));
 		if (bytes > 0) {
 			remaining -= bytes;
 		}
 		return bytes;
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#read(byte[])
 	 */
 	@Override
 	public int read(byte[] b) throws IOException {
 		return read(b, 0, b.length);
 	}
 
 	/* (non-Javadoc)
 	 * @see java.io.InputStream#skip(long)
 	 */
 	@Override
 	public long skip(long n) throws IOException {
 		return n > 0 ? (long) inner.skip(Math.min(n, remaining)) : 0;
 	}
 	
 	/**
 	 * Gets the number of bytes remaining.
 	 * @return The number of bytes remaining.
 	 */
 	public int remaining() {
 		return remaining;
 	}
 	
 	/**
 	 * Skips over the remainder of the available bytes.
 	 * @throws IOException
 	 */
 	public void moveToEnd() throws IOException {
 		int bytes;
		do {
 			bytes = (int) inner.skip(remaining);
 			if (bytes < 0) {
 				break;
 			}
 			remaining -= bytes;
		} while (remaining > 0);
 	}
 
 }
