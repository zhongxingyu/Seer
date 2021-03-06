 /*
  * Copyright 2002-2004 the original author or authors.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package org.springframework.richclient.dialog;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Window;
 import java.awt.event.KeyEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowFocusListener;
 
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.KeyStroke;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.context.MessageSourceResolvable;
 import org.springframework.richclient.application.Application;
 import org.springframework.richclient.application.ApplicationServicesAccessorSupport;
 import org.springframework.richclient.application.ApplicationWindow;
 import org.springframework.richclient.command.AbstractCommand;
 import org.springframework.richclient.command.ActionCommand;
 import org.springframework.richclient.command.CommandGroup;
 import org.springframework.richclient.core.Guarded;
 import org.springframework.richclient.core.TitleConfigurable;
 import org.springframework.richclient.util.GuiStandardUtils;
 import org.springframework.util.Assert;
 import org.springframework.util.StringUtils;
 
 import com.jgoodies.forms.factories.Borders;
 
 /**
  * Abstract Base Class for a dialog with standard layout, buttons, and behavior.
  * <p>
  * Use of this class will apply a standard appearance to dialogs in the
  * application.
  * <P>
  * Subclasses implement the body of the dialog (wherein business objects are
  * manipulated), and the action taken by the <code>OK</code> button.
  * 
  * <P>
  * Services of a <code>ApplicationDialog</code> include:
  * <ul>
  * <li>centering on the parent frame
  * <li>reusing the parent's icon
  * <li>standard layout and border spacing, based on Java Look and Feel
  * guidelines.
  * <li>uniform naming style for dialog title
  * <li><code>OK</code> and <code>Cancel</code> buttons at the bottom of the
  * dialog -<code>OK</code> is the default, and the <code>Escape</code> key
  * activates <code>Cancel</code> (the latter works only if the dialog receives
  * the escape keystroke, and not one of its components)
  * <li>by default, modal enabling & disabling of resizing
  * </ul>
  */
 public abstract class ApplicationDialog extends
         ApplicationServicesAccessorSupport implements TitleConfigurable,
         Guarded {
     private static final String DEFAULT_DIALOG_TITLE = "Application Dialog";
 
     protected final Log logger = LogFactory.getLog(getClass());
 
     protected static final String DEFAULT_FINISH_KEY = "okCommand";
 
     protected static final String DEFAULT_CANCEL_KEY = "cancelCommand";
 
     private String title;
 
     private JDialog dialog;
 
     private Window parent;
 
     private CloseAction closeAction = CloseAction.DISPOSE;
 
     private boolean defaultEnabled = true;
 
     private boolean modal = true;
 
     private boolean resizable = true;
 
     private ActionCommand finishCommand;
 
     private ActionCommand cancelCommand;
 
     private CommandGroup dialogCommandGroup;
 
     private boolean displayFinishSuccessMessage;
 
     public ApplicationDialog() {
 
     }
 
     public ApplicationDialog(String title, Window parent) {
         setTitle(title);
         setParent(parent);
     }
 
     /**
      * Creates a new application dialog; the actual UI is not initialized until
      * showDialog() is called.
      * 
      * @param title
      *            text which appears in the title bar after the name of the
      *            application, and satisfies
      * @param parent
      *            frame to which this dialog is attached.
      * @param closeAction
      *            sets the behaviour of the dialog upon close.
      */
     public ApplicationDialog(String title, Window parent,
             CloseAction closeAction) {
         setTitle(title);
         setParent(parent);
         setCloseAction(closeAction);
     }
 
     protected String getTitle() {
         if (!StringUtils.hasText(this.title)) {
             if (StringUtils.hasText(getCallingCommandText())) {
                 return getCallingCommandText();
             }
             else {
                 return DEFAULT_DIALOG_TITLE;
             }
         }
         return this.title;
     }
 
     public void setTitle(String title) {
         this.title = title;
         if (dialog != null) {
             dialog.setTitle(getTitle());
         }
     }
 
     public void setParent(Window parent) {
         if (parent != null) {
             if (parent instanceof JFrame || parent instanceof JDialog) {
                 if (this.parent != parent) {
                     if (isControlCreated()) {
                         dialog.setLocationRelativeTo(parent);
                     }
                     this.parent = parent;
                 }
             }
             else {
                 throw new IllegalArgumentException(
                         "Parent must be a JFrame or JDialog.");
             }
         }
         else {
             this.parent = null;
         }
     }
 
     public void setCloseAction(CloseAction action) {
         this.closeAction = action;
     }
 
     /**
      * Should the finish button be enabled by default?
      * 
      * @param enabled
      *            true or false
      */
     public void setDefaultEnabled(boolean enabled) {
         this.defaultEnabled = enabled;
     }
 
     public void setModal(boolean modal) {
         this.modal = modal;
     }
 
     public void setResizable(boolean resizable) {
         this.resizable = resizable;
     }
 
     public void setEnabled(boolean enabled) {
         setFinishEnabled(enabled);
     }
 
     public void setDisplayFinishSuccessMessage(
             boolean displayFinishSuccessMessage) {
         this.displayFinishSuccessMessage = displayFinishSuccessMessage;
     }
 
     protected boolean getDisplayFinishSuccessMessage() {
         return displayFinishSuccessMessage;
     }
 
     protected void setFinishEnabled(boolean enabled) {
         if (isControlCreated()) {
             finishCommand.setEnabled(enabled);
         }
     }
 
     public boolean isShowing() {
         if (!isControlCreated()) { return false; }
         return dialog.isShowing();
     }
 
     public boolean isControlCreated() {
         return dialog != null;
     }
 
     public JDialog getDialog() {
         return dialog;
     }
 
     protected Container getDialogContentPane() {
         Assert.isTrue(isControlCreated());
         return dialog.getContentPane();
     }
 
     protected ApplicationWindow getActiveWindow() {
         return Application.instance().getActiveWindow();
     }
 
     /**
      * Show the dialog. If the dialog has not already been initialized it will
      * be here.
      */
     public void showDialog() {
         if (!isControlCreated()) {
             createDialog();
             dialog.pack();
             onAboutToShow();
             dialog.setLocationRelativeTo(parent);
             dialog.setVisible(true);
         }
         else {
             if (!isShowing()) {
                 onAboutToShow();
                 dialog.setVisible(true);
             }
         }
     }
 
     /**
      * Subclasses should call if layout of the dialog components changes.
      */
     protected void componentsChanged() {
         if (isControlCreated()) {
             dialog.pack();
             dialog.setLocationRelativeTo(parent);
         }
     }
 
     /**
      * Builds/initializes the dialog and all of its components.
      * <p>
      * Follows the Java Look and Feel guidelines for spacing elements.
      */
     protected final void createDialog() {
         constructDialog();
         addDialogComponents();
         attachListeners();
         registerDefaultCommand();
         onInitialized();
     }
 
     private void constructDialog() {
         if (parent instanceof JFrame) {
            dialog = new JDialog((JFrame)parent, getTitle(), modal);
         }
         else if (parent instanceof JDialog) {
            dialog = new JDialog((JDialog)parent, getTitle(), modal);
         }
         else {
             if (logger.isInfoEnabled()) {
                 logger
                         .warn("Parent is not a JFrame or JDialog, it is "
                                 + parent
                                 + ". Using current active window as parent by default.");
             }
             if (getActiveWindow() != null) {
                 dialog = new JDialog(getActiveWindow().getControl(), title,
                         modal);
             }
             else {
                 dialog = new JDialog((JFrame)null, title, modal);
             }
         }
         dialog.getContentPane().setLayout(new BorderLayout());
         dialog.setDefaultCloseOperation(this.closeAction.getValue());
         dialog.setResizable(resizable);
         initStandardCommands();
         addCancelByEscapeKey();
     }
 
     private void initStandardCommands() {
         finishCommand = new ActionCommand(getFinishFaceConfigurationKey()) {
             public void doExecuteCommand() {
                 try {
                     boolean result = onFinish();
                     if (result) {
                         if (getDisplayFinishSuccessMessage()) {
                             showFinishSuccessMessageDialog();
                         }
                         hide();
                     }
                 }
                 catch (Exception e) {
                     logger
                             .warn(
                                     "Exception occured executing dialog finish command.",
                                     e);
                     onFinishException(e);
                 }
             }
         };
         finishCommand.setEnabled(defaultEnabled);
 
         cancelCommand = new ActionCommand(getCancelFaceConfigurationKey()) {
             public void doExecuteCommand() {
                 onCancel();
             }
         };
     }
 
     /**
      * Subclasses may override to return a custom message key, default is
      * "dialog.ok", corresponding to the "&OK" label.
      * 
      * @return The message key to use for the finish ("ok") button
      */
     protected String getFinishFaceConfigurationKey() {
         return DEFAULT_FINISH_KEY;
     }
 
     protected void showFinishSuccessMessageDialog() {
         JOptionPane.showMessageDialog(getDialog(), getFinishSuccessMessage(),
                 getFinishSuccessTitle(), JOptionPane.INFORMATION_MESSAGE);
     }
 
     protected String getFinishSuccessMessage() {
         return getMessage(getFinishCommand().getId() + ".successMessage",
                 getFinishSuccessMessageArguments());
     }
 
     protected Object[] getFinishSuccessMessageArguments() {
         return new Object[0];
     }
 
     protected String getFinishSuccessTitle() {
         return getMessage("finishSuccessTitle",
                 getFinishSuccessTitleArguments());
     }
 
     protected Object[] getFinishSuccessTitleArguments() {
         if (StringUtils.hasText(getCallingCommandText())) {
             return new Object[] { getCallingCommandText() };
         }
         else {
             return new Object[0];
         }
     }
 
     protected String getCallingCommandText() {
         return null;
     }
 
     protected void onFinishException(Exception e) {
         String exceptionMessage;
         if (e instanceof MessageSourceResolvable) {
             exceptionMessage = getMessageSourceAccessor().getMessage(
                     (MessageSourceResolvable)e);
         }
         else {
             exceptionMessage = e.getLocalizedMessage();
         }
         if (!StringUtils.hasText(exceptionMessage)) {
             String defaultMessage = "Unable to finish; an application exception occured.\nPlease contact your administrator.";
             exceptionMessage = getMessageSourceAccessor().getMessage(
                     "applicationDialog.defaultFinishException", defaultMessage);
         }
         JOptionPane.showMessageDialog(getDialog(), exceptionMessage,
                 Application.instance().getName(), JOptionPane.ERROR_MESSAGE);
     }
 
     protected ActionCommand getFinishCommand() {
         return finishCommand;
     }
 
     protected ActionCommand getCancelCommand() {
         return cancelCommand;
     }
 
     /**
      * Force the escape key to call the same action as pressing the Cancel
      * button. This does not always work. See class comment.
      */
     private void addCancelByEscapeKey() {
         int noModifiers = 0;
         KeyStroke escapeKey = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,
                 noModifiers, false);
         addActionKeyBinding(escapeKey, cancelCommand.getId());
     }
 
     protected void addActionKeyBinding(KeyStroke key, String actionKey) {
         if (actionKey == finishCommand.getId()) {
             addActionKeyBinding(key, actionKey, finishCommand
                     .getActionAdapter());
         }
         else if (actionKey == cancelCommand.getId()) {
             addActionKeyBinding(key, actionKey, cancelCommand
                     .getActionAdapter());
         }
         else {
             throw new IllegalArgumentException("Unknown action key "
                     + actionKey);
         }
     }
 
     protected void addActionKeyBinding(KeyStroke key, String actionKey,
             Action action) {
         getInputMap().put(key, actionKey);
         getActionMap().put(actionKey, action);
     }
 
     protected ActionMap getActionMap() {
         return getDialog().getRootPane().getActionMap();
     }
 
     protected InputMap getInputMap() {
         return getDialog().getRootPane().getInputMap(
                 JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
     }
 
     /**
      * Subclasses may override to customize how this dialog is built.
      */
     protected void addDialogComponents() {
         JComponent dialogContentPane = createDialogContentPane();
         dialogContentPane.setBorder(Borders.DIALOG_BORDER);
         getDialogContentPane().add(dialogContentPane);
         getDialogContentPane().add(createButtonBar(), BorderLayout.SOUTH);
     }
 
     protected final void attachListeners() {
         dialog.addWindowFocusListener(new WindowFocusListener() {
             public void windowActivated(WindowEvent e) {
                 ApplicationDialog.this.onWindowActivated();
             }
 
             public void windowGainedFocus(WindowEvent e) {
                 ApplicationDialog.this.onWindowGainedFocus();
             }
 
             public void windowLostFocus(WindowEvent e) {
                 ApplicationDialog.this.onWindowLostFocus();
             }
         });
         dialog.addWindowListener(new WindowAdapter() {
             public void windowClosing(WindowEvent e) {
                 ApplicationDialog.this.onWindowClosing();
                 if (closeAction == CloseAction.DISPOSE) {
                     dialog = null;
                 }
             }
         });
     }
 
     /**
      * Return a standardized row of command buttons, right-justified and all of
      * the same size, with OK as the default button, and no mnemonics used, as
      * per the Java Look and Feel guidelines.
      */
     protected JComponent createButtonBar() {
         this.dialogCommandGroup = CommandGroup.createCommandGroup(null,
                 getCommandGroupMembers());
         JComponent buttonBar = this.dialogCommandGroup.createButtonBar();
         GuiStandardUtils.attachBorder(buttonBar, Borders.DIALOG_BORDER);
         return buttonBar;
     }
 
     protected Object[] getCommandGroupMembers() {
         return new AbstractCommand[] { getFinishCommand(), getCancelCommand() };
     }
 
     /**
      * Register the finish button as the default dialog button.
      */
     protected void registerDefaultCommand() {
         if (isControlCreated()) {
             finishCommand.setDefaultButtonIn(getDialog());
         }
     }
 
     protected final void registerCancelCommandAsDefault() {
         if (isControlCreated()) {
             cancelCommand.setDefaultButtonIn(getDialog());
         }
     }
 
     /**
      * Register the provided button as the default dialog button. The button
      * must be present on the dialog.
      * 
      * @param button
      *            The button to become the default.
      */
     protected final void registerDefaultCommand(ActionCommand command) {
         if (isControlCreated()) {
             command.setDefaultButtonIn(getDialog());
         }
     }
 
     protected String getCancelFaceConfigurationKey() {
         return DEFAULT_CANCEL_KEY;
     }
 
     /**
      * Template lifecycle method invoked after the dialog control is
      * initialized.
      */
     protected void onInitialized() {
     }
 
     /**
      * Template lifecycle method invoked right before the dialog is to become
      * visible.
      */
     protected void onAboutToShow() {
     }
 
     /**
      * Template lifecycle method invoked when the dialog gains focus.
      */
     protected void onWindowGainedFocus() {
     }
 
     /**
      * Template lifecycle method invoked when the dialog is activated.
      */
     protected void onWindowActivated() {
     }
 
     /**
      * Template lifecycle method invoked when the dialog loses focus.
      */
     protected void onWindowLostFocus() {
     }
 
     /**
      * Template lifecycle method invoked when the dialog's window is closing.
      */
     protected void onWindowClosing() {
     }
 
     /**
      * Handle a dialog cancellation request.
      */
     protected void onCancel() {
         if (closeAction == CloseAction.HIDE) {
             hide();
         }
         else {
             dispose();
         }
     }
 
     /**
      * Close and dispose of the editor dialog. This forces the dialog to be
      * re-built on the next show.
      */
     protected final void dispose() {
         onWindowClosing();
         this.dialog.dispose();
         this.dialog = null;
     }
 
     /**
      * Hide the dialog. This differs from dispose in that the dialog control
      * stays cached in memory.
      */
     protected final void hide() {
         onWindowClosing();
         this.dialog.setVisible(false);
     }
 
     /**
      * Return the GUI which allows the user to manipulate the business objects
      * related to this dialog; this GUI will be placed above the <code>OK</code>
      * and <code>Cancel</code> buttons, in a standard manner.
      */
     protected abstract JComponent createDialogContentPane();
 
     /**
      * Request invocation of the action taken when the user hits the
      * <code>OK</code> (finish) button.
      * 
      * @return true if action completed succesfully; false otherwise.
      */
     protected abstract boolean onFinish();
 
 }
