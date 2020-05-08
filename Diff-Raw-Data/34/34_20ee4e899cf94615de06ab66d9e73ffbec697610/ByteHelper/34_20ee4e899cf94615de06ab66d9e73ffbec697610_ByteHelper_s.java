 package uk.ac.cam.cl.xf214.blackadderWrapper;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.util.Arrays;
 
 public class ByteHelper {
 	
 	public static byte[] getBytes(int value) {
 		return new byte[] {
 	            (byte)(value >> 24),
 	            (byte)(value >> 16),
 	            (byte)(value >> 8),
 	            (byte)value};
 	}
 	
 	public static byte[] getBytes(long value) {
 		return new byte[] {
 				(byte)(value >> 56),
 				(byte)(value >> 48),
 				(byte)(value >> 40),
 				(byte)(value >> 32),
 	            (byte)(value >> 24),
 	            (byte)(value >> 16),
 	            (byte)(value >>  8),
 	            (byte)(value >>  0)
 	           };
 	}
 	
 	public static byte[] getBytes(short value) {
 		return new byte[] {
 				(byte)(value >> 8),
 	            (byte)value};
 	}
 	
 	public static int getInt(byte[] b) {
 		return getInt(b, 0);
 	}
 	
 	public static int getInt(byte[] b, int off) {
 		return b[off + 0] << 24 
 				| (b[off + 1] & 0xFF) << 16 
 				| (b[off + 2] & 0xFF) << 8 
 				| (b[off + 3] & 0xFF) << 0;
 	}
 	
 	public static long getLong(byte[] b) {
 		return getLong(b, 0);
 	}
 	
 	public static long getLong(byte[] b, int off) {
 		return ((long)b[off + 0]) << 56
 				| (b[off + 1] & 0xFFL) << 48 
 				| (b[off + 2] & 0xFFL) << 40 
 				| (b[off + 3] & 0xFFL) << 32 
 				| (b[off + 4] & 0xFFL) << 24 
 				| (b[off + 5] & 0xFFL) << 16  
 				| (b[off + 6] & 0xFFL) << 8 
 				| (b[off + 7] & 0xFFL) << 0;
 	}
 	
 	public static short getShort(byte[] b) {
 		return getShort(b, 0);
 	}
 	
 	public static short getShort(byte[] b, int off) {
 		return (short)(b[off+ 0] << 8 | b[off+ 1] & 0xFF);
 	}
 	
	public static void writeBytes(int value, byte[] b, int off) {
 		b[off + 0] = (byte)(value >> 24);
 		b[off + 1] = (byte)(value >> 16);
 		b[off + 2] = (byte)(value >>  8);
 		b[off + 3] = (byte)(value >>  0);
 	}
 	
	public static void writeBytes(long value, byte[] b, int off) {
 		b[off + 0] = (byte)(value >> 56);
 		b[off + 1] = (byte)(value >> 48);
 		b[off + 2] = (byte)(value >> 40);
 		b[off + 3] = (byte)(value >> 32);
 		b[off + 4] = (byte)(value >> 24);
 		b[off + 5] = (byte)(value >> 16);
 		b[off + 6] = (byte)(value >>  8);
 		b[off + 7] = (byte)(value >>  0);
 	}
 	
	public static void writeBytes(short value, byte[] b, int off) {
 		b[off + 0] = (byte)(value >> 8);
 		b[off + 1] = (byte)(value >> 0);
 	}
 	
 	
 	
 	// TEST ONLY
 	public static void main(String[] args) {
 		byte[] payloadA = new byte[100];
 		byte[] payloadB = new byte[50];
 		Arrays.fill(payloadA, (byte)5);
 		Arrays.fill(payloadB, (byte)1);
 		ByteArrayOutputStream os = new ByteArrayOutputStream(200);
 		try {
 			System.out.printf("Writing %d bytes value 5 to OS\n", payloadA.length);
 			os.write(payloadA);
 			os.flush();
 			System.out.printf("OS size is %d\n", os.size());
 			os.reset();
 			System.out.printf("OS size after reset() is %d\n", os.size());
 			System.out.printf("Writting %d bytes value 1 to OS\n", payloadB.length);
 			os.write(payloadB);
 			os.flush();
 			System.out.printf("OS size is %d\n", os.size());
 			byte[] result = os.toByteArray();
 			System.out.printf("Convert OS to byte[], size is %d\n", result.length);
 			
 
 			os.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		byte[] shared = new byte[10];
 		int off = 2;
 		
 		// "short" test
 		short shortVal = Short.MAX_VALUE - 123;
 		if (shortVal != getShort(getBytes(shortVal))) {
 			System.err.println("Error in short conversion functions");
 			return;
 		}
		writeBytes(shortVal, shared, off);
 		if (shortVal != getShort(shared, off)) {
 			System.err.println("Error in short conversion direct bytes functions");
 			return;
 		}
 		System.out.println("Success: short conversion");	
 
 		
 		// "int" test
 		int intVal = Integer.MAX_VALUE - 2322;
 		if (intVal != getInt(getBytes(intVal))) {
 			System.err.println("Error in int conversion functions");
 			return;
 		}
		writeBytes(intVal, shared, off);
 		if (intVal != getInt(shared, off)) {
 			System.err.println("Error in int conversion direct bytes functions");
 			return;
 		}
 		System.out.println("Success: int conversion");		
 		
 		
 		// "long" test
 		long longVal = Long.MAX_VALUE - 123128123;
 		if (longVal != getLong(getBytes(longVal))) {
 			System.err.println("Error in long conversion functions");
 			return;
 		}
		writeBytes(longVal, shared, off);
 		if (longVal != getLong(shared, off)) {
 			System.err.println("Error in long conversion direct bytes functions");
 			return;
 		}
 		System.out.println("Success: long conversion");
 		
 		
 		
 		System.out.println();
 		long x = Long.MAX_VALUE - 19122;
 		int loop = Integer.MAX_VALUE;
 		
 		long start = System.nanoTime();
 		for (int i = 0; i < loop; i++) {
			getLong(getBytes(x), 0);
 		}
 		long end = System.nanoTime();
 		System.out.printf("Average execution time: %.6f ns\n", (end - start) / (double)loop);
 		System.out.println();
 		
 		System.out.println("ALL TESTS COMPLETE SUCCESSFULLY!");
 	}
 }
