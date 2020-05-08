 /*******************************************************************************
  * Copyright (c) 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.php.zendserver.deployment.ui.wizards;
 
 import java.text.MessageFormat;
 
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.program.Program;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Link;
 import org.eclipse.swt.widgets.Text;
 import org.zend.sdklib.internal.target.OpenShiftTarget;
 
 /**
  * Target attributes page.
  * 
  * @author Wojciech Galanciak, 2012
  * 
  */
 public class OpenShiftTargetPage extends WizardPage {
 
 	private Text targetNameText;
 	// private TableViewer platformsViewer;
 	private Combo gearProfileCombo;
 	private OpenShiftTarget target;
 	private Button mySqlButton;
 
 	private OpenShiftTargetData data;
 	private Text zsPassword;
 	private Text zsConfirmPassword;
 
 	private boolean init;
 	private String description;
 
 	protected OpenShiftTargetPage(OpenShiftTargetWizard wizard,
 			OpenShiftTargetData data) {
 		super(Messages.OpenShiftTargetPage_PageTitle);
 		this.description = Messages.OpenShiftTargetPage_PageDescription;
 		setDescription(Messages.OpenShiftTargetPage_EnterNameMessage);
 		setTitle(Messages.OpenShiftTargetPage_PageTitle);
 		this.target = new OpenShiftTarget(wizard.getUsername(),
 				wizard.getPassword());
 		this.data = data;
 	}
 
 	protected OpenShiftTargetPage(OpenShiftTargetData data, String host,
 			boolean init) {
 		super(Messages.OpenShiftInitializationWizard_WizardTitle);
 		this.description = MessageFormat.format(
 				Messages.OpenShiftTargetPage_InitDescription, host);
 		setDescription(description);
 		setTitle(Messages.OpenShiftInitializationWizard_WizardTitle);
 		this.data = data;
 		this.init = init;
 	}
 
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout(2, false);
 		layout.horizontalSpacing = 0;
 		container.setLayout(layout);
 		if (!init) {
 			createTargetGroup(container);
 		}
 		createPasswordGroup(container);
 		// platformsViewer = createPlatformsSection(container);
 		setControl(container);
 		initializeValues();
 		setPageComplete(validatePage());
 	}
 
 	public void setVisible(boolean visible) {
 		super.setVisible(visible);
 		if (visible) {
			if (targetNameText != null) {
				targetNameText.setFocus();
			} else{
				zsPassword.setFocus();
			}
 		}
 	}
 
 	public void initializeValues() {
 		if (data.getGearProfiles() != null && data.getGearProfiles().size() > 0) {
 			Display.getDefault().asyncExec(new Runnable() {
 
 				public void run() {
 					for (String profile : data.getGearProfiles()) {
 						gearProfileCombo.add(profile);
 					}
 					gearProfileCombo.select(0);
 				}
 			});
 		}
 	}
 
 	public void updateData() {
 		if (targetNameText != null) {
 			data.setName(targetNameText.getText());
 		}
 		if (gearProfileCombo != null) {
 			data.setGearProfile(gearProfileCombo.getText());
 		}
 		if (mySqlButton != null) {
 			data.setMySQLSupport(mySqlButton.getSelection());
 		}
 		if (zsPassword != null) {
 			data.setPassword(zsPassword.getText());
 		}
 		if (zsConfirmPassword != null) {
 			data.setConfirmPassword(zsConfirmPassword.getText());
 		}
 		data.setTarget(target);
 	}
 
 	private boolean validatePage() {
 		setErrorMessage(null);
 		if (targetNameText != null) {
 			if (targetNameText.getText().isEmpty()) {
 				setMessage(Messages.OpenShiftTargetPage_EnterNameMessage);
 				return false;
 			}
 			String name = targetNameText.getText();
 			for (String existingTarget : data.getZendTargets()) {
 				if (existingTarget.equalsIgnoreCase(name)) {
 					setErrorMessage(Messages.OpenShiftTargetPage_SameTargetErrorMessage);
 					return false;
 				}
 			}
 		}
 		if (zsPassword != null && zsConfirmPassword != null) {
 			String password = zsPassword.getText().trim();
 			String confirmPassword = zsConfirmPassword.getText().trim();
 			if (password.isEmpty()) {
 				setMessage(Messages.OpenShiftTargetPage_EnterPasswordMessage);
 				return false;
 			}
 			if (password.indexOf(' ') > -1) {
 				setErrorMessage(Messages.OpenShiftTargetPage_ContainSpacesError);
 				return false;
 			}
 			if (password.length() > 20) {
 				setErrorMessage(Messages.OpenShiftTargetPage_TooLongError);
 				return false;
 			}
 			if (password.length() < 4) {
 				setErrorMessage(Messages.OpenShiftTargetPage_TooShortError);
 				return false;
 			}
 			if (confirmPassword.isEmpty()) {
 				setMessage(Messages.OpenShiftTargetPage_ConfirmationMessage);
 				return false;
 			}
 			if (!password.equals(confirmPassword)) {
 				setErrorMessage(Messages.OpenShiftTargetPage_DoNotMatchError);
 				return false;
 			}
 		}
 		setErrorMessage(null);
 		setMessage(description);
 		return true;
 	}
 
 	private void createTargetGroup(Composite container) {
 		Group targetGroup = new Group(container, SWT.NONE);
 		targetGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false,
 				2, 1));
 		targetGroup.setLayout(new GridLayout(2, false));
 		targetGroup.setText(Messages.OpenShiftTargetPage_TargetGroupLabel);
 		targetNameText = createLabelWithText(
 				Messages.OpenShiftTargetPage_TargetNameLabel, false,
 				targetGroup);
 		gearProfileCombo = createLabelWithCombo(
 				Messages.OpenShiftTargetPage_GearProfileLabel, targetGroup);
 		mySqlButton = createMySqlSection(targetGroup);
 	}
 
 	private void createPasswordGroup(Composite container) {
 		Group zendServerGroup = new Group(container, SWT.NONE);
 		zendServerGroup.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true,
 				false, 2, 1));
 		zendServerGroup.setLayout(new GridLayout(2, false));
 		zendServerGroup
 				.setText(Messages.OpenShiftTargetPage_ZendServerGroupLabel);
 		zsPassword = createLabelWithText(
 				Messages.OpenShiftTargetPage_PasswordLabel, true,
 				zendServerGroup);
 		zsPassword.setToolTipText(Messages.OpenShiftTargetPage_PasswordTooltip);
 		zsConfirmPassword = createLabelWithText(
 				Messages.OpenShiftTargetPage_ConfirmLabel, true,
 				zendServerGroup);
 		Link description = new Link(zendServerGroup, SWT.NONE);
 		description.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		description.setText(Messages.OpenShiftTargetPage_PasswordDescription);
 		description.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				Program.launch(Messages.OpenShiftTargetPage_12);
 			}
 		});
 	}
 
 	private Text createLabelWithText(String labelText, boolean isPassword,
 			Composite container) {
 		Composite parent = new Composite(container, SWT.NONE);
 		parent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		parent.setLayout(layout);
 		Label label = new Label(parent, SWT.NONE);
 		label.setText(labelText);
 		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true);
 		gd.widthHint = 100;
 		label.setLayoutData(gd);
 		int style = SWT.BORDER | SWT.SINGLE;
 		if (isPassword) {
 			style |= SWT.PASSWORD;
 		}
 		Text text = new Text(parent, style);
 		text.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				setPageComplete(validatePage());
 			}
 		});
 		text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		return text;
 	}
 
 	private Button createMySqlSection(Composite container) {
 		Label label = new Label(container, SWT.NONE);
 		GridData gd = new GridData(SWT.FILL, SWT.LEFT, false, false);
 		gd.widthHint = 100;
 		label.setLayoutData(gd);
 		Button mySqlButton = new Button(container, SWT.CHECK);
 		mySqlButton.setText(Messages.OpenShiftTargetPage_AddMySQLLabel);
 		mySqlButton
 				.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
 		mySqlButton.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				setPageComplete(validatePage());
 			}
 		});
 		return mySqlButton;
 	}
 
 	private Combo createLabelWithCombo(String labelText, Composite container) {
 		Composite parent = new Composite(container, SWT.NONE);
 		parent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 		GridLayout layout = new GridLayout(2, false);
 		layout.marginWidth = 0;
 		layout.marginHeight = 0;
 		parent.setLayout(layout);
 		Label label = new Label(parent, SWT.NONE);
 		label.setText(labelText);
 		GridData gd = new GridData(SWT.LEFT, SWT.CENTER, false, true);
 		gd.widthHint = 100;
 		label.setLayoutData(gd);
 		Combo combo = new Combo(parent, SWT.BORDER | SWT.SINGLE);
 		combo.addSelectionListener(new SelectionAdapter() {
 
 			public void widgetSelected(SelectionEvent e) {
 				setPageComplete(validatePage());
 			}
 		});
 		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
 		return combo;
 	}
 
 	/*
 	 * private TableViewer createPlatformsSection(Composite container) {
 	 * Composite parent = new Composite(container, SWT.NONE);
 	 * parent.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 2, 1));
 	 * GridLayout layout = new GridLayout(2, false); layout.marginWidth = 0;
 	 * layout.marginHeight = 0; parent.setLayout(layout); Label label = new
 	 * Label(parent, SWT.NONE); label.setText("Cartridges:"); GridData gd = new
 	 * GridData(SWT.LEFT, SWT.CENTER, false, true); gd.widthHint = 100;
 	 * label.setLayoutData(gd); CheckboxTableViewer viewer =
 	 * CheckboxTableViewer.newCheckList(parent, SWT.CHECK | SWT.BORDER |
 	 * SWT.V_SCROLL | SWT.SINGLE); viewer.setContentProvider(new
 	 * IStructuredContentProvider() {
 	 * 
 	 * public void dispose() { }
 	 * 
 	 * public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
 	 * { }
 	 * 
 	 * public Object[] getElements(Object inputElement) { try { return
 	 * target.getCartridges(); } catch (SdkException e) { Activator.log(e); }
 	 * return new Object[0]; } }); viewer.setLabelProvider(new
 	 * ColumnLabelProvider() {
 	 * 
 	 * public String getText(Object element) { return
 	 * target.getCartridgeLabel(element); } }); final Table table =
 	 * viewer.getTable(); table.setHeaderVisible(false);
 	 * table.setLinesVisible(true); table.setLayoutData(new
 	 * GridData(GridData.FILL_HORIZONTAL));
 	 * viewer.addSelectionChangedListener(new ISelectionChangedListener() {
 	 * 
 	 * public void selectionChanged(SelectionChangedEvent event) {
 	 * 
 	 * } }); return viewer; }
 	 */
 
 }
