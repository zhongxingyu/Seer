 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui.breakpoints;
 
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.debug.core.model.IScriptBreakpoint;
import org.eclipse.dltk.debug.ui.DLTKDebugUIPlugin;
 import org.eclipse.dltk.internal.ui.editor.ScriptSourceViewer;
 import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
 import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
 import org.eclipse.dltk.ui.util.SWTFactory;
 import org.eclipse.jface.text.Document;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.dialogs.PropertyPage;
 
 public class ScriptBreakpointPropertyPage extends PropertyPage {
 
 	private static final int UPDATE_INITIAL = 0;
 	private static final int UPDATE_EXPRESSION_ENABLE = 1;
 	private static final int UPDATE_OTHER = 2;
 
 	// Enabled
 	private Button enabledBreakpointButton;
 
 	// Hit count checking
 	private Button hitCountCheckingButton;
 	private Combo hitConditionCombo;
 	private Text hitValueText;
 
 	// Expression
 	private ScriptSourceViewer expressionViewer;
 	private Button enableExpressionButton;
 
 	// Simple access methods
 	protected boolean getBreakointEnableState() {
 		return enabledBreakpointButton.getSelection();
 	}
 
 	protected boolean getEnabledHitChecking() {
 		return hitCountCheckingButton.getSelection();
 	}
 
 	protected void setEnabledHitChecking(boolean state) {
 		hitCountCheckingButton.setSelection(state);
 	}
 
 	// Hit value & condition
 	protected void setHitCondition(int condition) {
 		hitConditionCombo.select(condition);
 	}
 
 	protected int getHitCondition() {
 		return getEnabledHitChecking() ? hitConditionCombo.getSelectionIndex()
 				: -1;
 	}
 
 	protected void setHitValue(int value) {
 		hitValueText.setText(Integer.toString(value));
 	}
 
 	protected int getHitValue() {
 		return getEnabledHitChecking() ? Integer.parseInt(hitValueText
 				.getText()) : -1;
 	}
 
 	// Expression
 	protected void setExpression(String expression) {
 		expressionViewer.getDocument().set(expression);
 	}
 
 	protected String getExpression() {
 		return expressionViewer.getDocument().get();
 	}
 
 	protected void setExpressionState(boolean state) {
 		enableExpressionButton.setSelection(state);
 	}
 
 	protected boolean getExpressionState() {
 		return enableExpressionButton.getSelection();
 	}
 
 	// Static breakpoint information
 	protected void createLabels(Composite parent) throws CoreException {
 		IScriptBreakpoint breakpoint = getBreakpoint();
 
 		Composite labelComposite = SWTFactory.createComposite(parent, parent
 				.getFont(), 2, 1, GridData.FILL_HORIZONTAL);
 
 		// Script language
 		SWTFactory.createLabel(labelComposite,
 				BreakpointMessages.LanguageLabel, 1);
 		SWTFactory.createLabel(labelComposite, BreakpointUtils
 				.getLanguageToolkit(breakpoint).getLanguageName(), 1);
 
 		createLocationLabels(labelComposite);
 
 		// Id
 		SWTFactory.createLabel(labelComposite,
 				BreakpointMessages.InternalIdLabel, 1);
 
 		String engineId = breakpoint.getIdentifier();
 		String engineIdText = (engineId == null || engineId.length() == 0) ? BreakpointMessages.InternalIdNotAvailableMessage
 				: engineId;
 		SWTFactory.createLabel(labelComposite, engineIdText, 1);
 
 		// Hit count
 		SWTFactory.createLabel(labelComposite,
 				BreakpointMessages.HitCountLabel, 1);
 
 		int hitCount = breakpoint.getHitCount();
 		String hitCountText = hitCount == -1 ? BreakpointMessages.HitCountNotAvailableMessage
 				: Integer.toString(hitCount);
 		SWTFactory.createLabel(labelComposite, hitCountText, 1);
 
 		// from debugging engine
 
 		createTypeSpecificLabels(labelComposite);
 	}
 
 	protected void createLocationLabels(Composite parent) throws CoreException {
 		// Resource name
 		String resourceName = getBreakpointResourceName();
 		if (resourceName != null && resourceName.length() > 0) {
 			SWTFactory.createLabel(parent, getBreakpointLocationLabel(), 1);
 			SWTFactory.createLabel(parent, resourceName, 1);
 		}
 	}
 
 	/**
 	 * Returns the label text for the location field
 	 * 
 	 * @return
 	 */
 	protected String getBreakpointLocationLabel() {
 		return BreakpointMessages.FileLabel;
 	}
 
 	/**
 	 * Returns the value for the location field
 	 * 
 	 * @return
 	 * @throws CoreException
 	 */
 	protected String getBreakpointResourceName() throws CoreException {
 		return getBreakpoint().getResourceName();
 	}
 
 	protected void createTypeSpecificLabels(Composite parent)
 			throws CoreException {
 		// Nothing to do here
 	}
 
 	// Breakpoint information
 	protected void createButtons(Composite parent) throws CoreException {
 		Composite buttonsComposite = SWTFactory.createComposite(parent, parent
 				.getFont(), 1, 1, GridData.FILL_HORIZONTAL);
 
 		enabledBreakpointButton = SWTFactory.createCheckButton(
 				buttonsComposite, BreakpointMessages.EnabledLabel, null, false,
 				1);
 
 		createTypeSpecificButtons(buttonsComposite);
 	}
 
 	protected void createTypeSpecificButtons(Composite parent) {
 
 	}
 
 	protected void createHitCountEditor(Composite parent) {
 		Composite hitCountComposite = SWTFactory.createComposite(parent, parent
 				.getFont(), 4, 1, GridData.FILL_HORIZONTAL);
 
 		// Hit count checking
 		hitCountCheckingButton = SWTFactory.createCheckButton(
 				hitCountComposite, BreakpointMessages.BreakWhenHitCountLabel,
 				null, false, 1);
 
 		hitCountCheckingButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updateControlsState(UPDATE_OTHER);
 			}
 		});
 
 		hitConditionCombo = new Combo(hitCountComposite, SWT.READ_ONLY);
 
 		// Hit condition
 		hitConditionCombo.add(BreakpointMessages.HitConditionGreaterOrEqual,
 				IScriptBreakpoint.HIT_CONDITION_GREATER_OR_EQUAL);
 
 		hitConditionCombo.add(BreakpointMessages.HitConditionEqual,
 				IScriptBreakpoint.HIT_CONDITION_EQUAL);
 
 		hitConditionCombo.add(BreakpointMessages.HitConditionMultiple,
 				IScriptBreakpoint.HIT_CONDITION_MULTIPLE);
 
 		hitConditionCombo
 				.select(IScriptBreakpoint.HIT_CONDITION_GREATER_OR_EQUAL);
 
 		hitConditionCombo.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updateControlsState(UPDATE_OTHER);
 			}
 		});
 
 		hitConditionCombo.setData(new GridData());
 
 		// Hit value
 		hitValueText = new Text(hitCountComposite, SWT.BORDER);
 		hitValueText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true,
 				false));
 
 		hitValueText.addKeyListener(new KeyAdapter() {
 			public void keyReleased(KeyEvent e) {
 				updateControlsState(UPDATE_OTHER);
 			}
 		});
 
 		SWTFactory.createLabel(hitCountComposite, BreakpointMessages.HitsLabel,
 				1);
 	}
 
 	protected void createExpressionEditor(Composite parent) {
 		Group group = new Group(parent, SWT.NONE);
 		group.setLayout(new GridLayout(1, false));
 		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
 
 		enableExpressionButton = new Button(group, SWT.CHECK);
 		enableExpressionButton.setText(BreakpointMessages.UseConditionLabel);
 		enableExpressionButton.addSelectionListener(new SelectionAdapter() {
 			public void widgetSelected(SelectionEvent e) {
 				updateControlsState(UPDATE_EXPRESSION_ENABLE);
 			}
 		});
 
 		IDLTKUILanguageToolkit toolkit = BreakpointUtils
 				.getUILanguageToolkit(getBreakpoint());
 
 		expressionViewer = new ScriptSourceViewer(group, null, null, false,
 				SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL, toolkit
 						.getPreferenceStore());
 
 		IDocument document = new Document();
 
 		toolkit.getTextTools().setupDocumentPartitioner(document,
 				toolkit.getPartitioningId());
 
 		ScriptSourceViewerConfiguration config = toolkit
 				.createSourceViewerConfiguration();
 
 		expressionViewer.configure(config);
 		expressionViewer.setDocument(document);
 
 		expressionViewer.getControl().setLayoutData(
 				new GridData(SWT.FILL, SWT.FILL, true, true));
 
 	}
 
 	protected IScriptBreakpoint getBreakpoint() {
 		return (IScriptBreakpoint) getElement();
 	}
 
 	protected Control createContents(Composite parent) {
 		noDefaultAndApplyButton();
 		Composite composite = SWTFactory.createComposite(parent, parent
 				.getFont(), 1, 1, GridData.FILL_BOTH);
 
 		try {
 			createLabels(composite);
 			createButtons(composite);
 
 			if (hasHitCountEditor()) {
 				createHitCountEditor(composite);
 			}
 
 			if (hasExpressionEditor()) {
 				createExpressionEditor(composite);
 			}
 
 			loadValues();
 			updateControlsState(UPDATE_INITIAL);
 		} catch (CoreException e) {
			DLTKDebugUIPlugin.log(e);
 		}
 
 		return composite;
 	}
 
 	protected boolean hasHitCountEditor() {
 		return true;
 	}
 
 	protected boolean hasExpressionEditor() {
 		return true;
 	}
 
 	protected void loadValues() throws CoreException {
 		IScriptBreakpoint breakpoint = getBreakpoint();
 
 		// Enabled
 		enabledBreakpointButton.setSelection(breakpoint.isEnabled());
 
 		// Hit conditions
 		if (hasHitCountEditor()) {
 
 			final int hitValue = breakpoint.getHitValue();
 			if (hitValue != -1) {
 				setHitValue(hitValue);
 				setHitCondition(breakpoint.getHitCondition());
 				setEnabledHitChecking(true);
 			} else {
 				setEnabledHitChecking(false);
 			}
 		}
 
 		// Expression
 		if (hasExpressionEditor()) {
 			setExpressionState(breakpoint.getExpressionState());
 			setExpression(breakpoint.getExpression());
 		}
 	}
 
 	protected void saveValues() throws CoreException {
 		IScriptBreakpoint breakpoint = getBreakpoint();
 
 		breakpoint.setEnabled(getBreakointEnableState());
 
 		if (hasHitCountEditor()) {
 			breakpoint.setHitValue(getHitValue());
 			breakpoint.setHitCondition(getHitCondition());
 		}
 
 		if (hasExpressionEditor()) {
 			breakpoint.setExpression(getExpression());
 			breakpoint.setExpressionState(getExpressionState());
 		}
 	}
 
 	protected void updateControlsState(int mode) {
 		// Hit count
 		if (hasHitCountEditor()) {
 			boolean hitChecking = hitCountCheckingButton.getSelection();
 			hitConditionCombo.setEnabled(hitChecking);
 			hitValueText.setEnabled(hitChecking);
 		}
 
 		// Expression
 		if (hasExpressionEditor()) {
 			boolean expressionEnabled = enableExpressionButton.getSelection();
 			Control control = expressionViewer.getControl();
 			control.setEnabled(expressionEnabled);
 			if (expressionEnabled) {
 				expressionViewer.initializeViewerColors();
 				if (mode == UPDATE_EXPRESSION_ENABLE) {
 					expressionViewer.getTextWidget().setFocus();
 				}
 			} else {
 				Color color = expressionViewer.getControl().getDisplay()
 						.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
 				expressionViewer.getTextWidget().setBackground(color);
 			}
 		}
 
 		validateValues();
 	}
 
 	protected void validateValues() {
 		boolean valid = true;
 		String errorMessage = null;
 
 		if (hasHitCountEditor()) {
 			if (getEnabledHitChecking()) {
 				try {
 					getHitValue();
 				} catch (NumberFormatException e) {
 					valid = false;
 					errorMessage = BreakpointMessages.InvalidNumberOfHits;
 				}
 			}
 		}
 
 		setValid(valid);
 		setErrorMessage(errorMessage);
 	}
 
 	public boolean performOk() {
 		try {
 			ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {
 				public void run(IProgressMonitor monitor) throws CoreException {
 					saveValues();
 					DebugPlugin.getDefault().getBreakpointManager()
 							.fireBreakpointChanged(getBreakpoint());
 				}
 			}, null, 0, null);
 		} catch (CoreException e) {
 			DebugPlugin.log(e);
 		}
 
 		return super.performOk();
 	}
 }
