 /*
  * Thibaut Colar Feb 5, 2010
  */
 package net.colar.netbeans.fan.test;
 
 import net.colar.netbeans.fan.parboiled.FantomParser;
 import net.jot.testing.JOTTestable;
 import net.jot.testing.JOTTester;
 import org.parboiled.Parboiled;
 import org.parboiled.common.StringUtils;
 import org.parboiled.support.ParseTreeUtils;
 import org.parboiled.support.ParsingResult;
 
 /**
  * Test new parboiled based parser
  * @author thibautc
  */
 public class FantomParserTest implements JOTTestable
 {
 
 	public void jotTest() throws Throwable
 	{
 		FantomParser parser = Parboiled.createParser(FantomParser.class);
 
 		ParsingResult<Object> result = null;
 		//spacing
 		result = parser.parse(parser.spacing(), "  \r \n\r\t	\n \u000c ");
 		testNodeName("Spacing", result, "spacing");
 		result = parser.parse(parser.spacing(), "   \n//fdsfdsfs\n	\t");
 		testNodeName("Spacing2", result, "spacing");
 		result = parser.parse(parser.spacing(), "   \n/*fdsfdsfs*/	\t");
 		testNodeName("Spacing3", result, "spacing");
 		result = parser.parse(parser.spacing(), "/* blah\nblu"); // unterminated ml comment
 		testNodeName("Spacing4", result, "spacing", "/* blah\n"); // should match until \n
 		// litterals
 		result = parser.parse(parser.char_(), "'a'");
 		testNodeName("Char", result, "char_", "'a'");
 		result = parser.parse(parser.char_(), "'\\n'");
 		testNodeName("Char2", result, "char_");
 		result = parser.parse(parser.char_(), "'\\u5F9a'");
 		testNodeName("Char3", result, "char_");
 		result = parser.parse(parser.uri(), "`http://www.google.com/`");
 		testNodeName("Uri", result, "uri");
 		result = parser.parse(parser.strs(), "\"aa\"");
 		testNodeName("String", result, "strs");
 		result = parser.parse(parser.strs(), "\"aa\\r\\nbb\"");
 		testNodeName("String 2", result, "strs", "\"aa\\r\\nbb\"");
 		result = parser.parse(parser.strs(), "\"a \\n $toto \\$thingy \\t \\u5F39\"");
 		testNodeName("String 3", result, "strs");
 		result = parser.parse(parser.strs(), "\"\"\"a\"\"\"");
 		testNodeName("3Quotes Str 1", result, "strs", "\"\"\"a\"\"\"");
 		result = parser.parse(parser.strs(), "\"\"\"\"hello\" \"\"\"");
 		testNodeName("3Quotes Str 2", result, "strs", "\"\"\"\"hello\" \"\"\"");
 		result = parser.parse(parser.strs(), "\"\"\"\"hello\"\\n\"\" \"\"\"");
 		testNodeName("3Quotes Str 3", result, "strs", "\"\"\"\"hello\"\\n\"\" \"\"\"");
 		result = parser.parse(parser.dsl(), "Str<| dfdsfsd \\ \\n |  > dsfsd |>");
 		testNodeName("DSL", result, "dsl",  "Str<| dfdsfsd \\ \\n |  > dsfsd |>");
 		// numbers
 		result = parser.parse(parser.digit(), "7");
 		testNodeName("Digit", result, "digit");
 		result = parser.parse(parser.hexDigit(), "7");
 		testNodeName("HexDigit", result, "hexDigit");
 		result = parser.parse(parser.hexDigit(), "D");
 		testNodeName("HexDigit2", result, "hexDigit");
 		result = parser.parse(parser.hexDigit(), "d");
 		testNodeName("HexDigit3", result, "hexDigit");
 		result = parser.parse(parser.number(), "0X_5__A6_F");
 		testNodeName("Number 1", result, "number");
 		result = parser.parse(parser.number(), "0xCAFE_BABE");
 		testNodeName("Number 2", result, "number");
 		result = parser.parse(parser.number(), "1_000_000");
 		testNodeName("Number 3", result, "number");
 		result = parser.parse(parser.number(), "-89_039");
 		testNodeName("Number 4", result, "number");
 		result = parser.parse(parser.number(), "-.25");
 		testNodeName("Number f 1", result, "number");
 		result = parser.parse(parser.number(), "3.0f");
 		testNodeName("Number f 2", result, "number");
 		result = parser.parse(parser.number(), "3f");
 		testNodeName("Number f 3", result, "number");
 		result = parser.parse(parser.number(), "123_456.0f");
 		testNodeName("Number f 4", result, "number");
 		result = parser.parse(parser.number(), "3e6f");
 		testNodeName("Number f 5", result, "number");
 		result = parser.parse(parser.number(), "0.2e+6f");
 		testNodeName("Number f 6", result, "number");
 		result = parser.parse(parser.number(), "1_2.3_7e-5_6f");
 		testNodeName("Number f 7", result, "number");
 		result = parser.parse(parser.number(), "4d");
 		testNodeName("Number d 1", result, "number");
 		result = parser.parse(parser.number(), "4.00");
 		testNodeName("Number d 2", result, "number");
 		result = parser.parse(parser.number(), "123_456d");
 		testNodeName("Number d 3", result, "number");
 		result = parser.parse(parser.number(), "0.2e+6D");
 		testNodeName("Number d 4", result, "number");
 		result = parser.parse(parser.number(), "1_2.3_7e-5_6");
 		testNodeName("Number d 5", result, "number");
 		// base items
 		result = parser.parse(parser.doc(), "**\n");
 		testNodeName("Doc", result, "doc");
 		result = parser.parse(parser.doc(), "**\n** some doc\n");
 		testNodeName("Multi Doc", result, "doc");
 		result = parser.parse(parser.id(), "ab_FG09__");
 		testNodeName("ID", result, "id");
 		// types
 		result = parser.parse(parser.typeAndOrId(), "ab_FG09__");
 		testNodeName("TypeAndOrId", result, "typeAndOrId");
 		result = parser.parse(parser.typeAndOrId(), "ab_FG09__ ab_FG09__");
 		testNodeName("TypeAndOrId 2", result, "typeAndOrId", "ab_FG09__ ab_FG09__");
 		result = parser.parse(parser.simpleType(), "myThingy");
 		testNodeName("SimpleType", result, "simpleType");
 		result = parser.parse(parser.simpleType(), "Email::email");
 		testNodeName("SimpleType2", result, "simpleType");
 		result = parser.parse(parser.mapType(), "[Sys::Str:Int]");
 		testNodeName("MapType2", result, "mapType");
 		result = parser.parse(parser.simpleMapType(), "Str:Int");
 		testNodeName("MapType3", result, "simpleMapType");
 		result = parser.parse(parser.simpleMapType(), "[Str:Int]:Str");
 		testNodeName("MapType4", result, "simpleMapType");
 		result = parser.parse(parser.mapType(), "[Str?[]?:Int?]");
 		testNodeName("MapType5", result, "mapType");
 		result = parser.parse(parser.mapType(), "[|Str->Int|:Int?]");
 		testNodeName("MapType6", result, "mapType");
 		result = parser.parse(parser.mapType(), "[Sys::Str:Sys::Int]");
 		testNodeName("MapType7", result, "mapType");
 		result = parser.parse(parser.formals(), "myThingy, Str myOtherThingy, [Str:Int] one5More");
 		testNodeName("Formals", result, "formals");
 		result = parser.parse(parser.funcType(), "|->|");
 		testNodeName("FuncType1", result, "funcType");
 		result = parser.parse(parser.funcType(), "|Sys::Str->|");
 		testNodeName("FuncType2", result, "funcType");
 		result = parser.parse(parser.funcType(), "|->Int|");
 		testNodeName("FuncType3", result, "funcType");
 		result = parser.parse(parser.funcType(), "|Str?[]? s, Int[]? i->[Str:Int?]?|");
 		testNodeName("FuncType4", result, "funcType");
 		result = parser.parse(parser.typeList(), "myThingy");
 		testNodeName("TypeList", result, "typeList");
 		result = parser.parse(parser.typeList(), "myThingy, myOtherThingy, one5More");
 		testNodeName("TypeList2", result, "typeList");
 		result = parser.parse(parser.type(), "toto");
 		testNodeName("Type", result, "type");
 		result = parser.parse(parser.type(), "toto?");
 		testNodeName("Type2", result, "type");
 		result = parser.parse(parser.type(), "toto[]?");
 		testNodeName("Type3", result, "type");
 		result = parser.parse(parser.type(), "Str:email");
 		testNodeName("Type4", result, "type");
 		result = parser.parse(parser.type(), "[Str?[]?:Int]");
 		testNodeName("Type5", result, "type");
 		result = parser.parse(parser.type(), "|Str s, Int->Int|");
 		testNodeName("Type6", result, "type");
 		result = parser.parse(parser.type(), "Sys::Str?[]?");
 		testNodeName("Type7", result, "type");
 
 		// expressions
 		result = parser.parse(parser.mapPair(), "doThis:doThat");
 		testNodeName("Map pair", result, "mapPair", "doThis:doThat");
 		result = parser.parse(parser.mapItems(), "doThis:doThat, a:b, c:d");
 		testNodeName("Map items", result, "mapItems", "doThis:doThat, a:b, c:d");
 		result = parser.parse(parser.map(), "[:]");
 		testNodeName("Map 1", result, "map", "[:]");
 		result = parser.parse(parser.map(), "[5:9]");
 		testNodeName("Map 2", result, "map", "[5:9]");
 		result = parser.parse(parser.map(), "[5:9,7:12]");
 		testNodeName("Map 3", result, "map", "[5:9,7:12]");
 		result = parser.parse(parser.map(), "Int:Str[5:9,7:12]");
 		testNodeName("Map 4", result, "map", "Int:Str[5:9,7:12]");
 		result = parser.parse(parser.map(), "[Int:Str][5:9,7:12]");
 		testNodeName("Map 5", result, "map", "[Int:Str][5:9,7:12]");
 		result = parser.parse(parser.closure(), "|->|{}");
 		testNodeName("Closure 1", result, "closure", "|->|{}");
 		result = parser.parse(parser.closure(), "|->|{i=5}");
 		testNodeName("Closure 2", result, "closure", "|->|{i=5}");
 		result = parser.parse(parser.call(), "toto()");
 		testNodeName("Call1", result, "call", "toto()");
 		result = parser.parse(parser.call(), "toto(a, b)");
 		testNodeName("Call2", result, "call", "toto(a, b)");
 		result = parser.parse(parser.call(), "toto|->|{i=5}");
 		testNodeName("Call3", result, "call", "toto|->|{i=5}");
 		result = parser.parse(parser.call(), "toto(a, b)|->|{i=5}");
 		testNodeName("Call4", result, "call", "toto(a, b)|->|{i=5}");
 		result = parser.parse(parser.idExpr(), "toto");
 		testNodeName("IdExpr1", result, "idExpr", "toto");//id
 		result = parser.parse(parser.idExpr(), "*toto");
 		testNodeName("IdExpr2", result, "idExpr", "*toto");//field
 		result = parser.parse(parser.idExpr(), "blah()");
 		testNodeName("IdExpr3", result, "idExpr", "blah()");//call
 		result = parser.parse(parser.staticCall(), "toto.blah()");
 		testNodeName("StaticCall1", result, "staticCall", "toto.blah()");
 		result = parser.parse(parser.staticCall(), "toto.toto");
 		testNodeName("StaticCall2", result, "staticCall", "toto.toto");
 		result = parser.parse(parser.expr(), "toto.doit");
 		testNodeName("expr1", result, "expr", "toto.doit");
 		result = parser.parse(parser.expr(), "i=5");
 		testNodeName("expr2", result, "expr", "i=5");
		result = parser.parse(parser.expr(), "Int i:=5");
		testNodeName("expr3", result, "expr", "Int i:=5");
 
 		result = parser.parse(parser.localDef(), "a:=23");
 		testNodeName("localDef1", result, "localDef", "a:=23");
 		result = parser.parse(parser.localDef(), "Int a:=23");
 		testNodeName("localDef2", result, "localDef", "Int a:=23");
		result = parser.parse(parser.localDef(), "var");
		testNodeName("localDef4", result, "localDef", "var");
 		result = parser.parse(parser.localDef(), "Int var");
 		testNodeName("localDef5", result, "localDef", "Int var");
 
 		// TODO: many more expr
 
 		// facets
 		result = parser.parse(parser.facet(), "@Str");
 		testNodeName("facet1", result, "facet", "@Str");
 		result = parser.parse(parser.facet(), "@Sys::Str");
 		testNodeName("facet2", result, "facet", "@Sys::Str");
 		result = parser.parse(parser.facet(), "@Str{id=toto.doit}");
 		testNodeName("facet3", result, "facet", "@Str{id=toto.doit}");
 		result = parser.parse(parser.facet(), "@Str{id=toto.doit;name='c'\nstatus=25}");
 		testNodeName("facet4", result, "facet", "@Str{id=toto.doit;name='c'\nstatus=25}");
 
 		// stmt
 		result = parser.parse(parser.stmt(), "toto.doit");
 		testNodeName("stmt1", result, "stmt", "toto.doit");
 		result = parser.parse(parser.stmt(), "i=5");
 		testNodeName("Stmt2", result, "stmt", "i=5");
 		result = parser.parse(parser.stmt(), "Int i:=5");
 		testNodeName("Stmt3", result, "stmt", "Int i:=5");
 		result = parser.parse(parser.stmt(), "Int i:=5+3");
 		testNodeName("Stmt4", result, "stmt", "Int i:=5+3");
 		result = parser.parse(parser.block(), "i=5");
 		testNodeName("Block", result, "block", "i=5");
 		result = parser.parse(parser.block(), "{i=5}");
 		testNodeName("Block2", result, "block", "{i=5}");
 		result = parser.parse(parser.block(), "{i=5;k=7\n\nj=6}");
 		testNodeName("Block3", result, "block", "{i=5;k=7\n\nj=6}");
 
 		// Slot Def
 		result = parser.parse(parser.fieldAccessor(), "{get{i=23}}");
 		testNodeName("FiledAccesor1", result, "fieldAccessor", "{get{i=23}}");
 		result = parser.parse(parser.fieldAccessor(), "{get{i=23}private set{}}");
 		testNodeName("FiledAccesor2", result, "fieldAccessor", "{get{i=23}private set{}}");
 		result = parser.parse(parser.fieldDef(), "Int a");
 		testNodeName("FiledDef1", result, "fieldDef", "Int a");
 		result = parser.parse(parser.fieldDef(), "private const Int a");
 		testNodeName("FiledDef2", result, "fieldDef", "private const Int a");
 		result = parser.parse(parser.fieldDef(), "static override readonly Int a:=23");
 		testNodeName("FiledDef3", result, "fieldDef", "static override readonly Int a:=23");
 		result = parser.parse(parser.fieldDef(), "Int a{get{i=23}\nprivate set{}\n}");
 		testNodeName("FiledDef4", result, "fieldDef", "Int a{get{i=23}\nprivate set{}\n}");
 		result = parser.parse(parser.fieldDef(), "Int a:=23{get{i=23}\nprivate set{}\n}");
 		testNodeName("FiledDef5", result, "fieldDef", "Int a:=23{get{i=23}\nprivate set{}\n}");
 		result = parser.parse(parser.methodDef(), "private static Void doit(Str a, Int b){}");
 		testNodeName("MethodDef1", result, "methodDef", "private static Void doit(Str a, Int b){}");
		result = parser.parse(parser.methodDef(), "Void doit(Str s){i=5}");
		testNodeName("MethodDef2", result, "methodDef", "Void doit(Str s){i=5}");
		result = parser.parse(parser.methodDef(), "Void doit(Str s){Int i=5\n\n\tj=7}");
		testNodeName("MethodDef3", result, "methodDef", "Void doit(Str s){Int i=5\n\n\tj=7}");
 
 		// Type Def
 
 		// Comp. unit
 
 		// TODO: eos() noNL() ?
 		
 
 
 		/*DataInputStream dis = new DataInputStream(new FileInputStream("/home/thibautc/NetBeansProjects/Fan/test/parser_test/test.fan"));
 		byte[] buffer = new byte[dis.available()];
 		dis.readFully(buffer);
 		String testInput = new String(buffer);
 
 		ParsingResult<Object> result = parser.parse(parser.compilationUnit(), testInput);
 
 		System.out.println(testInput + " = " + result.parseTreeRoot.getValue() + '\n');
 		System.out.println("Parse Tree:\n" + ParseTreeUtils.printNodeTree(result) + '\n');
 
 		if (result.hasErrors())
 		{
 		System.out.println(StringUtils.join(result.parseErrors, "---\n"));
 		}
 		JOTTester.checkIf("Errors? ", ! result.hasErrors());*/
 	}
 
 	public void testNodeName(String label, ParsingResult<Object> result, String nodeName) throws Exception
 	{
 		testNodeName(label, result, nodeName, null);
 	}
 
 	public void testNodeName(String label, ParsingResult<Object> result, String nodeName, String value) throws Exception
 	{
 		if (result.hasErrors())
 		{
 			System.out.println(StringUtils.join(result.parseErrors, "---\n"));
 		}
 		JOTTester.checkIf(label + " - parsing success", !result.hasErrors());
 		JOTTester.checkIf(label + " - root not null", result.parseTreeRoot != null);
 		JOTTester.checkIf(label + " - check name(" + nodeName + ")", result.parseTreeRoot.getLabel().equals(nodeName), result.parseTreeRoot.getLabel());
 		if(value!=null)
 		{
 			String txt = ParseTreeUtils.getNodeText(result.parseTreeRoot, result.inputBuffer);
 			JOTTester.checkIf(label+" - check value", txt.equals(value), "'"+value+"' VS '"+txt+"'");
 		}
 	}
 
 	public static void main(String[] args)
 	{
 		try
 		{
 			JOTTester.singleTest(new FantomParserTest(), false);
 		} catch (Throwable t)
 		{
 			t.printStackTrace();
 		}
 	}
 }
