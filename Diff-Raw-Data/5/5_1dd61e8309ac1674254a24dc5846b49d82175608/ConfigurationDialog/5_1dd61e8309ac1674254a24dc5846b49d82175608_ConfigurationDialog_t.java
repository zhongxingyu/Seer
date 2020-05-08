 package com.zombietank.jankytray;
 
 import java.io.IOException;
 
 import javax.inject.Inject;
 
 import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.dialogs.TitleAreaDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 import com.zombietank.support.IntegerBuilder;
 import com.zombietank.support.URLBuilder;
 import com.zombietank.swt.ImageRegistryWrapper;
 
 @Component
 public class ConfigurationDialog extends TitleAreaDialog {
 	private static final Logger logger = LoggerFactory.getLogger(ConfigurationDialog.class);
 	private final JankyOptions options;
 	private final ImageRegistryWrapper imageRegistry;
 	private Text jenkinsUrlText;
 	private Text pollingIntervalText;
 	
 	@Inject
 	public ConfigurationDialog(Shell parentShell, ImageRegistryWrapper imageRegistry, JankyOptions options) {
 		super(parentShell);
 		this.imageRegistry = imageRegistry;
 		this.options = options;
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText("Jenkins System Tray Settings");
 		newShell.setSize(500, 210);
 		newShell.setImage(imageRegistry.get(JankyConfiguration.SETTINGS_ICON));
 	}	
 	
 	@Override
 	public void create() {
 		super.create();
 		setTitle("Settings");
 		clearMessages();
 		setMessage("Provide a URL for either your dashboard or a view.", IMessageProvider.INFORMATION);
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		GridLayout layout = new GridLayout();
 		layout.numColumns = 2;
 		parent.setLayout(layout);
 		
 		Label label2 = new Label(parent, SWT.NONE);
 		label2.setText("Jenkins URL:");
 
 		GridData gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 		jenkinsUrlText = new Text(parent, SWT.BORDER);
 		jenkinsUrlText.setLayoutData(gridData);
 		jenkinsUrlText.setText(options.hasJenkinsUrl() ? options.getJenkinsUrl().toExternalForm() : "");
 
 		gridData = new GridData();
 		gridData.grabExcessHorizontalSpace = true;
 		gridData.horizontalAlignment = GridData.FILL;
 
 		Label label1 = new Label(parent, SWT.NONE);
 		label1.setText("Polling Interval (seconds):");
 
 		pollingIntervalText = new Text(parent, SWT.BORDER);
 		pollingIntervalText.setLayoutData(gridData);
 		pollingIntervalText.setText(Integer.toString(options.getPollingInterval()));
 		return parent;
 	}
 	
 	private boolean isValidInput() {
 		boolean valid = true;
 		
 		if (!IntegerBuilder.forInput(pollingIntervalText.getText()).isValid()) {
 			setErrorMessage("Please provide a valid polling interval.");
 			valid = false;
 		}
 		
 		if (!URLBuilder.forInput(jenkinsUrlText.getText()).isValid()) {
 			setErrorMessage("Please provide a valid jenkins url.");
 			valid = false;
 		}
 		return valid;
 	}
 
 	@Override
 	protected void okPressed() {
 		if(isValidInput()) {
 			saveInput();
 			super.okPressed();
 		}
 	}
 	
 	private void saveInput() {
 		options.setJenkinsUrl(URLBuilder.forInput(jenkinsUrlText.getText()).build());
 		options.setPollingInterval(IntegerBuilder.forInput(pollingIntervalText.getText()).build());
 		try {
 			options.save();
 		} catch (IOException e) {
			MessageDialog.openError(getShell(), "Unable to save settings", "Error saving settings, please check the log for more details.  If the problem persists, delete preferences.ini and restart the application.");
			logger.error("Unable to save settings.", e);
 		}
 	}
 	
 	private void clearMessages() {
 		setMessage(null);
 		setErrorMessage(null);
 	}
 }
