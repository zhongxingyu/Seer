 package com.vectorsf.jvoice.ui.navigator.util;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.maven.model.Model;
 import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
 import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.jface.wizard.IWizardPage;
 import org.eclipse.ltk.core.refactoring.RefactoringStatus;
 import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
 import org.eclipse.ltk.internal.core.refactoring.resource.RenameResourceProcessor;
 import org.eclipse.ltk.internal.ui.refactoring.RefactoringUIMessages;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizard;
 import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.xtext.resource.SaveOptions;
 
 import com.vectorsf.jvoice.base.model.service.BaseModel;
 import com.vectorsf.jvoice.model.base.JVBean;
 import com.vectorsf.jvoice.model.base.JVProject;
 import com.vectorsf.jvoice.model.operations.Flow;
 import com.vectorsf.jvoice.prompt.model.voiceDsl.VoiceDsl;
 import com.vectorsf.jvoice.ui.navigator.activator.Activator;
 
 public class RenameIVRResourceWizard extends RefactoringWizard {
 
 	private IResource resource;
 	RenameResourceRefactoringConfigurationPage reconfigRename;
 
 	@SuppressWarnings("restriction")
 	public RenameIVRResourceWizard(IResource resource) {
 
 		super(new RenameRefactoring(new RenameResourceProcessor(resource)),
 				DIALOG_BASED_USER_INTERFACE);
 		setDefaultPageTitle(RefactoringUIMessages.RenameResourceWizard_page_title);
 		setWindowTitle(RefactoringUIMessages.RenameResourceWizard_window_title);
 
 		this.resource = resource;
 	}
 
 	@SuppressWarnings("restriction")
 	@Override
 	protected void addUserInputPages() {
 		RenameResourceProcessor processor = (RenameResourceProcessor) getRefactoring()
 				.getAdapter(RenameResourceProcessor.class);
 
 		reconfigRename = new RenameResourceRefactoringConfigurationPage(
 				processor, resource);
 		addPage(reconfigRename);
 	}
 
 	public boolean isDsl() {
 		return reconfigRename.isDsl();
 	}
 
 	public String getNewName() {
 		return reconfigRename.getText();
 	}
 
 	private static class RenameResourceRefactoringConfigurationPage extends
 			UserInputWizardPage {
 
 		private static final String POM_XML = "/pom.xml";
 		@SuppressWarnings("restriction")
 		private final RenameResourceProcessor fRefactoringProcessor;
 		private Text fNameField;
 		private IResource resource;
 		private String nombre;
 		private String textoExtension;
 		private boolean isDsl = false;
 		private String newName;
 
 		@SuppressWarnings("restriction")
 		public RenameResourceRefactoringConfigurationPage(
 				RenameResourceProcessor processor, IResource resource) {
 			super("RenameResourceRefactoringInputPage"); //$NON-NLS-1$
 			fRefactoringProcessor = processor;
 			this.resource = resource;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt
 		 * .widgets.Composite)
 		 */
 		@SuppressWarnings("restriction")
 		@Override
 		public void createControl(Composite parent) {
 			Composite composite = new Composite(parent, SWT.NONE);
 			composite.setLayout(new GridLayout(2, false));
 			composite
 					.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 			composite.setFont(parent.getFont());
 
 			Label label = new Label(composite, SWT.NONE);
 			label.setText(RefactoringUIMessages.RenameResourceWizard_name_field_label);
 			label.setLayoutData(new GridData());
 
 			if (resource instanceof IFile) {
 				nombre = fRefactoringProcessor.getNewResourceName();
 				if (nombre.contains(".")
 						&& nombre.substring(nombre.indexOf(".")).length() > 1) {
 					textoExtension = nombre.substring(nombre.indexOf(".") + 1);
 					nombre = nombre.substring(0, nombre.indexOf("."));
 				}
 			}
 
 			if (resource instanceof IFolder || resource instanceof IProject) {
 				nombre = fRefactoringProcessor.getNewResourceName();
 			}
 
 			fNameField = new Text(composite, SWT.BORDER);
 			fNameField.setText(nombre);
 			fNameField.setFont(composite.getFont());
 			fNameField.setLayoutData(new GridData(GridData.FILL,
 					GridData.BEGINNING, true, false));
 			fNameField.addModifyListener(new ModifyListener() {
 				@Override
 				public void modifyText(ModifyEvent e) {
 					validatePage();
 				}
 			});
 
 			fNameField.selectAll();
 			setPageComplete(false);
 			setControl(composite);
 
 		}
 
 		@Override
 		public void setVisible(boolean visible) {
 			if (visible) {
 				fNameField.setFocus();
 			}
 			super.setVisible(visible);
 		}
 
 		@SuppressWarnings("restriction")
 		protected final void validatePage() {
 			String text = fNameField.getText();
 
 			RefactoringStatus status = null;
 			RefactoringStatus statusName;
 			if (textoExtension != null) {
 
 				status = fRefactoringProcessor.validateNewElementName(text
 						+ "." + textoExtension);
 			} else {
 
 				status = fRefactoringProcessor.validateNewElementName(text);
 			}
 
 			statusName = evaluateName(text);
 
 			status.merge(statusName);
 			setPageComplete(status);
 		}
 
 		private RefactoringStatus evaluateName(String text) {
 			if (text != null) {
 				char initial = text.charAt(0);
 				if (Character.isDigit(initial)) {
 					return RefactoringStatus
 							.createFatalErrorStatus("The first letter of name can not be a digit");
 				} else if (!Character.isJavaLetter(initial)) {
 					return RefactoringStatus
 							.createFatalErrorStatus("The first letter of name is not valid");
 				} else {
 					for (int i = 1; i < text.length(); i++) {
 						char letter = text.charAt(i);
 						if (!Character.isJavaLetterOrDigit(letter)) {
 							return RefactoringStatus
 									.createFatalErrorStatus("Name contains incorrect character");
 						}
 
 					}
 					return null;
 				}
 			}
 			return null;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.eclipse.ltk.ui.refactoring.UserInputWizardPage#performFinish()
 		 */
 		@Override
 		protected boolean performFinish() {
 			try {
 				IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
 					@Override
 					public void run(IProgressMonitor monitor)
 							throws CoreException {
 						initializeRefactoring();
 						if (resource instanceof IFile) {
 							renameBean(monitor);
 						} else if (resource instanceof IProject) {
 							renameProjec(monitor);
 						}
 						superPerformFinish();
 					}
 
 				};
 				ResourcesPlugin.getWorkspace().run(runnable, null);
 
 				return true;
 			} catch (CoreException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		protected void superPerformFinish() {
 			super.performFinish();
			if (resource instanceof IProject) {
				// Metodo para cambiar el Group Id y el Artifact Id
				changeGroupAndArtifact();
			}
 
 		}
 
 		// Metodo para cambiar el group Id y artifact del proyecto o Aplicacion
 		// con el nuevo nombre que se le introduzca.
 		private void changeGroupAndArtifact() {
 			JVProject newproject = BaseModel.getInstance().getModel()
 					.getProject(newName);
 			IProject iproject = ResourcesPlugin.getWorkspace().getRoot()
 					.getProject(newproject.getName());
 			try {
 				MavenXpp3Reader reader = new MavenXpp3Reader();
 				Model mm = null;
 				File pomFile = new File(iproject.getLocationURI().getPath(),
 						POM_XML);
 				mm = reader.read(new FileInputStream(pomFile));
 
 				if (mm.getArtifactId().equals(resource.getName())) {
 					mm.setArtifactId(newName);
 				}
 				if (mm.getGroupId().equals(resource.getName())) {
 					mm.setGroupId(newName);
 				}
 
 				MavenXpp3Writer writer = new MavenXpp3Writer();
 				writer.write(new FileOutputStream(pomFile), mm);
 			} catch (IOException | XmlPullParserException e) {
 				Activator
 						.getDefault()
 						.getLog()
 						.log(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
 								"Error leyendo fichero", e));
 
 				e.printStackTrace();
 			}
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see org.eclipse.ltk.ui.refactoring.UserInputWizardPage#getNextPage()
 		 */
 		@Override
 		public IWizardPage getNextPage() {
 			initializeRefactoring();
 			return super.getNextPage();
 		}
 
 		@SuppressWarnings("restriction")
 		private void initializeRefactoring() {
 			if (textoExtension != null) {
 				fRefactoringProcessor.setNewResourceName(fNameField.getText()
 						+ "." + textoExtension);
 			} else {
 				fRefactoringProcessor.setNewResourceName(fNameField.getText());
 			}
 
 		}
 
 		private boolean renameProjec(IProgressMonitor monitor)
 				throws CoreException {
 			newName = fNameField.getText();
 
 			JVProject project = BaseModel.getInstance().getModel()
 					.getProject(resource.getName());
 
 			ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 			Resource emfRes = resourceSetImpl.createResource(project
 					.eResource().getURI());
 
 			try {
 				emfRes.load(null);
 				for (EObject obj : emfRes.getContents()) {
 					if (obj instanceof JVProject) {
 						((JVProject) obj).setName(newName);
 					}
 
 					try {
 						emfRes.save(SaveOptions.newBuilder().noValidation()
 								.getOptions().toOptionsMap());
 
 					} catch (RuntimeException re) {
 						re.printStackTrace();
 					}
 
 				}
 
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			return true;
 
 		}
 
 		private boolean renameBean(IProgressMonitor monitor)
 				throws CoreException {
 			newName = fNameField.getText();
 			ResourceSetImpl resourceSetImpl = new ResourceSetImpl();
 			Resource emfRes = resourceSetImpl.createResource(URI
 					.createPlatformResourceURI(resource.getFullPath()
 							.toString(), true));
 
 			try {
 				emfRes.load(null);
 				for (EObject obj : emfRes.getContents()) {
 					if (obj instanceof VoiceDsl) {
 						((VoiceDsl) obj).setName(newName);
 						isDsl = true;
 
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
 						try {
 							emfRes.save(SaveOptions.newBuilder().noValidation()
 									.getOptions().toOptionsMap());
 
 						} catch (RuntimeException re) {
 							re.printStackTrace();
 						}
 					}
 				}
 
 				return true;
 			} catch (IOException e) {
 				e.printStackTrace();
 				return false;
 			}
 		}
 
 		public boolean isDsl() {
 			return isDsl;
 		}
 
 		public String getText() {
 			return newName;
 		}
 
 	}
 }
