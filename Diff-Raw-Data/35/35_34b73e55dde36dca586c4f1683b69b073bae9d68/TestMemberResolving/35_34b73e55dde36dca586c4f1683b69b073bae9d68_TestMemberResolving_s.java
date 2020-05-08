 package ch.codedump.ooplss.test;
 
 import org.junit.Test;
 
 import ch.codedump.ooplss.symbolTable.exceptions.ClassNeededForMemberAccess;
 import ch.codedump.ooplss.symbolTable.exceptions.IllegalMemberAccessException;
 import ch.codedump.ooplss.symbolTable.exceptions.NotCallableException;
 import ch.codedump.ooplss.symbolTable.exceptions.UnknownDefinitionException;
 import ch.codedump.ooplss.symbolTable.exceptions.VariableIsAMethodException;
 import ch.codedump.ooplss.utils.ErrorHandler;
 
 
 public class TestMemberResolving extends OoplssTest {
 
 	
 	@Test (expected=IllegalMemberAccessException.class) 
 	public void testUndefinedMemberAccess() throws Exception {
 		String str = 	"class foo {\n" +
 						"	var x:foo;\n" +
 						"	def __construct() {\n" +
 						"		x.a;\n" +
 						"	}\n" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testRecursiveCalling() throws Exception {
 		String str = 	"class foo {\n" +
 						"	var x:bar;"+
 						"}\n" +
 						"class bar {\n" +
 						"	var y:foo;\n" + 
 						"	def __construct() {\n" +
 						"		y.x.y.x;\n" +
 						"	}\n" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=IllegalMemberAccessException.class)
 	public void testIllegalRecursion() throws Exception {
 		String str = 	"class foo {\n" +
 						"	var x:bar;\n" +
 						"}\n" +
 						"class bar {\n" +
 						"	var y:foo;\n" +
 						"	def __construct() {\n" +
 						"		y.x.x;\n" +
 						"	}\n" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testSelfAccess() throws Exception {
 		String str = 	"class foo{\n" +
 						"	var x:foo;\n" +
 						"	def __construct() {\n" +
 						"		self.x;\n" +
 						"	}\n" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testSuperTypes() throws Exception {
 		String str = 	"class a {\n" + 
 						"	var x:a;\n" + 
 						"}\n" +
 						"class b subtypeOf a {\n" +
 						"	def __construct() {\n" +
 						"		x;\n" +
 						"	}\n" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test(expected=UnknownDefinitionException.class)
 	public void testClassVarAccess() throws Exception {
 		String str = 	"class a {\n" + 
 						"	var x:a;\n" +
 						"}\n" +
 						"class b {\n" +
 						"	def __construct() {\n" +
 						"		a;\n" +
 						"	}\n" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testClassMemberAccess() throws Exception {
 		String str = 	"class a {\n" +
 						"	var x:Int;\n" +
 						"	def __construct() {\n" +
 						"		x;\n" +
 						"	}\n" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testClassMethodAccess() throws Exception {
 		String str = 	"class a {\n" +
						"	def x():Int;\n" +
 						"	def __construct() {\n" +
 						"		x();\n" +
 						"	}\n" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=IllegalMemberAccessException.class)
 	public void testInvalidFunctionResolving() throws Exception {
 		String str = 	"class a {" +
 						"	var x:b;" +
 						"	def call():Void {}" +
 						"	def foo():Void {" +
 						"		x.call();" +
 						"	}" +
 						"}" +
 						"class b {}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=ClassNeededForMemberAccess.class)
 	public void testInvalidBuildInTypeMemberResolving() throws Exception {
 		String str = 	"class a {" +
 						"	def blah():Void {" +
 						"		var x:Int;" +
 						"		x.y;" +
 						"	}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=ClassNeededForMemberAccess.class)
 	public void testInvalidBuildInTypeMemberResolving2() throws Exception {
 		String str = 	"class a {" +
 						"	def blubb():Void {}" +
 						"	def blah():Void {" +
 						"		var x:Int;" +
 						"		x.blubb();" +
 						"	}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testInheritedMethodCall() throws Exception {
 		String str = 	"class foo {" +
						"	def blah():Int;" +
 						"}" +
 						"class bar subtypeOf foo {}" +
 						"class third {" +
 						"	var x:bar;" +
 						"	def __construct() {" +
 						"		x.blah();" +
 						"	}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test
 	public void testComplicatedMemberResolving() throws Exception {
 		// m().foo.bar();
 		
 		String str = 	"class Foo {" +
 						"	var foo:Foo;" +
 						"	def m():Foo {" +
 						"		return new Foo();" +
 						"	}" +
 						"	def bar():Void {}" +
 						"	def run():Void {" +
 						"		m().foo.bar();" +
 						"	}" +
 						"}";
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=VariableIsAMethodException.class)
 	public void testMethodAccessAsVar() throws Exception {
 		String str =	"class bar {\n" + 
 						"    var x:bar;\n" + 
 						"    def foo():Void {}\n" + 
 						"    def blubb():Void {\n" + 
 						"        x.foo; // variable access, but is a method\n" + 
 						"   }\n" + 
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test (expected=NotCallableException.class)
 	public void testVarAccessAsMethod() throws Exception {
 		String str =	"class bar {\n" + 
 						"    var x:bar;\n" + 
 						"    def foo():Void {}\n" + 
 						"    def blubb():Void {\n" + 
 						"        x.x(); // method call but is a variable\n" + 
 						"   }\n" + 
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test 
 	public void testSuperClassVariable() throws Exception {
 		String str = 	"class foo {" +
 						"	var x:Int;" +
 						"}" +
 						"class bar subclassOf foo {" +
 						"	def blubb():Void {" +
 						"		x;" +
 						"	}" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test 
 	public void testSuperClassMethod() throws Exception {
 		String str = 	"class foo {" +
						"	def x():Int;" +
 						"}" +
 						"class bar subclassOf foo {" +
 						"	def blubb():Void {" +
 						"		x();" +
 						"	}" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 	
 	@Test 
 	public void testSelfSuperClassVariable() throws Exception {
 		String str = 	"class foo {" +
 						"	var x:Int;" +
 						"}" +
 						"class bar subclassOf foo {" +
 						"	def blubb():Void {" +
 						"		self.x;" +
 						"	}" +
 						"}";
 		this.symTab.disableStandaloneCheck();
 		this.createRef(str);
 		ErrorHandler.getInstance().throwException();
 	}
 }
 
 
 
 
 
 
 
 
 
