 /*******************************************************************************
  * Copyright (c) 2007-2010 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.jst.jsp.i18n;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.TextSelection;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.ISelectionProvider;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.VerifyEvent;
 import org.eclipse.swt.events.VerifyListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wst.xml.core.internal.document.AttrImpl;
 import org.eclipse.wst.xml.core.internal.document.TextImpl;
 import org.jboss.tools.common.model.loaders.impl.EncodedProperties;
 import org.jboss.tools.common.model.ui.ModelUIImages;
 import org.jboss.tools.jst.jsp.JspEditorPlugin;
 import org.jboss.tools.jst.jsp.bundle.BundleMap;
 import org.jboss.tools.jst.jsp.bundle.BundleMap.BundleEntry;
 import org.jboss.tools.jst.jsp.messages.JstUIMessages;
 import org.jboss.tools.jst.jsp.util.Constants;
 import org.w3c.dom.Attr;
 
 public class ExternalizeStringsWizardPage extends WizardPage {
 
 	public static final String PAGE_NAME = "ExternalizeStringsWizardBasicPage"; //$NON-NLS-1$
 	
 	private final int DIALOG_WIDTH = 450;
 	private final int DIALOG_HEIGHT = 650;
 	private Text propsKey;
 	private Text propsValue;
 	private Button newFile;
 	private Label propsFileLabel;
 	private Text propsFile;
 	private Label rbListLabel;
 	private Combo rbCombo;
 	private BundleMap bm;
 	private Group propsFilesGroup;
 	private Status propsKeyStatus;
 	private Status propsValueStatus;
 	private Status duplicateKeyStatus;
 	private Status duplicateValueStatus;
 	private Table tagsTable;
 	private IDocument document;
 	private ISelectionProvider selectionProvider;
 
 	/**
 	 * Creates the wizard page
 	 * 
 	 * @param pageName
 	 *            the name of the page
 	 * @param editor
 	 *            the source text editor
 	 * @param bm
 	 *            bundle map, or not null
 	 */
 	public ExternalizeStringsWizardPage(String pageName, BundleMap bm, IDocument document, ISelectionProvider selectionProvider) {
 		/*
 		 * Setting dialog Title, Description, Image.
 		 */
 		super(pageName,
 				JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_TITLE, 
 				ModelUIImages.getImageDescriptor(ModelUIImages.WIZARD_DEFAULT));
 		setDescription(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_DESCRIPTION);
 		setPageComplete(false);
 		this.bm=bm;
 		this.document = document;
 		this.selectionProvider = selectionProvider;
 		propsKeyStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 		propsValueStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 		duplicateKeyStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 		duplicateValueStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 	}
 
 	public void createControl(Composite parent) {
 
 		Composite composite = new Composite(parent, SWT.NONE);
 		composite.setLayout(new GridLayout(1, false));
 
 		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
 		gd.widthHint = DIALOG_WIDTH;
 		gd.heightHint = DIALOG_HEIGHT;
 		composite.setLayoutData(gd);
 		
 		/*
 		 * Create properties string group
 		 */
 		Group propsStringGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
 		propsStringGroup.setLayout(new GridLayout(3, false));
 		propsStringGroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 1, 1));
 		propsStringGroup.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_PROPS_STRINGS_GROUP);
 
 		/*
 		 * Create Properties Key label
 		 */
 		Label propsKeyLabel = new Label(propsStringGroup, SWT.NONE);
 		propsKeyLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false, 1, 1));
 		propsKeyLabel.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_PROPERTIES_KEY);
 		/*
 		 * Create Properties Key value
 		 */
 		propsKey = new Text(propsStringGroup, SWT.BORDER);
 		propsKey.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
 		propsKey.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_DEFAULT_KEY);
 		
 		/*
 		 * Create Properties Value  label
 		 */
 		Label propsValueLabel = new Label(propsStringGroup, SWT.NONE);
 		propsValueLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false, 1, 1));
 		propsValueLabel.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_PROPERTIES_VALUE);
 		/*
 		 * Create Properties Value value
 		 */
 		propsValue = new Text(propsStringGroup, SWT.BORDER);
 		propsValue.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
 		propsValue.setText(Constants.EMPTY);
 
 		/*
 		 * Create New File Checkbox
 		 */
 		newFile = new Button(composite, SWT.CHECK);
 		newFile.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false, 1, 1));
 		newFile.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_NEW_FILE);
 		
 		/*
 		 * Create properties string group
 		 */
 		propsFilesGroup = new Group(composite, SWT.SHADOW_ETCHED_IN);
 		propsFilesGroup.setLayout(new GridLayout(3, false));
 		gd = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
 		gd.heightHint = 300; 
 		propsFilesGroup.setLayoutData(gd);
 		propsFilesGroup.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_PROPS_FILES_GROUP);
 		
 		/*
 		 * Create Resource Bundles List label
 		 */
 		rbListLabel = new Label(propsFilesGroup, SWT.NONE);
 		rbListLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false, false, 1, 1));
 		rbListLabel.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_RESOURCE_BUNDLE_LIST);
 		/*
 		 * Create Resource Bundles combobox
 		 */
 		rbCombo = new Combo(propsFilesGroup, SWT.NONE);
 		rbCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
 		
 		/*
 		 * Create Properties File label
 		 */
 		propsFileLabel = new Label(propsFilesGroup, SWT.NONE);
 		propsFileLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.NONE, false,false, 1, 1));
 		propsFileLabel.setText(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_PROPERTIES_FILE);
 		/*
 		 * Create Properties File path field
 		 */
 		propsFile = new Text(propsFilesGroup, SWT.BORDER);
 		propsFile.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,false, 2, 1));
 		propsFile.setText(Constants.EMPTY);
 		propsFile.setEditable(false);
 		/*
 		 * Create properties file table of content
 		 */
 		tagsTable = ExternalizeStringsUtils.createPropertiesTable(propsFilesGroup, SWT.BORDER); 
 		/*
 		 * Initialize all fields with real values.
 		 */
 		initializeFieldsAndAddLIsteners();
 		
 		/*
 		 * Wizard Page control should be initialized.
 		 */
 		setControl(composite);
 	}
 
 	/**
 	 * Gets the bundle prefix.
 	 *
 	 * @return the bundle prefix
 	 */
 	public String getBundlePrefix() {
 		String bundlePrefix = Constants.EMPTY;
 		if (!isNewFile()) {
 			for (BundleEntry be : bm.getBundles()) {
 				if (be.uri.equalsIgnoreCase(rbCombo.getText())) {
 					bundlePrefix = be.prefix;
 				}
 			}
 		}
 		return bundlePrefix;
 	}
 	
 	/**
 	 * Gets resource bundle's file
 	 * @return the file
 	 */
 	public IFile getBundleFile() {
 		return bm.getBundleFile(rbCombo.getText());
 	}
 	
 	/**
 	 * Use existed key-value pair from the properties file
 	 * without writing any data to the file.
 	 * 
 	 * @return 
 	 */
 	public boolean isDuplicatedKeyAndValue() {
 		boolean exists = false; 
 		if (isValueDuplicated(propsValue.getText()) 
 				&& isKeyDuplicated(propsKey.getText())) {
 			exists = true;
 		}
 		return exists;
 	}
 	
 	/**
 	 * Gets <code>key=value</code> pair
 	 * 
 	 * @return a pair <code>key=value</code>
 	 */
 	public String getKeyValuePair() {
 		/*
 		 * While initializing 'propsValue' field \t\r\n were replaced by \\\\t, etc.
 		 * Now we should return escaped characters, and during saving tha properties
 		 * they will treated as escape symbols as well.
 		 * Otherwise \\\\t string will be put into the properties file.
 		 */
 		String value = propsValue.getText();
 		if (value != null) {
 			value = value.replaceAll("\\\\t", "\t"); //$NON-NLS-1$ //$NON-NLS-2$
 			value = value.replaceAll("\\\\r", "\r"); //$NON-NLS-1$ //$NON-NLS-2$
 			value = value.replaceAll("\\\\n", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		return propsKey.getText() + Constants.EQUAL + EncodedProperties.saveConvert(value, true);
 	}
 	
 	/**
 	 * Gets the key.
 	 *
 	 * @return the key
 	 */
 	public String getKey() {
 		return propsKey.getText();
 	}
 	
 	/**
 	 * Check if "Create new file.." option is enabled
 	 * 
 	 * @return the status
 	 */
 	public boolean isNewFile() {
 		return newFile.getSelection();
 	}
 	
 	/**
 	 * Replaces the text in the current file
 	 */
 	public void replaceText(String replacement) {
 		IDocument doc = getDocument();
 		ISelection sel = getSelectionProvider().getSelection();
 		if (ExternalizeStringsUtils.isSelectionCorrect(sel)) {
 			try {
 				/*
 				 * Get source text and new text
 				 */
 				TextSelection textSel = (TextSelection) sel;
 				IStructuredSelection structuredSelection = (IStructuredSelection) sel;
 				Object firstElement = structuredSelection.getFirstElement();
 				int offset = 0;
 				int length = 0;
 				/*
 				 * When user selection is empty 
 				 * underlying node will e automatically selected.
 				 * Thus we need to correct replacement offsets. 
 				 */
 				if ((textSel.getLength() != 0)) {
 					offset = textSel.getOffset();
 					length = textSel.getLength();
 				} else if (firstElement instanceof TextImpl) {
 					TextImpl ti = (TextImpl) firstElement;
 					offset = ti.getStartOffset();
 					length = ti.getLength();
 				} else if (firstElement instanceof AttrImpl) {
 					AttrImpl ai = (AttrImpl) firstElement;
 					/*
 					 * Get offset and length without quotes ".."
 					 */
 					offset = ai.getValueRegionStartOffset() + 1;
 					length = ai.getValueRegionText().length() - 2;
 				}
 				/*
 				 * Replace text in the editor with "key.value"
 				 */
 				doc.replace(offset, length, replacement);
 			} catch (BadLocationException ex) {
 				JspEditorPlugin.getPluginLog().logError(ex);
 			}
 		}
 	}
 
 	@Override
 	public boolean isPageComplete() {
 		boolean isPageComplete = false;
 		/*
 		 * The page is ready when there are no error messages 
 		 * and the bundle is selected
 		 * and "key=value" exists.
 		 */
 		if ((getErrorMessage() == null)
 				&& !Constants.EMPTY.equalsIgnoreCase(propsKey.getText().trim())
 				&& !Constants.EMPTY.equalsIgnoreCase(propsValue.getText().trim())
 				&& ((rbCombo.getSelectionIndex() != -1) || isNewFile())) {
 			isPageComplete = true;
 		}
 		return isPageComplete;
 	}
 	
 	@Override
 	public boolean canFlipToNextPage() {
 		return isPageComplete() && (getNextPage() != null)
 				&& isNewFile();
 	}
 	
 	/**
 	 * Initialize dialog's controls.
 	 * Fill in appropriate text and make validation.
 	 */
 	private void initializeFieldsAndAddLIsteners() {
 		ISelection sel = getSelectionProvider().getSelection();
 		if (ExternalizeStringsUtils.isSelectionCorrect(sel)) {
 			String text = Constants.EMPTY;
 			String stringToUpdate = Constants.EMPTY;
 			TextSelection textSelection = null;
 			IStructuredSelection structuredSelection = (IStructuredSelection) sel;
 			textSelection = (TextSelection) sel;
 			text = textSelection.getText();
 			Object selectedElement = structuredSelection.getFirstElement();
 			/*
 			 * When selected text is empty
 			 * parse selected element and find a string to replace..
 			 */
 			if ((text.trim().length() == 0)) {
 				if (selectedElement instanceof org.w3c.dom.Text) {
 					/*
 					 * ..it could be a plain text
 					 */
 					org.w3c.dom.Text textNode = (org.w3c.dom.Text) selectedElement;
 					if (textNode.getNodeValue().trim().length() > 0) {
 						stringToUpdate = textNode.getNodeValue();
 						getSelectionProvider().setSelection(new StructuredSelection(stringToUpdate));
 					}
 				} else if (selectedElement instanceof Attr) {
 					/*
 					 * ..or an attribute's value
 					 */
 					Attr attrNode = (Attr) selectedElement;
 					if (attrNode.getNodeValue().trim().length() > 0) {
 						stringToUpdate = attrNode.getNodeValue();
 						getSelectionProvider().setSelection(new StructuredSelection(stringToUpdate));
 					}
 				}
 				if ((stringToUpdate.trim().length() > 0)) {
 					text = stringToUpdate;
 				}
 			}
 			/*
 			 * https://issues.jboss.org/browse/JBIDE-9203
 			 * Replace special characters with their string representation.
 			 * Key should be generated first.
 			 */
 			propsKey.setText(ExternalizeStringsUtils.generatePropertyKey(text));
 			/*
 			 * Replaced escaped symbols by strings.
 			 */
 			String value = text;
 			if (value != null) {
 				value = value.replaceAll("\t", "\\\\t"); //$NON-NLS-1$ //$NON-NLS-2$
 				value = value.replaceAll("\r", "\\\\r"); //$NON-NLS-1$ //$NON-NLS-2$
 				value = value.replaceAll("\n", "\\\\n"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			propsValue.setText(value);
 			/*
 			 * Initialize bundle messages field
 			 */
 			if (bm == null) {
 				JspEditorPlugin.getDefault().logWarning(
 						JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_RB_IS_MISSING);
 			} else {
 				BundleEntry[] bundles = bm.getBundles();
 				Set<String> uriSet = new HashSet<String>(); 
 				for (BundleEntry bundleEntry : bundles) {
 					if (!uriSet.contains(bundleEntry.uri)) {
 						uriSet.add(bundleEntry.uri);
 						rbCombo.add(bundleEntry.uri);
 					}
 				}
 				/*
 				 * Select the first bundle if there is any in the list 
 				 */
 				if (rbCombo.getItemCount() > 0) {
 					rbCombo.select(0);
 					setResourceBundlePath(rbCombo.getText());
 				} else {
 					/*
 					 * https://jira.jboss.org/browse/JBIDE-7247
 					 * Select 'Create new file' checkbox and
 					 * disable bundle group if no bundles are found.
 					 */
 					newFile.setSelection(true);
 					enableBundleGroup(false);
 				}
 			}
 			/*
 			 * Check the initial value status
 			 * When the same value is already externalized --
 			 * suggest to use already created key as well.
 			 */
 			updatePropertiesValueStatus();
 			updateDuplicateValueStatus();
 			/*
 			 * When selected text is fine perform further validation
 			 */
 			if (propsValueStatus.isOK()) {
 				/*
 				 * Check the bundle for the value in it.
 				 * if so -- show the warning and set correct key.
 				 */
 				if (!duplicateValueStatus.isOK()) {
 					/*
 					 * Set the same key value
 					 */
 					propsKey.setText(getValueForKey(propsValue.getText()));
 					/*
 					 * Set the new warning message
 					 */
 					applyStatus(this, new IStatus[] {duplicateValueStatus});
 				} else {
 					/*
 					 * Check the initial key status
 					 * If there is the error - add sequence number to the key
 					 */
 					updateDuplicateKeyStatus();
 					while (!duplicateKeyStatus.isOK()) {
 						int index = propsKey.getText().lastIndexOf('_');
 						String newKey = Constants.EMPTY;
 						if (index != -1) {
 							/*
 							 * String sequence at the end should be checked.
 							 * If it is a sequence number - it should be increased by 1.
 							 * If not - new number should be added.
 							 */
 							String numberString =  propsKey.getText().substring(index + 1);
 							int number;
 							try {
 								number = Integer.parseInt(numberString);
 								number++;
 								newKey = propsKey.getText().substring(0, index + 1) + number;
 							} catch (NumberFormatException e) {
 								newKey = propsKey.getText() + "_1"; //$NON-NLS-1$
 							}
 						} else {
 							/*
 							 * If the string has no sequence number - add it.
 							 */
 							newKey = propsKey.getText() + "_1"; //$NON-NLS-1$
 						}
 						/*
 						 * Set the new key text
 						 */
 						propsKey.setText(newKey);
 						updateDuplicateKeyStatus();
 					}
 					/*
 					 * https://jira.jboss.org/browse/JBIDE-6945
 					 * Set the greeting message only.
 					 * All the validation will take place in the fields' listeners
 					 * after user enters some new values. 
 					 */
 					setMessage(JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_ENTER_KEY_NAME,
 							IMessageProvider.INFORMATION);
 				}
 			} else {
 				/*
 				 * Set the message about wrong selected text.
 				 */
 				applyStatus(this, new IStatus[] {propsValueStatus});
 			}
 			/*
 			 * Update the Buttons state.
 			 * When all the fields are correct -- 
 			 * then user should be able to  press OK
 			 */
 			setPageComplete(isPageComplete());
 			/*
 			 * Add selection listeners to the fields
 			 */
 			propsKey.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					updateStatus();
 				}
 			});
 			propsKey.addVerifyListener(new VerifyListener() {
 				  public void verifyText(VerifyEvent e) {
 						for (int i = 0; i < ExternalizeStringsUtils.REPLACED_CHARACTERS.length; i++) {
 							/*
 							 * Entering of the forbidden characters will be prevented.
 							 */
							if (e.character == ExternalizeStringsUtils.REPLACED_CHARACTERS[i]) {
 								e.doit = false;
 								break;
 							}
 						}
 				    }
 				});
 			propsValue.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					updateStatus();
 				}
 			});
 			newFile.addSelectionListener( new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					boolean selected = ((Button)e.getSource()).getSelection();
 					if (selected) {
 						enableBundleGroup(false);
 					} else {
 						enableBundleGroup(true);
 					}
 					updateStatus();
 				}
 			});
 			rbCombo.addSelectionListener(new SelectionAdapter() {
 				@Override
 				public void widgetSelected(SelectionEvent e) {
 					setResourceBundlePath(rbCombo.getText());
 					updateDuplicateKeyStatus();
 					updateStatus();
 				}
 			});
 		} else {
 			JspEditorPlugin.getDefault().logWarning(
 					JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_INITIALIZATION_ERROR);
 		}
 	}
 	
 	/**
 	 * Checks keys in the selected resource bundle.
 	 * 
 	 * @param key the key name
 	 * @return <code>true</code> if there is a key with the specified name
 	 */
 	private boolean isKeyDuplicated(String key) {
 		boolean isDuplicated = false;
 		if ((tagsTable.getItemCount() > 0) && (null != key) && !isNewFile()) {
 			TableItem[] items = tagsTable.getItems();
 			for (TableItem tableItem : items) {
 				if (key.equalsIgnoreCase(tableItem.getText(0))) {
 					isDuplicated = true;
 					break;
 				}
 			}
 		} 
 		return isDuplicated; 
 	}
 	
 	/**
 	 * Checks values in the selected resource bundle.
 	 * 
 	 * @param value the text string to externalize
 	 * @return <code>true</code> if there is a key with the specified name
 	 */
 	private boolean isValueDuplicated(String value) {
 		boolean isDuplicated = false;
 		if ((tagsTable.getItemCount() > 0) && (null != value) && !isNewFile()) {
 			TableItem[] items = tagsTable.getItems();
 			for (TableItem tableItem : items) {
 				if (value.equalsIgnoreCase(tableItem.getText(1))) {
 					isDuplicated = true;
 					break;
 				}
 			}
 		} 
 		return isDuplicated; 
 	}
 	
 	
 	/**
 	 * Returns the key for the specified value
 	 * from the visual table.
 	 * 
 	 * @param value - the value
 	 * @return the key or empty string
 	 */
 	private String getValueForKey(String value) {
 		String key = Constants.EMPTY;
 		if ((tagsTable.getItemCount() > 0) && (null != value) && !isNewFile()) {
 			TableItem[] items = tagsTable.getItems();
 			for (TableItem tableItem : items) {
 				if (value.equalsIgnoreCase(tableItem.getText(1))) {
 					key = tableItem.getText(0);
 					break;
 				}
 			}
 		} 
 		return key; 
 	}
 	
 	/**
 	 * Enables or disables resource bundle information
 	 * 
 	 * @param enabled shows the status
 	 */
 	private void enableBundleGroup(boolean enabled) {
 			propsFilesGroup.setEnabled(enabled);
 			propsFileLabel.setEnabled(enabled);
 			propsFile.setEnabled(enabled);
 			rbListLabel.setEnabled(enabled);
 			rbCombo.setEnabled(enabled);
 			tagsTable.setEnabled(enabled);
 	}
 	
 	/**
 	 * Update duplicate key status.
 	 */
 	private void updateDuplicateKeyStatus() {
 		if (isKeyDuplicated(propsKey.getText())) {
 			duplicateKeyStatus = new Status(
 					IStatus.ERROR,
 					JspEditorPlugin.PLUGIN_ID,
 					JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_KEY_ALREADY_EXISTS); 
 		} else {
 			duplicateKeyStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 		}
 	}
 	
 	/**
 	 * Update duplicate key status.
 	 */
 	private void updateDuplicateValueStatus() {
 		if (isValueDuplicated(propsValue.getText())) {
 			duplicateValueStatus = new Status(
 					IStatus.WARNING,
 					JspEditorPlugin.PLUGIN_ID,
 					JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_VALUE_EXISTS); 
 		} else {
 			duplicateValueStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 		}
 	}
 
 	private void updatePropertiesValueStatus() {
 		String text = propsValue.getText();
 		if ((text == null)
 				|| (Constants.EMPTY.equalsIgnoreCase(text.trim()))
 				|| (text.indexOf(Constants.GT) != -1) 
 				||  (text.indexOf(Constants.LT) != -1)) {
 			propsValueStatus = new Status(
 					IStatus.ERROR,
 					JspEditorPlugin.PLUGIN_ID,
 					JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_WRONG_SELECTED_TEXT);
 		} else {
 			propsValueStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 		}
 	}
 	
 	/**
 	 * Update properties key status.
 	 */
 	private void updatePropertiesKeyStatus() {
 		if ((propsKey.getText() == null) 
 				|| (Constants.EMPTY.equalsIgnoreCase(propsKey.getText().trim()))) {
 			propsKeyStatus = new Status(
 					IStatus.ERROR,
 					JspEditorPlugin.PLUGIN_ID,
 					JstUIMessages.EXTERNALIZE_STRINGS_DIALOG_KEY_MUST_BE_SET);
 		} else {
 			propsKeyStatus = new Status(IStatus.OK, JspEditorPlugin.PLUGIN_ID, Constants.EMPTY);
 		}
 	}
 	
 	/**
 	 * Update page status.
 	 */
 	private void updateStatus() {
 		/*
 		 * Update all statuses
 		 */
 		updatePropertiesKeyStatus();
 		updatePropertiesValueStatus();
 		updateDuplicateKeyStatus();
 		updateDuplicateValueStatus();
 		/*
 		 * Apply status to the dialog
 		 */
 		if (!duplicateValueStatus.isOK()
 				&& getValueForKey(propsValue.getText())
 						.equalsIgnoreCase(propsKey.getText())) {
 			applyStatus(this, new IStatus[] { propsKeyStatus, propsValueStatus,
 					duplicateValueStatus});
 		} else {
 			applyStatus(this, new IStatus[] { propsKeyStatus, propsValueStatus,
 					duplicateKeyStatus});
 		}
 		/*
 		 * Set page complete
 		 */
 		setPageComplete(isPageComplete());
 	}
 	
 	/**
 	 * Apply status to the dialog.
 	 *
 	 * @param page the page
 	 * @param statuses all the statuses
 	 */
 	private void applyStatus(DialogPage page, IStatus[] statuses) {
 		IStatus severeStatus = statuses[0];
 		for (IStatus status : statuses) {
 			severeStatus = severeStatus.getSeverity() >= status.getSeverity() 
 				? severeStatus : status;
 		}
 		String message = severeStatus.getMessage();
 		switch (severeStatus.getSeverity()) {
 		case IStatus.OK:
 			page.setMessage(null, IMessageProvider.NONE);
 			page.setErrorMessage(null);
 			break;
 
 		case IStatus.WARNING:
 			page.setMessage(message, IMessageProvider.WARNING);
 			page.setErrorMessage(null);
 			break;
 
 		case IStatus.INFO:
 			page.setMessage(message, IMessageProvider.INFORMATION);
 			page.setErrorMessage(null);
 			break;
 
 		default:
 			if (message.length() == 0) {
 				message = null;
 			}
 			page.setMessage(null);
 			page.setErrorMessage(message);
 			break;
 		}
 	}
 
 	/**
 	 * Sets the resource bundle path according to the selection
 	 * from the bundles list.
 	 *
 	 * @param bundleName the resource bundle name
 	 */
 	private void setResourceBundlePath(String bundleName) {
 		IFile bundleFile = bm.getBundleFile(bundleName);
 		String bundlePath = Constants.EMPTY;
 		if (bundleFile != null) {
 			bundlePath = bundleFile.getFullPath().toString();
 			ExternalizeStringsUtils.populatePropertiesTable(tagsTable, bundleFile);
 		} else {
 			JspEditorPlugin.getDefault().logError(
 					"Could not get Bundle File for resource '" //$NON-NLS-1$
 							+ bundleName + "'"); //$NON-NLS-1$
 		}
 		propsFile.setText(bundlePath);
 	}
 	
 	private ISelectionProvider getSelectionProvider(){
 		return selectionProvider;
 	}
 	
 	private IDocument getDocument(){
 		return document;
 	}
 }
