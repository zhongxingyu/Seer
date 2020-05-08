 /*******************************************************************************
  * Copyright (c) 2001, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.internal.beaninfo.adapters;
 /*
  *  $RCSfile: BeaninfoNature.java,v $
 *  $Revision: 1.26 $  $Date: 2004/11/19 21:17:40 $ 
  */
 
 import java.io.*;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.logging.Level;
 
 import javax.xml.parsers.*;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.eclipse.core.resources.*;
 import org.eclipse.core.runtime.*;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.jdt.core.*;
 import org.osgi.framework.Bundle;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 import org.eclipse.jem.internal.beaninfo.core.*;
 import org.eclipse.jem.internal.java.adapters.JavaXMIFactoryImpl;
 import org.eclipse.jem.internal.java.beaninfo.IIntrospectionAdapter;
 import org.eclipse.jem.internal.java.init.JavaInit;
 import org.eclipse.jem.internal.plugin.JavaEMFNature;
 import org.eclipse.jem.internal.proxy.core.*;
 
 import com.ibm.wtp.emf.workbench.ResourceHandler;
 
 /**
  * The beaninfo nature. It is created for a project and holds the
  * necessary info for beaninfo to be performed on a project.
  */
 
 public class BeaninfoNature implements IProjectNature {
 
 	public static final String NATURE_ID = BeaninfoPlugin.PI_BEANINFO_PLUGINID + ".BeanInfoNature"; //$NON-NLS-1$
 	public static final String P_BEANINFO_SEARCH_PATH = ".beaninfoConfig"; //$NON-NLS-1$
 	
 	public static final QualifiedName CONFIG_INFO_SESSION_KEY = new QualifiedName(BeaninfoPlugin.PI_BEANINFO_PLUGINID, "CONFIG_INFO"); //$NON-NLS-1$
 	public static final QualifiedName BEANINFO_CONTRIBUTORS_SESSION_KEY = new QualifiedName(BeaninfoPlugin.PI_BEANINFO_PLUGINID, "BEANINFO_CONTRIBUTORS"); //$NON-NLS-1$
 
 	private ResourceTracker resourceTracker;
 	// This class listens for changes to the beaninfo paths file, and if changed it marks all stale
 	// so the next time anything is needed it will recycle the vm. It will also listen about to close or
 	// about to delete of the project so that it can cleanup.
 	private class ResourceTracker implements IResourceChangeListener{
 		public void resourceChanged(IResourceChangeEvent e) {
 			// About to close or delete the project and it is ours, so we need to cleanup.
 			// Performance: It has been noted that dres.equals(...) can be slow with the number
 			// of visits done. Checking just the last segment (getName()) first before checking
 			// the entire resource provides faster testing. If the last segment is not equal,
 			// then the entire resource could not be equal.
 			IResource eventResource = e.getResource();
 			if (eventResource.getName().equals(getProject().getName()) && eventResource.equals(getProject())) {
 				cleanup(false, true);	// No need to clean up resources (false parm) because in this case Java EMF Model will always be going away.
 				return;
 			}
 			// Note: the BeaninfoModelSynchronizer takes care of both .classpath and .beaninfoconfig changes
 			// in this project and any required projects.
 		}
 	}
 
 	private ProxyFactoryRegistry.IRegistryListener registryListener = new ProxyFactoryRegistry.IRegistryListener() {
 		/**
 		 * @see org.eclipse.jem.internal.proxy.core.ProxyFactoryRegistry.IRegistryListener#registryTerminated(ProxyFactoryRegistry)
 		 */
 		public void registryTerminated(ProxyFactoryRegistry registry) {
 			markAllStale();
 		};
 	};
 
 	/**
 	 * Get the runtime nature for the project, create it if necessary.
 	 */
 	public static BeaninfoNature getRuntime(IProject project) throws CoreException {
		JavaEMFNature.createRuntime(project);	// Must force JAVAEMFNature creation first before we try to get ours. There is a chicken/egg problem if we let our nature try to get JavaEMFNature during setProject.
 		if (project.hasNature(NATURE_ID))
 			return (BeaninfoNature) project.getNature(NATURE_ID);
 		else
 			return createRuntime(project);
 	}
 	
 	/**
 	 * Return whether this project has a BeanInfo runtime turned on.
 	 * 
 	 * @param project
 	 * @return <code>true</code> if it has the a BeanInfo runtime.
 	 * @throws CoreException
 	 * 
 	 * @since 1.0.0
 	 */
 	public static boolean hasRuntime(IProject project) throws CoreException {
 		return project.hasNature(NATURE_ID);
 	}
 
 	/**
 	 * Test if this is a valid project for a Beaninfo Nature. It must be
 	 * a JavaProject.
 	 */
 	public static boolean isValidProject(IProject project) {
 		try {
 			return project.hasNature(JavaCore.NATURE_ID);
 		} catch (CoreException e) {
 			return false;
 		}
 	}
 
 	/**
 	 * Create the runtime.
 	 */
 	private static BeaninfoNature createRuntime(IProject project) throws CoreException {
 		if (!isValidProject(project))
 			throw new CoreException(
 				new Status(
 					IStatus.ERROR,
 					BeaninfoPlugin.PI_BEANINFO_PLUGINID,
 					0,
 					MessageFormat.format(
 						BeanInfoAdapterMessages.getString(BeanInfoAdapterMessages.INTROSPECTFAILED),
 						new Object[] { project.getName(), BeanInfoAdapterMessages.getString("BeaninfoNature.InvalidProject")}), //$NON-NLS-1$
 					null));
 
 		addNatureToProject(project, NATURE_ID);
 		return (BeaninfoNature) project.getNature(NATURE_ID);
 	}
 
 	private static void addNatureToProject(IProject proj, String natureId) throws CoreException {
 		IProjectDescription description = proj.getDescription();
 		String[] prevNatures = description.getNatureIds();
 		String[] newNatures = new String[prevNatures.length + 1];
 		System.arraycopy(prevNatures, 0, newNatures, 0, prevNatures.length);
 		newNatures[prevNatures.length] = natureId;
 		description.setNatureIds(newNatures);
 		proj.setDescription(description, null);
 	}
 
 	private IProject fProject;
 	protected ProxyFactoryRegistry fRegistry;
 	protected ResourceSet javaRSet;
 	protected BeaninfoModelSynchronizer fSynchronizer;
 	protected static BeaninfoJavaReflectionKeyExtension fReflectionKeyExtension;
 
 	/** 
 	 * Configures the project with this nature.
 	 * This is called by <code>IProject.getNature</code> and should not
 	 * be called directly by clients.
 	 * The nature extension id is added to the list of natures on the project by
 	 * <code>IProject.getNature</code>, and need not be added here.
 	 *
 	 * @exception CoreException if this method fails.
 	 */
 	public void configure() throws CoreException {
 	}
 
 	/** 
 	 * Removes this nature from the project, performing any required deconfiguration.
 	 * This is called by <code>IProject.removeNature</code> and should not
 	 * be called directly by clients.
 	 * The nature id is removed from the list of natures on the project by
 	 * <code>IProject.removeNature</code>, and need not be removed here.
 	 *
 	 * @exception CoreException if this method fails. 
 	 */
 	public void deconfigure() throws CoreException {
 		removeSharedProperty(P_BEANINFO_SEARCH_PATH, null);
 		cleanup(true, true);
 	}
 	
 	/**
 	 * Shutdown the nature. Called by BeanInfoPlugin to tell the nature that the plugin is being shutdown.
 	 * It needs to cleanup.
 	 * TODO <package-protected> because only BeanInfoPlugin should call it. (public for now but when we make
 	 * BeanInfoNature an API it will be moved into the same package as BeanInfoPlugin).
 	 * 
 	 * @since 1.0.0
 	 */
 	public void shutdown() {
 		cleanup(true, false);
 	}
 
 
 	/**
 	 * Return a new ResourceSet that is linked correctly to this Beaninfo Nature.
 	 * <p>
 	 * This links up a ResourceSet so that it will work correctly with this nature.
 	 * It makes sure that going through the ResourceSet that any "java:/..."
 	 * classes can be found and it makes sure that any new classes are placed into the
 	 * nature's resource set and not resource set doing the calling.
 	 * <p>
 	 * This should be used any time a resource set is needed that is not the
 	 * project wide resource set associated with beaninfos, but will reference
 	 * Java Model classes or instantiate.
 	 * <p>
 	 * An additional change is made too. The ResourceFactoryRegistry's extensionToResourceFactory map is modified
 	 * to have an "java"->XMIResourceFactory entry added to it if EMF Examples is loaded. EMF Examples add
 	 * the "java" extension and sets it to their own special JavaResourceFactory. 
 	 * If EMF Examples is not loaded, then it falls back to the default "*" mapping, which is to XMIResourceFactory.
 	 * This normally causes problems for many
 	 * customers. If users of this resource set really want the EMF examples entry instead, after they retrieve the
 	 * new resource set they can do this:
 	 * <p>
 	 * <pre><code>
 	 * 	rset = beaninfoNature.newResourceSet();
 	 * 	rset.getResourceFactoryRegistry().getExtensionToFactoryMap().remove("java");
 	 * </code></pre>
 	 * 
 	 * @return a ResourceSet that is specially connected to the JEM java model.
 	 * 
 	 * @since 1.0.0
 	 */
 	public ResourceSet newResourceSet() {
 		SpecialResourceSet rset = new SpecialResourceSet();
 		rset.add(new ResourceHandler() {
 			public EObject getEObjectFailed(ResourceSet originatingResourceSet, URI uri, boolean loadOnDemand) {
 				return null; // We don't have any special things we can do in this case.
 			}
 
 			public Resource getResource(ResourceSet originatingResourceSet, URI uri) {
 				// Always try to get it out of the nature's resource set because it may of been loaded there either as 
 				// the "java:..." type or it could of been an override extra file (such as an override EMF package, for
 				// example jcf has a side package containing the definition of the new attribute type. That file
 				// will also be loaded into this resourceset. So to find it we need to go in here and try.
 				//
 				// However, if not found we won't go and try to load the resource. That could load in the wrong place.
 				// Kludge: Because of a bug (feature :-)) in XMLHandler.getPackageFromURI(), it doesn't use getResource(...,true) and it tries instead
 				// to use uri inputstream to load the package when not found. This bypasses our special create resource and so
 				// packages are not automatically created. So we need to do load on demand here instead if it is a java protocol.
 				// EMF will not be fixing this. It is working as designed.
 				return getResourceSet().getResource(uri, JavaXMIFactoryImpl.SCHEME.equals(uri.scheme()));
 			}
 
 			public Resource createResource(ResourceSet originatingResourceSet, URI uri) {
 				// This is the one. It has got here because it couldn't find a resource already loaded.
 				// If it is a "java:/..." protocol resource, then we want to make sure it is loaded at the BeaninfoNature context
 				// instead of the lower one.
 				if (JavaXMIFactoryImpl.SCHEME.equals(uri.scheme()))
 					return getResourceSet().getResource(uri, true);
 				else
 					return null;
 			}
 		});
 		// [71473] Restore "*.java" to be an XMIResource. If EMF Examples are loaded they overload this and load their special resource for "*.java" which we don't want.
 		// If some user really wants that, they grab the resource resource set and remove our override.
 		if (Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().containsKey("java")) {
 			// Need to add an override to go to XMI instead.
 			rset.getResourceFactoryRegistry().getExtensionToFactoryMap().put("java", new XMIResourceFactoryImpl());
 		}
 		return rset;
 	}
 	/**
 	 * Clean up, this means either the project is being closed, deleted, or it means that
 	 * the nature is being removed from the project. Either way that means to
 	 * terminate the VM and remove what we added to the context if the flag says clear it.
 	 * <p>
 	 * This should be called ONLY when this instance of the nature is no longer needed. It
 	 * will be recreated for any new uses. That is because we will be removing ourselves
 	 * from the list of active natures in the BeanInfoPlugin.
 	 * 
 	 * @param clearResults clear the results such that any JEM model objects have no BeanInfo
 	 * adapters attached to them. This allows BeanInfo to be GC'd without being hung onto.
 	 *  
 	 * @param deregister Deregister from the BeanInfoPlugin. Normally this will always be true, but it
 	 * will be called with false when BeanInfoPlugin is calling back to shutdown.
 	 */
 	protected void cleanup(boolean clearResults, boolean deregister) {
 		if (deregister)
 			BeaninfoPlugin.getPlugin().removeBeanInfoNature(this);
 		getProject().getWorkspace().removeResourceChangeListener(resourceTracker);
 		resourceTracker = null;
 		fSynchronizer.stopSynchronizer(clearResults);
 		Init.cleanup(javaRSet, clearResults);
 		if (fRegistry != null)
 			fRegistry.terminateRegistry();
 
 		javaRSet = null;
 		fRegistry = null;
 		fProject = null;
 		fSynchronizer = null;
 	}
 
 	/** 
 	 * Returns the project to which this project nature applies.
 	 *
 	 * @return the project handle
 	 */
 	public IProject getProject() {
 		return fProject;
 	}
 
 	/**
 	 * Sets the project to which this nature applies.
 	 * Used when instantiating this project nature runtime.
 	 * This is called by <code>IProject.addNature</code>
 	 * and should not be called directly by clients.
 	 *
 	 * @param project the project to which this nature applies
 	 */
 	public void setProject(IProject project) {
 		fProject = project;
 		BeaninfoPlugin.getPlugin().addBeanInfoNature(this);
 
 		try {
 			// The nature has been started for this project, need to setup the introspection process now.
 			JavaEMFNature javaNature = JavaEMFNature.createRuntime(fProject);
 			JavaInit.init();
 			if (fReflectionKeyExtension == null) {
 				// Register the reflection key extension.
 				fReflectionKeyExtension = new BeaninfoJavaReflectionKeyExtension();
 				JavaXMIFactoryImpl.INSTANCE.registerReflectionKeyExtension(fReflectionKeyExtension);
 			}
 
 			javaRSet = javaNature.getResourceSet();
 			Init.initialize(javaRSet, new IBeaninfoSupplier() {
 				public ProxyFactoryRegistry getRegistry() {
 					return BeaninfoNature.this.getRegistry();
 				}
 
 				public boolean isRegistryCreated() {
 					return BeaninfoNature.this.isRegistryCreated();
 				}
 				
 				public void closeRegistry() {
 					BeaninfoNature.this.closeRegistry();
 				}
 				
 				public IProject getProject() {
 					return BeaninfoNature.this.getProject();
 				}
 			});
 			fSynchronizer =
 				new BeaninfoModelSynchronizer(
 					(BeaninfoAdapterFactory) EcoreUtil.getAdapterFactory(javaRSet.getAdapterFactories(), IIntrospectionAdapter.ADAPTER_KEY),
 					JavaCore.create(javaNature.getProject()));
 			resourceTracker = new ResourceTracker();
 			project.getWorkspace().addResourceChangeListener(resourceTracker, IResourceChangeEvent.PRE_CLOSE | IResourceChangeEvent.PRE_DELETE);
 		} catch (CoreException e) {
 			BeaninfoPlugin.getPlugin().getLogger().log(e.getStatus());
 		}
 	}
 
 	/**
 	 * Close the registry. It needs to be recycled because a class has changed
 	 * and now the new class needs to be accessed.
 	 */
 	protected void closeRegistry() {
 		ProxyFactoryRegistry reg = null;
 		synchronized (this) {
 			reg = fRegistry;
 			fRegistry = null;
 			try {
 				// Wipe out the Session properties so that they are recomputed.
 				getProject().setSessionProperty(CONFIG_INFO_SESSION_KEY, null);
 				getProject().setSessionProperty(BEANINFO_CONTRIBUTORS_SESSION_KEY, null);			
 			} catch (CoreException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e, Level.INFO);
 			}
 		}
 		if (reg != null) {
 			reg.removeRegistryListener(registryListener);
 			reg.terminateRegistry();
 		}
 	}
 
 	/**
 	 * Get registry, creating it if necessary.
 	 * @return the registry.
 	 * 
 	 * @since 1.0.0
 	 */
 	public ProxyFactoryRegistry getRegistry() {
 		synchronized (this) {
 			if (fRegistry != null)
 				return fRegistry;
 		}
 		// Now need to start the appropriate job. In another class so that it can handle dynamically checking if 
 		// UI is available to even do this (it maybe not in a UI mode, so then just do it.
 		CreateRegistryJobHandler.createRegistry(this);
 		return fRegistry;
 	}
 	
 	/*
 	 * This is <package-protected> so that only the appropriate create job in this
 	 * package can call it. This is because this must be controlled to only be
 	 * done when build not in progress and serial access.
 	 */
 	void createRegistry(IProgressMonitor pm) {
 		pm.beginTask(BeanInfoAdapterMessages.getString("UICreateRegistryJobHandler.StartBeaninfoRegistry"), 100); //$NON-NLS-1$		
 		if (isRegistryCreated()) {
 			pm.done();
 			return; // It had already been created. Could of been because threads were racing to do the creation, and one got there first.
 		}
 
 		try {
 			ConfigurationContributor configurationContributor = (ConfigurationContributor) getConfigurationContributor();
 			configurationContributor.setNature(this);
 			ProxyFactoryRegistry registry = ProxyLaunchSupport.startImplementation(fProject, "Beaninfo", //$NON-NLS-1$
 					new IConfigurationContributor[] { configurationContributor}, false, new SubProgressMonitor(pm, 100));
 			registry.addRegistryListener(registryListener);
 			synchronized(this) {
 				fRegistry = registry;
 			}
 		} catch (CoreException e) {
 			BeaninfoPlugin.getPlugin().getLogger().log(e.getStatus());
 		} finally {
 			pm.done();
 		}
 	}
 	
 	public synchronized boolean isRegistryCreated() {
 		return fRegistry != null;
 	}
 	
 	/**
 	 * Check to see if the nature is still valid. If the project has been
 	 * renamed, the nature is still around, but the project has been closed.
 	 * So the nature is now invalid.
 	 * 
 	 * @return Is this a valid nature. I.e. is the project still open.
 	 */
 	public boolean isValidNature() {
 		return fProject != null;
 	}
 
 	/**
 	 * Set the search path onto the registry.
 	 */
 	protected void setProxySearchPath(ProxyFactoryRegistry registry, List searchPaths) {
 		if (searchPaths != null) {
 			String[] stringSearchPath = (String[]) searchPaths.toArray(new String[searchPaths.size()]);
 			Utilities.setBeanInfoSearchPath(registry, stringSearchPath);
 		} else
 			Utilities.setBeanInfoSearchPath(registry, null);
 	}
 
 	private static final String ENCODING = "UTF-8"; //$NON-NLS-1$
 	static final String sBeaninfos = "beaninfos"; // Root element name //$NON-NLS-1$
 	/**
 	 * Get the persistent search path. It is copy.
 	 */
 	public BeaninfosDoc getSearchPath() {
 		BeaninfosDoc bdoc = null;
 		try {
 			InputStream property = getSharedProperty(P_BEANINFO_SEARCH_PATH);
 			if (property != null) {
 				try {
 					// Need to reconstruct from the XML format.
 					Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new InputStreamReader(property, ENCODING)));
 					Element root = doc.getDocumentElement();
 					if (root != null && root.getNodeName().equalsIgnoreCase(sBeaninfos)) {
 						bdoc = BeaninfosDoc.readEntry(new DOMReader(), root, getProject());
 					}
 				} finally {
 					try {
 						property.close();
 					} catch (IOException e) {
 					}
 				}
 			}
 		} catch (CoreException e) {
 			BeaninfoPlugin.getPlugin().getLogger().log(e.getStatus());
 		} catch (Exception e) {
 			BeaninfoPlugin.getPlugin().getLogger().log(new Status(IStatus.WARNING, BeaninfoPlugin.PI_BEANINFO_PLUGINID, 0, "", e)); //$NON-NLS-1$
 		}
 		return bdoc;
 	}
 
 	/**
 	 * Set the persistent search path. No progress monitor.
 	 */
 	public void setSearchPath(BeaninfosDoc searchPath) throws CoreException {
 		setSearchPath(searchPath, null);
 	}
 
 	/**
 	 * Set the persistent search path with a progress monitor
 	 */
 	public void setSearchPath(BeaninfosDoc searchPath, IProgressMonitor monitor) throws CoreException {
 		String property = null;
 		if (searchPath != null && searchPath.getSearchpath().length > 0) {
 			try {
 				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
 				Element root = doc.createElement(sBeaninfos); // Create Root Element
 				IBeaninfosDocEntry[] entries = searchPath.getSearchpath();
 				for (int i = 0; i < entries.length; i++)
 					root.appendChild(entries[i].writeEntry(doc, getProject())); // Add to the search path
 				doc.appendChild(root); // Add Root to Document
 				StringWriter strWriter = new StringWriter();
 
 				Result result = new StreamResult(strWriter);
 				Source source = new DOMSource(doc);
 				Transformer transformer = TransformerFactory.newInstance().newTransformer();
 				transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$
 				transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
 				transformer.transform(source, result);
 				property = strWriter.toString();
 			} catch (TransformerConfigurationException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e, Level.WARNING);
 			} catch (TransformerException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e, Level.WARNING);
 			} catch (ParserConfigurationException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e, Level.WARNING);
 			} catch (FactoryConfigurationError e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e, Level.WARNING);
 			}
 		}
 
 		if (property != null) {
 			// If it hasn't changed, don't write it back out. This is so that if the file hasn't
 			// been checked out and it is the same, we don't want to bother the user. This is because
 			// we don't know if the user had simply browsed the search path or had actually changed and
 			// set it back to what it was. In either of those cases it would be a bother to ask the
 			// user to checkout the file.
 			InputStream is = getSharedProperty(P_BEANINFO_SEARCH_PATH);
 			if (is != null) {
 				try {
 					try {
 						InputStreamReader reader = new InputStreamReader(is, ENCODING);
 						char[] chars = new char[1000];
 						StringBuffer oldProperty = new StringBuffer(1000);
 						int read = reader.read(chars);
 						while (read != -1) {
 							oldProperty.append(chars, 0, read);
 							read = reader.read(chars);
 						}
 						if (oldProperty.toString().equals(property))
 							return;
 					} catch (IOException e) {
 					} // Didn't change.
 				} finally {
 					try {
 						is.close();
 					} catch (IOException e) {
 					}
 				}
 			}
 			setSharedProperty(P_BEANINFO_SEARCH_PATH, property, monitor);
 		} else
 			removeSharedProperty(P_BEANINFO_SEARCH_PATH, monitor);
 	}
 
 	/**
 	 * Return the resource set for all java packages in this nature.
 	 */
 	public ResourceSet getResourceSet() {
 		return javaRSet;
 	}
 
 	protected void markAllStale() {
 		// Mark all stale so that the registry will be recycled.
 		if (fRegistry != null) {
 			// We have a registry running, we need to indicate recycle is needed.
 			fSynchronizer.getAdapterFactory().markAllStale();
 			// Mark all stale. Next time we need anything it will be recycled.
 		}
 	}
 
 	/**
 	 * Compute the file name to use for a given shared property
 	 */
 	protected String computeSharedPropertyFileName(QualifiedName qName) {
 		return qName.getLocalName();
 	}
 
 	/**
 	 * Retrieve a shared property on a project. If the property is not defined, answers null.
 	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
 	 * which form of storage to use appropriately. Shared properties produce real resource files which
 	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
 	 *
 	 */
 	protected InputStream getSharedProperty(String propertyFileName) throws CoreException {
 		IFile rscFile = getProject().getFile(propertyFileName);
 		if (rscFile.exists())
 			return rscFile.getContents(true);
 		else
 			return null;
 	}
 
 	/**
 	 * Record a shared persistent property onto a project.
 	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
 	 * which form of storage to use appropriately. Shared properties produce real resource files which
 	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
 	 * 
 	 * shared properties end up in resource files, and thus cannot be modified during
 	 * delta notifications (a CoreException would then be thrown).
 	 * 
 	 */
 	protected void setSharedProperty(String propertyName, String value, IProgressMonitor monitor) throws CoreException {
 
 		try {
 			IFile rscFile = getProject().getFile(propertyName);
 			InputStream input = new ByteArrayInputStream(value.getBytes(ENCODING));
 			// update the resource content
 			if (rscFile.exists()) {
 				rscFile.setContents(input, true, false, null);
 			} else {
 				rscFile.create(input, true, monitor);
 			}
 		} catch (UnsupportedEncodingException e) {
 		}
 	}
 
 	/**
 	 * Remove a shared persistent property onto a project.
 	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
 	 * which form of storage to use appropriately. Shared properties produce real resource files which
 	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
 	 * 
 	 * shared properties end up in resource files, and thus cannot be modified during
 	 * delta notifications (a CoreException would then be thrown).
 	 * 
 	 */
 	protected void removeSharedProperty(String propertyName, IProgressMonitor monitor) throws CoreException {
 
 		IFile rscFile = getProject().getFile(propertyName);
 		rscFile.delete(true, true, monitor);
 	}
 
 	/**
 	 * Return a configuration contributor that sets up a vm to allow
 	 * introspection. This will make sure the appropriate paths
 	 * are in the classpath to allow access to the beaninfos, and
 	 * it will setup the beaninfo search path for this project.
 	 */
 	public IConfigurationContributor getConfigurationContributor() {
 		return new ConfigurationContributor(getSearchPath());
 	}
 
 	private static class ConfigurationContributor extends ConfigurationContributorAdapter {
 
 		private BeaninfosDoc doc;
 		List computedSearchPath;
 				
 		// The nature. If the nature is not set then this contributor is one
 		// used by some other later proxy registry to get the beaninfo classes into their paths. In that case
 		// we can expect the config info to be in the session variable for our use. Otherwise we will need to
 		// add it here. Also don't set searchpath stuff if not nature because only the beaninfo one will do introspection.
 		private BeaninfoNature nature;	
 		
 		private IConfigurationContributionInfo info;
 		private IBeanInfoContributor[] explicitContributors;
 
 		public ConfigurationContributor(BeaninfosDoc doc) {
 			this.doc = doc;
 		}
 
 		/*
 		 * Set that this is the nature contributor. Not null, means that this is the contributor being
 		 * used to setup the registry for the project's beaninfo nature. null (default) means that this
 		 * is one created to add to some editor's registry.
 		 * 
 		 * Note: This MUST be set before initialize is called or it will not work correctly. If not set, it 
 		 * will be considered not for BeanInfo nature directly.
 		 */
 		public void setNature(BeaninfoNature nature) {
 			this.nature = nature;
 			if (nature != null)
 				computedSearchPath = new ArrayList(3);	// We will be gathering this info.
 		}
 		
 		private static final String PI_CLASS = "class"; //$NON-NLS-1$
 		
 		/* (non-Javadoc)
 		 * @see org.eclipse.jem.internal.proxy.core.IConfigurationContributor#initialize(org.eclipse.jem.internal.proxy.core.IConfigurationContributionInfo)
 		 */
 		public void initialize(IConfigurationContributionInfo info) {
 			this.info = info;
 			try {
 				if (info.getJavaProject().getProject().getSessionProperty(CONFIG_INFO_SESSION_KEY) == null) {
 					// First time for this nature, or first time after registry reset. Need to compute the info.
 					// It is possible for this to be called BEFORE the first usage of BeanInfo. The editor usually
 					// brings up the editor's registry before it gets anything from BeanInfo.
 					List contributorsList = new ArrayList(10);
 					if (!info.getContainerIds().isEmpty()) {
 						// Run through all of the visible container ids that are applicable and get BeanInfo contributors.
 						Iterator containerIdItr = info.getContainerIds().entrySet().iterator();
 						while (containerIdItr.hasNext()) {
 							Map.Entry entry = (Map.Entry) containerIdItr.next();
 							if (((Boolean) entry.getValue()).booleanValue()) {
 								IConfigurationElement[] contributors = BeaninfoPlugin.getPlugin().getContainerIdContributors(
 										(String) entry.getKey());
 								if (contributors != null) {
 									for (int i = 0; i < contributors.length; i++) {
 										try {
 											Object contributor = contributors[i].createExecutableExtension(PI_CLASS);
 											if (contributor instanceof IBeanInfoContributor)
 												contributorsList.add(contributor);
 										} catch (CoreException e) {
 											BeaninfoPlugin.getPlugin().getLogger().log(e, Level.WARNING);
 										}
 									}
 								}
 							}
 						}						
 					}
 					
 					if (!info.getPluginIds().isEmpty()) {
 						// Run through all of the visible plugin ids that are applicable and get BeanInfo contributors.
 						Iterator pluginIdItr = info.getPluginIds().entrySet().iterator();
 						while (pluginIdItr.hasNext()) {
 							Map.Entry entry = (Map.Entry) pluginIdItr.next();
 							if (((Boolean) entry.getValue()).booleanValue()) {
 								IConfigurationElement[] contributors = BeaninfoPlugin.getPlugin().getPluginContributors(
 										(String) entry.getKey());
 								if (contributors != null) {
 									for (int i = 0; i < contributors.length; i++) {
 									try {
 										Object contributor = contributors[i].createExecutableExtension(PI_CLASS);
 										if (contributor instanceof IBeanInfoContributor)
 											contributorsList.add(contributor);
 									} catch (CoreException e) {
 										BeaninfoPlugin.getPlugin().getLogger().log(e, Level.WARNING);
 									}
 								}
 							}
 }
 						}
 					}
 					
 					// Save it for all beaninfo processing (and configuration processing if they implement proxy configuration contributor).
 					explicitContributors = (IBeanInfoContributor[]) contributorsList.toArray(new IBeanInfoContributor[contributorsList.size()]);
 					info.getJavaProject().getProject().setSessionProperty(BEANINFO_CONTRIBUTORS_SESSION_KEY, explicitContributors);
 					// Save it for override processing. That happens over and over later after all config processing is done.
 					// Do it last so that if there is a race condition, since this property is a flag to indicate we have data,
 					// we need to make sure the Beaninfo data is already set at the point we set this.
 					// We could actually set it twice because of this, but it is the same data, so, so what.
 					info.getJavaProject().getProject().setSessionProperty(CONFIG_INFO_SESSION_KEY, info);					
 				} else {
 					explicitContributors = (IBeanInfoContributor[]) info.getJavaProject().getProject().getSessionProperty(BEANINFO_CONTRIBUTORS_SESSION_KEY);
 				}
 			} catch (CoreException e) {
 				BeaninfoPlugin.getPlugin().getLogger().log(e);
 			}
 		}
 		
 		public void contributeClasspaths(final IConfigurationContributionController controller) throws CoreException {
 			// Contribute for this project
 			contributeClasspathsForProject(controller, info.getJavaProject().getProject(), doc, true);
 						
 			if (!info.getProjectPaths().isEmpty()) {
 				// Run through all of the visible projects and contribute the classpaths (which come from the BeanInfo docs, if they have any).
 				IWorkspaceRoot root = info.getJavaProject().getProject().getWorkspace().getRoot();
 				Iterator projIter = info.getProjectPaths().entrySet().iterator();
 				while (projIter.hasNext()) {
 					Map.Entry entry = (Map.Entry) projIter.next();
 					if (((Boolean) entry.getValue()).booleanValue()) {
 						IResource res = root.findMember((IPath) entry.getKey());
 						if (res instanceof IProject && ((IProject) res).isOpen() && BeaninfoNature.hasRuntime((IProject) res))
 							contributeClasspathsForProject(controller, (IProject) res, BeaninfoNature.getRuntime((IProject) res)
 									.getSearchPath(), false);
 					}
 				}
 			}			
 			
 			if (!info.getContainerIds().isEmpty()) {
 				// Run through all of the visible container ids that are applicable.
 				Iterator containerIdItr = info.getContainerIds().entrySet().iterator();
 				while (containerIdItr.hasNext()) {
 					Map.Entry entry = (Map.Entry) containerIdItr.next();
 					if (((Boolean) entry.getValue()).booleanValue()) {
 						processBeaninfoEntries(BeaninfoPlugin.getPlugin().getContainerIdBeanInfos((String) entry.getKey()),
 								controller, info.getJavaProject());
 					}
 				}
 				
 			}
 			
 			if (!info.getPluginIds().isEmpty()) {
 				// Run through all of the visible plugin ids that are applicable.
 				Iterator pluginIdItr = info.getPluginIds().entrySet().iterator();
 				while (pluginIdItr.hasNext()) {
 					Map.Entry entry = (Map.Entry) pluginIdItr.next();
 					if (((Boolean) entry.getValue()).booleanValue()) {
 						processBeaninfoEntries(BeaninfoPlugin.getPlugin().getPluginBeanInfos((String) entry.getKey()), controller, info.getJavaProject());
 					}
 				}
 				
 			}
 			
 			if (!info.getContainers().isEmpty()) {
 				// Run through all of the visible containers that implement IBeanInfoContributor and ask them for the contributions.
 				Iterator containerItr = info.getContainers().entrySet().iterator();
 				while (containerItr.hasNext()) {
 					Map.Entry entry = (Map.Entry) containerItr.next();
 					if (((Boolean) entry.getValue()).booleanValue()) {
 						if (entry.getKey() instanceof IBeanInfoContributor)
 							processBeaninfoEntries(((IBeanInfoContributor) entry.getKey()).getBeanInfoEntryContributions(info),
 									controller, info.getJavaProject());
 					}
 				}
 				
 			}			
 				
 			// And finally run through the explicit contributors.				
 			for (int i = 0; i < explicitContributors.length; i++) {
 				final IBeanInfoContributor contributor = explicitContributors[i];
 				processBeaninfoEntries(contributor.getBeanInfoEntryContributions(info), controller, info.getJavaProject());
 				if (contributor instanceof IConfigurationContributor) {
 					Platform.run(new ISafeRunnable() {
 						public void handleException(Throwable exception) {
 							// do nothing. by default platform logs.
 						}
 
 						public void run() throws Exception {;
 							if (contributor instanceof IConfigurationContributor)
 								((IConfigurationContributor) contributor).contributeClasspaths(controller);
 						}
 					});
 				}
 			}
 			
 			// Add the beaninfovm.jar and any nls to the end of the classpath.
 			controller.contributeClasspath(BeaninfoPlugin.getPlugin().getBundle(), "vm/beaninfovm.jar", IConfigurationContributionController.APPEND_USER_CLASSPATH, true); //$NON-NLS-1$
 		}
 
 		private IClasspathEntry get(IClasspathEntry[] array, SearchpathEntry se) {
 			for (int i = 0; i < array.length; i++) {
 				if (array[i].getEntryKind() == se.getKind() && array[i].getPath().equals(se.getPath()))
 					return array[i];
 			}
 			return null;
 		}
 
 		private static final IBeaninfosDocEntry[] EMPTY_ENTRIES = new IBeaninfosDocEntry[0];
 		
 		/*
 		 * Contribute classpaths for the specified project. If doc is passed in, then this is the top level and
 		 * all should be added. If no doc, then this is pre-req'd project, and then we will handle exported entries only.
 		 */
 		protected void contributeClasspathsForProject(
 			IConfigurationContributionController controller,
 			IProject project,
 			BeaninfosDoc doc,
 			boolean toplevelProject)
 			throws CoreException {
 			
 			IJavaProject jProject = JavaCore.create(project);
 			IClasspathEntry[] rawPath = jProject.getRawClasspath();
 
 			// Search path of this project
 			IBeaninfosDocEntry[] entries = (doc != null) ? doc.getSearchpath() : EMPTY_ENTRIES;
 
 			for (int i = 0; i < entries.length; i++) {
 				IBeaninfosDocEntry entry = entries[i];
 				if (entry instanceof BeaninfoEntry) {
 					BeaninfoEntry be = (BeaninfoEntry) entry;
 					if (toplevelProject || be.isExported()) {
 						// First project or this is an exported beaninfo, so we process it.
 						processBeaninfoEntry(be, controller, jProject);
 					}
 				} else if (nature != null){
 					// Just a search path entry. There is no beaninfo jar to pick up.
 					// We have a nature, so we process search path.
 					SearchpathEntry se = (SearchpathEntry) entry;
 					if (!toplevelProject) {
 						// We are in a nested project, find the raw classpath entry to see
 						// if this entry is exported. Only do it if exported. (Note: exported is only used on non-source. Source are always exported).
 						IClasspathEntry cpe = get(rawPath, se);
 						if (cpe == null || (cpe.getEntryKind() != IClasspathEntry.CPE_SOURCE && !cpe.isExported())) {
 							continue; // Not exist or not exported, so we don't want it here either.
 						}
 					}
 
 					String pkg = se.getPackage();
 					if (pkg != null) {
 						// Explicit search path
 						if (!computedSearchPath.contains(pkg))
 							computedSearchPath.add(pkg);
 					} else {
 						// We no longer allow this, but just to be on safe side we test for it.
 					}
 				}
 			}
 		}
 
 		protected void processBeaninfoEntries(
 			BeaninfoEntry[] entries,
 			IConfigurationContributionController controller,
 			IJavaProject javaProject)
 			throws CoreException {
 			if (entries != null) {
 				for (int i = 0; i < entries.length; i++)
 					processBeaninfoEntry(entries[i], controller, javaProject);
 			}
 		}
 
 		protected void processBeaninfoEntry(
 			BeaninfoEntry entry,
 			IConfigurationContributionController controller,
 			IJavaProject javaProject)
 			throws CoreException {
 			Object[] cps = entry.getClasspath(javaProject);
 			for (int j = 0; j < cps.length; j++) {
 				Object cp = cps[j];
 				if (cp instanceof IProject)
 					controller.contributeProject((IProject) cp);
 				else if (cp instanceof String)
 					controller.contributeClasspath(ProxyLaunchSupport.convertStringPathToURL((String) cp), IConfigurationContributionController.APPEND_USER_CLASSPATH);
 				else if (cp instanceof IPath) {
 					IPath path = (IPath) cp;
 					Bundle bundle = Platform.getBundle(path.segment(0));
 					if (bundle != null)
 						controller.contributeClasspath(bundle, path.removeFirstSegments(1), IConfigurationContributionController.APPEND_USER_CLASSPATH, true);
 				}
 			}
 
 			if (nature != null) {
 				// Now add in the package names.
 				SearchpathEntry[] sees = entry.getSearchPaths();
 				for (int j = 0; j < sees.length; j++) {
 					SearchpathEntry searchpathEntry = sees[j];
 					if (!computedSearchPath.contains(searchpathEntry.getPackage()))
 						computedSearchPath.add(searchpathEntry.getPackage());
 				}
 			}
 		}
 
 		public void contributeToConfiguration(final ILaunchConfigurationWorkingCopy config) {
 			for (int i = 0; i < explicitContributors.length; i++) {
 				final int ii = i;
 				Platform.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						// do nothing. by default platform logs.
 					}
 
 					public void run() throws Exception {
 						IBeanInfoContributor contributor = explicitContributors[ii];
 						if (contributor instanceof IConfigurationContributor)
 							((IConfigurationContributor) contributor).contributeToConfiguration(config);
 					}
 				});
 			}			
 		}
 
 		public void contributeToRegistry(final ProxyFactoryRegistry registry) {
 			if (nature != null)
 				nature.setProxySearchPath(registry, computedSearchPath);
 			for (int i = 0; i < explicitContributors.length; i++) {
 				final int ii = i;
 				Platform.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						// do nothing. by default platform logs.
 					}
 
 					public void run() throws Exception {
 						IBeanInfoContributor contributor = explicitContributors[ii];
 						if (contributor instanceof IConfigurationContributor)
 							((IConfigurationContributor) contributor).contributeToRegistry(registry);
 					}
 				});
 			}			
 		}
 	}
 
 
 }
