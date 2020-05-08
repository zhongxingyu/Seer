 package de.uni_koblenz.jgralab.impl.disk;
 
 import java.rmi.RemoteException;
 
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.RemoteJGraLabServer;
 import de.uni_koblenz.jgralab.schema.Schema;
 
 public class PartialGraphDatabase extends GraphDatabaseBaseImpl implements
 		RemoteGraphDatabaseAccessWithInternalMethods {
 
 	private final RemoteGraphDatabaseAccessWithInternalMethods completeGraphDatabase;
 
 	private ParentEntityKind kindOfParentElement;
 
 	public PartialGraphDatabase(Schema schema, String uniqueGraphId,
 			String hostnameOfCompleteGraph, long parentSubgraphId,
 			ParentEntityKind kindOfParentElement, int localPartialGraphId) {
 		super(schema, uniqueGraphId, parentSubgraphId, localPartialGraphId);
 		this.kindOfParentElement = kindOfParentElement;
 		try {
 			RemoteJGraLabServer remoteInstance = localJGraLabServer
 					.getRemoteInstance(hostnameOfCompleteGraph);

 			completeGraphDatabase = remoteInstance
 					.getGraphDatabase(uniqueGraphId);
 			GraphData data = new GraphData();
 			data.globalSubgraphId = GraphDatabaseElementaryMethods
 					.getToplevelGraphForPartialGraphId(localPartialGraphId);
 			data.typeId = schema.getClassId(schema.getGraphClass());
 			localSubgraphData.add(data);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public String getHostname(int id) {
 		try {
			System.out
					.println("Asking complete graph db for hostname of partial graph "
							+ id);
			System.out.println("CompleteGraphDb: " + completeGraphDatabase);
 			return completeGraphDatabase.getHostname(id);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// for partial graph database
 	public int internalCreatePartialGraphInEntity(String remoteHostname,
 			long parentEntityGlobalId, ParentEntityKind parentEntityKind) {
 		RemoteGraphDatabaseAccessWithInternalMethods compDatabase = getGraphDatabase(GraphDatabaseElementaryMethods.TOPLEVEL_PARTIAL_GRAPH_ID);
 		int partialGraphId;
 		try {
 			partialGraphId = compDatabase.internalCreatePartialGraphInEntity(
 					remoteHostname, parentEntityGlobalId, parentEntityKind);
 		} catch (RemoteException e1) {
 			throw new RuntimeException(e1);
 		}
 		RemoteJGraLabServer remoteServer = localJGraLabServer
 				.getRemoteInstance(remoteHostname);
 		RemoteGraphDatabaseAccess p;
 		try {
 			p = remoteServer.getGraphDatabase(uniqueGraphId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 		GraphData data = new GraphData();
 		data.globalSubgraphId = getToplevelGraphForPartialGraphId(partialGraphId);
 		data.containingElementId = parentEntityGlobalId;
 		data.parentEntityKind = parentEntityKind;
 		localSubgraphData.add(data);
 		partialGraphDatabases.put(partialGraphId,
 				(RemoteGraphDatabaseAccessWithInternalMethods) p);
 		return partialGraphId;
 	}
 
 	@Override
 	public void registerPartialGraph(int id, String hostname) {
 		try {
 			completeGraphDatabase.registerPartialGraph(id, hostname);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public ParentEntityKind getParentEntityKind(long globalSubgraphId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			try {
 				return getGraphDatabase(partialGraphId).getParentEntityKind(
 						globalSubgraphId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		int localSubgraphId = convertToLocalId(globalSubgraphId);
 		// for the local toplevel graph, the value is stored in the
 		// kindOfParentElement flag
 		// of the partial graph database
 		if (localSubgraphId == TOPLEVEL_LOCAL_SUBGRAPH_ID) {
 			return kindOfParentElement;
 		}
 		// a non-toplevel graph is implemented by subordinate graph
 		// and thus nested either in a vertex or a edge, which is
 		// encoded by the sign of its sigma value
 
 		return getGraphData(localSubgraphId).containingElementId < 0 ? ParentEntityKind.EDGE
 				: ParentEntityKind.VERTEX;
 	}
 
 	@Override
 	public void deletePartialGraph(int partialGraphId) {
 		try {
 			completeGraphDatabase.deletePartialGraph(partialGraphId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 		// remove from list of partial graph ids
 	}
 
 	@Override
 	public void edgeListModified() {
 		edgeListVersion++;
 		try {
 			completeGraphDatabase.edgeListModified();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void vertexListModified() {
 		vertexListVersion++;
 		try {
 			completeGraphDatabase.vertexListModified();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void graphModified() {
 		graphVersion++;
 		try {
 			completeGraphDatabase.graphModified();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/* **************************************************************************
 	 * Methods to access traversal context
 	 * *************************************************************************
 	 */
 
 	@Override
 	public Graph getTraversalContext() {
 		try {
 			return getGraphObject(completeGraphDatabase
 					.getTraversalContextSubgraphId());
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void releaseTraversalContext() {
 		try {
 			completeGraphDatabase.releaseTraversalContext();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setTraversalContext(Graph traversalContext) {
 		try {
 			completeGraphDatabase.setTraversalContext(traversalContext
 					.getGlobalId());
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void incidenceListOfEdgeModified(long edgeId) {
 		try {
 			getGraphDatabase(
 					getPartialGraphId(GraphDatabaseElementaryMethods.GLOBAL_GRAPH_ID))
 					.incidenceListOfEdgeModified(edgeId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	// graphmodified, edgelistemodified etc in base class, activating all
 	// partial graphs and calling their internalXXModified method
 
 	@Override
 	public void graphModified(int graphId) {
 		try {
 			getGraphDatabase(
 					getPartialGraphId(GraphDatabaseElementaryMethods.GLOBAL_GRAPH_ID))
 					.graphModified();
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setGraphVersion(long graphVersion) {
 		this.graphVersion = graphVersion;
 		if (graphVersion != 0)
 			try {
 				getGraphDatabase(
 						getPartialGraphId(GraphDatabaseElementaryMethods.GLOBAL_GRAPH_ID))
 						.setGraphVersion(graphVersion);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 	}
 
 	public Object getGraphAttribute(String attributeName) {
 		try {
 			return getGraphDatabase(
 					getPartialGraphId(GraphDatabaseElementaryMethods.GLOBAL_GRAPH_ID))
 					.getGraphAttribute(attributeName);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setGraphAttribute(String attributeName, Object data) {
 		try {
 			getGraphDatabase(
 					getPartialGraphId(GraphDatabaseElementaryMethods.GLOBAL_GRAPH_ID))
 					.setGraphAttribute(attributeName, data);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public long getTraversalContextSubgraphId() throws RemoteException {
 		return completeGraphDatabase.getTraversalContextSubgraphId();
 	}
 
 	@Override
 	public void setTraversalContext(long globalId) throws RemoteException {
 		completeGraphDatabase.setTraversalContext(globalId);
 	}
 
 }
