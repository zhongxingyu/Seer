 package org.eclipse.stem.ui.widgets;
 
 /*******************************************************************************
  * Copyright (c) 2010 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.ArrayList;
 
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.eclipse.emf.ecore.xmi.XMIResource;
 import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
 import org.eclipse.jface.dialogs.InputDialog;
 import org.eclipse.stem.ui.widgets.MatrixEditorWidget.MatrixEditorEvent;
 import org.eclipse.stem.ui.widgets.MatrixEditorWidget.MatrixEditorValidator;
 import org.eclipse.stem.ui.wizards.Messages;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.PaintEvent;
 import org.eclipse.swt.events.PaintListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Dialog;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 public class RenameDialog extends Dialog {
 
 	private String title;
 	boolean cancelPressed = false;
 	XMIResource resource;
 	String value;
 	
 	public RenameDialog (Shell parent, int style, XMIResource resource, String title) {
 		super (parent, style);
 		this.title = title;
 		this.resource = resource;
 	}
 	
 	/**
 	 * open the modal window. 
 	 * @return The String[] with results, or null if cancel was pressed.
 	 */
 	public String  open () {
 		Shell parent = getParent();
 		final Shell shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
 		shell.setText(title);
 		GridLayout gl = new GridLayout();
 		gl.numColumns = 2;
 		shell.setLayout(gl);
 		
 		GridData gd = new GridData();
 		gd.horizontalSpan = 2;
 		
 		final Text text = new Text(shell, SWT.NONE);
 		if(resource != null) {
 			text.setText(resource.getURI().lastSegment());
 			value = resource.getURI().lastSegment();
 		}
 		text.setLayoutData(gd);
 		gd.minimumWidth = 100;
 		gd.grabExcessHorizontalSpace = true;
 		
 		final Button okayButton = new Button(shell, SWT.NONE);
 		gd = new GridData();
 		okayButton.setText(Messages.getString("Rename.dialog.okay"));
 		okayButton.setLayoutData(gd);
 		okayButton.setEnabled(false);
 		
 		Button cancelButton = new Button(shell, SWT.NONE);
 		gd = new GridData();
 		cancelButton.setText(Messages.getString("Rename.dialog.cancel"));
 		cancelButton.setLayoutData(gd);
 		
 		shell.pack();
 		shell.open();
 		
 		
 		text.addModifyListener(new ModifyListener() {
 			
 			public void modifyText(ModifyEvent arg0) {
 				URI oldURI = resource.getURI();
 				String path = oldURI.path();
				int ind  = path.lastIndexOf("/");
				String truncpath = "";
				if(ind != -1) truncpath = path.substring(0, ind+1);
 				
 				String newpath = truncpath+text.getText();
 				File f = new File(newpath);
 				if(f.exists() || text.getText().trim().equals("") || text.getText().equals(resource.getURI().lastSegment())) {
 					okayButton.setEnabled(false);
 				} else {
 					okayButton.setEnabled(true);
 					value = text.getText();
 				}
 			}
 		});
 		
 		okayButton.addSelectionListener( new SelectionListener() {
 			
 			public void widgetSelected(SelectionEvent arg0) {
 				cancelPressed=false;
 				shell.dispose();
 			}
 			
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		cancelButton.addSelectionListener( new SelectionListener() {
 			
 			public void widgetSelected(SelectionEvent arg0) {
 				cancelPressed=true;
 				shell.dispose();
 			}
 			
 			public void widgetDefaultSelected(SelectionEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 		
 		Display display = parent.getDisplay();
 		while (!shell.isDisposed()) {
 		if (!display.readAndDispatch()) display.sleep();
 		}
 		
 		
 		if(!cancelPressed) {
 			String res = value;
 			return res;
 		}
 		return null;
 	}
 	
 	public static void main(String [] args) {
 		Display display = new Display();
 	    Shell shell = new Shell(display);
 	    
 //	    String [] rn = {"AGE0-10", "AGE11-35","AGE36-100"};
 //		String [] cn = {"AGE0-10", "AGE11-35","AGE36-100"};
 		
 //		MatrixEditorDialog dialog = new MatrixEditorDialog(shell, SWT.PUSH, "Enter Values", (short)3, (short)3, rn, cn, new MatrixEditorValidator() {
 
 		
 		RenameDialog dialog  = new RenameDialog(shell, SWT.PUSH, null, "Enter stuff");
 			 
 	    dialog.open();
 	    while (!shell.isDisposed()) {
 	      if (!display.readAndDispatch()) {
 	        display.sleep();
 	      }
 	    }
 	    display.dispose();
 	}
 	
 }
