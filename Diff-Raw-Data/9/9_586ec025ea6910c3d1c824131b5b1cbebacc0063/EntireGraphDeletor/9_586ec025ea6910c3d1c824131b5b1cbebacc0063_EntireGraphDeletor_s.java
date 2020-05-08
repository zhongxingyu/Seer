 package org.neo4j.util;
 
 import java.util.Collection;
 import java.util.HashSet;
 
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 
 /**
 * Deletes an entire graph, i.e. all connected nodes (and their connected nodes)
  * from a start node (any start node in that graph). It is mostly used in
  * some test cases.
  */
 public class EntireGraphDeletor implements GraphDeletor
 {
 	public void delete( Node startNode )
 	{
 		removeNodeAndThoseConnectedWith( new HashSet<Node>(),
 			new HashSet<Relationship>(), startNode );
 	}
 
 	private void removeNodeAndThoseConnectedWith( Collection<Node> deletedNodes,
 		Collection<Relationship> deletedRels, Node node )
 	{
 		for ( Relationship rel : node.getRelationships() )
 		{
 			Node sub = rel.getOtherNode( node );
 			if ( deletedRels.add( rel ) )
 			{
 				aboutToDeleteRelationship( rel );
 				rel.delete();
 			}
 			removeNodeAndThoseConnectedWith( deletedNodes, deletedRels, sub );
 		}
 		if ( deletedNodes.add( node ) )
 		{
 			aboutToDeleteNode( node );
 			node.delete();
 		}
 	}
 	
 	protected void aboutToDeleteRelationship( Relationship relationship )
 	{
 	}
 	
 	protected void aboutToDeleteNode( Node node )
 	{
 	}
 }
