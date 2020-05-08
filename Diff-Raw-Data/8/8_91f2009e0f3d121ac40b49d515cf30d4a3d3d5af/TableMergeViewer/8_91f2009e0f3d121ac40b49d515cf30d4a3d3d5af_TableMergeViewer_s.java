 /*******************************************************************************
  * Copyright (c) 2012 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.table;
 
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Lists.newArrayList;
 
 import com.google.common.collect.ImmutableMap;
 
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.emf.compare.Diff;
 import org.eclipse.emf.compare.DifferenceState;
 import org.eclipse.emf.compare.Match;
 import org.eclipse.emf.compare.ReferenceChange;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.AbstractMergeViewer;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.EMFCompareContentMergeViewer;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.provider.IStructuralFeatureAccessor;
 import org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.util.DiffInsertionPoint;
 import org.eclipse.emf.compare.utils.DiffUtil;
 import org.eclipse.emf.compare.utils.EqualityHelper;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 class TableMergeViewer extends AbstractMergeViewer<Table> {
 
 	private IStructuralFeatureAccessor fInput;
 
 	private final EMFCompareContentMergeViewer fContentMergeViewer;
 
 	private ImmutableMap<Match, DiffInsertionPoint> fInsertionPoints;
 
 	TableMergeViewer(Composite parent, EMFCompareContentMergeViewer contentMergeViewer, MergeViewerSide side) {
 		super(parent, side);
 		fContentMergeViewer = contentMergeViewer;
 		fInsertionPoints = ImmutableMap.of();
 
 		getControl().addListener(SWT.EraseItem, new Listener() {
 			public void handleEvent(Event event) {
 				TableMergeViewer.this.handleEraseItemEvent(event);
 			}
 		});
 
 		getControl().addListener(SWT.MeasureItem, new Listener() {
 			public void handleEvent(Event event) {
				event.height = (int)(event.gc.getFontMetrics().getHeight() * 1.33d);
				if (event.height % 2 == 1) {
					event.height += 1;
 				}
 			}
 		});
 
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewer#getControl()
 	 */
 	public Table getControl() {
 		return getStructuredViewer().getTable();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.AbstractMergeViewer#createStructuredViewer()
 	 */
 	@Override
 	protected final TableViewer createStructuredViewer(Composite parent) {
 		return new TableViewer(parent);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.AbstractMergeViewer#getStructuredViewer()
 	 */
 	@Override
 	protected TableViewer getStructuredViewer() {
 		return (TableViewer)super.getStructuredViewer();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.compare.ide.ui.internal.contentmergeviewer.IMergeViewer#setInput(java.lang.Object)
 	 */
 	public void setInput(Object object) {
 		if (object instanceof IStructuralFeatureAccessor) {
 			fInput = (IStructuralFeatureAccessor)object;
 			final List<Object> values = newArrayList(fInput.getValues());
 			addInsertionPoints(values);
 			getStructuredViewer().setInput(values);
 		} else {
 			fInput = null;
 			getStructuredViewer().setInput(null);
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.jface.viewers.IInputProvider#getInput()
 	 */
 	public Object getInput() {
 		return fInput;
 	}
 
 	public void setSelection(Object selection) {
 		if (selection instanceof IStructuralFeatureAccessor) {
 			setSelection((IStructuralFeatureAccessor)selection);
 		} else {
 			setSelection(StructuredSelection.EMPTY);
 		}
 	}
 
 	private void setSelection(IStructuralFeatureAccessor selection) {
 		if (selection != null) {
 			final Object value = selection.getValue();
 			if (((Collection<?>)getStructuredViewer().getInput()).contains(value)) {
 				setSelection(new StructuredSelection(value));
 			} else {
 				DiffInsertionPoint insertionPoint = fInsertionPoints.get(selection.getMatch());
 				if (insertionPoint != null) {
 					setSelection(new StructuredSelection(insertionPoint));
 				} else {
 					setSelection(StructuredSelection.EMPTY);
 				}
 			}
 		} else {
 			setSelection(StructuredSelection.EMPTY);
 		}
 	}
 
 	private void addInsertionPoints(final List<Object> values) {
 		ImmutableMap.Builder<Match, DiffInsertionPoint> insertionsPoints = ImmutableMap.builder();
 		for (ReferenceChange diff : filter(fInput.getDiffFromTheOtherSide().reverse(), ReferenceChange.class)) {
 			boolean rightToLeft = (getSide() == MergeViewerSide.LEFT);
 
 			Match match = diff.getMatch();
 			int insertionIndex = DiffUtil.findInsertionIndex(match.getComparison(), new EqualityHelper(),
 					diff, rightToLeft);
 			Match matchOfDiffValue = match.getComparison().getMatch(diff.getValue());
 			DiffInsertionPoint insertionPoint = new DiffInsertionPoint(diff);
 
 			values.add(insertionIndex, insertionPoint);
 			insertionsPoints.put(matchOfDiffValue, insertionPoint);
 		}
 		fInsertionPoints = insertionsPoints.build();
 	}
 
 	public void setSelection(Match match) {
 		final EObject eObject;
 		switch (getSide()) {
 			case ANCESTOR:
 				eObject = match.getOrigin();
 				break;
 			case LEFT:
 				eObject = match.getLeft();
 				break;
 			case RIGHT:
 				eObject = match.getRight();
 				break;
 			default:
 				throw new IllegalStateException();
 		}
 		final DiffInsertionPoint insertionPoint = fInsertionPoints.get(match);
 		ISelection selection = createSelectionForFirstNonNull(eObject, insertionPoint);
 		setSelection(selection);
 	}
 
 	private ISelection createSelectionForFirstNonNull(final Object first, final Object second) {
 		final ISelection selection;
 		if (first != null) {
 			selection = new StructuredSelection(first);
 		} else if (second != null) {
 			selection = new StructuredSelection(second);
 		} else {
 			selection = StructuredSelection.EMPTY;
 		}
 		return selection;
 	}
 
 	private void handleEraseItemEvent(Event event) {
 		TableItem tableItem = (TableItem)event.item;
 		Object data = tableItem.getData();
 
 		boolean specialPaint = false;
 		if (data instanceof DiffInsertionPoint) {
 			DiffInsertionPoint insertionPoint = (DiffInsertionPoint)data;
 			paintItemDiffBox(event, insertionPoint.getDiff(), getBoundsForInsertionPoint(tableItem));
 			specialPaint = true;
 		} else if (fInput != null) {
 			for (Diff diff : fInput.getDiffFromThisSide()) {
 				if (fInput.getValue(diff) == data) {
 					paintItemDiffBox(event, diff, getBounds(tableItem));
 					specialPaint = true;
 				}
 			}
 			if (getSide() == MergeViewerSide.ANCESTOR) {
 				for (Diff diff : fInput.getDiffFromAncestor()) {
 					if (fInput.getValue(diff) == data) {
 						paintItemDiffBox(event, diff, getBounds(tableItem));
 						specialPaint = true;
 					}
 				}
 			}
 		}
 
 		if (!specialPaint) {
 			paintItem(event, tableItem);
 		}
 	}
 
 	/**
 	 * @param event
 	 * @param tableItem
 	 */
 	private void paintItem(Event event, TableItem tableItem) {
 		event.detail &= ~SWT.HOT;
 
 		if (isSelected(event)) {
 			Rectangle fill = getBounds(tableItem);
 			drawSelectionBox(event, fill);
 		}
 	}
 
 	private void paintItemDiffBox(Event event, Diff diff, Rectangle bounds) {
 		event.detail &= ~SWT.HOT;
 
 		if (diff.getState() == DifferenceState.DISCARDED || diff.getState() == DifferenceState.MERGED) {
 			return;
 		}
 
 		GC g = event.gc;
 		Color oldBackground = g.getBackground();
 		Color oldForeground = g.getForeground();
 
 		setDiffColorsToGC(g, diff, isSelected(event));
 		g.fillRectangle(bounds);
 		g.drawRectangle(bounds);
 
 		if (isSelected(event)) {
 			g.setForeground(event.display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
 			g.setBackground(event.display.getSystemColor(SWT.COLOR_LIST_BACKGROUND));
 			event.detail &= ~SWT.SELECTED;
 		} else {
 			g.setBackground(oldBackground);
 			g.setForeground(oldForeground);
 		}
 	}
 
 	private static boolean isSelected(Event event) {
 		return (event.detail & SWT.SELECTED) != 0;
 	}
 
 	private static void drawSelectionBox(Event event, Rectangle fill) {
 		Display display = event.display;
 		GC g = event.gc;
 		g.setBackground(display.getSystemColor(SWT.COLOR_LIST_SELECTION));
 		g.setForeground(display.getSystemColor(SWT.COLOR_LIST_SELECTION_TEXT));
 		g.fillRectangle(fill);
 		event.detail &= ~SWT.SELECTED;
 	}
 
 	private static Rectangle getBounds(TableItem tableItem) {
 		Rectangle tableBounds = tableItem.getParent().getBounds();
 		Rectangle itemBounds = tableItem.getBounds();
 
 		Rectangle fill = new Rectangle(0, 0, 0, 0);
 		fill.x = 2;
 		fill.y = itemBounds.y + 2;
 		fill.width = tableBounds.width - 6;
 		fill.height = itemBounds.height - 3;
 
 		return fill;
 	}
 
 	private static Rectangle getBoundsForInsertionPoint(TableItem tableItem) {
 		Rectangle fill = getBounds(tableItem);
 		fill.y = fill.y + 6;
 		fill.height = fill.height - 12;
 
 		return fill;
 	}
 
 	private void setDiffColorsToGC(GC g, Diff diff, boolean selected) {
 		g.setForeground(fContentMergeViewer.getColors().getStrokeColor(diff,
 				fContentMergeViewer.isThreeWay(), false, selected));
 		g.setBackground(fContentMergeViewer.getColors().getFillColor(diff, fContentMergeViewer.isThreeWay(),
 				false, selected));
 	}
 
 }
