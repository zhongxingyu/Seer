 /*******************************************************************************
  * Copyright (c) 2008 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.ui.editor.highlighting;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.jface.text.Position;
 
 /**
  * Abstract implementation of the {@link ISemanticHighlightingUpdater}.
  * 
  * Descendant classes should override
  * {@link #doHighlighting(org.eclipse.dltk.compiler.env.ISourceModule)} and call
  * {@link #addPosition(int, int, int)} to highlight specified regions.
  * 
  * Comparing old and new positions is performed in this class and calculated
  * "delta" is returned from the
  * {@link #reconcile(org.eclipse.dltk.compiler.env.ISourceModule, HighlightedPosition[])}
  * method.
  */
 public abstract class AbstractSemanticHighlighter implements
 		ISemanticHighlightingUpdater, ISemanticHighlightingRequestor {
 
 	private IHighlightedPositionFactory positionFactory;
 	private Map<String, HighlightingStyle> highlightingStyles = new HashMap<String, HighlightingStyle>();
 
 	public void initialize(IHighlightedPositionFactory factory,
 			HighlightingStyle[] styles) {
 		this.positionFactory = factory;
 		this.highlightingStyles.clear();
		for (HighlightingStyle style : styles) {
			this.highlightingStyles.put(style.getSemaHighlighting()
					.getPreferenceKey(), style);
 		}
 	}
 
 	private final List<HighlightedPosition> newPositions = new ArrayList<HighlightedPosition>();
 	private int oldPositionCount = 0;
 	private final List<HighlightedPosition> oldPositions = new ArrayList<HighlightedPosition>();
 
 	public UpdateResult reconcile(IModuleSource code,
 			List<HighlightedPosition> currentPositions) {
 		try {
 			newPositions.clear();
 			this.oldPositionCount = currentPositions.size();
 			this.oldPositions.clear();
 			this.oldPositions.addAll(currentPositions);
 			if (doHighlighting(code)) {
 				checkNewPositionOrdering();
 				final HighlightedPosition[] removed = getRemovedPositions();
 				if (DEBUG) {
 					System.out
 							.println("Add:" + newPositions.size() + " " + newPositions); //$NON-NLS-1$ //$NON-NLS-2$
 					System.out
 							.println("Remove:" + removed.length + " " + Arrays.asList(removed)); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				return new UpdateResult(getAddedPositions(), removed);
 			}
 		} catch (Exception e) {
 			DLTKCore.error("Error in SemanticPositionUpdater", e); //$NON-NLS-1$
 		}
 		return new UpdateResult(HighlightedPosition.NO_POSITIONS,
 				HighlightedPosition.NO_POSITIONS);
 	}
 
 	/**
 	 * This method should do all of the semantic highlighting. When something
 	 * should be highlighted
 	 * 
 	 * @param code
 	 * @return
 	 * @throws Exception
 	 */
 	protected abstract boolean doHighlighting(IModuleSource code)
 			throws Exception;
 
 	public void addPosition(int start, int end, String highlightingKey) {
 		final int len = end - start;
 		if (len <= 0) {
 			return;
 		}
 		final HighlightingStyle hl = highlightingStyles.get(highlightingKey);
 		if (hl == null) {
 			return;
 		}
 		for (int i = 0, size = oldPositions.size(); i < size; ++i) {
 			final HighlightedPosition p = oldPositions.get(i);
 			if (p != null && p.isEqual(start, len, hl)) {
 				oldPositions.set(i, null);
 				--oldPositionCount;
 				return;
 			}
 		}
 		if (!newPositions.isEmpty()) {
 			final int lowBound = Math.max(newPositions.size() - 2, 0);
 			for (int i = newPositions.size(); --i >= lowBound;) {
 				final HighlightedPosition p = newPositions.get(i);
 				if (p.isEqual(start, len, hl)) {
 					if (DEBUG) {
 						System.err
 								.println("WARN: duplicate in new positions [" + start + "+" + len + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 					}
 					return;
 				}
 			}
 		}
 		final HighlightedPosition hp = positionFactory
 				.createHighlightedPosition(start, len, hl);
 		newPositions.add(hp);
 		if (DEBUG) {
 			checkNewPositionOrdering();
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	protected HighlightedPosition[] getAddedPositions() {
 		final HighlightedPosition[] result = new HighlightedPosition[newPositions
 				.size()];
 		newPositions.toArray(result);
 		return result;
 	}
 
 	protected HighlightedPosition[] getRemovedPositions() {
 		final HighlightedPosition[] result = new HighlightedPosition[oldPositionCount];
 		int index = 0;
 		for (int i = 0, size = oldPositions.size(); i < size; ++i) {
 			final HighlightedPosition p = oldPositions.get(i);
 			if (p != null) {
 				result[index++] = p;
 			}
 		}
 		return result;
 	}
 
 	protected void checkNewPositionOrdering() {
 		if (newPositions.isEmpty())
 			return;
 		Collections.sort(newPositions, new Comparator<HighlightedPosition>() {
 			public int compare(HighlightedPosition p1, HighlightedPosition p2) {
 				return p1.getOffset() - p2.getOffset();
 			}
 		});
 		Position previous = null;
 		for (Iterator<HighlightedPosition> i = newPositions.iterator(); i
 				.hasNext();) {
 			final Position current = i.next();
 			if (previous != null
 					&& previous.getOffset() + previous.getLength() > current
 							.getOffset()) {
 				if (DEBUG) {
 					System.err.println("ERROR: unordered position " + current); //$NON-NLS-1$
 				}
 				i.remove();
 			} else {
 				previous = current;
 			}
 		}
 	}
 
 	private static final boolean DEBUG = false;
 
 }
