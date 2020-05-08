 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.jee.model.internal;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.content.IContentDescription;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.xmi.impl.XMLResourceImpl;
 import org.eclipse.jem.util.emf.workbench.ProjectResourceSet;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.impl.WTPResourceFactoryRegistry;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 
 public class JEE5ModelProvider implements IModelProvider{
 
 	protected XMLResourceImpl writableResource;
 	protected IProject proj;
 	protected IPath defaultResourcePath;
 	private static boolean resourceChangeListenerEnabled = false;
 	private static HashMap<IProject, HashSet<IPath>> modelResources = new HashMap<IProject, HashSet<IPath>>();
 
 	public JEE5ModelProvider() {
 		super();
 	}
 
 	protected ProjectResourceSet getResourceSet(IProject proj2) {
 		return (ProjectResourceSet)WorkbenchResourceHelperBase.getResourceSet(proj);
 	}
 
 	public XMLResourceImpl getWritableResource() {
 		return writableResource;
 	}
 
 	public void setWritableResource(XMLResourceImpl writableResource) {
 		this.writableResource = writableResource;
 	}
 
 	protected XMLResourceImpl getModelResource(IPath modelPath) {
 		if (writableResource != null)
 			return writableResource;
 		if (modelPath == null)
 			modelPath = getDefaultResourcePath();
 		ProjectResourceSet resSet = getResourceSet(proj);
 		IVirtualFolder container = ComponentCore.createComponent(proj).getRootFolder();
 		String modelPathURI = modelPath.toString();
 		URI uri = URI.createURI(container.getProjectRelativePath().toString() + Path.SEPARATOR + modelPathURI);
 		XMLResourceImpl res = null;
 		try {
 			if (proj.getFile(uri.toString()).exists())
 			{
 				res = (XMLResourceImpl) resSet.getResource(uri,true);
 				HashSet<IPath> currentResources = modelResources.get(proj);
 				if (currentResources == null)
 				{
 					currentResources = new HashSet<IPath>();
 				}
 				currentResources.add(new Path(uri.toString()));
 				modelResources.put(proj, currentResources);
 				if (!resourceChangeListenerEnabled)
 				{
 					resourceChangeListenerEnabled = true;
 					ResourcesPlugin.getWorkspace().addResourceChangeListener(new ResourceChangeListener(), IResourceChangeEvent.POST_CHANGE);
 				}
			} else //Create new Empty Resource if no file exists
				return createModelResource(modelPath, resSet, uri);
 		} catch (WrappedException ex) {
 			if (ex.getCause() instanceof FileNotFoundException)
 				return null;
 			else throw ex;
 		}
 		return res;
 	}
 	
 
 	protected XMLResourceImpl createModelResource(IPath modelPath, ProjectResourceSet resourceSet, URI uri) {
 		// Create temp resource if no file exists
 		XMLResourceImpl res =  (XMLResourceImpl)resourceSet.createResource(uri,WTPResourceFactoryRegistry.INSTANCE.getFactory(uri, getContentType(getContentTypeDescriber())));
 		populateRoot(res);
 		//Remove this resource from resource set so its not re-used
 		res.getResourceSet().getResources().remove(res);
 		return res;
 	}
 
 	public void populateRoot(XMLResourceImpl res) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private IContentDescription getContentType(String contentTypeDescriber) {
 		
 			if (contentTypeDescriber != null)
 				return Platform.getContentTypeManager().getContentType(contentTypeDescriber).getDefaultDescription();
 			else
 				return null;
 			
 		}
 
 	public IPath getDefaultResourcePath() {
 		return defaultResourcePath;
 	}
 
 	public void setDefaultResourcePath(IPath defaultResourcePath) {
 		this.defaultResourcePath = defaultResourcePath;
 	}
 
 	public Object getModelObject() {
 		return getModelObject(getDefaultResourcePath());
 	}
 
 	public Object getModelObject(IPath modelPath) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	/**
 	 * Used to optionally define an associated content type for XML file creation
 	 * @return
 	 */
 	protected String getContentTypeDescriber() {
 		
 		return null;
 	}
 	
 
 
 	public IStatus validateEdit(IPath modelPath, Object context) {
 		if (modelPath == null)
 			modelPath = getDefaultResourcePath();
 		IWorkspace work = ResourcesPlugin.getWorkspace();
 		IFile file = WorkbenchResourceHelper.getFile(getModelResource(modelPath));
 		if (file != null) {
 			IFile[] files = { file };
 			if (context == null)
 				context = IWorkspace.VALIDATE_PROMPT;
 			return work.validateEdit(files, context);
 		} else
 			return Status.OK_STATUS;
 	}
 
 	public void modify(Runnable runnable, IPath modelPath) {
 		//About to modify and save this model
 		try {
 			XMLResourceImpl res = getModelResource(modelPath);
 			if (res != null)
 				setWritableResource(res);
 			runnable.run();
 			try {
 				if (res != null)
 					res.save(Collections.EMPTY_MAP);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		} catch (Exception ex) {
 			ex.printStackTrace();
 		} finally {
 			setWritableResource(null);
 		}
 		
 	}
 
 	private class ResourceChangeListener implements IResourceChangeListener {
 		public void resourceChanged(IResourceChangeEvent event) {
 			IResourceDelta delta= event.getDelta();
 			// make sure that there is a delta (since some events don't have one)
 			if (delta != null)
 			{
 				IResourceDelta[] affectedChildren= delta.getAffectedChildren(IResourceDelta.CHANGED | IResourceDelta.REMOVED , IResource.FILE);
 				IResourceDelta projectDelta = null;
 				IResource changedResource = null; 
 				IProject changedProject = null;
 				IPath resourcePath = null;
 
 				for (int i= 0; i < affectedChildren.length; i++) {
 					projectDelta = affectedChildren[i];
 					changedResource = projectDelta.getResource(); 
 					changedProject = changedResource.getProject();
 					HashSet<IPath> currentResources = modelResources.get(changedProject);
 					// only deal with the projects that have resources that have been loaded 
 					if (currentResources != null)
 					{
 						// if this is a project deletion, remove the project from the HashMap.
 						if (changedResource == changedProject && projectDelta.getKind() == IResourceDelta.REMOVED)
 						{
 							modelResources.remove(changedProject);
 							// if modelResources is empty, we should self-destruct
 							if (modelResources.isEmpty())
 							{
 								resourceChangeListenerEnabled = false;
 								ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 							}
 						}
 						else
 						{
 							Iterator<IPath> iter = currentResources.iterator();
 							ArrayList<IPath> toUnload = new ArrayList<IPath>();
 							// check each resource that was loaded from the project to see if it is part of the change
 							while (iter.hasNext())
 							{
 								resourcePath = iter.next();
 								if (projectDelta.findMember(resourcePath) != null)
 								{
 									// limit the list of resources that need to be unloaded to those that have changed
 									toUnload.add(resourcePath);
 								}
 							}
 							if (toUnload.size() > 0)
 							{
 								Resource current = null;
 								ProjectResourceSet resourceSet = getResourceSet(changedProject);
 								URIConverter uriConverter = resourceSet.getURIConverter();
 								HashSet<URI> resourceURIs = new HashSet<URI>();
 								iter = toUnload.iterator();
 								while (iter.hasNext())
 								{
 									// convert all of the resources to URIs - this is a faster match during the compare
 									resourceURIs.add(uriConverter.normalize(URI.createURI(iter.next().toString())));
 								}
 								Iterator<Resource> iter2 = resourceSet.getResources().iterator();
 								while (iter2.hasNext())
 								{
 									current = iter2.next();
 									if (resourceURIs.contains(current.getURI()))
 									{
 										current.unload();
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
