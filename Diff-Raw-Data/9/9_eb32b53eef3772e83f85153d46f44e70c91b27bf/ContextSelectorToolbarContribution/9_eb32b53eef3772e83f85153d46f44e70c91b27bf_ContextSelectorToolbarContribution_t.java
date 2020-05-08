 /*******************************************************************************
  * Copyright (c) 2013 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.tcf.ui.handler;
 
 import java.util.EventObject;
 
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.ILabelProvider;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.MouseAdapter;
 import org.eclipse.swt.events.MouseEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.tcf.te.runtime.concurrent.util.ExecutorsUtil;
 import org.eclipse.tcf.te.runtime.events.ChangeEvent;
 import org.eclipse.tcf.te.runtime.events.EventManager;
 import org.eclipse.tcf.te.runtime.events.TriggerCommandEvent;
 import org.eclipse.tcf.te.runtime.interfaces.events.IEventListener;
 import org.eclipse.tcf.te.runtime.services.ServiceManager;
 import org.eclipse.tcf.te.tcf.locator.interfaces.IPeerModelListener;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode;
 import org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNodeProperties;
 import org.eclipse.tcf.te.tcf.locator.interfaces.services.IDefaultContextService;
 import org.eclipse.tcf.te.tcf.locator.model.ModelManager;
 import org.eclipse.tcf.te.tcf.ui.activator.UIPlugin;
 import org.eclipse.tcf.te.tcf.ui.internal.ImageConsts;
 import org.eclipse.tcf.te.tcf.ui.nls.Messages;
 import org.eclipse.tcf.te.ui.swt.SWTControlUtil;
 import org.eclipse.tcf.te.ui.views.navigator.DelegatingLabelProvider;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.menus.IMenuService;
 import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
 
 /**
  * Configurations control implementation.
  */
 public class ContextSelectorToolbarContribution extends WorkbenchWindowControlContribution implements IEventListener, IPeerModelListener {
 
 	final MenuManager menuMgr;
 	private Menu menu = null;
 	private Composite panel = null;
 	private Composite mainPanel = null;
 	private Label image = null;
 	private Label text = null;
 	private Button button = null;
 
 	/**
 	 * Constructor.
 	 */
 	public ContextSelectorToolbarContribution() {
 		super();
 
 		menuMgr = new MenuManager();
 		menuMgr.markDirty();
 	}
 
 	public ContextSelectorToolbarContribution(String id) {
 		super(id);
 
 		menuMgr = new MenuManager();
 		menuMgr.markDirty();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.action.ControlContribution#createControl(org.eclipse.swt.widgets.Composite)
 	 */
 	@Override
 	protected Control createControl(final Composite parent) {
 		mainPanel = new Composite(parent, SWT.NONE);
 		GridLayout layout = new GridLayout();
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.marginRight = 2;
 		mainPanel.setLayout(layout);
 
 		panel = new Composite(mainPanel, SWT.BORDER);
 		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		panel.setLayoutData(layoutData);
 		panel.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WHITE));
 		layout = new GridLayout(3, false);
 		layout.marginHeight = 0;
 		layout.marginWidth = 0;
 		layout.marginLeft = 2;
 		layout.horizontalSpacing = 5;
 		layout.verticalSpacing = 0;
 		panel.setLayout(layout);
 
 		image = new Label(panel, SWT.NONE);
 		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
 		image.setLayoutData(layoutData);
 		image.setBackground(panel.getBackground());
 		image.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				IPeerNode peerNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 				if (peerNode == null) {
 					TriggerCommandEvent event = new TriggerCommandEvent(this, "org.eclipse.tcf.te.ui.command.newWizards"); //$NON-NLS-1$
 					EventManager.getInstance().fireEvent(event);
 				}
 				else {
 					onButtonClick();
 				}
 			}
 		});
 		text = new Label(panel, SWT.NONE);
 		text.setBackground(panel.getBackground());
 		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
 		layoutData.minimumWidth = SWTControlUtil.convertWidthInCharsToPixels(text, 25);
 		text.setLayoutData(layoutData);
 		text.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseUp(MouseEvent e) {
 				IPeerNode peerNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 				if (peerNode == null) {
 					TriggerCommandEvent event = new TriggerCommandEvent(this, "org.eclipse.tcf.te.ui.command.newWizards"); //$NON-NLS-1$
 					EventManager.getInstance().fireEvent(event);
 				}
 				else {
 					onButtonClick();
 				}
 			}
 		});
 
 		button = new Button(panel, SWT.ARROW | SWT.DOWN | SWT.FLAT);
 		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
 		button.setLayoutData(layoutData);
 		button.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				IPeerNode peerNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 				if (peerNode == null) {
 					TriggerCommandEvent event = new TriggerCommandEvent(this, "org.eclipse.tcf.te.ui.command.newWizards"); //$NON-NLS-1$
 					EventManager.getInstance().fireEvent(event);
 				}
 				else {
 					onButtonClick();
 				}
 			}
 		});
 
 	    EventManager.getInstance().addEventListener(this, ChangeEvent.class);
 	    ModelManager.getPeerModel().addListener(this);
 
 	    update();
 
 		return mainPanel;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.action.ContributionItem#dispose()
 	 */
 	@Override
 	public void dispose() {
 	    super.dispose();
 
 	    EventManager.getInstance().removeEventListener(this);
 	    ModelManager.getPeerModel().removeListener(this);
 
 	    image.dispose();
 	    text.dispose();
 	    menuMgr.dispose();
 
 	    image = null;
 	    text = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.action.ContributionItem#update()
 	 */
 	@Override
 	public void update() {
 		menuMgr.markDirty();
 		if (image != null && text != null) {
 			IPeerNode peerNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 			if (peerNode != null) {
 			    DelegatingLabelProvider labelProvider = new DelegatingLabelProvider();
 				image.setImage(labelProvider.decorateImage(labelProvider.getImage(peerNode), peerNode));
 				String fullName = labelProvider.getText(peerNode);
 				String name = fullName;
 				if (name.length() > 22 && name.length() >= 25) {
 					name = name.substring(0, 22) + "..."; //$NON-NLS-1$
 				}
 				text.setText(name);
 
 				image.setToolTipText(!fullName.equals(name) ? fullName : Messages.ContextSelectorToolbarContribution_tooltip_button);
 				text.setToolTipText(!fullName.equals(name) ? fullName : Messages.ContextSelectorToolbarContribution_tooltip_button);
 				button.setToolTipText(Messages.ContextSelectorToolbarContribution_tooltip_button);
 			}
 			else {
 				image.setImage(UIPlugin.getImage(ImageConsts.NEW_CONFIG));
 				text.setText(Messages.ContextSelectorToolbarContribution_label_new);
 
 				image.setToolTipText(Messages.ContextSelectorToolbarContribution_tooltip_new);
 				text.setToolTipText(Messages.ContextSelectorToolbarContribution_tooltip_new);
 				button.setToolTipText(Messages.ContextSelectorToolbarContribution_tooltip_new);
 			}
 		}
 	}
 
 	protected void onButtonClick() {
 		IPeerNode peerNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 		if (peerNode == null) {
 			TriggerCommandEvent event = new TriggerCommandEvent(this, "org.eclipse.tcf.te.ui.command.newWizards"); //$NON-NLS-1$
 			EventManager.getInstance().fireEvent(event);
 		}
 		else {
 			createContextMenu(mainPanel);
 			Point point = mainPanel.toDisplay(mainPanel.getLocation());
 			menu.setLocation(point.x, point.y + mainPanel.getBounds().height);
 			menu.setVisible(true);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.action.ContributionItem#isDynamic()
 	 */
 	@Override
 	public boolean isDynamic() {
 	    return true;
 	}
 
 	protected void createContextMenu(Composite panel) {
 		if (menuMgr.isDirty()) {
 			menuMgr.removeAll();
		    menuMgr.add(new Separator("group.top")); //$NON-NLS-1$
 		    menuMgr.add(new Separator("group.launch")); //$NON-NLS-1$
 		    menuMgr.add(new Separator("group.launch.rundebug")); //$NON-NLS-1$
		    menuMgr.add(new Separator("group.open")); //$NON-NLS-1$
		    menuMgr.add(new GroupMarker("group.delete")); //$NON-NLS-1$
		    menuMgr.add(new GroupMarker("group.new")); //$NON-NLS-1$
			menuMgr.add(new Separator("group.additions")); //$NON-NLS-1$
 			menuMgr.add(new Separator("group.configurations")); //$NON-NLS-1$
     		IPeerNode defaultContext = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
 		    for (final IPeerNode peerNode : ModelManager.getPeerModel().getPeerNodes()) {
 		    	if (peerNode == defaultContext) {
 		    		continue;
 		    	}
 				IAction action = new Action() {
 					private IPeerNode node = peerNode;
 					@Override
 	                public void run() {
 						ServiceManager.getInstance().getService(IDefaultContextService.class).setDefaultContext(node);
 						update();
 					}
 				};
 			    DelegatingLabelProvider labelProvider = new DelegatingLabelProvider();
 				action.setText(labelProvider.getText(peerNode));
 				Image image = labelProvider.decorateImage(labelProvider.getImage(peerNode), peerNode);
 				action.setImageDescriptor(ImageDescriptor.createFromImage(image));
 				menuMgr.add(action);
 		    }
 			final IMenuService service = (IMenuService) getWorkbenchWindow().getPartService().getActivePart().getSite().getService(IMenuService.class);
 			service.populateContributionManager(menuMgr, "menu:org.eclipse.tcf.te.tcf.ui.ContextSelectorToolbarContribution"); //$NON-NLS-1$
 		}
 		menu = menuMgr.createContextMenu(panel);
 	}
 
 	/**
 	 * Get the label provider for a peer model node.
 	 *
 	 * @param peerNode The peer model node.
 	 * @return The label provider or <code>null</code>.
 	 */
 	protected ILabelProvider getLabelProvider(IPeerNode peerNode) {
 		ILabelProvider labelProvider = (ILabelProvider)peerNode.getAdapter(ILabelProvider.class);
 		if (labelProvider == null) {
 			labelProvider = (ILabelProvider)Platform.getAdapterManager().loadAdapter(peerNode, ILabelProvider.class.getName());
 		}
 		return labelProvider;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.runtime.interfaces.events.IEventListener#eventFired(java.util.EventObject)
 	 */
     @Override
     public void eventFired(EventObject event) {
     	if (event instanceof ChangeEvent) {
     		ChangeEvent changeEvent = (ChangeEvent)event;
     		IPeerNode peerNode = ServiceManager.getInstance().getService(IDefaultContextService.class).getDefaultContext(null);
     		if (changeEvent.getSource() instanceof IDefaultContextService ||
     						(changeEvent.getSource() == peerNode &&
     						(IPeerNodeProperties.PROP_CONNECT_STATE.equals(changeEvent.getEventId()) || "properties".equals(changeEvent.getEventId())))) { //$NON-NLS-1$
     			menuMgr.markDirty();
     			ExecutorsUtil.executeInUI(new Runnable() {
     				@Override
     				public void run() {
     					update();
     				}
     			});
     		}
     	}
     }
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.IPeerModelListener#modelChanged(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel, org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerNode, boolean)
 	 */
     @Override
     public void modelChanged(IPeerModel model, IPeerNode peerNode, boolean added) {
 		menuMgr.markDirty();
 		ExecutorsUtil.executeInUI(new Runnable() {
 			@Override
 			public void run() {
 				update();
 			}
 		});
     }
 
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.tcf.te.tcf.locator.interfaces.IPeerModelListener#modelDisposed(org.eclipse.tcf.te.tcf.locator.interfaces.nodes.IPeerModel)
 	 */
     @Override
     public void modelDisposed(IPeerModel model) {
     }
 }
