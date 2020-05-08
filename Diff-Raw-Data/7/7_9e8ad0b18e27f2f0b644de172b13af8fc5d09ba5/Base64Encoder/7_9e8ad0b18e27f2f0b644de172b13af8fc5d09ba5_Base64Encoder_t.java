 package com.base64;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 
 public class Base64Encoder {
 
 	private static final int BYTE = 8;
 	private static final int WORD = 2 * BYTE;
 	private static final int SLICE = 6;
 	private static final int TOKEN_LENGTH = 3;
 	private static final int RADIX = 2;
 	
 	private char[] map;
 	private FileInputStream fIn;
 	private FileChannel inChan;
 	private MappedByteBuffer inBuf;
 	private long inSize;
	private long outSize;
 	private StringBuffer base64Value;
 	private ByteBuffer outBuf;
 	private StringBuffer token;
 
 	/**
 	 * Creates an instance of <code>Base64Encoder</code>
 	 * 
 	 * @param fInStream
 	 *            <code>FileInputStream</code> for which the <strong>Base 64
 	 *            literal</strong> will be generated
 	 * @return An instance of <code>Base64Encoder</code>, <code>null</code> if
 	 *         the <code>FileInputStream</code> is <code>null</code>
 	 */
 	public static Base64Encoder getInstance(FileInputStream fInStream) {
 		if (fInStream != null)
 			return new Base64Encoder(fInStream);
 		else {
 			System.err.println("InputStream cannot be null");
 			return null;
 		}
 	}
 
 	private Base64Encoder(FileInputStream fInStream) {
 		fIn = fInStream;
 		inChan = fIn.getChannel();
 		base64Value = new StringBuffer();
 		token = new StringBuffer();
 		map = new char[64];
 		initialize();
 	}
 
 	private void initialize() {
 		/**
 		 * initialize the Base 64 mapping
 		 */
 		for (int i = 0; i < 26; i++) {
 			map[i] = (char) ('A' + i);
 			map[i + 26] = (char) ('a' + i);
 		}
 		for (int i = 52; i <= 61; i++)
 			map[i] = (char) (i - 4);
 		map[62] = '+';
 		map[63] = '/';
 
 		try {
 			inSize = inChan.size();
 			inBuf = inChan.map(FileChannel.MapMode.READ_ONLY, 0, inSize);
			outSize = (long) (4 * ((inSize / 3) + 1));
			outBuf = ByteBuffer.allocate((int)outSize);
 		} catch (IOException e) {
 			e.printStackTrace(System.err);
 		}
 		encode();
 	}
 
 	private void encode() {
 		for (int i = 0; i < inSize; i++) {
 			token.append(ByteNumber.binValue(inBuf.get(), BYTE));
 			if ((i + 1) % TOKEN_LENGTH == 0) {
 				encodeToken(token);
 				token.delete(0, token.length());
 			}
 		}
 
 		if (token.length() == BYTE)
 			token.append("0000");
 		else if (token.length() == WORD)
 			token.append("00");
 
 		encodeToken(token);
 	}
 
 	private void encodeToken(StringBuffer token) {
 		int counter = 0;
 		char c;
 		for (int begIdx = 0, endIdx = SLICE; endIdx <= token.length(); begIdx += SLICE, endIdx += SLICE) {
 			String sub = token.substring(begIdx, endIdx);
 			c = map[Integer.parseInt(sub, RADIX)];
 			base64Value.append(c);
 			outBuf.put((byte) c);
 			counter++;
 		}
 		for (int i = 0; i < 4 - counter; i++) {
 			c = '=';
 			base64Value.append(c);
 			outBuf.put((byte) c);
 		}
 	}
 
 	/**
 	 * Calculates the Base64 equivalent 
 	 * @return
 	 * 		Base64 encoding of the given stream
 	 */
 	public String getBase64Value() {
 		return base64Value.toString();
 	}
 
 	/**
 	 * Writes the Base64 value to the given <code>FileOutputStream</code>
 	 * @param	fOt The stream to which the Base64 value will be written
 	 * @return	<code>true</code> if the write is successful, <code>false</code> otherwise
 	 */
 	public boolean writeToFile(FileOutputStream fOt) {
 		try (FileChannel outChan = fOt.getChannel()) {
 			outBuf.rewind();
 			outChan.write(outBuf);
 			return true;
 		} catch (IOException e) {
 			e.printStackTrace(System.err);
 			return false;
 		}
 	}
 
 	public static void main(String... args) throws FileNotFoundException {
 		FileInputStream f = new FileInputStream("sample.txt");
 		Base64Encoder benc = new Base64Encoder(f);
 		System.out.println(benc.getBase64Value());
 		benc.writeToFile(new FileOutputStream("sample_base64.txt"));
 	}
 }
