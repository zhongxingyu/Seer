 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.core.tests.model;
 
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.compiler.ISourceElementRequestor;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 
 public class TestSourceElementParser implements ISourceElementParser {
 
 	private ISourceElementRequestor requestor;
 
	TestSourceElementParser() {
 
 	}
 
 //	TestSourceElementParser(ISorceElementRequestor requestor) {
 //		this.requestor = requestor;
 //	}
 
 	public ModuleDeclaration parseSourceModule(char[] contents,
 			ISourceModuleInfo astCashe) {
 		requestor.enterModule();
 		ISourceElementRequestor.TypeInfo ti = new ISourceElementRequestor.TypeInfo();
 		ti.name = "Class1";
 		requestor.enterType(ti);
 		ISourceElementRequestor.MethodInfo mi = new ISourceElementRequestor.MethodInfo();
 		mi.name = "Method1";
 		requestor.enterMethod(mi);
 		requestor.exitMethod(10);
 		requestor.exitType(10);
 		mi.name = "Procedure1";
 		requestor.enterMethod(mi);
 		requestor.exitMethod(11);
 		requestor.exitModule(20);
 		return null;
 	}
 
 	public void setRequestor(ISourceElementRequestor requestor) {
 		this.requestor = requestor;
 	}
 
 	public void setReporter(IProblemReporter reporter) {
		// TODO Auto-generated method stub

 	}
 }
