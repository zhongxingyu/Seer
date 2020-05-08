 package net.timendum.denis.rfc;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.regex.Pattern;
 
 import net.timendum.denis.DomainNameCompressor;
 import net.timendum.denis.io.ArrayDataInputStream;
 import net.timendum.denis.io.ArrayDataOutputStream;
 
 
 /**
  * A representation of a domain name.
  *
  * @author timendum
  */
 public class DomainName implements Cloneable {
 	public static final char SEPARATOR = '.';
 	private static final Pattern REGEXP = Pattern.compile("\\.");
 	/**
 	 * the labels (at least 2)
 	 */
 	private String[] labels;
 	/**
 	 * labels joined by dots
 	 */
 	private String domainName;
 	/**
 	 * the length of the output in bytes
 	 */
 	private int length;
 
 	/**
 	 * Create a DomainName from its labels
 	 * @param labels	the labels of the domain name
 	 */
 	public DomainName(String[] labels) {
 		this.labels = labels;
 		domainName = toString(labels, 0, labels.length);
 		computeLength();
 
 	}
 
 	/**
 	 * Create a DomainName from the domain name string
 	 * @param labels	the string joined by dots
 	 */
 	public DomainName(String domainName) {
 		this(domainName, null, 0);
 	}
 
 	private DomainName(String domainName, String[] labels, int length) {
 		check(domainName);
 		this.domainName = domainName;
 		if (labels == null) {
 			this.labels = REGEXP.split(domainName);
 		} else {
 			this.labels = labels;
 		}
 		if (length < 1) {
 			computeLength();
 		} else {
 			this.length = length;
 		}
 	}
 
 	/**
 	 * Compute the complete length (bytes)
 	 */
 	private void computeLength() {
 		if (labels.length == 0 || (labels.length == 1 && labels[0].isEmpty())) {
 			// empty dn
 			length = 1;
 			return;
 		}
 		length = length(labels, 0, labels.length) + 1;
 		// +1 for closing null byte
 
 	}
 
 	/**
 	 * Compute the length (bytes) of the labels form <tt>i</tt>.
 	 * @param i	the start of the count
 	 * @return	the number of bytes of the domain name
 	 */
 	public static int length(String[] labels, int from, int to) {
 		if (to > labels.length) {
 			throw new IndexOutOfBoundsException("Index: " + to + ", Size: " + labels.length);
 		}
 		if (from < 0) {
 			throw new IndexOutOfBoundsException("Index: " + from + ", Size: " + labels.length);
 		}
 		int l = 0;
 		String s;
 		for (int i = from; i < to; i++) {
 			s = labels[i];
 			l++;
 			l += s.length();
 		}
 		return l;
 
 	}
 
 	/**
 	 * Read a DomainName
 	 * @param dis	the input
 	 * @param dnc	the compression manager
 	 * @return		a new DomainName
 	 * @throws IOException
 	 */
 	public static DomainName read(ArrayDataInputStream dis, DomainNameCompressor dnc) throws IOException {
 		StringBuilder sb = new StringBuilder();
 		byte c;
 		int count;
 		int position = dnc.getPosition();
 		while (true) {
 			count = dis.readUnsignedByte();
 			if (count == 0) {
 				break;
 			}
 			if (count >= 0xC0) {
 				int pointer = dis.readUnsignedByte();
 				if (sb.length() > 0) {
 					sb.append('.');
 					sb.append(dnc.getAt(count << 8 | pointer));
 					break;
 				}
 				return new DomainName(dnc.getAt(count << 8 | pointer));
 			}
 
 			if (sb.length() > 0) {
 				sb.append('.');
 			}
 
 			while (count-- > 0) {
 				c = dis.readByte();
 				sb.append((char) c);
 			}
 		}
 		String s = sb.toString();
 		DomainName dn = new DomainName(s);
 		dnc.set(s, position);
 		return dn;
 
 	}
 
 	/**
 	 * Write the DomainName
 	 * @param dos	the output
 	 */
 	public void write(ArrayDataOutputStream dos) {
 		if (length > 1) {
 			// non empty dn
 			for (String s : labels) {
 				dos.writeByte(s.length());
 				for (byte c : s.getBytes()) {
 					dos.writeByte(c);
 				}
 			}
 		}
 		dos.writeByte(0);
 
 	}
 
 	public int getLength() {
 		return length;
 	}
 
 	public static boolean valid(String value) {
 		return (value != null);
 	}
 
 	public static void check(String value) {
 		if (!valid(value)) {
 			throw new IllegalArgumentException("DomainName " + value + " is not valid");
 		}
 	}
 
 	public int getLabelsLength() {
 		return labels.length;
 	}
 
 	public String[] getLabels() {
 		return Arrays.copyOf(labels, labels.length);
 	}
 
 	@Override
 	public DomainName clone() {
 		return new DomainName(domainName);
 	}
 
 	public static String toString(String[] labels, int from, int to) {
 		if (to > labels.length) {
 			throw new IndexOutOfBoundsException("Index: " + to + ", Size: " + labels.length);
 		}
 		if (from < 0) {
 			throw new IndexOutOfBoundsException("Index: " + from + ", Size: " + labels.length);
 		}
 		StringBuilder sb = new StringBuilder();
 		for (int i = from; i < to; i++) {
 			sb.append(labels[i]);
			if (i != to - 1) {
 				sb.append(SEPARATOR);
 			}
 		}
 		return sb.toString();
 	}
 
 	@Override
 	public String toString() {
 		return domainName;
 	}
 
 }
