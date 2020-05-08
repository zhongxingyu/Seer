 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
 import org.eclipse.jst.j2ee.application.internal.operations.ClasspathElement;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EJBJarFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ManifestException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifestImpl;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateInputProvider;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateValidator;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateValidatorImpl;
 import org.eclipse.wst.common.internal.emfworkbench.validateedit.ResourceStateValidatorPresenter;
 
 public class ClasspathModel implements ResourceStateInputProvider, ResourceStateValidator {
 
 	protected IProject project;
 	protected IVirtualComponent selectedEARComponent;
 	protected EARFile earFile;
 	protected IVirtualComponent component;
 	protected Archive archive;
 	public EARArtifactEdit earArtifactEdit;
 	/** The EAR nature runtimes for all the open EAR projects in the workspace */
 	protected IVirtualComponent[] availableEARComponents = null;
 	protected ClassPathSelection classPathSelection;
 	protected List listeners;
 	protected List nonResourceFiles;
 	protected ResourceStateValidator stateValidator;
 	protected ArchiveManifest manifest;
 	public static String NO_EAR_MESSAGE = CommonEditResourceHandler.getString("NO_EAR_JARDEP_FOR_MOD_UI_"); //$NON-NLS-1$
 	protected List targetWLPRefComponentList;
 	protected boolean isWLPModel = false;
 	protected ClassPathSelection classPathWLPSelection;
 
 	protected Comparator comparator = new Comparator() {
 		public int compare(Object o1, Object o2) {
 			IVirtualComponent e1 = (IVirtualComponent) o1;
 			IVirtualComponent e2 = (IVirtualComponent) o2;
 			return e1.getProject().getName().compareTo(e2.getProject().getName());
 		}
 	};
 
 
 	public ClasspathModel(ArchiveManifest initialManifest) {
 		super();
 		manifest = initialManifest;
 	}
 
 	public IProject getProject() {
 		return project;
 	}
 
 	public void setProject(IProject project) {
 		this.project = project;
 		initializeComponent();
 		getAvailableEARComponents();
 	}
 
 	private void initializeComponent() {
 		setComponent(ComponentCore.createComponent(getProject()));
 	}
 
 	protected IVirtualComponent[] refreshAvailableEARs() {
 		if( component != null ){
 			IProject[] earProjects = J2EEProjectUtilities.getReferencingEARProjects(getComponent().getProject());
 			availableEARComponents = ComponentUtilities.getComponents(earProjects);
 			if (availableEARComponents != null && availableEARComponents.length > 0) {
 				Arrays.sort(availableEARComponents, comparator);
 				if (selectedEARComponent == null || !Arrays.asList(availableEARComponents).contains(selectedEARComponent)) {
 					if (availableEARComponents.length > 0)
 						selectedEARComponent = availableEARComponents[0];
 					else
 						selectedEARComponent = null;
 				}
 			}
 		}
 		return availableEARComponents;
 	}
 
 	public IVirtualComponent[] getAvailableEARComponents() {
 		if (availableEARComponents == null)
 			refreshAvailableEARs();
 		return availableEARComponents;
 	}
 
 	/**
 	 * Gets the selectedEARComponent.
 	 * 
 	 * @return Returns a EARNatureRuntime
 	 */
 	public IVirtualComponent getSelectedEARComponent() {
 		return selectedEARComponent;
 	}
 
 	public  void setSelectedEARComponent(IVirtualComponent component) {
 		selectedEARComponent = component;
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.EAR_PROJECT_CHANGED));
 	}
 	
 	public String getArchiveURI() {
 		if (selectedEARComponent != null) {
 			return getEARArtifactEdit().getModuleURI(getComponent()); 
 		}
 		return null;
 	}
 	
 	public EARArtifactEdit getEARArtifactEdit() {
 		if(earArtifactEdit == null || selectedEARComponentChanged()) 
 			earArtifactEdit = EARArtifactEdit.getEARArtifactEditForRead(selectedEARComponent);
 		return earArtifactEdit;
 	}
 
 	private boolean selectedEARComponentChanged() {
 		if(earArtifactEdit != null && !earArtifactEdit.getComponent().getName().equals(selectedEARComponent.getName())) {
 			earArtifactEdit.dispose();
 			earArtifactEdit = null;
 			return true;
 		}
 		return false;
 	}
 
 	protected void initializeEARFile() {
 		if (selectedEARComponent == null || !isDDInEAR(selectedEARComponent)) {
 			earFile = null;
 			return;
 		}
 		try {
 			earFile = (EARFile)getEARArtifactEdit().asArchive(false);
 		} catch (OpenFailureException ex) {
 			handleOpenFailureException(ex);
 		}
 	}
 
 	/**
 	 * initializeSelection method comment.
 	 */
 	protected void initializeSelection(ArchiveManifest existing) {
 		try {
 			initializeEARFile();
 			initializeArchive();
 			if (archive != null) {
 				if (existing == null) {
 					if (manifest != null)
 						archive.setManifest(manifest);
 					else
 						//Load it now because we're going to close the EAR;
 						//this might be a binary project
 						archive.getManifest();
 				} else
 					archive.setManifest(existing);
 				List archiveFiles = earFile.getArchiveFiles();
 				for (int i = 0; i < archiveFiles.size(); i++) {
 					Archive anArchive = (Archive) archiveFiles.get(i);
 					try {
 						if (anArchive.isEJBJarFile())
 							((EJBJarFile) anArchive).getDeploymentDescriptor();
 						anArchive.getManifest();
 					} catch (ManifestException mfEx) {
 						Logger.getLogger().logError(mfEx);
 						anArchive.setManifest((ArchiveManifest) new ArchiveManifestImpl());
 					}
 				}
 			}
 		} finally {
 			if (earFile != null)
 				earFile.close();
 		}
 		createClassPathSelection();
 	}
 
 	protected void initializeArchive() {
 		if (earFile == null) {
 			archive = null;
 			return;
 		}
 		String uri = getArchiveURI();
 		if (uri != null) {
 			try {
 				archive = (Archive) earFile.getFile(uri);
 			} catch (java.io.FileNotFoundException ex) {
 				archive = null;
 			}
 		}
 	}
 
 	protected void createClassPathSelection() {
 		if (archive != null)
 			classPathSelection = new ClassPathSelection(archive, earFile);
 		else
 			classPathSelection = null;
 	}
 
 	protected boolean isDDInEAR(IVirtualComponent aComponent) {
 		IContainer mofRoot = aComponent.getProject();
 		if (mofRoot == null || !mofRoot.exists())
 			return false;
 
 		return mofRoot.exists(new Path(aComponent.getRootFolder().getProjectRelativePath().toString() + "//" + J2EEConstants.APPLICATION_DD_URI)); //$NON-NLS-1$
 	}
 
 	protected void handleOpenFailureException(OpenFailureException ex) {
 		org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 	}
 
 	public void dispose() {
 		   if(earArtifactEdit != null) {
 			   earArtifactEdit.dispose();
 			   earArtifactEdit = null;
 		   }
 	}
 
 	public ClassPathSelection getClassPathSelection() {
 		if (classPathSelection == null)
 			initializeSelection(null);
 		return classPathSelection;
 	}
 
 	public void resetClassPathSelection(ArchiveManifest mf) {
 		initializeSelection(mf);
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.CLASS_PATH_RESET));
 	}
 
 	public void resetClassPathSelection() {
 		resetClassPathSelection(null);
 	}
 
 	public void addListener(ClasspathModelListener listener) {
 		if (listeners == null)
 			listeners = new ArrayList();
 
 		listeners.add(listener);
 	}
 
 	public void removeListener(ClasspathModelListener listener) {
 		if (listeners != null)
 			listeners.remove(listener);
 	}
 
 	public void fireNotification(ClasspathModelEvent evt) {
 		if (listeners == null)
 			return;
 
 		for (int i = 0; i < listeners.size(); i++) {
 			ClasspathModelListener listener = (ClasspathModelListener) listeners.get(i);
 			listener.modelChanged(evt);
 		}
 	}
 
 	/**
 	 * Sets the isSelected for the classpath element and sends out a notification of type
 	 * {@link ClasspathModelEvent#CLASS_PATH_CHANGED}
 	 */
 	public void setSelection(ClasspathElement element, boolean selected) {
 		element.setSelected(selected);
 		if (!isWLPModel()) {
 			updateManifestClasspath();
 		} else {
 			// at least fire the change event
 			fireNotification(new ClasspathModelEvent(ClasspathModelEvent.CLASS_PATH_CHANGED));
 		}
 	}
 
 	/**
 	 * Select or deselect all and notify
 	 */
 	public void setAllClasspathElementsSelected(boolean selected) {
 		ClassPathSelection s = getClassPathSelection();
 		if (s != null) {
 			s.setAllSelected(selected);
 			updateManifestClasspath();
 		}
 	}
 
 	/**
 	 * Select or deselect all and notify
 	 */
 	public void setAllClasspathElementsSelected(List elements, boolean selected) {
 		ClassPathSelection s = getClassPathSelection();
 		if (s != null) {
 			s.setAllSelected(elements, selected);
 			updateManifestClasspath();
 		}
 	}
 
 	/**
 	 * Gets the archive.
 	 * 
 	 * @return Returns a Archive
 	 */
 	public Archive getArchive() {
 		return archive;
 	}
 
 	/**
 	 * Updates the manifest Class-Path:, and sends out a notification of type
 	 * {@link ClasspathModelEvent#CLASS_PATH_CHANGED}
 	 */
 	public void updateManifestClasspath() {
 		if (classPathSelection != null && classPathSelection.isModified()) {
 			archive.getManifest().setClassPath(classPathSelection.toString());
 			fireNotification(new ClasspathModelEvent(ClasspathModelEvent.CLASS_PATH_CHANGED));
 		}
 	}
 
 	/**
 	 * Updates the manifest Main-Class:, and sends out a notification of type
 	 * {@link ClasspathModelEvent#MAIN_CLASS_CHANGED}
 	 */
 	public void updateMainClass(String mainClass) {
 		archive.getManifest().setMainClass(mainClass);
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.MAIN_CLASS_CHANGED));
 	}
 	
 	/**
 	 * Updates the manifest Main-Class:, and sends out a notification of type
 	 * {@link ClasspathModelEvent#MAIN_CLASS_CHANGED}
 	 */
 	public void updateImplVersion(String implVersion) {
 		archive.getManifest().setImplemenationVersion(implVersion);
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.IMPL_VERSION_CHANGED));
 	}
 
 	public void fireSavedEvent() {
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.MODEL_SAVED));
 	}
 
 	/**
 	 * Sets the manifest without touching the archive, or notifying
 	 */
 	public void primSetManifest(ArchiveManifest mf) {
 		manifest = mf;
 	}
 
 	/**
 	 * Sets the manfest on the archive, updates the classpath selection, and notifies
 	 */
 	public void setManifest(ArchiveManifest manifest) {
 		archive.setManifest(manifest);
 		getClassPathSelection(); //Ensure the selection is initialized.
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.MANIFEST_CHANGED));
 	}
 
 	public void selectEAR(int index) {
 		ArchiveManifest mf = new ArchiveManifestImpl((ArchiveManifestImpl) getArchive().getManifest());
 		earFile.close();
 		selectedEARComponent = availableEARComponents[index];
 		initializeSelection(mf);
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.EAR_PROJECT_CHANGED));
 	}
 
 	public void moveUp(List toMoveUp) {
 		getClassPathSelection().moveUp(toMoveUp);
 		updateManifestClasspath();
 	}
 
 	public void moveDown(List toMoveDown) {
 		getClassPathSelection().moveDown(toMoveDown);
 		updateManifestClasspath();
 	}
 
 	public void refresh() {
 		ArchiveManifest mf = null;
 		if (archive != null)
 			mf = new ArchiveManifestImpl((ArchiveManifestImpl) getArchive().getManifest());
 		refreshAvailableEARs();
 		resetClassPathSelection(mf);
 	}
 
 
 	/**
 	 * @see com.ibm.etools.emf.workbench.ResourceStateInputProvider#cacheNonResourceValidateState(List)
 	 */
 	public void cacheNonResourceValidateState(List roNonResourceFiles) {
 	}
 
 
 	/**
 	 * @see com.ibm.etools.emf.workbench.ResourceStateInputProvider#getNonResourceFiles()
 	 */
 	public List getNonResourceFiles() {
 		if (nonResourceFiles == null)
 			initNonResourceFiles();
 		return nonResourceFiles;
 	}
 
 	protected void initNonResourceFiles() {
 		//Might be opened from a JAR
 		if (getComponent() == null)
 			return;
 		nonResourceFiles = new ArrayList(3);
 		nonResourceFiles.add(getComponent().getProject().getFile(ProjectUtilities.DOT_PROJECT));
 		nonResourceFiles.add(getComponent().getProject().getFile(ProjectUtilities.DOT_CLASSPATH));
 		IFile mf = J2EEProjectUtilities.getManifestFile(getComponent().getProject());
 		if (mf != null)
 			nonResourceFiles.add(mf);
 	}
 
 
 	/**
 	 * @see com.ibm.etools.emf.workbench.ResourceStateInputProvider#getNonResourceInconsistentFiles()
 	 */
 	public List getNonResourceInconsistentFiles() {
 		return null;
 	}
 
 	/**
 	 * @see com.ibm.etools.emf.workbench.ResourceStateInputProvider#isDirty()
 	 */
 	public boolean isDirty() {
 		ClassPathSelection selection = getClassPathSelection();
 		if (selection == null)
 			return false;
 		return selection.isModified();
 	}
 
 
 	/**
 	 * Return a list of all the files that will get modified as a result of running this operation;
 	 * used for validateEdit
 	 */
 	public Set getAffectedFiles() {
 		Set result = new HashSet();
 		IFile aFile = J2EEProjectUtilities.getManifestFile(getComponent().getProject());
 		if (aFile != null && aFile.exists())
 			result.add(aFile);
 		result.addAll(ProjectUtilities.getFilesAffectedByClasspathChange(getComponent().getProject()));
 		return result;
 	}
 
 	/**
 	 * @see com.ibm.etools.emf.workbench.ResourceStateInputProvider#getResources()
 	 */
 	public List getResources() {
 		return Collections.EMPTY_LIST;
 	}
 
 	public boolean selectDependencyIfNecessary(IProject referencedProject) {
 		getClassPathSelection();
 		if (classPathSelection == null || classPathSelection.hasDirectOrIndirectDependencyTo(referencedProject))
 			return false;
 
 		ClasspathElement element = classPathSelection.getClasspathElement(referencedProject);
 		if (element != null) {
 			setSelection(element, true);
 			return true;
 		}
 		return false;
 	}
 
 	public boolean selectDependencyIfNecessary(String jarName) {
 		getClassPathSelection();
 		if (classPathSelection == null || classPathSelection.hasDirectOrIndirectDependencyTo(jarName))
 			return false;
 
 		ClasspathElement element = classPathSelection.getClasspathElement(jarName);
 		if (element != null) {
 			setSelection(element, true);
 			return true;
 		}
 		return false;
 	}
 
 	public void removeDependency(String jarName) {
 		getClassPathSelection();
 		if (classPathSelection == null)
 			return;
 
 		ClasspathElement element = classPathSelection.getClasspathElement(jarName);
 		if (element != null && element.isValid())
 			setSelection(element, false);
 	}
 
 	public void removeDependency(IProject referencedProject) {
 		getClassPathSelection();
 		if (classPathSelection == null)
 			return;
 
 		ClasspathElement element = classPathSelection.getClasspathElement(referencedProject);
 		if (element != null && element.isValid())
 			setSelection(element, false);
 	}
 
 	public void selectFilterLevel(int filterLevel) {
 		getClassPathSelection();
 		if (classPathSelection != null)
 			classPathSelection.selectFilterLevel(filterLevel);
 		updateManifestClasspath();
 	}
 
 	/**
 	 * Gets the stateValidator.
 	 * 
 	 * @return Returns a ResourceStateValidator
 	 */
 	public ResourceStateValidator getStateValidator() {
 		if (stateValidator == null)
 			stateValidator = createStateValidator();
 		return stateValidator;
 	}
 
 	/**
 	 * Method createStateValidator.
 	 * 
 	 * @return ResourceStateValidator
 	 */
 	private ResourceStateValidator createStateValidator() {
 		return new ResourceStateValidatorImpl(this);
 	}
 
 	/**
 	 * @see ResourceStateValidator#checkActivation(ResourceStateValidatorPresenter)
 	 */
 	public void checkActivation(ResourceStateValidatorPresenter presenter) throws CoreException {
 		getStateValidator().checkActivation(presenter);
 	}
 
 	/**
 	 * @see ResourceStateValidator#lostActivation(ResourceStateValidatorPresenter)
 	 */
 	public void lostActivation(ResourceStateValidatorPresenter presenter) throws CoreException {
 		getStateValidator().lostActivation(presenter);
 	}
 
 	/**
 	 * @see ResourceStateValidator#validateState(ResourceStateValidatorPresenter)
 	 */
 	public IStatus validateState(ResourceStateValidatorPresenter presenter) throws CoreException {
 		return getStateValidator().validateState(presenter);
 	}
 
 	/**
 	 * @see ResourceStateValidator#checkSave(ResourceStateValidatorPresenter)
 	 */
 	public boolean checkSave(ResourceStateValidatorPresenter presenter) throws CoreException {
 		return getStateValidator().checkSave(presenter);
 	}
 
 	/**
 	 * @see ResourceStateValidator#checkReadOnly()
 	 */
 	public boolean checkReadOnly() {
 		return getStateValidator().checkReadOnly();
 	}
 
 	public IVirtualComponent getComponent() {
 		return component;
 	}
 
 	public void setComponent(IVirtualComponent component) {
 		this.component = component;
 	}
 
 	public ClassPathSelection getClassPathSelectionForWLPs() {
 		if(classPathWLPSelection == null)
 			initializeSelectionForWLPs();
 		return classPathWLPSelection;
 	}
 
 	private void initializeSelectionForWLPs() {
 		classPathWLPSelection = new ClassPathSelection();
 		try {
 			IJavaProject javaProject = JemProjectUtilities.getJavaProject(component.getProject());
 			IClasspathEntry[] entry = javaProject.getRawClasspath();
			List allValidUtilityProjects = J2EEProjectUtilities.getAllJavaNonFlexProjects();
 			IProject[] utilityProjects = J2EEProjectUtilities.getAllProjectsInWorkspaceOfType(J2EEProjectUtilities.UTILITY);
			allValidUtilityProjects.addAll(Arrays.asList(utilityProjects));
			for (int i = 0; i < allValidUtilityProjects.size(); i++) {
 				IProject utilProject = null;
				if (allValidUtilityProjects.get(i) instanceof IProject){
					utilProject = (IProject) allValidUtilityProjects.get(i);
 					if( utilProject.getName().startsWith(".")){
 						continue;
 					}
 				}
				else if (allValidUtilityProjects.get(i) instanceof IVirtualComponent)
					utilProject = ((IVirtualComponent) allValidUtilityProjects.get(i)).getProject();
 				boolean existingEntry = false;
 				for (int j = 0; j < entry.length; j++) {
 					IClasspathEntry eachEntry = entry[j];
 					if (eachEntry.getEntryKind() == IClasspathEntry.CPE_PROJECT && eachEntry.getPath().toString().equals("/" + utilProject.getName())) { //$NON-NLS-1$
 
 						IVirtualReference ref = component.getReference(utilProject.getName());
 						IPath path = new Path("/WEB-INF/lib");
 						if( ref != null && ref.getRuntimePath().equals(path)){
 							existingEntry = true;
 						}
 						break;
 					}
 				}
 				classPathWLPSelection.createProjectElement(utilProject, existingEntry);
 				classPathWLPSelection.setFilterLevel(ClassPathSelection.FILTER_NONE);
 			}
 			
 			if( component != null && J2EEProjectUtilities.isDynamicWebProject(component.getProject())){
 				IVirtualReference[] newrefs = component.getReferences();
 				for( int i=0; i < newrefs.length; i++){
 					IVirtualReference ref = newrefs[i];
 					IVirtualComponent referencedComponent = ref.getReferencedComponent();
 					if (referencedComponent == null)
 						continue;
 					boolean isBinary = referencedComponent.isBinary();
 					if( isBinary ){
 						//String uri = ComponentUtilities.getResolvedPathForArchiveComponent(referencedComponent.getName()).toString();
 						String unresolvedURI = ""; //$NON-NLS-1$
 						try {
 							unresolvedURI = ModuleURIUtil.getArchiveName(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)));
 						} catch (UnresolveableURIException e) {
 							e.printStackTrace();
 						}
 						URI archiveURI = URI.createURI(unresolvedURI);	
 						
 						boolean  alreadyInList = false;
 						Iterator iter = classPathWLPSelection.getClasspathElements().iterator();
 						while(iter.hasNext()){
 							ClasspathElement tmpelement = (ClasspathElement)iter.next();
 							if( tmpelement.getText().equals(archiveURI.lastSegment())){
 								alreadyInList = true;
 								break;
 							}
 						}
 						ClasspathElement element = null;
 						if( !alreadyInList ){
 							if( inClassPath(javaProject, archiveURI.lastSegment())){
 								element = classPathWLPSelection.createArchiveElement(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)), referencedComponent.getName(), archiveURI.lastSegment());
 								classPathWLPSelection.addClasspathElement(element, unresolvedURI);
 							}
 							else
 							{
 								element = classPathWLPSelection.createArchiveElement(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)), archiveURI.lastSegment(), null);
 								classPathWLPSelection.addClasspathElement(element, unresolvedURI);							
 							}
 						}
 					}
 				}	//for
 			}	
 //			for (int j = 0; j < entry.length; j++) {
 //				IClasspathEntry eachEntry = entry[j];
 //				if (eachEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY ) {
 //					classPathWLPSelection.crcrecreateProjectElement(utilProject, existingEntry);
 //				}
 //			}			
 		} catch (CoreException e) {
 		}catch (Exception e) {
 			Logger.getLogger().logError(e);
 		}
 	}
 
 	boolean inClassPath(IJavaProject javaProject, String archiveName ){
 		boolean existingEntry = false;
 		IClasspathEntry[] entry = null;
 		try {
 			entry = javaProject.getRawClasspath();
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		}
 		for (int j = 0; j < entry.length; j++) {
 			IClasspathEntry eachEntry = entry[j];
 			if (eachEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY || eachEntry.getEntryKind() == IClasspathEntry.CPE_VARIABLE ) {
 				if( eachEntry.getPath().lastSegment().equals(archiveName)){
 					existingEntry = true;
 					break;
 				}
 			}
 		}
 		return existingEntry;
 	}
 	
 	
 	public boolean isWLPModel() {
 		return isWLPModel;
 	}
 
 	public void setWLPModel(boolean isWLPModel) {
 		this.isWLPModel = isWLPModel;
 	}
 
 }
