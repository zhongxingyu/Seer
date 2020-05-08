 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.python.internal.core.codeassist;
 
 import org.eclipse.dltk.ast.ASTNode;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 
 public class PythonSelectionParser extends PythonAssistParser {
 	private final boolean VERBOSE = true;
 	public void handleNotInElement(ASTNode node, int position) {
 	}
 
 	public void parseBlockStatements(ASTNode node, ASTNode inNode, int position) {
 		if( VERBOSE ) {
 			System.out.println(node);
 		}
 	}

	public ModuleDeclaration getModule() {
		return module; 
	}
 }
