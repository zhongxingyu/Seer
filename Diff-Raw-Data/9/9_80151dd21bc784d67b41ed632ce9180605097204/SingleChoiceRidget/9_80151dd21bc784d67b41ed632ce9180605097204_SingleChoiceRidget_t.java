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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Arrays;
 import java.util.List;
 
 import org.eclipse.core.databinding.Binding;
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateListStrategy;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.databinding.observable.value.WritableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 
 import org.eclipse.riena.core.util.ListenerList;
 import org.eclipse.riena.ui.ridgets.IChoiceRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.ISingleChoiceRidget;
 import org.eclipse.riena.ui.ridgets.listener.ISelectionListener;
 import org.eclipse.riena.ui.ridgets.swt.AbstractSWTRidget;
 import org.eclipse.riena.ui.ridgets.swt.MarkerSupport;
 import org.eclipse.riena.ui.swt.ChoiceComposite;
 import org.eclipse.riena.ui.swt.lnf.LnFUpdater;
 
 /**
  * Ridget for a {@link ChoiceComposite} widget with single selection.
  */
 public class SingleChoiceRidget extends AbstractSWTRidget implements ISingleChoiceRidget {
 
 	private final static LnFUpdater LNF_UPDATER = new LnFUpdater();
 
 	/** The list of available options. */
 	private final WritableList optionsObservable;
 	/** The selected option. */
 	private final WritableValue selectionObservable;
 
 	private Binding optionsBinding;
 	private Binding selectionBinding;
 	private String[] optionLabels;
 
 	/** A list of selection listeners. */
 	private ListenerList<ISelectionListener> selectionListeners;
 
 	public SingleChoiceRidget() {
 		optionsObservable = new WritableList();
 		selectionObservable = new WritableValue();
 		selectionObservable.addChangeListener(new IChangeListener() {
 			public void handleChange(ChangeEvent event) {
 				disableMandatoryMarkers(hasInput());
 			}
 		});
 		addPropertyChangeListener(IRidget.PROPERTY_ENABLED, new PropertyChangeListener() {
 			public void propertyChange(PropertyChangeEvent evt) {
 				updateSelection(getUIControl());
 			}
 		});
 	}
 
 	@Override
 	protected void bindUIControl() {
 		if (optionsBinding != null) {
 			createChildren(getUIControl());
 		}
 	}
 
 	@Override
 	protected void checkUIControl(Object uiControl) {
 		AbstractSWTRidget.assertType(uiControl, ChoiceComposite.class);
 		if (uiControl != null) {
 			ChoiceComposite composite = (ChoiceComposite) uiControl;
 			Assert.isTrue(!composite.isMultipleSelection(), "expected single selection ChoiceComposite"); //$NON-NLS-1$
 		}
 	}
 
 	@Override
 	protected void unbindUIControl() {
 		super.unbindUIControl();
 		Composite control = getUIControl();
 		disposeChildren(control);
 	}
 
 	// public methods
 	// ///////////////
 
 	@Override
 	public ChoiceComposite getUIControl() {
 		return (ChoiceComposite) super.getUIControl();
 	}
 
 	public void bindToModel(IObservableList optionValues, IObservableValue selectionValue) {
 		Assert.isNotNull(optionValues, "optionValues"); //$NON-NLS-1$
 		Assert.isNotNull(selectionValue, "selectionValue"); //$NON-NLS-1$
 		bindToModel(optionValues, null, selectionValue);
 	}
 
 	public void bindToModel(Object listHolder, String listPropertyName, Object selectionHolder,
 			String selectionPropertyName) {
 		Assert.isNotNull(listHolder, "listHolder"); //$NON-NLS-1$
 		Assert.isNotNull(listPropertyName, "listPropertyName"); //$NON-NLS-1$
 		Assert.isNotNull(selectionHolder, "selectionHolder"); //$NON-NLS-1$
 		Assert.isNotNull(selectionPropertyName, "selectionPropertyName"); //$NON-NLS-1$
 		IObservableList optionValues = PojoObservables.observeList(listHolder, listPropertyName);
 		IObservableValue selectionValue = PojoObservables.observeValue(selectionHolder, selectionPropertyName);
 		bindToModel(optionValues, null, selectionValue);
 	}
 
 	public void bindToModel(List<? extends Object> optionValues, List<String> optionLabels, Object selectionHolder,
 			String selectionPropertyName) {
 		Assert.isNotNull(optionValues, "optionValues"); //$NON-NLS-1$
 		Assert.isNotNull(selectionHolder, "selectionHolder"); //$NON-NLS-1$
 		Assert.isNotNull(selectionPropertyName, "selectionPropertyName"); //$NON-NLS-1$
 		IObservableList list = new WritableList(optionValues, null);
 		IObservableValue selection = PojoObservables.observeValue(selectionHolder, selectionPropertyName);
 		bindToModel(list, optionLabels, selection);
 	}
 
 	@Override
 	public void updateFromModel() {
 		assertIsBoundToModel();
 		super.updateFromModel();
 		optionsBinding.updateModelToTarget();
 		Object oldSelection = selectionObservable.getValue();
 		selectionBinding.updateModelToTarget();
 		ChoiceComposite control = getUIControl();
 		disposeChildren(control);
 		createChildren(control);
 		// remove unavailable element and re-apply selection
 		Object newSelection = oldSelection;
 		if (newSelection != null && !optionsObservable.contains(newSelection)) {
 			selectionObservable.setValue(null);
 			newSelection = null;
 		}
 		firePropertyChange(PROPERTY_SELECTION, oldSelection, newSelection);
 	}
 
 	public Object getSelection() {
 		return selectionObservable.getValue();
 	}
 
 	public void setSelection(Object candidate) {
 		assertIsBoundToModel();
 		if (candidate != null && !optionsObservable.contains(candidate)) {
 			throw new BindingException("candidate not in option list: " + candidate); //$NON-NLS-1$
 		}
 		Object oldSelection = selectionObservable.getValue();
 		selectionObservable.setValue(candidate);
 		updateSelection(getUIControl());
 		firePropertyChange(PROPERTY_SELECTION, oldSelection, candidate);
 	}
 
 	public IObservableList getObservableList() {
 		return optionsObservable;
 	}
 
 	@Override
 	public final boolean isDisableMandatoryMarker() {
 		return hasInput();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 1.2
 	 */
 	public void addSelectionListener(ISelectionListener selectionListener) {
 		Assert.isNotNull(selectionListener, "selectionListener is null"); //$NON-NLS-1$
 		if (selectionListeners == null) {
 			selectionListeners = new ListenerList<ISelectionListener>(ISelectionListener.class);
 			addPropertyChangeListener(IChoiceRidget.PROPERTY_SELECTION, new PropertyChangeListener() {
 				public void propertyChange(PropertyChangeEvent evt) {
 					notifySelectionListeners(Arrays.asList(evt.getOldValue()), Arrays.asList(evt.getNewValue()));
 				}
 			});
 		}
 		selectionListeners.add(selectionListener);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * 
 	 * @since 1.2
 	 */
 	public void removeSelectionListener(ISelectionListener selectionListener) {
 		if (selectionListeners != null) {
 			selectionListeners.remove(selectionListener);
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void assertIsBoundToModel() {
 		if (optionsBinding == null || selectionBinding == null) {
 			throw new BindingException("ridget not bound to model"); //$NON-NLS-1$
 		}
 	}
 
 	private void bindToModel(IObservableList optionValues, List<String> optionLabels, IObservableValue selectionValue) {
 		if (optionLabels != null) {
 			String msg = "Mismatch between number of optionValues and optionLabels"; //$NON-NLS-1$
 			Assert.isLegal(optionValues.size() == optionLabels.size(), msg);
 		}
 
 		unbindUIControl();
 
 		// clear observables as they may be bound to another model
 		// must dispose old bindings first to avoid updating the old model
 		if (optionsBinding != null) {
 			optionsBinding.dispose();
 			optionsBinding = null;
 			optionsObservable.clear();
 		}
 		if (selectionBinding != null) {
 			selectionBinding.dispose();
 			selectionBinding = null;
 			selectionObservable.setValue(null);
 		}
 
 		// set up new binding
 		DataBindingContext dbc = new DataBindingContext();
 		optionsBinding = dbc.bindList(optionsObservable, optionValues, new UpdateListStrategy(
 				UpdateListStrategy.POLICY_UPDATE), new UpdateListStrategy(UpdateListStrategy.POLICY_ON_REQUEST));
 		selectionBinding = dbc.bindValue(selectionObservable, selectionValue, new UpdateValueStrategy(
 				UpdateValueStrategy.POLICY_UPDATE), new UpdateValueStrategy(UpdateValueStrategy.POLICY_ON_REQUEST));
 		if (optionLabels != null) {
 			this.optionLabels = optionLabels.toArray(new String[optionLabels.size()]);
 		} else {
 			this.optionLabels = null;
 		}
 
 		bindUIControl();
 	}
 
 	private void createChildren(Composite control) {
 		if (control != null && !control.isDisposed()) {
 			Object[] values = optionsObservable.toArray();
 			for (int i = 0; i < values.length; i++) {
 				Object value = values[i];
 				String caption = optionLabels != null ? optionLabels[i] : String.valueOf(value);
 
 				Button button = new Button(control, SWT.RADIO);
 				button.setText(caption);
 				button.setForeground(control.getForeground());
 				button.setBackground(control.getBackground());
 				button.setData(value);
 				button.addSelectionListener(new SelectionAdapter() {
 					@Override
 					public void widgetSelected(SelectionEvent e) {
 						Button button = (Button) e.widget;
 						Object data = button.getData();
 						if (button.getSelection()) {
 							if (isOutputOnly()) {
 								// silently revert UI change
 								updateSelection(getUIControl());
 							} else {
 								// this is a workaround to make composite table aware of focus changes, Bug #264627
 								SingleChoiceRidget.this.setSelection(data);
 								fireFocusIn(button.getParent());
 							}
 						}
 					}
 				});
 			}
 			updateSelection(control);
			LNF_UPDATER.updateUIControlsAfterBind(control);
 		}
 	}
 
 	private void disposeChildren(Composite control) {
 		if (control != null && !control.isDisposed()) {
 			for (Control child : control.getChildren()) {
 				child.dispose();
 			}
 		}
 	}
 
 	private void fireFocusIn(Control control) {
 		Event event = new Event();
 		event.type = SWT.FocusIn;
 		event.widget = control;
 		control.notifyListeners(SWT.FocusIn, event);
 	}
 
 	private boolean hasInput() {
 		return selectionObservable.getValue() != null;
 	}
 
 	/**
 	 * Iterates over the composite's children, disabling all buttons, except the
 	 * one that has value as it's data element. If the ridget is not enabled, it
 	 * may deselect all buttons, as mandated by
 	 * {@link MarkerSupport#isHideDisabledRidgetContent()}.
 	 */
 	private void updateSelection(Composite control) {
 		boolean canSelect = isEnabled() || !MarkerSupport.isHideDisabledRidgetContent();
 		if (control != null && !control.isDisposed()) {
 			Object value = selectionObservable.getValue();
 			for (Control child : control.getChildren()) {
 				Button button = (Button) child;
 				boolean isSelected = canSelect && (value != null) && (value.equals(child.getData()));
 				button.setSelection(isSelected);
 			}
 		}
 	}
 
 	private void notifySelectionListeners(List<?> oldSelectionList, List<?> newSelectionList) {
 		if (selectionListeners != null) {
 			org.eclipse.riena.ui.ridgets.listener.SelectionEvent event = new org.eclipse.riena.ui.ridgets.listener.SelectionEvent(
 					this, oldSelectionList, newSelectionList);
 			for (ISelectionListener listener : selectionListeners.getListeners()) {
 				listener.ridgetSelected(event);
 			}
 		}
 	}
 }
