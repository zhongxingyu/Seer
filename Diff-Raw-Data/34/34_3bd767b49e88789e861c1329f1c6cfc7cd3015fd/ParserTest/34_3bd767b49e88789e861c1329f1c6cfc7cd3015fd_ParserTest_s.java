 package veeju.tests.parser;
 
 import java.util.*;
 import java.io.*;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.SafeConstructor;
 import difflib.*;
 import veeju.parser.*;
 import veeju.forms.*;
 import veeju.forms.dump.*;
 
 public final class ParserTest {
     @Test
     public void testParse() {
         Form[] forms = {
             Symbol.create("a"),
             Symbol.create("b")
         };
         Program expected = new Program(forms);
         assertEquals(expected, Parser.parse("a; b"));
         assertEquals(expected, Parser.parse("a ;b"));
         assertEquals(expected, Parser.parse("a ;; b"));
         assertEquals(expected, Parser.parse(" ; a ;; b"));
         assertEquals(expected, Parser.parse("a\r\nb"));
         assertEquals(expected, Parser.parse("#!a;b;c\na\nb"));
         assertEquals(expected, Parser.parse("#!/usr/bin/env veeju\na;b"));
         forms = new Form[] {
             Symbol.create("\uac00"),
             Symbol.create("\ub098")
         };
         expected = new Program(forms);
         assertEquals(expected, Parser.parse("\uac00;\ub098"));
         try {
             assertEquals(expected,
                          Parser.parse("\uac00;\ub098".getBytes("utf-8")));
         } catch (UnsupportedEncodingException e) {
             fail(e.getMessage());
         }
         try {
             String code = "#!/bin/veeju -eutf-8\n\uac00;\ub098";
             assertEquals(expected, Parser.parse(code.getBytes("utf-8")));
         } catch (UnsupportedEncodingException e) {
             fail(e.getMessage());
         }
         try {
             String code = "#!/bin/veeju -eeuc-kr\n\uac00;\ub098";
             assertEquals(expected, Parser.parse(code.getBytes("euc-kr")));
         } catch (UnsupportedEncodingException e) {
             fail(e.getMessage());
         }
     }
 
     @Test
     public void testParseProgram() {
         final Form[] forms = {
             Symbol.create("a"),
             Symbol.create("b")
         };
         final Program expected = new Program(forms);
         ParsingResult result;
         result = Parser.parseProgram("a; b");
         assertEquals(4, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseProgram("a ;b");
         assertEquals(4, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseProgram("a ;; b");
         assertEquals(6, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseProgram(" ; a ;; b");
         assertEquals(9, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseProgram("a\r\nb");
         assertEquals(4, result.getOffset());
         assertEquals(expected, result.getNode());
     }
 
     @Test
     public void testParseForm_symbol() {
         ParsingResult result;
         result = Parser.parseForm("id");
         assertEquals(2, result.getOffset());
         assertEquals(Symbol.create("id"), result.getNode());
     }
 
     @Test
     public void testParseForm_quote() {
         ParsingResult result;
         result = Parser.parseForm(":(id)");
         assertEquals(5, result.getOffset());
         assertEquals(new Quote(Symbol.create("id")), result.getNode());
         result = Parser.parseForm(":id");
         assertEquals(3, result.getOffset());
         assertEquals(new Quote(Symbol.create("id")), result.getNode());
     }
 
     @Test
     public void testParseForm_definition() {
         ParsingResult result;
         result = Parser.parseForm("a = b");
         assertEquals(5, result.getOffset());
         assertEquals(new Definition(Symbol.create("a"), Symbol.create("b")),
                      result.getNode());
         result = Parser.parseForm("c:=d");
         assertEquals(4, result.getOffset());
         assertEquals(new Definition(Symbol.create("c"),
                                     Symbol.create("d"),
                                     true),
                      result.getNode());
     }
 
     @Test
     public void testParseForm_literal() {
         ParsingResult result;
         result = Parser.parseForm("123");
         assertEquals(3, result.getOffset());
         assertEquals(new NumberLiteral(123), result.getNode());
         result = Parser.parseAtomicForm("\"Avishai Cohen Trio\"");
         assertEquals(20, result.getOffset());
         assertEquals(new StringLiteral("Avishai Cohen Trio"), result.getNode());
     }
 
     @Test
     public void testParseForm_attribute() {
         ParsingResult result;
         Attribute expected;
         expected = new Attribute(Symbol.create("rcvr"), Symbol.create("attr"));
         result = Parser.parseForm("rcvr.attr");
         assertEquals(9, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseForm("(rcvr .attr)");
         assertEquals(12, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseForm("(rcvr). attr");
         assertEquals(12, result.getOffset());
         assertEquals(expected, result.getNode());
         expected = new Attribute(new Quote(Symbol.create("q")),
                                  Symbol.create("attr"));
         result = Parser.parseForm(":q . attr");
         assertEquals(9, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseForm(":(q) .attr");
         assertEquals(10, result.getOffset());
         assertEquals(expected, result.getNode());
         expected = new Attribute(expected, Symbol.create("b"));
         result = Parser.parseForm(":q.attr.b");
         assertEquals(9, result.getOffset());
         assertEquals(expected, result.getNode());
         expected = new Attribute(Symbol.create("a"), new Index());
         result = Parser.parseForm("a[]");
         assertEquals(3, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseForm("a.[]");
         assertEquals(4, result.getOffset());
         assertEquals(expected, result.getNode());
         expected = new Attribute(Symbol.create("a"), new Block());
         result = Parser.parseForm("a{}");
         assertEquals(3, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseForm("a.{}");
         assertEquals(4, result.getOffset());
         assertEquals(expected, result.getNode());
     }
 
     @Test
     public void testParseForm_call() {
         ParsingResult result;
         result = Parser.parseForm("func()");
         assertEquals(6, result.getOffset());
         assertEquals(new Call(Symbol.create("func")), result.getNode());
         Call expected = new Call(Symbol.create("f"),
                                  new ArgumentList(Symbol.create("x"),
                                                   Symbol.create("y")));
         result = Parser.parseForm("f(x, y)");
         assertEquals(7, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseForm("f: x, y");
         assertEquals(7, result.getOffset());
         assertEquals(expected, result.getNode());
         result = Parser.parseForm("a b c");
         assertEquals(5, result.getOffset());
         assertEquals(new Call(new Attribute(Symbol.create("a"),
                                             Symbol.create("b")),
                               new ArgumentList(Symbol.create("c"))),
                      result.getNode());
     }
 
     @Test
     public void testParseForm_invalidSyntax() {
         try {
             Parser.parseForm("(sym");
             fail();
         } catch (InvalidSyntaxException e) {
             assertEquals(4, e.getOffset());
         }
         try {
             Parser.parseForm("((sym)");
             fail();
         } catch (InvalidSyntaxException e) {
             assertEquals(6, e.getOffset());
         }
         try {
             Parser.parseForm("()");
             fail();
         } catch (InvalidSyntaxException e) {
             assertEquals(1, e.getOffset());
         }
         try {
             Parser.parseForm("abc .");
             fail();
         } catch (InvalidSyntaxException e) {
             assertEquals(5, e.getOffset());
         }
     }
 
     @Test
     public void testParseAtomicForm_symbol() {
         ParsingResult result;
         result = Parser.parseAtomicForm("id");
         assertEquals(2, result.getOffset());
         assertEquals(Symbol.create("id"), result.getNode());
         result = Parser.parseAtomicForm("(id)");
         assertEquals(4, result.getOffset());
         assertEquals(Symbol.create("id"), result.getNode());
         result = Parser.parseAtomicForm("(((id)))");
         assertEquals(8, result.getOffset());
         assertEquals(Symbol.create("id"), result.getNode());
     }
 
     @Test
     public void testParseAtomicForm_numberLiteral() {
         ParsingResult result;
         result = Parser.parseAtomicForm("123");
         assertEquals(3, result.getOffset());
         assertEquals(new NumberLiteral(123), result.getNode());
         result = Parser.parseAtomicForm("-3.14");
         assertEquals(5, result.getOffset());
         assertEquals(new NumberLiteral(-3.14), result.getNode());
         result = Parser.parseAtomicForm("10e-4");
         assertEquals(5, result.getOffset());
         assertEquals(new NumberLiteral(0.001), result.getNode());
         result = Parser.parseAtomicForm("-3.141e+4");
         assertEquals(9, result.getOffset());
         assertEquals(new NumberLiteral(-31410.0), result.getNode());
     }
 
     @Test
     public void testParseAtomicForm_stringLiteral() {
         ParsingResult result;
         result = Parser.parseAtomicForm("\"Avishai Cohen Trio\"");
         assertEquals(20, result.getOffset());
         assertEquals(new StringLiteral("Avishai Cohen Trio"), result.getNode());
         result = Parser.parseAtomicForm("\"Avishai\\tCohen\\nTrio\n\"");
         assertEquals(23, result.getOffset());
         assertEquals(new StringLiteral("Avishai\tCohen\nTrio\n"),
                      result.getNode());
         result = Parser.parseAtomicForm("'Avishai Cohen Trio'");
         assertEquals(20, result.getOffset());
         assertEquals(new StringLiteral("Avishai Cohen Trio"), result.getNode());
         result = Parser.parseAtomicForm("'Avishai\\tCohen\\nTrio\n'");
         assertEquals(23, result.getOffset());
         assertEquals(new StringLiteral("Avishai\\tCohen\\nTrio\n"),
                      result.getNode());
         result = Parser.parseAtomicForm("p\"with prefix\"");
         assertEquals(14, result.getOffset());
         assertEquals(new StringLiteral("with prefix", 'p'), result.getNode());
         result = Parser.parseAtomicForm("p'with prefix'");
         assertEquals(14, result.getOffset());
         assertEquals(new StringLiteral("with prefix", 'p'), result.getNode());
         result = Parser.parseAtomicForm("\"\"\"Avishai\\tCohen\\nTrio\n\"\"\"");
         assertEquals(27, result.getOffset());
         assertEquals(new StringLiteral("Avishai\tCohen\nTrio\n"),
                      result.getNode());
         result = Parser.parseAtomicForm("'''Avishai Cohen Trio'''");
         assertEquals(24, result.getOffset());
         assertEquals(new StringLiteral("Avishai Cohen Trio"), result.getNode());
         result = Parser.parseAtomicForm("'''Avishai\\tCohen\\nTrio\n'''");
         assertEquals(27, result.getOffset());
         assertEquals(new StringLiteral("Avishai\\tCohen\\nTrio\n"),
                      result.getNode());
         result = Parser.parseAtomicForm("p\"\"\"with prefix\"\"\"");
         assertEquals(18, result.getOffset());
         assertEquals(new StringLiteral("with prefix", 'p'), result.getNode());
         result = Parser.parseAtomicForm("p'''with prefix'''");
         assertEquals(18, result.getOffset());
         assertEquals(new StringLiteral("with prefix", 'p'), result.getNode());
     }
 
     @Test
     public void testParseAtomicForm_quote() {
         ParsingResult result;
         result = Parser.parseAtomicForm(":(id)");
         assertEquals(5, result.getOffset());
         assertEquals(new Quote(Symbol.create("id")), result.getNode());
         result = Parser.parseAtomicForm(":(( id) )");
         assertEquals(9, result.getOffset());
         assertEquals(new Quote(Symbol.create("id")), result.getNode());
         result = Parser.parseAtomicForm(":id");
         assertEquals(3, result.getOffset());
         assertEquals(new Quote(Symbol.create("id")), result.getNode());
     }
 
     @Test
     public void testParseAtomicForm_block() {
         ParsingResult result;
         result = Parser.parseAtomicForm("{ }");
         assertEquals(3, result.getOffset());
         assertEquals(new Block(new Program()), result.getNode());
         result = Parser.parseAtomicForm("{ a; b\nc(123)\nd}");
         assertEquals(16, result.getOffset());
         final ArgumentList.Entry[] args = {
             new ArgumentList.Entry(new NumberLiteral(123))
         };
         final Form[] forms = {
             Symbol.create("a"), Symbol.create("b"),
             new Call(Symbol.create("c"), new ArgumentList(args)),
             Symbol.create("d")
         };
         assertEquals(new Block(new Program(forms)), result.getNode());
     }
 
     @Test
     public void testParseArguments_empty() {
         ParsingResult result;
         result = Parser.parseArguments("", true);
         assertEquals(0, result.getOffset());
         assertEquals(new ArgumentList(), result.getNode());
         result = Parser.parseArguments("  ", true);
         assertEquals(2, result.getOffset());
         assertEquals(new ArgumentList(), result.getNode());
     }
 
     @Test
     public void testParseArguments_positionalArguments() {
         ParsingResult result;
         result = Parser.parseArguments("arg", true);
         assertEquals(3, result.getOffset());
         assertEquals(new ArgumentList(Symbol.create("arg")),
                      result.getNode());
         result = Parser.parseArguments("a ,b,c", true);
         assertEquals(6, result.getOffset());
         assertEquals(new ArgumentList(Symbol.create("a"),
                                       Symbol.create("b"),
                                       Symbol.create("c")),
                      result.getNode());
         result = Parser.parseArguments(":abc, :def", true);
         assertEquals(10, result.getOffset());
         assertEquals(new ArgumentList(new Quote(Symbol.create("abc")),
                                       new Quote(Symbol.create("def"))),
                      result.getNode());
     }
 
     @Test
     public void testParseArguments_namedArguments() {
         ParsingResult result;
         ArgumentList.Entry[] args = new ArgumentList.Entry[1];
         result = Parser.parseArguments("abc: def", true);
         assertEquals(8, result.getOffset());
         args[0] = new ArgumentList.Entry(Symbol.create("abc"),
                                          Symbol.create("def"));
         assertEquals(new ArgumentList(args), result.getNode());
         result = Parser.parseArguments("a::b", true);
         assertEquals(4, result.getOffset());
         args[0] = new ArgumentList.Entry(Symbol.create("a"),
                                          new Quote(Symbol.create("b")));
         assertEquals(new ArgumentList(args), result.getNode());
         result = Parser.parseArguments("a ::b", true);
         assertEquals(5, result.getOffset());
         assertEquals(new ArgumentList(args), result.getNode());
         result = Parser.parseArguments("a : :b", true);
         assertEquals(6, result.getOffset());
         assertEquals(new ArgumentList(args), result.getNode());
     }
 
     @Test
     public void testParseArguments_mixedArguments() {
         ParsingResult result = Parser.parseArguments("a ,b: c, d,e:f", true);
         assertEquals(14, result.getOffset());
         final ArgumentList.Entry[] args = {
             new ArgumentList.Entry(Symbol.create("a")),
             new ArgumentList.Entry(Symbol.create("b"),
                                    Symbol.create("c")),
             new ArgumentList.Entry(Symbol.create("d")),
             new ArgumentList.Entry(Symbol.create("e"),
                                    Symbol.create("f"))
         };
         final ArgumentList expected = new ArgumentList(args);
         assertEquals(expected, result.getNode());
     }
 
     @Test
     public void testParseArguments_complexArguments() {
         final ParsingResult result = Parser.parseArguments(
             "x.add(y), x: xs, y: ys, x.cmp(y)",
             true
         );
         assertEquals(32, result.getOffset());
         final ArgumentList args = (ArgumentList) result.getNode();
         assertNull(args.get(0).getKey());
         assertEquals(new Call(new Attribute(Symbol.create("x"),
                                             Symbol.create("add")),
                               new ArgumentList(Symbol.create("y"))),
                      args.get(0).getValue());
         assertEquals(Symbol.create("x"), args.get(1).getKey());
         assertEquals(Symbol.create("xs"), args.get(1).getValue());
         assertEquals(Symbol.create("y"), args.get(2).getKey());
         assertEquals(Symbol.create("ys"), args.get(2).getValue());
         assertNull(args.get(3).getKey());
         assertEquals(new Call(new Attribute(Symbol.create("x"),
                                             Symbol.create("cmp")),
                               new ArgumentList(Symbol.create("y"))),
                      args.get(3).getValue());
     }
 
     @Test
     public void testUnescapeStringLiteral() {
         assertEquals("Avishai Cohen Trio",
                      Parser.unescapeStringLiteral("Avishai Cohen Trio"));
         assertEquals("Avishai\tCohen\nTrio\n",
                      Parser.unescapeStringLiteral("Avishai\\tCohen\\nTrio\n"));
         assertEquals("Hong Minhee",
                      Parser.unescapeStringLiteral("Hong\\x20Minhee"));
         assertEquals("Hong Minhee",
                      Parser.unescapeStringLiteral("Hong\\u0020Minhee"));
         assertEquals("Hong Minhee",
                      Parser.unescapeStringLiteral("Hong\\U00000020Minhee"));
     }
 
     private static String color(final String text, final String color) {
         if (System.console() == null) return text;
         return "\033[" + color + "m" + text + "\033[0m";
     }
 
     public static void main(String[] args) {
         if (args.length < 2) {
             System.err.println("usage: java veeju.tests.parser.ParserTest " +
                                " src-dir ast-dir");
             return;
         }
         final File srcDir = new File(args[0]),
                    astDir = new File(args[1]);
         final File[] dirs = {srcDir, astDir};
         for (int i = 0; i < dirs.length; ++i) {
             if (!dirs[i].exists()) {
                 System.err.print(dirs[i].toString());
                 System.err.println(" doesn't exist.");
             } else if (!dirs[i].isDirectory()) {
                 System.err.print(dirs[i].toString());
                 System.err.println(" is not a directory.");
             }
         }
         final File[] srcFiles = srcDir.listFiles(new FilenameFilter() {
             public boolean accept(final File dir, final String name) {
                 return name.endsWith(".vu");
             }
         });
         final File[] testFiles = astDir.listFiles(new FilenameFilter() {
             public boolean accept(final File dir, String name) {
                 if (!name.endsWith(".yaml") && !name.endsWith(".yml")) {
                     return false;
                 }
                 String srcName;
                 name = name.replaceFirst("\\.ya?ml$", "");
                 for (File srcFile : srcFiles) {
                     srcName = srcFile.getName().replaceFirst("\\.vu$", "");
                     if (srcName.equals(name)) return true;
                 }
                 return false;
             }
         });
         FileInputStream testInputStream, srcInputStream;
         final Yaml yaml = new Yaml(new SafeConstructor());
         for (File testFile : testFiles) {
             final String testName = testFile.getName()
                                             .replaceFirst("\\.ya?ml$", "");
             final File srcFile = new File(srcDir, testName + ".vu");
             try {
                 srcInputStream = new FileInputStream(srcFile);
                 testInputStream = new FileInputStream(testFile);
             } catch (FileNotFoundException e) {
                 System.err.println(color("TEST", "31") + ": " + testName);
                 e.printStackTrace();
                 continue;
             }
             Program program;
             @SuppressWarnings("unchecked")
             final Map<String, Object> ast
                 = (Map<String, Object>) yaml.load(testInputStream);
             try {
                 program = Parser.parse(srcInputStream);
                 srcInputStream.close();
             } catch (IOException e) {
                 continue;
             }
             @SuppressWarnings("unchecked")
             final Map<String, Object> dmp = MapDump.dump(program);
             if (dmp.equals(ast)) {
                 System.out.println(color("PASS", "32") + ": " + testName);
             } else {
                 System.err.println(color("FAIL", "31") + ": " + testName);
                 final String actual = yaml.dump(dmp), expected = yaml.dump(ast);
                 List<String> actualLines, expectedLines;
                 actualLines = Arrays.asList(actual.split("\\r?\\n"));
                 expectedLines = Arrays.asList(expected.split("\\r?\\n"));
                 final Patch patch = DiffUtils.diff(expectedLines, actualLines);
                 List<String> diff;
                 diff = DiffUtils.generateUnifiedDiff(expected, actual,
                                                      expectedLines, patch, 3);
                 System.err.println(color("=========", "33"));
                 System.err.println(color("YAML Diff", "33"));
                 System.err.println(color("---------", "33"));
                 boolean start = false;
                 for (String diffLine : diff) {
                     if (!start) {
                         if (diffLine.substring(0, 2).equals("@@")) {
                             start = true;
                         } else {
                             continue;
                         }
                     }
                     if ('+' == diffLine.charAt(0)) {
                         diffLine = color(diffLine, "42");
                     } else if ('-' == diffLine.charAt(0)) {
                         diffLine = color(diffLine, "41");
                     }
                     System.err.println(diffLine);
                 }
                 System.err.println(color("=====================", "33"));
                 System.err.println(color("Serialized Actual AST", "33"));
                 System.err.println(color("---------------------", "33"));
                 System.err.println(MapLoad.load(dmp).generateCodeString());
                 System.err.println(color("=======================", "33"));
                 System.err.println(color("Serialized Expected AST", "33"));
                 System.err.println(color("-----------------------", "33"));
                 System.err.println(MapLoad.load(ast).generateCodeString());
             }
         }
     }
 }
 
