 /*
  * JGraLab - The Java Graph Laboratory
  * 
  * Copyright (C) 2006-2010 Institute for Software Technology
  *                         University of Koblenz-Landau, Germany
  *                         ist@uni-koblenz.de
  * 
  * This program is free software; you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the
  * Free Software Foundation; either version 3 of the License, or (at your
  * option) any later version.
  * 
  * This program is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
  * Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses>.
  * 
  * Additional permission under GNU GPL version 3 section 7
  * 
  * If you modify this Program, or any covered work, by linking or combining
  * it with Eclipse (or a modified version of that program or an Eclipse
  * plugin), containing parts covered by the terms of the Eclipse Public
  * License (EPL), the licensors of this Program grant you additional
  * permission to convey the resulting work.  Corresponding Source for a
  * non-source form of such a combination shall include the source code for
  * the parts of JGraLab used as well as that of the covered work.
  */
 package de.uni_koblenz.jgralab.impl.std;
 
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 /**
  * The implementation of an {@link Edge} accessing attributes without
  * versioning.
  * 
  * @author Jose Monte(monte@uni-koblenz.de)
  */
 public abstract class EdgeImpl extends de.uni_koblenz.jgralab.impl.EdgeBaseImpl {
 	// global edge sequence
 	protected EdgeImpl nextEdgeInGraph;
 	protected EdgeImpl prevEdgeInGraph;
 
 	protected IncidenceImpl firstIncidenceAtEdge;
 	protected IncidenceImpl lastIncidenceAtEdge;
 
 	@Override
 	public Incidence getFirstIncidence(Graph traversalContext) {
 		Incidence firstIncidence = firstIncidenceAtEdge;
 		if (firstIncidence == null
 				|| !traversalContext.getContainingElement().containsElement(
 						this)) {
 			// all incidences belong to the same graph like this edge
 			return null;
 		} else {
 			return firstIncidence;
 		}
 	}
 
 	@Override
 	public Edge getNextEdge(Graph traversalContext) {
 		assert isValid();
 		Edge nextEdge = nextEdgeInGraph;
 		if (nextEdge == null
 				|| !traversalContext.getContainingElement().containsElement(
						nextEdge)) {
 			return null;
 		} else {
 			return nextEdge;
 		}
 	}
 
 	@Override
 	public Edge getPreviousEdge(Graph traversalContext) {
 		assert isValid();
 		Edge previousEdge = prevEdgeInGraph;
 		if (previousEdge == null
 				|| !traversalContext.getContainingElement().containsElement(
						previousEdge)) {
 			// all incidences belong to the same graph like this edge
 			return null;
 		} else {
 			return previousEdge;
 		}
 	}
 
 	@Override
 	public Incidence getLastIncidence(Graph traversalContext) {
 		Incidence lastIncidence = lastIncidenceAtEdge;
 		if (lastIncidence == null
 				|| !traversalContext.getContainingElement().containsElement(
 						this)) {
 			// all incidences belong to the same graph like this edge
 			return null;
 		} else {
 			return lastIncidence;
 		}
 	}
 
 	@Override
 	protected void setNextEdge(Edge nextEdge) {
 		nextEdgeInGraph = (EdgeImpl) nextEdge;
 	}
 
 	@Override
 	protected void setPrevEdge(Edge prevEdge) {
 		prevEdgeInGraph = (EdgeImpl) prevEdge;
 	}
 
 	@Override
 	protected void setPreviousEdge(Edge prevEdge) {
 		prevEdgeInGraph = (EdgeImpl) prevEdge;
 	}
 
 	@Override
 	public void setFirstIncidence(IncidenceImpl firstIncidence) {
 		firstIncidenceAtEdge = firstIncidence;
 	}
 
 	@Override
 	public void setLastIncidence(IncidenceImpl lastIncidence) {
 		lastIncidenceAtEdge = lastIncidence;
 	}
 
 	@Override
 	protected void setNextEdgeInGraph(Edge nextEdge) {
 		nextEdgeInGraph = (EdgeImpl) nextEdge;
 	}
 
 	@Override
 	protected void setPrevEdgeInGraph(Edge prevEdge) {
 		prevEdgeInGraph = (EdgeImpl) prevEdge;
 	}
 
 	/**
 	 * Creates a new {@link EdgeImpl} instance.
 	 * 
 	 * @param id
 	 *            int
 	 * @param graph
 	 *            {@link Graph}
 	 */
 	protected EdgeImpl(int anId, Graph graph) {
 		super(anId, graph);
 		((GraphImpl) graph).addEdge(this);
 	}
 
 	@Override
 	protected void setId(int id) {
 		assert id >= 0;
 		this.id = id;
 	}
 
 	/**
 	 * Checks if this {@link Edge} is a binary edge. An edge is called binary,
 	 * if {@link Edge#getType()} is connected to exactly two {@link VertexClass}
 	 * es by a multiplicity of 1. One of the two {@link Incidence}s must have
 	 * the direction {@link Direction#EDGE_TO_VERTEX} and the other
 	 * {@link Direction#VERTEX_TO_EDGE}.
 	 * 
 	 * @return boolean <code>true</code> if this {@link Edge} is a binary edge.
 	 */
 	@Override
 	public boolean isBinary() {
 		return false;
 	}
 
 	@Override
 	public Graph getSubordinateGraph() {
 		if (subOrdinateGraph != null) {
 			return subOrdinateGraph;
 		}
 		return new SubordinateGraphImpl(this);// TODO
 	}
 
 }
