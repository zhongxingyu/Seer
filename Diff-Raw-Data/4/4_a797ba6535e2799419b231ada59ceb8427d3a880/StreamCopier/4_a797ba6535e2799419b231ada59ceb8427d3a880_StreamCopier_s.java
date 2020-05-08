 /*
  * utils - Closer.java - Copyright © 2006-2010 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.io;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 /**
  * Helper class that copies bytes from an {@link InputStream} to an
  * {@link OutputStream}.
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class StreamCopier {
 
 	/** Default buffer size is 64k. */
 	private static final int DEFAULT_BUFFER_SIZE = 1 << 16;
 
 	/** The current buffer size. */
 	private static int bufferSize = DEFAULT_BUFFER_SIZE;
 
 	/**
 	 * Sets the buffer size for following transfers.
 	 *
 	 * @param bufferSize
 	 *            The new buffer size
 	 */
 	public static void setBufferSize(int bufferSize) {
 		StreamCopier.bufferSize = bufferSize;
 	}
 
 	/**
 	 * Copies <code>length</code> bytes from the source input stream to the
 	 * destination output stream. If <code>length</code> is <code>-1</code> as
 	 * much bytes as possible will be copied (i.e. until
 	 * {@link InputStream#read()} returns <code>-1</code> to signal the end of
 	 * the stream).
 	 *
 	 * @param source
 	 *            The input stream to read from
 	 * @param destination
 	 *            The output stream to write to
 	 * @param length
 	 *            The number of bytes to copy
 	 * @return The number of bytes that have been read from the input stream and
 	 *         written to the output stream
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public static long copy(InputStream source, OutputStream destination, long length) throws IOException {
 		long remaining = length;
 		byte[] buffer = new byte[bufferSize];
 		long total = 0;
 		int read = 0;
 		while ((remaining == -1) || (remaining > 0)) {
 			read = source.read(buffer, 0, ((remaining > bufferSize) || (remaining == -1)) ? bufferSize : (int) remaining);
 			if (read == -1) {
 				if (length == -1) {
 					return total;
 				}
 				throw new EOFException("stream reached eof");
 			}
 			destination.write(buffer, 0, read);
			remaining -= read;
 			total += read;
 		}
 		return total;
 	}
 
 	/**
 	 * Copies as much bytes as possible (i.e. until {@link InputStream#read()}
 	 * returns <code>-1</code>) from the source input stream to the destination
 	 * output stream.
 	 *
 	 * @param source
 	 *            The input stream to read from
 	 * @param destination
 	 *            The output stream to write to
 	 * @return The number of bytes that have been read from the input stream and
 	 *         written to the output stream
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public static long copy(InputStream source, OutputStream destination) throws IOException {
 		return copy(source, destination, -1);
 	}
 
 	/**
 	 * Finds the length of the input stream by reading until
 	 * {@link InputStream#read(byte[])} returns <code>-1</code>.
 	 *
 	 * @param source
 	 *            The input stream to measure
 	 * @return The length of the input stream in bytes
 	 * @throws IOException
 	 *             if an I/O error occurs
 	 */
 	public static long findLength(InputStream source) throws IOException {
 		long length = 0;
 		byte[] buffer = new byte[bufferSize];
 		int read = 0;
 		while (read != -1) {
 			read = source.read(buffer);
 			if (read != -1) {
 				length += read;
 			}
 		}
 		return length;
 	}
 
 }
