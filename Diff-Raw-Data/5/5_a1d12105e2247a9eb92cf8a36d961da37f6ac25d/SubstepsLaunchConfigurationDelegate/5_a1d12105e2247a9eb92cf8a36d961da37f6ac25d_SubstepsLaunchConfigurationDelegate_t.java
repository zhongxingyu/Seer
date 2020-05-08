 /*******************************************************************************
  * Copyright Technophobia Ltd 2012
  * 
  * This file is part of the Substeps Eclipse Plugin.
  * 
  * The Substeps Eclipse Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the Eclipse Public License v1.0.
  * 
  * The Substeps Eclipse Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * Eclipse Public License for more details.
  * 
  * You should have received a copy of the Eclipse Public License
  * along with the Substeps Eclipse Plugin.  If not, see <http://www.eclipse.org/legal/epl-v10.html>.
  ******************************************************************************/
 package com.technophobia.substeps.junit.launcher;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate;
 import org.eclipse.jdt.launching.ExecutionArguments;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.SocketUtil;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 
 import com.technophobia.eclipse.launcher.config.SubstepsLaunchConfigurationConstants;
 import com.technophobia.substeps.FeatureEditorPlugin;
 import com.technophobia.substeps.FeatureRunnerPlugin;
 import com.technophobia.substeps.junit.launcher.config.SubstepsLaunchConfigWorkingCopyDecorator;
 import com.technophobia.substeps.junit.ui.SubstepsFeatureMessages;
 import com.technophobia.substeps.runner.RemoteTestRunner;
 import com.technophobia.substeps.supplier.Callback1;
 import com.technophobia.substeps.supplier.Predicate;
 import com.technophobia.substeps.util.ModelOperation;
 import com.technophobia.substeps.util.TemporaryModelEnhancer;
 
 public class SubstepsLaunchConfigurationDelegate extends AbstractJavaLaunchConfigurationDelegate {
 
     private boolean keepAlive = false;
     private int port;
     private IMember[] testElements;
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.
      * eclipse.debug.core.ILaunchConfiguration, java.lang.String,
      * org.eclipse.debug.core.ILaunch,
      * org.eclipse.core.runtime.IProgressMonitor)
      */
     @Override
     public synchronized void launch(final ILaunchConfiguration configuration, final String m, final ILaunch launch,
             final IProgressMonitor mtr) throws CoreException {
         final IProgressMonitor monitor;
         if (mtr == null) {
             monitor = new NullProgressMonitor();
         } else {
             monitor = mtr;
         }
 
         final String mode;
         if (m.equals(SubstepsLaunchConfigurationConstants.MODE_RUN_QUIETLY_MODE)) {
             launch.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_NO_DISPLAY, "true"); //$NON-NLS-1$
             mode = ILaunchManager.RUN_MODE;
         } else {
             mode = m;
         }
 
         monitor.beginTask(MessageFormat.format("{0}...", configuration.getName()), 5); //$NON-NLS-1$
         // check for cancellation
         if (monitor.isCanceled()) {
             return;
         }
 
         try {
             monitor.subTask(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_verifying_attributes_description);
 
             try {
                 preLaunchCheck(configuration, launch, new SubProgressMonitor(monitor, 2));
             } catch (final CoreException e) {
                 if (e.getStatus().getSeverity() == IStatus.CANCEL) {
                     monitor.setCanceled(true);
                     return;
                 }
                 throw e;
             }
             // check for cancellation
             if (monitor.isCanceled()) {
                 return;
             }
 
             final TemporaryModelEnhancer<IJavaProject> modelEnhancer = new TemporaryModelEnhancer<IJavaProject>(
                     addSubstepsToClasspath(), removeSubstepsFromClasspath(), doRunTests(mode, configuration, launch,
                             monitor), isSubstepsNotOnClasspath(configuration));
             modelEnhancer.doOperationFor(getJavaProject(configuration));
 
         } finally {
             testElements = null;
             monitor.done();
         }
     }
 
 
     private Callback1<IJavaProject> addSubstepsToClasspath() {
         return new Callback1<IJavaProject>() {
 
             @Override
             public void doCallback(final IJavaProject project) {
 
                 try {
                     final List<IClasspathEntry> newClasspath = new ArrayList<IClasspathEntry>(Arrays.asList(project
                             .getRawClasspath()));
 
                     final List<String> jarFiles = new SubstepJarProvider().junitRunnerJars();
                     for (final String jarFile : jarFiles) {
                         newClasspath.add(JavaCore.newLibraryEntry(new Path(jarFile), null, null));
                     }
                     project.setRawClasspath(newClasspath.toArray(new IClasspathEntry[newClasspath.size()]), null);
                 } catch (final JavaModelException ex) {
                     FeatureRunnerPlugin.error("Could not add substeps jars to classpath", ex);
                 }
             }
         };
     }
 
 
     private Callback1<IJavaProject> removeSubstepsFromClasspath() {
         return new Callback1<IJavaProject>() {
 
             @Override
             public void doCallback(final IJavaProject project) {
                 try {
                     final List<IClasspathEntry> newClasspath = new ArrayList<IClasspathEntry>(Arrays.asList(project
                             .getRawClasspath()));
 
                     final List<String> jarFiles = new SubstepJarProvider().junitRunnerJars();
                     for (final String jarFile : jarFiles) {
                         newClasspath.remove(JavaCore.newLibraryEntry(new Path(jarFile), null, null));
                     }
                     project.setRawClasspath(newClasspath.toArray(new IClasspathEntry[newClasspath.size()]), null);
                 } catch (final JavaModelException ex) {
                     FeatureRunnerPlugin.error("Could not remove substeps jars from classpath", ex);
                 }
             }
         };
     }
 
 
     private ModelOperation<IJavaProject> doRunTests(final String mode, final ILaunchConfiguration configuration,
             final ILaunch launch, final IProgressMonitor monitor) {
         return new ModelOperation<IJavaProject>() {
 
             @Override
             public void doOperationOn(final IJavaProject t) throws CoreException {
                 keepAlive = mode.equals(ILaunchManager.DEBUG_MODE)
                         && configuration.getAttribute(SubstepsLaunchConfigurationConstants.ATTR_KEEPRUNNING, false);
                 port = evaluatePort();
                 launch.setAttribute(SubstepsLaunchConfigurationConstants.ATTR_PORT, String.valueOf(port));
                 testElements = evaluateTests(configuration, new SubProgressMonitor(monitor, 1));
 
                 final String mainTypeName = verifyMainTypeName(configuration);
                 final IVMRunner runner = getVMRunner(configuration, mode);
 
                 final File workingDir = verifyWorkingDirectory(configuration);
                 String workingDirName = null;
                 if (workingDir != null) {
                     workingDirName = workingDir.getAbsolutePath();
                 }
 
                 // Environment variables
                 final String[] envp = getEnvironment(configuration);
 
                 final ArrayList<String> vmArguments = new ArrayList<String>();
                 final ArrayList<String> programArguments = new ArrayList<String>();
                 collectExecutionArguments(configuration, vmArguments, programArguments);
 
                 // VM-specific attributes
                 final Map<String, Object> vmAttributesMap = getVMSpecificAttributesMap(configuration);
 
                 // Classpath
                 final String[] classpath = getClasspath(configuration);
 
                 // Create VM config
                 final VMRunnerConfiguration runConfig = new VMRunnerConfiguration(mainTypeName, classpath);
                 runConfig.setVMArguments(vmArguments.toArray(new String[vmArguments.size()]));
                 runConfig.setProgramArguments(programArguments.toArray(new String[programArguments.size()]));
                 runConfig.setEnvironment(envp);
                 runConfig.setWorkingDirectory(workingDirName);
                 runConfig.setVMSpecificAttributesMap(vmAttributesMap);
 
                 // Bootpath
                 runConfig.setBootClassPath(getBootpath(configuration));
 
                 // check for cancellation
                 if (monitor.isCanceled()) {
                     return;
                 }
 
                 // done the verification phase
                 monitor.worked(1);
 
                 monitor.subTask(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_create_source_locator_description);
                 // set the default source locator if required
                 setDefaultSourceLocator(launch, configuration);
                 monitor.worked(1);
 
                 // Launch the configuration - 1 unit of work
                 runner.run(runConfig, launch, monitor);
 
                 // check for cancellation
                 if (monitor.isCanceled()) {
                     return;
                 }
             }
         };
     }
 
 
     private Predicate<IJavaProject> isSubstepsNotOnClasspath(final ILaunchConfiguration configuration) {
         return new Predicate<IJavaProject>() {
 
             @Override
             public boolean forModel(final IJavaProject project) {
                 try {
                     final IJavaElement element = getMainElementFromProject(configuration, project);
                     return element == null;
                 } catch (final JavaModelException ex) {
                     FeatureRunnerPlugin.error("Could not determine if substeps runner jars were on the classpath", ex);
                 } catch (final CoreException ex) {
                     FeatureRunnerPlugin.error("Could not determine if substeps runner jars were on the classpath", ex);
                 }
                 return true;
             }
         };
     }
 
 
     private int evaluatePort() throws CoreException {
         final int p = SocketUtil.findFreePort();
         if (p == -1) {
             abort(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_error_no_socket, null,
                     IJavaLaunchConfigurationConstants.ERR_NO_SOCKET_AVAILABLE);
         }
         return p;
     }
 
 
     /**
      * Performs a check on the launch configuration's attributes. If an
      * attribute contains an invalid value, a {@link CoreException} with the
      * error is thrown.
      * 
      * @param configuration
      *            the launch configuration to verify
      * @param launch
      *            the launch to verify
      * @param monitor
      *            the progress monitor to use
      * @throws CoreException
      *             an exception is thrown when the verification fails
      */
     protected void preLaunchCheck(final ILaunchConfiguration configuration, final ILaunch launch,
             final IProgressMonitor monitor) throws CoreException {
         try {
             final IJavaProject javaProject = getJavaProject(configuration);
             if ((javaProject == null) || !javaProject.exists()) {
                 abort(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_error_invalidproject, null,
                         IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT);
             }
         } finally {
             monitor.done();
         }
     }
 
 
     // private String getTestRunnerKind(final ILaunchConfiguration
     // configuration) {
     // try {
     // return configuration
     // .getAttribute(SubstepsLaunchConfigurationConstants.ATTR_TEST_RUNNER_KIND,
     // (String) null);
     // } catch (final CoreException e) {
     // return null;
     // }
     // }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#
      * verifyMainTypeName(org.eclipse.debug.core.ILaunchConfiguration)
      */
     @Override
     public String verifyMainTypeName(final ILaunchConfiguration configuration) throws CoreException {
         return RemoteTestRunner.class.getName();
     }
 
 
     /**
      * Evaluates all test elements selected by the given launch configuration.
      * The elements are of type {@link IType} or {@link IMethod}. At the moment
      * it is only possible to run a single method or a set of types, but not
      * mixed or more than one method at a time.
      * 
      * @param configuration
      *            the launch configuration to inspect
      * @param monitor
      *            the progress monitor
      * @return returns all types or methods that should be ran
      * @throws CoreException
      *             an exception is thrown when the search for tests failed
      */
     protected IMember[] evaluateTests(final ILaunchConfiguration configuration, final IProgressMonitor monitor)
             throws CoreException {
         final IJavaProject javaProject = getJavaProject(configuration);
 
         final IJavaElement testTarget = getTestTarget(configuration, javaProject);
         final String testMethodName = configuration.getAttribute(
                 SubstepsLaunchConfigurationConstants.ATTR_TEST_METHOD_NAME, ""); //$NON-NLS-1$
         if (testMethodName.length() > 0) {
             if (testTarget instanceof IType) {
                 return new IMember[] { ((IType) testTarget).getMethod(testMethodName, new String[0]) };
             }
         }
         final HashSet<IMember> result = new HashSet<IMember>();
         result.add(testTarget.getJavaProject().findType(SubstepsLaunchConfigWorkingCopyDecorator.FEATURE_TEST));
         return result.toArray(new IMember[result.size()]);
     }
 
 
     /**
      * Collects all VM and program arguments. Implementors can modify and add
      * arguments.
      * 
      * @param configuration
      *            the configuration to collect the arguments for
      * @param vmArguments
      *            a {@link List} of {@link String} representing the resulting VM
      *            arguments
      * @param programArguments
      *            a {@link List} of {@link String} representing the resulting
      *            program arguments
      * @exception CoreException
      *                if unable to collect the execution arguments
      */
     protected void collectExecutionArguments(final ILaunchConfiguration configuration, final List<String> vmArguments,
             final List<String> programArguments) throws CoreException {
 
         // add program & VM arguments provided by getProgramArguments and
         // getVMArguments
         final String pgmArgs = getProgramArguments(configuration);
         final String vmArgs = getVMArguments(configuration);
         final ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
         vmArguments.addAll(Arrays.asList(execArgs.getVMArgumentsArray()));
         vmArguments.addAll(substepsVMArguments(configuration));
         programArguments.addAll(Arrays.asList(execArgs.getProgramArgumentsArray()));
 
         final String testFailureNames = configuration.getAttribute(
                 SubstepsLaunchConfigurationConstants.ATTR_FAILURES_NAMES, ""); //$NON-NLS-1$
 
         programArguments.add("version=3");
         programArguments.add("port=" + String.valueOf(port));
         if (keepAlive)
             programArguments.add("keepalive"); //$NON-NLS-1$
 
         // final String testRunnerKind = getTestRunnerKind(configuration);
 
         //                    programArguments.add("-testLoaderClass"); //$NON-NLS-1$
         // programArguments.add(testRunnerKind.getLoaderClassName());
         //                    programArguments.add("-loaderpluginname"); //$NON-NLS-1$
         // programArguments.add(testRunnerKind.getLoaderPluginId());
 
         final IMember[] elements = this.testElements;
 
         // a test name was specified just run the single test
         if (elements.length == 1) {
             if (elements[0] instanceof IMethod) {
                 final IMethod method = (IMethod) elements[0];
                 programArguments.add("test=" + method.getDeclaringType().getFullyQualifiedName() + ':'
                         + method.getElementName());
             } else if (elements[0] instanceof IType) {
                 final IType type = (IType) elements[0];
                 programArguments.add("classNames=" + type.getFullyQualifiedName());
             } else {
                 abort(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_error_wrong_input, null,
                         IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
             }
         } else if (elements.length > 1) {
             final String fileName = createTestNamesFile(elements);
             programArguments.add("testNameFile=" + fileName);
         }
         if (testFailureNames.length() > 0) {
             programArguments.add("testfailures=" + testFailureNames);
         }
     }
 
 
     private Collection<String> substepsVMArguments(final ILaunchConfiguration configuration) {
         final Collection<String> results = new ArrayList<String>();
         final IProject project = projectFromConfig(configuration);
 
         if (project != null) {
             results.add("-DsubstepsFeatureFile="
                    + project.getLocation().addTrailingSeparator()
                             .append(getConfigAttribute(configuration, SubstepsFeatureLaunchShortcut.ATTR_FEATURE_FILE))
                             .toOSString());
 
             results.add("-DsubstepsFile="
                     + project
                            .getLocation()
                             .addTrailingSeparator()
                             .append(getConfigAttribute(configuration,
                                     SubstepsLaunchConfigurationConstants.ATTR_SUBSTEPS_FILE)).toOSString());
 
             final Collection<String> stepImplementationClasses = FeatureEditorPlugin.instance()
                     .getStepImplementationProvider().stepImplementationClasses(project);
             results.add("-DsubstepsImplClasses=" + createStringFrom(stepImplementationClasses));
 
             results.add("-DsubstepsTags=--unimplemented");
 
             try {
                 results.add("-DoutputFolder="
                         + getJavaProject(configuration).getOutputLocation().removeFirstSegments(1).toOSString());
             } catch (final JavaModelException e) {
                 FeatureRunnerPlugin.log(e);
             } catch (final CoreException e) {
                 FeatureRunnerPlugin.log(e);
             }
 
             // results.add(getConfigAttribute(configuration,
             // IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS));
         }
         return results;
     }
 
 
     private IProject projectFromConfig(final ILaunchConfiguration configuration) {
         try {
             return getJavaProject(configuration).getProject();
         } catch (final CoreException e) {
             FeatureRunnerPlugin.log(e);
             return null;
         }
     }
 
 
     private String getConfigAttribute(final ILaunchConfiguration configuration, final String configName) {
         try {
             return configuration.getAttribute(configName, "");
         } catch (final CoreException e) {
             FeatureRunnerPlugin.log(e);
             return "";
         }
     }
 
 
     private String createTestNamesFile(final IMember[] elements) throws CoreException {
         try {
             final File file = File.createTempFile("testNames", ".txt"); //$NON-NLS-1$ //$NON-NLS-2$
             file.deleteOnExit();
             BufferedWriter bw = null;
             try {
                 bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8")); //$NON-NLS-1$
                 for (int i = 0; i < elements.length; i++) {
                     if (elements[i] instanceof IType) {
                         final IType type = (IType) elements[i];
                         final String testName = type.getFullyQualifiedName();
                         bw.write(testName);
                         bw.newLine();
                     } else {
                         abort(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_error_wrong_input, null,
                                 IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
                     }
                 }
             } finally {
                 if (bw != null) {
                     bw.close();
                 }
             }
             return file.getAbsolutePath();
         } catch (final IOException e) {
             throw new CoreException(new Status(IStatus.ERROR, FeatureRunnerPlugin.PLUGIN_ID, IStatus.ERROR, "", e)); //$NON-NLS-1$
         }
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.jdt.launching.AbstractJavaLaunchConfigurationDelegate#
      * getClasspath(org.eclipse.debug.core.ILaunchConfiguration)
      */
     @Override
     public String[] getClasspath(final ILaunchConfiguration configuration) throws CoreException {
         final String[] cp = super.getClasspath(configuration);
         final List<String> junitEntries = new SubstepJarProvider().allSubstepJars();
 
         final String[] classPath = new String[cp.length + junitEntries.size()];
         final String[] jea = junitEntries.toArray(new String[junitEntries.size()]);
         System.arraycopy(cp, 0, classPath, 0, cp.length);
         System.arraycopy(jea, 0, classPath, cp.length, jea.length);
         return classPath;
     }
 
 
     /*
      * private static class ClasspathLocalizer {
      * 
      * private final boolean fInDevelopmentMode;
      * 
      * public ClasspathLocalizer(final boolean inDevelopmentMode) {
      * fInDevelopmentMode = inDevelopmentMode; }
      * 
      * public List localizeClasspath(final ITestKind kind) { final
      * JUnitRuntimeClasspathEntry[] entries= kind.getClasspathEntries(); final
      * List junitEntries= new ArrayList();
      * 
      * for (int i= 0; i < entries.length; i++) { try { addEntry(junitEntries,
      * entries[i]); } catch (final IOException e) { Assert.isTrue(false,
      * entries[i].getPluginId() + " is available (required JAR)"); //$NON-NLS-1$
      * } } return junitEntries; }
      * 
      * private void addEntry(final List junitEntries, final
      * JUnitRuntimeClasspathEntry entry) throws IOException,
      * MalformedURLException { final String entryString= entryString(entry); if
      * (entryString != null) junitEntries.add(entryString); }
      * 
      * private String entryString(final JUnitRuntimeClasspathEntry entry) throws
      * IOException, MalformedURLException { if (inDevelopmentMode()) { try {
      * return localURL(entry.developmentModeEntry()); } catch (final IOException
      * e3) { // fall through and try default } } return localURL(entry); }
      * 
      * private boolean inDevelopmentMode() { return fInDevelopmentMode; }
      * 
      * private String localURL(final JUnitRuntimeClasspathEntry jar) throws
      * IOException, MalformedURLException { final Bundle bundle=
      * JUnitCorePlugin.getDefault().getBundle(jar.getPluginId()); URL url; if
      * (jar.getPluginRelativePath() == null) url= bundle.getEntry("/");
      * //$NON-NLS-1$ else url= bundle.getEntry(jar.getPluginRelativePath()); if
      * (url == null) throw new IOException(); return
      * FileLocator.toFileURL(url).getFile(); } }
      */
 
     private final IJavaElement getTestTarget(final ILaunchConfiguration configuration, final IJavaProject javaProject)
             throws CoreException {
         final String containerHandle = configuration.getAttribute(
                 SubstepsLaunchConfigurationConstants.ATTR_TEST_CONTAINER, ""); //$NON-NLS-1$
         if (containerHandle.length() != 0) {
             final IJavaElement element = JavaCore.create(containerHandle);
             if (element == null || !element.exists()) {
                 abort(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_error_input_element_deosn_not_exist,
                         null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
             }
             return element;
         }
         final IJavaElement element = getMainElementFromProject(configuration, javaProject);
         if (element != null) {
             return element;
         }
 
         abort(SubstepsFeatureMessages.SubstepsLaunchConfigurationDelegate_input_type_does_not_exist, null,
                 IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE);
         return null; // not reachable
     }
 
 
     private IJavaElement getMainElementFromProject(final ILaunchConfiguration configuration,
             final IJavaProject javaProject) throws CoreException, JavaModelException {
         final String testTypeName = getMainTypeName(configuration);
         if (testTypeName != null && testTypeName.length() != 0) {
             final IType type = javaProject.findType(testTypeName);
             if (type != null && type.exists()) {
                 return type;
             }
         }
         return null;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * org.eclipse.jdt.internal.junit.launcher.ITestFindingAbortHandler#abort
      * (java.lang.String, java.lang.Throwable, int)
      */
     @Override
     protected void abort(final String message, final Throwable exception, final int code) throws CoreException {
         throw new CoreException(new Status(IStatus.ERROR, FeatureRunnerPlugin.PLUGIN_ID, code, message, exception));
     }
 
 
     private String createStringFrom(final Collection<String> collection) {
         final StringBuilder sb = new StringBuilder();
         if (collection != null) {
             for (final String stepImpl : collection) {
                 sb.append(stepImpl);
                 sb.append(";");
             }
         }
         return sb.toString();
     }
 }
