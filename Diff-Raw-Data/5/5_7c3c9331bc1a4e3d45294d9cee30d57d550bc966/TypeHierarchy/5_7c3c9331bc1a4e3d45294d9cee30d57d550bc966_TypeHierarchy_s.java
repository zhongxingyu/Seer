 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
 
  *******************************************************************************/
 package org.eclipse.dltk.internal.core.hierarchy;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.ElementChangedEvent;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IElementChangedListener;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementDelta;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ITypeHierarchy;
 import org.eclipse.dltk.core.ITypeHierarchyChangedListener;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.ModelStatus;
 import org.eclipse.dltk.internal.core.Openable;
 import org.eclipse.dltk.internal.core.Region;
 import org.eclipse.dltk.internal.core.ScriptFolder;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.core.SourceModule;
 import org.eclipse.dltk.internal.core.TypeVector;
 import org.eclipse.dltk.internal.core.util.Messages;
 import org.eclipse.dltk.internal.core.util.Util;
 
 /**
  * @see ITypeHierarchy
  */
 public class TypeHierarchy implements ITypeHierarchy, IElementChangedListener {
 
 	public static boolean DEBUG = false;
 
 	static final byte VERSION = 0x0000;
 	// SEPARATOR
 	static final byte SEPARATOR1 = '\n';
 	static final byte SEPARATOR2 = ',';
 	static final byte SEPARATOR3 = '>';
 	static final byte SEPARATOR4 = '\r';
 	// general info
 	static final byte COMPUTE_SUBTYPES = 0x0001;
 
 	// type info
 	static final byte CLASS = 0x0000;
 	static final byte INTERFACE = 0x0001;
 	static final byte COMPUTED_FOR = 0x0002;
 	static final byte ROOT = 0x0004;
 
 	// cst
 	static final byte[] NO_FLAGS = new byte[] {};
 	static final int SIZE = 10;
 
 	/**
 	 * The Java Project in which the hierarchy is being built - this provides
 	 * the context for determining a classpath and namelookup rules. Possibly
 	 * null.
 	 */
 	protected IScriptProject project;
 	/**
 	 * The type the hierarchy was specifically computed for, possibly null.
 	 */
 	protected IType focusType;
 
 	/*
 	 * The working copies that take precedence over original compilation units
 	 */
 	protected ISourceModule[] workingCopies;
 
 	protected Map classToSuperclass;
 	protected Map typeToSubtypes;
 	protected Map typeFlags;
 	protected TypeVector rootClasses = new TypeVector();
 	public ArrayList missingTypes = new ArrayList(4);
 
 	protected static final IType[] NO_TYPE = new IType[0];
 
 	/**
 	 * The progress monitor to report work completed too.
 	 */
 	protected IProgressMonitor progressMonitor = null;
 
 	/**
 	 * Change listeners - null if no one is listening.
 	 */
 	protected ArrayList changeListeners = null;
 
 	/*
 	 * A map from Openables to ArrayLists of ITypes
 	 */
 	public Map files = null;
 
 	/**
 	 * A region describing the packages considered by this hierarchy. Null if
 	 * not activated.
 	 */
 	protected Region packageRegion = null;
 
 	/**
 	 * A region describing the projects considered by this hierarchy. Null if
 	 * not activated.
 	 */
 	protected Region projectRegion = null;
 
 	/**
 	 * Whether this hierarchy should contains subtypes.
 	 */
 	protected boolean computeSubtypes;
 
 	/**
 	 * The scope this hierarchy should restrain itsef in.
 	 */
 	IDLTKSearchScope scope;
 
 	/*
 	 * Whether this hierarchy needs refresh
 	 */
 	public boolean needsRefresh = true;
 
 	/*
 	 * Collects changes to types
 	 */
 	protected ChangeCollector changeCollector;
 
 	/**
 	 * Creates an empty TypeHierarchy
 	 */
 	public TypeHierarchy() {
 		// Creates an empty TypeHierarchy
 	}
 
 	/**
 	 * Creates a TypeHierarchy on the given type.
 	 */
 	public TypeHierarchy(IType type, ISourceModule[] workingCopies,
 			IScriptProject project, boolean computeSubtypes) {
 		this(type, workingCopies, SearchEngine.createSearchScope(project),
 				computeSubtypes);
 		this.project = project;
 	}
 
 	/**
 	 * Creates a TypeHierarchy on the given type.
 	 */
 	public TypeHierarchy(IType type, ISourceModule[] workingCopies,
 			IDLTKSearchScope scope, boolean computeSubtypes) {
 		this.focusType = type == null ? null : (IType) ((ModelElement) type); // unsure
 		// the
 		// focus
 		// type
 		// is
 		// unresolved
 		// (see
 		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=92357)
 		if (DLTKCore.DEBUG) {
 			System.err.println("Bu possible. type should be unresolved..."); //$NON-NLS-1$
 		}
 		this.workingCopies = workingCopies;
 		this.computeSubtypes = computeSubtypes;
 		this.scope = scope;
 	}
 
 	/**
 	 * Initializes the file, package and project regions
 	 */
 	protected void initializeRegions() {
 
 		IType[] allTypes = getAllTypes();
 		for (int i = 0; i < allTypes.length; i++) {
 			IType type = allTypes[i];
 			Openable o = (Openable) ((ModelElement) type).getOpenableParent();
 			if (o != null) {
 				ArrayList types = (ArrayList) this.files.get(o);
 				if (types == null) {
 					types = new ArrayList();
 					this.files.put(o, types);
 				}
 				types.add(type);
 			}
 			IScriptFolder pkg = type.getScriptFolder();
 			this.packageRegion.add(pkg);
 			IScriptProject declaringProject = type.getScriptProject();
 			if (declaringProject != null) {
 				this.projectRegion.add(declaringProject);
 			}
 			checkCanceled();
 		}
 	}
 
 	/**
 	 * Adds all of the elements in the collection to the list if the element is
 	 * not already in the list.
 	 */
 	private void addAllCheckingDuplicates(ArrayList list, IType[] collection) {
 		for (int i = 0; i < collection.length; i++) {
 			IType element = collection[i];
 			if (!list.contains(element)) {
 				list.add(element);
 			}
 		}
 	}
 
 	/**
 	 * Adds the type to the collection of root classes if the classes is not
 	 * already present in the collection.
 	 */
 	protected void addRootClass(IType type) {
 		if (this.rootClasses.contains(type)) {
 			return;
 		}
 		this.rootClasses.add(type);
 	}
 
 	/**
 	 * Adds the given subtype to the type.
 	 */
 	protected void addSubtype(IType type, IType subtype) {
 		TypeVector subtypes = (TypeVector) this.typeToSubtypes.get(type);
 		if (subtypes == null) {
 			subtypes = new TypeVector();
 			this.typeToSubtypes.put(type, subtypes);
 		}
 		if (!subtypes.contains(subtype)) {
 			subtypes.add(subtype);
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public synchronized void addTypeHierarchyChangedListener(
 			ITypeHierarchyChangedListener listener) {
 		ArrayList listeners = this.changeListeners;
 		if (listeners == null) {
 			this.changeListeners = listeners = new ArrayList();
 		}
 
 		// register with JavaCore to get Java element delta on first listener
 		// added
 		if (listeners.size() == 0) {
 			DLTKCore.addElementChangedListener(this);
 		}
 
 		// add listener only if it is not already present
 		if (listeners.indexOf(listener) == -1) {
 			listeners.add(listener);
 		}
 	}
 
 	private static Integer bytesToFlags(byte[] bytes) {
 		if (bytes != null && bytes.length > 0) {
 			return new Integer(new String(bytes));
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * cacheFlags.
 	 */
 	public void cacheFlags(IType type, int flags) {
 		this.typeFlags.put(type, new Integer(flags));
 	}
 
 	/**
 	 * Caches the handle of the superclass for the specified type. As a side
 	 * effect cache this type as a subtype of the superclass.
 	 */
 	protected void cacheSuperclass(IType type, IType superclass) {
 		if (superclass != null) {
 			TypeVector superTypes = (TypeVector) this.classToSuperclass
 					.get(type);
 			if (superTypes == null) {
 				superTypes = new TypeVector();
 				this.classToSuperclass.put(type, superTypes);
 			}
 			if (!superTypes.contains(superclass)) {
 				superTypes.add(superclass);
 			}
 			addSubtype(superclass, type);
 		}
 	}
 
 	/**
 	 * Checks with the progress monitor to see whether the creation of the type
 	 * hierarchy should be canceled. Should be regularly called so that the user
 	 * can cancel.
 	 * 
 	 * @exception OperationCanceledException
 	 *                if cancelling the operation has been requested
 	 * @see IProgressMonitor#isCanceled
 	 */
 	protected void checkCanceled() {
 		if (this.progressMonitor != null && this.progressMonitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 	}
 
 	/**
 	 * Compute this type hierarchy.
 	 */
 	protected void compute() throws ModelException, CoreException {
 		if (this.focusType != null) {
 			HierarchyBuilder builder = new IndexBasedHierarchyBuilder(this,
 					this.scope);
 			builder.build(this.computeSubtypes);
 		} // else a RegionBasedTypeHierarchy should be used
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public boolean contains(IType type) {
 		// classes
		TypeVector superTypes = (TypeVector) this.classToSuperclass.get(type);
		if (superTypes != null && superTypes.contains(type)) {
 			return true;
 		}
 
 		// root classes
 		if (this.rootClasses.contains(type)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Determines if the change effects this hierarchy, and fires change
 	 * notification if required.
 	 */
 	public void elementChanged(ElementChangedEvent event) {
 		// type hierarchy change has already been fired
 		if (this.needsRefresh) {
 			return;
 		}
 
 		if (isAffected(event.getDelta())) {
 			this.needsRefresh = true;
 			fireChange();
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public boolean exists() {
 		if (!this.needsRefresh) {
 			return true;
 		}
 
 		return (this.focusType == null || this.focusType.exists())
 				&& this.javaProject().exists();
 	}
 
 	/**
 	 * Notifies listeners that this hierarchy has changed and needs refreshing.
 	 * Note that listeners can be removed as we iterate through the list.
 	 */
 	public void fireChange() {
 		ArrayList listeners = this.changeListeners;
 		if (listeners == null) {
 			return;
 		}
 		if (DEBUG) {
 			System.out
 					.println("FIRING hierarchy change [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 			if (this.focusType != null) {
 				System.out
 						.println("    for hierarchy focused on " + ((ModelElement) this.focusType).toStringWithAncestors()); //$NON-NLS-1$
 			}
 		}
 		// clone so that a listener cannot have a side-effect on this list when
 		// being notified
 		listeners = (ArrayList) listeners.clone();
 		for (int i = 0; i < listeners.size(); i++) {
 			final ITypeHierarchyChangedListener listener = (ITypeHierarchyChangedListener) listeners
 					.get(i);
 			SafeRunner.run(new ISafeRunnable() {
 				public void handleException(Throwable exception) {
 					Util
 							.log(exception,
 									"Exception occurred in listener of Type hierarchy change notification"); //$NON-NLS-1$
 				}
 
 				public void run() throws Exception {
 					listener.typeHierarchyChanged(TypeHierarchy.this);
 				}
 			});
 		}
 	}
 
 	private static byte[] flagsToBytes(Integer flags) {
 		if (flags != null) {
 			return flags.toString().getBytes();
 		} else {
 			return NO_FLAGS;
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getAllClasses() {
 		TypeVector classes = this.rootClasses.copy();
 		for (Iterator iter = this.classToSuperclass.keySet().iterator(); iter
 				.hasNext();) {
 			classes.add((IType) iter.next());
 		}
 		return classes.elements();
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getAllSubtypes(IType type) {
 		return getAllSubtypesForType(type);
 	}
 
 	/**
 	 * @see #getAllSubtypes(IType)
 	 */
 	private IType[] getAllSubtypesForType(IType type) {
 		ArrayList subTypes = new ArrayList();
 		getAllSubtypesForType0(type, subTypes, new HashSet());
 		IType[] subClasses = new IType[subTypes.size()];
 		subTypes.toArray(subClasses);
 		return subClasses;
 	}
 
 	/**
 	 */
 	private void getAllSubtypesForType0(IType type, ArrayList subs,
 			Set alreadyProcessed) {
 		IType[] subTypes = getSubtypesForType(type);
 		if (subTypes.length != 0) {
 			for (int i = 0; i < subTypes.length; i++) {
 				IType subType = subTypes[i];
 				if (!alreadyProcessed.contains(subType)) {
 					alreadyProcessed.add(subType);
 					subs.add(subType);
 					getAllSubtypesForType0(subType, subs, alreadyProcessed);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getAllSuperclasses(IType type) {
 		IType[] superclass = getSuperclass(type);
 		TypeVector supers = new TypeVector();
 		if (superclass == null) {
 			return supers.elements();
 		}
 		supers.addAll(superclass);
 		for (int i = 0; i < superclass.length; ++i) {
 			IType[] superclass2 = getAllSuperclasses(superclass[i]);
 			supers.addAll(superclass2);
 		}
 		return supers.elements();
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getAllSupertypes(IType type) {
 		ArrayList supers = new ArrayList();
 		getAllSupertypes0(type, supers);
 		IType[] supertypes = new IType[supers.size()];
 		supers.toArray(supertypes);
 		return supertypes;
 	}
 
 	private void getAllSupertypes0(IType type, ArrayList supers) {
 		TypeVector superTypes = (TypeVector) this.classToSuperclass.get(type);
 		if (superTypes != null) {
 			IType[] superclasses = superTypes.elements();
 			if (superclasses.length != 0) {
 				addAllCheckingDuplicates(supers, superclasses);
 				for (int i = 0; i < superclasses.length; i++) {
 					getAllSupertypes0(superclasses[i], supers);
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getAllTypes() {
 		IType[] classes = getAllClasses();
 		int classesLength = classes.length;
 		IType[] all = new IType[classesLength];
 		System.arraycopy(classes, 0, all, 0, classesLength);
 		return all;
 	}
 
 	/**
 	 * @see ITypeHierarchy#getCachedFlags(IType)
 	 */
 	public int getCachedFlags(IType type) {
 		Integer flagObject = (Integer) this.typeFlags.get(type);
 		if (flagObject != null) {
 			return flagObject.intValue();
 		}
 		return -1;
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getRootClasses() {
 		return this.rootClasses.elements();
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getSubclasses(IType type) {
 		TypeVector vector = (TypeVector) this.typeToSubtypes.get(type);
 		if (vector == null) {
 			return NO_TYPE;
 		} else {
 			return vector.elements();
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getSubtypes(IType type) {
 		return getSubtypesForType(type);
 	}
 
 	/**
 	 * Returns an array of subtypes for the given type - will never return null.
 	 */
 	private IType[] getSubtypesForType(IType type) {
 		TypeVector vector = (TypeVector) this.typeToSubtypes.get(type);
 		if (vector == null) {
 			return NO_TYPE;
 		} else {
 			return vector.elements();
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getSuperclass(IType type) {
 		TypeVector superTypes = (TypeVector) this.classToSuperclass.get(type);
 		if (superTypes != null) {
 			return superTypes.elements();
 		}
 		return TypeVector.NoElements;
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType[] getSupertypes(IType type) {
 		return getSuperclass(type);
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public IType getType() {
 		return this.focusType;
 	}
 
 	/**
 	 * Adds the new elements to a new array that contains all of the elements of
 	 * the old array. Returns the new array.
 	 */
 	protected IType[] growAndAddToArray(IType[] array, IType[] additions) {
 		if (array == null || array.length == 0) {
 			return additions;
 		}
 		IType[] old = array;
 		array = new IType[old.length + additions.length];
 		System.arraycopy(old, 0, array, 0, old.length);
 		System.arraycopy(additions, 0, array, old.length, additions.length);
 		return array;
 	}
 
 	/**
 	 * Adds the new element to a new array that contains all of the elements of
 	 * the old array. Returns the new array.
 	 */
 	protected IType[] growAndAddToArray(IType[] array, IType addition) {
 		if (array == null || array.length == 0) {
 			return new IType[] { addition };
 		}
 		IType[] old = array;
 		array = new IType[old.length + 1];
 		System.arraycopy(old, 0, array, 0, old.length);
 		array[old.length] = addition;
 		return array;
 	}
 
 	/*
 	 * Whether fine-grained deltas where collected and affects this hierarchy.
 	 */
 	public boolean hasFineGrainChanges() {
 		ChangeCollector collector = this.changeCollector;
 		return collector != null && collector.needsRefresh();
 	}
 
 	/**
 	 * Returns whether one of the subtypes in this hierarchy has the given
 	 * simple name or this type has the given simple name.
 	 */
 	private boolean hasSubtypeNamed(String simpleName) {
 		if (this.focusType != null
 				&& this.focusType.getElementName().equals(simpleName)) {
 			return true;
 		}
 		IType[] types = this.focusType == null ? getAllTypes()
 				: getAllSubtypes(this.focusType);
 		for (int i = 0; i < types.length; i++) {
 			if (types[i].getElementName().equals(simpleName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns whether one of the types in this hierarchy has the given simple
 	 * name.
 	 */
 	private boolean hasTypeNamed(String simpleName) {
 		IType[] types = this.getAllTypes();
 		for (int i = 0; i < types.length; i++) {
 			if (types[i].getElementName().equals(simpleName)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns whether the simple name of the given type or one of its
 	 * supertypes is the simple name of one of the types in this hierarchy.
 	 */
 	boolean includesTypeOrSupertype(IType type) {
 		try {
 			// check type
 			if (hasTypeNamed(type.getElementName())) {
 				return true;
 			}
 
 			// check superclass
 			String[] superclassNames = type.getSuperClasses();
 			if (superclassNames != null) {
 				// int lastSeparator = superclassName.lastIndexOf('.');
 				// String simpleName =
 				// superclassName.substring(lastSeparator+1);
 				// if (hasTypeNamed(simpleName)) return true;
 				for (int i = 0; i < superclassNames.length; i++) {
 					String superinterfaceName = superclassNames[i];
 					// for (String superinterfaceName : superclassNames) {
 					int lastSeparator = superinterfaceName.lastIndexOf('.');
 					String simpleName = superinterfaceName
 							.substring(lastSeparator + 1);
 					if (hasTypeNamed(simpleName)) {
 						return true;
 					}
 				}
 			}
 
 			// // check superinterfaces
 			// String[] superinterfaceNames = type.getSuperInterfaceNames();
 			// if (superinterfaceNames != null) {
 			// for (int i = 0, length = superinterfaceNames.length; i < length;
 			// i++) {
 			// String superinterfaceName = superinterfaceNames[i];
 			// int lastSeparator = superinterfaceName.lastIndexOf('.');
 			// String simpleName =
 			// superinterfaceName.substring(lastSeparator+1);
 			// if (hasTypeNamed(simpleName)) return true;
 			// }
 			// }
 		} catch (ModelException e) {
 			// ignore
 		}
 		return false;
 	}
 
 	/**
 	 * Initializes this hierarchy's internal tables with the given size.
 	 */
 	protected void initialize(int size) {
 		if (size < 10) {
 			size = 10;
 		}
 		int smallSize = (size / 2);
 		this.classToSuperclass = new HashMap(size);
 		this.missingTypes = new ArrayList(smallSize);
 		this.rootClasses = new TypeVector();
 		this.typeToSubtypes = new HashMap(smallSize);
 		this.typeFlags = new HashMap(smallSize);
 
 		this.projectRegion = new Region();
 		this.packageRegion = new Region();
 		this.files = new HashMap(5);
 	}
 
 	/**
 	 * Returns true if the given delta could change this type hierarchy
 	 */
 	public synchronized boolean isAffected(IModelElementDelta delta) {
 		IModelElement element = delta.getElement();
 		switch (element.getElementType()) {
 		case IModelElement.SCRIPT_MODEL:
 			return isAffectedByJavaModel(delta, element);
 		case IModelElement.SCRIPT_PROJECT:
 			return isAffectedByJavaProject(delta, element);
 		case IModelElement.PROJECT_FRAGMENT:
 			return isAffectedByPackageFragmentRoot(delta, element);
 		case IModelElement.SCRIPT_FOLDER:
 			return isAffectedByPackageFragment(delta, (ScriptFolder) element);
 		case IModelElement.SOURCE_MODULE:
 			return isAffectedByOpenable(delta, element);
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if any of the children of a project, package fragment root,
 	 * or package fragment have changed in a way that effects this type
 	 * hierarchy.
 	 */
 	private boolean isAffectedByChildren(IModelElementDelta delta) {
 		if ((delta.getFlags() & IModelElementDelta.F_CHILDREN) > 0) {
 			IModelElementDelta[] children = delta.getAffectedChildren();
 			for (int i = 0; i < children.length; i++) {
 				if (isAffected(children[i])) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the given java model delta could affect this type
 	 * hierarchy
 	 */
 	private boolean isAffectedByJavaModel(IModelElementDelta delta,
 			IModelElement element) {
 		switch (delta.getKind()) {
 		case IModelElementDelta.ADDED:
 		case IModelElementDelta.REMOVED:
 			return element.equals(this.javaProject().getModel());
 		case IModelElementDelta.CHANGED:
 			return isAffectedByChildren(delta);
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the given java project delta could affect this type
 	 * hierarchy
 	 */
 	private boolean isAffectedByJavaProject(IModelElementDelta delta,
 			IModelElement element) {
 		int kind = delta.getKind();
 		int flags = delta.getFlags();
 		if ((flags & IModelElementDelta.F_OPENED) != 0) {
 			kind = IModelElementDelta.ADDED; // affected in the same way
 		}
 		if ((flags & IModelElementDelta.F_CLOSED) != 0) {
 			kind = IModelElementDelta.REMOVED; // affected in the same way
 		}
 		switch (kind) {
 		case IModelElementDelta.ADDED:
 			try {
 				// if the added project is on the classpath, then the hierarchy
 				// has changed
 				IBuildpathEntry[] classpath = ((ScriptProject) this
 						.javaProject()).getExpandedBuildpath(false);
 				for (int j = 0; j < classpath.length; j++) {
 					IBuildpathEntry element2 = classpath[j];
 					if (element2.getEntryKind() == IBuildpathEntry.BPE_PROJECT
 							&& element2.getPath().equals(element.getPath())) {
 						return true;
 					}
 				}
 				if (this.focusType != null) {
 					// if the hierarchy's project is on the added project
 					// classpath, then the hierarchy has changed
 					classpath = ((ScriptProject) element)
 							.getExpandedBuildpath(true);
 					IPath hierarchyProject = javaProject().getPath();
 					for (int j = 0; j < classpath.length; j++) {
 						IBuildpathEntry element2 = classpath[j];
 						if (element2.getEntryKind() == IBuildpathEntry.BPE_PROJECT
 								&& element2.getPath().equals(hierarchyProject)) {
 							return true;
 						}
 					}
 				}
 				return false;
 			} catch (ModelException e) {
 				return false;
 			}
 		case IModelElementDelta.REMOVED:
 			// removed project - if it contains packages we are interested in
 			// then the type hierarchy has changed
 			IModelElement[] pkgs = this.packageRegion.getElements();
 			for (int i = 0; i < pkgs.length; i++) {
 				IModelElement pkg = pkgs[i];
 				IScriptProject javaProject = pkg.getScriptProject();
 				if (javaProject != null && javaProject.equals(element)) {
 					return true;
 				}
 			}
 			return false;
 		case IModelElementDelta.CHANGED:
 			return isAffectedByChildren(delta);
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the given package fragment delta could affect this type
 	 * hierarchy
 	 */
 	private boolean isAffectedByPackageFragment(IModelElementDelta delta,
 			ScriptFolder element) {
 		switch (delta.getKind()) {
 		case IModelElementDelta.ADDED:
 			// if the package fragment is in the projects being considered, this
 			// could
 			// introduce new types, changing the hierarchy
 			return this.projectRegion.contains(element);
 		case IModelElementDelta.REMOVED:
 			// is a change if the package fragment contains types in this
 			// hierarchy
 			return packageRegionContainsSamePackageFragment(element);
 		case IModelElementDelta.CHANGED:
 			// look at the files in the package fragment
 			return isAffectedByChildren(delta);
 		}
 		return false;
 	}
 
 	/**
 	 * Returns true if the given package fragment root delta could affect this
 	 * type hierarchy
 	 */
 	private boolean isAffectedByPackageFragmentRoot(IModelElementDelta delta,
 			IModelElement element) {
 		switch (delta.getKind()) {
 		case IModelElementDelta.ADDED:
 			return this.projectRegion.contains(element);
 		case IModelElementDelta.REMOVED:
 		case IModelElementDelta.CHANGED:
 			int flags = delta.getFlags();
 			if ((flags & IModelElementDelta.F_ADDED_TO_BUILDPATH) > 0) {
 				// check if the root is in the classpath of one of the projects
 				// of this hierarchy
 				if (this.projectRegion != null) {
 					IProjectFragment root = (IProjectFragment) element;
 					IPath rootPath = root.getPath();
 					IModelElement[] elements = this.projectRegion.getElements();
 					for (int i = 0; i < elements.length; i++) {
 						IModelElement element2 = elements[i];
 						ScriptProject javaProject = (ScriptProject) element2;
 						try {
 							IBuildpathEntry[] classpath = javaProject
 									.getResolvedBuildpath();
 							for (int j = 0; j < classpath.length; j++) {
 								IBuildpathEntry entry = classpath[j];
 								if (entry.getPath().equals(rootPath)) {
 									return true;
 								}
 							}
 						} catch (ModelException e) {
 							// igmore this project
 						}
 					}
 				}
 			}
 			if ((flags & IModelElementDelta.F_REMOVED_FROM_BUILDPATH) > 0
 					|| (flags & IModelElementDelta.F_CONTENT) > 0) {
 				// 1. removed from classpath - if it contains packages we are
 				// interested in
 				// the the type hierarchy has changed
 				// 2. content of a jar changed - if it contains packages we are
 				// interested in
 				// the the type hierarchy has changed
 				IModelElement[] pkgs = this.packageRegion.getElements();
 				for (int i = 0; i < pkgs.length; i++) {
 					if (pkgs[i].getParent().equals(element)) {
 						return true;
 					}
 				}
 				return false;
 			}
 		}
 		return isAffectedByChildren(delta);
 	}
 
 	/**
 	 * Returns true if the given type delta (a compilation unit delta or a class
 	 * file delta) could affect this type hierarchy.
 	 */
 	protected boolean isAffectedByOpenable(IModelElementDelta delta,
 			IModelElement element) {
 		if (element instanceof SourceModule) {
 			SourceModule cu = (SourceModule) element;
 			ChangeCollector collector = this.changeCollector;
 			if (collector == null) {
 				collector = new ChangeCollector(this);
 			}
 			try {
 				collector.addChange(cu, delta);
 			} catch (ModelException e) {
 				if (DEBUG) {
 					e.printStackTrace();
 				}
 			}
 			if (cu.isWorkingCopy()) {
 				// changes to working copies are batched
 				this.changeCollector = collector;
 				return false;
 			} else {
 				return collector.needsRefresh();
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Returns the java project this hierarchy was created in.
 	 */
 	public IScriptProject javaProject() {
 		return this.focusType.getScriptProject();
 	}
 
 	protected static byte[] readUntil(InputStream input, byte separator)
 			throws ModelException, IOException {
 		return readUntil(input, separator, 0);
 	}
 
 	protected static byte[] readUntil(InputStream input, byte separator,
 			int offset) throws IOException, ModelException {
 		int length = 0;
 		byte[] bytes = new byte[SIZE];
 		byte b;
 		while ((b = (byte) input.read()) != separator && b != -1) {
 			if (bytes.length == length) {
 				System.arraycopy(bytes, 0, bytes = new byte[length * 2], 0,
 						length);
 			}
 			bytes[length++] = b;
 		}
 		if (b == -1) {
 			throw new ModelException(new ModelStatus(IStatus.ERROR));
 		}
 		System.arraycopy(bytes, 0, bytes = new byte[length + offset], offset,
 				length);
 		return bytes;
 	}
 
 	public static ITypeHierarchy load(IType type, InputStream input,
 			WorkingCopyOwner owner) throws ModelException {
 		try {
 			TypeHierarchy typeHierarchy = new TypeHierarchy();
 			typeHierarchy.initialize(1);
 
 			IType[] types = new IType[SIZE];
 			int typeCount = 0;
 
 			byte version = (byte) input.read();
 
 			if (version != VERSION) {
 				throw new ModelException(new ModelStatus(IStatus.ERROR));
 			}
 			byte generalInfo = (byte) input.read();
 			if ((generalInfo & COMPUTE_SUBTYPES) != 0) {
 				typeHierarchy.computeSubtypes = true;
 			}
 
 			byte b;
 			byte[] bytes;
 
 			// read project
 			bytes = readUntil(input, SEPARATOR1);
 			if (bytes.length > 0) {
 				typeHierarchy.project = (IScriptProject) DLTKCore
 						.create(new String(bytes));
 				typeHierarchy.scope = SearchEngine
 						.createSearchScope(typeHierarchy.project);
 			} else {
 				typeHierarchy.project = null;
 				typeHierarchy.scope = SearchEngine
 						.createWorkspaceScope(DLTKLanguageManager
 								.getLanguageToolkit(type));
 			}
 
 			// read missing type
 			{
 				bytes = readUntil(input, SEPARATOR1);
 				byte[] missing;
 				int j = 0;
 				int length = bytes.length;
 				for (int i = 0; i < length; i++) {
 					b = bytes[i];
 					if (b == SEPARATOR2) {
 						missing = new byte[i - j];
 						System.arraycopy(bytes, j, missing, 0, i - j);
 						typeHierarchy.missingTypes.add(new String(missing));
 						j = i + 1;
 					}
 				}
 				System.arraycopy(bytes, j, missing = new byte[length - j], 0,
 						length - j);
 				typeHierarchy.missingTypes.add(new String(missing));
 			}
 
 			// read types
 			while ((b = (byte) input.read()) != SEPARATOR1 && b != -1) {
 				bytes = readUntil(input, SEPARATOR4, 1);
 				bytes[0] = b;
 				IType element = (IType) DLTKCore.create(new String(bytes),
 						owner);
 
 				if (types.length == typeCount) {
 					System.arraycopy(types, 0,
 							types = new IType[typeCount * 2], 0, typeCount);
 				}
 				types[typeCount++] = element;
 
 				// read flags
 				bytes = readUntil(input, SEPARATOR4);
 				Integer flags = bytesToFlags(bytes);
 				if (flags != null) {
 					typeHierarchy.cacheFlags(element, flags.intValue());
 				}
 
 				// read info
 				byte info = (byte) input.read();
 
 				// if ((info & INTERFACE) != 0) {
 				// typeHierarchy.addInterface(element);
 				// }
 				if ((info & COMPUTED_FOR) != 0) {
 					if (!element.equals(type)) {
 						throw new ModelException(new ModelStatus(IStatus.ERROR));
 					}
 					typeHierarchy.focusType = element;
 				}
 				if ((info & ROOT) != 0) {
 					typeHierarchy.addRootClass(element);
 				}
 			}
 
 			// read super class
 			while ((b = (byte) input.read()) != SEPARATOR1 && b != -1) {
 				bytes = readUntil(input, SEPARATOR3, 1);
 				bytes[0] = b;
 				int subClass = new Integer(new String(bytes)).intValue();
 
 				// read super type
 				bytes = readUntil(input, SEPARATOR1);
 				int superClass = new Integer(new String(bytes)).intValue();
 
 				typeHierarchy.cacheSuperclass(types[subClass],
 						types[superClass]);
 			}
 
 			// read super interface
 			while ((b = (byte) input.read()) != SEPARATOR1 && b != -1) {
 				bytes = readUntil(input, SEPARATOR3, 1);
 				bytes[0] = b;
 				// int subClass = new Integer(new String(bytes)).intValue();
 
 				// read super interface
 				bytes = readUntil(input, SEPARATOR1);
 				IType[] superInterfaces = new IType[(bytes.length / 2) + 1];
 				int interfaceCount = 0;
 
 				int j = 0;
 				byte[] b2;
 				for (int i = 0; i < bytes.length; i++) {
 					if (bytes[i] == SEPARATOR2) {
 						b2 = new byte[i - j];
 						System.arraycopy(bytes, j, b2, 0, i - j);
 						j = i + 1;
 						superInterfaces[interfaceCount++] = types[new Integer(
 								new String(b2)).intValue()];
 					}
 				}
 				b2 = new byte[bytes.length - j];
 				System.arraycopy(bytes, j, b2, 0, bytes.length - j);
 				superInterfaces[interfaceCount++] = types[new Integer(
 						new String(b2)).intValue()];
 				System.arraycopy(superInterfaces, 0,
 						superInterfaces = new IType[interfaceCount], 0,
 						interfaceCount);
 
 			}
 			if (b == -1) {
 				throw new ModelException(new ModelStatus(IStatus.ERROR));
 			}
 			return typeHierarchy;
 		} catch (IOException e) {
 			throw new ModelException(e, IModelStatusConstants.IO_EXCEPTION);
 		} catch (CoreException e) {
 			throw new ModelException(e, IModelStatusConstants.INTERNAL_ERROR);
 		}
 	}
 
 	/**
 	 * Returns <code>true</code> if an equivalent package fragment is included
 	 * in the package region. Package fragments are equivalent if they both have
 	 * the same name.
 	 */
 	protected boolean packageRegionContainsSamePackageFragment(
 			ScriptFolder element) {
 		IModelElement[] pkgs = this.packageRegion.getElements();
 		for (int i = 0; i < pkgs.length; i++) {
 			ScriptFolder pkg = (ScriptFolder) pkgs[i];
 			if (pkg.getElementName().equals(element.getElementName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @see ITypeHierarchy TODO (jerome) should use a PerThreadObject to build
 	 *      the hierarchy instead of synchronizing (see also
 	 *      isAffected(IJavaElementDelta))
 	 */
 	public synchronized void refresh(IProgressMonitor monitor)
 			throws ModelException {
 		try {
 			this.progressMonitor = monitor;
 			if (monitor != null) {
 				if (this.focusType != null) {
 					monitor.beginTask(Messages.bind(
 							Messages.hierarchy_creatingOnType, this.focusType
 									.getFullyQualifiedName()), 100);
 				} else {
 					monitor.beginTask(Messages.hierarchy_creating, 100);
 				}
 			}
 			long start = -1;
 			if (DEBUG) {
 				start = System.currentTimeMillis();
 				if (this.computeSubtypes) {
 					System.out
 							.println("CREATING TYPE HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 				} else {
 					System.out
 							.println("CREATING SUPER TYPE HIERARCHY [" + Thread.currentThread() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				if (this.focusType != null) {
 					System.out
 							.println("  on type " + ((ModelElement) this.focusType).toStringWithAncestors()); //$NON-NLS-1$
 				}
 			}
 
 			compute();
 			initializeRegions();
 			this.needsRefresh = false;
 			this.changeCollector = null;
 
 			if (DEBUG) {
 				if (this.computeSubtypes) {
 					System.out
 							.println("CREATED TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 				} else {
 					System.out
 							.println("CREATED SUPER TYPE HIERARCHY in " + (System.currentTimeMillis() - start) + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				System.out.println(this.toString());
 			}
 		} catch (ModelException e) {
 			throw e;
 		} catch (CoreException e) {
 			throw new ModelException(e);
 		} finally {
 			if (monitor != null) {
 				monitor.done();
 			}
 			this.progressMonitor = null;
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public synchronized void removeTypeHierarchyChangedListener(
 			ITypeHierarchyChangedListener listener) {
 		ArrayList listeners = this.changeListeners;
 		if (listeners == null) {
 			return;
 		}
 		listeners.remove(listener);
 
 		// deregister from JavaCore on last listener removed
 		if (listeners.isEmpty()) {
 			DLTKCore.removeElementChangedListener(this);
 		}
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public void store(OutputStream output, IProgressMonitor monitor)
 			throws ModelException {
 		try {
 			// compute types in hierarchy
 			Hashtable hashtable = new Hashtable();
 			Hashtable hashtable2 = new Hashtable();
 			int count = 0;
 
 			if (this.focusType != null) {
 				Integer index = new Integer(count++);
 				hashtable.put(this.focusType, index);
 				hashtable2.put(index, this.focusType);
 			}
 			Object[] types = this.classToSuperclass.entrySet().toArray();
 			for (int k = 0; k < types.length; k++) {
 				Object type = types[k];
 				Map.Entry entry = (Map.Entry) type;
 				Object t = entry.getKey();
 				if (hashtable.get(t) == null) {
 					Integer index = new Integer(count++);
 					hashtable.put(t, index);
 					hashtable2.put(index, t);
 				}
 				TypeVector superClasses = (TypeVector) entry.getValue();
 				if (superClasses != null) {
 					IType[] sp = superClasses.elements();
 					for (int i = 0; i < sp.length; i++) {
 
 						IType superInterface = sp[i];
 						if (superInterface != null
 								&& hashtable.get(superInterface) == null) {
 							Integer index = new Integer(count++);
 							hashtable.put(superInterface, index);
 							hashtable2.put(index, superInterface);
 						}
 					}
 				}
 			}
 			// save version of the hierarchy format
 			output.write(VERSION);
 
 			// save general info
 			byte generalInfo = 0;
 			if (this.computeSubtypes) {
 				generalInfo |= COMPUTE_SUBTYPES;
 			}
 			output.write(generalInfo);
 
 			// save project
 			if (this.project != null) {
 				output.write(this.project.getHandleIdentifier().getBytes());
 			}
 			output.write(SEPARATOR1);
 
 			// save missing types
 			for (int i = 0; i < this.missingTypes.size(); i++) {
 				if (i != 0) {
 					output.write(SEPARATOR2);
 				}
 				output.write(((String) this.missingTypes.get(i)).getBytes());
 
 			}
 			output.write(SEPARATOR1);
 
 			// save types
 			for (int i = 0; i < count; i++) {
 				IType t = (IType) hashtable2.get(new Integer(i));
 
 				// n bytes
 				output.write(t.getHandleIdentifier().getBytes());
 				output.write(SEPARATOR4);
 				output.write(flagsToBytes((Integer) this.typeFlags.get(t)));
 				output.write(SEPARATOR4);
 				byte info = CLASS;
 				if (this.focusType != null && this.focusType.equals(t)) {
 					info |= COMPUTED_FOR;
 				}
 				if (this.rootClasses.contains(t)) {
 					info |= ROOT;
 				}
 				output.write(info);
 			}
 			output.write(SEPARATOR1);
 
 			// save superclasses
 			types = this.classToSuperclass.entrySet().toArray();
 			for (int q = 0; q < types.length; q++) {
 				Object type = types[q];
 				Map.Entry entry = (Map.Entry) type;
 				IModelElement key = (IModelElement) entry.getKey();
 
 				TypeVector superTypes = (TypeVector) entry.getValue();
 				if (superTypes != null) {
 
 					IType[] values = superTypes.elements();
 					if (values.length > 0) {
 						output.write(((Integer) hashtable.get(key)).toString()
 								.getBytes());
 						output.write(SEPARATOR3);
 						for (int j = 0; j < values.length; j++) {
 							IModelElement value = values[j];
 							if (j != 0) {
 								output.write(SEPARATOR2);
 							}
 							output.write(((Integer) hashtable.get(value))
 									.toString().getBytes());
 						}
 						output.write(SEPARATOR1);
 					}
 				}
 			}
 			output.write(SEPARATOR1);
 		} catch (IOException e) {
 			throw new ModelException(e, IModelStatusConstants.IO_EXCEPTION);
 		}
 	}
 
 	/**
 	 * Returns whether the simple name of a supertype of the given type is the
 	 * simple name of one of the subtypes in this hierarchy or the simple name
 	 * of this type.
 	 */
 	boolean subtypesIncludeSupertypeOf(IType type) {
 
 		// look for super classes
 		String[] superclassNames = null;
 		try {
 			superclassNames = type.getSuperClasses();
 		} catch (ModelException e) {
 			if (DEBUG) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 		int dot;
 		if (superclassNames != null) {
 			for (int i = 0; i < superclassNames.length; i++) {
 				String interfaceName = superclassNames[i];
 				dot = -1;
 				String simpleInterface = (dot = interfaceName.lastIndexOf('.')) > -1 ? interfaceName
 						.substring(dot)
 						: interfaceName;
 				if (hasSubtypeNamed(simpleInterface)) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * @see ITypeHierarchy
 	 */
 	public String toString() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("Focus: "); //$NON-NLS-1$
 		buffer.append(this.focusType == null ? "<NONE>" //$NON-NLS-1$
 				: ((ModelElement) this.focusType)
 						.toStringWithAncestors(false/*
 													 * don't show key
 													 */));
 		buffer.append("\n"); //$NON-NLS-1$
 		if (exists()) {
 			if (this.focusType != null) {
 				buffer.append("Super types:\n"); //$NON-NLS-1$
 				toString(buffer, this.focusType, 1, true);
 				buffer.append("Sub types:\n"); //$NON-NLS-1$
 				toString(buffer, this.focusType, 1, false);
 			} else {
 				buffer.append("Sub types of root classes:\n"); //$NON-NLS-1$
 				IModelElement[] roots = Util.sortCopy(getRootClasses());
 				for (int i = 0; i < roots.length; i++) {
 					toString(buffer, (IType) roots[i], 1, false);
 				}
 			}
 			if (this.rootClasses.size > 1) {
 				buffer.append("Root classes:\n"); //$NON-NLS-1$
 				IModelElement[] roots = Util.sortCopy(getRootClasses());
 				for (int i = 0; i < roots.length; i++) {
 					toString(buffer, (IType) roots[i], 1, false);
 				}
 			} else if (this.rootClasses.size == 0) {
 				// see http://bugs.eclipse.org/bugs/show_bug.cgi?id=24691
 				buffer.append("No root classes"); //$NON-NLS-1$
 			}
 		} else {
 			buffer.append("(Hierarchy became stale)"); //$NON-NLS-1$
 		}
 		return buffer.toString();
 	}
 
 	/**
 	 * Append a String to the given buffer representing the hierarchy for the
 	 * type, beginning with the specified indentation level. If ascendant, shows
 	 * the super types, otherwise show the sub types.
 	 */
 	private void toString(StringBuffer buffer, IType type, int indent,
 			boolean ascendant) {
 		IType[] types = ascendant ? getSupertypes(type) : getSubtypes(type);
 		IModelElement[] sortedTypes = Util.sortCopy(types);
 		for (int i = 0; i < sortedTypes.length; i++) {
 			for (int j = 0; j < indent; j++) {
 				buffer.append("  "); //$NON-NLS-1$
 			}
 			ModelElement element = (ModelElement) sortedTypes[i];
 			buffer.append(element
 					.toStringWithAncestors(false/* don't show key */));
 			buffer.append('\n');
 			toString(buffer, types[i], indent + 1, ascendant);
 		}
 	}
 
 	/**
 	 * Returns whether one of the types in this hierarchy has a supertype whose
 	 * simple name is the given simple name.
 	 */
 	boolean hasSupertype(String simpleName) {
 		for (Iterator iter = this.classToSuperclass.values().iterator(); iter
 				.hasNext();) {
 			TypeVector typeVector = (TypeVector) iter.next();
 			if (typeVector != null) {
 				IType[] elements = typeVector.elements();
 				for (int i = 0; i < elements.length; i++) {
 					IType superType = elements[i];
 					if (superType.getElementName().equals(simpleName)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @see IProgressMonitor
 	 */
 	protected void worked(int work) {
 		if (this.progressMonitor != null) {
 			this.progressMonitor.worked(work);
 			checkCanceled();
 		}
 	}
 }
