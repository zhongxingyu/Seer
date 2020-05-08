 package de.ptb.epics.eve.editor.graphical;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Iterator;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.XYLayout;
 import org.eclipse.draw2d.geometry.Point;
 import org.eclipse.gef.EditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.editparts.ScalableRootEditPart;
 import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.MouseListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IViewReference;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.part.EditorPart;
 import org.xml.sax.SAXException;
 
 import de.ptb.epics.eve.data.SaveAxisPositionsTypes;
 import de.ptb.epics.eve.data.measuringstation.IMeasuringStation;
 import de.ptb.epics.eve.data.scandescription.Chain;
 import de.ptb.epics.eve.data.scandescription.Connector;
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.data.scandescription.ScanModule;
 import de.ptb.epics.eve.data.scandescription.StartEvent;
 import de.ptb.epics.eve.data.scandescription.processors.ScanDescriptionLoader;
 import de.ptb.epics.eve.data.scandescription.processors.ScanDescriptionSaverToXMLusingXerces;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.dialogs.LostDevicesDialog;
 import de.ptb.epics.eve.editor.graphical.editparts.ChainEditPart;
 import de.ptb.epics.eve.editor.graphical.editparts.EventEditPart;
 import de.ptb.epics.eve.editor.graphical.editparts.ScanModuleEditPart;
 import de.ptb.epics.eve.editor.views.ErrorView;
 import de.ptb.epics.eve.editor.views.scanmoduleview.ScanModuleView;
 import de.ptb.epics.eve.editor.views.scanview.ScanView;
 
 /**
  * <code>GraphicalEditor</code> is the central element of the EveEditor Plug In.
  * It allows creating and editing of scan descriptions.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class GraphicalEditor extends EditorPart implements IModelUpdateListener {
 
 	// logging
 	private static Logger logger = Logger.getLogger(GraphicalEditor.class);
 	
 	// a graphical view of the model (hosts the figures)
 	private ScrollingGraphicalViewer viewer;
 	
 	/*
 	 * the currently loaded scan description
 	 */
 	private ScanDescription scanDescription;
 	
 	/*
 	 * reminder of the currently selected scan module
 	 * decides what is shown in the scan module view
 	 */
 	private ScanModule selectedScanModule = null;
 	
 	/*
 	 * reminder of the currently selected edit part
 	 * if it is a scan module, it is selected (colored)
 	 */
 	private EditPart selectedEditPart;
 	
 	/*
 	 * reminder of the recently right-clicked edit part
 	 * used by the actions of the context menu
 	 */
 	private EditPart rightClickEditPart;
 	
 	private EditDomain editDomain = new EditDomain();
 	
 	// the context menu of the editor (right-click)
 	private Menu menu;
 	
 	// context menu actions to add appended/nested, delete or rename modules
 	private MenuItem addAppendedScanModulMenuItem;
 	private MenuItem addNestedScanModulMenuItem;
 	private MenuItem deleteScanModulMenuItem;
 	private MenuItem renameScanModulMenuItem;
 	
 	// dirty flag indicating whether the editor has unsaved changes
 	private boolean dirty;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void createPartControl(final Composite parent) {
 		logger.debug("create part control");
 		
 		this.viewer = new ScrollingGraphicalViewer();
 		this.viewer.createControl(parent);
 		this.editDomain.addViewer(this.viewer);
 
 		this.viewer.getControl().addMouseListener(new ViewerMouseListener());
 
 		// configure GraphicalViewer		
 		this.viewer.getControl().setBackground(ColorConstants.listBackground);
 		
 		((ScalableRootEditPart)this.viewer.getRootEditPart()).
 				getLayer(ScalableRootEditPart.PRIMARY_LAYER).
 				setLayoutManager(new XYLayout());
 		
 		this.viewer.setEditPartFactory(new GraphicalEditorEditPartFactory());
 		
 		this.viewer.setContents(this.scanDescription);
 		
 		menu = createContextMenu();
 		
 		updateViews();
 	}
 
 	private Menu createContextMenu()
 	{
 		menu = new Menu(this.viewer.getControl());
 		this.addAppendedScanModulMenuItem = new MenuItem(menu, SWT.NONE);
 		this.addAppendedScanModulMenuItem.setText("Add appended Scan Modul");
 		this.addAppendedScanModulMenuItem.addSelectionListener(
 				new AddAppendedScanModuleMenuItemSelectionListener());
 		this.addNestedScanModulMenuItem = new MenuItem(menu, SWT.NONE);
 		this.addNestedScanModulMenuItem.setText("Add nested Scan Modul");
 		this.addNestedScanModulMenuItem.addSelectionListener(
 				new AddNestedScanModuleMenuItemSelectionListener());
 		this.deleteScanModulMenuItem = new MenuItem(menu, SWT.NONE);
 		this.deleteScanModulMenuItem.setText("Delete");
 		this.deleteScanModulMenuItem.addSelectionListener(
 				new DeleteScanModuleMenuItemSelectionListener());
 		this.renameScanModulMenuItem = new MenuItem(menu, SWT.NONE);
 		this.renameScanModulMenuItem.setText("Rename");
 		this.renameScanModulMenuItem.addSelectionListener(
 				new RenameScanModuleMenuItemSelectionListener());
 		this.viewer.getControl().setMenu(menu);
 		
 		return menu;
 	}
 	
 	/*
 	 * called by setFocus()
 	 */
 	private void updateErrorView()
 	{
 		// get all views
 		IViewReference[] ref = getSite().getPage().getViewReferences();
 		
 		// inform the error view about the current scan description
 		ErrorView errorView = null;
 		for(int i = 0; i < ref.length; ++i) {
 			if(ref[i].getId().equals(ErrorView.ID)) {
 				errorView = (ErrorView)ref[i].getPart(false);
 				
 			}
 		}
 		
 		if(errorView != null) {
 			errorView.setCurrentScanDescription(this.scanDescription);
 		}
 	}
 	
 	private void updateScanView()
 	{
 		// get all views
 		IViewReference[] ref = getSite().getPage().getViewReferences();
 		
 		// try to get the scan view
 		ScanView scanView = null;
 		for(int i = 0; i < ref.length; ++i) {
 			if(ref[i].getId().equals(ScanView.ID)) {
 				scanView = (ScanView)ref[i].getPart(false);
 			}
 		}
 		
 		// scan view found ?
 		if(scanView != null) {
 			// tell the view about the currently selected scan module
 			
 			if(selectedScanModule != null)
 			{
 				scanView.setCurrentChain(selectedScanModule.getChain());
 				logger.debug("currentChain: " + selectedScanModule.getChain());
 			}
 			else {
 				scanView.setCurrentChain(null);
 				logger.debug("currentChain: " + null);
 			}
 		} 
 	}
 	
 	/*
 	 * called by setFocus() & the mouse listener
 	 */
 	private void updateScanModuleView()
 	{
 		// get all views
 		IViewReference[] ref = getSite().getPage().getViewReferences();
 		
 		// try to get the scan module view
 		ScanModuleView scanModuleView = null;
 		for(int i = 0; i < ref.length; ++i) {
 			if(ref[i].getId().equals(ScanModuleView.ID)) {
 				scanModuleView = (ScanModuleView)ref[i].getPart(false);
 			}
 		}
 		
 		// scan module view found ?
 		if(scanModuleView != null) {
 			// tell the view about the currently selected scan module
 			scanModuleView.setCurrentScanModule(selectedScanModule);
 		}
 		logger.debug("selectedScanModule: " + selectedScanModule);
 	}
 	
 	/*
 	 * wrapper to update all views
 	 */
 	private void updateViews()
 	{
 		updateErrorView();
 		updateScanView();
 		updateScanModuleView();
 	}
 	
 	/*
 	 * used to select a scan module (and deselect the old one) by 
 	 * updating all necessary references
 	 * 
 	 * @param part the corresponding edit part of the scan module that should
 	 * 		  be selected
 	 */
 	private void selectScanModule(ScanModuleEditPart part)
 	{
 		// if a scan module was previously selected -> deselect it
 		if(selectedEditPart instanceof ScanModuleEditPart) {
 			((ScanModuleEditPart)selectedEditPart).setFocus(false);
 		}
 		
 		if(part != null)
 		{
 			// remember the selected scan module
 			selectedEditPart = part;
 		
 			// update the model to the currently selected module 
 			selectedScanModule = (ScanModule)selectedEditPart.getModel();
 						
 			// set the focus (to select/color it)
 			((ScanModuleEditPart)selectedEditPart).setFocus(true);
 		} else {
 			// reset selection
 			selectedEditPart = null;
 			// reset model
 			selectedScanModule = null;
 		}
 		
 		// tell the views about the changes
 		updateViews();
 	}
 	
 	// ***********************************************************************
 	// ************* methods inherited from IModelUpdateListener *************
 	// ***********************************************************************
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(final ModelUpdateEvent modelUpdateEvent) {
 		logger.debug("update event");
 		this.dirty = true;
 		this.firePropertyChange(PROP_DIRTY);	
 	}
 	
 	// ***********************************************************************
 	// ************ methods inherited from IWorkbenchPart ********************
 	// ***********************************************************************
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void setFocus() {
 		
 		logger.debug("Focus gained");
 						
 		updateViews();
 	}
 	
 	// ***********************************************************************
 	// ************ methods inherited from EditorPart ************************
 	// ***********************************************************************
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(final IEditorSite site, final IEditorInput input) 
 													throws PartInitException {
 		logger.debug("Init");
 		
 		this.selectedScanModule = null;
 		
 		this.setSite(site);
 		this.setInput(input);
 		this.setPartName(input.getName());
 		
 		final FileStoreEditorInput fileStoreEditorInput = 
 										(FileStoreEditorInput)input;
 		final File scanDescriptionFile = new File(fileStoreEditorInput.getURI());
 		
 		if (scanDescriptionFile.isFile() == true) {
 			if (scanDescriptionFile.length() == 0) {
 				// file exists but is empty -> do not read
 				return;
 			}
 		}
 		else {
 			// file does not exist -> do not read
 			return;
 		}
 		
 		final ScanDescriptionLoader scanDescriptionLoader = 
 				new ScanDescriptionLoader(Activator.getDefault().
 													getMeasuringStation(), 
 										  Activator.getDefault().
 										  			getSchemaFile());
 		this.dirty = false;
 		try {
 			scanDescriptionLoader.load(scanDescriptionFile);
 			this.scanDescription = scanDescriptionLoader.getScanDescription();
 
 			if (scanDescriptionLoader.getLostDevices() != null) {
 				Shell shell = getSite().getShell();
 				LostDevicesDialog dialog = 
 						new LostDevicesDialog(shell, scanDescriptionLoader);
 				dialog.open();
 				this.dirty = true;
 			}
 			
 			this.scanDescription.addModelUpdateListener(this);
 		} catch(final ParserConfigurationException e) {
 			logger.error(e.getMessage(), e);
 		} catch(final SAXException e) {
 			logger.error(e.getMessage(), e);
 		} catch(final IOException e) {
 			logger.error(e.getMessage(), e);
 		}
 		this.firePropertyChange(PROP_DIRTY);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void doSave(final IProgressMonitor monitor) {
 		
 		if(scanDescription.getModelErrors().size() > 0)
 		{
 			MessageDialog.openError(
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
 				"Save Error", 
				"Scandescription could not be saved! Please remove any errors present.");
 			
 			return;
 		}
 		
 		final FileStoreEditorInput fileStoreEditorInput = 
 				(FileStoreEditorInput)this.getEditorInput();
 		final File scanDescriptionFile = new File(fileStoreEditorInput.getURI());
 		
 		try {
 			final FileOutputStream os = new FileOutputStream(scanDescriptionFile);	
 			final IMeasuringStation measuringStation = 
 					Activator.getDefault().getMeasuringStation();
 			final ScanDescriptionSaverToXMLusingXerces scanDescriptionSaver = 
 					new ScanDescriptionSaverToXMLusingXerces(
 							os, measuringStation, this.scanDescription);
 			scanDescriptionSaver.save();
 			
 			this.dirty = false;
 			this.firePropertyChange(PROP_DIRTY);
 			
 		} catch(final FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void doSaveAs() {
 		// als filePath wird das Verzeichnis des aktuellen Scans gesetzt
 		final FileStoreEditorInput fileStoreEditorInput2 = 
 					(FileStoreEditorInput)this.getEditorInput();
 		
 		int lastSeperatorIndex = 
 			fileStoreEditorInput2.getURI().getRawPath().lastIndexOf("/");
 		final String filePath = fileStoreEditorInput2.getURI().getRawPath().
 										substring(0, lastSeperatorIndex + 1);
 		
 		final FileDialog dialog = 
 				new FileDialog(this.getEditorSite().getShell(), SWT.SAVE);
 		dialog.setFilterPath(filePath);
 		final String fileName = dialog.open();
 
 		String fileNameLang = fileName;
 		
 		if(fileName != null) {
 			// eventuel vorhandener Datentyp wird weggenommen
 			final int lastPoint = fileName.lastIndexOf(".");
 			final int lastSep = fileName.lastIndexOf("/");
 			
 			if ((lastPoint > 0) && (lastPoint > lastSep))
 				fileNameLang = fileName.substring(0, lastPoint) + ".scml";
 			else
 				fileNameLang = fileName + ".scml";
 		}
 		
 		final File scanDescriptionFile = new File(fileNameLang);
 		
 		try {
 			final FileOutputStream os = 
 					new FileOutputStream(scanDescriptionFile);	
 			final IMeasuringStation measuringStation = 
 					Activator.getDefault().getMeasuringStation();
 			final ScanDescriptionSaverToXMLusingXerces scanDescriptionSaver = 
 					new ScanDescriptionSaverToXMLusingXerces(os, 
 														measuringStation, 
 														this.scanDescription);
 			scanDescriptionSaver.save();
 			
 			final IFileStore fileStore = 
 				EFS.getLocalFileSystem().getStore(new Path(fileNameLang));
 			final FileStoreEditorInput fileStoreEditorInput = 
 				new FileStoreEditorInput(fileStore);
 			this.setInput(fileStoreEditorInput);
 			
 			this.dirty = false;
 			this.firePropertyChange(PROP_DIRTY);
 			
 			this.setPartName(fileStoreEditorInput.getName());
 		} catch(final FileNotFoundException e) {
 			logger.error(e.getMessage(), e);
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isDirty() {
 		return this.dirty;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isSaveAsAllowed() {
 		return true;
 	}
 	
 	// ***********************************************************************
 	// ************************* Listener ************************************
 	// ***********************************************************************
 	
 	/**
 	 * <code>MouseListener</code> of viewer.
 	 */
 	class ViewerMouseListener implements MouseListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void mouseDoubleClick(MouseEvent e) {
 		}
 
 		/**
 		 * {@inheritDoc}<br><br>
 		 * Updates available context menu entries depending on where the user 
 		 * (right-)clicked.
 		 */
 		@Override
 		public void mouseDown(MouseEvent e) {
 
 			if(e.button == 1) return;
 			
 			// get the object (part) the user clicked on
 			EditPart part = viewer.findObjectAt(new Point(e.x, e.y));
 			
 			//part.refresh();
 			
 			// check on what object the user clicked at
 			
 			if(part instanceof ScanModuleEditPart) {
 				
 				// user clicked on a scan module
 				final ScanModule scanModule = (ScanModule)part.getModel();
 				// enable/disable context menu entries depending on the 
 				// selected scan module
 				if(scanModule.getAppended() == null) {
 					// no appended module present -> add appended allowed
 					addAppendedScanModulMenuItem.setEnabled(true);
 				} else {
 					// appended already present -> add appended not allowed
 					addAppendedScanModulMenuItem.setEnabled(false);
 				}
 				if(scanModule.getNested() == null) {
 					// no nested scan module present -> add nested allowed
 					addNestedScanModulMenuItem.setEnabled(true);
 				} else {
 					// nested already present -> add nested not allowed
 					addNestedScanModulMenuItem.setEnabled(false);
 				}
 				// delete and rename is always allowed
 				deleteScanModulMenuItem.setEnabled(true);
 				renameScanModulMenuItem.setEnabled(true);
 				
 			} else if(part instanceof EventEditPart) {
 				
 				// user clicked on an event
 				EventEditPart eventEditPart = (EventEditPart)part;
 				if(((StartEvent)eventEditPart.getModel()).getConnector() == null) {
 					// no appended module present -> add appended allowed
 					addAppendedScanModulMenuItem.setEnabled(true);
 				} else {
 					// appended already present -> add appended not allowed
 					addAppendedScanModulMenuItem.setEnabled(false);
 				}
 				// add nested and delete module never allowed for events
 				addNestedScanModulMenuItem.setEnabled(false);
 				deleteScanModulMenuItem.setEnabled(false);
 				
 			} else {
 				
 				// user clicked anywhere else -> disable all actions
 				addAppendedScanModulMenuItem.setEnabled(false);
 				addNestedScanModulMenuItem.setEnabled(false);
 				deleteScanModulMenuItem.setEnabled(false);
 				renameScanModulMenuItem.setEnabled(false);
 			}
 			
 			// save the edit part the user recently (right-)clicked on
 			rightClickEditPart = part;
 		}
 
 		/**
 		 * {@inheritDoc}<br><br>
 		 * Updates the coloring depending on the selected scan module and 
 		 * tells the scan module view about it.
 		 */
 		@Override
 		public void mouseUp(MouseEvent e) {
 			
 			logger.debug("Mouse " + e.button);
 			
 			if(e.button == 2) return;
 			
 			// find the object the user clicked on
 			EditPart part = viewer.findObjectAt(new Point(e.x, e.y));
 
 			if(part instanceof ScanModuleEditPart) {
 				// user clicked on a scan module
 				
 				selectScanModule((ScanModuleEditPart)part);
 			}
 			else
 			{
 				// user clicked anywhere else -> deselect scan module
 				selectScanModule(null);
 			}
 		}
 	}
 	
 	// ********************* MenuItem Listener...
 	
 	/**
 	 * <code>SelectionListener</code> of 
 	 * <code>addAppendedScanModulMenuItem</code>.
 	 */
 	class AddAppendedScanModuleMenuItemSelectionListener 
 												implements SelectionListener {
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@SuppressWarnings("unchecked")
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			
 			// TODO !!!!! split try catch in smaller parts ?
 			try {
 				if(rightClickEditPart instanceof ScanModuleEditPart) {
 					// get the edit part the user right-clicked on
 					// (before choosing add nested scan module)
 					ScanModuleEditPart scanModuleEditPart = 
 							(ScanModuleEditPart)rightClickEditPart;
 					// get the model of the edit part the user clicked on
 					ScanModule scanModule = 
 							(ScanModule)rightClickEditPart.getModel();
 					
 					// find the next free id which can be used for the new module
 					
 					// create a new array of all available chains
 					Chain[] chains = scanModule.getChain().getScanDescription().
 											getChains().toArray(new Chain[0]);
 					
 					int newId = 1;
 					
 					do {
 						boolean repeat = false;
 						for(int i = 0; i < chains.length; ++i) {
 							ScanModule[] scanModules = chains[i].getScanModuls().
 													  toArray(new ScanModule[0]);
 							for(int j = 0; j < scanModules.length; ++j) {
 								if(scanModules[j].getId() == newId) {
 									newId++;
 									repeat = true;
 								}
 							}	
 						}
 						if(!repeat)
 							break;
 					} while(true);
 					// end of: find new id
 					
 					ScanModule newScanModule = new ScanModule(newId);
 					newScanModule.setName("SM " + newId + " append");
 					newScanModule.setX(scanModule.getX() + 130);
 					newScanModule.setY(scanModule.getY());
 					// Voreinstellungen für das neue Scan Modul
 					newScanModule.setTriggerdelay(0);
 					newScanModule.setSettletime(0);
 					newScanModule.setSaveAxisPositions(SaveAxisPositionsTypes.NEVER);
 					
 					Connector connector = new Connector();
 					connector.setParentScanModul(scanModule);
 					connector.setChildScanModul(newScanModule);
 					scanModule.setAppended(connector);
 					newScanModule.setParent(connector);
 
 					scanModule.getChain().add(newScanModule);
 					
 					scanModuleEditPart.refresh();
 					scanModuleEditPart.getParent().refresh();
 					
 					// select the newly created module
 					EditPart part = viewer.findObjectAt(
 							new Point(newScanModule.getX()+2, 
 									  newScanModule.getY()+2));
 					if(part instanceof ScanModuleEditPart)
 						selectScanModule((ScanModuleEditPart)part);
 					
 				} else if(rightClickEditPart instanceof EventEditPart) {
 					EventEditPart eventEditPart = 
 							(EventEditPart)rightClickEditPart;
 					StartEvent startEvent = 
 							(StartEvent)rightClickEditPart.getModel();
 					Chain[] chains = ((ScanDescription)eventEditPart.getParent().
 							getModel()).getChains().toArray(new Chain[0]);
 					int newId = 1;
 					
 					do {
 						boolean repeat = false;
 						for(int i = 0; i < chains.length; ++i) {
 							ScanModule[] scanModules = chains[i].getScanModuls().
 													  toArray(new ScanModule[0]);
 							for(int j = 0; j < scanModules.length; ++j) {
 								if(scanModules[j].getId() == newId) {
 									newId++;
 									repeat = true;
 								}
 							}
 						}
 						if(!repeat)
 							break;
 					} while(true);
 					
 					ScanModule newScanModule = new ScanModule(newId);
 					newScanModule.setName("SM " + newId);
 					newScanModule.setX(100);
 					newScanModule.setY(20);
 					// Voreinstellungen für das neue Scan Modul
 					newScanModule.setTriggerdelay(0);
 					newScanModule.setSettletime(0);
 					newScanModule.setSaveAxisPositions(SaveAxisPositionsTypes.NEVER);
 					
 					Connector connector = new Connector();
 					connector.setParentEvent(startEvent);
 					connector.setChildScanModul(newScanModule);
 					startEvent.setConnector(connector);
 					newScanModule.setParent(connector);
 					Iterator<Chain> it = scanDescription.getChains().iterator();
 					while(it.hasNext()) {
 						Chain currentChain = it.next();
 						if(currentChain.getStartEvent() == startEvent) {
 						   currentChain.add(newScanModule);
 							break;
 						}
 					}
 					eventEditPart.refresh();
 					eventEditPart.getParent().refresh();
 					Iterator<EditPart> it2 = eventEditPart.getParent().getChildren().iterator();
 					while(it2.hasNext()) {
 						EditPart editPart = (EditPart)it2.next();
 						if(editPart instanceof ChainEditPart) {
 							ChainEditPart chainEditPart = (ChainEditPart)editPart;
 							final Chain chain = (Chain)editPart.getModel();
 							if(chain.getStartEvent() == startEvent) {
 							   chainEditPart.refresh();
 							}
 						}
 					}
 					
 					// select the newly created module
 					EditPart part = viewer.findObjectAt(
 							new Point(newScanModule.getX()+2, 
 									  newScanModule.getY()+2));
 					if(part instanceof ScanModuleEditPart)
 						selectScanModule((ScanModuleEditPart)part);
 				}
 				
 			} catch(Exception ex) {
 				ex.printStackTrace(); // TODO: remove and replace with smaller blocks
 			}		
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>addNestedScanModulMenuItem</code>.
 	 */
 	class AddNestedScanModuleMenuItemSelectionListener implements SelectionListener {
 		
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
 			
 			ScanModuleEditPart scanModuleEditPart = 
 					(ScanModuleEditPart)rightClickEditPart;
 			ScanModule scanModule = (ScanModule)rightClickEditPart.getModel();
 			Chain[] chains = scanModule.getChain().getScanDescription().
 					getChains().toArray(new Chain[0]);
 			
 			// get the next available id
 			int newId = 1;
 			do {
 				boolean repeat = false;
 				
 				for(int i = 0; i < chains.length; ++i) {
 					ScanModule[] scanModules = 
 						chains[i].getScanModuls().toArray(new ScanModule[0]);
 					for(int j=0; j<scanModules.length; ++j) {
 						if(scanModules[j].getId() == newId) {
 							newId++;
 							repeat = true;
 						}
 					}
 				}
 				if(!repeat)
 					break;
 			} while(true);
 			// end of: get free id
 			
 			ScanModule newScanModule = new ScanModule(newId);
 			newScanModule.setName("SM " + newId + " nested");
 			newScanModule.setX(scanModule.getX() + 130);
 			newScanModule.setY(scanModule.getY() + 100);
 			// Voreinstellungen für das neue Scan Modul
 			newScanModule.setTriggerdelay(0);
 			newScanModule.setSettletime(0);
 			newScanModule.setSaveAxisPositions(SaveAxisPositionsTypes.NEVER);
 			
 			Connector connector = new Connector();
 			connector.setParentScanModul(scanModule);
 			connector.setChildScanModul(newScanModule);
 			scanModule.setNested(connector);
 			newScanModule.setParent(connector);
 			
 			scanModule.getChain().add(newScanModule);
 			
 			scanModuleEditPart.refresh();
 			scanModuleEditPart.getParent().refresh();
 			
 			// select the newly created module
 			EditPart part = viewer.findObjectAt(
 					new Point(newScanModule.getX()+2, newScanModule.getY()+2));
 			if(part instanceof ScanModuleEditPart)
 				selectScanModule((ScanModuleEditPart)part);
 		}
 	}
 
 	/**
 	 * <code>SelectionListener</code> of <code>deleteScanModulMenuItem</code>.
 	 */
 	class DeleteScanModuleMenuItemSelectionListener implements SelectionListener {
 		
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
 			
 			// get the scan module the user right-clicked
 			ScanModuleEditPart scanModuleEditPart = 
 					(ScanModuleEditPart)rightClickEditPart;
 
 			// deselect currently selected scan module (if existing)
 			if(selectedEditPart instanceof ScanModuleEditPart) {
 				((ScanModuleEditPart)selectedEditPart).setFocus(false);
 			}
 			
 			// try to find parent scan module
 			EditPart newPart = null;
 			ScanModule scanModule = (ScanModule)scanModuleEditPart.getModel();
 			ScanModule parentModule = scanModule.getParent().getParentScanModule();
 			if (parentModule != null) {
 				int x = parentModule.getX();
 				int y = parentModule.getY();
 				newPart = viewer.findObjectAt(new Point(x, y));
 			}
 			// scanModule mit angehängten Modulen wird entfernt
 			scanModuleEditPart.removeYourSelf();
 			
 			if (newPart != null){ // && newPart instanceof ScanModuleEditPart) {
 				// parent scan module exists -> select it
 				selectScanModule((ScanModuleEditPart)newPart);
 			} else {
 				// no parent scan module -> select nothing
 				selectedEditPart = null;
 				selectedScanModule = null;
 			}
 			
 			// tell the other views about the change
 			updateViews();
 		}
 	}
 	
 	/**
 	 * <code>SelectionListener</code> of <code>renameScanModuleMenuItem</code>.
 	 */
 	class RenameScanModuleMenuItemSelectionListener implements SelectionListener {
 		
 		/**
 		 * {@inheritDoc}
 		 */
 		@Override
 		public void widgetDefaultSelected(SelectionEvent e) {
 		}
 		
 		/**
 		 * {@inheritDoc}<br><br>
 		 * Shows a dialog to enter a new name for the scan module. 
 		 * If OK is pressed, the name is changed. 
 		 */
 		@Override
 		public void widgetSelected(SelectionEvent e) {
 			
 			// get the scan module the user right-clicked
 			ScanModuleEditPart scanModuleEditPart = 
 					(ScanModuleEditPart)rightClickEditPart;
 			ScanModule scanModule = (ScanModule)rightClickEditPart.getModel();
 			
 			// show dialog to input new name
 			Shell shell = getSite().getShell();
 			InputDialog dialog = new InputDialog(shell, 
 					"Renaming ScanModule:" + scanModule.getId(), 
 					"Please enter a new name for the Scan Module:", 
 					scanModule.getName(), null);
 			// if user acknowledges (OK Button) -> change name, 
 			// do nothing if not (Cancel Button)
 			if(InputDialog.OK == dialog.open()) {
 				scanModule.setName(dialog.getValue());
 				scanModuleEditPart.refresh();
 				scanModuleEditPart.getFigure().repaint();
 			}	
 		}
 	}
 }
