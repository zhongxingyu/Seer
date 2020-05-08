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
 package org.eclipse.graphiti.ui.internal.util.clipboard;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.emf.common.command.CommandStack;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EReference;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.emf.ecore.util.EcoreUtil.Copier;
 import org.eclipse.emf.transaction.RecordingCommand;
 import org.eclipse.emf.transaction.TransactionalEditingDomain;
 import org.eclipse.emf.transaction.util.TransactionUtil;
 import org.eclipse.graphiti.ui.internal.Messages;
 import org.eclipse.graphiti.ui.internal.T;
 import org.eclipse.graphiti.ui.internal.services.GraphitiUiInternal;
 import org.eclipse.graphiti.ui.internal.util.ReflectionUtil;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.dnd.FileTransfer;
 import org.eclipse.swt.dnd.TextTransfer;
 import org.eclipse.swt.dnd.Transfer;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.part.ResourceTransfer;
 
 /**
  * Provides a clipboard-like storage of EMF-related data based on SWT
  * {@link Clipboard}.
  * 
  * @noinstantiate This class is not intended to be instantiated by clients.
  * @noextend This class is not intended to be subclassed by clients.
  */
 public final class ModelClipboard {
 
 	private static final String EMPTY_TOSTRING_PLACEHOLDER = "<empty>"; //$NON-NLS-1$
 	private static final EObject[] NO_E_OBJECTS = new EObject[0];
 	private static final ModelClipboard INSTANCE = new ModelClipboard();
 
 	/**
 	 * Creates a {@link ModelClipboard}.
 	 */
 	private ModelClipboard() {
 	}
 
 	/**
 	 * @return the default {@link ModelClipboard} instance to represent a global
 	 *         {@link ModelClipboard} to the user, which is connected to the SWT
 	 *         {@link Clipboard}.
 	 */
 	public static ModelClipboard getDefault() {
 		return INSTANCE;
 	}
 
 	/**
 	 * Sets the content of the {@link Clipboard} and deletes all previous data.
 	 * Must be called in the UI thread.
 	 * 
 	 * @param objects
 	 *            the {@link EObject} <code>objects</code> to store
 	 * @throws IllegalStateException
 	 *             if not called from UI thread
 	 * @throws IllegalArgumentException
 	 *             if <code>objects</code> parameter is null
 	 */
 	public synchronized void setContent(EObject[] objects) throws IllegalStateException {
 		if (objects == null) {
 			throw new IllegalArgumentException("EObject[] objects must not be null"); //$NON-NLS-1$
 		}
 		if (objects.length == 0) {
 			return;
 		}
 		if (canUseNative()) {
 			// must run in UI thread
 			setNativeContentObjects(Arrays.asList(objects));
 		}
 	}
 
 	/**
 	 * Returns the SWT {@link Clipboard} content in form of {@link EObject}s.
 	 * 
 	 * @param resourceSet
 	 *            the ResourceSet to resolve the stored URI information
 	 * @return the content as live objects
 	 * @throws IllegalStateException
 	 *             if not called from UI thread
 	 * @throws IllegalArgumentException
 	 *             if <code>resourceSet</code> parameter is null
 	 */
 	public synchronized EObject[] getContentAsEObjects(ResourceSet resourceSet) throws IllegalStateException {
 		if (resourceSet == null) {
 			throw new IllegalArgumentException("ResourceSet resourceSet must not be null"); //$NON-NLS-1$
 		}
 		final List<String> uriStrings;
 		if (canUseNative()) {
 			uriStrings = getNativeContent();
 		} else {
 			uriStrings = Collections.emptyList();
 		}
 		if (uriStrings.isEmpty()) {
 			return NO_E_OBJECTS;
 		}
 		final List<EObject> result = getObjectsFromUri(uriStrings, resourceSet, EObject.class);
 		return result.toArray(new EObject[result.size()]);
 	}
 
 	/**
 	 * Answers whether at least one of the given objects can be aggregated below
 	 * the given parent as composite children. This generic implementation
 	 * considers type compatibility and cardinalities but no additional domain
 	 * specific constraints.
 	 * 
 	 * @param parent
 	 *            the composite parent
 	 * @param objects
 	 *            the objects to check
 	 * @return <code>true</code> if at least one object may be a composite child
 	 *         of <code>parent</code>
 	 */
 	public boolean isCompositionAllowed(EObject parent, EObject[] objects) {
 		for (final EObject object : objects) {
 			// optimistic approach: if one association can aggregate one
 			// element, we are OK
 			final List<EReference> assocs = findUsableTargetAssociations(parent, object);
 			if (assocs.size() > 0) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Duplicates the clipboard's content using EMF's deep copy service. Note
 	 * that only elements from the content that are {@link EObject}s are
 	 * considered, pure {@link EObject}s like packages cannot be duplicated.
 	 * 
 	 * @param target
 	 *            an object acting as composite parent for the copies.
 	 *            <code>null</code> if the copied elements should be top-level
 	 *            elements.
 	 * @param transactionalEditingDomain
 	 *            the TransactionalEditingDomain to write the copies into. Must
 	 *            not be <code>null</code> nor dead.
 	 * @return the copy result or <code>null</code> in case of an empty
 	 *         clipboard
 	 * @throws IllegalStateException
 	 *             if not called from UI thread
 	 * @throws IllegalArgumentException
 	 *             if <code>transactionalEditingDomain</code> parameter is null
 	 * @throws IllegalArgumentException
 	 *             if <code>transactionalEditingDomain</code> parameter is not
 	 *             equal to the TransactionalEditingDomain of
 	 *             <code>target</code> parameter
 	 * @see #isCompositionAllowed(EObject, EObject[])
 	 * @see #getContentAsEObjects(ResourceSet)
 	 */
 	@SuppressWarnings("unchecked")
 	public Collection<EObject> duplicateAndPaste(final Object target, TransactionalEditingDomain transactionalEditingDomain)
 			throws IllegalStateException {
 
 		if (transactionalEditingDomain == null) {
 			throw new IllegalStateException("TransactionalEditingDomain targetConnection should not be null"); //$NON-NLS-1$
 		}
 		final EObject parent = GraphitiUiInternal.getEmfService().getEObject(target);
 		if (parent != null) {
 			// detect TransactionalEditingDomain mismatch to prevent
 			// modification of wrong object
 			final TransactionalEditingDomain parentEditingDomain = TransactionUtil.getEditingDomain(parent);
 			if (parentEditingDomain != null && !transactionalEditingDomain.equals(parentEditingDomain)) {
 				throw new IllegalStateException(
 						"Ambiguous TransactionalEditingDomains: transactionalEditingDomain: " + transactionalEditingDomain //$NON-NLS-1$
 								+ " <-> TransactionalEditingDomain of target object: " + parentEditingDomain //$NON-NLS-1$
 								+ ". Not clear which one to use for copy."); //$NON-NLS-1$
 			}
 		}
 
 		EObject[] srcObjects;
 		try {
 			srcObjects = getContentAsEObjects(transactionalEditingDomain.getResourceSet());
 			if (srcObjects.length == 0) {
 				return null; // no or no resolvable objects in clipboard
 			}
 
 		} catch (final OperationCanceledException e) { // $JL-EXC$
 			return null;
 		} catch (final Exception e) { // $JL-EXC$
 			T.racer().error(e.getMessage(), e);
 			return null;
 		}
 
 		// subsequent operations run in a command and are rolled back in case of
 		// errors
 		final CommandStack commandStack = transactionalEditingDomain.getCommandStack();
 		final Collection<EObject>[] copyResults = new Collection[1];
 		try {
 			final EObject[] srcObjectsFinal = srcObjects;
 			final RecordingCommand command = new CopyCommand(transactionalEditingDomain, parent, srcObjectsFinal, copyResults);
 			commandStack.execute(command);
 			// commit
 			return copyResults[0];
 		} catch (final OperationCanceledException e) { // $JL-EXC$
 			// user cancelled
 			rollback(transactionalEditingDomain);
 			return null;
 		} catch (final Exception e) { // $JL-EXC$
 			// unspecific error
 			T.racer().error(e.getMessage(), e);
 			rollback(transactionalEditingDomain);
 			return null;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<EObject> deepCopy(final EObject[] srcObjects) {
 		if (srcObjects == null) {
 			throw new IllegalArgumentException("EObject[] srcObjects must not be null"); //$NON-NLS-1$
 		}
 		if (srcObjects.length == 0) {
 			throw new IllegalArgumentException("EObject[] srcObjects.length must not be 0"); //$NON-NLS-1$
 		}
 		final Collection<EObject>[] result = new Collection[1];
 		// in the case of a UI
 		if (canUseUI()) {
 			BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
 				public void run() {
 					final Copier copier = new Copier(true, true);
 					result[0] = copier.copyAll(Arrays.asList(srcObjects));
 					copier.copyReferences();
 				}
 			});
 			return result[0];
 		}
 
 		// in the non-UI case
 		final Copier copier = new Copier(true, true);
 		result[0] = copier.copyAll(Arrays.asList(srcObjects));
 		copier.copyReferences();
 		return result[0];
 	}
 
 	/**
 	 * Adds the given elements to <code>parent</code> as composite children.s
 	 * 
 	 * @param parent
 	 *            parent to add the objects to. Must not be <code>null</code>.
 	 * @param objects
 	 *            the objects to add
 	 * @param association
 	 *            an explicit association or <code>null</code>
 	 */
 	private void addToCompositeParent(EObject parent, EObject[] objects, EReference association) {
 		if (parent == null) {
 			throw new IllegalStateException("Parent must not be null"); //$NON-NLS-1$
 		}
 
 		for (final EObject object : objects) {
 			final EObject objectParent = object.eContainer();
 			if (objectParent != null) {
 				if (T.racer().debug()) {
 					final String msg = "Ignoring " + toObjectString(object) //$NON-NLS-1$
 							+ " for parent assignment. Already assigned to " + toObjectString(objectParent); //$NON-NLS-1$
 					T.racer().debug(msg);
 				}
 				continue;
 			}
 			// Find the composition relationships to use between parent and
 			// child
 			final List<EReference> assocs = findUsableTargetAssociations(parent, object);
 			EReference assoc;
 
 			switch (assocs.size()) {
 			case 0:
 				final String msg = "No composite associations found for " //$NON-NLS-1$
 						+ toObjectString(parent.eClass()) + " -> " //$NON-NLS-1$
 						+ toObjectString(object.eClass());
 				// Don't issue an error here. The client might already have
 				// composed the objects.
 				T.racer().debug(msg);
 				continue;
 			case 1:
 				assoc = assocs.get(0);
 				break;
 			default: // multiple associations
 				if (association != null) {
 					if (!assocs.contains(association)) {
 						throw new IllegalStateException("Given association " + association.getName() //$NON-NLS-1$
 								+ " not valid among " + toAssociationNames(assocs)); //$NON-NLS-1$
 					}
 					assoc = association;
 					break;
 				} else {
 					throw new IllegalStateException("Multiple associations available " //$NON-NLS-1$
 							+ toAssociationNames(assocs));
 				}
 			}
 
 			// Use the obtained association to compose the child into the parent
 			compose(parent, object, assoc);
 		}
 	}
 
 	private static List<String> toAssociationNames(List<EReference> assocs) {
 		final List<String> names = new ArrayList<String>(assocs.size());
 		for (final EReference assoc : assocs) {
 			names.add(assoc.getName());
 		}
 		return names;
 	}
 
 	private static String toObjectString(Object o) {
 		if (o instanceof Collection<?>) {
 			return toObjectsString((Collection<?>) o);
 		}
 		return toObjectsString(Collections.singleton(o));
 	}
 
 	private static String toObjectsString(Collection<?> objects) {
 		final StringBuilder b = new StringBuilder();
 		for (final Object o : objects) {
 			if (o instanceof EObject) {
 				final EObject object = (EObject) o;
 				final String name = GraphitiUiInternal.getEmfService().getObjectName(object);
 				final String type = object.eClass().getName();
 				b.append(type).append(" '").append(name).append("'"); //$NON-NLS-1$ //$NON-NLS-2$
 			} else {
 				b.append(o);
 			}
 		}
 		return b.toString();
 	}
 
 	/**
 	 * Adds the given child to the given parent in the given association.
 	 */
 	@SuppressWarnings("unchecked")
 	private static void compose(EObject parent, EObject child, EReference assoc) {
 		final Object o = parent.eGet(assoc, true);
 		if (o instanceof List<?>) {
 			final List<EObject> list = (List<EObject>) o;
 			list.add(child);
 		} else {
 			parent.eSet(assoc, child);
 		}
 	}
 
 	/**
 	 * @return all copied elements from <code>result</code> that originate from
 	 *         root elements in the source
 	 */
 	private Map<EObject, EObject> getTargetRootElements(Collection<EObject> result, EObject[] srcElements) {
 		final Map<EObject, EObject> elements = new LinkedHashMap<EObject, EObject>(srcElements.length);
 		EObject[] resultArray = new EObject[result.size()];
 		resultArray = result.toArray(resultArray);
 		for (int i = 0; i < srcElements.length; i++) {
 			elements.put(resultArray[i], srcElements[i]);
 		}
 		return elements;
 	}
 
 	/**
 	 * Finds all composite associations for the given <code>parent</code> object
 	 * that reference a child of type <code>child</code>. Associations that have
 	 * a upper cardinality of '1' are not contained in the returned
 	 * <code>List</code>.
 	 * 
 	 * @param parent
 	 *            the composite parent element.
 	 * @param child
 	 *            the <code>EObject</code> defining the type of the composite
 	 *            children.
 	 * @return the list containing all composite associations that fulfil the
 	 *         selection criteria described above.
 	 */
 	static List<EReference> findUsableTargetAssociations(EObject parent, EObject child) {
 		final List<EReference> compositeAssociations = new ArrayList<EReference>();
 
 		final Collection<EReference> contents = getContainmentReferences(parent.eClass());
 		for (final Iterator<EReference> iterator = contents.iterator(); iterator.hasNext();) {
 			final EReference reference = iterator.next();
 			final EClassifier referenceType = reference.getEType();
 			if (referenceType.isInstance(child)) {
 				final Object value = parent.eGet(reference);
 				if (reference.getUpperBound() != 1 || value == null) {
 					compositeAssociations.add(reference);
 				}
 			}
 
 		}
 		return compositeAssociations;
 	}
 
 	private static Collection<EReference> getContainmentReferences(EClass eclass) {
 		final Collection<EReference> assocs = new ArrayList<EReference>();
 		final EList<EObject> contents = eclass.eContents();
 		for (final Iterator<EObject> iterator = contents.iterator(); iterator.hasNext();) {
 			final EObject object = iterator.next();
 			if (object instanceof EReference) {
 				final EReference reference = (EReference) object;
 				if (reference.isContainment()) {
 					assocs.add(reference);
 				}
 			}
 		}
 
 		final EList<EClass> superTypes = eclass.getESuperTypes();
 		for (final Iterator<EClass> iterator = superTypes.iterator(); iterator.hasNext();) {
 			assocs.addAll(getContainmentReferences(iterator.next()));
 		}
 
 		return assocs;
 	}
 
 	/**
 	 * Reverts the active command group.
 	 */
 	private void rollback(final TransactionalEditingDomain targetTED) {
 		try {
 			targetTED.runExclusive(new Runnable() {
 				public void run() {
 					final EList<Resource> resources = targetTED.getResourceSet().getResources();
 					for (final Iterator<Resource> iterator = resources.iterator(); iterator.hasNext();) {
 						final Resource resource = iterator.next();
 						resource.unload();
 						resource.setModified(false);
 					}
 				}
 			});
 		} catch (final InterruptedException e) {
 		}
 	}
 
 	private synchronized List<String> getContentAsStringList() {
 		List<String> strings = Collections.emptyList();
 		if (canUseNative()) {
 			// must run in UI thread
 			strings = getNativeContent();
 		}
 		return strings;
 	}
 
 	/**
 	 * Sets the content of the SWT {@link clipboard} with the given objects.
 	 */
 	private void setNativeContentObjects(List<EObject> objects) {
 		final Map<Transfer, Object> nativeFormat = toTransferObjects(objects);
 		final int size = nativeFormat.size();
 		if (size > 0) {
 			final Object[] data = nativeFormat.values().toArray(new Object[size]);
 			final Transfer[] dataTypes = nativeFormat.keySet().toArray(new Transfer[size]);
 
 			final Clipboard cb = new Clipboard(Display.getCurrent());
 			try {
 				cb.setContents(data, dataTypes);
 			} finally {
 				cb.dispose();
 			}
 		}
 	}
 
 	/**
 	 * @returns currently the {@link URI} content as {@link UriTransfer},
 	 *          {@link TextTransfer}, {@link FileTransfer}, and Eclipse
 	 *          {@link ResourceTransfer}.
 	 */
 	private synchronized Map<Transfer, Object> toTransferObjects(List<EObject> objects) {
 		final Map<Transfer, Object> empty = Collections.emptyMap();
 		final int size = objects.size();
 		if (size == 0) {
 			return empty;
 		}
 
 		final List<String> uriStrings = new ArrayList<String>(size);
 		final List<IResource> files = new ArrayList<IResource>(size);
 		final List<String> filePaths = new ArrayList<String>(size);
 		for (int i = 0; i < size; i++) {
 			final EObject o = objects.get(i);
 			uriStrings.add(EcoreUtil.getURI(o).toString());
 			final IFile file = GraphitiUiInternal.getEmfService().getFile(o);
 			if (file != null && file.exists() && !files.contains(file)) {
 				files.add(file);
 				filePaths.add(file.getLocation().toOSString());
 			}
 		}
 		final Map<Transfer, Object> result = new HashMap<Transfer, Object>(7);
 		final UriTransferData data = new UriTransferData(uriStrings);
		result.put(LocalSelectionTransfer.getTransfer(), new StructuredSelection(objects));
 		result.put(UriTransfer.getInstance(), data);
 		result.put(FileTransfer.getInstance(), filePaths.toArray(new String[filePaths.size()]));
 		// Resource Transfer resides in org.eclipse.ui.ide. We need to support
 		// an RCP scenario without having this plug-in installed.
 		try {
 			Transfer resourceTransfer = ReflectionUtil.getResourceTransfer();
 			if (resourceTransfer != null)
 				result.put(resourceTransfer, files.toArray(new IResource[files.size()]));
 		} catch (Exception e) {
 			T.racer().debug(e.getMessage());
 		}
 		result.put(TextTransfer.getInstance(), toExtendedString(objects));
 		return result;
 	}
 
 	private static List<String> getNativeContent() {
 		final Clipboard cb = new Clipboard(Display.getCurrent());
 		try {
 			final UriTransferData contents = (UriTransferData) cb.getContents(UriTransfer.getInstance());
 			if (contents != null) {
 				return contents.getUriStrings();
 			}
 			return Collections.emptyList();
 		} finally {
 			cb.dispose();
 		}
 	}
 
 	@SuppressWarnings({ "unchecked", "hiding" })
 	private static <T extends EObject> List<T> getObjectsFromUri(List<String> uriStrings, ResourceSet resourceSet, Class<T> type) {
 		final List<T> result = new ArrayList<T>(uriStrings.size());
 		for (final String uriString : uriStrings) {
 			final URI uri = URI.createURI(uriString);
 			if (resourceSet.getURIConverter().exists(uri, null)) {
 				final EObject element = resourceSet.getEObject(uri, true);
 				if (element != null && type.isInstance(element)) {
 					result.add((T) element);
 				}
 			}
 		}
 		return result;
 	}
 
 	private String toExtendedString(List<EObject> objects) {
 		final StringBuilder result = new StringBuilder();
 		for (int i = 0; i < objects.size(); i++) {
 			final EObject o = objects.get(i);
 			GraphitiUiInternal.getEmfService().toString(o, result);
 			if (i < objects.size() - 1) {
 				result.append(UriTransferData.LINE_SEP);
 			}
 		}
 		return result.toString();
 	}
 
 	@Override
 	public String toString() {
 		if (!(getContentAsStringList().size() > 0)) {
 			return EMPTY_TOSTRING_PLACEHOLDER;
 		}
 		final List<String> content = getContentAsStringList();
 		final String[] strings = content.toArray(new String[content.size()]);
 		final StringBuilder b = new StringBuilder();
 		for (int i = 0; i < strings.length; i++) {
 			final String s = strings[i];
 			b.append(s);
 			if (i < strings.length - 1) {
 				b.append(UriTransferData.LINE_SEP);
 			}
 		}
 		return b.toString();
 	}
 
 	/**
 	 * @return whether the SWT {@link Clipboard} can be accessed at all and in
 	 *         the current thread
 	 */
 	private synchronized boolean canUseNative() {
 		final boolean result = canUseUI();
 		if (!result) {
 			throw new IllegalStateException("ModelClipboard must be called from UI thread."); //$NON-NLS-1$
 		}
 		return result;
 	}
 
 	/**
 	 * @return whether Ui may be raised
 	 */
 	private static boolean canUseUI() {
 		return Display.getCurrent() != null;
 	}
 
 	private final class CopyCommand extends RecordingCommand {
 		private final EObject parent;
 		private final EObject[] srcObjectsFinal;
 		private final Collection<EObject>[] copyResults;
 
 		private CopyCommand(TransactionalEditingDomain domain, EObject parent, EObject[] srcObjectsFinal, Collection<EObject>[] copyResults) {
 			super(domain);
 			this.parent = parent;
 			this.srcObjectsFinal = srcObjectsFinal;
 			this.copyResults = copyResults;
 		}
 
 		@Override
 		public String getLabel() {
 			return Messages.ModelClipBoardPasteAction_0_xfld;
 		}
 
 		@Override
 		protected void doExecute() {
 			// actual copy
 			final Collection<EObject> copyResult = deepCopy(this.srcObjectsFinal);
 			// Process along the root elements only, avoid the effect of a child
 			// element to appear before its parent could be handled.
 			final Map<EObject, EObject> targetRootElements = getTargetRootElements(copyResult, this.srcObjectsFinal);
 			final Set<EObject> targetElements = targetRootElements.keySet();
 
 			final EObject[] elements = targetElements.toArray(new EObject[targetElements.size()]);
 			if (this.parent != null) {
 				addToCompositeParent(this.parent, elements, null);
 			}
 			this.copyResults[0] = copyResult;
 		}
 	}
 }
