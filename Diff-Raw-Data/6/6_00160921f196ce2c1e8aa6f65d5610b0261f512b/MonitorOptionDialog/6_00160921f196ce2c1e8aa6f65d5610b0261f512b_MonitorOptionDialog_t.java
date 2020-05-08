 package de.ptb.epics.eve.editor.dialogs.monitoroptions;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.viewers.ColumnLabelProvider;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.jface.window.Window;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.dialogs.SelectionDialog;
 
 import de.ptb.epics.eve.data.measuringstation.Option;
 import de.ptb.epics.eve.data.measuringstation.AbstractDevice;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.views.scanmoduleview.ActionComposite;
 
 /**
  * 
  * @author Hartmut Scherr
  * @since 1.14
  */
 public class MonitorOptionDialog extends SelectionDialog {
 
 	private static Logger logger = 
 			Logger.getLogger(ActionComposite.class.getName());
 
 	private ScanDescription scanDescription;
 	
 	// the model visualized by the tree
 	private IMeasuringStation measuringStation;
 
 	// the tree viewer visualizing the model
 	private TreeViewer treeViewer;
 
 	private TreeViewerSelectionChangedListener treeViewerSelectionChangedListener;
 	
 	// the table of options
 	private TableViewer optionsTable;
 	private ContentProvider optionsTableContentProvider;
 
 	// Liste der Optionen die in der Tabelle angezeigt werden
 	private List<AbstractDevice> tableDevices;
 	
 	private Image ascending;
 	private Image descending;
 
 	// sorting
 	private OptionsTableNameColumnSelectionListener optionsTableNameColumnSelectionListener;
 	private TableViewerComparator optionsTableViewerComparator;
 	private int optionsTableSortState; // 0 no sort, 1 asc, 2 desc
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell 
 	 * @param scanDescriptionLoader
 	 */
 	public MonitorOptionDialog(final Shell shell,
 			final ScanDescription scanDescription) {
 		super(shell);
 		this.scanDescription = scanDescription;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	protected Control createDialogArea(final Composite parent) {
 		Composite area = (Composite) super.createDialogArea(parent);
 
 		Composite container = new Composite(area, SWT.NONE);
 
 		GridData gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
 		container.setLayoutData(gridData);
 
 		GridLayout gridLayout = new GridLayout();
 		gridLayout.numColumns = 2;
 		container.setLayout(gridLayout);
 
 		measuringStation = Activator.getDefault().getMeasuringStation();
 
 		this.tableDevices = new ArrayList<AbstractDevice>();
 		
 		this.createViewer(container);
 		
 		return area;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void createButtonsForButtonBar(Composite parent) {
 		Button closeButton = createButton(parent, IDialogConstants.CLOSE_ID,
 					IDialogConstants.CLOSE_LABEL, true);
 			closeButton.addSelectionListener(new SelectionAdapter() {
 				@Override public void widgetSelected(SelectionEvent e) {
 					setReturnCode(Window.OK);
 					close();
 				}
 			}
 		);
 	}
 	
 	/*
 	 * 
 	 */
 	private void createViewer(Composite parent) {
 		
 		treeViewer = new TreeViewer(parent);
 		treeViewer.setContentProvider(new TreeViewerContentProvider());
 		treeViewer.setLabelProvider(new TreeViewerLabelProvider());
 		treeViewer.getTree().setEnabled(false);
 		treeViewer.setAutoExpandLevel(1);
 	
 		// create context menu
 		MenuManager menuManager = new MenuManager();
 		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		menuManager.setRemoveAllWhenShown(true);
 		treeViewer.getTree().setMenu(
 				menuManager.createContextMenu(treeViewer.getTree()));
 		// register menu
 
 		treeViewer.setInput(measuringStation);
 		GridData gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		treeViewer.getTree().setLayoutData(gridData);
 		treeViewer.getTree().setEnabled(measuringStation != null);
 
 		optionsTable = new TableViewer(parent);
 
 		this.optionsTable.getTable().setHeaderVisible(true);
 		this.optionsTable.getTable().setLinesVisible(true);
 		this.optionsTableContentProvider = new ContentProvider();
 		this.optionsTable.setContentProvider(optionsTableContentProvider);
 		this.optionsTable.setInput(this.tableDevices);
 		
 		// for sorting
 		ascending =	 de.ptb.epics.eve.util.Activator.getDefault().getImageRegistry().get("SORT_ASCENDING");
 		descending = de.ptb.epics.eve.util.Activator.getDefault().getImageRegistry().get("SORT_DESCENDING");
 
 		optionsTableNameColumnSelectionListener = 
 				new OptionsTableNameColumnSelectionListener();
 		optionsTableSortState = 0;
 
 		optionsTableViewerComparator = new TableViewerComparator();
 
 		createColumns(parent, optionsTable);
 		
 		gridData = new GridData();
 		gridData.horizontalAlignment = GridData.FILL;
 		gridData.verticalAlignment = GridData.FILL;
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.grabExcessVerticalSpace = true;
 		optionsTable.getTable().setLayoutData(gridData);
 
 		// Listener hinzufügen
 		this.treeViewerSelectionChangedListener =
 				new TreeViewerSelectionChangedListener();
 		treeViewer.addSelectionChangedListener(
 				treeViewerSelectionChangedListener);
 		
 	}
 
 	/*
 	 * helper for createPartControl
 	 */
 	private void createColumns(final Composite parent, final TableViewer viewer) {
 		
 		// Enable tooltips
 		ColumnViewerToolTipSupport.enableFor(
 				optionsTable, ToolTip.NO_RECREATE);
 
 		// check box column wird angelegt
 		final TableViewerColumn selColumn =
 				new TableViewerColumn(viewer, SWT.NONE);
 //		selColumn.getColumn().setText("Select");
 		selColumn.getColumn().setWidth(20);
 		selColumn.setEditingSupport(new SelColumnEditingSupport(
 				viewer, scanDescription));
 		selColumn.setLabelProvider(new ColumnLabelProvider(){
 			@Override
 			public String getText(Object element) {
 				return "";
 			}
 			@Override
 			public Image getImage(Object element) {
 				Option o = (Option)element;
 
 				if (scanDescription.getMonitors().contains(o)) {
 					// Option wird schon gemonitort
 					return Activator.getDefault().getImageRegistry().get("CHECKED");
 				}
 				else {
 					return Activator.getDefault().getImageRegistry().get("UNCHECKED");
 				}
 			}
 		});
 		
 		// column für option name wird angelegt
 		final TableViewerColumn optionViewerColumn = 
 				new TableViewerColumn(viewer, SWT.NONE);
 		final TableColumn optionColumn = optionViewerColumn.getColumn();
 		optionColumn.setText("Option Name");
 		optionColumn.setWidth(120);
 		optionColumn.setResizable(true);
 
 		optionColumn.addSelectionListener(optionsTableNameColumnSelectionListener);
 		
 		optionViewerColumn.setLabelProvider(new ColumnLabelProvider(){
 			@Override
 			public String getText(Object element) {
 				return ((Option)element).getName();
 			}
 		});
 		
 		// column for device name wird angelegt
 		final TableViewerColumn deviceViewerColumn = 
 				new TableViewerColumn(viewer, SWT.NONE);
 		final TableColumn deviceColumn = deviceViewerColumn.getColumn();
 		deviceColumn.setText("Device Name");
 		deviceColumn.setWidth(120);
 		deviceColumn.setResizable(true);
 		deviceViewerColumn.setLabelProvider(new ColumnLabelProvider(){
 			@Override
 			public String getText(Object element) {
 				return ((Option)element).getParent().getName();
 			}
 		});
 
 	}
 	
 	/**
 	 * <code>TreeViewerSelectionListener</code>.
 	 * 
 	 * @author Hartmut Scherr
 	 * @since 1.14
 	 */
 	private class TreeViewerSelectionChangedListener implements ISelectionChangedListener {
 		
 		@Override
 		public void selectionChanged(SelectionChangedEvent event) {
 			
 			ISelection sel = event.getSelection();
 			
 			// Liste der AbstractDevices wird geleert bevor sie wieder mit den
 			// selektierten Einträgen gefüllt wird
 			tableDevices.clear();
 
 			Iterator<Object> obj = ((IStructuredSelection)sel).toList().iterator();
 
 			while (obj.hasNext()) {
 				Object o = obj.next();
 				if (o instanceof AbstractDevice) {
 					tableDevices.add((AbstractDevice)o);
 				}
 				else {
 					// Selektion gehört zu einer Klasse und wird bisher nicht beachtet
 					// TODO: später sollen die Klassen auch beachtet werden
 				}
 			}
 			optionsTable.setInput(tableDevices);
 		}
 	}	
 	
 	/**
 	 * 
 	 * 
 	 * @author Hartmut Scherr
 	 * @since 1.16
 	 */
 	class OptionsTableNameColumnSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			logger.debug("option name column clicked");
 			logger.debug("old table sort state: " + optionsTableSortState);
 			switch(optionsTableSortState) {
 				case 0: // was no sorting -> now ascending
 						optionsTableViewerComparator.setDirection(
 								TableViewerComparator.ASCENDING);
 						optionsTable.setComparator(optionsTableViewerComparator);
 						optionsTable.getTable().getColumn(1).
 								setImage(ascending);
 						break;
 				case 1: // was ascending -> now descending
 						optionsTableViewerComparator.setDirection(
 								TableViewerComparator.DESCENDING);
 						optionsTable.setComparator(optionsTableViewerComparator);
 						optionsTable.refresh();
 						optionsTable.getTable().getColumn(1).
 								setImage(descending);
 						break;
 				case 2: // was descending -> now no sorting
 						optionsTable.setComparator(null);
 						optionsTable.getTable().getColumn(1).setImage(null);
 						break;
 			}
 			// set is {0,1,2}
 			// if it becomes 3 it has to be 0 again
 			// but before the state has to be increased to the new state
 			optionsTableSortState = ++optionsTableSortState % 3;
 			logger.debug("new options table sort state: " + optionsTableSortState);
 		}
 	}
 }
