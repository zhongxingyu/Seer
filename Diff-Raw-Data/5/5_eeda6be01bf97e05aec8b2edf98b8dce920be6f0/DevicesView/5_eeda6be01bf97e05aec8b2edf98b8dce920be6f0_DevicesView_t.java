 package de.ptb.epics.eve.viewer.views.devicesview;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.commands.HandlerEvent;
 import org.eclipse.core.commands.IHandlerListener;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.ViewerFilter;
 import org.eclipse.jface.viewers.ViewerSorter;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.dnd.DND;
 import org.eclipse.swt.dnd.DragSource;
 import org.eclipse.swt.dnd.DragSourceEvent;
 import org.eclipse.swt.dnd.DragSourceListener;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.TreeItem;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.part.ViewPart;
 
 import de.ptb.epics.eve.data.measuringstation.AbstractDevice;
 import de.ptb.epics.eve.data.measuringstation.Detector;
 import de.ptb.epics.eve.data.measuringstation.DetectorChannel;
 import de.ptb.epics.eve.data.measuringstation.Device;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.measuringstation.Motor;
 import de.ptb.epics.eve.data.measuringstation.MotorAxis;
 
 import de.ptb.epics.eve.viewer.Activator;
 import de.ptb.epics.eve.viewer.MessageSource;
 import de.ptb.epics.eve.viewer.XMLDispatcher;
 import de.ptb.epics.eve.viewer.messages.Levels;
 import de.ptb.epics.eve.viewer.messages.ViewerMessage;
 import de.ptb.epics.eve.viewer.views.deviceinspectorview.DeviceInspectorView;
 
 /**
  * <code>DevicesView</code> visualizes available 
  * {@link de.ptb.epics.eve.data.measuringstation.AbstractDevice}s of a 
  * {@link de.ptb.epics.eve.data.measuringstation.MeasuringStation} in a 
  * {@link org.eclipse.jface.viewers.TreeViewer}.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public final class DevicesView extends ViewPart implements PropertyChangeListener {
 
 	/** the unique identifier of this view */
 	public static final String ID = "DevicesView";
 	
 	// logging
 	private static Logger logger = Logger.getLogger(DevicesView.class.getName());
 	
 	// the model visualized by the tree
 	private IMeasuringStation measuringStation;
 	
 	// the tree viewer visualizing the model
 	private TreeViewer treeViewer;
 	
 	// listener (for focus lost deselection)
 	private TreeViewerFocusListener treeViewerFocusListener;
 	
 	// the tree viewer acts as a drag source
 	private DragSource source;
 	
 	// flag indicating if a drag is in progress
 	// (necessary to "block" the focus lost effect when dragging)
 	private boolean dragInProgress;
 	
 	// flags indicating the toggle state of the toolbar menu (true=filter active)
 	private boolean motorsAxesToggleState;
 	private boolean detectorsChannelsToggleState;
 	private boolean devicesToggleState;
 	
 	// filter classes
 	private ViewerFilter motorsAxesFilter;
 	private ViewerFilter detectorsChannelsFilter;
 	private ViewerFilter devicesFilter;
 	
 	private ViewerSorter treeViewerSorter;
 	
 	private ToggleHandlerListener toggleHandlerListener;
 	private ICommandService cmdService;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		
 		measuringStation = Activator.getDefault().getMeasuringStation();
 		if(measuringStation == null) {
 			final Label errorLabel = new Label(parent, SWT.NONE);
 			errorLabel.setText("No device description has been loaded. " +
 					"Please check Preferences!");
 			return;
 		}
 		
 		final FillLayout fillLayout = new FillLayout();
 		parent.setLayout(fillLayout);
 		
 		treeViewer = new TreeViewer(parent);
 		treeViewer.setContentProvider(new TreeViewerContentProvider());
 		treeViewer.setLabelProvider(new TreeViewerLabelProvider());
 		treeViewer.getTree().setEnabled(false);
 		treeViewer.setAutoExpandLevel(1);
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=177669
 		
 		// listen to double clicks (inserts the clicked element to the inspector)
 		treeViewer.addDoubleClickListener(new TreeViewerDoubleClickListener());
 		
 		treeViewerFocusListener = new TreeViewerFocusListener();
 		treeViewer.getTree().addFocusListener(treeViewerFocusListener);
 		
 		// create context menu
 		MenuManager menuManager = new MenuManager();
 		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		menuManager.setRemoveAllWhenShown(true);
 		treeViewer.getTree().setMenu(
 				menuManager.createContextMenu(treeViewer.getTree()));
 		// register menu
 		getSite().registerContextMenu(
 				"de.ptb.epics.eve.viewer.views.devicesview.treepopup", 
 				menuManager, treeViewer);
 		
 		// set this tree viewer as a source for drag n drop (drop in inspector)
 		this.source = new DragSource(
 				this.treeViewer.getTree(), DND.DROP_COPY | DND.DROP_MOVE);
 		Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
 		source.setTransfer(types);
 		source.addDragListener(new DragSourceDragListener());
 		
 		dragInProgress = false;
 		
 		// the DevicesView of the Device Perspective always holds the whole 
 		// measuring station
 		if(this.getPartName().equals("Local Devices")) {
 			setMeasuringStation(measuringStation);
 		} else {
 			// the DevicesView of the Engine Perspective holds the the 
 			// measuringStation currently active in the engine...
 			Activator
 					.getDefault().getXMLDispatcher()
 					.addPropertyChangeListener(
 							XMLDispatcher.DEVICE_DEFINITION_PROP, this);
 			logger.debug("observer added");
 		}
 		
 		// Selection Service
 		getSite().setSelectionProvider(treeViewer);
 		
 		// Filtering
 		motorsAxesFilter = new TreeViewerFilterMotorsAxes();
 		detectorsChannelsFilter = new TreeViewerFilterDetectorsChannels();
 		devicesFilter = new TreeViewerFilterDevices();
 		toggleHandlerListener = new ToggleHandlerListener();
 		cmdService = (ICommandService) getSite().getService(
 				ICommandService.class);
 		cmdService.getCommand("de.ptb.epics.eve.viewer.FilterMotorsAxes").
 				getHandler().addHandlerListener(toggleHandlerListener);
 		cmdService.getCommand("de.ptb.epics.eve.viewer.FilterDetectorsChannels").
 				getHandler().addHandlerListener(toggleHandlerListener);
 		cmdService.getCommand("de.ptb.epics.eve.viewer.FilterDevices").
 				getHandler().addHandlerListener(toggleHandlerListener);
 		readToggleStates();
 		setTreeFilters();
 		
 		this.treeViewerSorter = new TreeViewerSorter();
 		this.treeViewer.setSorter(treeViewerSorter);
 	} // end of: createPartControl()
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		this.treeViewer.getTree().setFocus();
 	}
 
 	/**
 	 * 
 	 * @param measuringStation
 	 */
 	public void setMeasuringStation(final IMeasuringStation measuringStation) {
 		Activator.getDefault().getMessagesContainer().addMessage(
 				new ViewerMessage(MessageSource.VIEWER, Levels.INFO, 
 						"Got new measuring station description."));
 		this.measuringStation = measuringStation;
 		this.treeViewer.setInput(this.measuringStation);
 		this.treeViewer.getTree().setEnabled(this.measuringStation != null);
 	}
 	
 	/*
 	 * checks whether the filters are selected (toolbar toggle items) and saves 
 	 * the state in the local variables. Should be called before calling 
 	 * setTreeFilters().
 	 */
 	private void readToggleStates() {
 		Command motorsAxesCommand = cmdService.
 				getCommand("de.ptb.epics.eve.viewer.FilterMotorsAxes");
 		if(motorsAxesCommand.isDefined()) {
 			motorsAxesToggleState = (Boolean) motorsAxesCommand.
 					getState("org.eclipse.ui.commands.toggleState").getValue();
 		} else {
 			motorsAxesToggleState = false;
 		}
 		Command detectorsChannelsCommand = cmdService.
 				getCommand("de.ptb.epics.eve.viewer.FilterDetectorsChannels");
 		if(detectorsChannelsCommand.isDefined()) {
 			detectorsChannelsToggleState = (Boolean) detectorsChannelsCommand.
 					getState("org.eclipse.ui.commands.toggleState").getValue();
 		} else {
 			detectorsChannelsToggleState = false;
 		}
 		Command devicesCommand = cmdService.
 				getCommand("de.ptb.epics.eve.viewer.FilterDevices");
 		if(devicesCommand.isDefined()) {
 			devicesToggleState = (Boolean) devicesCommand.
 					getState("org.eclipse.ui.commands.toggleState").getValue();
 		} else {
 			devicesToggleState = false;
 		}
 		
 		if(logger.isDebugEnabled()) {
 			logger.debug(
 				"Toggle States: MotorsAxes (" + 
 						motorsAxesToggleState + "), " +
 				"Toggle States: DetectorsChannels (" + 
 						detectorsChannelsToggleState + "), " +
 				"Toggle States: Devices (" + devicesToggleState + ")");
 		}
 	}
 	
 	/*
 	 * checks which of the filters are selected and sets them in the tree viewer
 	 */
 	private void setTreeFilters() {
 		if(this.getPartName().equals("Devices")) {
 			// do not filter the tree contained in the Devices View
 			// (EveEngine Perspective)
 			return;
 		}
 		List<ViewerFilter> filters = new LinkedList<ViewerFilter>();
 		if(motorsAxesToggleState) {
 			filters.add(motorsAxesFilter);
 		}
 		if(detectorsChannelsToggleState) {
 			filters.add(detectorsChannelsFilter);
 		}
 		if(devicesToggleState) {
 			filters.add(devicesFilter);
 		}
 		filters.add(new TreeViewerFilterClasses(motorsAxesToggleState, 
 				detectorsChannelsToggleState, devicesToggleState));
 		treeViewer.setFilters(filters.toArray(new ViewerFilter[0]));
 	}
 	
 	/**
 	 * @since 1.1
 	 */
 	public void expandAll() {
 		this.treeViewer.expandAll();
 	}
 	
 	/**
 	 * @since 1.1
 	 */
 	public void collapseAll() {
 		this.treeViewer.collapseAll();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 1.13
 	 */
 	@Override
 	public void propertyChange(PropertyChangeEvent e) {
 		if (e.getPropertyName().equals(XMLDispatcher.DEVICE_DEFINITION_PROP)) {
 			if (e.getNewValue() instanceof IMeasuringStation) {
 				this.setMeasuringStation((IMeasuringStation)e.getNewValue());
 			}
 		}
 	}
 	
 	// ************************* DnD *****************************************
 
 	/**
 	 * 
 	 * @author Marcus Michalsky
 	 */
 	private class DragSourceDragListener implements DragSourceListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void dragStart(final DragSourceEvent event) {
 			event.doit = true;
 			
 			for(TreeItem item : treeViewer.getTree().getSelection()) {
 				if(logger.isDebugEnabled()) {
 					logger.debug(item.getData() + " selected");
 				}
 				// if the selection contains a class (a collection of different
 				// devices) do not allow dragNdrop
 				if(item.getData() instanceof String) {
 					event.doit = false;
 				}
 			}
 			dragInProgress = true;
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void dragSetData(final DragSourceEvent event) {
 			
 			// provide the data of the requested type
 			
 			if(TextTransfer.getInstance().isSupportedType(event.dataType)) {
 				TreeItem[] items = treeViewer.getTree().getSelection();
 				StringBuffer data = new StringBuffer();
 				int count = 0;
 				
 				// build the string that is transfered to the drop target
 				
 				// add prefixes defining the type of the device
 				for(TreeItem item : items) {
 					if(item.getData() instanceof Motor) {
 						data.append("M" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof MotorAxis) {
 						data.append("A" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof Detector) {
 						data.append("D" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof DetectorChannel) {
 						data.append("C" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof Device) {
 						data.append("d" + ((AbstractDevice)item.getData()).
 								getFullIdentifyer());
 					} else if(item.getData() instanceof List<?>) {
 						
 						int countB = 0;
 						
 						for(Object o : (List<Object>)item.getData()) {
 							if(o instanceof Motor) {
 								data.append("M");
 							} else if(o instanceof Detector) {
 								data.append("D");
 							} else if(o instanceof Device) {
 								data.append("d");
 							}
 							data.append(((AbstractDevice)o).
 									getFullIdentifyer());
 							
 							countB++;
 							if(countB != ((List<Object>)item.getData()).size() ||
 									((List<Object>)item.getData()).size() == 1) {
 								data.append(",");
 							}
 						}
 					}
 					
 					count++;
 					if(count != items.length && 
 					   !(item.getData() instanceof List<?>)) {
 						data.append(",");
 					}
 				}
 				
 				if(logger.isDebugEnabled()) {
 					logger.debug("DragSource: " + data.toString());
 				}
 				
 				event.data = data.toString();
 			}
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void dragFinished(DragSourceEvent event) {
 			// if a move operation has been performed -> remove the data 
 			// from the source
 			// ! we only want to copy data -> do nothing
 			dragInProgress = false;
 			treeViewer.getTree().deselectAll();
 		}
 	}
 	
 	// ***********************************************************************
 	// **************************** Listener *********************************
 	// ***********************************************************************
 	
 	/**
 	 * 
 	 */
 	private class TreeViewerDoubleClickListener implements IDoubleClickListener {
 	
 		/**
 		 * {@inheritDoc}
 	 	 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void doubleClick(DoubleClickEvent event) {
 			if(logger.isDebugEnabled()) {
 				logger.debug("Double Click: " + event.getSelection());
 			}
 			
 			// get all views
 			IViewReference[] ref = getSite().getPage().getViewReferences();
 			
 			DeviceInspectorView deviceInspectorView = null;
 			for(IViewReference ivr : ref) {
 				if(ivr.getId().equals(DeviceInspectorView.ID)) {
					if (DeviceInspectorView.activeDeviceInspectorView == null || 
							ivr.getSecondaryId() == null) {
						// bug #244
						continue;
					}
 					if(DeviceInspectorView.activeDeviceInspectorView.equals(
 							ivr.getSecondaryId())) {
 						deviceInspectorView = (DeviceInspectorView)
 								ivr.getPart(false);
 					}
 				}
 			}
 			
 			if(deviceInspectorView != null) {
 				Object selection = 
 					treeViewer.getTree().getSelection()[0].getData();
 				if(selection instanceof AbstractDevice) {
 					deviceInspectorView.addAbstractDevice((AbstractDevice)
 							treeViewer.getTree().getSelection()[0].getData());
 				} else if(selection instanceof List<?>) {
 					for(Object o : (List<Object>)selection) {
 						deviceInspectorView.addAbstractDevice((AbstractDevice)o);
 					}
 				} else if(selection instanceof String) {
 					List<AbstractDevice> devices = 
 						measuringStation.getDeviceList((String)selection);
 					for(AbstractDevice d : devices) {
 						deviceInspectorView.addAbstractDevice(d);
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @author Marcus Michalsky
 	 */
 	private class TreeViewerFocusListener implements FocusListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusGained(FocusEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void focusLost(FocusEvent e) {
 			if(!dragInProgress) {
 				treeViewer.getTree().deselectAll();
 			}
 		}
 	}
 	
 	/**
 	 * <code>ToggleHandlerListener</code>.
 	 * 
 	 * @author Marcus Michalsky
 	 * @since 1.1
 	 */
 	private class ToggleHandlerListener implements IHandlerListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void handlerChanged(HandlerEvent handlerEvent) {
 			readToggleStates();
 			setTreeFilters();
 			
 			if(logger.isDebugEnabled()) {
 				logger.debug("Filter State changed (toggle).");
 			}
 		}
 	}
 }
