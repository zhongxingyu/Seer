 package info.gehrels.diplomarbeit.neo4j;
 
 import com.google.common.base.Stopwatch;
 import org.neo4j.graphdb.Direction;
 import org.neo4j.graphdb.GraphDatabaseService;
 import org.neo4j.graphdb.Node;
 import org.neo4j.graphdb.Relationship;
 import org.neo4j.tooling.GlobalGraphOperations;
 
 import java.util.Iterator;
 
 public class Neo4jStronglyConnectedComponents extends AbstractStronglyConnectedComponentsCalculator<GraphDatabaseService, Node> {
 	public static void main(String... args) throws Exception {
 		Stopwatch stopwatch = new Stopwatch().start();
 		new Neo4jStronglyConnectedComponents(args[0]).calculateStronglyConnectedComponents();
 		stopwatch.stop();
 		System.out.println(stopwatch);
 	}
 
 	public Neo4jStronglyConnectedComponents(String dbPath) {
 		super(Neo4jHelper.createNeo4jDatabase(dbPath));
 	}
 
 	@Override
 	protected Iterable<Node> getAllNodes() {
 		final Iterator<Node> iterator = GlobalGraphOperations.at(graphDB).getAllNodes().iterator();
 		return new Iterable<Node>() {
 			@Override
 			public Iterator<Node> iterator() {
 				return new Iterator<Node>() {
 
 					public Node next;
 
 					@Override
 					public boolean hasNext() {
 						ensureNextIsFetched();
 						return next != null;
 					}
 
 					private void ensureNextIsFetched() {
 						if (next == null && iterator.hasNext()) {
 							next = iterator.next();
 						}
 
						if (next != null && next.getId() == 0) {
 							next = null;
 							ensureNextIsFetched();
 						}
 					}
 
 					@Override
 					public Node next() {
 						ensureNextIsFetched();
						Node nodeToReturn = next;
						next = null;
						return nodeToReturn;
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 				};
 			}
 		};
 	}
 
 	protected long getNodeName(Node endNode) {
 		return (Long) endNode.getProperty(Neo4jImporter.NAME_KEY);
 	}
 
 	protected Iterable<Node> getOutgoingIncidentNodes(Node node) {
 		final Iterator<Relationship> relationships = node.getRelationships(Direction.OUTGOING).iterator();
 		return new Iterable<Node>() {
 			@Override
 			public Iterator<Node> iterator() {
 				return new Iterator<Node>() {
 					@Override
 					public boolean hasNext() {
 						return relationships.hasNext();
 					}
 
 					@Override
 					public Node next() {
 						return relationships.next().getEndNode();
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException();
 					}
 				};
 			}
 		};
 	}
 }
