 package org.neo4j.util;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 
 public class TestIndexedSet extends TxNeo4jTest
 {
 	public void testIndexedSet() throws Exception
 	{
 		Node rootNode = graphDb().createNode();
 		Collection<AnItem> collection = new IndexedNodeCollection<AnItem>( graphDb(),
 			rootNode, new AnItemComparator(), AnItem.class );
 		
 		List<String> strings = new ArrayList<String>( Arrays.asList(
 			"Mattias",
 			"Persson",
 			"Went",
 			"And",
 			"Implemented",
 			"An",
 			"Indexed",
			"GraphDb",
 			"Set",
 			"And",
 			"How",
 			"About",
 			"That"
 		) );
 		
 		for ( String string : strings )
 		{
 			assertTrue( collection.add( new AnItem( string ) ) );
 		}
 		
 		// Compare the indexed node collection against a natural sorting order
 		Collections.sort( strings );
 		assertCollectionSame( strings, collection );
 		
 		String toRemove = "Persson";
 		AnItem toRemoveItem = findItem( collection, toRemove );
 		assertTrue( collection.remove( toRemoveItem ) );
 		assertTrue( strings.remove( toRemove ) );
 		assertCollectionSame( strings, collection );
 		
 		collection.clear();
 		( ( IndexedNodeCollection<AnItem> ) collection ).delete();
 		rootNode.delete();
 		for ( Node node : AnItem.createdNodes )
 		{
 			node.delete();
 		}
 	}
 	
 	private AnItem findItem( Collection<AnItem> collection, String name )
 	{
 		for ( AnItem item : collection )
 		{
 			if ( item.getName().equals( name ) )
 			{
 				return item;
 			}
 		}
 		fail( "'" + name + "' not found" );
 		return null;
 	}
 	
 	private void assertCollectionSame( List<String> strings,
 		Collection<AnItem> collection )
 	{
 		assertEquals( strings.size(), collection.size() );
 		int i = 0;
 		for ( AnItem item : collection ) 
 		{
 			String naturalOrder = strings.get( i++ );
 			String indexed = item.getName();
 			assertEquals( naturalOrder, indexed );
 		}
 	}
 	
 	private static class AnItem extends NodeWrapperImpl
 	{
 		static Collection<Node> createdNodes = new ArrayList<Node>();
 		
 		public AnItem( GraphDatabaseService graphDb, Node node )
 		{
 			super( graphDb, node );
 		}
 		
 		public AnItem( String name )
 		{
 			this( newNode(), name );
 		}
 		
 		private static Node newNode()
 		{
 			Node node = graphDb().createNode();
 			createdNodes.add( node );
 			return node;
 		}
 		
 		public AnItem( Node node, String name )
 		{
 			this( graphDb(), node );
 			node.setProperty( "name", name );
 		}
 		
 		public String getName()
 		{
 			return ( String ) getUnderlyingNode().getProperty( "name" );
 		}
 	}
 	
 	private static class AnItemComparator implements Comparator<AnItem>
 	{
 		public int compare( AnItem o1, AnItem o2 )
 		{
 			return o1.getName().compareTo( o2.getName() );
 		}
 	}
 }
