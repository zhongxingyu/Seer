 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.core.model;
 
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IValue;
 import org.eclipse.debug.core.model.IVariable;
 import org.eclipse.dltk.dbgp.IDbgpProperty;
 import org.eclipse.dltk.dbgp.commands.IDbgpCoreCommands;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.debug.core.model.IRefreshableScriptVariable;
 import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 
 public class ScriptVariable extends ScriptDebugElement implements
 		IScriptVariable, IRefreshableScriptVariable {
 	private final IScriptStackFrame frame;
 	private final String name;
 	private IDbgpProperty property;
 	private IValue value;
 	private boolean isValueChanged = false;
 
 	public ScriptVariable(IScriptStackFrame frame, String name,
 			IDbgpProperty property) {
 		this.frame = frame;
 		this.name = name;
 		this.property = property;
 	}
 
 	public IDebugTarget getDebugTarget() {
 		return frame.getDebugTarget();
 	}
 
 	public synchronized IValue getValue() throws DebugException {
 		if (value == null) {
 			value = ScriptValue.createValue(frame, property);
 		}
 		return value;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public String getReferenceTypeName() throws DebugException {
 		return property.getType();
 	}
 
 	public boolean hasValueChanged() throws DebugException {
 		return isValueChanged;
 	}
 
 	public synchronized void setValue(String expression) throws DebugException {
 		try {
 			if (("String".equals(property.getType())) && //$NON-NLS-1$
 					(!expression.startsWith("'") || !expression.endsWith("'")) && //$NON-NLS-1$ //$NON-NLS-2$
 					(!expression.startsWith("\"") || !expression.endsWith("\""))) //$NON-NLS-1$ //$NON-NLS-2$
 				expression = "\"" + expression.replaceAll("\\\"", "\\\\\"") + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
 			if (getCoreCommands().setProperty(property.getEvalName(),
 					frame.getLevel(), expression)) {
 				clearEvaluationManagerCache();
 				update();
 			}
 		} catch (DbgpException e) {
 			// TODO: localize
 			throw wrapDbgpException(Messages.ScriptVariable_cantAssignVariable,
 					e);
 		}
 	}
 
 	private IDbgpCoreCommands getCoreCommands() {
 		return ((IScriptThread) frame.getThread()).getDbgpSession()
 				.getCoreCommands();
 	}
 
 	private void clearEvaluationManagerCache() {
 		ScriptThread thread = (ScriptThread) frame.getThread();
 		thread.notifyModified();
 
 	}
 
 	private void update() throws DbgpException {
 		this.value = null;
 
 		// String key = property.getKey();
 		String name = property.getEvalName();
 
 		// TODO: Use key if provided
 		this.property = getCoreCommands().getProperty(name, frame.getLevel());
 
 		DebugEventHelper.fireChangeEvent(this);
 	}
 
 	public void setValue(IValue value) throws DebugException {
 		setValue(value.getValueString());
 	}
 
 	public boolean supportsValueModification() {
 		return !property.isConstant();
 	}
 
 	public boolean verifyValue(String expression) throws DebugException {
 		// TODO: perform more smart verification
 		return expression != null;
 	}
 
 	public boolean verifyValue(IValue value) throws DebugException {
 		return verifyValue(value.getValueString());
 	}
 
 	public boolean isConstant() {
 		return property.isConstant();
 	}
 
 	public String toString() {
 		return getName();
 	}
 
 	public String getId() {
 		return property.getKey();
 	}
 
 	/**
 	 * @param newVariable
 	 * @return
 	 * @throws DebugException
 	 */
 	public IVariable refreshVariable(IVariable newVariable)
 			throws DebugException {
 		if (newVariable instanceof ScriptVariable) {
 			final ScriptVariable v = (ScriptVariable) newVariable;
 			isValueChanged = !equals(property, v.property);
 			if (!isValueChanged) {
				if (property.getChildrenCount() != 0
						&& v.property.getChildrenCount() != 0) {
 					ScriptStackFrame.refreshVariables(v.getValue()
 							.getVariables(), getValue().getVariables());
 				}
 			}
 			value = v.value;
 			property = v.property;
 			return this;
 		} else {
 			return newVariable;
 		}
 	}
 
 	private static boolean equals(IDbgpProperty p1, IDbgpProperty p2) {
 		if (StrUtils.equals(p1.getType(), p2.getType())) {
 			if (!StrUtils.equals(p1.getValue(), p2.getValue())) {
 				return false;
 			}
 		}
 		if (StrUtils.isNotEmpty(p1.getKey())
 				&& StrUtils.isNotEmpty(p2.getKey())) {
 			return p1.getKey().equals(p2.getKey());
 		}
 		return true;
 	}
 }
