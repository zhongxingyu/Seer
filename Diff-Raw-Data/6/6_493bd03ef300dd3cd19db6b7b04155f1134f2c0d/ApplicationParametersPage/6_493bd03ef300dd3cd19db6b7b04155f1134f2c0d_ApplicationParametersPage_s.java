 package org.zend.php.zendserver.deployment.ui.wizards;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 
 import org.eclipse.jface.preference.PreferenceDialog;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.php.internal.server.core.Server;
 import org.eclipse.php.internal.server.core.manager.ServersManager;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.ScrolledComposite;
 import org.eclipse.swt.events.ControlAdapter;
 import org.eclipse.swt.events.ControlEvent;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.PreferencesUtil;
 import org.zend.php.zendserver.deployment.core.descriptor.IDescriptorContainer;
 import org.zend.php.zendserver.deployment.core.descriptor.IParameter;
 import org.zend.php.zendserver.deployment.core.descriptor.ParameterType;
 
 
 public class ApplicationParametersPage extends WizardPage {
 
 	private IDescriptorContainer model;
 	private Combo deployCombo;
 	private Link targetLocation;
 	private Text baseURL;
 
 	private List<DeploymentParameter> parameters;
 	private List<Server> serverList;
 
 	private class DeploymentParameter {
 
 		private Control control;
 		private IParameter parameter;
 		private String name;
 		private ParameterType type;
 
 		private DeploymentParameter(IParameter parameter, String name,
 				ParameterType type) {
 			this.parameter = parameter;
 			this.name = name;
 			this.type = type;
 		}
 
 		public String getValue() {
 			switch (type) {
 			case STRING:
 			case EMAIL:
 			case PASSWORD:
 				return ((Text) control).getText();
 			case CHOICE:
 				return ((Combo) control).getText();
 			case CHECKBOX:
 				return String.valueOf(((Button) control).getSelection());
 			case UNKNOWN:
 			default:
 				return "";
 			}
 		}
 
 		public IParameter getParameter() {
 			return parameter;
 		}
 
 		public String getId() {
 			return parameter.getId();
 		}
 
 		public void createControl(Composite composite) {
 			String tooltip = parameter.getLongDescription();
 			if (tooltip == null) {
 				tooltip = parameter.getDescription();
 			}
 			switch (type) {
 			case STRING:
 			case EMAIL:
 				control = createLabelWithText(name, tooltip, composite);
 				if (parameter.getDefaultValue() != null) {
 					((Text) control).setText(parameter.getDefaultValue());
 				}
 				break;
 			case PASSWORD:
 				control = createLabelWithText(name, tooltip, composite);
 				((Text) control).setEchoChar('*');
 				if (parameter.getDefaultValue() != null) {
 					((Text) control).setText(parameter.getDefaultValue());
 				}
 				break;
 			case CHOICE:
 				control = createLabelWithCombo(name, tooltip, composite);
 				String[] values = parameter.getValidValues();
 				((Combo) control).setItems(values);
 				break;
 			case CHECKBOX:
 				control = createLabelWithCheckbox(name, tooltip, composite);
 				if (parameter.getDefaultValue() != null) {
 					((Button) control).setSelection(Boolean.valueOf(parameter
 							.getDefaultValue()));
 				}
 				break;
 			case UNKNOWN:
 			default:
 				break;
 			}
 		}
 	}
 
 	protected ApplicationParametersPage(IDescriptorContainer model) {
 		super(Messages.parametersPage_Title);
 		this.model = model;
 		this.parameters = new ArrayList<DeploymentParameter>();
 		setDescription(Messages.deployWizardPage_Description);
 		setTitle(Messages.parametersPage_Title);
 	}
 
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout(2, false);
 		container.setLayout(layout);
 		createDeployCombo(container);
 		createLocationLink(container);
 		baseURL = createLabelWithText(Messages.parametersPage_baseURL, "",
 				container);
 		createParameterGroups(container);
 		setControl(container);
 		setPageComplete(false);
 		validatePage();
 	}
 
 	public String getBaseURL() {
 		return baseURL.getText();
 	}
 
 	public Server getTargetLocation() {
 		String selectedLocation = deployCombo.getText();
 		for (Server server : serverList) {
 			if (server.getName().equals(selectedLocation)) {
 				return server;
 			}
 		}
 		return null;
 	}
 
 	public HashMap<String, String> getParameters() {
 		HashMap<String, String> result = new HashMap<String, String>();
 		for (DeploymentParameter param : parameters) {
 			result.put(param.getId(), param.getValue());
 		}
 		return result;
 	}
 
 	private void createParameterGroups(Composite container) {
 		Group paramsGroup = new Group(container, SWT.NULL);
 		paramsGroup.setText(Messages.parametersPage_applicationParams);
 		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gd.horizontalSpan = 2;
 		paramsGroup.setLayoutData(gd);
 		paramsGroup.setLayout(new FillLayout(SWT.FILL));
 		if (model.getDescriptorModel() != null) {
 			final ScrolledComposite scrollComposite = new ScrolledComposite(
 					paramsGroup, SWT.V_SCROLL);
 			scrollComposite.setLayout(new FillLayout());
 			final Composite parent = new Composite(scrollComposite, SWT.NONE);
			GridLayout layout = new GridLayout(1, false);
 			parent.setLayout(layout);
 			scrollComposite.setExpandVertical(true);
 			scrollComposite.setExpandHorizontal(true);
 			scrollComposite.setContent(parent);
 			scrollComposite.addControlListener(new ControlAdapter() {
 				public void controlResized(ControlEvent e) {
 					Rectangle r = scrollComposite.getClientArea();
 					scrollComposite.setMinSize(parent.computeSize(r.width,
 							SWT.DEFAULT));
 				}
 			});
 			List<ParametersCategory> categories = createCategories(model
 					.getDescriptorModel().getParameters());
 			for (ParametersCategory category : categories) {
 				createGroup(category, parent);
 			}
 		}
 	}
 
 	private void createGroup(ParametersCategory category, Composite parent) {
 		if (category.getParameter() == null) {
 			Group paramsGroup = new Group(parent, SWT.NULL);
 			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 			gd.horizontalSpan = 2;
 			paramsGroup.setLayoutData(gd);
 			paramsGroup.setText(category.getName());
 			GridLayout layout = new GridLayout(2, true);
 			paramsGroup.setLayout(layout);
 			List<ParametersCategory> categories = category.getCategories();
 			for (ParametersCategory cat : categories) {
 				createGroup(cat, paramsGroup);
 			}
 		} else {
 			DeploymentParameter parameter = null;
 			ParameterType type = ParameterType.byName(category.getParameter()
 					.getType());
 			if (type != ParameterType.UNKNOWN) {
 				parameter = new DeploymentParameter(category.getParameter(),
 						category.getName(), type);
 				parameter.createControl(parent);
 				parameters.add(parameter);
 			}
 		}
 	}
 
 	private List<ParametersCategory> createCategories(List<IParameter> params) {
 		List<ParametersCategory> categories = new ArrayList<ParametersCategory>();
 		for (IParameter param : params) {
 			List<String> labels = new ArrayList<String>(Arrays.asList(param
 					.getDisplay().split("\\.")));
 			ParametersCategory cat = createCategory(categories, labels, param);
 			if (cat != null) {
 				categories.add(cat);
 			}
 		}
 		return categories;
 	}
 
 	private ParametersCategory createCategory(
 			List<ParametersCategory> categories, List<String> labels,
 			IParameter dp) {
 		if (labels.size() > 1) {
 			String catName = labels.remove(0);
 			for (ParametersCategory category : categories) {
 				if (category.getName().equals(catName)) {
 					ParametersCategory cat = createCategory(
 							category.getCategories(), labels, dp);
 					if (cat != null) {
 						category.addCategory(cat);
 					}
 					return null;
 				}
 			}
 			ParametersCategory cat = new ParametersCategory(catName);
 			cat.addCategory(createCategory(categories, labels, dp));
 			return cat;
 		} else {
 			String catName = labels.remove(0);
 			return new ParametersCategory(dp, catName);
 		}
 	}
 
 	private void createLocationLink(Composite container) {
 		targetLocation = new Link(container, SWT.NONE);
 		String text = "<a>" + Messages.parametersPage_TargetLocation + "</a>";
 		targetLocation.setText(text);
 		targetLocation.addListener(SWT.Selection, new Listener() {
 			public void handleEvent(Event event) {
 				Server prevSelection = getTargetLocation();
 				PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
 						event.display.getActiveShell(),
 						"org.eclipse.php.server.internal.ui.PHPServersPreferencePage",
 						null, null);
 				if (dialog.open() == Window.OK) {
 					populateLocationList();
 					for (int i = 0; i < serverList.size(); i++) {
 						if (prevSelection == serverList.get(i)) {
 							deployCombo.select(i);
 						}
 					}
 					validatePage();
 				}
 			}
 		});
 		GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		gd.horizontalSpan = 2;
 		targetLocation.setLayoutData(gd);
 	}
 
 	private Combo createLabelWithCombo(String labelText, String tooltip,
 			Composite container) {
 		Label label = new Label(container, SWT.NULL);
 		label.setText(labelText);
 		label.setToolTipText(tooltip);
 		Combo combo = new Combo(container, SWT.SIMPLE | SWT.DROP_DOWN
 				| SWT.READ_ONLY);
 		combo.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				validatePage();
 			}
 		});
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		combo.setLayoutData(gd);
 		return combo;
 	}
 
 	private Text createLabelWithText(String labelText, String tooltip,
 			Composite container) {
 		Label label = new Label(container, SWT.NULL);
 		label.setText(labelText);
 		label.setToolTipText(tooltip);
 		Text text = new Text(container, SWT.BORDER | SWT.SINGLE);
 		text.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				validatePage();
 			}
 		});
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		text.setLayoutData(gd);
 		return text;
 	}
 
 	private Button createLabelWithCheckbox(String desc, String tooltip,
 			Composite composite) {
 		Button button = new Button(composite, SWT.CHECK);
 		button.setText(desc);
 		button.setToolTipText(tooltip);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 2;
 		button.setLayoutData(gd);
 		return button;
 	}
 
 	private void createDeployCombo(Composite container) {
 		deployCombo = createLabelWithCombo(Messages.parametersPage_DeployTo,
 				"", container);
 		populateLocationList();
 	}
 
 	private void populateLocationList() {
 		Server[] servers = ServersManager.getServers();
 		if (serverList == null) {
 			serverList = new ArrayList<Server>();
 		}
 		serverList.clear();
 		if (servers != null) {
 			int size = servers.length;
 			for (int i = 0; i < size; i++) {
 				serverList.add(servers[i]);
 			}
 		}
 		deployCombo.removeAll();
 		if (!serverList.isEmpty()) {
 			for (Server server : serverList) {
 				deployCombo.add(server.getName());
 			}
 		}
 		if (deployCombo.getItemCount() > 0) {
 			deployCombo.select(-1);
 		}
 	}
 
 	private void validatePage() {
 		setErrorMessage(null);
 		setMessage(null);
 		if (getTargetLocation() == null) {
 			setErrorMessage(Messages.parametersPage_ValidationError_TargetLocation);
 			setPageComplete(false);
 			return;
 		}
 		if (!baseURL.getText().isEmpty()) {
 			for (DeploymentParameter param : parameters) {
 				if (param.getParameter().isRequired()
 						&& param.getValue().isEmpty()) {
					setErrorMessage("Not all required parameters have values");
 					setPageComplete(false);
 					return;
 				}
 			}
 			setPageComplete(true);
 		} else {
 			setErrorMessage(Messages.parametersPage_ValidationError_BaseUrl);
 			setPageComplete(false);
 		}
 
 	}
 }
