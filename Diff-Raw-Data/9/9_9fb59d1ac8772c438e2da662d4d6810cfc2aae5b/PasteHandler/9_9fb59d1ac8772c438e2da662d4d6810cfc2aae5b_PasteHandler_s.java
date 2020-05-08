 package com.vectorsf.jvoice.ui.navigator.handler;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.expressions.IEvaluationContext;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.jface.util.LocalSelectionTransfer;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.dnd.Clipboard;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.xtext.resource.SaveOptions;
 
 import com.vectorsf.jvoice.base.model.service.BaseModel;
 import com.vectorsf.jvoice.model.base.JVApplication;
 import com.vectorsf.jvoice.model.base.JVBean;
 import com.vectorsf.jvoice.model.base.JVModule;
 import com.vectorsf.jvoice.model.base.JVPackage;
 import com.vectorsf.jvoice.model.base.JVProject;
 import com.vectorsf.jvoice.model.operations.Flow;
 import com.vectorsf.jvoice.model.operations.LocutionState;
 import com.vectorsf.jvoice.model.operations.State;
 import com.vectorsf.jvoice.prompt.model.voiceDsl.VoiceDsl;
 
 public class PasteHandler extends AbstractHandler {
 
 	private IPath targetPath;
 	private IPath resourcesPath;
 	private IResource targetRes;
 	private IFolder miPack;
 	private IFile miBean;
 	private IFolder recursos;
 	private boolean rename;
 
 	@Override
 	public void setEnabled(Object evaluationContext) {
 
 		Clipboard clip = new Clipboard(Display.getCurrent());
 		Object contents = clip
 				.getContents(LocalSelectionTransfer.getTransfer());
 		clip.dispose();
 		if (contents == null) {
 			setBaseEnabled(false);
 		} else {
 			Object target = getTarget(evaluationContext);
 			if (target instanceof JVPackage) {
 				boolean state = getListFromClipboard(contents, Flow.class) != null;
 				setBaseEnabled(state);
 			} else if (target instanceof JVProject) {
 				boolean state = getListFromClipboard(contents, JVPackage.class) != null;
 				setBaseEnabled(state);
 			} else if (target instanceof Flow) {
 				boolean state = getListFromClipboard(contents, VoiceDsl.class) != null;
 				setBaseEnabled(state);
 			} else {
 				setBaseEnabled(false);
 			}
 		}
 	}
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 
 		rename = false;
 		String nombreUsuario = "";
 		Clipboard clip = new Clipboard(Display.getCurrent());
 		Object contents = clip
 				.getContents(LocalSelectionTransfer.getTransfer());
 		clip.dispose();
 		if (contents == null) {
 			return null;
 		}
 
 		Object objtarget = getTarget(event.getApplicationContext());
 
 		if (objtarget == null) {
 			return null;
 		}
 		final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		// Distinguimos si lo que hemos seleccionado es un paquete o un
 		// proyecto.
 		if (objtarget instanceof JVPackage) {
 			JVPackage target = (JVPackage) objtarget;
 
 			List<JVBean> beans = getListFromClipboard(contents, JVBean.class);
 			if (beans == null) {
 				return null;
 			}
 			for (JVBean bean : beans) {
 
 				miBean = (IFile) Platform.getAdapterManager().getAdapter(bean,
 						IFile.class);
 
 				IPath pathRecursos = new Path(miBean.getName().replace(
 						".jvflow", ".resources"));
 				recursos = miBean.getParent().getFolder(pathRecursos);
 
 				targetRes = (IResource) Platform.getAdapterManager()
 						.getAdapter(target, IResource.class);
 				targetPath = targetRes.getFullPath().append(miBean.getName());
 				resourcesPath = targetRes.getFullPath().append(
 						recursos.getName());
 
 				// Se comprueba el nombre del fichero y en caso de existir en el
 				// destino se recupera un nombre valido para ofrecer al usuario.
 				String nombre = nombreValido(root, targetPath, target, miBean,
 						targetRes, miBean.getName());
 				// Comprobamos si el fichero existe en el destino. Si existe, se
 				// lanza un cuadro de dialogo donde el usuario podra poner el
 				// nombre que desee al fichero.
 				if (root.getFile(targetPath).exists()) {
 					rename = true;
 					// Proponemos al usuario un nombre valido para el fichero.
 					InputDialog ventana = new InputDialog(
 							HandlerUtil.getActiveShell(event),
 							"Conflicto de nombre",
 							"Ya existe un recurso llamado " + miBean.getName(),
 							nombre, new IInputValidator() {
 
 								@Override
 								public String isValid(String input) {
 
 									IStatus validateName = root
 											.getWorkspace()
 											.validateName(input, IResource.FILE);
 									if (!validateName.isOK()) {
 										return validateName.getMessage();
 									}
 
 									targetPath = targetRes.getFullPath()
 											.append(input);
 									if (root.getFile(targetPath).exists()) {
 										return "Ya existe un recurso con ese nombre";
 									}
 
 									return null;
 								}
 							});
 
 					ventana.open();
 					nombreUsuario = ventana.getValue();
 
 					// Obtenemos el nuevo target con la ruta, y el nombre del
 					// paquete seleccionado por el usuario.
 					targetPath = targetRes.getFullPath().append(nombreUsuario);
 					resourcesPath = targetRes.getFullPath().append(
 							nombreUsuario.replace(".jvflow", ".resources"));
 				} else {
 					nombreUsuario = bean.getName();
 				}
 
 				// Realizamos la copia del fichero al destino.
 				try {
 					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
 						@Override
 						public void run(IProgressMonitor monitor)
 								throws CoreException {
 							miBean.copy(targetPath, true, monitor);
 							recursos.copy(resourcesPath, true, monitor);
 							if (rename) {
 								renameBean(targetPath);
 							}
 						}
 					};
 					ResourcesPlugin.getWorkspace().run(runnable, null);
 
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 
 				// ahora tenemos que obtener el flujo copiado, revisar todos sus
 				// estados y si tiene locutions
 				// cambiar sus URIs
 				Flow flujoChanged = (Flow) target.getBean(nombreUsuario
 						.replace(".jvflow", ""));
 				for (State estado : flujoChanged.getStates()) {
 					if (estado instanceof LocutionState) {
 						LocutionState locution = (LocutionState) estado;
 						VoiceDsl voiceDsl = locution.getLocution();
 						URI uri = EcoreUtil.getURI(flujoChanged);
 						Flow connectedFlow = (Flow) BaseModel.getInstance()
 								.getResourceSet().getEObject(uri, true);
 						IFile targetFlow = (IFile) Platform.getAdapterManager()
 								.getAdapter(connectedFlow, IFile.class);
 
 						IPath pathnuevosResources = new Path(
 								nombreUsuario.replace(".jvflow", ".resources"));
 
 						IResource recursoResource = targetFlow.getParent()
 								.getFolder(pathnuevosResources);
 						IPath pathDsl = recursoResource.getFullPath().append(
 								voiceDsl.getName() + ".voiceDsl");
 						VoiceDsl modificado = changeURI(pathDsl);
 						locution.setLocution(modificado);
 					}
 				}
 			}
 
 		} else if (objtarget instanceof JVProject
 				&& !(objtarget instanceof JVApplication)) {
 			JVProject target = (JVProject) objtarget;
 
 			List<JVPackage> packs = getListFromClipboard(contents,
 					JVPackage.class);
 			if (packs == null) {
 				return null;
 			}
 			for (JVPackage pack : packs) {
 
 				miPack = (IFolder) Platform.getAdapterManager().getAdapter(
 						pack, IFolder.class);
 
 				targetRes = (IResource) Platform.getAdapterManager()
 						.getAdapter(target, IResource.class);
 				targetPath = targetRes.getFullPath().append(
 						miPack.getProjectRelativePath());
 
 				// Se comprueba el nombre de la carpeta y en caso de existir en
 				// el destino se recupera un nombre valido para ofrecer al
 				// usuario.
 				String nombre = nombreValido(root, targetPath, target, miPack,
 						targetRes, miPack.getName());
 				// Comprobamos si la carpeta existe en el destino. Si existe, se
 				// lanza un cuadro de dialogo donde el usuario podra poner el
 				// nombre que desee a la carpeta
 				if (root.getFolder(targetPath).exists()) {
 
 					// Proponemos al usuario un nombre valido para la carpeta.
 					InputDialog ventana = new InputDialog(
 							HandlerUtil.getActiveShell(event),
 							"Conflicto de nombre",
 							"Ya existe un recurso llamado " + miPack.getName(),
 							nombre, new IInputValidator() {
 
 								@Override
 								public String isValid(String input) {
 
 									IStatus validateName = root.getWorkspace()
 											.validateName(input,
 													IResource.FOLDER);
 									if (!validateName.isOK()) {
 										return validateName.getMessage();
 									}
 
 									targetPath = targetRes.getFullPath()
 											.append(miPack.getParent()
 													.getProjectRelativePath()
 													.append(input));
 									if (root.getFolder(targetPath).exists()) {
 										return "Ya existe un recurso con ese nombre";
 									}
 
 									return null;
 								}
 							});
 
 					ventana.open();
 					nombreUsuario = ventana.getValue();
 
 					// Obtenemos el nuevo target con la ruta, y el nombre del
 					// paquete seleccionado por el usuario.
 					targetPath = targetRes.getFullPath().append(
 							miPack.getParent().getProjectRelativePath()
 									.append(nombreUsuario));
 
 				} else {
					nombreUsuario = miPack.getName();
 				}
 
 				// Realizamos la copia del paquete al destino.
 				try {
 					String path = "";
 					int size = targetPath.segmentCount() - 1;
 					for (int i = 0; i < size; i++) {
 						path += "/" + targetPath.segment(i);
 						if (i > 0) {
 							IPath ipath = new Path(path);
 							if (!root.getFolder(ipath).exists()) {
 								IProgressMonitor monitor = null;
 								root.getFolder(ipath).create(true, true,
 										monitor);
 							}
 						}
 
 					}
 					miPack.copy(targetPath, true, null);
 				} catch (CoreException e) {
 					throw new ExecutionException(e.getMessage(), e);
 				}
 
 				JVModule project = (JVModule) target;
 				JVPackage paqueteCopiado = project.getPackage(nombreUsuario);
 				for (JVBean bean : paqueteCopiado.getBeans()) {
 					if (bean instanceof Flow) {
 						Flow flujo = (Flow) bean;
 						for (State estado : flujo.getStates()) {
 							if (estado instanceof LocutionState) {
 								LocutionState locution = (LocutionState) estado;
 								VoiceDsl voiceDsl = locution.getLocution();
 								URI uri = EcoreUtil.getURI(flujo);
 								Flow connectedFlow = (Flow) BaseModel
 										.getInstance().getResourceSet()
 										.getEObject(uri, true);
 								IFile targetFlow = (IFile) Platform
 										.getAdapterManager().getAdapter(
 												connectedFlow, IFile.class);
 
 								IPath pathnuevosResources = new Path(
 										flujo.getName() + ".resources");
 
 								IResource recursoResource = targetFlow
 										.getParent().getFolder(
 												pathnuevosResources);
 								IPath pathDsl = recursoResource.getFullPath()
 										.append(voiceDsl.getName()
 												+ ".voiceDsl");
 								VoiceDsl modificado = changeURI(pathDsl);
 								locution.setLocution(modificado);
 							}
 						}
 					}
 				}
 			}
 
 		} else if (objtarget instanceof Flow) {
 			Flow target = (Flow) objtarget;
 
 			List<VoiceDsl> dsls = getListFromClipboard(contents, VoiceDsl.class);
 			if (dsls == null) {
 				return null;
 			}
 			for (VoiceDsl dsl : dsls) {
 
 				miBean = (IFile) Platform.getAdapterManager().getAdapter(dsl,
 						IFile.class);
 
 				targetRes = (IResource) Platform.getAdapterManager()
 						.getAdapter(target, IResource.class);
 
 				// tenemos el path del flujo de destino, necesitamos su carpeta
 				// resources
 				targetRes = targetRes.getParent().findMember(
 						targetRes.getName().replace(".jvflow", ".resources"));
 
 				if (!targetRes.exists()) {
 					return null;
 				}
 
 				targetPath = targetRes.getFullPath().append(miBean.getName());
 
 				// Se comprueba el nombre del fichero y en caso de existir en el
 				// destino se recupera un nombre valido para ofrecer al usuario.
 				String nombre = nombreValido(
 						root,
 						targetPath,
 						target,
 						miBean,
 						targetRes,
 						miBean.getName().substring(0,
 								miBean.getName().lastIndexOf(".")));
 				// Comprobamos si el fichero existe en el destino. Si existe, se
 				// lanza un cuadro de dialogo donde el usuario podra poner el
 				// nombre que desee al fichero.
 				if (root.getFile(targetPath).exists()) {
 					rename = true;
 					// Proponemos al usuario un nombre valido para el fichero.
 					InputDialog ventana = new InputDialog(
 							HandlerUtil.getActiveShell(event),
 							"Conflicto de nombre",
 							"Ya existe un recurso llamado " + miBean.getName(),
 							nombre, new IInputValidator() {
 
 								@Override
 								public String isValid(String input) {
 
 									IStatus validateName = root
 											.getWorkspace()
 											.validateName(input, IResource.FILE);
 									if (!validateName.isOK()) {
 										return validateName.getMessage();
 									}
 									// targetPath = targetRes.getFullPath()
 									// .append(miBean.getParent()
 									// .getProjectRelativePath()
 									// .append(input));
 
 									targetPath = targetRes.getFullPath()
 											.append(input);
 									if (root.getFile(targetPath).exists()) {
 										return "Ya existe un recurso con ese nombre";
 									}
 
 									return null;
 								}
 							});
 
 					ventana.open();
 					nombreUsuario = ventana.getValue();
 
 					// Obtenemos el nuevo target con la ruta, y el nombre del
 					// paquete seleccionado por el usuario.
 					targetPath = targetRes.getFullPath().append(
 							nombreUsuario + ".voiceDsl");
 
 				}
 
 				// Realizamos la copia del fichero al destino.
 				try {
 					IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
 						@Override
 						public void run(IProgressMonitor monitor)
 								throws CoreException {
 							miBean.copy(targetPath, true, null);
 							if (rename) {
 								renameBean(targetPath);
 							}
 
 						}
 					};
 					ResourcesPlugin.getWorkspace().run(runnable, null);
 
 				} catch (CoreException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 
 		return null;
 	}
 
 	private Object getTarget(Object target) {
 		Object def = ((IEvaluationContext) target).getDefaultVariable();
 		if (def instanceof Collection<?> && ((Collection<?>) def).size() == 1) {
 
 			Object o = ((Collection<?>) def).iterator().next();
 
 			if (o instanceof JVPackage || o instanceof JVProject
 					|| o instanceof JVBean) {
 				return o;
 			} else {
 				return null;
 			}
 		}
 		return null;
 	}
 
 	private <T> List<T> getListFromClipboard(Object contents, Class<T> clazz) {
 
 		// Todos los elementos deben ser navegaciones
 		if (contents instanceof IStructuredSelection) {
 			IStructuredSelection selection = (IStructuredSelection) contents;
 			if (selection.isEmpty()) {
 				return null;
 			}
 			List<?> toPaste = selection.toList();
 			for (Object o : toPaste) {
 				if (!clazz.isInstance(o)) {
 					return null;
 				}
 			}
 			@SuppressWarnings("unchecked")
 			List<T> ret = (List<T>) toPaste;
 			return ret;
 		}
 		return null;
 	}
 
 	// Metodo para recuperar un nombre valido para el fichero o la carpeta que
 	// se quiere copiar en caso de que haya uno en el destino con el mismo
 	// nombre.
 	private String nombreValido(IWorkspaceRoot root, IPath targetPath,
 			Object targets, Object miobjeto, IResource targetRes,
 			String nameUser) {
 		if (targets instanceof JVProject) {
 			JVProject target = (JVProject) targets;
 			IFolder mipackage = (IFolder) miobjeto;
 
 			if (root.getFolder(targetPath).exists()) {
 
 				String newName = "CopyOf" + nameUser;
 
 				targetPath = targetRes.getFullPath().append(
 						mipackage.getParent().getProjectRelativePath()
 								.append(newName));
 
 				return nombreValido(root, targetPath, target, mipackage,
 						targetRes, newName);
 
 			} else {
 				return nameUser;
 			}
 		} else if (targets instanceof JVPackage) {
 			JVPackage target = (JVPackage) targets;
 			IFile mipackage = (IFile) miobjeto;
 
 			if (root.getFile(targetPath).exists()) {
 
 				String newName = "CopyOf" + nameUser;
 
 				// targetPath = targetRes.getFullPath().append(
 				// mipackage.getParent().getProjectRelativePath()
 				// .append(newName));
 
 				targetPath = targetRes.getFullPath().append(newName);
 
 				return nombreValido(root, targetPath, target, mipackage,
 						targetRes, newName);
 
 			} else {
 				return nameUser;
 			}
 
 		} else if (targets instanceof Flow) {
 			Flow target = (Flow) targets;
 			IFile miDsl = (IFile) miobjeto;
 
 			if (root.getFile(targetPath).exists()) {
 
 				String newName = "CopyOf" + nameUser;
 
 				// targetPath = targetRes.getFullPath().append(
 				// mipackage.getParent().getProjectRelativePath()
 				// .append(newName));
 
 				targetPath = targetRes.getFullPath().append(
 						newName + ".voiceDsl");
 
 				return nombreValido(root, targetPath, target, miDsl, targetRes,
 						newName);
 
 			} else {
 				return nameUser;
 			}
 		} else {
 			return null;
 		}
 	}
 
 	private void renameBean(IPath targetPath) {
 		String newName = targetPath.lastSegment();
 		newName = newName.substring(0, newName.lastIndexOf('.'));
 		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 		Resource emfRes = resourceSetImpl.createResource(URI
 				.createPlatformResourceURI(targetPath.toString(), true));
 		try {
 			emfRes.load(null);
 
 			for (EObject obj : emfRes.getContents()) {
 				if (obj instanceof VoiceDsl) {
 					((VoiceDsl) obj).setName(newName);
 
 				} else if (obj instanceof JVBean) {
 					((JVBean) obj).setName(newName);
 					((JVBean) obj).setDescription(newName);
 					List<EObject> listaObjetos = ((Flow) obj).eResource()
 							.getContents();
 					for (int i = 0; i < listaObjetos.size(); i++) {
 						EObject objeto = listaObjetos.get(i);
 						if (objeto instanceof Diagram) {
 							((Diagram) objeto).setName(newName);
 						}
 					}
 				}
 			}
 			try {
 				emfRes.save(SaveOptions.newBuilder().noValidation()
 						.getOptions().toOptionsMap());
 			} catch (RuntimeException re) {
 				re.printStackTrace();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private VoiceDsl changeURI(IPath resourcesPath) {
 		VoiceDsl modificado = null;
 		String newName = resourcesPath.lastSegment();
 		newName = newName.substring(0, newName.lastIndexOf('.'));
 		ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 		Resource emfRes = resourceSetImpl.createResource(URI
 				.createPlatformResourceURI(resourcesPath.toString(), true));
 		try {
 			emfRes.load(null);
 
 			for (EObject obj : emfRes.getContents()) {
 				if (obj instanceof VoiceDsl) {
 					modificado = (VoiceDsl) obj;
 
 				}
 			}
 			try {
 			} catch (RuntimeException re) {
 				re.printStackTrace();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return modificado;
 	}
 
 }
