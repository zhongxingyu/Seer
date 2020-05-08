 package multiplexer.test;
 
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.util.Arrays;
 import java.util.zip.CRC32;
 
import junit.framework.TestCase;

 import org.jboss.netty.buffer.ChannelBuffer;
 import org.jboss.netty.buffer.ChannelBuffers;
 
 public class TestByteBufferSerialization extends TestCase {
 
 	public void testByteBufferSerialization() {
 		byte[] a = new byte[] { 'a', 'l', 'a' };
 		CRC32 crc = new CRC32();
 		crc.update(a);
 
 		ByteBuffer b = ByteBuffer.allocate(8);
 		b.order(ByteOrder.LITTLE_ENDIAN);
 		b.putInt(a.length);
 		b.putInt((int) crc.getValue());
 
 		assertTrue(Arrays.equals(b.array(), new byte[] { 3, 0, 0, 0, 96, 13,
 			169 - 256, 69 }));
 	}
 
 	public void testNettyBufferSimplest() {
 		ByteBuffer bb = ByteBuffer.allocate(4);
 		bb.order(ByteOrder.LITTLE_ENDIAN);
 		assertEquals(bb.order(), ByteOrder.LITTLE_ENDIAN);
 		bb = bb.slice();
 		assertEquals(bb.order(), ByteOrder.BIG_ENDIAN);
 
 		ChannelBuffer b = ChannelBuffers.directBuffer(ByteOrder.LITTLE_ENDIAN,
 			4);
 		assertEquals("a bug in netty", b.order(), ByteOrder.LITTLE_ENDIAN);
 		b.writeInt(1);
 		assertEquals(b.readByte(), 1);
 		assertEquals(b.readByte(), 0);
 		assertEquals(b.readByte(), 0);
 		assertEquals(b.readByte(), 0);
 	}
 
 	public void testNettyBufferSerialization() {
 		byte[] a = new byte[] { 'a', 'l', 'a' };
 		CRC32 crc = new CRC32();
 		crc.update(a);
 
 		ChannelBuffer b = ChannelBuffers.buffer(ByteOrder.LITTLE_ENDIAN,
 			8);
 		b.writeInt(a.length);
 		b.writeInt((int) crc.getValue());
 
 		byte[] copy = new byte[8];
 		b.getBytes(0, copy);
 		for (byte c : copy)
 			System.out.print(c + ", ");
 		System.out.println();
 		assertTrue(Arrays.equals(copy, new byte[] { 3, 0, 0, 0, 96, 13,
 			169 - 256, 69 }));
 	}
 }
