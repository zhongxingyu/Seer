 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.parser.comments;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.vjet.dsf.jst.meta.JsTypingMeta;
 import org.eclipse.vjet.dsf.jst.meta.Token;
 
 public class JsFuncType extends JsTypingMeta {
 	
 	private JsTypingMeta m_returnType;
 	private String m_funcName;
 	private List<JsParam> m_params = new ArrayList<JsParam>();
 	private boolean m_typeFactoryEnabled = false;
 	private boolean m_funcArgMetaExtensionEnabled = false;
 	
 	public JsFuncType(JsTypingMeta returnType) {
 		m_returnType = returnType;
 		if (returnType != null) {
 			setTypingToken(returnType.getTypingToken());
 		}
 	}
 		
 	public JsTypingMeta getReturnType() {
 		return m_returnType;
 	}
 	
 	public JsFuncType setFuncName(String name, Token nameToken) {
 		m_funcName = name;
 		if (nameToken != null) {
 			setTypingToken(nameToken);
 		}
 		return this;
 	}
 	
 	public String getFuncName() {
 		return m_funcName;
 	}
 	
 	public List<JsParam> getParams() {
 		return m_params;
 	}
 	
 	public JsFuncType addParam(JsParam param) {
 		m_params.add(param);
 		return this;
 	}
 	
 	public boolean isTypeFactoryEnabled() {
 		return m_typeFactoryEnabled;
 	}
 	
 	public JsFuncType setTypeFactoryEnabled(boolean set) {
 		m_typeFactoryEnabled = set;
 		return this;
 	}
 	
 	public boolean isFuncArgMetaExtensionEnabled() {
 		return m_funcArgMetaExtensionEnabled;
 	}
 	
 	public JsFuncType setFuncArgMetaExtensionEnabled(boolean set) {
 		m_funcArgMetaExtensionEnabled = set;
 		return this;
 	}
 
 	void addParam(String name, JsTypingMeta typing, boolean isFinal,
 		boolean isOptional, boolean isVariable) throws ParseException {
 
 		JsParam param = new JsParam(name);
 		if (typing instanceof JsVariantType) {
 			for (JsTypingMeta t : ((JsVariantType)typing).getTypes()) {
 				update(param, t, isFinal, isOptional, isVariable);
 			}
 		} else {
 			update(param, typing, isFinal, isOptional, isVariable);
 		}
 
 
 		// add semantic validation
 		int size = m_params.size();
 		if (size != 0) {
 			JsParam prevParam = m_params.get(size - 1);
 			if (prevParam.isVariable()) {
 				throw new ParseException(
 						"Only the last argument in your parameter list can be variable.");
 			}
			if (prevParam.isOptional() && !(isOptional || isVariable)) {
				throw new ParseException(
						"optional param can't be followed by regular param");
			}
 		}
 
 		param.setVariable(isVariable);
 		param.setOptional(isOptional);
 		param.setFinal(isFinal);
 
 		m_params.add(param);
 	}
 	
 	void update(JsParam param, JsTypingMeta t,
 		boolean isFinal, boolean isOptional, boolean isVariable) {
 		t.setFinal(isFinal);
 		t.setOptional(isOptional);
 		t.setVariable(isVariable);
 		param.addTyping(t);
 	}
 
 	@Override
 	public String getType() {
 		return m_returnType == null? "void" : m_returnType.getType();
 	}
 	
 	
 }
