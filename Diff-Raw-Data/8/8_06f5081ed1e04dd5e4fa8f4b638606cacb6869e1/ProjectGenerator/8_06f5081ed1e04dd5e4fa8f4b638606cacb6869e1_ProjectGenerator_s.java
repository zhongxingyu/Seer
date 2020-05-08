 /**
  * Copyright (c) 2008, 2010 The RCER Development Team.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html. If redistributing this code,
  * this entire header must remain intact.
  *
  * $Id$
  */
 package net.sf.rcer.jcoimport;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import org.apache.tools.tar.TarEntry;
 import org.apache.tools.tar.TarInputStream;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.pde.core.plugin.IPluginModelBase;
 import org.eclipse.pde.internal.core.PDECore;
 import org.eclipse.pde.internal.core.PluginModelManager;
 import org.eclipse.pde.internal.core.exports.FeatureExportInfo;
 import org.eclipse.pde.internal.core.exports.PluginExportOperation;
 import org.eclipse.pde.internal.ui.PDEPluginImages;
 import org.eclipse.ui.progress.IProgressConstants;
 
 /**
  * The runnable that performs the actual project generation.
  * @author vwegert
  *
  */
 @SuppressWarnings("restriction")
 public class ProjectGenerator implements IRunnableWithProgress {
 
 	/**
 	 * The ID of the plug-in project nature as used by the PDE. 
 	 */
 	public static final String PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature"; //$NON-NLS-1$
 
 	private ProjectGeneratorSettings settings;
 	private IWorkspaceRoot workspaceRoot;
 	private ArrayList<IPluginModelBase> exportableBundles = new ArrayList<IPluginModelBase>();
 	private PluginModelManager modelManager;
 
 	/**
 	 * Default constructor.
 	 * @param generatorSettings
 	 */
 	public ProjectGenerator(ProjectGeneratorSettings generatorSettings) {
 		this.settings = generatorSettings;
 		workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		modelManager = PDECore.getDefault().getModelManager();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void run(IProgressMonitor monitor) throws InvocationTargetException,	InterruptedException {
 
 		String sourceArchive;
 
 		exportableBundles.clear();
 
 		monitor.beginTask(Messages.ProjectGenerator_TaskDescription, getNumberOfSteps());
 
 		// choose the source file for the sapjco.jar and the documentation from the selected files
 		if (settings.getWin32FileName().length() > 0) {
 			sourceArchive = settings.getWin32FileName();
 		} else if (settings.getWin64IAFileName().length() > 0)  {
 			sourceArchive = settings.getWin64IAFileName();
 		} else if (settings.getWin64x86FileName().length() > 0)  {
 			sourceArchive = settings.getWin64x86FileName();
 		} else if (settings.getLinux32FileName().length() > 0)  {
 			sourceArchive = settings.getLinux32FileName();
 		} else if (settings.getLinux64IAFileName().length() > 0)  {
 			sourceArchive = settings.getLinux64IAFileName();
 		} else if (settings.getLinux64x86FileName().length() > 0)  {
 			sourceArchive = settings.getLinux64x86FileName();
 		} else if (settings.getDarwin32FileName().length() > 0)  {
 			sourceArchive = settings.getDarwin32FileName();
 		} else if (settings.getDarwin64FileName().length() > 0)  {
 			sourceArchive = settings.getDarwin64FileName();
 		} else {
 			sourceArchive = ""; //$NON-NLS-1$
 		}
 
 		try {
 			// generate the plug-in containing the sapjco.jar archive
 			if (settings.isPluginProjectSelected()) {
 				if (sourceArchive.length() > 0) {
 					createJCoPluginProject(monitor, sourceArchive, IProjectNames.PLUGIN_JCO);
 				} else {
 					throw new InvocationTargetException(null, Messages.ProjectGenerator_NoInputFileError);
 				}
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			// generate the plug-in containing the documentation archive
 			if (settings.isDocPluginProjectSelected()) {
 				if (sourceArchive.length() > 0) {
 					createDocPluginProject(monitor, sourceArchive, IProjectNames.PLUGIN_JCO_DOC);
 				} else {
 					throw new InvocationTargetException(null, Messages.ProjectGenerator_NoInputFileError);
 				}
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			/*
 			 * See http://www.osgi.org/Specifications/Reference for a list of arch aliases
 			 * specified by OSGi.
 			 */
 
 			if (settings.isWin32FragmentSelected()) {
 				createFragmentProject(monitor, settings.getWin32FileName(), 
 						IProjectNames.FRAGMENT_WINDOWS_32,
 						"sapjco3.dll", Messages.ProjectGenerator_Win32Description, "(& (osgi.os=win32) (osgi.arch=x86))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isWin64IAFragmentSelected())  {
 				createFragmentProject(monitor, settings.getWin64IAFileName(), 
 						IProjectNames.FRAGMENT_WINDOWS_64IA,
 						"sapjco3.dll", Messages.ProjectGenerator_Win64IADescription, "(& (osgi.os=win32) (osgi.arch=ia64n))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isWin64x86FragmentSelected())  {
 				createFragmentProject(monitor, settings.getWin64x86FileName(), 
 						IProjectNames.FRAGMENT_WINDOWS_64X86,
						"sapjco3.dll", Messages.ProjectGenerator_Win64x86Description, "(& (osgi.os=win32) (osgi.arch=x86-64))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isLinux32FragmentSelected())  {
 				createFragmentProject(monitor, settings.getLinux32FileName(), 
 						IProjectNames.FRAGMENT_LINUX_32,
 						"libsapjco3.so", Messages.ProjectGenerator_Linux32Description, "(& (osgi.os=linux) (osgi.arch=x86))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isLinux64IAFragmentSelected())  {
 				createFragmentProject(monitor, settings.getLinux64IAFileName(), 
 						IProjectNames.FRAGMENT_LINUX_64IA,
 						"libsapjco3.so", Messages.ProjectGenerator_Linux64IADescription, "(& (osgi.os=linux) (osgi.arch=ia64n))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isLinux64x86FragmentSelected())  {
 				createFragmentProject(monitor, settings.getLinux64x86FileName(), 
 						IProjectNames.FRAGMENT_LINUX_64X86,
						"libsapjco3.so", Messages.ProjectGenerator_Linux64x86Description, "(& (osgi.os=linux) (osgi.arch=x86-64))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isDarwin32FragmentSelected())  {
 				createFragmentProject(monitor, settings.getDarwin32FileName(), 
 						IProjectNames.FRAGMENT_DARWIN_32,
 						"libsapjco3.jnilib", Messages.ProjectGenerator_Darwin32Description, "(& (osgi.os=macosx) (osgi.arch=x86))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isDarwin64FragmentSelected())  {
 				createFragmentProject(monitor, settings.getDarwin64FileName(), 
 						IProjectNames.FRAGMENT_DARWIN_64,
						"libsapjco3.jnilib", Messages.ProjectGenerator_Darwin64Description, "(& (osgi.os=macosx) (osgi.arch=x86-64))"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			// generate the plug-in containing the documentation archive
 			if (settings.isIDocPluginProjectSelected()) {
 				createIDocPluginProject(monitor, settings.getIDocFileName(), IProjectNames.PLUGIN_IDOC, IProjectNames.PLUGIN_JCO);
 			}
 			if (monitor.isCanceled()) throw new InterruptedException();
 
 			if (settings.isBundleExportSelected()) {
 				exportPlugins(monitor);
 			}
 
 		} catch (CoreException e) {
 			throw new InvocationTargetException(e);
 		} catch (IOException e) {
 			throw new InvocationTargetException(e);
 		} finally {
 			monitor.done();
 		}
 
 	}
 
 	/**
 	 * Creates a plug-in project for the SAP JCo from the source file specified.
 	 * @param monitor
 	 * @param sourceFileName
 	 * @param pluginName
 	 * @throws CoreException 
 	 * @throws IOException 
 	 */
 	private void createJCoPluginProject(IProgressMonitor monitor, String sourceFileName, String pluginName) throws CoreException, IOException {
 
 		monitor.subTask(MessageFormat.format(Messages.ProjectGenerator_CreatePluginTaskDescription, pluginName));
 
 		// read the source file                                                                              10
 		final Map<String, byte[]> files = readArchiveFile(sourceFileName);
 		monitor.worked(10);                                                
 
 		// remove the project if it exists                                                                    5
 		IProject project = workspaceRoot.getProject(pluginName);
 		if (project.exists()) {
 			project.delete(true, true, new SubProgressMonitor(monitor, 5));
 		} else {
 			monitor.worked(5);
 		}
 
 		// create and open the project                                                                       10
 		project.create(new SubProgressMonitor(monitor, 5));
 		project.open(new SubProgressMonitor(monitor, 5));
 
 		// update the project description                                                                     5
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(new String[] {	JavaCore.NATURE_ID, PLUGIN_NATURE_ID });
 		project.setDescription(description, new SubProgressMonitor(monitor, 5));
 
 		// set the basic Java project properties                                                              5
 		IJavaProject javaProject = JavaCore.create(project);
 		IFolder binDir = project.getFolder("bin"); //$NON-NLS-1$
 		IPath binPath = binDir.getFullPath();
 		javaProject.setOutputLocation(binPath, new SubProgressMonitor(monitor, 5));
 
 		// create empty jni folder                                                                            5
 		project.getFolder("jni").create(true, true, new SubProgressMonitor(monitor, 5)); //$NON-NLS-1$
 
 		// copy sapjco3.jar                                                                                  10
 		project.getFile("sapjco3.jar").create(new ByteArrayInputStream(files.get("sapjco3.jar")),  //$NON-NLS-1$ //$NON-NLS-2$
 				true, new SubProgressMonitor(monitor, 10));
 
 		// create META-INF and MANIFEST.MF                                                                   10
 		// TODO use the version information from the MANIFEST.MF file in the archive
 		IFolder metaInfFolder = project.getFolder("META-INF"); //$NON-NLS-1$
 		metaInfFolder.create(true, true, new SubProgressMonitor(monitor, 5));
 		StringBuilder manifest = new StringBuilder();
 		manifest.append("Manifest-Version: 1.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ManifestVersion: 2\n"); //$NON-NLS-1$
 		manifest.append("Bundle-Name: SAP Java Connector v3\n");  //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Bundle-SymbolicName: {0}\n", pluginName)); //$NON-NLS-1$
 		manifest.append("Bundle-Version: 7.11.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ClassPath: bin/,\n"); //$NON-NLS-1$
 		manifest.append(" sapjco3.jar,\n"); //$NON-NLS-1$
 		manifest.append(" jni/\n"); //$NON-NLS-1$
 		manifest.append("Bundle-Vendor: SAP AG, Walldorf (packaged using RCER)\n"); //$NON-NLS-1$
 		manifest.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"); //$NON-NLS-1$
 		manifest.append("Export-Package: com.sap.conn.jco,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.jco.ext,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.jco.monitor,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.jco.rt,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.jco.server\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ActivationPolicy: lazy\n"); //$NON-NLS-1$
 		writeTextFile(monitor, manifest, metaInfFolder.getFile("MANIFEST.MF")); //$NON-NLS-1$
 
 		// set classpath                                                                                      5
 		final IPath jcoPath = new Path(MessageFormat.format("/{0}/sapjco3.jar", pluginName)); //$NON-NLS-1$
 		IClasspathEntry jcoEntry = JavaCore.newLibraryEntry(jcoPath, Path.EMPTY, Path.EMPTY, true);
 		final IPath jniPath = new Path(MessageFormat.format("/{0}/jni", pluginName)); //$NON-NLS-1$
 		IClasspathEntry jniEntry = JavaCore.newLibraryEntry(jniPath, Path.EMPTY, Path.EMPTY, true);
 		javaProject.setRawClasspath(new IClasspathEntry[] { jcoEntry, jniEntry }, new SubProgressMonitor(monitor, 5));
 
 		// create build.properties                                                                            5
 		StringBuilder buildProperties = new StringBuilder();
 		buildProperties.append("bin.includes = META-INF/,\\\n"); //$NON-NLS-1$
 		buildProperties.append("               sapjco3.jar,\\\n"); //$NON-NLS-1$
 		buildProperties.append("               jni/,\\\n"); //$NON-NLS-1$
 		buildProperties.append("               .\n"); //$NON-NLS-1$
 		writeTextFile(monitor, buildProperties, project.getFile("build.properties")); //$NON-NLS-1$
 
 		// collect the plug-in for export
 		exportableBundles.add(modelManager.findModel(project));
 	}
 
 	/**
 	 * Creates a plug-in project for the SAP JCo documentation from the source file specified.
 	 * @param monitor
 	 * @param sourceFileName
 	 * @param pluginName
 	 * @throws CoreException 
 	 * @throws IOException 
 	 */
 	private void createDocPluginProject(IProgressMonitor monitor, String sourceFileName, String pluginName) throws CoreException, IOException {
 
 		monitor.subTask(MessageFormat.format(Messages.ProjectGenerator_CreatePluginTaskDescription, pluginName));
 
 		// read the source file                                                                              10
 		final Map<String, byte[]> files = readArchiveFile(sourceFileName);
 		monitor.worked(10);                                                
 
 		// remove the project if it exists                                                                    5
 		IProject project = workspaceRoot.getProject(pluginName);
 		if (project.exists()) {
 			project.delete(true, true, new SubProgressMonitor(monitor, 5));
 		} else {
 			monitor.worked(5);
 		}
 
 		// create and open the project                                                                       10
 		project.create(new SubProgressMonitor(monitor, 5));
 		project.open(new SubProgressMonitor(monitor, 5));
 
 		// update the project description                                                                     5
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(new String[] {	PLUGIN_NATURE_ID });
 		project.setDescription(description, new SubProgressMonitor(monitor, 5));
 
 		// copy the files from the generator to the target plug-in                                            5
 		copyPluginFile(monitor, project, "/docfiles/plugin.xml", "plugin.xml"); //$NON-NLS-1$ //$NON-NLS-2$
 		copyPluginFile(monitor, project, "/docfiles/toc.xml", "toc.xml"); //$NON-NLS-1$ //$NON-NLS-2$
 		copyPluginFile(monitor, project, "/docfiles/build.properties", "build.properties"); //$NON-NLS-1$ //$NON-NLS-2$
 		project.getFolder("html").create(true, true, null); //$NON-NLS-1$
 		copyPluginFile(monitor, project, "/docfiles/book.css", "html/book.css"); //$NON-NLS-1$ //$NON-NLS-2$
 		copyPluginFile(monitor, project, "/docfiles/note.html", "html/note.html"); //$NON-NLS-1$ //$NON-NLS-2$
 
 		// copy the documentation and example files                                                          25
 		monitor.subTask(Messages.ProjectGenerator_CopyDocumentationTaskDescription);
 		for (final String filename: files.keySet()) {
 			if ((filename.startsWith("examples") || filename.startsWith("javadoc")) && !(filename.endsWith("/"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 				monitor.subTask(MessageFormat.format(Messages.ProjectGenerator_CopyingFileTaskDescription, filename));
 				// first ensure that the target path exists
 				IFolder currentFolder = null;
 				final String[] parts = filename.split("/"); //$NON-NLS-1$
 				for (int i = 0; i < parts.length - 1; i++) {
 					if (currentFolder == null) {
 						currentFolder = project.getFolder(parts[i]);
 					} else {
 						currentFolder = currentFolder.getFolder(parts[i]);
 					}
 					if (!currentFolder.exists()) {
 						currentFolder.create(true, true, null);
 					}
 				}
 				project.getFile("/" + filename).create(new ByteArrayInputStream(files.get(filename)),  //$NON-NLS-1$ 
 						true, null);
 			}
 		}
 		monitor.worked(25);
 
 		// create META-INF and MANIFEST.MF                                                                   10
 		// TODO use the version information from the MANIFEST.MF file in the archive
 		IFolder metaInfFolder = project.getFolder("META-INF"); //$NON-NLS-1$
 		metaInfFolder.create(true, true, new SubProgressMonitor(monitor, 5));
 		StringBuilder manifest = new StringBuilder();
 		manifest.append("Manifest-Version: 1.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ManifestVersion: 2\n"); //$NON-NLS-1$
 		manifest.append("Bundle-Name: SAP Java Connector v3 Documentation\n");  //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Bundle-SymbolicName: {0};singleton:=true\n", pluginName)); //$NON-NLS-1$
 		manifest.append("Bundle-Version: 7.11.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-Vendor: SAP AG, Walldorf (packaged using RCER)\n"); //$NON-NLS-1$
 		manifest.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ActivationPolicy: lazy\n"); //$NON-NLS-1$
 		manifest.append("Require-Bundle: net.sf.rcer.doc\n"); //$NON-NLS-1$
 		writeTextFile(monitor, manifest, metaInfFolder.getFile("MANIFEST.MF")); //$NON-NLS-1$
 
 		// collect the plug-in for export
 		exportableBundles.add(modelManager.findModel(project));
 	}
 
 	/**
 	 * Auxiliary method to copy a file from the generator plug-in to the generated plug-in. 
 	 * @param monitor
 	 * @param project
 	 * @param sourceFileName
 	 * @param targetFileName
 	 * @throws CoreException
 	 * @throws IOException
 	 */
 	private void copyPluginFile(IProgressMonitor monitor, IProject project, String sourceFileName, String targetFileName) throws CoreException, IOException {
 		monitor.subTask(MessageFormat.format(Messages.ProjectGenerator_CopyingFileTaskDescription, targetFileName));
 		project.getFile(targetFileName).create(FileLocator.openStream(Activator.getDefault().getBundle(), //$NON-NLS-1$ 
 				new Path(sourceFileName), false),  //$NON-NLS-1$ 
 				true, null);
 		monitor.worked(1);
 	}
 	
 	/**
 	 * Creates a fragment project from the source file specified.
 	 * @param monitor 
 	 * @param sourceFileName 
 	 * @param fragmentName 
 	 * @param nativeLibraryFilename 
 	 * @param platformName 
 	 * @param platformFilter 
 	 * @throws CoreException 
 	 * @throws IOException 
 	 */
 	private void createFragmentProject(IProgressMonitor monitor, String sourceFileName, 
 			String fragmentName, String nativeLibraryFilename, String platformName, String platformFilter) 
 	throws CoreException, IOException {
 		monitor.subTask(MessageFormat.format(Messages.ProjectGenerator_CreateFragmentTaskDescription, fragmentName));
 
 		// read the source file                                                                              10
 		final Map<String, byte[]> files = readArchiveFile(sourceFileName);
 		monitor.worked(10);
 
 		// remove the project if it exists                                                                    5
 		IProject project = workspaceRoot.getProject(fragmentName);
 		if (project.exists()) {
 			project.delete(true, true, new SubProgressMonitor(monitor, 5));
 		} else {
 			monitor.worked(5);
 		}
 
 		// create and open the project                                                                       10
 		project.create(new SubProgressMonitor(monitor, 5));
 		project.open(new SubProgressMonitor(monitor, 5));
 
 		// update the project description                                                                     5
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(new String[] {	PLUGIN_NATURE_ID });
 		project.setDescription(description, new SubProgressMonitor(monitor, 5));
 
 		// create jni folder                                                                                  5
 		IFolder jniFolder = project.getFolder("jni"); //$NON-NLS-1$
 		jniFolder.create(true, true, new SubProgressMonitor(monitor, 5));
 
 		// copy native library file                                                                          10
 		jniFolder.getFile(nativeLibraryFilename).create(new ByteArrayInputStream(files.get(nativeLibraryFilename)), 
 				true, new SubProgressMonitor(monitor, 10));
 
 		// create META-INF and MANIFEST.MF                                                                   10
 		// TODO use the version information from the MANIFEST.MF file in the archive
 		IFolder metaInfFolder = project.getFolder("META-INF"); //$NON-NLS-1$
 		metaInfFolder.create(true, true, new SubProgressMonitor(monitor, 5));
 		StringBuilder manifest = new StringBuilder();
 		manifest.append("Manifest-Version: 1.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ManifestVersion: 2\n"); //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Bundle-Name: SAP Java Connector v3 - Native Libraries for {0}\n", platformName));  //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Bundle-SymbolicName: {0}\n", fragmentName)); //$NON-NLS-1$
 		manifest.append("Bundle-Version: 7.11.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-Vendor: SAP AG, Walldorf (packaged using RCER)\n"); //$NON-NLS-1$
 		manifest.append("Fragment-Host: com.sap.conn.jco;bundle-version=\"7.11.0\"\n");  //$NON-NLS-1$
 		manifest.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"); //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Bundle-NativeCode: jni/{0}\n", nativeLibraryFilename)); //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Eclipse-PlatformFilter: {0}\n", platformFilter)); //$NON-NLS-1$
 		writeTextFile(monitor, manifest, metaInfFolder.getFile("MANIFEST.MF")); //$NON-NLS-1$
 
 		// create build.properties                                                                            5
 		StringBuilder buildProperties = new StringBuilder();
 		buildProperties.append("bin.includes = META-INF/,\\\n"); //$NON-NLS-1$
 		buildProperties.append("               jni/\n"); //$NON-NLS-1$
 		writeTextFile(monitor, buildProperties, project.getFile("build.properties")); //$NON-NLS-1$
 
 		// collect the fragment for export
 		exportableBundles.add(modelManager.findModel(project));
 	}
 
 	/**
 	 * Creates a plug-in project for the SAP IDoc library from the source file specified.
 	 * @param monitor
 	 * @param sourceFileName
 	 * @param pluginName
 	 * @throws CoreException 
 	 * @throws IOException 
 	 */
 	private void createIDocPluginProject(IProgressMonitor monitor, String sourceFileName, String pluginName, String pluginNameJCo) throws CoreException, IOException {
 
 		monitor.subTask(MessageFormat.format(Messages.ProjectGenerator_CreatePluginTaskDescription, pluginName));
 
 		// read the source file                                                                              10
 		final Map<String, byte[]> files = readArchiveFile(sourceFileName);
 		monitor.worked(10);                                                
 
 		// remove the project if it exists                                                                    5
 		IProject project = workspaceRoot.getProject(pluginName);
 		if (project.exists()) {
 			project.delete(true, true, new SubProgressMonitor(monitor, 5));
 		} else {
 			monitor.worked(5);
 		}
 
 		// create and open the project                                                                       10
 		project.create(new SubProgressMonitor(monitor, 5));
 		project.open(new SubProgressMonitor(monitor, 5));
 
 		// update the project description                                                                     5
 		IProjectDescription description = project.getDescription();
 		description.setNatureIds(new String[] {	JavaCore.NATURE_ID, PLUGIN_NATURE_ID });
 		project.setDescription(description, new SubProgressMonitor(monitor, 5));
 
 		// set the basic Java project properties                                                              5
 		IJavaProject javaProject = JavaCore.create(project);
 		IFolder binDir = project.getFolder("bin"); //$NON-NLS-1$
 		IPath binPath = binDir.getFullPath();
 		javaProject.setOutputLocation(binPath, new SubProgressMonitor(monitor, 5));
 
 		// copy sapidoc3.jar                                                                                 15
 		project.getFile("sapidoc3.jar").create(new ByteArrayInputStream(files.get("sapidoc3.jar")),  //$NON-NLS-1$ //$NON-NLS-2$
 				true, new SubProgressMonitor(monitor, 15));
 
 		// create META-INF and MANIFEST.MF                                                                   10
 		// TODO use the version information from the MANIFEST.MF file in the archive
 		IFolder metaInfFolder = project.getFolder("META-INF"); //$NON-NLS-1$
 		metaInfFolder.create(true, true, new SubProgressMonitor(monitor, 5));
 		StringBuilder manifest = new StringBuilder();
 		manifest.append("Manifest-Version: 1.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ManifestVersion: 2\n"); //$NON-NLS-1$
 		manifest.append("Bundle-Name: SAP IDoc Library v3\n");  //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Bundle-SymbolicName: {0}\n", pluginName)); //$NON-NLS-1$
 		manifest.append("Bundle-Version: 7.11.0\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ClassPath: bin/,\n"); //$NON-NLS-1$
 		manifest.append(" sapidoc3.jar\n"); //$NON-NLS-1$
 		manifest.append("Bundle-Vendor: SAP AG, Walldorf (packaged using RCER)\n"); //$NON-NLS-1$
 		manifest.append("Bundle-RequiredExecutionEnvironment: J2SE-1.5\n"); //$NON-NLS-1$
 		manifest.append("Export-Package: com.sap.conn.idoc,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.idoc.jco,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.idoc.rt.cp,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.idoc.rt.record,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.idoc.rt.record.impl,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.idoc.rt.trace,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.idoc.rt.util,\n"); //$NON-NLS-1$
 		manifest.append(" com.sap.conn.idoc.rt.xml\n"); //$NON-NLS-1$
 		manifest.append("Bundle-ActivationPolicy: lazy\n"); //$NON-NLS-1$
 		manifest.append(MessageFormat.format("Require-Bundle: {0}\n", pluginNameJCo)); //$NON-NLS-1$
 		writeTextFile(monitor, manifest, metaInfFolder.getFile("MANIFEST.MF")); //$NON-NLS-1$
 
 		// set classpath                                                                                      5
 		final IPath jcoPath = new Path(MessageFormat.format("/{0}/sapidoc3.jar", pluginName)); //$NON-NLS-1$
 		IClasspathEntry jcoEntry = JavaCore.newLibraryEntry(jcoPath, Path.EMPTY, Path.EMPTY, true);
 		javaProject.setRawClasspath(new IClasspathEntry[] { jcoEntry }, new SubProgressMonitor(monitor, 5));
 
 		// create build.properties                                                                            5
 		StringBuilder buildProperties = new StringBuilder();
 		buildProperties.append("bin.includes = META-INF/,\\\n"); //$NON-NLS-1$
 		buildProperties.append("               sapidoc3.jar,\\\n"); //$NON-NLS-1$
 		buildProperties.append("               .\n"); //$NON-NLS-1$
 		writeTextFile(monitor, buildProperties, project.getFile("build.properties")); //$NON-NLS-1$
 
 		// collect the plug-in for export
 		exportableBundles.add(modelManager.findModel(project));
 	}
 
 	/**
 	 * Exports the generated plug-ins and fragments to the selected location.
 	 * @param monitor
 	 */
 	private void exportPlugins(IProgressMonitor monitor) {
 		FeatureExportInfo info = new FeatureExportInfo();
 		info.toDirectory = true;
 		info.useJarFormat = true;
 		info.exportSource = false;
 		info.destinationDirectory = settings.getExportPath();
 		info.items = exportableBundles.toArray();
 		PluginExportOperation job = new PluginExportOperation(info, ""); //$NON-NLS-1$
 		job.setUser(true);
 		job.schedule();
 		job.setProperty(IProgressConstants.ICON_PROPERTY, PDEPluginImages.DESC_PLUGIN_OBJ);
 	}
 
 	/**
 	 * Auxiliary method to dump a {@link StringBuilder} to a file. 
 	 * @param monitor
 	 * @param source
 	 * @param file
 	 * @throws CoreException
 	 */
 	private void writeTextFile(IProgressMonitor monitor, StringBuilder source, IFile file) throws CoreException {
 		file.create(new ByteArrayInputStream(source.toString().getBytes()), true, new SubProgressMonitor(monitor, 5));	
 	}
 
 	/**
 	 * @return the estimated number of steps
 	 */
 	private int getNumberOfSteps() {
 		final int PLUGIN_STEPS = 70;
 		final int FRAGMENT_STEPS = 60;
 		int steps = 0;
 		if (settings.isPluginProjectSelected())         steps += PLUGIN_STEPS;
 		if (settings.isDocPluginProjectSelected())      steps += PLUGIN_STEPS;
 		if (settings.isWin32FragmentSelected())         steps += FRAGMENT_STEPS;
 		if (settings.isWin64IAFragmentSelected())       steps += FRAGMENT_STEPS;
 		if (settings.isWin64x86FragmentSelected())      steps += FRAGMENT_STEPS;
 		if (settings.isLinux32FragmentSelected())       steps += FRAGMENT_STEPS;
 		if (settings.isLinux64IAFragmentSelected())     steps += FRAGMENT_STEPS;
 		if (settings.isLinux64x86FragmentSelected())    steps += FRAGMENT_STEPS;
 		if (settings.isDarwin32FragmentSelected())      steps += FRAGMENT_STEPS;
 		if (settings.isDarwin64FragmentSelected())      steps += FRAGMENT_STEPS;
 		return steps;
 	}
 
 	/**
 	 * Reads an archive file into memory, guessing its type according to its extension.
 	 * @param filename
 	 * @return
 	 * @throws IOException
 	 */
 	private Map<String, byte[]> readArchiveFile(String filename) throws IOException {
 		if (filename.toLowerCase().endsWith(".zip")) { //$NON-NLS-1$
 			return readZIPFile(filename);
 		} else if (filename.toLowerCase().endsWith(".tgz")) { //$NON-NLS-1$
 			return readTGZFile(filename);
 		} else if (filename.toLowerCase().endsWith(".tar.gz")) { //$NON-NLS-1$
 			return readTGZFile(filename);
 		} else {
 			throw new UnsupportedOperationException(MessageFormat.format(Messages.ProjectGenerator_UnknownFileTypeMessage, filename));
 		}
 	}
 
 	/**
 	 * Reads a .tgz or .tar.gz file into memory.
 	 * @param filename
 	 * @return
 	 * @throws IOException 
 	 */
 	private Map<String, byte[]> readTGZFile(String filename) throws IOException {
 		HashMap<String, byte[]> result = new HashMap<String, byte[]>();
 		TarInputStream tin = new TarInputStream(new GZIPInputStream(new FileInputStream(new File(filename))));
 		TarEntry tarEntry = tin.getNextEntry();     
 		while (tarEntry != null) {  
 			if (!tarEntry.isDirectory()) {
 				// tar.getName()
 				ByteArrayOutputStream os = new ByteArrayOutputStream();
 				tin.copyEntryContents(os);   
 				os.close();
 				result.put(tarEntry.getName(), os.toByteArray());
 			}
 			tarEntry = tin.getNextEntry(); 
 		}    
 		tin.close();
 		return result;
 	}
 
 	/**
 	 * Reads a .zip file into memory.
 	 * @param filename
 	 * @return
 	 * @throws IOException
 	 */
 	private Map<String, byte[]> readZIPFile(String filename) throws IOException {
 		HashMap<String, byte[]> result = new HashMap<String, byte[]>();
 		byte[] buf = new byte[32 * 1024];
 		ZipFile file = new ZipFile(new File(filename));
 		Enumeration<? extends ZipEntry> entries = file.entries();
 		while (entries.hasMoreElements()) {
 			ZipEntry entry = entries.nextElement();
 			InputStream is = file.getInputStream(entry);
 			ByteArrayOutputStream os = new ByteArrayOutputStream();
 			while (true) {
 				int numRead = is.read(buf, 0, buf.length);
 				if (numRead == -1) {
 					break;
 				}
 				os.write(buf, 0, numRead);
 			}
 			is.close();
 			os.close();
 			result.put(entry.getName(), os.toByteArray());
 		}
 		file.close();
 		return result;
 	}
 
 }
