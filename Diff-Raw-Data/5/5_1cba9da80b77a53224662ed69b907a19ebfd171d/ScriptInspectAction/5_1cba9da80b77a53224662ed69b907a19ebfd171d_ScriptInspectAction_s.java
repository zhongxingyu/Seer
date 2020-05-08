 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.internal.debug.ui.actions;
 
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.debug.core.eval.IScriptEvaluationResult;
import org.eclipse.dltk.debug.core.eval.InsepctEvaluatedScriptExpression;
 import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * Places the result of an evaluation in the debug expression view.
  */
 public class ScriptInspectAction extends ScriptEvaluationAction {
 	protected void displayResult(final IScriptEvaluationResult result) {
 		final Display display = DLTKDebugUIPlugin.getStandardDisplay();
 		display.asyncExec(new Runnable() {
 			public void run() {
 				if (!display.isDisposed()) {
 					showExpressionView();
					InsepctEvaluatedScriptExpression expression = new InsepctEvaluatedScriptExpression(
 							result);
 					DebugPlugin.getDefault().getExpressionManager()
 							.addExpression(expression);
 				}
 				evaluationCleanup();
 			}
 		});
 	}
 }
