 /*******************************************************************************
  * Copyright (c) 2004, 2005 Sybase, Inc. and others.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sybase, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.jst.jsf.facesconfig.ui.pageflow.util;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowPage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.Pageflow;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowFactory;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowNode;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.PageflowPackage;
 import org.eclipse.jst.jsf.facesconfig.ui.pageflow.model.impl.PageflowPackageImpl;
 
 /**
  * Drives the model. Acts as the model entry point, including create, load, save
  * operations.
  * 
  * @author Xiao-guang Zhang
  */
 public class PageflowModelManager {
 	/**
 	 * In EMF, a resource provides the way to have access to the model content.
 	 */
 	private Resource resourcePageflow = null;
 
 	/**
 	 * the path of current pageflow model resource provides the way to have
 	 * access to the model content.
 	 */
 	private IPath pathPageflow = null;
 
 	/**
 	 * Contains the factory associated with the model.
 	 */
 	private static PageflowFactory pageflowFactory = null;
 
 	/**
 	 * Gives access to the top level pageflow contained in the resource.
 	 */
 	private Pageflow pageflow = null;
 
 	/**
 	 * resource set
 	 */
 	private ResourceSet resourceSet = null;
 
 	/** unicode encoding UTF-8 support */
 	private static HashMap defaultSaveOptions = new HashMap();
 
 	static {
 		defaultSaveOptions.put(XMLResource.OPTION_ENCODING, "UTF-8"); //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the resource containing the pageflow. Uses lazy initialization.
 	 * 
 	 * @param path -
 	 *            pageflow file name
 	 * @return
 	 */
 	public Resource getResource(IPath path) {
 		if (resourcePageflow == null) {
 			pathPageflow = path;
 			ResourceSet resSet = getResourceSet();
			resourcePageflow = resSet.getResource(URI.createFileURI(path
 					.toString()), true);
 		}
 
 		return resourcePageflow;
 	}
 
 	/**
 	 * Gets the top level pageflow model.
 	 * 
 	 * @return
 	 */
 	public Pageflow getModel() {
 		if (null == pageflow) {
 			EList l = resourcePageflow.getContents();
 			Iterator i = l.iterator();
 			while (i.hasNext()) {
 				Object o = i.next();
 				if (o instanceof Pageflow) {
 					pageflow = (Pageflow) o;
 				}
 			}
 		}
 		return pageflow;
 	}
 
 	/**
 	 * Creates a resource to contain the network. The resource file does not
 	 * exist yet.
 	 * 
 	 * @param path
 	 * @return
 	 */
 	private Resource createResource(IPath path) {
 		if (resourcePageflow == null) {
 			pathPageflow = path;
 			ResourceSet resSet = getResourceSet();
			resourcePageflow = resSet.createResource(URI.createFileURI(path
 					.toString()));
 		}
 		return resourcePageflow;
 	}
 
 	/**
 	 * Returns the resource set.
 	 * 
 	 * @param
 	 * @return
 	 */
 	private ResourceSet getResourceSet() {
 		if (null == resourceSet) {
 			// Initialize the pageflow package, this line can not be removed.
 			PageflowPackageImpl.init();
 			// Register the XML resource factory for the .pageflow extension
 			Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
 			Map regMap = reg.getExtensionToFactoryMap();
 			// m.put("pageflow", new XMIResourceFactoryImpl());
 			regMap.put("pageflow", new PageflowResourceFactoryImpl());
 
 			resourceSet = new ResourceSetImpl();
 		}
 
 		return resourceSet;
 	}
 
 	/**
 	 * Returns the factory associated with the model. Object creation are made
 	 * through that factory.
 	 * 
 	 * @return - pageflow factory
 	 */
 	static public PageflowFactory getFactory() {
 		if (pageflowFactory == null) {
 			// Access the factory (needed to create instances)
 			Map registry = EPackage.Registry.INSTANCE;
 			String pageflowURI = PageflowPackage.eNS_URI;
 			PageflowPackage pageflowPackage = (PageflowPackage) registry
 					.get(pageflowURI);
 			pageflowFactory = pageflowPackage.getPageflowFactory();
 		}
 		return pageflowFactory;
 	}
 
 	/**
 	 * Creates a new pageflow model with begin and end nodes.
 	 * 
 	 * @param path -
 	 *            the new pageflow file name
 	 * @return - new pageflow model
 	 */
 	public Pageflow createPageflow(IPath path) {
 		createResource(path);
 		// Create a new pageflow model
 		Map registry = EPackage.Registry.INSTANCE;
 		String pageflowURI = PageflowPackage.eNS_URI;
 		PageflowPackage nPackage = (PageflowPackage) registry.get(pageflowURI);
 		PageflowFactory nFactory = nPackage.getPageflowFactory();
 		pageflow = nFactory.createPageflow();
 
 		resourcePageflow.getContents().add(pageflow);
 		return pageflow;
 	}
 
 	/**
 	 * Loads the content of the model from the file.
 	 * 
 	 * @param path
 	 */
 	public void load(IPath path) throws IOException {
 		getResource(path);
 	}
 
 	/**
 	 * reloads the content of the model from the file.
 	 * 
 	 * @param path
 	 */
 	public void reload(IPath path) throws IOException {
 		getResource(path).unload();
 		load(path);
 	}
 
 	/**
 	 * Saves the content of the model to the file.
 	 * 
 	 * @param path
 	 */
 	public void save(final IPath path) throws IOException {
 		if (!pathPageflow.toString().equalsIgnoreCase(path.toString())) {
 			pathPageflow = path;
 			URI fileURI = URI.createPlatformResourceURI(path.toString());
 			resourcePageflow.setURI(fileURI);
 		}
 		resourcePageflow.save(defaultSaveOptions);
 	}
 
 	/**
 	 * get the file path of current pageflow resource
 	 * 
 	 * @return - the file path
 	 */
 	public IPath getPath() {
 		return pathPageflow;
 	}
 
 	/**
 	 * found page node according the web path.
 	 * 
 	 * @param webPath
 	 * @return
 	 */
 	public PageflowPage foundPage(String webPath) {
 		PageflowPage page = null;
 
 		if (getModel() != null) {
 			Iterator iterNodes = getModel().getNodes().iterator();
 			while (iterNodes.hasNext()) {
 				PageflowNode node = (PageflowNode) iterNodes.next();
 				if (node instanceof PageflowPage) {
 					if (((PageflowPage) node).getPath().equalsIgnoreCase(webPath)) {
 						page = (PageflowPage) node;
 						break;
 					}
 				}
 			}
 		}
 
 		return page;
 	}
 
 	/**
 	 * 
 	 * Build a path for the resource in the .metadata directory given the path
 	 * of the model resource. For example, given a model resource path of
 	 * \test\folder\filename.ext the resulting Pageflow path name will be
 	 * \test\.metadata\folder\filename.pageflow
 	 * 
 	 * @param pathFacesConfig -
 	 *            faces-config file path.
 	 * 
 	 * @return
 	 */
 	public static IPath makePageflowPath(IPath pathFacesConfig) {
 		IPath pageflowPath;
 		String[] segs = pathFacesConfig.removeFileExtension().segments();
 		pageflowPath = new Path(segs[0]).makeAbsolute();
 		pageflowPath = pageflowPath.append(".metadata");
 		for (int i = 1; i < segs.length; i++) {
 			pageflowPath = pageflowPath.append(segs[i]);
 		}
 		// pageflowPath.removeFileExtension();
 		pageflowPath = pageflowPath.addFileExtension("pageflow");
 		return pageflowPath;
 	}
 }
