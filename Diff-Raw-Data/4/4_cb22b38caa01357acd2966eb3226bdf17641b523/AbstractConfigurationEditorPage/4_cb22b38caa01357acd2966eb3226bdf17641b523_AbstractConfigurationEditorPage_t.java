 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.editor;
 
 import java.io.IOException;
 import java.util.EventObject;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.services.interfaces.ISimulatorService;
 import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerUtil;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProperties;
 import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.tcf.ui.sections.SimulatorTypeSelectionSection;
 import org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 
 /**
  * Abstract configuration editor page implementation.
  */
 public abstract class AbstractConfigurationEditorPage extends AbstractCustomFormToolkitEditorPage {
 
 	// Section to select real or simulator
 	/* default */ SimulatorTypeSelectionSection simulatorTypeSelectionSection = null;
 
 	private IEventListener listener = null;
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#setInput(org.eclipse.ui.IEditorInput)
 	 */
 	@Override
 	protected void setInput(IEditorInput input) {
 		IEditorInput oldInput = getEditorInput();
 		// do nothing when input did not change
 		if (oldInput != null && oldInput.equals(input)) {
 			return;
 		}
 		super.setInput(input);
 	    if (listener == null) {
 	    	listener = new IEventListener() {
 				@SuppressWarnings("synthetic-access")
                 @Override
 				public void eventFired(EventObject event) {
 					ChangeEvent changeEvent = (ChangeEvent)event;
 					if (IPeerNodeProperties.PROP_CONNECT_STATE.equals(changeEvent.getEventId()) && event.getSource() == getEditorInputNode()) {
 						ExecutorsUtil.executeInUI(new Runnable() {
 							@Override
 							public void run() {
 								if (!getManagedForm().getForm().isDisposed()) {
 									getManagedForm().getForm().setImage(getFormImage());
 								}
 							}
 						});
 					}
 				}
 			};
 	    	EventManager.getInstance().addEventListener(listener, ChangeEvent.class);
 	    }
 	}
 
 	/**
 	 * Add the target selector section if an {@link ISimulatorService} is available.
 	 * @param form The form.
 	 * @param parent The parent composite.
 	 */
 	protected void addTargetSelectorSection(IManagedForm form, Composite parent) {
 		ISimulatorService service = ServiceManager.getInstance().getService(getEditorInputNode(), ISimulatorService.class);
 		if (service != null) {
 			simulatorTypeSelectionSection = doCreateTargetSelectorSection(form, parent);
 			simulatorTypeSelectionSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
 			getManagedForm().addPart(simulatorTypeSelectionSection);
 		}
 	}
 
 	protected SimulatorTypeSelectionSection getTargetSelectorSection() {
 		return simulatorTypeSelectionSection;
 	}
 
 	/**
 	 * Create the target selector section.
 	 * @param form The form.
 	 * @param parent The parent composite.
 	 * @return The target selector section.
 	 */
 	protected SimulatorTypeSelectionSection doCreateTargetSelectorSection (IManagedForm form, Composite parent) {
 		return new SimulatorTypeSelectionSection(getManagedForm(), parent);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (simulatorTypeSelectionSection != null) { simulatorTypeSelectionSection.dispose(); simulatorTypeSelectionSection = null; }
 		if (listener != null) { EventManager.getInstance().removeEventListener(listener); listener = null; }
 		super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#setActive(boolean)
 	 */
 	@Override
 	public void setActive(boolean active) {
 		super.setActive(active);
 
 		if (simulatorTypeSelectionSection != null) {
 			simulatorTypeSelectionSection.setActive(active);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#doValidate()
 	 */
 	@Override
 	protected ValidationResult doValidate() {
 		ValidationResult result = super.doValidate();
 
 		if (simulatorTypeSelectionSection != null) {
 			simulatorTypeSelectionSection.isValid();
 			result.setResult(simulatorTypeSelectionSection);
 		}
 
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#postDoSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void postDoSave(IProgressMonitor monitor) {
 		super.postDoSave(monitor);
 
 		// If necessary, write the changed peer attributes
 		final Object input = getEditorInputNode();
 		if (input instanceof IPeerNode) {
 			Runnable runnable = new Runnable() {
 				@Override
 				public void run() {
 					try {
 						// Get the persistence service
 						IURIPersistenceService uRIPersistenceService = ServiceManager.getInstance().getService(IURIPersistenceService.class);
 						if (uRIPersistenceService == null) {
 							throw new IOException("Persistence service instance unavailable."); //$NON-NLS-1$
 						}
 						// Save the peer node to the new persistence storage
 						uRIPersistenceService.write(((IPeerNode)input).getPeer(), null);
 
 						// Reopen the editor on the current page
//						ViewsUtil.reopenEditor(getEditor(), getEditor().getActivePageInstance().getId(), false);
 					} catch (IOException e) {
 						// Build up the message template
 						String template = NLS.bind(Messages.AbstractConfigurationEditorPage_error_save, ((IPeerNode)input).getName(), Messages.AbstractConfigurationEditorPage_error_possibleCause);
 						// Handle the status
 						StatusHandlerUtil.handleStatus(StatusHelper.getStatus(e), input, template, null, IContextHelpIds.MESSAGE_SAVE_FAILED, AbstractConfigurationEditorPage.this, null);
 					}
 				}
 			};
 			Assert.isTrue(!Protocol.isDispatchThread());
 			Protocol.invokeAndWait(runnable);
 
 			Protocol.invokeLater(new Runnable() {
 				@Override
 				public void run() {
 					// Trigger a change event for the original data node
 					((IPeerNode)input).fireChangeEvent("properties", null, ((IPeerNode)input).getProperties()); //$NON-NLS-1$
 				}
 			});
 		}
 	}
 }
