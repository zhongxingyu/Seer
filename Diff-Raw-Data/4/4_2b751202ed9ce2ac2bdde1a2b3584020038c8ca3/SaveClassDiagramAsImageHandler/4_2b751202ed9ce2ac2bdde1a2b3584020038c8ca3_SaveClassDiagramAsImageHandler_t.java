 package net.fikovnik.projects.taco.ui.internal.handlers;
 
 import java.io.File;
 
 import net.fikovnik.projects.taco.core.TacoCorePlugin;
 import net.fikovnik.projects.taco.core.graphwiz.IGraphwiz.GraphwizOutputType;
 import net.fikovnik.projects.taco.core.jobs.GenerateClassDiagramJob;
 import net.fikovnik.projects.taco.ui.TacoUIPlugin;
 
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.runtime.jobs.IJobChangeEvent;
 import org.eclipse.core.runtime.jobs.JobChangeAdapter;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.impl.EcoreFactoryImpl;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialogWithToggle;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 public final class SaveClassDiagramAsImageHandler extends AbstractHandler
 		implements IHandler {
 
 	private static final String REVEL_RESULT_TOGGLE = "SaveClassDiagramAsImage.revealResult";
 
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
 				.getActiveMenuSelectionChecked(event);
 
 		if (selection.isEmpty()) {
 			return null;
 		}
 
 		EPackage pkg = EcoreFactoryImpl.eINSTANCE.createEPackage();
 		
 		Object[] selectedItems = selection.toArray();
 		
 		// if there is only one item selected and it is a package then copy all class from it
 		if (selectedItems.length == 1 && selectedItems[0] instanceof EPackage) {
 			EPackage selPkg = (EPackage) selectedItems[0];
 			pkg.getEClassifiers().addAll(EcoreUtil.copyAll(selPkg.getEClassifiers()));
 		} else {
 			for (Object o : selectedItems) {
 				assert o instanceof EClass;
 				pkg.getEClassifiers().add(EcoreUtil.copy((EClass)o));
 			}
 		}
 		
 		GraphwizOutputType[] types = GraphwizOutputType.values();
 		String[] typeNames = new String[types.length];
 		String[] typeExtensions = new String[types.length];
 		for (int i = 0; i < types.length; i++) {
 			typeNames[i] = types[i].getName();
 			typeExtensions[i] = "*." + types[i].getFileExtension();
 		}
 		
 		FileDialog fd = new FileDialog(
 				HandlerUtil.getActiveShellChecked(event), SWT.SAVE);
 		fd.setOverwrite(true);
 		fd.setText("Save Class Diagram As");
		fd.setFileName("ClassDiagram."+types[0].getFileExtension());
 		fd.setFilterExtensions(typeExtensions);
 		fd.setFilterNames(typeNames);
 
 		String fileName = fd.open();
 		if (fileName == null) {
 			return null;
 		}
 		
 		File target = new File(fileName);
 		GraphwizOutputType type = types[fd.getFilterIndex()];
 
 		GenerateClassDiagramJob job = new GenerateClassDiagramJob(pkg, target, type,
 				TacoCorePlugin.getDefault().getGraphwizService());
 
 		job.addJobChangeListener(new JobChangeAdapter() {
 			@Override
 			public void done(IJobChangeEvent event) {
 				if (!event.getResult().isOK()) {
 					return;
 				}
 				GenerateClassDiagramJob job = (GenerateClassDiagramJob) event
 						.getJob();
 				final File targetFile = job.getTarget();
 
 				Display.getDefault().asyncExec(new Runnable() {
 
 					@Override
 					public void run() {
 						Shell shell = PlatformUI.getWorkbench()
 								.getActiveWorkbenchWindow().getShell();
 
 						MessageDialogWithToggle dialog = MessageDialogWithToggle
 								.openYesNoQuestion(
 										shell,
 										"Class Diagram Generated",
 										"Class Diagram has been generated. Do you want to see it?",
 										null, false, TacoUIPlugin.getDefault()
 												.getPreferenceStore(),
 										REVEL_RESULT_TOGGLE);
 						if (dialog.getReturnCode() == IDialogConstants.YES_ID) {
 							Program.launch(targetFile.getAbsolutePath());
 						}
 					}
 				});
 			}
 		});
 
 		job.schedule();
 
 		return null;
 	}
 }
