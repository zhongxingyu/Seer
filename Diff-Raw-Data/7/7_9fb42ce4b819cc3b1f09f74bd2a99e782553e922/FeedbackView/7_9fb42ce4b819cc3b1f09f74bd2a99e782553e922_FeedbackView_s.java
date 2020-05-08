 package edu.berkeley.eduride.feedbackview.views;
 
 //import org.eduride.junitview.tests.*;
 /*
 import studentview.controller.NavigationListener;
 import studentview.NavigatorActivator;
 import studentview.model.Step;
 */
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Device;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.part.PageBook;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IMarkSelection;
 import org.eclipse.jface.text.ITextSelection;
 import org.eclipse.jface.text.TextViewer;
 import org.eclipse.jface.viewers.*;
 import org.eclipse.jface.action.*;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.*;
 import org.eclipse.swt.widgets.*;
 import edu.berkeley.eduride.feedbackview.model.*;
 
 public class FeedbackView extends ViewPart {
 	/**
 	 * The ID of the view as specified by the extension.
 	 */
 	public static final String ID = "plugin.views.FeedbackView";
 
 	private TableViewer viewer;
 	private Action action1;
 	private Action action2;
 	private Action doubleClickAction;
 
 	// private Observer observe;
 	private final Device device = Display.getCurrent();
 	private Color white = new Color(device, 255, 255, 255);
 	private Color gray = new Color(device, 190, 190, 190);
 	private Color black = new Color(device, 0, 0, 0);
 	private Color green = new Color(device, 0, 100, 0);
 	private Color red = new Color(device, 255, 0, 0);
 	
 	public TestListStore store = new TestListStore();
 	
 	TestList test = null; //placeholder to pull from within a runnable
 
 	/**
 	 * The constructor.
 	 */
 	public FeedbackView() {
 		super();
 	}
 
 	private PageBook pagebook;
 	private TableViewer tableviewer;
 	private TextViewer textviewer;
 
 	// the listener we register with the selection service
 	private ISelectionListener listener = new ISelectionListener() {
 		public void selectionChanged(IWorkbenchPart sourcepart,
 				ISelection selection) {
 			// we ignore our own selections
 			if (sourcepart != FeedbackView.this) {
 				showSelection(sourcepart, selection);
 			}
 		}
 	};
 
 	/**
 	 * Shows the given selection in this view.
 	 */
 	public void showSelection(IWorkbenchPart sourcepart, ISelection selection) {
 		// setContentDescription(sourcepart.getTitle() + " (" +
 		// selection.getClass().getName() + ")");
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection ss = (IStructuredSelection) selection;
 			showItems(ss.toArray());
 		}
 		if (selection instanceof ITextSelection) {
 			ITextSelection ts = (ITextSelection) selection;
 			showText(ts.getText());
 		}
 		if (selection instanceof IMarkSelection) {
 			IMarkSelection ms = (IMarkSelection) selection;
 			try {
 				showText(ms.getDocument().get(ms.getOffset(), ms.getLength()));
 			} catch (BadLocationException ble) {
 			}
 		}
 	}
 
 	private void showItems(Object[] items) {
 		tableviewer.setInput(items);
 		pagebook.showPage(tableviewer.getControl());
 	}
 
 	private void showText(String text) {
 		textviewer.setDocument(new Document(text));
 		pagebook.showPage(textviewer.getControl());
 	}
 
 	public void dispose() {
 		// important: We need do unregister our listener when the view is
 		// disposed
 		getSite().getWorkbenchWindow().getSelectionService()
 				.removeSelectionListener(listener);
 		super.dispose();
 	}
 
 	/**
 	 * This is a callback that will allow us to create the viewer and initialize
 	 * it.
 	 */
 	public void createPartControl(Composite parent) {
 		GridLayout layout = new GridLayout(2, false);
 		parent.setLayout(layout);
 		
 		createViewer(parent);
 		// Create the help context id for the viewer's control
 		PlatformUI.getWorkbench().getHelpSystem()
 				.setHelp(viewer.getControl(), "plugin.viewer");
 		makeActions();
 		hookContextMenu();
 		hookDoubleClickAction();
 		contributeToActionBars();
 
 		// the PageBook allows simple switching between two viewers
 		pagebook = new PageBook(parent, SWT.NONE);
 
 		tableviewer = new TableViewer(pagebook, SWT.NONE);
 		tableviewer.setLabelProvider(new WorkbenchLabelProvider());
 		tableviewer.setContentProvider(new ArrayContentProvider());
 
 		// we're cooperative and also provide our selection
 		// at least for the tableviewer
 		getSite().setSelectionProvider(tableviewer);
 
 		textviewer = new TextViewer(pagebook, SWT.H_SCROLL | SWT.V_SCROLL);
 		textviewer.setEditable(false);
 
 		getSite().getWorkbenchWindow().getSelectionService()
 				.addSelectionListener(listener);
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().layout(true, true);
 
 	}
 
 	public void updateTests(TestList tl) {
 		test = tl;
 		Display.getDefault().asyncExec(new Runnable() {
 		    public void run() {
 		    	Table table = viewer.getTable();
 		    	if (test != null){
 		    		setContentDescription(test.test_results.get(0).getName());
 					table.setBackground(white);
 					table.setForeground(black);
 					viewer.setInput(test.test_results);
 				} else {
 					setContentDescription("No associated test suite");
 					table.setBackground(gray);
 					table.setForeground(gray);
 					viewer.setInput(null);
 				}
 				// Make the selection available to other views
 				getSite().setSelectionProvider(viewer);
 				// Set the sorter for the table
 				viewer.addFilter(new ViewerFilter() {
 					public boolean select(Viewer viewer, Object parentElement,
 							Object element) {
 						TestResult t = (TestResult) element;
 						return (t.getSuccess() || !t.hideWhenSuccessful());
 					}
 				});
 				
 				// Layout the viewer
 				GridData gridData = new GridData();
 				gridData.verticalAlignment = GridData.FILL;
 				gridData.horizontalSpan = 2;
 				gridData.grabExcessHorizontalSpace = true;
 				gridData.grabExcessVerticalSpace = true;
 				gridData.horizontalAlignment = GridData.FILL;
 				viewer.getControl().setLayoutData(gridData);
 		    }
 		});
 	}
 
 	private void createViewer(Composite parent) {
 		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
 				| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
 		createColumns(parent, viewer);
 		final Table table = viewer.getTable();
 		table.setHeaderVisible(true);
 		table.setLinesVisible(true);
 
 		viewer.setContentProvider(new ArrayContentProvider());
 		// Get the content for the viewer, setInput will call getElements in the
 		// contentProvider
 		updateTests(test);
 	}
 
 //	public TableViewer getViewer() {
 //		return viewer;
 //	}
 
 	private void createColumns(final Composite parent, final TableViewer viewer) {
 		String[] titles = { "Success", "Name", "Description", "Expected",
 				"Observed" };
 		int[] bounds = { 100, 100, 100, 100, 100 };
 		System.out.println("creatingColumns");
 		// First column is the name
 		TableViewerColumn col = createTableViewerColumn(titles[0], bounds[0], 0);
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				TestResult t = (TestResult) element;
 				if (t.getSuccess()) {
 					return "Correct";
 				} else {
 					return "Incorrect";
 				}
 			}
 
 			public Color getBackground(Object element) {
 				if (((TestResult) element).getSuccess()) {
 					return green;
 				} else {
 					return red;
 				}
 			}
 		});
 
 		col = createTableViewerColumn(titles[1], bounds[1], 1);
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				TestResult t = (TestResult) element;
 				return t.getName();
 			}
 		});
 		/*********** DESCRIPTION IS GOING TO BE A TOOLTIP */
 
 		// Second column is the description
 		col = createTableViewerColumn(titles[2], bounds[2], 2);
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				TestResult t = (TestResult) element;
 				return t.getDescription();
 			}
 		});
 		/*******/
 		col = createTableViewerColumn(titles[3], bounds[3], 3);
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				TestResult t = (TestResult) element;
 				return t.getExpected();
 			}
 		});
 
 		col = createTableViewerColumn(titles[4], bounds[4], 4);
 		col.setLabelProvider(new ColumnLabelProvider() {
 			@Override
 			public String getText(Object element) {
 				TestResult t = (TestResult) element;
 				return t.getObserved();
 			}
 		});
 		/***********/
 		// grey out the titles
 	}
 
 	private TableViewerColumn createTableViewerColumn(String title, int bound,
 			final int colNumber) {
 		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
 				SWT.NONE);
 		final TableColumn column = viewerColumn.getColumn();
 		column.setText(title);
 		column.setWidth(bound);
 		column.setResizable(true);
 		column.setMoveable(true);
 		return viewerColumn;
 	}
 
 	private void hookContextMenu() {
 		MenuManager menuMgr = new MenuManager("#PopupMenu");
 		menuMgr.setRemoveAllWhenShown(true);
 		menuMgr.addMenuListener(new IMenuListener() {
 			public void menuAboutToShow(IMenuManager manager) {
 				FeedbackView.this.fillContextMenu(manager);
 			}
 		});
 		Menu menu = menuMgr.createContextMenu(viewer.getControl());
 		viewer.getControl().setMenu(menu);
 		getSite().registerContextMenu(menuMgr, viewer);
 	}
 
 	private void contributeToActionBars() {
 		IActionBars bars = getViewSite().getActionBars();
 		fillLocalPullDown(bars.getMenuManager());
 		fillLocalToolBar(bars.getToolBarManager());
 	}
 
 	private void fillLocalPullDown(IMenuManager manager) {
 		manager.add(action1);
 		manager.add(new Separator());
 		manager.add(action2);
 	}
 
 	private void fillContextMenu(IMenuManager manager) {
 		manager.add(action1);
 		manager.add(action2);
 		// Other plug-ins can contribute there actions here
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 	}
 
 	private void fillLocalToolBar(IToolBarManager manager) {
 		// eventually we'll want a icon here that toggles whether the view
 		// changes when the editor changes
 		manager.add(action1);
 		manager.add(action2);
 	}
 
 	private void makeActions() {
 		action1 = new Action() {
 			public void run() {
 				showMessage("Action 1 executed");
 			}
 		};
 		action1.setText("Action 1");
 		action1.setToolTipText("Action 1 tooltip");
 		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
 				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 
 		action2 = new Action() {
 			public void run() {
 				showMessage("Action 2 executed");
 			}
 		};
 		action2.setText("Action 2");
 		action2.setToolTipText("Action 2 tooltip");
 		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
 				.getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
 		doubleClickAction = new Action() {
 			public void run() {
 				ISelection selection = viewer.getSelection();
 				Object obj = ((IStructuredSelection) selection)
 						.getFirstElement();
 				showMessage("Double-click detected on " + obj.toString());
 			}
 		};
 	}
 
 	private void hookDoubleClickAction() {
 		viewer.addDoubleClickListener(new IDoubleClickListener() {
 			public void doubleClick(DoubleClickEvent event) {
 				doubleClickAction.run();
 			}
 		});
 	}
 
 	private void showMessage(String message) {
 		MessageDialog.openInformation(viewer.getControl().getShell(),
 				"Test Plugin", message);
 	}
 
 	/**
 	 * Passing the focus request to the viewer's control.
 	 */
 	public void setFocus() {
 		viewer.getControl().setFocus();
 		pagebook.setFocus();
 	}
 }
