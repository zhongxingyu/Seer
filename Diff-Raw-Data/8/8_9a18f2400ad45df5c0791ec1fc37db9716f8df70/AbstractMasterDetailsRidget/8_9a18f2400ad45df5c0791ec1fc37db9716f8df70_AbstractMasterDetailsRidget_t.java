 /*******************************************************************************
  * Copyright (c) 2007, 2012 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.ridgets.swt;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.databinding.BindingException;
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.core.databinding.beans.PojoObservables;
 import org.eclipse.core.databinding.observable.ChangeEvent;
 import org.eclipse.core.databinding.observable.IChangeListener;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.value.ComputedValue;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.widgets.Control;
 
 import org.eclipse.riena.core.marker.IMarkable;
 import org.eclipse.riena.ui.core.marker.ErrorMarker;
 import org.eclipse.riena.ui.core.marker.MandatoryMarker;
 import org.eclipse.riena.ui.ridgets.AbstractCompositeRidget;
 import org.eclipse.riena.ui.ridgets.IActionListener;
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IBasicMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMarkableRidget;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsActionRidgetFacade;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsDelegate;
 import org.eclipse.riena.ui.ridgets.IMasterDetailsRidget;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IRidgetContainer;
 import org.eclipse.riena.ui.ridgets.ITableRidget;
 import org.eclipse.riena.ui.ridgets.annotation.processor.RidgetContainerAnnotationProcessor;
 import org.eclipse.riena.ui.swt.AbstractMasterDetailsComposite;
 import org.eclipse.riena.ui.swt.MasterDetailsComposite;
 
 /**
  * Common functionality that is shared between implementations of the {@link IMasterDetailsRidget}.
  * <p>
  * This class defines several widget-specific abstract methods, which must be implemented by clients. It is expected that clients will write widget-specific
  * subclass of {@link AbstractMasterDetailsComposite}.
  * 
  * @since 1.2
  */
 public abstract class AbstractMasterDetailsRidget extends AbstractCompositeRidget implements IMasterDetailsRidget {
 
 	private static final GlobalMandatoryMarker GLOBAL_MARKER = new GlobalMandatoryMarker();
 
 	private IObservableList rowObservables;
 	private IMasterDetailsDelegate delegate;
 	private DataBindingContext dbc;
 
 	/**
 	 * A blank model entry for clearing the details area. Will be initialized lazily.
 	 */
 	private Object blankEntry;
 	/**
 	 * The object we are currently editing; null if not editing
 	 */
 	private Object editable;
 	/**
 	 * All ridgets from the details area.
 	 */
 	private IRidgetContainer detailRidgets;
 	/**
 	 * If true, Apply can only complete when there are no errors in the details.
 	 */
 	private boolean applyRequiresNoErrors;
 	/**
 	 * If true, Apply can only complete when there are no enabled mandatory markers in the details.
 	 */
 	private boolean applyRequiresNoMandatories;
 	/**
 	 * If true, successful completion of an 'Apply' action will trigger creating a new entry.
 	 */
 	private boolean applyTriggersNew;
 	/**
 	 * True when the details area is enabled, false otherwise.
 	 */
 	private boolean detailsEnabled;
 	/**
 	 * If true, change events will not be processed.
 	 */
 	private boolean ignoreChanges;
 	/**
 	 * True when direct writing is on. In that case any value change will be immediately be applied back to the (master) editable.
 	 */
 	private boolean isDirectWriting;
 	/**
 	 * True when mandatory and error markers should be hidden when the user invokes the 'New' action or an entry is suggested programmatically. The global
 	 * mandatory marker is excluded from hiding. The markers will re-enable as soon as the user changes a value in the details area.
 	 * <p>
 	 * This is the user preference for this ridget not the current state.
 	 */
 	private boolean isHideMarkersOnNew;
 	/**
 	 * The ridget state of hiding (true) or not hiding markers.
 	 */
 	private boolean isHidingMarkersOnNew;
 	/**
 	 * True if the global mandatory marker should be visible, false otherwise.
 	 * 
 	 * @see GlobalMandatoryMarker
 	 */
 	private boolean isShowingGlobalMarker;
 	/**
 	 * True when the editable was suggested via #suggestNewEntry. Suggested editables are always considered modified.
 	 */
 	private boolean isSuggestedEditable;
 	/**
 	 * If true, the 'Remove' action will be enabled while editing a new entry. Hitting Remove will abort editing the new entry and restore the last selection.
 	 */
 	private boolean removeCancelsNew;
 	/**
 	 * If true, successful completion of an 'Remove' action will trigger creating a new entry.
 	 */
 	private boolean removeTriggersNew;
 	/**
 	 * If removeCancelsNew is enabled, this variable stores the element that was selected before pressing 'New'. Null if no selection is stored.
 	 */
 	private StoredSelection preNewSelection;
 
 	/**
 	 * Facade for convenient access to {@link IActionRidget}s
 	 */
 	private ActionRidgetFacade actionRidgetFacade;
 
 	public AbstractMasterDetailsRidget() {
 		actionRidgetFacade = new ActionRidgetFacade();
 		addPropertyChangeListener(null, new PropertyChangeListener() {
 			public void propertyChange(final PropertyChangeEvent evt) {
 				final String propertyName = evt.getPropertyName();
 				if (!hasApplyButton() || isDirectWriting || ignoreChanges || delegate == null
 						|| editable == null
 						// ignore these events:
 						|| (!applyRequiresNoErrors && !applyRequiresNoMandatories && IBasicMarkableRidget.PROPERTY_MARKER.equals(propertyName))
 						|| IRidget.PROPERTY_ENABLED.equals(propertyName)
 						|| "textInternal".equals(propertyName) //$NON-NLS-1$
 						|| IMarkableRidget.PROPERTY_OUTPUT_ONLY.equals(propertyName) || IBasicMarkableRidget.PROPERTY_MARKER_HIDING.equals(propertyName)
 						|| (IBasicMarkableRidget.PROPERTY_MARKER.equals(propertyName) && getApplyButtonRidget() == evt.getSource())) {
 					return;
 				}
 				// traceEvent(evt);
 				updateMarkers(evt);
 				updateApplyButton();
 			}
 		});
 	}
 
 	public final void bindToModel(final IObservableList rowObservables, final Class<? extends Object> rowClass, final String[] columnPropertyNames,
 			final String[] columnHeaders) {
 		this.rowObservables = rowObservables;
 		bindTableToModel(rowObservables, rowClass, columnPropertyNames, columnHeaders);
 	}
 
 	public final void bindToModel(final Object listHolder, final String listPropertyName, final Class<? extends Object> rowClass,
 			final String[] columnPropertyNames, final String[] headerNames) {
 		IObservableList rowObservableList;
 		if (AbstractSWTWidgetRidget.isBean(rowClass)) {
 			rowObservableList = BeansObservables.observeList(listHolder, listPropertyName);
 		} else {
 			rowObservableList = PojoObservables.observeList(listHolder, listPropertyName);
 		}
 		bindToModel(rowObservableList, rowClass, columnPropertyNames, headerNames);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public boolean canSuggest() {
 		boolean result = true;
 		if (areDetailsChanged()) {
 			result = getUIControl().confirmDiscardChanges();
 		}
 		return result;
 	}
 
 	@Override
 	public void configureRidgets() {
 
 		configureTableRidget();
 
 		if (hasNewButton()) {
 			getNewButtonRidget().addListener(new IActionListener() {
 				public void callback() {
 					if (canAdd()) {
 						handleAdd();
 					}
 				}
 			});
 		}
 
 		if (hasRemoveButton()) {
 			getRemoveButtonRidget().addListener(new IActionListener() {
 				public void callback() {
 					if (hasPreNewSelection()) {
 						if (canCancel()) {
 							handleCancel();
 						}
 					} else if (canRemove()) {
 						handleRemove();
 					}
 				}
 			});
 		}
 
 		if (hasApplyButton()) {
 			getApplyButtonRidget().addListener(new IActionListener() {
 				public void callback() {
 					if (canApply()) {
 						handleApply();
 					}
 				}
 			});
 		}
 
 		setEnabled(false, detailsEnabled);
 
 		final IObservableValue viewerSelection = getSelectionObservable();
 
 		Assert.isLegal(dbc == null);
 		if (hasRemoveButton()) {
 			dbc = new DataBindingContext();
 			bindEnablementToValue(dbc, getRemoveButtonRidget(), new ComputedValue(Boolean.TYPE) {
 				@Override
 				protected Object calculate() {
 					return Boolean.valueOf(viewerSelection.getValue() != null);
 				}
 			});
 		}
 
 		final PropertyChangeListener directWritingPCL = new DirectWritingPropertyChangeListener();
 		for (final IRidget ridget : getDetailRidgetContainer().getRidgets()) {
 			ridget.addPropertyChangeListener(directWritingPCL);
 		}
 	}
 
 	private IRidgetContainer getDetailRidgetContainer() {
 		if (detailRidgets == null) {
 			detailRidgets = new DetailRidgetContainer();
 		}
 		return detailRidgets;
 	}
 
 	public final IMasterDetailsDelegate getDelegate() {
 		return this.delegate;
 	}
 
 	public final Object getSelection() {
 		return getSelectionObservable().getValue();
 	}
 
 	@Override
 	public AbstractMasterDetailsComposite getUIControl() {
 		return (AbstractMasterDetailsComposite) super.getUIControl();
 	}
 
 	public boolean isApplyRequiresNoErrors() {
 		return applyRequiresNoErrors;
 	}
 
 	public boolean isApplyRequiresNoMandatories() {
 		return applyRequiresNoMandatories;
 	}
 
 	public boolean isApplyTriggersNew() {
 		return applyTriggersNew && hasNewButton();
 	}
 
 	public boolean isDirectWriting() {
 		return isDirectWriting;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public boolean isHideMandatoryAndErrorMarkersOnNewEntries() {
 		return isHideMarkersOnNew;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public boolean isRemoveCancelsNew() {
 		return removeCancelsNew && hasRemoveButton();
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public boolean isRemoveTriggersNew() {
 		return removeTriggersNew && hasRemoveButton();
 	}
 
 	public void setApplyRequiresNoErrors(final boolean requiresNoErrors) {
 		if (applyRequiresNoErrors != requiresNoErrors) {
 			applyRequiresNoErrors = requiresNoErrors;
 			updateApplyButton();
 		}
 	}
 
 	public void setApplyRequiresNoMandatories(final boolean requiresNoMandatories) {
 		if (applyRequiresNoMandatories != requiresNoMandatories) {
 			applyRequiresNoMandatories = requiresNoMandatories;
 			updateApplyButton();
 		}
 	}
 
 	public void setApplyTriggersNew(final boolean triggersNew) {
 		applyTriggersNew = triggersNew;
 	}
 
 	public void setColumnWidths(final Object[] widths) {
 		((ITableRidget) getTableRidget()).setColumnWidths(widths);
 	}
 
 	public final void setDelegate(final IMasterDetailsDelegate delegate) {
 		Assert.isLegal(this.delegate == null, "setDelegate can only be called once"); //$NON-NLS-1$
 		Assert.isLegal(delegate != null, "delegate cannot be null"); //$NON-NLS-1$
 		this.delegate = delegate;
 		delegate.configureRidgets(getDetailRidgetContainer());
 		RidgetContainerAnnotationProcessor.getInstance().processAnnotations(getDetailRidgetContainer(), delegate);
 	}
 
 	public void setDirectWriting(final boolean directWriting) {
 		if (directWriting != isDirectWriting) {
 			isDirectWriting = directWriting;
 			if (hasApplyButton()) {
 				getApplyButtonRidget().setVisible(!directWriting);
 			}
 		}
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void setHideMandatoryAndErrorMarkersOnNewEntries(final boolean hideMarkers) {
 		isHideMarkersOnNew = hideMarkers;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void setRemoveCancelsNew(final boolean cancelsNew) {
 		removeCancelsNew = cancelsNew;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void setRemoveTriggersNew(final boolean triggersNew) {
 		removeTriggersNew = triggersNew;
 	}
 
 	public void setSelection(final Object newSelection) {
 		setTableSelection(newSelection);
 		handleSelectionChange(newSelection);
 		final AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			revealTableSelection();
 		}
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void suggestNewEntry() {
 		final Object entry = delegate.createMasterEntry();
 		suggestNewEntry(entry, false);
 	}
 
 	public void suggestNewEntry(final Object entry) {
 		suggestNewEntry(entry, true);
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	public void suggestNewEntry(final Object entry, final boolean treatAsDirty) {
 		ignoreChanges = true;
 		try {
 			if (!isDirectWriting) {
 				clearTableSelection();
 				editable = entry;
 				delegate.prepareItemSelected(editable);
 				updateMarkers(isHideMarkersOnNew, isShowGlobalMarker());
 				setEnabled(treatAsDirty, true);
 				isSuggestedEditable = treatAsDirty;
 				updateDetails(editable);
 				ignoreChanges = true;
 				delegate.itemSelected(editable);
 			} else {
 				editable = entry;
 				delegate.itemCreated(editable);
 				rowObservables.add(editable);
 				getTableRidget().updateFromModel();
 				setSelection(editable);
 				isSuggestedEditable = false; // direct writing -> not dirty
 				updateMarkers(isHideMarkersOnNew, isShowGlobalMarker());
 				setEnabled(false, true); // directy writing -> disable apply
 				updateDetails(editable);
 			}
 
 			// set focus to first focusable ridget in detailsarea
 			if (!setFocusToFirstDetailsRidget()) {
 				getApplyButtonRidget().requestFocus();
 			}
 
 		} finally {
 			ignoreChanges = false;
 		}
 	}
 
 	/**
 	 * 
 	 */
 	private boolean setFocusToFirstDetailsRidget() {
 		final Collection<? extends IRidget> ridgets = getDetailRidgetContainer().getRidgets();
 		final IRidget[] ridgetArray = ridgets.toArray(new IRidget[ridgets.size()]);
 
 		if (!ridgets.isEmpty()) {
 			for (int i = ridgetArray.length - 1; i >= 0; i--) {
 				final IRidget current = ridgetArray[i];
 				if (current.isEnabled() && current.isFocusable()) {
 					if (current instanceof IMarkableRidget && ((IMarkableRidget) current).isOutputOnly()) {
 						continue;
 					}
 
 					current.requestFocus();
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public final void updateApplyButton() {
 		if (!hasApplyButton()) {
 			return;
 		}
 		if (applyRequiresNoErrors || applyRequiresNoMandatories) {
 			// inlined for performance
 			// isEnabled = areDetailsChanged() && noErrors && noMandatories
 			final boolean isEnabled = areDetailsChanged() && (applyRequiresNoErrors ? !hasErrors(getDetailRidgetContainer()) : true)
 					&& (applyRequiresNoMandatories ? !hasMandatories(getDetailRidgetContainer()) : true);
 			getApplyButtonRidget().setEnabled(isEnabled);
 		} else {
 			getApplyButtonRidget().setEnabled(areDetailsChanged());
 		}
 		if (delegate != null) {
 			delegate.updateMasterDetailsActionRidgets(actionRidgetFacade, editable);
 		}
 	}
 
 	@Override
 	public void updateFromModel() {
 		checkDelegate();
 		super.updateFromModel();
 	}
 
 	// protected methods
 	// //////////////////
 
 	@Override
 	protected void checkUIControl(final Object uiControl) {
 		checkType(uiControl, AbstractMasterDetailsComposite.class);
 	}
 
 	protected abstract void bindTableToModel(IObservableList rowObservables, Class<? extends Object> rowClass, String[] columnPropertyNames,
 			String[] columnHeaders);
 
 	protected abstract void configureTableRidget();
 
 	protected abstract void clearTableSelection();
 
 	protected abstract Object getTableSelection();
 
 	protected abstract IObservableValue getSelectionObservable();
 
 	protected abstract void revealTableSelection();
 
 	protected abstract void setTableSelection(Object value);
 
 	protected final boolean areDetailsChanged() {
 		if (detailsEnabled) {
 			if (editable != null) {
 				return isSuggestedEditable || delegate.isChanged(editable, delegate.getWorkingCopy());
 			}
 		}
 		return false;
 	}
 
 	protected void handleSelectionChange(final Object newSelection) {
 		final IMarkableRidget table = getRidget(IMarkableRidget.class, MasterDetailsComposite.BIND_ID_TABLE);
 		final Collection<ErrorMarker> errorMarkers = table.getMarkersOfType(ErrorMarker.class);
 		for (final ErrorMarker m : errorMarkers) {
 			table.removeMarker(m);
 		}
		// trying to fix an ugly border decoration bug
		((Control) table.getUIControl()).getDisplay().update();
 
 		ignoreChanges = true;
 		try {
 			delegate.prepareItemSelected(newSelection);
			// trying to fix an ugly border decoration bug
			((Control) table.getUIControl()).getDisplay().update();
 			delegate.updateMasterDetailsActionRidgets(actionRidgetFacade, newSelection);
 			if (newSelection != null) { // selection changed
 				if (editable == null) {
 					updateMarkers(true, false); // workaround for 327177
 				}
 				editable = newSelection;
 				setEnabled(false, true);
 				updateDetails(editable);
 				updateMarkers(false, false);
 			} else { // nothing selected
 				setEnabled(false, false); // workaround for 327177
 				clearSelection();
 				setEnabled(false, false);
 				updateMarkers(false, false);
 			}
 			ignoreChanges = true;
 			delegate.itemSelected(newSelection);
 			clearPreNewSelection();
 		} finally {
 			ignoreChanges = false;
 			for (final ErrorMarker m : errorMarkers) {
 				table.addMarker(m);
 			}
			// trying to fix an ugly border decoration bug
			((Control) table.getUIControl()).getDisplay().update();
 		}
 	}
 
 	@Override
 	protected boolean isUIControlVisible() {
 		return getUIControl().isVisible();
 	}
 
 	@Override
 	protected final void updateEnabled() {
 		final AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			if (!isEnabled()) {
 				clearSelection();
 				clearTableSelection();
 				final Collection<? extends IRidget> ridgets = getRidgets();
 				for (final IRidget ridget : ridgets) {
 					ridget.setEnabled(false);
 				}
 			} else {
 				if (getTableRidget() != null) {
 					getTableRidget().setEnabled(true);
 				}
 				if (getNewButtonRidget() != null) {
 					getNewButtonRidget().setEnabled(true);
 				}
 			}
 			control.setEnabled(isEnabled());
 		}
 	}
 
 	@Override
 	protected final void updateToolTipText() {
 		final AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			control.setToolTipText(getToolTipText());
 		}
 	}
 
 	@Override
 	protected final void updateVisible() {
 		final AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			control.setVisible(!isMarkedHidden());
 		}
 	}
 
 	// helping methods
 	// ////////////////
 
 	private void assertIsBoundToModel() {
 		if (rowObservables == null) {
 			throw new BindingException("ridget not bound to model"); //$NON-NLS-1$
 		}
 	}
 
 	private void bindEnablementToValue(final DataBindingContext dbc, final IRidget ridget, final IObservableValue value) {
 		Assert.isNotNull(ridget);
 		Assert.isNotNull(value);
 		final IObservableValue ridgetObservable = BeansObservables.observeValue(ridget, IRidget.PROPERTY_ENABLED);
 		dbc.bindValue(ridgetObservable, value, null, null);
 		ridgetObservable.addChangeListener(new IChangeListener() {
 
 			public void handleChange(final ChangeEvent event) {
 				if (delegate != null) {
 					delegate.updateMasterDetailsActionRidgets(actionRidgetFacade, getTableSelection());
 				}
 			}
 		});
 
 	}
 
 	private boolean canAdd() {
 		boolean result = true;
 		if (areDetailsChanged()) {
 			result = getUIControl().confirmDiscardChanges();
 		}
 		return result;
 	}
 
 	private boolean canApply() {
 		final String reason = delegate.isValid(getDetailRidgetContainer());
 		if (reason != null) {
 			getUIControl().warnApplyFailed(reason);
 		}
 		return reason == null;
 	}
 
 	private boolean canApplyDirectly() {
 		final boolean noErrors = applyRequiresNoErrors ? !hasErrors(getDetailRidgetContainer()) : true;
 		final boolean noMandatories = applyRequiresNoMandatories ? !hasMandatories(getDetailRidgetContainer()) : true;
 		return noErrors && noMandatories && (delegate.isValid(getDetailRidgetContainer()) == null);
 	}
 
 	private boolean canCancel() {
 		boolean result = true;
 		if (areDetailsChanged()) {
 			result = getUIControl().confirmDiscardChanges();
 		}
 		return result;
 	}
 
 	private boolean canRemove() {
 		final Object selection = getSelection();
 		Assert.isNotNull(selection);
 		final String reason = delegate.isRemovable(selection);
 		if (reason != null) {
 			getUIControl().warnRemoveFailed(reason);
 			return false;
 		}
 		return getUIControl().confirmRemove(selection);
 	}
 
 	private void checkDelegate() {
 		if (delegate == null) {
 			throw new IllegalStateException("no delegate: call setDelegate(...)"); //$NON-NLS-1$
 		}
 	}
 
 	private void clearPreNewSelection() {
 		preNewSelection = null;
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void clearSelection() {
 		updateDetails(getBlankEntry());
 		editable = null;
 	}
 
 	private Object getBlankEntry() {
 		if (blankEntry == null) {
 			blankEntry = delegate.createMasterEntry();
 		}
 		return blankEntry;
 	}
 
 	private Control getDetailsControl() {
 		Control result = null;
 		final AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			result = control.getDetails();
 		}
 		return result;
 	}
 
 	private IRidget getTableRidget() {
 		// this is not necessarily an ITableRidget, can be any IRidget
 		return getRidget(AbstractMasterDetailsComposite.BIND_ID_TABLE);
 	}
 
 	private IActionRidget getNewButtonRidget() {
 		return getRidget(IActionRidget.class, AbstractMasterDetailsComposite.BIND_ID_NEW);
 	}
 
 	private IActionRidget getRemoveButtonRidget() {
 		return getRidget(IActionRidget.class, AbstractMasterDetailsComposite.BIND_ID_REMOVE);
 	}
 
 	private IActionRidget getApplyButtonRidget() {
 		return getRidget(IActionRidget.class, AbstractMasterDetailsComposite.BIND_ID_APPLY);
 	}
 
 	private boolean hasErrors(final IRidgetContainer ridgetContainer) {
 		boolean foundMarker = hasErrors(ridgetContainer.getRidgets());
 		if (!foundMarker) {
 			final Iterator<? extends IRidget> iter = ridgetContainer.getRidgets().iterator();
 			while (!foundMarker && iter.hasNext()) {
 				final IRidget ridget = iter.next();
 				if (ridget instanceof IRidgetContainer) {
 					foundMarker = hasErrors((IRidgetContainer) ridget);
 				}
 			}
 		}
 		return foundMarker;
 	}
 
 	private boolean hasErrors(final Collection<? extends IRidget> ridgets) {
 		for (final IRidget ridget : ridgets) {
 			if (ridget instanceof IMarkableRidget) {
 				final IMarkableRidget markableRidget = (IMarkableRidget) ridget;
 				if (markableRidget.isEnabled() && markableRidget.getMarkersOfType(ErrorMarker.class).size() > 0) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean hasMandatories(final IRidgetContainer ridgetContainer) {
 		boolean foundMarker = hasMandatories(ridgetContainer.getRidgets());
 		if (!foundMarker) {
 			final Iterator<? extends IRidget> iter = ridgetContainer.getRidgets().iterator();
 			while (!foundMarker && iter.hasNext()) {
 				final IRidget ridget = iter.next();
 				if (ridget instanceof IRidgetContainer) {
 					foundMarker = hasMandatories((IRidgetContainer) ridget);
 				}
 			}
 		}
 		return foundMarker;
 	}
 
 	private boolean hasMandatories(final Collection<? extends IRidget> ridgets) {
 		for (final IRidget ridget : ridgets) {
 			if (ridget instanceof IMarkableRidget) {
 				final IMarkableRidget markableRidget = (IMarkableRidget) ridget;
 				if (markableRidget.isEnabled()) {
 					for (final MandatoryMarker marker : markableRidget.getMarkersOfType(MandatoryMarker.class)) {
 						if (!marker.isDisabled()) {
 							return true;
 						}
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	private boolean hasNewButton() {
 		return getNewButtonRidget() != null;
 	}
 
 	private boolean hasPreNewSelection() {
 		return preNewSelection != null;
 	}
 
 	private boolean hasRemoveButton() {
 		return getRemoveButtonRidget() != null;
 	}
 
 	private boolean hasApplyButton() {
 		return getApplyButtonRidget() != null;
 	}
 
 	private boolean isShowGlobalMarker() {
 		return !delegate.isValidMaster(this);
 	}
 
 	private void setEnabled(final boolean applyEnabled, final boolean detailsEnabled) {
 		ignoreChanges = true;
 		try {
 			if (hasApplyButton()) {
 				getApplyButtonRidget().setEnabled(applyEnabled);
 			}
 			if (!applyEnabled) {
 				isSuggestedEditable = false;
 			}
 			this.detailsEnabled = detailsEnabled;
 			for (final IRidget ridget : getDetailRidgetContainer().getRidgets()) {
 				ridget.setEnabled(detailsEnabled);
 			}
 		} finally {
 			ignoreChanges = false;
 		}
 	}
 
 	private void setFocusToDetails() {
 		final Control focusable = getDetailsControl();
 		if (focusable != null) {
 			focusable.getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					if (!focusable.isDisposed()) {
 						clearTableSelection();
 						focusable.setFocus();
 					}
 				}
 			});
 		}
 	}
 
 	private void setFocusToTable() {
 		final AbstractMasterDetailsComposite control = getUIControl();
 		if (control != null) {
 			final Control table = control.getTable();
 			table.getDisplay().asyncExec(new Runnable() {
 				public void run() {
 					if (!table.isDisposed()) {
 						table.setFocus();
 						// this has to be after table.setFocus() otherwise
 						// the 'focus' rectangle is sometimes lost
 						clearTableSelection();
 					}
 				}
 			});
 		}
 	}
 
 	private void storePreNewSelection() {
 		Assert.isTrue(removeCancelsNew);
 		final Object tableSelection = getTableSelection();
 		if (preNewSelection == null || tableSelection != null) {
 			// the clause above ensures the last selection is kept, if 'New' button
 			// is pressed several times.
 			preNewSelection = new StoredSelection(tableSelection);
 		}
 		clearTableSelection();
 		if (hasRemoveButton()) {
 			getRemoveButtonRidget().setEnabled(true);
 		}
 	}
 
 	@SuppressWarnings("unused")
 	private void traceEvent(final PropertyChangeEvent evt) {
 		final String className = evt.getSource().getClass().getSimpleName();
 		System.out.println(String.format("prop: %s %s", evt.getPropertyName(), className)); //$NON-NLS-1$
 	}
 
 	/**
 	 * @since 3.0
 	 */
 	protected void updateDetails(final Object masterEntry) {
 		Assert.isNotNull(masterEntry);
 		ignoreChanges = true;
 		try {
 			delegate.copyMasterEntry(masterEntry, delegate.getWorkingCopy());
 			delegate.updateDetails(getDetailRidgetContainer());
 		} finally {
 			ignoreChanges = false;
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private synchronized void updateMarkers(final boolean hideMarkers, final boolean showGlobalMarker) {
 		if (isHidingMarkersOnNew != hideMarkers) {
 			isHidingMarkersOnNew = hideMarkers;
 			for (final IRidget ridget : getDetailRidgetContainer().getRidgets()) {
 				if (ridget instanceof IBasicMarkableRidget) {
 					final IBasicMarkableRidget markableRidget = (IBasicMarkableRidget) ridget;
 					if (isHidingMarkersOnNew) {
 						if (showGlobalMarker) {
 							markableRidget.hideMarkersOfType(ErrorMarker.class);
 						} else {
 							markableRidget.hideMarkersOfType(ErrorMarker.class, MandatoryMarker.class);
 						}
 					} else {
 						markableRidget.showMarkersOfType(ErrorMarker.class, MandatoryMarker.class);
 					}
 				}
 			}
 		}
 		if (isShowingGlobalMarker != showGlobalMarker) {
 			isShowingGlobalMarker = showGlobalMarker;
 			for (final IRidget ridget : getDetailRidgetContainer().getRidgets()) {
 				if (!(ridget instanceof IMarkable)) {
 					continue;
 				}
 				final IMarkable markable = (IMarkable) ridget;
 				if (isShowingGlobalMarker) {
 					markable.addMarker(GLOBAL_MARKER);
 				} else {
 					markable.removeMarker(GLOBAL_MARKER);
 				}
 			}
 		}
 	}
 
 	private void updateMarkers(final PropertyChangeEvent evt) {
 		final String propertyName = evt.getPropertyName();
 		if (evt.getSource() == getTableRidget() || IRidget.PROPERTY_SHOWING.equals(propertyName)) {
 			return;
 		}
 		updateMarkers(false, false);
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleAdd() {
 		if (!isDirectWriting) {
 			// create the editable and update the details
 			editable = delegate.createMasterEntry();
 			delegate.itemCreated(editable);
 			updateMarkers(isHideMarkersOnNew, isShowGlobalMarker());
 			setEnabled(false, true);
 			updateDetails(editable);
 			if (isRemoveCancelsNew()) {
 				storePreNewSelection();
 			} else {
 				clearTableSelection();
 			}
 			getUIControl().getDetails().setFocus();
 		} else {
 			// create the editable, add it to the table, update the details
 			editable = delegate.createMasterEntry();
 			delegate.itemCreated(editable);
 			rowObservables.add(editable);
 			getTableRidget().updateFromModel();
 			setSelection(editable);
 			updateMarkers(isHideMarkersOnNew, isShowGlobalMarker());
 			setEnabled(false, true);
 			updateDetails(editable);
 			getUIControl().getDetails().setFocus();
 		}
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleApply() {
 		assertIsBoundToModel();
 		Assert.isNotNull(editable);
 		delegate.prepareItemApplied(editable);
 		delegate.copyWorkingCopy(delegate.getWorkingCopy(), editable);
 		if (!rowObservables.contains(editable)) { // add to table
 			rowObservables.add(editable);
 			getTableRidget().updateFromModel();
 			setTableSelection(editable);
 		} else { // update
 			getTableRidget().updateFromModel();
 		}
 		revealTableSelection();
 		delegate.itemApplied(editable);
 		clearPreNewSelection();
 		if (!isDirectWriting && isApplyTriggersNew()) {
 			handleAdd(); // automatically hit the 'new/add' button
 			setFocusToDetails();
 		} else {
 			setEnabled(false, false);
 			updateMarkers(false, false);
 			setFocusToTable();
 		}
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 * 
 	 * @since 3.0
 	 */
 	public void handleCancel() {
 		assertIsBoundToModel();
 		if (hasRemoveButton()) {
 			getRemoveButtonRidget().setEnabled(false);
 		}
 		final Object oldSelection = preNewSelection.getSelection();
 		if (oldSelection == null && !isDirectWriting() && isRemoveTriggersNew()) {
 			clearPreNewSelection();
 			handleAdd(); // automatically hit the 'new/add' button
 			setFocusToDetails();
 		} else {
 			setSelection(oldSelection);
 			clearPreNewSelection();
 		}
 	}
 
 	/**
 	 * Non API; public for testing only.
 	 */
 	public void handleRemove() {
 		assertIsBoundToModel();
 		final Object selection = getSelection();
 		Assert.isNotNull(selection);
 		rowObservables.remove(selection);
 		setEnabled(false, false); // workaround for 327177
 		clearSelection();
 		clearTableSelection();
 		getTableRidget().updateFromModel();
 		setEnabled(false, false);
 		delegate.itemRemoved(selection);
 		if (!isDirectWriting && isRemoveTriggersNew()) {
 			handleAdd(); // automatically hit the 'new/add' button
 			setFocusToDetails();
 		}
 	}
 
 	// helping classes
 	// ////////////////
 
 	private final class ActionRidgetFacade implements IMasterDetailsActionRidgetFacade {
 
 		public IActionRidget getAddActionRidget() {
 			return getRidget(IActionRidget.class, AbstractMasterDetailsComposite.BIND_ID_NEW);
 		}
 
 		public IActionRidget getApplyActionRidget() {
 			return getRidget(IActionRidget.class, AbstractMasterDetailsComposite.BIND_ID_APPLY);
 		}
 
 		public IActionRidget getRemoveActionRidget() {
 			return getRidget(IActionRidget.class, AbstractMasterDetailsComposite.BIND_ID_REMOVE);
 		}
 
 	}
 
 	/**
 	 * IRidgetContainer exposing the 'detail' ridgets only (instead of all ridgets).
 	 */
 	private final class DetailRidgetContainer implements IRidgetContainer {
 
 		private List<IRidget> detailRidgets;
 		private boolean configured = false;
 
 		public void addRidget(final String id, final IRidget ridget) {
 			throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
 		}
 
 		public boolean removeRidget(final String id) {
 			throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
 		}
 
 		public void configureRidgets() {
 			throw new UnsupportedOperationException("not supported"); //$NON-NLS-1$
 		}
 
 		public <R extends IRidget> R getRidget(final String id) {
 			return (R) AbstractMasterDetailsRidget.this.getRidget(id);
 		}
 
 		public <R extends IRidget> R getRidget(final Class<R> ridgetClazz, final String id) {
 			return AbstractMasterDetailsRidget.this.getRidget(ridgetClazz, id);
 		}
 
 		public Collection<? extends IRidget> getRidgets() {
 			if (detailRidgets == null || detailRidgets.isEmpty()) {
 				detailRidgets = getDetailRidgets();
 			}
 			return detailRidgets;
 		}
 
 		private List<IRidget> getDetailRidgets() {
 			final List<IRidget> result = new ArrayList<IRidget>(AbstractMasterDetailsRidget.this.getRidgets());
 			result.remove(getNewButtonRidget());
 			result.remove(getRemoveButtonRidget());
 			result.remove(getApplyButtonRidget());
 			result.remove(getTableRidget());
 			return result;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public void setConfigured(final boolean configured) {
 			this.configured = configured;
 		}
 
 		/**
 		 * {@inheritDoc}
 		 */
 		public boolean isConfigured() {
 			return configured;
 		}
 
 	}
 
 	private final class DirectWritingPropertyChangeListener implements PropertyChangeListener {
 		public void propertyChange(final PropertyChangeEvent evt) {
 			final String propertyName = evt.getPropertyName();
 			if (!isDirectWriting || ignoreChanges || delegate == null || editable == null
 					// ignore these events:
 					|| (!applyRequiresNoErrors && !applyRequiresNoMandatories && IBasicMarkableRidget.PROPERTY_MARKER.equals(propertyName))
 					|| IRidget.PROPERTY_ENABLED.equals(propertyName) || "textInternal".equals(propertyName) //$NON-NLS-1$
 					|| IMarkableRidget.PROPERTY_OUTPUT_ONLY.equals(propertyName) || IBasicMarkableRidget.PROPERTY_MARKER_HIDING.equals(propertyName)) {
 				return;
 			}
 			// traceEvent(evt);
 			updateMarkers(evt);
 			if (canApplyDirectly()) {
 				delegate.prepareItemApplied(editable);
 				// this is only reached when direct writing is on and one of 'interesting' events happens
 				delegate.copyWorkingCopy(delegate.getWorkingCopy(), editable);
 				delegate.itemApplied(editable);
 				getTableRidget().updateFromModel();
 				// we are already editing, so we want to invoke getTR().setSelection(editable) instead
 				// of setSelection(editable). This will just select the editable in the table.
 				setTableSelection(editable);
 			}
 		}
 	}
 
 	/**
 	 * This {@link MandatoryMarker} is always enabled and is shared with all ridgets in the details area. It is added to the ridgets in the details when the
 	 * result of {@link IMasterDetailsDelegate#isValidMaster(IMasterDetailsRidget)} is false.
 	 */
 	private static final class GlobalMandatoryMarker extends MandatoryMarker {
 
 		GlobalMandatoryMarker() {
 			super(false);
 		}
 
 		/**
 		 * Returns false always.
 		 */
 		@Override
 		public boolean isDisabled() {
 			return false;
 		}
 	}
 
 	/**
 	 * A stored selection.
 	 */
 	private static final class StoredSelection {
 
 		private final Object selection;
 
 		StoredSelection(final Object selection) {
 			this.selection = selection;
 		}
 
 		private Object getSelection() {
 			return selection;
 		}
 	}
 }
