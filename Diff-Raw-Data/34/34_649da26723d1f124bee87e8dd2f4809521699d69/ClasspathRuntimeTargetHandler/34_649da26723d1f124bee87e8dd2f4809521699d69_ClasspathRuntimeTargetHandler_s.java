 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.core;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.*;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jst.server.core.internal.IMemento;
 import org.eclipse.jst.server.core.internal.JavaServerPlugin;
 import org.eclipse.jst.server.core.internal.RuntimeClasspathContainer;
 import org.eclipse.jst.server.core.internal.Trace;
 import org.eclipse.jst.server.core.internal.XMLMemento;
 import org.eclipse.wst.server.core.IRuntime;
 import org.eclipse.wst.server.core.model.RuntimeTargetHandlerDelegate;
 /**
  * A runtime target handler that supports changing the classpath of the
  * project by adding one or more classpath containers. Runtime providers
  * can extend this class and implement the abstract methods to provide
  * the correct build path for their runtime type. 
  * 
  * @since 1.0
  */
 public abstract class ClasspathRuntimeTargetHandler extends RuntimeTargetHandlerDelegate {
 	
 	class SourceAttachmentUpdate {
 		String runtimeId;
 		String id;
 		IPath entry;
 		IPath sourceAttachmentPath;
 		IPath sourceAttachmentRootPath;
 	}
 	
 	protected List sourceAttachments;
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.model.IRuntimeTargetDelegate#setRuntimeTarget(org.eclipse.core.resources.IProject, org.eclipse.wst.server.core.IRuntime)
 	 */
 	public void setRuntimeTarget(IProject project, IRuntime runtime, IProgressMonitor monitor) throws CoreException {
 		if (project == null || runtime == null)
 			return;
 
 		IJavaProject javaProject = null;
 		try {
 			javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
 		} catch (Exception e) {
 			// ignore
 		}
 		
 		if (javaProject == null)
 			return;
 		
 		try {
 			List list = new ArrayList();
 			IClasspathEntry[] cp = javaProject.getRawClasspath();
 			int size = cp.length;
 			for (int i = 0; i < size; i++) {
 				if (cp[i].getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 					if (!cp[i].getPath().segment(0).equals(RuntimeClasspathContainer.SERVER_CONTAINER))
 						list.add(cp[i]);
 				} else
 					list.add(cp[i]);
 			}
 			
 			List add = new ArrayList();
 			IClasspathEntry[] entries = getDelegateClasspathEntries(runtime, monitor);
 			if (entries != null) {
 				size = entries.length;
 				for (int i = 0; i < size; i++)
 					add.add(entries[i]);
 			}
 			
 			String[] ids = getClasspathEntryIds();
 			if (ids != null) {
 				size = ids.length;
 				for (int i = 0; i < size; i++) {
 					String id2 = getRuntimeTargetHandler().getId();
 					IPath path = new Path(RuntimeClasspathContainer.SERVER_CONTAINER).append(id2).append(runtime.getId());
 					if (ids[i] != null)
 						path.append(ids[i]);
 					add.add(JavaCore.newContainerEntry(path));
 					String id = "";
 					if (path.segmentCount() > 3)
 						id = path.segment(3);
 					RuntimeClasspathContainer rcc = new RuntimeClasspathContainer(path, this, runtime, id);
 					JavaCore.setClasspathContainer(path, new IJavaProject[] { javaProject}, new IClasspathContainer[] { rcc }, monitor);
 				}
 			}
 			
 			// clean up duplicates
 			cleanupDuplicateClasspath(javaProject, list, add);
 			
 			Iterator iterator = add.iterator();
 			while (iterator.hasNext()) {
 				list.add(iterator.next());
 			}
 			
 			cp = new IClasspathEntry[list.size()];
 			list.toArray(cp);
 			javaProject.setRawClasspath(cp, monitor);
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Error setting runtime target", e);
 			throw new CoreException(new Status(IStatus.ERROR, JavaServerPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
 		}
 	}
 	
 	protected void cleanupDuplicateClasspath(IJavaProject project, List current, List add) {
 		// check if we even have to bother
 		boolean sourceOnly = true;
 		Iterator iterator = current.iterator();
 		while (iterator.hasNext()) {
 			IClasspathEntry entry = (IClasspathEntry) iterator.next();
 			if (entry.getEntryKind() != IClasspathEntry.CPE_SOURCE)
 				sourceOnly = false;
 		}
 		if (sourceOnly)
 			return;
 		
 		// remove any of our own containers
 		List remove = new ArrayList();
 		iterator = current.iterator();
 		while (iterator.hasNext()) {
 			IClasspathEntry entry = (IClasspathEntry) iterator.next();
 			
 			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 				try {
 					if (RuntimeClasspathContainer.SERVER_CONTAINER.equals(entry.getPath().segment(0))
 						|| JavaRuntime.JRE_CONTAINER.equals(entry.getPath().segment(0)))
 						remove.add(entry);
 				} catch (Exception e) {
 					Trace.trace(Trace.FINEST, "Error resolving classpath container", e);
 				}
 			}
 		}
 
 		// expand the "add" list
 		List addExpanded = new ArrayList();
 		iterator = add.iterator();
 		while (iterator.hasNext()) {
 			IClasspathEntry entry = (IClasspathEntry) iterator.next();
 			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 				try {
 					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
 					IClasspathEntry[] entries = container.getClasspathEntries();
 					int size = entries.length;
 					for (int i = 0; i < size; i++) {
 						if (entries[i] != null)
 							addExpanded.add(entries[i]);
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.FINEST, "Error resolving classpath container 2", e);
 				}
 			} else if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
 				entry = JavaCore.getResolvedClasspathEntry(entry);
 				if (entry != null)
 					addExpanded.add(entry);
 			} else
 				addExpanded.add(entry);
 		}
 		
 		// check for duplicates by also expanding the current list
 		iterator = current.iterator();
 		while (iterator.hasNext()) {
 			IClasspathEntry entry = (IClasspathEntry) iterator.next();
 			
 			List currentExpanded = new ArrayList();
 			if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 				try {
 					IClasspathContainer container = JavaCore.getClasspathContainer(entry.getPath(), project);
 					IClasspathEntry[] entries = container.getClasspathEntries();
 					int size = entries.length;
 					for (int i = 0; i < size; i++) {
 						if (entries[i] != null)
 							currentExpanded.add(entries[i]);
 					}
 				} catch (Exception e) {
 					Trace.trace(Trace.FINEST, "Error resolving classpath container 3", e);
 				}
 			} else if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE) {
 				entry = JavaCore.getResolvedClasspathEntry(entry);
 				if (entry != null)
 					currentExpanded.add(entry);
 			} else
 				currentExpanded.add(entry);
 			
 			// loop over all of the expanded entries of this current entry
 			boolean dup = false;
 			Iterator iterator2 = currentExpanded.iterator();
 			while (!dup && iterator2.hasNext()) {
 				IClasspathEntry entry2 = (IClasspathEntry) iterator2.next();
 				Iterator iterator3 = addExpanded.iterator();
 				while (iterator3.hasNext()) {
 					IClasspathEntry entry3 = (IClasspathEntry) iterator3.next();
 					if (entry3.getPath().equals(entry2.getPath()))
 						dup = true;
 				}
 			}
 			if (dup && !remove.contains(entry))
 				remove.add(entry);
 		}
 		
 		// remove duplicates
 		iterator = remove.iterator();
 		while (iterator.hasNext()) {
 			current.remove(iterator.next());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.model.IRuntimeTargetDelegate#removeRuntimeTarget(org.eclipse.core.resources.IProject, org.eclipse.wst.server.core.IRuntime)
 	 */
 	public void removeRuntimeTarget(IProject project, IRuntime runtime, IProgressMonitor monitor) {
 		Trace.trace(Trace.FINEST, "Removing runtime target");
 		if (project == null || runtime == null)
 			return;
 
 		IJavaProject javaProject = null;
 		try {
 			javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
 		} catch (Exception e) {
 			// ignore
 		}
 
 		if (javaProject == null)
 			return;
 		
 		try {
 			IClasspathEntry[] delegates = getDelegateClasspathEntries(runtime, monitor);
 			int delegateSize = 0;
 			if (delegates != null)
 				delegateSize = delegates.length;
 
 			List list = new ArrayList();
 			IClasspathEntry[] cp = javaProject.getRawClasspath();
 			int size = cp.length;
 			for (int i = 0; i < size; i++) {
 				boolean remove = false;
 				
 				if (cp[i].getPath().segment(0).equals(RuntimeClasspathContainer.SERVER_CONTAINER))
 					remove = true;
 				
 				for (int j = 0; j < delegateSize; j++) {
 					if (cp[i].equals(delegates[j]))
 						remove = true;
 				}
 				if (!remove)
 					list.add(cp[i]);
 			}
 			
 			cp = new IClasspathEntry[list.size()];
 			list.toArray(cp);
 			javaProject.setRawClasspath(cp, monitor);
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Error removing runtime target", e);
 		}
 	}
 	
 	protected static void addJarFiles(File dir, List list, boolean includeSubdirectories) {
 		int depth = 0;
 		if (includeSubdirectories)
 			depth = 2;
 		addJarFiles(dir, list, depth);
 	}
 	
 	private static void addJarFiles(File dir, List list, int depth) {
 		if (dir == null)
			return;
 		
 		File[] files = dir.listFiles();
 		if (files != null) {
 			int size = files.length;
 			for (int i = 0; i < size; i++) {
 				if (files[i].isDirectory() && depth > 0) {
 					addJarFiles(files[i], list, depth - 1);
 				} else if (files[i].getAbsolutePath().endsWith(".jar") || files[i].getAbsolutePath().endsWith(".zip")) {
 					IPath path = new Path(files[i].getAbsolutePath());
 					list.add(JavaCore.newLibraryEntry(path, null, null));
 				}
 			}
 		}
 	}
 	
 	protected static void addLibraryEntries(List list, File dir, boolean includeSubdirectories) {
 		addJarFiles(dir, list, includeSubdirectories);
 	}
 	
 	protected static void addLibraryEntry(List list, File dir) {
 		IPath path = new Path(dir.getAbsolutePath());
 		list.add(JavaCore.newLibraryEntry(path, null, null));
 	}
 	
 	protected static void addLibraryEntry(List list, IPath path) {
 		list.add(JavaCore.newLibraryEntry(path, null, null));
 	}
 	
 	protected static void addLibraryEntry(List list, IPath path, IPath source, IPath root) {
 		list.add(JavaCore.newLibraryEntry(path, source, root));
 	}
 
 	protected static IClasspathEntry[] resolveList(List list) {
 		IClasspathEntry[] entries = new IClasspathEntry[list.size()]; 
 		list.toArray(entries);
 		return entries;
 	}
 
 	/**
 	 * Returns the classpath entries that correspond to the given runtime.
 	 * 
 	 * @param runtime
 	 * @param monitor
 	 * @return an array of classpath entries
 	 */
 	public IClasspathEntry[] getDelegateClasspathEntries(IRuntime runtime, IProgressMonitor monitor) {
 		return null;
 	}
 
 	/**
 	 * Returns the classpath entry ids for this runtime target handler. These
 	 * ids will be added to the classpath container id to create a new fully
 	 * qualified classpath container id.
 	 * <p>
 	 * By default, there is a single classpath entry for the runtime, with no
 	 * extra id (<code>new String[1]</code>). To create multiple ids, just
 	 * return a string array containing the ids. For instance, to have two
 	 * classpath containers with ids "id1" and "id2", use
 	 * <code>new String[] { "id1", "id2" }</code>
 	 * </p>
 	 * 
 	 * @return an array of classpath entry ids
 	 */
 	public String[] getClasspathEntryIds() {
 		return new String[1];
 	}
 
 	/**
 	 * Request that the classpath container for the given runtime and id be updated
 	 * with the given classpath container entries.
 	 * 
 	 * @param runtime
 	 * @param id
 	 * @param entries
 	 */
 	public void requestClasspathContainerUpdate(IRuntime runtime, String id, IClasspathEntry[] entries) {
 		// default behaviour is to save the source path entries
 		if (runtime == null || entries == null)
 			return;
 		
 		// find the source attachments
 		sourceAttachments = new ArrayList();
 		
 		int size = entries.length;
 		for (int i = 0; i < size; i++) {
 			if (entries[i].getSourceAttachmentPath() != null) {
 				SourceAttachmentUpdate sau = new SourceAttachmentUpdate();
 				sau.runtimeId = runtime.getId();
 				sau.id = id;
 				sau.entry = entries[i].getPath();
 				sau.sourceAttachmentPath = entries[i].getSourceAttachmentPath();
 				sau.sourceAttachmentRootPath = entries[i].getSourceAttachmentRootPath();
 				sourceAttachments.add(sau);
 			}
 		}
 		save();
 	}
 
 	/**
 	 * Returns the classpath container label for the given runtime and the given
 	 * classpath container id (returned from getClasspathEntryIds()). This method
 	 * must not return null.
 	 * 
 	 * @param runtime the runtime to resolve the container label for
 	 * @param id the classpath entry id
 	 * @return a classpath container label
 	 */
 	public abstract String getClasspathContainerLabel(IRuntime runtime, String id);
 
 	public IClasspathEntry[] resolveClasspathContainerImpl(IRuntime runtime, String id) {
 		IClasspathEntry[] entries = resolveClasspathContainer(runtime, id);
 		
 		if (entries == null)
 			entries = new IClasspathEntry[0];
 		
 		if (sourceAttachments == null)
 			load();
 		
 		int size = entries.length;
 		int size2 = sourceAttachments.size();
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size2; j++) {
 				SourceAttachmentUpdate sau = (SourceAttachmentUpdate) sourceAttachments.get(j);
 				if ((id != null && sau.id.equals(id)) || (id == null && sau.id == null)) {
 					if (sau.runtimeId.equals(runtime.getId()) && sau.entry.equals(entries[i].getPath())) {
 						entries[i] = JavaCore.newLibraryEntry(entries[i].getPath(), sau.sourceAttachmentPath, sau.sourceAttachmentRootPath);
 					}
 				}
 			}
 		}
 		
 		return entries;
 	}
 	
 	private void save() {
 		if (sourceAttachments == null)
 			return;
 		String id = getRuntimeTargetHandler().getId();
 		String filename = JavaServerPlugin.getInstance().getStateLocation().append(id + ".xml").toOSString();
 		try {
 			XMLMemento memento = XMLMemento.createWriteRoot("classpath");
 
 			Iterator iterator = sourceAttachments.iterator();
 			while (iterator.hasNext()) {
 				SourceAttachmentUpdate sau = (SourceAttachmentUpdate) iterator.next();
 				IMemento child = memento.createChild("source-attachment");
 				child.putString("runtime-id", sau.runtimeId);
 				if (sau.id != null)
 					child.putString("id", sau.id);
 				if (sau.entry != null)
 					child.putString("entry", sau.entry.toPortableString());
 				if (sau.sourceAttachmentPath != null)
 					child.putString("source-attachment-path", sau.sourceAttachmentPath.toPortableString());
 				if (sau.sourceAttachmentRootPath != null)
 					child.putString("source-attachment-root-path", sau.sourceAttachmentRootPath.toPortableString());
 			}
 			
 			memento.saveToFile(filename);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error saving source path info", e);
 		}
 	}
 
 	private void load() {
 		if (getRuntimeTargetHandler() == null)
 			return;
 		String id = getRuntimeTargetHandler().getId();
 		String filename = JavaServerPlugin.getInstance().getStateLocation().append(id + ".xml").toOSString();
 		
		sourceAttachments = new ArrayList();
 		try {
 			IMemento memento = XMLMemento.loadMemento(filename);
 			
 			IMemento[] children = memento.getChildren("source-attachment");
 			int size = children.length;
 			
 			for (int i = 0; i < size; i++) {
 				try {
 					SourceAttachmentUpdate sau = new SourceAttachmentUpdate();
 					sau.runtimeId = children[i].getString("runtime-id");
 					sau.id = children[i].getString("id");
 					String temp = children[i].getString("entry");
 					if (temp != null)
 						sau.entry = new Path(temp);
 					temp = children[i].getString("source-attachment-path");
 					if (temp != null)
 						sau.sourceAttachmentPath = new Path(temp);
 					temp = children[i].getString("source-attachment-root-path");
 					if (temp != null)
 						sau.sourceAttachmentRootPath = new Path(temp);
 					sourceAttachments.add(sau);
 				} catch (Exception e) {
 					Trace.trace(Trace.WARNING, "Could not load monitor: " + e);
 				}
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.WARNING, "Could not load source path info: " + e.getMessage());
 		}
 	}
 
 	/**
 	 * Resolves (creates the classpath entries for) the classpath container with
 	 * the given runtime and the given classpath container id (returned from
 	 * getClasspathEntryIds()). If the classpath container cannot be resolved
 	 * (for instance, if the runtime does not exist), return null.
 	 * 
 	 * @param runtime the runtime to resolve the container for
 	 * @param id the classpath entry id
 	 * @return an array of classpath entries for the container, or null if the
 	 *   container could not be resolved
 	 */
 	public abstract IClasspathEntry[] resolveClasspathContainer(IRuntime runtime, String id);
 }
