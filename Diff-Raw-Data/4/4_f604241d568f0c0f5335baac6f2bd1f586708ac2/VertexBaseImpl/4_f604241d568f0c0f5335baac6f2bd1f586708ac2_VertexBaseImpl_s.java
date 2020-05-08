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
 
 package de.uni_koblenz.jgralab.impl;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.NoSuchElementException;
 import java.util.Queue;
 import java.util.Set;
 
 import de.uni_koblenz.jgralab.AttributedElement;
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.PathElement;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.impl.std.IncidenceImpl;
 import de.uni_koblenz.jgralab.impl.std.VertexImpl;
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 import de.uni_koblenz.jgralab.schema.IncidenceClass;
 import de.uni_koblenz.jgralab.schema.IncidenceType;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 /**
  * Implementation of all methods of the interface {@link Vertex} which are
  * independent of the fields of a specific VertexImpl.
  * 
  * @author ist@uni-koblenz.de
  */
 public abstract class VertexBaseImpl extends GraphElementImpl<Vertex, Edge>
 		implements Vertex {
 
 	/**
 	 * Creates a new {@link Vertex} instance.
 	 * 
 	 * @param id
 	 *            int the id of the vertex
 	 * @param graph
 	 *            {@link Graph} its corresponding graph
 	 */
 	protected VertexBaseImpl(int id, Graph graph) {
 		super(graph);
 		this.id = id;
 	}
 
 	@Override
 	public Incidence getFirstIncidence(Direction direction) {
 		assert isValid();
 		Incidence i = getFirstIncidence();
 		while ((i != null) && direction != null
 				&& i.getDirection() != direction) {
 			i = i.getNextIncidenceAtVertex();
 		}
 		return i;
 	}
 
 	@Override
 	public Incidence getFirstIncidence(boolean thisIncidence,
 			IncidenceType... incidentTypes) {
 		assert isValid();
 		Incidence i = getFirstIncidence();
 		if (incidentTypes.length == 0) {
 			return i;
 		}
 		while (i != null) {
 			for (IncidenceType element : incidentTypes) {
 				if ((thisIncidence ? i.getThisSemantics() : i
 						.getThatSemantics()) == element) {
 					return i;
 				}
 			}
 			i = i.getNextIncidenceAtVertex();
 		}
 		return null;
 	}
 
 	@Override
 	public Incidence getFirstIncidence(
 			Class<? extends Incidence> anIncidenceClass, Direction direction,
 			boolean noSubclasses) {
 		assert anIncidenceClass != null;
 		assert isValid();
 		Incidence currentIncidence = getFirstIncidence(direction);
 		while (currentIncidence != null) {
 			if (noSubclasses) {
 				if (anIncidenceClass == currentIncidence.getM1Class()) {
 					return currentIncidence;
 				}
 			} else {
 				if (anIncidenceClass.isInstance(currentIncidence)) {
 					return currentIncidence;
 				}
 			}
 			currentIncidence = currentIncidence
 					.getNextIncidenceAtVertex(direction);
 		}
 		return null;
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences() {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this, direction);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(
 			Class<? extends Incidence> anIncidenceClass) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this, anIncidenceClass);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(IncidenceClass anIncidenceClass) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this,
 				anIncidenceClass.getM1Class());
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(
 			Class<? extends Incidence> anIncidenceClass, Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this, anIncidenceClass,
 				direction);
 	}
 
 	@Override
 	public Iterable<Incidence> getIncidences(IncidenceClass anIncidenceClass,
 			Direction direction) {
 		assert isValid();
 		return new IncidenceIterableAtVertex<Incidence>(this,
 				anIncidenceClass.getM1Class(), direction);
 	}
 
 	@Override
 	public Vertex getNextVertex(Class<? extends Vertex> vertexClass) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(vertexClass, false);
 	}
 
 	@Override
 	public Vertex getNextVertex(Class<? extends Vertex> m1VertexClass,
 			boolean noSubclasses) {
 		assert m1VertexClass != null;
 		assert isValid();
 		VertexBaseImpl v = (VertexBaseImpl) getNextVertex();
 		while (v != null) {
 			if (noSubclasses) {
 				if (m1VertexClass == v.getM1Class()) {
 					return v;
 				}
 			} else {
 				if (m1VertexClass.isInstance(v)) {
 					return v;
 				}
 			}
 			v = (VertexBaseImpl) v.getNextVertex();
 		}
 		return null;
 	}
 
 	@Override
 	public Vertex getNextVertex(VertexClass vertexClass) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(vertexClass.getM1Class(), false);
 	}
 
 	@Override
 	public Vertex getNextVertex(VertexClass vertexClass, boolean noSubclasses) {
 		assert vertexClass != null;
 		assert isValid();
 		return getNextVertex(vertexClass.getM1Class(), noSubclasses);
 	}
 
 	@Override
 	public Iterable<Edge> getAlphaEdges() {
 		return new IncidentEdgeIterable<Edge>(this, Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public Iterable<Edge> getAlphaEdges(EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class(),
 				Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public Iterable<Edge> getAlphaEdges(Class<? extends Edge> anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass,
 				Direction.EDGE_TO_VERTEX);
 	}
 
 	@Override
 	public Iterable<Edge> getOmegaEdges() {
 		return new IncidentEdgeIterable<Edge>(this, Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public Iterable<Edge> getOmegaEdges(EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class(),
 				Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public Iterable<Edge> getOmegaEdges(Class<? extends Edge> anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass,
 				Direction.VERTEX_TO_EDGE);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges() {
 		return new IncidentEdgeIterable<Edge>(this);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Direction direction) {
 		return new IncidentEdgeIterable<Edge>(this, direction);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(EdgeClass anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class());
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Class<? extends Edge> anEdgeClass) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(EdgeClass anEdgeClass,
 			Direction direction) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass.getM1Class(),
 				direction);
 	}
 
 	@Override
 	public Iterable<Edge> getIncidentEdges(Class<? extends Edge> anEdgeClass,
 			Direction direction) {
 		return new IncidentEdgeIterable<Edge>(this, anEdgeClass, direction);
 	}
 
 	@Override
 	public boolean isValid() {
 		return graph.containsVertex(this);
 	}
 
 	@Override
 	public boolean isBefore(Vertex v) {
 		assert v != null;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		if (this == v) {
 			return false;
 		}
 		Vertex prev = v.getPreviousVertex();
 		while ((prev != null) && (prev != this)) {
 			prev = v.getPreviousVertex();
 		}
 		return prev != null;
 	}
 
 	@Override
 	public void putBefore(Vertex v) {
 		assert v != null;
 		assert v != this;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		graph.putVertexBefore((VertexBaseImpl) v, this);
 	}
 
 	@Override
 	public boolean isAfter(Vertex v) {
 		assert v != null;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		if (this == v) {
 			return false;
 		}
 		VertexBaseImpl next = (VertexBaseImpl) v.getNextVertex();
 		while ((next != null) && (next != this)) {
 			next = (VertexBaseImpl) next.getNextVertex();
 		}
 		return next != null;
 	}
 
 	@Override
 	public void putAfter(Vertex v) {
 		assert v != null;
 		assert v != this;
 		assert getGraph() == v.getGraph();
 		assert isValid() && v.isValid();
 		graph.putVertexAfter((VertexBaseImpl) v, this);
 	}
 
 	/**
 	 * Puts <code>nextVertex</code> after this {@link Vertex} in the sequence of
 	 * all vertices in the graph.
 	 * 
 	 * @param nextVertex
 	 *            {@link Vertex}which should be put after this {@link Vertex}
 	 */
 	protected abstract void setNextVertex(Vertex nextVertex);
 
 	/**
 	 * Puts <code>prevVertex</code> before this {@link Vertex} in the sequence
 	 * of all vertices in the graph.
 	 * 
 	 * @param prevVertex
 	 *            {@link Vertex}which should be put before this {@link Vertex}
 	 */
 	protected abstract void setPrevVertex(Vertex prevVertex);
 
 	@Override
 	public int getDegree() {
 		int d = 0;
 		Incidence i = getFirstIncidence();
 		while (i != null) {
 			d++;
 			i = i.getNextIncidenceAtVertex();
 		}
 		return d;
 	}
 
 	@Override
 	public int getDegree(Direction direction) {
 		if (direction == null) {
 			return getDegree();
 		}
 		int d = 0;
 		Incidence i = getFirstIncidence();
 		while (i != null) {
 			if (i.getDirection() == direction) {
 				d++;
 			}
 			i = i.getNextIncidenceAtVertex();
 		}
 		return d;
 	}
 
 	@Override
 	public int getDegree(IncidenceClass ic, boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(ic, noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(ic, noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public int getDegree(Class<? extends Incidence> ic, boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(ic, noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(ic, noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public int getDegree(IncidenceClass ic, Direction direction,
 			boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(ic, direction, noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(ic, direction, noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public int getDegree(Class<? extends Incidence> ic, Direction direction,
 			boolean noSubClasses) {
 		assert ic != null;
 		assert isValid();
 		int degree = 0;
 		Incidence i = getFirstIncidence(ic, direction, noSubClasses);
 		while (i != null) {
 			++degree;
 			i = i.getNextIncidenceAtVertex(ic, direction, noSubClasses);
 		}
 		return degree;
 	}
 
 	@Override
 	public String toString() {
 		assert isValid();
 		return "v" + id + ": " + getAttributedElementClass().getQualifiedName();
 	}
 
 	@Override
 	public int compareTo(AttributedElement a) {
 		assert a instanceof Vertex;
 		Vertex v = (Vertex) a;
 		assert isValid() && v.isValid();
 		assert getGraph() == v.getGraph();
 		return getId() - v.getId();
 	}
 
 	@Override
 	public void delete() {
 		assert isValid() : this + " is not valid!";
 		graph.deleteVertex(this);
 	}
 
 	@Override
 	protected void putIncidenceAfter(IncidenceImpl target, IncidenceImpl moved) {
 		assert (target != null) && (moved != null);
 		// TODO adapt to hierarchical graphs
 		assert target.getGraph() == moved.getGraph();
 		assert target.getGraph() == getGraph();
 		assert target.getThis() == moved.getThis();
 		assert target != moved;
 
 		if ((target == moved) || (target.getNextIncidenceAtVertex() == moved)) {
 			return;
 		}
 
 		// there are at least 2 incidences in the incidence list
 		// such that firstIncidence != lastIncidence
 		assert getFirstIncidence() != getLastIncidence();
 
 		// remove moved incidence from lambdaSeq
 		if (moved == getFirstIncidence()) {
 			setFirstIncidence((IncidenceImpl) moved.getNextIncidenceAtVertex());
 			if (!graph.hasSavememSupport()) {
 				((IncidenceImpl) moved.getNextIncidenceAtVertex())
 						.setPreviousIncidenceAtVertex(null);
 			}
 		} else if (moved == getLastIncidence()) {
 			setLastIncidence((IncidenceImpl) moved
 					.getPreviousIncidenceAtVertex());
 			((IncidenceImpl) moved.getPreviousIncidenceAtVertex())
 					.setNextIncidenceAtVertex(null);
 		} else {
 			((IncidenceImpl) moved.getPreviousIncidenceAtVertex())
 					.setNextIncidenceAtVertex((IncidenceImpl) moved
 							.getNextIncidenceAtVertex());
 			if (!graph.hasSavememSupport()) {
 				((IncidenceImpl) moved.getNextIncidenceAtVertex())
 						.setPreviousIncidenceAtVertex((IncidenceImpl) moved
 								.getPreviousIncidenceAtVertex());
 			}
 		}
 
 		// insert moved incidence in lambdaSeq immediately after target
 		if (target == getLastIncidence()) {
 			setLastIncidence(moved);
 			moved.setNextIncidenceAtVertex(null);
 		} else {
 			if (!graph.hasSavememSupport()) {
 				((IncidenceImpl) target.getNextIncidenceAtVertex())
 						.setPreviousIncidenceAtVertex(moved);
 			}
 			moved.setNextIncidenceAtVertex((IncidenceImpl) target
 					.getNextIncidenceAtVertex());
 		}
 		if (!graph.hasSavememSupport()) {
 			moved.setPreviousIncidenceAtVertex(target);
 		}
 		target.setNextIncidenceAtVertex(moved);
 		incidenceListModified();
 	}
 
 	@Override
 	protected void putIncidenceBefore(IncidenceImpl target, IncidenceImpl moved) {
 		assert (target != null) && (moved != null);
 		// TODO adapt to hierarchical graphs
 		assert target.getGraph() == moved.getGraph();
 		assert target.getGraph() == getGraph();
 		assert target.getThis() == moved.getThis();
 		assert target != moved;
 
 		if ((target == moved)
 				|| (target.getPreviousIncidenceAtVertex() == moved)) {
 			return;
 		}
 
 		// there are at least 2 incidences in the incidence list
 		// such that firstIncidence != lastIncidence
 		assert getFirstIncidence() != getLastIncidence();
 
 		// remove moved incidence from lambdaSeq
 		if (moved == getFirstIncidence()) {
 			setFirstIncidence((IncidenceImpl) moved.getNextIncidenceAtVertex());
 			if (!graph.hasSavememSupport()) {
 				((IncidenceImpl) moved.getNextIncidenceAtVertex())
 						.setPreviousIncidenceAtVertex(null);
 			}
 		} else if (moved == getLastIncidence()) {
 			setLastIncidence((IncidenceImpl) moved
 					.getPreviousIncidenceAtVertex());
 			((IncidenceImpl) moved.getPreviousIncidenceAtVertex())
 					.setNextIncidenceAtVertex(null);
 		} else {
 			((IncidenceImpl) moved.getPreviousIncidenceAtVertex())
 					.setNextIncidenceAtVertex((IncidenceImpl) moved
 							.getNextIncidenceAtVertex());
 			if (!graph.hasSavememSupport()) {
 				((IncidenceImpl) moved.getNextIncidenceAtVertex())
 						.setPreviousIncidenceAtVertex((IncidenceImpl) moved
 								.getPreviousIncidenceAtVertex());
 			}
 		}
 
 		// insert moved incidence in lambdaSeq immediately before target
 		if (target == getFirstIncidence()) {
 			setFirstIncidence(moved);
 			if (!graph.hasSavememSupport()) {
 				moved.setPreviousIncidenceAtVertex(null);
 			}
 		} else {
 			IncidenceImpl previousIncidence = (IncidenceImpl) target
 					.getPreviousIncidenceAtVertex();
 			previousIncidence.setNextIncidenceAtVertex(moved);
 			if (!graph.hasSavememSupport()) {
 				moved.setPreviousIncidenceAtVertex(previousIncidence);
 			}
 		}
 		moved.setNextIncidenceAtVertex(target);
 		if (!graph.hasSavememSupport()) {
 			target.setPreviousIncidenceAtVertex(moved);
 		}
 		incidenceListModified();
 	}
 
 	@Override
 	protected void appendIncidenceToLambdaSeq(IncidenceImpl i) {
 		assert i != null;
 		assert i.getVertex() != this;
 		i.setIncidentVertex((VertexImpl) this);
 		i.setNextIncidenceAtVertex(null);
 		if (getFirstIncidence() == null) {
 			setFirstIncidence(i);
 		}
 		if (getLastIncidence() != null) {
 			((IncidenceImpl) getLastIncidence()).setNextIncidenceAtVertex(i);
 			if (!graph.hasSavememSupport()) {
 				i.setPreviousIncidenceAtVertex((IncidenceImpl) getLastIncidence());
 			}
 		}
 		setLastIncidence(i);
 		incidenceListModified();
 	}
 
 	@Override
 	protected void removeIncidenceFromLambdaSeq(IncidenceImpl i) {
 		assert i != null;
 		assert i.getVertex() == this;
 		if (i == getFirstIncidence()) {
 			// delete at head of incidence list
 			setFirstIncidence((IncidenceImpl) i.getNextIncidenceAtVertex());
 			if (getFirstIncidence() != null && !graph.hasSavememSupport()) {
 				((IncidenceImpl) getFirstIncidence())
 						.setPreviousIncidenceAtVertex(null);
 			}
 			if (i == getLastIncidence()) {
 				// this incidence was the only one...
 				setLastIncidence(null);
 			}
 		} else if (i == getLastIncidence()) {
 			// delete at tail of incidence list
 			setLastIncidence((IncidenceImpl) i.getPreviousIncidenceAtVertex());
 			if (getLastIncidence() != null) {
 				((IncidenceImpl) getLastIncidence())
 						.setNextIncidenceAtVertex(null);
 			}
 		} else {
 			// delete somewhere in the middle
 			((IncidenceImpl) i.getPreviousIncidenceAtVertex())
 					.setNextIncidenceAtVertex((IncidenceImpl) i
 							.getNextIncidenceAtVertex());
 			if (!graph.hasSavememSupport()) {
 				((IncidenceImpl) i.getNextIncidenceAtVertex())
 						.setPreviousIncidenceAtVertex((IncidenceImpl) i
 								.getPreviousIncidenceAtVertex());
 			}
 		}
 		// delete incidence
 		i.setIncidentVertex(null);
 		i.setNextIncidenceAtVertex(null);
 		if (!graph.hasSavememSupport()) {
 			i.setPreviousIncidenceAtVertex(null);
 		}
 		incidenceListModified();
 	}
 
 	@Override
 	public void sortIncidences(Comparator<Incidence> comp) {
 		assert isValid();
 
 		if (getFirstIncidence() == null) {
 			// no sorting required for empty incidence lists
 			return;
 		}
 		class IncidenceList {
 			IncidenceImpl first;
 			IncidenceImpl last;
 
 			public void add(IncidenceImpl i) {
 				if (first == null) {
 					first = i;
 					assert (last == null);
 					last = i;
 				} else {
 					if (!graph.hasSavememSupport()) {
 						i.setPreviousIncidenceAtVertex(last);
 					}
 					last.setNextIncidenceAtVertex(i);
 					last = i;
 				}
 				i.setNextIncidenceAtVertex(null);
 			}
 
 			public IncidenceImpl remove() {
 				if (first == null) {
 					throw new NoSuchElementException();
 				}
 				IncidenceImpl out;
 				if (first == last) {
 					out = first;
 					first = null;
 					last = null;
 					return out;
 				}
 				out = first;
 				first = (IncidenceImpl) out.getNextIncidenceAtVertex();
 				if (!graph.hasSavememSupport()) {
 					first.setPreviousIncidenceAtVertex(null);
 				}
 				return out;
 			}
 
 			public boolean isEmpty() {
 				assert ((first == null) == (last == null));
 				return first == null;
 			}
 
 		}
 
 		IncidenceList a = new IncidenceList();
 		IncidenceList b = new IncidenceList();
 		IncidenceList out = a;
 
 		// split
 		IncidenceImpl last;
 		IncidenceList l = new IncidenceList();
 		l.first = (IncidenceImpl) getFirstIncidence();
 		l.last = (IncidenceImpl) getLastIncidence();
 
 		out.add(last = l.remove());
 		while (!l.isEmpty()) {
 			IncidenceImpl current = l.remove();
 			if (comp.compare(current, last) < 0) {
 				out = (out == a) ? b : a;
 			}
 			out.add(current);
 			last = current;
 		}
 		if (a.isEmpty() || b.isEmpty()) {
 			out = a.isEmpty() ? b : a;
 			setFirstIncidence(out.first);
 			setLastIncidence(out.last);
 			return;
 		}
 
 		while (true) {
 			if (a.isEmpty() || b.isEmpty()) {
 				out = a.isEmpty() ? b : a;
 				setFirstIncidence(out.first);
 				setLastIncidence(out.last);
 				incidenceListModified();
 				return;
 			}
 
 			IncidenceList c = new IncidenceList();
 			IncidenceList d = new IncidenceList();
 			out = c;
 
 			last = null;
 			while (!a.isEmpty() && !b.isEmpty()) {
 				int compareAToLast = last != null ? comp.compare(a.first, last)
 						: 0;
 				int compareBToLast = last != null ? comp.compare(b.first, last)
 						: 0;
 
 				if ((compareAToLast >= 0) && (compareBToLast >= 0)) {
 					if (comp.compare(a.first, b.first) <= 0) {
 						out.add(last = a.remove());
 					} else {
 						out.add(last = b.remove());
 					}
 				} else if ((compareAToLast < 0) && (compareBToLast < 0)) {
 					out = (out == c) ? d : c;
 					last = null;
 				} else if ((compareAToLast < 0) && (compareBToLast >= 0)) {
 					out.add(last = b.remove());
 				} else {
 					out.add(last = a.remove());
 				}
 			}
 
 			// copy rest of A
 			while (!a.isEmpty()) {
 				IncidenceImpl current = a.remove();
 				if (comp.compare(current, last) < 0) {
 					out = (out == c) ? d : c;
 				}
 				out.add(current);
 				last = current;
 			}
 
 			// copy rest of B
 			while (!b.isEmpty()) {
 				IncidenceImpl current = b.remove();
 				if (comp.compare(current, last) < 0) {
 					out = (out == c) ? d : c;
 				}
 				out.add(current);
 				last = current;
 			}
 
 			a = c;
 			b = d;
 		}
 
 	}
 
 	@Override
 	public List<? extends Vertex> adjacences(String role) {
 		return adjacences(getIncidenceClassForRolename(role));
 	}
 
 	@Override
 	public List<? extends Vertex> adjacences(IncidenceClass ic) {
 		assert ic != null;
 		assert isValid();
 		List<Vertex> adjacences = new ArrayList<Vertex>();
 		Class<? extends Edge> ec = ic.getEdgeClass().getM1Class();
 		Direction dir = ic.getDirection();
 		for (Edge e : getIncidentEdges(ec, dir)) {
 			for (Vertex v : e
 					.getIncidentVertices(dir == Direction.EDGE_TO_VERTEX ? Direction.VERTEX_TO_EDGE
 							: Direction.EDGE_TO_VERTEX)) {
 				adjacences.add(v);
 			}
 		}
 		return adjacences;
 	}
 
 	@Override
 	public Edge addAdjacence(String role, Vertex other) {
 		return addAdjacence(getIncidenceClassForRolename(role), other);
 	}
 
 	@Override
 	public Edge addAdjacence(IncidenceClass ic, Vertex other) {
 		// TODO there should exists methods of type addIncident(..) which should
 		// be used (graph and incidencelists modified)
 		return null;
 		// assert (role != null) && (role.length() > 0);
 		// assert isValid();
 		// assert other.isValid();
 		// assert getGraph() == other.getGraph();
 		//
 		// DirectedM1EdgeClass entry = getEdgeForRolename(role);
 		// Class<? extends Edge> ec = entry.getM1Class();
 		// Direction dir = entry.getDirection();
 		// Vertex from = null;
 		// Vertex to = null;
 		// if (dir == Direction.IN) {
 		// from = other;
 		// to = this;
 		// } else {
 		// to = other;
 		// from = this;
 		// }
 		// Edge e = getGraph().createEdge(ec, from, to);
 		// return e;
 	}
 
 	@Override
 	public List<Vertex> removeAdjacences(String role) {
 		return removeAdjacences(getIncidenceClassForRolename(role));
 	}
 
 	@Override
 	public List<Vertex> removeAdjacences(IncidenceClass ic) {
 		// TODO (graph and incidencelists modified)
 		return null;
 		// assert (role != null) && (role.length() > 0);
 		// assert isValid();
 		//
 		// DirectedM1EdgeClass entry = getEdgeForRolename(role);
 		// Class<? extends Edge> ec = entry.getM1Class();
 		// List<Vertex> adjacences = new ArrayList<Vertex>();
 		// List<Edge> deleteList = new ArrayList<Edge>();
 		// Direction dir = entry.getDirection();
 		// for (Edge e : incidences(ec, dir)) {
 		// deleteList.add(e);
 		// adjacences.add(e.getThat());
 		// }
 		// for (Edge e : deleteList) {
 		// e.delete();
 		// }
 		// return adjacences;
 	}
 
 	@Override
 	public void removeAdjacence(String role, Vertex other) {
 		removeAdjacence(getIncidenceClassForRolename(role), other);
 	}
 
 	@Override
 	public void removeAdjacence(IncidenceClass ic, Vertex other) {
 		// TODO (graph and incidencelists modified)
 		// assert (role != null) && (role.length() > 0);
 		// assert isValid();
 		// assert other.isValid();
 		// assert getGraph() == other.getGraph();
 		//
 		// DirectedM1EdgeClass entry = getEdgeForRolename(role);
 		// Class<? extends Edge> ec = entry.getM1Class();
 		// List<Edge> deleteList = new ArrayList<Edge>();
 		// Direction dir = entry.getDirection();
 		// for (Edge e : incidences(ec, dir)) {
 		// if (e.getThat() == other) {
 		// deleteList.add(e);
 		// }
 		// }
 		// for (Edge e : deleteList) {
 		// e.delete();
 		// }
 	}
 
 	@Override
 	public synchronized <T extends Vertex> List<T> reachableVertices(
 			String pathDescription, Class<T> vertexType) {
 		return graph.reachableVertices(this, pathDescription, vertexType);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Vertex> Set<T> reachableVertices(Class<T> returnType,
 			PathElement... pathElements) {
 		Set<T> result = new LinkedHashSet<T>();
 		Queue<Vertex> q = new LinkedList<Vertex>();
 		q.add(this);
 
 		for (int i = 0; i < pathElements.length; i++) {
 			PathElement t = pathElements[i];
 			// the null marks the end of the iteration with PathElement t
 			q.add(null);
 			Vertex vx = q.poll();
 			while (vx != null) {
 				for (Edge e : vx.getIncidentEdges(t.edgeClass, t.direction)) {
 					if (!t.strictType
 							|| (t.strictType && (t.edgeClass == e.getM1Class()))) {
 						for (Incidence inci : e
 								.getIncidences(t.direction == Direction.EDGE_TO_VERTEX ? Direction.VERTEX_TO_EDGE
 										: Direction.EDGE_TO_VERTEX)) {
 							if (i == pathElements.length - 1) {
 								Vertex r = inci.getVertex();
 								if (returnType.isInstance(r)) {
 									result.add((T) r);
 								}
 							} else {
								q.add(inci.getVertex());
 							}
 						}
 					}
 				}
 				vx = q.poll();
 			}
 		}
 		return result;
 	}
 
 	@Override
 	public void connect(String rolename, Edge elemToConnect) {
 		connect(getIncidenceClassForRolename(rolename), elemToConnect);
 	}
 
 	@Override
 	public void connect(IncidenceClass incidenceClass, Edge elemToConnect) {
 		connect(incidenceClass.getM1Class(), elemToConnect);
 	}
 
 	@Override
 	public void connect(Class<? extends Incidence> incidenceClass,
 			Edge elemToConnect) {
 		getSchema().getGraphFactory().createIncidence(incidenceClass, this,
 				elemToConnect);
 	}
 
 }
