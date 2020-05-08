 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.swt;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.value.ComputedValue;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.IValueChangeListener;
 import org.eclipse.core.databinding.observable.value.ValueChangeEvent;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.layout.TableColumnLayout;
 import org.eclipse.jface.viewers.ColumnWeightData;
 import org.eclipse.riena.internal.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.ridgets.AbstractCompositeRidget;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.databinding.UnboundPropertyWritableList;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 
 /**
  * TODO [ev] docs
  */
 public class MasterDetailsRidget extends AbstractCompositeRidget implements IMasterDetailsRidget<Object> {
 
 	private String[] columnHeaders;
 	private IObservableList rowObservables;
 	private Class<? extends Object> rowBeanClass;
 	private String[] renderingMethods;
 
 	private IMasterDetailsDelegate delegate;
 	private DataBindingContext dbc;
 
 	public void setDelegate(IMasterDetailsDelegate delegate) {
 		Assert.isLegal(this.delegate == null, "setDelegate can only be called once");
 		Assert.isLegal(delegate != null, "delegate cannot be null");
 		this.delegate = delegate;
 		delegate.configureRidgets(this);
 	}
 
 	@Override
 	public MasterDetailsComposite getUIControl() {
 		return (MasterDetailsComposite) super.getUIControl();
 	}
 
 	@Override
 	public void setUIControl(Object uiControl) {
 		System.out.println("MasterDetailsRidget.setUIControl()");
 		AbstractSWTRidget.assertType(uiControl, MasterDetailsComposite.class);
 		super.setUIControl(uiControl);
 		bindUIControl();
 	}
 
 	public void bindToModel(IObservableList rowBeansObservable, Class<? extends Object> rowBeanClass,
 			String[] columnPropertyNames, String[] columnHeaders) {
 		System.out.println("MasterDetailsRidget.bindToModel()");
 
 		// unbindUIControl();
 
 		this.rowBeanClass = rowBeanClass;
 		rowObservables = rowBeansObservable;
 		renderingMethods = new String[columnPropertyNames.length];
 		System.arraycopy(columnPropertyNames, 0, renderingMethods, 0, renderingMethods.length);
 
 		if (columnHeaders != null) {
 			this.columnHeaders = new String[columnHeaders.length];
 			System.arraycopy(columnHeaders, 0, this.columnHeaders, 0, this.columnHeaders.length);
 		} else {
 			this.columnHeaders = null;
 		}
 
 		bindUIControl();
 	}
 
 	public void bindToModel(Object listBean, String listPropertyName, Class<? extends Object> rowBeanClass,
 			String[] columnPropertyNames, String[] headerNames) {
 		IObservableList listObservableValue = new UnboundPropertyWritableList(listBean, listPropertyName);
 		bindToModel(listObservableValue, rowBeanClass, columnPropertyNames, headerNames);
 	}
 
 	@Override
 	public final void configureRidgets() {
 		System.out.println("MasterDetailsRidget.configureRidgets()");
 
 		final ITableRidget tableRidget = getTableRidget();
 		getAddButtonRidget().addListener(new IActionListener() {
 			public void callback() {
 				getTableRidget().clearSelection();
 				clearDetails();
 				getUpdateButtonRidget().setEnabled(true);
 			}
 		});
 		getRemoveButtonRidget().addListener(new IActionListener() {
 			public void callback() {
 				Object selection = getMasterSelection();
 				Assert.isNotNull(selection);
 				rowObservables.remove(selection);
 				getTableRidget().clearSelection();
 				clearDetails();
 			}
 		});
 		getUpdateButtonRidget().addListener(new IActionListener() {
 			public void callback() {
 				copyFromDetailsToMaster();
 			}
 		});
 
 		final IObservableValue viewerSelection = tableRidget.getSingleSelectionObservable();
 		IObservableValue hasSelection = new ComputedValue(Boolean.TYPE) {
 			@Override
 			protected Object calculate() {
 				return Boolean.valueOf(viewerSelection.getValue() != null);
 			}
 		};
 		viewerSelection.addValueChangeListener(new IValueChangeListener() {
 			public void handleValueChange(ValueChangeEvent event) {
 				updateDetails();
 			}
 		});
 
 		Assert.isLegal(dbc == null);
 		dbc = new DataBindingContext();
 		bindEnablementToValue(dbc, getRemoveButtonRidget(), hasSelection);
 		bindEnablementToValue(dbc, getUpdateButtonRidget(), hasSelection);
 	}
 
 	public void addToMaster() {
 		Object newValue = delegate.createWorkingCopyObject();
 		copyBean(getWorkingCopy(), newValue);
 		rowObservables.add(newValue);
 		getTableRidget().setSelection((Object) newValue);
 		getUIControl().getTable().showSelection();
 	}
 
 	public void clearDetails() {
 		copyBean(null, getWorkingCopy());
 		updateDetails();
 	}
 
 	public boolean copyFromDetailsToMaster() {
 		Object selection = getMasterSelection();
 		if (selection != null) {
 			copyBean(getWorkingCopy(), selection);
 		} else {
 			addToMaster();
 		}
 		return true;
 	}
 
 	public void copyFromMasterToDetails() {
 		updateDetails();
 	}
 
 	public Object getMasterSelection() {
 		return getTableRidget().getSingleSelectionObservable().getValue();
 	}
 
 	public Object getWorkingCopy() {
 		return delegate.getWorkingCopy();
 	}
 
 	public boolean isDetailsChanged() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public boolean isInputValid() {
 		return true;
 	}
 
 	public void selectionChanged(Object newSelection) {
 		// TODO Auto-generated method stub
 	}
 
 	public void setSelection(Object newSelection, boolean triggerChanged) {
 		// TODO Auto-generated method stub
 	}
 
 	public final Object createWorkingCopyObject() {
 		return delegate.createWorkingCopyObject();
 	}
 
 	public final Object copyBean(final Object source, final Object target) {
 		return delegate.copyBean(source, target);
 	}
 
 	public final void updateDetails() {
 		Object selection = getMasterSelection();
 		copyBean(selection, getWorkingCopy());
 		delegate.updateDetails(this);
 	}
 
 	// helping methods
 	//////////////////
 
 	private void bindEnablementToValue(DataBindingContext dbc, IMarkableRidget ridget, IObservableValue value) {
 		Assert.isNotNull(ridget);
 		Assert.isNotNull(value);
 		dbc.bindValue(BeansObservables.observeValue(ridget, IMarkableRidget.PROPERTY_ENABLED), value, null, null);
 	}
 
 	private void bindUIControl() {
 		System.out.println("MasterDetailsRidget.bindUIControl()");
 		MasterDetailsComposite control = getUIControl();
 		if (control != null && rowObservables != null) {
 			prepareTable(control);
 			ITableRidget tableRidvget = getTableRidget();
 			tableRidvget.bindToModel(rowObservables, rowBeanClass, renderingMethods, columnHeaders);
 		}
 	}
 
 	private ITableRidget getTableRidget() {
 		return (ITableRidget) getRidget("mdTable"); //$NON-NLS-1$
 	}
 
 	private IActionRidget getAddButtonRidget() {
 		return (IActionRidget) getRidget("mdAddButton"); //$NON-NLS-1$
 	}
 
 	private IActionRidget getRemoveButtonRidget() {
 		return (IActionRidget) getRidget("mdRemoveButton"); //$NON-NLS-1$
 	}
 
 	private IActionRidget getUpdateButtonRidget() {
 		return (IActionRidget) getRidget("mdUpdateButton"); //$NON-NLS-1$
 	}
 
 	private void prepareTable(MasterDetailsComposite control) {
 		int numColumns = renderingMethods.length;
 		Table tableWidget = control.getTable();
 		if (tableWidget.getColumnCount() != numColumns) {
 			for (TableColumn column : tableWidget.getColumns()) {
 				column.dispose();
 			}
 			TableColumnLayout layout = new TableColumnLayout();
 			for (int i = 0; i < numColumns; i++) {
 				TableColumn column = new TableColumn(tableWidget, SWT.NONE);
 				layout.setColumnData(column, new ColumnWeightData(10));
 			}
			Composite tableComposite = tableWidget.getParent();
			tableComposite.setLayout(layout);
			tableComposite.layout(true);
 		}
 	}
 
 }
