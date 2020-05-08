 /*******************************************************************************
  * Copyright (c) 2007 BEA Systems, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.classpathdep;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.common.jdt.internal.javalite.IJavaProjectLite;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaCoreLite;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.UpdateClasspathAttributeUtil;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 
 /**
  * DataModelOperation that updates the WTP component dependency attribute on the
  * the classpath entries for the target Java project.
  */
 public class UpdateClasspathAttributesOperation extends AbstractDataModelOperation implements UpdateClasspathAttributesDataModelProperties {
 
 	public UpdateClasspathAttributesOperation(final IDataModel dataModel) {
 		super(dataModel);
 	}
 	
 	@Override
 	public IStatus execute(final IProgressMonitor monitor, final IAdaptable info)
 			throws ExecutionException {
 		final IProject project = ProjectUtilities.getProject(model.getStringProperty(PROJECT_NAME));
 		final boolean isLegacyJ2EE = J2EEProjectUtilities.isLegacyJ2EEProject(project);
 
 		try {
 			if (project.hasNature(JavaCore.NATURE_ID)) {
 				final IJavaProject javaProject = JavaCore.create(project);
 				validateEdit(project);
 				final Map entriesToAdd = (Map) model.getProperty(ENTRIES_TO_ADD_ATTRIBUTE);
 				final boolean modifyComponentDep = model.getBooleanProperty(MODIFY_CLASSPATH_COMPONENT_DEPENDENCY);
 				if (entriesToAdd == null) {
 					final Map entriesToRemove = (Map ) model.getProperty(ENTRIES_TO_REMOVE_ATTRIBUTE);
 					if (entriesToRemove == null) {
 						final Map entriesToRuntimePath = (Map) model.getProperty(ENTRIES_WITH_ATTRIBUTE);
 						final Map entriesToAttrib = new HashMap();
 						final Iterator i = entriesToRuntimePath.keySet().iterator();
 						while (i.hasNext()) {
 							IClasspathEntry entry = (IClasspathEntry) i.next();
 							IPath runtimePath = (IPath) entriesToRuntimePath.get(entry);
 							IClasspathAttribute attrib = ClasspathDependencyUtil.checkForComponentDependencyAttribute(entry, 
 									modifyComponentDep ? DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY : DependencyAttributeType.CLASSPATH_COMPONENT_NONDEPENDENCY);
 							if (attrib == null) {
 								if (modifyComponentDep) {
 									attrib = UpdateClasspathAttributeUtil.createDependencyAttribute(runtimePath);
 								} else {
 									attrib = UpdateClasspathAttributeUtil.createNonDependencyAttribute();
 								}
 							}
 							entriesToAttrib.put(entry, attrib);
 						}
 						updateDependencyAttributes(javaProject, entriesToAttrib, modifyComponentDep, isLegacyJ2EE); 
 					} else {
 						removeDependencyAttributes(javaProject, entriesToRemove, modifyComponentDep); 
 					}
 				} else {
 					
 					addDependencyAttributes(javaProject, entriesToAdd, modifyComponentDep); 
 				}
 			}
 		} catch (CoreException ce) {
 			J2EEPlugin.logError(ce);
 			return new Status(IStatus.ERROR, J2EEPlugin.PLUGIN_ID, 0, ce.getLocalizedMessage(), ce);
 		}
 		return Status.OK_STATUS;
 	}
 
 	/**
 	 * Performs a validateEdit call on the files impacted by the operation.
 	 */
 	protected void validateEdit(final IProject project) throws CoreException {
 		final List affectedFiles = ProjectUtilities.getFilesAffectedByClasspathChange(project);
 		final IFile[] files = (IFile[]) affectedFiles.toArray(new IFile[affectedFiles.size()]);
 		final IStatus result = J2EEPlugin.getWorkspace().validateEdit(files, null);
 		if (!result.isOK()) {
 			throw new CoreException(result);
 		}
 	}
 	
 	/**
 	 * Adds the WTP component dependency attribute to the specified classpath entries.
 	 * @param javaProject Target Java project.
 	 * @param entries Classpath entries to which the component dependency attribute should be added.
  	 * @param modifyComponentDep True if adding the dependency attribute, false if adding the non-dependency attribute.
 	 * @throws CoreException Thrown if an error is encountered.
 	 */	
 	private void addDependencyAttributes(final IJavaProject javaProject, final Map entries, final boolean modifyComponentDep) throws CoreException {
 		alterDependencyAttributes(javaProject, entries, true, modifyComponentDep);
 	}
 	
 	/**
 	 * Removes the WTP component dependency attribute from the specified classpath entries.
 	 * @param javaProject Target Java project.
 	 * @param entries Classpath entries from which the component dependency attribute should be removed.
 	 * @param modifyComponentDep True if removing the dependency attribute, false if removing the non-dependency attribute.
 	 * @throws CoreException Thrown if an error is encountered.
 	 */	
 	private void removeDependencyAttributes(final IJavaProject javaProject, final Map entries, final boolean modifyComponentDep) throws CoreException {
 		alterDependencyAttributes(javaProject, entries, false, modifyComponentDep);
 	}
 
 	private void alterDependencyAttributes(final IJavaProject javaProject, final Map entries, final boolean add, final boolean modifyComponentDep) throws CoreException {
		final boolean isWebApp = JavaEEProjectUtilities.isDynamicWebProject(javaProject.getProject());
 		final boolean isLegacyJ2EE = J2EEProjectUtilities.isLegacyJ2EEProject(javaProject.getProject());
 		final IJavaProjectLite javaProjectLite = JavaCoreLite.create(javaProject);
 		
 		// initialize to the set of raw entries with the attrib
 		final Map entriesWithAttrib = ClasspathDependencyUtil.getRawComponentClasspathDependencies(javaProjectLite, modifyComponentDep ? DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY : DependencyAttributeType.CLASSPATH_COMPONENT_NONDEPENDENCY, isLegacyJ2EE);
 		
 		Iterator i = entries.keySet().iterator();
 		while (i.hasNext()) {
 			final IClasspathEntry entry = (IClasspathEntry) i.next();
 			if (add) {
 				if (getMatchingEntryIgnoreAttributes(entriesWithAttrib, entry) == null) {
 					IPath runtimePath = (IPath) entries.get(entry);
 					if (runtimePath == null) {
 						// compute the default runtime path
						runtimePath = ClasspathDependencyUtil.getDefaultRuntimePath(isWebApp, ClasspathDependencyUtil.isClassFolderEntry(entry));
 					}
 					IClasspathAttribute attrib = null;
 					if (modifyComponentDep) {
 						attrib = UpdateClasspathAttributeUtil.createDependencyAttribute(runtimePath);
 					} else {
 						attrib = UpdateClasspathAttributeUtil.createNonDependencyAttribute();
 					}
  					entriesWithAttrib.put(entry, attrib);
 				}
 			} else {
 				IClasspathEntry matching = getMatchingEntryIgnoreAttributes(entriesWithAttrib, entry);
 				if (matching != null) {
 					entriesWithAttrib.remove(matching);
 				}
 			}
 		}
 		updateDependencyAttributes(javaProject, entriesWithAttrib, modifyComponentDep, isLegacyJ2EE);
 	}
 	
 	private IClasspathEntry getMatchingEntryIgnoreAttributes(final Map entries, final IClasspathEntry entry) {
 		final Iterator i = entries.keySet().iterator();
 		while (i.hasNext()) {
 			final IClasspathEntry e = (IClasspathEntry) i.next();
 			if (e.getEntryKind() == entry.getEntryKind()
 					&& e.getPath().equals(entry.getPath())
 					&& e.isExported() == entry.isExported()) {
 				return e;
 			}
 		}
 		return null;
 
 	}
 
 	/**
 	 * Updates the specified Java project so that only the specified classpath entries have
 	 * the WTP component dependency attribute.
 	 * @param javaProject Target Java project.
 	 * @param entries Classpath entries that should have the component dependency attribute. Map from IClasspathEntry
 	 * to the IClasspathAttribute for the WTP classpath component dependency.
 	 * @param modifyComponentDep True if modifying the dependency attribute, false if modifying the non-dependency attribute.
 	 * @throws CoreException Thrown if an error is encountered.
 	 */	
 	private void updateDependencyAttributes(final IJavaProject javaProject, final Map entriesWithAttrib, final boolean modifyComponentDep, final boolean isLegacyJ2EE) throws CoreException {
 		if (javaProject == null || !javaProject.getProject().isAccessible()) {
 			return;
 		}
 		
 		final List updatedClasspath = new ArrayList();
 		final IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();
 		boolean needToUpdateClasspath = false;
 		IClasspathAttribute attrib = UpdateClasspathAttributeUtil.createDependencyAttribute();
 		if (!modifyComponentDep) {
 			attrib = UpdateClasspathAttributeUtil.createNonDependencyAttribute();
 		}
 		for (int i = 0; i < rawClasspath.length; i++) {
 			IClasspathEntry entry = rawClasspath[i];
 			final int kind = entry.getEntryKind();
 			boolean hasAttribute = ClasspathDependencyUtil.checkForComponentDependencyAttribute(entry, 
 					modifyComponentDep ? DependencyAttributeType.CLASSPATH_COMPONENT_DEPENDENCY : DependencyAttributeType.CLASSPATH_COMPONENT_NONDEPENDENCY, isLegacyJ2EE) != null;
 			boolean shouldHaveAttribute = entriesWithAttrib.containsKey(entry);
 			boolean updateAttributes = false;
 			IClasspathAttribute[] updatedAttributes = null;
 			if (shouldHaveAttribute) {
 				if (!hasAttribute) {
 					// should have the attribute and currently missing it
 					attrib = (IClasspathAttribute) entriesWithAttrib.get(entry);
 					updatedAttributes = updateAttributes(entry.getExtraAttributes(), attrib, true);
 					needToUpdateClasspath = true;
 					updateAttributes = true;
 				}
 			} else if (hasAttribute) {
 				// should not have the attribute and currently has it
 				updatedAttributes = updateAttributes(entry.getExtraAttributes(), attrib, false);
 				needToUpdateClasspath = true;
 				updateAttributes = true;				
 			}
 			
 			if (updateAttributes) {
 				switch(kind) {
 				case IClasspathEntry.CPE_CONTAINER:
 					entry = JavaCore.newContainerEntry(entry.getPath(), entry.getAccessRules(), updatedAttributes, entry.isExported());
 					break;
 				case IClasspathEntry.CPE_LIBRARY:
 					entry = JavaCore.newLibraryEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.getAccessRules(), updatedAttributes, entry.isExported());
 					break;
 				case IClasspathEntry.CPE_VARIABLE:
 					entry = JavaCore.newVariableEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), entry.getAccessRules(), updatedAttributes, entry.isExported());
 					break;					
 				case IClasspathEntry.CPE_PROJECT: // although project entries are not yet supported, allow the attribute here and let the validator flag as an error
 					entry = JavaCore.newProjectEntry(entry.getPath(), entry.getAccessRules(), entry.combineAccessRules(), updatedAttributes, entry.isExported());
 					break;										
 				case IClasspathEntry.CPE_SOURCE: // although source entries are not supported, allow the attribute here and let the validator flag as an error
 					entry = JavaCore.newSourceEntry(entry.getPath(), entry.getInclusionPatterns(), entry.getExclusionPatterns(), entry.getOutputLocation(), updatedAttributes);
 					break;															
 				}
 			}
 			
 			updatedClasspath.add(entry);
 		}
 		if (needToUpdateClasspath) {
 			final IClasspathEntry[] updatedCPArray = (IClasspathEntry[]) updatedClasspath.toArray(new IClasspathEntry[updatedClasspath.size()]);
 			javaProject.setRawClasspath(updatedCPArray, null);
 		}
 	}
 
 	private IClasspathAttribute[] updateAttributes(final IClasspathAttribute[] currentAttribs, final IClasspathAttribute targetAttrib, final boolean add) {
 		final List updatedAttribs = new ArrayList();
 		boolean hasAttrib = false;
 		for (int i = 0; i < currentAttribs.length; i++) {
 			if (currentAttribs[i].getName().equals(targetAttrib.getName())) {
 				hasAttrib = true;
 				if (!add) {
 					continue;
 				}
 			}
 			updatedAttribs.add(currentAttribs[i]);
 		}
 		if (add && !hasAttrib) {
 			updatedAttribs.add(targetAttrib);
 		}
 		return (IClasspathAttribute[]) updatedAttribs.toArray(new IClasspathAttribute[updatedAttribs.size()]);
 	}
 }
