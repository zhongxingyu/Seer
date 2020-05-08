 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ui.preferences;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.dltk.internal.ui.dialogs.StatusInfo;
 import org.eclipse.dltk.internal.ui.dialogs.StatusUtil;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Configures preferences.
  */
 public abstract class ImprovedAbstractConfigurationBlock implements
 		IPreferenceConfigurationBlock {
 
 	protected static class DependencyManager {
 		private ArrayList masterSlaveListeners = new ArrayList();
 
 		public void initialize() {
 			Iterator it = masterSlaveListeners.iterator();
 			while (it.hasNext()) {
 				((SelectionListener) it.next()).widgetSelected(null);
 			}
 		}
 
 		public void createDependency(final Button master, final Control[] slaves) {
 			SelectionListener listener = new SelectionListener() {
 				public void widgetSelected(SelectionEvent e) {
 					boolean state = master.getSelection();
 
 					for (int i = 0; i < slaves.length; i++) {
 						slaves[i].setEnabled(state);
 					}
 				}
 
 				public void widgetDefaultSelected(SelectionEvent e) {
 				}
 			};
 			master.addSelectionListener(listener);
 
 			masterSlaveListeners.add(listener);
 		}
 	}
 
 	private static class BindingManager {
 		private class ValidatorManager {
 			private HashMap map = new HashMap();
 
 			public void registerValidator(Text text, IFieldValidator validator) {
 				map.put(text, validator);
 			}
 
 			public void unregisterValidator(Text text) {
 				map.remove(text);
 			}
 
 			public IFieldValidator getValidator(Control control) {
 				return (IFieldValidator) map.get(control);
 			}
 		}
 
 		private HashMap textControls;
 		private HashMap buttonControls;
		private HashMap comboControls;
 
 		private ValidatorManager validatorManager;
 
 		private IPreferenceStore store;
 		private PreferencePage page;
 
 		protected void updateStatus(IStatus status) {
 			if (status == null) {
 				status = new StatusInfo();
 			}
 
 			page.setValid(status.getSeverity() != IStatus.ERROR);
 			StatusUtil.applyToStatusLine(page, status);
 		}
 
 		public BindingManager(IPreferenceStore store, PreferencePage page) {
 			this.store = store;
 			this.page = page;
 			this.textControls = new HashMap();
 			this.buttonControls = new HashMap();
			this.comboControls = new HashMap();
 			this.validatorManager = new ValidatorManager();
 		}
 
 		public IPreferenceStore getPreferenceStore() {
 			return store;
 		}
 
 		public void initialize() {
 			Iterator it = textControls.keySet().iterator();
 			while (it.hasNext()) {
 				final Text text = (Text) it.next();
 				final String key = (String) textControls.get(text);
 				text.setText(store.getString(key));
 			}
 
 			it = buttonControls.keySet().iterator();
 			while (it.hasNext()) {
 				final Button button = (Button) it.next();
 				final String key = (String) buttonControls.get(button);
 				button.setSelection(store.getBoolean(key));
 			}
			it = comboControls.keySet().iterator();
			while (it.hasNext()) {
				final Combo combo = (Combo) it.next();
				final String key = (String) comboControls.get(combo);
				String value = store.getString(key);
				String[] items = combo.getItems();
				boolean selected = false;
				for (int i = 0; i < items.length; i++) {
					if( items[i].equals(value)) {
						combo.select(i);
						selected = true;
						break;
					}
				}
				if( !selected ) {
					combo.select(0);
				}
			}
 		}
 
 		protected void bindControl(final Button button, final String key) {
 			buttonControls.put(button, key);
 
 			button.addSelectionListener(new SelectionListener() {
 				public void widgetDefaultSelected(SelectionEvent e) {
 
 				}
 
 				public void widgetSelected(SelectionEvent e) {
 					final boolean value = button.getSelection();
 					store.setValue(key, value);
 				}
 			});
 		}
 
 		protected void bindControl(final Text text, final String key,
 				IFieldValidator validator) {
 			if (key != null) {
 				textControls.put(text, key);
 			}
 
 			if (validator != null) {
 				validatorManager.registerValidator(text, validator);
 			}
 
 			text.addModifyListener(new ModifyListener() {
 				public void modifyText(ModifyEvent e) {
 					final String value = text.getText();
 
 					IStatus status = new StatusInfo();
 
 					IFieldValidator validator = validatorManager
 							.getValidator(text);
 
 					if (validator != null && text.isEnabled()) {
 						status = validator.validate(value);
 					}
 
 					if (key != null) {
 						if (status.getSeverity() != IStatus.ERROR) {
 							store.setValue(key, value);
 						}
 					}
 
 					updateStatus(status);
 				}
 			});
 		}
 
 		protected void bindControl(final Combo combo, final String key) {
			if (key != null) {
				comboControls.put(combo, key);
			}
 			combo.addSelectionListener(new SelectionListener() {
 				public void widgetDefaultSelected(SelectionEvent e) {
 				}
 
 				public void widgetSelected(SelectionEvent e) {
					int index = combo.getSelectionIndex();
					store.setValue(key, combo.getItem(index));
 				}
 			});
 		}
 	}
 
 	private BindingManager bindManager;
 	private DependencyManager dependencyManager;
 
 	protected void initializeFields() {
 		bindManager.initialize();
 		dependencyManager.initialize();
 	}
 
 	public ImprovedAbstractConfigurationBlock(IPreferenceStore store,
 			PreferencePage page) {
 		this.bindManager = new BindingManager(store, page);
 		this.dependencyManager = new DependencyManager();
 	}
 
 	public void initialize() {
 		initializeFields();
 	}
 
 	public void performOk() {
 	}
 
 	public void performDefaults() {
 		initializeFields();
 	}
 
 	public void dispose() {
 	}
 
 	// Preference store
 	public IPreferenceStore getPreferenceStore() {
 		return bindManager.getPreferenceStore();
 	}
 
 	// Binding
 	protected void bindControl(final Button button, final String key) {
 		bindManager.bindControl(button, key);
 	}
 
 	protected void bindControl(final Text text, final String key,
 			IFieldValidator validator) {
 		bindManager.bindControl(text, key, validator);
 	}
 
 	protected void bindControl(final Text text, final String key) {
 		bindManager.bindControl(text, key, null);
 	}
 
 	protected void bindControl(final Text text, IFieldValidator validator) {
 		bindManager.bindControl(text, null, validator);
 	}
	protected void bindControl(final Combo combo,
			final String key) {
		bindManager.bindControl(combo, key);
	}
 
 	// Dependency
 	protected void createDependency(final Button master, final Control slave) {
 		createDependency(master, new Control[] { slave });
 	}
 
 	protected void createDependency(final Button master, final Control[] slaves) {
 		dependencyManager.createDependency(master, slaves);
 	}
 
 	// Status
 	protected void updateStatus(IStatus status) {
 		bindManager.updateStatus(status);
 	}
 }
