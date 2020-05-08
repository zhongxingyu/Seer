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
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.eclipse.dltk.compiler.problem.IProblem;
 import org.eclipse.dltk.core.builder.IBuildParticipant;
 import org.eclipse.dltk.core.tests.TestSupport;
 import org.eclipse.dltk.core.tests.util.StringList;
 import org.eclipse.dltk.internal.javascript.ti.IReferenceAttributes;
 import org.eclipse.dltk.internal.javascript.validation.TypeInfoValidator;
 import org.eclipse.dltk.javascript.core.JavaScriptProblems;
 import org.eclipse.dltk.javascript.core.tests.AbstractValidationTest;
 import org.eclipse.dltk.javascript.typeinference.IValueCollection;
 import org.eclipse.dltk.javascript.typeinference.IValueReference;
 import org.eclipse.dltk.javascript.typeinfo.ITypeNames;
 
 @SuppressWarnings("nls")
 public class TypeInfoValidationTests extends AbstractValidationTest {
 
 	private boolean notYetImplemented() {
 		return TestSupport.notYetImplemented(this);
 	}
 
 	@Override
 	protected IBuildParticipant createValidator() {
 		return new TypeInfoValidator();
 	}
 
 	public void testKnownType() {
 		final List<IProblem> problems = validate("var x:String");
 		assertTrue(problems.isEmpty());
 	}
 
 	public void testUnknownType() {
 		final List<IProblem> problems = validate("var x:LongString");
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 	}
 
 	public void testUnknownJavaScriptFunctionType() {
 		final List<IProblem> problems = validate("var x = new LongString()");
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 	}
 
 	public void testUnknownJavaScriptFunctionCall() {
 		final List<IProblem> problems = validate("var x = longString()");
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testDeprecatedType() {
 		final List<IProblem> problems = validate("var x:ExampleService2");
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_TYPE, problems.get(0)
 				.getID());
 	}
 
 	public void testValidMethodCall() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.execute()");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.isEmpty());
 	}
 
 	public void testUndefinedMethodCall() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.runUndefindMethod()");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_JAVA_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testMethodOverload1() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.run()");
 		code.add("x.run('Hello')");
 		code.add("x.run('Hello','World')");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.isEmpty());
 	}
 
 	public void testMethodOverload2() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.run('Hello','World','!')");
 		final List<IProblem> problems = validate(code.toString());
		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_JAVA_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testMethodCallWrongParamCount() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.execute(1)");
 		final List<IProblem> problems = validate(code.toString());
		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_JAVA_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testDeprecatedMethodCall() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.executeCompatible()");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testDeprecatedMethodCall_TypeInference() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var q = x.execute().service");
 		code.add("q.executeCompatible()");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testPropertyAccess() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var name = x.name");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.isEmpty());
 	}
 
 	public void testDeprecatedPropertyAccess() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var name = x.nameCompatible");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testDeprecatedTopLevelProperty() {
 		StringList code = new StringList();
 		code.add("myExampleForms.service");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_PROPERTY, problems.get(0)
 				.getID());
 		assertTrue(problems.get(0).getMessage().contains("myExampleForms"));
 	}
 
 	public void testFunctionHidesPoperty() {
 		StringList code = new StringList();
 		code.add("function exampleForms(){}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.FUNCTION_HIDES_VARIABLE, problems
 				.get(0).getID());
 		assertTrue(problems.get(0).getMessage().contains("exampleForms"));
 	}
 
 	public void testFunctionDoesntHidesAllowHidePoperty() {
 		StringList code = new StringList();
 		code.add("function exampleFormsHide(){}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testUndefinedPropertyAccess() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var name = x.noname");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_JAVA_PROPERTY, problems
 				.get(0).getID());
 	}
 
 	public void testUndefinedTypePropertyAccess() {
 		StringList code = new StringList();
 		code.add("/** @type PersonClassIsNotDefined */");
 		code.add("var person");
 		code.add("var name = person.name");
 		code.add("var address = person.address");
 		final List<IProblem> problems = new ArrayList<IProblem>(
 				validate(code.toString()));
 		Collections.sort(problems, new Comparator<IProblem>() {
 			public int compare(IProblem o1, IProblem o2) {
 				return o1.getSourceStart() - o2.getSourceStart();
 			}
 		});
 		assertEquals(problems.toString(), 3, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(1)
 				.getID());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(2)
 				.getID());
 	}
 
 	public void testUndefinedPropertyAssignment() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("x.noname = true");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_JAVA_PROPERTY, problems
 				.get(0).getID());
 	}
 
 	public void testUndefinedVariableAssignment() {
 		StringList code = new StringList();
 		code.add("x.noname = true");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDECLARED_VARIABLE, problems.get(0)
 				.getID());
 	}
 
 	public void testPropertyAssignment() {
 		StringList code = new StringList();
 		code.add("var x = { }");
 		code.add("x.noname = true");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testMethodAsPropertyAccess() {
 		StringList code = new StringList();
 		code.add("var x:ExampleService");
 		code.add("var name = x.execute");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testElementResolver() {
 		StringList code = new StringList();
 		code.add("exampleForms.service.name");
 		code.add("exampleForms.service.nameCompatible");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.DEPRECATED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testFunctionCallWithCorrectFunctionObjectArgument() {
 		StringList code = new StringList();
 		code.add("function Node1() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("function Node2() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("function call(arg:Node1){");
 		code.add("}");
 		code.add("function test(){");
 		code.add("call(new Node1());");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testFunctionCallWithInCorrectFunctionObjectArgument() {
 		StringList code = new StringList();
 		code.add("function Node1() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("function Node2() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("function call(arg:Node1){");
 		code.add("}");
 		code.add("function test(){");
 		code.add("call(new Node2());");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 	}
 
 	public void testFunctionCallWithCorrectFunctionObjectArgumentJSDOC() {
 		StringList code = new StringList();
 		code.add("function Node1() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("function Node2() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("/**");
 		code.add(" * @param {Node1} arg");
 		code.add(" */");
 		code.add("function call(arg){");
 		code.add("}");
 		code.add("function test(){");
 		code.add("call(new Node1());");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testFunctionCallWithInCorrectFunctionObjectArgumentJSDOC() {
 		StringList code = new StringList();
 		code.add("function Node1() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("function Node2() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("/**");
 		code.add(" * @param {Node1} arg");
 		code.add(" */");
 		code.add("function call(arg){");
 		code.add("}");
 		code.add("function test(){");
 		code.add("call(new Node2());");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 	}
 
 	public void testLazyJSObject() {
 		StringList code = new StringList();
 		code.add("var x = new function x(){");
 		code.add("this.newNode = function newNode(){");
 		code.add("return new Node()");
 		code.add("}");
 		code.add("function Node() {");
 		code.add("this.a=10;");
 		code.add("}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testReturnType() {
 		StringList code = new StringList();
 		code.add("/**");
 		code.add(" * @return {String}");
 		code.add(" */");
 		code.add("function test() {");
 		code.add("	return '';");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testReturnType2() {
 		StringList code = new StringList();
 		code.add("/**");
 		code.add(" * @param {ExampleService} srv");
 		code.add(" * @return {ExampleService}");
 		code.add(" */");
 		code.add("function test(srv) {");
 		code.add("	return srv;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testReturnWrongType() {
 		StringList code = new StringList();
 		code.add("/**");
 		code.add(" * @return {String}");
 		code.add(" */");
 		code.add("function test() {");
 		code.add("	return 1;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 	}
 
 	public void testLazyReturnTypeWithJSDocParam() {
 		StringList code = new StringList();
 		code.add("/**");
 		code.add(" * @param {Node} node");
 		code.add(" */");
 		code.add("function addChild(node) {");
 		code.add("	return node;");
 		code.add("}");
 		code.add("function TestObject() {");
 		code.add("	var x = addChild(new Node());");
 		code.add("	x.a;");
 		code.add("}");
 		code.add("function Node() {");
 		code.add("	this.a = 10;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testLazyReturnTypeWithTypeInfo() {
 		StringList code = new StringList();
 		code.add("/**");
 		code.add(" * @param node");
 		code.add(" */");
 		code.add("function addChild(node:Node) {");
 		code.add("	return node;");
 		code.add("}");
 		code.add("function TestObject() {");
 		code.add("	var x = addChild(new Node());");
 		code.add("	x.a;");
 		code.add("}");
 		code.add("function Node() {");
 		code.add("	this.a = 10;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testLazyReturnTypeWithNoTypeParamInfo() {
 		StringList code = new StringList();
 		code.add("/**");
 		code.add(" * @param node");
 		code.add(" */");
 		code.add("function addChild(node) {");
 		code.add("	return node;");
 		code.add("}");
 		code.add("function TestObject() {");
 		code.add("	var x = addChild(new Node());");
 		code.add("	var y = x.a;");
 		code.add("}");
 		code.add("function Node() {");
 		code.add("	this.a = 10;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 	}
 
 	public void testLazyTypeWithAssignment() {
 		StringList code = new StringList();
 		code.add("function Node() {");
 		code.add("	this.nummer = 10;");
 		code.add("}");
 		code.add("function testNode() {");
 		code.add("	returnNode().nummer = 11;");
 		code.add("}");
 		code.add("function returnNode() {");
 		code.add("	return new Node();");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testGenericParamWtihNoneGenericCall() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add(" * @param {Array<String>} array the value of the node");
 		code.add(" */");
 		code.add("function testArray(array) {");
 		code.add("}");
 		code.add("function test() {");
 		code.add("testArray(new Array());");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testGenericParamWtihGenericCall() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add(" * @param {Array<String>} array the value of the node");
 		code.add(" */");
 		code.add("function testArray(array) {");
 		code.add("}");
 		code.add("function test() {");
 		code.add("testArray(['test','test2']);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNoneGenericParamWtihGenericCall() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add(" * @param {Array} array the value of the node");
 		code.add(" */");
 		code.add("function testArray(array) {");
 		code.add("}");
 		code.add("function test() {");
 		code.add("testArray(['test','test2']);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testFunctionWithOptionalParams() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add(" * @param {String} a");
 		code.add(" * @param {String} [b]");
 		code.add(" */");
 		code.add("function tester(a,b) {");
 		code.add("}");
 		code.add("function test() {");
 		code.add("tester('test');");
 		code.add("tester('test','test2');");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testFunctionCallWithUndefinedArgumments() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add(" * @param {String} a");
 		code.add(" * @param {String} b");
 		code.add(" */");
 		code.add("function tester(a,b) {");
 		code.add("}");
 		code.add("function test() {");
 		code.add("tester(a,b);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(2, problems.size());
 		assertEquals(JavaScriptProblems.UNDECLARED_VARIABLE, problems.get(0)
 				.getID());
 		assertEquals(JavaScriptProblems.UNDECLARED_VARIABLE, problems.get(1)
 				.getID());
 	}
 
 	public void testUndefinedVariables() {
 		List<String> code = new StringList();
 		code.add("function test() {");
 		code.add("a=10");
 		code.add("b['test']='1'");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(2, problems.size());
 		assertEquals(JavaScriptProblems.UNDECLARED_VARIABLE, problems.get(0)
 				.getID());
 		assertEquals(JavaScriptProblems.UNDECLARED_VARIABLE, problems.get(1)
 				.getID());
 	}
 
 	public void testArrayLookupWithoutAssign() {
 		List<String> code = new StringList();
 		code.add("function test() {");
 		code.add("var object = new Array();");
 		code.add("var x = object['test'].length;");
 		code.add("var y = object.test;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testArrayLookupWithAssign() {
 		List<String> code = new StringList();
 		code.add("function test() {");
 		code.add("var object = new Array();");
 		code.add("object['test'] = '';");
 		code.add("var x = object['test'].length;");
 		code.add("object.test;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(0, problems.size());
 	}
 
 	public void testObjectInitalizerWithPropertyAndFunction() {
 		List<String> code = new StringList();
 		code.add("var initializer = {x:10,y:function(z){}};");
 		code.add("function testInitializer() {");
 		code.add("var a = initializer.x;");
 		code.add("var b = initializer.y(a);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testDifferentReturnTypeThenDeclared() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {Number} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("return '';");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 	}
 
 	public void testDifferentReturnTypesThenDeclared() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {String} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("var result_1 = 1;");
 		code.add("var condition_1 = false;");
 		code.add("if(condition_1) {");
 		code.add("return result_1;");
 		code.add("}");
 		code.add("return '';");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 2, problems.size());
 	}
 
 	public void testDifferentReturnTypesThenDeclaredWithUndefined() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {String} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("var result_1 = 1;");
 		code.add("var condition_1 = false;");
 		code.add("if(condition_1) {");
 		code.add("return undefined;");
 		code.add("}");
 		code.add("return '';");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testDeclaredtArrayTypeWithUndefineReturn() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {Array<String>} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("return undefined;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testDeclaredtArrayTypeWithNullReturn() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {Array<String>} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("return null;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testDeclaredtArrayTypeWithEmptyReturn() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {Array<String>} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("return;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 	}
 
 	public void testDeclaredtArrayTypeWithNoReturn() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {Array<String>} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 	}
 
 	public void testDifferentReturnTypesThenDeclaredWithNull() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @return {String} some result info");
 		code.add("*/");
 		code.add("function someFunction() {");
 		code.add("var result_1 = 1;");
 		code.add("var condition_1 = false;");
 		code.add("if(condition_1) {");
 		code.add("return null;");
 		code.add("}");
 		code.add("return '';");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testDifferentReturnTypes() {
 		List<String> code = new StringList();
 		code.add("function someFunction() {");
 		code.add("var condition_1 = false;");
 		code.add("if(condition_1) {");
 		code.add("return 1;");
 		code.add("}");
 		code.add("return '';");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.RETURN_INCONSISTENT, problems.get(0)
 				.getID());
 
 	}
 
 	public void testStringTypeAsFunctionCall() {
 		StringList code = new StringList();
 		code.add("var b = true");
 		code.add("var s = String(b)");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testNumberTypeAsFunctionCall() {
 		StringList code = new StringList();
 		code.add("var b = true");
 		code.add("var n = Number(b)");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testCallExternalFunctionViaReferenceCopy() {
 		StringList code = new StringList();
 		code.add("var p = parseInt");
 		code.add("var n = p('1')");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testVariableAssignedFunctionCallingItself() {
 		if (notYetImplemented())
 			return;
 		StringList code = new StringList();
 		code.add("var b = function a() {");
 		code.add("application.output(a);}");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testVariableAssignedObjectInitializerFunctionCallingItSelf() {
 		if (notYetImplemented())
 			return;
 		StringList code = new StringList();
 		code.add("var object = {");
 		code.add(" a: function c() {");
 		code.add(" c(); } }");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testVariableAssignedObjectInitializerFunctionWithThis() {
 		if (notYetImplemented())
 			return;
 		StringList code = new StringList();
 		code.add("var object = {");
 		code.add("a: function c() {this.b();},");
 		code.add("b: function() {}}");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testVariableAssignedObjectInitializerFunctionWithoutThis() {
 		StringList code = new StringList();
 		code.add("var object = {");
 		code.add("a: function c() {b();},");
 		code.add("b: function() {}}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_METHOD, problems.get(0)
 				.getID());
 	}
 
 	public void testJSDocWithPropertiesNoArgumentProperties() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testJSDocWithPropertiesCorrectArgumentProperties() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("obj.display ='display'");
 		code.add("obj.page ='page'");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testJSDocTypedObjectWithPropertiesCorrectArgumentProperties() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("obj.display ='display'");
 		code.add("obj.page ='page'");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testJSDocWithPropertiesWrongTypedArgumentProperties() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("obj.display =10");
 		code.add("obj.page ='page'");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testJSDocWithOptionalPropertiesWithOnlyMandatoryArgumentProperties() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("* @param {String} [anchor.space]");
 		code.add("* @param {String} [anchor.anchor]");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("obj.display ='display'");
 		code.add("obj.page ='page'");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testJSDocWithOptionalPropertiesWithSomeOptionalArgumentProperties() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("* @param {String} [anchor.space]");
 		code.add("* @param {String} [anchor.anchor]");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("obj.display ='display'");
 		code.add("obj.page ='page'");
 		code.add("obj.space ='display'");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testJSDocWithOptionalPropertiesWithAllArgumentProperties() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("* @param {String} [anchor.space]");
 		code.add("* @param {String} [anchor.anchor]");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("obj.display ='display'");
 		code.add("obj.page ='page'");
 		code.add("obj.space ='display'");
 		code.add("obj.anchor ='page'");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertTrue(problems.toString(), problems.isEmpty());
 	}
 
 	public void testJSDocWithOptionalPropertiesWithAllArgumentPropertiesWrongType() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("* @param {String} anchor.display");
 		code.add("* @param {String} anchor.page");
 		code.add("* @param {String} [anchor.space]");
 		code.add("* @param {String} [anchor.anchor]");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("}");
 		code.add("function tester() {");
 		code.add("var obj = new Object();");
 		code.add("obj.display ='display'");
 		code.add("obj.page ='page'");
 		code.add("obj.space ='display'");
 		code.add("obj.anchor =10");
 		code.add("test(obj);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testVariableDeclartionWithUnknowPropertyAssignment() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("var x = anchor.y");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testVariableWithUnknowPropertyAssignment() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("var x = null;");
 		code.add("x = anchor.y");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testFunctionCallWithUnknowProperty() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("test(anchor.xxx);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testUnknowPropertyIfTest() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("if(anchor.xxx){}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testDoubleUnknowPropertyIfTest() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("if(anchor.xxx && anchor.yyyy){}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testUnknowVariableIfTest() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("if(anchorr.xxx){}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDECLARED_VARIABLE, problems.get(0)
 				.getID());
 	}
 
 	public void testUnknowPropertyOfPropertyIfTest() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {Object} anchor");
 		code.add("*/");
 		code.add("function test(anchor) {");
 		code.add("if(anchor.xxx.yyy){}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testStringArrayxInitializerWithNullElement() {
 		List<String> code = new StringList();
 		code.add("/**");
 		code.add("* @param {String[]} args");
 		code.add("*/");
 		code.add("function test2(args) {");
 		code.add(" test2(['hello',null]);");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNestedAnonymousReturnType() {
 		List<String> code = new StringList();
 		code.add("function Test() {");
 		code.add(" function Node() {");
 		code.add(" this.fun = function() {");
 		code.add("  return new Node();");
 		code.add("}}");
 		code.add("this.getNode = function() {");
 		code.add(" return new Node();");
 		code.add(" }");
 		code.add("}");
 		code.add("function caller(){");
 		code.add(" var x = new Test();");
 		code.add(" var node = x.getNode();");
 		code.add(" var node2 = node.fun();");
 		code.add(" var node3 = node2.fun();");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testMoreDeeplyNestedCallsToAnonymousReturnType() {
 		List<String> code = new StringList();
 		code.add("function Test() {");
 		code.add(" function Node() {");
 		code.add(" this.fun = function() {");
 		code.add("  return new Node();");
 		code.add("}}");
 		code.add("this.getNode = function() {");
 		code.add(" return new Node();");
 		code.add(" }");
 		code.add("}");
 		code.add("function caller(){");
 		code.add(" var x = new Test();");
 		code.add(" var node = x.getNode();");
 		code.add(" var node2 = node.fun();");
 		code.add(" var node3 = node2.fun();");
 		code.add(" var node4 = node3.fun();");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNestedTypeToReturnType() {
 		if (notYetImplemented())
 			return;
 		List<String> code = new StringList();
 		code.add("function Test() {");
 		code.add(" function Node() {");
 		code.add(" this.fun = function() {");
 		code.add("  return new Node();");
 		code.add("}}");
 		code.add("this.getNode = function() {");
 		code.add(" return new Node();");
 		code.add(" }");
 		code.add("this.Node = Node;");
 		code.add("}");
 		code.add("/**");
 		code.add(" * @return {Test.Node} */");
 		code.add("function caller(){");
 		code.add(" var x = new Test();");
 		code.add(" return x.getNode();");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNestedFunctionTypeNewConstruct() {
 		List<String> code = new StringList();
 		code.add("function Test() {");
 		code.add(" function Node() {");
 		code.add(" this.fun = function() {");
 		code.add("  return new Node();");
 		code.add("}}");
 		code.add("this.getNode = function() {");
 		code.add(" return new Node();");
 		code.add(" }");
 		code.add("this.Node = Node;");
 		code.add("}");
 		code.add("function caller(){");
 		code.add(" var x = new Test();");
 		code.add(" var node =  new x.Node();");
 		code.add(" node.fun();");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNestedObjectInitializerType() {
 		StringList code = new StringList();
 		code.add("var init = {Node: function(){} }");
 		code.add("/**");
 		code.add(" * @return {init.Node} */");
 		code.add("function caller2() {");
 		code.add(" return new init.Node();}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNestedObjectInitializerTypeWithFunctionCall() {
 		StringList code = new StringList();
 		code.add("var init = {Node: function(){}, ");
 		code.add("fun: function() {return new init.Node();} }");
 		code.add("/**");
 		code.add(" * @return {init.Node} */");
 		code.add("function caller2() {");
 		code.add(" return init.fun();}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testNestedObjectInitializerWithThisTypeWithFunctionCall() {
 		if (notYetImplemented())
 			return;
 		final StringList code = new StringList();
 		code.add("var init = {");
 		code.add("Node: function(){}, ");
 		code.add("fun: function() {return new this.Node();}");
 		code.add("}");
 		code.add("/**");
 		code.add(" * @return {init.Node} */");
 		code.add("function caller2() {");
 		code.add(" return init.fun();}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testOuterVariableInInnerFunction() {
 		if (notYetImplemented())
 			return;
 		StringList code = new StringList();
 		code.add("function Outer() {");
 		code.add("function inner() {");
 		code.add("variable = 10;");
 		code.add("}");
 		code.add("var variable;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testAssignmentOfVariableToUndefined() {
 		StringList code = new StringList();
 		code.add("function test() {");
 		code.add(" var str = '';");
 		code.add(" str.toLocaleString();");
 		code.add(" str = undefined;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testAssignmentOfArgumentToUndefined() {
 		StringList code = new StringList();
 		code.add("/** @param {String} str */");
 		code.add("function test(str) {");
 		code.add(" str.toLocaleString();");
 		code.add(" str = undefined;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testCompareOperatorsReturningBoolean() {
 		StringList code = new StringList();
 		code.add("/** @return {Boolean} */");
 		code.add("function test() {");
 		code.add(" var x = 1; var y = 2;");
 		code.add(" if(x > 1)");
 		code.add("  return (y == 2);");
 		code.add(" if(x > 1)");
 		code.add("  return (y != 2);");
 		code.add(" if(x > 1)");
 		code.add("  return (y < 2);");
 		code.add(" if(x > 1)");
 		code.add("  return (y > 2);");
 		code.add(" if(x > 1)");
 		code.add("  return (y >= 2);");
 		code.add(" if(x > 1)");
 		code.add("  return (y <= 2);");
 		code.add(" if(x > 1)");
 		code.add("  return (y === 2);");
 		code.add(" if(x > 1)");
 		code.add("  return (y !== 2);");
 		code.add(" return false;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testReturnFromNestedCollections() {
 		StringList code = new StringList();
 		code.add("/** @return {Number} */");
 		code.add("function test() {");
 		code.add("  if (1 > 0) {");
 		code.add("    return 1");
 		code.add("  }");
 		code.add("  else {");
 		code.add("    return 2");
 		code.add("  }");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testObjectProperyReturnTypeWithDirectInitializer() {
 		StringList code = new StringList();
 		code.add("/** @return {{astring:String,anumber:Number}} */");
 		code.add("function test() {");
 		code.add("return {astring:'test',anumber:1}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testObjectProperyReturnTypeWithInvalidDirectInitializer() {
 		StringList code = new StringList();
 		code.add("/** @return {{astring:String,anumber:Number}} */");
 		code.add("function test() {");
 		code.add("return {aastring:'test',anumber:1}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 
 	}
 
 	public void testObjectProperyReturnTypeWithObjectInitializer() {
 		StringList code = new StringList();
 		code.add("/** @return {{astring:String,anumber:Number}} */");
 		code.add("function test() {");
 		code.add(" var obj =  {astring:'test',anumber:1}");
 		code.add("  return obj;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testObjectProperyReturnTypeWithInvalidObjectInitializer() {
 		StringList code = new StringList();
 		code.add("/** @return {{astring:String,anumber:Number}} */");
 		code.add("function test() {");
 		code.add(" var obj =  {aastring:'test',anumber:1}");
 		code.add("  return obj;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 	}
 
 	public void testObjectProperyReturnTypeWithObject() {
 		StringList code = new StringList();
 		code.add("/** @return {{astring:String,anumber:Number}} */");
 		code.add("function test() {");
 		code.add(" var obj =  new Object();");
 		code.add(" obj.astring = ''");
 		code.add(" obj.anumber = 1");
 		code.add(" return obj;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testObjectProperyReturnTypeWithObjectWithDifferentType() {
 		StringList code = new StringList();
 		code.add("/** @return {{astring:String,anumber:Number}} */");
 		code.add("function test() {");
 		code.add(" var obj =  new Object();");
 		code.add(" obj.astring = ''");
 		code.add(" obj.anumber = '1'");
 		code.add(" return obj;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 	}
 
 	public void testObjectProperyReturnTypeWithInvalidObject() {
 		StringList code = new StringList();
 		code.add("/** @return {{astring:String,anumber:Number}} */");
 		code.add("function test() {");
 		code.add(" var obj =  new Object();");
 		code.add(" obj.aastring = ''");
 		code.add(" obj.anumber = 1");
 		code.add(" return obj;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(
 				JavaScriptProblems.DECLARATION_MISMATCH_ACTUAL_RETURN_TYPE,
 				problems.get(0).getID());
 	}
 
 	public void testParamWithObjectDeclaration() {
 		StringList code = new StringList();
 		code.add("/** @param {{astring:String,anumber:Number}} myparam */");
 		code.add("function test(myparam) {");
 		code.add(" var x = myparam.astring");
 		code.add(" var y = myparam.anumber");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testParamWithObjectDeclarationInvalidProperty() {
 		StringList code = new StringList();
 		code.add("/** @param {{astring:String,anumber:Number}} myparam */");
 		code.add("function test(myparam) {");
 		code.add(" var x = myparam.aastring");
 		code.add(" var y = myparam.anumber");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNDEFINED_PROPERTY, problems.get(0)
 				.getID());
 	}
 
 	public void testParamWithObjectDeclarationCall() {
 		StringList code = new StringList();
 		code.add("/** @param {{astring:String,anumber:Number}} myparam */");
 		code.add("function test(myparam) {");
 		code.add("}");
 		code.add("function test2(myparam) {");
 		code.add(" test({astring:'',anumber:1});");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testParamWithObjectDeclarationCallWithDifferentType() {
 		StringList code = new StringList();
 		code.add("/** @param {{astring:String,anumber:Number}} myparam */");
 		code.add("function test(myparam) {");
 		code.add("}");
 		code.add("function test2(myparam) {");
 		code.add(" test({astring:'',anumber:'1'});");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testParamWithObjectDeclarationInvalidCall() {
 		StringList code = new StringList();
 		code.add("/** @param {{astring:String,anumber:Number}} myparam */");
 		code.add("function test(myparam) {");
 		code.add("}");
 		code.add("function test2(myparam) {");
 		code.add(" test({aastring:'',anumber:1});");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.WRONG_PARAMETERS, problems.get(0)
 				.getID());
 	}
 
 	public void testReturnStringAdd() {
 		StringList code = new StringList();
 		code.add("/** @return {String} */");
 		code.add("function test() {");
 		code.add("  return 1 + ''");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testMapWithValueDeclaration() {
 		StringList code = new StringList();
 		code.add("/** @param {Object<String>} param */");
 		code.add("function test(param) {");
 		code.add(" var x = param['test'];");
 		code.add(" var p = x.toLocaleLowerCase();");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testMapWithKeyValueDeclaration() {
 		StringList code = new StringList();
 		code.add("/** @param {Object<Number,String>} param */");
 		code.add("function test(param) {");
 		code.add(" var x = param['test'];");
 		code.add(" var p = x.toLocaleLowerCase();");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testMapWithValueWithWrongTypeDeclaration() {
 		StringList code = new StringList();
 		code.add("/** @param {Object<Stringg>} param */");
 		code.add("function test(param) {");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 	}
 
 	public void testMapWithKeyValueWithWrongTypeDeclaration() {
 		StringList code = new StringList();
 		code.add("/** @param {Object<Stringg,Numberr>} param */");
 		code.add("function test(param) {");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 2, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(1).getID());
 	}
 
 	public void testArrayWithWrongTypeDeclaration() {
 		StringList code = new StringList();
 		code.add("/** @param {Array<Stringg>} param */");
 		code.add("function test(param) {");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 	}
 
 	public void testUnionWithWrongTypeDeclaration() {
 		StringList code = new StringList();
 		code.add("/** @param {Stringg|Number} param */");
 		code.add("function test(param) {");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNKNOWN_TYPE, problems.get(0).getID());
 	}
 
 	public void testJavaObjectAndUndefineReturn() {
 		StringList code = new StringList();
 		code.add("function test1(param) {");
 		code.add(" if (param) return new ExampleService();");
 		code.add(" else return undefined;");
 		code.add("}");
 		code.add("function test2(param) {");
 		code.add(" test1(true).name = 10;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testXMLWithParentheseExpression() {
 		StringList code = new StringList();
 		code.add("function test() {");
 		code.add(" var tmp = 10;");
 		code.add(" var xml = <></>;");
 		code.add(" var node = xml..*.object.(@qualifiedName == tmp);");
 		code.add(" if (node.length() == 0) {}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testXMLWithParentheseExpressionWithUnknownVariableReference() {
 		StringList code = new StringList();
 		code.add("function test() {");
 		code.add(" var xml = <></>;");
 		code.add(" var node = xml..*.object.(@qualifiedName == tmp);");
 		code.add(" if (node.length() == 0) {}");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 1, problems.size());
 		assertEquals(JavaScriptProblems.UNDECLARED_VARIABLE, problems.get(0)
 				.getID());
 
 	}
 
 	public void testReassignOfAVariableWithDifferentDataAccessingItRightBefore() {
 		StringList code = new StringList();
 		code.add("function getParamXML() {");
 		code.add(" var xml = <xml><data></data></xml>;");
 		code.add(" xml.data.ignoreComments;");
 		code.add(" xml = <xml><data1></data1></xml>;");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testRecordTypeWithSpacesAndOwnCurlyBrackets() {
 		StringList code = new StringList();
 		code.add("/**");
 		code.add(" * @type {{height:Number, widgetName:String, widgetTitle: String,originalColumn: forms.widgetColumn, originalPosition:Number,currentColumn:forms.widgetColumn,currentPosition:Number,columns:{} }}");
 		code.add("*/");
 		code.add("var eventDataType;");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testUnionTypeTest() throws Exception {
 		StringList code = new StringList();
 		code.add("/** @param {String|Number} param */");
 		code.add("function test(param) {");
 		code.add(" param.toFixed(1)");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 0, problems.size());
 	}
 
 	public void testUnionUnknownTypeTest() throws Exception {
 		StringList code = new StringList();
 		code.add("/** @param {String|Numberr} param */");
 		code.add("function test(param) {");
 		code.add(" param.toFixed(1)");
 		code.add("}");
 		final List<IProblem> problems = validate(code.toString());
 		assertEquals(problems.toString(), 2, problems.size());
 	}
 
 }
