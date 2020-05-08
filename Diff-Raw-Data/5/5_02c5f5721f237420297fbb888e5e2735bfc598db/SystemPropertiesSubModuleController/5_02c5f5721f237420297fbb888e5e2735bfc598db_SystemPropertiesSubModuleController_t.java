 /*******************************************************************************
  * Copyright (c) 2007, 2013 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.example.client.controllers;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Table;
 
 import org.eclipse.riena.beans.common.AbstractBean;
 import org.eclipse.riena.beans.common.TypedComparator;
 import org.eclipse.riena.core.util.StringUtils;
 import org.eclipse.riena.example.client.views.SystemPropertiesSubModuleView;
 import org.eclipse.riena.internal.ui.swt.facades.WorkbenchFacade;
 import org.eclipse.riena.navigation.ISubModuleNode;
 import org.eclipse.riena.navigation.ui.controllers.SubModuleController;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.ISelectableRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.IToggleButtonRidget;
 import org.eclipse.riena.ui.ridgets.swt.TableFormatter;
 import org.eclipse.riena.ui.swt.RienaMessageDialog;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 
 /**
  * Controller for the {@link SystemPropertiesSubModuleView} example.
  */
 public class SystemPropertiesSubModuleController extends SubModuleController {
 
 	/** Manages a collection of PropertyBeans */
 	private final List<KeyValueBean> properties;
 	private final Properties sysProperties;
 	/** Bean for holding the value being edited. */
 	private final KeyValueBean valueBean;
 	/** IActionListener for double click on the table */
 	private final IActionListener doubleClickListener;
 	private ITableRidget tableProperties;
 	private ITextRidget textKey;
 	private ITextRidget textValue;
 
 	public SystemPropertiesSubModuleController() {
 		this(null);
 	}
 
 	public SystemPropertiesSubModuleController(final ISubModuleNode navigationNode) {
 
 		super(navigationNode);
 		valueBean = new KeyValueBean();
 		doubleClickListener = new DoubleClickListener();
 
 		properties = new ArrayList<KeyValueBean>();
 		sysProperties = System.getProperties();
 		final Set<Object> keys = sysProperties.keySet();
 		for (final Object key : keys) {
 			final KeyValueBean bean = new KeyValueBean();
 			bean.setKey((String) key);
 			bean.setValue(System.getProperty((String) key));
 			properties.add(bean);
 		}
 
 	}
 
 	private boolean isNewKey(final KeyValueBean keyValue) {
 		return sysProperties.get(keyValue.getKey()) == null;
 	}
 
 	private boolean isNewValue(final KeyValueBean keyValue) {
 		final Object value = sysProperties.get(keyValue.getKey());
 		if (value instanceof String) {
 			return !StringUtils.equals((String) value, keyValue.getValue());
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * @see org.eclipse.riena.navigation.ui.controllers.SubModuleController#afterBind()
 	 */
 	@Override
 	public void afterBind() {
 		super.afterBind();
 		bindModels();
 	}
 
 	private void bindModels() {
 		tableProperties.bindToModel(new WritableList(properties, KeyValueBean.class), KeyValueBean.class,
 				new String[] { "key", "value" }, new String[] { "Key", "Value" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 		tableProperties.updateFromModel();
 		tableProperties.setSelectionType(ISelectableRidget.SelectionType.SINGLE);
 		tableProperties.setComparator(0, new TypedComparator<String>());
 		tableProperties.setComparator(1, new TypedComparator<String>());
 		tableProperties.setMoveableColumns(true);
 		if (!properties.isEmpty()) {
 			tableProperties.setSelection(0);
 		}
 
 		textKey.bindToModel(valueBean, "key"); //$NON-NLS-1$
 		textKey.updateFromModel();
 		textValue.bindToModel(valueBean, "value"); //$NON-NLS-1$
 		textValue.updateFromModel();
 	}
 
 	/**
 	 * Binds and updates the ridgets.
 	 * 
 	 * @see org.eclipse.riena.ui.ridgets.IRidgetContainer#configureRidgets()
 	 */
 	@Override
 	public void configureRidgets() {
 
 		tableProperties = getRidget(ITableRidget.class, "tableProperties"); //$NON-NLS-1$
 		textKey = getRidget(ITextRidget.class, "textKey"); //$NON-NLS-1$
 		textValue = getRidget(ITextRidget.class, "textValue"); //$NON-NLS-1$
 		final IActionRidget buttonAdd = getRidget(IActionRidget.class, "buttonAdd"); //$NON-NLS-1$
 		final IToggleButtonRidget toggleDoubleClick = getRidget(IToggleButtonRidget.class, "toggleDoubleClick"); //$NON-NLS-1$
 		final IActionRidget buttonSave = getRidget(IActionRidget.class, "buttonSave"); //$NON-NLS-1$
 
 		tableProperties.addPropertyChangeListener(ITableRidget.PROPERTY_SELECTION, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				final List<Object> selection = tableProperties.getSelection();
 				if (!selection.isEmpty()) {
 					valueBean.setBean((KeyValueBean) selection.get(0));
 					textKey.updateFromModel();
 					textValue.updateFromModel();
 				}
 			}
 		});
 		tableProperties.setTableFormatter(new MyTableFormatter());
 
 		buttonAdd.setText("&Add"); //$NON-NLS-1$
 		buttonAdd.addListener(new IActionListener() {
 			private int count = 0;
 
 			public void callback() {
 				final KeyValueBean bean = new KeyValueBean();
 				bean.setKey("key" + ++count); //$NON-NLS-1$
 				bean.setValue("newValue"); //$NON-NLS-1$
 				properties.add(bean);
 				tableProperties.updateFromModel();
 				tableProperties.setSelection(bean);
 				((Table) tableProperties.getUIControl()).showSelection();
 			}
 		});
 
 		toggleDoubleClick.setText("Handle &Double Click"); //$NON-NLS-1$
 		toggleDoubleClick.addListener(new IActionListener() {
 			public void callback() {
 				if (toggleDoubleClick.isSelected()) {
 					tableProperties.addDoubleClickListener(doubleClickListener);
 				} else {
 					tableProperties.removeDoubleClickListener(doubleClickListener);
 				}
 			}
 		});
 
 		buttonSave.setText("&Save"); //$NON-NLS-1$
 		buttonSave.addListener(new IActionListener() {
 			public void callback() {
 				valueBean.update();
 				tableProperties.updateFromModel();
 			}
 		});
 	}
 
 	// helping classes
 	// ////////////////
 
 	/**
 	 * Bean for holding a pair of Strings.
 	 */
 	public static final class KeyValueBean extends AbstractBean {
 
 		private KeyValueBean bean;
 		private String tempKey;
 		private String tempValue;
 
 		public String getKey() {
 			return tempKey;
 		}
 
 		public void setKey(final String key) {
 			this.tempKey = key;
 		}
 
 		public String getValue() {
 			return tempValue;
 		}
 
 		public void setValue(final String value) {
 			this.tempValue = value;
 		}
 
 		public void setBean(final KeyValueBean bean) {
 			this.bean = bean;
 			setKey(bean.getKey());
 			setValue(bean.getValue());
 		}
 
 		public void update() {
 			bean.setKey(tempKey);
 			bean.setValue(tempValue);
 		}
 	}
 
 	/**
 	 * Show a {@link MessageDialog} on double click.
 	 */
 	private final class DoubleClickListener implements IActionListener {
 
 		public void callback() {
 			final Shell shell = WorkbenchFacade.getInstance().getActiveShell();
 			final String title = "Information"; //$NON-NLS-1$
 			String message = "The key ''{0}'' is selected and has the value ''{1}''"; //$NON-NLS-1$
 			message = NLS.bind(message, valueBean.getKey(), valueBean.getValue());
 			RienaMessageDialog.openInformation(shell, title, message);
 		}
 
 	}
 
 	private final class MyTableFormatter extends TableFormatter {
 
 		@Override
		public Object getForeground(final Object element, final Object cellElement, final int columnIndex) {
 			if (element instanceof KeyValueBean) {
 				final KeyValueBean keyValue = (KeyValueBean) element;
 				if (isNewKey(keyValue)) {
 					return LnfManager.getLnf().getColor("red");
 				}
 				if ((columnIndex == 1) && isNewValue(keyValue)) {
 					return LnfManager.getLnf().getColor("blue");
 				}
 			}
			return super.getForeground(element, cellElement, columnIndex);
 		}
 	}
 
 }
