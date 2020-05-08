 package com.vectorsf.jvoice.ui.diagram.properties.editting;
 
 import org.eclipse.jface.viewers.ICellEditorListener;
 import org.eclipse.jface.viewers.TextCellEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.ui.PlatformUI;
 
 public class ValidateListener implements ICellEditorListener {
 
 	private TextCellEditor testCell;
 	private CLabel error;

 	public ValidateListener(TextCellEditor testCell, CLabel error) {
		this.testCell = testCell;
		this.error = error;
	}

 	@Override
	public void applyEditorValue() {
	}
 
 	@Override
	public void cancelEditor() {
	}
 
 	@Override
 	public void editorValueChanged(boolean oldValidState, boolean newValidState) {
 		if (!newValidState) {
 			error.setText(testCell.getErrorMessage());
			error.setBackground(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay()
					.getSystemColor(SWT.COLOR_RED));
			error.setForeground(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay()
					.getSystemColor(SWT.COLOR_WHITE));
		} else {
 			error.setText("");
			error.setBackground(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay()
					.getSystemColor(SWT.INHERIT_DEFAULT));
 		}
 	}
 }
