 package org.jboss.tools.ws.creation.core.commands;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.LineNumberReader;
 
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.frameworks.datamodel.AbstractDataModelOperation;
 import org.jboss.tools.ws.core.utils.StatusUtils;
 import org.jboss.tools.ws.creation.core.JBossWSCreationCore;
 import org.jboss.tools.ws.creation.core.data.ServiceModel;
 import org.jboss.tools.ws.creation.core.messages.JBossWSCreationCoreMessages;
 import org.jboss.tools.ws.creation.core.utils.JBossWSCreationUtils;
 
 abstract class AbstractGenerateCodeCommand extends AbstractDataModelOperation {
 
 	protected ServiceModel model;
 	private String cmdFileName_linux;
 	private String cmdFileName_win;
 
 	public AbstractGenerateCodeCommand(ServiceModel model) {
 		this.model = model;
 		cmdFileName_linux = getCommandLineFileName_linux();
 		cmdFileName_win = getCommandLineFileName_win();
 	}
 
 	@Override
 	public IStatus execute(IProgressMonitor monitor, IAdaptable info)
 			throws ExecutionException {
 		IStatus status = Status.OK_STATUS;
 
 		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
 				model.getWebProjectName());
 
 		String projectRoot = JBossWSCreationUtils.getProjectRoot(
 				model.getWebProjectName()).toOSString();
 		IJavaProject javaProject = JavaCore.create(project);
 		try {
 			String runtimeLocation = JBossWSCreationUtils
 					.getJBossWSRuntimeLocation(project);
 			String commandLocation = runtimeLocation + Path.SEPARATOR + "bin";
 			IPath path = new Path(commandLocation);
 			StringBuffer command = new StringBuffer();
 			if (System.getProperty("os.name").toLowerCase().indexOf("win") >= 0) {
 				command.append("cmd.exe /c ").append(cmdFileName_win);
 				path = path.append(cmdFileName_win);
 			} else {
 				command.append("sh ").append(cmdFileName_linux);
 				path = path.append(cmdFileName_linux);
 			}
 
 			if (!path.toFile().getAbsoluteFile().exists()) {
 				return StatusUtils
 						.errorStatus(NLS
 								.bind(
 										JBossWSCreationCoreMessages.Error_Message_Command_File_Not_Found,
 										new String[] { path.toOSString() }));
 			}
 
 			String args = getCommandlineArgs();
 			command.append(" -k ").append(args).append(" ");
 			if(model.getWsdlURI() != null){
 				command.append(model.getWsdlURI());
 			}
 			command.append(" -o ").append(projectRoot).append(Path.SEPARATOR)
 					.append(
 							javaProject.getOutputLocation()
 									.removeFirstSegments(1).toOSString());
 			Runtime rt = Runtime.getRuntime();
 			Process proc = rt.exec(command.toString(), null, new File(
 					commandLocation));
 			StringBuffer errorResult = new StringBuffer();
 			StringBuffer inputResult = new StringBuffer();
 
 			convertInputStreamToString(errorResult, proc.getErrorStream());
 			convertInputStreamToString(inputResult, proc.getInputStream());
 
 			int exitValue = proc.waitFor();
 
 			if (exitValue != 0) {
 				return StatusUtils.errorStatus(errorResult.toString());
 			} else {
 				String resultInput = inputResult.toString();
 				if (resultInput != null && resultInput.indexOf("[ERROR]") >= 0) {
 					JBossWSCreationCore.getDefault().logError(resultInput);
					IStatus errorStatus = StatusUtils.errorStatus(resultInput);
 					status = StatusUtils
							.errorStatus(
 									JBossWSCreationCoreMessages.Error_Message_Failed_To_Generate_Code,
 									new CoreException(errorStatus));
 				} else {
 					JBossWSCreationCore.getDefault().logInfo(resultInput);
 				}
 			}
 
 		} catch (IOException e) {
 			JBossWSCreationCore.getDefault().logError(e);
 
 		} catch (InterruptedException e) {
 			// ignore
 		} catch (CoreException e) {
 			JBossWSCreationCore.getDefault().logError(e);
 			// unable to get runtime location
 			return e.getStatus();
 		}
 
 		refreshProject(model.getWebProjectName(), monitor);
 
 		return status;
 	}
 
 	private void convertInputStreamToString(final StringBuffer result,
 			final InputStream input) {
 
 		Thread thread = new Thread() {
 			public void run() {
 
 				try {
 					InputStreamReader ir = new InputStreamReader(input);
 					LineNumberReader reader = new LineNumberReader(ir);
 					String str;
 					str = reader.readLine();
 					while (str != null) {
 						result.append(str).append("\t\r");
 						str = reader.readLine();
 
 					}
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 
 			}
 		};
 
 		thread.start();
 
 	}
 
 	private void refreshProject(String project, IProgressMonitor monitor) {
 		try {
 			JBossWSCreationUtils.getProjectByName(project).refreshLocal(2,
 					monitor);
 		} catch (CoreException e) {
 			e.printStackTrace();
 			JBossWSCreationCore.getDefault().logError(e);
 		}
 	}
 
 	abstract protected String getCommandlineArgs();
 
 	abstract protected String getCommandLineFileName_linux();
 
 	abstract protected String getCommandLineFileName_win();
 
 }
