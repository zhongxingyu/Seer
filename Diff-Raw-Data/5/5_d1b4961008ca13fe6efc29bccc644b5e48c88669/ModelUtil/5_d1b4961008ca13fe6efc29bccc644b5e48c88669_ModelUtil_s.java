 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Maximilian Koegel, Edgar Mueller, Otto von Wesendonk - initial API and implementation
  * Johannes Faltermeier - adaptions for independent storage
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.common.model.util;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.ECollections;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.EStructuralFeature.Setting;
 import org.eclipse.emf.ecore.EcoreFactory;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.URIConverter;
 import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.ecore.xmi.XMLResource;
 import org.eclipse.emf.ecore.xmi.impl.XMLParserPoolImpl;
 import org.eclipse.emf.emfstore.common.ESResourceSetProvider;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionElement;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPoint;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESExtensionPointException;
 import org.eclipse.emf.emfstore.common.extensionpoint.ESPriorityComparator;
 import org.eclipse.emf.emfstore.common.model.ESSingletonIdResolver;
 import org.eclipse.emf.emfstore.internal.common.CommonUtil;
 import org.eclipse.emf.emfstore.internal.common.ResourceFactoryRegistry;
 import org.eclipse.emf.emfstore.internal.common.model.AssociationClassElement;
 import org.eclipse.emf.emfstore.internal.common.model.IdEObjectCollection;
 import org.eclipse.emf.emfstore.internal.common.model.ModelElementId;
 import org.eclipse.emf.emfstore.internal.common.model.ModelFactory;
 import org.eclipse.emf.emfstore.internal.common.model.Project;
 import org.eclipse.emf.emfstore.internal.common.model.impl.ESModelElementIdImpl;
 import org.eclipse.emf.emfstore.internal.common.model.impl.IdEObjectCollectionImpl;
 import org.eclipse.emf.emfstore.internal.common.model.impl.ProjectImpl;
 import org.osgi.framework.Bundle;
 
 /**
  * Utility class for ModelElements.
  * 
  * @author koegel
  * @author emueller
  * @author ottovonwesen
  * @author jfaltermeier
  */
 public final class ModelUtil {
 
 	private static final String CLIENT_RESOURCE_SET_PROVIDER_EXT_POINT_ID = "org.eclipse.emf.emfstore.client.resourceSetProvider"; //$NON-NLS-1$
 
 	private static final String SERVER_RESOURCE_SET_PROVIDER_EXT_POINT_ID = "org.eclipse.emf.emfstore.server.resourceSetProvider"; //$NON-NLS-1$
 
 	private static final String SINGLETON_ID_RESOLVER_EXT_POINT_ID = "org.eclipse.emf.emfstore.common.model.singletonIdResolver"; //$NON-NLS-1$
 
 	private static final String IGNORED_DATATYPE_EXT_POINT_ID = "org.eclipse.emf.emfstore.common.model.ignoreDatatype"; //$NON-NLS-1$
 
 	/**
 	 * Constant that may be used in case no checksum computation has taken place.
 	 */
 	public static final long NO_CHECKSUM = -1;
 
 	/**
 	 * URI used to serialize EObject with the model util.
 	 */
 	public static final URI VIRTUAL_URI = URI.createURI("virtualUri"); //$NON-NLS-1$
 
 	private static final String ORG_ECLIPSE_EMF_EMFSTORE_COMMON_MODEL = "org.eclipse.emf.emfstore.common.model"; //$NON-NLS-1$
 	private static final String DISCARD_DANGLING_HREF_ID = "org.eclipse.emf.emfstore.common.discardDanglingHREFs"; //$NON-NLS-1$
 
 	private static IResourceLogger resourceLogger = new IResourceLogger() {
 
 		public void logWarning(String msg) {
 			ModelUtil.logWarning(msg);
 		}
 
 		/**
 		 * {@inheritDoc}
 		 * 
 		 * @see org.eclipse.emf.emfstore.internal.common.model.util.IResourceLogger#logError(java.lang.String)
 		 */
 		public void logError(String msg) {
 			ModelUtil.logError(msg);
 		}
 	};
 
 	/**
 	 * Contains the canonical names of classes which will be ignored.
 	 */
 	private static Set<String> ignoredDataTypes;
 
 	/**
 	 * Contains all ID resolvers for singleton datatypes.
 	 */
 	private static Set<ESSingletonIdResolver> singletonIdResolvers;
 	private static HashMap<Object, Object> resourceLoadOptions;
 	private static HashMap<Object, Object> resourceSaveOptions;
 	private static HashMap<Object, Object> checksumSaveOptions;
 
 	/**
 	 * Private constructor.
 	 */
 	private ModelUtil() {
 		// nothing to do
 	}
 
 	/**
 	 * Creates a ModelElementId object from a string.
 	 * 
 	 * @param id
 	 *            as string
 	 * @return id as object
 	 */
 	public static ModelElementId createModelElementId(String id) {
 		final ModelElementId modelElementId = ModelFactory.eINSTANCE.createModelElementId();
 		modelElementId.setId(id);
 		return modelElementId;
 	}
 
 	/**
 	 * Compares two {@link IdEObjectCollection}s. Order of the model elements at root level
 	 * of a collection does not influence the outcome of this operations
 	 * 
 	 * @param collectionA
 	 *            the first collection
 	 * @param collectionB
 	 *            the second collection
 	 * @return true if the two collections are equal, false otherwise
 	 */
 	public static boolean areEqual(IdEObjectCollection collectionA, IdEObjectCollection collectionB) {
 		long a;
 		long b;
 
 		try {
 			// collections are treated specially because the order
 			// of root elements should not matter
 			a = computeChecksum(collectionA);
 			b = computeChecksum(collectionB);
 		} catch (final SerializationException e) {
 			return false;
 		}
 
 		return a == b;
 	}
 
 	/**
 	 * Copies the given EObject and converts it to a string.
 	 * 
 	 * @param object
 	 *            the eObject
 	 * @return the string representation of the EObject
 	 * @throws SerializationException
 	 *             if a serialization problem occurs
 	 */
 	public static String eObjectToString(EObject object) throws SerializationException {
 
 		if (object == null) {
 			return null;
 		}
 
 		final ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 		resourceSetImpl.setResourceFactoryRegistry(new ResourceFactoryRegistry());
 		final XMIResource res = (XMIResource) resourceSetImpl.createResource(VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 
 		EObject copy;
 		if (object instanceof IdEObjectCollection) {
 			copy = copyIdEObjectCollection((IdEObjectCollection) object, res);
 		} else {
 			copy = copyEObject(ModelUtil.getProject(object), object, res);
 		}
 
 		return copiedEObjectToString(copy, res);
 	}
 
 	/**
 	 * Converts the given {@link EObject} to a string.
 	 * 
 	 * @param copy The copied {@link EObject}.
 	 * @param resource The resource for the {@link EObject}.
 	 * @return The string representing the {@link EObject}.
 	 * @throws SerializationException If a serialization problem occurs.
 	 */
 	private static String copiedEObjectToString(EObject copy, XMIResource resource) throws SerializationException {
 		final int step = 200;
 		final int initialSize = step;
 		resource.getContents().add(copy);
 
 		final StringWriter stringWriter = new StringWriter(initialSize);
 		final URIConverter.WriteableOutputStream uws =
 			new URIConverter.WriteableOutputStream(stringWriter, CommonUtil.getEncoding());
 
 		try {
 			resource.save(uws, getChecksumSaveOptions());
 		} catch (final IOException e) {
 			throw new SerializationException(e);
 		}
 
		return stringWriter.toString();
 	}
 
 	/**
 	 * Computes the checksum for a given string representing an {@link EObject}.
 	 * 
 	 * @param eObjectString
 	 *            the string representing the {@link EObject}.
 	 * @return the computed checksum
 	 * 
 	 * @throws SerializationException
 	 *             in case any errors occur during computation of the checksum
 	 */
 	private static long computeChecksum(String eObjectString) {
 		long h = 1125899906842597L; // prime
 		final int len = eObjectString.length();
 
 		for (int i = 0; i < len; i++) {
 			final char c = eObjectString.charAt(i);
 
 			h = 31 * h + c;
 
 		}
 
 		return h;
 	}
 
 	/**
 	 * Computes the checksum for a given {@link EObject}.
 	 * 
 	 * @param eObject
 	 *            the EObject for which to compute a checksum
 	 * @return the computed checksum
 	 * 
 	 * @throws SerializationException
 	 *             in case any errors occur during computation of the checksum
 	 */
 	public static long computeChecksum(EObject eObject) throws SerializationException {
 		return computeChecksum(eObjectToString(eObject));
 	}
 
 	/**
 	 * Computes the checksum for a given {@link IdEObjectCollection}.
 	 * The checksum for a collection is independent of the order of the
 	 * collection's elements at the root level.
 	 * 
 	 * @param collection
 	 *            the collection for which to compute a checksum
 	 * @return the computed checksum
 	 * 
 	 * @throws SerializationException
 	 *             in case any errors occur during computation of the checksum
 	 */
 	public static long computeChecksum(IdEObjectCollection collection) throws SerializationException {
 
 		final ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 		// TODO: do we need to instantiate the factory registry each time?
 		resourceSetImpl.setResourceFactoryRegistry(new ResourceFactoryRegistry());
 		final XMIResource res = (XMIResource) resourceSetImpl.createResource(VIRTUAL_URI);
 		((ResourceImpl) res).setIntrinsicIDToEObjectMap(new HashMap<String, EObject>());
 		final IdEObjectCollection copy = copyIdEObjectCollection(collection, res);
 
 		ECollections.sort(copy.getModelElements(), new Comparator<EObject>() {
 			public int compare(EObject o1, EObject o2) {
 				return copy.getModelElementId(o1).getId().compareTo(copy.getModelElementId(o2).getId());
 			}
 		});
 
		final String serialized = copiedEObjectToString(copy, res).trim();
 
 		return computeChecksum(serialized);
 	}
 
 	/**
 	 * Returns the resource logger.
 	 * 
 	 * @return the resource logger
 	 */
 	public static IResourceLogger getResourceLogger() {
 		return resourceLogger;
 	}
 
 	/**
 	 * Copies the given {@link IdEObjectCollection} and writes the IDs it contains into the given {@link XMIResource}.
 	 * 
 	 * @param collection
 	 *            the collection to be copied
 	 * @param res
 	 *            the resource into which the collection's IDs should be written into
 	 * @return the copied collection
 	 */
 	public static IdEObjectCollection copyIdEObjectCollection(IdEObjectCollection collection, XMIResource res) {
 		final IdEObjectCollection copiedCollection = clone(collection);
 
 		for (final EObject modelElement : copiedCollection.getAllModelElements()) {
 			if (isIgnoredDatatype(modelElement)) {
 				continue;
 			}
 			final ModelElementId modelElementId = copiedCollection.getModelElementId(modelElement);
 			res.setID(modelElement, modelElementId.getId());
 		}
 
 		for (final EObject modelElement : ((Project) copiedCollection).getCutElements()) {
 			if (isIgnoredDatatype(modelElement)) {
 				continue;
 			}
 			final ModelElementId modelElementId = ((IdEObjectCollectionImpl) copiedCollection)
 				.getModelElementId(modelElement);
 			res.setID(modelElement, modelElementId.getId());
 		}
 
 		return copiedCollection;
 	}
 
 	// TODO: javadoc
 	private static EObject copyEObject(IdEObjectCollection collection, EObject object, XMIResource res) {
 		final IdEObjectCollection copiedCollection = copyIdEObjectCollection(collection, res);
 		final EObject copiedEObject = copiedCollection.getModelElement(collection.getModelElementId(object));
 		return copiedEObject;
 	}
 
 	/**
 	 * Determines whether the type of an EObject is an ignored one.
 	 * 
 	 * @param eObject
 	 *            the EObject which is to be checked
 	 * @return true, if the EObject will be ignored, false otherwise
 	 */
 	public static synchronized boolean isIgnoredDatatype(EObject eObject) {
 
 		if (ignoredDataTypes == null) {
 			ignoredDataTypes = new LinkedHashSet<String>();
 			for (final ESExtensionElement element : new ESExtensionPoint(
 				IGNORED_DATATYPE_EXT_POINT_ID,
 				true).getExtensionElements()) {
 				try {
 					ignoredDataTypes.add(element.getAttribute("type")); //$NON-NLS-1$
 				} catch (final ESExtensionPointException e) {
 				}
 			}
 		}
 
 		return ignoredDataTypes.contains(eObject.eClass().getInstanceClassName());
 	}
 
 	/**
 	 * Delivers a map of options for loading resources. Especially {@link XMLResource#OPTION_DEFER_IDREF_RESOLUTION}
 	 * which speeds up loading
 	 * due to our id based resources.
 	 * 
 	 * @return map of options for {@link XMIResource} or {@link XMLResource}.
 	 */
 	@SuppressWarnings("rawtypes")
 	public static synchronized Map<Object, Object> getResourceLoadOptions() {
 		if (resourceLoadOptions == null) {
 			resourceLoadOptions = new LinkedHashMap<Object, Object>();
 			resourceLoadOptions.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_USE_DEPRECATED_METHODS, Boolean.FALSE);
 			resourceLoadOptions.put(XMLResource.OPTION_USE_PARSER_POOL, new XMLParserPoolImpl());
 			resourceLoadOptions.put(XMLResource.OPTION_USE_XML_NAME_TO_FEATURE_MAP, new HashMap());
 			resourceLoadOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
 			resourceLoadOptions.put(XMLResource.OPTION_ENCODING, CommonUtil.getEncoding());
 		}
 		return resourceLoadOptions;
 	}
 
 	/**
 	 * Delivers a map of mandatory options for saving resources.
 	 * 
 	 * @return map of options for {@link XMIResource} or {@link XMLResource}.
 	 */
 	public static synchronized Map<Object, Object> getResourceSaveOptions() {
 
 		if (resourceSaveOptions == null) {
 
 			resourceSaveOptions = new LinkedHashMap<Object, Object>();
 			resourceSaveOptions.put(XMLResource.OPTION_USE_ENCODED_ATTRIBUTE_STYLE, Boolean.TRUE);
 			resourceSaveOptions.put(XMLResource.OPTION_USE_CACHED_LOOKUP_TABLE, new ArrayList<Object>());
 			resourceSaveOptions.put(XMLResource.OPTION_ENCODING, CommonUtil.getEncoding());
 			resourceSaveOptions.put(XMLResource.OPTION_FLUSH_THRESHOLD, 100000);
 			resourceSaveOptions.put(XMLResource.OPTION_USE_FILE_BUFFER, Boolean.TRUE);
 
 			final ESExtensionPoint extensionPoint = new ESExtensionPoint(DISCARD_DANGLING_HREF_ID);
 			final Boolean discardDanglingHREFs = extensionPoint.getBoolean("value", Boolean.FALSE); //$NON-NLS-1$
 			if (discardDanglingHREFs) {
 				resourceSaveOptions.put(XMLResource.OPTION_PROCESS_DANGLING_HREF,
 					XMLResource.OPTION_PROCESS_DANGLING_HREF_RECORD);
 			}
 
 			logInfo(Messages.ModelUtil_Save_Options_Initialized);
 			for (final Map.Entry<Object, Object> entry : resourceSaveOptions.entrySet()) {
 				logInfo("\t" + entry.getKey() + ": " + entry.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		return resourceSaveOptions;
 	}
 
 	/**
 	 * Delivers a map of options that is used while computing a checksum.
 	 * 
 	 * @return map of options for {@link XMIResource} or {@link XMLResource}.
 	 */
 	public static synchronized Map<Object, Object> getChecksumSaveOptions() {
 
 		if (checksumSaveOptions == null) {
 			final Map<Object, Object> saveOptions = getResourceSaveOptions();
 			saveOptions.put(XMLResource.OPTION_DECLARE_XML, Boolean.FALSE);
 			saveOptions.put(XMLResource.OPTION_FORMATTED, Boolean.FALSE);
 		}
 
 		return checksumSaveOptions;
 	}
 
 	/**
 	 * Saves a given resource and logs any warning and/or errors.
 	 * 
 	 * @param resource
 	 *            the resource to be saved
 	 * @param logger
 	 *            a logger instance which will be used to log warnings and errors on resources
 	 * @throws IOException
 	 *             in case an exception occurs during save
 	 */
 	public static void saveResource(Resource resource, IResourceLogger logger) throws IOException {
 		try {
 			resource.save(ModelUtil.getResourceSaveOptions());
 		} catch (final IOException e) {
 			// rethrow exception
 			throw e;
 		} finally {
 			logWarningsAndErrors(resource, logger);
 		}
 	}
 
 	/**
 	 * Loads a given resource and logs any warning and/or errors.
 	 * 
 	 * @param resource
 	 *            the resource to be loaded
 	 * @param logger
 	 *            a logger instance which will be used to log warnings and errors on resources
 	 * @throws IOException
 	 *             in case an exception occurs during load
 	 */
 	public static void loadResource(Resource resource, IResourceLogger logger) throws IOException {
 		try {
 			resource.load(ModelUtil.getResourceLoadOptions());
 		} catch (final IOException e) {
 			// rethrow exception
 			throw e;
 		} finally {
 			logWarningsAndErrors(resource, logger);
 		}
 	}
 
 	private static void logWarningsAndErrors(Resource resource, IResourceLogger logger) {
 
 		if (resource.getWarnings().size() > 0) {
 			for (final Diagnostic diagnostic : resource.getErrors()) {
 				logger.logWarning(logDiagnostic(diagnostic).toString());
 			}
 		}
 
 		if (resource.getErrors().size() > 0) {
 			for (final Diagnostic diagnostic : resource.getErrors()) {
 				logger.logError(logDiagnostic(diagnostic).toString());
 			}
 		}
 
 	}
 
 	private static StringWriter logDiagnostic(Diagnostic diagnostic) {
 
 		final StringWriter error = new StringWriter();
 		error.append(diagnostic.getLocation() + "\n"); //$NON-NLS-1$
 		error.append(diagnostic.getMessage() + "\n"); //$NON-NLS-1$
 
 		if (diagnostic instanceof Exception) {
 			final StringWriter stringWriter = new StringWriter();
 			final PrintWriter printWriter = new PrintWriter(stringWriter);
 			((Throwable) diagnostic).printStackTrace(printWriter);
 			error.append(stringWriter.toString() + "\n"); //$NON-NLS-1$
 		}
 
 		return error;
 	}
 
 	private static boolean canHaveInstances(EClass eClass) {
 		return !(eClass.isAbstract() || eClass.isInterface());
 	}
 
 	/**
 	 * Recursively goes through model and create a list of all non-Abstract
 	 * classes.
 	 * 
 	 * @param ePackage
 	 *            the package to start with.
 	 * @return list of all non-Abstract model element classes in starting
 	 *         package and its sub-packages
 	 */
 	public static Set<EClass> getNonAbstractMETypes(EPackage ePackage) {
 
 		final Set<EClass> nonAbstractMETypes = new LinkedHashSet<EClass>();
 		final Set<EClass> allMETypes = getAllMETypes(ePackage);
 
 		final Iterator<EClass> iterator = allMETypes.iterator();
 		while (iterator.hasNext()) {
 			final EClass eClass = iterator.next();
 			if (canHaveInstances(eClass)) {
 				nonAbstractMETypes.add(eClass);
 			}
 		}
 
 		return nonAbstractMETypes;
 
 	}
 
 	/**
 	 * Recursively goes through package and returns a list of all EClasses
 	 * inheriting ModelElement (abstract classes and interfaces are also
 	 * include).
 	 * 
 	 * @param ePackage
 	 *            starting package
 	 * @return a list of all EClasses inheriting ModelElement (inclusive
 	 *         abstract classes and interfaces) in starting package and all its
 	 *         sub-packages.
 	 */
 	public static Set<EClass> getAllMETypes(EPackage ePackage) {
 		final Set<EClass> meTypes = new LinkedHashSet<EClass>();
 
 		for (final EObject eObject : ePackage.eContents()) {
 			if (eObject instanceof EClass) {
 				final EClass eClass = (EClass) eObject;
 				meTypes.add(eClass);
 			} else if (eObject instanceof EPackage) {
 				final EPackage eSubPackage = (EPackage) eObject;
 				meTypes.addAll(getAllMETypes(eSubPackage));
 			}
 		}
 
 		return meTypes;
 	}
 
 	/**
 	 * This will add a new entry to error log view of eclipse.
 	 * 
 	 * @param message
 	 *            message
 	 * @param exception
 	 *            exception
 	 * @param statusInt
 	 *            severity. Use one of constants in
 	 *            org.eclipse.core.runtime.Status class.
 	 * @throws LoggedException
 	 */
 	public static void log(String message, Throwable exception, int statusInt) {
 		final Bundle bundle = Platform.getBundle(ORG_ECLIPSE_EMF_EMFSTORE_COMMON_MODEL);
 		if (bundle == null) {
 			return;
 		}
 		final Status status = new Status(statusInt, bundle.getSymbolicName(), statusInt, message, exception);
 		Platform.getLog(bundle).log(status);
 	}
 
 	/**
 	 * Log an exception to the platform log. This will create a popup in the ui.
 	 * 
 	 * @param message
 	 *            the message
 	 * @param exception
 	 *            the exception
 	 */
 	public static void logException(String message, Throwable exception) {
 		log(message, exception, IStatus.ERROR);
 	}
 
 	/**
 	 * Log an exception to the platform log. This will create a popup in the ui.
 	 * 
 	 * @param exception
 	 *            the exception
 	 */
 	public static void logException(Throwable exception) {
 		logException(exception.getMessage(), exception);
 	}
 
 	/**
 	 * Log a warning to the platform log. This will NOT create a popup in the UI.
 	 * 
 	 * @param message
 	 *            the message
 	 * @param exception
 	 *            the exception
 	 */
 	public static void logWarning(String message, Throwable exception) {
 		log(message, exception, IStatus.WARNING);
 	}
 
 	/**
 	 * Log a warning to the platform log. This will NOT create a popup in the UI.
 	 * 
 	 * @param message
 	 *            the message being logged
 	 */
 	public static void logWarning(String message) {
 		log(message, null, IStatus.WARNING);
 	}
 
 	/**
 	 * Log a error to the platform log. This will NOT create a popup in the UI.
 	 * 
 	 * @param message
 	 *            the message being logged
 	 */
 	public static void logError(String message) {
 		log(message, null, IStatus.ERROR);
 	}
 
 	/**
 	 * Log an exception to the platform log. This will create a popup in the ui.
 	 * 
 	 * @param message
 	 *            the message
 	 */
 	public static void logInfo(String message) {
 		log(message, null, IStatus.INFO);
 	}
 
 	/**
 	 * Clone any EObject.
 	 * 
 	 * @param <T>
 	 *            the Eobject sub type
 	 * @param eObject
 	 *            the Eobject instance
 	 * @return a clone of the Eobject instance
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends EObject> T clone(T eObject) {
 		if (eObject instanceof ProjectImpl) {
 			return (T) ((ProjectImpl) eObject).copy();
 		}
 		final EObject clone = EcoreUtil.copy(eObject);
 		return (T) clone;
 	}
 
 	/**
 	 * Clone a list of EObjects.
 	 * 
 	 * @param <T>
 	 *            the EObject sub type the list consists of
 	 * @param list
 	 *            the list instance
 	 * @return a clone of the list and its contents instance
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends EObject> List<T> clone(List<T> list) {
 		final ArrayList<T> result = new ArrayList<T>();
 		for (final EObject eObject : list) {
 			final T clone = (T) ModelUtil.clone(eObject);
 			result.add(clone);
 		}
 		return result;
 	}
 
 	/**
 	 * Create a flat clone of the list, the list if cloned but ot its content.
 	 * 
 	 * @param <T>
 	 *            the list type parameter
 	 * @param originalList
 	 *            the original list
 	 * @return a flat copy
 	 */
 	public static <T extends EObject> List<T> flatCloneList(List<T> originalList) {
 		final List<T> clonedList = new ArrayList<T>(originalList.size());
 		for (final T element : originalList) {
 			clonedList.add(element);
 		}
 		return clonedList;
 	}
 
 	/**
 	 * Load an EObject from a resource, the resource is supposed to contain only
 	 * one root object of the given EClass type. Type T must match EClass type.
 	 * 
 	 * @param <T>
 	 *            Type of the EObject
 	 * @param eClass
 	 *            the EClass of the EObject
 	 * @param resourceURI
 	 *            the resources URI
 	 * @param checkConstraints
 	 *            whether to perform additional sanity checks. These checks
 	 *            basically try to enforce that a resource contains exactly one
 	 *            object.
 	 * @return the object loaded from the resource
 	 * @throws IOException
 	 *             if loading the object from the resource fails.
 	 */
 	@SuppressWarnings("unchecked")
 	public static <T extends EObject> T loadEObjectFromResource(EClass eClass, URI resourceURI, boolean checkConstraints)
 		throws IOException {
 
 		final ResourceSet resourceSet = getResourceSetForURI(resourceURI);
 
 		Resource resource;
 
 		if (checkConstraints) {
 			resource = resourceSet.getResource(resourceURI, false);
 		} else {
 			resource = resourceSet.getResource(resourceURI, true);
 		}
 
 		loadResource(resource, resourceLogger);
 		final EList<EObject> contents = resource.getContents();
 
 		if (checkConstraints) {
 			if (contents.size() > 1) {
 				throw new IOException(Messages.ModelUtil_Resource_Contains_Multiple_Objects);
 			}
 		}
 
 		if (contents.size() < 1) {
 			throw new IOException(Messages.ModelUtil_Resource_Contains_No_Objects);
 		}
 
 		final EObject eObject = contents.get(0);
 
 		if (eObject instanceof Project && resource instanceof XMIResource) {
 			final XMIResource xmiResource = (XMIResource) resource;
 			final Project project = (Project) eObject;
 			final Map<EObject, String> eObjectToIdMap = new LinkedHashMap<EObject, String>();
 			final Map<String, EObject> idToEObjectMap = new LinkedHashMap<String, EObject>();
 
 			final TreeIterator<EObject> it = project.eAllContents();
 			while (it.hasNext()) {
 				final EObject obj = it.next();
 				final String id = xmiResource.getID(obj);
 				if (id != null) {
 					eObjectToIdMap.put(obj, id);
 					idToEObjectMap.put(id, obj);
 				}
 			}
 
 			project.initMapping(eObjectToIdMap, idToEObjectMap);
 		}
 
 		if (!eClass.isInstance(eObject)) {
 			throw new IOException(Messages.ModelUtil_Resource_Contains_No_Objects_Of_Given_Class);
 		}
 
 		return (T) eObject;
 	}
 
 	private static ResourceSet getResourceSetForURI(URI resourceURI) {
 		ResourceSet resourceSet = null;
 		if (resourceURI != null && resourceURI.scheme().equals("emfstore")) { //$NON-NLS-1$
 			ESExtensionPoint extensionPoint = null;
 			if (resourceURI.authority().equals("workspaces")) { //$NON-NLS-1$
 				extensionPoint = new ESExtensionPoint(CLIENT_RESOURCE_SET_PROVIDER_EXT_POINT_ID,
 					false, new ESPriorityComparator("priority", true)); //$NON-NLS-1$
 			} else {
 				extensionPoint = new ESExtensionPoint(SERVER_RESOURCE_SET_PROVIDER_EXT_POINT_ID,
 					false, new ESPriorityComparator("priority", true)); //$NON-NLS-1$
 			}
 
 			final ESResourceSetProvider resourceSetProvider = extensionPoint
 				.getElementWithHighestPriority().getClass("class", //$NON-NLS-1$
 					ESResourceSetProvider.class);
 
 			if (resourceSetProvider == null) {
 				resourceSet = new ResourceSetImpl();
 			} else {
 				resourceSet = resourceSetProvider.getResourceSet();
 			}
 
 		} else {
 			resourceSet = new ResourceSetImpl();
 		}
 		return resourceSet;
 	}
 
 	/**
 	 * Save a list of EObjects to the resource with the given URI.
 	 * 
 	 * @param eObjects
 	 *            the EObjects to be saved
 	 * @param resourceURI
 	 *            the URI of the resource, which should be used to save the
 	 *            EObjects
 	 * @param options The save options for the resource.
 	 * @throws IOException
 	 *             if saving to the resource fails
 	 */
 	public static void saveEObjectToResource(List<? extends EObject> eObjects, URI resourceURI,
 		Map<Object, Object> options) throws IOException {
 		final ResourceSet resourceSet = new ResourceSetImpl();
 		final Resource resource = resourceSet.createResource(resourceURI);
 		final EList<EObject> contents = resource.getContents();
 
 		for (final EObject eObject : eObjects) {
 			contents.add(eObject);
 			if (eObject instanceof Project && resource instanceof XMIResource) {
 				setXmiIdsOnResource((Project) eObject, (XMIResource) resource);
 			}
 		}
 
 		contents.addAll(eObjects);
 		resource.save(options);
 	}
 
 	/**
 	 * Save a list of EObjects to the resource with the given URI.
 	 * 
 	 * @param eObjects
 	 *            the EObjects to be saved
 	 * @param resourceURI
 	 *            the URI of the resource, which should be used to save the
 	 *            EObjects
 	 * @throws IOException
 	 *             if saving to the resource fails
 	 */
 	public static void saveEObjectToResource(List<? extends EObject> eObjects, URI resourceURI) throws IOException {
 		saveEObjectToResource(eObjects, resourceURI, null);
 	}
 
 	/**
 	 * Set all IDs contained in the project as XMI IDs for the model elements in
 	 * the project.
 	 * 
 	 * @param project
 	 *            a project
 	 * @param xmiResource
 	 *            the resource that will contain the XMI IDs
 	 */
 	public static void setXmiIdsOnResource(Project project, XMIResource xmiResource) {
 		for (final EObject modelElement : project.getAllModelElements()) {
 			final ModelElementId modelElementId = project.getModelElementId(modelElement);
 			xmiResource.setID(modelElement, modelElementId.getId());
 		}
 	}
 
 	/**
 	 * Save an EObject to a resource.
 	 * 
 	 * @param eObject
 	 *            the object
 	 * @param resourceURI
 	 *            the resources URI
 	 * @throws IOException
 	 *             if saving to the resource fails.
 	 */
 	public static void saveEObjectToResource(EObject eObject, URI resourceURI) throws IOException {
 		final ArrayList<EObject> list = new ArrayList<EObject>();
 		list.add(eObject);
 		saveEObjectToResource(list, resourceURI);
 	}
 
 	/**
 	 * Deletes all resources from resourceSet, which string representation of URI starts with prefix.
 	 * 
 	 * @param resourceSet resource set
 	 * @param prefix string prefix of the resource path
 	 * @throws IOException if deleting the resource fails
 	 */
 	public static void deleteResourcesWithPrefix(ResourceSet resourceSet, String prefix) throws IOException {
 		final List<Resource> toDelete = new ArrayList<Resource>();
 		for (final Resource resource : resourceSet.getResources()) {
 			if (resource.getURI().toFileString().startsWith(prefix)) {
 				toDelete.add(resource);
 			}
 		}
 		for (final Resource resource : toDelete) {
 			resource.delete(null);
 		}
 	}
 
 	/**
 	 * Get Project that contains a model element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @return the project or null if the element is not contained in a project.
 	 */
 	public static Project getProject(EObject modelElement) {
 		final Set<EObject> seenModelElements = new LinkedHashSet<EObject>();
 		seenModelElements.add(modelElement);
 		return getParent(Project.class, modelElement, seenModelElements);
 	}
 
 	/**
 	 * Searches for the project and then looks for the modelelement id.
 	 * 
 	 * @param modelElement me
 	 * @return id
 	 */
 	public static ModelElementId getModelElementId(EObject modelElement) {
 		final Project project = getProject(modelElement);
 		if (project == null) {
 			return null;
 		}
 		return project.getModelElementId(modelElement);
 	}
 
 	/**
 	 * Get the EContainer that contains the given model element and whose {@code eContainer} is {@code null}.
 	 * 
 	 * @param parent
 	 *            the class of the parent
 	 * @param child
 	 *            the model element whose container should get returned
 	 * @param <T> type of the parent class
 	 * @return the container
 	 */
 	public static <T extends EObject> T getParent(Class<T> parent, EObject child) {
 		final Set<EObject> seenModelElements = new LinkedHashSet<EObject>();
 		seenModelElements.add(child);
 		return getParent(parent, child, seenModelElements);
 	}
 
 	@SuppressWarnings("unchecked")
 	private static <T extends EObject> T getParent(Class<T> parent, EObject child, Set<EObject> seenModelElements) {
 		if (child == null) {
 			return null;
 		}
 
 		if (seenModelElements.contains(child.eContainer())) {
 			throw new IllegalStateException(Messages.ModelUtil_ModelElement_Is_In_Containment_Cycle);
 		}
 
 		if (parent.isInstance(child)) {
 			return (T) child;
 		}
 
 		seenModelElements.add(child);
 		return getParent(parent, child.eContainer(), seenModelElements);
 	}
 
 	/**
 	 * Whether a {@link EClass} is a association class. Association classes are
 	 * not displayed as dedicated elements. A link from one element to another
 	 * which goes over an association class is displayed by a dedicated widget.
 	 * This widgets allows to trace transparently without seeing the association
 	 * class.
 	 * 
 	 * @param eClazz
 	 *            the {@link EClass}
 	 * @return if it is an association
 	 */
 	public static boolean isAssociationClassElement(EClass eClazz) {
 		if (eClazz == null || eClazz.isAbstract() || eClazz.getInstanceClass() == null) {
 			return false;
 		}
 		return AssociationClassElement.class.isAssignableFrom(eClazz.getInstanceClass());
 	}
 
 	/**
 	 * Get all contained elements of a given element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @return a set of contained model elements
 	 */
 	public static Set<EObject> getAllContainedModelElements(EObject modelElement, boolean includeTransientContainments) {
 		return getAllContainedModelElements(modelElement, includeTransientContainments, false);
 	}
 
 	/**
 	 * Get all contained elements of a given element.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @param ignoreSingletonDatatypes
 	 *            whether to ignore singleton datatypes like, for example,
 	 *            EString
 	 * @return a set of contained model elements
 	 */
 	public static Set<EObject> getAllContainedModelElements(EObject modelElement, boolean includeTransientContainments,
 		boolean ignoreSingletonDatatypes) {
 		return getAllContainedModelElements(Collections.singletonList(modelElement), includeTransientContainments,
 			ignoreSingletonDatatypes);
 	}
 
 	/**
 	 * Get all contained elements of a given resource.
 	 * 
 	 * @param resource
 	 *            the resource
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @param ignoreSingletonDatatypes
 	 *            whether to ignore singleton datatypes like, for example,
 	 *            EString
 	 * @return a set of contained model elements
 	 *         Get all
 	 */
 	public static Set<EObject> getAllContainedModelElements(Resource resource, boolean includeTransientContainments,
 		boolean ignoreSingletonDatatypes) {
 		return getAllContainedModelElements(resource.getContents(), includeTransientContainments,
 			ignoreSingletonDatatypes);
 	}
 
 	/**
 	 * Get all contained elements of a given collection of model elements.
 	 * 
 	 * @param modelElements
 	 *            a collection of elements
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @param ignoreSingletonDatatypes
 	 *            whether to ignore singleton datatypes like, for example,
 	 *            EString
 	 * @return a set of contained model elements
 	 */
 	public static Set<EObject> getAllContainedModelElements(Collection<EObject> modelElements,
 		boolean includeTransientContainments, boolean ignoreSingletonDatatypes) {
 
 		final Set<EObject> result = new LinkedHashSet<EObject>();
 
 		for (final EObject modelElement : modelElements) {
 			for (final EObject containee : modelElement.eContents()) {
 
 				if (!ignoreSingletonDatatypes && isSingleton(containee)) {
 					continue;
 				}
 
 				if (!containee.eContainingFeature().isTransient() || includeTransientContainments) {
 					final Set<EObject> elements = getAllContainedModelElements(containee, includeTransientContainments,
 						ignoreSingletonDatatypes);
 					result.add(containee);
 					result.addAll(elements);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Get the container of an EObject.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @return the container
 	 */
 	public static EObject getContainerModelElement(EObject modelElement) {
 		final EObject container = modelElement.eContainer();
 		if (container == null) {
 			return null;
 		}
 		if (EcoreFactory.eINSTANCE.getEcorePackage().getEObject().isInstance(container)) {
 			return container;
 		}
 		return null;
 	}
 
 	/**
 	 * Get all contained elements of a given element as a list.
 	 * 
 	 * @param modelElement
 	 *            the model element
 	 * @param includeTransientContainments
 	 *            true if transient containments should be included in the
 	 *            result
 	 * @return a list of contained model elements
 	 */
 	public static List<EObject> getAllContainedModelElementsAsList(EObject modelElement,
 		boolean includeTransientContainments) {
 
 		final TreeIterator<EObject> it = modelElement.eAllContents();
 
 		final List<EObject> result = new ArrayList<EObject>();
 		while (it.hasNext()) {
 			final EObject containee = it.next();
 			if (containee.eContainingFeature() != null && !containee.eContainingFeature().isTransient()
 				|| includeTransientContainments) {
 				result.add(containee);
 			}
 		}
 
 		return result;
 	}
 
 	/**
 	 * Delete the given incoming cross references to the given model element from any
 	 * other model element in the given project.
 	 * 
 	 * @param inverseReferences a collection of inverse references
 	 * @param modelElement
 	 *            the model element
 	 */
 	public static void deleteIncomingCrossReferencesFromParent(Collection<Setting> inverseReferences,
 		EObject modelElement) {
 		for (final Setting setting : inverseReferences) {
 			final EStructuralFeature eStructuralFeature = setting.getEStructuralFeature();
 			final EReference reference = (EReference) eStructuralFeature;
 
 			if (reference.isContainer() || reference.isContainment() || !reference.isChangeable()) {
 				continue;
 			}
 
 			final EObject opposite = setting.getEObject();
 
 			if (eStructuralFeature.isMany()) {
 				((EList<?>) opposite.eGet(eStructuralFeature)).remove(modelElement);
 			} else {
 				if (opposite instanceof Map.Entry<?, ?> && eStructuralFeature.getName().equals("key")) { //$NON-NLS-1$
 					logWarning(MessageFormat.format(
 						Messages.ModelUtil_Incoming_CrossRef_Is_Map_Key, modelElement));
 				}
 
 				opposite.eUnset(eStructuralFeature);
 			}
 		}
 	}
 
 	/**
 	 * Delete all outgoing cross references of the given model element to any element in the given collection.
 	 * 
 	 * @param collection the collection
 	 * @param modelElement
 	 *            the model element
 	 */
 	public static void deleteOutgoingCrossReferences(IdEObjectCollection collection, EObject modelElement) {
 		final Set<EObject> allModelElements = new LinkedHashSet<EObject>();
 		allModelElements.add(modelElement);
 		allModelElements.addAll(ModelUtil.getAllContainedModelElements(modelElement, false));
 
 		final List<SettingWithReferencedElement> crossReferences = collectOutgoingCrossReferences(collection,
 			allModelElements);
 		for (final SettingWithReferencedElement settingWithReferencedElement : crossReferences) {
 			final Setting setting = settingWithReferencedElement.getSetting();
 			if (!settingWithReferencedElement.getSetting().getEStructuralFeature().isMany()) {
 				setting.getEObject().eUnset(setting.getEStructuralFeature());
 			} else {
 				final List<?> references = (List<?>) setting.getEObject().eGet(setting.getEStructuralFeature());
 				references.remove(settingWithReferencedElement.getReferencedElement());
 			}
 		}
 	}
 
 	/**
 	 * Retrieve all outgoing connections from the model elements to other elements in the collection.
 	 * 
 	 * @param collection the collection
 	 * @param modelElements the model elements
 	 * @return a List of references
 	 */
 	public static List<SettingWithReferencedElement> collectOutgoingCrossReferences(IdEObjectCollection collection,
 		Set<EObject> modelElements) {
 		// result object
 		final List<SettingWithReferencedElement> settings = new ArrayList<SettingWithReferencedElement>();
 
 		for (final EObject currentElement : modelElements) {
 
 			for (final EReference reference : currentElement.eClass().getEAllReferences()) {
 				final EClassifier eType = reference.getEType();
 				// sanity checks
 				if (reference.isContainer() || reference.isContainment() || !reference.isChangeable()
 					|| !(eType instanceof EClass)) {
 					continue;
 				}
 
 				final Setting setting = ((InternalEObject) currentElement).eSetting(reference);
 
 				// multi references
 				if (reference.isMany()) {
 					@SuppressWarnings("unchecked")
 					final List<EObject> referencedElements = (List<EObject>) currentElement.eGet(reference);
 					for (final EObject referencedElement : referencedElements) {
 						if (shouldBeCollected(collection, modelElements, referencedElement)) {
 							settings.add(new SettingWithReferencedElement(setting, referencedElement));
 						}
 					}
 				} else {
 					// single references
 
 					final EObject referencedElement = (EObject) currentElement.eGet(reference);
 					if (shouldBeCollected(collection, modelElements, referencedElement)) {
 						settings.add(new SettingWithReferencedElement(setting, referencedElement));
 					}
 
 				}
 			}
 		}
 
 		return settings;
 	}
 
 	/**
 	 * Checks if the referenced elements is an element in the given collection which is not a singleton, not an ignored
 	 * data type and not already contained in the given set of elements.
 	 * 
 	 * @param collection the collection
 	 * @param allModelElements the set of model elements
 	 * @param referencedElement the referenced element
 	 * @return true, if the specified conditions are met.
 	 */
 	public static boolean shouldBeCollected(IdEObjectCollection collection, Set<EObject> allModelElements,
 		EObject referencedElement) {
 
 		if (referencedElement == null) {
 			return false;
 		}
 		if (!collection.contains(referencedElement)
 			&& ((ProjectImpl) collection).getDeletedModelElementId(referencedElement) == null) {
 			return false;
 		}
 
 		return !ModelUtil.isSingleton(referencedElement) && !ModelUtil.isIgnoredDatatype(referencedElement)
 			&& !allModelElements.contains(referencedElement);
 	}
 
 	/**
 	 * Get the singleton instance for a given model element id.
 	 * 
 	 * @param singletonId
 	 *            the id
 	 * @return the singleton instance
 	 */
 	public static EObject getSingleton(ModelElementId singletonId) {
 
 		initSingletonIdResolvers();
 
 		for (final ESSingletonIdResolver resolver : singletonIdResolvers) {
 			final EObject singleton = resolver.getSingleton(singletonId.toAPI());
 			if (singleton != null) {
 				return singleton;
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Get the singleton id for a singleton instance.
 	 * 
 	 * @param singleton
 	 *            the singleton
 	 * @return the id
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESSingletonIdResolver#getSingletonModelElementId(org.eclipse.emf.ecore.EObject)
 	 */
 	public static ModelElementId getSingletonModelElementId(EObject singleton) {
 
 		initSingletonIdResolvers();
 
 		for (final ESSingletonIdResolver resolver : singletonIdResolvers) {
 			final ESModelElementIdImpl id = (ESModelElementIdImpl) resolver.getSingletonModelElementId(singleton);
 			if (id != null) {
 				return clone(id.toInternalAPI());
 			}
 		}
 
 		return null;
 	}
 
 	/**
 	 * Return whether the given eObject instance is a singelton.
 	 * 
 	 * @param eObject
 	 *            the instance
 	 * @return true if it is a singleton
 	 * 
 	 * @see org.eclipse.emf.emfstore.common.model.ESSingletonIdResolver#isSingleton(org.eclipse.emf.ecore.EObject)
 	 */
 	public static boolean isSingleton(EObject eObject) {
 
 		initSingletonIdResolvers();
 
 		for (final ESSingletonIdResolver resolver : singletonIdResolvers) {
 			if (resolver.isSingleton(eObject)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Initializes all available {@link ESSingletonIdResolver}.
 	 */
 	private static synchronized void initSingletonIdResolvers() {
 		if (singletonIdResolvers == null) {
 			// collect singleton ID resolvers
 			singletonIdResolvers = new LinkedHashSet<ESSingletonIdResolver>();
 
 			for (final ESExtensionElement element : new ESExtensionPoint(
 				SINGLETON_ID_RESOLVER_EXT_POINT_ID).getExtensionElements()) {
 				try {
 					singletonIdResolvers.add(element.getClass("class", ESSingletonIdResolver.class)); //$NON-NLS-1$
 				} catch (final ESExtensionPointException e) {
 					ModelUtil.logWarning(Messages.ModelUtil_SingletonIdResolver_Not_Instantiated + e.getMessage());
 				}
 			}
 		}
 	}
 }
