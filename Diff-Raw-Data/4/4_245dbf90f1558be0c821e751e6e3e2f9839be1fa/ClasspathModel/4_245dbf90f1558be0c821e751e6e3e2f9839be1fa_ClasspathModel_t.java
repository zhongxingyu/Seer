 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common;
 
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
 import org.eclipse.jst.j2ee.application.internal.operations.ClasspathElement;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.DeploymentDescriptorLoadException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.ManifestException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifestImpl;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.JavaEEArchiveUtilities;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.jee.archive.ArchiveOpenFailureException;
 import org.eclipse.jst.jee.archive.IArchive;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ModuleStructuralModel;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
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
 	protected IArchive earFile;
 	protected IVirtualComponent component;
 //	protected IArchive archive;
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
 		this(initialManifest, false);
 	}
 	
 	public ClasspathModel(ArchiveManifest initialManifest, boolean isWLPModel) {
 		super();
 		manifest = initialManifest;
 		this.isWLPModel = isWLPModel;
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
 		if (component != null) {
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
 
 	public void setSelectedEARComponent(IVirtualComponent component) {
 		selectedEARComponent = component;
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.EAR_PROJECT_CHANGED));
 	}
 
 	public String getArchiveURI() {
 		if (selectedEARComponent != null) {
 			IVirtualReference [] refs = selectedEARComponent.getReferences();
 			IVirtualComponent moduleComp = getComponent();
 			for(int i=0; i<refs.length; i++){
 				if(refs[i].getReferencedComponent().equals(moduleComp)){
 					return refs[i].getArchiveName();
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @deprecated - will be removed in WTP 3.0
 	 */
 	public EARArtifactEdit getEARArtifactEdit() {
 		if (earArtifactEdit == null || selectedEARComponentChanged())
 			earArtifactEdit = EARArtifactEdit.getEARArtifactEditForRead(selectedEARComponent);
 		return earArtifactEdit;
 	}
 
 	private boolean selectedEARComponentChanged() {
 		if (earArtifactEdit != null && !earArtifactEdit.getComponent().getName().equals(selectedEARComponent.getName())) {
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
 			earFile = JavaEEArchiveUtilities.INSTANCE.openArchive(selectedEARComponent);
 		} catch (ArchiveOpenFailureException aofex)
 		{
 			handleOpenFailureException(aofex);
 		}
 	}
 
 	/**
 	 * initializeSelection method comment.
 	 */
 	protected void initializeSelection(ArchiveManifest existing) {
 		try {
 			initializeEARFile();
 			initializeArchive();
 			if (!J2EEProjectUtilities.isEARProject(getProject())) {
 				if (getProject() != null) {
 					if (existing == null) {
 						if (manifest != null)
 						{
 							J2EEProjectUtilities.writeManifest(getProject(), manifest);
 						}
 						else
 							// Load it now because we're going to close the EAR;
 							// this might be a binary project
 							manifest = J2EEProjectUtilities.readManifest(getProject());
 					} else
 					{
 						J2EEProjectUtilities.writeManifest(getProject(), existing);
 						manifest = existing;
 					}
 					if (selectedEARComponent != null) {
 						IVirtualReference[] archiveFiles = selectedEARComponent.getReferences();
 						IVirtualComponent anArchive = null;
 						for (int i = 0; i < archiveFiles.length; i++) {
 							anArchive = archiveFiles[i].getReferencedComponent();
 							try {
 								if (J2EEProjectUtilities.isEJBComponent(anArchive)) {
 									IModelProvider modelProvider = ModelProviderManager.getModelProvider(anArchive.getProject());
 									modelProvider.getModelObject();
 								}
 								J2EEProjectUtilities.readManifest(anArchive.getProject());
 							} catch (ManifestException mfEx) {
 								Logger.getLogger().logError(mfEx);
 								// anArchive.setManifest((ArchiveManifest) new ArchiveManifestImpl());
 							} catch (DeploymentDescriptorLoadException ddException) {
 								Logger.getLogger().logError(ddException);
 							}
 						}
 					}
 				}
 				createClassPathSelection();
 			}
 		} catch (IOException e) {
 			handleOpenFailureException(e);
 		} finally {
 			if (earFile != null)
 				JavaEEArchiveUtilities.INSTANCE.closeArchive(earFile);
 		}
 	}
 
 	protected void initializeArchive() {
 		// do nothing now- just use the project
 	}
 
 	protected void createClassPathSelection() {
 		if (getComponent() != null && selectedEARComponent != null ) {
 			classPathSelection = new ClassPathSelection(getComponent(), selectedEARComponent);
 		} else if (selectedEARComponent == null) {
 			classPathSelection = new ClassPathSelection(getComponent());
 		} else {
 			classPathSelection = null;
 		}
 	}
 
 	protected boolean isDDInEAR(IVirtualComponent aComponent) {
 		IContainer mofRoot = aComponent.getProject();
 		if (mofRoot == null || !mofRoot.exists())
 			return false;
 
 		return mofRoot.exists(new Path(aComponent.getRootFolder().getProjectRelativePath().toString() + "//" + J2EEConstants.APPLICATION_DD_URI)); //$NON-NLS-1$
 	}
 
 	protected void handleOpenFailureException(Exception ex) {
 		org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 	}
 
 	public void dispose() {
 		if (earArtifactEdit != null) {
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
 	
 	public void resetClassPathSelectionForWLPs() {
 		classPathWLPSelection = null;
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
 	 * @deprecated - use the project
 	 */
 	public Archive getArchive() {
 		return null;
 	}
 
 	/**
 	 * Updates the manifest Class-Path:, and sends out a notification of type
 	 * {@link ClasspathModelEvent#CLASS_PATH_CHANGED}
 	 */
 	public void updateManifestClasspath() {
 		if (classPathSelection != null && classPathSelection.isModified()) {
 			manifest.setClassPath(classPathSelection.toString());
 			fireNotification(new ClasspathModelEvent(ClasspathModelEvent.CLASS_PATH_CHANGED));
 		}
 	}
 
 	/**
 	 * Updates the manifest Main-Class:, and sends out a notification of type
 	 * {@link ClasspathModelEvent#MAIN_CLASS_CHANGED}
 	 */
 	public void updateMainClass(String mainClass) {
 		manifest.setMainClass(mainClass);
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.MAIN_CLASS_CHANGED));
 	}
 
 	/**
 	 * Updates the manifest Main-Class:, and sends out a notification of type
 	 * {@link ClasspathModelEvent#MAIN_CLASS_CHANGED}
 	 */
 	public void updateImplVersion(String implVersion) {
 		manifest.setImplemenationVersion(implVersion);
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
 	public void setManifest(ArchiveManifest newManifest) {
 		try {
 			J2EEProjectUtilities.writeManifest(getProject(), newManifest);
 		} catch (IOException e) {
 			handleOpenFailureException(e);
 		}
 		getClassPathSelection(); // Ensure the selection is initialized.
 		fireNotification(new ClasspathModelEvent(ClasspathModelEvent.MANIFEST_CHANGED));
 	}
 
 	public void selectEAR(int index) {
 		ArchiveManifest mf = new ArchiveManifestImpl((ArchiveManifestImpl) getArchive().getManifest());
 		JavaEEArchiveUtilities.INSTANCE.closeArchive(earFile);
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
 		if (getComponent() != null)
 			mf = J2EEProjectUtilities.readManifest(getProject());
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
 		// Might be opened from a JAR
 		if (getComponent() == null){
 			return;
 		} 
 		nonResourceFiles = new ArrayList(1);
 		if(isWLPModel){
 			IFile dotProject = getComponent().getProject().getFile(ProjectUtilities.DOT_PROJECT);
 			if(dotProject != null){
 				nonResourceFiles.add(dotProject);
 			}
 		} else {
 			IFile mf = J2EEProjectUtilities.getManifestFile(getComponent().getProject());
 			if (mf != null){
 				nonResourceFiles.add(mf);
 			}
 		}
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
 		if(isWLPModel){
 			StructureEdit sEdit = null;
 			try {
 				sEdit = StructureEdit.getStructureEditForWrite(project);
 				ModuleStructuralModel structuralModel = sEdit.getModuleStructuralModel();
 				List resources = new ArrayList();
 				resources.addAll(structuralModel.getResources());
 				return resources;
 			} catch(Exception e){
 				e.printStackTrace();
 				return Collections.EMPTY_LIST;	
 			}
 			finally {
 				if (sEdit !=null){
 					sEdit.dispose();
 				}
 			}
 		} else {
 			return Collections.EMPTY_LIST;
 		}
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
 		if (classPathWLPSelection == null)
 			initializeSelectionForWLPs();
 		return classPathWLPSelection;
 	}
 
 	//copied from WebArtifactEdit
 	private static IPath WEBLIB =  new Path(J2EEConstants.WEB_INF_LIB).makeAbsolute();
	private static IPath WEBINF_CLASSES =  new Path(J2EEConstants.WEB_INF_CLASSES).makeAbsolute();
 	private static IVirtualReference[] getLibModules(IProject project) {
 		List result = new ArrayList();
 		IVirtualComponent comp = ComponentCore.createComponent(project);
 		IVirtualReference[] refComponents = null;
 		if (!comp.isBinary())
 			refComponents = ((J2EEModuleVirtualComponent)comp).getNonManifestReferences();
 		else
 			refComponents = comp.getReferences();
 		// Check the deployed path to make sure it has a lib parent folder and matchs the web.xml
 		// base path
 		for (int i = 0; i < refComponents.length; i++) {
 			if (refComponents[i].getRuntimePath().equals(WEBLIB))
 				result.add(refComponents[i]);
 		}
 
 		return (IVirtualReference[]) result.toArray(new IVirtualReference[result.size()]);
 	}
 	
 	private void initializeSelectionForWLPs() {
 		classPathWLPSelection = new ClassPathSelection();
 		classPathWLPSelection.setFilterLevel(ClassPathSelection.FILTER_NONE);
 		//this is the set of all projects that are already mapped as web library projects
 		HashSet existingWebLibProjects = new HashSet();
 		List otherExistingWebLibRefs = new ArrayList();
 		IVirtualReference [] libModules = getLibModules(component.getProject());
 		for(int i=0;i<libModules.length; i++){
 			IVirtualComponent comp = libModules[i].getReferencedComponent();
 			if(comp != null && comp.exists()){
 				if(comp.isBinary()){
 					otherExistingWebLibRefs.add(libModules[i]);
 				} else {
 					IProject p = comp.getProject();
 					if(p != null && p.exists()){
 						existingWebLibProjects.add(p);
 					}
 				}
 			}
 		}
 		
 		//this is the list of all projects that should show up in the list
 		HashSet possibleWebLibs = new HashSet();
 		try{
 			List javaProjects = J2EEProjectUtilities.getAllJavaNonFlexProjects();
 			possibleWebLibs.addAll(javaProjects);
 		} catch(CoreException e){
 			Logger.getLogger().logError(e);
 		}
 		IProject[] utilityProjects = J2EEProjectUtilities.getAllProjectsInWorkspaceOfType(J2EEProjectUtilities.UTILITY);
 		possibleWebLibs.addAll(Arrays.asList(utilityProjects));
 		IProject[] ejbProjects = J2EEProjectUtilities.getAllProjectsInWorkspaceOfType(J2EEProjectUtilities.EJB);
 		possibleWebLibs.addAll(Arrays.asList(ejbProjects));
 		
 		Map pathToComp = new HashMap();
 		
 		//first handle the projects case
 		for(Iterator iterator = possibleWebLibs.iterator(); iterator.hasNext();){
 			IProject possibleWebLib = (IProject)iterator.next();
 			if(possibleWebLib.getName().startsWith(".")){ //$NON-NLS-1$
 				continue;
 			}
 			boolean isExistingWebLib = existingWebLibProjects.contains(possibleWebLib);
 			classPathWLPSelection.createProjectElement(possibleWebLib, isExistingWebLib);
 			// build of map of all unique classpath component contributions from dependent projects
 			if(isExistingWebLib){
 				IVirtualComponent component = ComponentCore.createComponent(possibleWebLib);
 				if(component != null){
 					classPathWLPSelection.buildClasspathComponentDependencyMap(component, pathToComp);
 				}
 			}
 		}
 		//next handle entries for all other web lib refs
 		//e.g. to jars in other projects, or outside the workspace or relative to a var
 		for(Iterator iterator = otherExistingWebLibRefs.iterator(); iterator.hasNext();){
 			IVirtualReference ref = (IVirtualReference)iterator.next();
 			IVirtualComponent referencedComponent = ref.getReferencedComponent();
 			// do not add if this is a classpath entry dependency
 			if (ClasspathDependencyUtil.isClasspathComponentDependency(referencedComponent)) {
 				continue;
 			}
 			try {
 				String unresolvedURI = ModuleURIUtil.getArchiveName(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)));
 				URI archiveURI = URI.createURI(unresolvedURI);
 				ClasspathElement element = classPathWLPSelection.createArchiveElement(URI.createURI(ModuleURIUtil.getHandleString(referencedComponent)), referencedComponent.getName(), archiveURI.lastSegment());
 				classPathWLPSelection.addClasspathElement(element, unresolvedURI);
 			} catch (UnresolveableURIException e) {
 				Logger.getLogger(J2EEPlugin.PLUGIN_ID).logError(e);
 			}
 		}
 		
 		// add ClasspathElements for all dependent project cp dependencies
 		final Iterator it = pathToComp.values().iterator();
 		while (it.hasNext()) {
 			final IVirtualComponent c = (IVirtualComponent) it.next();
 			final URI archiveURI = URI.createURI(ModuleURIUtil.getHandleString(c));
 			String unresolvedURI = null;
 			try {
 				unresolvedURI = ModuleURIUtil.getArchiveName(archiveURI);
 			} catch (UnresolveableURIException e) {
 				Logger.getLogger(J2EEPlugin.PLUGIN_ID).logError(e);
 			}
 			if (unresolvedURI != null) {
 				final ClasspathElement element = classPathWLPSelection.createClasspathArchiveElement(c.getProject(), archiveURI, unresolvedURI);
 				classPathWLPSelection.addClasspathElement(element, unresolvedURI);
 			}
 		}
 		
 		// Add elements for raw classpath entries (either already tagged or potentially taggable) 
 		try {
			classPathWLPSelection.createClasspathEntryElements(component, WEBLIB, WEBINF_CLASSES);
 		} catch (CoreException ce) {
 			Logger.getLogger(J2EEPlugin.PLUGIN_ID).logError(ce);
 		}
 	}
 
 	public boolean isWLPModel() {
 		return isWLPModel;
 	}
 
 	/**
 	 * @deprecated do not use this method
 	 * @param isWLPModel
 	 */
 	public void setWLPModel(boolean isWLPModel) {
 		this.isWLPModel = isWLPModel;
 	}
 	
 }
