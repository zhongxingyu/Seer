 package org.mule.tooling.devkit.wizards;
 
 import java.util.regex.Pattern;
 
 import org.eclipse.jface.dialogs.IDialogPage;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.mule.tooling.core.runtime.server.ServerDefinition;
 import org.mule.tooling.devkit.common.ConnectorMavenModel;
 import org.mule.tooling.devkit.common.DevkitUtils;
 import org.mule.tooling.ui.MuleUiConstants;
 import org.mule.tooling.ui.common.ServerChooserComponent;
 import org.mule.tooling.ui.preferences.MuleStudioPreference;
 import org.mule.tooling.ui.utils.UiUtils;
 import org.mule.tooling.ui.wizards.extensible.PartStatusHandler;
 import org.mule.tooling.ui.wizards.extensible.WizardPagePartExtension;
 
 public class NewDevkitProjectWizardPage extends WizardPage {
 
 	private static final String DEFAULT_VERSION = "1.0.0-SNAPSHOT";
 	private static final String DEFAULT_ARTIFACT_ID = "hello-module";
 	private static final String DEFAULT_GROUP_ID = "org.mule.modules";
 	private static final String DEFAULT_NAME = "Hello";
 	private static final String DEFAULT_CATEGORY = DevkitUtils.CATEGORY_COMMUNITY;
	private static final String GROUP_TITLE_CONNECTOR = "AnyPoint Connector";
 	private static final String GROUP_TITLE_MAVEN_SETTINGS = "Maven Settings";
 	private static final String CREATE_POM_LABEL = "Manually set values";
 	private Text groupId;
 	private Text artifactId;
 	private Text version;
 	private Text name;
 	private String devkitVersion;
 	private String connectorCategory = DEFAULT_CATEGORY;
 	private final Pattern connectorName = Pattern.compile("[A-Z]+[a-zA-Z]+");
 
 	private ServerDefinition selectedServerDefinition;
 	private Button cbCreatePomCheckbox;
 	private ConnectorMavenModel model;
 
 	public NewDevkitProjectWizardPage(ISelection selection,
 			ConnectorMavenModel model) {
 		super("wizardPage");
		setTitle("New Connector");
 		setDescription("This wizard creates a new connector project");
 		selectedServerDefinition = new MuleStudioPreference()
 				.getDefaultRuntimeSelection();
 		this.model = model;
 	}
 
 	/**
 	 * @see IDialogPage#createControl(Composite)
 	 */
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 3;
 		layout.verticalSpacing = 6;
 
 		Group connectorGroupBox = UiUtils.createGroupWithTitle(container,
 				GROUP_TITLE_CONNECTOR, 2);
 		ModifyListener connectorNameListener = new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				updateComponentsEnablement();
 			}
 		};
 		name = initializeTextField(connectorGroupBox, "Name: ",
 				DEFAULT_GROUP_ID, connectorNameListener);
 
 		name.addModifyListener(new ModifyListener() {
 
 			public void modifyText(ModifyEvent e) {
 				if (name.getText().isEmpty()) {
 					artifactId.setText(DEFAULT_ARTIFACT_ID);
 				} else {
 					artifactId
 							.setText(name.getText().toLowerCase() + "-module");
 				}
 				model.setConnectorName(name.getText());
 				dialogChanged();
 			}
 		});
 
 		Group mavenGroupBox = UiUtils.createGroupWithTitle(container,
 				GROUP_TITLE_MAVEN_SETTINGS, 2);
 		initializeCheckBox(mavenGroupBox);
 
 		ModifyListener groupIdListener = new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				updateComponentsEnablement();
 			}
 		};
 		ModifyListener artifactIdListener = new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				updateComponentsEnablement();
 			}
 		};
 		ModifyListener versionListener = new ModifyListener() {
 
 			@Override
 			public void modifyText(ModifyEvent e) {
 				updateComponentsEnablement();
 			}
 		};
 		groupId = initializeTextField(mavenGroupBox, "Group Id: ",
 				DEFAULT_GROUP_ID, groupIdListener);
 		artifactId = initializeTextField(mavenGroupBox, "Artifact Id: ",
 				DEFAULT_ARTIFACT_ID, artifactIdListener);
 		version = initializeTextField(mavenGroupBox, "Version: ",
 				DEFAULT_VERSION, versionListener);
 
 		mavenGroupBox.layout();
 
 		devkitVersion = DevkitUtils.devkitVersions[0];
 
 		
 		ServerChooserComponent serverChooserComponent = new ServerChooserComponent(
 				"Please select a Runtime");
 		serverChooserComponent.createControl(container);
 		serverChooserComponent.setServerDefinition(selectedServerDefinition);
 		serverChooserComponent.setStatusHandler(new PartStatusHandler(){
 
 			@Override
 			public void clearErrors(WizardPagePartExtension part) {
 				
 			}
 
 			@Override
 			public void setErrorMessage(WizardPagePartExtension part,
 					String message) {
 				
 			}
 
 			@Override
 			public void notifyUpdate(WizardPagePartExtension part, String key,
 					Object value) {
 				if (ServerChooserComponent.KEY_SERVER_DEFINITION.equals(key)) {
                     selectedServerDefinition=(ServerDefinition) value;
                 }
 			}
 
 			@Override
 			public void setPartComplete(WizardPagePartExtension part,
 					boolean isComplete) {
 				
 			}
 			
 		});
 				GridLayoutFactory.fillDefaults().numColumns(1)
 				.extendedMargins(2, 2, 10, 0).margins(0, 0).spacing(0, 0)
 				.applyTo(container);
 		GridDataFactory.fillDefaults().indent(0, 0).applyTo(container);
 
 		initialize();
 		dialogChanged();
 		setControl(container);
 	}
 
 	private void initialize() {
 		groupId.setText(DEFAULT_GROUP_ID);
 		artifactId.setText(DEFAULT_ARTIFACT_ID);
 		version.setText(DEFAULT_VERSION);
 		name.setText(DEFAULT_NAME);
 
 	}
 
 	private void dialogChanged() {
 
 		String version = getVersion();
 
 		if (version.length() == 0) {
 			updateStatus("Version must be specified");
 			return;
 		}
 
 		if (this.getName().length() == 0) {
 			updateStatus("Name must be specified");
 			return;
 		}else if(!connectorName.matcher(this.getName()).matches()){
 			updateStatus("Name can only contains letters");
 			return;	
 		}
 		
 		updateStatus(null);
 	}
 
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 
 	public String getGroupId() {
 		return groupId.getText();
 	}
 
 	public String getArtifactId() {
 		return artifactId.getText();
 	}
 
 	public String getVersion() {
 		return version.getText();
 	}
 
 	public String getDevkitVersion() {
 		return getDevkitVersion(this.selectedServerDefinition);
 	}
 
 	public String getPackage() {
 		return groupId.getText() + "." + name.getText().toLowerCase();
 	}
 
 	public String getName() {
 		return name.getText();
 	}
 
 	public String getCategory() {
 		return connectorCategory;
 	}
 
 	private void initializeCheckBox(Composite parent) {
 		cbCreatePomCheckbox = new Button(parent, SWT.CHECK);
 		cbCreatePomCheckbox.setSelection(false);
 		cbCreatePomCheckbox.setText(" " + CREATE_POM_LABEL);
 		cbCreatePomCheckbox.setLayoutData(GridDataFactory.swtDefaults()
 				.span(2, 1).create());
 		cbCreatePomCheckbox.addSelectionListener(new SelectionListener() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				updateComponentsEnablement();
 			}
 
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 				updateComponentsEnablement();
 			}
 		});
 		cbCreatePomCheckbox.setSelection(false);
 	}
 
 	private Text initializeTextField(Group groupBox, String labelText,
 			String defaultValue, ModifyListener modifyListener) {
 		Label label = new Label(groupBox, SWT.NULL);
 		label.setText(labelText);
 		label.setLayoutData(GridDataFactory.swtDefaults()
 				.align(SWT.BEGINNING, SWT.CENTER)
 				.hint(MuleUiConstants.LABEL_WIDTH, SWT.DEFAULT).create());
 		Text textField = new Text(groupBox, SWT.BORDER);
 		textField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		textField.setText(defaultValue);
 		textField.addModifyListener(modifyListener);
 		return textField;
 	}
 
 	private void updateComponentsEnablement() {
 		boolean createPom = cbCreatePomCheckbox.getSelection();
 		artifactId.setEnabled(createPom);
 		groupId.setEnabled(createPom);
 		version.setEnabled(createPom);
 	}
 	
 	private String getDevkitVersion(
 			ServerDefinition selectedServerDefinition) {
 		if (selectedServerDefinition.getId().contains("3.4.2"))
 			return "3.4.2";
 		if (selectedServerDefinition.getId().contains("3.4.1"))
 			return "3.4.1";
 		if (selectedServerDefinition.getId().contains("3.4.0"))
 			return "3.4.0";
 		return "3.5.0-cascade";
 	}
 }
