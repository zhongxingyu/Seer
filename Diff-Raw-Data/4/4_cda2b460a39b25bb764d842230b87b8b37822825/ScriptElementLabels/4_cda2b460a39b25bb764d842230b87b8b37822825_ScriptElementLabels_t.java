 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.ui;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.dltk.core.BuildpathContainerInitializer;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IBuildpathContainer;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.IScriptFolder;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.internal.core.BuiltinProjectFragment;
 import org.eclipse.dltk.internal.core.ExternalProjectFragment;
 import org.eclipse.ui.model.IWorkbenchAdapter;
 
 /**
  * <code>ScriptElementLabels</code> provides helper methods to render names of
  * Script elements.
  */
 public class ScriptElementLabels {
 	/**
 	 * Method names contain parameter types. e.g. <code>foo(int)</code>
 	 */
 	public final static long M_PARAMETER_TYPES = 1L << 0;
 
 	/**
 	 * Method names contain parameter names. e.g. <code>foo(index)</code>
 	 */
 	public final static long M_PARAMETER_NAMES = 1L << 1;
 
 	public final static long M_PARAMETER_INITIALIZERS = 1L << 49;
 
 	/**
 	 * Method names contain type parameters prepended. e.g.
 	 * <code><A> foo(A index)</code>
 	 */
 	public final static long M_PRE_TYPE_PARAMETERS = 1L << 2;
 
 	/**
 	 * Method names contain type parameters appended. e.g.
 	 * <code>foo(A index) <A></code>
 	 */
 	public final static long M_APP_TYPE_PARAMETERS = 1L << 3;
 
 	/**
 	 * Method names contain thrown exceptions. e.g.
 	 * <code>foo throws IOException</code>
 	 */
 	public final static long M_EXCEPTIONS = 1L << 4;
 
 	/**
 	 * Method names contain return type (appended) e.g. <code>foo : int</code>
 	 */
 	public final static long M_APP_RETURNTYPE = 1L << 5;
 
 	/**
 	 * Method names contain return type (appended) e.g. <code>int foo</code>
 	 */
 	public final static long M_PRE_RETURNTYPE = 1L << 6;
 
 	/**
 	 * Method names are fully qualified. e.g. <code>java.util.Vector.size</code>
 	 */
 	public final static long M_FULLY_QUALIFIED = 1L << 7;
 
 	/**
 	 * Method names are post qualified. e.g.
 	 * <code>size - java.util.Vector</code>
 	 */
 	public final static long M_POST_QUALIFIED = 1L << 8;
 
 	/**
 	 * Initializer names are fully qualified. e.g.
 	 * <code>java.util.Vector.{ ... }</code>
 	 */
 	public final static long I_FULLY_QUALIFIED = 1L << 10;
 
 	/**
 	 * Type names are post qualified. e.g. <code>{ ... } - java.util.Map</code>
 	 */
 	public final static long I_POST_QUALIFIED = 1L << 11;
 
 	/**
 	 * Field names contain the declared type (appended) e.g.
 	 * <code>fHello : int</code>
 	 */
 	public final static long F_APP_TYPE_SIGNATURE = 1L << 14;
 
 	/**
 	 * Field names contain the declared type (prepended) e.g.
 	 * <code>int fHello</code>
 	 */
 	public final static long F_PRE_TYPE_SIGNATURE = 1L << 15;
 
 	/**
 	 * Fields names are fully qualified. e.g. <code>java.lang.System.out</code>
 	 */
 	public final static long F_FULLY_QUALIFIED = 1L << 16;
 
 	/**
 	 * Fields names are post qualified. e.g. <code>out - java.lang.System</code>
 	 */
 	public final static long F_POST_QUALIFIED = 1L << 17;
 
 	/**
 	 * Type names are fully qualified. e.g. <code>java.util.Map.MapEntry</code>
 	 */
 	public final static long T_FULLY_QUALIFIED = 1L << 18;
 
 	/**
 	 * Type names are type container qualified. e.g. <code>Map.MapEntry</code>
 	 */
 	public final static long T_CONTAINER_QUALIFIED = 1L << 19;
 
 	/**
 	 * Type names are post qualified. e.g. <code>MapEntry - java.util.Map</code>
 	 */
 	public final static long T_POST_QUALIFIED = 1L << 20;
 
 	/**
 	 * Type names contain type parameters. e.g. <code>Map&lt;S, T&gt;</code>
 	 */
 	public final static long T_TYPE_PARAMETERS = 1L << 21;
 
 	/**
 	 * Declarations (import container / declaration, package declaration) are
 	 * qualified. e.g. <code>java.util.Vector.class/import container</code>
 	 */
 	public final static long D_QUALIFIED = 1L << 24;
 
 	/**
 	 * Declarations (import container / declaration, package declaration) are
 	 * post qualified. e.g.
 	 * <code>import container - java.util.Vector.class</code>
 	 */
 	public final static long D_POST_QUALIFIED = 1L << 25;
 
 	/**
 	 * Class file names are fully qualified. e.g.
 	 * <code>java.util.Vector.class</code>
 	 */
 	public final static long CF_QUALIFIED = 1L << 27;
 
 	/**
 	 * Class file names are post qualified. e.g.
 	 * <code>Vector.class - java.util</code>
 	 */
 	public final static long CF_POST_QUALIFIED = 1L << 28;
 
 	/**
 	 * Compilation unit names are fully qualified. e.g.
 	 * <code>java.util.Vector.java</code>
 	 */
 	public final static long CU_QUALIFIED = 1L << 31;
 
 	/**
 	 * Compilation unit names are post qualified. e.g.
 	 * <code>Vector.java - java.util</code>
 	 */
 	public final static long CU_POST_QUALIFIED = 1L << 32;
 
 	/**
 	 * Package names are qualified. e.g. <code>MyProject/src/java.util</code>
 	 */
 	public final static long P_QUALIFIED = 1L << 35;
 
 	/**
 	 * Package names are post qualified. e.g.
 	 * <code>java.util - MyProject/src</code>
 	 */
 	public final static long P_POST_QUALIFIED = 1L << 36;
 
 	/**
 	 * Package names are compressed. e.g. <code>o*.e*.search</code>
 	 */
 	public final static long P_COMPRESSED = 1L << 37;
 
 	/**
 	 * Package Fragment Roots contain variable name if from a variable.
 	 */
 	public final static long ROOT_VARIABLE = 1L << 40;
 
 	/**
 	 * Package Fragment Roots contain the project name if not an archive
 	 * (prepended). e.g. <code>MyProject/src</code>
 	 */
 	public final static long ROOT_QUALIFIED = 1L << 41;
 
 	/**
 	 * Package Fragment Roots contain the project name if not an archive
 	 * (appended). e.g. <code>src - MyProject</code>
 	 */
 	public final static long ROOT_POST_QUALIFIED = 1L << 42;
 
 	/**
 	 * Add root path to all elements except Package Fragment Roots and script
 	 * projects. Option only applies to getElementLabel
 	 */
 	public final static long APPEND_ROOT_PATH = 1L << 43;
 
 	/**
 	 * Add root path to all elements except Package Fragment Roots and script
 	 * projects. Option only applies to getElementLabel
 	 */
 	public final static long PREPEND_ROOT_PATH = 1L << 44;
 
 	/**
 	 * Post qualify referenced package fragment roots.
 	 */
 	public final static long REFERENCED_ROOT_POST_QUALIFIED = 1L << 45;
 
 	/**
 	 * Specified to use the resolved information of a IType, IMethod or IField.
 	 * See {@link IType#isResolved()}. If resolved information is available,
 	 * types will be rendered with type parameters of the instantiated type.
 	 * Resolved method render with the parameter types of the method instance.
 	 * <code>Vector<String>.get(String)</code>
 	 */
 	public final static long USE_RESOLVED = 1L << 48;
 
 	public final static long APPEND_FILE = 1L << 63;
 
 	/**
 	 * Qualify all elements
 	 */
 	public final static long ALL_FULLY_QUALIFIED = new Long(F_FULLY_QUALIFIED
 			| M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED
 			| D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED | P_QUALIFIED
 			| ROOT_QUALIFIED).longValue();
 
 	/**
 	 * Post qualify all elements
 	 */
 	public final static long ALL_POST_QUALIFIED = new Long(F_POST_QUALIFIED
 			| M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED
 			| D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED
 			| P_POST_QUALIFIED | ROOT_POST_QUALIFIED).longValue();
 
 	/**
 	 * Default options (M_PARAMETER_TYPES, M_APP_TYPE_PARAMETERS &
 	 * T_TYPE_PARAMETERS enabled)
 	 */
 	public final static long ALL_DEFAULT = new Long(M_PARAMETER_NAMES
 			| T_TYPE_PARAMETERS | M_PARAMETER_INITIALIZERS).longValue();
 
 	public final static long F_CATEGORY = 1L << 49;
 
 	/**
 	 * Prepend first category (if any) to method.
 	 * 
 	 * 
 	 */
 	public final static long M_CATEGORY = 1L << 50;
 
 	/**
 	 * Prepend first category (if any) to type.
 	 * 
 	 * 
 	 */
 	public final static long T_CATEGORY = 1L << 51;
 
 	public final static long ALL_CATEGORY = new Long(
 			ScriptElementLabels.F_CATEGORY | ScriptElementLabels.M_CATEGORY
 					| ScriptElementLabels.T_CATEGORY).longValue();
 
 	/**
 	 * Default qualify options (All except Root and Package)
 	 */
 	public final static long DEFAULT_QUALIFIED = new Long(F_FULLY_QUALIFIED
 			| M_FULLY_QUALIFIED | I_FULLY_QUALIFIED | T_FULLY_QUALIFIED
 			| D_QUALIFIED | CF_QUALIFIED | CU_QUALIFIED).longValue();
 
 	/**
 	 * Default post qualify options (All except Root and Package)
 	 */
 	public final static long DEFAULT_POST_QUALIFIED = new Long(F_POST_QUALIFIED
 			| M_POST_QUALIFIED | I_POST_QUALIFIED | T_POST_QUALIFIED
 			| D_POST_QUALIFIED | CF_POST_QUALIFIED | CU_POST_QUALIFIED)
 			.longValue();
 
 	/**
 	 * User-readable string for separating post qualified names (e.g. " - ").
 	 */
 	public final static String CONCAT_STRING = " - "; //$NON-NLS-1$
 
 	/**
 	 * User-readable string for separating list items (e.g. ", ").
 	 */
 	public final static String COMMA_STRING = ", "; //$NON-NLS-1$
 
 	/**
 	 * User-readable string for separating the return type (e.g. " : ").
 	 */
 	public final static String DECL_STRING = " : "; //$NON-NLS-1$
 
 	/**
 	 * User-readable string for ellipsis ("...").
 	 */
 	public final static String ELLIPSIS_STRING = "..."; //$NON-NLS-1$
 
 	/**
 	 * User-readable string for the default package name (e.g. "(default
 	 * package)").
 	 */
 	public final static String DEFAULT_PACKAGE = "(default package)"; //$NON-NLS-1$
 
 	public final static String BUILTINS_FRAGMENT = "(builtins)"; //$NON-NLS-1$
 
 	private final static long QUALIFIER_FLAGS = P_COMPRESSED | USE_RESOLVED;
 
 	private static ScriptElementLabels sInstanceO = new ScriptElementLabels() {
 	};
 	private static ScriptElementLabels sInstance = new ScriptElementLabels() {
 		private ScriptElementLabels getLabels(IModelElement element) {
 			IDLTKUILanguageToolkit languageToolkit = DLTKUILanguageManager
 					.getLanguageToolkit(element);
 			if (languageToolkit != null) {
 				ScriptElementLabels scriptElementLabels = languageToolkit
 						.getScriptElementLabels();
 				if (scriptElementLabels != null) {
 					return scriptElementLabels;
 				}
 			}
 			return sInstanceO;
 		}
 
 		public String getContainerEntryLabel(IPath containerPath,
 				IScriptProject project) throws ModelException {
 
 			return getLabels(project).getContainerEntryLabel(containerPath,
 					project);
 		}
 
 		public void getDeclarationLabel(IModelElement declaration, long flags,
 				StringBuffer buf) {
 			getLabels(declaration).getDeclarationLabel(declaration, flags, buf);
 		}
 
 		public void getElementLabel(IModelElement element, long flags,
 				StringBuffer buf) {
 			getLabels(element).getElementLabel(element, flags, buf);
 		}
 
 		public String getElementLabel(IModelElement element, long flags) {
 			return getLabels(element).getElementLabel(element, flags);
 		}
 
 		public void getProjectFragmentLabel(IProjectFragment root, long flags,
 				StringBuffer buf) {
 			getLabels(root).getProjectFragmentLabel(root, flags, buf);
 		}
 
 		public void getScriptFolderLabel(IProjectFragment pack, long flags,
 				StringBuffer buf) {
 			getLabels(pack).getScriptFolderLabel(pack, flags, buf);
 		}
 
 		protected void getTypeLabel(IType type, long flags, StringBuffer buf) {
 			getLabels(type).getTypeLabel(type, flags, buf);
 		}
 	};
 
 	public static ScriptElementLabels getDefault() {
 		return sInstance;
 	}
 
 	protected ScriptElementLabels() {
 
 	}
 
 	protected char getTypeDelimiter() {
 		return '.';
 	}
 
 	private static final boolean getFlag(long flags, long flag) {
 
 		return (flags & flag) != 0;
 	}
 
 	/*
 	 * Package name compression
 	 */
 
 	private static String fgPkgNamePrefix;
 
 	private static String fgPkgNamePostfix;
 
 	private static int fgPkgNameChars;
 
 	private static int fgPkgNameLength = -1;
 
 	/**
 	 * Returns the label of the given object. The object must be of type
 	 * {@link IScriptElement} or adapt to {@link IWorkbenchAdapter}. The empty
 	 * string is returned if the element type is not known.
 	 * 
 	 * @param obj
 	 *            Object to get the label from.
 	 * @param flags
 	 *            The rendering flags
 	 * @return Returns the label or the empty string if the object type is not
 	 *         supported.
 	 */
 	public String getTextLabel(Object obj, long flags) {
 		if (obj instanceof IModelElement) {
 			IModelElement element = (IModelElement) obj;
 			if (this.equals(sInstance)) {
 				IDLTKUILanguageToolkit uiToolkit = DLTKUILanguageManager
 						.getLanguageToolkit(element);
 				if (uiToolkit != null) {
 					ScriptElementLabels labels = uiToolkit
 							.getScriptElementLabels();
 					if (labels != null) {
 						return labels.getElementLabel(element, flags);
 					}
 				}
 			}
 			return getElementLabel((IModelElement) obj, flags);
 		} else if (obj instanceof IAdaptable) {
 			IWorkbenchAdapter wbadapter = (IWorkbenchAdapter) ((IAdaptable) obj)
 					.getAdapter(IWorkbenchAdapter.class);
 			if (wbadapter != null) {
 				return wbadapter.getLabel(obj);
 			}
 		}
 		return ""; //$NON-NLS-1$
 	}
 
 	/**
 	 * Returns the label for a model element with the flags as defined by this
 	 * class.
 	 * 
 	 * @param element
 	 *            The element to render.
 	 * @param flags
 	 *            The rendering flags.
 	 * @return the label of the model element
 	 */
 	public String getElementLabel(IModelElement element, long flags) {
 		StringBuffer buf = new StringBuffer(61);
 		getElementLabel(element, flags, buf);
 		return buf.toString();
 	}
 
 	/**
 	 * Returns the label for a model element with the flags as defined by this
 	 * class.
 	 * 
 	 * @param element
 	 *            The element to render.
 	 * @param flags
 	 *            The rendering flags.
 	 * @param buf
 	 *            The buffer to append the resulting label to.
 	 */
 	public void getElementLabel(IModelElement element, long flags,
 			StringBuffer buf) {
 
 		int type = element.getElementType();
 		IProjectFragment root = null;
 
 		IScriptProject project = element.getScriptProject();
 
 		if (type != IModelElement.SCRIPT_MODEL
 				&& type != IModelElement.SCRIPT_PROJECT
 				&& type != IModelElement.PROJECT_FRAGMENT) {
 			IResource resource = element.getResource();
 			if (resource != null) {
 				root = project.getProjectFragment(resource);
 			}
 			if (root == null) {
 				root = findProjectFragment(element);
 			}
 		}
 		if (root != null && getFlag(flags, PREPEND_ROOT_PATH)) {
 			getProjectFragmentLabel(root, ROOT_QUALIFIED, buf);
 			buf.append(CONCAT_STRING);
 		}
 
 		switch (type) {
 		case IModelElement.METHOD:
 			getMethodLabel((IMethod) element, flags, buf);
 			break;
 		case IModelElement.FIELD:
 			getFieldLabel((IField) element, flags, buf);
 			break;
 		case IModelElement.TYPE:
 			getTypeLabel((IType) element, flags, buf);
 			break;
 		case IModelElement.SOURCE_MODULE:
 			getSourceModel((ISourceModule) element, flags, buf);
 			break;
 		case IModelElement.SCRIPT_PROJECT:
 		case IModelElement.SCRIPT_MODEL:
 			buf.append(element.getElementName());
 			break;
 		case IModelElement.PACKAGE_DECLARATION:
 			getDeclarationLabel(element, flags, buf);
 			break;
 		case IModelElement.SCRIPT_FOLDER:
 			getScriptFolderLabel((IScriptFolder) element, flags, buf);
 			break;
 		case IModelElement.PROJECT_FRAGMENT:
 			getProjectFragmentLabel((IProjectFragment) element, flags, buf);
 			break;
 		default:
 			buf.append(element.getElementName());
 		}
 
 		if (root != null && getFlag(flags, APPEND_ROOT_PATH)) {
 			buf.append(CONCAT_STRING);
 			getProjectFragmentLabel(root, ROOT_QUALIFIED, buf);
 		}
 
 		ISourceModule sourceModule = (ISourceModule) element
 				.getAncestor(IModelElement.SOURCE_MODULE);
 		if (sourceModule != null && getFlag(flags, APPEND_FILE)) {
 			buf.append(CONCAT_STRING);
 			getSourceModel(sourceModule, flags, buf);
 		}
 	}
 
 	private IProjectFragment findProjectFragment(IModelElement element) {
 		while (element != null
 				&& element.getElementType() != IModelElement.PROJECT_FRAGMENT) {
 			element = element.getParent();
 		}
 		return (IProjectFragment) element;
 	}
 
 	protected void getScriptFolderLabel(IScriptFolder folder, StringBuffer buf) {
 		buf.append(folder.getElementName()/*
 											 * .replace(IScriptFolder.PACKAGE_DELIMITER,
 											 * '.')
 											 */);
 	}
 
 	private void getScriptFolderLabel(IScriptFolder folder, long flags,
 			StringBuffer buf) {
 		if (getFlag(flags, P_QUALIFIED)) {
 			getProjectFragmentLabel((IProjectFragment) folder.getParent(),
 					ROOT_QUALIFIED, buf);
 			buf.append('/');
 		}
 		// refreshPackageNamePattern();
 		if (folder.isRootFolder()) {
 			buf.append(DEFAULT_PACKAGE);
 		} else if (getFlag(flags, P_COMPRESSED) && fgPkgNameLength >= 0) {
 			String name = folder.getElementName();
 			int start = 0;
 			int dot = name.indexOf(IScriptFolder.PACKAGE_DELIMITER, start);
 			while (dot > 0) {
 				if (dot - start > fgPkgNameLength - 1) {
 					buf.append(fgPkgNamePrefix);
 					if (fgPkgNameChars > 0)
 						buf.append(name.substring(start, Math.min(start
 								+ fgPkgNameChars, dot)));
 					buf.append(fgPkgNamePostfix);
 				} else
 					buf.append(name.substring(start, dot + 1));
 				start = dot + 1;
 				dot = name.indexOf(IScriptFolder.PACKAGE_DELIMITER, start);
 			}
 			buf.append(name.substring(start));
 		} else {
 			getScriptFolderLabel(folder, buf);
 		}
 		if (getFlag(flags, P_POST_QUALIFIED)) {
 			buf.append(CONCAT_STRING);
 			getProjectFragmentLabel((IProjectFragment) folder.getParent(),
 					ROOT_QUALIFIED, buf);
 		}
 	}
 
 	private void getSourceModel(ISourceModule module, long flags,
 			StringBuffer buf) {
 		if (getFlag(flags, CU_QUALIFIED)) {
 			IScriptFolder pack = (IScriptFolder) module.getParent();
 
 			getScriptFolderLabel(pack, (flags & QUALIFIER_FLAGS), buf);
 			buf.append(getTypeDelimiter());
 		}
 		buf.append(module.getElementName());
 
 		if (getFlag(flags, CU_POST_QUALIFIED)) {
 			buf.append(CONCAT_STRING);
 			getScriptFolderLabel((IScriptFolder) module.getParent(), flags
 					& QUALIFIER_FLAGS, buf);
 		}
 	}
 
 	protected void getTypeLabel(IType type, long flags, StringBuffer buf) {
 
 		IScriptProject project = type.getScriptProject();
 
 		if (getFlag(flags, T_FULLY_QUALIFIED)) {
 			IResource resource = type.getResource();
 			IProjectFragment pack = null;
 			if (resource != null) {
 				pack = project.getProjectFragment(resource);
 			} else {
 				pack = findProjectFragment(type);
 			}
 			if (pack == null) {
 				pack = findProjectFragment(type);
 			}
 			getScriptFolderLabel(pack, (flags & QUALIFIER_FLAGS), buf);
 		}
 
 		if (getFlag(flags, T_FULLY_QUALIFIED | T_CONTAINER_QUALIFIED)) {
 			IModelElement elem = type.getParent();
 			IType declaringType = (elem instanceof IType) ? (IType) elem : null;
 			if (declaringType != null) {
 				getTypeLabel(declaringType, T_CONTAINER_QUALIFIED
 						| (flags & QUALIFIER_FLAGS), buf);
 				buf.append(getTypeDelimiter());
 			}
 			int parentType = type.getParent().getElementType();
 			if (parentType == IModelElement.METHOD
 					|| parentType == IModelElement.FIELD) { // anonymous
 				// or
 				// local
 				getElementLabel(type.getParent(),
 						(parentType == IModelElement.METHOD ? M_FULLY_QUALIFIED
 								: F_FULLY_QUALIFIED)
 								| (flags & QUALIFIER_FLAGS), buf);
 				buf.append(getTypeDelimiter());
 			}
 		}
 
 		String typeName = type.getElementName();
 		if (typeName.length() == 0) { // anonymous
 			try {
 				if (type.getParent() instanceof IField) {
 					typeName = '{' + ELLIPSIS_STRING + '}';
 				} else {
 					String[] superNames = type.getSuperClasses();
 					if (superNames != null) {
 						int count = 0;
 						typeName += DECL_STRING;
 						for (int i = 0; i < superNames.length; ++i) {
 
 							if (count > 0) {
 								typeName += COMMA_STRING + " "; //$NON-NLS-1$
 							}
 							typeName += superNames[i];
 							count++;
 						}
 					}
 				}
 			} catch (ModelException e) {
 				// ignore
 				typeName = ""; //$NON-NLS-1$
 			}
 		}
 
 		buf.append(typeName);
 
 		// post qualification
 		if (getFlag(flags, T_POST_QUALIFIED)) {
 			IModelElement elem = type.getParent();
 			IType declaringType = (elem instanceof IType) ? (IType) elem : null;
 			if (declaringType != null) {
 				buf.append(CONCAT_STRING);
 				getTypeLabel(declaringType, T_FULLY_QUALIFIED
 						| (flags & QUALIFIER_FLAGS), buf);
 				int parentType = type.getParent().getElementType();
 				if (parentType == IModelElement.METHOD
 						|| parentType == IModelElement.FIELD) { // anonymous
 					// or
 					// local
 					buf.append(getTypeDelimiter());
 					getElementLabel(type.getParent(), 0, buf);
 				}
 			}
 			int parentType = type.getParent().getElementType();
 			if (parentType == IModelElement.METHOD
 					|| parentType == IModelElement.FIELD) { // anonymous
 				// or
 				// local
 				buf.append(CONCAT_STRING);
 				getElementLabel(type.getParent(),
 						(parentType == IModelElement.METHOD ? M_FULLY_QUALIFIED
 								: F_FULLY_QUALIFIED)
 								| (flags & QUALIFIER_FLAGS), buf);
 			}
 		}
 	}
 
 	private void getFieldLabel(IField field, long flags, StringBuffer buf) {
 
 		buf.append(field.getElementName());
 		// TODO: Add type detection here.
 	}
 
 	private void getMethodLabel(IMethod method, long flags, StringBuffer buf) {
 
 		try {
 			// qualification
 			if (getFlag(flags, M_FULLY_QUALIFIED)) {
 				IType type = method.getDeclaringType();
 				if (type != null) {
 					getTypeLabel(type, T_FULLY_QUALIFIED
 							| (flags & QUALIFIER_FLAGS), buf);
 					buf.append(getTypeDelimiter());
 				}
 			}
 
 			buf.append(method.getElementName());
 
 			// parameters
 			buf.append('(');
 			if (getFlag(flags, M_PARAMETER_TYPES | M_PARAMETER_NAMES)) {
 				// TODO: Add type detection calls from here.
 				String[] names = null;
 				String[] initializers = null;
 				int nParams = 0;
 				if (getFlag(flags, M_PARAMETER_NAMES) && method.exists()) {
 					names = method.getParameters();
 					initializers = method.getParameterInitializers();
 					nParams = names.length;
 				}
 
 				for (int i = 0; i < nParams; i++) {
 					if (i > 0) {
 						buf.append(COMMA_STRING);
 					}
 
 					if (names != null) {
 						buf.append(names[i]);
 
 					}
 
 					if (getFlag(flags, M_PARAMETER_INITIALIZERS)
 							&& initializers != null && initializers[i] != null) {// &&
 						// initializers[i].length()
 						// > 0
 						// ) {
 						buf.append("=\"" + initializers[i] + "\""); //$NON-NLS-1$ //$NON-NLS-2$
 					}
 				}
 			} else {
 				String[] params = method.getParameters();
 				if (params.length > 0) {
 					buf.append(ELLIPSIS_STRING);
 				}
 			}
 			buf.append(')');
 
 			// post qualification
 			if (getFlag(flags, M_POST_QUALIFIED)) {
 				IType declaringType = method.getDeclaringType();
 				if (declaringType != null) {
 					buf.append(CONCAT_STRING);
 					getTypeLabel(declaringType, T_FULLY_QUALIFIED
 							| (flags & QUALIFIER_FLAGS), buf);
 				}
 			}
 
 			// TODO: Add return type method detection here,
 			// if( getFlag( flags, M_APP_RETURNTYPE ) && method.exists( ) ) {
 			// }
 		} catch (ModelException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void getProjectFragmentLabel(IProjectFragment root, long flags,
 			StringBuffer buf) {
 		if (root.isArchive())
 			getArchiveLabel(root, flags, buf);
 		else {
 			if (root.getPath().toString().startsWith(
 					IBuildpathEntry.BUILTIN_EXTERNAL_ENTRY_STR)) {
 				buf.append(BUILTINS_FRAGMENT);
 			} else if (root.isExternal()) {
 				getExternalFolderLabel(root, flags, buf);
 			} else {
 				getFolderLabel(root, flags, buf);
 			}
 		}
 
 	}
 
 	private void getArchiveLabel(IProjectFragment root, long flags,
 			StringBuffer buf) {
 		// Handle variables different
 		boolean external = root.isExternal();
 		if (external)
 			getExternalArchiveLabel(root, flags, buf);
 		else
 			getInternalArchiveLabel(root, flags, buf);
 	}
 
 	private void getExternalArchiveLabel(IProjectFragment root, long flags,
 			StringBuffer buf) {
 		IPath path = root.getPath();
 		if (getFlag(flags, REFERENCED_ROOT_POST_QUALIFIED)) {
 			int segements = path.segmentCount();
 			if (segements > 0) {
 				buf.append(path.segment(segements - 1));
 				if (segements > 1 || path.getDevice() != null) {
 					buf.append(CONCAT_STRING);
 					buf.append(path.removeLastSegments(1).toOSString());
 				}
 			} else {
 				buf.append(path.toOSString());
 			}
 		} else {
 			buf.append(path.toOSString());
 		}
 	}
 
 	/**
 	 * Returns <code>true</code> if the given package fragment root is
 	 * referenced. This means it is own by a different project but is referenced
 	 * by the root's parent. Returns <code>false</code> if the given root
 	 * doesn't have an underlying resource.
 	 * 
 	 * 
 	 */
 	private boolean isReferenced(IProjectFragment root) {
 		IResource resource = root.getResource();
 		if (resource != null) {
 			IProject archiveProject = resource.getProject();
 			IProject container = root.getScriptProject().getProject();
 			return !container.equals(archiveProject);
 		}
 		return false;
 	}
 
 	private void getInternalArchiveLabel(IProjectFragment root, long flags,
 			StringBuffer buf) {
 		IResource resource = root.getResource();
 		boolean rootQualified = getFlag(flags, ROOT_QUALIFIED);
 		boolean referencedQualified = getFlag(flags,
 				REFERENCED_ROOT_POST_QUALIFIED)
 				&& isReferenced(root);
 		if (rootQualified) {
 			buf.append(root.getPath().makeRelative().toString());
 		} else {
 			buf.append(root.getElementName());
 			if (referencedQualified) {
 				buf.append(CONCAT_STRING);
 				buf.append(resource.getParent().getFullPath().makeRelative()
 						.toString());
 			} else if (getFlag(flags, ROOT_POST_QUALIFIED)) {
 				buf.append(CONCAT_STRING);
 				buf
 						.append(root.getParent().getPath().makeRelative()
 								.toString());
 			}
 		}
 	}
 
 	private void getFolderLabel(IProjectFragment root, long flags,
 			StringBuffer buf) {
 
 		IResource resource = root.getResource();
 		boolean rootQualified = getFlag(flags, ROOT_QUALIFIED);
 		boolean referencedQualified = getFlag(flags,
 				REFERENCED_ROOT_POST_QUALIFIED)
 				&& resource != null;
 		if (rootQualified) {
 			buf.append(root.getPath().makeRelative().toString());
 		} else {
 			if (resource != null)
 				buf.append(resource.getProjectRelativePath().toString());
 			else
 				buf.append(root.getElementName());
 			if (referencedQualified) {
 				buf.append(CONCAT_STRING);
 				buf.append(resource.getProject().getName());
 			} else if (getFlag(flags, ROOT_POST_QUALIFIED)) {
 				buf.append(CONCAT_STRING);
 				buf.append(root.getParent().getElementName());
 			}
 		}
 	}
 
 	private void getExternalFolderLabel(IProjectFragment root, long flags,
 			StringBuffer buf) {
 
 		boolean rootQualified = getFlag(flags, ROOT_QUALIFIED);
 		boolean referencedQualified = getFlag(flags,
 				REFERENCED_ROOT_POST_QUALIFIED);
 		if (rootQualified) {
 			buf.append(root.getPath().makeRelative().toString());
 		} else {
 			buf.append(root.getPath().toOSString());
 			if (referencedQualified) {
 				buf.append(CONCAT_STRING);
 				buf.append(root.getScriptProject().getElementName());
 			} else if (getFlag(flags, ROOT_POST_QUALIFIED)) {
 				buf.append(CONCAT_STRING);
 				buf.append(root.getParent().getElementName());
 			}
 		}
 	}
 
 	/**
 	 * Appends the label for a package fragment to a {@link StringBuffer}.
 	 * Considers the P_* flags.
 	 * 
 	 * @param pack
 	 *            The element to render.
 	 * @param flags
 	 *            The rendering flags. Flags with names starting with P_' are
 	 *            considered.
 	 * @param buf
 	 *            The buffer to append the resulting label to.
 	 */
 	public void getScriptFolderLabel(IProjectFragment pack, long flags,
 			StringBuffer buf) {
 
 		if (getFlag(flags, P_QUALIFIED)) {
 			getProjectFragmentLabel((IProjectFragment) pack.getParent(),
 					ROOT_QUALIFIED, buf);
 			buf.append('/');
 		}
 		// refreshPackageNamePattern();
 
 		// if (getFlag(flags, P_COMPRESSED) && fgPkgNameLength >= 0) {
 		// String name = pack.getElementName();
 		// int start = 0;
 		// int dot = name.indexOf(getTypeDelimiter(), start);
 		// while (dot > 0) {
 		// if (dot - start > fgPkgNameLength - 1) {
 		// buf.append(fgPkgNamePrefix);
 		// if (fgPkgNameChars > 0)
 		// buf.append(name.substring(start, Math.min(start + fgPkgNameChars,
 		// dot)));
 		// buf.append(fgPkgNamePostfix);
 		// } else
 		// buf.append(name.substring(start, dot + 1));
 		// start = dot + 1;
 		// dot = name.indexOf(getTypeDelimiter(), start);
 		// }
 		// buf.append(name.substring(start));
 		// } else {
 		if (pack instanceof ExternalProjectFragment) {
 			buf.append(pack.getElementName().replace(
 					ExternalProjectFragment.JEM_SKIP_DELIMETER, Path.SEPARATOR)
 					+ " "); //$NON-NLS-1$
 		} else {
 			if (pack instanceof BuiltinProjectFragment) {
 				buf.append(BUILTINS_FRAGMENT + " "); //$NON-NLS-1$
 			} else {
				if (pack != null) {
					buf.append(pack.getElementName() + " "); //$NON-NLS-1$
				}
 			}
 		}
 		// }
 		if (getFlag(flags, P_POST_QUALIFIED)) {
 			buf.append(CONCAT_STRING);
 			getProjectFragmentLabel((IProjectFragment) pack.getParent(),
 					ROOT_QUALIFIED, buf);
 		}
 	}
 
 	/**
 	 * Returns the label of a buildpath container
 	 * 
 	 * @param containerPath
 	 *            The path of the container.
 	 * @param project
 	 *            The project the container is resolved in.
 	 * @return Returns the label of the buildpath container
 	 * @throws ModelException
 	 *             Thrown when the resolving of the container failed.
 	 */
 	public String getContainerEntryLabel(IPath containerPath,
 			IScriptProject project) throws ModelException {
 		IBuildpathContainer container = DLTKCore.getBuildpathContainer(
 				containerPath, project);
 		if (container != null) {
 			return container.getDescription();
 		}
 		BuildpathContainerInitializer initializer = DLTKCore
 				.getBuildpathContainerInitializer(containerPath.segment(0));
 		if (initializer != null) {
 			return initializer.getDescription(containerPath, project);
 		}
 		return containerPath.toString();
 	}
 
 	/**
 	 * Appends the label for a import container, import or package declaration
 	 * to a {@link StringBuffer}. Considers the D_* flags.
 	 * 
 	 * @param declaration
 	 *            The element to render.
 	 * @param flags
 	 *            The rendering flags. Flags with names starting with 'D_' are
 	 *            considered.
 	 * @param buf
 	 *            The buffer to append the resulting label to.
 	 */
 	public void getDeclarationLabel(IModelElement declaration, long flags,
 			StringBuffer buf) {
 		if (getFlag(flags, D_QUALIFIED)) {
 			IModelElement openable = (IModelElement) declaration.getOpenable();
 			if (openable != null) {
 				buf.append(getElementLabel(openable, CF_QUALIFIED
 						| CU_QUALIFIED | (flags & QUALIFIER_FLAGS)));
 				buf.append('/');
 			}
 		}
 		// if (declaration.getElementType() == IModelElement.IMPORT_CONTAINER) {
 		// buf.append(JavaUIMessages.ModelElementLabels_import_container);
 		// } else {
 		buf.append(declaration.getElementName());
 		// }
 		// post qualification
 		if (getFlag(flags, D_POST_QUALIFIED)) {
 			IModelElement openable = (IModelElement) declaration.getOpenable();
 			if (openable != null) {
 				buf.append(CONCAT_STRING);
 				buf.append(getElementLabel(openable, CF_QUALIFIED
 						| CU_QUALIFIED | (flags & QUALIFIER_FLAGS)));
 			}
 		}
 	}
 }
