 package com.schmeedy.pdt.joomla.ui.preferences;
 
 import java.io.File;
 
 import org.eclipse.core.databinding.AggregateValidationStatus;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.conversion.Converter;
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.databinding.validation.IValidator;
 import org.eclipse.core.databinding.validation.ValidationStatus;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.emf.databinding.EMFObservables;
 import org.eclipse.emf.databinding.EMFProperties;
 import org.eclipse.emf.databinding.FeaturePath;
 import org.eclipse.jface.databinding.swt.SWTObservables;
 import org.eclipse.jface.databinding.viewers.ViewersObservables;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.jface.viewers.ArrayContentProvider;
 import org.eclipse.jface.viewers.ComboViewer;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.DirectoryDialog;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import com.schmeedy.pdt.joomla.core.server.JoomlaInstallationValidator;
 import com.schmeedy.pdt.joomla.core.server.JoomlaInstallationValidator.ValidationStatusAndVersionInfo;
 import com.schmeedy.pdt.joomla.core.server.cfg.JoomlaServerConfigurationPackage.Literals;
 import com.schmeedy.pdt.joomla.core.server.cfg.LocalJoomlaServer;
 import com.schmeedy.pdt.joomla.core.server.cfg.MajorJoomlaVersion;
 
 public class EditLocalJoomlaServerDialog extends TitleAreaDialog {
 
 	private final boolean edit;
 	private final String initialMessage;
 	private final LocalJoomlaServer serverConfiguration;
 	
 	private Text installRootText;
 	private Text nameText;
 	private Text baseUrlText;
 	private Text exactVersionText;
 	
 	private DataBindingContext dataBindingContext;
 	private Text adminUsernameText;
 	private Text adminPasswordText;
 	private Text teamIdText;
 	private ComboViewer versionFamilyComboViewer;
 	
 	private boolean manuallyConfiguredVersion = false;
 
 	public EditLocalJoomlaServerDialog(Shell parent, LocalJoomlaServer serverConfiguration, boolean edit) {
 		super(parent);
 		this.edit = edit;
 		this.initialMessage = edit ? "Change Joomla! server configuration properties." : "New Joomla! server configuration.";
 		this.serverConfiguration = serverConfiguration;
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 		setHelpAvailable(false);
 	}
 	
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		final Composite dialogComposite = (Composite) super.createDialogArea(parent);
 		final GridLayout gl_dialogComposite = new GridLayout(1, false);
 		gl_dialogComposite.verticalSpacing = 0;
 		gl_dialogComposite.marginWidth = 0;
 		gl_dialogComposite.marginHeight = 0;
 		dialogComposite.setLayout(gl_dialogComposite);
 		
 		if (edit) {
 			setTitle("Edit Joomla! Server Configuration");
 			getShell().setText("Edit Joomla! Server");
 		} else {
 			setTitle("New Joomla! Server Configuration");
 			getShell().setText("New Joomla! Server");
 		}
 		setMessage(initialMessage);
 		
 		final Composite installDirSelectionComposite = new Composite(dialogComposite, SWT.NONE);
 		final GridLayout gl_installDirSelectionComposite = new GridLayout(3, false);
 		installDirSelectionComposite.setLayout(gl_installDirSelectionComposite);
 		installDirSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));
 		
 		final Label lblJoomlaRoot = new Label(installDirSelectionComposite, SWT.NONE);
 		lblJoomlaRoot.setText("Joomla! Root:");
 		
 		installRootText = new Text(installDirSelectionComposite, SWT.BORDER);
 		installRootText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		
 		final Button browseButton = new Button(installDirSelectionComposite, SWT.NONE);
 		final GridData gd_browseButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		gd_browseButton.widthHint = 90;
 		browseButton.setLayoutData(gd_browseButton);
 		browseButton.setText("Browse...");
 		browseButton.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				final DirectoryDialog dirDialog = new DirectoryDialog(getShell());
 				final String directory = dirDialog.open();
 				if (directory != null) {
 					installRootText.setText(directory);
 				}
 			}
 		});
 		
 		final Composite basicPropertiesGroupComposite = new Composite(dialogComposite, SWT.NONE);
 		basicPropertiesGroupComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		final GridLayout gl_basicPropertiesGroupComposite = new GridLayout(1, false);
 		gl_basicPropertiesGroupComposite.marginHeight = 0;
 		basicPropertiesGroupComposite.setLayout(gl_basicPropertiesGroupComposite);
 		
 		final Group serverPropertiesGroup = new Group(basicPropertiesGroupComposite, SWT.NONE);
 		serverPropertiesGroup.setText("Properties");
 		final GridLayout gl_serverPropertiesGroup = new GridLayout(2, false);
 		serverPropertiesGroup.setLayout(gl_serverPropertiesGroup);
 		serverPropertiesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
 		
 		final Label lblName = new Label(serverPropertiesGroup, SWT.NONE);
 		lblName.setText("Name:");
 		
 		nameText = new Text(serverPropertiesGroup, SWT.BORDER);
 		nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		
 		final Label lblBaseUrl = new Label(serverPropertiesGroup, SWT.NONE);
 		lblBaseUrl.setText("Base URL:");
 		
 		baseUrlText = new Text(serverPropertiesGroup, SWT.BORDER);
 		baseUrlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		
 		final Label lblAdminUsername_1 = new Label(serverPropertiesGroup, SWT.NONE);
 		lblAdminUsername_1.setText("Admin Username:");
 		
 		adminUsernameText = new Text(serverPropertiesGroup, SWT.BORDER);
 		adminUsernameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		
 		final Label lblAdminUsername = new Label(serverPropertiesGroup, SWT.NONE);
 		lblAdminUsername.setText("Admin Password:");
 		
 		adminPasswordText = new Text(serverPropertiesGroup, SWT.BORDER | SWT.PASSWORD);
 		adminPasswordText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		
 		final Label lblTeamId = new Label(serverPropertiesGroup, SWT.NONE);
 		lblTeamId.setText("Team ID:");
 		
 		teamIdText = new Text(serverPropertiesGroup, SWT.BORDER);
 		teamIdText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		
 		final Label lblVersionFamily_1 = new Label(serverPropertiesGroup, SWT.NONE);
 		lblVersionFamily_1.setText("Version Family:");
 		
 		versionFamilyComboViewer = new ComboViewer(serverPropertiesGroup, SWT.READ_ONLY);
 		final Combo versionFamilyCombo = versionFamilyComboViewer.getCombo();
 		versionFamilyCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		versionFamilyCombo.setEnabled(false);
 		versionFamilyComboViewer.setContentProvider(new ArrayContentProvider());
 		versionFamilyComboViewer.setInput(new MajorJoomlaVersion[] {MajorJoomlaVersion.ONE_FIVE, MajorJoomlaVersion.ONE_SIX});
 		versionFamilyComboViewer.addSelectionChangedListener(new ISelectionChangedListener() {
 			@Override
 			public void selectionChanged(SelectionChangedEvent event) {
 				if (versionFamilyCombo.isEnabled()) { // enabled when version auto-detection fails
 					final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
 					if (!selection.isEmpty()) {
 						final MajorJoomlaVersion majorVersion = (MajorJoomlaVersion) selection.getFirstElement();
 						serverConfiguration.setExactVersion(majorVersion.getLiteral().replace('x', '0'));
 						manuallyConfiguredVersion = true;
						dataBindingContext.updateModels(); // trigger validation
 					}
 				}
 			}
 		});
 		
 		final Label lblExactVersion = new Label(serverPropertiesGroup, SWT.NONE);
 		lblExactVersion.setText("Exact Version:");
 		
 		exactVersionText = new Text(serverPropertiesGroup, SWT.BORDER);
 		exactVersionText.setEnabled(false);
 		exactVersionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		
 		dataBindingContext = initDataBindings();
 
 		final WritableValue statusObservable = new WritableValue();
 		statusObservable.addChangeListener(new IChangeListener() {
 			@Override
 			public void handleChange(ChangeEvent event) {
 				final IStatus status = AggregateValidationStatus.getStatusMaxSeverity(dataBindingContext.getBindings());
 				if (status.isOK()) {
 					setMessage(initialMessage);
 				} else {
 					setMessage(status.getMessage(), getMessageType(status));
 				}
 				final Button okButton = getButton(IDialogConstants.OK_ID);
 				if (okButton != null) {
 					okButton.setEnabled(status.isOK());
 				}
 			}
 			
 			private int getMessageType(IStatus status) {
 				switch (status.getSeverity()) {
 					case IStatus.INFO:
 						return IMessageProvider.INFORMATION;
 					case IStatus.WARNING:
 						return IMessageProvider.WARNING;
 					case IStatus.ERROR:
 						return IMessageProvider.ERROR;
 					default:
 						return IMessageProvider.NONE;
 				}
 			}
 		});
 		dataBindingContext.bindValue(statusObservable, new AggregateValidationStatus(dataBindingContext, AggregateValidationStatus.MAX_SEVERITY));
 		
 		applyDialogFont(dialogComposite);
 		return dialogComposite;
 	}
 	
 	@Override
 	protected Control createButtonBar(Composite parent) {
 		final Control buttonBarControl = super.createButtonBar(parent);
 		dataBindingContext.updateModels(); // trigger validation
 		return buttonBarControl;
 	}
 	
 	@Override
 	public boolean close() {
 		dataBindingContext.dispose();
 		return super.close();
 	}
 	
 	public class JoomlaInstallDirValidator implements IValidator {
 		private final JoomlaInstallationValidator joomlaInstallationValidator = new JoomlaInstallationValidator();
 		
 		@Override
 		public IStatus validate(Object value) {
 			if (value == null || ((String) value).trim().length() == 0) {
 				return ValidationStatus.info("Select location of Joomla! installation directory.");
 			}
 			final String stringValue = (String) value;
 			final File file = new File(stringValue);
 			if (!file.exists()) {
 				clearVersionInfo();
 				return ValidationStatus.error("Given Joomla! root directory does not exist.");
 			}
 			final ValidationStatusAndVersionInfo statusAndInfo = joomlaInstallationValidator.validate(file);
 			if (statusAndInfo.getStatus().isOK()) {
 				serverConfiguration.setMajorVersion(statusAndInfo.getMajorVersion());
 				serverConfiguration.setExactVersion(statusAndInfo.getExactVersion());
 				versionFamilyComboViewer.getCombo().setEnabled(false);
 			} else if (statusAndInfo.getStatus().getSeverity() < IStatus.ERROR) {
 				versionFamilyComboViewer.getCombo().setEnabled(true);
 				manuallyConfiguredVersion = !versionFamilyComboViewer.getSelection().isEmpty();
 			}
 			return manuallyConfiguredVersion ? Status.OK_STATUS : statusAndInfo.getStatus();
 		}
 
 		private void clearVersionInfo() {
 			manuallyConfiguredVersion = false;
 			serverConfiguration.setMajorVersion(null);
 			serverConfiguration.setExactVersion(null);
 			versionFamilyComboViewer.getCombo().setEnabled(false);
 		}
 	}
 	
 	public class NonEmptyStringValidator implements IValidator {
 		private final String errorMessage;
 		private final String pleaseEnterMessage;
 		
 		public NonEmptyStringValidator(String errorMessage) {
 			this(errorMessage, null);
 		}
 		
 		public NonEmptyStringValidator(String errorMessage, String pleaseEnterMessage) {
 			this.errorMessage = errorMessage;
 			this.pleaseEnterMessage = pleaseEnterMessage;
 		}
 
 		@Override
 		public IStatus validate(Object value) {
 			final IStatus errorStatus = !edit && pleaseEnterMessage != null ? ValidationStatus.info(errorMessage) : ValidationStatus.error(errorMessage);
 			if (value == null) {
 				return errorStatus;
 			}
 			final String stringVal = (String) value;
 			return stringVal.trim().length() == 0 ? errorStatus : Status.OK_STATUS;
 		}
 	}
 	
 	public static class VersionFamilyConverter extends Converter {
 		public VersionFamilyConverter() {
 			super(MajorJoomlaVersion.class, String.class);
 		}
 
 		@Override
 		public Object convert(Object fromObject) {
 			if (fromObject == null) {
 				return null;
 			}
 			final MajorJoomlaVersion majorJoomlaVersion = (MajorJoomlaVersion) fromObject;
 			switch (majorJoomlaVersion) {
 				case ONE_FIVE:
 					return "Joomla! 1.5.x";
 				case ONE_SIX:
 					return "Joomla! 1.6.x";
 				default:
 					return "unknown";
 			}
 		}
 	}
 	protected DataBindingContext initDataBindings() {
 		final DataBindingContext bindingContext = new DataBindingContext();
 		//
 		final IObservableValue installRootTextObserveTextObserveWidget = SWTObservables.observeText(installRootText, SWT.Modify);
 		final IObservableValue serverConfigurationInstallDirObserveValue = EMFObservables.observeValue(serverConfiguration, Literals.LOCAL_JOOMLA_SERVER__INSTALL_DIR);
 		final UpdateValueStrategy strategy_1 = new UpdateValueStrategy();
 		strategy_1.setAfterConvertValidator(new JoomlaInstallDirValidator());
 		bindingContext.bindValue(installRootTextObserveTextObserveWidget, serverConfigurationInstallDirObserveValue, strategy_1, null);
 		//
 		final IObservableValue nameTextObserveTextObserveWidget = SWTObservables.observeText(nameText, SWT.Modify);
 		final IObservableValue serverConfigurationNameObserveValue = EMFObservables.observeValue(serverConfiguration, Literals.LOCAL_JOOMLA_SERVER__NAME);
 		final UpdateValueStrategy strategy = new UpdateValueStrategy();
 		strategy.setAfterConvertValidator(new NonEmptyStringValidator("Server name cannot be empty."));
 		bindingContext.bindValue(nameTextObserveTextObserveWidget, serverConfigurationNameObserveValue, strategy, null);
 		//
 		final IObservableValue baseUrlTextObserveTextObserveWidget = SWTObservables.observeText(baseUrlText, SWT.Modify);
 		final IObservableValue serverConfigurationBaseUrlObserveValue = EMFObservables.observeValue(serverConfiguration, Literals.LOCAL_JOOMLA_SERVER__BASE_URL);
 		final UpdateValueStrategy strategy_2 = new UpdateValueStrategy();
 		strategy_2.setAfterConvertValidator(new NonEmptyStringValidator("Server base URL cannot be empty."));
 		bindingContext.bindValue(baseUrlTextObserveTextObserveWidget, serverConfigurationBaseUrlObserveValue, strategy_2, null);
 		//
 		final IObservableValue exactVersionTextObserveTextObserveWidget = SWTObservables.observeText(exactVersionText, SWT.Modify);
 		final IObservableValue serverConfigurationExactVersionObserveValue = EMFObservables.observeValue(serverConfiguration, Literals.LOCAL_JOOMLA_SERVER__EXACT_VERSION);
 		bindingContext.bindValue(exactVersionTextObserveTextObserveWidget, serverConfigurationExactVersionObserveValue, new UpdateValueStrategy(UpdateValueStrategy.POLICY_NEVER), null);
 		//
 		final IObservableValue adminUsernameTextObserveTextObserveWidget = SWTObservables.observeText(adminUsernameText, SWT.Modify);
 		final IObservableValue serverConfigurationUsernameObserveValue = EMFProperties.value(FeaturePath.fromList(Literals.LOCAL_JOOMLA_SERVER__ADMIN_USER_CREDENTIALS, Literals.USER_CREDENTIALS__USERNAME)).observe(serverConfiguration);
 		final UpdateValueStrategy strategy_4 = new UpdateValueStrategy();
 		strategy_4.setAfterConvertValidator(new NonEmptyStringValidator("Admin username cannot be empty.", "Please enter admin user's login name."));
 		bindingContext.bindValue(adminUsernameTextObserveTextObserveWidget, serverConfigurationUsernameObserveValue, strategy_4, null);
 		//
 		final IObservableValue adminPasswordTextObserveTextObserveWidget = SWTObservables.observeText(adminPasswordText, SWT.Modify);
 		final IObservableValue serverConfigurationPasswordObserveValue = EMFProperties.value(FeaturePath.fromList(Literals.LOCAL_JOOMLA_SERVER__ADMIN_USER_CREDENTIALS, Literals.USER_CREDENTIALS__PASSWORD)).observe(serverConfiguration);
 		final UpdateValueStrategy strategy_5 = new UpdateValueStrategy();
 		strategy_5.setAfterConvertValidator(new NonEmptyStringValidator("Admin password cannot be empty.", "Please enter admin user's password."));
 		bindingContext.bindValue(adminPasswordTextObserveTextObserveWidget, serverConfigurationPasswordObserveValue, strategy_5, null);
 		//
 		final IObservableValue teamIdTextObserveTextObserveWidget = SWTObservables.observeText(teamIdText, SWT.Modify);
 		final IObservableValue serverConfigurationTeamIdObserveValue = EMFObservables.observeValue(serverConfiguration, Literals.LOCAL_JOOMLA_SERVER__TEAM_ID);
 		final UpdateValueStrategy strategy_6 = new UpdateValueStrategy();
 		strategy_6.setAfterConvertValidator(new NonEmptyStringValidator("Team ID cannot be empty."));
 		bindingContext.bindValue(teamIdTextObserveTextObserveWidget, serverConfigurationTeamIdObserveValue, strategy_6, null);
 		//
 		final IObservableValue comboViewerObserveSingleSelection = ViewersObservables.observeSingleSelection(versionFamilyComboViewer);
 		final IObservableValue serverConfigurationMajorVersionObserveValue = EMFObservables.observeValue(serverConfiguration, Literals.LOCAL_JOOMLA_SERVER__MAJOR_VERSION);
 		bindingContext.bindValue(comboViewerObserveSingleSelection, serverConfigurationMajorVersionObserveValue, null, null);
 		//
 		return bindingContext;
 	}
 }
