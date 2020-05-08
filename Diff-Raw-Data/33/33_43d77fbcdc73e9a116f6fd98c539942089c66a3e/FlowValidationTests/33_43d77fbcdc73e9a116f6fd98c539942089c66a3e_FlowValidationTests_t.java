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
 package org.eclipse.dltk.javascript.core.tests.validation;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.compiler.problem.IProblemIdentifier;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.internal.javascript.validation.FlowValidation;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.core.tests.AbstractValidationTest;
 
 @SuppressWarnings("restriction")
 public class FlowValidationTests extends AbstractValidationTest {
 
 	@Override
 	protected IBuildParticipant createValidator() {
 		return new FlowValidation();
 	}
 
 	public void testUnreachableCodeAfterReturn() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else return 0");
 		code.add("  var x = 1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.contains(JavaScriptProblems.UNREACHABLE_CODE));
 	}
 
 	public void testUnreachableCodeAfterBreak() {
 		StringList code = new StringList();
 		code.add("function x() {");
 		code.add("  for (;;) {");
 		code.add("    var q = 0");
 		code.add("    break;");
 		code.add("    ++q");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.contains(JavaScriptProblems.UNREACHABLE_CODE));
 	}
 
 	public void testCodeAfterBreak() {
 		StringList code = new StringList();
 		code.add("function x() {");
 		code.add("  for (;;) {");
 		code.add("    var q = 0");
 		code.add("    break;");
 		code.add("  }");
 		code.add("  return true;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testUnreachableCodeAfterContinue() {
 		StringList code = new StringList();
 		code.add("function x() {");
 		code.add("  for (;;) {");
 		code.add("    var q = 0");
 		code.add("    continue;");
 		code.add("    ++q");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds.contains(JavaScriptProblems.UNREACHABLE_CODE));
 	}
 
 	public void testFunctionAlwaysReturnValue1() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE));
 	}
 
 	public void testFunctionAlwaysReturnValue2() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  return");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(2, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE));
 		assertTrue(problemIds.contains(JavaScriptProblems.RETURN_INCONSISTENT));
 	}
 
 	public void testFunctionWithIfThenNotAllReturningValuesWithReturn() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else a = 1");
 		code.add("  return 1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testFunctionWithIfThenNotAllReturningValues() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else if (a == 2) a = 1");
 		code.add("  else if (a == 3) a = 1");
 		code.add("  else return 2");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(1, problemIds.size());
 		assertTrue(problemIds
 				.contains(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE));
 	}
 
 	public void testFunctionWithIfThenAllReturningValues() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  if (a ==1) return 1");
 		code.add("  else if (a == 2) return 2");
 		code.add("  else if (a == 3) return 3");
 		code.add("  else return -1");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testFunctionWithThrowsStatement() {
 
 		StringList code = new StringList();
 		code.add("/**");
 		code.add("* @return {String} some result info");
 		code.add("* @throws {String} some error info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("var result_1 = '';");
 		code.add("var condition_1 = false;");
 		code.add("if(condition_1) {");
 		code.add("return result_1;}");
 		code.add("throw 'Some error message';");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testFunctionReturnInFinally() {
 		StringList code = new StringList();
 		code.add("function q(a) {");
 		code.add("  try {");
 		code.add("    if (a ==1) return 1");
 		code.add("  } finally {");
 		code.add("    return 0;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(0, problemIds.size());
 	}
 
 	public void testThrowCatch() {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = 0");
 		code.add("  try {");
 		code.add("    throw new Error('aaa')");
 		code.add("  } catch (e) {");
 		code.add("    x++");
 		code.add("  }");
 		code.add("  return x");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 0, problemIds.size());
 	}
 
 	public void testReturnCatch() {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = 0");
 		code.add("  try {");
 		code.add("    return 0");
 		code.add("  } catch (e) {");
 		code.add("    x++");
 		code.add("  }");
 		code.add("  return x");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 0, problemIds.size());
 	}
 
 	public void testReturnCatchReturn() {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = 0");
 		code.add("  try {");
 		code.add("    return 0");
 		code.add("  } catch (e) {");
 		code.add("    return 1");
 		code.add("  }");
 		code.add("  return x");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertEquals(JavaScriptProblems.UNREACHABLE_CODE, problemIds.iterator()
 				.next());
 	}
 
 	public void testSwitchCaseWithDefaultReturn() throws Exception {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = new Array()");
 		code.add("  switch(x.length) {");
 		code.add("  case 1: return 'test1';break;");
 		code.add("  case 2: return 'test2';break;");
 		code.add("  default: return 'test3';break;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 0, problemIds.size());
 	}
 
 	public void testSwitchCaseWithDefaultReturnAndOptionalReturn()
 			throws Exception {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = new Array()");
 		code.add("  switch(x.length) {");
 		code.add("  case 1: if (x.length> 0) return 'test1';break;");
 		code.add("  case 2: return 'test2';break;");
 		code.add("  default: return 'test3';break;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertEquals(
 				Collections
 						.singleton(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE),
 				problemIds);
 	}
 
 	public void testSwitchCase() throws Exception {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = new Array()");
 		code.add("  switch(x.length) {");
 		code.add("  case 1: return 'test1';break;");
 		code.add("  case 2: return 'test2';break;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertEquals(
 				Collections
 						.singleton(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE),
 				problemIds);
 	}
 
 	public void testSwitchCaseWithDefaultReturnAndNoAllCaseReturn()
 			throws Exception {
 		StringList code = new StringList();
 		code.add("function q() {");
 		code.add("  var x = new Array()");
 		code.add("  switch(x.length) {");
 		code.add("  case 1: return 'test1';break;");
 		code.add("  case 2: break;");
 		code.add("  default: return 'test3';break;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 1, problemIds.size());
 		assertEquals(
 				Collections
 						.singleton(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE),
 				problemIds);
 	}
 
 	public void testSwitchCaseWithEmptyDefault() {
 		StringList code = new StringList();
 		code.add("function a() {");
 		code.add("var x = new Array()");
 		code.add("  switch (x.length) {");
 		code.add("  case 0:");
 		code.add("    return 0;");
 		code.add("  default:");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(
 				Collections
 						.singleton(JavaScriptProblems.FUNCTION_NOT_ALWAYS_RETURN_VALUE),
 				problemIds);
 	}
 
 	public void testSwitchCaseFallThrough() {
 		StringList code = new StringList();
 		code.add("function a() {");
 		code.add("var x = new Array()");
 		code.add("  switch (x.length) {");
 		code.add("  case 0:");
 		code.add("  case 1:");
 		code.add("    return 0;");
 		code.add("  default:");
 		code.add("    return -1;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 0, problemIds.size());
 	}
 
 	public void testUnreachableCodeInSwitchCase() {
 		StringList code = new StringList();
 		code.add("function a() {");
 		code.add("var x = new Array()");
 		code.add("  switch (x.length) {");
 		code.add("  case 1:");
 		code.add("    return 0;");
 		code.add("    return 1;");
 		code.add("  default:");
 		code.add("    return -1;");
 		code.add("  }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(
 				Collections.singleton(JavaScriptProblems.UNREACHABLE_CODE),
 				problemIds);
 	}
 
 	public void testIfAndSwitchWithNoReturns() throws Exception {
 		StringList code = new StringList();
 		code.add("function criteria_tree_node_text() {");
 		code.add("var is_group = 1;");
 		code.add("var filter_operator = '=';");
 		code.add("if (is_group == 1)");
 		code.add(" return '<html><body></body></html>';");
 		code.add("else {");
 		code.add(" var _retText = '<html><body><b></b> ';");
 		code.add(" switch (filter_operator) {");
 		code.add("  case '=': _retText += 'equals';break;");
 		code.add("  case '<': _retText += 'is less than';break;");
 		code.add("  case '<=': _retText += 'is less than equals';break;");
 		code.add("  case '>': _retText += 'is greater than';break;");
 		code.add("  case '>=': _retText += 'is greater than equals';break;");
 		code.add("  case 'CONTAINS': _retText += 'contains';break;");
 		code.add("  }");
 		code.add(" _retText += ' <b>'+ filter_value + '</b></body></html>';");
 		code.add(" return _retText;");
 		code.add(" }");
 		code.add("}");
 		final Set<IProblemIdentifier> problemIds = extractIds(validate(code
 				.toString()));
 		assertEquals(problemIds.toString(), 0, problemIds.size());
 	}
 
 	public void testNestedFunctionDeclaration() {
 		final StringList code = new StringList();
 		code.add("function test() {");
 		code.add("  return 1");
 		code.add("  function nested() {");
 		code.add("    return 1;");
 		code.add("  }");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNestedFunctionDeclaration_nestedError() {
 		final StringList code = new StringList();
 		code.add("function test() {");
 		code.add("  return 1");
 		code.add("  function nested(x) {");
 		code.add("    if (a > 0) { return 1; }");
 		code.add("    else { return -1; }");
 		code.add("    return false;");
 		code.add("  }");
 		code.add("}");
 		final String sCode = code.toString();
 		final List<IProblem> problems = validate(sCode);
 		assertEquals(problems.toString(), 1, problems.size());
 		final IProblem problem = problems.get(0);
 		assertEquals(JavaScriptProblems.UNREACHABLE_CODE, problem.getID());
 		assertEquals(
 				"return false;",
 				sCode.substring(problem.getSourceStart(),
 						problem.getSourceEnd()));
 	}
 
 	public void testNestedFunctionDeclaration_outerError() {
 		final StringList code = new StringList();
 		code.add("function test() {");
 		code.add("  return 1");
 		code.add("  function nested() {");
 		code.add("  }");
 		code.add("  return false;");
 		code.add("}");
 		final String sCode = code.toString();
 		final List<IProblem> problems = validate(sCode);
 		assertEquals(problems.toString(), 1, problems.size());
 		final IProblem problem = problems.get(0);
 		assertEquals(JavaScriptProblems.UNREACHABLE_CODE, problem.getID());
 		assertEquals(
 				"return false;",
 				sCode.substring(problem.getSourceStart(),
 						problem.getSourceEnd()));
 	}
	
	
	public void testDefinePropertyWithGet() {
		final StringList code = new StringList();
		code.add("function testing(args) {");
		code.add("	this.data = args.data");
		code.add("	Object.defineProperty(this,'data', {");
		code.add("		get data() {return args.data}");
		code.add("})");
		code.add("}");
		final String sCode = code.toString();
		final List<IProblem> problems = validate(sCode);
		assertEquals(problems.toString(), 0, problems.size());
	}

 }
