 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.forms.parts;
 
 import java.lang.reflect.Field;
 import java.security.AccessController;
 import java.security.PrivilegedAction;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.dialogs.IDialogSettings;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.ToolBar;
 import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
 import org.eclipse.tcf.te.ui.forms.activator.UIPlugin;
 import org.eclipse.tcf.te.ui.forms.interfaces.tracing.ITraceIds;
 import org.eclipse.tcf.te.ui.jface.interfaces.IValidatable;
 import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;
 import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
 import org.eclipse.ui.forms.AbstractFormPart;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.SectionPart;
 import org.eclipse.ui.forms.widgets.ExpandableComposite;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Section;
 
 /**
  * Abstract section implementation.
  */
 public abstract class AbstractSection extends SectionPart implements IAdaptable, IValidatable {
 	// The message text
 	private String message = null;
 	// The message type. See IMessageProvider
 	private int messageType = NONE;
 
 	// Flag to mark if the controls of the sections are currently updated.
 	// While the control are updated, validation and similar logic should
 	// not be processed.
 	// <p>
 	// <b>Note:</b> This flag default to <code>true</code> on instantiation.
 	private boolean updating = true;
 
 	// Flag to remember the read-only state
 	/* default */ boolean readOnly = false;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param form The parent managed form. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 * @param style The section style.
 	 */
 	public AbstractSection(IManagedForm form, Composite parent, int style) {
 		this(form, parent, style, true);
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param form The parent managed form. Must not be <code>null</code>.
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 * @param style The section style.
 	 * @param titleBar If <code>true</code>, the title bar style bit is added to <code>style</code>.
 	 */
 	public AbstractSection(IManagedForm form, Composite parent, int style, boolean titleBar) {
 		super(parent, form.getToolkit(), titleBar ? (ExpandableComposite.TITLE_BAR | style) : style);
 		initialize(form);
 		configureSection(getSection());
 	}
 
 	/**
 	 * Get the validation container.
 	 * @return The validation container.
 	 */
 	protected IValidatingContainer getValidatingContainer() {
 		Object container = getManagedForm().getContainer();
 		return container instanceof IValidatingContainer ? (IValidatingContainer)container : null;
 	}
 
 	/**
 	 * Configure the section.
 	 *
 	 * @param section The section. Must not be <code>null</code>.
 	 */
 	protected void configureSection(Section section) {
 		Assert.isNotNull(section);
 
 		section.clientVerticalSpacing = FormLayoutFactory.SECTION_HEADER_VERTICAL_SPACING;
 		section.setData("part", this); //$NON-NLS-1$
 
 		// Adjust the background
 		Color bg = section.getParent().getBackground();
 		if (bg != null && !bg.equals(section.getBackground())) {
 			section.setBackground(bg);
 		}
 	}
 
 	/**
 	 * Creates the section client.
 	 *
 	 * @param section The parent section. Must not be <code>null</code>.
 	 * @param toolkit The form toolkit. Must not be <code>null</code>.
 	 */
 	protected abstract void createClient(Section section, FormToolkit toolkit);
 
 	/**
 	 * Creates the client container composite.
 	 *
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 * @param numColumns The number of columns.
 	 * @param toolkit The form toolkit or <code>null</code>.
 	 *
 	 * @return The client container composite.
 	 */
 	protected Composite createClientContainer(Composite parent, int numColumns, FormToolkit toolkit) {
 		Composite container = toolkit != null ? toolkit.createComposite(parent) : new Composite(parent, SWT.NONE);
 		container.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, numColumns));
 
 		// Adjust the background
 		Color bg = parent.getBackground();
 		if (bg != null && !bg.equals(container.getBackground())) {
 			container.setBackground(bg);
 		}
 
 		return container;
 	}
 
 	/**
 	 * Convenience method to create a "invisible" label for creating an
 	 * empty space between controls.
 	 *
 	 * @param parent The parent composite. Must not be <code>null</code>.
 	 * @param span The horizontal span.
 	 * @param toolkit The form toolkit or <code>null</code>.
 	 *
 	 * @return
 	 */
 	protected Label createEmptySpace(Composite parent, int span, FormToolkit toolkit) {
 		Assert.isNotNull(parent);
 
 		Label emptySpace = toolkit != null ? toolkit.createLabel(parent, null) : new Label(parent, SWT.NONE);
 
 		GridData layoutData = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
 		layoutData.horizontalSpan = span;
 		layoutData.widthHint = 0; layoutData.heightHint = SWTControlUtil.convertHeightInCharsToPixels(emptySpace, 1) / 2;
 
 		emptySpace.setLayoutData(layoutData);
 
 		return emptySpace;
 	}
 
 	/**
 	 * Convenience method to create a section toolbar.
 	 *
 	 * @param section The section. Must not be <code>null</code>.
 	 * @param toolkit The form toolkit or <code>null</code>.
 	 */
 	protected void createSectionToolbar(Section section, FormToolkit toolkit) {
 		Assert.isNotNull(section);
 
 		// Create the toolbar manager and the toolbar control
 		ToolBarManager tlbMgr = new ToolBarManager(SWT.FLAT);
 		ToolBar tlb = tlbMgr.createControl(section);
 
 		// If the user moves over the toolbar area, change the cursor to become a hand
 		final Cursor cursor = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
 		tlb.setCursor(cursor);
 
 		// Cursor needs to be explicitly disposed
 		tlb.addDisposeListener(new DisposeListener() {
 			@Override
 			public void widgetDisposed(DisposeEvent e) {
 				if (cursor.isDisposed() == false) {
 					cursor.dispose();
 				}
 			}
 		});
 
 		// Create the toolbar items
 		createSectionToolbarItems(section, toolkit, tlbMgr);
 
 		// Update the toolbar manager
 		tlbMgr.update(true);
 		// Associate the toolbar control with the section
 		section.setTextClient(tlb);
 	}
 
 	/**
 	 * Convenience method to create section toolbar items.
 	 * <p>
 	 * This method is called from {@link #createSectionToolbar(Section, FormToolkit)}.
 	 *
 	 * @param section The section. Must not be <code>null</code>.
 	 * @param toolkit The form toolkit or <code>null</code>.
 	 * @param tlbMgr The toolbar manager. Must not be <code>null</code>.
 	 */
 	protected void createSectionToolbarItems(Section section, FormToolkit toolkit, ToolBarManager tlbMgr) {
 		Assert.isNotNull(section);
 		Assert.isNotNull(tlbMgr);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
 	 */
 	@Override
 	public Object getAdapter(Class adapter) {
 		return Platform.getAdapterManager().getAdapter(this, adapter);
 	}
 
 	/**
 	 * Marks the section dirty or reset the dirty state.
 	 *
 	 * @param dirty <code>True</code> to mark the section dirty, <code>false</code> otherwise.
 	 */
 	public final void markDirty(boolean dirty) {
 		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_SECTIONS_DIRTY_STATE)) {
 			UIPlugin.getTraceHandler().trace("markDirty: Set dirty state to " + dirty, //$NON-NLS-1$
 											 ITraceIds.TRACE_SECTIONS_DIRTY_STATE, this);
 		}
 
 		if (dirty) markDirty();
 		else {
 			// For now, there is no direct way to reset the dirty state,
 			// and the refresh() method is setting back both flags (stale and dirty).
 			// Plus, refresh() might be overwritten to refresh the widget content
 			// from the data itself, what will trigger an stack overflow after all.
 			try {
 				final Field f = AbstractFormPart.class.getDeclaredField("dirty"); //$NON-NLS-1$
 				AccessController.doPrivileged(new PrivilegedAction<Object>() {
 					@Override
 					public Object run() {
 						f.setAccessible(true);
 						return null;
 					}
 				});
 				f.setBoolean(this, dirty);
 				if (getManagedForm() != null) getManagedForm().dirtyStateChanged();
 			} catch (Exception e) { /* ignored on purpose */ }
 		}
 	}
 
 	/**
 	 * Marks the section stale or reset the stale state.
 	 *
 	 * @param stale <code>True</code> to mark the section stale, <code>false</code> otherwise.
 	 */
 	public final void markStale(boolean stale) {
 		if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_SECTIONS_STALE_STATE)) {
 			UIPlugin.getTraceHandler().trace("markStale: Set stale state to " + stale, //$NON-NLS-1$
 											 ITraceIds.TRACE_SECTIONS_STALE_STATE, this);
 		}
 
 		if (stale) markStale();
 		else {
 			// For now, there is no direct way to reset the stale state,
 			// and the refresh() method is setting back both flags (stale and dirty).
 			// Plus, refresh() might be overwritten to refresh the widget content
 			// from the data itself, what will trigger an stack overflow after all.
 			try {
 				final Field f = AbstractFormPart.class.getDeclaredField("stale"); //$NON-NLS-1$
 				AccessController.doPrivileged(new PrivilegedAction<Object>() {
 					@Override
 					public Object run() {
 						f.setAccessible(true);
 						return null;
 					}
 				});
 				f.setBoolean(this, stale);
 				if (getManagedForm() != null) getManagedForm().staleStateChanged();
 			} catch (Exception e) { /* ignored on purpose */ }
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
 	 */
 	@Override
 	public void commit(boolean onSave) {
 		// commit is reseting the dirty state
 		boolean hasBeenDirty = isDirty();
 		// Execute the commit
 		super.commit(onSave);
 		// signal the dirty state change to the manager form if
 		// it really has changed
 		if (hasBeenDirty) {
 			if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_SECTIONS_DIRTY_STATE)) {
 				UIPlugin.getTraceHandler().trace("Commit(" + onSave + ") reseted the dirty state to false.", //$NON-NLS-1$ //$NON-NLS-2$
 												 ITraceIds.TRACE_SECTIONS_DIRTY_STATE, this);
 			}

			getManagedForm().dirtyStateChanged();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
 	 */
 	@Override
 	public void refresh() {
 		// refresh is reseting both the stale and the dirty state
 		boolean hasBeenStale = isStale();
 		boolean hasBeenDirty = isDirty();
 
 		super.refresh();
 
 		if (hasBeenStale) {
 			if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_SECTIONS_STALE_STATE)) {
 				UIPlugin.getTraceHandler().trace("refresh() reseted the stale state to false.", //$NON-NLS-1$
 												 ITraceIds.TRACE_SECTIONS_STALE_STATE, this);
 			}
 
 			getManagedForm().staleStateChanged();
 		}
 
 		if (hasBeenDirty) {
 			if (UIPlugin.getTraceHandler().isSlotEnabled(0, ITraceIds.TRACE_SECTIONS_DIRTY_STATE)) {
 				UIPlugin.getTraceHandler().trace("refresh() reseted the dirty state to false.", //$NON-NLS-1$
 												 ITraceIds.TRACE_SECTIONS_DIRTY_STATE, this);
 			}
 
 			getManagedForm().dirtyStateChanged();
 		}
 	}
 
 	/**
 	 * Set the sections read only state.
 	 * @param readOnly <code>true</code> if the sections controls should be read only.
 	 */
 	public void setReadOnly(boolean readOnly) {
 		this.readOnly = readOnly;
 	}
 
 	/**
 	 * Get the read only state of this section.
 	 * @return The read onyly state.
 	 */
 	public boolean isReadOnly() {
 		return readOnly;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.jface.interfaces.IValidatable#isValid()
 	 */
 	@Override
 	public boolean isValid() {
 		setMessage(null, IMessageProvider.NONE);
 		return true;
 	}
 
 	/**
 	 * Sets the message text and type.
 	 *
 	 * @param message The message or <code>null</code>.
 	 * @param messageType The message type. See {@link IMessageProvider}.
 	 */
 	protected final void setMessage(String message, int messageType) {
 		this.message = message;
 		this.messageType = messageType;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
 	 */
 	@Override
 	public final String getMessage() {
 		return message;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
 	 */
 	@Override
 	public final int getMessageType() {
 		return messageType;
 	}
 
 	/**
 	 * Marks if or if not the controls of the section are currently updated.
 	 */
 	protected final void setIsUpdating(boolean updating) {
 		this.updating = updating;
 	}
 
 	/**
 	 * Returns if or if not the controls of the section are currently updated.
 	 *
 	 * @return <code>True</code> if the controls are currently updated, <code>false</code> otherwise.
 	 */
 	protected final boolean isUpdating() {
 		return updating;
 	}
 
 	/**
 	 * Saves the widget history of all UI elements of the section.
 	 *
 	 * @param settings The dialog settings or <code>null</code>.
 	 */
 	public void saveWidgetValues(IDialogSettings settings) {
 	}
 
 	/**
 	 * Restores the widget history of all UI elements of the section.
 	 *
 	 * @param settings The dialog settings or <code>null</code>.
 	 */
 	public void restoreWidgetValues(IDialogSettings settings) {
 	}
 
 }
