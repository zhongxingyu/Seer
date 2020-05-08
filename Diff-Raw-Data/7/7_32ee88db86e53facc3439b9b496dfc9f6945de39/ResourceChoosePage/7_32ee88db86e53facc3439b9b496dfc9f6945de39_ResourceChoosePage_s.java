 package org.dawb.common.ui.wizard;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.dawb.common.services.conversion.IConversionContext;
 import org.dawb.common.services.conversion.IConversionContext.ConversionScheme;
 import org.dawb.common.ui.Activator;
 import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
 import org.dawb.common.ui.util.EclipseUtils;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
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
 import uk.ac.gda.ui.content.FileContentProposalProvider;
 
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
 	private String path;
	private String fileLabel="File location";
 	private Text txtPath;
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
 		
 		Label txtLabel = new Label(container, SWT.NULL);
		txtLabel.setText(isDirectory() ? "&Folder  " : "&File  ");
 		txtLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		
 		this.txtPath = new Text(container, SWT.BORDER);
 		txtPath.setEditable(pathEditable);
 		
 		FileContentProposalProvider prov = new FileContentProposalProvider();
 		ContentProposalAdapter ad = new ContentProposalAdapter(txtPath, new TextContentAdapter(), prov, null, null);
 		ad.setProposalAcceptanceStyle(ContentProposalAdapter.PROPOSAL_REPLACE);
		txtPath.setToolTipText(getFileLabel());
         if (getPath()!=null) txtPath.setText(getPath());
 		txtPath.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		txtPath.addModifyListener(new ModifyListener() {			
 			@Override
 			public void modifyText(ModifyEvent e) {
 				path = txtPath.getText();
 				pathChanged();
 			}
 		});
 
 		Button button = new Button(container, SWT.PUSH);
 		button.setText("...");
 		button.setImage(Activator.getImageDescriptor("icons/Project-data.png").createImage());
 		button.setToolTipText("Browse to "+(isDirectory()?"folder":"file")+" inside a project");
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleResourceBrowse();
 			}
 		});
 		
 		button = new Button(container, SWT.PUSH);
 		button.setText("...");
 		button.setImage(Activator.getImageDescriptor("icons/data_folder_link.gif").createImage());
 		button.setToolTipText("Browse to an external "+(isDirectory()?"folder":"file")+".");
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleFileBrowse();
 			}
 		});
 		
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
 
 	public String getPath() {
 		return path;
 	}
 	
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
 	}
 	
 	
 	
 	protected String getSourcePath(IConversionContext context) {
 		if (context!=null) return context.getFilePath();
 		
 		try {
 			ISelection selection = EclipseUtils.getActivePage().getSelection();
 			StructuredSelection s = (StructuredSelection)selection;
 			final Object        o = s.getFirstElement();
 			if (o instanceof IFile) {
 				IFile source = (IFile)o;
 				return source.getLocation().toOSString();
 			}
 		} catch (Throwable ignored) {
 			// default ""
 		}
 		return "";
 	        
 	}
 	
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
 		final IMetaData        meta   = LoaderFactory.getMetaData(source, new ProgressMonitorWrapper(monitor));
         final List<String>     names  = new ArrayList<String>(7);
         for (String name : meta.getDataShapes().keySet()) {
 			final int[] shape = meta.getDataShapes().get(name);
 			if (scheme.isRankSupported(shape.length)) {
 				names.add(name);
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
 	}
 
 	public String getFileLabel() {
 		return fileLabel;
 	}
 
 	public void setFileLabel(String fileLabel) {
 		this.fileLabel = fileLabel;
 	}
 }
