 /*******************************************************************************
  * <copyright>
  *
 * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.ui.internal.platform;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IContributor;
 import org.eclipse.core.runtime.IExtension;
 import org.eclipse.core.runtime.IExtensionPoint;
 import org.eclipse.core.runtime.IExtensionRegistry;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.graphiti.dt.IDiagramType;
 import org.eclipse.graphiti.dt.IDiagramTypeProvider;
 import org.eclipse.graphiti.features.IFeatureProvider;
 import org.eclipse.graphiti.internal.util.T;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.ui.internal.editor.DiagramEditorDummy;
 import org.eclipse.graphiti.ui.internal.util.ui.print.IDiagramsExporter;
 import org.eclipse.graphiti.ui.platform.IImageProvider;
 import org.eclipse.graphiti.ui.platform.PlatformImageProvider;
 import org.eclipse.graphiti.ui.services.IExtensionManager;
 
 /**
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public class ExtensionManager implements IExtensionManager {
 
 	private static ExtensionManager singleton;
 
 	private static String EP_DIAGRAM_TYPES = "org.eclipse.graphiti.ui.diagramTypes"; //$NON-NLS-1$
 
 	private static String EP_IMAGE_PROVIDERS = "org.eclipse.graphiti.ui.imageProviders"; //$NON-NLS-1$
 
 	private static String EP_DIAGRAM_TYPE_PROVIDERS = "org.eclipse.graphiti.ui.diagramTypeProviders"; //$NON-NLS-1$
 
 	private static String EP_DIAGRAM_EXPORTERS = "org.eclipse.graphiti.ui.diagramExporters"; //$NON-NLS-1$
 
 	private static final String EP_CHILD_NODE_DIAGRAM_EXPORTER = "diagramexporter"; //$NON-NLS-1$
 
 	private static final String EP_CHILD_NODE_IMAGE_PROVIDER = "imageProvider"; //$NON-NLS-1$
 
 	private static final String EP_CHILD_NODE_DIAGRAM_TYPE = "diagramType"; //$NON-NLS-1$
 
 	private static final String EP_ATTRIBUTE_CLASS = "class"; //$NON-NLS-1$
 
 	private static final String EP_ATTRIBUTE_ID = "id"; //$NON-NLS-1$
 
 	private static final String EP_ATTRIBUTE_TYPE = "type"; //$NON-NLS-1$
 
 	private static final String EP_ATTRIBUTE_ENABLEUI = "enableScaling"; //$NON-NLS-1$
 
 	private static final String EP_ATTRIBUTE_NAME = "name"; //$NON-NLS-1$
 
 	private static final String EP_ATTRIBUTE_DESCRIPTION = "description"; //$NON-NLS-1$
 
 	private IDiagramType diagramTypes[] = null;
 
 	private IImageProvider imageProviders[] = null;
 
 	/**
 	 * 
 	 */
 	private ExtensionManager() {
 		super();
 		searchForExtensions();
 	}
 
 	public static ExtensionManager getSingleton() {
 		if (singleton == null) {
 			singleton = new ExtensionManager();
 		}
 		return singleton;
 	}
 
 	@Override
 	public String[] getDiagramTypeProviderIds(String diagramTypeId) {
 		String ret[] = new String[0];
 		if (diagramTypeId == null) {
 			return ret;
 		}
 		List<String> retList = new ArrayList<String>();
 
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 
 		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EP_DIAGRAM_TYPE_PROVIDERS);
 		IExtension[] extensions = extensionPoint.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configurationElements = extension.getConfigurationElements();
 			for (int j = 0; j < configurationElements.length; j++) {
 				IConfigurationElement element = configurationElements[j];
 				String extensionId = element.getAttribute(EP_ATTRIBUTE_ID);
 				if (extensionId != null) {
 					// read references to diagram types
 					IConfigurationElement[] children = element.getChildren();
 					for (int k = 0; k < children.length; k++) {
 						IConfigurationElement childElement = children[k];
 						String childName = childElement.getName();
 						String childExtensionId = childElement.getAttribute(EP_ATTRIBUTE_ID);
 						if (childName != null && childExtensionId != null) {
 							if (EP_CHILD_NODE_DIAGRAM_TYPE.equals(childName)) {
 								String typeId = getDiagramTypeIdForDiagramTypeProviderId(childExtensionId);
 								if (diagramTypeId.equals(typeId)) {
 									retList.add(extensionId);
 									break;
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 
 		ret = retList.toArray(ret);
 		return ret;
 	}
 
 	public Map<String, Boolean> getDiagramExporterTypes() {
 		Map<String, Boolean> ret = new HashMap<String, Boolean>();
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EP_DIAGRAM_EXPORTERS);
 		IExtension[] extensions = extensionPoint.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configurationElements = extension.getConfigurationElements();
 			for (int j = 0; j < configurationElements.length; j++) {
 				IConfigurationElement element = configurationElements[j];
 				String name = element.getName();
 				String type = element.getAttribute(EP_ATTRIBUTE_TYPE);
 				if (name != null && type != null) {
 					if (EP_CHILD_NODE_DIAGRAM_EXPORTER.equals(name)) {
 						String enableScaling = element.getAttribute(EP_ATTRIBUTE_ENABLEUI);
 						ret.put(type, Boolean.valueOf(enableScaling));
 						break;
 					}
 				}
 			}
 		}
 		return ret;
 	}
 
 	public IDiagramsExporter getDiagramExporterForType(String type) {
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EP_DIAGRAM_EXPORTERS);
 		IExtension[] extensions = extensionPoint.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configurationElements = extension.getConfigurationElements();
 			for (int j = 0; j < configurationElements.length; j++) {
 				IConfigurationElement element = configurationElements[j];
 				String name = element.getName();
 				String currType = element.getAttribute(EP_ATTRIBUTE_TYPE);
 				if (name != null && type != null) {
 					if (EP_CHILD_NODE_DIAGRAM_EXPORTER.equals(name) && type.equals(currType)) {
 						try {
 							Object executableExtension = element.createExecutableExtension(EP_ATTRIBUTE_CLASS);
 							if (executableExtension instanceof IDiagramsExporter) {
 								return (IDiagramsExporter) executableExtension;
 							}
 						} catch (CoreException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private void searchForExtensions() {
 
 		// read and store all diagram types which are registered with the
 		// extension point
 		List<IDiagramType> diagramTypeList = createDiagramTypes();
 		diagramTypes = diagramTypeList.toArray(new IDiagramType[0]);
 
 		imageProviders = new IImageProvider[0];
 
 		// load image providers from the framework
 		loadImageProvider(PlatformImageProvider.ID);
 	}
 
 	@Override
 	public IDiagramTypeProvider createDiagramTypeProvider(String providerId) {
 		IDiagramTypeProvider diagramTypeProvider = null;
 
 		if (providerId == null) {
 			return diagramTypeProvider;
 		}
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 
 		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EP_DIAGRAM_TYPE_PROVIDERS);
 		IExtension[] extensions = extensionPoint.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configurationElements = extension.getConfigurationElements();
 			for (int j = 0; j < configurationElements.length; j++) {
 				IConfigurationElement element = configurationElements[j];
 				String extensionId = element.getAttribute(EP_ATTRIBUTE_ID);
 				if (extensionId != null && extensionId.equals(providerId)) {
 					String name = element.getAttribute(EP_ATTRIBUTE_NAME);
 					String description = element.getAttribute(EP_ATTRIBUTE_DESCRIPTION);
 					if (description == null) {
 						description = ""; //$NON-NLS-1$
 					}
 					if (name != null) {
 						// read references to image extensions and try to
 						// instantiate image provider
 						IConfigurationElement[] children = element.getChildren();
 						for (int k = 0; k < children.length; k++) {
 							IConfigurationElement childElement = children[k];
 							String childName = childElement.getName();
 							String childExtensionId = childElement.getAttribute(EP_ATTRIBUTE_ID);
 							if (childName != null && childExtensionId != null) {
 								if (EP_CHILD_NODE_IMAGE_PROVIDER.equals(childName)) {
 									boolean ret = loadImageProvider(childExtensionId);
 									if (ret == false) {
										return null;
 									}
 								}
 							}
 						}
 						try {
 							Object executableExtension = element.createExecutableExtension(EP_ATTRIBUTE_CLASS);
 							if (executableExtension instanceof IDiagramTypeProvider) {
 								diagramTypeProvider = (IDiagramTypeProvider) executableExtension;
 								diagramTypeProvider.setProviderId(extensionId);
 								// TODO later store also name and description -
 								// is only necessary if this information
 								// are shown in the UI
 							}
 						} catch (CoreException e) {
 							// $JL-EXC$
 							T.racer().error(e.toString());
 						}
 					}
 				}
 			}
 		}
 
 		return diagramTypeProvider;
 	}
 
 	public IImageProvider[] getImageProviders() {
 		return imageProviders;
 	}
 
 	@Override
 	public IDiagramType[] getDiagramTypes() {
 		return diagramTypes;
 	}
 
 	private String getDiagramTypeIdForDiagramTypeProviderId(String providerId) {
 		if (providerId != null) {
 			IDiagramType diagramTypes[] = getDiagramTypes();
 			for (int i = 0; i < diagramTypes.length; i++) {
 				IDiagramType diagramType = diagramTypes[i];
 				if (providerId.equals(diagramType.getProviderId())) {
 					return diagramType.getId();
 				}
 			}
 		}
 		return null;
 	}
 
 	private boolean loadImageProvider(String providerId) {
 
 		// check whether the image provider is already loaded
 		IImageProvider providers[] = getImageProviders();
 		for (int i = 0; i < providers.length; i++) {
 			IImageProvider provider = providers[i];
 			if (providerId.equals(provider.getProviderId())) {
 				return true;
 			}
 		}
 		IImageProvider newProvider = createImageProvider(providerId);
 		if (newProvider != null) {
 
 			// extend current list of image providers
 			IImageProvider imageProvidersDest[] = new IImageProvider[imageProviders.length + 1];
 			System.arraycopy(imageProviders, 0, imageProvidersDest, 0, imageProviders.length);
 			imageProvidersDest[imageProviders.length] = newProvider;
 			imageProviders = imageProvidersDest;
 
 			return true;
 		}
 
 		return false;
 	}
 
 	private List<IDiagramType> createDiagramTypes() {
 
 		List<IDiagramType> ret = new ArrayList<IDiagramType>();
 
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 
 		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EP_DIAGRAM_TYPES);
 		IExtension[] extensions = extensionPoint.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configurationElements = extension.getConfigurationElements();
 			for (int j = 0; j < configurationElements.length; j++) {
 				IConfigurationElement element = configurationElements[j];
 				String extensionId = element.getAttribute(EP_ATTRIBUTE_ID);
 				String typeId = element.getAttribute(EP_ATTRIBUTE_TYPE);
 				String typeName = element.getAttribute(EP_ATTRIBUTE_NAME);
 				String typeDescription = element.getAttribute(EP_ATTRIBUTE_DESCRIPTION);
 				if (typeDescription == null) {
 					typeDescription = ""; //$NON-NLS-1$
 				}
 				if (extensionId != null && typeId != null && typeName != null) {
 					IDiagramType diagramType = new DiagramTypeImpl(typeId, typeName, typeDescription);
 					diagramType.setProviderId(extensionId);
 					ret.add(diagramType);
 				}
 			}
 		}
 		return ret;
 	}
 
 	private IImageProvider createImageProvider(String providerId) {
 
 		if (providerId == null) {
 			return null;
 		}
 		IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
 
 		IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(EP_IMAGE_PROVIDERS);
 		IExtension[] extensions = extensionPoint.getExtensions();
 		for (int i = 0; i < extensions.length; i++) {
 			IExtension extension = extensions[i];
 			IConfigurationElement[] configurationElements = extension.getConfigurationElements();
 			for (int j = 0; j < configurationElements.length; j++) {
 				IConfigurationElement element = configurationElements[j];
 				String extensionId = element.getAttribute(EP_ATTRIBUTE_ID);
 				if (providerId.equals(extensionId)) {
 					try {
 						Object executableExtension = element.createExecutableExtension(EP_ATTRIBUTE_CLASS);
 						if (executableExtension instanceof IImageProvider) {
 							IContributor contributor = extension.getContributor();
 							if (contributor != null) {
 								String pluginId = contributor.getName();
 								if (pluginId != null) {
 									IImageProvider imageProvider = (IImageProvider) executableExtension;
 									imageProvider.setProviderId(extensionId);
 									imageProvider.setPluginId(pluginId);
 									return imageProvider;
 								}
 							}
 						}
 					} catch (CoreException e) {
 						// $JL-EXC$
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		return null;
 	}
 
 	@Override
 	public IFeatureProvider createFeatureProvider(Diagram diagram) {
 		Assert.isNotNull(diagram);
 		String providerId = getDiagramTypeProviderId(diagram.getDiagramTypeId());
 		if (providerId != null) {
 			IDiagramTypeProvider dtp = createDiagramTypeProvider(diagram, providerId);
 			return dtp.getFeatureProvider();
 		}
 		return null;
 	}
 
 	@Override
 	public IDiagramTypeProvider createDiagramTypeProvider(Diagram diagram, String providerId) {
 		Assert.isNotNull(diagram);
 		IDiagramTypeProvider dtp = createDiagramTypeProvider(providerId);
 		if (dtp != null) {
 			TransactionalEditingDomain editingDomain = TransactionUtil.getEditingDomain(diagram);
 			DiagramEditorDummy diagramEditor = new DiagramEditorDummy(dtp, editingDomain);
 			dtp.init(diagram, diagramEditor);
 		}
 		return dtp;
 	}
 
 	@Override
 	public String getDiagramTypeProviderId(String diagramTypeId) {
 		String diagramTypeProviders[] = getDiagramTypeProviderIds(diagramTypeId);
 		if (diagramTypeProviders != null && diagramTypeProviders.length > 0) {
 			return diagramTypeProviders[0];
 		}
 		return null;
 	}
 
 }
