 package com.vectorsf.jvoice.diagram.core.updateModules;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.m2e.actions.ExecutePomAction;
 import org.eclipse.m2e.core.ui.internal.UpdateMavenProjectJob;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 import com.vectorsf.jvoice.core.project.JVoiceApplicationNature;
 import com.vectorsf.jvoice.diagram.core.Activator;
 import com.vectorsf.jvoice.model.base.impl.JVModuleImpl;
 
 /**
  * Acción para reinstalar los módulos seleccionados. Para Tomcat, instala los módulos seleccionados, la aplicación y
  * arranca Tomcat.
  */
 public class IVRMavenLauncher extends AbstractHandler {
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService()
 				.getSelection();
 		Object project = ((IStructuredSelection) selection).getFirstElement();
 
 		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 		if (!(project instanceof JVModuleImpl) || ((IStructuredSelection) selection).size() > 1) {
 			MessageDialog.openWarning(shell, "Despliegue de módulos", "Debe seleccionar un módulo");
 			return null;
 		}
 
 		stopTomcat();
 
 		installModules(selection, project, shell);
 
 		return null;
 	}
 
 	private void installModules(ISelection selection, Object project, Shell shell) {
 		try {
 			if (!(project instanceof JVModuleImpl) || ((IStructuredSelection) selection).size() > 1) {
 				MessageDialog.openWarning(shell, "Despliegue de módulos", "Debe seleccionar un módulo");
 				return;
 			}
 
 			// Instalamos el módulo seleccionado
 			ExecutePomAction action = new ExecutePomAction();
 			action.setInitializationData(null, null, "clean install");
 			action.launch(selection, "run");
 
 			boolean installModule = MessageDialog.openConfirm(null, "Instalar módulo",
 					"Pulse OK cuando acabe la instalación del módulo");
 			if (!installModule) {
 				return;
 			}
 
 			for (IProject prj : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
 				if (prj.hasNature(JVoiceApplicationNature.NATURE_ID)) {
 					ExecutePomAction action2 = new ExecutePomAction();
 					action2.setInitializationData(null, null, "clean install");
 					action2.launch(new StructuredSelection(prj), "run");
 				}
 			}
 
 			// Cuando acabe Maven de instalar los módulos actualizamos la aplicación y arrancamos Tomcat
 			boolean startTomcat = MessageDialog.openConfirm(null, "Arrancar Tomcat",
 					"Pulse OK cuando acabe la instalación de la aplicación para arrancar Tomcat");
 
 			if (!startTomcat) {
 				return;
 			}
 
 			// Actualizamos las aplicaciones
 			for (IProject prj : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
 				if (prj.hasNature(JVoiceApplicationNature.NATURE_ID)) {
 					new UpdateMavenProjectJob(new IProject[] { prj }, true, // offline
 							false, // forceUpdateDependencies
 							true, // UpdateConfiguration
 							true, // cleanProjects(),
 							true // refreshFromLocal()
 					).schedule();
 				}
 			}
 			startTomcat();
 
 		} catch (Exception e) {
 			log("IVRMavenLauncher.installModules(): " + e);
 		}
 	}
 
 	private void startTomcat() {
 		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
 		try {
 			for (ILaunchConfiguration conf : manager.getLaunchConfigurations()) {
 				if ("org.eclipse.jst.server.tomcat.core.launchConfigurationType".equals(conf.getType().getIdentifier())) {
					DebugUITools.launch(conf.getWorkingCopy(), ILaunchManager.DEBUG_MODE);
 				}
 			}
 		} catch (Exception e) {
 
 			log("IVRMavenLauncher.startTomcat(): " + e);
 		}
 	}
 
 	private void stopTomcat() {
 		for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
 			try {
 				if (launch.getLaunchConfiguration().getName().contains("Tomcat")) {
 					launch.terminate();
 				}
 			} catch (Exception e) {
 				log("IVRMavenLauncher.stopTomcat(): " + e);
 			}
 		}
 	}
 
 	private void log(String text) {
 		System.err.println(text);
 		Activator.getLogger().log(IStatus.WARNING, "Actualizador de versiones: " + text);
 	}
 }
