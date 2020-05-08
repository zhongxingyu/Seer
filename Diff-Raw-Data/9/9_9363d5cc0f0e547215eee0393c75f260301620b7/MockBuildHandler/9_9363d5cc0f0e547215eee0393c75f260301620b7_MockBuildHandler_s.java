 /*******************************************************************************
  * Copyright (c) 2010 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.rpm;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.linuxtools.rpm.core.utils.Utils;
 import org.eclipse.osgi.util.NLS;
 
 public class MockBuildHandler extends RPMHandler {
 	@Override
 	public IStatus doExecute(ExecutionEvent event, IProgressMonitor monitor) throws ExecutionException {
 		// build fresh SRPM
 		IStatus result = makeSRPM(event, monitor);
 		if (result.isOK()) {
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			result = createMockJob(monitor);
 		}
 
 		return result;
 	}
 
 	protected IStatus createMockJob(IProgressMonitor monitor) {
 		// get buildarch
 		try {
 			String buildarch = rpmEval("_arch"); //$NON-NLS-1$
 			final String mockcfg = getMockcfg(buildarch);
 
 			monitor.subTask(NLS.bind(Messages.getString("MockBuildHandler.1"), specfile.getName())); //$NON-NLS-1$
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			}
 			return mockBuild(mockcfg, monitor);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return handleError(e);
 		}
 	}
 
 	protected IStatus mockBuild(String mockcfg, IProgressMonitor monitor) {
 		IStatus status;
 		IResource parent = specfile.getParent();
 		String dir = parent.getLocation().toString();
 
 		try {
			String[] cmd = { "mock", "-r", mockcfg + "--resultdir=" + dir //$NON-NLS-1$ //$NON-NLS-2$
 					+ Path.SEPARATOR + makeTagName(), "rebuild", dir //$NON-NLS-1$
 					+ Path.SEPARATOR + rpmQuery(specfile, "NAME") + "-" //$NON-NLS-1$ //$NON-NLS-2$
 					+ rpmQuery(specfile, "VERSION") + "-" //$NON-NLS-1$ //$NON-NLS-2$
 					+ rpmQuery(specfile, "RELEASE") + ".src.rpm" }; //$NON-NLS-1$ //$NON-NLS-2$
 			InputStream is = Utils.runCommandToInputStream(cmd);
 			status = runShellCommand(is, monitor); //$NON-NLS-1$
 
 			// refresh containing folder
 			parent.refreshLocal(IResource.DEPTH_INFINITE,
 					new NullProgressMonitor());
 		} catch (CoreException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			status = handleError(e);
 		}
 		return status;
 	}
 
 	private String getMockcfg(String buildarch) throws CoreException {
 		HashMap<String, String> branch = branches.get(specfile.getParent()
 				.getName());
 		String distvar = branch.get("distvar"); //$NON-NLS-1$
 		String distval = branch.get("distval"); //$NON-NLS-1$
 		String mockcfg = null;
 		if (distvar.equals("rhel")) { //$NON-NLS-1$
 			mockcfg = "fedora-" + distval + "-" + buildarch + "-epel"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 		} else {
 			mockcfg = "fedora-" + distval + "-" + buildarch; //$NON-NLS-1$ //$NON-NLS-2$
 			if (distval.equals("4") || distval.equals("5") //$NON-NLS-1$ //$NON-NLS-2$
 					|| distval.equals("6")) { //$NON-NLS-1$
 				mockcfg += "-core"; //$NON-NLS-1$
 			}
 			
 			if (getBranchName(specfile.getParent().getName()).equals("devel")) { //$NON-NLS-1$
 				mockcfg = "fedora-devel-" + buildarch; //$NON-NLS-1$
 			}
 			
 			if (specfile.getParent().getName().equals("devel")) {
 				//If the specified mockcfg does not exist...
 				File file = new File("/etc/mock/" + mockcfg); //$NON-NLS-1$
 				if (!file.exists()){ 
 					mockcfg = "fedora-devel-" + buildarch; 
 				}
 			}
 		}
 		return mockcfg;
 	}
 
 	@Override
 	protected String getTaskName() {
 		return Messages.getString("MockBuildHandler.27"); //$NON-NLS-1$
 	}
 }
