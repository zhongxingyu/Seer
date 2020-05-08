 /*******************************************************************************
  * Copyright (c) 2010 The Eclipse Foundation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * 	The Eclipse Foundation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.epp.internal.mpc.ui.wizards;
 
 import org.eclipse.epp.internal.mpc.ui.catalog.MarketplaceNodeCatalogItem;
 import org.eclipse.equinox.internal.p2.discovery.model.CatalogItem;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 
 /**
  * A controller that controls the multi-state install/uninstall button
  * 
  * @author David Green
  */
 @SuppressWarnings("rawtypes")
 class ItemButtonController {
 	private enum ButtonState {
 		INSTALL(Messages.ItemButtonController_install, Operation.NONE, false), //
 		UNINSTALL(Messages.ItemButtonController_uninstall, Operation.NONE, false), //
 		INSTALL_PENDING(Messages.ItemButtonController_installPending, Operation.INSTALL, false), //
 		UNINSTALL_PENDING(Messages.ItemButtonController_uninstallPending, Operation.UNINSTALL, false), //
		DISABLED(Messages.ItemButtonController_install, Operation.NONE, true), // 
		UPDATE_DISABLED(Messages.ItemButtonController_update, Operation.NONE, true), // 
 		UPDATE(Messages.ItemButtonController_update, Operation.NONE, false), //
 		UPDATE_PENDING(Messages.ItemButtonController_updatePending, Operation.CHECK_FOR_UPDATES, false);
 
 		final String label;
 
 		private final Operation operation;
 
 		private final boolean disabled;
 
 		private ButtonState(String label, Operation operation, boolean disabled) {
 			this.label = label;
 			this.operation = operation;
 			this.disabled = disabled;
 		}
 
 		public ButtonState nextState() {
 			switch (this) {
 			case INSTALL:
 				return INSTALL_PENDING;
 			case INSTALL_PENDING:
 				return INSTALL;
 			case UNINSTALL:
 				return UNINSTALL_PENDING;
 			case UNINSTALL_PENDING:
 				return UNINSTALL;
 			case UPDATE:
 				return UPDATE_PENDING;
 			case UPDATE_PENDING:
 				return UPDATE;
 			}
 			return this;
 		}
 
 		public ButtonState noActionState() {
 			switch (this) {
 			case INSTALL_PENDING:
 				return INSTALL;
 			case UNINSTALL_PENDING:
 				return UNINSTALL;
 			case UPDATE_PENDING:
 				return UPDATE;
 			}
 			return this;
 		}
 
 	}
 
 	private final DiscoveryItem item;
 
 	private final Button button;
 
 	private ButtonState buttonState;
 
 	private final Button secondaryButton;
 
 	private ButtonState secondaryButtonState;
 
 	@SuppressWarnings("unused")
 	private final MarketplaceViewer viewer;
 
 	public ItemButtonController(MarketplaceViewer marketplaceViewer, DiscoveryItem discoveryItem, Button button,
 			Button secondaryButton) {
 		this.item = discoveryItem;
 		this.button = button;
 		this.viewer = marketplaceViewer;
 		this.secondaryButton = secondaryButton;
 		updateButtonState();
 		updateAppearance();
 		button.addSelectionListener(new SelectionListener() {
 			public void widgetSelected(SelectionEvent e) {
 				buttonClicked(buttonState, secondaryButtonState);
 			}
 
 			public void widgetDefaultSelected(SelectionEvent e) {
 				widgetSelected(e);
 			}
 		});
 		if (secondaryButton != null) {
 			secondaryButton.addSelectionListener(new SelectionListener() {
 				public void widgetSelected(SelectionEvent e) {
 					buttonClicked(secondaryButtonState, buttonState);
 				}
 
 				public void widgetDefaultSelected(SelectionEvent e) {
 					widgetSelected(e);
 				}
 			});
 		}
 	}
 
 	private void buttonClicked(ButtonState primary, ButtonState secondary) {
 		if (primary != ButtonState.DISABLED) {
 			primary = primary.nextState();
 			secondary = secondary.noActionState();
 			item.maybeModifySelection(primary.operation);
 		}
 		refresh();
 	}
 
 	private void updateButtonState() {
 		MarketplaceNodeCatalogItem catalogItem = (MarketplaceNodeCatalogItem) item.getData();
 		if (catalogItem.getInstallableUnits().isEmpty()) {
 			buttonState = ButtonState.DISABLED;
 			secondaryButtonState = ButtonState.DISABLED;
 		} else {
 			Operation operation = item.getOperation();
 			boolean installed = isItemInstalled();
 			if (installed) {
 				switch (operation) {
 				case CHECK_FOR_UPDATES:
 					buttonState = ButtonState.UPDATE_PENDING;
 					secondaryButtonState = ButtonState.UNINSTALL;
 					break;
 				case UNINSTALL:
 					buttonState = ButtonState.UPDATE;
 					secondaryButtonState = ButtonState.UNINSTALL_PENDING;
 					break;
 				case NONE:
 					buttonState = ButtonState.UPDATE;
 					secondaryButtonState = ButtonState.UNINSTALL;
 					break;
 				}
 				if (buttonState == ButtonState.UPDATE) {
 					if (catalogItem.getUpdateAvailable() != null && !catalogItem.getUpdateAvailable()) {
 						buttonState = ButtonState.UPDATE_DISABLED;
 					}
 				}
 			} else {
 				switch (operation) {
 				case INSTALL:
 					buttonState = ButtonState.INSTALL_PENDING;
 					break;
 				case NONE:
 					buttonState = ButtonState.INSTALL;
 					break;
 				}
 				secondaryButtonState = ButtonState.DISABLED;
 				if (!isItemAvailable()) {
 					buttonState = ButtonState.DISABLED;
 				}
 			}
 		}
 	}
 
 	private boolean isItemInstalled() {
 		return ((CatalogItem) item.getData()).isInstalled();
 	}
 
 	private boolean isItemAvailable() {
 		Boolean available = ((CatalogItem) item.getData()).getAvailable();
 		return available == null || available;
 	}
 
 	private void updateAppearance() {
 		button.setText(buttonState.label);
 		button.setEnabled(!buttonState.disabled);
 		if (secondaryButton != null) {
 			secondaryButton.setText(secondaryButtonState.label);
 			secondaryButton.setEnabled(!secondaryButtonState.disabled);
 		}
 		// button image? Due to platform limitations we can't set the button color
 
 		item.layout(true, true);
 	}
 
 	public void refresh() {
 		updateButtonState();
 		updateAppearance();
 	}
 
 }
