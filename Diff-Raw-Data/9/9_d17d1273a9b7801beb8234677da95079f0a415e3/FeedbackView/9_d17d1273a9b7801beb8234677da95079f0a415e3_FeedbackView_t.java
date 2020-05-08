 /*
  *
  *  JMoney - A Personal Finance Manager
  *  Copyright (c) 2010 Nigel Westbury <westbury@users.sourceforge.net>
  *
  *
  *  This program is free software; you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation; either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  *
  */
 
 package net.sf.jmoney.views.feedback;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import net.sf.jmoney.JMoneyPlugin;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.viewers.CellLabelProvider;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.TreeViewerColumn;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.viewers.ViewerCell;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.StackLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.Tree;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.part.ViewPart;
 
 public class FeedbackView extends ViewPart {
 	public static String ID = "net.sf.jmoney.FeedbackView";
 
 //	private TableSorter sorter = new TableSorter(VISIBLE_FIELDS);
 	private StatusFilter filter = new StatusFilter();
 	private TreeViewer viewer;
 
 	private ExecuteAgainAction fExecuteAgainAction;
 	private FeedbackHistoryDropDownAction fFeedbackHistoryDropDownAction;
 
 	private List<Feedback> feedbackHistory = new ArrayList<Feedback>();
 
 	private StackLayout stackLayout;
 
 	private Composite stackComposite;
 
 	private Label noFeedbackLabel;
 
 	public FeedbackView() {
 		fExecuteAgainAction= new ExecuteAgainAction(this);
 		fExecuteAgainAction.setEnabled(false);
 
 		fFeedbackHistoryDropDownAction= new FeedbackHistoryDropDownAction(this);
 		fFeedbackHistoryDropDownAction.setEnabled(false);
 	}
 
 	@Override
 	public void init(IViewSite viewSite, IMemento memento) throws PartInitException {
 		super.init(viewSite, memento);
 		
 		if (memento != null) {
 			filter.init(memento.getChild("filter"));
 		}
 	}
 	
 	@Override
 	public void saveState(IMemento memento) {	
 		super.saveState(memento);
 		filter.saveState(memento.createChild("filter"));
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		// No items are currently in the pull down menu.
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 		manager.add(fExecuteAgainAction);
 		manager.add(fFeedbackHistoryDropDownAction);
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 			stackComposite = new Composite(parent, SWT.NONE);
 		
 			stackLayout = new StackLayout();
 			stackComposite.setLayout(stackLayout);
 
 			noFeedbackLabel = new Label(stackComposite, SWT.WRAP);
 			noFeedbackLabel.setText("No feedback is available.");
 
 			createTable(stackComposite);
 
 			stackLayout.topControl = noFeedbackLabel;
 
 			contributeToActionBars();
 
 			// Activate the handlers
 			IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
 
 //			IHandler handler = new DeleteTransactionHandler(rowTracker);
 //			handlerService.activateHandler("net.sf.jmoney.deleteTransaction", handler);		
 	}
 
 	private Control createTable(Composite parent) {
 		viewer = new TreeViewer(parent, /*SWT.MULTI |*/ SWT.H_SCROLL
 				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
 		viewer.setContentProvider(new StatusContentProvider());
 		
 //		viewer.setSorter(sorter);
 		viewer.addFilter(filter);
 		
 		Tree tree = viewer.getTree();
 		
 		tree.setHeaderVisible(true);
 		tree.setLinesVisible(true);
 
 		TreeViewerColumn statusColumn = new TreeViewerColumn(viewer, SWT.LEFT);
 		statusColumn.getColumn().setText("Status");
 		statusColumn.getColumn().setWidth(100);
 
 		statusColumn.setLabelProvider(new CellLabelProvider() {
 			@Override
 			public void update(ViewerCell cell) {
 				IStatus statusElement = (IStatus)cell.getElement();
 				switch (statusElement.getSeverity()) {
 				case IStatus.CANCEL:
 					cell.setImage(JMoneyPlugin.createImage("status_canceled.gif"));
 					break;
 				case IStatus.ERROR:
 					cell.setImage(JMoneyPlugin.createImage("status_error.gif"));
 					break;
 				case IStatus.WARNING:
 					cell.setImage(JMoneyPlugin.createImage("status_warning.gif"));
 					break;
 				case IStatus.INFO:
 					cell.setImage(JMoneyPlugin.createImage("status_info.gif"));
 					break;
 				case IStatus.OK:
					cell.setImage(JMoneyPlugin.createImage("status_ok.gif"));
 					break;
 				}
 			}
 		});
 
 		TreeViewerColumn descriptionColumn = new TreeViewerColumn(viewer, SWT.LEFT);
 		descriptionColumn.getColumn().setText("Description");
 		descriptionColumn.getColumn().setWidth(300);
 
 		descriptionColumn.setLabelProvider(new CellLabelProvider() {
 			@Override
 			public void update(ViewerCell cell) {
 				IStatus statusElement = (IStatus)cell.getElement();
 				cell.setText(statusElement.getMessage());
 			}
 		});
 
 		// Create the pop-up menu
 		MenuManager menuMgr = new MenuManager();
 		menuMgr.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
 		getSite().registerContextMenu(menuMgr, viewer);
 			
 		Control control = viewer.getControl();
 		Menu menu = menuMgr.createContextMenu(control);
 		control.setMenu(menu);
 		
 		return control;
 	}
 
 	@Override
 	public void setFocus() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void showResults(Feedback feedback) {
 		/*
 		 * Add to the end of the past feedback list, removing
 		 * it first from the list if it is already somewhere
 		 * in the list.
 		 */
 		feedbackHistory.remove(feedback);
 		feedbackHistory.add(0, feedback);
 
 		viewer.setInput(feedback.getRootStatus());
 
 		fExecuteAgainAction.setEnabled(true);
 		fFeedbackHistoryDropDownAction.setEnabled(true);
 
 		this.setContentDescription(feedback.getTooltip());
 
 		stackLayout.topControl = viewer.getControl();
 		stackComposite.layout(false);
 	}
 
 	public boolean hasQueries() {
 		return !feedbackHistory.isEmpty();
 	}
 
 	public List<Feedback> getQueries() {
 		return feedbackHistory;
 	}
 
 	public Feedback getCurrentFeedback() {
 		return feedbackHistory.get(0);
 	}
 
 	public void removeAllFeedbackResults() {
 		feedbackHistory.clear();
 
 		/*
 		 * Despite what the javadoc says, passing the empty string seems
 		 * to cause the content description to be removed from the view.
 		 */
 		setContentDescription("");
 
 		// TODO: Is this correct?
 		viewer.setInput(null);
 
 		stackLayout.topControl = noFeedbackLabel;
 		stackComposite.layout(false);
 
 		fExecuteAgainAction.setEnabled(false);
 		fFeedbackHistoryDropDownAction.setEnabled(false);
 	}
 
 	public void removeFeedback(List<Feedback> removedEntries) {
 		feedbackHistory.removeAll(removedEntries);		
 	}
 
 	class FiltersAction extends Action {
 		
 		/**
 		 * Creates the action
 		 */
 		public FiltersAction() {
 			super("Filter...");
 			setImageDescriptor(JMoneyPlugin.createImageDescriptor("filter_ps.gif")); //$NON-NLS-1$
 			setToolTipText("Filter the Displayed Problems");
 		}
 		
 		/**
 		 * Opens the dialog. Notifies the view if the filter has been modified.
 		 */
 		@Override
 		public void run() {
 			StatusFilterDialog dialog = new StatusFilterDialog(viewer.getControl().getShell(), filter);
 			if (dialog.open() == Window.OK) {
 				updateTitle();
 				viewer.refresh();
 			}
 		}
 	}
 	
 	void updateTitle() {
 		int errorCount = 0;
 		int warningCount = 0;
 		int infoCount = 0;
 		
 //		for (IProblemContent problem: problems.values()) {
 //			if (filter.select(viewer, null, problem)) {
 //				switch (problem.getSeverity()) {
 //				case error:
 //					errorCount++;
 //					break;
 //				case warning:
 //					warningCount++;
 //					break;
 //				case ok:
 //					infoCount++;
 //					break;
 //				}
 //			}
 //		}
 //		
 //		String breakdown = Messages.getFormattedString(
 //				"ProblemView.statusSummaryBreakdown", //$NON-NLS-1$
 //				new Object[] {
 //						new Integer(errorCount),
 //						new Integer(warningCount),
 //						new Integer(infoCount)
 //				});
 //		
 //		int filteredCount = errorCount + warningCount + infoCount;
 //		int totalCount = problems.size();
 //		if (filteredCount != totalCount) 
 //			breakdown = Messages.getFormattedString("ProblemView.filterMatchedMessage", //$NON-NLS-1$
 //					new Object[] { 
 //					breakdown, 
 //					new Integer(filteredCount), 
 //					new Integer(totalCount)});
 //		
 //		setContentDescription(breakdown);
 		setContentDescription(getCurrentFeedback().getFullDescription());
 	}
 	
 	public class TableSorter extends ViewerSorter implements Comparator {
 		
 		public static final int MAX_DEPTH = 4; 
 		public static final int ASCENDING = 1;
 		public static final int DESCENDING = -1; 
 		
 		protected Collator collator = Collator.getInstance();
 		
 		protected int[] priorities; 
 		protected int[] directions;
 		
 		private final String TAG_DIALOG_SECTION = "sorter"; //$NON-NLS-1$
 		private final String TAG_PRIORITY = "priority"; //$NON-NLS-1$ 
 		private final String TAG_DIRECTION = "direction"; //$NON-NLS-1$
 		
 		public TableSorter() {
 			super();
 		}
 		
 		public void init(IMemento memento) {
 			if (memento != null) {
 				try {
 					for (int i = 0; i < priorities.length; i++) {
 						priorities[i] = memento.getInteger(TAG_PRIORITY + i);
 						directions[priorities[i]] = memento.getInteger(TAG_DIRECTION + i);
 					}
 				} catch (NumberFormatException e) {
 					resetState();
 				} catch (ArrayIndexOutOfBoundsException e) {
 					resetState();
 				} catch (NullPointerException e) {
 					resetState();
 				}
 			}
 		}
 		
 		public void saveState(IMemento memento) {
 			for (int i = 0; i < priorities.length; i++) { 
 				memento.putInteger(TAG_PRIORITY + i, priorities[i]);
 				memento.putInteger(TAG_DIRECTION + i, directions[priorities[i]]);
 			}
 		}
 
 		public void reverseTopPriority() {
 			directions[priorities[0]] *= -1;
 		}
 		
 		public void setTopPriority(int columnIndex) {
 			int index = -1;
 			for (int i = 0; i < priorities.length; i++) {
 				if (priorities[i] == columnIndex)
 					index = i;
 			}
 			
 			if (index == -1) {
 				// This sort does not currently sort on this column.
 				// Drop off the last sorted column to make room.
 				index = priorities.length - 1;
 			}
 			
 			//shift the array
 			for (int i = index; i > 0; i--) {
 				priorities[i] = priorities[i - 1];
 			}
 			priorities[0] = columnIndex;
 //			directions[columnIndex] = DEFAULT_DIRECTIONS[columnIndex];
 		}
 		
 		public void setTopPriorityDirection(int direction) {
 			if (direction == ASCENDING || direction == DESCENDING)
 				directions[priorities[0]] = direction;
 		}
 		
 		public int getTopPriorityDirection() {
 			return directions[priorities[0]];
 		}
 		
 		public int getTopPriority() {
 			return priorities[0];
 		}
 		
 		public int[] getPriorities() {
 			int[] copy = new int[priorities.length];
 			System.arraycopy(priorities, 0, copy, 0, copy.length);
 			return copy;
 		}
 		
 		public int[] getDirections() {
 			int[] copy = new int[directions.length];
 			System.arraycopy(directions, 0, copy, 0, copy.length);
 			return copy;
 		}
 		
 		public int compare(Viewer viewer, Object e1, Object e2) {
 			return compare(e1, e2, 0);
 		}
 		
 		protected int compare(Object obj1, Object obj2, int depth) {
 			if (depth >= priorities.length) {
 				return 0;
 			}
 
 			return 0;
 //			int column = priorities[depth];
 //			IField property = fields[column];
 //			int result = property.compare((IProblemContent)obj1, (IProblemContent)obj2);
 //			if (result == 0)
 //				return compare(obj1, obj2, depth + 1);
 //			return result * directions[column];
 		}
 		
 		/* (non-Javadoc)
 		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 		 */
 		public int compare(Object o1, Object o2) {
 			return compare(null, o1, o2);
 		}
 		
 		private void resetState() {
 //			System.arraycopy(DEFAULT_PRIORITIES, 0, this.priorities, 0, priorities.length);
 //			System.arraycopy(DEFAULT_DIRECTIONS, 0, this.directions, 0, directions.length);
 		}
 	}
 	
 }
