 // $codepro.audit.disable com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.alwaysOverridetoString.alwaysOverrideToString, com.instantiations.assist.eclipse.analysis.audit.rule.effectivejava.constructorsOnlyInvokeFinalMethods, useForLoop, com.instantiations.assist.eclipse.analysis.deserializeabilitySecurity, com.instantiations.assist.eclipse.analysis.disallowReturnMutable, com.instantiations.assist.eclipse.analysis.enforceCloneableUsageSecurity, explicitThisUsage
 /*******************************************************************************
  * Copyright (c) 2010 Ericsson Research Canada
  * 
  * All rights reserved. This program and the accompanying materials are
  * made available under the terms of the Eclipse Public License v1.0 which
  * accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Description:
  * 
  * This class implements the dialog used to fill-in the Anomaly element details
  * This is a modeless-like dialog
  * 
  * Contributors:
  *   Sebastien Dubois - Created for Mylyn Review R4E project
  *   
  ******************************************************************************/
 
 package org.eclipse.mylyn.reviews.r4e.ui.dialogs;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.IInputValidator;
 import org.eclipse.mylyn.reviews.r4e.ui.Activator;
 import org.eclipse.mylyn.reviews.r4e.ui.utils.R4EUIConstants;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * @author lmcdubo
  * @version $Revision: 1.0 $
  */
 public class R4EAnomalyInputDialog extends Dialog {
 
 	// ------------------------------------------------------------------------
 	// Constants
 	// ------------------------------------------------------------------------
 	
 	/**
 	 * Field MINIMUM_DIALOG_AREA_HEIGHT.
 	 * (value is 150)
 	 */
 	private static final int MINIMUM_DIALOG_AREA_HEIGHT = 150;
 
     /**
      * The title of the dialog.
      */
     private final String fTitle;
 
     /**
      * The message to display, or <code>null</code> if none.
      */
     private final String fCommentMessage;
     
     /**
      * The input value; the empty string by default.
      */
     private String fCommentValue = "";
     
     /**
      * Input text widget.
      */
     private Text fCommentInputTextField;
     
 	/**
 	 * Field fAnomalyMessage.
 	 */
 	private String fAnomalyMessage = null;
 	
 	/**
 	 * Field fAnomalyValue.
 	 */
 	private String fAnomalyValue = "";
 	
 	/**
 	 * Field fAnomalyInputTextField.
 	 */
 	private Text fAnomalyInputTextField = null;
 
     /**
      * Ok button widget.
      */
     private Button fOKButton;
 	
     /**
      * The input validator, or <code>null</code> if none.
      */
     private final IInputValidator fValidator;
     
     
 	// ------------------------------------------------------------------------
 	// Constructors
 	// ------------------------------------------------------------------------
     
 	/**
 	 * Constructor for R4EAnomalyInputDialog.
 	 * @param aParentShell Shell
 	 * @param aDialogTitle String
 	 * @param aAnomalyMessage String
 	 * @param aCommentMessage String
 	 */
 	public R4EAnomalyInputDialog(Shell aParentShell, String aDialogTitle, String aAnomalyMessage, 
 			String aCommentMessage) {
 		super(aParentShell);
 		setBlockOnOpen(false);
 		fTitle = aDialogTitle;
 		fAnomalyMessage = aAnomalyMessage;
 		fCommentMessage = aCommentMessage;
 		fValidator = new EmptyInputValidator();
 	}
 	
 	
 	// ------------------------------------------------------------------------
 	// Methods
 	// ------------------------------------------------------------------------
 	
     /**
      * Method buttonPressed.
      * @param buttonId int
      * @see org.eclipse.jface.dialogs.Dialog#buttonPressed(int)
      */
     @Override
 	protected void buttonPressed(int buttonId) {
         if (buttonId == IDialogConstants.OK_ID) {
 	    	this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_WAIT));
 
         	//Validate Anomaly Title
         	String validateResult = validateEmptyInput(fAnomalyInputTextField);
         	if (null != validateResult) {
         		//Validation of input failed
     			final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR, "No input given for Anomaly Title", 
     					new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, validateResult, null), IStatus.ERROR);
     			dialog.open();
     			this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
         		return;
         	}
         	fAnomalyValue = fAnomalyInputTextField.getText();
         	
         	//Validate Anomaly Comment
         	validateResult = validateEmptyInput(fCommentInputTextField);
         	if (null != validateResult) {
         		//Validation of input failed
     			final ErrorDialog dialog = new ErrorDialog(null, R4EUIConstants.DIALOG_TITLE_ERROR, "No input given for Anomaly Comment",
         				new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0, validateResult, null), IStatus.ERROR);
     			dialog.open();
     			this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
         		return;
         	}
         	fCommentValue = fCommentInputTextField.getText();
         	
         } else {
         	fAnomalyValue = null;
         	fCommentValue = null;
         }
 		this.getShell().setCursor(this.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
         super.buttonPressed(buttonId);
     }
     
     /**
      * Method configureShell.
      * @param shell Shell
      * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
      */
     @Override
 	protected void configureShell(Shell shell) {
         super.configureShell(shell);
         if (null != fTitle) {
 			shell.setText(fTitle);
 		}
     }
     
     /**
      * Method createDialogArea.
      * @param parent Composite
      * @return Control
      * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
      */
     @Override
 	protected Control createDialogArea(Composite parent) {
         
     	// create composite
         final Composite composite = (Composite) super.createDialogArea(parent);
         
         // create Anomaly Title message label
         if (null != fAnomalyMessage) {
             final Label label = new Label(composite, SWT.WRAP);
             label.setText(fAnomalyMessage);
             final GridData anomalyLabelData = new GridData(GridData.FILL, GridData.FILL, true, false);
             anomalyLabelData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
             label.setLayoutData(anomalyLabelData);
             label.setFont(parent.getFont());
         }
         
         //create Anomaly Title input text field
         fAnomalyInputTextField = new Text(composite, getInputTextStyle());
         fAnomalyInputTextField.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false));
         
         // create Comment message label
         if (null != fCommentMessage) {
             final Label label = new Label(composite, SWT.WRAP);
             label.setText(fCommentMessage);
             final GridData commentLabelData = new GridData(GridData.FILL, GridData.FILL, true, false);
             commentLabelData.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
             label.setLayoutData(commentLabelData);
             label.setFont(parent.getFont());
         }
         
         //create Comment input text field
         fCommentInputTextField = new Text(composite, getInputTextStyle() | SWT.V_SCROLL);
         fCommentInputTextField.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
         composite.getShell().setMinimumSize(convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH), 
         		convertHorizontalDLUsToPixels(MINIMUM_DIALOG_AREA_HEIGHT));
         
         applyDialogFont(composite);
         return composite;
     }
 
     /**
      * Method createButtonsForButtonBar.
      * @param parent Composite
      * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
      */
     @Override
 	protected void createButtonsForButtonBar(Composite parent) {
         // create OK and Cancel buttons by default
     	fOKButton = createButton(parent, IDialogConstants.OK_ID,
                 IDialogConstants.OK_LABEL, true);
         createButton(parent, IDialogConstants.CANCEL_ID,
                 IDialogConstants.CANCEL_LABEL, false);
         //do this here because setting the text will set enablement on the ok button
         fAnomalyInputTextField.setFocus();
         if (null != fAnomalyValue) {
         	fAnomalyInputTextField.setText(fAnomalyValue);
         	fAnomalyInputTextField.selectAll();
         }
     }
     
 	/**
 	 * Returns the style bits that should be used for the input text field.
 	 * Defaults to a single line entry. Subclasses may override.
 	 * @since 3.4
 	 * @return the integer style bits that should be used when creating the input text 
 	 */
 	protected int getInputTextStyle() {
 		return SWT.MULTI | SWT.WRAP;
 	}
     
 	/**
 	 * Method isResizable.
 	 * @return boolean
 	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
 	 */
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 	
     /**
      * Returns the ok button.
      * @return the ok button
      */
     protected Button getOkButton() {
         return fOKButton;
     }
     
     /**
      * Returns the text area.
      * @return the anomaly title text area
      */
     protected Text getAnomalyText() {
         return fAnomalyInputTextField;
     }
     
     /**
      * Returns the string typed into this input dialog.
      * @return the anomaly title input string
      */
     public String getAnomalyValue() {
         return fAnomalyValue;
     }
     
     /**
      * Returns the text area.
      * @return the comment text area
      */
     protected Text getCommentText() {
         return fCommentInputTextField;
     }
     
     /**
      * Returns the string typed into this input dialog.
      * @return the comment input string
      */
     public String getCommentValue() {
         return fCommentValue;
     }
     
     /**
      * Method validateEmptyInput.
      * @param aText Text
      * @return String
      */
     private String validateEmptyInput(Text aText) {
         if (null != fValidator) {
             return ((EmptyInputValidator) fValidator).isValid(aText.getText());
         }
         return null;
     }
     
     /**
      * Method setShellStyle.
      * @param newShellStyle int
      */
     @Override
 	protected void setShellStyle(int newShellStyle) {	
     	int newstyle = newShellStyle & ~SWT.APPLICATION_MODAL; /* turn off APPLICATION_MODAL */
     	newstyle |= SWT.MODELESS; /* turn on MODELESS */
     	super.setShellStyle(newstyle); 
     }
     
     /**
      * Method open.
      * @return int
      */
     @Override
 	public int open() {
     	super.open();
     	pumpMessages(); /* this will let the caller wait till OK, Cancel is pressed, but will let the other GUI responsive */
     	//return retVal;
     	return super.getReturnCode();
     }
 
     /**
      * Method pumpMessages.
      */
     protected void pumpMessages() {
     	final Shell sh = getShell();	
     	final Display disp = sh.getDisplay(); 
     	while( !sh.isDisposed() ) { // $codepro.audit.disable methodInvocationInLoopCondition
     		if( !disp.readAndDispatch() ) disp.sleep();
     	}
     	disp.update();
     }
 }
