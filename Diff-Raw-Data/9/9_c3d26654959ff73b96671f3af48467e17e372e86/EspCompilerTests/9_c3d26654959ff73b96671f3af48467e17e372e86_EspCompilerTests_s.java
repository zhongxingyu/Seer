 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.build.esp;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.oobium.build.esp.EspCompiler;
 import org.oobium.build.esp.EspDom;
 import org.oobium.build.esp.ESourceFile;
 
 public class EspCompilerTests {
 
 	private String body(String method) {
 		int s1 = 0;
 		while(s1 < method.length() && method.charAt(s1) != '{') {
 			s1++;
 		}
 		s1++;
 		while(s1 < method.length() && Character.isWhitespace(method.charAt(s1))) {
 			s1++;
 		}
 		return method.substring(s1, method.length() - 3).replace("\n\t\t", "\n");
 	}
 	
 	private String html(String esp) {
 		ESourceFile src = src(esp);
 		String str = body(src.getMethod("doRender"));
 		System.out.println(src.getMethod("doRender").replace("\n\t", "\n"));
 		return str;
 	}
 	
 	private ESourceFile src(String esp) {
 		EspDom dom = new EspDom("MyEsp", esp);
 		EspCompiler e2j = new EspCompiler("com.mydomain", dom);
 		return e2j.compile();
 	}
 	
 	@Test
 	public void testImport() throws Exception {
 		assertTrue(src("import com.mydomain.MyClass").hasImport("com.mydomain.MyClass"));
 		assertEquals("com.mydomain.MyClass", src("import com.mydomain.MyClass").getImport("com.mydomain.MyClass"));
 		assertTrue(src("import com.mydomain.MyClass").getSource().contains("\nimport com.mydomain.MyClass;\n"));
 
 		assertTrue(src("import com.mydomain.MyClass;").hasImport("com.mydomain.MyClass"));
 		assertEquals("com.mydomain.MyClass", src("import com.mydomain.MyClass;").getImport("com.mydomain.MyClass"));
 		assertTrue(src("import com.mydomain.MyClass;").getSource().contains("\nimport com.mydomain.MyClass;\n"));
 
 		assertFalse(src("import com.mydomain.MyClass;;").getSource().contains("\nimport com.mydomain.MyClass;\n"));
 	}
 	
 	@Test
 	public void testConstructor() throws Exception {
 		String esp;
 		
 		// single arg
 		esp = "MyEsp(String arg1)";
 		assertTrue(src(esp).hasVariable("arg1"));
 		assertEquals("public String arg1", src(esp).getVariable("arg1"));
 		assertEquals(1, src(esp).getConstructorCount());
 		assertTrue(src(esp).hasConstructor(0));
 		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(0));
 		
 		// multiple args
 		esp = "MyEsp(String arg1,  int  arg2 )";
 		assertTrue(src(esp).hasVariable("arg1"));
 		assertTrue(src(esp).hasVariable("arg2"));
 		assertEquals("public String arg1", src(esp).getVariable("arg1"));
 		assertEquals("public int arg2", src(esp).getVariable("arg2"));
 		assertEquals(1, src(esp).getConstructorCount());
 		assertTrue(src(esp).hasConstructor(0));
 		assertEquals("\tpublic MyEsp(String arg1, int arg2) {\n\t\tthis.arg1 = arg1;\n\t\tthis.arg2 = arg2;\n\t}", src(esp).getConstructor(0));
 		
 		// complex arg
 		esp = "MyEsp(List<Map<String, Object>> arg1)";
 		assertTrue(src(esp).hasVariable("arg1"));
 		assertEquals("public List<Map<String, Object>> arg1", src(esp).getVariable("arg1"));
 		assertEquals(1, src(esp).getConstructorCount());
 		assertTrue(src(esp).hasConstructor(0));
 		assertEquals("\tpublic MyEsp(List<Map<String, Object>> arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(0));
 		
 		EspDom dom = new EspDom("Error404", "Error404(Exception exception)");
 		EspCompiler e2j = new EspCompiler("com.mydomain", dom);
 		ESourceFile jfile = e2j.compile();
 		assertTrue(jfile.hasVariable("exception"));
 		assertEquals("public Exception exception", jfile.getVariable("exception"));
 		assertEquals(1, jfile.getConstructorCount());
 		assertTrue(jfile.hasConstructor(0));
 		assertEquals("\tpublic Error404(Exception exception) {\n\t\tthis.exception = exception;\n\t}", jfile.getConstructor(0));
 	}
 	
 	@Test
 	public void testConstructorsWithDefaultValue() throws Exception {
 		String esp;
 		esp = "MyEsp(String arg1=\"hello\")";
 		assertTrue(src(esp).hasVariable("arg1"));
 		assertEquals("public String arg1", src(esp).getVariable("arg1"));
 		assertEquals(2, src(esp).getConstructorCount());
 		assertTrue(src(esp).hasConstructor(0));
 		assertEquals("\tpublic MyEsp() {\n\t\tthis.arg1 = \"hello\";\n\t}", src(esp).getConstructor(0));
 		assertTrue(src(esp).hasConstructor(1));
 		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis.arg1 = arg1;\n\t}", src(esp).getConstructor(1));
 
 		esp = "MyEsp(String arg1, String arg2=\"hello\")";
 		assertTrue(src(esp).hasVariable("arg1"));
 		assertEquals("public String arg1", src(esp).getVariable("arg1"));
 		assertEquals(2, src(esp).getConstructorCount());
 		assertTrue(src(esp).hasConstructor(0));
 		assertEquals("\tpublic MyEsp(String arg1) {\n\t\tthis.arg1 = arg1;\n\t\tthis.arg2 = \"hello\";\n\t}", src(esp).getConstructor(0));
 		assertTrue(src(esp).hasConstructor(1));
 		assertEquals("\tpublic MyEsp(String arg1, String arg2) {\n\t\tthis.arg1 = arg1;\n\t\tthis.arg2 = arg2;\n\t}", src(esp).getConstructor(1));
 	}
 	
 	@Test
 	public void testTitle() throws Exception {
 		String esp;
 		esp = "title hello";
 		assertTrue(src(esp).hasMethod("doRenderTitle"));
 		assertTrue(src(esp).hasMethod("hasTitle"));
 		assertEquals("__sb__.append(\"hello\");", body(src(esp).getMethod("doRenderTitle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasTitle")));
 
 		esp = "title \"hello\"";
 		assertTrue(src(esp).hasMethod("doRenderTitle"));
 		assertTrue(src(esp).hasMethod("hasTitle"));
 		assertEquals("__sb__.append(\"\\\"hello\\\"\");", body(src(esp).getMethod("doRenderTitle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasTitle")));
 
 		esp = "title hello <- div goodbye";
 		assertTrue(src(esp).hasMethod("doRenderTitle"));
 		assertTrue(src(esp).hasMethod("hasTitle"));
 		assertEquals("__sb__.append(\"hello \");", body(src(esp).getMethod("doRenderTitle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasTitle")));
 
 		esp = "title hello\n\tdiv goodbye";
 		assertTrue(src(esp).hasMethod("doRenderTitle"));
 		assertTrue(src(esp).hasMethod("hasTitle"));
 		assertEquals("__sb__.append(\"hello\");", body(src(esp).getMethod("doRenderTitle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasTitle")));
 
 		esp = "title hello\ntitle goodbye";
 		assertTrue(src(esp).hasMethod("doRenderTitle"));
 		assertTrue(src(esp).hasMethod("hasTitle"));
 		assertEquals("__sb__.append(\"goodbye\");", body(src(esp).getMethod("doRenderTitle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasTitle")));
 
 		esp = "title hello\ntitle += and\ntitle += goodbye";
 		assertTrue(src(esp).hasMethod("doRenderTitle"));
 		assertTrue(src(esp).hasMethod("hasTitle"));
 		assertEquals("__sb__.append(\"hello and goodbye\");", body(src(esp).getMethod("doRenderTitle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasTitle")));
 	}
 	
 	@Test
 	public void testScript() throws Exception {
 		assertEquals(0, src("script").getMethodCount());
 		assertEquals("__sb__.append(\"<script>function() { alert('hello') }</script>\");", html("script function() { alert('hello') }"));
 		assertEquals("__sb__.append(\"<script>function() { alert('hello') }\\n\\tfunction() { alert('goodbye') }</script>\");", html("script function() { alert('hello') }\n\tfunction() { alert('goodbye') }"));
 
 		// after java line
 		assertEquals("if(true)\n\t__sb__.append(\"<script>alert('hello');</script>\");", html("-if(true)\n\tscript alert('hello');"));
 
 		// including an EJS file
 		assertEquals("__sb__.append(\"<script>\");\nMyScripts myScripts$0 = new MyScripts();\nmyScripts$0.render(__sb__);\n__sb__.append(\"</script>\");", html("script<MyScripts>"));
 	}
 	
 	@Test
 	public void testStyle() throws Exception {
 		assertEquals(0, src("style").getMethodCount());
 		assertEquals("__sb__.append(\"<div></div>\");", html("div <- style"));
 		assertEquals("__sb__.append(\"<style>.myClass{color:red}</style>\");", html("style .myClass { color: red; }"));
 		assertEquals("__sb__.append(\"<style>.myClass{color:red}</style>\");", html("style\n\t.myClass { color: red; }"));
 		assertEquals("__sb__.append(\"<div><style>.myClass{color:red}</style></div>\");", html("div <- style .myClass { color: red; }"));
 		assertEquals("__sb__.append(\"<div><style>.myClass{color:red}</style></div>\");", html("div\n\tstyle .myClass { color: red; }"));
 		assertEquals("__sb__.append(\"<div><style>.myClass{color:red}</style></div>\");", html("div\n\tstyle\n\t\t.myClass { color: red; }"));
 
 		// with java
 		assertEquals("__sb__.append(\"<div><style>.myClass{width:\").append(height * 2 + \"px\").append(\"}</style></div>\");", html("div\n\tstyle\n\t\t.myClass { width:= height * 2 + \"px\"; }"));
 
 		// with comments
 		assertEquals("__sb__.append(\"<style>.myClass{color:red}</style>\");", html("style .myClass { /* color:blue; */ color: red; }"));
 
 		// after java line
 		assertEquals("if(true)\n\t__sb__.append(\"<style>td{border:0}</style>\");", html("-if(true)\n\tstyle td { border: 0 }"));
 
 		// including an ESS file
 		assertEquals("__sb__.append(\"<style>\");\nMyStyles myStyles$0 = new MyStyles();\nmyStyles$0.render(__sb__);\n__sb__.append(\"</style>\");", html("style<MyStyles>"));
 	}
 
 	@Test
 	public void testScriptInHead() throws Exception {
 		String esp;
 		esp = "head <- script";
 		assertFalse(src(esp).hasMethod("doRenderScript"));
 		assertFalse(src(esp).hasMethod("hasScript"));
 
 		esp = "head <- script function { alert('hello'); }";
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("__sb__.append(\"<script>function { alert('hello'); }</script>\");", body(src(esp).getMethod("doRenderScript")));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 
 		esp = "head\n\tscript function { alert('hello'); }";
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("__sb__.append(\"<script>function { alert('hello'); }</script>\");", body(src(esp).getMethod("doRenderScript")));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 
 		esp = "head <- script(defaults)";
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("__sb__.append(\"<script src='/jquery-1.4.4.js'></script><script src='/application.js'></script>\");", body(src(esp).getMethod("doRenderScript")));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 
 		esp = "head <- script(myFile)";
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("__sb__.append(\"<script src='/myFile.js'></script>\");", body(src(esp).getMethod("doRenderScript")));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 
 		esp = "head <- script(myFile.js)";
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("__sb__.append(\"<script src='/myFile.js'></script>\");", body(src(esp).getMethod("doRenderScript")));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 
 		esp = "head <- script function { alert('hello'); }\n\tfunction { alert('goodbye'); }";
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("__sb__.append(\"<script>function { alert('hello'); }\\n\\tfunction { alert('goodbye'); }</script>\");", body(src(esp).getMethod("doRenderScript")));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 
 		// including an external EJS file
 		esp = "head <- script<MyScripts>";
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertEquals("String path$8 = underscored(MyScripts.class.getName()).replace('.', '/');\n" +
 					 "__sb__.append(\"<script src='/\").append(path$8).append(\".js'></script>\");",
 				body(src(esp).getMethod("doRenderScript")));
 
 		// including an EJS file
 		esp = "head <- script<MyScripts>(inline: true)";
 		assertTrue(src(esp).hasMethod("hasScript"));
 		assertEquals("return true;", body(src(esp).getMethod("hasScript")));
 		assertTrue(src(esp).hasMethod("doRenderScript"));
 		assertEquals("__sb__.append(\"<script>\");\nMyScripts myScripts$8 = new MyScripts();\nmyScripts$8.render(__sb__);\n__sb__.append(\"</script>\");", body(src(esp).getMethod("doRenderScript")));
 	}
 	
 	@Test
 	public void testStyleInHead() throws Exception {
 		String esp;
 		esp = "head <- style";
 		assertFalse(src(esp).hasMethod("doRenderStyle"));
 		assertFalse(src(esp).hasMethod("hasStyle"));
 
 		esp = "head <- style .myClass { color: red; }";
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("__sb__.append(\"<style>.myClass{color:red}</style>\");", body(src(esp).getMethod("doRenderStyle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 
 		esp = "head\n\tstyle .myClass { color: red; }";
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("__sb__.append(\"<style>.myClass{color:red}</style>\");", body(src(esp).getMethod("doRenderStyle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 
 		esp = "head <- style(defaults)";
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("__sb__.append(\"<link rel='stylesheet' type='text/css' href='/application.css' />\");", body(src(esp).getMethod("doRenderStyle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 
 		esp = "head <- style(myFile)";
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("__sb__.append(\"<link rel='stylesheet' type='text/css' href='/myFile.css' />\");", body(src(esp).getMethod("doRenderStyle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 
 		esp = "head <- style(myFile.css)";
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("__sb__.append(\"<link rel='stylesheet' type='text/css' href='/myFile.css' />\");", body(src(esp).getMethod("doRenderStyle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 
 		esp = "head <- style .myClass1 { color: red; }\n\t.myClass2 { color: blue; }";
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("__sb__.append(\"<style>.myClass1{color:red} .myClass2{color:blue}</style>\");", body(src(esp).getMethod("doRenderStyle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 
 		esp = "head\n\tstyle\n\t\t.myClass1\n\t\t\tcolor: red\n\t\t.myClass2\n\t\t\tcolor: blue";
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("__sb__.append(\"<style>.myClass1{color:red} .myClass2{color:blue}</style>\");", body(src(esp).getMethod("doRenderStyle")));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 
 		// including an external ESS file
 		esp = "head <- style<MyStyles>";
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertEquals("String path$8 = underscored(MyStyles.class.getName()).replace('.', '/');\n" +
 					 "__sb__.append(\"<link rel='stylesheet' type='text/css' href='/\").append(path$8).append(\".css' />\");",
 				body(src(esp).getMethod("doRenderStyle")));
 
 		// including an ESS file
 		esp = "head <- style<MyStyles>(inline: true)";
 		assertTrue(src(esp).hasMethod("hasStyle"));
 		assertEquals("return true;", body(src(esp).getMethod("hasStyle")));
 		assertTrue(src(esp).hasMethod("doRenderStyle"));
 		assertEquals("__sb__.append(\"<style>\");\nMyStyles myStyles$8 = new MyStyles();\nmyStyles$8.render(__sb__);\n__sb__.append(\"</style>\");", body(src(esp).getMethod("doRenderStyle")));
 	}
 	
 	@Test
 	public void testEmpty() throws Exception {
 		assertEquals(0, src("").getMethodCount());
 	}
 	
 	@Test
 	public void testHtmlDiv() throws Exception {
 		assertEquals("__sb__.append(\"<div id=\\\"myDiv\\\" class=\\\"class1 class2\\\" attr1=\\\"value1\\\">text</div>\");",
 				html("div#myDiv.class1.class2(attr1:value1) text"));
 	}
 	
 	@Test
 	public void testHtmlNestedDivs() throws Exception {
 		assertEquals("__sb__.append(\"<div><div><div></div></div></div>\");", html("div\n\tdiv\n\t\tdiv"));
 
 		// blank lines
 		assertEquals("__sb__.append(\"<div></div><div><div></div></div>\");", html("div\n\n\tdiv\n\t\tdiv"));
 		assertEquals("__sb__.append(\"<div><div><div></div></div></div>\");", html("div\n\t\n\tdiv\n\t\tdiv"));
 	}
 	
 	@Test
 	public void testHtmlDivWithQuotesInInnerHtml() throws Exception {
 		assertEquals("__sb__.append(\"<span><a href=\\\"/contact\\\">contact me</a></span>\");",
 				html("span <a href=\"/contact\">contact me</a>"));
 	}
 	
 	@Test
 	public void testHtmlDivWithStyle() throws Exception {
 		assertEquals("__sb__.append(\"<div style=\\\"color:red\\\">text</div>\");", html("div(style:\"color:red\") text"));
 	}
 	
 	@Test
 	public void testHtmlDivHidden() throws Exception {
 		assertEquals("__sb__.append(\"<div style=\\\"display:none\\\">text</div>\");", html("div|hide text"));
 		assertEquals("__sb__.append(\"<div id=\\\"myDiv\\\" style=\\\"display:none\\\">text</div>\");", html("div#myDiv|hide text"));
 		assertEquals("__sb__.append(\"<div style=\\\"color:red;display:none\\\">text</div>\");", html("div|hide(style:\"color:red\") text"));
 	}
 	
 	@Test
 	public void testHtmlSingleLineMultiWithStyles() throws Exception {
 		assertEquals("__sb__.append(\"<div style=\\\"float:left;width:200px\\\"><img height=\\\"200\\\" width=\\\"200\\\" src=\\\"/software/cdatetime.png\\\"></img></div>\");",
 				html("div(style:\"float:left;width:200px\") <- img(src:\"/software/cdatetime.png\", width:200, height:200)"));
 	}
 
 	@Test
 	public void testHtmlDivWithJavaPart() throws Exception {
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(h(var1)).append(\"Div\\\"></div>\");", html("div#{var1}Div"));
 		assertEquals("__sb__.append(\"<div class=\\\"a\").append(h(var2)).append(\"Class\\\"></div>\");", html("div.a{var2}Class"));
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(h(var1)).append(\"Div\\\" class=\\\"a\").append(h(var2)).append(\"Class\\\" attr1=\\\"v\").append(h(var3)).append(\"1\\\">t\").append(h(var4)).append(\"xt</div>\");",
 				html("div#{var1}Div.a{var2}Class(attr1:v{var3}1) t{var4}xt"));
 	}
 	
 	@Test
 	public void testJavaParts() throws Exception {
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(h(id)).append(\"\\\"></div>\");", html("div#{id}"));
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(h(nid)).append(\"\\\"></div>\");", html("div#{nid}"));
 		
 		// this is how it works right now... not really what we wanted, but is it a problem? (just use short-hand)
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(h(n(id))).append(\"\\\"></div>\");", html("div#{n(id)}"));
 		
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(n(id)).append(\"\\\"></div>\");", html("div#{n id}"));
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(h(id)).append(\"\\\"></div>\");", html("div#{h id}"));
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(j(id)).append(\"\\\"></div>\");", html("div#{j id}"));
 		assertEquals("__sb__.append(\"<div id=\\\"\").append(id).append(\"\\\"></div>\");", html("div#{r id}"));
 	}
 
 	@Test
 	public void testJavaParts_Escaped() throws Exception {
 		assertEquals("__sb__.append(\"<div>{id}</div>\");", html("div \\{id}"));
 	}
 
 	@Test
 	public void testJavaLines() throws Exception {
 		assertEquals("line1\nline2", html("- line1\n- line2"));
 		assertEquals("if(true) {\n\t__sb__.append(\"<div>hello</div>\");\n}", html("- if(true) {\n\t\tdiv hello\n- }"));
 		assertEquals("if(true) {\n\t__sb__.append(\"<div>hello</div>\");\n} else {\n\t__sb__.append(\"<div>goodbye</div>\");\n}", html("- if(true) {\n\t\tdiv hello\n- } else {\n\t\tdiv goodbye\n- }"));
 		assertEquals("__sb__.append(\"<div>\");\nif(true) {\n\t__sb__.append(\"<div>hello</div>\");\n} else {\n\t__sb__.append(\"<div>goodbye</div>\");\n}\n__sb__.append(\"<span></span></div>\");",
 				html("div\n\t- if(true) {\n\t\t\tdiv hello\n\t- } else {\n\t\t\tdiv goodbye\n\t- }\n\tspan"));
 	}
 	
 	@Test
 	public void testJavaIfStatementWithoutBraces() throws Exception {
 		assertEquals("if(true)\n\t__sb__.append(\"<div>hello</div>\");", html("-if(true)\n\t\tdiv hello"));
 		assertEquals("if(true)\n\t__sb__.append(\"<div>hello</div>\");\nelse\n\t__sb__.append(\"<div>goodbye</div>\");", html("-if(true)\n\t\tdiv hello\n-else\n\tdiv goodbye"));
 		assertEquals("if(true)\n\t__sb__.append(\"<div>hello</div>\");\n__sb__.append(\"<div>goodbye</div>\");", html("-if(true)\n\t\tdiv hello\ndiv goodbye"));
 		assertEquals("if(true)\n\t__sb__.append(\"<div>hello</div>\");\nelse\n\t__sb__.append(\"<div>goodbye</div>\");\n__sb__.append(\"<div>what?</div>\");", html("-if(true)\n\t\tdiv hello\n-else\n\tdiv goodbye\ndiv what?"));
 	}
 	
 	@Test
 	public void testJavaLinesAfterFirstElement() throws Exception {
 		assertEquals("if(true) {\n\t__sb__.append(\"<div>hello</div>\");\n} else {\n\t__sb__.append(\"<div>goodbye</div>\");\n}", html("head\n- if(true) {\n\t\tdiv hello\n- } else {\n\t\tdiv goodbye\n- }"));
 		assertEquals("if(true) {\n\t__sb__.append(\"<div>hello</div><div>bye</div>\");\n} else {\n\t__sb__.append(\"<div>goodbye</div>\");\n}", html("head\n- if(true) {\n\t\tdiv hello\n\t\tdiv bye\n- } else {\n\t\tdiv goodbye\n- }"));
 		String esp;
 //		esp = "head\n\tscript\n- if(greeting) {\n\t\tdiv hello world!\n- } else {\n\t\tdiv good bye...\n- }";
 		esp = "head\n- int i = 0;\n- int j = 0;";
 		assertEquals("int i = 0;\nint j = 0;", html(esp));
 	}
 	
 	@Test
 	public void testView() throws Exception {
 		assertEquals("yield(new MyView(), __sb__);", html("view<MyView>"));
 		assertEquals("yield(new MyView(arg1, arg2), __sb__);", html("view<MyView>(arg1,arg2)"));
 		assertEquals("yield(new MyView(arg1, arg2), __sb__);", html("view<MyView>( arg1, arg2 )"));
 		assertEquals("yield(new MyView(\"arg1\", arg2), __sb__);", html("view<MyView>(\"arg1\", arg2)"));
 		assertEquals("yield(new MyView(\"arg1\", arg2), __sb__);\nyield(new MyView(\"arg3\", arg4), __sb__);", html("view<MyView>(\"arg1\", arg2)\nview<MyView>(\"arg3\", arg4)"));
 		assertEquals("if(i == 0) {\n\tyield(new MyView(), __sb__);\n}", html("- if(i == 0) {\n\tview<MyView>\n- }"));
 	}
 	
 	@Test
 	public void testImg() throws Exception {
 		assertEquals("__sb__.append(\"<img src=\\\"/software/cdatetime.png\\\"></img>\");", html("img(\"/software/cdatetime.png\""));
 		assertEquals("__sb__.append(\"<img src=\\\"/software/cdatetime.png\\\"></img>\");", html("img(src:\"/software/cdatetime.png\""));
 		assertEquals("__sb__.append(\"<img height=\\\"200\\\" width=\\\"200\\\" src=\\\"/software/cdatetime.png\\\"></img>\");",
 				html("img(src:\"/software/cdatetime.png\", width:200, height:200)"));
 	}
 	
 	@Test
 	public void testLink() throws Exception {
 		assertEquals("__sb__.append(\"<a href=\\\"/home\\\">/home</a>\");", html("a(\"/home\")"));
 		assertEquals("__sb__.append(\"<a href=\\\"/home\\\"></a>\");", html("a(href:\"/home\")"));
 
 		assertEquals("__sb__.append(\"<a href=\\\"\").append(h(pathTo(something))).append(\"\\\">something</a>\");", html("a({pathTo(something)}) something"));
 
 		assertEquals("__sb__.append(\"<a href=\\\"http://mydomain.com/home\\\">http://mydomain.com/home</a>\");", html("a(\"http://mydomain.com/home\")"));
 		assertEquals("__sb__.append(\"<a href=\\\"http://mydomain.com/home\\\"></a>\");", html("a(href:\"http://mydomain.com/home\")"));
 
 		// one var - no modification to var1 (handy for create, update, and destroy)
 		assertEquals("__sb__.append(\"<a href=\\\"member\\\">New</a>\");", html("a(member, action: new) New"));
 		
 		// two vars - convert to pathTo form (convenience for html forms)
 		assertEquals("__sb__.append(\"<a href=\\\"\").append(pathTo(member, Action.showNew)).append(\"\\\">New Member</a>\");", html("a(member, showNew)"));
 		assertEquals("__sb__.append(\"<a href=\\\"\").append(pathTo(member, Action.showNew)).append(\"\\\">New</a>\");", html("a(member, showNew) New"));
 		
 		// unknown action for second variable - pass it through so we get a compiler error and can fix it
 		assertEquals("__sb__.append(\"<a href=\\\"\").append(pathTo(member, new)).append(\"\\\">New</a>\");", html("a(member, new) New"));
 	}
 
 	@Test
 	public void testMessages() throws Exception {
 		assertEquals("messagesBlock(__sb__);", html("messages"));
 	}
 	
 	@Test
 	public void testButton() throws Exception {
 		assertEquals("__sb__.append(\"<button></button>\");", html("button"));
 		assertEquals("__sb__.append(\"<button href=\\\"/home\\\">/home</button>\");", html("button(\"/home\""));
 		assertEquals("__sb__.append(\"<button href=\\\"\").append(pathTo(member, Action.show)).append(\"\\\">Show Member</button>\");", html("button(member, show)"));
 		assertEquals("__sb__.append(\"<button href=\\\"\").append(pathTo(Member.class, Action.showAll)).append(\"\\\">All Members</button>\");", html("button(Member.class, showAll)"));
 
 		assertEquals("__sb__.append(\"<button href=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" onclick=\\\"var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'POST';f.action = '\").append(pathTo(member, Action.create)).append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', 'null');m.setAttribute('value', 'null');f.appendChild(m);f.submit();return false;\\\">Create</button>\");",
 				html("button(member, create) Create"));
 
 		assertEquals("__sb__.append(\"<button href=\\\"\").append(pathTo(member, Action.update)).append(\"\\\" onclick=\\\"var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'POST';f.action = '\").append(pathTo(member, Action.update)).append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '_method');m.setAttribute('value', 'put');f.appendChild(m);var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', 'null');m.setAttribute('value', 'null');f.appendChild(m);f.submit();return false;\\\">Update</button>\");",
 				html("button(member, update) Update"));
 
 		assertEquals("__sb__.append(\"<button href=\\\"\").append(pathTo(member, Action.destroy)).append(\"\\\" onclick=\\\"var f = document.createElement('form');f.style.display = 'none';this.parentNode.appendChild(f);f.method = 'POST';f.action = '\").append(pathTo(member, Action.destroy)).append(\"';var m = document.createElement('input');m.setAttribute('type', 'hidden');m.setAttribute('name', '_method');m.setAttribute('value', 'delete');f.appendChild(m);f.submit();return false;\\\">Destroy</button>\");", 
 				html("button(member, destroy) Destroy"));
 	}
 
 	@Test
 	public void testDate() throws Exception {
 		assertEquals("__sb__.append(\"<span>\");\n__sb__.append(dateTimeTags(\"datetime\", \"MMM/dd/yyyy\"));\n__sb__.append(\"</span>\");", html("date"));
 		assertEquals("__sb__.append(\"<span>\");\n" +
 				"__sb__.append(dateTimeTags(\"datetime\", \"MMM/dd/yyyy\", new Date()));\n" +
 				"__sb__.append(\"</span>\");",
 			html("date(new Date())"));
 		assertEquals("__sb__.append(\"<span>\");\n" +
 				"__sb__.append(dateTimeTags(\"datetime\", \"dd/MM/yy\", new Date()));\n" +
 				"__sb__.append(\"</span>\");",
 			html("date(new Date(), format: \"dd/MM/yy\")"));
 		
 		assertEquals(
 				"String formModelName$0 = \"post\";\n" +
 				"__sb__.append(\"<form action=\\\"\").append(pathTo(post, post.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"POST\\\">\");\n" +
 				"if(!post.isNew()) {\n" +
 				"\t__sb__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
 				"}\n" +
 				"__sb__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(post.getId()).append(\"\\\" /><span id=\\\"\").append(formModelName$0).append(\"[publishedAt]\\\"\");\n" +
 				"if(post.hasErrors(\"publishedAt\")) {\n" +
 				"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 				"}\n" +
 				"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[publishedAt]\\\">\");\n" +
 				"__sb__.append(dateTimeTags(\"datetime\", \"MMM/dd/yyyy\", post.getPublishedAt()));\n" +
 				"__sb__.append(\"</span></form>\");",
 			html("form(post)\n\tdate(publishedAt)"));
 	}
 	
 	@Test
 	public void testFile() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"file\\\" />\");", html("file"));
 	}
 	
 	@Test
 	public void testFields() throws Exception {
 		assertEquals(0, src("fields").getMethodCount());
 		assertEquals(0, src("fields()").getMethodCount());
 		assertEquals("String formModelName$0 = \"member\";\n__sb__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" />\");", html("fields(member)"));
 		assertEquals(0, src("fields(arg1, arg2)").getMethodCount());
 	}
 	
 	@Test
 	public void testForm() throws Exception {
 		assertEquals("__sb__.append(\"<form></form>\");", html("form"));
 		assertEquals("__sb__.append(\"<form id=\\\"myForm\\\"></form>\");", html("form#myForm"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, member.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"POST\\\">\");\n" +
 						"if(!member.isNew()) {\n" +
 						"\t__sb__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
 						"}\n" +
 						"__sb__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /></form>\");", 
 				html("form(member)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /></form>\");", 
 				html("form(member, action: create)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.update)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /></form>\");", 
 				html("form(member, action: update)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.destroy)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"DELETE\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /></form>\");", 
 				html("form(member, action: delete)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /></form>\");", 
 				html("form(member, method: post)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.update)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /></form>\");", 
 				html("form(member, method: put)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.destroy)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"DELETE\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /></form>\");", 
 				html("form(member, method: delete)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\" enctype=\\\"multipart/form-data\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"file\\\" /></form>\");", 
 				html("form(member, create)\n\tfile"));
 
 		assertEquals("__sb__.append(\"<form action=\\\"/test\\\" method=\\\"post\\\"></form>\");",
 				html("form(action: \"/test\", method: post)"));
 		
 		assertEquals("__sb__.append(\"<form action=\\\"\").append(h(pathTo(\"sessions\"))).append(\"\\\" method=\\\"post\\\"></form>\");",
 				html("form(action: {pathTo(\"sessions\")}, method: post)"));
 		
 		// hasMany routing
 		assertEquals("Comment formModel$0 = new Comment();\n" +
 						"String formModelName$0 = \"comment\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(post, \"comments\")).append(\"\\\" method=\\\"POST\\\" enctype=\\\"multipart/form-data\\\"><input type=\\\"hidden\\\" name=\\\"id\\\" value=\\\"\").append(post.getId()).append(\"\\\" /><input type=\\\"file\\\" /></form>\");", 
 				html("form<Comment>(post, \"comments\")\n\tfile"));
 
 		assertEquals("Comment formModel$0 = new Comment();\n" +
 						"String formModelName$0 = \"comment\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(post, \"comments\")).append(\"\\\" method=\\\"POST\\\" enctype=\\\"multipart/form-data\\\"><input type=\\\"hidden\\\" name=\\\"id\\\" value=\\\"\").append(post.getId()).append(\"\\\" /><input type=\\\"file\\\" /></form>\");", 
 				html("form(post, \"comments\")\n\tfile"));
 	}
 
 	@Test
 	public void testErrors() throws Exception {
 		assertEquals("String formModelName$0 = \"member\";\n" +
 					"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" />\");\n" +
 					"errorsBlock(__sb__, member, null, null);\n" +
 					"__sb__.append(\"</form>\");", 
 			html("form(member, create)\n\terrors"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 					"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" />\");\n" +
 					"errorsBlock(__sb__, member, \"There errors\", \"Please fix them\");\n" +
 					"__sb__.append(\"</form>\");", 
 			html("form(member, create)\n\terrors(title: \"There errors\", message: \"Please fix them\""));
 	}
 	
 	@Test
 	public void testCheck() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"checkbox\\\" />\");", html("check"));
 		assertEquals("__sb__.append(\"<input type=\\\"checkbox\\\" name=\\\"color\\\" value=\\\"red\\\" />\");", html("check(name: color, value: red"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[active]\\\" value=\\\"false\\\" /><input type=\\\"checkbox\\\" id=\\\"\").append(formModelName$0).append(\"[active]\\\"\");\n" +
 						"if(member.hasErrors(\"active\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[active]\\\" value=\\\"true\\\"\");\nif(member.getActive()) {\n\t__sb__.append(\" CHECKED\");\n}\n__sb__.append(\" /></form>\");",
 				html("form(member, create)\n\tcheck(active)"));
 	}
 	
 	@Test
 	public void testHidden() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"hidden\\\" />\");", html("hidden"));
 		assertEquals("__sb__.append(\"<input type=\\\"hidden\\\" id=\\\"myField\\\" value=\\\"hello\\\" />\");", html("hidden#myField(value: hello)"));
 		assertEquals("__sb__.append(\"<input type=\\\"hidden\\\" id=\\\"myField\\\" name=\\\"say\\\" value=\\\"hello\\\" />\");", html("hidden#myField(name: say, value: hello)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"hidden\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\").append(f(member.getFirstName())).append(\"\\\" /></form>\");",
 				html("form(member, create)\n\thidden(firstName)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"hidden\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"bob\\\" /></form>\");",
 				html("form(member, create)\n\thidden(firstName, value: \"bob\")"));
 	}
 	
 	@Test
 	public void testInput() throws Exception {
 		assertEquals("__sb__.append(\"<input />\");", html("input"));
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" id=\\\"myField\\\" value=\\\"hello\\\" />\");", html("input#myField(type: text, value: hello)"));
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" id=\\\"myField\\\" name=\\\"say\\\" value=\\\"hello\\\" />\");", html("input#myField(type: text, name: say, value: hello)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\").append(f(member.getFirstName())).append(\"\\\" /></form>\");", 
 				html("form(member, create)\n\tinput(firstName)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\").append(f(member.getFirstName())).append(\"\\\" /></form>\");", 
 				html("form(member, create)\n\tinput(firstName, type: text)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"bob\\\" /></form>\");", 
 				html("form(member, create)\n\tinput(firstName, type: text, value: \"bob\")"));
 	}
 	
 	@Test
 	public void testLabel() throws Exception {
 		assertEquals("__sb__.append(\"<label></label>\");", html("label"));
 		assertEquals("__sb__.append(\"<label id=\\\"myField\\\" for=\\\"firstName\\\"></label>\");", html("label#myField(for: firstName)"));
 		assertEquals("__sb__.append(\"<label id=\\\"myField\\\" for=\\\"firstName\\\">First Name</label>\");", html("label#myField(for: firstName) First Name"));
 		assertEquals("if(true)\n\t__sb__.append(\"<label></label>\");", html("-if(true)\n\tlabel"));
 		
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><label\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" for=\\\"\").append(formModelName$0).append(\"[firstName]\\\">First Name\");\nif(member.isRequired(\"firstName\")) {\n\t__sb__.append(\"<span class=\\\"required\\\">*</span>\");\n}\n__sb__.append(\"</label></form>\");",
 				html("form(member, create)\n\tlabel(firstName)"));
 		
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><label\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" for=\\\"\").append(formModelName$0).append(\"[firstName]\\\">Member Name\");\nif(member.isRequired(\"firstName\")) {\n\t__sb__.append(\"<span class=\\\"required\\\">*</span>\");\n}\n__sb__.append(\"</label></form>\");",
 				html("form(member, create)\n\tlabel(firstName, text:\"Member Name\")"));
 		
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><label\");\n" +
 						"if(member.hasErrors(\"spouse\", \"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" for=\\\"\").append(formModelName$0).append(\"[spouse][firstName]\\\">First Name\");\nif(member.isRequired(\"spouse\", \"firstName\")) {\n\t__sb__.append(\"<span class=\\\"required\\\">*</span>\");\n}\n__sb__.append(\"</label></form>\");", 
 				html("form(member, create)\n\tlabel(spouse, firstName)"));
 	}
 	
 	@Test
 	public void testNumber() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" onkeypress=\\\"var k=window.event?event.keyCode:event.which;return !(k>31&&(k<48||k>57));\\\" />\");", html("number"));
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" onkeypress=\\\"alert('hello');var k=window.event?event.keyCode:event.which;return !(k>31&&(k<48||k>57));\\\" />\");", html("number(onkeypress: alert('hello'))"));
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" onkeypress=\\\"alert('hello');var k=window.event?event.keyCode:event.which;return !(k>31&&(k<48||k>57));\\\" />\");", html("number(onkeypress: alert('hello');)"));
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" id=\\\"myField\\\" value=\\\"10\\\" onkeypress=\\\"var k=window.event?event.keyCode:event.which;return !(k>31&&(k<48||k>57));\\\" />\");", html("number#myField(value:10)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[age]\\\"\");\n" +
 						"if(member.hasErrors(\"age\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[age]\\\" value=\\\"\").append(f(member.getAge())).append(\"\\\" onkeypress=\\\"var k=window.event?event.keyCode:event.which;return !(k>31&&(k<48||k>57));\\\" /></form>\");",
 				html("form(member, create)\n\tnumber(age)"));
 	}
 	
 	@Test
 	public void testPassword() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"password\\\" />\");", html("password"));
 		assertEquals("__sb__.append(\"<input type=\\\"password\\\" id=\\\"myPassword\\\" />\");", html("password#myPassword"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"password\\\" id=\\\"\").append(formModelName$0).append(\"[passkey]\\\"\");\n" +
 						"if(member.hasErrors(\"passkey\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[passkey]\\\" value=\\\"\").append(f(member.getPasskey())).append(\"\\\" /></form>\");",
 				html("form(member, create)\n\tpassword(passkey)"));
 	}
 	
 	@Test
 	public void testRadio() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"radio\\\" />\");", html("radio"));
 		assertEquals("__sb__.append(\"<input type=\\\"radio\\\" id=\\\"myRadio\\\" />\");", html("radio#myRadio"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"radio\\\" id=\\\"\").append(formModelName$0).append(\"[active]\\\"\");\n" +
 						"if(member.hasErrors(\"active\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[active]\\\" value=\\\"\").append(f(member.getActive())).append(\"\\\" /></form>\");",
 				html("form(member, create)\n\tradio(active)"));
 	}
 	
 	@Test
 	public void testReset() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"reset\\\" value=\\\"Reset\\\" />\");", html("reset"));
 	}
 	
 	@Test
 	public void testSelect() throws Exception {
 		assertEquals("__sb__.append(\"<select></select>\");", html("select"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><select id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\"></select></form>\");",
 				html("form(member, create)\n\tselect(firstName)"));
 	}
 	
 	@Test
 	public void testSelectOptions() throws Exception {
 		assertEquals("__sb__.append(\"<select></select>\");", html("select<-options"));
 		assertEquals("__sb__.append(\"<select>\");\n__sb__.append(optionTags(members));\n__sb__.append(\"</select>\");", html("select<-options(members)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><select id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\">\");\n" +
 						"__sb__.append(optionTags(members, member.getFirstName(), member.isRequired(\"firstName\")));\n" +
 						"__sb__.append(\"</select></form>\");",
 				html("form(member, create)\n\tselect(firstName)<-options(members)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><select id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\">\");\nObject selection$41 = member.getFirstName();\nfor(Member option : members) {\n\tboolean selected$41 = isEqual(option.getId(), selection$41);\n\t__sb__.append(\"<option value=\\\"\"+f(option.getId())+\"\\\" \"+(selected$41 ? \"selected >\" : \">\")+h(option.getNameLF())+\"</option>\");\n}\n__sb__.append(\"</select></form>\");",
 				html("form(member, create)\n\tselect(firstName)<-options<Member>(members, text:\"option.getNameLF()\", value:\"option.getId()\", sort:\"option.getNameLF()\")"));
 	}
 	
 	@Test
 	public void testSubmit() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"submit\\\" value=\\\"Submit\\\" />\");", html("submit"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, member.isNew() ? Action.create : Action.update)).append(\"\\\" method=\\\"POST\\\">\");\n" +
 						"if(!member.isNew()) {\n" +
 						"\t__sb__.append(\"<input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" />\");\n" +
 						"}\n" +
 						"__sb__.append(\"<input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"submit\\\" value=\\\"\").append(member.isNew() ? \"Create \" : \"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");", 
 				html("form(member)\n\tsubmit"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Create \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
 				html("form(member, create)\n\tsubmit"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.update)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
 				html("form(member, update)\n\tsubmit"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Create \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
 				html("form(member, action:create)\n\tsubmit"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.update)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
 				html("form(member, action:update)\n\tsubmit"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Create \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
 				html("form(member, method: post)\n\tsubmit"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.update)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"_method\\\" value=\\\"PUT\\\" /><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"submit\\\" value=\\\"Update \").append(titleize(formModelName$0)).append(\"\\\" /></form>\");",
 				html("form(member, method: put)\n\tsubmit"));
 	}
 	
 	@Test
 	public void testText() throws Exception {
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" />\");", html("text"));
 		assertEquals("__sb__.append(\"<input type=\\\"text\\\" id=\\\"myText\\\" />\");", html("text#myText"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\").append(f(member.getFirstName())).append(\"\\\" /></form>\");",
 				html("form(member, create)\n\ttext(firstName)"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\\\" /></form>\");",
 				html("form(member, create)\n\ttext(firstName, value:)"));
 
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><input type=\\\"text\\\" id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\" value=\\\"\").append(h(value)).append(\"\\\" /></form>\");",
 				html("form(member, create)\n\ttext(firstName, value: {value})"));
 	}
 	
 	@Test
 	public void testTextArea() throws Exception {
 		assertEquals("__sb__.append(\"<textarea></textarea>\");", html("textArea"));
 		assertEquals("__sb__.append(\"<textarea id=\\\"myText\\\"></textarea>\");", html("textArea#myText"));
 		assertEquals("String formModelName$0 = \"member\";\n" +
 						"__sb__.append(\"<form action=\\\"\").append(pathTo(member, Action.create)).append(\"\\\" method=\\\"POST\\\"><input type=\\\"hidden\\\" name=\\\"\").append(formModelName$0).append(\"[id]\\\" value=\\\"\").append(member.getId()).append(\"\\\" /><textarea id=\\\"\").append(formModelName$0).append(\"[firstName]\\\"\");\n" +
 						"if(member.hasErrors(\"firstName\")) {\n" +
 						"\t__sb__.append(\" class=\\\"fieldWithErrors\\\"\");\n" +
 						"}\n" +
 						"__sb__.append(\" name=\\\"\").append(formModelName$0).append(\"[firstName]\\\">\").append(f(member.getFirstName())).append(\"</textarea></form>\");",
 				html("form(member, create)\n\ttextArea(firstName)"));
 	}
 
 	@Test
 	public void testYield() throws Exception {
 		assertEquals("yield(__sb__);", html("yield"));
 		assertEquals("__sb__.append(\"<div>\");\nyield(__sb__);\n__sb__.append(\"</div>\");", html("div <- yield"));
 
 		assertEquals("yield(\"name\", __sb__);", html("yield(\"name\")"));
 		
 		assertEquals("yield(view, __sb__);", html("yield(view)"));
 
 		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
 				 "yield(view, test$0);\n" +
 				 "putContent(\"test\", test$0.toString());\n" +
 				 "test$0 = null;",
 			html("contentFor(\"test\")\n\tyield(view)"));
 
 		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
 				 "yield(test$0);\n" +
 				 "putContent(\"test\", test$0.toString());\n" +
 				 "test$0 = null;",
 			html("contentFor(\"test\")\n\tyield"));
 	}
 	
 	@Test
 	public void testInnerText() throws Exception {
 		// regular (can contain java parts)
 		assertEquals("__sb__.append(\"<div>start:</div>\");", html("div start:\n\t+end")); // space required
 		assertEquals("__sb__.append(\"<div>start:end</div>\");", html("div start:\n\t+ end"));
 		assertEquals("__sb__.append(\"<div>start : end</div>\");", html("div start :\n\t+  end"));
 		assertEquals("__sb__.append(\"<div>start:e\").append(h(n)).append(\"d</div>\");", html("div start:\n\t+ e{n}d"));
 		assertEquals("__sb__.append(\"<div>start:<code>src</code></div>\");", html("div start:\n\t+ <code>src</code>"));
 
 		// literal (no java parts)
 		assertEquals("__sb__.append(\"<div>start:</div>\");", html("div start:\n\t+=middle\n\t+=end")); // space required
 		assertEquals("__sb__.append(\"<div>start:middleend</div>\");", html("div start:\n\t+= middle\n\t+= end"));
 		assertEquals("__sb__.append(\"<div>start : middle end</div>\");", html("div start :\n\t+=  middle\n\t+=  end"));
 		assertEquals("__sb__.append(\"<div>start:e{n}d1e{n}d2</div>\");", html("div start:\n\t+= e{n}d1\n\t+= e{n}d2"));
 		assertEquals("__sb__.append(\"<div>start:</div>\");", html("div start:\n\t+= \n\t+= "));
 		assertEquals("__sb__.append(\"<div>start:<code>src</code></div>\");", html("div start:\n\t+= <code>src</code>"));
 		
 		// literal w/ word separators
 		assertEquals("__sb__.append(\"<div>start:</div>\");", html("div start:\n\t+wmiddle\n\t+wend")); // space required
 		assertEquals("__sb__.append(\"<div>start: middle end</div>\");", html("div start:\n\t+w middle\n\t+w end"));
 		assertEquals("__sb__.append(\"<div>start :  middle  end</div>\");", html("div start :\n\t+w  middle\n\t+w  end"));
 		assertEquals("__sb__.append(\"<div>start: e{n}d1 e{n}d2</div>\");", html("div start:\n\t+w e{n}d1\n\t+w e{n}d2"));
 		assertEquals("__sb__.append(\"<div>start:</div>\");", html("div start:\n\t+w \n\t+w "));
 		assertEquals("__sb__.append(\"<div>start: <code>src</code></div>\");", html("div start:\n\t+w <code>src</code>"));
 
 		// prompt (HTML escaped literal w/ line endings)
 		assertEquals("__sb__.append(\"<div>start:</div>\");", html("div start:\n\t+>middle\n\t+>end")); // space required
 		assertEquals("__sb__.append(\"<div>start:middle\\nend\\n</div>\");", html("div start:\n\t+> middle\n\t+> end"));
 		assertEquals("__sb__.append(\"<div>start : middle\\n end\\n</div>\");", html("div start :\n\t+>  middle\n\t+>  end"));
 		assertEquals("__sb__.append(\"<div>start:e{n}d1\\ne{n}d2\\n</div>\");", html("div start:\n\t+> e{n}d1\n\t+> e{n}d2"));
 		assertEquals("__sb__.append(\"<div>start:\\n\\n</div>\");", html("div start:\n\t+> \n\t+> "));
 		assertEquals("__sb__.append(\"<div>start:&lt;code&gt;src&lt;/code&gt;\\n</div>\");", html("div start:\n\t+> <code>src</code>"));
 	}
 	
 	@Test
 	public void testCapture() throws Exception {
 		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
 					 "test$0.append(\"<div>hello</div>\");\n" +
 					 "String test = test$0.toString();\n" +
 					 "test$0 = null;",
 				html("capture(test)\n\tdiv hello"));
 
 		assertEquals("__sb__.append(\"<div>1</div>\");\n" +
 					 "StringBuilder test$6 = new StringBuilder();\n" +
 					 "test$6.append(\"<div>2</div>\");\n" +
 					 "String test = test$6.toString();\n" +
 					 "test$6 = null;\n" +
 					 "__sb__.append(\"<div>3</div>\");",
 				html("div 1\ncapture(test)\n\tdiv 2\ndiv 3"));
 	}
 	
 	@Test
 	public void testContentFor() throws Exception {
 		assertEquals("StringBuilder test$0 = new StringBuilder();\n" +
 					 "test$0.append(\"<div>hello</div>\");\n" +
 					 "putContent(\"test\", test$0.toString());\n" +
 					 "test$0 = null;",
 				html("contentFor(\"test\")\n\tdiv hello"));
 
 		assertEquals("__sb__.append(\"<div>1</div>\");\n" +
 					 "StringBuilder test$6 = new StringBuilder();\n" +
 					 "test$6.append(\"<div>2</div>\");\n" +
 					 "putContent(\"test\", test$6.toString());\n" +
 					 "test$6 = null;\n" +
 					 "__sb__.append(\"<div>3</div>\");",
 				html("div 1\ncontentFor(\"test\")\n\tdiv 2\ndiv 3"));
 	}
 	
 }
