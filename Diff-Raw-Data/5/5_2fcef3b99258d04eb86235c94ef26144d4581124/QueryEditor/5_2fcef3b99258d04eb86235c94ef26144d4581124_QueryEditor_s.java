 /**
 * This is the example editor skeleton that is build
 * in <i>Building an editor</i> in chapter <i>Introduction to GEF</i>.
 *
 * @see org.eclipse.ui.part.EditorPart
 */
 package squared.editor;
 
 import java.util.List;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.gef.DefaultEditDomain;
 import org.eclipse.gef.EditPartFactory;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.gef.KeyHandler;
 import org.eclipse.gef.KeyStroke;
 import org.eclipse.gef.LayerConstants;
 import org.eclipse.gef.editparts.LayerManager;
 import org.eclipse.gef.editparts.ScalableFreeformRootEditPart;
 import org.eclipse.gef.ui.actions.GEFActionConstants;
 import org.eclipse.gef.ui.parts.GraphicalEditor;
 import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
 import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.ImageData;
 import org.eclipse.swt.graphics.ImageLoader;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IEditorSite;
 import org.eclipse.ui.ISelectionListener;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 
 import squared.ITexts;
 import squared.core.ClassReflection;
 import squared.model.Diagram;
 import squared.model.Node;
 import squared.model.NodeLink;
 import squared.part.NodePart;
 import squared.part.factory.SquaredEditPartFactory;
 
 import com.db4o.reflect.ReflectField;
 
 public class QueryEditor extends GraphicalEditor
 							implements ISelectionListener
 {
 	/** the <code>EditDomain</code>, will be initialized lazily */
 	private DefaultEditDomain editDomain;
 	
 	/** the graphical viewer */
 	private GraphicalViewer graphicalViewer;
 	
 	private Diagram diagram;
 	
 	private static QueryEditor instance = null;
 	
 	public QueryEditor()
 	{
 		instance = this;
 		editDomain = new DefaultEditDomain(this);
 		setEditDomain(editDomain);
 	}
 		
 	public static QueryEditor getInstance()
 	{
 		return instance;
 	}
 	
 	/**
 	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
 	 */
 	public void dispose() {
 		instance = null;
 		super.dispose();
 	}
 	
 	@Override
 	public void init(IEditorSite site, IEditorInput input)
 			throws PartInitException {
 		// TODO Auto-generated method stub
 		
 		// store site and input
 		setSite(site);
 		setInput(input);
 
 		// add CommandStackListener
 		getCommandStack().addCommandStackListener(this);
 
 		// add selection change listener
 		getSite().getWorkbenchWindow().getSelectionService().addSelectionListener(this);
 
 		// initialize actions
 		createActions();
 		
 	}
 	
 	public void setFocus()
 	{
 		// what should be done if the editor gains focus?
 		// it's your part
 	}
 	
 	public void doSaveAs()
 	{
 		Control figureCanvas = graphicalViewer.getControl();
 		FileDialog dialog = new FileDialog(figureCanvas.getShell(), SWT.SAVE);
 		
 		try {
 			IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
 			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 //			String filename = window.getActivePage().getActiveEditor().getEditorInput().getName();
 			
 			dialog.setFilterPath(workspace.toPortableString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		dialog.setOverwrite(true);
 		dialog.setText(ITexts.QUERY_EDITOR_SAVE_AS_IMG);
 		String saveLocation = dialog.open();
 		
 		ScalableFreeformRootEditPart rootEditPart = (ScalableFreeformRootEditPart)graphicalViewer.getRootEditPart();
 		IFigure rootFigure = ((LayerManager) rootEditPart).getLayer(LayerConstants.PRINTABLE_LAYERS);
 		Rectangle rootFigureBounds = rootFigure.getBounds();
 		
 
 		Image img = new Image(Display.getDefault(), rootFigureBounds.width, 
 		rootFigureBounds.height);
 		GC imageGC = new GC(img);
 		figureCanvas.print(imageGC);
 
 		ImageLoader imgLoader = new ImageLoader();
 		imgLoader.data = new ImageData[] { img.getImageData() };
		imgLoader.save(saveLocation, SWT.IMAGE_PNG);
 	}
 	
 	public boolean isDirty()
 	{
 		return false;
 	}
 	
 	public boolean isSaveAsAllowed()
 	{
 		return true;
 	}
 	
 //	public void gotoMarker(IMarker marker)
 //	{}
 
 	@Override
 	public void doSave(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void createPartControl(Composite parent) {
 		// TODO Auto-generated method stub
 		createGraphicalViewer(parent);
 	}
 
 	@Override
 	protected void initializeGraphicalViewer() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/*
 	* Returns the <code>EditDomain</code> used by this editor.
 	* @return the <code>EditDomain</code> used by this editor
 	*/
 	public DefaultEditDomain getEditDomain()
 	{
 		if (editDomain == null)
 			editDomain = new DefaultEditDomain(null);
 		return editDomain;
 	}
 	
 	/**
 	 * Creates a new <code>GraphicalViewer</code>, configures, registers and
 	 * initializes it.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 * @return a new <code>GraphicalViewer</code>
 	 */
 	protected void createGraphicalViewer(Composite parent)
 	{
 
 		IEditorSite editorSite = getEditorSite();
 		GraphicalViewer viewer = new ScrollingGraphicalViewer();
 		viewer.createControl(parent);			
 		setGraphicalViewer(viewer);
 		configureGraphicalViewer();
 		hookGraphicalViewer();
 		initializeGraphicalViewer();
 		
 		// configure the viewer
 		viewer.getControl().setBackground(ColorConstants.white);
 		viewer.setRootEditPart(new ScalableFreeformRootEditPart());
 		viewer.setKeyHandler(new GraphicalViewerKeyHandler(viewer));
 
 		//viewer.addDropTargetListener(new DataEditDropTargetListener(viewer));
 
 		// initialize the viewer with input
 		viewer.setEditPartFactory(getEditPartFactory());
 		GraphicalViewerKeyHandler graphicalViewerKeyHandler = new GraphicalViewerKeyHandler(viewer);
 		KeyHandler parentKeyHandler = graphicalViewerKeyHandler.setParent(getCommonKeyHandler());
 		viewer.setKeyHandler(parentKeyHandler);
 
 		// hook the viewer into the EditDomain
 		getEditDomain().addViewer(viewer);
 
 		// acticate the viewer as selection provider for Eclipse
 		getSite().setSelectionProvider(viewer);
 
 		// this makes background unselectable (maybe root part takes whole space?)
 		viewer.setContents(Diagram.getInstance());
 
 //		ContextMenuProvider provider = new SchemaContextMenuProvider(viewer, getActionRegistry());
 //		viewer.setContextMenu(provider);
 //		getSite().registerContextMenu("squared.editor.contextmenu", provider, viewer);
 
 		this.graphicalViewer = viewer;
 	}
 	
 	/**
 	 * Returns the <code>EditPartFactory</code> that the
 	 * <code>GraphicalViewer</code> will use.
 	 * 
 	 * @return the <code>EditPartFactory</code>
 	 */
 	protected EditPartFactory getEditPartFactory()
 	{
 		return new SquaredEditPartFactory();
 	}
 	
 	protected KeyHandler getCommonKeyHandler()
 	{
 		KeyHandler sharedKeyHandler = new KeyHandler();
 		sharedKeyHandler.put(KeyStroke.getPressed(SWT.DEL, 127, 0), getActionRegistry().getAction(
 				GEFActionConstants.DELETE));
 		sharedKeyHandler.put(KeyStroke.getPressed(SWT.F2, 0), getActionRegistry().getAction(
 				GEFActionConstants.DIRECT_EDIT));
 
 		return sharedKeyHandler;
 	}
 	
 	/**
 	 * @see org.eclipse.ui.part.EditorPart#setInput(org.eclipse.ui.IEditorInput)
 	 */
 //	protected void setInput(IEditorInput input)
 //	{
 //		super.setInput(input);
 //
 //		IFile file = ((IFileEditorInput) input).getFile();
 //		try
 //		{
 //			setPartName(file.getName());
 //			InputStream is = file.getContents(true);
 //			ObjectInputStream ois = new ObjectInputStream(is);
 //			diagram = (Diagram) ois.readObject();
 //			ois.close();
 //		}
 //		catch (Exception e)
 //		{
 //			e.printStackTrace();
 //			diagram = null;
 //		}
 //	}
 	
     /**
      * @see ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
      */
 	public void selectionChanged(IWorkbenchPart part, ISelection selection) 
 	{
 //		Object first = ((IStructuredSelection) selection).getFirstElement();
 		if (selection instanceof IStructuredSelection) {
 			List selected = ((IStructuredSelection) selection).toList();
 			for (Object elem : selected) {
 				if (elem instanceof NodePart) {
 					System.out.println("selected  node "+elem.toString() + "["+part.getTitle()+"]  "+part.getSite().getId());
 				}
 			}
 		}
 	}
 	
 	public void setDiagramRoot(ClassReflection root)
 	{
 		Node child = new Node(root);
 		if (!Diagram.getInstance().isEmpty())
 		{
 			MessageDialog dialog = new MessageDialog(graphicalViewer.getControl().getShell(), 
 					ITexts.QUERY_EDITOR_CREATE_NEW, null,
 					ITexts.QUERY_EDITOR_CLEAR_DIAGRAM, MessageDialog.CONFIRM,
 					new String[] { ITexts.QUERY_EDITOR_CREATE_NEW, ITexts.TXT_CANCEL }, 0);
 			int ret = dialog.open();
 			if (ret == 1)
 				return;
 			
 			Diagram.getInstance().clear();
 		}
 		
 		Diagram.getInstance().addElement(child);
 		graphicalViewer.setContents(Diagram.getInstance());
 		System.out.println("TODO layout");
 		
 //		graphicalViewer.getControl().pack();
 		graphicalViewer.getContents().refresh();
 	}
 	
 	public void spawnChildNode(Node node, String childName) {
 		if (!node.alreadySpawned(childName)) {
 			
 			ReflectField field = node.getData().getType().getDeclaredField(childName);
 			if (field != null) {
 				Node child = new Node(new ClassReflection(field.getFieldType(), childName));
 				node.addChild(child);
 				new NodeLink(node, child, childName);
 				Diagram.getInstance().addElement(child);
 				graphicalViewer.setContents(Diagram.getInstance());
 			}
 		} 
 	}
 	
 	public void constrainField(Node node, String childName) {
 			
 		ReflectField field = node.getData().getType().getDeclaredField(childName);
 		if (field != null) {
 			
 			InputDialog dialog = new InputDialog(graphicalViewer.getControl().getShell(), 
 					ITexts.QUERY_EDITOR_CONSTRAIN_FIELD + "'" + childName + "'", 
 					ITexts.QUERY_EDITOR_ENTER_CONSTRAINT, "", null);//IInputValidator validator);
 			if (dialog.open() == 0) {
 				node.constrainField(childName, dialog.getValue(), true);
 			}
 
 			graphicalViewer.setContents(Diagram.getInstance());
 		}
 	}
 	
  
 }
 
 
