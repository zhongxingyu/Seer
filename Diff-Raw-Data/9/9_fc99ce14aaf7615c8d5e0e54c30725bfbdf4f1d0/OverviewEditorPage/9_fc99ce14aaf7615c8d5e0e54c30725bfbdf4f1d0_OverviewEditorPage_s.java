 /*******************************************************************************
  * Copyright (c) 2011, 2013 Wind River Systems, Inc. and others. All rights reserved.
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
 import org.eclipse.jface.action.ControlContribution;
 import org.eclipse.jface.action.IContributionItem;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.viewers.ILabelDecorator;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.tcf.protocol.Protocol;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
 import org.eclipse.tcf.te.runtime.persistence.interfaces.IURIPersistenceService;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.runtime.statushandler.StatusHandlerUtil;
 import org.eclipse.tcf.te.runtime.utils.StatusHelper;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.IDefaultContextService;
 import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.ui.editor.sections.AttributesSection;
 import org.eclipse.tcf.te.tcf.ui.editor.sections.GeneralInformationSection;
 import org.eclipse.tcf.te.tcf.ui.editor.sections.ServicesSection;
 import org.eclipse.tcf.te.tcf.ui.editor.sections.TransportSection;
 import org.eclipse.tcf.te.tcf.ui.help.IContextHelpIds;
 import org.eclipse.tcf.te.tcf.ui.internal.ImageConsts;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
 import org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage;
 import org.eclipse.tcf.te.ui.views.extensions.LabelProviderDelegateExtensionPointManager;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.events.IHyperlinkListener;
 import org.eclipse.ui.forms.widgets.ImageHyperlink;
 import org.eclipse.ui.forms.widgets.TableWrapData;
 import org.eclipse.ui.forms.widgets.TableWrapLayout;
 
 /**
  * Peer overview page implementation.
  */
 public class OverviewEditorPage extends AbstractCustomFormToolkitEditorPage {
 	// References to the page sub sections
 	private GeneralInformationSection infoSection;
 	private TransportSection transportSection;
 	private ServicesSection servicesSection;
 	private AttributesSection attributesSection;
 
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
                 @Override
 				public void eventFired(EventObject event) {
 					ChangeEvent changeEvent = (ChangeEvent)event;
 					if (IPeerNodeProperties.PROP_CONNECT_STATE.equals(changeEvent.getEventId()) && event.getSource() == getEditorInputNode()) {
 						ExecutorsUtil.executeInUI(new Runnable() {
 							@Override
 							public void run() {
 								getManagedForm().getForm().setImage(getFormImage());
 							}
 						});
 					}
 				}
 			};
 	    	EventManager.getInstance().addEventListener(listener, ChangeEvent.class);
 	    }
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		if (listener != null) { EventManager.getInstance().removeEventListener(listener); listener = null; }
 		if (infoSection != null) { infoSection.dispose(); infoSection = null; }
 		if (transportSection != null) { transportSection.dispose(); transportSection = null; }
 		if (servicesSection != null) { servicesSection.dispose(); servicesSection = null; }
 		if (attributesSection != null) { attributesSection.dispose(); attributesSection = null; }
 	    super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getContextHelpId()
 	 */
 	@Override
 	protected String getContextHelpId() {
 	    return IContextHelpIds.OVERVIEW_EDITOR_PAGE;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormTitle()
 	 */
 	@Override
     protected String getFormTitle() {
 		return Messages.OverviewEditorPage_title;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#getFormImage()
 	 */
 	@Override
 	protected Image getFormImage() {
 		Image image = null;
 		if (getEditorInputNode() instanceof IPeerNode) {
 			ILabelProvider[] delegates = LabelProviderDelegateExtensionPointManager.getInstance().getDelegates(getEditorInputNode(), false);
 			if (delegates != null && delegates.length > 0) {
 				image = delegates[0].getImage(getEditorInputNode());
 				if (image != null && delegates[0] instanceof ILabelDecorator) {
 					image = ((ILabelDecorator)delegates[0]).decorateImage(image, getEditorInputNode());
 				}
 			}
 		}
 		return image != null ? image : UIPlugin.getImage(ImageConsts.PEER_NODE);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#doCreateFormContent(org.eclipse.swt.widgets.Composite, org.eclipse.tcf.te.ui.forms.CustomFormToolkit)
 	 */
 	@Override
     protected void doCreateFormContent(Composite parent, CustomFormToolkit toolkit) {
 		Assert.isNotNull(parent);
 		Assert.isNotNull(toolkit);
 
 		// Setup the main panel (using the table wrap layout)
 		Composite panel = toolkit.getFormToolkit().createComposite(parent);
 		TableWrapLayout layout = new TableWrapLayout();
 		layout.makeColumnsEqualWidth = true;
 		layout.numColumns = 2;
 		panel.setLayout(layout);
 		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		infoSection = new GeneralInformationSection(getManagedForm(), panel);
 		infoSection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP));
 		getManagedForm().addPart(infoSection);
 
 		transportSection = new TransportSection(getManagedForm(), (Composite)infoSection.getSection().getClient());
 		((GridData)transportSection.getSection().getLayoutData()).horizontalSpan = 2;
 		getManagedForm().addPart(transportSection);
 
 		servicesSection = new ServicesSection(getManagedForm(), panel);
 		TableWrapData layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP);
 		servicesSection.getSection().setLayoutData(layoutData);
 		getManagedForm().addPart(servicesSection);
 
 		attributesSection = new AttributesSection(getManagedForm(), panel);
 		layoutData = new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.FILL_GRAB);
 		layoutData.colspan = 2;
 		attributesSection.getSection().setLayoutData(layoutData);
 		getManagedForm().addPart(attributesSection);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#setActive(boolean)
 	 */
 	@Override
 	public void setActive(boolean active) {
 	    super.setActive(active);
 	    if (infoSection != null) infoSection.setActive(active);
 	    if (transportSection != null) transportSection.setActive(active);
 	    if (servicesSection != null) servicesSection.setActive(active);
 	    if (attributesSection != null) attributesSection.setActive(active);
 	    validate();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#doValidate()
 	 */
 	@Override
 	protected ValidationResult doValidate() {
 		ValidationResult result = super.doValidate();
 
 		if (infoSection != null) {
 			infoSection.isValid();
 			result.setResult(infoSection);
 		}
 
 		if (transportSection != null) {
 			transportSection.isValid();
 			result.setResult(transportSection);
 		}
 
 		if (servicesSection != null) {
 			servicesSection.isValid();
 			result.setResult(servicesSection);
 		}
 
 		if (attributesSection != null) {
 			attributesSection.isValid();
 			result.setResult(attributesSection);
 		}
 
 		return result;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractEditorPage#postDoSave(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void postDoSave(IProgressMonitor monitor) {
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
 					} catch (IOException e) {
 						// Build up the message template
 						String template = NLS.bind(Messages.OverviewEditorPage_error_save, ((IPeerNode)input).getName(), Messages.PossibleCause);
 						// Handle the status
 						StatusHandlerUtil.handleStatus(StatusHelper.getStatus(e), input, template, null, IContextHelpIds.MESSAGE_SAVE_FAILED, OverviewEditorPage.this, null);
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
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.ui.views.editor.pages.AbstractCustomFormToolkitEditorPage#doCreateLinkContribution(org.eclipse.jface.action.IToolBarManager)
 	 */
 	@Override
 	protected IContributionItem doCreateLinkContribution(final IToolBarManager tbManager) {
 		return new ControlContribution("SetAsDefaultContextLink") { //$NON-NLS-1$
 			IEventListener eventListener = null;
 			@Override
 			public void dispose() {
 				super.dispose();
 				if (eventListener == null) {
 					EventManager.getInstance().removeEventListener(eventListener);
 				}
 			}
 			@Override
 			protected Control createControl(Composite parent) {
 				final ImageHyperlink hyperlink = new ImageHyperlink(parent, SWT.NONE);
 				hyperlink.setText(Messages.OverviewEditorPage_setAsDefault_link);
 				hyperlink.setUnderlined(true);
 				hyperlink.setForeground(getManagedForm().getToolkit().getHyperlinkGroup().getForeground());
 				IPeerNode defaultNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 				setVisible(defaultNode == null || defaultNode != getEditorInputNode());
 				hyperlink.addHyperlinkListener(new IHyperlinkListener() {
 					@Override
 					public void linkActivated(HyperlinkEvent e) {
 						ServiceManager.getInstance().getService(IDefaultContextService.class).setDefaultContext((IPeerNode)getEditorInputNode());
 					}
 					@Override
 					public void linkEntered(HyperlinkEvent e) {
 						hyperlink.setForeground(getManagedForm().getToolkit().getHyperlinkGroup().getActiveForeground());
 					}
 					@Override
 					public void linkExited(HyperlinkEvent e) {
 						hyperlink.setForeground(getManagedForm().getToolkit().getHyperlinkGroup().getForeground());
 					}
 				});
 
 				eventListener = new IEventListener() {
 					@Override
 					public void eventFired(EventObject event) {
 						if (event instanceof ChangeEvent) {
 							ChangeEvent changeEvent = (ChangeEvent)event;
 							if (changeEvent.getSource() instanceof IDefaultContextService) {
 								IPeerNode defaultNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
								setVisible(defaultNode == null || defaultNode != getEditorInputNode());
								tbManager.update(true);
 							}
 						}
 					}
 				};
 
 				EventManager.getInstance().addEventListener(eventListener, ChangeEvent.class);
 
 				return hyperlink;
 			}
 		};
 	}
 }
