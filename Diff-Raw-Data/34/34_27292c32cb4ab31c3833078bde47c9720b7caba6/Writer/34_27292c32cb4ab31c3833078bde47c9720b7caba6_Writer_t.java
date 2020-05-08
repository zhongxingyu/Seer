 package eu.gemstonewebdesign.java.kompressor;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 public class Writer
 {
 	private OutputStream output;
 	
 	private byte current_byte = 0;
 	
 	private byte current_count = 0;
 	
 	private long bit_count = 0;
 	
 	public Writer(OutputStream output)
 	{
 		this.output = output;
 	}
 	
 	public void write(Boolean data) throws IOException
 	{
 		if (data)
 		{
 			this.current_byte = (byte) ((this.current_byte << 1) | 1);
 		}
 		else
 		{
 			this.current_byte = (byte) (this.current_byte << 1);
 		}
 		this.current_count++;
 		this.bit_count++;
 		
 		if (this.current_count == 8)
 		{
 			this.output.write(this.current_byte);
			// System.out.println("new byte: "+printBinary8(this.current_byte));
 			
 			this.reset();
 		}
 	}
 	
 	public void flush() throws IOException
 	{
 		if (this.current_count != 0)
 		{
 			this.current_byte = (byte) (this.current_byte << (8 - this.current_count));
 			this.output.write(this.current_byte);
			// System.out.println("flushing "+printBinary8(this.current_byte));
 			this.current_byte = 0;
 			this.reset();
 		}
 		this.output.flush();
 	}
 	
 	private void reset()
 	{
 		this.current_byte = 0;
 		this.current_count = 0;
 	}
 	
 	public long bitCount()
 	{
 		return this.bit_count;
 	}
 	
 	// test purposes only
	static protected String printBinary8(byte b)
 	{
 		String bin = Integer.toBinaryString(b);
 		if (bin.length() > 8)
 		{
 			return bin.substring(bin.length()-8);
 		}
 		else
 		{
 			switch (bin.length())
 			{
 			case 8:
 				return bin;
 			case 7: 
 				return "0"+bin;
 			case 6: 
 				return "00"+bin;
 			case 5: 
 				return "000"+bin;
 			case 4: 
 				return "0000"+bin;
 			case 3: 
 				return "00000"+bin;
 			case 2: 
 				return "000000"+bin;
 			case 1: 
 				return "0000000"+bin;
 			default:
 				return "00000000"+bin;
 			}
 		}
 	}
 }
