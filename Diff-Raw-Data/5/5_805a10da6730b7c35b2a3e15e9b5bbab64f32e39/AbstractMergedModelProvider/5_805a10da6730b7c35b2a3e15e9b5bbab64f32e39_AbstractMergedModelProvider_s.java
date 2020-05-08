 /***********************************************************************
  * Copyright (c) 2008 by SAP AG, Walldorf. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     SAP AG - initial API and implementation
  ***********************************************************************/
 package org.eclipse.jst.jee.model.internal.common;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.IModelProviderEvent;
 import org.eclipse.jst.j2ee.model.IModelProviderListener;
 import org.eclipse.jst.jee.JEEPlugin;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 
 /**
  * A base class for model providers providing a merged view between two
  * different model providers. The instance will be called "mergedModelProvider"
  * where the two composed providers will be called "internalProviders"
  * 
  * This class introduces the notation of a disposed state. {@link #dispose()} is
  * used to dispose the model provider. {@link #isDisposed()} is used to get the
  * state of the provider. If the method {@link #getModelObject()} is called for
  * a model provider in a disposed state, the provider should try to move to a
  * non disposed state and return a correct model object.
  * {@link #getModelObject()} is loading the model. Specific implementations may
  * throw exceptions so calling {@link #getModelObject()} on a disposed provider
  * does not guarantee that calling {@link #isDisposed()} after that will return
  * <code>false</code>.
  * 
  * <p>
  * Subclasses may enable/disable notifications from internalProviders with the
  * methods {@link #enableInternalNotifications()} and
  * {@link #disableInternalNotifications()}.
  * </p>
  * 
  * <p>
  * internalProviders are loaded with {@link #loadDeploymentDescriptorModel()}
  * and {@link #loadAnnotationModel(Object)}. This methods should be overridden to
  * provide specific model providers. Loading the model providers is wrapped in a
  * workspace runnable to assure proper access to the workspace.
  * </p>
  * 
  * <p>
  * The mergedModelProvider is a listener to the internalProviders. After
  * disposing the instance of a mergedModelProvider it should no longer accept
  * notifications from the internalProviders. It should also properly "dispose"
  * the internalProviders if needed.
  * </p>
  * 
  * @author Kiril Mitov k.mitov@sap.com
  * 
  */
 public abstract class AbstractMergedModelProvider<T> implements IModelProvider {
 
 	protected IModelProvider ddProvider;
 
 	protected IModelProvider annotationModelProvider;
 
 	private class AnnotationModelListener implements IModelProviderListener {
 		public void modelsChanged(IModelProviderEvent event) {
 			if (disposeIfNeeded(event))
 				return;
 			AbstractMergedModelProvider.this.annotationModelChanged(event);
 		}
 	}
 
 	private class XmlModelListener implements IModelProviderListener {
 		public void modelsChanged(IModelProviderEvent event) {
 			if (disposeIfNeeded(event))
 				return;
 			AbstractMergedModelProvider.this.xmlModelChanged(event);
 		}
 	}
 
 	/**
 	 * @param event
 	 * @return true if the model provider is disposed.
 	 */
 	private boolean disposeIfNeeded(IModelProviderEvent event) {
 		if (isDisposed() || mergedModel == null)
 			return true;
 		if (shouldDispose(event)) {
 			dispose();
 			notifyListeners(event);
 			return true;
 		}
 		return false;
 	}
 
 	private Collection<IModelProviderListener> listeners;
 
 	protected IProject project;
 
 	private AnnotationModelListener annotationModelListener;
 	private XmlModelListener xmlModelListener;
 
 	protected T mergedModel;
 
 	protected long cache_last_change;
 
 	public AbstractMergedModelProvider(IProject project) {
 		this.project = project;
 	}
 
 	public void addListener(IModelProviderListener listener) {
 		getListeners().add(listener);
 	}
 
 	/**
 	 * Returns the model merged from annotation and xml model. If the project is
 	 * closed or does not exist the returns <code>null</code>
 	 * 
 	 * @see org.eclipse.jst.j2ee.model.IModelProvider#getModelObject()
 	 */
 	public Object getModelObject() {
 		return getMergedModel();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.model.IModelProvider#modify(java.lang.Runnable,
 	 * org.eclipse.core.runtime.IPath)
 	 */
 	public void modify(Runnable runnable, IPath modelPath) {
 	}
 
 	public void removeListener(IModelProviderListener listener) {
 		getListeners().remove(listener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jst.j2ee.model.IModelProvider#validateEdit(org.eclipse.core
 	 * .runtime.IPath, java.lang.Object)
 	 */
 	public IStatus validateEdit(IPath modelPath, Object context) {
 		if (ddProvider == null)
 			getModelObject();
 		return ddProvider.validateEdit(modelPath, context);
 	}
 
 	/**
 	 * Called when the annotationModel has changed. See also
 	 * {@link #enableInternalNotifications()} and
 	 * {@link #disableInternalNotifications()}
 	 * 
 	 * @param event
 	 */
 	protected abstract void annotationModelChanged(IModelProviderEvent event);
 
 	/**
 	 * Called when the xmlModel has changed. See also
 	 * {@link #enableInternalNotifications()} and
 	 * {@link #disableInternalNotifications()}
 	 * 
 	 * @param event
 	 */
 	protected abstract void xmlModelChanged(IModelProviderEvent event);
 
 	/**
 	 * Return a merged view of the two passed models.
 	 * 
 	 * @param ddModel
 	 * @param annotationsModel
 	 * @return
 	 */
 	protected abstract T merge(T ddModel, T annotationsModel);
 
 	/**
 	 * Load the annotation model in the context of the ddModel.
 	 * 
 	 * @param ddModel
 	 * @return
 	 * @throws CoreException
 	 */
 	protected abstract IModelProvider loadAnnotationModel(T ddModel) throws CoreException;
 
 	/**
 	 * @return
 	 * @throws CoreException
 	 */
 	protected abstract IModelProvider loadDeploymentDescriptorModel() throws CoreException;
 
 	public Collection<IModelProviderListener> getListeners() {
 		if (listeners == null)
 			listeners = new ArrayList<IModelProviderListener>();
 		return listeners;
 	}
 
 	protected T getMergedModel() {
 		try {
 			if (mergedModel == null || hasToReloadModel()){
 				mergedModel = loadModel();
 			}
 		} catch (CoreException e) {
 			JEEPlugin.getDefault().getLog().log(e.getStatus());
 			return null;
 		}
 		return mergedModel;
 	}
 
 	private boolean hasToReloadModel() {
 		long lastModificationTimeOfDDFile = getLastModificationTimeOfDDFile();
 		return lastModificationTimeOfDDFile == -1 ? false : lastModificationTimeOfDDFile != cache_last_change;
 	}
 
 	private long getLastModificationTimeOfDDFile() {
 		if (ddProvider == null || ((EObject)ddProvider.getModelObject()).eResource() == null){
 			return -1;
 		}
 		return WorkbenchResourceHelper.getFile(((EObject)ddProvider.getModelObject()).eResource()).getLocalTimeStamp();
 	}
 
 	/**
 	 * @return a merged view of the models from the internalProviders. This may
 	 *         include loading the internalProviders.
 	 * @throws CoreException
 	 */
 	protected T loadModel() throws CoreException {
 		if (!project.isAccessible())
 			throw new IllegalStateException("The project <" + project + "> is not accessible."); //$NON-NLS-1$//$NON-NLS-2$
 		project.getWorkspace().run(new LoadModelsWorkspaceRunnable(), project, IWorkspace.AVOID_UPDATE,
 				new NullProgressMonitor());
 		return mergedModel;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void loadProviders() throws CoreException {
 		if (ddProvider == null || hasToReloadModel()) {
 			if (hasToReloadModel())
 				((EObject)ddProvider.getModelObject()).eResource().unload();
 			ddProvider = loadDeploymentDescriptorModel();
 		}
 		if (ddProvider == null || ddProvider.getModelObject() == null)
 			return;
 		if (annotationModelProvider == null)
 			annotationModelProvider = loadAnnotationModel((T) ddProvider.getModelObject());
 		if (annotationModelProvider == null || annotationModelProvider.getModelObject() == null)
 			return;
 		T ddModel = (T) ddProvider.getModelObject();
 		T annotationModel = (T) annotationModelProvider.getModelObject();
 		mergedModel = createNewModelInstance();
 		initMergedModelResource((EObject) ddModel);
 
 		enableInternalNotifications();
 		merge(ddModel, annotationModel);
 	}
 
 	private class LoadModelsWorkspaceRunnable implements IWorkspaceRunnable {
 		public void run(IProgressMonitor monitor) throws CoreException {
 			loadProviders();
 		}
 	}
 
 	/**
 	 * Creates a new instance of the model that will be used for mergedModel
 	 * 
 	 * @return
 	 */
 	protected abstract T createNewModelInstance();
 
 	protected void initMergedModelResource(EObject ddModel) {
 		Resource resourceDD = ddModel.eResource();
 		if (ddProvider != null){
 			cache_last_change = getLastModificationTimeOfDDFile();	
 		}
 		
 		Resource resourceMM = ((EObject) mergedModel).eResource();
 		if (resourceDD != null && resourceMM == null) {
 			ResourceImpl resRes = new ResourceImpl(resourceDD.getURI());
 			resRes.getContents().add((EObject) mergedModel);
 		}
 
 	}
 
 	/**
 	 * The method is used for enabling notifications from the internalProviders.
 	 * This will add the appropriate listener to the internalProviders so that
 	 * {@link #annotationModelChanged(IModelProviderEvent)} and
 	 * {@link #xmlModelChanged(IModelProviderEvent)} are called when needed.
 	 */
 	protected final void enableInternalNotifications() {
 		xmlModelListener = new XmlModelListener();
 		ddProvider.addListener(xmlModelListener);
 		annotationModelListener = new AnnotationModelListener();
 		annotationModelProvider.addListener(annotationModelListener);
 	}
 
 	/**
 	 * Disable notifications from internalProviders. See also
 	 * {@link #enableInternalNotifications()}
 	 */
 	protected final void disableInternalNotifications() {
 		ddProvider.removeListener(xmlModelListener);
 		annotationModelProvider.removeListener(annotationModelListener);
 	}
 
 	protected void notifyListeners(final IModelProviderEvent event) {
 		event.setModel(this);
 		event.setProject(project);
 		final Collection<IModelProviderListener> listeners = getListeners();
 		IModelProviderListener[] backup = listeners.toArray(new IModelProviderListener[listeners.size()]);
 		for (final IModelProviderListener listener : backup) {
 			SafeRunner.run(new ISafeRunnable() {
 				public void handleException(Throwable exception) {
 				}
 
 				public void run() throws Exception {
 					listener.modelsChanged(event);
 				}
 			});
 		}
 		backup = null;
 	}
 
 	protected boolean shouldDispose(IModelProviderEvent event) {
 		return (event.getEventCode() == IModelProviderEvent.UNLOADED_RESOURCE);
 	}
 
 	/**
 	 * Returns the dispose state of this model provider. When the provider is
 	 * disposed it can not be used until getModelObject is called again.
 	 * 
 	 * Subclasses may override this method.
 	 * 
 	 * @return true if the model provider is to be treated as disposed
 	 */
 	public boolean isDisposed() {
 		return (ddProvider == null && annotationModelProvider == null);
 	}
 
 	/**
 	 * Dispose the model provider. If the provider is already disposed the
 	 * method has no effect.
 	 * 
 	 * Subclasses may override this method.
 	 * 
 	 * @see #isDisposed()
 	 */
 	public void dispose() {
 		if (isDisposed())
 			return;
 		disableInternalNotifications();
 		ddProvider = null;
 		annotationModelProvider = null;
 		mergedModel = null;
 	}
 
 }
