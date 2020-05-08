 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.sample.snippets;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.UpdateValueStrategy;
 import org.eclipse.core.databinding.beans.BeansObservables;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
import org.eclipse.riena.beans.common.DoubleBean;
 import org.eclipse.riena.ui.ridgets.IDecimalTextRidget;
 import org.eclipse.riena.ui.ridgets.ITextRidget;
 import org.eclipse.riena.ui.ridgets.swt.SwtRidgetFactory;
 import org.eclipse.riena.ui.swt.lnf.LnfKeyConstants;
 import org.eclipse.riena.ui.swt.lnf.LnfManager;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Demonstrates how to use a {@link IDecimalTextRidget}.
  */
 public final class SnippetDecimalTextRidget001 {
 
 	public static void main(final String[] args) {
 		final Display display = Display.getDefault();
 		try {
 			final Shell shell = new Shell();
 			shell.setBackground(LnfManager.getLnf().getColor(LnfKeyConstants.SUB_MODULE_BACKGROUND));
 			GridLayoutFactory.fillDefaults().numColumns(2).margins(10, 10).spacing(20, 10).applyTo(shell);
 
 			UIControlsFactory.createLabel(shell, "###,###,###.0000:"); //$NON-NLS-1$
 			final Text txtInput = UIControlsFactory.createTextDecimal(shell);
 			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtInput);
 
 			UIControlsFactory.createLabel(shell, "Output:"); //$NON-NLS-1$
 			final Text txtOutput = UIControlsFactory.createText(shell);
 			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtOutput);
 
 			final IDecimalTextRidget rInput = (IDecimalTextRidget) SwtRidgetFactory.createRidget(txtInput);
 			rInput.setMaxLength(9);
 			rInput.setPrecision(4);
 			rInput.setDirectWriting(true);
 
 			final ITextRidget rOutput = (ITextRidget) SwtRidgetFactory.createRidget(txtOutput);
 			rOutput.setOutputOnly(true);
 
 			final DataBindingContext dbc = new DataBindingContext();
 			dbc.bindValue(BeansObservables.observeValue(rOutput, ITextRidget.PROPERTY_TEXT), BeansObservables
 					.observeValue(rInput, ITextRidget.PROPERTY_TEXT), new UpdateValueStrategy(
 					UpdateValueStrategy.POLICY_NEVER), new UpdateValueStrategy(UpdateValueStrategy.POLICY_UPDATE));
			rInput.bindToModel(new DoubleBean(1234.56), DoubleBean.PROP_VALUE);
 			rInput.updateFromModel();
 
 			shell.setSize(300, 200);
 			shell.open();
 			while (!shell.isDisposed()) {
 				if (!display.readAndDispatch()) {
 					display.sleep();
 				}
 			}
 		} finally {
 			display.dispose();
 		}
 	}
 
 }
