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
 
 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.QualifiedName;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.linuxtools.rpm.core.utils.Utils;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.fedoraproject.eclipse.packager.CommonHandler;
 import org.fedoraproject.eclipse.packager.ConsoleWriterThread;
 
 public abstract class RPMHandler extends CommonHandler {
 	protected static final QualifiedName KEY = new QualifiedName(
 			RPMPlugin.PLUGIN_ID, "source"); //$NON-NLS-1$
 
 	public static final String CONSOLE_NAME = Messages.getString("RPMHandler.1"); //$NON-NLS-1$
 
 	protected HashMap<String, String> sources;
 
 	protected static final String repo = "http://cvs.fedoraproject.org/repo/pkgs"; //$NON-NLS-1$
 
 	protected IStatus retrieveSources(ExecutionEvent event, IProgressMonitor monitor) {
 		try {
 			sources = getSources();
 		} catch (IOException e) {
 			e.printStackTrace();
 			return handleError(e);
 		}
 
 		Set<String> sourcesToGet = sources.keySet();
 
 		// check md5sum of any local sources
 		checkSources(sourcesToGet);
 
 		if (sourcesToGet.isEmpty()) {
 			return handleOK(Messages.getString("RPMHandler.3"), false); //$NON-NLS-1$
 		}
 
 		// Need to download remaining sources from repo
 		IStatus status = null;
 		for (final String source : sourcesToGet) {
 			monitor.subTask(NLS.bind(Messages.getString("RPMHandler.4"), source)); //$NON-NLS-1$
 			final String url = repo + "/" + specfile.getProject().getName() //$NON-NLS-1$
 			+ "/" + source + "/" + sources.get(source) + "/" + source; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			status = download(url, source, monitor);
 			if (!status.isOK()) {
 				// download failed
 				deleteSource(source);
 				break;
 			}
 		}
 
 		if (!status.isOK()) {
 			return handleError(status.getMessage());
 		}
 
 		// sources downloaded successfully, check MD5
 		checkSources(sourcesToGet);
 
 		// if all checks pass we should have an empty list
 		if (!sources.isEmpty()) {
 			String failedSources = ""; //$NON-NLS-1$
 			for (String source : sources.keySet()) {
 				failedSources += source + '\n';
 			}
 			return handleError(Messages.getString("RPMHandler.10") //$NON-NLS-1$
 					+ failedSources);
 		} else {
 			return Status.OK_STATUS;
 		}
 	}
 
 	private void deleteSource(String file) {
 		IContainer branch = specfile.getParent();
 		IResource toDelete = branch.findMember(file);
 		if (toDelete != null) {
 			try {
 				toDelete.delete(true, null);
 			} catch (CoreException e) {
 				e.printStackTrace();
 				handleError(e);
 			}
 		}
 	}
 
 	protected MessageConsole getConsole(String name) {
 		MessageConsole ret = null;
 		for (IConsole cons : ConsolePlugin.getDefault().getConsoleManager()
 				.getConsoles()) {
 			if (cons.getName().equals(name)) {
 				ret = (MessageConsole) cons;
 			}
 		}
 		// no existing console, create new one
 		if (ret == null) {
 			ret = new MessageConsole(name, RPMPlugin
 					.getImageDescriptor("icons/rpm.gif")); //$NON-NLS-1$
 		}
 		return ret;
 	}
 
 	protected void checkSources(Set<String> sourcesToGet) {
 		ArrayList<String> toRemove = new ArrayList<String>();
 		for (String source : sourcesToGet) {
 			IResource r = specfile.getParent().findMember(source);
 			// matched source name
 			if (r != null && checkMD5(sources.get(source), r)) {
 				// match
 				toRemove.add(source);
 			}
 		}
 
 		for (String source : toRemove) {
 			sourcesToGet.remove(source);
 		}
 	}
 
 	protected boolean checkMD5(String other, IResource r) {
 		// open file
 		File file = r.getLocation().toFile();
 		String md5 = getMD5(file);
 
 		// perform check
 		return md5 == null ? false : md5.equalsIgnoreCase(other);
 	}
 
 	protected String getMD5(File file) {
 		String result = null;
 		FileInputStream fis = null;
 		try {
 			fis = new FileInputStream(file);
 			byte buf[] = new byte[(int) file.length()];
 			fis.read(buf); // read entire file into buf array
 			result = DigestUtils.md5Hex(buf);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			handleError(e);
 		} catch (IOException e) {
 			e.printStackTrace();
 			handleError(e);
 		} finally {
 			if (fis != null) {
 				try {
 					fis.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 					handleError(e);
 				}
 			}
 		}
 
 		return result;
 	}
 
 	protected IStatus download(String location, String fileName,
 			IProgressMonitor monitor) {
 		InputStream in = null;
 		OutputStream out = null;
 		File file = null;
 		try {
 			URL url = new URL(location);
 			file = new File(specfile.getParent().getLocation().toOSString()
 					+ Path.SEPARATOR + fileName);
 
 			if (!file.createNewFile()) {
 				// try overwriting the file if it exists
 				file.delete();
 				if (!file.createNewFile()) {
 					return handleError(NLS.bind(Messages.getString("RPMHandler.12"), fileName)); //$NON-NLS-1$
 				}
 			}
 			out = new BufferedOutputStream(new FileOutputStream(file));
 
 			// connect to repo
 			URLConnection conn = url.openConnection();
 
 			in = conn.getInputStream();
 			// 1K buffer
 			byte[] buf = new byte[1024];
 
 			// download file
 			int bytesRead = -1;
 			while ((bytesRead = in.read(buf)) != -1) {
 				if (monitor.isCanceled()) {
 					throw new OperationCanceledException();
 				}
 				out.write(buf, 0, bytesRead);
 			}
 
 			// refresh folder in resource tree
 			specfile.getParent().refreshLocal(IResource.DEPTH_ONE, monitor);
 
 			return Status.OK_STATUS;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return handleError(NLS.bind(Messages.getString("RPMHandler.12"), fileName)); //$NON-NLS-1$
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return handleError(Messages.getString("RPMHandler.14")); //$NON-NLS-1$
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 				if (out != null) {
 					out.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	protected HashMap<String, String> getSources() throws IOException {
 		HashMap<String, String> ret = new LinkedHashMap<String, String>();
 		IResource sourcesResource = specfile.getParent().findMember("sources"); //$NON-NLS-1$
 		if (sourcesResource instanceof IFile) {
 			File sourcesFile = sourcesResource.getLocation().toFile();
 			BufferedReader br = null;
 			try {
 				br = new BufferedReader(new FileReader(sourcesFile));
 				String line = br.readLine();
 				while (line != null) {
 					String[] source = line.split("\\s+"); //$NON-NLS-1$
 					if (source.length != 2) {
 						return null;
 					}
 					ret.put(source[1], source[0]);
 					line = br.readLine();
 				}
 			} finally {
 				if (br != null) {
 					br.close();
 				}
 			}
 		}
 		return ret;
 	}
 
 	protected IStatus rpmBuild(List<String> flags, File log, IProgressMonitor monitor) {		
 		monitor.subTask(NLS.bind(Messages.getString("RPMHandler.17"), specfile.getName())); //$NON-NLS-1$
 		IResource parent = specfile.getParent();
 		String dir = parent.getLocation().toString();
 		List<String> defines = getRPMDefines(dir);
 
 		HashMap<String, String> branch = branches.get(parent.getName());
 		List<String> distDefines = getDistDefines(branch);
 
 		defines.add(0, "rpmbuild");
 		defines.addAll(distDefines);
 		defines.addAll(flags);
 		defines.add(specfile.getLocation().toString());
 
 		InputStream is;
 		IStatus status = null;
 		try {
 			is = Utils.runCommandToInputStream(defines.toArray(new String[0]));
 			status = runShellCommand(is, monitor); //$NON-NLS-1$
 		} catch (IOException e) {
 			e.printStackTrace();
 			handleError(e);
 		}
 		if (monitor.isCanceled()) {
 			throw new OperationCanceledException();
 		}
 
 		// refresh containing folder
 		try {
 			parent.refreshLocal(IResource.DEPTH_INFINITE,
 					new NullProgressMonitor());
 		} catch (CoreException e) {
 			e.printStackTrace();
 			handleError(e);
 		}
 		return status;
 	}
 
 	protected IStatus runShellCommand(InputStream is, IProgressMonitor mon) {
 		boolean terminateMonitor = false;
 		if (mon == null) {
 			terminateMonitor=true;
 			mon = new NullProgressMonitor();
 			mon.beginTask(Messages.getString("RPMHandlerMockBuild"), 1); //$NON-NLS-1$
 		}
 		IStatus status;
 		final MessageConsole console = getConsole(CONSOLE_NAME);
 		IConsoleManager manager = ConsolePlugin.getDefault()
 		.getConsoleManager();
 		manager.addConsoles(new IConsole[] { console });
 		console.activate();
 
 		final MessageConsoleStream outStream = console.newMessageStream();
 		final MessageConsoleStream errStream = console.newMessageStream();
 
 		Display.getDefault().syncExec(new Runnable() {
 
 			@Override
 			public void run() {
 				errStream.setColor(new Color(Display.getDefault(), 255, 0, 0));
 			}
 
 		});
 
 		try {
 			// create thread for reading inputStream (process' stdout)
 			ConsoleWriterThread outThread = new ConsoleWriterThread(is, outStream);
 			// start both threads
 			outThread.start();
 			
 			while (!mon.isCanceled()) {
 				try {
 					//Don't waste system resources
 					Thread.sleep(300);
 					break;
 				} catch (IllegalThreadStateException e) {
 					//Do nothing
 				}
 			}
 			
 			if (mon.isCanceled()) {
 				outThread.close();
 				Display.getDefault().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						MessageDialog.openError(new Shell(), Messages.getString("RPMHandlerScriptCancelled"), //$NON-NLS-1$
 								Messages.getString("RPMHandlerUserWarning")); //$NON-NLS-1$
 					}
 					
 				});
 				handleError(Messages.getString("RPMHandlerTerminationErrorHandling")); //$NON-NLS-1$
 				return Status.CANCEL_STATUS;
 			}
 			
 			if (terminateMonitor)
 				mon.done();
 			
 			// finish reading whatever's left in the buffers
 			outThread.join();
 			
 			status = Status.OK_STATUS;
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			status = Status.OK_STATUS;
 		}
 		return status;
 	}
 
 	protected String rpmEval(String format) throws CoreException {
		String cmd = "rpm --eval %{" + format + "}"; //$NON-NLS-1$ //$NON-NLS-2$
 
 		String result;
 		try {
 			result = Utils.runCommandToString(cmd);
 		} catch (IOException e) {
 			throw new CoreException(new Status(IStatus.ERROR, RPMPlugin.PLUGIN_ID, e.getMessage(), e));
 		}
 
 		return result.substring(0, result.indexOf('\n'));
 	}
 
 	protected IStatus makeSRPM(ExecutionEvent event, IProgressMonitor monitor) {
 				IStatus result = retrieveSources(event, monitor);
 				if (result.isOK()) {
 					if (monitor.isCanceled()) {
 						throw new OperationCanceledException();
 					}
 					ArrayList<String> flags = new ArrayList<String>();
 					flags.add("--nodeps");
 					flags.add("-bs");
 					result = rpmBuild(flags, null, monitor); //$NON-NLS-1$
 				}
 				return result;
 			}
 }
