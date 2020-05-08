 /*
  * Copyright (c) 2005 Borland Software Corporation
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Artem Tikhomirov (Borland) - initial API and implementation
  */
 package org.eclipse.gmf.tests.setup;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import junit.framework.AssertionFailedError;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.gmf.tests.Plugin;
 import org.eclipse.jdt.core.IJavaModelMarker;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.pde.internal.core.PDECore;
 import org.eclipse.pde.internal.core.PDEState;
 import org.eclipse.pde.internal.ui.wizards.imports.PluginImportOperation;
 import org.osgi.framework.Bundle;
 
 /**
  * With PDE, we need source code in the running workspace to allow compilation of our code 
  * (because PDE doesn't reexport set of plugins from it's running configuration, and it's no longer possible 
  * to set Target Platform to "same as running" as it was back in Eclipse 2.x).
  * 
  * Classloading works because there's -dev argument in the command line. With PDE launch, it's done by PDE.
  * Without PDE, running tests as part of the build relies on Eclipse Testing Framework's org.eclipse.test_3.1.0/library.xml
  * which specifies "-dev bin". Once it's not specified, or new format (properties file with plugin-id=binfolder) 
  * is in use, classloading of the generated code will fail and another mechanism should be invented then.
  * 
  * If you get ClassNotFoundException while running tests in PDE environment, try to set read-only attribute for the next file:
  * 'development-workspace'\.metadata\.plugins\org.eclipse.pde.core\'JUnitLaunchConfigName'\dev.properties
  * @author artem
  */
 public class RuntimeWorkspaceSetup {
 
 	public RuntimeWorkspaceSetup() {
 	}
 
 	/**
 	 * @return <code>this</code> for convenience
 	 */
 	// TODO Refactor to clear away similar code (CodeCompilationTest, RuntimeWorkspaceSetup, GenProjectSetup)
 	public RuntimeWorkspaceSetup init() throws Exception {
 		ensureJava14();
 		if (PDECore.isDevLaunchMode()) {
 			// Need to get some gmf source code into target workspace 
 			importDevPluginsIntoRunTimeWorkspace(new String[] {
 					"org.apache.batik",
 					"org.eclipse.gmf.runtime.notation",
 					"org.eclipse.gmf.runtime.notation.edit",
 					"org.eclipse.wst.common.ui.properties",
 					"org.eclipse.gmf.runtime.common.core",
 					"org.eclipse.gmf.runtime.common.ui",
 					"org.eclipse.gmf.runtime.draw2d.ui",
 					"org.eclipse.gmf.runtime.draw2d.ui.render",
 					"org.eclipse.gmf.runtime.gef.ui",
 					"org.eclipse.gmf.runtime.common.ui.services",
 					"org.eclipse.gmf.runtime.emf.type.core",
 					"org.eclipse.gmf.runtime.emf.clipboard.core",
 					"org.eclipse.emf.validation",
 					"org.eclipse.gmf.runtime.emf.core",
 					"org.eclipse.gmf.runtime.common.ui.services",
 					"org.eclipse.gmf.runtime.common.ui.services.action",
 					"org.eclipse.gmf.runtime.common.ui.action",
 					"org.eclipse.gmf.runtime.common.ui.action.ide",
 					"org.eclipse.gmf.runtime.emf.ui",
 					"org.eclipse.gmf.runtime.emf.commands.core",
 					"org.eclipse.gmf.runtime.diagram.core",
 					"org.eclipse.gmf.runtime.diagram.ui",
 					"org.eclipse.gmf.runtime.common.ui.services.properties",
 					"org.eclipse.gmf.runtime.emf.ui.properties",
 					"org.eclipse.gmf.runtime.diagram.ui.actions",
 					"org.eclipse.gmf.runtime.diagram.ui.properties",
 					"org.eclipse.gmf.runtime.diagram.ui.providers",
 					"org.eclipse.gmf.runtime.diagram.ui.providers.ide",
 					"org.eclipse.gmf.runtime.diagram.ui.render",
 					"org.eclipse.gmf.runtime.diagram.ui.resources.editor",
 					"org.eclipse.gmf.runtime.diagram.ui.resources.editor.ide",
 					"org.eclipse.gmf.runtime.notation.providers",
 			});
 		}
 		return this;
 	}
 
 	private void importDevPluginsIntoRunTimeWorkspace(String[] pluginIDs) throws CoreException {
 		PDEState pdeState = new PDEState(getDevPluginsLocations(pluginIDs), false, new NullProgressMonitor());
		PluginImportOperation.IImportQuery query = new PluginImportOperation.IImportQuery() {
			public int doQuery(String arg0) {
 				return YES;
 			}
 		};
		PluginImportOperation op = new PluginImportOperation(pdeState.getModels(), PluginImportOperation.IMPORT_WITH_SOURCE, query, query, true);
 		ResourcesPlugin.getWorkspace().run(op, new NullProgressMonitor());
 		for (int i = 0; i < pluginIDs.length; i++) {
 			IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(pluginIDs[i]);
 			if (i == 13){
 				IFolder fold = p.getFolder("bin");
 			}
 			p.build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
 			IMarker[] compileErrors = getJavaErrors(p);
 			if (compileErrors.length > 0) {
 				String errorsMsg = formatErrors(new StringBuilder("UNEXPECTED: Compilation errors in imported plugin " + pluginIDs[i] + ":\n"), compileErrors);
 				Plugin.logError(errorsMsg);
 				throw new AssertionFailedError(errorsMsg);
 			}
 		}
 	}
 
 	private URL[] getDevPluginsLocations(String[] pluginIDs) {
 		ArrayList/*<URL>*/ urls = new ArrayList/*<URL>*/(pluginIDs.length); 
 		for (int i = 0; i < pluginIDs.length; i++) {
 			try {
 				Bundle b = Platform.getBundle(pluginIDs[i]);
 				urls.add(Platform.resolve(b.getEntry("/")));
 			} catch (Exception ex) {
 				Plugin.logError("Error looking for " + pluginIDs[i] + " plug-in:", ex);
 				ex.printStackTrace();
 			}
 		}
 		return (URL[]) urls.toArray(new URL[urls.size()]);
 	}
 
 	private IMarker[] collectJavaMarkers(IProject p) throws CoreException {
 		return p.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
 	}
 
 	private IMarker[] getJavaErrors(IProject p) throws CoreException {
 		return filterSevereMarkers(collectJavaMarkers(p));
 	}
 
 	private IMarker[] filterSevereMarkers(IMarker[] problems) throws CoreException {
 		ArrayList rv = new ArrayList(problems.length);
 		for (int i = 0; i < problems.length; i++) {
 			if (IMarker.SEVERITY_ERROR == ((Integer) problems[i].getAttribute(IMarker.SEVERITY)).intValue()) {
 				rv.add(problems[i]);
 			}
 		}
 		return (IMarker[]) rv.toArray(new IMarker[rv.size()]);
 	}
 
 	private String formatErrors(StringBuilder sb, IMarker[] compileErrors) {
 		for (int i = 0; i < compileErrors.length; i++) {
 			try {
 				sb.append(compileErrors[i].getAttribute(IMarker.MESSAGE));
 			} catch (CoreException ex) {
 				sb.append("--ex:");
 				sb.append(ex.getMessage());
 			}
 			sb.append(",\n");
 		}
 		return sb.toString();
 	}
 
 	/**
 	 * at least
 	 */
 	public void ensureJava14() {
 		if (!JavaCore.VERSION_1_4.equals(JavaCore.getOption(JavaCore.COMPILER_SOURCE))) {
 			Hashtable options = JavaCore.getOptions();
 			options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
 			options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
 			options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);
 			JavaCore.setOptions(options);
 		}
 	}
 }
