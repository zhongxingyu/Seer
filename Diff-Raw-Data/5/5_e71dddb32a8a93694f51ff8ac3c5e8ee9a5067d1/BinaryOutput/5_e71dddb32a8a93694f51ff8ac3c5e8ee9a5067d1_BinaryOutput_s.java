 package se.l4.aurochs.serialization.format;
 
 import java.io.IOException;
 import java.io.OutputStream;
 
 /**
  * Output for custom binary format.
  * 
  * @author Andreas Holstenson
  *
  */
 public class BinaryOutput
 	implements StreamingOutput
 {
 	private static final int MAX_LEVELS = 10;
 
 	public static final int TAG_KEY = 0;
 	
 	public static final int TAG_OBJECT_START = 1;
 	public static final int TAG_OBJECT_END = 2;
 	public static final int TAG_LIST_START = 3;
 	public static final int TAG_LIST_END = 4;
 	
 	public static final int TAG_STRING = 10;
 	public static final int TAG_INT = 11;
 	public static final int TAG_LONG = 12;
 	public static final int TAG_NULL = 13;
 	public static final int TAG_FLOAT = 14;
 	public static final int TAG_DOUBLE = 15;
 	public static final int TAG_BOOLEAN = 16;
 	public static final int TAG_BYTE_ARRAY = 17;
 	
 	private final OutputStream out;
 	
 	private final boolean[] lists;
 	private final boolean[] hasData;
 	
 	private int level;
 	
 	public BinaryOutput(OutputStream out)
 	{
 		this.out = out;
 		
 		lists = new boolean[MAX_LEVELS];
 		hasData = new boolean[MAX_LEVELS];
 	}
 	
 	/**
 	 * Increase the level by one.
 	 * 
 	 * @param list
 	 */
 	private void increaseLevel(boolean list)
 	{
 		level++;
 		hasData[level] = false;
 		lists[level] = list;
 	}
 	
 	/**
 	 * Decrease the level by one.
 	 * 
 	 * @throws IOException
 	 */
 	private void decreaseLevel()
 		throws IOException
 	{
 		level--;
 	}
 
 	/**
 	 * Start a write, will output commas and beautification if needed.
 	 * 
 	 * @throws IOException
 	 */
 	private void startWrite()
 		throws IOException
 	{
 //		if(hasData[level]) writer.write(',');
 		
 		hasData[level] = true;
 	}
 	
 	/**
 	 * Check if the name should be written or not.
 	 * 
 	 * @return
 	 */
 	private boolean shouldOutputName()
 	{
 		return level != 0 && ! lists[level];
 	}
 	
 	/**
 	 * Write the name if needed.
 	 * 
 	 * @param name
 	 * @throws IOException
 	 */
 	private void writeName(String name)
 		throws IOException
 	{
 		if(shouldOutputName())
 		{
 			out.write(TAG_KEY);
 			writeStringNoTag(name);
 		}
 	}
 	
 	/**
 	 * Write an integer to the output stream without tagging it.
 	 * 
 	 * @param value
 	 * @throws IOException
 	 */
 	private void writeIntegerNoTag(int value)
 		throws IOException
 	{
 		while(true)
 		{
 			if((value & ~0x7F) == 0)
 			{
 				out.write(value);
 				break;
 			}
 			else
 			{
 				out.write((value & 0x7f) | 0x80);
 				value >>>= 7;
 			}
 		}
 	}
 	
 	/**
 	 * Write an integer to the output stream.
 	 * 
 	 * @param value
 	 * @throws IOException
 	 */
 	private void writeInteger(int value)
 		throws IOException
 	{
 		out.write(TAG_INT);
 		writeIntegerNoTag(value);
 	}
 	
 	/**
 	 * Write a long to the output stream.
 	 * 
 	 * @param value
 	 * @throws IOException
 	 */
 	private void writeLongNoTag(long value)
 		throws IOException
 	{
 		while(true)
 		{
 			if((value & ~0x7FL) == 0)
 			{
 				out.write((int) value);
 				break;
 			}
 			else
 			{
 				out.write(((int) value & 0x7f) | 0x80);
 				value >>>= 7;
 			}
 		}
 	}
 	
 	/**
 	 * Write a long to the output stream.
 	 * 
 	 * @param value
 	 * @throws IOException
 	 */
 	private void writeLong(long value)
 		throws IOException
 	{
 		out.write(TAG_LONG);
 		writeLongNoTag(value);
 	}
 	
 	/**
 	 * Write a string to the output without tagging that its actually a string.
 	 * 
 	 * @param value
 	 * @throws IOException
 	 */
 	private void writeStringNoTag(String value)
 		throws IOException
 	{
 		writeIntegerNoTag(value.length());
 		for(int i=0, n=value.length(); i<n; i++)
 		{
 			char c = value.charAt(i);
			if(c >= 0x007f)
 			{
 				out.write((byte) c);
 			}
			else if(c > 0x007f)
 			{
 				out.write((byte) (0xe0 | c >> 12 & 0x0f));
 				out.write((byte) (0x80 | c >> 6 & 0x3f));
 				out.write((byte) (0x80 | c >> 0 & 0x3f));
 			}
 			else
 			{
 				out.write((byte) (0xc0 | c >> 6 & 0x1f));
 				out.write((byte) (0x80 | c >> 0 & 0x3f));
 			}
 		}
 	}
 	
 	private void writeString(String value)
 		throws IOException
 	{
 		out.write(TAG_STRING);
 		writeStringNoTag(value);
 	}
 	
 	private void writeNull()
 		throws IOException
 	{
 		out.write(TAG_NULL);
 	}
 	
 	private void writeFloat(float value)
 		throws IOException
 	{
 		out.write(TAG_FLOAT);
 		
 		int i = Float.floatToRawIntBits(value);
 		out.write(i & 0xff);
 		out.write((i >> 8) & 0xff);
 		out.write((i >> 16) & 0xff);
 		out.write((i >> 24) & 0xff);
 	}
 	
 	private void writeDouble(double value)
 		throws IOException
 	{
 		out.write(TAG_DOUBLE);
 		
 		long l = Double.doubleToRawLongBits(value);
 		out.write((int) l & 0xff);
 		out.write((int) (l >> 8) & 0xff);
 		out.write((int) (l >> 16) & 0xff);
 		out.write((int) (l >> 24) & 0xff);
 		out.write((int) (l >> 32) & 0xff);
 		out.write((int) (l >> 40) & 0xff);
 		out.write((int) (l >> 48) & 0xff);
 		out.write((int) (l >> 56) & 0xff);
 	}
 	
 	private void writeBoolean(boolean b)
 		throws IOException
 	{
 		out.write(TAG_BOOLEAN);
 		
 		out.write(b ? 1 : 0);
 	}
 	
 	private void writeByteArray(byte[] data)
 		throws IOException
 	{
 		out.write(TAG_BYTE_ARRAY);
 		
 		writeIntegerNoTag(data.length);
 		
 		out.write(data);
 	}
 	
 	@Override
 	public void writeObjectStart(String name)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		out.write(TAG_OBJECT_START);
 		
 		increaseLevel(false);
 	}
 	
 	@Override
 	public void writeObjectEnd(String name)
 		throws IOException
 	{
 		decreaseLevel();
 		
 		out.write(TAG_OBJECT_END);
 	}
 	
 	@Override
 	public void writeListStart(String name)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		out.write(TAG_LIST_START);
 		
 		increaseLevel(true);
 	}
 	
 	@Override
 	public void writeListEnd(String name)
 		throws IOException
 	{
 		decreaseLevel();
 		out.write(TAG_LIST_END);
 	}
 	
 	@Override
 	public void write(String name, String value)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		
 		if(value == null)
 		{
 			writeNull();
 		}
 		else
 		{
 			writeString(value);
 		}
 	}
 	
 	@Override
 	public void write(String name, int number)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		// Always write with Protobufs ZigZag  
 		writeInteger((number << 1) ^ (number >> 31));
 	}
 	
 	@Override
 	public void write(String name, long number)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		// Always write with Protobufs ZigZag
 		writeLong((number << 1) ^ (number >> 63));	
 	}
 	
 	@Override
 	public void write(String name, float number)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		writeFloat(number);
 	}
 	
 	@Override
 	public void write(String name, double number)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		writeDouble(number);
 	}
 	
 	@Override
 	public void write(String name, boolean bool)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		writeBoolean(bool);
 	}
 	
 	@Override
 	public void write(String name, byte[] data)
 		throws IOException
 	{
 		startWrite();
 		
 		writeName(name);
 		writeByteArray(data);
 	}
 	
 	@Override
 	public void writeNull(String name)
 		throws IOException
 	{
 		writeName(name);
 		writeNull();
 	}
 	
 	@Override
 	public void flush()
 		throws IOException
 	{
 		out.flush();
 	}
 }
