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
 
 import junit.framework.TestCase;
 
 import org.eclipse.dltk.compiler.env.ModuleSource;
 import org.eclipse.dltk.core.ISourceElementParser;
 import org.eclipse.dltk.core.tests.util.StringList;
 
 @SuppressWarnings("restriction")
 public class StructureParserTests extends TestCase {
 
 	protected ISourceElementParser createParser() {
 		return new TestJavaScriptSourceElementParser();
 	}
 
 	protected Root parse(String code) {
 		if (TestJavaScriptSourceElementParser.DEBUG) {
 			System.out.println(code.trim());
 			System.out.println("---");
 		}
 		Recorder rec = new Recorder();
 		ISourceElementParser parser = createParser();
 		parser.setRequestor(rec);
 		parser.parseSourceModule(new ModuleSource(code));
 		if (TestJavaScriptSourceElementParser.DEBUG) {
 			System.out.println("---");
 			System.out.println(rec.getRoot());
 			System.out.println("------");
 		}
 		return rec.getRoot();
 	}
 
 	public void testMethod() {
 		Root root = parse("function hello(){}");
 		assertEquals(new Root(new Method("hello")), root);
 	}
 
 	public void testMethodWithParams() {
 		Root root = parse("function hello(name){}");
 		assertEquals(new Root(new Method("hello", new Parameter("name"))), root);
 	}
 
 	public void testField() {
 		Root root = parse("var x = 1");
 		assertEquals(new Root(new Field("x")), root);
 	}
 
 	public void testMethodField() {
 		Root root = parse("function hello(){var x = 1}");
 		assertEquals(new Root(new Method("hello")), root);
 	}
 
 	public void testMethodFieldMethod() {
 		Root root = parse("function hello() { var a = { z: function() { } } }");
 		assertEquals(
 				new Root(new Method("hello").skip().add(
 						new Field("a").add(new Method("z")))), root);
 	}
 
 	public void testMethodMethod() {
 		Root root = parse("function hello() { function world() { } }");
 		assertEquals(new Root(new Method("hello").add(new Method("world"))),
 				root);
 	}
 
 	public void testMethodReference() {
 		Root root = parse("var x = calculate()");
 		assertEquals(new Root(new Field("x").add(new MethodRef("calculate"))),
 				root);
 	}
 
 	public void testFieldReference() {
 		Root root = parse("var x = database.get()");
 		assertEquals(
 				new Root(new Field("x").add(new FieldRef("database"),
 						new MethodRef("get"))), root);
 	}
 
 	public void testFieldReference1() {
 		Root root = parse("var a = 1; var x = a");
 		assertEquals(
 				new Root(new Field("a"), new Field("x").add(new FieldRef("a"))),
 				root);
 	}
 
 	public void testFunctionFieldReference() {
 		Root root = parse("var database;function run() {var x = database.get()}");
 		assertEquals(
 				new Root(new Field("database"), new Method("run").add(
 						new FieldRef("database"), new MethodRef("get"))), root);
 	}
 
 	public void testFunctionFieldReference1() {
 		Root root = parse("function hello() { var a = 1; var x = a }");
 		assertEquals(new Root(new Method("hello")), root);
 	}
 
 	public void testThisVariableUnnamedFunctionReference() {
 		Root root = parse("function hello() { this.a = function(){} }");
 		assertEquals(new Root(new Method("hello").add(new Method("a"))), root);
 	}
 
 	public void testThisVariableUnnamedFunctionReferenceNestedVariable() {
 		Root root = parse("function hello() { this.a = function(){var b} }");
 		assertEquals(new Root(new Method("hello").add(new Method("a"))), root);
 	}
 
 	public void testThisVariableSameNamedFunctionReference() {
 		Root root = parse("function hello() { this.a = function a(){} }");
 		assertEquals(new Root(new Method("hello").add(new Method("a"))), root);
 	}
 
 	public void testThisVariableNamedFunctionReference() {
 		Root root = parse("function hello() { this.a = function b(){} }");
 		assertEquals(new Root(new Method("hello").add(new Method("a"))), root);
 	}
 
 	public void testObjectInitializerFunctionReference() {
 		Root root = parse("function hello() { var a = {b: function(){}} }");
 		assertEquals(
 				new Root(new Method("hello").skip().add(
 						new Field("a").add(new Method("b")))), root);
 	}
 
 	public void testThisVariableUnnamedFunctionReferenceWithArgumentAssignment() {
 		Root root = parse("function hello() { var x = function(a){this.b =a;}}");
 		assertEquals(
 				new Root(new Method("hello").add(new Method("x", new Parameter(
 						"a")).add(new FieldRef("b")))), root);
 	}
 
 	public void testVariableInitializedWithFunction() {
 		Root root = parse("var x = function(a) {this.b = a;}");
 		assertEquals(
 				new Root(new Method("x", new Parameter("a")).add(new FieldRef(
 						"b"))), root);
 	}
 
 	public void testManyFunctionCalls() {
 		StringList code = new StringList();
 		code.add("function a(){x=1} a();fa=1;");
 		code.add("function b(){y=2} b();fb=2;");
 		code.add("function c(){z=3} c();fc=3;");
 		Root root = parse(code.toString());
 		Root expected = new Root();
 		expected.add(new Method("a").add(new FieldRef("x")),
 				new MethodRef("a"), new FieldRef("fa"));
 		expected.add(new Method("b").add(new FieldRef("y")),
 				new MethodRef("b"), new FieldRef("fb"));
 		expected.add(new Method("c").add(new FieldRef("z")),
 				new MethodRef("c"), new FieldRef("fc"));
 		assertEquals(expected, root);
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.ti.JSDocSupport
 	 */
 	public void testJsDocsType() {
 		StringList code = new StringList();
 		code.add("/** @type Number */");
 		code.add("function a(){}");
 		Method a = new Method("a");
 		a.setType("Number");
 		assertEquals(new Root(a), parse(code.toString()));
 		code.remove(0);
 		assertFalse(new Root(a).equals(parse(code.toString())));
 	}
 
 	/**
 	 * @see org.eclipse.dltk.internal.javascript.ti.JSDocSupport
 	 */
 	public void testJsDocsParam() {
 		StringList code = new StringList();
 		code.add("/** @param {Number} n */");
 		code.add("function a(n,s){}");
 		Method a = new Method("a", new Parameter("n", "Number"), new Parameter(
 				"s"));
 		assertEquals(new Root(a), parse(code.toString()));
 		code.remove(0);
 		assertFalse(new Root(a).equals(parse(code.toString())));
 	}
 
 	public void testObjectLiterals() {
 		{
 			final StringList code = new StringList();
 			code.add("function a(){");
 			code.add("  return {");
 			code.add("    color: 'red',");
 			code.add("    point: {");
 			code.add("      x: function(){},");
 			code.add("      y: function(){}");
 			code.add("    }");
 			code.add("  }");
 			code.add("}");
			assertEquals(new Root(new Method("a").setType("Object")), parse(code.toString()));
 		}
 		{
 			final StringList code = new StringList();
 			code.add("a(");
 			code.add("  {");
 			code.add("    color: 'red',");
 			code.add("    point: {");
 			code.add("      x: function(){var xx = 1},");
 			code.add("      y: function(){var yy = 2}");
 			code.add("    }");
 			code.add("  }");
 			code.add(")");
 			assertEquals(new Root(new MethodRef("a")), parse(code.toString()));
 		}
 	}
 
 	public void testGlobalObjectLiteral() {
 		final StringList code = new StringList();
 		code.add("var POINT = {");
 		code.add("  /** @return {Point} */");
 		code.add("  copy: function() {},");
 		code.add("  x: 1,");
 		code.add("  y: 2");
 		code.add("}");
 		final Root actual = parse(code.toString());
 
 		final Root expected = new Root();
 		expected.add(new Field("POINT").add(
 				new Method("copy").setType("Point"), new Field("x"), new Field(
 						"y")));
 		assertEquals(expected, actual);
 	}
 
 	public void testLocalObjectLiteral() {
 		final StringList code = new StringList();
 		code.add("function test() {");
 		code.add("  var POINT = {");
 		code.add("    /** @return {Point} */");
 		code.add("    copy: function() {},");
 		code.add("    x: 1,");
 		code.add("    y: 2");
 		code.add("  }");
 		code.add("}");
 
 		final Root expected = new Root();
 		expected.add(new Method("test"));
 		assertEquals(expected, parse(code.toString()));
 	}
 
 }
