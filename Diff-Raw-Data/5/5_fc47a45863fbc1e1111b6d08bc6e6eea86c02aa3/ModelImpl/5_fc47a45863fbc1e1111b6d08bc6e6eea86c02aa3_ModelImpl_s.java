 package org.eclipse.stem.core.model.impl;
 
 /******************************************************************************* 
  * Copyright (c) 2006, 2008 IBM Corporation and others. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html 
  * 
  * Contributors: 
  *     IBM Corporation - initial API and implementation 
  *******************************************************************************/
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.util.EObjectResolvingEList;
 import org.eclipse.stem.core.common.IdentifiableFilter;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.stem.core.STEMURI;
 import org.eclipse.stem.core.common.DublinCore;
 import org.eclipse.stem.core.common.impl.IdentifiableFilterImpl;
 import org.eclipse.stem.core.common.impl.IdentifiableImpl;
 import org.eclipse.stem.core.graph.Edge;
 import org.eclipse.stem.core.graph.Graph;
 import org.eclipse.stem.core.graph.GraphFactory;
 import org.eclipse.stem.core.graph.GraphPackage;
 import org.eclipse.stem.core.graph.Node;
 import org.eclipse.stem.core.graph.NodeLabel;
 import org.eclipse.stem.core.graph.UnresolvedIdentifiable;
 import org.eclipse.stem.core.model.Decorator;
 import org.eclipse.stem.core.model.EdgeDecorator;
 import org.eclipse.stem.core.model.GraphDecorator;
 import org.eclipse.stem.core.model.Model;
 import org.eclipse.stem.core.model.ModelPackage;
 import org.eclipse.stem.core.model.NodeDecorator;
 import org.eclipse.stem.core.model.STEMTime;
 import org.eclipse.stem.core.scenario.impl.ScenarioImpl;
 
 /**
  * <!-- begin-user-doc --> An implementation of the model object '<em><b>Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link org.eclipse.stem.core.model.impl.ModelImpl#getModels <em>Models</em>}</li>
  *   <li>{@link org.eclipse.stem.core.model.impl.ModelImpl#getGraphs <em>Graphs</em>}</li>
  *   <li>{@link org.eclipse.stem.core.model.impl.ModelImpl#getGraphDecorators <em>Graph Decorators</em>}</li>
  *   <li>{@link org.eclipse.stem.core.model.impl.ModelImpl#getNodeDecorators <em>Node Decorators</em>}</li>
  *   <li>{@link org.eclipse.stem.core.model.impl.ModelImpl#getEdgeDecorators <em>Edge Decorators</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class ModelImpl extends IdentifiableImpl implements Model {
 
 	/**
 	 * This is used to generate unique URI's for each Model generated
 	 */
 	static int modelCounter = 0;
 
 	/**
 	 * The cached value of the '{@link #getModels() <em>Models</em>}' reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getModels()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Model> models;
 
 	/**
 	 * The cached value of the '{@link #getGraphs() <em>Graphs</em>}' reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getGraphs()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<Graph> graphs;
 
 	/**
 	 * The cached value of the '{@link #getGraphDecorators() <em>Graph Decorators</em>}' reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getGraphDecorators()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<GraphDecorator> graphDecorators;
 
 	/**
 	 * The cached value of the '{@link #getNodeDecorators() <em>Node Decorators</em>}' reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getNodeDecorators()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<NodeDecorator> nodeDecorators;
 
 	/**
 	 * The cached value of the '{@link #getEdgeDecorators() <em>Edge Decorators</em>}' reference list.
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @see #getEdgeDecorators()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<EdgeDecorator> edgeDecorators;
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * 
 	 * @generated NOT
 	 */
 	protected ModelImpl() {
 		super();
 		setURI(STEMURI.createURI(URI_TYPE_MODEL_SEGMENT + "/"
 				+ Integer.toString(modelCounter++)));
 		setTypeURI(STEMURI.MODEL_TYPE_URI);
 	} // ModelImpl
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return ModelPackage.Literals.MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Model> getModels() {
 		if (models == null) {
 			models = new EObjectResolvingEList<Model>(Model.class, this, ModelPackage.MODEL__MODELS);
 		}
 		return models;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<Graph> getGraphs() {
 		if (graphs == null) {
 			graphs = new EObjectResolvingEList<Graph>(Graph.class, this, ModelPackage.MODEL__GRAPHS);
 		}
 		return graphs;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<GraphDecorator> getGraphDecorators() {
 		if (graphDecorators == null) {
 			graphDecorators = new EObjectResolvingEList<GraphDecorator>(GraphDecorator.class, this, ModelPackage.MODEL__GRAPH_DECORATORS);
 		}
 		return graphDecorators;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<NodeDecorator> getNodeDecorators() {
 		if (nodeDecorators == null) {
 			nodeDecorators = new EObjectResolvingEList<NodeDecorator>(NodeDecorator.class, this, ModelPackage.MODEL__NODE_DECORATORS);
 		}
 		return nodeDecorators;
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	public EList<EdgeDecorator> getEdgeDecorators() {
 		if (edgeDecorators == null) {
 			edgeDecorators = new EObjectResolvingEList<EdgeDecorator>(EdgeDecorator.class, this, ModelPackage.MODEL__EDGE_DECORATORS);
 		}
 		return edgeDecorators;
 	}
 
 	/**
 	 * 
 	 * 
 	 * This method combines all of the graph "fragments" together to make a
 	 * graph suitable for simulation.
 	 * 
 	 * 
 	 * 
 	 * @generated NOT
 	 */
 	public Graph getCanonicalGraph(final URI uri, IdentifiableFilter parentFilter, STEMTime time) {
 		IdentifiableFilterImpl _parentfilter = (IdentifiableFilterImpl)parentFilter;
 		final Graph retValue = GraphFactory.eINSTANCE.createGraph();
 		retValue.setURI(uri);
 
 		IdentifiableFilterImpl myFilter = new IdentifiableFilterImpl(this.getDublinCore().getCoverage());
 		if(parentFilter != null)myFilter.restrict(_parentfilter);	
 		
 		// Basically, we iterate through each of the models getting their
 		// canonical graphs and then merge them together. Then we add in the
 		// graphs of this model and finally "resolve" all the "dangling" links
 
 		for (final Iterator<Model> modelIter = getModels().iterator(); modelIter
 				.hasNext();) {
 			final Model model = (Model) modelIter.next();
 			retValue.addGraph(model.getCanonicalGraph(uri, myFilter, time), myFilter);
 		} // for models
 
 		// Now do the graphs that are part of this model
 		for (final Iterator<Graph> graphIter = getGraphs().iterator(); graphIter
 				.hasNext();) {
 			final Graph graph = (Graph) graphIter.next();
 
 			final Graph copyGraph = (Graph) EcoreUtil.copy(graph);
 			// final Graph graph = (Graph) graphIter.next();
 			// final Copier copier = new Copier();
 			// final Graph copyGraph = (Graph) copier.copy(graph);
 			// copier.copyReferences();
 			retValue.addGraph(copyGraph, myFilter);
 		} // for graphs
 
 		// Hook up the Identifiables with other ones they reference via URI's,
 		// if any URI are unresolved, then instances of UnresolvedIdentifiables
 		// will be added to the graph's collection of "unresolved
 		// identifiables".
 		resolveURIs(this, retValue);
 
 		invokeNodeDecorators(retValue, time);
 		invokeEdgeDecorators(retValue, time);
 		invokeGraphDecorators(retValue, time);
 
 		// Update the Dublin Core for this graph
 		final DublinCore dc = retValue.getDublinCore();
 		dc.populate();
 		dc.setFormat(GraphPackage.eNS_URI);
 
 		return retValue;
 	} // getCanonicalGraph
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void prepare(STEMTime time) {
 		// Iterate submodels and call prepare
 		for (final Iterator<Model> modelIter = getModels().iterator(); modelIter
 		.hasNext();) {
 			final Model model = (Model) modelIter.next();
 			model.prepare(time);
 		} // for models
 		
 		ArrayList<NodeDecorator>list = (ArrayList<NodeDecorator>)EcoreUtil.copyAll(getNodeDecorators());
 		for (final Iterator<NodeDecorator> nodeDecoratorIter = list.iterator(); nodeDecoratorIter
 		.hasNext();) {
 			final NodeDecorator nodeDecorator = (NodeDecorator) nodeDecoratorIter.next();
 			nodeDecorator.prepare(this, time);
 		}
 		
 		ArrayList<EdgeDecorator>list2 = (ArrayList<EdgeDecorator>)EcoreUtil.copyAll(getEdgeDecorators());
 		for (final Iterator<EdgeDecorator> edgeDecoratorIter = list2.iterator(); edgeDecoratorIter
 		.hasNext();) {
 			final EdgeDecorator edgeDecorator = (EdgeDecorator) edgeDecoratorIter.next();
 			edgeDecorator.prepare(this, time);
 		}
 		
 		ArrayList<GraphDecorator>list3 = (ArrayList<GraphDecorator>)EcoreUtil.copyAll(getGraphDecorators());
 		for (final Iterator<GraphDecorator> graphDecoratorIter = list3.iterator(); graphDecoratorIter
 		.hasNext();) {
 			final GraphDecorator graphDecorator = (GraphDecorator) graphDecoratorIter.next();
 			graphDecorator.prepare(this, time);
 		}
 
 	}
 
 	/**
 	 * Process all of the components of a graph and link up all unresolved URI
 	 * references in the graph.
 	 * 
 	 * @param model
 	 *            the model that contains the graph
 	 * @param graph
 	 *            a graph
 	 * 
 	 * @generated NOT
 	 */
 	private void resolveURIs(final Model model, final Graph graph) {
 
 		// TODO optimize by keeping a list of unresolved edges
 
 		// Go through each edge in the graph and try to hook it up with the
 		// nodes it references
 		for (final Iterator<Edge> edgeIter = graph.getEdges().values().iterator(); edgeIter
 				.hasNext();) {
 			final Edge edge = (Edge) edgeIter.next();
 
 			// Does Node A of this edge need resolving?
 			if (!edge.eIsSet(GraphPackage.Literals.EDGE__A)) {
 				// Yes
 				final Node nodeA = graph.getNode(edge.getNodeAURI());
 				// Did we find it?
 				if (nodeA != null) {
 					// Yes
 					edge.setA(nodeA);
 					nodeA.getEdges().add(edge);
 				} else {
 					// No
 					final UnresolvedIdentifiable ui = GraphFactory.eINSTANCE
 							.createUnresolvedIdentifiable();
 					ui.setModel(model);
 					ui.setGraph(graph);
 					ui.setUnresolvedURI(edge.getNodeAURI());
 					ui.setIdentifiable(edge);
 					ui.setFieldName("NODE_A");
 					if(ScenarioImpl.reportDanglingAirTransportEdges ||
 							!edge.getURI().toString().contains("/pipetransport"))
 						graph.getUnresolvedIdentifiables().add(ui);
 				} // else
 			} // if nodeA
 
 			// Does Node B of this edge need resolving?
 			if (!edge.eIsSet(GraphPackage.Literals.EDGE__B)
 					&& edge.eIsSet(GraphPackage.Literals.EDGE__A)) {
 				// Yes
 				final Node nodeB = graph.getNode(edge.getNodeBURI());
 				// Did we find it?
 				if (nodeB != null) {
 					// Yes
 					edge.setB(nodeB);
 					nodeB.getEdges().add(edge);
 				} else {
 					// No
 					// Was Node A found though?
 					if (edge.getA() != null) {
 						// Yes
 						// We remove our link with it then so that we don't have
 						// a dangling edge in the graph
 						final Node nodeA = edge.getA();
 						edge.setA(null);
 						nodeA.getEdges().remove(edge);
 					} // if Node A
 
 					final UnresolvedIdentifiable ui = GraphFactory.eINSTANCE
 							.createUnresolvedIdentifiable();
 					ui.setModel(model);
 					ui.setGraph(graph);
 					ui.setUnresolvedURI(edge.getNodeBURI());
 					ui.setIdentifiable(edge);
 					ui.setFieldName("NODE_B");
 					if(ScenarioImpl.reportDanglingAirTransportEdges ||
 							!edge.getURI().toString().contains("/pipetransport"))
 						graph.getUnresolvedIdentifiables().add(ui);
 				} // else
 			} // if nodeB
 		} // for each edge
 
 		// Now do the labels
 		for (final Iterator<NodeLabel> labelIter = graph.getNodeLabels().values()
 				.iterator(); labelIter.hasNext();) {
 			final NodeLabel label = (NodeLabel) labelIter.next();
 			// Can you find the Node for this label?
 			final Node node = graph.getNode(label
 					.getURIOfIdentifiableToBeLabeled());
 			if (node != null) {
 				// Yes
 				node.getLabels().add(label);
 			} else {
 				// No
 				final UnresolvedIdentifiable ui = GraphFactory.eINSTANCE
 						.createUnresolvedIdentifiable();
 				ui.setModel(model);
 				ui.setGraph(graph);
 				ui.setUnresolvedURI(label.getURIOfIdentifiableToBeLabeled());
 				ui.setIdentifiable(label);
 				ui.setFieldName("Node");
 				graph.getUnresolvedIdentifiables().add(ui);
 			}
 		} // for
 
 	} // resolveURIs
 
 	/**
 	 * Iterate through each node decorator and give it a chance to decorate the
 	 * nodes of the graph.
 	 * 
 	 * @param graph
 	 *            the {@link Graph} to decorate
 	 * @param time STEM time
 	 */
 	private void invokeNodeDecorators(Graph graph, STEMTime time) {
 		ArrayList<Decorator>failed = null; 
 		
 		// Stefan fix. We add the decorators to the graph first since we need to know
 		// about what decorators are available when we determine how to decorate when
 		// there are overlapping sets of nodes (e.g. for population models)
 		for (final Iterator<NodeDecorator> nodeDecoratorIter = getNodeDecorators().iterator(); nodeDecoratorIter
 		.hasNext();) {
 			final NodeDecorator nodeDecorator = (NodeDecorator) nodeDecoratorIter
 			.next();
 			final Decorator canonicalDecorator = (Decorator) EcoreUtil
 			.copy(nodeDecorator);
 			graph.getDecorators().add(canonicalDecorator);
 		}
 		for (Decorator canonicalDecorator:graph.getDecorators()) {
 			boolean success = canonicalDecorator.decorateGraph(time);
 			if(!success) {
 				if(failed == null)failed = new ArrayList<Decorator>();
 				failed.add(canonicalDecorator);
 			}
 		} // for
 		
 		// Redo failed decorators. This can happen due to dependencies, for instance a population model must be allowed to decorate
 		// before a disease model.	
 		// ASSUMING THE USER HAS BUILD THE MODEL WITH THE CORRECT HIERARCHY, THIS WILL NEVER HAPPEN
 		
		if(failed!=null)
 			for(Decorator d:failed)d.decorateGraph(time);
 	} // invokeNodeDecorators
 
 	/**
 	 * Iterate through each edge decorator and give it a chance to decorate the
 	 * edges of the graph.
 	 * 
 	 * @param graph
 	 *            the {@link Graph} to decorate
 	 * @param time
 	 */
 	private void invokeEdgeDecorators(final Graph graph, STEMTime time) {
 		for (final Iterator<EdgeDecorator> edgeDecoratorIter = getEdgeDecorators().iterator(); edgeDecoratorIter
 				.hasNext();) {
 			final EdgeDecorator edgeDecorator = (EdgeDecorator) edgeDecoratorIter
 					.next();
 			final Decorator canonicalDecorator = (Decorator) EcoreUtil
 					.copy(edgeDecorator);
 			graph.getDecorators().add(canonicalDecorator);
 			canonicalDecorator.decorateGraph(time);
 		} // for
 	} // invokeEdgeDecorators
 
 	/**
 	 * Iterate through each graph decorator and give it a chance to decorate the
 	 * graph itself.
 	 * 
 	 * @param graph
 	 *            the {@link Graph} to decorate
 	 * @param time
 	 */
 	private void invokeGraphDecorators(Graph graph, STEMTime time) {
 		for (final Iterator<GraphDecorator> graphDecoratorIter = getGraphDecorators()
 				.iterator(); graphDecoratorIter.hasNext();) {
 			final GraphDecorator graphDecorator = (GraphDecorator) graphDecoratorIter
 					.next();
 			final Decorator canonicalDecorator = (Decorator) EcoreUtil
 					.copy(graphDecorator);
 			graph.getDecorators().add(canonicalDecorator);
 			canonicalDecorator.decorateGraph(time);
 		} // for
 
 	} // invokeGraphDecorators
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case ModelPackage.MODEL__MODELS:
 				return getModels();
 			case ModelPackage.MODEL__GRAPHS:
 				return getGraphs();
 			case ModelPackage.MODEL__GRAPH_DECORATORS:
 				return getGraphDecorators();
 			case ModelPackage.MODEL__NODE_DECORATORS:
 				return getNodeDecorators();
 			case ModelPackage.MODEL__EDGE_DECORATORS:
 				return getEdgeDecorators();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case ModelPackage.MODEL__MODELS:
 				getModels().clear();
 				getModels().addAll((Collection<? extends Model>)newValue);
 				return;
 			case ModelPackage.MODEL__GRAPHS:
 				getGraphs().clear();
 				getGraphs().addAll((Collection<? extends Graph>)newValue);
 				return;
 			case ModelPackage.MODEL__GRAPH_DECORATORS:
 				getGraphDecorators().clear();
 				getGraphDecorators().addAll((Collection<? extends GraphDecorator>)newValue);
 				return;
 			case ModelPackage.MODEL__NODE_DECORATORS:
 				getNodeDecorators().clear();
 				getNodeDecorators().addAll((Collection<? extends NodeDecorator>)newValue);
 				return;
 			case ModelPackage.MODEL__EDGE_DECORATORS:
 				getEdgeDecorators().clear();
 				getEdgeDecorators().addAll((Collection<? extends EdgeDecorator>)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case ModelPackage.MODEL__MODELS:
 				getModels().clear();
 				return;
 			case ModelPackage.MODEL__GRAPHS:
 				getGraphs().clear();
 				return;
 			case ModelPackage.MODEL__GRAPH_DECORATORS:
 				getGraphDecorators().clear();
 				return;
 			case ModelPackage.MODEL__NODE_DECORATORS:
 				getNodeDecorators().clear();
 				return;
 			case ModelPackage.MODEL__EDGE_DECORATORS:
 				getEdgeDecorators().clear();
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc --> <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case ModelPackage.MODEL__MODELS:
 				return models != null && !models.isEmpty();
 			case ModelPackage.MODEL__GRAPHS:
 				return graphs != null && !graphs.isEmpty();
 			case ModelPackage.MODEL__GRAPH_DECORATORS:
 				return graphDecorators != null && !graphDecorators.isEmpty();
 			case ModelPackage.MODEL__NODE_DECORATORS:
 				return nodeDecorators != null && !nodeDecorators.isEmpty();
 			case ModelPackage.MODEL__EDGE_DECORATORS:
 				return edgeDecorators != null && !edgeDecorators.isEmpty();
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * @see org.eclipse.stem.common.impl.IdentifiableImpl#toString()
 	 */
 	@Override
 	public String toString() {
 		final StringBuilder sb = new StringBuilder("\"" + dublinCore.getTitle()
 				+ "\"\n");
 
 		sb.append(models != null ? models.size() + " models\n" : "");
 		sb.append(graphs != null ? graphs.size() + " graphs\n" : "");
 		sb.append(graphDecorators != null ? graphDecorators.size()
 				+ " graph decorators\n" : "");
 		sb.append(nodeDecorators != null ? nodeDecorators.size()
 				+ " node decorators\n" : "");
 		sb.append(edgeDecorators != null ? edgeDecorators.size()
 				+ "edge decorators\n" : "");
 
 		return sb.toString();
 	} // toString
 
 	/**
 	 * @see org.eclipse.stem.common.impl.IdentifiableImpl#sane()
 	 * @generated NOT
 	 */
 	@Override
 	public boolean sane() {
 		boolean retValue = super.sane();
 
 		if (retValue && models != null) {
 			for (final Iterator<Model> modelIter = models.iterator(); modelIter
 					.hasNext()
 					&& retValue;) {
 				final Model model = (Model) modelIter.next();
 				retValue = retValue && model.sane();
 				assert retValue;
 			} // for each model
 		}
 
 		if (retValue && graphs != null) {
 			for (final Iterator<Graph> graphIter = graphs.iterator(); graphIter
 					.hasNext()
 					&& retValue;) {
 				final Graph graph = (Graph) graphIter.next();
 				retValue = retValue && graph.sane();
 				assert retValue;
 			} // for each graph
 		}
 
 		if (retValue && graphDecorators != null) {
 			for (final Iterator<GraphDecorator> graphDecoratorIter = graphDecorators.iterator(); graphDecoratorIter
 					.hasNext()
 					&& retValue;) {
 				final GraphDecorator graphDecorator = (GraphDecorator) graphDecoratorIter
 						.next();
 				retValue = retValue && graphDecorator.sane();
 				assert retValue;
 			} // for each graphDecorator
 		} // if
 
 		if (retValue && nodeDecorators != null) {
 			for (Iterator<NodeDecorator> nodeDecoratorIter = nodeDecorators.iterator(); nodeDecoratorIter
 					.hasNext()
 					&& retValue;) {
 				NodeDecorator nodeDecorator = (NodeDecorator) nodeDecoratorIter
 						.next();
 				retValue = retValue && nodeDecorator.sane();
 				assert retValue;
 			} // for each noeDecorator
 		} // if
 
 		if (retValue && edgeDecorators != null) {
 			for (Iterator<EdgeDecorator> edgeDecoratorIter = edgeDecorators.iterator(); edgeDecoratorIter
 					.hasNext()
 					&& retValue;) {
 				EdgeDecorator edgeDecorator = (EdgeDecorator) edgeDecoratorIter
 						.next();
 				retValue = retValue && edgeDecorator.sane();
 				assert retValue;
 			} // for each edgeDecorator
 		}
 		return retValue;
 	} // sane
 } // ModelImpl
