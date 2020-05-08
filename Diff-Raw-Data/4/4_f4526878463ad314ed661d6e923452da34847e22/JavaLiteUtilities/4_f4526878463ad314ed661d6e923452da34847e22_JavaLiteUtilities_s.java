 /*******************************************************************************
  * Copyright (c) 2009 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.common.jdt.internal.javalite;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jst.common.frameworks.CommonFrameworksPlugin;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 
 public final class JavaLiteUtilities {
 
 	/**
 	 * Returns the Java source (i.e. where the .java files are) IContainers for
 	 * the specified IJavaProjectLite
 	 * 
 	 * @param javaProjectLite
 	 * @return
 	 */
 	public final static List<IContainer> getJavaSourceContainers(final IJavaProjectLite javaProjectLite) {
 		IClasspathEntry[] entries = javaProjectLite.readRawClasspath();
 		List<IContainer> containers = new ArrayList<IContainer>();
 		for (IClasspathEntry entry : entries) {
 			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				IContainer container = javaProjectLite.getProject().getFolder(entry.getPath());
 				containers.add(container);
 			}
 		}
 		return containers;
 	}
 
 	/**
 	 * Returns the Java source (i.e. where the compiled .class files are) IContainers for
 	 * the specified IJavaProjectLite
 	 * 
 	 * @param javaProjectLite
 	 * @return
 	 */
 	public final static List<IContainer> getJavaOutputContainers(final IJavaProjectLite javaProjectLite) {
 		List<IContainer> containers = new ArrayList<IContainer>();
 		IContainer defaultOutputContainer = getDefaultJavaOutputContainer(javaProjectLite);
 		containers.add(defaultOutputContainer);
 		IClasspathEntry[] entries = javaProjectLite.readRawClasspath();
 		for (IClasspathEntry entry : entries) {
 			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 				IContainer outputContainer = getJavaOutputContainer(javaProjectLite, entry);
 				if (!containers.contains(outputContainer)) {
 					containers.add(outputContainer);
 				}
 			}
 		}
 		return containers;
 	}
 
 	private static enum JavaContainerType {
 		SOURCE, OUTPUT
 	}
 
 	/**
 	 * Returns all Java output (i.e. where the compiled .class files are)
 	 * IContainers whose source is explicitly mapped by the specified component,
 	 * or if the output container itself is explicitly mapped.
 	 * 
 	 * @param virtualComponent
 	 * @return
 	 */
 	public static List<IContainer> getJavaOutputContainers(IVirtualComponent virtualComponent) {
 		return getJavaContainers(virtualComponent, JavaContainerType.OUTPUT);
 	}
 
 	/**
 	 * Returns all Java source (i.e. where the .java files are) IContainers
 	 * explicitly mapped by the specified component.
 	 * 
 	 * @param virtualComponent
 	 * @return
 	 */
 	public static List<IContainer> getJavaSourceContainers(IVirtualComponent virtualComponent) {
 		return getJavaContainers(virtualComponent, JavaContainerType.SOURCE);
 	}
 
 	private static List<IContainer> getJavaContainers(IVirtualComponent virtualComponent, JavaContainerType javaContainerType) {
 		if (virtualComponent.isBinary()) {
 			return Collections.emptyList();
 		}
 		IProject project = virtualComponent.getProject();
 		try {
 			if (!project.hasNature(JavaCoreLite.NATURE_ID)) {
 				return Collections.emptyList();
 			}
 		} catch (CoreException e) {
 			CommonFrameworksPlugin.logError(e);
 			return Collections.emptyList();
 		}
 
 		IJavaProjectLite javaProjectLite = JavaCoreLite.create(project);
 		List<IContainer> containers = new ArrayList<IContainer>();
 		if (javaContainerType == JavaContainerType.OUTPUT) {
 			IContainer defaultOutputContainer = getDefaultJavaOutputContainer(javaProjectLite);
 			IVirtualResource[] virtualResources = ComponentCore.createResources(defaultOutputContainer);
 			for (IVirtualResource virtualResource : virtualResources) {
 				if (virtualResource.getComponent().equals(virtualComponent)) {
 					containers.add(defaultOutputContainer);
 					break;
 				}
 			}
 		}
 		IClasspathEntry[] entries = javaProjectLite.readRawClasspath();
 		for (IClasspathEntry entry : entries) {
 			if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 				IPath sourcePath = entry.getPath().removeFirstSegments(1); // remove the project from the path
 				IContainer sourceContainer = sourcePath.segmentCount() == 0 ? project : project.getFolder(sourcePath);
 				if (sourceContainer != null) {
 					IVirtualResource[] virtualResources = ComponentCore.createResources(sourceContainer);
 					for (IVirtualResource virtualResource : virtualResources) {
 						if (virtualResource.getComponent().equals(virtualComponent)) {
 							switch (javaContainerType) {
 							case SOURCE:
 								if (!containers.contains(sourceContainer)) {
 									containers.add(sourceContainer);
 								}
 								break;
 							case OUTPUT:
 								IContainer outputContainer = getJavaOutputContainer(javaProjectLite, entry);
 								if (!containers.contains(outputContainer)) {
 									containers.add(outputContainer);
 								}
 								break;
 							}
 						}
 					}
 				}
 			}
 		}
 		return containers;
 	}
 
 	/**
 	 * Returns the default Java output IContainer (i.e. where the compiled
 	 * .class files go)
 	 * 
 	 * @param javaProjectLite
 	 * @return
 	 */
 	public static IContainer getDefaultJavaOutputContainer(IJavaProjectLite javaProjectLite) {
 		IProject project = javaProjectLite.getProject();
 		IPath defaultOutputPath = javaProjectLite.readOutputLocation();
 		if (defaultOutputPath.segmentCount() == 1) {
 			return project;
 		}
 		return project.getFolder(defaultOutputPath.removeFirstSegments(1));
 	}
 
 	/**
 	 * Returns the Java output (i.e. where the compiled .class files go)
 	 * IContainer for the specified IClasspathEntry
 	 * 
 	 * @param javaProjectLite
 	 * @param entry
 	 * @return
 	 */
 	public static IContainer getJavaOutputContainer(IJavaProjectLite javaProjectLite, IClasspathEntry entry) {
 		IProject project = javaProjectLite.getProject();
 		IPath outputPath = entry.getOutputLocation();
 		if (outputPath != null) {
 			return project.getFolder(outputPath.removeFirstSegments(1));
 		}
 		return getDefaultJavaOutputContainer(javaProjectLite);
 	}
 }
