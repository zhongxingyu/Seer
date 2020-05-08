 /**
  * <copyright>
  * 
  * Copyright (c) 2012, 2012 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  * 
  * </copyright>
  */
 package org.eclipse.graphiti.tools.newprojectwizard.internal;
 
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.search.SearchEngine;
 import org.eclipse.jdt.ui.IJavaElementSearchConstants;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.pde.ui.templates.BaseOptionTemplateSection;
 import org.eclipse.pde.ui.templates.StringOption;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.dialogs.SelectionDialog;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleReference;
 
 /**
  * An option that realizes a group.
  */
 public class SelectTypeOption extends StringOption {
 
 	private GroupOption groupOption;
 
 	private Text text;
 	private Label labelControl;
 	private boolean ignoreListener;
 	private Button buttonControl;
 	private int fStyle;
 
 	private String bundleName = null;
 
 	private final static int F_DEFAULT_STYLE = SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY;
 
 	/**
 	 * The constructor.
 	 * 
 	 * @param section
 	 *            the parent section
 	 * @param name
 	 *            the unique option name
 	 * @param label
 	 *            the translatable label of the option
 	 * @param groupOption
 	 */
 	public SelectTypeOption(BaseOptionTemplateSection section, String name, String label, GroupOption groupOption) {
 		super(section, name, label);
 		fStyle = F_DEFAULT_STYLE;
 		setRequired(true);
		setEnabled(false);
 		this.groupOption = groupOption;
 	}
 
 	/**
 	 * A utility version of the <samp>getValue() </samp> method that converts
 	 * the current value into the String object.
 	 * 
 	 * @return the string version of the current value.
 	 */
 	public String getText() {
 		if (getValue() != null)
 			return getValue().toString();
 		return null;
 	}
 
 	/**
 	 * A utility version of the <samp>setValue </samp> method that accepts
 	 * String objects.
 	 * 
 	 * @param newText
 	 *            the new text value of the option
 	 * @see #setValue(Object)
 	 */
 	public void setText(String newText) {
 		setValue(newText);
 	}
 
 	/**
 	 * Implements the superclass method by passing the string value of the new
 	 * value to the widget
 	 * 
 	 * @param value
 	 *            the new option value
 	 */
 	public void setValue(Object value) {
 		super.setValue(value);
 		if (text != null) {
 			ignoreListener = true;
 			String textValue = getText();
 			text.setText(textValue != null ? textValue : ""); //$NON-NLS-1$
 			ignoreListener = false;
 		}
 	}
 
 	/**
 	 * Creates the string option control.
 	 * 
 	 * @param parent
 	 *            parent composite of the string option widget
 	 * @param span
 	 *            the number of columns that the widget should span
 	 */
 	public void createControl(Composite parent, int span) {
 		Composite composite = new Composite(groupOption.getGroup(), SWT.NONE);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = span;
 		composite.setLayoutData(gd);
 		composite.setLayout(new GridLayout(3, false));
 
 		labelControl = createLabel(composite, 1);
 		labelControl.setEnabled(isEnabled());
 
 		text = new Text(composite, fStyle);
 		if (getValue() != null)
 			text.setText(getValue().toString());
 		gd = new GridData(GridData.FILL_HORIZONTAL);
 		gd.horizontalSpan = 1;
 		text.setLayoutData(gd);
 		text.setEnabled(isEnabled());
 		text.addModifyListener(new ModifyListener() {
 			public void modifyText(ModifyEvent e) {
 				if (ignoreListener)
 					return;
 				SelectTypeOption.super.setValue(text.getText());
 				getSection().validateOptions(SelectTypeOption.this);
 			}
 		});
 
 		buttonControl = new Button(composite, SWT.PUSH);
 		buttonControl.setText(Messages.SelectTypeOption_BrowseButton);
 		buttonControl.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				Shell parent = Display.getCurrent().getActiveShell();
 				SelectionDialog dialog = null;
 				try {
 					dialog = JavaUI.createTypeDialog(parent, PlatformUI.getWorkbench().getProgressService(),
 							SearchEngine.createWorkspaceScope(),
 							IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES, false);
 				} catch (JavaModelException jme) {
 					MessageDialog.openError(parent, "Could not open type selection dialog", jme.getMessage()); //$NON-NLS-1$
 					return;
 				}
 
 				// SelectionDialog dialog = new OpenTypeSelectionDialog(parent,
 				// true, PlatformUI.getWorkbench()
 				// .getProgressService(), null, IJavaSearchConstants.TYPE);
 				dialog.setTitle(Messages.SelectTypeOption_TitleSelectDomainObject);
 				dialog.setMessage(Messages.SelectTypeOption_DescriptionSelectDomainObject);
 
 				if (dialog.open() == Dialog.OK) {
 					Object[] result = dialog.getResult();
 					if (result != null && result.length > 0 && result[0] instanceof IType) {
 						IType type = (IType) result[0];
 						text.setText(type.getFullyQualifiedName());

 						Bundle containingBundle = null;
 
 						// Search for the first bundle that can resolve the
 						// desired class
 						Bundle[] bundles = Activator.getDefault().getBundle().getBundleContext().getBundles();
 						for (Bundle bundle : bundles) {
 							try {
 								Class<?> loadClass = bundle.loadClass(type.getFullyQualifiedName());
 
 								// Use the class loader of the class to identify
 								// the containing bundle
 								ClassLoader classLoader = loadClass.getClassLoader();
 								if (classLoader instanceof BundleReference) {
 									containingBundle = ((BundleReference) classLoader).getBundle();
 									setBundleName(containingBundle.getSymbolicName());
 									return;
 								}
 							} catch (ClassNotFoundException cnfe) {
 								// Simply ignore
 							}
 						}
 						if (containingBundle == null) {
 							MessageDialog.openError(parent, "No Bundle found", //$NON-NLS-1$
 									"The class '" + type.getFullyQualifiedName() //$NON-NLS-1$
 											+ "' could not be resolved within an installed plugin."); //$NON-NLS-1$
 						}
 					}
 				}
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		buttonControl.setEnabled(isEnabled());
 	}
 
 	/**
 	 * A string option is empty if its text field contains no text.
 	 * 
 	 * @return true if there is no text in the text field.
 	 */
 	public boolean isEmpty() {
 		return getValue() == null || getValue().toString().length() == 0;
 	}
 
 	/**
 	 * Implements the superclass method by passing the enabled state to the
 	 * option's widget.
 	 * 
 	 * @param enabled
 	 */
 	public void setEnabled(boolean enabled) {
 		super.setEnabled(enabled);
 		if (labelControl != null) {
 			labelControl.setEnabled(enabled);
 			text.setEnabled(enabled);
 			buttonControl.setEnabled(enabled);
 		}
 	}
 
 	public String getBundleName() {
 		return bundleName;
 	}
 
 	public void setBundleName(String bundleName) {
 		this.bundleName = bundleName;
 	}
 }
