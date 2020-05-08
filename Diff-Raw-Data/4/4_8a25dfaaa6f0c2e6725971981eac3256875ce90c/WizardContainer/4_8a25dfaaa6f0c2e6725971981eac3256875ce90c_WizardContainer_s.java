 /*
  * Copyright 2005 jWic group (http://www.jwic.de)
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  * de.jwic.wap.core.wizard.Wizard
  * Created on 23.03.2006
  * $Id: WizardContainer.java,v 1.6 2010/02/07 14:26:34 lordsam Exp $
  */
 package de.jwic.controls.wizard;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import de.jwic.base.ControlContainer;
 import de.jwic.base.IControlContainer;
 import de.jwic.controls.Button;
 import de.jwic.controls.ErrorWarning;
 import de.jwic.controls.GroupControl;
 import de.jwic.controls.GroupControl.GroupControlLayout;
 import de.jwic.controls.Label;
 import de.jwic.controls.StackedContainer;
 import de.jwic.controls.Window;
 import de.jwic.controls.dialogs.BasicDialog;
 import de.jwic.events.SelectionEvent;
 import de.jwic.events.SelectionListener;
 import de.jwic.util.Messages;
 
 /**
  * Basic host control for Wizard implementations.
  * 
  * @author Florian Lippisch
  * @version $Revision: 1.6 $
  */
 public class WizardContainer extends BasicDialog {
 	private static final long serialVersionUID = 1L;
 	private Button btBack = null;
 	private Button btNext = null;
 	private Button btFinish = null;
 	private Button btAbort = null;
 	
 	private Label lblPageTitle = null;
 	private Label lblPageSubTitle = null;
 	
 	private ErrorWarning errorWarning = null;
 	
 	private StackedContainer pages = null;
 	
 	private Wizard wizard = null;
 	private WizardPage currentPage = null;
 	
 	private Map<WizardPage, ControlContainer> pageMap = new HashMap<WizardPage, ControlContainer>();
 	
 	/**
 	 * @param site
 	 */
 	public WizardContainer(Wizard wizard, IControlContainer container) {
 		super(container);
 		this.wizard = wizard;
 		currentPage = wizard.createWizardPages(container.getSessionContext());
 	}
 
 	/* (non-Javadoc)
 	 * @see de.jwic.wap.core.Dialog#createControls(de.jwic.base.IControlContainer)
 	 */
 	protected void createControls(IControlContainer container) {
 
 		Messages messages = new Messages(container.getSessionContext().getLocale(), "de.jwic.controls.wizard.messages");
 		
 		Window win = new Window(container);
 		win.setTitle(wizard.getTitle());
 		win.setCloseable(false);
 		win.setMaximizable(true);
 		win.setMinimizable(false);
 		win.setPopup(true);
 		if(wizard.getWidth() > 0){
 			win.setWidth(wizard.getWidth());
 		}else {
 			// default width
 			win.setWidth(900);
 		}
 		
 		ControlContainer winContainer = new ControlContainer(win);
 		winContainer.setTemplateName(getClass().getName());
 		
 		lblPageTitle = new Label(winContainer, "lblPageTitle");
 		lblPageTitle.setCssClass("title");
 		lblPageSubTitle = new Label(winContainer, "lblPageSubTitle");
 		lblPageSubTitle.setCssClass("subtitle");
 		
 		errorWarning = new ErrorWarning(winContainer, "errorWarning");
 		errorWarning.setVisible(false);
 		errorWarning.setClosable(true);
 		errorWarning.setShowStackTrace(false);
 		
 		pages = new StackedContainer(winContainer, "pages");
		pages.setWidth(wizard.getWidth());
		pages.setHeight(wizard.getHeight());
 		
 		
 		NavigationController navContr = new NavigationController();
 		
 		btBack = new Button(winContainer, "btBack");
 		btBack.setTitle(messages.getString("wizard.button.back"));
 		btBack.addSelectionListener(navContr);
 		
 		btNext = new Button(winContainer, "btNext");
 		btNext.setTitle(messages.getString("wizard.button.next"));
 		btNext.addSelectionListener(navContr);
 
 		btFinish = new Button(winContainer, "btFinish");
 		btFinish.setTitle(messages.getString("wizard.button.finish"));
 		btFinish.addSelectionListener(navContr);
 
 		btAbort = new Button(winContainer, "btAbort");
 		btAbort.setTitle(messages.getString("wizard.button.abort"));
 		btAbort.addSelectionListener(navContr);
 
 		activatePage(currentPage);
 		
 	}
 
 	/**
 	 * @param currentPage2
 	 */
 	private void activatePage(WizardPage page) {
 		currentPage = page;
 		
 		lblPageTitle.setText(page.getTitle());
 		lblPageSubTitle.setText(page.getSubTitle());
 		
 		ControlContainer container = pageMap.get(page);
 		if (container == null) {
 			container = new ControlContainer(pages);
 			pageMap.put(page, container);
 			page.createControls(container);
 		}
 		
 		pages.setCurrentControlName(container.getName());
 		errorWarning.setVisible(false);
 		
 		if (!wizard.hasPrevious(currentPage) && !wizard.hasNext(currentPage)) {
 			btBack.setVisible(false);
 			btNext.setVisible(false);
 		} else {
 			btBack.setEnabled(wizard.hasPrevious(currentPage));
 			btNext.setEnabled(wizard.hasNext(currentPage));
 		}
 		
 		btFinish.setEnabled(wizard.canFinish(currentPage));
 		page.activated();
 	}
 
 	/**
 	 * Navigate back.
 	 */
 	protected void performBack () {
 	
 		if (wizard.hasPrevious(currentPage)) {
 			WizardPage newPage = wizard.getPreviousPage(currentPage);
 			if (newPage != null) {
 				activatePage(newPage);
 			}
 		}
 	}
 	
 	/**
 	 * Navigate to the next page.
 	 */
 	protected void performNext() {
 		
 		try {
 			if (currentPage.validate() && wizard.hasNext(currentPage)) {
 				WizardPage newPage = wizard.getNextPage(currentPage);
 				if (newPage != null) {
 					activatePage(newPage);
 				}
 			}
 		} catch (ValidationException ve) {
 			errorWarning.showError(ve);
 		}
 		
 	}
 
 	/**
 	 * Perform the finish action.
 	 *
 	 */
 	protected void performFinish() {
 		try {
 			if (currentPage.validate() && wizard.performFinish()) {
 				finish();
 			}
 		} catch (ValidationException ve) {
 			errorWarning.showError(ve);
 		}
 	}
 	
 	/**
 	 * Abort.
 	 */
 	protected void performAbort() {
 		// abort the wizard
 		wizard.performAbort();
 		abort();
 	}
 	
 	/**
 	 * Returns the wizard hosted by this dialog.
 	 * @return
 	 */
 	public Wizard getWizard() {
 		return wizard;
 	}
 	
 	/**
 	 * Handles navigation-button selections. 
 	 * @author Florian Lippisch
 	 * @version $Revision: 1.6 $
 	 */
 	private class NavigationController implements SelectionListener {
 		private static final long serialVersionUID = 1L;
 		public void objectSelected(SelectionEvent event) {
 			if (event.getEventSource() == btBack) {
 				performBack();
 			} else
 			if (event.getEventSource() == btNext) {
 				performNext();
 			} else
 			if (event.getEventSource() == btFinish) {
 				performFinish();
 			} else
 			if (event.getEventSource() == btAbort) {
 				performAbort();
 			} 
 		}
 	}
 
 }
