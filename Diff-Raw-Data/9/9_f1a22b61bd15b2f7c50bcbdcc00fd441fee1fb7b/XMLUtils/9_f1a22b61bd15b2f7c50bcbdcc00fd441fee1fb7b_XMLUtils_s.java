 /***************************************************************************************************
 * Copyright (c) 2005 Eteration A.S. and Gorkem Ercan. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Gorkem Ercan - initial API and implementation
  *               
  **************************************************************************************************/
 
 package org.eclipse.jst.server.generic.internal.xml;
 
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.common.util.WrappedException;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.jst.server.generic.core.internal.CorePlugin;
 import org.eclipse.jst.server.generic.core.internal.Trace;
 import org.eclipse.jst.server.generic.internal.core.util.ExtensionPointUtil;
 import org.eclipse.jst.server.generic.internal.servertype.definition.ServerTypePackage;
 import org.eclipse.jst.server.generic.internal.servertype.definition.util.ServerTypeResourceFactoryImpl;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
 import org.osgi.framework.Bundle;
 
 /**
  * Utility for handling the xml data from .serverdef and .runtimedef files.
  * 
  * @author Gorkem Ercan
  */
 public class XMLUtils {
 
 	
     private ArrayList serverDefinitions;
     private ArrayList runtimeDefinitions;
 
 
 	/**
 	 * Constructor
 	 */
 	public XMLUtils() {
 		refresh();
 	}
 
 	/**
 	 * Load all the serverdefinition and runtimedefinition extensions.
 	 */
 	private void refresh() {
         serverDefinitions = new ArrayList();
         
         IExtension[] serverDefExtensions = ExtensionPointUtil.getGenericServerDefinitionExtensions();
         
         for (int i = 0; serverDefExtensions != null && i < serverDefExtensions.length; i++) {
             java.net.URI definitionFile = null;
             IExtension extension = serverDefExtensions[i];
             IConfigurationElement[] elements = ExtensionPointUtil.getConfigurationElements(extension);
             
             for (int j = 0; j < elements.length; j++) {
                 IConfigurationElement element = elements[j];
                 definitionFile = getDefinitionFile(element);
                 ServerRuntime runtime = readFile(definitionFile);
                 if (runtime != null) {
                     runtime.setId(element.getAttribute("id")); //$NON-NLS-1$
                     runtime.setConfigurationElementNamespace(element.getNamespace());
                     serverDefinitions.add(runtime);
                 }
             }
         }
 
         runtimeDefinitions = new ArrayList();
         
         IExtension[] runtimeDefExtensions = ExtensionPointUtil.getGenericServerRuntimeDefinitionExtensions();
         
         for (int i = 0; runtimeDefExtensions != null && i < runtimeDefExtensions.length; i++) {
             java.net.URI definitionFile = null;
             IExtension extension = runtimeDefExtensions[i];
             IConfigurationElement[] elements = ExtensionPointUtil.getConfigurationElements(extension);
             
             for (int j = 0; j < elements.length; j++) {
                 IConfigurationElement element = elements[j];
                 definitionFile = getDefinitionFile(element);
                 ServerRuntime runtime = readFile(definitionFile);
                 if (runtime != null) {
                     runtime.setId(element.getAttribute("id")); //$NON-NLS-1$
                     runtime.setConfigurationElementNamespace(element.getNamespace());
                     runtimeDefinitions.add(runtime);
                 }
             }
         }
     }
 
     private java.net.URI getDefinitionFile(IConfigurationElement element) {
         
         Bundle bundle = Platform.getBundle(element.getNamespace());
         String definitionFile = element.getAttribute("definitionfile"); //$NON-NLS-1$
         Trace.trace(Trace.FINEST,"Loading serverdef file "+definitionFile+" from bundle "+bundle.getSymbolicName()); //$NON-NLS-1$ //$NON-NLS-2$
         
         URL url = bundle.getEntry(definitionFile);
         if (url == null )
         {
             CorePlugin.getDefault().getLog().log( new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID,1,
                     "Definition file "+definitionFile+" can not be resolved in bundle "+ bundle.getSymbolicName() ,null) ); //$NON-NLS-1$ //$NON-NLS-2$
             return null;
         }
 		try {
			java.net.URI uri = new java.net.URI(url.getProtocol(), url.getHost(),url.getPath(), url.getQuery());
 		    return uri;
 		} catch (URISyntaxException e) {
			//ignore
 		}
         return null;
     }
 
     private ServerRuntime readFile(java.net.URI file) {
         // Create a resource set.
         ResourceSet resourceSet = new ResourceSetImpl();
 
         // Register the default resource factory -- only needed for
         // stand-alone!
         resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
                 .put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                         new ServerTypeResourceFactoryImpl());
 
          ServerTypePackage gstPack = ServerTypePackage.eINSTANCE;
 
         // Get the URI of the model file.
         URI fileURI = URI.createURI(file.toString());
 
         // Demand load the resource for this file.
         Resource resource = null;
         try {
             resource = resourceSet.getResource(fileURI, true);
         } catch (WrappedException e) {
             // sth wrong with this .server file.
             CorePlugin.getDefault().getLog().log(
                     new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 1,
                             "Error loading the server type definition", e)); //$NON-NLS-1$
         }
 
         if (resource != null) {
             ServerRuntime def = (ServerRuntime) resource.getContents().get(0);
             if (def != null) {
                 def.setFilename(file.toString());
                 return def;
             }
         }
         return null;
 
     }
 
 	/**
 	 * @return <code>java.util.List</code> of <code>ServerRuntime</code>s.
 	 */
 	public List getServerTypeDefinitions() {
 		return serverDefinitions;
 	}
 
     /**
      * Get the memory presentation for the .serverdef file
      * @param id
      * @return serverRuntime
      */
     public ServerRuntime getServerTypeDefinition(String id) {
     	Iterator defs = getServerTypeDefinitions().iterator();
         while (id != null && defs.hasNext()) {
             ServerRuntime elem = (ServerRuntime) defs.next();
             if ( elem.getId().equals( id ) )
                 return elem;
         }
         return null;
     }
 
     /**
      * @return <code>java.util.List</code> of <code>ServerRuntime</code>s.
      */
     public List getRuntimeTypeDefinitions() {
         return runtimeDefinitions;
     }
 
     /**
      * Get the memory presentation for the .runtimedef file
      * @param id
      * @return runtime 
      */
     public ServerRuntime getRuntimeTypeDefinition(String id) {
         Iterator defs = getRuntimeTypeDefinitions().iterator();
         while (defs.hasNext()) {
             ServerRuntime elem = (ServerRuntime) defs.next();
             if ( elem.getId().equals(id) )
                 return elem;
         }
         return null;
     }
 }
