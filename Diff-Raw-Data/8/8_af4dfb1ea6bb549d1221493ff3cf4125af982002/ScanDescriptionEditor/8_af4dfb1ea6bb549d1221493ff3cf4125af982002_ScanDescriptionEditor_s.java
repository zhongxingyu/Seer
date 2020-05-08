 package de.ptb.epics.eve.editor.gef;
 
 import java.io.File;
 import java.io.IOException;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
 import org.eclipse.gef.palette.PaletteRoot;
 import org.eclipse.gef.ui.parts.GraphicalEditorWithFlyoutPalette;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 import org.xml.sax.SAXException;
 
 import de.ptb.epics.eve.data.scandescription.ScanDescription;
 import de.ptb.epics.eve.data.scandescription.processors.ScanDescriptionLoader;
 import de.ptb.epics.eve.editor.Activator;
 import de.ptb.epics.eve.editor.dialogs.lostdevices.LostDevicesDialog;
 
 /**
  * 
  * @author Marcus Michalsky
  * @since 1.6
  */
 public class ScanDescriptionEditor extends GraphicalEditorWithFlyoutPalette {
 
 	private static Logger logger = Logger.getLogger(ScanDescriptionEditor.class
 			.getName());
 	
 	// editor save state
 	private boolean dirty;
 	
 	private ScanDescription scanDescription;
 	
 	/**
 	 * Constructor.
 	 */
 	public ScanDescriptionEditor() {
 		setEditDomain(new DefaultEditDomain(this));
 	}
 	
 	/* ********************************************************************* */
 	/* ****************************** EditorPart *************************** */
 	/* ********************************************************************* */
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
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
 			
 			// TODO ??? this.scanDescription.addModelUpdateListener(this);
 		} catch(final ParserConfigurationException e) {
 			logger.error(e.getMessage(), e);
 		} catch(final SAXException e) {
 			logger.error(e.getMessage(), e);
 		} catch(final IOException e) {
 			logger.error(e.getMessage(), e);
 		}
 		this.firePropertyChange(PROP_DIRTY);
 		super.init(site, fileStoreEditorInput);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	/* ********************************************************************* */
 	/* ********************** end of: EditorPart *************************** */
 	/* ********************************************************************* */
 	
 	/* ********************************************************************* */
 	/* ****************** GraphicalEditor(WithFlyoutPalette) *************** */
 	/* ********************************************************************* */
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void configureGraphicalViewer() {
 		GraphicalViewer viewer = this.getGraphicalViewer();
 		viewer.setEditPartFactory(new ScanDescriptionEditorEditPartFactory());
 		// construct layered view for displaying FreeformFigures (zoomable)
 		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
 		viewer.setSelectionManager(new ModifiedSelectionManager(viewer));
 		super.configureGraphicalViewer();
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected void initializeGraphicalViewer() {
 		super.initializeGraphicalViewer();
 		this.getGraphicalViewer().setContents(this.scanDescription);
 		this.getSite().setSelectionProvider(this.getGraphicalViewer());
		MenuManager menuManager = new MenuManager();
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		this.getSite().registerContextMenu(menuManager,
				this.getGraphicalViewer()); // TODO does not work (also plugin xml)
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected PaletteRoot getPaletteRoot() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/* ********************************************************************* */
 	/* ************ end of: GraphicalEditor(WithFlyoutPalette) ************* */
 	/* ********************************************************************* */
 }
