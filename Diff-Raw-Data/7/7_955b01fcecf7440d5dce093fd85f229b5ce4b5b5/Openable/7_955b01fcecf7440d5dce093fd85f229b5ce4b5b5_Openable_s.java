 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.dltk.codeassist.ICompletionEngine;
 import org.eclipse.dltk.codeassist.ISelectionEngine;
 import org.eclipse.dltk.core.BufferChangedEvent;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IBufferChangedListener;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IOpenable;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 
 /**
  * Abstract class for implementations of model elements which are IOpenable.
  * 
  * @see IModelElement
  * @see IOpenable
  */
 public abstract class Openable extends ModelElement implements IOpenable,
 		IBufferChangedListener {
 
 	protected Openable(ModelElement parent) {
 		super(parent);
 	}
 
 	/**
 	 * The buffer associated with this element has changed. Registers this
 	 * element as being out of synch with its buffer's contents. If the buffer
 	 * has been closed, this element is set as NOT out of synch with the
 	 * contents.
 	 * 
 	 * @see IBufferChangedListener
 	 */
 	public void bufferChanged(BufferChangedEvent event) {
 		if (event.getBuffer().isClosed()) {
 			ModelManager.getModelManager().getElementsOutOfSynchWithBuffers()
 					.remove(this);
 			getBufferManager().removeBuffer(event.getBuffer());
 		} else {
 			ModelManager.getModelManager().getElementsOutOfSynchWithBuffers()
 					.add(this);
 		}
 	}
 
 	/**
 	 * Builds this element's structure and properties in the given info object,
 	 * based on this element's current contents (reuse buffer contents if this
 	 * element has an open buffer, or resource contents if this element does not
 	 * have an open buffer). Children are placed in the given newElements table
 	 * (note, this element has already been placed in the newElements table).
 	 * Returns true if successful, or false if an error is encountered while
 	 * determining the structure of this element.
 	 */
 	protected abstract boolean buildStructure(OpenableElementInfo info,
 			IProgressMonitor pm, Map newElements, IResource underlyingResource)
 			throws ModelException;
 
 	/*
 	 * Returns whether this element can be removed from the model cache to make
 	 * space.
 	 */
 	public boolean canBeRemovedFromCache() {
 		try {
 			return !hasUnsavedChanges();
 		} catch (ModelException e) {
 			return false;
 		}
 	}
 
 	/*
 	 * Returns whether the buffer of this element can be removed from the Script
 	 * model cache to make space.
 	 */
 	public boolean canBufferBeRemovedFromCache(IBuffer buffer) {
 		return !buffer.hasUnsavedChanges();
 	}
 
 	/**
 	 * Close the buffer associated with this element, if any.
 	 */
 	protected void closeBuffer() {
 		if (!hasBuffer())
 			return; // nothing to do
 		IBuffer buffer = getBufferManager().getBuffer(this);
 		if (buffer != null) {
 			buffer.close();
 			buffer.removeBufferChangedListener(this);
 		}
 	}
 
 	/**
 	 * This element is being closed. Do any necessary cleanup.
 	 */
 	protected void closing(Object info) {
 		closeBuffer();
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public boolean exists() {
 		ModelManager manager = ModelManager.getModelManager();
 		if (manager.getInfo(this) != null)
 			return true;
 		if (!parentExists())
 			return false;
 		ProjectFragment root = getProjectFragment();
 		if (root != null && (root == this || !root.isArchive())) {
 			return resourceExists();
 		}
 		return super.exists();
 	}
 
 	protected void generateInfos(Object info, HashMap newElements,
 			IProgressMonitor monitor) throws ModelException {
 
 		if (ModelManager.VERBOSE) {
 			String element;
 			switch (getElementType()) {
 			case SCRIPT_PROJECT:
 				element = "project"; //$NON-NLS-1$
 				break;
 			case PROJECT_FRAGMENT:
 				element = "fragment"; //$NON-NLS-1$
 				break;
 			case SCRIPT_FOLDER:
 				element = "folder"; //$NON-NLS-1$
 				break;
 			case BINARY_MODULE:
 				element = "binary module"; //$NON-NLS-1$
 				break;
 			case SOURCE_MODULE:
 				element = "source module"; //$NON-NLS-1$
 				break;
 			default:
 				element = "element"; //$NON-NLS-1$
 			}
 			System.out
 					.println(Thread.currentThread()
 							+ " OPENING " + element + " " + this.toStringWithAncestors()); //$NON-NLS-1$//$NON-NLS-2$
 		}
 
 		// open the parent if necessary
 		openParent(info, newElements, monitor);
 		if (monitor != null && monitor.isCanceled())
 			throw new OperationCanceledException();
 
 		// puts the info before building the structure so that questions to the
 		// handle behave as if the element existed
 		// (case of compilation units becoming working copies)
 		newElements.put(this, info);
 
 		// build the structure of the openable (this will open the buffer if
 		// needed)
 		try {
 			OpenableElementInfo openableElementInfo = (OpenableElementInfo) info;
 			boolean isStructureKnown = buildStructure(openableElementInfo,
 					monitor, newElements, getResource());
 			openableElementInfo.setIsStructureKnown(isStructureKnown);
 		} catch (ModelException e) {
 			newElements.remove(this);
 			throw e;
 		}
 
 		// remove out of sync buffer for this element
 		ModelManager.getModelManager().getElementsOutOfSynchWithBuffers()
 				.remove(this);
 
 		if (ModelManager.VERBOSE) {
 			System.out.println(ModelManager.getModelManager().cache
 					.toStringFillingRation("-> ")); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Note: a buffer with no unsaved changes can be closed by the Model since
 	 * it has a finite number of buffers allowed open at one time. If this is
 	 * the first time a request is being made for the buffer, an attempt is made
 	 * to create and fill this element's buffer. If the buffer has been closed
 	 * since it was first opened, the buffer is re-created.
 	 * 
 	 * @see IOpenable
 	 */
 	public IBuffer getBuffer() throws ModelException {
 		if (hasBuffer()) {
 			// ensure element is open
 			Object info = getElementInfo();
 			IBuffer buffer = getBufferManager().getBuffer(this);
 			if (buffer == null) {
 				// try to (re)open a buffer
 				buffer = openBuffer(null, info);
 			}
 			return buffer;
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Returns the buffer manager for this element.
 	 */
 	protected BufferManager getBufferManager() {
 		return BufferManager.getDefaultBufferManager();
 	}
 
 	/**
 	 * Return my underlying resource. Elements that may not have a corresponding
 	 * resource must override this method.
 	 * 
 	 * @see IScriptElement
 	 */
 	public IResource getCorrespondingResource() throws ModelException {
 		return getUnderlyingResource();
 	}
 
 	/*
 	 * @see IModelElement
 	 */
 	public IOpenable getOpenable() {
 		return this;
 	}
 
 	public IResource getUnderlyingResource() throws ModelException {
 		IResource parentResource = this.parent.getUnderlyingResource();
 		if (parentResource == null) {
 			return null;
 		}
 		int type = parentResource.getType();
 		if (type == IResource.FOLDER || type == IResource.PROJECT) {
 			IContainer folder = (IContainer) parentResource;
 			IResource resource = folder.findMember(getElementName());
 			if (resource == null) {
 				throw newNotPresentException();
 			} else {
 				return resource;
 			}
 		} else {
 			return parentResource;
 		}
 	}
 
 	/**
 	 * Returns true if this element may have an associated source buffer,
 	 * otherwise false. Subclasses must override as required.
 	 */
 	protected boolean hasBuffer() {
 		return false;
 	}
 
 	/**
 	 * @see IOpenable
 	 */
 	public boolean hasUnsavedChanges() throws ModelException {
 
 		if (isReadOnly() || !isOpen()) {
 			return false;
 		}
 		IBuffer buf = this.getBuffer();
 		if (buf != null && buf.hasUnsavedChanges()) {
 			return true;
 		}
 		// for package fragments, package fragment roots, and projects must
 		// check open buffers
 		// to see if they have an child with unsaved changes
 		int elementType = getElementType();
 		if (elementType == SCRIPT_FOLDER || elementType == PROJECT_FRAGMENT
 				|| elementType == SCRIPT_PROJECT || elementType == SCRIPT_MODEL) { // fix
 																					// for
 																					// 1FWNMHH
 			Enumeration openBuffers = getBufferManager().getOpenBuffers();
 			while (openBuffers.hasMoreElements()) {
 				IBuffer buffer = (IBuffer) openBuffers.nextElement();
 				if (buffer.hasUnsavedChanges()) {
 					IModelElement owner = buffer.getOwner();
 					if (isAncestorOf(owner)) {
 						return true;
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Subclasses must override as required.
 	 * 
 	 * @see IOpenable
 	 */
 	public boolean isConsistent() {
 		return true;
 	}
 
 	/**
 	 * 
 	 * @see IOpenable
 	 */
 	public boolean isOpen() {
 		return ModelManager.getModelManager().getInfo(this) != null;
 	}
 
 	/**
 	 * Returns true if this represents a source element. Openable source
 	 * elements have an associated buffer created when they are opened.
 	 */
 	protected boolean isSourceElement() {
 		return false;
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public boolean isStructureKnown() throws ModelException {
 		return ((OpenableElementInfo) getElementInfo()).isStructureKnown();
 	}
 
 	/**
 	 * @see IOpenable
 	 */
 	public void makeConsistent(IProgressMonitor monitor) throws ModelException {
 		// only source modules can be inconsistent
 		// other openables cannot be inconsistent so default is to do nothing
 	}
 
 	/**
 	 * @see IOpenable
 	 */
 	public void open(IProgressMonitor pm) throws ModelException {
 		getElementInfo(pm);
 	}
 
 	/**
 	 * Opens a buffer on the contents of this element, and returns the buffer,
 	 * or returns <code>null</code> if opening fails. By default, do nothing -
 	 * subclasses that have buffers must override as required.
 	 */
 	protected IBuffer openBuffer(IProgressMonitor pm, Object info)
 			throws ModelException {
 		return null;
 	}
 
 	/**
 	 * Open the parent element if necessary.
 	 */
 	protected void openParent(Object childInfo, HashMap newElements,
 			IProgressMonitor pm) throws ModelException {
 
 		Openable openableParent = (Openable) getOpenableParent();
 		if (openableParent != null && !openableParent.isOpen()) {
 			openableParent.generateInfos(openableParent.createElementInfo(),
 					newElements, pm);
 		}
 	}
 
 	/**
 	 * Answers true if the parent exists (null parent is answering true)
 	 * 
 	 */
 	protected boolean parentExists() {
 		IModelElement parentElement = getParent();
 		if (parentElement == null)
 			return true;
 		return parentElement.exists();
 	}
 
 	/**
 	 * Returns whether the corresponding resource or associated file exists
 	 */
 	protected boolean resourceExists() {
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 		if (workspace == null)
 			return false; // workaround for
 							// http://bugs.eclipse.org/bugs/show_bug.cgi?id=34069
 		return Model.getTarget(workspace.getRoot(), this.getPath()
 				.makeRelative(), // ensure path is relative (see
 									// http://dev.eclipse.org/bugs/show_bug.cgi?id=22517)
 				true) != null;
 	}
 
 	/**
 	 * @see IOpenable
 	 */
 	public void save(IProgressMonitor pm, boolean force) throws ModelException {
 		if (isReadOnly()) {
 			throw new ModelException(new ModelStatus(
 					IModelStatusConstants.READ_ONLY, this));
 		}
 		IBuffer buf = getBuffer();
 		if (buf != null) { // some Openables (like a ScriptProject) don't have a
 							// buffer
 			buf.save(pm, force);
 			this.makeConsistent(pm); // update the element info of this
 										// element
 		}
 	}
 
 	/**
 	 * Find enclosing project fragment if any
 	 */
 	public ProjectFragment getProjectFragment() {
 		return (ProjectFragment) getAncestor(IModelElement.PROJECT_FRAGMENT);
 	}
 
 	/** Code Completion */
 	protected void codeComplete(org.eclipse.dltk.compiler.env.ISourceModule cu,
 			int position, CompletionRequestor requestor, WorkingCopyOwner owner)
 			throws ModelException {
 		if (requestor == null) {
 			throw new IllegalArgumentException("Completion requestor cannot be null");
 		}
 
 		IBuffer buffer = getBuffer();
 		if (buffer == null) {
 			return;
 		}
 		if (position < -1 || position > buffer.getLength()) {
			throw new ModelException(new ModelStatus(
					IModelStatusConstants.INDEX_OUT_OF_BOUNDS));
 		}
 		
 		ScriptProject project = (ScriptProject) getScriptProject();
 
 		//TODO: Add searchable environment support.
 		SearchableEnvironment environment = project
 				.newSearchableNameEnvironment(owner);
 
 		// set unit to skip
 		environment.unitToSkip = cu;
 
 		IDLTKLanguageToolkit toolkit = null;
 		
 		//TODO: rewrite this ugly code
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(this);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		
 		if (toolkit == null) {
 			toolkit = DLTKLanguageManager.findToolkit(this.getResource());
 			if (toolkit == null) {
 				return;
 			}
 		}
 		
 		// code complete
 		ICompletionEngine engine = null;
 		try {
 			engine = DLTKLanguageManager.getCompletionEngine(toolkit.getNatureId());
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if( engine == null ) {
 			return;
 		}
 		engine.setEnvironment(environment);
 		engine.setRequestor(requestor);
 		engine.setOptions(project.getOptions(true));
 		engine.setProject(project);
 		
 		/*toolkit.createCompletionEngine(environment,
 				requestor, project.getOptions(true), project);*/
 
 		engine.complete(cu, position, 0);
 
 		if (NameLookup.VERBOSE) {
 			System.out
 					.println(Thread.currentThread()
 							+ " TIME SPENT in NameLoopkup#seekTypesInSourcePackage: " + environment.nameLookup.timeSpentInSeekTypesInSourcePackage + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 			System.out
 					.println(Thread.currentThread()
 							+ " TIME SPENT in NameLoopkup#seekTypesInBinaryPackage: " + environment.nameLookup.timeSpentInSeekTypesInBinaryPackage + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 	}
 
 	protected IModelElement[] codeSelect(
 			org.eclipse.dltk.compiler.env.ISourceModule cu, int offset,
 			int length, WorkingCopyOwner owner) throws ModelException {
 
 		ScriptProject project = (ScriptProject) getScriptProject();
 		SearchableEnvironment environment = null;
 		try {
 			environment = project.newSearchableNameEnvironment(owner);
 		} catch (ModelException e) {
 			// e.printStackTrace();
 			return new IModelElement[0];
 		}
 
 		IBuffer buffer = getBuffer();
 		if (buffer == null) {
 			return new IModelElement[0];
 		}
 		IDLTKLanguageToolkit toolkit = null;
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(this);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 		if (toolkit == null) {
 			toolkit = DLTKLanguageManager.findToolkit(this.getResource());
 			if (toolkit == null) {
 				if (DLTKCore.VERBOSE) {
 					System.out
 							.println("DLTK.Openable.VERBOSE: Failed to detect language toolkit... for module:"
 									+ this.getResource().getName());
 				}
 				return new IModelElement[0];
 			}
 		}
 
 		int end = buffer.getLength();
 		if (offset < 0 || length < 0 || offset + length > end) {
 			throw new ModelException(new ModelStatus(
 					IModelStatusConstants.INDEX_OUT_OF_BOUNDS));
 		}
 
 		ISelectionEngine engine = null;
 		try {
 			engine = DLTKLanguageManager.getSelectionEngine(toolkit.getNatureId());
 		} catch (CoreException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		if (engine == null) {
 			return new IModelElement[0];
 		}
 		engine.setEnvironment(environment);
 		engine.setOptions(project.getOptions(true));
 //		createSelectionEngine(environment,
 //				project.getOptions(true));
 		
 		IModelElement[] elements = engine.select(cu, offset, offset + length
 				- 1);
 
 		if (NameLookup.VERBOSE) {
 			System.out
 					.println(Thread.currentThread()
 							+ " TIME SPENT in NameLoopkup#seekTypesInSourcePackage: " + environment.nameLookup.timeSpentInSeekTypesInSourcePackage + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 			System.out
 					.println(Thread.currentThread()
 							+ " TIME SPENT in NameLoopkup#seekTypesInBinaryPackage: " + environment.nameLookup.timeSpentInSeekTypesInBinaryPackage + "ms"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return elements;
 	}
 }
