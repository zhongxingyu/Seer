 /*******************************************************************************
  * Copyright (c) 2011, 2012 Wind River Systems, Inc. and others. All rights reserved.
  * This program and the accompanying materials are made available under the terms
  * of the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.te.ui.views.editor.pages;
 
 import org.eclipse.core.commands.Command;
 import org.eclipse.core.commands.ParameterizedCommand;
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.StructuredSelection;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.tcf.te.ui.forms.CustomFormToolkit;
 import org.eclipse.tcf.te.ui.forms.FormLayoutFactory;
 import org.eclipse.tcf.te.ui.views.activator.UIPlugin;
 import org.eclipse.tcf.te.ui.views.interfaces.ImageConsts;
 import org.eclipse.tcf.te.ui.views.nls.Messages;
 import org.eclipse.ui.ISources;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.commands.ICommandService;
 import org.eclipse.ui.forms.IManagedForm;
 import org.eclipse.ui.handlers.IHandlerService;
 import org.eclipse.ui.menus.IMenuService;
 
 /**
  * Abstract details editor page implementation managing
  *                  an custom form toolkit instance.
  */
 public abstract class AbstractCustomFormToolkitEditorPage extends AbstractEditorPage {
 	// Reference to the form toolkit instance
 	private CustomFormToolkit toolkit = null;
 
 	// The default help action class definition
 	static protected class HelpAction extends Action {
 		/* default */ final String helpID;
 
 		/**
          * Constructor.
          *
          * @param helpID The context help id. Must not be <code>null</code>.
          */
         public HelpAction(String helpID) {
         	super(Messages.AbstractCustomFormToolkitEditorPage_HelpAction_label, IAction.AS_PUSH_BUTTON);
         	Assert.isNotNull(helpID);
         	this.helpID = helpID;
         	setToolTipText(Messages.AbstractCustomFormToolkitEditorPage_HelpAction_tooltip);
         	setImageDescriptor(UIPlugin.getImageDescriptor(ImageConsts.HELP));
         }
 
         /* (non-Javadoc)
          * @see org.eclipse.jface.action.Action#run()
          */
         @Override
         public void run() {
         	PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
         		@Override
         		public void run() {
         			PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpID);
         		}
         	});
         }
 	}
 
 	// The default "Show In System Management" view action
 	static protected class ShowInSystemManagementAction extends Action {
 		private final AbstractCustomFormToolkitEditorPage parentPage;
 
 		/**
          * Constructor.
          */
         public ShowInSystemManagementAction(AbstractCustomFormToolkitEditorPage parentPage) {
         	super(Messages.AbstractCustomFormToolkitEditorPage_ShowInSystemManagementCommandAction_label, IAction.AS_PUSH_BUTTON);
         	setToolTipText(Messages.AbstractCustomFormToolkitEditorPage_ShowInSystemManagementCommandAction_tooltip);
         	setImageDescriptor(UIPlugin.getImageDescriptor(ImageConsts.VIEW));
 
         	Assert.isNotNull(parentPage);
         	this.parentPage = parentPage;
         }
 
         /* (non-Javadoc)
          * @see org.eclipse.jface.action.Action#run()
          */
         @Override
         public void run() {
     		ICommandService service = (ICommandService)PlatformUI.getWorkbench().getService(ICommandService.class);
     		Command command = service != null ? service.getCommand("org.eclipse.tcf.te.ui.command.showIn.systemManagement") : null; //$NON-NLS-1$
     		if (command != null && command.isDefined() && command.isEnabled()) {
     			try {
     				ISelection selection = new StructuredSelection(parentPage.getEditorInputNode());
     				EvaluationContext ctx = new EvaluationContext(null, selection);
     				ctx.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME, selection);
     				ctx.addVariable(ISources.ACTIVE_MENU_SELECTION_NAME, selection);
     				ctx.addVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME, parentPage.getSite().getWorkbenchWindow());
     				ctx.addVariable(ISources.ACTIVE_PART_ID_NAME, parentPage.getSite().getId());
     				ctx.addVariable(ISources.ACTIVE_PART_NAME, parentPage.getSite().getPart());
     				ctx.addVariable(ISources.ACTIVE_SITE_NAME, parentPage.getSite());
     				ctx.addVariable(ISources.ACTIVE_SHELL_NAME, parentPage.getSite().getShell());
     				ctx.setAllowPluginActivation(true);
 
     				ParameterizedCommand pCmd = ParameterizedCommand.generateCommand(command, null);
     				Assert.isNotNull(pCmd);
     				IHandlerService handlerSvc = (IHandlerService)PlatformUI.getWorkbench().getService(IHandlerService.class);
     				Assert.isNotNull(handlerSvc);
     				handlerSvc.executeCommandInContext(pCmd, null, ctx);
     			} catch (Exception e) {
     				// If the platform is in debug mode, we print the exception to the log view
     				if (Platform.inDebugMode()) {
     					IStatus status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), e.getLocalizedMessage(), e);
     					UIPlugin.getDefault().getLog().log(status);
     				}
     			}
     		}
         }
 	}
 
 	/**
 	 * Returns the custom form toolkit instance.
 	 *
 	 * @return The custom form toolkit instance or <code>null</code>.
 	 */
 	protected final CustomFormToolkit getFormToolkit() {
 		return toolkit;
 	}
 
 	/**
 	 * Sets the custom form toolkit instance.
 	 *
 	 * @param toolkit The custom form toolkit instance or <code>null</code>.
 	 */
 	protected final void setFormToolkit(CustomFormToolkit toolkit) {
 		this.toolkit = toolkit;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#dispose()
 	 */
 	@Override
 	public void dispose() {
 		// Get the menu service and release the toolbar manager
		IToolBarManager manager = getManagedForm().getForm().getForm().getToolBarManager();
		IMenuService service = (IMenuService) getSite().getService(IMenuService.class);
		if (service != null) {
			service.releaseContributions((ToolBarManager)manager);
 		}
 		// Dispose the custom form toolkit
 		if (toolkit != null) { toolkit.dispose(); toolkit = null; }
 		// Dispose all the rest
 		super.dispose();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
 	 */
 	@Override
 	protected void createFormContent(IManagedForm managedForm) {
 		super.createFormContent(managedForm);
 
 		Assert.isNotNull(managedForm);
 
 		// Create the toolkit instance
 		toolkit = new CustomFormToolkit(managedForm.getToolkit());
 
 		// Configure the managed form
 		configureManagedForm(managedForm);
 
 		// Do create the content of the form now
 		doCreateFormContent(managedForm.getForm().getBody(), getFormToolkit());
 
 		// Re-arrange the controls
 		managedForm.reflow(true);
 	}
 
 	/**
 	 * Configure the managed form to be ready for usage.
 	 *
 	 * @param managedForm The managed form. Must not be <code>null</code>.
 	 */
 	protected void configureManagedForm(IManagedForm managedForm) {
 		Assert.isNotNull(managedForm);
 
 		// Configure main layout
 		Composite body = managedForm.getForm().getBody();
 		body.setLayout(FormLayoutFactory.createFormGridLayout(false, 1));
 
 		// Set context help id
 		if (getContextHelpId() != null) {
 			PlatformUI.getWorkbench().getHelpSystem().setHelp(managedForm.getForm(), getContextHelpId());
 		}
 
 		// Decorate the form header
 		getFormToolkit().getFormToolkit().decorateFormHeading(managedForm.getForm().getForm());
 		// And set the header text and image
 		if (getFormTitle() != null) managedForm.getForm().getForm().setText(getFormTitle());
 		managedForm.getForm().getForm().setImage(getFormImage());
 
 		// Add the toolbar items which will appear in the form header
		IToolBarManager manager = managedForm.getForm().getForm().getToolBarManager();
 		// Add the default "additions" separator
 		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
 		// Create fixed toolbar contribution items
 		createToolbarContributionItems(manager);
 		// Get the menu service and populate contributed toolbar actions
 		IMenuService service = (IMenuService) getSite().getService(IMenuService.class);
		if (service != null) {
			service.populateContributionManager((ToolBarManager)manager, "toolbar:" + getId()); //$NON-NLS-1$
 		}
 		// Trigger an update of the toolbar widget
 		manager.update(true);
 	}
 
 	/**
 	 * Returns the context help id to associate with the page form.
 	 *
 	 * @return The context help id.
 	 */
 	protected String getContextHelpId() {
 		return null;
 	}
 
 	/**
 	 * Returns the form title to set to the top form header.
 	 *
 	 * @return The form title.
 	 */
 	protected String getFormTitle() {
 		return null;
 	}
 
 	/**
 	 * Returns the image to be set to the top form header.
 	 *
 	 * @return The image or <code>null</code> to use no image.
 	 */
 	protected Image getFormImage() {
 		return null;
 	}
 
 	/**
 	 * Create the toolbar contribution items.
 	 *
 	 * @param manager The toolbar manager. Must not be <code>null</code>.
 	 */
 	protected void createToolbarContributionItems(IToolBarManager manager) {
 		Assert.isNotNull(manager);
 
 		// If the page does have "Show In System Management", add the action into the toolbar
 		if (hasShowInSystemMangementAction()) {
 			Action showInAction = doCreateShowInSystemManagementAction();
 			if (showInAction != null) manager.add(showInAction);
 		}
 
 		// If the page is associated with a context help id, add a default
 		// help action button into the toolbar
 		if (getContextHelpId() != null) {
 			if (hasShowInSystemMangementAction()) manager.add(new Separator());
 			Action helpAction = doCreateHelpAction(getContextHelpId());
 			if(helpAction != null) manager.add(helpAction);
 		}
 	}
 
 	/**
 	 * Create the help action.
 	 *
 	 * @param contextHelpId The context help id. Must not be <code>null</code>.
 	 * @return The help action or <code>null</code>.
 	 */
 	protected Action doCreateHelpAction(String contextHelpId) {
 		Assert.isNotNull(contextHelpId);
 		return new HelpAction(contextHelpId);
 	}
 
 	/**
 	 * Returns if or if not the page has the "Show In System Management" action.
 	 * <p>
 	 * The default implementation returns <code>false</code>.
 	 *
 	 * @return <code>True</code> if the action is visible in the form toolbar, <code>false</code> otherwise.
 	 */
 	protected boolean hasShowInSystemMangementAction() {
 		return false;
 	}
 
 	/**
 	 * Creates the "Show In System Management" action.
 	 *
 	 * @return The action or <code>null</code>.
 	 */
     protected Action doCreateShowInSystemManagementAction() {
 		return new ShowInSystemManagementAction(this);
 	}
 
 	/**
 	 * Do create the managed form content.
 	 *
 	 * @param parent The parent composite. Must not be <code>null</code>
 	 * @param toolkit The {@link CustomFormToolkit} instance. Must not be <code>null</code>.
 	 */
 	protected abstract void doCreateFormContent(Composite parent, CustomFormToolkit toolkit);
 }
