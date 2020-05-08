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
 
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 
 import de.uni_koblenz.jgralab.Direction;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.schema.GraphElementClass;
 
 /**
  * This class provides an {@link Iterable} for the incident {@link GraphElement}
  * s at a given {@link GraphElement}.
  * 
  * @author ist@uni-koblenz.de
  */
 public abstract class IncidentGraphElementIterable<G extends GraphElement<?, ?>>
 		implements Iterable<G> {
 
 	/**
 	 * This class provides an {@link Iterator} for the incident
 	 * {@link GraphElement}s at a given {@link GraphElement}.
 	 * 
 	 * @author ist@uni-koblenz.de
 	 * 
 	 */
 	abstract class IncidentGraphElementIterator implements Iterator<G> {
 
 		/**
 		 * The current {@link Incidence}.
 		 */
 		protected Incidence current = null;
 
 		/**
 		 * {@link GraphElement} which incident {@link GraphElement}s are
 		 * iterated.
 		 */
 		protected GraphElement<?, ?> graphElement = null;
 
 		/**
 		 * The {@link Class} of the desired incident {@link GraphElement}s.
 		 */
 		protected Class<? extends GraphElement<?, ?>> gc;
 
 		/**
 		 * {@link Direction} of the desired incident {@link GraphElement}s.
 		 */
 		protected Direction dir;
 
 		/**
 		 * The version of the incidence list of the {@link GraphElement} at the
 		 * beginning of the iteration. This information is used to check if the
 		 * incidence list has changed. The failfast-{@link Iterator} will throw
 		 * an {@link ConcurrentModificationException} if the {@link #hasNext()}
 		 * or {@link #next()} is called.
 		 */
 		protected long incidenceListVersion;
 
 		/**
 		 * Creates an {@link Iterator} over the {@link Incidence}s of
 		 * <code>graphElement</code>.
 		 * 
 		 * @param graphElement
 		 *            {@link GraphElement} which {@link Incidence}s should be
 		 *            iterated.
 		 * @param gc
 		 *            {@link Class} only instances of this class are returned.
 		 * @param dir
 		 *            {@link Direction} of the desired {@link Incidence}s.
 		 */
		public IncidentGraphElementIterator(GraphElement<?, ?> graphElement,
				Class<? extends GraphElement<?, ?>> gc, Direction dir) {
 			this.graphElement = graphElement;
 			this.gc = gc;
 			this.dir = dir;
			incidenceListVersion = ((GraphElementImpl<?, ?>) graphElement)
 					.getIncidenceListVersion();
 			current = graphElement.getFirstIncidence(dir);
 		}
 
 		@Override
 		public boolean hasNext() {
 			checkConcurrentModification();
 			return current != null;
 		}
 
 		/**
 		 * Checks if the sequence of {@link Incidence}s was modified. In this
 		 * case a {@link ConcurrentModificationException} is thrown
 		 * 
 		 * @throws ConcurrentModificationException
 		 */
 		protected void checkConcurrentModification() {
 			if (((GraphElementImpl<?, ?>) graphElement)
 					.isIncidenceListModified(incidenceListVersion)) {
 				throw new ConcurrentModificationException(
 						"The incidence list of the GraphElement has been modified - the iterator is not longer valid");
 			}
 		}
 
 		/**
 		 * Sets {@link #current} to the next {@link Incidence} which is
 		 * connected to an {@link GraphElement} different from
 		 * {@link #graphElement} and of {@link GraphElementClass} {@link #gc}.
 		 * If such an element does not exist {@link #current} is set to
 		 * <code>null</code>.
 		 */
 		protected abstract void setCurrentToNextIncidentGraphElement();
 
 		@Override
 		public void remove() {
 			throw new UnsupportedOperationException(
 					"Cannot remove GraphElements using Iterator");
 		}
 
 	}
 
 	/**
 	 * The current {@link Iterator}.
 	 */
 	protected IncidentGraphElementIterator iter = null;
 
 	@Override
 	public Iterator<G> iterator() {
 		return iter;
 	}
 }
