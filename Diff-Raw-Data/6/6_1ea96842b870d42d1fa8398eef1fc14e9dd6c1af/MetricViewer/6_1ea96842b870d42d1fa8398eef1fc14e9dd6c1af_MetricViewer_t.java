 /*******************************************************************************
  * Copyright (c) 2000, 2008 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Brock Janiczak (brockj@tpg.com.au)
  *         - https://bugs.eclipse.org/bugs/show_bug.cgi?id=102236: [JUnit] display execution time next to each test
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpdepend.ui.views.metricrunner;
 
 import java.util.AbstractList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.AbstractTreeViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.viewers.StructuredViewer;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.php.internal.ui.SelectionProviderMediator;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.part.PageBook;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.IMetricElement;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.MetricClass;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.MetricElement;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.MetricFile;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.MetricMethod;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.MetricRunSession;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.MetricSummary;
 import org.phpsrc.eclipse.pti.tools.phpdepend.core.model.IMetricElement.Status;
 
 public class MetricViewer {
 	private final class MetricSelectionListener implements ISelectionChangedListener {
 		public void selectionChanged(SelectionChangedEvent event) {
 			handleSelected();
 		}
 	}
 
 	private final class MetricOpenListener extends SelectionAdapter {
 		public void widgetDefaultSelected(SelectionEvent e) {
 			handleDefaultSelected();
 		}
 	}
 
 	private final class FailuresOnlyFilter extends ViewerFilter {
 		public boolean select(Viewer viewer, Object parentElement, Object element) {
 			return select(((IMetricElement) element));
 		}
 
 		public boolean select(IMetricElement metricElement) {
			return metricElement.hasWarnings() || metricElement.hasErrors();
 		}
 	}
 
 	private static class ReverseList extends AbstractList {
 		private final List fList;
 
 		public ReverseList(List list) {
 			fList = list;
 		}
 
 		public Object get(int index) {
 			return fList.get(fList.size() - index - 1);
 		}
 
 		public int size() {
 			return fList.size();
 		}
 	}
 
 	private class ExpandAllAction extends Action {
 		public ExpandAllAction() {
 			setText(PHPDependMessages.ExpandAllAction_text);
 			setToolTipText(PHPDependMessages.ExpandAllAction_tooltip);
 		}
 
 		public void run() {
 			fTreeViewer.expandAll();
 		}
 	}
 
 	private final FailuresOnlyFilter fFailuresOnlyFilter = new FailuresOnlyFilter();
 
 	private final MetricRunnerViewPart fMetricRunnerPart;
 	private final Clipboard fClipboard;
 
 	private PageBook fViewerbook;
 	private TreeViewer fTreeViewer;
 	private MetricSessionTreeContentProvider fTreeContentProvider;
 	private MetricSessionLabelProvider fTreeLabelProvider;
 	private TableViewer fTableViewer;
 	private MetricSessionTableContentProvider fTableContentProvider;
 	private MetricSessionLabelProvider fTableLabelProvider;
 	private SelectionProviderMediator fSelectionProvider;
 
 	private int fLayoutMode;
 	private boolean fTreeHasFilter;
 	private boolean fTableHasFilter;
 
 	private MetricRunSession fMetricRunSession;
 
 	private boolean fTreeNeedsRefresh;
 	private boolean fTableNeedsRefresh;
 	private HashSet/* <MetricElement> */fNeedUpdate;
 
 	private LinkedList/* <TestSuiteElement> */fAutoClose;
 	private HashSet/* <TestSuite> */fAutoExpand;
 
 	public MetricViewer(Composite parent, Clipboard clipboard, MetricRunnerViewPart runner) {
 		fMetricRunnerPart = runner;
 		fClipboard = clipboard;
 
 		fLayoutMode = MetricRunnerViewPart.LAYOUT_HIERARCHICAL;
 
 		createTestViewers(parent);
 
 		registerViewersRefresh();
 
 		initContextMenu();
 	}
 
 	private void createTestViewers(Composite parent) {
 		fViewerbook = new PageBook(parent, SWT.NULL);
 
 		fTreeViewer = new TreeViewer(fViewerbook, SWT.V_SCROLL | SWT.SINGLE);
 		fTreeViewer.setUseHashlookup(true);
 		fTreeContentProvider = new MetricSessionTreeContentProvider();
 		fTreeViewer.setContentProvider(fTreeContentProvider);
 		fTreeLabelProvider = new MetricSessionLabelProvider(fMetricRunnerPart, MetricRunnerViewPart.LAYOUT_HIERARCHICAL);
 		fTreeViewer.setLabelProvider(fTreeLabelProvider);
 
 		fTableViewer = new TableViewer(fViewerbook, SWT.V_SCROLL | SWT.H_SCROLL | SWT.SINGLE);
 		fTableViewer.setUseHashlookup(true);
 		fTableContentProvider = new MetricSessionTableContentProvider();
 		fTableViewer.setContentProvider(fTableContentProvider);
 		fTableLabelProvider = new MetricSessionLabelProvider(fMetricRunnerPart, MetricRunnerViewPart.LAYOUT_FLAT);
 		fTableViewer.setLabelProvider(fTableLabelProvider);
 
 		fSelectionProvider = new SelectionProviderMediator(new StructuredViewer[] { fTreeViewer, fTableViewer },
 				fTreeViewer);
 		fSelectionProvider.addSelectionChangedListener(new MetricSelectionListener());
 		MetricOpenListener testOpenListener = new MetricOpenListener();
 		fTreeViewer.getTree().addSelectionListener(testOpenListener);
 		fTableViewer.getTable().addSelectionListener(testOpenListener);
 
 		fMetricRunnerPart.getSite().setSelectionProvider(fSelectionProvider);
 
 		fViewerbook.showPage(fTreeViewer.getTree());
 	}
 
 	private void initContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				handleMenuAboutToShow(manager);
 			}
 		});
 		fMetricRunnerPart.getSite().registerContextMenu(menuMgr, fSelectionProvider);
 		Menu menu = menuMgr.createContextMenu(fViewerbook);
 		fTreeViewer.getTree().setMenu(menu);
 		fTableViewer.getTable().setMenu(menu);
 	}
 
 	void handleMenuAboutToShow(IMenuManager manager) {
 		IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
 		if (!selection.isEmpty()) {
 			IMetricElement element = (IMetricElement) selection.getFirstElement();
 
 			if (element instanceof MetricMethod) {
 				manager.add(new OpenFileAction(fMetricRunnerPart, (MetricMethod) element));
 			} else if (element instanceof MetricClass) {
 				manager.add(new OpenFileAction(fMetricRunnerPart, (MetricClass) element));
 			} else if (element instanceof MetricFile) {
 				manager.add(new OpenFileAction(fMetricRunnerPart, (MetricFile) element));
 			}
 
 			if (fLayoutMode == MetricRunnerViewPart.LAYOUT_HIERARCHICAL) {
 				manager.add(new Separator());
 				manager.add(new ExpandAllAction());
 			}
 
 		}
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS + "-end")); //$NON-NLS-1$
 	}
 
 	public Control getTestViewerControl() {
 		return fViewerbook;
 	}
 
 	public synchronized void registerActiveSession(MetricRunSession testRunSession) {
 		fMetricRunSession = testRunSession;
 		registerViewersRefresh();
 	}
 
 	void handleDefaultSelected() {
 		IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
 		if (selection.size() != 1)
 			return;
 
 		IMetricElement element = (IMetricElement) selection.getFirstElement();
 
 		OpenFileAction action = null;
 		if (element instanceof MetricMethod) {
 			action = new OpenFileAction(fMetricRunnerPart, (MetricMethod) element);
 		} else if (element instanceof MetricClass) {
 			action = new OpenFileAction(fMetricRunnerPart, (MetricClass) element);
 		} else if (element instanceof MetricFile) {
 			action = new OpenFileAction(fMetricRunnerPart, (MetricFile) element);
 		} else {
 			throw new IllegalStateException(String.valueOf(element));
 		}
 
 		if (action != null && action.isEnabled())
 			action.run();
 	}
 
 	private void handleSelected() {
 		IStructuredSelection selection = (IStructuredSelection) fSelectionProvider.getSelection();
 		IMetricElement metricElement = null;
 		if (selection.size() == 1) {
 			metricElement = (IMetricElement) selection.getFirstElement();
 		}
 		fMetricRunnerPart.handleElementSelected(metricElement);
 	}
 
 	public synchronized void setShowTime(boolean showTime) {
 		try {
 			fViewerbook.setRedraw(false);
 			fTreeLabelProvider.setShowTime(showTime);
 			fTableLabelProvider.setShowTime(showTime);
 		} finally {
 			fViewerbook.setRedraw(true);
 		}
 	}
 
 	public synchronized void setShowFailuresOnly(boolean failuresOnly, int layoutMode) {
 		/*
 		 * Management of fTreeViewer and fTableViewer
 		 * ****************************************** - invisible viewer is
 		 * updated on registerViewerUpdate unless its f*NeedsRefresh is true -
 		 * invisible viewer is not refreshed upfront - on layout change, new
 		 * viewer is refreshed if necessary - filter only applies to "current"
 		 * layout mode / viewer
 		 */
 		try {
 			fViewerbook.setRedraw(false);
 
 			IStructuredSelection selection = null;
 			boolean switchLayout = layoutMode != fLayoutMode;
 			if (switchLayout) {
 				selection = (IStructuredSelection) fSelectionProvider.getSelection();
 				if (layoutMode == MetricRunnerViewPart.LAYOUT_HIERARCHICAL) {
 					if (fTreeNeedsRefresh) {
 						clearUpdateAndExpansion();
 					}
 				} else {
 					if (fTableNeedsRefresh) {
 						clearUpdateAndExpansion();
 					}
 				}
 				fLayoutMode = layoutMode;
 				fViewerbook.showPage(getActiveViewer().getControl());
 			}
 
 			// avoid realizing all TableItems, especially in flat mode!
 			StructuredViewer viewer = getActiveViewer();
 			if (failuresOnly) {
 				if (!getActiveViewerHasFilter()) {
 					setActiveViewerNeedsRefresh(true);
 					setActiveViewerHasFilter(true);
 					viewer.setInput(null);
 					viewer.addFilter(fFailuresOnlyFilter);
 				}
 
 			} else {
 				if (getActiveViewerHasFilter()) {
 					setActiveViewerNeedsRefresh(true);
 					setActiveViewerHasFilter(false);
 					viewer.setInput(null);
 					viewer.removeFilter(fFailuresOnlyFilter);
 				}
 			}
 			processChangesInUI();
 
 			if (selection != null) {
 				// workaround for
 				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=125708
 				// (ITreeSelection not adapted if TreePaths changed):
 				StructuredSelection flatSelection = new StructuredSelection(selection.toList());
 				fSelectionProvider.setSelection(flatSelection, true);
 			}
 
 		} finally {
 			fViewerbook.setRedraw(true);
 		}
 	}
 
 	private boolean getActiveViewerHasFilter() {
 		if (fLayoutMode == MetricRunnerViewPart.LAYOUT_HIERARCHICAL)
 			return fTreeHasFilter;
 		else
 			return fTableHasFilter;
 	}
 
 	private void setActiveViewerHasFilter(boolean filter) {
 		if (fLayoutMode == MetricRunnerViewPart.LAYOUT_HIERARCHICAL)
 			fTreeHasFilter = filter;
 		else
 			fTableHasFilter = filter;
 	}
 
 	private StructuredViewer getActiveViewer() {
 		if (fLayoutMode == MetricRunnerViewPart.LAYOUT_HIERARCHICAL)
 			return fTreeViewer;
 		else
 			return fTableViewer;
 	}
 
 	private boolean getActiveViewerNeedsRefresh() {
 		if (fLayoutMode == MetricRunnerViewPart.LAYOUT_HIERARCHICAL)
 			return fTreeNeedsRefresh;
 		else
 			return fTableNeedsRefresh;
 	}
 
 	private void setActiveViewerNeedsRefresh(boolean needsRefresh) {
 		if (fLayoutMode == MetricRunnerViewPart.LAYOUT_HIERARCHICAL)
 			fTreeNeedsRefresh = needsRefresh;
 		else
 			fTableNeedsRefresh = needsRefresh;
 	}
 
 	/**
 	 * To be called periodically by the TestRunnerViewPart (in the UI thread).
 	 */
 	public void processChangesInUI() {
 		MetricSummary testRoot;
 		if (fMetricRunSession == null) {
 			registerViewersRefresh();
 			fTreeNeedsRefresh = false;
 			fTableNeedsRefresh = false;
 			fTreeViewer.setInput(null);
 			fTableViewer.setInput(null);
 			return;
 		}
 
 		testRoot = fMetricRunSession.getSummaryRoot();
 
 		StructuredViewer viewer = getActiveViewer();
 		if (getActiveViewerNeedsRefresh()) {
 			clearUpdateAndExpansion();
 			setActiveViewerNeedsRefresh(false);
 			viewer.setInput(testRoot);
 
 		} else {
 			Object[] toUpdate;
 			synchronized (this) {
 				toUpdate = fNeedUpdate.toArray();
 				fNeedUpdate.clear();
 			}
 			if (!fTreeNeedsRefresh && toUpdate.length > 0) {
 				if (fTreeHasFilter)
 					for (int i = 0; i < toUpdate.length; i++)
 						updateElementInTree((MetricElement) toUpdate[i]);
 				else {
 					HashSet toUpdateWithParents = new HashSet();
 					toUpdateWithParents.addAll(Arrays.asList(toUpdate));
 					for (int i = 0; i < toUpdate.length; i++) {
 						IMetricElement parent = ((MetricElement) toUpdate[i]).getParent();
 						while (parent != null) {
 							toUpdateWithParents.add(parent);
 							parent = parent.getParent();
 						}
 					}
 					fTreeViewer.update(toUpdateWithParents.toArray(), null);
 				}
 			}
 			if (!fTableNeedsRefresh && toUpdate.length > 0) {
 				if (fTableHasFilter)
 					for (int i = 0; i < toUpdate.length; i++)
 						updateElementInTable((MetricElement) toUpdate[i]);
 				else
 					fTableViewer.update(toUpdate, null);
 			}
 		}
 		autoScrollInUI();
 	}
 
 	private void updateElementInTree(final MetricElement MetricElement) {
 		if (isShown(MetricElement)) {
 			updateShownElementInTree(MetricElement);
 		} else {
 			IMetricElement current = MetricElement;
 			do {
 				if (fTreeViewer.testFindItem(current) != null)
 					fTreeViewer.remove(current);
 				current = current.getParent();
 			} while (!(current instanceof MetricSummary) && !isShown(current));
 
 			while (current != null && !(current instanceof MetricSummary)) {
 				fTreeViewer.update(current, null);
 				current = current.getParent();
 			}
 		}
 	}
 
 	private void updateShownElementInTree(IMetricElement metricElement) {
 		if (metricElement == null || metricElement instanceof MetricSummary) // paranoia
 			// null
 			// check
 			return;
 
 		IMetricElement parent = metricElement.getParent();
 		updateShownElementInTree(parent); // make sure parent is shown and
 		// up-to-date
 
 		if (fTreeViewer.testFindItem(metricElement) == null) {
 			fTreeViewer.add(parent, metricElement); // if not yet in tree: add
 		} else {
 			fTreeViewer.update(metricElement, null); // if in tree: update
 		}
 	}
 
 	private void updateElementInTable(IMetricElement element) {
 		if (isShown(element)) {
 			if (fTableViewer.testFindItem(element) == null) {
 				IMetricElement previous = getNextFailure(element, false);
 				int insertionIndex = -1;
 				if (previous != null) {
 					TableItem item = (TableItem) fTableViewer.testFindItem(previous);
 					if (item != null)
 						insertionIndex = fTableViewer.getTable().indexOf(item);
 				}
 				fTableViewer.insert(element, insertionIndex);
 			} else {
 				fTableViewer.update(element, null);
 			}
 		} else {
 			fTableViewer.remove(element);
 		}
 	}
 
 	private boolean isShown(IMetricElement current) {
 		return fFailuresOnlyFilter.select(current);
 	}
 
 	private void autoScrollInUI() {
 		// if (!fTestRunnerPart.isAutoScroll()) {
 		if (true) {
 			clearAutoExpand();
 			fAutoClose.clear();
 			return;
 		}
 
 		synchronized (this) {
 			for (Iterator iter = fAutoExpand.iterator(); iter.hasNext();) {
 				IMetricElement suite = (IMetricElement) iter.next();
 				fTreeViewer.setExpandedState(suite, true);
 			}
 			clearAutoExpand();
 		}
 
 		IMetricElement current = null;
 
 		IMetricElement parent = current == null ? null : (IMetricElement) fTreeContentProvider.getParent(current);
 		if (fAutoClose.isEmpty() || !fAutoClose.getLast().equals(parent)) {
 			// we're in a new branch, so let's close old OK branches:
 			for (ListIterator iter = fAutoClose.listIterator(fAutoClose.size()); iter.hasPrevious();) {
 				IMetricElement previousAutoOpened = (IMetricElement) iter.previous();
 				if (previousAutoOpened.equals(parent))
 					break;
 
 				if (previousAutoOpened.getStatus() == MetricElement.Status.OK) {
 					// auto-opened the element, and all children are OK -> auto
 					// close
 					iter.remove();
 					fTreeViewer.collapseToLevel(previousAutoOpened, AbstractTreeViewer.ALL_LEVELS);
 				}
 			}
 
 			while (parent != null && !fMetricRunSession.getSummaryRoot().equals(parent)
 					&& fTreeViewer.getExpandedState(parent) == false) {
 				fAutoClose.add(parent); // add to auto-opened elements -> close
 				// later if STATUS_OK
 				parent = (IMetricElement) fTreeContentProvider.getParent(parent);
 			}
 		}
 		if (current != null)
 			fTreeViewer.reveal(current);
 	}
 
 	public void selectFirstFailure() {
 		IMetricElement firstFailure = getNextChildFailure(fMetricRunSession.getSummaryRoot(), true);
 		if (firstFailure != null)
 			getActiveViewer().setSelection(new StructuredSelection(firstFailure), true);
 	}
 
 	public void selectFailure(boolean showNext) {
 		IStructuredSelection selection = (IStructuredSelection) getActiveViewer().getSelection();
 		IMetricElement selected = (MetricElement) selection.getFirstElement();
 		IMetricElement next;
 
 		if (selected == null)
 			selected = fMetricRunSession.getSummaryRoot();
 		
 		next = getNextFailure(selected, showNext);
 		if (next != null)
 			getActiveViewer().setSelection(new StructuredSelection(next), true);
 	}
 
 	private IMetricElement getNextFailure(IMetricElement selected, boolean showNext) {
 		IMetricElement nextChild = getNextChildFailure(selected, showNext);
 		if (nextChild != null)
 			return nextChild;
 		
 		return getNextFailureSibling(selected, showNext);
 	}
 	
 	private IMetricElement getNextFailureSibling(IMetricElement current, boolean showNext) {
 		IMetricElement parent = current.getParent();
 		if (parent == null)
 			return null;
 
 		List<IMetricElement> siblings = Arrays.asList(parent.getChildren());
 		if (!showNext)
 			siblings = new ReverseList(siblings);
 
 		int nextIndex = siblings.indexOf(current) + 1;
 		for (int i = nextIndex; i < siblings.size(); i++) {
 			IMetricElement sibling = siblings.get(i);
 			if (sibling.hasErrors() || sibling.hasWarnings()) {
 				if (sibling.getStatus().isErrorOrWarning()) {
 					return sibling;
 				} else {
 					return getNextChildFailure(sibling, showNext);
 				}
 			}
 		}
 		
 		return getNextFailureSibling(parent, showNext);
 	}
 
 	private IMetricElement getNextChildFailure(IMetricElement root, boolean showNext) {
 		List<IMetricElement> children = Arrays.asList(root.getChildren());
 		if (!showNext)
 			children = new ReverseList(children);
 		for (int i = 0; i < children.size(); i++) {
 			MetricElement child = (MetricElement) children.get(i);
 			if (child.hasErrors() || child.hasWarnings()) {
 				if (child.getStatus().isErrorOrWarning()) {
 					return child;
 				} else {
 					return getNextChildFailure(child, showNext);
 				}
 			}
 		}
 		
 		return null;
 	}
 
 	public synchronized void registerViewersRefresh() {
 		fTreeNeedsRefresh = true;
 		fTableNeedsRefresh = true;
 		clearUpdateAndExpansion();
 	}
 
 	private void clearUpdateAndExpansion() {
 		fNeedUpdate = new LinkedHashSet();
 		fAutoClose = new LinkedList();
 		fAutoExpand = new HashSet();
 	}
 
 	/**
 	 * @param MetricElement
 	 *            the added test
 	 */
 	public synchronized void registerTestAdded(MetricElement MetricElement) {
 		// TODO: performance: would only need to refresh parent of added element
 		fTreeNeedsRefresh = true;
 		fTableNeedsRefresh = true;
 	}
 
 	public synchronized void registerViewerUpdate(final MetricElement MetricElement) {
 		fNeedUpdate.add(MetricElement);
 	}
 
 	private synchronized void clearAutoExpand() {
 		fAutoExpand.clear();
 	}
 
 	public synchronized void registerFailedForAutoScroll(MetricElement MetricElement) {
 		Object parent = fTreeContentProvider.getParent(MetricElement);
 		if (parent != null)
 			fAutoExpand.add(parent);
 	}
 
 	public void expandFirstLevel() {
 		fTreeViewer.expandToLevel(2);
 	}
 
 }
