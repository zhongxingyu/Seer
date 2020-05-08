 package org.neo4j.util;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.graphdb.RelationshipType;
 import org.neo4j.graphdb.ReturnableEvaluator;
 import org.neo4j.graphdb.StopEvaluator;
 import org.neo4j.graphdb.Transaction;
 import org.neo4j.graphdb.TraversalPosition;
 import org.neo4j.graphdb.Traverser;
 import org.neo4j.graphdb.Traverser.Order;
 
 /**
  * Wraps a linked list of nodes. It has a max length specified so that
  * only the latest N are stored (latest added is first in list).
  */
 public class FixedLengthNodeList
 {
     private static final String KEY_LENGTH = "list_length";
     
 	private GraphDatabaseService graphDb;
 	private Node rootNode;
 	private RelationshipType relType;
 	private GraphDatabaseUtil graphDbUtil;
 	private Integer maxLength;
 	
	public FixedLengthNodeList( GraphDatabaseService graphDb, Node rootNode,
 	    RelationshipType relType, Integer maxLengthOrNull )
 	{
		this.graphDb = graphDb;
 		this.rootNode = rootNode;
 		this.relType = relType;
		this.graphDbUtil = new GraphDatabaseUtil( graphDb );
 		this.maxLength = maxLengthOrNull;
 	}
 	
 	private Relationship getFirstRelationship()
 	{
 		return rootNode.getSingleRelationship( relType, Direction.OUTGOING );
 	}
 	
 	private Relationship getLastRelationship()
 	{
 		return rootNode.getSingleRelationship( relType, Direction.INCOMING );
 	}
 	
 	public Node add()
 	{
 		Transaction tx = graphDb.beginTx();
 		graphDbUtil.getLockManager().getWriteLock( rootNode );
 		try
 		{
 			Node node = graphDb.createNode();
 			Relationship rel = getFirstRelationship();
 			if ( rel == null )
 			{
 				rootNode.createRelationshipTo( node, relType );
 				node.createRelationshipTo( rootNode, relType );
 			}
 			else
 			{
 				Node firstNode = rel.getEndNode();
 				rel.delete();
 	            rootNode.createRelationshipTo( node, relType );
 				node.createRelationshipTo( firstNode, relType );
 			}
 			
 			if ( maxLength != null )
 			{
     			int length = ( Integer ) rootNode.getProperty( KEY_LENGTH, 0 );
     			length++;
     			if ( length > maxLength )
     			{
     			    // Remove the last one
     			    Relationship lastRel = getLastRelationship();
     			    Node lastNode = lastRel.getStartNode();
     			    Relationship previousRel = lastNode.getSingleRelationship(
                         relType, Direction.INCOMING );
     			    Node previousNode = previousRel.getStartNode();
     			    lastRel.delete();
     			    previousRel.delete();
     			    nodeFellOut( lastNode );
     			    previousNode.createRelationshipTo( rootNode, relType );
     			}
     			else
     			{
     			    rootNode.setProperty( KEY_LENGTH, length );
     			}
 			}
 			
 			tx.success();
 			return node;
 		}
 		finally
 		{
 			graphDbUtil.getLockManager().releaseWriteLock( rootNode );
 			tx.finish();
 		}
 	}
 	
 	protected void nodeFellOut( Node lastNode )
     {
 	    lastNode.delete();
     }
 
     public boolean remove()
 	{
 	    return remove( 1 ) == 1;
 	}
 	
 	public int remove( int max )
 	{
         Transaction tx = graphDb.beginTx();
         graphDbUtil.getLockManager().getWriteLock( rootNode );
         try
         {
             Relationship rel = getFirstRelationship();
             int removed = 0;
             if ( rel != null )
             {
                 Node node = rel.getEndNode();
                 Node nextNode = null;
                 for ( int i = 0; i < max; i++ )
                 {
                     Relationship relToNext = node.getSingleRelationship(
                         relType, Direction.OUTGOING );
                     nextNode = relToNext.getEndNode();
                     for ( Relationship relToDel : node.getRelationships(
                         relType ) )
                     {
                         relToDel.delete();
                     }
                     nodeFellOut( node );
                     removed++;
                     if ( nextNode.equals( rootNode ) )
                     {
                         break;
                     }
                     node = nextNode;
                 }
 
                 if ( nextNode != null && !nextNode.equals( rootNode ) )
                 {
                     rootNode.createRelationshipTo( nextNode, relType );
                 }
             }
             
             tx.success();
             return removed;
         }
         finally
         {
             graphDbUtil.getLockManager().releaseWriteLock( rootNode );
             tx.finish();
         }
 	}
 	
 	public Node peek()
 	{
 		Transaction tx = graphDb.beginTx();
 		try
 		{
 			Relationship rel = getFirstRelationship();
 			Node result = null;
 			if ( rel != null )
 			{
 				result = rel.getEndNode();
 			}
 			tx.success();
 			return result;
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	public Node[] peek( int max )
 	{
 	    Transaction tx = graphDb.beginTx();
 	    try
 	    {
 	        Collection<Node> result = new ArrayList<Node>( max );
 	        Node node = rootNode;
 	        for ( int i = 0; i < max; i++ )
 	        {
 	            Relationship rel = node.getSingleRelationship( relType,
 	                Direction.OUTGOING );
 	            if ( rel == null )
 	            {
 	                break;
 	            }
 	            Node otherNode = rel.getEndNode();
 	            if ( otherNode.equals( rootNode ) )
 	            {
 	                break;
 	            }
 	            result.add( otherNode );
 	            node = otherNode;
 	        }
             tx.success();
             return result.toArray( new Node[ 0 ] );
 	    }
 	    finally
 	    {
 	        tx.finish();
 	    }
 	}
 	
 	public Iterator<Node> iterate()
 	{
 	    StopEvaluator stopEvaluator = new StopEvaluator()
         {
             public boolean isStopNode( TraversalPosition pos )
             {
                 return pos.lastRelationshipTraversed() != null &&
                     pos.currentNode().equals( rootNode );
             }
         };
 	    
 	    Traverser traverser = rootNode.traverse( Order.BREADTH_FIRST,
 	        stopEvaluator, ReturnableEvaluator.ALL_BUT_START_NODE, relType,
 	        Direction.OUTGOING );
 	    return traverser.iterator();
 	}
 }
