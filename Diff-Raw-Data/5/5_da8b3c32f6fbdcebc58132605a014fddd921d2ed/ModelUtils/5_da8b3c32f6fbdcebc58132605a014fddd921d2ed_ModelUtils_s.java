 /*******************************************************************************
  * Copyright (c) 2006, 2009 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.util;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.EMFCompareMessages;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 
 /**
  * Utility class for model loading/saving and serialization.
  * 
  * @author <a href="mailto:laurent.goubet@obeo.fr">Laurent Goubet</a>
  */
 public final class ModelUtils {
 	/** Constant for the file encoding system property. */
 	private static final String ENCODING_PROPERTY = "file.encoding"; //$NON-NLS-1$
 
 	/**
 	 * Utility classes don't need to (and shouldn't) be instantiated.
 	 */
 	private ModelUtils() {
 		// prevents instantiation
 	}
 
 	/**
 	 * Attaches the given {@link EObject} to a new resource created in a new {@link ResourceSet} with the
 	 * given URI.
 	 * 
 	 * @param resourceURI
 	 *            URI of the new resource to create.
 	 * @param root
 	 *            EObject to attach to a new resource.
 	 * @return The resource <tt>root</tt> has been attached to.
 	 */
 	public static Resource attachResource(URI resourceURI, EObject root) {
 		if (root == null)
 			throw new NullPointerException(EMFCompareMessages.getString("ModelUtils.NullRoot")); //$NON-NLS-1$
 
 		final Resource newResource = createResource(resourceURI);
 		newResource.getContents().add(root);
 		return newResource;
 	}
 
 	/**
 	 * Attaches the given {@link EObject} to a new resource created in the given {@link ResourceSet} with the
 	 * given URI.
 	 * 
 	 * @param resourceURI
 	 *            URI of the new resource to create.
 	 * @param resourceSet
 	 *            ResourceSet in which to create the resource.
 	 * @param root
 	 *            EObject to attach to a new resource.
 	 * @return The resource <tt>root</tt> has been attached to.
 	 */
 	public static Resource attachResource(URI resourceURI, ResourceSet resourceSet, EObject root) {
 		if (root == null)
 			throw new NullPointerException(EMFCompareMessages.getString("ModelUtils.NullRoot")); //$NON-NLS-1$
 
 		final Resource newResource = createResource(resourceURI, resourceSet);
 		newResource.getContents().add(root);
 		return newResource;
 	}
 
 	/**
 	 * This will create a {@link Resource} given the model extension it is intended for.
 	 * 
 	 * @param modelURI
 	 *            {@link org.eclipse.emf.common.util.URI URI} where the model is stored.
 	 * @return The {@link Resource} given the model extension it is intended for.
 	 */
 	public static Resource createResource(URI modelURI) {
 		return createResource(modelURI, new ResourceSetImpl());
 	}
 
 	/**
 	 * This will create a {@link Resource} given the model extension it is intended for and a ResourceSet.
 	 * 
 	 * @param modelURI
 	 *            {@link org.eclipse.emf.common.util.URI URI} where the model is stored.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The {@link Resource} given the model extension it is intended for.
 	 */
 	public static Resource createResource(URI modelURI, ResourceSet resourceSet) {
 		String fileExtension = modelURI.fileExtension();
 		if (fileExtension == null || fileExtension.length() == 0) {
 			fileExtension = Resource.Factory.Registry.DEFAULT_EXTENSION;
 		}
 
 		// First search the resource set for our resource factory
 		Resource.Factory.Registry registry = resourceSet.getResourceFactoryRegistry();
 		Object resourceFactory = registry.getExtensionToFactoryMap().get(fileExtension);
 		if (resourceFactory == null) {
 			// then the global registry
 			registry = Resource.Factory.Registry.INSTANCE;
 			resourceFactory = registry.getExtensionToFactoryMap().get(fileExtension);
 			if (resourceFactory != null) {
 				resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 						resourceFactory);
 			}
 		}
 
 		return resourceSet.createResource(modelURI);
 	}
 
 	/**
 	 * This will try and find the common file extension for the compared models.
 	 * 
 	 * @param uris
 	 *            The resource URIs that will be compared.
 	 * @return The file extension to consider when searching for a match engine or <code>null</code> if file
 	 *         extensions are distinct.
 	 */
 	public static String getCommonExtension(URI... uris) {
 		String extension = null;
 		for (int i = 0; i < uris.length; i++) {
 			if (uris[i] != null) {
 				if (extension == null) {
 					extension = uris[i].fileExtension();
 				} else if (uris[i].fileExtension() != null && !extension.equals(uris[i].fileExtension())) {
 					return null;
 				}
 			}
 		}
 		return extension;
 	}
 
 	/**
 	 * This will try to find the common namespace of the given resources.
 	 * 
 	 * @param resources
 	 *            The resources that will be compared.
 	 * @return The namespace to consider when searching for a match engine or <code>null</code> if namespaces
 	 *         are distinct.
 	 * @since 1.1
 	 */
 	public static String getCommonNamespace(Resource... resources) {
 		String namespace = null;
 		for (int i = 0; i < resources.length; i++) {
 			if (resources[i] != null) {
 				if (!resources[i].getContents().isEmpty()) {
 
 					final EObject rootContainer = EcoreUtil.getRootContainer(resources[i].getContents()
 							.get(0).eClass());
 					if (rootContainer instanceof EPackage) {
 						if (namespace == null) {
 							namespace = ((EPackage)rootContainer).getNsURI();
 						} else if (!namespace.equals(((EPackage)rootContainer).getNsURI())) {
 							return null;
 						}
 					}
 				}
 			}
 		}
 		return namespace;
 	}
 
 	/**
 	 * Loads the models contained by the given directory in the given ResourceSet.
 	 * <p>
 	 * If <code>resourceSet</code> is <code>null</code>, all models will be loaded in a new resourceSet.
 	 * </p>
 	 * 
 	 * @param directory
 	 *            The directory from which to load the models.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in. If <code>null</code>, all models will be
 	 *            loaded in a new resourceSet.
 	 * @return The models contained by the given directory.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted.
 	 */
 	public static List<EObject> getModelsFrom(File directory, ResourceSet resourceSet) throws IOException {
 		return getModelsFrom(directory, null, resourceSet);
 	}
 
 	/**
 	 * Loads the files with the given extension contained by the given directory as EObjects in the given
 	 * ResourceSet.
 	 * <p>
 	 * If <code>resourceSet</code> is <code>null</code>, all models will be loaded in a new resourceSet.
 	 * </p>
 	 * <p>
 	 * The argument <code>extension</code> is in fact the needed suffix for its name in order for a file to be
 	 * loaded. If it is equal to &quot;rd&quot;, a file named &quot;model.aird&quot; will be loaded, but so
 	 * would be a file named &quot;Shepherd&quot;.
 	 * </p>
 	 * <p>
 	 * The empty String or <code>null</code> will result in all the files of the given directory to be loaded,
 	 * and would then be equivalent to {@link #getModelsFrom(File)}.
 	 * </p>
 	 * 
 	 * @param directory
 	 *            The directory from which to load the models.
 	 * @param extension
 	 *            File extension of the files to load. If <code>null</code>, will consider all extensions.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in. If <code>null</code>, all models will be
 	 *            loaded in a new resourceSet.
 	 * @return The models contained by the given directory.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted.
 	 */
 	public static List<EObject> getModelsFrom(File directory, String extension, ResourceSet resourceSet)
 			throws IOException {
 		final List<EObject> models = new ArrayList<EObject>();
 		final String fileExtension;
 		if (extension != null)
 			fileExtension = extension;
 		else
 			fileExtension = ""; //$NON-NLS-1$
 
 		final ResourceSet theResourceSet;
 		if (resourceSet == null)
 			theResourceSet = new ResourceSetImpl();
 		else
 			theResourceSet = resourceSet;
 
 		if (directory.exists() && directory.isDirectory() && directory.listFiles() != null) {
 			final File[] files = directory.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				final File aFile = files[i];
 
 				if (!aFile.isDirectory() && aFile.getName().matches("[^.].*?\\Q" + fileExtension + "\\E")) { //$NON-NLS-1$ //$NON-NLS-2$
 					models.add(load(aFile, theResourceSet));
 				}
 			}
 		}
 
 		return models;
 	}
 
 	/**
 	 * Loads a model from a {@link java.io.File File} in a given {@link ResourceSet}.
 	 * <p>
 	 * This will return the first root of the loaded model, other roots can be accessed via the resource's
 	 * content.
 	 * </p>
 	 * 
 	 * @param file
 	 *            {@link java.io.File File} containing the model to be loaded.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the file.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(File file, ResourceSet resourceSet) throws IOException {
 		return load(URI.createFileURI(file.getPath()), resourceSet);
 	}
 
 	/**
 	 * Load a model from an {@link java.io.InputStream InputStream} in a given {@link ResourceSet}.
 	 * <p>
 	 * This will return the first root of the loaded model, other roots can be accessed via the resource's
 	 * content.
 	 * </p>
 	 * 
 	 * @param stream
 	 *            The inputstream to load from
 	 * @param fileName
 	 *            The original filename
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The loaded model
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(InputStream stream, String fileName, ResourceSet resourceSet)
 			throws IOException {
 		if (stream == null)
 			throw new NullPointerException(EMFCompareMessages.getString("ModelUtils.NullInputStream")); //$NON-NLS-1$
 		EObject result = null;
 
 		final Resource modelResource = createResource(URI.createURI(fileName), resourceSet);
 		modelResource.load(stream, Collections.emptyMap());
 		if (modelResource.getContents().size() > 0)
 			result = modelResource.getContents().get(0);
 		return result;
 	}
 
 	/**
 	 * Loads a model from the String representing the location of a model.
 	 * <p>
 	 * This can be called with pathes of the form
 	 * <ul>
 	 * <li><code>/pluginID/path</code></li>
 	 * <li><code>platform:/plugin/pluginID/path</code></li>
 	 * <li><code>platform:/resource/pluginID/path</code></li>
 	 * </ul>
 	 * </p>
 	 * <p>
 	 * This will return the first root of the loaded model, other roots can be accessed via the resource's
 	 * content.
 	 * </p>
 	 * 
 	 * @param path
 	 *            Location of the model.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the path.
 	 * @throws IOException
 	 *             If the path doesn't resolve to a reachable location.
 	 */
 	public static EObject load(String path, ResourceSet resourceSet) throws IOException {
 		if (path == null || "".equals(path)) //$NON-NLS-1$
 			throw new IllegalArgumentException(EMFCompareMessages.getString("ModelUtils.NullPath")); //$NON-NLS-1$
 
 		final EObject result;
 		// path is already defined with a platform scheme
 		if (path.startsWith("platform")) //$NON-NLS-1$
 			result = load(URI.createURI(path), resourceSet);
 		else {
 			EObject temp = null;
 			try {
 				// Will first try and load as if the model is in the plugins
 				temp = load(URI.createPlatformPluginURI(path, true), resourceSet);
 			} catch (IOException e) {
 				// Model wasn't in the plugins, try and load it within the workspace
 				try {
 					temp = load(URI.createPlatformResourceURI(path, true), resourceSet);
 				} catch (IOException ee) {
 					// Silently discarded, will fail later on
 				}
 			}
 			result = temp;
 		}
 		if (result == null)
 			throw new IOException(EMFCompareMessages.getString("ModelUtils.LoadFailure", path)); //$NON-NLS-1$
 		return result;
 	}
 
 	/**
 	 * Loads a model from an {@link org.eclipse.emf.common.util.URI URI} in a given {@link ResourceSet}.
 	 * <p>
 	 * This will return the first root of the loaded model, other roots can be accessed via the resource's
 	 * content.
 	 * </p>
 	 * 
 	 * @param modelURI
 	 *            {@link org.eclipse.emf.common.util.URI URI} where the model is stored.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the URI.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(URI modelURI, ResourceSet resourceSet) throws IOException {
 		EObject result = null;
 
 		final Resource modelResource = createResource(modelURI, resourceSet);
 		modelResource.load(Collections.emptyMap());
 		if (modelResource.getContents().size() > 0)
 			result = modelResource.getContents().get(0);
 		return result;
 	}
 
 	/**
 	 * Saves a model as a file to the given path.
 	 * 
 	 * @param root
 	 *            Root of the objects to be serialized in a file.
 	 * @param path
 	 *            File where the objects have to be saved.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted during the saving process.
 	 */
 	public static void save(EObject root, String path) throws IOException {
 		if (root == null)
 			throw new NullPointerException(EMFCompareMessages.getString("ModelUtils.NullSaveRoot")); //$NON-NLS-1$
 
 		final Resource newModelResource = createResource(URI.createFileURI(path));
 		newModelResource.getContents().add(root);
 		final Map<String, String> options = new EMFCompareMap<String, String>();
 		options.put(XMLResource.OPTION_ENCODING, System.getProperty(ENCODING_PROPERTY));
 		newModelResource.save(options);
 	}
 
 	/**
 	 * Serializes the given EObjet as a String.
 	 * 
 	 * @param root
 	 *            Root of the objects to be serialized.
 	 * @return The given EObjet serialized as a String.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted during the saving process.
 	 */
 	public static String serialize(EObject root) throws IOException {
 		if (root == null)
 			throw new NullPointerException(EMFCompareMessages.getString("ModelUtils.NullSaveRoot")); //$NON-NLS-1$
 
 		// Copies the root to avoid modifying it
 		final EObject copyRoot = EcoreUtil.copy(root);
 		attachResource(URI.createFileURI("resource.xml"), copyRoot); //$NON-NLS-1$
 
 		final StringWriter writer = new StringWriter();
 		final Map<String, String> options = new EMFCompareMap<String, String>();
 		options.put(XMLResource.OPTION_ENCODING, System.getProperty(ENCODING_PROPERTY));
 		// Should not throw ClassCast since uri calls for an xml resource
 		((XMLResource)copyRoot.eResource()).save(writer, options);
 		final String result = writer.toString();
 		writer.flush();
 		return result;
 	}
 
 	/**
 	 * Checks whether the given resource contains the given object by searching its complete contents (
 	 * {@link Resource#getAllContents()}).
 	 * 
 	 * @param resource
 	 *            The resource whose contents is to be processed.
 	 * @param eObject
 	 *            The object to be evaluated.
 	 * @return <code>true</code> if the resource contains the eObject, <code>false</code> otherwise.
 	 */
 	public static boolean contains(Resource resource, EObject eObject) {
 		final TreeIterator<EObject> contentsIterator = resource.getAllContents();
 		while (contentsIterator.hasNext()) {
 			if (contentsIterator.next() == eObject) {
 				return true;
 			}
 		}
 		return false;
 	}
 }
