 /*******************************************************************************
  * Copyright (c) 2006, 2007 Obeo.
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
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.compare.Messages;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
 
 /**
  * Utility class for model loading/saving and serialization.
  * 
  * @author Cedric Brun <a href="mailto:cedric.brun@obeo.fr">cedric.brun@obeo.fr</a>
  */
 public final class ModelUtils {
 	/** Will determine if the URIs must be encoded depending on the current system. */
 	private static final boolean ENCODE_PLATFORM_RESOURCE_URIS = System
 			.getProperty("org.eclipse.emf.common.util.URI.encodePlatformResourceURIs") != null && //$NON-NLS-1$
 			!System
 					.getProperty("org.eclipse.emf.common.util.URI.encodePlatformResourceURIs").equalsIgnoreCase("false"); //$NON-NLS-1$ //$NON-NLS-2$
 
 	/** Constant for the file encoding system property. */
 	private static final String ENCODING_PROPERTY = "file.encoding"; //$NON-NLS-1$
 
 	/**
 	 * Utility classes don't need to (and shouldn't) be instantiated.
 	 */
 	private ModelUtils() {
 		// prevents instantiation
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
 
 		final Resource.Factory.Registry registry = Resource.Factory.Registry.INSTANCE;
 		final Object resourceFactory = registry.getExtensionToFactoryMap().get(fileExtension);
 		if (resourceFactory != null) {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 					resourceFactory);
 		} else {
 			resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put(fileExtension,
 					new XMIResourceFactoryImpl());
 		}
 
 		return resourceSet.createResource(modelURI);
 	}
 
 	/**
 	 * Loads the models contained by the given directory.
 	 * 
 	 * @param directory
 	 *            The directory from which to load the models.
 	 * @return The models contained by the given directory.
 	 * @throws IOException
 	 *             Thrown if an I/O operation has failed or been interrupted.
 	 */
 	public static List<EObject> getModelsFrom(File directory) throws IOException {
 		final List<EObject> models = new ArrayList<EObject>();
 
 		if (directory.exists() && directory.isDirectory() && directory.listFiles() != null) {
 			final File[] files = directory.listFiles();
 			for (int i = 0; i < files.length; i++) {
 				final File aFile = files[i];
 
 				final ResourceSet resourceSet = new ResourceSetImpl();
 				if (!aFile.isDirectory() && !aFile.getName().startsWith(".")) { //$NON-NLS-1$
 					models.add(load(aFile, resourceSet));
 				}
 			}
 		}
 
 		return models;
 	}
 
 	/**
 	 * Loads a model from a {@link java.io.File File} in a given {@link ResourceSet}.
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
 	 * Loads a model from an {@link org.eclipse.core.resources.IFile IFile} in a given {@link ResourceSet}.
 	 * 
 	 * @param file
 	 *            {@link org.eclipse.core.resources.IFile IFile} containing the model to be loaded.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the file.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	@SuppressWarnings("unchecked")
 	public static EObject load(IFile file, ResourceSet resourceSet) throws IOException {
 		EObject result = null;
 
 		final Resource modelResource = createResource(URI.createPlatformResourceURI(file.getFullPath()
 				.toOSString(), ENCODE_PLATFORM_RESOURCE_URIS), resourceSet);
 		final Map<String, String> options = new EMFCompareMap<String, String>();
 		options.put(XMLResource.OPTION_ENCODING, System.getProperty(ENCODING_PROPERTY));
 		modelResource.load(options);
 		if (modelResource.getContents().size() > 0)
 			result = modelResource.getContents().get(0);
 		return result;
 	}
 
 	/**
 	 * Load a model from an {@link java.io.InputStream  InputStream} in a given {@link ResourceSet}.
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
 	@SuppressWarnings("unchecked")
 	public static EObject load(InputStream stream, String fileName, ResourceSet resourceSet)
 			throws IOException {
 		if (stream == null)
 			throw new NullPointerException(Messages.getString("ModelUtils.NullInputStream")); //$NON-NLS-1$
 		EObject result = null;
 
 		final Resource modelResource = createResource(URI.createURI(fileName), resourceSet);
 		final Map<String, String> options = new EMFCompareMap<String, String>();
 		options.put(XMLResource.OPTION_ENCODING, System.getProperty(ENCODING_PROPERTY));
 		modelResource.load(stream, options);
 		if (modelResource.getContents().size() > 0)
 			result = modelResource.getContents().get(0);
 		return result;
 	}
 
 	/**
 	 * Loads a model from an {@link IPath} in a given {@link ResourceSet}.
 	 * 
 	 * @param path
 	 *            {@link IPath} where the model lies.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the path.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	public static EObject load(IPath path, ResourceSet resourceSet) throws IOException {
 		return load(ResourcesPlugin.getWorkspace().getRoot().getFile(path), resourceSet);
 	}
 
 	/**
 	 * Loads a model from an {@link org.eclipse.emf.common.util.URI URI} in a given {@link ResourceSet}.
 	 * 
 	 * @param modelURI
 	 *            {@link org.eclipse.emf.common.util.URI URI} where the model is stored.
 	 * @param resourceSet
 	 *            The {@link ResourceSet} to load the model in.
 	 * @return The model loaded from the URI.
 	 * @throws IOException
 	 *             If the given file does not exist.
 	 */
 	@SuppressWarnings("unchecked")
 	public static EObject load(URI modelURI, ResourceSet resourceSet) throws IOException {
 		EObject result = null;
 
 		final Resource modelResource = createResource(modelURI, resourceSet);
 		final Map<String, String> options = new EMFCompareMap<String, String>();
 		options.put(XMLResource.OPTION_ENCODING, System.getProperty(ENCODING_PROPERTY));
 		modelResource.load(options);
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
 	@SuppressWarnings("unchecked")
 	public static void save(EObject root, String path) throws IOException {
 		if (root == null)
 			throw new NullPointerException(Messages.getString("ModelUtils.NullSaveRoot")); //$NON-NLS-1$
 
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
 	@SuppressWarnings("unchecked")
 	public static String serialize(EObject root) throws IOException {
 		if (root == null)
 			throw new NullPointerException(Messages.getString("ModelUtils.NullSaveRoot")); //$NON-NLS-1$
 
 		final XMIResourceImpl newResource = new XMIResourceImpl();
 		final StringWriter writer = new StringWriter();
 		newResource.getContents().add(root);
 		newResource.save(writer, Collections.EMPTY_MAP);
 		return writer.toString();
 	}
 }
