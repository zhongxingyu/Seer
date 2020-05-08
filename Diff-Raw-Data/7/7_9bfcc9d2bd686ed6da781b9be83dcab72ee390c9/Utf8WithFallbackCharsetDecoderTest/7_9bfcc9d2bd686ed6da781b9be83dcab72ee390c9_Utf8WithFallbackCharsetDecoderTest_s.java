 package se.raek.charset;
 
 import static org.junit.Assert.*;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.CharacterCodingException;
 import java.nio.charset.CharsetDecoder;
 
 import org.junit.Before;
 import org.junit.Test;
 
 public class Utf8WithFallbackCharsetDecoderTest {
 	
 	private CharsetDecoder decoder;
 
 	@Before
 	public void setUp() throws Exception {
 		decoder = new Utf8WithIso88591FallbackCharset().newDecoder();
 	}
 
 	@Test
 	public void testDecodingAscii() throws UnsupportedEncodingException, CharacterCodingException {
 		ByteBuffer buffer = ByteBuffer.wrap("abc".getBytes("US-ASCII"));
 		assertEquals("abc", decoder.decode(buffer).toString());
 	}
 
 	@Test
 	public void testDecodingUtf8() throws UnsupportedEncodingException, CharacterCodingException {
 		ByteBuffer buffer = ByteBuffer.wrap("köttfärssås".getBytes("UTF-8"));
 		assertEquals("köttfärssås", decoder.decode(buffer).toString());
 	}
 
 	@Test
 	public void testDecodingIso88591() throws UnsupportedEncodingException, CharacterCodingException {
 		ByteBuffer buffer = ByteBuffer.wrap("köttfärssås".getBytes("ISO-8859-1"));
 		assertEquals("köttfärssås", decoder.decode(buffer).toString());
 	}
 	
 	@Test
 	public void testDecodingTwoByteChars() throws UnsupportedEncodingException, CharacterCodingException {
 		String s = "абвг"; // Cyrillic
 		ByteBuffer buffer = ByteBuffer.wrap(s.getBytes("UTF-8"));
 		assertEquals(s, decoder.decode(buffer).toString());
 	}
 	
 	@Test
 	public void testDecodingThreeByteChars() throws UnsupportedEncodingException, CharacterCodingException {
 		String s = "あいうえお"; // Hiragana
 		ByteBuffer buffer = ByteBuffer.wrap(s.getBytes("UTF-8"));
 		assertEquals(s, decoder.decode(buffer).toString());
 	}
 	
 	@Test
 	public void testDecodingFourByteChars() throws UnsupportedEncodingException, CharacterCodingException {
 		String s = "𐌰𐌱𐌲𐌳"; // Gothic
 		ByteBuffer buffer = ByteBuffer.wrap(s.getBytes("UTF-8"));
 		assertEquals(s, decoder.decode(buffer).toString());
 	}
 	
 	@Test
 	public void testDecodingUtf8FollowedByIso88591() throws CharacterCodingException {
 		// utf-8("åäö") + iso-8859-1("åäö")
 		byte[] bytes = {
 				(byte) 0xC3, (byte) 0xA5, (byte) 0xC3, (byte) 0xA4, (byte) 0xC3, (byte) 0xB6,
 				(byte) 0xE5, (byte) 0xE4, (byte) 0xF6
 		};
 		ByteBuffer buffer = ByteBuffer.wrap(bytes);
 		assertEquals("åäöåäö", decoder.decode(buffer).toString());
 	}
 	
 	@Test
 	public void testDecodingIso88591FollowedByUtf8() throws CharacterCodingException {
 		// iso-8859-1("åäö") + utf-8("åäö") 
 		byte[] bytes = {
 				(byte) 0xE5, (byte) 0xE4, (byte) 0xF6,
 				(byte) 0xC3, (byte) 0xA5, (byte) 0xC3, (byte) 0xA4, (byte) 0xC3, (byte) 0xB6
 		};
 		ByteBuffer buffer = ByteBuffer.wrap(bytes);
 		assertEquals("åäöåäö", decoder.decode(buffer).toString());
 	}
 	
 	@Test
 	public void testDecodingIllegalUtf8Bytes() throws UnsupportedEncodingException, CharacterCodingException {
 		String s = "üýþÿ";
 		ByteBuffer buffer = ByteBuffer.wrap(s.getBytes("ISO-8859-1"));
 		assertEquals(s, decoder.decode(buffer).toString());
 	}
 	
 	@Test
 	public void testMedialBufferOverflow() {
 		// Input the first two bytes of a three byte sequence followed by ASCII 'a'.
 		// Provide a one char buffer, so that overflow will occur when the first two
 		// bytes are being flushed. Then, give the one char buffer again, so overflow
 		// will occur one more time before proceeding.
 		byte[] bytes = {(byte) 0xE3, (byte) 0x81, (byte) 0x61};
 		ByteBuffer in = ByteBuffer.wrap(bytes);
 		CharBuffer small = CharBuffer.allocate(1);
 		CharBuffer result = CharBuffer.allocate(10);
 		
 		decoder.reset();
 		assertTrue(decoder.decode(in, small, true).isOverflow());
 		small.flip();
 		assertEquals(1, small.length());
 		result.put(small.get());
 		small.clear();
 		assertTrue(decoder.decode(in, small, true).isOverflow());
 		small.flip();
 		assertEquals(1, small.length());
 		result.put(small.get());
 		assertTrue(decoder.decode(in, result, true).isUnderflow());
 		assertTrue(decoder.flush(result).isUnderflow());
 		result.flip();
 		assertEquals("\u00E3\u0081a", result.toString());
 	}
 	
 	@Test
 	public void testFlushBufferOverflow() {
 		// Input the first two bytes of a three byte sequence and stop there.
 		// When flushing, provide a one char buffer, so that overflow will occur.
 		byte[] bytes = {(byte) 0xE3, (byte) 0x81};
 		ByteBuffer in = ByteBuffer.wrap(bytes);
 		CharBuffer small = CharBuffer.allocate(1);
 		CharBuffer result = CharBuffer.allocate(10);
 		
 		decoder.reset();
 		assertTrue(decoder.decode(in, result, true).isUnderflow());
 		result.flip();
 		assertFalse(result.hasRemaining());
 		result.clear();
 		assertTrue(decoder.flush(small).isOverflow());
 		small.flip();
 		assertEquals(1, small.remaining());
 		result.put(small.get());
 		small.clear();
 		assertTrue(decoder.flush(result).isUnderflow());
 		result.flip();
 		assertEquals("\u00E3\u0081", result.toString());
 	}
 	
 	@Test
 	public void testSupplementaryCharacterOverflow() {
 		// Input one non-BMP character, which is represented as two surrogate
 		// characters in Java char sequences. Provide a one char buffer, so that
 		// only the first surrogate pair fits during the first decode call.
 		// Then, give the one char buffer again, so overflow will occur one more
 		// time before proceeding.
 		byte[] bytes = {(byte) 0xF0, (byte) 0x90, (byte) 0x8C, (byte) 0xB0};
 		ByteBuffer in = ByteBuffer.wrap(bytes);
 		CharBuffer small = CharBuffer.allocate(1);
 		CharBuffer result = CharBuffer.allocate(10);
 		
 		decoder.reset();
 		assertTrue(decoder.decode(in, small, true).isOverflow());
 		small.flip();
 		assertEquals(1, small.remaining());
 		result.put(small.get());
 		small.clear();
 		assertTrue(decoder.decode(in, small, true).isOverflow());
 		small.flip();
 		assertEquals(1, small.remaining());
 		result.put(small.get());
 		small.clear();
 		assertTrue(decoder.decode(in, result, true).isUnderflow());
 		assertTrue(decoder.flush(result).isUnderflow());
 		result.flip();
 		assertEquals("\uD800\uDF30", result.toString());
 	}
 
 }
