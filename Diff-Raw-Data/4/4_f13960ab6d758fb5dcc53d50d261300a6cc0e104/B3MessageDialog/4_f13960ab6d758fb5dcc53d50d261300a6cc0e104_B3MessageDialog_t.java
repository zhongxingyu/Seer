 /*******************************************************************************
  * Copyright (c) 2000, 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *     Sebastian Davids <sdavids@gmx.de> - Fix for bug 93353 - 
  *     [Dialogs] B3MessageDialog#buttonPressed should explicitly call super
  *     Cloudsmith Inc - modified for use in b3 and renamed from B3MessageDialog
  *******************************************************************************/
 package org.eclipse.b3.build.ui.dialogs;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * Added a Details button to the MessageDialog to show the exception
  * stack trace.
  */
 public class B3MessageDialog extends MessageDialog {
 
 	public static void openMultiStatusError(Shell parent, String title, String message, IStatus status, int defaultIndex) {
 		String[] labels;
 		if(status == null || status.isOK()) {
 			labels = new String[] { IDialogConstants.OK_LABEL };
 			defaultIndex = 0;
 		}
 		else {
 			labels = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.SHOW_DETAILS_LABEL };
 			defaultIndex = 0;
 		}
 
 		B3MessageDialog dialog = new B3MessageDialog(parent, title, null, // accept the default window icon
 		message, status, MessageDialog.ERROR, labels, defaultIndex);
 		if(status != null && !status.isOK()) {
 			dialog.setDetailButton(1);
 		}
 		dialog.open();
 
 	}
 
 	/**
 	 * Convenience method to open a simple Yes/No question dialog.
 	 * 
 	 * @param parent
 	 *            the parent shell of the dialog, or <code>null</code> if none
 	 * @param title
 	 *            the dialog's title, or <code>null</code> if none
 	 * @param message
 	 *            the message
 	 * @param detail
 	 *            the error
 	 * @param defaultIndex
 	 *            the default index of the button to select
 	 * @return <code>true</code> if the user presses the OK button, <code>false</code> otherwise
 	 */
 	public static boolean openQuestion(Shell parent, String title, String message, Throwable detail, int defaultIndex) {
 		String[] labels;
 		if(detail == null) {
 			labels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL };
 		}
 		else {
 			labels = new String[] {
 					IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.SHOW_DETAILS_LABEL };
 		}
 
 		B3MessageDialog dialog = new B3MessageDialog(parent, title, null, // accept the default window icon
 		message, detail, QUESTION, labels, defaultIndex);
 		if(detail != null) {
 			dialog.setDetailButton(2);
 		}
 		return dialog.open() == 0;
 	}
 
 	/**
 	 * Convenience method to open an error dialog with a stack trace
 	 * 
 	 * @param parent
 	 *            the parent shell of the dialog, or <code>null</code> if none
 	 * @param title
 	 *            the dialog's title, or <code>null</code> if none
 	 * @param message
 	 *            the message
 	 * @param detail
 	 *            the error
 	 * @param defaultIndex
 	 *            the default index of the button to select
 	 * @return <code>true</code> if the user presses the OK button, <code>false</code> otherwise
 	 */
 	public static void openStackTrace(Shell parent, String title, String message, Throwable detail, int defaultIndex) {
 		String[] labels;
 		if(detail == null) {
 			labels = new String[] { IDialogConstants.OK_LABEL };
 			defaultIndex = 0;
 		}
 		else {
 			labels = new String[] { IDialogConstants.OK_LABEL, IDialogConstants.SHOW_DETAILS_LABEL };
 			defaultIndex = 0;
 		}
 
 		B3MessageDialog dialog = new B3MessageDialog(parent, title, null, // accept the default window icon
 		message, detail, MessageDialog.ERROR, labels, defaultIndex);
 		if(detail != null) {
 			dialog.setDetailButton(1);
 		}
 		dialog.open();
 	}
 
 	private Throwable detail;
 
 	private IStatus detailedStatus;
 
 	private int detailButtonID = -1;
 
 	private Text text;
 
 	// Workaround. SWT does not seem to set the default button if
 	// there is not control with focus. Bug: 14668
 	private int defaultButtonIndex = 0;
 
 	/**
 	 * Size of the text in lines.
 	 */
 	private static final int TEXT_LINE_COUNT = 15;
 
 	public B3MessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
 			IStatus detail, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
 		super(
 			parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
 			defaultIndex);
 		defaultButtonIndex = defaultIndex;
 		this.detail = null;
 		this.detailedStatus = detail;
 		setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
 	}
 
 	/**
 	 * Create a new dialog.
 	 * 
 	 * @param parentShell
 	 *            the parent shell
 	 * @param dialogTitle
 	 *            the title
 	 * @param dialogTitleImage
 	 *            the title image
 	 * @param dialogMessage
 	 *            the message
 	 * @param detail
 	 *            the error to display
 	 * @param dialogImageType
 	 *            the type of image
 	 * @param dialogButtonLabels
 	 *            the button labels
 	 * @param defaultIndex
 	 *            the default selected button index
 	 */
 	public B3MessageDialog(Shell parentShell, String dialogTitle, Image dialogTitleImage, String dialogMessage,
 			Throwable detail, int dialogImageType, String[] dialogButtonLabels, int defaultIndex) {
 		super(
 			parentShell, dialogTitle, dialogTitleImage, dialogMessage, dialogImageType, dialogButtonLabels,
 			defaultIndex);
 		defaultButtonIndex = defaultIndex;
 		this.detail = detail;
 		this.detailedStatus = null;
 		setShellStyle(getShellStyle() | SWT.APPLICATION_MODAL);
 	}
 
 	// Workaround. SWT does not seem to set rigth the default button if
 	// there is not control with focus. Bug: 14668
 	@Override
 	public int open() {
 		create();
 		Button b = getButton(defaultButtonIndex);
 		b.setFocus();
 		b.getShell().setDefaultButton(b);
 		return super.open();
 	}
 
 	/**
 	 * Set the detail button;
 	 * 
 	 * @param index
 	 *            the detail button index
 	 */
 	public void setDetailButton(int index) {
 		detailButtonID = index;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * Method declared on Dialog.
 	 */
 	@Override
 	protected void buttonPressed(int buttonId) {
 		if(buttonId == detailButtonID) {
 			toggleDetailsArea();
 		}
 		else {
 			super.buttonPressed(buttonId);
 		}
 	}
 
 	/**
 	 * Create this dialog's drop-down list component.
 	 * 
 	 * @param parent
 	 *            the parent composite
 	 */
 	protected void createDropDownText(Composite parent) {
 		// create the list
 		text = new Text(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
 		text.setFont(parent.getFont());
 
 		// print the stacktrace in the text field
 		if(this.detail != null) {
 			try {
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				PrintStream ps = new PrintStream(baos);
 				detail.printStackTrace(ps);
 				ps.flush();
 				baos.flush();
 				text.setText(baos.toString());
 			}
 			catch(IOException e) {
 			}
 		}
 		else {
 			try {
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				PrintStream ps = new PrintStream(baos);
 				printStatusOnStream(detailedStatus, ps, 0);
 				ps.flush();
 				baos.flush();
 				text.setText(baos.toString());
 			}
 			catch(IOException e) {
 			}
 		}
 
 		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL |
 				GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
 		data.heightHint = text.getLineHeight() * TEXT_LINE_COUNT;
 		data.horizontalSpan = 2;
 		text.setLayoutData(data);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
 	 */
 	@Override
 	protected boolean isResizable() {
 		return true;
 	}
 
 	private void indent(PrintStream ps, int indentLevel) {
 		for(int i = 0; i < indentLevel * 4; i++)
 			ps.print(' ');
 	}
 
 	/**
 	 * Prints the exception message.
 	 * 
 	 * @param t
 	 * @param ps
 	 * @param indentLevel
 	 */
 	private void printExceptionOnStream(Throwable t, PrintStream ps, int indentLevel) {
 		if(t == null || t.getMessage() == null)
 			return;
 		indent(ps, indentLevel);
 		ps.println(t.getMessage());
 	}
 
 	private void printStatusOnStream(IStatus status, PrintStream ps, int indentLevel) {
 		indent(ps, indentLevel);
 		ps.println(status.getMessage());
 
 		if(status.isMultiStatus()) {
 			for(IStatus s : status.getChildren())
 				printStatusOnStream(s, ps, indentLevel + 1);
 		}
 		else {
 			Throwable t = status.getException();
 			if(t instanceof CoreException) {
				printStatusOnStream(((CoreException) t).getStatus(), ps, indentLevel + 1);
 			}
 			else {
 				printExceptionOnStream(t, ps, indentLevel + 1);
 			}
 		}
 	}
 
 	/**
 	 * Toggles the unfolding of the details area. This is triggered by
 	 * the user pressing the details button.
 	 */
 	private void toggleDetailsArea() {
 		Point windowSize = getShell().getSize();
 		Point oldSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
 
 		if(text != null) {
 			text.dispose();
 			text = null;
 			getButton(detailButtonID).setText(IDialogConstants.SHOW_DETAILS_LABEL);
 		}
 		else {
 			createDropDownText((Composite) getContents());
 			getButton(detailButtonID).setText(IDialogConstants.HIDE_DETAILS_LABEL);
 		}
 
 		Point newSize = getContents().computeSize(SWT.DEFAULT, SWT.DEFAULT);
 		getShell().setSize(new Point(windowSize.x, windowSize.y + (newSize.y - oldSize.y)));
 	}
 }
