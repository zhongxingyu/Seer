 
 package org.paxle.util;
 
 import java.io.OutputStream;
import java.nio.ByteBuffer;
 import java.nio.charset.Charset;
 
 public class ArrayByteBuffer extends OutputStream implements Cloneable {
 	
 	private static final int INC_BUF_BYTES = 8;
 	
 	public static ArrayByteBuffer wrap(final byte[] b) {
 		return new ArrayByteBuffer(b, b.length);
 	}
 	
 	public static ArrayByteBuffer wrap(final byte[] b, final int len) {
 		return new ArrayByteBuffer(b, len);
 	}
 	
 	protected byte[] buf;
 	protected int len;
 	
 	public ArrayByteBuffer() {
 		this(INC_BUF_BYTES);
 	}
 	
 	public ArrayByteBuffer(final int len) {
 		buf = new byte[len];
 	}
 	
 	public ArrayByteBuffer(final byte[] buf) {
 		this(buf, 0, buf.length);
 	}
 	
 	public ArrayByteBuffer(final byte[] buf, final int off, final int len) {
 		this.buf = new byte[len];
 		System.arraycopy(buf, off, this.buf, 0, len);
 		this.len = len;
 	}
 	
 	private ArrayByteBuffer(final byte[] buf, final int len) {
 		if (len > buf.length)
 			throw new IndexOutOfBoundsException("len > buf.length: " + len);
 		this.buf = buf;
 		this.len = len;
 	}
 	
 	@Override
 	public ArrayByteBuffer clone() {
 		try {
 			final ArrayByteBuffer bb = (ArrayByteBuffer)super.clone();
 			bb.buf = new byte[buf.length];
 			System.arraycopy(buf, 0, bb.buf, 0, buf.length);
 			bb.len = len;
 			return bb;
 		} catch (CloneNotSupportedException e) {
 			throw new InternalError("clone not supported: " + e.getMessage());
 		}
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof ArrayByteBuffer))
 			return super.equals(obj);
 		final ArrayByteBuffer bb = (ArrayByteBuffer)obj;
 		final int len = this.len;
 		if (len != bb.len)
 			return false;
 		final byte[] buf = this.buf, bbbuf = bb.buf;
 		for (int i=0; i<len; i++)
 			if (buf[i] != bbbuf[i])
 				return false;
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		return new String(buf, 0, len);
 	}
 	
 	protected void checkLength(final int additional) {
 		final int add = len + additional - buf.length;
 		if (add > 0) {
 			final byte[] n = new byte[buf.length + Math.max(add, INC_BUF_BYTES)];
 			System.arraycopy(buf, 0, n, 0, len);
 			buf = n;
 		}
 	}
 	
 	/* --------------------------------------------------------------------
 	 * GENERAL METHODS
 	 * -------------------------------------------------------------------- */
 	
 	public byte[] toByteArray() {
 		return toByteArray(0, len);
 	}
 	
 	public byte[] toByteArray(final int off, final int num) {
 		final byte[] r = new byte[num];
 		System.arraycopy(buf, off, r, 0, num);
 		return r;
 	}
 	
 	public String toString(final Charset cs) {
		return cs.decode(ByteBuffer.wrap(buf, 0, len)).toString();
 	}
 	
 	public byte[] getBuffer() {
 		return buf;
 	}
 	
 	public int size() {
 		return len;
 	}
 	
 	public int capacity() {
 		return buf.length;
 	}
 	
 	public void clear() {
 		len = 0;
 	}
 	
 	/* --------------------------------------------------------------------
 	 * OUTPUTSTREAM METHODS
 	 * --------------------------------------------------------------------  */
 	
 	@Override
 	public void write(byte[] b, int off, int len) {
 		append(b, off, len);
 	}
 	
 	@Override
 	public void write(byte[] b) {
 		append(b, 0, b.length);
 	}
 	
 	@Override
 	public void write(int b) {
 		append((byte)(b & 0xFF));
 	}
 	
 	@Override
 	public void close() {
 	}
 	
 	@Override
 	public void flush() {
 	}
 	
 	/* --------------------------------------------------------------------
 	 * APPEND METHODS
 	 * --------------------------------------------------------------------  */
 	
 	public ArrayByteBuffer append(final byte b) {
 		checkLength(1);
 		buf[len++] = b;
 		return this;
 	}
 	
 	public ArrayByteBuffer append(final byte[] b) {
 		return append(b, 0, b.length);
 	}
 	
 	public ArrayByteBuffer append(final byte[] b, final int off, final int len) {
 		checkLength(len);
 		if (off < 0 || off + len > b.length)
 			throw new IndexOutOfBoundsException("off: " + off + ", len: " + len);
 		System.arraycopy(b, off, buf, this.len, len);
 		this.len += len;
 		return this;
 	}
 	
 	/* --------------------------------------------------------------------
 	 * INDEX OF METHODS
 	 * --------------------------------------------------------------------  */
 	
 	public int indexOfBNDM(final byte[] b) {
 		return indexOfBNDM(b, 0, b.length, 0);
 	}
 	
 	public int indexOfBNDM(final byte[] b, final int from) {
 		return indexOfBNDM(b, 0, b.length, from);
 	}
 	
 	/**
 	 * Better for long patterns and/or much data to search
 	 * @see <a href="http://www.dcc.uchile.cl/~gnavarro/ps/cpm98.ps.gz">Backward Nondeterministic Dawg Matching</a>
 	 */
 	public int indexOfBNDM(final byte[] b, final int off, final int num, final int from) {
 		return (num > Integer.SIZE) ? indexOfBNDM64(b, off, num, from) : indexOfBNDM32(b, off, num, from);
 	}
 	
 	private int indexOfBNDM32(final byte[] b, final int off, final int m, final int from) {
 		// pre-process
 		if (m > Integer.SIZE)
 			throw new UnsupportedOperationException("cannot process patterns with " + m + " elements");
 		final int mMask = (1 << m) - 1;
 		final int dMask = 1 << (m - 1);
 		final int[] bitmask = new int[256];
 		for (int i=off; i<m; i++)
 			bitmask[b[i] & 0xFF] |= 1 << (m - i - 1);
 		
 		// search
 		final int n = len - from;
 		final int lastPos = n - m;
 		
 		int pos = from, j, last;
 		int d;
 		while (pos <= lastPos) {
 			j = m;
 			last = m;
 			d = mMask;
 			do {
 				d &= bitmask[buf[pos + --j] & 0xFF];
 				if ((d & dMask) != 0) {
 					if (j > 0) {
 						last = j;
 					} else {
 						return pos;
 					}
 				}
 				d <<= 1;
 			} while ((d & mMask) != 0);
 			pos += last;
 		}
 		return -1;
 	}
 	
 	private int indexOfBNDM64(final byte[] b, final int off, final int m, final int from) {
 		// pre-process
 		if (m > Long.SIZE)
 			throw new UnsupportedOperationException("cannot process patterns with " + m + " elements");
 		final long mMask = (1L << m) - 1;
 		final long dMask = 1L << (m - 1);
 		final long[] bitmask = new long[256];
 		for (int i=off; i<m; i++)
 			bitmask[b[i] & 0xFF] |= 1L << (m - i - 1);
 		
 		// search
 		final int n = len - from;
 		final int lastPos = n - m;
 		
 		int pos = from, j, last;
 		long d;
 		while (pos <= lastPos) {
 			j = m;
 			last = m;
 			d = mMask;
 			do {
 				d &= bitmask[buf[pos + --j] & 0xFF];
 				if ((d & dMask) != 0) {
 					if (j > 0) {
 						last = j;
 					} else {
 						return pos;
 					}
 				}
 				d <<= 1;
 			} while ((d & mMask) != 0);
 			pos += last;
 		}
 		return -1;
 	}
 	
 	public int indexOf(final byte[] b) {
 		return indexOf(b, 0, b.length, 0);
 	}
 	
 	public int indexOf(final byte[] b, final int from) {
 		return indexOf(b, 0, b.length, from);
 	}
 	
 	public int indexOf(final byte[] b, final int off, final int num, final int from) {
 		outer: for (int i=0; i<num; i++) {
 			if (buf[from + i] == b[off + i]) {
 				for (int j=1; j<num; j++)
 					if (buf[from + j] != b[off + j])
 						continue outer;
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	/* --------------------------------------------------------------------
 	 * REPLACE METHODS
 	 * --------------------------------------------------------------------  */
 	
 	public ArrayByteBuffer replace(final byte[] b, final int from) {
 		return replace(b, 0, b.length, from, from + b.length);
 	}
 	
 	public ArrayByteBuffer replace(final byte[] b, final int off, final int len, final int from) {
 		return replace(b, off, len, from, from + len);
 	}
 	
 	public ArrayByteBuffer replace(final byte[] b, final int from, final int to) {
 		return replace(b, 0, b.length, from, to);
 	}
 	
 	public ArrayByteBuffer replace(final byte[] b, final int off, final int num, final int from, final int to) {
 		if (off < 0 || off + num > b.length)
 			throw new IndexOutOfBoundsException("off: " + off + ", num: " + num);
 		if (from < 0 || to < from || to > len)
 			throw new IndexOutOfBoundsException("from: " + from  + ", to: " + to + ", len: " + len);
 		
 		final int delta = to - from;
 		final int add = num - delta;
 		if (add == 0) {
 			System.arraycopy(b, off, buf, from, num);				// directly replace bytes in buf with b
 		} else if (add < 0) {
 			System.arraycopy(b, off, buf, from, num);				// copy b into buf
 			System.arraycopy(buf, to, buf, to + add, len - to);		// move bytes in buf from 'to' to replacement end @ to + add (== from + num)
 		} else {
 			checkLength(add);										// ensure additional space
 			System.arraycopy(buf, to, buf, to + add, len - to);		// move bytes in buf from 'to' to replacement end @ to + add
 			System.arraycopy(b, off, buf, from, num);				// copy b into buf
 		}
 		len += add;
 		return this;
 	}
 	
 	/* --------------------------------------------------------------------
 	 * REMOVE METHODS
 	 * -------------------------------------------------------------------- */
 	
 	public ArrayByteBuffer removeBytesAt(final int idx, final int num) {
 		if (idx < 0 || idx > len)
 			throw new IndexOutOfBoundsException("invalid index: " + idx + " (len: " + len + ")");
 		final int loff = idx + num;
 		if (num < 0 || loff > len)
 			throw new IndexOutOfBoundsException("invalid num: " + num + ", index: " + idx + " (len: " + len + ")");
 		
 		if (loff == len - 1) {
 			return removeLastBytes(num);
 		} else {
 			System.arraycopy(buf, idx + num, buf, idx, num);
 			len -= num;
 			return this;
 		}
 	}
 	
 	public ArrayByteBuffer removeLastBytes(final int num) {
 		if (len < num)
 			throw new IndexOutOfBoundsException("len < num, len: " + len + ", num: " + num);
 		len -= num;
 		return this;
 	}
 	
 	public ArrayByteBuffer removeByteAt(final int idx) {
 		if (idx < 0 || idx > len)
 			throw new IndexOutOfBoundsException("invalid index: " + idx + " (len: " + len + ")");
 		
 		if (idx == len - 1) {
 			return removeLast();
 		} else {
 			System.arraycopy(buf, idx + 1, buf, idx, len - idx);
 			len--;
 			return this;
 		}
 	}
 	
 	public ArrayByteBuffer removeLast() {
 		if (len == 0)
 			throw new IndexOutOfBoundsException("len == 0");
 		len--;
 		return this;
 	}
 }
