 /*******************************************************************************
  * Copyright (c) 2009, 2010 Dejan Spasic
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package org.phpsrc.eclipse.pti.tools.phpmd.model;
 
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.ui.IMemento;
 import org.eclipse.ui.XMLMemento;
 
 public class ViolationMementoManager {
 	private static final String TAG_VIOLATIONS = "Violations"; //$NON-NLS-1$
 	private static final String TAG_VIOLATION = "Violation"; //$NON-NLS-1$
 	private static final String TAG_INFO = "info"; //$NON-NLS-1$
 	private static final String TAG_PACKAGE_NAME = "packageName"; //$NON-NLS-1$
 	private static final String TAG_CLASS_NAME = "className"; //$NON-NLS-1$
 	private static final String TAG_FUNCTION_NAME = "functionName"; //$NON-NLS-1$
 	private static final String TAG_METHOD_NAME = "methodName"; //$NON-NLS-1$
 	private static final String TAG_BEGINLINE = "beginline"; //$NON-NLS-1$
 	private static final String TAG_ENDLINE = "endline"; //$NON-NLS-1$
 	private static final String TAG_PRIORITY = "priority"; //$NON-NLS-1$
 	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
 	private static final String TAG_EXTERNAL_INFO_URL = "externalInfoURL"; //$NON-NLS-1$
 	private static final String TAG_RULE = "rule"; //$NON-NLS-1$
 	private static final String TAG_RULE_SET = "ruleSet"; //$NON-NLS-1$
 
 	public XMLMemento build(IViolation[] violations) {
 		XMLMemento memento = XMLMemento.createWriteRoot(TAG_VIOLATIONS);
 		for (IViolation violation : violations)
 			build(memento, violation);
 		return memento;
 	}
 
 	private void build(XMLMemento memento, IViolation violation) {
 		IMemento child = memento.createChild(TAG_VIOLATION);
 		child.putString(TAG_INFO, violation.getInfo());
 		child.putString(TAG_PACKAGE_NAME, violation.getPackageName());
 		child.putString(TAG_CLASS_NAME, violation.getClassName());
 		child.putString(TAG_FUNCTION_NAME, violation.getFunctionName());
 		child.putString(TAG_METHOD_NAME, violation.getMethodName());
 		child.putInteger(TAG_BEGINLINE, violation.getBeginline());
 		child.putInteger(TAG_ENDLINE, violation.getEndline());
 		child.putInteger(TAG_PRIORITY, violation.getPriority());
 		child.putString(TAG_DESCRIPTION, violation.getDescription());
 		child.putString(TAG_EXTERNAL_INFO_URL, violation.getExternalInfoURL().toString());
 		child.putString(TAG_RULE, violation.getRule());
 		child.putString(TAG_RULE_SET, violation.getRuleSet());
 	}
 
	public IViolation[] parse(XMLMemento memento) {
 		IMemento[] children = memento.getChildren(TAG_VIOLATION);
 		List<IViolation> violations = new ArrayList<IViolation>(children.length);
 		for (IMemento child : children) {
			IViolation v = parse(child);
 			if (null != v)
 				violations.add(v);
 		}
 		return violations.toArray(new IViolation[violations.size()]);
 	}
 
	public IViolation parse(IMemento memento) {
 		String info = memento.getString(TAG_INFO);
 		IViolation violation = ViolationResource.loadViolationByInfo(info);
 
 		if (null == violation)
 			return null;
 
 		Integer value;
 
 		value = memento.getInteger(TAG_BEGINLINE);
 		if (null != value)
 			violation.setBeginline(value.intValue());
 
 		value = memento.getInteger(TAG_ENDLINE);
 		if (null != value)
 			violation.setEndline(value.intValue());
 
 		value = memento.getInteger(TAG_PRIORITY);
 		if (null != value)
 			violation.setPriority(value.intValue());
 
 		try {
 			violation.setExternalInfoURL(memento.getString(TAG_EXTERNAL_INFO_URL));
 		} catch (MalformedURLException e) {
 			// do nothing
 		}
 
 		violation.setPackageName(memento.getString(TAG_PACKAGE_NAME));
 		violation.setClassName(memento.getString(TAG_CLASS_NAME));
 		violation.setFunctionName(memento.getString(TAG_FUNCTION_NAME));
 		violation.setMethodName(memento.getString(TAG_METHOD_NAME));
 		violation.setDescription(memento.getString(TAG_DESCRIPTION));
 		violation.setRule(memento.getString(TAG_RULE));
 		violation.setRuleSet(memento.getString(TAG_RULE_SET));
 
 		return violation;
 	}
 }
