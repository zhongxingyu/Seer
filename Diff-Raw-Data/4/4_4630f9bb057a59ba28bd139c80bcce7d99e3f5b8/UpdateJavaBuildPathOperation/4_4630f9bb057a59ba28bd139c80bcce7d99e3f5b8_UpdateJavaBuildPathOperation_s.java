 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common.operations;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.j2ee.application.internal.operations.ClassPathSelection;
 import org.eclipse.jst.j2ee.application.internal.operations.ClasspathElement;
 import org.eclipse.wst.common.frameworks.internal.enablement.nonui.WFTWrappedException;
 import org.eclipse.wst.common.frameworks.internal.operations.IHeadlessRunnableWithProgress;
 
 
 /**
  * Insert the type's description here. Creation date: (9/10/2001 12:35:38 PM)
  * 
  * @author: Administrator
  */
 public class UpdateJavaBuildPathOperation implements IHeadlessRunnableWithProgress {
 	protected IJavaProject javaProject;
 	protected ClassPathSelection classPathSelection;
 	//All the Java build path entries created by the classpath selection
 	protected Set allClasspathEntries;
 	protected List allUnselectedClasspathEntries;
 
 	/**
 	 * UpdateJavaBuildPathOperation constructor comment.
 	 */
 	public UpdateJavaBuildPathOperation(IJavaProject aJavaProject, ClassPathSelection aClassPathSelection) {
 		super();
 		javaProject = aJavaProject;
 		classPathSelection = aClassPathSelection;
 		allClasspathEntries = new HashSet();
		allClasspathEntries.addAll(Arrays.asList(aClassPathSelection.getClasspathEntriesForSelected()));
 	}
 	
 	/**
 	 * UpdateJavaBuildPathOperation constructor comment.
 	 */
 	public UpdateJavaBuildPathOperation(IJavaProject aJavaProject, ClassPathSelection selected,ClassPathSelection unselected) {
 		super();
 		javaProject = aJavaProject;
 		classPathSelection = selected;
 		allClasspathEntries = new HashSet();
 		if(selected != null && !selected.getClasspathElements().isEmpty())
 			allClasspathEntries.addAll(Arrays.asList(selected.getClasspathEntriesForSelected()));
 		
 		allUnselectedClasspathEntries = new ArrayList();
 		if(unselected != null && !unselected.getClasspathElements().isEmpty())
 			allUnselectedClasspathEntries.addAll(unselected.getClasspathElements());
 	}
 
 	protected void ensureClasspathEntryIsExported(List cp, IClasspathEntry entry) {
 		if (entry.isExported())
 			return;
 		int index = getIndex(cp, entry);
 		IClasspathEntry newEntry = null;
 		switch (entry.getEntryKind()) {
 			case IClasspathEntry.CPE_PROJECT :
 				newEntry = JavaCore.newProjectEntry(entry.getPath(), true);
 				break;
 			case IClasspathEntry.CPE_LIBRARY :
 				newEntry = JavaCore.newLibraryEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath(), true);
 				break;
 			case IClasspathEntry.CPE_VARIABLE:
 				newEntry = JavaCore.newVariableEntry(entry.getPath(), entry.getSourceAttachmentPath(), entry.getSourceAttachmentRootPath());
 			default :
 				break;
 		}
 		if (entry != null)
 			cp.set(index, newEntry);
 	}
 
 	protected IClasspathEntry ensureElementInList(List cp, ClasspathElement element, IClasspathEntry predecessor) {
 		IClasspathEntry addedEntry = null;
 		//The element might have multiple entries in the case of
 		//the imported_classes.jar file
 		IClasspathEntry[] cpEntries = element.newClasspathEntries();
 		if (cpEntries == null || cpEntries.length == 0)
 			//indicates an invalid entry
 			return null;
 
 		int predecessorPos = predecessor == null ? -1 : getIndex(cp, predecessor);
 		addedEntry = cpEntries[0];
 		//Ensure that the first item is in the list, and follows
 		//the predecessor if specified; preserve existing items in
 		//the case of source attachments
 		int pos = getIndex(cp, addedEntry);
 		if (pos == -1) {
 			if (predecessorPos == -1)
 				cp.add(addedEntry);
 			else
 				cp.add(predecessorPos + 1, addedEntry);
 		} else {
 			addedEntry = (IClasspathEntry) cp.get(pos);
 			if (pos < predecessorPos) {
 				cp.remove(addedEntry);
 				cp.add(predecessorPos, addedEntry);
 			}
 		}
 		ensureClasspathEntryIsExported(cp, addedEntry);
 
 		//Remove and add so we can ensure the proper order; this
 		//is the case of the imported_classes.jar; we always want it
 		//directly after the project
 		for (int i = 1; i < cpEntries.length; i++) {
 			int index = getIndex(cp, cpEntries[i]);
 			if (index != -1) {
 				addedEntry = (IClasspathEntry) cp.get(index);
 				cp.remove(index);
 			} else
 				addedEntry = cpEntries[i];
 			pos = getIndex(cp, cpEntries[0]);
 			cp.add(pos + 1, addedEntry);
 		}
 		return addedEntry;
 	}
 
 	protected int getIndex(List cp, IClasspathEntry entry) {
 		for (int i = 0; i < cp.size(); i++) {
 			IClasspathEntry elmt = (IClasspathEntry) cp.get(i);
 			if (elmt.getPath().equals(entry.getPath()))
 				return i;
 		}
 		return -1;
 	}
 
 	protected void ensureElementNotInList(List cp, ClasspathElement element) {
 		IClasspathEntry[] cpEntries = element.newClasspathEntries();
 		if (cpEntries == null || cpEntries.length == 0)
 			return;
 		for (int i = 0; i < cpEntries.length; i++) {
 			if (allClasspathEntries.contains(cpEntries[i]))
 				//This may be included indirectly by a transitive dependency
 				continue;
 			int index = getIndex(cp, cpEntries[i]);
 			if (index != -1)
 				cp.remove(index);
 		}
 	}
 	
 	protected void ensureRemoveElementInList(List cp, ClasspathElement element) {
 		IClasspathEntry[] cpEntries = element.newClasspathEntries();
 		if (cpEntries == null || cpEntries.length == 0)
 			return;
 		for (int i = 0; i < cpEntries.length; i++) {
 			if (cp.contains(cpEntries[i])) {
 				int index = getIndex(cp, cpEntries[i]);
 				if (index != -1)
 					cp.remove(index);
 			}
 		}
 	}
 
 	/**
 	 * Runs this operation. Progress should be reported to the given progress monitor. This method
 	 * is usually invoked by an <code>IRunnableContext</code>'s<code>run</code> method, which
 	 * supplies the progress monitor. A request to cancel the operation should be honored and
 	 * acknowledged by throwing <code>InterruptedException</code>.
 	 * 
 	 * @param monitor
 	 *            the progress monitor to use to display progress and receive requests for
 	 *            cancelation
 	 * @exception InvocationTargetException
 	 *                if the run method must propagate a checked exception, it should wrap it inside
 	 *                an <code>InvocationTargetException</code>; runtime exceptions are
 	 *                automatically wrapped in an <code>InvocationTargetException</code> by the
 	 *                calling context
 	 * @exception InterruptedException
 	 *                if the operation detects a request to cancel, using
 	 *                <code>IProgressMonitor.isCanceled()</code>, it should exit by throwing
 	 *                <code>InterruptedException</code>
 	 * 
 	 * @see IRunnableContext#run
 	 */
 	public void run(org.eclipse.core.runtime.IProgressMonitor monitor) throws java.lang.reflect.InvocationTargetException, InterruptedException {
 		try {
 			String[] prevRequiredProjects = javaProject.getRequiredProjectNames();
 			List cp = new ArrayList(Arrays.asList(javaProject.getRawClasspath()));
 			List classpathElements = classPathSelection.getClasspathElements();
 			IClasspathEntry predecessor = null;
 			IClasspathEntry result = null;
 			for (int i = 0; i < classpathElements.size(); i++) {
 				ClasspathElement element = (ClasspathElement) classpathElements.get(i);
 				if (element.isSelected()) {
 					result = ensureElementInList(cp, element, predecessor);
 					if (result != null)
 						predecessor = result;
 				} else
 					ensureElementNotInList(cp, element);
 			}
 			filterUnselectedEntries(cp);
 			IClasspathEntry[] newCp = ((IClasspathEntry[]) cp.toArray(new IClasspathEntry[cp.size()]));
 			javaProject.setRawClasspath(newCp, monitor);
 			updateRequiredProjects(javaProject, prevRequiredProjects, new SubProgressMonitor(monitor, 1));
 		} catch (Exception ex) {
 			throw new WFTWrappedException(ex);
 		}
 	}
 
 	private void filterUnselectedEntries(List cp) {
 		if (allUnselectedClasspathEntries != null) {
 			for (int i = 0; i < allUnselectedClasspathEntries.size(); i++) {
 				ClasspathElement element = (ClasspathElement) allUnselectedClasspathEntries.get(i);
 				ensureRemoveElementInList(cp, element);
 			}
 		}
 	}
 
 	protected void updateRequiredProjects(IJavaProject jproject, String[] prevRequiredProjects, IProgressMonitor monitor) throws CoreException {
 		String[] newRequiredProjects = jproject.getRequiredProjectNames();
 
 		ArrayList prevEntries = new ArrayList(Arrays.asList(prevRequiredProjects));
 		ArrayList newEntries = new ArrayList(Arrays.asList(newRequiredProjects));
 
 		IProject proj = jproject.getProject();
 		IProjectDescription projDesc = proj.getDescription();
 
 		ArrayList newRefs = new ArrayList();
 		IProject[] referencedProjects = projDesc.getReferencedProjects();
 		for (int i = 0; i < referencedProjects.length; i++) {
 			String curr = referencedProjects[i].getName();
 			if (newEntries.remove(curr) || !prevEntries.contains(curr)) {
 				newRefs.add(referencedProjects[i]);
 			}
 		}
 		IWorkspaceRoot root = proj.getWorkspace().getRoot();
 		for (int i = 0; i < newEntries.size(); i++) {
 			String curr = (String) newEntries.get(i);
 			newRefs.add(root.getProject(curr));
 		}
 		projDesc.setReferencedProjects((IProject[]) newRefs.toArray(new IProject[newRefs.size()]));
 		proj.setDescription(projDesc, monitor);
 	}
 }
