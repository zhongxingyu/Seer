 package ecologylab.generic;
 
 
import java.util.Vector;
 
 /**
  * A pool of reusable {@link java.lang.StringBuilder StringBuilder}s.
  */
@Deprecated public class StringBuffersPool
 extends Debug
 {
 	public static int DEFAULT_POOL_SIZE	=	64;
 	public Vector<StringBuilder> bufferPool;
 	
 	int	bufferSize;
 	int	poolSize;
 	
 	public StringBuffersPool(int bufferSize)
 	{
 		this(bufferSize, DEFAULT_POOL_SIZE);
 	}
 	public StringBuffersPool(int bufferSize, int poolSize)
 	{
 		this.bufferSize	= bufferSize;
 		bufferPool		= new Vector<StringBuilder>(poolSize);
 		for(int i = 0 ; i < poolSize; i++)
 		{
 			bufferPool.add(new StringBuilder(bufferSize));
 		}	
 	}
 	
 	public StringBuilder nextBuffer()
 	{
 		synchronized (bufferPool)
 		{
 			int freeIndex = bufferPool.size() - 1;
 			if (freeIndex == -1)
 			{
 				weird("extending pool size ");
 				return (new StringBuilder(bufferSize));
 			}
 			StringBuilder b = bufferPool.remove(freeIndex);
 			return b;
 		}
 	}
 	
 	public void release(StringBuilder b)
 	{
 		b.setLength(0);
 		bufferPool.add(b);		
 	}
 }
