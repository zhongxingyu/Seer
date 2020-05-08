 package uk.ed.inf.graph.compound.base;
 
 import java.util.Iterator;
 
 import uk.ed.inf.graph.basic.IBasicPair;
 import uk.ed.inf.graph.compound.ICompoundGraph;
 import uk.ed.inf.graph.compound.IModifiableCompoundGraph;
 import uk.ed.inf.graph.compound.ISubCompoundGraph;
 import uk.ed.inf.graph.directed.IDirectedPair;
 import uk.ed.inf.graph.state.IGraphState;
 import uk.ed.inf.graph.state.IRestorableGraph;
 import uk.ed.inf.graph.util.IndexCounter;
 import uk.ed.inf.graph.util.impl.EdgeFromNodeIterator;
 import uk.ed.inf.tree.ITree;
 
 public abstract class BaseCompoundGraph implements ICompoundGraph<BaseCompoundNode, BaseCompoundEdge>,
 		IRestorableGraph<BaseCompoundNode, BaseCompoundEdge>,
 		IModifiableCompoundGraph<BaseCompoundNode, BaseCompoundEdge> {
 	private final BaseCompoundGraphStateHandler stateHandler;
 	private BaseGraphCopyBuilder copyBuilder;
 	
 	protected BaseCompoundGraph(BaseGraphCopyBuilder copyBuilder){
 		this.stateHandler = new BaseCompoundGraphStateHandler(this);
 		this.copyBuilder = copyBuilder;
 	}
 
 	protected BaseCompoundGraph(BaseGraphCopyBuilder copyBuilder, BaseCompoundGraph otherGraph){
 		this(copyBuilder);
 		createCopyOfRootNode(getNodeCounter().getLastIndex(), otherGraph.getRootNode());
 		this.performCopy(otherGraph);
 	}
 	
 	protected abstract IndexCounter getNodeCounter();
 	
 	protected abstract IndexCounter getEdgeCounter();
 
 	protected abstract void createCopyOfRootNode(int newIndexValue, BaseCompoundNode otherRootNode);
 
 	protected abstract ITree<BaseCompoundNode> getNodeTree();
 
 	/**
 	 * To be used by copy constructor. After this constructor has been called extending classes should ensure that 
 	 *  the root node is copied, and a new Tree is created. The calling constructor can then call
 	 *  <code>perfromCopy()</code> to  actually copy the graph;
 	 * @param copyBuilder
 	 * @param otherGraph
 	 */
 	protected void performCopy(BaseCompoundGraph otherGraph){
 		BaseSubCompoundGraphFactory fact = otherGraph.subgraphFactory();
 		Iterator<BaseCompoundNode> iter = otherGraph.getNodeTree().getRootNode().getChildCompoundGraph().nodeIterator();
 		while(iter.hasNext()){
 			BaseCompoundNode level1Node = iter.next();
 			fact.addNode(level1Node);
 		}
 		BaseSubCompoundGraph subgraph = fact.createInducedSubgraph();
 		this.copyHere(subgraph);
 	}
 	
 	public abstract BaseCompoundNode getRootNode();
 
 	public final boolean containsDirectedEdge(BaseCompoundNode outNode, BaseCompoundNode inNode) {
 		boolean retVal = false;
 		if(inNode != null && outNode != null){
 			BaseCompoundNode node = this.getNodeTree().getLowestCommonAncestor(inNode, outNode);
 			if(node != null){
 				retVal = node.getChildCompoundGraph().containsDirectedEdge(outNode, inNode);
 			}
 		}
 		return retVal;
 	}
 
 	public final boolean containsConnection(BaseCompoundNode thisNode, BaseCompoundNode thatNode) {
 		boolean retVal = false;
 		if(thisNode != null && thatNode != null){
 			BaseCompoundNode node = this.getNodeTree().getLowestCommonAncestor(thisNode, thatNode);
 			if(node != null){
 				retVal = node.getChildCompoundGraph().containsConnection(thisNode, thatNode);
 			}
 		}
 		return retVal;
 	}
 
 	public final boolean containsEdge(BaseCompoundEdge edge) {
 		boolean retVal = false;
 		if(edge != null && edge.getGraph().equals(this)){
 			retVal = edge.getOwningChildGraph().containsEdge(edge);
 		}
 		return retVal;
 	}
 
 	public final boolean containsEdge(int edgeIdx) {
 		boolean retVal = false;
 		Iterator<BaseCompoundNode> iter = this.getNodeTree().levelOrderIterator();
 		while(iter.hasNext()){
 			BaseCompoundNode node = iter.next();
 			retVal = node.getChildCompoundGraph().containsEdge(edgeIdx);
 			if(retVal == true){
 				break;
 			}
 		}
 		return retVal;
 	}
 
 	public final boolean containsNode(int nodeIdx) {
 		return this.getNodeTree().containsNode(nodeIdx);
 	}
 
 	public final boolean containsNode(BaseCompoundNode node) {
 		boolean retVal = false;
 		if(node != null){
 			retVal = this.containsNode(node.getIndex());
 		}
 		return retVal;
 	}
 
 	public BaseCompoundEdge getEdge(int edgeIdx) {
 		Iterator<BaseCompoundEdge> iter = this.edgeIterator();
 		BaseCompoundEdge retVal = null;
 		while(iter.hasNext() && retVal == null){
 			BaseCompoundEdge edge = iter.next();
 			if(edge.getIndex() == edgeIdx){
 				retVal = edge;
 			}
 		}
 		return retVal;
 	}
 
 	/**
 	 * Returns all edges in tree level-node iteration order. For each node the edges are returned in the same
 	 * order as the CiNode edge iterator. Returns both undirected and directed nodes.
 	 */
 	public Iterator<BaseCompoundEdge> edgeIterator() {
 		return new EdgeFromNodeIterator<BaseCompoundNode, BaseCompoundEdge>(this.getNodeTree().levelOrderIterator());
 	}
 
 	public BaseCompoundNode getNode(int nodeIdx) {
		BaseCompoundNode retVal = this.getNodeTree().get(nodeIdx);
		if(retVal == null) throw new IllegalArgumentException("nodeIdx does not refer toa  node contained in this graph");
		return retVal;
 	}
 
 	public Iterator<BaseCompoundNode> nodeIterator() {
 		return this.getNodeTree().levelOrderIterator();
 	}
 
 	public final int getNumEdges() {
 		Iterator<BaseCompoundEdge> iter = this.edgeIterator();
 		int cntr = 0;
 		while(iter.hasNext()){
 			iter.next();
 			cntr++;
 		}
 		return cntr;
 	}
 
 	public final int getNumNodes() {
 		return this.getNodeTree().size();
 	}
 
 	public final void removeSubgraph(ISubCompoundGraph<? extends BaseCompoundNode, ? extends BaseCompoundEdge> subgraph) {
 		if(subgraph == null) throw new IllegalArgumentException("subgraph cannot be null");
 		if(subgraph.getSuperGraph() != this) throw new IllegalArgumentException("The subgraph must belong to this graph");
 		removeEdges(subgraph.edgeIterator());
 		removeNodes(subgraph.nodeIterator());
 	}
 
 
 	private void removeEdges(Iterator<? extends BaseCompoundEdge> edgeIterator){
 		while(edgeIterator.hasNext()){
 			BaseCompoundEdge edge = (BaseCompoundEdge)edgeIterator.next();
 			edge.markRemoved(true);
 		}
 	}
 	
 	private void removeNodes(Iterator<? extends BaseCompoundNode> nodeIterator){
 		while(nodeIterator.hasNext()){
 			BaseCompoundNode node = (BaseCompoundNode)nodeIterator.next();
 			if(node.equals(this.getRootNode())){
 				throw new IllegalStateException("Cannot remove the root node from a compound graph");
 			}
 			node.markRemoved(true);
 			// remove edges associated with node
 			Iterator<BaseCompoundEdge> edgeIter = node.edgeIterator();
 			while(edgeIter.hasNext()){
 				BaseCompoundEdge edge = edgeIter.next();
 				edge.markRemoved(true);
 			}
 		}
 	}
 	
 	final BaseCompoundNode getLcaNode(BaseCompoundNode inNode, BaseCompoundNode outNode){
 		if(inNode == null || outNode == null) throw new IllegalArgumentException("parameters cannot be null");
 		
 		return this.getNodeTree().getLowestCommonAncestor(inNode, outNode);
 	}
 	
 	/**
 	 * Tests if the ends define one or more directed edges.
 	 */
 	public final boolean containsDirectedEdge(IDirectedPair<? extends BaseCompoundNode, ? extends BaseCompoundEdge> ends) {
 		boolean retVal = false;
 		if(ends != null){
 			BaseCompoundNode outNode = (BaseCompoundNode)ends.getOutNode();
 			BaseCompoundNode inNode = (BaseCompoundNode)ends.getInNode();
 			// check that at least one node belongs to this graph, if so then we
 			// can be sure that the other node and edge will.
 			if(outNode.getGraph().equals(this)){
 				retVal = outNode.hasOutEdgeTo(inNode);
 			}
 		}
 		return retVal;
 	}
 
 	/**
 	 * Tests if the ends define any edge in this graph. Note that the node pair must
 	 * be created by this graph as the method expects <code>ends</code> to be of type
 	 * <code>IDirectedPair</code> and will return false if it is not.
 	 * @param ends the pair of nodes that may define the edges of an edge.
 	 * @return true if it does, false otherwise.  
 	 */
 	@SuppressWarnings("unchecked")
 	public final boolean containsConnection(IBasicPair<? extends BaseCompoundNode, ? extends BaseCompoundEdge> ends) {
 		boolean retVal = false;
 		if(ends != null && ends instanceof IDirectedPair){
 			// since this is a directed graph a valid edge pair must be an IDirectedPair
 			IDirectedPair<? extends BaseCompoundNode, ? extends BaseCompoundEdge> ciEnds = (IDirectedPair<? extends BaseCompoundNode, ? extends BaseCompoundEdge>)ends;
 			BaseCompoundNode oneNode = (BaseCompoundNode)ciEnds.getOutNode();
 			BaseCompoundNode twoNode = (BaseCompoundNode)ciEnds.getInNode();
 			// check that at least one node belongs to this graph, if so then we
 			// can be sure that the other node and edge will.
 			if(oneNode.getGraph().equals(this)){
 				retVal = this.containsConnection(oneNode, twoNode);
 			}
 		}
 		return retVal;
 	}
 
 	public IGraphState<BaseCompoundNode, BaseCompoundEdge> getCurrentState() {
 		return stateHandler.createGraphState();
 	}
 
 	public final void restoreState(IGraphState<BaseCompoundNode, BaseCompoundEdge> previousState) {
 		this.stateHandler.restoreState(previousState);
 	}
 
 	public final boolean canCopyHere(ISubCompoundGraph<? extends BaseCompoundNode, ? extends BaseCompoundEdge> subGraph) {
 		return subGraph != null && subGraph.isInducedSubgraph()
 			&& subGraph.isConsistentSnapShot();
 	}
 	
 	public final void copyHere(ISubCompoundGraph<? extends BaseCompoundNode, ? extends BaseCompoundEdge> subGraph) {
 		if(!canCopyHere(subGraph)) throw new IllegalArgumentException("Cannot copy graph here");
 		
 		BaseChildCompoundGraph rootCiGraph = this.getNodeTree().getRootNode().getChildCompoundGraph();
 		copyBuilder.setDestinatChildCompoundGraph(rootCiGraph);
 		copyBuilder.setSourceSubgraph(subGraph);
 		copyBuilder.makeCopy();
 	}
 	
 	public final ISubCompoundGraph<BaseCompoundNode, BaseCompoundEdge> getCopiedComponents() {
 		return copyBuilder.getCopiedComponents();
 	}
 
 	public abstract BaseCompoundNodeFactory nodeFactory();
 
 	public abstract BaseCompoundEdgeFactory edgeFactory();
 
 	public abstract BaseSubCompoundGraphFactory subgraphFactory();
 }
