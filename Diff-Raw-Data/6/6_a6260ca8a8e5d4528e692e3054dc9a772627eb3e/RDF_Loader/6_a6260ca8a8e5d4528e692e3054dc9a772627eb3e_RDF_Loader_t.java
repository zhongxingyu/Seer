 package net.justtrade.rest.handlers.rdf;
 
 import java.net.MalformedURLException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.justtrade.rest.handlers.graph.ManagementIndexHelper;
 import net.justtrade.rest.handlers.graph.StructureHelper;
 
 import net.justtrade.rest.util.RDF_Analyzer;
 import net.justtrade.rest.util.UnsupportedTypeException;
 
 import com.hp.hpl.jena.graph.Node;
 import com.hp.hpl.jena.graph.Node_Blank;
 import com.hp.hpl.jena.graph.Node_Literal;
 import com.hp.hpl.jena.graph.Node_URI;
 import com.hp.hpl.jena.graph.Triple;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 
 import com.tinkerpop.blueprints.pgm.Edge;
 import com.tinkerpop.blueprints.pgm.Index;
 import com.tinkerpop.blueprints.pgm.IndexableGraph;
 import com.tinkerpop.blueprints.pgm.TransactionalGraph;
 import com.tinkerpop.blueprints.pgm.Vertex;
 import com.tinkerpop.blueprints.pgm.util.IndexableGraphHelper;
 import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper;
 import com.tinkerpop.blueprints.pgm.util.TransactionalGraphHelper.CommitManager;
 import com.tinkerpop.rexster.RexsterResourceContext;
 
 /**
 * 
 * This is the handler for getting a file of RDF triples into a Blueprints database
 * 
 * @author Martin "Hasan" Bramwell (http://hasanbramwell.blogspot.com/2011/03/hello-world.html)
 */
 public class RDF_Loader {
 
 	private static final Logger logger = LoggerFactory.getLogger(RDF_Loader.class);
 	private static final String FIELD_ENTITY_NAME = "name";
 	public static final String FILE_SEPARATOR = System.getProperty("file.separator");
 	public static final String FILE_PROTOCOL = "file:" + FILE_SEPARATOR;
 	
 	/**
 	 * This method prepares the environment for loading rdf and handling failures
 	 * 
 	 * @param _tripleFile the name a file containing the triples to be loaded into the Blueprints managed graph.
 	 * @param _subRefNodeName the reference node for this collection.
 	 * @param _context the object of the triple.
 	 * @see net.justtrade.rest.handlers.graph.ManagementIndexHelper#getCollectionRefVertex(String, IndexableGraph, boolean)
 	 * 
 	 */
 
 	public void injectRDF(String _tripleFile, String _subRefNodeName, RexsterResourceContext _context)
 	{
 		final String sMETHOD = "injectRDF(String, String, RexsterResourceContext) --> ";
 		
 		String tripleFile = _tripleFile.trim();if (tripleFile.startsWith(FILE_SEPARATOR)) tripleFile = tripleFile.substring(1);
 
 		
 		TransactionalGraph tranGraph = (TransactionalGraph) _context.getRexsterApplicationGraph().getGraph();
 		
 		try {
 			
 			
 
			logger.info(sMETHOD + "Writing " + tripleFile + " contents to triple store.");
			writeToGraphStore(_subRefNodeName, tripleFile, tranGraph);
			logger.info(sMETHOD + "Wrote " + tripleFile + " contents to triple store.");
 			
 		} catch (MalformedURLException mfuex) {
 			logger.error(sMETHOD + "* * * Bad URL failure * * * \n" + mfuex.getLocalizedMessage() + "\n" + mfuex.getStackTrace());
 
 		} catch (Exception ex) {
 			logger.error(sMETHOD + "* * * Input/output problem with upload file * * * \n* * * " + ex.getLocalizedMessage() + " * * *");
 
 		}
 
 		RDF_Analyzer.analyzeModelData();			  	  	
 
 
 	}
 
 	private void writeToGraphStore (String subRefNodeName, String _tripleFile, TransactionalGraph _graph) throws MalformedURLException
 	{
 		final String sMETHOD = "writeToGraphStore(String, String, IndexableGraph) --> ";
 		logger.info(sMETHOD + "Writing :: " + FILE_PROTOCOL + _tripleFile);
 		Model model = null;
 
 		try {
 			model = ModelFactory.createDefaultModel();
 			model.read(FILE_PROTOCOL + _tripleFile);
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		}
 		
 		logger.info(sMETHOD + " * * * Starting Transaction * * * ");
 		CommitManager tranMan = TransactionalGraphHelper.createCommitManager(_graph, 50);
 		
 		StmtIterator it = model.listStatements();
 		while (it.hasNext()) {
 			Triple triple = it.next().asTriple();
 			try {
 				insertIntoDb(ManagementIndexHelper.getCollectionRefVertex(subRefNodeName, _graph, true), triple, ((IndexableGraph)_graph));
 
 				tranMan.incrCounter();
 				if(tranMan.atCommit()) System.out.print(".");
 
 				
 			} catch (Exception ex) {
 				logger.error(sMETHOD + " * * *  Hit an exception on triple " + triple + "\n" + ex.getStackTrace());
 				ex.printStackTrace();
 			}
 		}
 		logger.info(sMETHOD + " * * * Stopping Transaction * * * ");
 		tranMan.close();
 	}
 
 	/**
 	 * Inserts selected entities and relationships from Triples extracted
 	 * from the OWL document by the Jena parser. Only entities which have
 	 * a non-blank node for the subject and object are used. Further, only
 	 * relationship types listed in OntologyRelationshipTypes enum are 
 	 * considered. In addition, if the enum specifies that certain 
 	 * relationship types have an inverse, the inverse relation is also
 	 * created here.
 	 * @param neoService a reference to the Neo service.
 	 * @param indexService a reference to the Index service (for looking
 	 * up Nodes by name).
 	 * @param fileNode a reference to the Node that is an entry point into
 	 * this ontology. This node will connect to both the subject and object 
 	 * nodes of the selected triples via a CONTAINS relationship. 
 	 * @param triple a reference to the Triple extracted by the Jena parser.
 	 * @throws Exception if thrown.
 	 */
 	private void insertIntoDb(Vertex _referenceVertex, Triple _triple, IndexableGraph _graph)
 	{
 		final String sMETHOD = "insertIntoDb(Vertex, Triple, IndexableGraph) --> ";
 
 		RDF_Analyzer.collectModelAnalysisData(_triple);
 
 		Node subject = _triple.getSubject();
 		Node predicate = _triple.getPredicate();
 		Node object = _triple.getObject();
 
 		Vertex subjectVertex = null;
 		Vertex objectVertex = null;
 
 		try {
 
 // get or create the subject and object nodes
 			subjectVertex = getEntityVertex(subject, _graph, _referenceVertex);
 			objectVertex = getEntityVertex(object, _graph, _referenceVertex);
 
 			if ( ! StructureHelper.isConnected(subjectVertex, objectVertex, StructureHelper.FROM_SOURCE_TO_TARGET_ONLY))
 			{
 				logger.debug(sMETHOD
 						+ "\nTrying for triple between -- "
 						+ "\n S : " + subjectVertex.toString()  + " (" + subject + ")."
 						+ "\n O : " + objectVertex.toString()  + " (" + object + ")."
 				);
 
 				try {
 
 					if ( predicate.isURI()) {
 						
 						//logger.info("Added edge -- " + ((Node_URI) predicate).getURI());
 						logger.debug(sMETHOD
 								+ "\n" + ((Node_URI) predicate).getURI() 
 								+ "\n" + ((Node_URI) predicate).getLocalName()
 								+ "\n" + ((Node_URI) predicate).getNameSpace()
 						);
 						Edge edge = _graph.addEdge(((Node_URI) predicate).getURI(), subjectVertex, objectVertex, ((Node_URI) predicate).getLocalName());
 						logTriple(subjectVertex, edge, objectVertex);
 						
 					} else {
 						
 						throw new UnsupportedTypeException
 						(
 								  "\n" + predicate.toString()
 								+ "\n Is Blank ? [" + predicate.isBlank() 
 								+ "].\n Is Concrete ? [" + predicate.isConcrete() 
 								+ "].\n Is Literal ? [" + predicate.isLiteral() 
 								+ "].\n Is Variable ? [" + predicate.isVariable()
 								+ "]."  
 						);
 						
 					}
 
 				} catch (Throwable th) {
 					th.printStackTrace();
 				}
 
 			} else {
 				
 				logger.debug(sMETHOD + " * * *  ALREADY CONNECTED  * * * ");
 				
 			}
 
 		} catch (UnsupportedTypeException ustex) {
 			logger.warn("We can just silently ignore these"); 
 		}
 
 	}
 
 	/**
 	 * This method takes charge of determining the correct Jena handler 
 	 * 
 	 * @param _node
 	 * @param _graph
 	 * @param _managerVertex
 	 * @return
 	 * @throws UnsupportedTypeException
 	 */
 	private Vertex getEntityVertex(Node _node, IndexableGraph _graph, Vertex _managerVertex)
 		throws UnsupportedTypeException
 	{
 		final String sMETHOD = "getEntityVertex(Node, IndexableGraph, Vertex) --> ";
 
 		String value = null;
 		boolean bSupported = false;
 
 		// Possibilities : Literal, URI, blank
 		if (_node.isBlank())
 		{
 			value = ((Node_Blank) _node).getBlankNodeId().toString(); 
 			logger.debug(sMETHOD + "Examine URI '" + value + "'.");
 			bSupported = true;
 		}
 		if (_node.isLiteral())
 		{
 			if (bSupported) logger.warn(sMETHOD + " * * * previous node type being over-written * * *  '" + value + "'.");
 			value = ((Node_Literal) _node).getLiteral().toString(); 
 			logger.debug(sMETHOD + "Examine URI '" + value + "'.");
 			bSupported = true;
 		}
 		if (_node.isURI())
 		{
 			if (bSupported) logger.warn(sMETHOD + " * * * previous node type being over-written * * *  '" + value + "'.");
 			value = ((Node_URI) _node).getURI(); 
 			logger.debug(sMETHOD + "Examine URI '" + value + "'.");
 			bSupported = true;
 		}
 
 		if ( ! bSupported)
 		{
 			throw new UnsupportedTypeException
 			(
 				   sMETHOD + "\n'Variable' nodes not supported.\n" + _node.toString()
 			);
 		}
 
 		Index<Vertex> idxVertices = _graph.getIndex("vertices", Vertex.class);	
 		Vertex vertex = IndexableGraphHelper.addUniqueVertex
 			(_graph, null, idxVertices, FIELD_ENTITY_NAME, value);
 		vertex = ManagementIndexHelper.manageVertex(_graph, _managerVertex, vertex);
 
 		return vertex;
 	}
 	
 
 	/**
 	 * Convenience method to log the triple when it is inserted into the
 	 * database.
 	 * @param Vertex vtxSource; the subject of the triple.
 	 * @param Edge relation; the predicate of the triple.
 	 * @param Vertex vtxTarget; the object of the triple.
 	 */
 	private void logTriple(Vertex vtxSource, Edge edge, Vertex vtxTarget) {
 		logger.debug
 		(
 				"Wrote triple -- "
 				+ "\n S:" + vtxSource.getProperty(FIELD_ENTITY_NAME) 
 				+ "\n P:" + edge.toString()
 				+ "\n O:" + vtxTarget.getProperty(FIELD_ENTITY_NAME)
 		);
 	}
 }
