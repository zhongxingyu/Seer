 package de.ptb.epics.eve.viewer.views.deviceoptionsview;
 
 import org.apache.log4j.Logger;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.TableViewerColumn;
 import org.eclipse.jface.window.ToolTip;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.IPartService;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IViewSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.part.ViewPart;
 
 import de.ptb.epics.eve.data.measuringstation.AbstractDevice;
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.views.deviceinspectorview.CommonTableElement;
 
 /**
  * <code>DeviceOptionsView</code> displays 
  * {@link de.ptb.epics.eve.data.measuringstation.Option}s of an 
  * {@link de.ptb.epics.eve.data.measuringstation.AbstractDevice}.
  * <p>
  * Multiple instances of this view are allowed. Only the currently active 
  * instance will react to selection change events of the selection service.
  * The view instance is the active one if its secondary id equals the static 
  * property {@link #activeDeviceOptionsView}.
  * <p>
  * The Process Variable Id and sort state are saved in the memento for 
  * recreation.
  * 
  * @author Marcus Michalsky
  */
 public class DeviceOptionsView extends ViewPart implements ISelectionListener {
 	
 	/** the unique identifier of this view */
 	public static final String ID = "DeviceOptionsView";
 
 	/** 
 	 * The secondary id of the <code>DeviceOptionsView</code> that is shown. 
 	 * (Changes in the selection service will be addressed only by this one) 
 	 */
 	public static String activeDeviceOptionsView;
 	
 	private static Logger logger = 
 			Logger.getLogger(DeviceOptionsView.class.getName());
 	
 	// ensures that the right Device Options View is active after a perspective 
 	// switch.
 	private DeviceOptionsViewPartListener deviceOptionsViewPartListener;
 	
 	// underlying model of this view
 	private AbstractDevice device;
 	
 	// the table of options
 	private TableViewer optionsTable;
 	private ContentProvider optionsTableContentProvider;
 	private OptionColumnLabelProvider optionColumnLabelProvider;
 	private ValueColumnLabelProvider valueColumnLabelProvider;
 	private ValueColumnEditingSupport valueColumnEditingSupport;
 	private TableViewerComparator tableViewerComparator;
 	private OptionColumnSelectionListener optionColumnSelectionListener;
 	
 	private int tableViewerSortState;
 	
 	private Image ascending;
 	private Image descending;
 	
 	// saves/restores user defined settings
 	private IMemento memento;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(final IViewSite site, final IMemento memento) 
 													throws PartInitException {
 		super.init(site, memento);
 		
 		this.memento = memento;
 		
 		if(memento == null){
 			return;
 		}
 		
 		final String identifier = memento.getString("device");
 		if(identifier != null && !identifier.isEmpty()) {
 			final AbstractDevice savedDevice = 
 				Activator.getDefault().getMeasuringStation().
 				getAbstractDeviceByFullIdentifyer(identifier);
 			if(savedDevice != null) {
 				this.device = savedDevice;
 			}
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(Composite parent) {
 		
 		parent.setLayout(new FillLayout());
 		
 		// initialize the table (viewer)
 		createViewer(parent);
		createColumns();
 		this.optionsTable.getTable().setHeaderVisible(true);
 		this.optionsTable.getTable().setLinesVisible(true);
 		this.optionsTableContentProvider = new ContentProvider();
 		this.optionsTable.setContentProvider(optionsTableContentProvider);
 		this.optionsTable.setInput(null);
 		this.tableViewerComparator = new TableViewerComparator();
 		
 		ascending =	 Activator.getDefault().getImageRegistry().get("ASCENDING");
 		descending = Activator.getDefault().getImageRegistry().get("DESCENDING");
 		
 		// Register Context Menu (in order to get the CSS PV menu entry)
 		MenuManager optionsTableMenuManager = new MenuManager();
 		optionsTableMenuManager.add(
 				new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		optionsTable.getTable().setMenu(
 			optionsTableMenuManager.createContextMenu(optionsTable.getTable()));
 		getSite().registerContextMenu(
 			"de.ptb.epics.eve.viewer.views.deviceoptionsview.axistablepopup", 
 			optionsTableMenuManager, optionsTable);
 		
 		restoreState();
 		
 		// // if no active view is present, this one will be set as active
 		IViewReference[] ref = getSite().getPage().getViewReferences();
 		DeviceOptionsView deviceOptionsView = null;
 		for(IViewReference ivr : ref) {
 			// 
 			if(ivr.getId().equals(DeviceOptionsView.ID)) {
 					deviceOptionsView = (DeviceOptionsView)ivr.getPart(false);
 			}
 		}
 		if(deviceOptionsView == null) {
 			activeDeviceOptionsView = this.getViewSite().getSecondaryId();
 		}
 		
 		// listen to part changes (necessary to distinguish between the 
 		// DeviceOptionsViews of the EveEngine and EveDevice Perspective).
 		deviceOptionsViewPartListener = new DeviceOptionsViewPartListener();
 		IPartService service = 
 				(IPartService) getSite().getService(IPartService.class);
 		service.addPartListener(deviceOptionsViewPartListener);
 		
 		// listen to selection changes (the selected device's options are 
 		// displayed)
 		getSite().getWorkbenchWindow().getSelectionService().
 				addSelectionListener(this);
 		
 		if(this.device != null) {
 			this.setDevice(device);
 		}
 	} // end of: createPartControl
 
 	/*
 	 * helper for createPartControl
 	 */
 	private void createViewer(final Composite parent) {
 		 optionsTable = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
 							| SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.BORDER);
 	}
 	
 	/*
 	 * helper for createPartControl
 	 */
	private void createColumns() {
 		
 		// Enable tooltips
 		ColumnViewerToolTipSupport.enableFor(
				this.optionsTable, ToolTip.NO_RECREATE);
 		
 		final TableViewerColumn optionViewerColumn = 
				new TableViewerColumn(this.optionsTable, SWT.NONE);
 		final TableColumn optionColumn = optionViewerColumn.getColumn();
 		optionColumn.setText("Option");
 		optionColumn.setWidth(150);
 		optionColumn.setResizable(true);
 		optionColumnLabelProvider = new OptionColumnLabelProvider();
 		optionViewerColumn.setLabelProvider(optionColumnLabelProvider);
 		optionColumnSelectionListener = new OptionColumnSelectionListener();
 		optionColumn.addSelectionListener(optionColumnSelectionListener);
 		
 		final TableViewerColumn valueViewerColumn = 
				new TableViewerColumn(this.optionsTable, SWT.NONE);
 		final TableColumn valueColumn = valueViewerColumn.getColumn();
 		valueColumn.setText("Value");
 		valueColumn.setWidth(70);
 		valueColumn.setResizable(true);
 		valueColumnLabelProvider = new ValueColumnLabelProvider();
 		valueViewerColumn.setLabelProvider(valueColumnLabelProvider);
 		valueColumnEditingSupport = new ValueColumnEditingSupport(optionsTable);
 		valueViewerColumn.setEditingSupport(valueColumnEditingSupport);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * The view becomes the active one (of all 
 	 * {@link de.ptb.epics.eve.viewer.views.deviceoptionsview.DeviceOptionsView} 
 	 * instances). Only the active instance responds to selection changes in the 
 	 * selection service.
 	 */
 	@Override
 	public void setFocus() {
 		activeDeviceOptionsView = this.getViewSite().getSecondaryId();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 		IPartService service = 
 			(IPartService) getSite().getService(IPartService.class);
 		service.removePartListener(deviceOptionsViewPartListener);
 	
 		getSite().getWorkbenchWindow().getSelectionService().
 			removeSelectionListener(this);
 		
 		if(this.getViewSite().getSecondaryId().equals(activeDeviceOptionsView)) {
 			activeDeviceOptionsView = null;
 		}
 		super.dispose();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * <p>
 	 * If the view is the currently active instance and the Selection is a 
 	 * {@link org.eclipse.jface.viewers.IStructuredSelection} and the contained 
 	 * data is either a 
 	 * {@link de.ptb.epics.eve.viewer.views.deviceinspectorview.CommonTableElement}
 	 * (a row in one of the device inspector tables) 
 	 * or an {@link de.ptb.epics.eve.data.measuringstation.AbstractDevice} 
 	 * (an item in the devices view tree) its options will be displayed.
 	 */
 	@Override
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) {
 		
 		if(logger.isDebugEnabled() && selection != null) {
 			logger.debug(selection.toString());
 		}
 		
 		if (logger.isDebugEnabled() && 
 			selection instanceof IStructuredSelection && !selection.isEmpty()) {
 			Object o = ((IStructuredSelection) selection).toList().get(0);
 			if(o instanceof OptionPV) {
 				logger.debug(((OptionPV)o).getName());
 			}
 		}
 		
 		// if this view instance is not the currently active one 
 		// (the one in front) -> do nothing
 		if (activeDeviceOptionsView == null ||
 			!(activeDeviceOptionsView.equals(this.getViewSite().getSecondaryId()))) {
 				logger.debug("Secondary Id different -> do nothing");
 				return;
 		}
 		
 		if(selection instanceof IStructuredSelection) {
 			if(((IStructuredSelection) selection).size() == 0) {
 				/*this.optionsTable.setInput(null);
 				this.device = null;
 				this.setPartName("nothing selected");*/
 				return;
 			}
 			// since at any given time this view can only display options of 
 			// one device we take the first element of the selection
 			Object o = ((IStructuredSelection) selection).toList().get(0);
 			if(o instanceof CommonTableElement) {
 				// the selection is a tableViewer selection (from the inspector)
 				if (this.device != null && this.device.equals(
 					((CommonTableElement)o).getAbstractDevice())) {
 						// the same element in the inspector was selected again
 						return;
 				}
 				this.device = ((CommonTableElement)o).getAbstractDevice();
 				this.optionsTable.setInput(device);
 				this.setPartName(device.getName());
 			} else if(o instanceof AbstractDevice) {
 				// the selection is from a treeViewer (from the DevicesView)
 				if (this.device != null && this.device.equals(o)) {
 						// the same element in the options was selected again
 						return;
 				}
 				this.device = (AbstractDevice)o;
 				this.optionsTable.setInput(device);
 				this.setPartName(device.getName());
 			} else {
 				/*this.optionsTable.setInput(null);
 				this.device = null;
 				this.setPartName("nothing selected");*/
 			}
 		} else {
 			this.optionsTable.setInput(null);
 			this.device = null;
 			this.setPartName("nothing selected");
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void saveState(final IMemento memento) {
 		if(this.device != null) {
 			memento.putString("device", this.device.getFullIdentifyer());
 		}
 		// save sorting
 		memento.putInteger("tableViewerSortState", tableViewerSortState);
 	}
 	
 	/*
 	 * gets called at the end of createPartControl() to restore the state 
 	 * saved in the memento.
 	 */
 	private void restoreState() {
 		if(memento == null) {
 			// nothing saved
 			return;
 		}
 		
 		// restore sorting
 		tableViewerSortState = 
 				(memento.getInteger("tableViewerSortState") == null)
 				? TableViewerComparator.NONE
 				: memento.getInteger("tableViewerSortState");
 		if(tableViewerSortState != TableViewerComparator.NONE) {
 			tableViewerComparator.setDirection(
 					tableViewerSortState == 1
 					? TableViewerComparator.ASCENDING
 					: TableViewerComparator.DESCENDING);
 			optionsTable.getTable().getColumn(0).setImage(
 					tableViewerSortState == 1 ? ascending : descending);
 			optionsTable.setComparator(tableViewerComparator);
 			optionsTable.refresh();
 		}
 	}
 
 	/**
 	 * Sets the device of which options should be shown.
 	 * 
 	 * @param device the device that should be set
 	 * @deprecated To ensure a loose coupling, the eclipse built-in selection 
 	 * service should be used instead.The <code>DeviceOptionsView</code> 
 	 * implements {@link org.eclipse.ui.ISelectionListener} and registers 
 	 * itself to the selection service. Parties interested in showing options 
 	 * of a device should register a selection provider. Consult the 
 	 * <a href="http://www.eclipse.org/articles/Article-WorkbenchSelections/article.html">
 	 * Eclipse Article</a> for further details.<br>
 	 * If a new view is created it should grab the active selection and set 
 	 * the device itself...
 	 * As for the "Open Device in new Options Tab" a command and handler should be used. (TODO)
 	 */
 	public void setDevice(final AbstractDevice device) {
 		this.device = device;
 		this.optionsTable.setInput(this.device);
 		this.setPartName(this.device.getName());
 	}
 	
 	/* ******************************************************************** */
 	
 	/**
 	 * <code>OptionColumnSelectionListener</code>.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.1
 	 */
 	private class OptionColumnSelectionListener implements SelectionListener {
 		
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
 			logger.debug("option column clicked");
 			logger.debug("old options table sort state: " + tableViewerSortState);
 			switch(tableViewerSortState) {
 				case TableViewerComparator.NONE: // was no sorting -> now ascending
 						tableViewerComparator.setDirection(
 								TableViewerComparator.ASCENDING);
 						optionsTable.setComparator(tableViewerComparator);
 						optionsTable.getTable().getColumn(0).setImage(ascending);
 						break;
 				case 1: // was ascending -> now descending
 						tableViewerComparator.setDirection(
 								TableViewerComparator.DESCENDING);
 						optionsTable.setComparator(tableViewerComparator);
 						optionsTable.refresh();
 						optionsTable.getTable().getColumn(0).setImage(descending);
 						break;
 				case 2: // was descending -> now no sorting
 						optionsTable.setComparator(null);
 						optionsTable.getTable().getColumn(0).setImage(null);
 						break;
 			}
 			// set is {0,1,2}
 			// if it becomes 3 it has to be 0 again
 			// but before the state has to be increased to the new state
 			tableViewerSortState = ++tableViewerSortState % 3;
 			logger.debug("new options table sort state: " + tableViewerSortState);
 		}
 	}
 }
