 package se.l4.aurochs.serialization.format;
 
 import java.io.IOException;
 
 public abstract class AbstractStreamingInput
 	implements StreamingInput
 {
 	private Object value;
 	private Token token;
 	
 	protected int level;
 	
 	public AbstractStreamingInput()
 	{
 	}
 	
 	protected void setValue(Object value)
 	{
 		this.value = value;
 	}
 	
 	@Override
 	public Token next()
 		throws IOException
 	{
 		Token token = next0();
 		switch(token)
 		{
 			case OBJECT_END:
 			case LIST_END:
 				level--;
 				break;
 			case OBJECT_START:
 			case LIST_START:
 				level++;
 		}
 		
 		return this.token = token;
 	}
 	
 	protected abstract Token next0()
 		throws IOException;
 	
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
 	public void skipValue()
 		throws IOException
 	{
 		if(token != Token.KEY)
 		{
 			throw new IOException("Value skipping can only be used with when token is " + Token.KEY);
 		}
 		
 		switch(peek())
 		{
 			case LIST_START:
 			case LIST_END:
 			case OBJECT_START:
 			case OBJECT_END:
 				next();
 				skip();
 				break;
 			default:
 				next();
 		}
 	}
 	
 	@Override
 	public void skip()
 		throws IOException
 	{
 		Token stop;
 		switch(token)
 		{
 			case LIST_START:
 				stop = Token.LIST_END;
 				break;
 			case OBJECT_START:
 				stop = Token.OBJECT_END;
 				break;
 			default:
 				throw new IOException("Can only skip when start of object or list, token is now " + token);
 		}
 		
 		int currentLevel = level;
 		
 		Token next = peek();
 		while(true)
 		{
 			// Loop until no more tokens or if we stopped and the level has been reset
 			if(next == null)
 			{
 				throw new IOException("No more tokens, but end of skipped value not found");
 			}
 			else if(next == stop && level == currentLevel)
 			{
 				// Consume this last token
 				next();
 				break;
 			}
 
 			// Read peeked value and peek for next one
 			next();
 			next = peek(); 
 		}
 	}
 	
 	@Override
 	public Token current()
 	{
 		return token;
 	}
 	
 	@Override
 	public Object getValue()
 	{
 		return value;
 	}
 	
 	@Override
 	public String getString()
 	{
 		return (String) value;
 	}
 
 	@Override
 	public boolean getBoolean()
 	{
 		return (Boolean) value;
 	}
 	
 	@Override
 	public double getDouble()
 	{
 		return ((Number) value).doubleValue();
 	}
 	
 	@Override
 	public float getFloat()
 	{
 		return ((Number) value).floatValue();
 	}
 	
 	@Override
 	public long getLong()
 	{
 		return ((Number) value).longValue();
 	}
 	
 	@Override
 	public int getInt()
 	{
 		return ((Number) value).intValue();
 	}
 	
 	@Override
 	public short getShort()
 	{
 		return ((Number) value).shortValue();
 	}
 }
