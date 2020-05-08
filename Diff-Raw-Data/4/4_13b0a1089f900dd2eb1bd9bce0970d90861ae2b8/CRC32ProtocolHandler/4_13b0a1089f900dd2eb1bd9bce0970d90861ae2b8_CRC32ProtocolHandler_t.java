 package uk.org.smithfamily.mslogger.comms;
 
 import java.util.zip.CRC32;
 
 public class CRC32ProtocolHandler
 {
 	public static byte[] wrap(byte[] naked)
 	{
 		byte[] wrapped = new byte[naked.length + 6];
 		wrapped[0] = 0;
 		wrapped[1] = (byte) naked.length;
 		System.arraycopy(naked, 0, wrapped, 2, naked.length);
 		CRC32 check = new CRC32();
 
 		check.update(naked);
 
 		long crc32value = check.getValue();
 		int crcIndex = wrapped.length - 4;
 		wrapped[crcIndex] = (byte) ((crc32value >> 24) & 0xff);
 		wrapped[crcIndex + 1] = (byte) ((crc32value >> 16) & 0xff);
 		wrapped[crcIndex + 2] = (byte) ((crc32value >> 8) & 0xff);
 		wrapped[crcIndex + 3] = (byte) ((crc32value >> 0) & 0xff);
 		
 		return wrapped;
 	}
 
 	public static boolean check(byte[] wrapped)
 	{
 		//TODO check incoming data here
 		return true;
 	}
 
 	public static byte[] unwrap(byte[] wrapped)
 	{
		if(wrapped.length < 7)// Bail out
		{
			return wrapped;
		}
 		byte[] naked = new byte[wrapped.length - 7];
 		System.arraycopy(wrapped, 3, naked, 0,wrapped.length-7);
 		return naked;
 	}
 	
 	public static void main(String[] args)
     {
 		byte[] test1 = new byte[]{'A'};
 		byte[] result = wrap(test1);
 		output(result);
 	}
 
 	private static void output(byte[] result)
 	{
         StringBuffer b = new StringBuffer();
         for (int i = 0; i < result.length; i++)
         {
             b.append(String.format(" %02x", result[i]));
             if((i+1) % 40 == 0)
             {
                 b.append("\n");
             }
         }
         System.out.println(b.toString());
 	}
 }
