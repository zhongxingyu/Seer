 /*******************************************************************************
  * Copyright (c) 2008 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.snippets.validators;
 
 import java.util.List;
 
 import org.eclipse.wazaabi.engine.edp.EDP;
 import org.eclipse.wazaabi.mm.core.widgets.TextComponent;
 import org.eclipse.wazaabi.mm.edp.handlers.EventHandler;
 
 public class VerySimpleValidator {
 
 	public VerySimpleValidator() {
 		System.out.println("creating " + getClass().getName());
 	}
 
 	public boolean isValid(TextComponent eventDispatcher,
 			EventHandler eventHandler) {
 		Object source = eventDispatcher.get(EDP.VALUE_SOURCE_KEY);
 		Object target = eventDispatcher.get(EDP.VALUE_TARGET_KEY);
 		if (source instanceof List){
 			if(target instanceof List)
 				if(((List<?>) source).size() == ((List<?>) target).size())
 					System.out.println("validator executed: source type ok");
 			return true;
 		}
 		else{
 			eventDispatcher.set("inputError", true);
 			System.out.println("validator executed: source type error. Source is "+ source.getClass());
 			return false;
 		}
 			
 	}
 	
	public String errorMessage() {
 		return "Input error";
 	}
 
 	public void dispose() {
 		System.out.println("disposing " + getClass().getName());
 	}
 }
