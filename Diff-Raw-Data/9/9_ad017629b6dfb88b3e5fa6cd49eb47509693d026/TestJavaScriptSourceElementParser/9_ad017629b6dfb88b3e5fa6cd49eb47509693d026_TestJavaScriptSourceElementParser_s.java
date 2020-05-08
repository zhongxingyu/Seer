 /*******************************************************************************
  * Copyright (c) 2010 xored software, Inc.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Alex Panchenko)
  *******************************************************************************/
 package org.eclipse.dltk.javascript.core.tests.structure;
 
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.internal.javascript.parser.JavaScriptSourceElementParser2;
import org.eclipse.dltk.internal.javascript.ti.TypeInferencer2;
 import org.eclipse.dltk.javascript.ast.Script;
import org.eclipse.dltk.javascript.core.tests.typeinference.TestTypeInferencer2;
 import org.eclipse.dltk.javascript.parser.JavaScriptParser;
 
 final class TestJavaScriptSourceElementParser extends
 		JavaScriptSourceElementParser2 {
 	@Override
	protected TypeInferencer2 createInferencer() {
		return new TestTypeInferencer2();
	}

	@Override
 	protected Script parse(IModuleSource module) {
 		final JavaScriptParser parser = new JavaScriptParser();
 		parser.setTypeInformationEnabled(true);
 		return parser.parse(module, fReporter);
 	}
 
 }
