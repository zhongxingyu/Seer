 package de.ptb.epics.eve.editor.gef;
 
 import java.io.File;
 import java.io.IOException;
import java.util.Arrays;
 import java.util.EventObject;
import java.util.List;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.filesystem.EFS;
 import org.eclipse.core.filesystem.IFileStore;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.EditDomain;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.MouseWheelHandler;
 import org.eclipse.gef.MouseWheelZoomHandler;
 import org.eclipse.gef.commands.CommandStack;
 import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
 import org.eclipse.gef.editparts.ZoomManager;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.actions.ZoomInAction;
 import org.eclipse.gef.ui.actions.ZoomOutAction;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
 import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.eclipse.ui.progress.IProgressService;
 import org.xml.sax.SAXException;
 
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.data.scandescription.processors.ScanDescriptionLoader;
 import de.ptb.epics.eve.data.scandescription.updatenotification.IModelUpdateListener;
 import de.ptb.epics.eve.data.scandescription.updatenotification.ModelUpdateEvent;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.dialogs.lostdevices.LostDevicesDialog;
 import de.ptb.epics.eve.editor.jobs.file.Save;
 
 /**
  * 
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class ScanDescriptionEditor extends GraphicalEditorWithFlyoutPalette
 		implements IModelUpdateListener {
 
 	private static Logger logger = Logger.getLogger(ScanDescriptionEditor.class
 			.getName());
 	
 	// editor save state
 	private boolean dirty;
 	
 	private ScanDescription scanDescription;
 	
 	/**
 	 * Constructor.
 	 */
 	public ScanDescriptionEditor() {
 		this.setEditDomain(new DefaultEditDomain(this));
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		logger.trace("init");
 		this.setSite(site);
 		this.setInput(input);
 		this.setPartName(input.getName());
 		
 		final FileStoreEditorInput fileStoreEditorInput = 
 										(FileStoreEditorInput)input;
 		final File scanDescriptionFile = new File(fileStoreEditorInput.getURI());
 		
 		if (scanDescriptionFile.isFile() == true) {
 			if (scanDescriptionFile.length() == 0) {
 				// file exists but is empty -> do not read
 				throw new PartInitException("File is empty!");
 			}
 		} else {
 			// file does not exist -> do not read
 			throw new PartInitException("File does not exist!");
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
 		// TODO add CommandStackListener
 		super.init(site, fileStoreEditorInput);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		if(!isSaveAllowed()) {
 			return;
 		}
 		try {
 			monitor.beginTask("Saving Scan Description", 3);
 			monitor.subTask("determine file name");
 			String saveFileName = ((FileStoreEditorInput)this.getEditorInput()).
 				getURI().getPath().toString();
 			monitor.worked(1);
 			monitor.subTask("creating save routine");
 			Job saveJob = new Save("Save", saveFileName, this.scanDescription, this);
 			monitor.worked(1);
 			monitor.subTask("running save routine");
 			saveJob.setUser(true);
 			saveJob.schedule();
 			monitor.worked(1);
 		} finally {
 			monitor.done();
 		}
 		this.getCommandStack().markSaveLocation();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void doSaveAs() {
 		if(!isSaveAllowed()) {
 			return;
 		}
 		// show dialog preconfigured with path of "last" file name
 		final FileDialog fileDialog = 
 				new FileDialog(this.getEditorSite().getShell(), SWT.SAVE);
 		fileDialog.setFilterPath(new File(((FileStoreEditorInput)
 				this.getEditorInput()).getURI()).getParent());
 		final String dialogName = fileDialog.open();
 		
 		// file path where the file will be saved
 		String saveFileName;
 		
 		// check dialog result
 		if(dialogName != null) {
 			// adjust filename to ".scml"
 			final int lastPoint = dialogName.lastIndexOf(".");
 			final int lastSep = dialogName.lastIndexOf("/");
 			if ((lastPoint > 0) && (lastPoint > lastSep)) {
 				saveFileName = dialogName.substring(0, lastPoint) + ".scml";
 			} else {
 				saveFileName = dialogName + ".scml";
 			}
 		} else {
 			// user pressed "cancel" -> do nothing
 			return;
 		}
 		
 		// create the save job
 		Job saveJob = new Save(
 				"Save As", saveFileName, this.scanDescription, this);
 		
 		// run the save job with a progress dialog
 		IProgressService service = (IProgressService) getSite().getService(
 				IProgressService.class);
 		service.showInDialog(getSite().getShell(), saveJob);
 		saveJob.setUser(true);
 		saveJob.schedule();
 		this.getCommandStack().markSaveLocation();
 	}
 	
 	/**
 	 * Called by the save as job to update the input.
 	 * 
 	 * @param filename the new file name
 	 */
 	public void resetEditorState(String filename) {
 		logger.debug("reset editor state");
 		
 		final IFileStore fileStore = 
 			EFS.getLocalFileSystem().getStore(new Path(filename));
 		final FileStoreEditorInput fileStoreEditorInput = 
 			new FileStoreEditorInput(fileStore);
 		this.setInput(fileStoreEditorInput);
 		this.firePropertyChange(PROP_INPUT);
 		
 		this.setPartName(fileStoreEditorInput.getName());
 		this.firePropertyChange(PROP_TITLE);
 		
 		this.dirty = false;
 		this.firePropertyChange(PROP_DIRTY);
 	}
 	
 	/*
 	 * helper for save and save as to determine whether saving is allowed.
 	 * returns true if saving is allowed, false otherwise
 	 */
 	private boolean isSaveAllowed() {
 		// check for errors in the model
 		// if present -> inform the user
 		if(scanDescription.getModelErrors().size() > 0)	{
 			MessageDialog.openError(
 				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
 				"Save Error", 
 				"Scandescription could not be saved! " +
 				"Consult the Error View for more Information.");
 			return false;
 		}
 		return true;
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
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public boolean isSaveOnCloseNeeded() {
 		return true;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void commandStackChanged(EventObject event) {
 		logger.debug("Command Stack changed");
 		firePropertyChange(IEditorPart.PROP_DIRTY);
 		super.commandStackChanged(event);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	public CommandStack getCommandStack() {
 		return this.getEditDomain().getCommandStack();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 		getSite().setSelectionProvider(null);
 		this.scanDescription.removeModelUpdateListener(this);
 		// TODO remove CommandStackListener
 		super.dispose();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public ScanDescription getContent() {
 		return this.scanDescription;
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@SuppressWarnings("rawtypes")
 	@Override
 	public Object getAdapter(Class type) {
 		if (type == ZoomManager.class) {
 			return ((ScalableFreeformRootEditPart) getGraphicalViewer()
 					.getRootEditPart()).getZoomManager();
 		} else if (type == CommandStack.class) {
 			return this.getEditDomain().getCommandStack();
 		} else if (type == EditDomain.class) {
 			return this.getEditDomain();
 		} else if (type == GraphicalViewer.class) {
 			return this.getGraphicalViewer();
 		} else if (type == EditPart.class) {
 			return this.getGraphicalViewer().getRootEditPart();
 		} 
 		return super.getAdapter(type);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void updateEvent(ModelUpdateEvent modelUpdateEvent) {
 		logger.debug("update event");
 		this.dirty = true;
 		this.firePropertyChange(PROP_DIRTY);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void configureGraphicalViewer() {
 		logger.trace("configureGraphicalViewer");
 		GraphicalViewer viewer = this.getGraphicalViewer();
 		viewer.setEditPartFactory(new ScanDescriptionEditorEditPartFactory());
 		// construct layered view for displaying FreeformFigures (zoomable)
 		ScalableFreeformRootEditPart root = new ScalableFreeformRootEditPart();
 		viewer.setRootEditPart(root);
 		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
 		// zoom with MouseWheel
 		viewer.setProperty(MouseWheelHandler.KeyGenerator.getKey(SWT.MOD1),
 				MouseWheelZoomHandler.SINGLETON);
		List<String> zoomContributions = Arrays.asList(new String[] { 
				ZoomManager.FIT_ALL, ZoomManager.FIT_HEIGHT,
				ZoomManager.FIT_WIDTH });
			root.getZoomManager().setZoomLevelContributions(zoomContributions);
 		IAction zoomIn = new ZoomInAction(root.getZoomManager());
 		IAction zoomOut = new ZoomOutAction(root.getZoomManager());
 		getActionRegistry().registerAction(zoomIn);
 		getActionRegistry().registerAction(zoomOut);
 		super.configureGraphicalViewer();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void initializeGraphicalViewer() {
 		logger.trace("initializeGraphicalViewer");
 		super.initializeGraphicalViewer();
 		this.getGraphicalViewer().setContents(this.scanDescription);
 		MenuManager menuManager = new MenuManager();
 		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		this.getGraphicalViewer().setContextMenu(menuManager);
 		getSite().registerContextMenu(menuManager, this.getGraphicalViewer());
 		this.getSite().setSelectionProvider(this.getGraphicalViewer());
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected PaletteRoot getPaletteRoot() {
 		return ScanDescriptionEditorPaletteFactory.createPalette();
 	}
 }
