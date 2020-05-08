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
 /*
  * Created on Mar 17, 2004
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.project;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.StringTokenizer;
 
 import org.eclipse.core.internal.runtime.Assert;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ILibrary;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IPluginDescriptor;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.emf.codegen.CodeGenPlugin;
 import org.eclipse.emf.codegen.jet.JETCompiler;
 import org.eclipse.emf.codegen.jet.JETEmitter;
 import org.eclipse.emf.codegen.jet.JETException;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaModel;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.core.ClasspathEntry;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jem.util.logger.proxy.Logger;
 
 /**
  * @author schacher, mdelder
  * 
  * To change the template for this generated type comment go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 public class WTPJETEmitter extends JETEmitter {
 
 	private Map variables;
 
 	private boolean intelligentLinkingEnabled = false;
 
 	private JETCompiler jetCompiler;
 
 	/**
 	 * @param templateURI
 	 */
 	public WTPJETEmitter(String templateURI) {
 		super(templateURI);
 	}
 
 	/**
 	 * @param templateURIPath
 	 * @param relativeTemplateURI
 	 */
 	public WTPJETEmitter(String[] templateURIPath, String relativeTemplateURI) {
 		super(templateURIPath, relativeTemplateURI);
 	}
 
 	/**
 	 * @param templateURI
 	 * @param classLoader
 	 */
 	public WTPJETEmitter(String templateURI, ClassLoader classLoader) {
 		super(templateURI, classLoader);
 	}
 
 	/**
 	 * @param templateURIPath
 	 * @param relativeTemplateURI
 	 * @param classLoader
 	 */
 	public WTPJETEmitter(String[] templateURIPath, String relativeTemplateURI, ClassLoader classLoader) {
 		super(templateURIPath, relativeTemplateURI, classLoader);
 	}
 
 	/**
 	 * @param templateURIPath
 	 * @param relativeTemplateURI
 	 * @param classLoader
 	 * @param encoding
 	 */
 	public WTPJETEmitter(String[] templateURIPath, String relativeTemplateURI, ClassLoader classLoader, String encoding) {
 		super(templateURIPath, relativeTemplateURI, classLoader, encoding);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.codegen.jet.JETEmitter#initialize(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void initialize(IProgressMonitor progressMonitor) throws JETException {
 
 		progressMonitor.beginTask("", 10); //$NON-NLS-1$
 		progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_GeneratingJETEmitterFor_message", new Object[]{templateURI})); //$NON-NLS-1$
 
 		try {
 			// This ensures that the JRE variables are initialized.
 			//
 			try {
 				JavaRuntime.getDefaultVMInstall();
 			} catch (Throwable throwable) {
 				// This is kind of nasty to come here.
 				//
 				URL jreURL = new URL(Platform.getPlugin("org.eclipse.emf.codegen").getDescriptor().getInstallURL(), "plugin.xml"); //$NON-NLS-1$ //$NON-NLS-2$
 				IPath jrePath = new Path(Platform.asLocalURL(jreURL).getFile());
 				jrePath = jrePath.removeLastSegments(1).append(new Path("../../jre/lib/rt.jar")); //$NON-NLS-1$
 				if (!jrePath.equals(JavaCore.getClasspathVariable(JavaRuntime.JRELIB_VARIABLE))) {
 					JavaCore.setClasspathVariable(JavaRuntime.JRELIB_VARIABLE, jrePath, null);
 				}
 			}
 
 			/*
 			 * final JETCompiler jetCompiler = templateURIPath == null ? new
 			 * MyJETCompiler(templateURI, encoding) :
 			 */
 
 			getJetCompiler();
 
 			progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETParsing_message", new Object[]{jetCompiler.getResolvedTemplateURI()})); //$NON-NLS-1$
 			jetCompiler.parse();
 			progressMonitor.worked(1);
 
 			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 			jetCompiler.generate(outputStream);
 			final InputStream contents = new ByteArrayInputStream(outputStream.toByteArray());
 
 			final IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			IJavaModel javaModel = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
 			if (!javaModel.isOpen()) {
 				javaModel.open(new SubProgressMonitor(progressMonitor, 1));
 			} else {
 				progressMonitor.worked(1);
 			}
 
 			final IProject project = workspace.getRoot().getProject(getProjectName());
 			progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETPreparingProject_message", new Object[]{project.getName()})); //$NON-NLS-1$
 
 			IJavaProject javaProject;
 			if (!project.exists()) {
 				javaProject = createJavaProject(progressMonitor, workspace, project);
 
 				initializeJavaProject(progressMonitor, project, javaProject);
 
 				javaProject.close();
 			} else {
 				project.open(new SubProgressMonitor(progressMonitor, 5));
 				javaProject = JavaCore.create(project);
 			}
 
 			IPackageFragmentRoot sourcePackageFragmentRoot = openJavaProjectIfNecessary(progressMonitor, project, javaProject);
 
 			String packageName = jetCompiler.getSkeleton().getPackageName();
 			StringTokenizer stringTokenizer = new StringTokenizer(packageName, "."); //$NON-NLS-1$
 			IProgressMonitor subProgressMonitor = new SubProgressMonitor(progressMonitor, 1);
 			subProgressMonitor.beginTask("", stringTokenizer.countTokens() + 4); //$NON-NLS-1$
 			subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_CreateTargetFile_message")); //$NON-NLS-1$
 			IContainer sourceContainer = (IContainer) sourcePackageFragmentRoot.getCorrespondingResource();
 			while (stringTokenizer.hasMoreElements()) {
 				String folderName = stringTokenizer.nextToken();
 				sourceContainer = sourceContainer.getFolder(new Path(folderName));
 				if (!sourceContainer.exists()) {
 					((IFolder) sourceContainer).create(false, true, new SubProgressMonitor(subProgressMonitor, 1));
 				}
 			}
 			IFile targetFile = sourceContainer.getFile(new Path(jetCompiler.getSkeleton().getClassName() + ".java")); //$NON-NLS-1$
 			if (!targetFile.exists()) {
 				subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETCreating_message", new Object[]{targetFile.getFullPath()})); //$NON-NLS-1$
 				targetFile.create(contents, true, new SubProgressMonitor(subProgressMonitor, 1));
 			} else {
 				subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETUpdating_message", new Object[]{targetFile.getFullPath()})); //$NON-NLS-1$
 				targetFile.setContents(contents, true, true, new SubProgressMonitor(subProgressMonitor, 1));
 			}
 
 			boolean errors = performBuild(project, subProgressMonitor, targetFile);
 
 			if (!errors) {
 				loadClass(workspace, project, javaProject, packageName, subProgressMonitor);
 			}
 
 			subProgressMonitor.done();
 		} catch (CoreException exception) {
 			throw new JETException(exception);
 		} catch (Exception exception) {
 			throw new JETException(exception);
 		} finally {
 			progressMonitor.done();
 		}
 
 	}
 
 	/**
 	 * @param progressMonitor
 	 * @param project
 	 * @param javaProject
 	 * @return
 	 * @throws JavaModelException
 	 */
 	protected IPackageFragmentRoot openJavaProjectIfNecessary(IProgressMonitor progressMonitor, final IProject project, IJavaProject javaProject) throws JavaModelException {
 		progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETOpeningJavaProject_message", new Object[]{project.getName()})); //$NON-NLS-1$
 		javaProject.open(new SubProgressMonitor(progressMonitor, 1));
 
 		IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
 		IPackageFragmentRoot sourcePackageFragmentRoot = null;
 		for (int j = 0; j < packageFragmentRoots.length; ++j) {
 			IPackageFragmentRoot packageFragmentRoot = packageFragmentRoots[j];
 			if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
 				sourcePackageFragmentRoot = packageFragmentRoot;
 				break;
 			}
 		}
 		return sourcePackageFragmentRoot;
 	}
 
 	/**
 	 * @param progressMonitor
 	 * @param project
 	 * @param javaProject
 	 * @throws CoreException
 	 * @throws JavaModelException
 	 */
 	protected void initializeJavaProject(IProgressMonitor progressMonitor, final IProject project, IJavaProject javaProject) throws CoreException, JavaModelException {
 		progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETInitializingProject_message", new Object[]{project.getName()})); //$NON-NLS-1$
 		IClasspathEntry classpathEntry = JavaCore.newSourceEntry(new Path("/" + project.getName() + "/src")); //$NON-NLS-1$ //$NON-NLS-2$
 
 		IClasspathEntry jreClasspathEntry = JavaCore.newVariableEntry(new Path(JavaRuntime.JRELIB_VARIABLE), new Path(JavaRuntime.JRESRC_VARIABLE), new Path(JavaRuntime.JRESRCROOT_VARIABLE));
 
 		List classpath = new ArrayList();
 		classpath.add(classpathEntry);
 		classpath.add(jreClasspathEntry);
 		classpath.addAll(classpathEntries);
 
 		IFolder sourceFolder = project.getFolder(new Path("src")); //$NON-NLS-1$
 		if (!sourceFolder.exists()) {
 			sourceFolder.create(false, true, new SubProgressMonitor(progressMonitor, 1));
 		}
 		IFolder runtimeFolder = project.getFolder(new Path("runtime")); //$NON-NLS-1$
 		if (!runtimeFolder.exists()) {
 			runtimeFolder.create(false, true, new SubProgressMonitor(progressMonitor, 1));
 		}
 
 		IClasspathEntry[] classpathEntryArray = (IClasspathEntry[]) classpath.toArray(new IClasspathEntry[classpath.size()]);
 
 		javaProject.setRawClasspath(classpathEntryArray, new SubProgressMonitor(progressMonitor, 1));
 
 		javaProject.setOutputLocation(new Path("/" + project.getName() + "/runtime"), new SubProgressMonitor(progressMonitor, 1)); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// appended from previous implementation
 		createClasspathEntries(project);
 	}
 
 	/**
 	 * @param progressMonitor
 	 * @param workspace
 	 * @param project
 	 * @return
 	 * @throws CoreException
 	 */
 	private IJavaProject createJavaProject(IProgressMonitor progressMonitor, final IWorkspace workspace, final IProject project) throws CoreException {
 		IJavaProject javaProject;
 		progressMonitor.subTask("JET creating project " + project.getName()); //$NON-NLS-1$
 		project.create(new SubProgressMonitor(progressMonitor, 1));
 		progressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETCreatingProject_message", new Object[]{project.getName()})); //$NON-NLS-1$
 		IProjectDescription description = workspace.newProjectDescription(project.getName());
 		description.setNatureIds(new String[]{JavaCore.NATURE_ID});
 		description.setLocation(null);
 		project.open(new SubProgressMonitor(progressMonitor, 1));
 		project.setDescription(description, new SubProgressMonitor(progressMonitor, 1));
 
 		javaProject = JavaCore.create(project);
 		return javaProject;
 	}
 
 	/**
 	 * @param workspace
 	 * @param project
 	 * @param javaProject
 	 * @param packageName
 	 * @param subProgressMonitor
 	 * @throws JavaModelException
 	 * @throws MalformedURLException
 	 * @throws ClassNotFoundException
 	 * @throws SecurityException
 	 */
 	protected void loadClass(final IWorkspace workspace, final IProject project, IJavaProject javaProject, String packageName, IProgressMonitor subProgressMonitor) throws JavaModelException, MalformedURLException, ClassNotFoundException, SecurityException {
 		//IContainer targetContainer =
 		// workspace.getRoot().getFolder(javaProject.getOutputLocation());
 
 		subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETLoadingClass_message", new Object[]{jetCompiler.getSkeleton().getClassName() + ".class"})); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// Construct a proper URL for relative lookup.
 		//
 		URL url = new File(project.getLocation() + "/" + javaProject.getOutputLocation().removeFirstSegments(1) + "/").toURL(); //$NON-NLS-1$ //$NON-NLS-2$
 		URLClassLoader theClassLoader = new URLClassLoader(new URL[]{url}, classLoader);
 		Class theClass = theClassLoader.loadClass((packageName.length() == 0 ? "" : packageName + ".") + jetCompiler.getSkeleton().getClassName()); //$NON-NLS-1$ //$NON-NLS-2$
 		String methodName = jetCompiler.getSkeleton().getMethodName();
 		Method[] methods = theClass.getDeclaredMethods();
 		for (int i = 0; i < methods.length; ++i) {
 			if (methods[i].getName().equals(methodName)) {
 				setMethod(methods[i]);
 				break;
 			}
 		}
 	}
 
 	/**
 	 * @param project
 	 * @param subProgressMonitor
 	 * @param targetFile
 	 * @return
 	 * @throws CoreException
 	 */
 	protected boolean performBuild(final IProject project, IProgressMonitor subProgressMonitor, IFile targetFile) throws CoreException {
 		subProgressMonitor.subTask(CodeGenPlugin.getPlugin().getString("_UI_JETBuilding_message", new Object[]{project.getName()})); //$NON-NLS-1$
 		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, new SubProgressMonitor(subProgressMonitor, 1));
 
 		IMarker[] markers = targetFile.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 		boolean errors = false;
 		for (int i = 0; i < markers.length; ++i) {
 			IMarker marker = markers[i];
 			if (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO) == IMarker.SEVERITY_ERROR) {
 				errors = true;
 				subProgressMonitor.subTask(marker.getAttribute(IMarker.MESSAGE) + " : " + (CodeGenPlugin.getPlugin().getString("jet.mark.file.line", new Object[]{targetFile.getLocation(), marker.getAttribute(IMarker.LINE_NUMBER)}))); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 		}
 		return errors;
 	}
 
 	/**
 	 * Create the correct classpath entries for the given project
 	 * 
 	 * @param project
 	 */
 	protected void createClasspathEntries(IProject project) {
 		Assert.isNotNull(project, "A valid project is required in order to append to a classpath"); //$NON-NLS-1$
 		String variableName = null;
 		String pluginId = null;
 		for (Iterator variablesKeyIterator = getVariables().keySet().iterator(); variablesKeyIterator.hasNext();) {
 			variableName = (String) variablesKeyIterator.next();
 			pluginId = (String) getVariables().get(variableName);
 			if (hasOutputDirectory(pluginId))
 				addLinkedFolderAsLibrary(project, variableName, pluginId);
 			else
 				addRuntimeJarsAsLibrary(project, pluginId);
 		}
 	}
 
 	/**
 	 * @param pluginId
 	 * @return
 	 */
 	protected boolean hasOutputDirectory(String pluginId) {
 		IPluginDescriptor pluginDescriptor = Platform.getPlugin(pluginId).getDescriptor();
 		URL outputDirectory = pluginDescriptor.find(new Path("bin")); //$NON-NLS-1$
 		if (outputDirectory == null)
 			return false;
 		java.io.File outputDirectoryFile = new File(outputDirectory.getPath());
 		return outputDirectoryFile.canRead() && outputDirectoryFile.isDirectory() && outputDirectoryFile.listFiles().length > 0;
 	}
 
 	/**
 	 * @param project
 	 * @param variableName
 	 * @param pluginId
 	 */
 	protected void addRuntimeJarsAsLibrary(IProject project, String pluginId) {
 		IPluginDescriptor pluginDescriptor = Platform.getPlugin(pluginId).getDescriptor();
 		ILibrary[] runtimeLibs = pluginDescriptor.getRuntimeLibraries();
 		IClasspathEntry entry = null;
 		IPath runtimeLibFullPath = null;
 		URL fullurl = null;
 		for (int i = 0; i < runtimeLibs.length; i++) {
 			fullurl = pluginDescriptor.find(runtimeLibs[i].getPath());
 			// fix the problem with leading slash that caused dup classpath entries
 			File file = new File(fullurl.getPath());
 			runtimeLibFullPath = new Path(file.toString());
 			entry = new ClasspathEntry(IPackageFragmentRoot.K_BINARY, IClasspathEntry.CPE_LIBRARY, runtimeLibFullPath, ClasspathEntry.INCLUDE_ALL, ClasspathEntry.EXCLUDE_NONE, null, /*
 																																													   * Source
 																																													   * attachment
 																																													   * root
 																																													   */
 			null, /* Source attachment root */
 			null, /* specific output folder */
 			false,
			null,false,ClasspathEntry.NO_EXTRA_ATTRIBUTES); /* is exported ? *///JavaCore.newLibraryEntry(runtimeLibFullPath, null, null);
 			try {
 				J2EEProjectUtilities.appendJavaClassPath(project, entry);
 			} catch (JavaModelException e) {
 				Logger.getLogger().logError("Problem appending \"" + entry.getPath() + "\" to classpath of Project \"" + project.getName() + "\"."); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				Logger.getLogger().logError(e);
 			}
 		}
 	}
 
 	/**
 	 * @param progressMonitor
 	 */
 	protected void updateProgress(IProgressMonitor progressMonitor, String key) {
 		progressMonitor.subTask(getMessage(key));
 	}
 
 	/**
 	 * @param progressMonitor
 	 */
 	protected void updateProgress(IProgressMonitor progressMonitor, String key, Object[] args) {
 		progressMonitor.subTask(getMessage(key, args));
 	}
 
 	protected String getMessage(String key) {
 		return CodeGenPlugin.getPlugin().getString(key);
 	}
 
 	protected String getMessage(String key, Object[] args) {
 		return CodeGenPlugin.getPlugin().getString(key, args);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.emf.codegen.jet.JETEmitter#addVariable(java.lang.String, java.lang.String)
 	 */
 	public void addVariable(String variableName, String pluginID) throws JETException {
 		if (!isIntelligentLinkingEnabled())
 			super.addVariable(variableName, pluginID);
 		else {
 			getVariables().put(variableName, pluginID);
 			IWorkspace workspace = ResourcesPlugin.getWorkspace();
 			IProject project = workspace.getRoot().getProject(getProjectName());
 			if (project != null && project.exists())
 				createClasspathEntries(project);
 		}
 
 	}
 
 	/**
 	 * @return
 	 */
 	private boolean isIntelligentLinkingEnabled() {
 		return intelligentLinkingEnabled;
 	}
 
 	/**
 	 * @return
 	 */
 	private Map getVariables() {
 		if (variables == null)
 			variables = new HashMap();
 		return variables;
 	}
 
 	protected void addLinkedFolderAsLibrary(IProject project, String variableName, String pluginID) {
 		try {
 			IPluginDescriptor pluginDescriptor = Platform.getPlugin(pluginID).getDescriptor();
 			URL outputDirectory = pluginDescriptor.find(new Path("bin")); //$NON-NLS-1$
 			String pathString = Platform.asLocalURL(outputDirectory).getPath();
 			if (pathString.endsWith("/")) //$NON-NLS-1$
 				pathString = pathString.substring(0, pathString.length() - 1);
 			if (pathString.startsWith("/")) //$NON-NLS-1$
 				pathString = pathString.substring(1, pathString.length());
 			IPath path = new Path(pathString).makeAbsolute();
 
 			String binDirectoryVariable = variableName + "_BIN"; //$NON-NLS-1$
 			IFolder linkedDirectory = project.getFolder(binDirectoryVariable);
 			if (!linkedDirectory.exists()) {
 				linkedDirectory.createLink(path, IResource.ALLOW_MISSING_LOCAL, null);
 				linkedDirectory.setDerived(true);
 				project.refreshLocal(IResource.DEPTH_INFINITE, null);
 			}
 			IClasspathEntry entry = new ClasspathEntry(IPackageFragmentRoot.K_BINARY, IClasspathEntry.CPE_LIBRARY, linkedDirectory.getFullPath(), ClasspathEntry.INCLUDE_ALL, ClasspathEntry.EXCLUDE_NONE, null, // source
 						// attachment
 						null, // source attachment root
 						null, // custom output location
						false,null,false,ClasspathEntry.NO_EXTRA_ATTRIBUTES);
 
 			J2EEProjectUtilities.appendJavaClassPath(project, entry);
 
 		} catch (Exception e) {
 			Logger.getLogger().logError(e);
 		}
 	}
 
 	/**
 	 * @param intelligentLinkingEnabled
 	 *            The intelligentLinkingEnabled to set.
 	 */
 	public void setIntelligentLinkingEnabled(boolean intelligentLinkingEnabled) {
 		this.intelligentLinkingEnabled = intelligentLinkingEnabled;
 	}
 
 	protected JETCompiler getJetCompiler() {
 		try {
 			if (jetCompiler == null) {
 				jetCompiler = templateURIPath == null ? new MyJETCompiler(templateURI, encoding) : new MyJETCompiler(templateURIPath, templateURI, encoding);
 			}
 		} catch (JETException e) {
 			Logger.getLogger().logError(e);
 		}
 		return jetCompiler;
 	}
 }
