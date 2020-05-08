 package org.dawb.common.ui.wizard;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.IExpressionObject;
 import org.dawb.common.services.IExpressionObjectService;
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionVisitor;
 import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
 import org.dawb.common.ui.Activator;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.dawb.common.ui.util.GridUtils;
 import org.dawnsci.common.widgets.content.FileContentProposalProvider;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.ui.dialogs.WorkspaceResourceDialog;
 import org.eclipse.jface.fieldassist.ContentProposalAdapter;
 import org.eclipse.jface.fieldassist.TextContentAdapter;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 
 /**
  * A page with a field for choosing an external file.
  * The file chosen may also be in a project or typed in.
  * @author fcp94556
  *
  */
 public class ResourceChoosePage extends WizardPage {
 
 	private boolean directory=false;
 	private boolean newFile=false;
 	private boolean pathEditable=false;
 	private boolean buttonsEnabled=true;
 	
 	private String   path;
 	private String   fileLabel=null;
 	private Label    txtLabel;
 	private Text     txtPath;
 	private Button   resourceButton;
 	private Button   fileButton;
 	protected Button overwrite;
 	/**
 	 * 
 	 * @param pageName
 	 * @param description, may be null
 	 * @param icon, may be null
 	 */
 	public ResourceChoosePage(String pageName, String description, ImageDescriptor icon) {
 		super(pageName, description, icon);
 		
 	}
 
 	@Override
 	public final void createControl(Composite parent) {
 		
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns      = 4;
 		layout.verticalSpacing = 9;
 		
 		createContentBeforeFileChoose(container);
 		createFileChooser(container);
 		createContentAfterFileChoose(container);
 		
 		setControl(container);
 
 	}
    public void setErrorMessage(String newMessage) {
        super.setErrorMessage(newMessage);
        setPageComplete(newMessage==null);
    }
 
 	/**
 	 * 
 	 * @param container with a 4-column grid layout
 	 */
 	protected void createContentBeforeFileChoose(Composite container) {
 		
 	}
 	
 	/**
 	 * 
 	 * @param container with a 4-column grid layout
 	 */
 	protected final void createFileChooser(Composite container) {
 		
 		this.txtLabel = new Label(container, SWT.NULL);
 		txtLabel.setText(getFileLabel()!=null ? getFileLabel() : (isDirectory() ? "&Folder  " : "&File  "));
 		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		this.txtPath = new Text(container, SWT.BORDER);
 		txtPath.setEditable(pathEditable);
 		
 		FileContentProposalProvider prov = new FileContentProposalProvider();
 		ContentProposalAdapter ad = new ContentProposalAdapter(txtPath, new TextContentAdapter(), prov, null, null);
 		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
         if (getPath()!=null) txtPath.setText(getPath());
 		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		txtPath.addModifyListener(new ModifyListener() {			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				path = txtPath.getText();
 				pathChanged();
 			}
 		});
 
 		this.resourceButton = new Button(container, SWT.PUSH);
 		resourceButton.setText("...");
 		resourceButton.setImage(Activator.getImageDescriptor("icons/Project-data.png").createImage());
 		resourceButton.setToolTipText("Browse to "+(isDirectory()?"folder":"file")+" inside a project");
 		resourceButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleResourceBrowse();
 			}
 		});
 		resourceButton.setEnabled(buttonsEnabled);
 		
 		this.fileButton = new Button(container, SWT.PUSH);
 		fileButton.setText("...");
 		fileButton.setImage(Activator.getImageDescriptor("icons/data_folder_link.gif").createImage());
 		fileButton.setToolTipText("Browse to an external "+(isDirectory()?"folder":"file")+".");
 		fileButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleFileBrowse();
 			}
 		});
 		resourceButton.setEnabled(buttonsEnabled);
 		
 		if (!isDirectory() && overwriteVisible) {
 			final Label filler = new Label(container, SWT.NONE);
 			filler.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 	
 			this.overwrite = new Button(container, SWT.CHECK);
 			overwrite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
 			overwrite.setToolTipText("Overwrite existing file(s) of the same name during processing.");
 			overwrite.setText("Overwrite file if it already exists");
 			overwrite.setSelection(true);
 			overwrite.addSelectionListener(new SelectionAdapter() {
 				public void widgetSelected(SelectionEvent e) {
 					pathChanged();
 				}
 			});
 		}
 	}
 	
 	public boolean isPageComplete() {
     	if (getErrorMessage()!=null) return false;
         return super.isPageComplete();
     }
 
 	/**
 	 * 
 	 * @param container with a 4-column grid layout
 	 */
 	protected void createContentAfterFileChoose(Composite container) {
 		
 	}
 
 
 	protected void handleResourceBrowse() {
 		
 		IResource[] res = null;
 		if (isDirectory()) {
 			res = WorkspaceResourceDialog.openFolderSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
 					"Directory location", "Please choose a location.", false, 
 					    new Object[]{getIResource()}, null);	
 			
 		} else {
 			if (isNewFile()) {
 				final IResource cur = getIResource();
 				final IPath path = cur!=null ? cur.getFullPath() : null;
 			    IFile file = WorkspaceResourceDialog.openNewFile(PlatformUI.getWorkbench().getDisplay().getActiveShell(),
 			    		                                  "File location", "Please choose a location.",
 			    		                                   path, null);	
 				res = file !=null ? new IResource[]{file} : null;
 			} else {
 			    res = WorkspaceResourceDialog.openFileSelection(PlatformUI.getWorkbench().getDisplay().getActiveShell(), 
 				           "File location", "Please choose a location.", false,
 				            new Object[]{getIResource()}, null);	
 			}
 		}
 		
 		
 		if (res!=null && res.length>0) {
 			this.path = res[0].getFullPath().toOSString();
 		    txtPath.setText(this.path);
 			pathChanged();
 		}
 	}
 	
 	protected void handleFileBrowse() {
 		
 		String path = null;
 		if (isDirectory()) {
 			final DirectoryDialog dialog = new DirectoryDialog(Display.getDefault().getActiveShell(), SWT.OPEN);
 			dialog.setText("Choose folder");
 			final String filePath = getAbsoluteFilePath();
 			if (filePath!=null) {
 				File file = new File(filePath);
 				if (file.exists()) {
 					// Nothing
 				} else if (file.getParentFile().exists()) {
 					file = file.getParentFile();
 				}
 				if (file.isDirectory()) {
 					dialog.setFilterPath(file.getAbsolutePath());
 				} else {
 					dialog.setFilterPath(file.getParent());
 				}
 			}
 			path = dialog.open();
 			
 		} else {
 			final FileDialog dialog = new FileDialog(Display.getDefault().getActiveShell(), (isNewFile()?SWT.SAVE:SWT.OPEN));
 			dialog.setText("Choose file");
 			final String filePath = getAbsoluteFilePath();
 			if (filePath!=null) {
 				final File file = new File(filePath);
 				if (file.exists()) {
 					if (file.isDirectory()) {
 						dialog.setFilterPath(file.getAbsolutePath());
 					} else {
 						dialog.setFilterPath(file.getParent());
 						dialog.setFileName(file.getName());
 					}
 				}
 				
 			}
 			path = dialog.open();
 		}
 		if (path!=null) {
 			setPath(path);
 		    txtPath.setText(this.path);
 			pathChanged();
 		}
 	}
 
 	/**
 	 * Call to update
 	 */
 	protected void pathChanged() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	/**
 	 * 
 	 * @return the output file path
 	 */
 	public String getPath() {
 		return path;
 	}
 	
 	/**
 	 * 
 	 * @return the output file path
 	 */
 	public String getAbsoluteFilePath() {
 		try{
 			IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(getPath());
 			if (res!=null) return res.getLocation().toOSString();
 			if (isNewFile()) { // We try for a new file
 				final File file = new File(getPath());
 				String parDir = file.getParent();
 				IContainer folder = (IContainer)ResourcesPlugin.getWorkspace().getRoot().findMember(parDir);
 				if (folder!=null) {
 					final IFile newFile = folder.getFile(new Path(file.getName()));
 					if (newFile.exists()) newFile.touch(null);
 					return newFile.getLocation().toOSString();
 				}
 			}
 			return getPath();
 		} catch (Throwable ignored) {
 			return null;
 		}
 	}
 
 	public void setPath(String path) {
 		this.path = path;
 		if (txtPath!=null) txtPath.setText(path);
 	}
 	
 	protected IResource getIResource() {
 		IResource res = null;
 		if (path!=null) {
 			res = ResourcesPlugin.getWorkspace().getRoot().findMember(getPath());
 		}
 		if (res == null && getPath()!=null) {
 			final String workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
 			if (getPath().startsWith(workspace)) {
 				String relPath = getPath().substring(workspace.length());
 				res = ResourcesPlugin.getWorkspace().getRoot().findMember(relPath);
 			}
 		}
 		return res;
 	}
 
 	public boolean isDirectory() {
 		return directory;
 	}
 
 	public void setDirectory(boolean directory) {
 		this.directory = directory;
 		setOverwriteVisible(!directory);
 	}
 	
 	private boolean overwriteVisible = true;
 	public void setOverwriteVisible(boolean isVis) {
 		overwriteVisible = isVis;
 		if (overwrite!=null) {
 			GridUtils.setVisible(overwrite, isVis);
 		}
 	}
 
 	protected String getSourcePath(IConversionContext context) {
 		if (context!=null) return context.getFilePaths().get(0);
 		
 		List<String> files = getSelectedFiles();
 		return files!=null ? files.get(0) : "";
 	        
 	}
 	
 	protected List<String> getSelectedFiles() {
 		try {
 			ISelection selection = EclipseUtils.getActivePage().getSelection();
 			StructuredSelection s = (StructuredSelection)selection;
 			if (s.isEmpty())       return null;
 			if (s.toArray()==null) return null;
 			
 			final Object[] oa = s.size() > 1 ? s.toArray() : getObjects(s.getFirstElement());
 			
 			final List<String> ret = new ArrayList<String>(oa.length);
 			for (Object object : oa) {
 				if (object instanceof IFile) {
 					ret.add(((IFile)object).getLocation().toOSString());
 				}
 				if (object instanceof File) {
 					final File file = (File)object;
 					if (file.isFile()) ret.add(file.getAbsolutePath());
 				}
 			}
 			return ret.size()>0 ? ret : null;
 		} catch (Throwable ignored) {
 			return null;
 		}
 	}
 	
 	/**
 	 * If IContainer, iterate contents, else return object in array.
 	 * @param firstElement
 	 * @return
 	 * @throws CoreException 
 	 */
 	private Object[] getObjects(Object firstElement) throws CoreException {
 		if (firstElement instanceof IContainer) {
 			IContainer cont = (IContainer)firstElement;
 			return cont.members();
 		} else if (firstElement instanceof File && ((File)firstElement).isDirectory()){
 			return ((File)firstElement).listFiles();
 		} else {
 			return new Object[]{firstElement};
 		}
 	}
 
 	protected String[] getSelectedPaths() {
 		final List<String> files = getSelectedFiles();
 		if (files==null || files.isEmpty()) return null;
 		return files.toArray(new String[files.size()]);
 	}
 
 
 	private Map<String, IExpressionObject> expressions;
 	/**
 	 * All datasets of the right rank in the conversion file.
 	 * 
 	 * @return
 	 * @throws Exception
 	 */
 	protected List<String> getActiveDatasets(IConversionContext context, IProgressMonitor monitor) throws Exception {
 		final String source = getSourcePath(context);
 		if (source==null || "".equals(source)) return null;
 
 		final ConversionScheme scheme = context.getConversionScheme();
 		IConversionVisitor     visitor= context.getConversionVisitor();
 		final IMetaData        meta   = LoaderFactory.getMetaData(source, new ProgressMonitorWrapper(monitor));
         final List<String>     names  = new ArrayList<String>(7);
         for (String name : meta.getDataShapes().keySet()) {
 			final int[] shape = meta.getDataShapes().get(name);
 			if (scheme!=null && scheme.isRankSupported(shape.length)) {
 				names.add(name);
 			} else if (visitor!=null && visitor.isRankSupported(shape.length)) {
 				names.add(name);
 			}
 		}
         
         // Process any expressions
         
     	final String sourcePath = getSourcePath(context);
     	final IExpressionObjectService service = (IExpressionObjectService)PlatformUI.getWorkbench().getService(IExpressionObjectService.class);
         final List<IExpressionObject>  exprs   = service.getActiveExpressions(sourcePath);
         
         if (exprs!=null && exprs.size()>0) {
         	
         	for (IExpressionObject iExpressionObject : exprs) {
         		final String name = iExpressionObject.getExpressionName()+" [Expression]";
 				names.add(name);
 				if (expressions==null) expressions = new HashMap<String, IExpressionObject>(exprs.size());
 				expressions.put(name, iExpressionObject);
 			}
         	
         }
        
         return names;
 
 	}
 
 	public boolean isNewFile() {
 		return newFile;
 	}
 
 	public void setNewFile(boolean newFile) {
 		this.newFile = newFile;
 	}
 
 	public boolean isPathEditable() {
 		return pathEditable;
 	}
 
 	public void setPathEditable(boolean pathEnabled) {
 		this.pathEditable = pathEnabled;
 		if (txtPath!=null && !txtPath.isDisposed()) {
 			txtPath.setEditable(pathEnabled);
 		}
 	}
 	
 	public void setButtonsEnabled(boolean enabled) {
 		this.buttonsEnabled = enabled;
 		if (resourceButton!=null && !resourceButton.isDisposed()) {
 			resourceButton.setEnabled(enabled);
 		}
 		if (fileButton!=null && !fileButton.isDisposed()) {
 			fileButton.setEnabled(enabled);
 		}
 	}
 	
 	/**
 	 * To be called only after file chooser has been created.
 	 */
 	public void setFileChoosingEnabled(boolean enabled) {
 		GridUtils.setVisible(txtLabel,       enabled);
 		GridUtils.setVisible(txtPath,        enabled);
 		GridUtils.setVisible(resourceButton, enabled);
 		GridUtils.setVisible(fileButton,     enabled);
 		txtLabel.getParent().layout();
 	}
 
 	public String getFileLabel() {
 		return fileLabel;
 	}
 
 	public void setFileLabel(String fileLabel) {
 		this.fileLabel = fileLabel;
 		if (txtLabel!=null  && !txtLabel.isDisposed()) {
 			txtLabel.setText(fileLabel);
 		}
 	}
 
 	protected IExpressionObject getExpression(String datasetName) {
 		return expressions!=null ? expressions.get(datasetName) : null;
 	}
 }
