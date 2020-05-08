 package se.l4.aurochs.serialization.format;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.io.Reader;
 
 /**
  * Input for JSON. Please note that this class is not intended for general use
  * and does not strictly conform to the JSON standard.
  * 
  * @author Andreas Holstenson
  *
  */
 public class JsonInput
 	extends AbstractStreamingInput
 {
 	private static final char NULL = 0;
 
 	private final Reader in;
 	
 	private final char[] buffer;
 	private int position;
 	private int limit;
 	
 	private final boolean[] lists;
 	
 	public JsonInput(Reader in)
 	{
 		this.in = in;
 		
 		lists = new boolean[10];
 		buffer = new char[1024];
 	}
 	
 	private void readWhitespace()
 		throws IOException
 	{
 		while(true)
 		{
 			if(limit - position < 1)
 			{
 				read(1);
 			}
 			
 			char c = buffer[position];
 			if(Character.isWhitespace(c) || c == ',')
 			{
 				position++;
 			}
 			else
 			{
 				return;
 			}
 		}
 	}
 	
 	private char readNext()
 		throws IOException
 	{
 		readWhitespace();
 		
 		return read();
 	}
 	
 	private char read()
 		throws IOException
 	{
 		if(limit - position < 1)
 		{
 			if(! read(1))
 			{
 				throw new EOFException();
 			}
 		}
 		
 		return buffer[position++];
 	}
 	
 	private boolean read(int minChars)
 		throws IOException
 	{
 		if(limit < 0)
 		{
 			return false;
 		}
 		else if(position + minChars < limit)
 		{
 			return true;
 		}
 		else if(limit >= position)
 		{
 			// If we have characters left we need to keep them in the buffer
 			int stop = limit - position;
 			System.arraycopy(buffer, position, buffer, 0, stop);
 			limit = stop;
 		}
 		
 		int read = in.read(buffer, limit, buffer.length - limit);
 		position = 0;
 		limit = read;
 		
 		if(read == -1)
 		{
 			return false;
 		}
 		
 		if(read < minChars)
 		{
 			throw new IOException("Needed " + minChars + " but got " + read);
 		}
 		
 		return true;
 	}
 	
 	private Token toToken(char c)
 	{
 		switch(c)
 		{
 			case '{':
 				return Token.OBJECT_START;
 			case '}':
 				return Token.OBJECT_END;
 			case '[':
 				return Token.LIST_START;
 			case ']':
 				return Token.LIST_END;
 			case '"':
 				if(current() != Token.KEY && ! lists[level])
 				{
 					return Token.KEY;
 				}
 		}
 		
 		return Token.VALUE;
 	}
 	
 	private Object readNextValue()
 		throws IOException
 	{
 		char c = readNext();
 		if(c == '"')
 		{
 			// This is a string
 			return readString(false);
 		}
 		else
 		{
 			StringBuilder value = new StringBuilder();
 			_outer:
 			while(true)
 			{
 				value.append(c);
 				
 				c = peekChar(false);
 				switch(c)
 				{
 					case '}':
 					case ']':
 					case ',':
 					case ':':
 						break _outer;
 					default:
 						if(Character.isWhitespace(c)) break _outer;
 				}
 				
 				read();
 			}
 			
 			return toObject(value.toString());
 		}
 	}
 	
 	private Object toObject(String in)
 	{
 		if(in.equals("false"))
 		{
 			return false;
 		}
 		else if(in.equals("true"))
 		{
 			return true;
 		}
 		
 		try
 		{
 			return Long.parseLong(in);
 		}
 		catch(NumberFormatException e)
 		{
 			try
 			{
 				return Double.parseDouble(in);
 			}
 			catch(NumberFormatException e2)
 			{
 			}
 		}
 		
 		return in;
 	}
 	
 	private String readString(boolean readStart)
 		throws IOException
 	{
 		StringBuilder key = new StringBuilder();
 		char c = read();
 		if(readStart)
 		{
 			if(c != '"') throw new IOException("Expected \", but got " + c);
 			c = read();
 		}
 		
 		while(c != '"')
 		{
 			if(c == '\\')
 			{
 				readEscaped(key);
 			}
 			else
 			{
 				key.append(c);
 			}
 			
 			c = read();
 		}
 		
 		return key.toString();
 	}
 
 	private void readEscaped(StringBuilder result)
 		throws IOException
 	{
 		char c = read();
 		switch(c)
 		{
 			case '\'':
 				result.append('\'');
 				break;
 			case '"':
 				result.append('"');
 				break;
 			case '\\':
 				result.append('\\');
 				break;
 			case '/':
 				result.append('/');
 				break;
 			case 'r':
 				result.append('\r');
 				break;
 			case 'n':
 				result.append('\n');
 				break;
 			case 't':
 				result.append('\t');
 				break;
 			case 'b':
 				result.append('\b');
 				break;
 			case 'f':
 				result.append('\f');
 				break;
 			case 'u':
 				// Unicode, read 4 chars and treat as hex
 				read(4);
 				String s = new String(buffer, position, 4);
 				result.append((char) Integer.parseInt(s, 16));
 				position += 4;
 				break;
 		}
 	}
 
 	@Override
 	public Token next(Token expected)
 		throws IOException
 	{
 		Token t = next();
 		if(t != expected)
 		{
 			throw new IOException("Expected "+ expected + " but got " + t);
 		}
 		return t;
 	}
 	
 	@Override
 	public Token next0()
 		throws IOException
 	{
 		Token token = toToken(peekChar());
 		switch(token)
 		{
 			case OBJECT_END:
 			case LIST_END:
 				readNext();
 				return token; 
 			case OBJECT_START:
 			case LIST_START:
 				readNext();
				lists[level + 1] = token == Token.LIST_START;
 				return token;
 			case KEY:
 			{
 				readWhitespace();
 				String key = readString(true);
 				char next = readNext();
 				if(next != ':')
 				{
 					throw new IOException("Expected :, got " + next);
 				}
 				
 				setValue(key);
 				return token;
 			}
 			case VALUE:
 			{
 				setValue(readNextValue());
 				
 				// Check for trailing commas
 				readWhitespace();
 				char c = peekChar();
 				if(c == ',') read();
 				
 				return token;
 			}
 		}
 		
 		return null;
 	}
 	
 	private char peekChar()
 		throws IOException
 	{
 		return peekChar(true);
 	}
 	
 	private char peekChar(boolean ws)
 		throws IOException
 	{
 		if(ws) readWhitespace();
 		
 		if(limit - position < 1)
 		{
 			if(false == read(1))
 			{
 				return NULL;
 			}
 		}
 		
 		if(limit - position > 0)
 		{
 			return buffer[position];
 		}
 		
 		return NULL;
 	}
 	
 	@Override
 	public Token peek()
 		throws IOException
 	{
 		readWhitespace();
 		
 		if(limit - position < 1)
 		{
 			if(false == read(1)) return null;
 		}
 		
 		if(limit - position > 0)
 		{
 			return toToken(buffer[position]);
 		}
 		
 		return null;
 	}
 	
 	
 }
