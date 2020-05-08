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
 
 package org.eclipse.jst.j2ee.componentcore;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jst.common.jdt.internal.integration.JavaArtifactEditModel;
 import org.eclipse.jst.common.jdt.internal.integration.WorkingCopyManager;
 import org.eclipse.jst.common.jdt.internal.integration.WorkingCopyProvider;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProviderFactory;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * <p>
  * EnterpriseArtifactEdit obtains a type-specific J2EE metamodel from the managed
  * {@see org.eclipse.wst.common.modulecore.ArtifactEditModel}. The underlying EditModel maintains
  * {@see org.eclipse.emf.ecore.resource.Resource}s, such as the J2EE deployment descriptor
  * resource. The defined methods extract data or manipulate the contents of the underlying resource.
  * </p>
  * 
  * <p>
  * This class is an abstract class, and clients are intended to subclass and own their
  * implementation.
  * </p>
  */
 public abstract class EnterpriseArtifactEdit extends ArtifactEdit implements WorkingCopyProvider, IModelProvider, IModelProviderFactory {
 
 	private ArtifactEdit writableEdit =  null; 
 	
 	/**
 	 * 
 	 */
 	protected EnterpriseArtifactEdit() {
 		super();
 	}
 
 	public EnterpriseArtifactEdit(IVirtualComponent aModule){
 		super(aModule);
 	}
 	
 	
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public EnterpriseArtifactEdit(IProject aProject, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly);
 	}
 	
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	protected EnterpriseArtifactEdit(IProject aProject, boolean toAccessAsReadOnly, boolean forCreate, String projectType) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly, forCreate, projectType);
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * <p>
 	 * Clients that use this constructor are required to release their access of the EditModel when
 	 * finished. Calling {@see ArtifactEdit#dispose()}will not touch the supplied EditModel.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 *            A valid, properly-accessed EditModel
 	 */
 	public EnterpriseArtifactEdit(ArtifactEditModel model) {
 		super(model);
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see WorkbenchComponent}.
 	 * </p>
 	 * <p>
 	 * Instances of EnterpriseArtifactEdit that are returned through this method must be
 	 * {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param aNature
 	 *            A non-null {@see ModuleCoreNature}&nbsp;for an accessible project
 	 * @param aModule
 	 *            A non-null {@see WorkbenchComponent}&nbsp;pointing to a module from the given
 	 *            {@see ModuleCoreNature}
 	 */
 
 	protected EnterpriseArtifactEdit(ModuleCoreNature aNature, IVirtualComponent aModule, boolean toAccessAsReadOnly) {
 		super(aNature, aModule, toAccessAsReadOnly);
 	}
 
 	/**
 	 * <p>
 	 * Retrieves J2EE version information from deployment descriptor resource.
 	 * </p>
 	 * 
 	 * @return An the J2EE Specification version of the underlying {@see WorkbenchComponent}
 	 * 
 	 */
 	public abstract int getJ2EEVersion();
 
 	/**
 	 * <p>
 	 * Retrieves a deployment descriptor resource from {@see ArtifactEditModel}using a defined URI.
 	 * </p>
 	 * 
 	 * @return The correct deployment descriptor resource for the underlying
 	 *         {@see WorkbenchComponent}
 	 * 
 	 */
 	public abstract Resource getDeploymentDescriptorResource();
 
 	/**
 	 * <p>
 	 * Obtains the root object from a deployment descriptor resource, the root object contains all
 	 * other resource defined objects. Examples of a deployment descriptor root include:
 	 * {@see org.eclipse.jst.j2ee.webapplication.WebApp},
 	 * {@see org.eclipse.jst.j2ee.application.Application}, and
 	 * {@see org.eclipse.jst.j2ee.ejb.EJBJar}
 	 * </p>
 	 * <p>
 	 * Subclasses may extend this method to perform their own deployment descriptor creataion/
 	 * retrieval.
 	 * </p>
 	 * 
 	 * @return An EMF metamodel object representing the J2EE deployment descriptor
 	 * 
 	 */
 
 	public EObject getDeploymentDescriptorRoot() {
 		Resource res = getDeploymentDescriptorResource();
 		return (EObject) res.getContents().get(0);
 	}
 
 	/**
 	 * Returns a working copy managet
 	 * 
 	 * @return
 	 */
 
 	public WorkingCopyManager getWorkingCopyManager() {
 		if(isBinary()){
 			throwAttemptedBinaryEditModelAccess();
 		}
 		return ((JavaArtifactEditModel)getArtifactEditModel()).getWorkingCopyManager();
 	}
 	
 	/**
 	 * Returns the working copy remembered for the compilation unit.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if there is no
 	 *         remembered working copy for this compilation unit
 	 */
 	public ICompilationUnit getWorkingCopy(ICompilationUnit cu, boolean forNewCU) throws org.eclipse.core.runtime.CoreException {
 		if (isReadOnly())
 			return null;
 		return getWorkingCopyManager().getWorkingCopy(cu, forNewCU);
 	}
 	protected boolean validProjectVersion(IProject project) {
 		
 		// Return true if project is being created
 		if (!project.exists()) return true;
 		IFacetedProject facetedProject = null;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (facetedProject == null)
 			// Return true if project facet is being created
 			return true;
		//Only return true for legacy projects
		return !J2EEProjectUtilities.isJEEProject(project);
 	}
 
 	/**
 	 * Returns the working copy remembered for the compilation unit encoded in the given editor
 	 * input. Does not connect the edit model to the working copy.
 	 * 
 	 * @param input
 	 *            ICompilationUnit
 	 * @return the working copy of the compilation unit, or <code>null</code> if the input does
 	 *         not encode an editor input, or if there is no remembered working copy for this
 	 *         compilation unit
 	 */
 	public ICompilationUnit getExistingWorkingCopy(ICompilationUnit cu) throws org.eclipse.core.runtime.CoreException {
 		return getWorkingCopyManager().getExistingWorkingCopy(cu);
 	}
 
 	public URI getModuleLocation(String moduleName) {
 		if (getProject()!=null)
 			return ModuleURIUtil.fullyQualifyURI(getProject());
 		return null;
 	}
 
 	/**
 	 * This will delete
 	 * 
 	 * @cu from the workbench and fix the internal references for this working copy manager.
 	 */
 	public void delete(org.eclipse.jdt.core.ICompilationUnit cu, org.eclipse.core.runtime.IProgressMonitor monitor) {
 		getWorkingCopyManager().delete(cu, monitor);
 	}
 
 	/**
 	 * <p>
 	 * Create an deployment descriptor resource if one does not get and return it. Subclasses should
 	 * overwrite this method to create their own type of deployment descriptor
 	 * </p>
 	 * 
 	 * @return an EObject
 	 */
 
 	public abstract EObject createModelRoot();
 
 	/**
 	 * <p>
 	 * Create an deployment descriptor resource if one does not get and return it. Subclasses should
 	 * overwrite this method to create their own type of deployment descriptor
 	 * </p>
 	 * 
 	 * @param int
 	 *            version of the component
 	 * @return an EObject
 	 */
 
 	public abstract EObject createModelRoot(int version);
 	
 	public Archive asArchive(boolean includeSource) throws OpenFailureException{
 		return asArchive(includeSource, true);
 	}
 	
 	public Archive asArchive(boolean includeSource, boolean includeClasspathComponents) throws OpenFailureException {
 		return null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.model.IModelProvider#getModelObject()
 	 */
 	public Object getModelObject() {
 		if ( getWritableEdit() != null)
 			return getWritableEdit().getContentModelRoot();
 		return getContentModelRoot();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.model.IModelProvider#getModelObject(org.eclipse.core.runtime.IPath)
 	 */
 	public Object getModelObject(IPath modelPath) {
 		if ( getWritableEdit() != null) {
 			Resource res = ((ArtifactEditModel)getWritableEdit().getAdapter(ArtifactEditModel.ADAPTER_TYPE)).getResource(URI.createURI(modelPath.toString()));
 			if (res != null && !res.getContents().isEmpty())
 				return res.getContents().get(0);
 			else return null;
 		}
 			return getContentModelRoot();
 	}
 
 	public IModelProvider create(IProject project) {
 		return (IModelProvider)getArtifactEditForRead(project);
 	}
 
 	public IModelProvider create(IVirtualComponent component) {
 		return (IModelProvider)getArtifactEditForRead(component);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jst.j2ee.model.IModelProvider#modify(java.lang.Runnable, org.eclipse.core.runtime.IPath)
 	 */
 	public void modify(Runnable runnable, IPath modelPath) {
 		//About to modify and save this model
 		
 		// access model  (write count)
 		// cache writable model (setWriteableEdit())
 		// run runnable
 		// save model
 		// release access count
 		// null Writable Edit
 		
 	}
 
 	public IStatus validateEdit(IPath modelPath, Object context) {
 		// ArtifactEdit will validate all files it manages, and uses its own context mechanism
 		return validateEdit();
 	}
 
 	/**
 	 * @param writableEdit the writableEdit to set
 	 */
 	protected void setWritableEdit(ArtifactEdit writableEdit) {
 		this.writableEdit = writableEdit;
 	}
 
 	/**
 	 * @return the writableEdit
 	 */
 	protected ArtifactEdit getWritableEdit() {
 		return writableEdit;
 	}
 }
