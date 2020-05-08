 package com.vectorsf.jvoice.diagram.core.diagram;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ContainerSelectionDialog;
 
 import com.vectorsf.jvoice.model.base.JVPackage;
 import com.vectorsf.jvoice.model.base.JVProject;
 
 @SuppressWarnings("restriction")
 public class DiagramNameWizardPage extends AbstractWizardPage {
 
 	private static final String PAGE_DESC = "Enter a diagram name";
 	private static final String PAGE_TITLE = "New JVoice Diagram";
 
 	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
 
 	Text textField;
 	Text textFieldFolder;
 
 	private Listener nameModifyListener = new Listener() {
 		@Override
 		public void handleEvent(Event e) {
 			boolean valid = validatePage();
 			setPageComplete(valid);
 
 		}
 	};
 	private Object selection;
 	private String initialFolder = "";
 
 	public DiagramNameWizardPage(String pageName, String title,
 			ImageDescriptor titleImage) {
 		super(pageName, title, titleImage);
 	}
 
 	protected DiagramNameWizardPage(String pageName) {
 		super(pageName);
 		setTitle(PAGE_TITLE);
 		setDescription(PAGE_DESC);
 	}
 
 	@Override
 	public void createControl(Composite parent) {
 		Composite composite = new Composite(parent, SWT.NULL);
 		composite.setFont(parent.getFont());
 
 		initializeDialogUnits(parent);
 
 		composite.setLayout(new GridLayout());
 		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		createWizardContents(composite);
 
 		// Show description on opening
 		setErrorMessage(null);
 		setMessage(null);
 
 		setPageComplete(validatePage());
 
 		setControl(composite);
 	}
 
 	@Override
 	public String getText() {
 		if (textField == null) {
 			return getInitialTextFieldValue();
 		}
 
 		return getTextFieldValue();
 	}
 
 	protected boolean validatePage() {
 		String text = getTextFieldValue();
 		String path = getPathFieldValue();
 		if (text.equals("") || path.equals("")) { //$NON-NLS-1$
 			setErrorMessage(null);
 			setMessage("Message empty");
 			return false;
 		}
 
 		IWorkspace workspace = ResourcesPlugin.getWorkspace();
 
 		IStatus status = doWorkspaceValidation(workspace, text);
 		if (!status.isOK()) {
 			setErrorMessage(status.getMessage());
 			return false;
 		}
 
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IPath ruta = new Path(path);
 
 		// si la ltima barra no est en la posicin 0 el path es un paquete
		if (ruta.toString().lastIndexOf("/") != 0) {
 			setSelection(root.getFolder(ruta));
 		} else {
 			// se trata de un proyecto
 			setErrorMessage("Debe seleccionar un paquete");
 			return false;
 		}
 
 		if (root.exists(ruta)) {
 			ruta = ruta.append(text);
 			ruta = ruta.addFileExtension("jvflow");
 			if (!root.exists(ruta)) {
 				setErrorMessage(null);
 				setMessage(null);
 				return true;
 			} else {
 				setErrorMessage("Resource with this name already exists");
 				return false;
 			}
 		} else {
 			setErrorMessage("La ruta seleccionada no existe");
 			return false;
 		}
 	}
 
 	@Override
 	protected void createWizardContents(Composite parent) {
 		createProjectNameGroup(parent);
 	}
 
 	private final void createProjectNameGroup(Composite parent) {
 
 		IProject project = null;
 		IFolder diagramFolder = null;
 		if (selection instanceof IProject) {
 			project = (IProject) selection;
 		} else if (selection instanceof IFolder) {
 			diagramFolder = (IFolder) selection;
 			project = diagramFolder.getProject();
 		} else if (selection instanceof JVProject) {
 			project = (IProject) Platform.getAdapterManager().getAdapter(
 					selection, IProject.class);
 			diagramFolder = (IFolder) Platform.getAdapterManager().getAdapter(
 					selection, IFolder.class);
 		} else if (selection instanceof JVPackage) {
 			diagramFolder = (IFolder) Platform.getAdapterManager().getAdapter(
 					selection, IFolder.class);
 			project = diagramFolder.getProject();
 		} else if (selection instanceof IPackageFragmentRoot) {
 			IPackageFragmentRoot prueba = (IPackageFragmentRoot) selection;
 			diagramFolder = (IFolder) prueba.getResource();
 			project = diagramFolder.getProject();
 		}
 
 		initialFolder = diagramFolder != null ? diagramFolder.getFullPath()
 				.toString() : project.getFullPath().toString();
 
 		Composite projectGroup = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		projectGroup.setLayout(layout);
 		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 
 		// new project label
 		Label projectLabel = new Label(projectGroup, SWT.NONE);
 		projectLabel.setText("Diagram name:");
 		projectLabel.setFont(parent.getFont());
 
 		// new project name entry field
 		textField = new Text(projectGroup, SWT.BORDER);
 		GridData data = new GridData(GridData.FILL_HORIZONTAL);
 		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
 		textField.setLayoutData(data);
 		textField.setFont(parent.getFont());
 
 		DialogField.createEmptySpace(projectGroup);
 
 		// new project label
 		Label projectLabel2 = new Label(projectGroup, SWT.NONE);
 		projectLabel2.setText("folder:");
 		projectLabel2.setFont(parent.getFont());
 
 		textFieldFolder = new Text(projectGroup, SWT.BORDER);
 		GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
 		data2.widthHint = SIZING_TEXT_FIELD_WIDTH;
 		textFieldFolder.setLayoutData(data2);
 		textFieldFolder.setFont(parent.getFont());
 
 		// browse button on right
 		Button browse = new Button(projectGroup, SWT.PUSH);
 		browse.setText("Browse...");
 		browse.addListener(SWT.Selection, new Listener() {
 
 			@Override
 			public void handleEvent(Event event) {
 				ContainerSelectionDialog dialog = new ContainerSelectionDialog(
 						getShell(), null, true, "Select a parent:");
 				dialog.setTitle("Container Selection");
 				dialog.open();
 				Object[] result = dialog.getResult();
 				if (result != null && result.length == 1) {
 					IPath ruta = (IPath) result[0];
 					IFolder fichero = ResourcesPlugin.getWorkspace().getRoot()
 							.getFolder(ruta);
 					setSelection(fichero);
 					textFieldFolder.setText(ruta.toString());
 				} else {
 					textFieldFolder.setText("");
 				}
 			}
 		});
 
 		// Set the initial value first before listener
 		// to avoid handling an event during the creation.
 		if (getInitialTextFieldValue() != null) {
 			textField.setText(getInitialTextFieldValue());
 		}
 		textField.addListener(SWT.Modify, nameModifyListener);
 
 		textFieldFolder.setText(getInitialTextFolderFieldValue());
 		textFieldFolder.addListener(SWT.Modify, nameModifyListener);
 	}
 
 	private String getTextFieldValue() {
 		if (textField == null) {
 			return ""; //$NON-NLS-1$
 		}
 
 		return textField.getText().trim();
 	}
 
 	private String getPathFieldValue() {
 		if (textFieldFolder == null) {
 			return ""; //$NON-NLS-1$
 		}
 		return textFieldFolder.getText().trim();
 	}
 
 	private String getInitialTextFieldValue() {
 		return "newJVoice"; //$NON-NLS-1$
 	}
 
 	private String getInitialTextFolderFieldValue() {
 		return initialFolder;
 	}
 
 	private IStatus doWorkspaceValidation(IWorkspace workspace, String text) {
 		IStatus ret = workspace.validateName(text, IResource.FILE);
 		return ret;
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
 			textField.setFocus();
 			textField.selectAll();
 		}
 	}
 
 	public void setSelection(Object firstElement) {
 		selection = firstElement;
 
 	}
 
 	public Object getSelection() {
 		return selection;
 	}
 }
