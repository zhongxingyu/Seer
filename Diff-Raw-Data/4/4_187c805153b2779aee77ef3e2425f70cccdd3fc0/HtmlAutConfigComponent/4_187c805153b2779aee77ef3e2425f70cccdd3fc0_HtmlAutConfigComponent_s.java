 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.toolkit.provider.html.gui;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO.ActivationMethod;
 import org.eclipse.jubula.client.core.model.IAUTConfigPO.Browser;
 import org.eclipse.jubula.client.ui.rcp.Plugin;
 import org.eclipse.jubula.client.ui.rcp.businessprocess.RemoteFileBrowserBP;
 import org.eclipse.jubula.client.ui.rcp.provider.ControlDecorator;
 import org.eclipse.jubula.client.ui.rcp.utils.DialogStatusParameter;
 import org.eclipse.jubula.client.ui.rcp.widgets.AutConfigComponent;
 import org.eclipse.jubula.client.ui.widgets.I18nEnumCombo;
 import org.eclipse.jubula.client.ui.widgets.UIComponentHelper;
 import org.eclipse.jubula.tools.constants.AutConfigConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.constants.SwtAUTHierarchyConstants;
 import org.eclipse.jubula.tools.exception.Assert;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.FileDialog;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 
 /**
  * @author BREDEX GmbH
  * @created Nov 4, 2009
  */
 public class HtmlAutConfigComponent extends AutConfigComponent {
     /** gui component */
     private Text m_autUrlTextField;
     /** gui field for browser */
     private Text m_browserTextField;
     /** gui field for aut id attribute text field */
     private Text m_autIdAttibuteTextField;
     /** gui button for browser path */
     private Button m_browserPathButton;
     /** gui checkbox for the singeWindowMode */
     private Button m_singleWindowCheckBox;
     /** gui component */
     private I18nEnumCombo<Browser> m_browserCombo;
     /** gui component */
     private I18nEnumCombo<ActivationMethod> m_activationMethodCombo;
     /** the WidgetModifyListener */
     private WidgetModifyListener m_modifyListener;
     /** the the WidgetSelectionListener */
     private WidgetSelectionListener m_selectionListener;
 
 
     /**
      * @param parent {@inheritDoc}
      * @param style {@inheritDoc}
      * @param autConfig data to be displayed/edited
      * @param autName the name of the AUT that will be using this configuration.
      */
     public HtmlAutConfigComponent(Composite parent, int style,
         Map<String, String> autConfig, String autName) {
         
         super(parent, style, autConfig, autName, true);
     }
 
     /**
      * @param basicAreaComposite The composite that represents the basic area.
      */
     protected void createBasicArea(Composite basicAreaComposite) {
         super.createBasicArea(basicAreaComposite);
 
         // URL property
         Label urlLabel = UIComponentHelper.createLabel(
                 basicAreaComposite, "WebAutConfigComponent.URL"); //$NON-NLS-1$
         urlLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.urlLabel"); //$NON-NLS-1$
         
         m_autUrlTextField = UIComponentHelper.createTextField(
                 basicAreaComposite, 2);
         m_autUrlTextField.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.autUrlTextField"); //$NON-NLS-1$
         
         // browser
         Label browserLabel = UIComponentHelper.createLabel(
                 basicAreaComposite, "WebAutConfigComponent.browser"); //$NON-NLS-1$
         browserLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.browserLabel"); //$NON-NLS-1$
         
         m_browserCombo = UIComponentHelper.createEnumCombo(
                 basicAreaComposite, 2, "WebAutConfigComponent.Browser", //$NON-NLS-1$
                     Browser.class);
         m_browserCombo.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.browserCombo"); //$NON-NLS-1$
     }
     
     /**
      * {@inheritDoc}
      */
     protected void createAdvancedArea(Composite advancedAreaComposite) {
         super.createAdvancedArea(advancedAreaComposite);
         
         createBrowserPathEditor(advancedAreaComposite);
         
         createSingleModeCheckBox(advancedAreaComposite);
 
     }
     
     /**
      * Create this dialog's expert area component.
      * 
      * @param expertAreaComposite Composite representing the expert area.
      */
     protected void createExpertArea(Composite expertAreaComposite) {
         super.createExpertArea(expertAreaComposite);
         
         // activation method editor
         Label activationMethodLabel = UIComponentHelper.createLabel(
                 expertAreaComposite, "AUTConfigComponent.activationMethod"); //$NON-NLS-1$
         activationMethodLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.activationMethodLabel"); //$NON-NLS-1$
         
         m_activationMethodCombo = UIComponentHelper.createEnumCombo(
                 expertAreaComposite, 2, "AUTConfigComponent.ActivationMethod", //$NON-NLS-1$
                     ActivationMethod.class);
         m_activationMethodCombo.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.activationMethodCombo"); //$NON-NLS-1$
         
         // AUT ID Attribute property
         Label autIdAttibuteLabel = UIComponentHelper.createLabel(
                 expertAreaComposite, "HTMLAutConfigComponent.AutIdAttibuteLabel"); //$NON-NLS-1$
         autIdAttibuteLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.autIdAttibuteLabel"); //$NON-NLS-1$
         
         m_autIdAttibuteTextField = UIComponentHelper.createTextField(
                 expertAreaComposite, 2);
         m_autIdAttibuteTextField.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.autIdAttibuteTextField"); //$NON-NLS-1$
     }
     
     /**
      * Inits the browser path area.
      * 
      * @param parent The parent Composite.
      */
     protected void createBrowserPathEditor(Composite parent) {
         
         Label browserPathLabel = UIComponentHelper.createLabel(parent, "WebAutConfigComponent.browserPath"); //$NON-NLS-1$ 
         browserPathLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.browserPathLabel"); //$NON-NLS-1$
         ControlDecorator.decorateInfo(browserPathLabel,  
                 "GDControlDecorator.WebBrowserPath", false); //$NON-NLS-1$
         
         m_browserTextField = UIComponentHelper.createTextField(
             parent, 1);
         m_browserTextField.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.BrowserTextField"); //$NON-NLS-1$
         
         m_browserPathButton = new Button(UIComponentHelper
                 .createLayoutComposite(parent), SWT.PUSH);
         m_browserPathButton.setText(I18n.getString("AUTConfigComponent.browse"));  //$NON-NLS-1$
         m_browserPathButton.setLayoutData(BUTTON_LAYOUT);
         m_browserPathButton.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.browserPathButton"); //$NON-NLS-1$
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     protected void installListeners() {
         super.installListeners();
         WidgetModifyListener modifyListener = getModifyListener();
         WidgetSelectionListener selectionListener = getSelectionListener();
         
         getServerCombo().addModifyListener(modifyListener);
         m_autUrlTextField.addModifyListener(modifyListener);
         m_autIdAttibuteTextField.addModifyListener(modifyListener);
         m_browserTextField.addModifyListener(modifyListener);
         m_browserPathButton.addSelectionListener(selectionListener);
         m_browserCombo.addSelectionListener(selectionListener);
         m_activationMethodCombo.addSelectionListener(selectionListener);
         m_singleWindowCheckBox.addSelectionListener(selectionListener);
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     protected void deinstallListeners() {
         super.deinstallListeners();
         WidgetModifyListener modifyListener = getModifyListener();
         WidgetSelectionListener selectionListener = getSelectionListener();
         
         getServerCombo().removeModifyListener(modifyListener);
         m_autUrlTextField.removeModifyListener(modifyListener);
         m_autIdAttibuteTextField.removeModifyListener(modifyListener);
         m_browserTextField.removeModifyListener(modifyListener);
         m_browserPathButton.removeSelectionListener(selectionListener);
         m_browserCombo.removeSelectionListener(selectionListener);
         m_activationMethodCombo.removeSelectionListener(selectionListener);
         m_singleWindowCheckBox.removeSelectionListener(selectionListener);
     }
     
     /**
      * This private inner class contains a new SelectionListener.
      * 
      * @author BREDEX GmbH
      * @created 13.07.2005
      */
     private class WidgetSelectionListener implements SelectionListener {
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void widgetSelected(SelectionEvent e) {
             Object source = e.getSource();
             boolean checked = false;
             
             if (source.equals(m_activationMethodCombo)) {
                 checked = true;
             } else if (source.equals(m_browserCombo)) {
                 internetExplorerSelected();
                 checked = true;
             } else if (source.equals(m_browserPathButton)) {
                 if (isRemoteRequest()) {
                     remoteBrowse(false, AutConfigConstants.BROWSER_PATH,
                             m_browserTextField,
                             I18n.getString("WebAutConfigComponent.SelectBrowserPath")); //$NON-NLS-1$
                 } else {
                     FileDialog fileDialog = new FileDialog(
                             Plugin.getShell(), SWT.APPLICATION_MODAL
                                     | SWT.ON_TOP);
                     //handleBrowserPathButtonEvent(fileDialog);
                     
                     fileDialog.setText(I18n.getString("WebAutConfigComponent.SelectBrowserPath")); //$NON-NLS-1$
                     String browserFile = fileDialog.open();
                     if (browserFile != null) {
                         m_browserTextField.setText(browserFile);
                     }
                 }
 
                 return;
             } else if (source.equals(m_singleWindowCheckBox)) {
                 checked = true;
             }
             if (checked) {
                 checkAll();
                 return;
             }
             Assert.notReached("Event activated by unknown widget(" + source + ")."); //$NON-NLS-1$ //$NON-NLS-2$    
         }
         /**
          * {@inheritDoc}
          */
         public void widgetDefaultSelected(SelectionEvent e) {
             // Do nothing
         }
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     protected boolean checkLocalhostServer() {
         boolean enable = super.checkLocalhostServer();
         boolean browseEnabled = enable || isRemoteRequest();
         m_browserPathButton.setEnabled(
                 browseEnabled && m_browserTextField.getEnabled());
         return enable;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     protected boolean internetExplorerSelected() {
         boolean enable = super.checkLocalhostServer();
         boolean browseEnabled = enable || isRemoteRequest();
         boolean isIE = m_browserCombo.getSelectedObject().equals(
                 Browser.InternetExplorer);
         m_browserTextField.setEnabled(!isIE);
         m_browserPathButton.setEnabled(!isIE && browseEnabled);
         return isIE;
     }
     
     /**
      * 
      * {@inheritDoc}
      */
     protected void initState() {
         m_activationMethodCombo.setEnabled(true);
         m_activationMethodCombo.setEnabled(true);
         m_autUrlTextField.setEnabled(true);
         m_autIdAttibuteTextField.setEnabled(true);
         m_browserCombo.setEnabled(true);
         m_browserPathButton.setEnabled(true);
         m_browserTextField.setEnabled(true);
         checkLocalhostServer();
         internetExplorerSelected();
         RemoteFileBrowserBP.clearCache(); // avoid all caches
     }
 
     /**
      * This private inner class contains a new ModifyListener.
      * 
      * @author BREDEX GmbH
      * @created 22.11.2006
      */
     private class WidgetModifyListener implements ModifyListener {
 
         /**
          * {@inheritDoc}
          */
         @SuppressWarnings("synthetic-access")
         public void modifyText(ModifyEvent e) {
             Object source = e.getSource();
             boolean checked = false;
             if (source.equals(m_autUrlTextField)) {
                 checked = true;
             } else if (source.equals(m_browserTextField)) {
                 checked = true;
             } else if (source.equals(m_autIdAttibuteTextField)) {
                 checked = true;
             } else if (source.equals(getServerCombo())) {
                 checkLocalhostServer();
                 checked = true;
             } else if (source.equals(m_browserCombo)) {
                 internetExplorerSelected();
                 checked = true;
             }
             if (checked) {
                 checkAll();
                 return;
             }
             Assert.notReached("Event activated by unknown widget."); //$NON-NLS-1$
         }
 
     }
 
     /** 
      * The action of the activation combo
      * @return true
      */
     boolean handleActivationComboEvent() {
         putConfigValue(AutConfigConstants.ACTIVATION_METHOD,
                 ActivationMethod.getRCString(m_activationMethodCombo
                         .getSelectedObject()));
         return true;
     }
     
     /** 
      * The action of the browser combo
      * @return true
      */
     boolean handleBrowserComboEvent() {
         putConfigValue(AutConfigConstants.BROWSER, m_browserCombo
             .getSelectedObject().toString());
         return true;
     }
 
     /**
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyUrlTextField() {
         DialogStatusParameter error = null;
         String urlText = m_autUrlTextField.getText();
         if (m_autUrlTextField.getText().length() == 0) {
             error = createErrorStatus(I18n.getString("WebAutConfigComponent.emptyUrl")); //$NON-NLS-1$
         } else {
             try {
                 new URL(urlText);
             } catch (MalformedURLException e) {
                 error = createErrorStatus(I18n.getString("WebAutConfigComponent.wrongUrl")); //$NON-NLS-1$
             }
         }
 
         putConfigValue(AutConfigConstants.AUT_URL, urlText);
         
         return error;
     }
     
     /**
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyIDAttributeTextField() {
         DialogStatusParameter error = null;
         String idText = m_autIdAttibuteTextField.getText();
         if (!idText.matches("[a-zA-Z]*")) { //$NON-NLS-1$
             error = createErrorStatus(I18n
                     .getString("HTMLAutConfigComponent.wrongAutIdAttribute")); //$NON-NLS-1$
         } else {
             putConfigValue(AutConfigConstants.WEB_ID_TAG, idText);
         }
         return error;
     }
     
     /**
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyBrowserPathTextField() {
         DialogStatusParameter error = null;
         String txt = m_browserTextField.getText();
 
         putConfigValue(AutConfigConstants.BROWSER_PATH, txt);
         
         return error;
     }
     
     /**
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifyBrowser() {
         final Browser browser = m_browserCombo.getSelectedObject();
         if (browser != null) {
             putConfigValue(AutConfigConstants.BROWSER, browser.toString());
         }
         
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     protected void populateBasicArea(Map<String, String> data) {
         super.populateBasicArea(data);
         
         String browser = data.get(AutConfigConstants.BROWSER);
         if (browser == null) {
             browser = Browser.InternetExplorer.toString();
         }
         m_browserCombo.setSelectedObject(Browser.valueOf(browser));
 
         if (!isDataNew(data)) {
             m_autUrlTextField.setText(
                 StringUtils.defaultString(
                         data.get(AutConfigConstants.AUT_URL)));
         }
     }
     
     /**
      * {@inheritDoc}
      */
     protected void populateAdvancedArea(Map<String, String> data) {
         if (!isDataNew(data)) {
             m_browserTextField.setText(StringUtils.defaultString(data
                     .get(AutConfigConstants.BROWSER_PATH)));
             String selection = data.get(AutConfigConstants.SINGLE_WINDOW_MODE);
             boolean selected = false;
             if (StringUtils.isEmpty(selection)) {
                 selected = true;
             } else {
                 selected = Boolean.parseBoolean(selection);
             }
             m_singleWindowCheckBox.setSelection(selected);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void populateExpertArea(Map<String, String> data) {
         m_activationMethodCombo.setSelectedObject(
                 ActivationMethod.getEnum(data
                         .get(AutConfigConstants.ACTIVATION_METHOD)));
         if (!isDataNew(data)) {
             String webIdTag = data.get(AutConfigConstants.WEB_ID_TAG);
             if (webIdTag == null) {
                 webIdTag = StringConstants.EMPTY;
             }
             m_autIdAttibuteTextField.setText(webIdTag);
         }
     }
 
     /**
      * 
      * @return the modifier listener.
      */
     @SuppressWarnings("synthetic-access")
     private WidgetModifyListener getModifyListener() {
         if (m_modifyListener == null) {
             m_modifyListener = new WidgetModifyListener();
         }
         
         return m_modifyListener;
 
     }
     
     /**
      * 
      * @return the single instance of the selection listener.
      */
     @SuppressWarnings("synthetic-access")
     private WidgetSelectionListener getSelectionListener() {
         if (m_selectionListener == null) {
             m_selectionListener = new WidgetSelectionListener();
         }
         
         return m_selectionListener;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     protected void checkAll(java.util.List<DialogStatusParameter> paramList) {
         super.checkAll(paramList);
         addError(paramList, modifyUrlTextField());
         addError(paramList, modifyIDAttributeTextField());
         addError(paramList, modifyBrowser());
         addError(paramList, modifyBrowserPathTextField());
         addError(paramList, modifySingleWindowCheckBox());
 
         handleActivationComboEvent();
         //handleBrowserComboEvent();
     }
     
     /**
      * @return <code>null</code> if the new value is valid. Otherwise, returns
      *         a status parameter indicating the cause of the problem.
      */
     DialogStatusParameter modifySingleWindowCheckBox() {
         DialogStatusParameter error = null;
         Boolean checked = m_singleWindowCheckBox.getSelection();
         putConfigValue(AutConfigConstants.SINGLE_WINDOW_MODE,
                 checked.toString());
         
         return error;
     }
 
     /**
      * Inits the SingleWindowMode CheckBox which tells the server in which mode to run
      * @param parent The parent Composite.
      */
     protected void createSingleModeCheckBox(Composite parent) {
         Label singleWindowModeLabel = UIComponentHelper.createLabel(parent, "WebAutConfigComponent.singleWindowMode"); //$NON-NLS-1$ 
         singleWindowModeLabel.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.singleWindowModeLabel"); //$NON-NLS-1$
         ControlDecorator.decorateInfo(singleWindowModeLabel,  
                 "GDControlDecorator.SingleWindowMode", false); //$NON-NLS-1$
         m_singleWindowCheckBox = UIComponentHelper
                 .createToggleButton(parent, 1);
         m_singleWindowCheckBox.setData(SwtAUTHierarchyConstants.WIDGET_NAME, "org.eclipse.jubula.toolkit.provider.html.gui.HtmlAutConfigComponent.SingleWindowCheckBox"); //$NON-NLS-1$ 
         
     }
     
 }
