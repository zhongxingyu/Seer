 package io.leon.eclipseintegration.ui.properties;
 
 import io.leon.eclipseintegration.ui.Activator;
 import io.leon.eclipseintegration.ui.natures.LeonNature;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
 import org.eclipse.ui.dialogs.ISelectionStatusValidator;
 import org.eclipse.ui.dialogs.PropertyPage;
 import org.eclipse.ui.model.BaseWorkbenchContentProvider;
 import org.eclipse.ui.model.WorkbenchLabelProvider;
 
 public class LeonPropertyPage extends PropertyPage {
 
 	private PreferencesHandler preferenceHandler;
 	private LeonPreferences leonPreferences;
 
 	private IProject project;
 
 	private Text webConfigurationFileInput;
 
 	public LeonPropertyPage() {
 		// super("LEON");
 		// setDescription("Leon web container configuration");
 		noDefaultAndApplyButton();
 
 	}
 
 	@Override
 	protected Control createContents(Composite parent) {
 		boolean initializationComplete = initPreferences(parent);
 
 		if (!initializationComplete) {
 			Label label = new Label(parent, SWT.NONE);
 			label.setText("No Leon project selected");
 			return label;
 		}
 
 		Composite contents = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 3;
 		contents.setLayout(layout);
 		contents.setLayoutData(new GridData(GridData.FILL_BOTH));
 
 		Label webConfigurationFile = new Label(contents, SWT.NONE);
 		webConfigurationFile.setText("Configuration file:");
 
 		webConfigurationFileInput = new Text(contents, SWT.BORDER);
 		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
 		webConfigurationFileInput.setLayoutData(gd);
 
 		final Button webConfigurationFileSelect = new Button(contents, SWT.PUSH);
 		webConfigurationFileSelect.setText("Browse");
 		webConfigurationFileSelect.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				if (!webConfigurationFileSelect.isDisposed()) {
 					ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(
 							webConfigurationFileSelect.getDisplay()
 									.getActiveShell(),
 							new WorkbenchLabelProvider(),
 							new BaseWorkbenchContentProvider());
 					dialog.setTitle("Select configuration file");
 					dialog.setMessage("Select the configuration file in the project:");
 					dialog.setInput(project);
 					dialog.setAllowMultiple(false);
 					dialog.setDoubleClickSelects(true);
 					dialog.setValidator(new ISelectionStatusValidator() {
 
 						public IStatus validate(Object[] selection) {
 							if (selection != null && selection.length == 1
 									&& selection[0] instanceof IFile) {
 								IFile file = (IFile) selection[0];
 								if (file.getFileExtension().equalsIgnoreCase(
										"js")) {
 									return new Status(IStatus.OK,
 											Activator.PLUGIN_ID, IStatus.OK,
 											"", null);
 								}
 							}
 
 							return new Status(IStatus.ERROR,
 									Activator.PLUGIN_ID, IStatus.OK,
									"Please select a javascript file", null);
 						}
 					});
 					int result = dialog.open();
 
 					if (result == Window.OK) {
 						Object resultObject = dialog.getFirstResult();
 
 						if (resultObject instanceof IFile) {
 							IFile file = (IFile) resultObject;
 							webConfigurationFileInput.setText(File.separator + file.getProjectRelativePath().toOSString());
 						}
 					}
 				}
 			}
 
 		});
 
 		dataToView();
 
 		return contents;
 	}
 
 	private boolean initPreferences(Composite parent) {
 		IResource resource = (IResource) getElement().getAdapter(
 				IResource.class);
 
 		if (resource == null || resource.getType() != IResource.PROJECT) {
 			return false;
 		}
 
 		try {
 			if (!((IProject) resource).hasNature(LeonNature.NATURE_ID)) {
 				return false;
 			}
 		} catch (CoreException e) {
 			return false;
 		}
 
 		project = (IProject) resource;
 		preferenceHandler = new PreferencesHandler(project);
 		leonPreferences = preferenceHandler.getPreferences();
 
 		return true;
 	}
 
 	private void dataToView() {
 		if (leonPreferences.getConfigurationFile() != null)
 			webConfigurationFileInput.setText(leonPreferences
 					.getConfigurationFile());
 
 	}
 
 	@Override
 	public boolean performOk() {
 		viewToData();
 		update();
 
 		return true;
 	}
 
 	private void viewToData() {
 		leonPreferences.setWebConfigurationFile(webConfigurationFileInput
 				.getText());
 	}
 
 	private void update() {
 
 		preferenceHandler.save();
 	}
 
 }
