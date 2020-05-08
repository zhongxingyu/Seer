 /*
  * Thibaut Colar Mar 24, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import net.colar.netbeans.fan.test.mock.DummyAstRootNode;
 import java.util.List;
 import net.colar.netbeans.fan.FanParserTask;
 import net.colar.netbeans.fan.indexer.model.FanSlot;
 import net.colar.netbeans.fan.parboiled.AstKind;
 import net.colar.netbeans.fan.parboiled.AstNode;
 import net.colar.netbeans.fan.parboiled.FanLexAstUtils;
 import net.colar.netbeans.fan.parboiled.FantomParser;
 import net.colar.netbeans.fan.parboiled.pred.NodeKindPredicate;
 import net.colar.netbeans.fan.scope.FanAstScopeVarBase;
 import net.colar.netbeans.fan.scope.FanLocalScopeVar;
 import net.colar.netbeans.fan.types.FanResolvedListType;
 import net.colar.netbeans.fan.types.FanResolvedNullType;
 import net.colar.netbeans.fan.types.FanResolvedType;
 import net.jot.testing.JOTTester;
 import org.parboiled.Parboiled;
 import org.parboiled.RecoveringParseRunner;
 import org.parboiled.common.StringUtils;
 import org.parboiled.support.ParsingResult;
 
 /**
  * Test type resolution
  * @author thibautc
  */
 public class FantomTypesTest extends FantomCSLTest
 {
 	// List some fq imports we will use in the tests, so we can add them to the node's scope (sys types implied)
 
 	static String[] testUsings =
 	{
 		"web::Weblet", "fwt::Button", "fwt::Widget", 
 		"web::WebUtil", "java.lang.Runtime"
 	};
 	// will put slots from those types in scope as well (as inherited)
 	static String[] testSlots =
 	{
 		"sys::Obj", "testCompiler::CompilerTest"
 	};
 
 	@Override
 	public void cslTest() throws Throwable
 	{
 		// Dummy task & rootNode to pass along
 		FanParserTask task = new FanParserTask(null);
 		DummyAstRootNode node = new DummyAstRootNode(task);
 		addUsingsToNode(node, testUsings);
 		addTypeSlotsToNode(node, task, testSlots);
 
 		
 		// Testing type signature stuff
 		FanResolvedType t = FanResolvedType.makeFromTypeSig(node, "sys::Str");
 		JOTTester.checkIf("Type sig 1", t.getQualifiedType().equals("sys::Str") && !t.isNullable(), t.toString());
 		JOTTester.checkIf("Type sig 1a", t.asNullableContext(true).isNullable() && t.asStaticContext(true).isStaticContext(), t.toString());
 		t = FanResolvedType.makeFromTypeSig(node, "sys::Str?");
 		JOTTester.checkIf("Type sig 2", t.getQualifiedType().equals("sys::Str") && t.isNullable(), t.toString());
 		t = FanResolvedType.makeFromTypeSig(node, "sys::Str[]");
 		JOTTester.checkIf("Type sig 3", (t instanceof FanResolvedListType) && t.toTypeSig(true).equals("sys::Str[]"), t.toString());
 		t = FanResolvedType.makeFromTypeSig(node, "Int");
 		check(t, "sys::Int", false, true);
 		t = FanResolvedType.makeFromTypeSig(node, "Int?");
 		check(t, "sys::Int?", true, true);
 
 		// checking expressions
 		checkExpr("5", "sys::Int", false, false);
 		AstNode resultNode = checkExpr("null", "null::null?", true, false);
 		JOTTester.checkIf("checking null type", resultNode.getType() instanceof FanResolvedNullType);
 
 		checkExpr("0x3bca.not.and(0xffff)", "sys::Int", false, false);
 		checkExpr("0x3bc7.not.and(0xffff)", "sys::Int", false, false);
 		checkExpr("0..<20", "sys::Range", false, false);
 		checkExpr("(0..<20).toList", "sys::Int[]", false, false);
 		checkExpr("\"abc\".split[0]", "sys::Str", false, false);
 		checkExpr("\"abc\".split[0..2]", "sys::Str[]", false, false);
 		checkExpr("Obj", "sys::Obj", false, true);
 		checkExpr("Obj()", "sys::Obj", false, false);
 		checkExpr("Str.defVal", "sys::Str", false, false);
 		checkExpr("\"abc\".isEmpty", "sys::Bool", false, false);
 		checkExpr("[3,4]", "sys::Int[]", false, false);
 		checkExpr("[3, 2f ,4]", "sys::Num[]", false, false);
 
 		// lists
 		checkExpr("[3,null,4]", "sys::Int?[]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Obj?[]");
 		check(t, "sys::Obj?[]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Obj[]?");
 		check(t, "sys::Obj[]?", true, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Str[][]?");
 		check(t, "sys::Str[][]?", true, false);
 		JOTTester.checkIf("List of List type",
 			(t instanceof FanResolvedListType)
 			&& ((FanResolvedListType) t).getItemType().toTypeSig(true).equals("sys::Str[]"), t.toString());
 
 		// maps
 		t = FanResolvedType.makeFromTypeSig(node, "[sys::Str : sys::Str]");
 		check(t, "[sys::Str:sys::Str]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "sys::Str : sys::Str[]");
 		check(t, "[sys::Str:sys::Str[]]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "[Str : Str]");
 		check(t, "[sys::Str:sys::Str]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Str:Str");
 		check(t, "[sys::Str:sys::Str]", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "Str:[Str:Int]");
 		check(t, "[sys::Str:[sys::Str:sys::Int]]", false, false);
 		checkExpr("[\"a\":[\"a\"]].keys.sort", null, false, false);
 
 		//functions:
 		t = FanResolvedType.makeFromTypeSig(node, "|->|");
 		check(t, "|sys::Void->sys::Void|", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "|->Int|");
 		check(t, "|sys::Void->sys::Int|", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "|Int|");
 		check(t, "|sys::Int->sys::Void|", false, false);
 		t = FanResolvedType.makeFromTypeSig(node, "|Int,Str,Str:Int->Str:Bool|");
 		check(t, "|sys::Int,sys::Str,[sys::Str:sys::Int]->[sys::Str:sys::Bool]|", false, false);
 		checkExpr("[Str:Str].keys.random", null, true, false);
 
 		// closures / itblocks
 		resultNode = checkExpr("|str| { echo(str) }", "|sys::Obj?->sys::Void|", false, false);
 		AstNode block = FanLexAstUtils.getFirstChildRecursive(resultNode, new NodeKindPredicate(AstKind.AST_BLOCK));
 		resultNode = checkExpr("|Int i| { echo(i) }", "|sys::Int->sys::Void|", false, false);
 		block = FanLexAstUtils.getFirstChildRecursive(resultNode, new NodeKindPredicate(AstKind.AST_BLOCK));
 		checkExpr("Button {text=\"aa\"; it.text=\"aa\"}", null, false, false);
 		checkExpr("Button.make {text=\"aa\"; it.text=\"aa\"}", null, false, false);
 		checkExpr("Button.make() {text=\"aa\"; it.text=\"aa\"}", null, false, false);
 		checkExpr("Str[,].with { fill(\"x\", 3) }", null, false, false);
 		checkExpr("Str[,] { fill(\"x\", 3) }", null, false, false);
 		checkExpr("[\"a\", \"b\"].each { echo(toStr); echo(it.toStr) }", null, false, false);
 		checkExpr("[5:3].each |Str val| { echo(val)}", null, false, false);
 		checkExpr("Actor(ActorPool()) |msg| {echo(msg)}", null, false, false);
 		checkExpr("Actor.make(ActorPool()) |msg| {echo(msg)}", null, false, false);
 		checkExpr("Buf() {capacity=100}", null, false, false);
 		checkExpr("Button(\"meh\") {text=100}", null, false, false);
 		FanResolvedType.forcedThisType = "testCompiler::CompilerTest";
 		checkExpr("compile(\"class Foo {}\") {it.isScript = false}", null, false, false); //compile method in CompilerTest
 		FanResolvedType.forcedThisType = null;
 		checkExpr("WebUtil.parseMultiPart(\"abc\").null, null) |h, in| {}", null, false, false);
 		
 		// enums
 		checkExpr("Month.vals", null, false, false);
 		checkExpr("Month.fromStr(\"abc)\"", null, false, false);
 		checkExpr("Month.mar", null, false, false);
 		checkExpr("Month.mar.name", null, false, false);
 		// Facet
 		checkExpr("Transient.defVal", null, false, false);
 
 		// java
 		checkExpr("Runtime.getRuntime().gc", null, false, false);
 
 		// others
 		checkExpr("Process().in", null, true,false);
 		checkExpr("Obj.super.hash",null,false,false);
 		FanResolvedType.forcedThisType = "sys::TimeoutErr";
 		checkExpr("Err.super.toStr)", null, false, false);
 		FanResolvedType.forcedThisType = null;
 		checkExpr("Obj.trap?.toStr", null, true, false);
 		checkExpr("Obj.trap?.toStr?.toStr", null, true, false);
 		checkExpr("500ms.ticks.abs", null, false, false);
 
 		// Testing isCompatible()
 		JOTTester.checkIf("Compatibility of Enum vs Obj", mkt("sys::Enum", node).isTypeCompatible(mkt("sys::Obj", node)));
 		JOTTester.checkIf("Compatibility of Obj vs Obj", mkt("sys::Obj", node).isTypeCompatible(mkt("sys::Obj", node)));
 		JOTTester.checkIf("Un-Compatibility of mixin vs Enum", !mkt("web::Weblet", node).isTypeCompatible(mkt("sys::Enum", node)));
 		JOTTester.checkIf("Compatibility of Button vs Obj", mkt("fwt::Button", node).isTypeCompatible(mkt("sys::Obj", node)));
 		JOTTester.checkIf("Compatibility of Button vs widget", mkt("fwt::Button", node).isTypeCompatible(mkt("fwt::Widget", node)));
 
 		// Testing getParent()
 		t = mkt("sys::Actor", node);
 		FanResolvedType p = t.getParentType();
 		JOTTester.checkIf("Actor parent is Obj", p.getDbType().getQualifiedName().equals("sys::Obj"), p.toString());
 		t = mkt("sys::Weekday", node);
 		p = t.getParentType();
 		JOTTester.checkIf("Weekday parent is Enum", p.getDbType().getQualifiedName().equals("sys::Enum"), p.toString());
 		t = mkt("sys::Enum", node);
 		p = t.getParentType();
 		JOTTester.checkIf("Enum parent is Obj", p.getDbType().getQualifiedName().equals("sys::Obj"), p.toString());
 		t = mkt("web::Weblet", node);
 		p = t.getParentType();
 		JOTTester.checkIf("Mixin parent is Obj", p.getDbType().getQualifiedName().equals("sys::Obj"), p.toString());
 
 		// Still failing items:
 		checkExpr("3>2 ? `gg` : null ", "sys::Uri[]?", true, false); // ternary result
 		checkExpr("Regex<|(a*)(a+)|>", "sys::Regex", false, false); // DSL call
 		checkExpr("Str<|{\"foo\": 1234}|>.in", "sys::InStream", false, false); // DSL too
 	}
 
 	private FanResolvedType mkt(String qType, AstNode node)
 	{
 		return FanResolvedType.makeFromDbType(node, qType);
 	}
 
 	/**
 	 * Chceck a type against expected values
 	 * @param t
 	 * @param typeSig
 	 * @param isNullable
 	 * @param isStatic
 	 * @throws Exception
 	 */
 	private void check(FanResolvedType t, String typeSig, boolean isNullable, boolean isStatic) throws Exception
 	{
 		check(t.getAsTypedType(), t, typeSig, isNullable, isStatic);
 	}
 
 	/**
 	 * if typeSig is null, not checked
 	 * @param infoStr
 	 * @param t
 	 * @param typeSig
 	 * @param isNullable
 	 * @param isStatic
 	 * @throws Exception
 	 */
 	private void check(String infoStr, FanResolvedType t, String typeSig, boolean isNullable, boolean isStatic) throws Exception
 	{
 		boolean good = t != null
 			&& t.isResolved()
 			&& (typeSig == null || t.toTypeSig(true).equals(typeSig))
 			&& t.isNullable() == isNullable
 			&& t.isStaticContext() == isStatic;
 		String expected = "got: " + t + ", expected: " + typeSig + ", ST:" + isStatic + ", NL:" + isNullable;
 		JOTTester.checkIf("Checking " + infoStr, good, expected);
 	}
 
 	/**
 	 * Parse the expression into a type and check it against expected values
 	 * @param expr
 	 * @param typeSig
 	 * @param isNullable
 	 * @param isStatic
 	 * @throws Exception
 	 */
 	private AstNode checkExpr(String expr, String typeSig, boolean isNullable, boolean isStatic) throws Exception
 	{
 		FanParserTask task = new FanParserTask(null);
 		FantomParser parser = Parboiled.createParser(FantomParser.class, task);
 		ParsingResult<AstNode> result = RecoveringParseRunner.run(parser.testExpr(), expr);
 		JOTTester.checkIf("Checking for parsing errors: " + expr, !result.hasErrors(), StringUtils.join(result.parseErrors, "\n"));
 		AstNode node = result.parseTreeRoot.getValue();
 		task.prune(node, task.getRootLabel(node));
 		addUsingsToNode(node, testUsings);
 		addTypeSlotsToNode(node, task, testSlots);
 		task.parseVars(node, null);
 		//System.out.println("Node for "+expr+" : ");
 		//FanLexAstUtils.dumpTree(node, 0);
 		JOTTester.checkIf("Checking for Semantic errors: " + expr, task.getDiagnostics().isEmpty(), StringUtils.join(task.getDiagnostics(), "\n"));
 		FanResolvedType t = node.getType();
 		check(expr, t, typeSig, isNullable, isStatic);
 		return node;
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomTypesTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 
 	public static void addUsingsToNode(AstNode node, String[] usings)
 	{
 		for (String using : usings)
 		{
 			String name=null;
 			if(using.indexOf(":")>=0)
 				name = using.substring(using.indexOf("::") + 2);
 			else
 			{
 				name = using.substring(using.lastIndexOf(".")+1);
 			}
			FanParserTask.addUsingToNode(name, using, node);
 		}
 	}
 
 	public static void addTypeSlotsToNode(AstNode node, FanParserTask task, String[] types)
 	{
 		for (String type : types)
 		{
 			FanResolvedType baseType = FanResolvedType.makeFromDbType(node, type);
 			List<FanSlot> slots = FanSlot.getAllSlotsForType(type, false, task);
 			for (FanSlot slot : slots)
 			{
 				FanAstScopeVarBase newVar = new FanLocalScopeVar(node, baseType, slot, slot.getName());
 				node.addScopeVar(newVar, true);
 			}
 		}
 	}
 }
