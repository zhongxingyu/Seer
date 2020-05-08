 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common.operations;
 
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.ABSTRACT_METHODS;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.CLASS_NAME;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.CONSTRUCTOR;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.INTERFACES;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.JAVA_PACKAGE;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.JAVA_PACKAGE_FRAGMENT_ROOT;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.JAVA_SOURCE_FOLDER;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.MAIN_METHOD;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.MODIFIER_ABSTRACT;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.MODIFIER_FINAL;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.MODIFIER_PUBLIC;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.OPEN_IN_EDITOR;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.PROJECT;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.QUALIFIED_CLASS_NAME;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.SOURCE_FOLDER;
 import static org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties.SUPERCLASS;
 
 import java.lang.reflect.Modifier;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragment;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaConventions;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.internal.operation.ArtifactEditOperationDataModelProvider;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModelOperation;
 import org.eclipse.wst.common.frameworks.internal.plugin.WTPCommonPlugin;
 
 /**
  * This data model provider is a subclass of AbstractDataModelProvider and follows the
  * IDataModelOperation and Data Model Wizard frameworks.
  * 
  * @see org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelProvider
  * 
  * This data model provider extends the ArtifactEditOperationDataModelProvider to get project name
  * and artifact edit information that during the operation, the artifact edit model can be used to
  * save changes.
  * @see org.eclipse.wst.common.componentcore.internal.operation.ArtifactEditOperationDataModelProvider
  * 
  * The NewJavaClassDataModelProvider is used to store all the base properties which would be needed
  * to generate a new instance of a java class. Validations for these properties such as class name,
  * package name, superclass, and modifiers are also provided.
  * 
  * The INewJavaClassDataModelProperties is implemented to store all of the property names.
  * @see org.eclipse.jst.j2ee.internal.common.operations.INewJavaClassDataModelProperties
  * 
  * Clients must subclass this data model provider and the properties interface to use it and to
  * cache and provide their own specific attributes. They should also provide their own validation
  * methods and default values for the properties they add.
  * 
  * The use of this class is EXPERIMENTAL and is subject to substantial changes.
  */
 public class NewJavaClassDataModelProvider extends ArtifactEditOperationDataModelProvider {
 
 	/**
 	 * Subclasses may extend this method to perform their own validation. This method should not
 	 * return null. It does not accept null as a parameter.
 	 * 
 	 * @see NewJavaClassDataModelProvider#validate(String)
 	 * 
 	 * @param folderFullPath
 	 * @return IStatus
 	 */
 	protected IStatus validateJavaSourceFolder(String folderFullPath) {
 		// Ensure that the source folder path is not empty
 		if (folderFullPath == null || folderFullPath.length() == 0) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NAME_EMPTY;
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Ensure that the source folder path is absolute
 		else if (!new Path(folderFullPath).isAbsolute()) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NOT_ABSOLUTE;
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		IProject project = getTargetProject();
 		// Ensure project is not closed
 		if (project == null) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NOT_EXIST;
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Ensure project is accessible.
 		if (!project.isAccessible()) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NOT_EXIST;
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Ensure the project is a java project.
 		try {
 			if (!project.hasNature(JavaCore.NATURE_ID)) {
 				String msg = J2EECommonMessages.ERR_JAVA_CLASS_NOT_JAVA_PROJECT;
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		} catch (CoreException e) {
 			Logger.getLogger().log(e);
 		}
 		// Ensure the selected folder is a valid java source folder for the component
 		IFolder sourcefolder = getJavaSourceFolder();
 		if (sourcefolder == null || (sourcefolder != null && !sourcefolder.getFullPath().equals(new Path(folderFullPath)))) {
 			String msg = J2EECommonMessages.getResourceString(J2EECommonMessages.ERR_JAVA_CLASS_FOLDER_NOT_SOURCE, new String[]{folderFullPath});
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Valid source is selected
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * Subclasses may extend this method to perform their own retrieval of a default java source
 	 * folder. This implementation returns the first source folder in the project for the component.
 	 * This method may return null.
 	 * 
 	 * @return IFolder instance of default java source folder
 	 */
 	protected IFolder getDefaultJavaSourceFolder() {
 		IProject project = getTargetProject();
 		if (project == null)
 			return null;
 		IPackageFragmentRoot[] sources = J2EEProjectUtilities.getSourceContainers(project);
 		// Try and return the first source folder
 		if (sources.length > 0) {
 			try {
 				return (IFolder) sources[0].getCorrespondingResource();
 			} catch (Exception e) {
 				return null;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Subclasses may extend this method to create their own specialized default WTP operation. This
 	 * implementation creates an instance of the NewJavaClassOperation to create a new java class.
 	 * This method will not return null.
 	 * 
 	 * @see WTPOperationDataModel#getDefaultOperation()
 	 * @see NewJavaClassOperation
 	 * 
 	 * @return WTPOperation
 	 */
 	public IDataModelOperation getDefaultOperation() {
 		return new NewJavaClassOperation(getDataModel());
 	}
 
 	/**
 	 * Subclasses may extend this method to add their own data model's properties as valid base
 	 * properties.
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider#getPropertyNames()
 	 */
 	public Set getPropertyNames() {
 		Set propertyNames = super.getPropertyNames();
 		propertyNames.add(SOURCE_FOLDER);
 		propertyNames.add(JAVA_PACKAGE);
 		propertyNames.add(CLASS_NAME);
 		propertyNames.add(SUPERCLASS);
 		propertyNames.add(MODIFIER_PUBLIC);
 		propertyNames.add(MODIFIER_ABSTRACT);
 		propertyNames.add(MODIFIER_FINAL);
 		propertyNames.add(INTERFACES);
 		propertyNames.add(MAIN_METHOD);
 		propertyNames.add(CONSTRUCTOR);
 		propertyNames.add(ABSTRACT_METHODS);
 		propertyNames.add(OPEN_IN_EDITOR);
 		propertyNames.add(JAVA_PACKAGE_FRAGMENT_ROOT);
 		propertyNames.add(JAVA_SOURCE_FOLDER);
 		propertyNames.add(PROJECT);
 		propertyNames.add(QUALIFIED_CLASS_NAME);
 		return propertyNames;
 	}
 
 	/**
 	 * Subclasses may extend this method to add the default values for their own specific data model
 	 * properties. This declares the default values for the new java class. This method does not
 	 * accept null. It may return null.
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider#getDefaultProperty(String)
 	 * 
 	 * @param propertyName
 	 * @return default object value of the property
 	 */
 	public Object getDefaultProperty(String propertyName) {
 		// Get the default source folder for the project
 		if (propertyName.equals(SOURCE_FOLDER)) {
 			IFolder sourceFolder = getDefaultJavaSourceFolder();
 			if (sourceFolder != null && sourceFolder.exists())
 				return sourceFolder.getFullPath().toOSString();
 		}
 		// Use Object as the default superclass if one is not specified
 		else if (propertyName.equals(SUPERCLASS))
 			return new String("java.lang.Object"); //$NON-NLS-1$
 		// Use public as default visibility
 		else if (propertyName.equals(MODIFIER_PUBLIC))
 			return Boolean.TRUE;
 		// Generate constructors from the superclass by default
 		else if (propertyName.equals(CONSTRUCTOR))
 			return Boolean.TRUE;
 		// Generate unimplemented methods from declared interfaces by default
 		else if (propertyName.equals(ABSTRACT_METHODS))
 			return Boolean.TRUE;
 		// Open the generated java class in the editor by default
 		else if (propertyName.equals(OPEN_IN_EDITOR))
 			return Boolean.TRUE;
 		else if (propertyName.equals(PROJECT))
 			return getTargetProject();
 		else if (propertyName.equals(JAVA_SOURCE_FOLDER))
 			return getJavaSourceFolder();
 		else if (propertyName.equals(JAVA_PACKAGE_FRAGMENT_ROOT))
 			return getJavaPackageFragmentRoot();
 		else if (propertyName.equals(QUALIFIED_CLASS_NAME))
 			return getQualifiedClassName();
 		return super.getDefaultProperty(propertyName);
 	}
 
 	/**
 	 * This method will return the qualified java class name as specified by the class name and
 	 * package name properties in the data model. This method should not return null.
 	 * 
 	 * @see #CLASS_NAME
 	 * @see #JAVA_PACKAGE
 	 * 
 	 * @return String qualified java classname
 	 */
 	private String getQualifiedClassName() {
 		// Use the java package name and unqualified class name to create a qualified java class
 		// name
 		String packageName = getStringProperty(JAVA_PACKAGE);
 		String className = getStringProperty(CLASS_NAME);
 		// Ensure the class is not in the default package before adding package name to qualified
 		// name
 		if (packageName != null && packageName.trim().length() > 0)
 			return packageName + "." + className; //$NON-NLS-1$
 		return className;
 	}
 
 	/**
 	 * Subclasses may override this method to provide their own validation of any of the data
 	 * model's properties. This implementation ensures that a java class can be properly generated
 	 * from the values as specified. This method will not return null. This method will not accept
 	 * null as a parameter.
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider#validate(java.lang.String)
 	 * 
 	 * @param propertyName
 	 * @return IStatus of the validity of the specifiec property
 	 */
 	public IStatus validate(String propertyName) {
 		IStatus result = super.validate(propertyName);
 		if (result != null && !result.isOK())
 			return result;
 		if (propertyName.equals(SOURCE_FOLDER))
 			return validateJavaSourceFolder(getStringProperty(propertyName));
 		if (propertyName.equals(JAVA_PACKAGE))
 			return validateJavaPackage(getStringProperty(propertyName));
 		if (propertyName.equals(CLASS_NAME)) {
 			result = validateJavaClassName(getStringProperty(propertyName));
 			if (result.isOK())
 				result = canCreateTypeInClasspath(getStringProperty(CLASS_NAME));
 		}
 		if (propertyName.equals(SUPERCLASS))
 			return validateSuperclass(getStringProperty(propertyName));
 		if (propertyName.equals(MODIFIER_ABSTRACT) || propertyName.equals(MODIFIER_FINAL))
 			return validateModifier(propertyName, getBooleanProperty(propertyName));
 		return result;
 	}
 
 	/**
 	 * This method will validate whether the specified package name is a valid java package name. It
 	 * will be called during the doValidateProperty(String). This method will accept null. It will
 	 * not return null.
 	 * 
 	 * @see NewJavaClassDataModelProvider#validate(String)
 	 * 
 	 * @param packName --
 	 *            the package name
 	 * @return IStatus of if the package name is valid
 	 */
 	private IStatus validateJavaPackage(String packName) {
 		if (packName != null && packName.trim().length() > 0) {
 			// Use standard java conventions to validate the package name
 			IStatus javaStatus = JavaConventions.validatePackageName(packName);
 			if (javaStatus.getSeverity() == IStatus.ERROR) {
 				String msg = J2EECommonMessages.ERR_JAVA_PACAKGE_NAME_INVALID + javaStatus.getMessage();
 				return WTPCommonPlugin.createErrorStatus(msg);
 			} else if (javaStatus.getSeverity() == IStatus.WARNING) {
 				String msg = J2EECommonMessages.ERR_JAVA_PACKAGE_NAME_WARNING + javaStatus.getMessage();
 				return WTPCommonPlugin.createWarningStatus(msg);
 			}
 		}
 		// java package name is valid
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * Subclasses may override this method to provide their own validation of the class name. This
 	 * implementation will verify if the specified class name is a valid UNQUALIFIED java class
 	 * name. This method will not accept null. It will not return null.
 	 * 
 	 * @see NewJavaClassDataModelProvider#validate(String)
 	 * 
 	 * @param className
 	 * @return IStatus of if java class name is valid
 	 */
 	protected IStatus validateJavaClassName(String className) {
		// Ensure the class name is not empty
		if (className == null || className.trim().length() == 0) {
			String msg = J2EECommonMessages.ERR_JAVA_CLASS_NAME_EMPTY;
			return WTPCommonPlugin.createErrorStatus(msg);
		}
 		// Do not allow qualified name
 		if (className.lastIndexOf('.') != -1) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_NAME_QUALIFIED;
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// Check Java class name by standard java conventions
 		IStatus javaStatus = JavaConventions.validateJavaTypeName(className);
 		if (javaStatus.getSeverity() == IStatus.ERROR) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_NAME_INVALID + javaStatus.getMessage();
 			return WTPCommonPlugin.createErrorStatus(msg);
 		} else if (javaStatus.getSeverity() == IStatus.WARNING) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_NAME_WARNING + javaStatus.getMessage();
 			return WTPCommonPlugin.createWarningStatus(msg);
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * This method will verify the specified superclass can be subclassed. It ensures the superclass
 	 * is a valid java class, that it exists, and that it is not final. This method will accept
 	 * null. It will not return null.
 	 * 
 	 * @see NewJavaClassDataModelProvider#validate(String)
 	 * 
 	 * @param superclassName
 	 * @return IStatus of if the superclass can be subclassed
 	 */
 	private IStatus validateSuperclass(String superclassName) {
 		// Ensure the superclass name is not empty
 		if (superclassName == null || superclassName.trim().length() == 0) {
 			String msg = J2EECommonMessages.ERR_JAVA_CLASS_NAME_EMPTY;
 			return WTPCommonPlugin.createErrorStatus(msg);
 		}
 		// In default case of Object, return OK right away
 		if (superclassName.equals("java.lang.Object")) //$NON-NLS-1$
 			return WTPCommonPlugin.OK_STATUS;
 		// Ensure the unqualified java class name of the superclass is valid
 		String className = superclassName;
 		int index = superclassName.lastIndexOf("."); //$NON-NLS-1$
 		if (index != -1) {
 			className = superclassName.substring(index + 1);
 		}
 		IStatus status = validateJavaClassName(className);
 		// If unqualified super class name is valid, ensure validity of superclass itself
 		if (status.getSeverity() != IStatus.ERROR) {
 			// If the superclass does not exist, throw an error
 			IJavaProject javaProject = JemProjectUtilities.getJavaProject(getTargetProject());
 			IType supertype = null;
 			try {
 				supertype = javaProject.findType(superclassName);
 			} catch (Exception e) {
 				// Just throw error below
 			}
 			if (supertype == null) {
 				String msg = J2EECommonMessages.ERR_JAVA_CLASS_SUPERCLASS_NOT_EXIST;
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 			// Ensure the superclass is not final
 			int flags = -1;
 			try {
 				flags = supertype.getFlags();
 				if (Modifier.isFinal(flags)) {
 					String msg = J2EECommonMessages.ERR_JAVA_CLASS_SUPERCLASS_FINAL;
 					return WTPCommonPlugin.createErrorStatus(msg);
 				}
 			} catch (Exception e) {
 				Logger.getLogger().log(e);
 			}
 		}
 		// Return status of specified superclass
 		return status;
 	}
 
 	/**
 	 * This method will ensure that if the abstract modifier is set, that final is not set, and
 	 * vice-versa, as this is not supported either way. This method will not accept null. It will
 	 * not return null.
 	 * 
 	 * @see NewJavaClassDataModelProvider#validate(String)
 	 * 
 	 * @param prop
 	 * @return IStatus of whether abstract value is valid
 	 */
 	private IStatus validateModifier(String propertyName, boolean prop) {
 		// Throw an error if both Abstract and Final are checked
 		if (prop) {
 			// Ensure final is not also checked
 			if (propertyName.equals(MODIFIER_ABSTRACT) && getBooleanProperty(MODIFIER_FINAL)) {
 				String msg = J2EECommonMessages.ERR_BOTH_FINAL_AND_ABSTRACT;
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 			// Ensure abstract is not also checked
 			if (propertyName.equals(MODIFIER_FINAL) && getBooleanProperty(MODIFIER_ABSTRACT)) {
 				String msg = J2EECommonMessages.ERR_BOTH_FINAL_AND_ABSTRACT;
 				return WTPCommonPlugin.createErrorStatus(msg);
 			}
 		}
 		// Abstract and final settings are valid
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * This method is intended for internal use only. This will check the java model for the
 	 * specified javaproject in the data model for the existence of the passed in qualified
 	 * classname. This method does not accept null. It may return null.
 	 * 
 	 * @see NewJavaClassDataModelProvider#getTargetProject()
 	 * @see JavaModelUtil#findType(IJavaProject, String)
 	 * 
 	 * @param fullClassName
 	 * @return IType for the specified classname
 	 */
 	protected IStatus canCreateTypeInClasspath(String className) {
 		// Retrieve the java project for the cached project
 		IJavaProject javaProject = JemProjectUtilities.getJavaProject(getTargetProject());
 		try {
 			String folderPath = getStringProperty(SOURCE_FOLDER);
 			String packagePath = getStringProperty(JAVA_PACKAGE);
 			// Replace all "." with "//" in the package path to denote folders
 			if (packagePath.indexOf('.')>-1) {
 				StringBuffer buffer = new StringBuffer(packagePath);
 				for (int i = 0; i < buffer.length(); i++) {
 					if (buffer.charAt(i)=='.') {
 						buffer.deleteCharAt(i);
 						buffer.insert(i, "//"); //$NON-NLS-1$
 					}
 				}
 				packagePath = buffer.toString();
 			}
 			IPath path = new Path(folderPath + "//" + packagePath); //$NON-NLS-1$
 			IPackageFragment pack = javaProject.findPackageFragment(path);
 			if (pack != null) {
 				ICompilationUnit cu = pack.getCompilationUnit(className + ".java"); //$NON-NLS-1$
 				IResource resource = cu.getResource();
 				if (resource.exists()) {
 					String msg = J2EECommonMessages.ERR_JAVA_CLASS_NAME_EXIST;
 					return WTPCommonPlugin.createErrorStatus(msg);
 				}
 			}
 			return WTPCommonPlugin.OK_STATUS;
 		} catch (Exception e) {
 			Logger.getLogger().log(e);
 		}
 		return WTPCommonPlugin.OK_STATUS;
 	}
 
 	/**
 	 * This will return the IFolder instance for the specified folder name in the data model. This
 	 * method may return null.
 	 * 
 	 * @see #SOURCE_FOLDER
 	 * @see NewJavaClassDataModelProvider#getAllSourceFolders()
 	 * 
 	 * @return IFolder java source folder
 	 */
 	protected final IFolder getJavaSourceFolder() {
 		IPackageFragmentRoot[] sources = J2EEProjectUtilities.getSourceContainers(getTargetProject());
 		// Ensure there is valid source folder(s)
 		if (sources == null || sources.length == 0)
 			return null;
 		String folderFullPath = getStringProperty(SOURCE_FOLDER);
 		// Get the source folder whose path matches the source folder name value in the data model
 		for (int i = 0; i < sources.length; i++) {
 			if (sources[i].getPath().equals(new Path(folderFullPath))) {
 				try {
 					return (IFolder) sources[i].getCorrespondingResource();
 				} catch (Exception e) {
 					break;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Subclasses may extend this method to perform their own retrieval mechanism. This
 	 * implementation simply returns the JDT package fragment root for the selected source folder.
 	 * This method may return null.
 	 * 
 	 * @see IJavaProject#getPackageFragmentRoot(org.eclipse.core.resources.IResource)
 	 * 
 	 * @return IPackageFragmentRoot
 	 */
 	protected IPackageFragmentRoot getJavaPackageFragmentRoot() {
 		IProject project = getTargetProject();
 		if (project != null) {
 			IJavaProject aJavaProject = JemProjectUtilities.getJavaProject(project);
 			// Return the source folder for the java project of the selected project
 			if (aJavaProject != null) {
 				IFolder sourcefolder = (IFolder) getProperty(JAVA_SOURCE_FOLDER);
 				if (sourcefolder != null)
 					return aJavaProject.getPackageFragmentRoot(sourcefolder);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * This method ensures the source folder is updated if the component is changed.
 	 * 
 	 * @see org.eclipse.wst.common.frameworks.datamodel.IDataModelProvider#propertySet(String,
 	 *      Object)
 	 * 
 	 * @return boolean if property set successfully
 	 */
 	public boolean propertySet(String propertyName, Object propertyValue) {
 		boolean result = super.propertySet(propertyName, propertyValue);
 		if (result) {
 			if (COMPONENT_NAME.equals(propertyName)){
 				if( getDefaultJavaSourceFolder() != null ){
 					setProperty(SOURCE_FOLDER, getDefaultJavaSourceFolder().getFullPath().toOSString());
 				}
 			}
 		}
 		return result;
 	}
 }
