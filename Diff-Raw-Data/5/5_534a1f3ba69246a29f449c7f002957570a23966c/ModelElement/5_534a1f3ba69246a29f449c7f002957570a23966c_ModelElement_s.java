 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.core;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.PlatformObject;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementMemento;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.IModelStatusConstants;
 import org.eclipse.dltk.core.IOpenable;
 import org.eclipse.dltk.core.IParent;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.ISourceReference;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 import org.eclipse.dltk.internal.core.util.Util;
 import org.eclipse.dltk.utils.CorePrinter;
 
 /**
  * Root of model element handle hierarchy.
  * 
  * @see IModelElement
  */
 
 public abstract class ModelElement extends PlatformObject implements
 		IModelElement, IModelElementMemento {
 
 	public static final char JEM_ESCAPE = '\\';
 	public static final char JEM_SCRIPTPROJECT = '=';
 	public static final char JEM_PROJECTFRAGMENT = '/';
 	public static final char JEM_SCRIPTFOLDER = '<';
 	public static final char JEM_FIELD = '^';
 	public static final char JEM_METHOD = '~';
 	public static final char JEM_SOURCEMODULE = '{';
 	public static final char JEM_TYPE = '[';
 	public static final char JEM_IMPORTDECLARATION = '&';
 	public static final char JEM_COUNT = '!';
 	public static final char JEM_LOCALVARIABLE = '@';
 	public static final char JEM_TYPE_PARAMETER = ']';
 	public static final char JEM_PACKAGEDECLARATION = '%';
 
 	/**
 	 * This Item is for direct user element handle. Resolving of elements with
 	 * such delimiter requires building of the model.
 	 */
 	public static final char JEM_USER_ELEMENT = '}';
 	public static final String JEM_USER_ELEMENT_ENDING = "=/<^~{[&!@]%}";
 
 	// Used to replace path / or \\ symbols in external package names and
 	// archives.
 	public static final char JEM_SKIP_DELIMETER = '>';
 
 	/**
 	 * This element's parent, or <code>null</code> if this element does not have
 	 * a parent.
 	 */
 	protected ModelElement parent;
 
 	protected static final ModelElement[] NO_ELEMENTS = new ModelElement[0];
 	protected static final Object NO_INFO = new Object();
 
 	/**
 	 * Constructs a handle for a model element with the given parent element.
 	 * 
 	 * @param parent
 	 *            The parent of model element
 	 * 
 	 * @exception IllegalArgumentException
 	 *                if the type is not one of the valid model element type
 	 *                constants
 	 * 
 	 */
 	protected ModelElement(ModelElement parent) throws IllegalArgumentException {
 		this.parent = parent;
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public boolean exists() {
 
 		try {
			getElementInfo();
			return true;
 		} catch (ModelException e) {
 			// element doesn't exist: return false
 		}
 		return false;
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public IModelElement getAncestor(int ancestorType) {
 
 		IModelElement element = this;
 		while (element != null) {
 			if (element.getElementType() == ancestorType)
 				return element;
 			element = element.getParent();
 		}
 		return null;
 	}
 
 	/**
 	 * @see IOpenable
 	 */
 	public void close() throws ModelException {
 		ModelManager.getModelManager().removeInfoAndChildren(this);
 	}
 
 	/**
 	 * This element is being closed. Do any necessary cleanup.
 	 */
 	protected abstract void closing(Object info) throws ModelException;
 
 	/**
 	 * Returns the info for this handle. If this element is not already open, it
 	 * and all of its parents are opened. Does not return null. NOTE: BinaryType
 	 * infos are NOT rooted under ModelElementInfo.
 	 * 
 	 * @exception ModelException
 	 *                if the element is not present or not accessible
 	 */
 	public Object getElementInfo() throws ModelException {
 		return getElementInfo(null);
 	}
 
 	/**
 	 * Returns the info for this handle. If this element is not already open, it
 	 * and all of its parents are opened. Does not return null. NOTE: BinaryType
 	 * infos are NOT rooted under ModelElementInfo.
 	 * 
 	 * @exception ModelException
 	 *                if the element is not present or not accessible
 	 */
 	public Object getElementInfo(IProgressMonitor monitor)
 			throws ModelException {
 
 		ModelManager manager = ModelManager.getModelManager();
 		Object info = manager.getInfo(this);
 		if (info != null)
 			return info;
 		return openWhenClosed(createElementInfo(), monitor);
 	}
 
 	/*
 	 * Opens an <code>Openable</code> that is known to be closed (no check for
 	 * <code>isOpen()</code>). Returns the created element info.
 	 */
 	protected Object openWhenClosed(Object info, IProgressMonitor monitor)
 			throws ModelException {
 		ModelManager manager = ModelManager.getModelManager();
 		boolean hadTemporaryCache = manager.hasTemporaryCache();
 		try {
 			HashMap newElements = manager.getTemporaryCache();
 			generateInfos(info, newElements, monitor);
 			if (info == null) {
 				info = newElements.get(this);
 			}
 			if (info == null) { // a source ref element could not be opened
 				// close the buffer that was opened for the openable parent
 				// close only the openable's buffer (see
 				// https://bugs.eclipse.org/bugs/show_bug.cgi?id=62854)
 				Openable openable = (Openable) getOpenable();
 				if (newElements.containsKey(openable)) {
 					openable.closeBuffer();
 				}
 				throw newNotPresentException();
 			}
 			if (!hadTemporaryCache) {
 				manager.putInfos(this, newElements);
 			}
 		} catch (CoreException e) {
 			// DLTKCore.error("openWhemClosed error", e);
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		} finally {
 			if (!hadTemporaryCache) {
 				manager.resetTemporaryCache();
 			}
 		}
 		return info;
 	}
 
 	/*
 	 * @see IModelElement
 	 */
 	public IOpenable getOpenable() {
 		return this.getOpenableParent();
 	}
 
 	/**
 	 * Return the first instance of IOpenable in the parent hierarchy of this
 	 * element.
 	 * 
 	 * <p>
 	 * Subclasses that are not IOpenable's must override this method.
 	 */
 	public IOpenable getOpenableParent() {
 		return (IOpenable) this.parent;
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public IModelElement getParent() {
 		return this.parent;
 	}
 
 	/*
 	 * Returns a new element info for this element.
 	 */
 	protected abstract Object createElementInfo();
 
 	/**
 	 * Generates the element infos for this element, its ancestors (if they are
 	 * not opened) and its children (if it is an Openable). Puts the newly
 	 * created element info in the given map.
 	 */
 	protected abstract void generateInfos(Object info, HashMap newElements,
 			IProgressMonitor pm) throws ModelException;
 
 	/**
 	 * @see IAdaptable
 	 */
 	public String getElementName() {
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * Creates and returns a new not present exception for this element.
 	 */
 	public ModelException newNotPresentException() {
 		return new ModelException(new ModelStatus(
 				IModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
 	}
 
 	/**
 	 * Returns true if this handle represents the same model element as the
 	 * given handle. By default, two handles represent the same element if they
 	 * are identical or if they represent the same type of element, have equal
 	 * names, parents, and occurrence counts.
 	 * 
 	 * <p>
 	 * If a subclass has other requirements for equality, this method must be
 	 * overridden.
 	 * 
 	 * @see Object#equals
 	 */
 	public boolean equals(Object o) {
 		if (this == o)
 			return true;
 		// model parent is null
 		if (this.parent == null) {
 			return super.equals(o);
 		}
 		if (o == null) {
 			return false;
 		}
 		// assume instanceof check is done in subclass
 		final ModelElement other = (ModelElement) o;
 		return getElementName().equals(other.getElementName())
 				&& this.parent.equals(other.parent);
 	}
 
 	/**
 	 * Returns the hash code for this model element. By default, the hash code
 	 * for an element is a combination of its name and parent's hash code.
 	 * Elements with other requirements must override this method.
 	 */
 	public int hashCode() {
 		if (this.parent == null)
 			return super.hashCode();
 		return Util.combineHashCodes(getElementName().hashCode(), this.parent
 				.hashCode());
 	}
 
 	/**
 	 * Returns true if this element is an ancestor of the given element,
 	 * otherwise false.
 	 */
 	public boolean isAncestorOf(IModelElement e) {
 		IModelElement parentElement = e.getParent();
 		while (parentElement != null && !parentElement.equals(this)) {
 			parentElement = parentElement.getParent();
 		}
 		return parentElement != null;
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public boolean isReadOnly() {
 		return false;
 	}
 
 	/**
 	 * Returns a collection of (immediate) children of this node of the
 	 * specified type.
 	 * 
 	 * @param type
 	 *            - one of the EM_* constants defined by ModelElement
 	 */
 	protected List<IModelElement> getChildrenOfType(int type)
 			throws ModelException {
 		return getChildrenOfType(type, null);
 	}
 
 	protected List<IModelElement> getChildrenOfType(int type,
 			IProgressMonitor monitor) throws ModelException {
 		IModelElement[] children = getChildren(monitor);
 		int size = children.length;
 		List<IModelElement> list = new ArrayList<IModelElement>(size);
 		for (int i = 0; i < size; ++i) {
 			IModelElement elt = (IModelElement) children[i];
 			if (elt.getElementType() == type) {
 				list.add(elt);
 			}
 		}
 		return list;
 	}
 
 	/**
 	 * @see IParent
 	 */
 	public IModelElement[] getChildren() throws ModelException {
 		return getChildren(null);
 	}
 
 	public IModelElement[] getChildren(IProgressMonitor monitor)
 			throws ModelException {
 		Object elementInfo = getElementInfo(monitor);
 		if (elementInfo instanceof ModelElementInfo) {
 			return ((ModelElementInfo) elementInfo).getChildren();
 		} else {
 			return NO_ELEMENTS;
 		}
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public IScriptModel getModel() {
 		IModelElement current = this;
 		do {
 			if (current instanceof IScriptModel)
 				return (IScriptModel) current;
 		} while ((current = current.getParent()) != null);
 		return null;
 	}
 
 	/**
 	 * Creates and returns a new model exception for this element with the given
 	 * status.
 	 */
 	public ModelException newModelException(IStatus status) {
 		if (status instanceof IModelStatus)
 			return new ModelException((IModelStatus) status);
 		else
 			return new ModelException(new ModelStatus(status.getSeverity(),
 					status.getCode(), status.getMessage()));
 	}
 
 	/**
 	 * @see IModelElement
 	 */
 	public IScriptProject getScriptProject() {
 		IModelElement current = this;
 		do {
 			if (current instanceof IScriptProject)
 				return (IScriptProject) current;
 		} while ((current = current.getParent()) != null);
 		return null;
 	}
 
 	public boolean hasChildren() throws ModelException {
 		// if I am not open, return true to avoid opening (case of a project, a
 		// source module or a binary file).
 		Object elementInfo = ModelManager.getModelManager().getInfo(this);
 		if (elementInfo instanceof ModelElementInfo) {
 			return ((ModelElementInfo) elementInfo).getChildren().length > 0;
 		} else {
 			return true;
 		}
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	protected String tabString(int tab) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = tab; i > 0; i--)
 			buffer.append("  "); //$NON-NLS-1$
 		return buffer.toString();
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	public String toDebugString() {
 		StringBuffer buffer = new StringBuffer();
 		this.toStringInfo(0, buffer, NO_INFO, true/* show resolved info */);
 		return buffer.toString();
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	public String toString() {
 		StringBuffer buffer = new StringBuffer();
 		toString(0, buffer);
 		return buffer.toString();
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	protected void toString(int tab, StringBuffer buffer) {
 		Object info = this.toStringInfo(tab, buffer);
 		if (tab == 0) {
 			this.toStringAncestors(buffer);
 		}
 		this.toStringChildren(tab, buffer, info);
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	public String toStringWithAncestors() {
 		return toStringWithAncestors(true/* show resolved info */);
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	public String toStringWithAncestors(boolean showResolvedInfo) {
 		StringBuffer buffer = new StringBuffer();
 		this.toStringInfo(0, buffer, NO_INFO, showResolvedInfo);
 		this.toStringAncestors(buffer);
 		return buffer.toString();
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	protected void toStringAncestors(StringBuffer buffer) {
 		ModelElement parentElement = (ModelElement) this.getParent();
 		if (parentElement != null && parentElement.getParent() != null) {
 			buffer.append(" [in "); //$NON-NLS-1$
 			parentElement.toStringInfo(0, buffer, NO_INFO, false); // don't show
 			// resolved
 			// info
 			parentElement.toStringAncestors(buffer);
 			buffer.append("]"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
 		if (info == null || !(info instanceof ModelElementInfo))
 			return;
 		IModelElement[] children = ((ModelElementInfo) info).getChildren();
 		for (int i = 0; i < children.length; i++) {
 			buffer.append("\n"); //$NON-NLS-1$
 			((ModelElement) children[i]).toString(tab + 1, buffer);
 		}
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	public Object toStringInfo(int tab, StringBuffer buffer) {
 		Object info = ModelManager.getModelManager().peekAtInfo(this);
 		this.toStringInfo(tab, buffer, info, true/* show resolved info */);
 		return info;
 	}
 
 	/**
 	 * Debugging purposes
 	 * 
 	 * @param showResolvedInfo
 	 *            TODO
 	 */
 	protected void toStringInfo(int tab, StringBuffer buffer, Object info,
 			boolean showResolvedInfo) {
 		buffer.append(this.tabString(tab));
 		toStringName(buffer);
 		if (info == null) {
 			buffer.append(" (not open)"); //$NON-NLS-1$
 		}
 	}
 
 	/**
 	 * Debugging purposes
 	 */
 	protected void toStringName(StringBuffer buffer) {
 		buffer.append(getElementName());
 	}
 
 	/**
 	 * Returns the element that is located at the given source position in this
 	 * element. This is a helper method for
 	 * <code>ISourceModule#getElementAt</code>, and only works on compilation
 	 * units and types. The position given is known to be within this element's
 	 * source range already, and if no finer grained element is found at the
 	 * position, this element is returned.
 	 */
 	protected IModelElement getSourceElementAt(int position)
 			throws ModelException {
 		IModelElement res = getSourceElementAtTop(position);
 		if (res != this)
 			return res;
 
 		if (this instanceof ISourceReference) {
 			IModelElement[] children = getChildren();
 			for (int i = children.length - 1; i >= 0; i--) {
 				IModelElement aChild = children[i];
 				if (aChild instanceof SourceRefElement) {
 					SourceRefElement child = (SourceRefElement) children[i];
 					if (child instanceof IParent) {
 						res = child.getSourceElementAt(position);
 						if (res != child)
 							return res;
 					}
 				}
 			}
 		} else {
 			// should not happen
 			Assert.isTrue(false);
 		}
 		return this;
 	}
 
 	/**
 	 * Returns the element that is located at the given source position in this
 	 * element. This is a helper method for
 	 * <code>ISourceModule#getElementAt</code>, and only works on compilation
 	 * units and types. The position given is known to be within this element's
 	 * source range already, and if no finer grained element is found at the
 	 * position, this element is returned.
 	 */
 	protected IModelElement getSourceElementAtTop(int position)
 			throws ModelException {
 		if (this instanceof ISourceReference) {
 			IModelElement[] children = getChildren();
 			for (int i = children.length - 1; i >= 0; i--) {
 				IModelElement aChild = children[i];
 				if (aChild instanceof SourceRefElement) {
 					SourceRefElement child = (SourceRefElement) children[i];
 					ISourceRange range = child.getSourceRange();
 					int start = range.getOffset();
 					int end = start + range.getLength();
 					if (start <= position && position <= end) {
 						if (child instanceof IField) {
 							// check muti-declaration case (see
 							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=39943
 							// )
 							int declarationStart = start;
 							SourceRefElement candidate = null;
 							do {
 								// check name range
 								range = ((IField) child).getNameRange();
 								if (position <= range.getOffset()
 										+ range.getLength()) {
 									candidate = child;
 								} else {
 									return candidate == null ? child
 											.getSourceElementAt(position)
 											: candidate
 													.getSourceElementAt(position);
 								}
 								child = --i >= 0 ? (SourceRefElement) children[i]
 										: null;
 							} while (child != null
 									&& child.getSourceRange().getOffset() == declarationStart);
 							// position in field's type: use first field
 							return candidate.getSourceElementAt(position);
 						} else if (child instanceof IParent) {
 							return child.getSourceElementAt(position);
 						} else {
 							return child;
 						}
 					}
 				}
 			}
 		} else {
 			// should not happen
 			Assert.isTrue(false);
 		}
 		return this;
 	}
 
 	/**
 	 * Only for testing.
 	 * 
 	 * Used to print this node with all sub childs.
 	 * 
 	 * @param output
 	 */
 	public abstract void printNode(CorePrinter output);
 
 	/*
 	 * @see IModelElement#getPrimaryElement()
 	 */
 	public IModelElement getPrimaryElement() {
 		return getPrimaryElement(true);
 	}
 
 	/*
 	 * Returns the primary element. If checkOwner, and the cu owner is primary,
 	 * return this element.
 	 */
 	public IModelElement getPrimaryElement(boolean checkOwner) {
 		return this;
 	}
 
 	public IModelElement getHandleFromMemento(MementoTokenizer memento,
 			WorkingCopyOwner owner) {
 		if (!memento.hasMoreTokens())
 			return this;
 		String token = memento.nextToken();
 		return getHandleFromMemento(token, memento, owner);
 	}
 
 	public abstract IModelElement getHandleFromMemento(String token,
 			MementoTokenizer memento, WorkingCopyOwner owner);
 
 	public String getHandleIdentifier() {
 		return getHandleMemento();
 	}
 
 	public String getHandleMemento() {
 		StringBuffer buff = new StringBuffer();
 		getHandleMemento(buff);
 		return buff.toString();
 	}
 
 	public void getHandleMemento(StringBuffer buff) {
 		((ModelElement) getParent()).getHandleMemento(buff);
 		buff.append(getHandleMementoDelimiter());
 		escapeMementoName(buff, getElementName());
 	}
 
 	protected abstract char getHandleMementoDelimiter();
 
 	protected void escapeMementoName(StringBuffer buffer, String mementoName) {
 		for (int i = 0, length = mementoName.length(); i < length; i++) {
 			char character = mementoName.charAt(i);
 			switch (character) {
 			case JEM_ESCAPE:
 			case JEM_COUNT:
 			case JEM_SCRIPTPROJECT:
 			case JEM_PROJECTFRAGMENT:
 			case JEM_SCRIPTFOLDER:
 			case JEM_FIELD:
 			case JEM_METHOD:
 			case JEM_SOURCEMODULE:
 			case JEM_TYPE:
 			case JEM_IMPORTDECLARATION:
 			case JEM_LOCALVARIABLE:
 			case JEM_TYPE_PARAMETER:
 			case JEM_USER_ELEMENT:
 				buffer.append(JEM_ESCAPE);
 			}
 			buffer.append(character);
 		}
 	}
 
 	public ISourceModule getSourceModule() {
 		return null;
 	}
 
 	public void accept(IModelElementVisitor visitor) throws ModelException {
 		if (visitor.visit(this)) {
 			IModelElement[] elements = getChildren();
 			for (int i = 0; i < elements.length; ++i) {
 				elements[i].accept(visitor);
 			}
 		}
 	}
 }
