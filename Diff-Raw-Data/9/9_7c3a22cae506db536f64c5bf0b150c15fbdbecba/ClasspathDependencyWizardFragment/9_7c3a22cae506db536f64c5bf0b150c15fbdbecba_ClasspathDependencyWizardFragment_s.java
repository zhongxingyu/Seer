 /******************************************************************************
  * Copyright (c) 2009 Red Hat
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Rob Stryker - initial implementation and ongoing maintenance
  ******************************************************************************/
 package org.eclipse.jst.j2ee.internal.ui.preferences;
 
 import java.lang.reflect.InvocationTargetException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.CheckStateChangedEvent;
 import org.eclipse.jface.viewers.CheckboxTreeViewer;
 import org.eclipse.jface.viewers.ICheckStateListener;
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants.DependencyAttributeType;
 import org.eclipse.jst.j2ee.classpathdep.UpdateClasspathAttributeUtil;
 import org.eclipse.jst.j2ee.internal.ManifestUIResourceHandler;
 import org.eclipse.jst.j2ee.internal.modulecore.util.DummyClasspathDependencyContainerVirtualComponent;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.ui.internal.propertypage.IReferenceEditor;
 import org.eclipse.wst.common.componentcore.ui.internal.taskwizard.IWizardHandle;
 import org.eclipse.wst.common.componentcore.ui.internal.taskwizard.WizardFragment;
 import org.eclipse.wst.common.componentcore.ui.propertypage.IReferenceWizardConstants;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.frameworks.internal.ui.WTPUIPlugin;
 import org.eclipse.wst.common.frameworks.internal.ui.WorkspaceModifyComposedOperation;
 
 /**
  * Handle the case of ears pulling in stuff from children
  * @author rob
  *
  */
 public class ClasspathDependencyWizardFragment extends WizardFragment implements IReferenceEditor {
 
 	private CheckboxTreeViewer viewer;
 	private IWizardHandle handle;
 	private HashMap<IProject, WrappedClasspathEntry[]> map = new HashMap<IProject, WrappedClasspathEntry[]>();
 	private ArrayList<WrappedClasspathEntry> originallyChecked = new ArrayList<WrappedClasspathEntry>();
 	public ClasspathDependencyWizardFragment() {
 	}
 
 	boolean isComplete = false;
 
 	@Override
 	public boolean isComplete() {
 		return isComplete;
 	}
 	
 	@Override
 	public boolean hasComposite() {
 		return true;
 	}
 
 	public boolean canEdit(IVirtualReference ref) {
 		return ref.getReferencedComponent() instanceof DummyClasspathDependencyContainerVirtualComponent;
 	}
 	
 	@Override
 	public Composite createComposite(Composite parent, IWizardHandle handle) {
 		this.handle = handle;
 		Composite c = new Composite(parent, SWT.NONE);
 		c.setLayout(new GridLayout());
 		handle.setTitle(Messages.ClasspathDependencyFragmentTitle);
 		handle.setDescription(Messages.ClasspathDependencyFragmentDescription);
 		viewer = new CheckboxTreeViewer(c);
 	    viewer.setContentProvider(new ClasspathPushupContentProvider());
 	    viewer.setLabelProvider(new ClasspathPushupLabelProvider());
 	    try {
 	    	loadModel();
 	    } catch( Exception e) {e.printStackTrace();}
 	    viewer.setInput(ResourcesPlugin.getPlugin()); // ignored
 	    viewer.expandAll();
 	    viewer.setGrayedElements(getSuitableProjects());
 	    viewer.setCheckedElements(originallyChecked.toArray(new WrappedClasspathEntry[originallyChecked.size()]));
 	    viewer.addCheckStateListener(new ICheckStateListener() {
 	        public void checkStateChanged(CheckStateChangedEvent event) {
 	        	handleCheckEvent(event);
 	        }
 	      });
 	    GridData data = new GridData(GridData.FILL_BOTH);
 		data.widthHint = 390;
 		data.heightHint = 185;
 		viewer.getTree().setLayoutData(data);
 		return c;
 	}
 	
 	protected void handleCheckEvent(CheckStateChangedEvent event) {
         if (event.getChecked()) {
             // cannot check projects
         	if( event.getElement() instanceof IProject ) 
         		viewer.setChecked(event.getElement(), false);
         }
         // Set the result for later
         if( event.getElement() instanceof WrappedClasspathEntry ) {
         	WrappedClasspathEntry wce = ((WrappedClasspathEntry)event.getElement());
         	wce.postStatus = event.getChecked();
         }
         
         Iterator<IProject> i = map.keySet().iterator();
         boolean hasChanged = false;
 		while(i.hasNext()) {
 			IProject p = i.next();
 			WrappedClasspathEntry[] entries = map.get(p);
 			for( int j = 0; j < entries.length; j++ ) {
 				if( entries[j].postStatus != entries[j].preStatus) {
 					hasChanged = true;
 					break;
 				}
 			}
 			if(hasChanged)
 				break;
 		}
 		if(isComplete != hasChanged) {
 			isComplete = hasChanged;
 			handle.update();
 		}				
 	}
 
 	// label provider
 	private class ClasspathPushupLabelProvider extends LabelProvider {
 		private Image classpathImage = null;
 		
 		@Override
 	    public void dispose() {
 	    	super.dispose();
 	    	if( classpathImage != null && !classpathImage.isDisposed())
 	    		classpathImage.dispose();
 	    }
 		
 	    @Override
 		public Image getImage(Object element) {
 			if( element instanceof IProject )
 				return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_PROJECT);
 			if (classpathImage == null) {
 				ImageDescriptor imageDescriptor = null;
 				URL gifImageURL = (URL) J2EEPlugin.getPlugin().getImage("CPDep"); //$NON-NLS-1$
 				imageDescriptor = ImageDescriptor.createFromURL(gifImageURL);
 				classpathImage = imageDescriptor.createImage();
 			}
 			return classpathImage;
 		}
 		@Override
 		public String getText(Object element) {
 			if( element instanceof IProject )
 				return ((IProject)element).getName();
 			if(element instanceof WrappedClasspathEntry) {
 				try {
 					WrappedClasspathEntry entry = (WrappedClasspathEntry)element;
 					if (entry.entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 						final IClasspathContainer container = JavaCore.getClasspathContainer(
 								entry.entry.getPath(), JavaCore.create(entry.project));
 						if (container != null) {
 							return container.getDescription();
 						}
 					}
 					else {
 						// use path for non-container type classpath entries
 						return entry.entry.getPath().toOSString();
 					}
 				} catch( JavaModelException jme) {
 					// ignore
 				}
 			}
 			return element == null ? "" : element.toString();//$NON-NLS-1$
 		}
 	}
 	
 	// wrap classpath entries into a slightly more useful form
 	private WrappedClasspathEntry[] wrapClasspathEntries(IClasspathEntry[] entries, IProject project, boolean preStatus) {
 		WrappedClasspathEntry[] results = new WrappedClasspathEntry[entries.length];
 		for( int i = 0; i < entries.length; i++ )
 			results[i] = new WrappedClasspathEntry(entries[i], project, preStatus, preStatus);
 		return results;
 	}
 	
 	// class that can cache the entry, project, and pre and post status of the attribute
 	private class WrappedClasspathEntry {
 		public IClasspathEntry entry;
 		public IProject project;
 		boolean preStatus;
 		boolean postStatus;
 		public WrappedClasspathEntry(IClasspathEntry entry2, IProject project2, boolean preStatus2, boolean postStatus2) {
 			this.entry = entry2;
 			this.project = project2;
 			this.preStatus = preStatus2;
 			this.postStatus = postStatus2;
 		}
 	}
 	
 	private class ClasspathPushupContentProvider implements ITreeContentProvider {
 		public Object[] getChildren(Object parentElement) {
 			if( parentElement instanceof IProject ) {
 				Object[] ret = map.get(parentElement);
 				return ret == null ? new Object[]{} : ret;
 			}
 			return new Object[]{};
 		}
 
 		
 		public Object getParent(Object element) {
 			return null;
 		}
 
 		public boolean hasChildren(Object element) {
 			return getChildren(element).length > 0;
 		}
 
 		public Object[] getElements(Object inputElement) {
 			// return suitable projects
 			return getSuitableProjects();
 		}
 		
 		public void dispose() {
 		}
 
 		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		}
 	}
 	
 	// creating the individual pieces
 	private WrappedClasspathEntry[] loadEntriesForProject(IProject parentElement) {
 		ArrayList<WrappedClasspathEntry> ret = new ArrayList<WrappedClasspathEntry>();
 		try {
 			Map <IClasspathEntry, IClasspathAttribute> results = 
 				ClasspathDependencyUtil.getRawComponentClasspathDependencies(
 					JavaCoreLite.create(parentElement), 
 							DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY);
 			
 			Set<IClasspathEntry> setResult = results.keySet();
 			IClasspathEntry[] setResultArray = 
 				setResult.toArray(new IClasspathEntry[setResult.size()]);
 			WrappedClasspathEntry[] wrappedArray = wrapClasspathEntries(setResultArray, parentElement, true);
 			originallyChecked.addAll(Arrays.asList(wrappedArray));
 			ret.addAll(Arrays.asList(wrappedArray));
 			
 			List<IClasspathEntry> potentialList = 
 				ClasspathDependencyUtil.getPotentialComponentClasspathDependencies(
 						JavaCoreLite.create(parentElement));
 			IClasspathEntry[] potentials = potentialList.toArray(new IClasspathEntry[potentialList.size()]);
 			ret.addAll(Arrays.asList(wrapClasspathEntries(potentials, parentElement, false)));
 		} catch( CoreException ce ) {
 			// ignore
 		}
 		return ret.toArray(new WrappedClasspathEntry[ret.size()]);
 	}
 
 	// Only projects with java nature are suitable here
 	protected IProject[] getSuitableProjects() {
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		ArrayList<IProject> projs = new ArrayList<IProject>();
 		for( int i = 0; i < projects.length; i++ ) {
 			try {
 				if( projects[i].hasNature(JavaCoreLite.NATURE_ID) && ModuleCoreNature.isFlexibleProject(projects[i]))
 					projs.add(projects[i]);
 			} catch( CoreException ce) {
 				// ignore
 			}
 		}
 		return projs.toArray(new IProject[projs.size()]);
 	}
 	
 	protected void loadModel() {
 		IProject[] projects = getSuitableProjects();
 		for( int i = 0; i < projects.length; i++ ) {
 			map.put(projects[i], loadEntriesForProject(projects[i]));
 		}
 	}
 	
 	@Override
 	public void performFinish(IProgressMonitor monitor) throws CoreException {
 		// iterate through and actually change the stuff
 		boolean anyChecked = false;
 		Iterator<IProject> i = map.keySet().iterator();
 		final WorkspaceModifyComposedOperation composedOp = new WorkspaceModifyComposedOperation();
 		while(i.hasNext()) {
 			IProject p = i.next();
 			WrappedClasspathEntry[] entries = map.get(p);
 			ArrayList<WrappedClasspathEntry> changed = new ArrayList<WrappedClasspathEntry>();
 			for( int j = 0; j < entries.length; j++ ) {
 				if( entries[j].postStatus) anyChecked = true;
 				if( entries[j].preStatus != entries[j].postStatus) {
 					changed.add(entries[j]);
 				}
 			}
 			if( !changed.isEmpty()) {
 				prepareChanges(composedOp, changed, p);
 			}
 		}
 		try {
 			composedOp.run(new NullProgressMonitor());
 		} catch( InvocationTargetException e ) {
 			final InvocationTargetException ex2 = e;
 			Display.getCurrent().asyncExec(new Runnable() {
 				public void run() {
 						final Shell shell = ((WizardPage)handle).getShell();
 						String title = ManifestUIResourceHandler.An_internal_error_occurred_ERROR_;
 						String msg = title;
 						if (ex2.getTargetException() != null && ex2.getTargetException().getMessage() != null)
 							msg = ex2.getTargetException().getMessage();
 						MessageDialog.openError(shell, title, msg);
 						org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex2);
 				}
 			});
 		} catch (InterruptedException e) {
 			// ignore
 		}
 		
 		if( anyChecked ) {
 			IProject project = (IProject)getTaskModel().getObject(IReferenceWizardConstants.PROJECT);
 			IVirtualComponent root = (IVirtualComponent)getTaskModel().getObject(IReferenceWizardConstants.ROOT_COMPONENT);
 			IVirtualComponent imported = new DummyClasspathDependencyContainerVirtualComponent(project, root);
 			VirtualReference ref = new VirtualReference(root, imported);
 			ref.setDerived(true);
 			ref.setRuntimePath(new Path("/")); //$NON-NLS-1$
 			getTaskModel().putObject(IReferenceWizardConstants.FINAL_REFERENCE, ref); 
 		} else {
 			getTaskModel().putObject(IReferenceWizardConstants.FINAL_REFERENCE, null);
 		}
 	}
 
 	protected void prepareChanges(WorkspaceModifyComposedOperation composedOp, 
 			ArrayList<WrappedClasspathEntry> elements, IProject project) {
 		final Map<IClasspathEntry, IPath> selectedEntriesToRuntimePath = new HashMap<IClasspathEntry, IPath>();
 		final Map<IClasspathEntry, IPath> unselectedEntriesToRuntimePath = new HashMap<IClasspathEntry, IPath>();
 		final IPath runtimePath = IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH; 
 		
 		for (int i = 0; i < elements.size(); i++) {
 			WrappedClasspathEntry wrapped = elements.get(i);
 			final IClasspathEntry entry = wrapped.entry;
 			if( !wrapped.preStatus && wrapped.postStatus )
 				selectedEntriesToRuntimePath.put(entry, runtimePath);
 			else if( wrapped.preStatus && !wrapped.postStatus) 
 				unselectedEntriesToRuntimePath.put(entry, runtimePath);
 		}
 		
 		// if there are any attributes to add, create an operation to add all necessary attributes 
 		if (!selectedEntriesToRuntimePath.isEmpty()) {
 			IDataModelOperation op = UpdateClasspathAttributeUtil.createAddDependencyAttributesOperation(project.getName(), selectedEntriesToRuntimePath); 
 			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(op));
 		}
 		
 		// if there are any attributes to remove, create an operation to remove all necessary attributes
 		if (!unselectedEntriesToRuntimePath.isEmpty()) {
 			IDataModelOperation op = UpdateClasspathAttributeUtil.createRemoveDependencyAttributesOperation(project.getName(), unselectedEntriesToRuntimePath); 
 			composedOp.addRunnable(WTPUIPlugin.getRunnableWithProgress(op));
 		}
 	}
 }
