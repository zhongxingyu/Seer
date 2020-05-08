 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.controls;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.dialogs.IMessageProvider;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.IService;
 import org.eclipse.tcf.te.runtime.services.interfaces.ISimulatorService;
 import org.eclipse.tcf.te.runtime.services.interfaces.IUIService;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.tcf.ui.sections.TargetSelectorSection;
 import org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl;
 import org.eclipse.tcf.te.ui.interfaces.services.ISimulatorServiceUIDelegate;
 import org.eclipse.tcf.te.ui.jface.interfaces.IValidatingContainer;
 import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
 
 /**
  * Simulator type selection control implementation.
  */
 public class SimulatorTypeSelectionControl extends BaseEditBrowseTextControl {
 	private final Map<String, ISimulatorServiceUIDelegate> id2delegate = new HashMap<String, ISimulatorServiceUIDelegate>();
 	private final Map<String, String> name2id = new HashMap<String, String>();
 	private final Map<String, String> id2name = new HashMap<String, String>();
 	private final Map<String, String> id2config = new HashMap<String, String>();
 
 	private final TargetSelectorSection parentSection;
 
 	/**
 	 * Constructor.
 	 *
 	 * @param parentSection The parent section.
 	 */
 	public SimulatorTypeSelectionControl(TargetSelectorSection parentSection) {
 		super(null);
 
 		Assert.isNotNull(parentSection);
 		this.parentSection = parentSection;
 
 		setIsGroup(false);
 		setEditFieldLabel(Messages.SimulatorTypeSelectionControl_label);
 		setButtonLabel(Messages.SimulatorTypeSelectionControl_button_configure);
 		setHasHistory(true);
 		setReadOnly(true);
 	}
 
 	/**
 	 * Initialize the control based on the given context.
 	 *
 	 * @param context The context or <code>null</code>.
 	 */
 	public void initialize(Object context) {
 		SWTControlUtil.removeAll(getEditFieldControl());
 		id2delegate.clear();
 		name2id.clear();
 		id2name.clear();
 		id2config.clear();
 
 		List<String> entries = new ArrayList<String>();
 
 		if (context != null) {
 			// Get all simulator services for the given context
 			IService[] services = ServiceManager.getInstance().getServices(context, ISimulatorService.class, false);
 			for (IService service : services) {
 				Assert.isTrue(service instanceof ISimulatorService);
 				// Get the UI service which is associated with the simulator service
 				IUIService uiService = ServiceManager.getInstance().getService(service, IUIService.class);
 				if (uiService == null) {
 					continue;
 				}
 				// Get the simulator service UI delegate
 				ISimulatorServiceUIDelegate uiDelegate = uiService.getDelegate(service, ISimulatorServiceUIDelegate.class);
 				String id = service.getId();
 				String name = uiDelegate != null ? uiDelegate.getName() : id;
 				id2delegate.put(id, uiDelegate);
 				name2id.put(name, id);
 				id2name.put(id, name);
 				if (!entries.contains(name)) {
 					entries.add(name);
 				}
 			}
 		}
 
 		SWTControlUtil.setItems(getEditFieldControl(), entries.toArray(new String[entries.size()]));
 		SWTControlUtil.select(getEditFieldControl(), 0);
 
 		if (getEditFieldControl() != null && getEditFieldControl().getLayoutData() instanceof GridData) {
 			doAdjustEditFieldControlLayoutData((GridData)getEditFieldControl().getLayoutData());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#doAdjustEditFieldControlLayoutData(org.eclipse.swt.layout.GridData)
 	 */
 	@Override
 	protected void doAdjustEditFieldControlLayoutData(GridData layoutData) {
 		super.doAdjustEditFieldControlLayoutData(layoutData);
 
 		int maxWidth = -1;
 		for (String item : SWTControlUtil.getItems(getEditFieldControl())) {
 			maxWidth = Math.max(item.length(), maxWidth);
 		}
 
 		if (maxWidth != -1) {
 			layoutData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
 			layoutData.grabExcessHorizontalSpace = false;
			layoutData.widthHint = SWTControlUtil.convertWidthInCharsToPixels(getEditFieldControl(), maxWidth + 10);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseDialogPageControl#getValidatingContainer()
 	 */
 	@Override
 	public IValidatingContainer getValidatingContainer() {
 		Object container = parentSection.getManagedForm().getContainer();
 		return container instanceof IValidatingContainer ? (IValidatingContainer)container : null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#onButtonControlSelected()
 	 */
 	@Override
 	protected void onButtonControlSelected() {
 		// Get the corresponding simulator service UI delegate
 		ISimulatorServiceUIDelegate uiDelegate = id2delegate.get(getSelectedSimulatorId());
 		if (uiDelegate != null) {
 			String oldConfig = getSimulatorConfig();
 			String newConfig = uiDelegate.configure(parentSection.getOriginalData(), oldConfig);
 			if ((oldConfig != null && !oldConfig.equals(newConfig)) || (newConfig != null && !newConfig.equals(oldConfig))) {
 				id2config.put(getSelectedSimulatorId(), newConfig);
 				parentSection.dataChanged(null);
 			}
 		}
 	}
 
 	/**
 	 * Set the selected simulator id.
 	 * @param id The selected simulator id or <code>null</code>.
 	 */
 	public void setSelectedSimulatorId(String id) {
 		String name = id2name.get(id);
 		if (name != null && name.trim().length() > 0) {
 			int index = ((Combo)getEditFieldControl()).indexOf(name);
 			if (index >= 0) {
 				SWTControlUtil.select(getEditFieldControl(), index);
 			}
 		}
 	}
 
 	/**
 	 * Get the selected simulator id.
 	 * @return The selected simulator id or <code>null</code>.
 	 */
 	public String getSelectedSimulatorId() {
 		return name2id.get(getEditFieldControlText());
 	}
 
 	/**
 	 * Set the simulator configuration for the selected simulator.
 	 *
 	 * @param config The simulator configuration or <code>null</code>.
 	 */
 	public void setSimulatorConfig(String config) {
 		id2config.put(getSelectedSimulatorId(), config);
 	}
 
 	/**
 	 * Get the simulator configuration for the selected simulator.
 	 * If no configuration was set, the default configuration or <code>null</code> will be returned.
 	 *
 	 * @return The simulator configuration or <code>null</code>.
 	 */
 	public String getSimulatorConfig() {
 		String config = id2config.get(getSelectedSimulatorId());
 		ISimulatorServiceUIDelegate uiDelegate = id2delegate.get(getSelectedSimulatorId());
 		if (uiDelegate != null && (config == null || config.trim().length() == 0)) {
 			config = uiDelegate.getService().getDefaultConfig();
 		}
 		return config;
 	}
 
 	private void setConfigureEnabled(boolean enabled) {
 		// Get the currently selected simulator type name
 		String id = getSelectedSimulatorId();
 		// Get the corresponding simulator service UI delegate
 		ISimulatorServiceUIDelegate uiDelegate = id2delegate.get(id);
 		if (getButtonControl() != null) {
 			getButtonControl().setEnabled(enabled && uiDelegate != null && uiDelegate.canConfigure());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#widgetSelected(org.eclipse.swt.events.SelectionEvent)
 	 */
 	@Override
 	public void widgetSelected(SelectionEvent e) {
 		super.widgetSelected(e);
 		setConfigureEnabled(isEnabled());
 		parentSection.dataChanged(e);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#setEnabled(boolean)
 	 */
 	@Override
 	public void setEnabled(boolean enabled) {
 		super.setEnabled(enabled);
 		setConfigureEnabled(enabled);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#onLabelControlSelectedChanged()
 	 */
 	@Override
 	protected void onLabelControlSelectedChanged() {
 		super.onLabelControlSelectedChanged();
 		setConfigureEnabled(isLabelControlSelected());
 		parentSection.dataChanged(null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.controls.BaseEditBrowseTextControl#isValid()
 	 */
 	@Override
 	public boolean isValid() {
 		boolean valid = super.isValid();
 
 		if (valid) {
 			// Get the corresponding simulator service UI delegate
 			ISimulatorServiceUIDelegate uiDelegate = id2delegate.get(getSelectedSimulatorId());
 			String config = getSimulatorConfig();
 			String defaultConfig = uiDelegate != null ? uiDelegate.getService().getDefaultConfig() : null;
 			valid = uiDelegate != null ? uiDelegate.getService().isValidConfig(parentSection.getOriginalData(), config) : true;
 			boolean isDefaultConfig = (defaultConfig != null && defaultConfig.equals(config)) || (defaultConfig == null && config == null);
 			if (!valid) {
 				setMessage(Messages.SimulatorTypeSelectionControl_error_invalidConfiguration, isDefaultConfig ? IMessageProvider.INFORMATION : IMessageProvider.ERROR);
 			}
 		}
 
 		if (getControlDecoration() != null) {
 			// Setup and show the control decoration if necessary
 			if (isEnabled() && (!valid || (getMessage() != null && getMessageType() != IMessageProvider.NONE))) {
 				// Update the control decorator
 				updateControlDecoration(getMessage(), getMessageType());
 			} else {
 				updateControlDecoration(null, IMessageProvider.NONE);
 			}
 		}
 
 		return valid;
 	}
 }
