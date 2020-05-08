 package org.neo4j.util;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Transaction;
 
 /**
  * This class uses the fact that node property values can be arrays.
  * It looks at one property on a node as if it was a collection of values.
  * 
  * @param <T> the type of values.
  */
 public class NeoPropertyArraySet<T> extends AbstractNeoSet<T>
 {
 	private Node node;
 	private String key;
 	private NeoUtil neoUtil;
 	
 	public NeoPropertyArraySet( NeoService neo, Node node, String key )
 	{
 		this.neoUtil = new NeoUtil( neo );
 		this.node = node;
 		this.key = key;
 	}
 	
 	protected NeoUtil neoUtil()
 	{
 		return this.neoUtil;
 	}
 	
 	protected Node node()
 	{
 		return this.node;
 	}
 	
 	protected String key()
 	{
 		return this.key;
 	}
 	
 	public boolean add( T o )
 	{
 		return neoUtil().addValueToArray( node(), key(), o );
 	}
 	
 	public void clear()
 	{
 		neoUtil().removeProperty( node(), key() );
 	}
 	
 	public boolean contains( Object o )
 	{
 		return neoUtil().getPropertyValues( node(), key() ).contains( o );
 	}
 	
 	public boolean isEmpty()
 	{
 		return neoUtil().getPropertyValues( node(), key() ).isEmpty();
 	}
 	
 	public Iterator<T> iterator()
 	{
 		return new CollectionWrapper<T, Object>(
 			neoUtil().getPropertyValues( node(), key() ) )
 		{
 			@Override
 			protected Object objectToUnderlyingObject( T object )
 			{
 				return object;
 			}
 			
 			@Override
 			protected T underlyingObjectToObject( Object object )
 			{
 				return ( T ) object;
 			}
 		}.iterator();
 	}
 	
 	public boolean remove( Object o )
 	{
 		return neoUtil().removeValueFromArray( node(), key(), o );
 	}
 	
 	public boolean retainAll( Collection<?> c )
 	{
 		Transaction tx = neoUtil().neo().beginTx();
 		try
 		{
 			Collection<Object> values =
 				neoUtil().getPropertyValues( node(), key() );
			boolean result = values.retainAll( c );
			if ( values.isEmpty() )
 			{
				node().removeProperty( key() );
			}
			else
			{
				node().setProperty( key(), neoUtil().asNeoProperty( values ) );
 			}
 			tx.success();
			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	public int size()
 	{
 		return neoUtil().getPropertyValues( node(), key() ).size();
 	}
 	
 	public Object[] toArray()
 	{
 		return neoUtil().getPropertyValues( node(), key() ).toArray();
 	}
 	
 	public <R> R[] toArray( R[] a )
 	{
 		return neoUtil().getPropertyValues( node(), key() ).toArray( a );
 	}
 }
