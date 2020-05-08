 package org.ita.neutrino.tests.junit3parser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 
 import java.util.List;
 
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.JavaModelException;
 import org.ita.neutrino.abstracttestparser.TestParserException;
 import org.ita.neutrino.astparser.ASTParser;
 import org.ita.neutrino.codeparser.CodeElement;
 import org.ita.neutrino.codeparser.ParserException;
 import org.ita.neutrino.junit3parser.JUnit3Parser;
 import org.ita.neutrino.junit3parser.JUnitAction;
 import org.ita.neutrino.junit3parser.JUnitAssertion;
 import org.ita.neutrino.junit3parser.JUnitTestBattery;
 import org.ita.neutrino.junit3parser.JUnitTestMethod;
 import org.ita.neutrino.junit3parser.JUnitTestSuite;
 import org.ita.neutrino.junitgenericparser.JUnitTestStatement;
 import org.ita.neutrino.tests.RefactoringAbstractTests;
 import org.junit.Test;
 
 public class JUnit3ParserTests extends RefactoringAbstractTests {
 	
 	private ASTParser codeParser;
 	private JUnitTestBattery battery;
 	private JUnitTestSuite suite;
 
 	private void prepareTests() throws JavaModelException, ParserException {
 		StringBuilder mockClassCode = new StringBuilder();
 
 		mockClassCode.append("package org.ita.neutrino.testfiles.junit3parsertests;\n");
 		mockClassCode.append("\n");
 		mockClassCode.append("import junit.framework.TestCase;\n");
 		mockClassCode.append("\n");
 		mockClassCode.append("public class MockClass extends TestCase {\n");
 		mockClassCode.append("\n");
 		mockClassCode.append("    @SuppressWarnings(\"unused\")\n");
 		mockClassCode.append("    private Object fixture0 = new Object();\n");
 		mockClassCode.append("    @SuppressWarnings(\"unused\")\n");
 		mockClassCode.append("    private Object fixture1 = new Object();\n");
 		mockClassCode.append("    \n");
		mockClassCode.append("    public void setup() {\n");
 		mockClassCode.append("        action();\n");
 		mockClassCode.append("    }\n");
 		mockClassCode.append("    \n");
 		mockClassCode.append("    public void testNothing0() {\n");
 		mockClassCode.append("        action();\n");
 		mockClassCode.append("        \n");
 		mockClassCode.append("        assertTrue(\"Comment\", true);\n");
 		mockClassCode.append("    }\n");
 		mockClassCode.append("\n");
 		mockClassCode.append("\n");
 		mockClassCode.append("    public void testNothing1() {\n");
 		mockClassCode.append("        action();\n");
 		mockClassCode.append("        \n");
 		mockClassCode.append("        assertTrue(\"Comment\", true);\n");
 		mockClassCode.append("    }\n");
 		mockClassCode.append("\n");
 		mockClassCode.append("    \n");
 		mockClassCode.append("    private void action() {\n");
 		mockClassCode.append("        \n");
 		mockClassCode.append("    }\n");
 		mockClassCode.append("    \n");
 		mockClassCode.append("    public void tearDown() {\n");
 		mockClassCode.append("    }\n");
 		mockClassCode.append("}\n");
 
 		ICompilationUnit mockCompilationUnit = createSourceFile("org.ita.neutrino.testfiles.junit3parsertests", "MockClass.java", mockClassCode);
 		
 		codeParser = new ASTParser();
 		
 		codeParser.setActiveCompilationUnit(mockCompilationUnit);
 		codeParser.setCompilationUnits(new ICompilationUnit[] {mockCompilationUnit});
 		
 		codeParser.parse();
 	}
 	
 	@Test
 	public void testTestParser() throws TestParserException, ParserException, JavaModelException {
 		prepareTests(); 
 		
 		JUnit3Parser testParser = new JUnit3Parser();
 		
 		testParser.setEnvironment(codeParser.getEnvironment());
 		
 		testParser.parse();
 		
 		battery = testParser.getBattery();
 		
 		testBatteryParser();
 		
 		suite = battery.getSuiteByName("MockClass");
 		
 		testSuiteParser();
 
 		testSuiteFixtureParser();
 		
 		testSuiteMethodParser();
 		
 		testBlockElementsParser();
 	}
 
 	private void testBatteryParser() {
 		assertNull("Bateria de testes: Parent", battery.getParent());
 		assertEquals("Bateria de testes: Size of suite list", 1, battery.getSuiteList().size());
 	}
 
 	private void testSuiteParser() {
 		assertEquals("Suite: parent", battery, suite.getParent());
 		
 		CodeElement expectedSuiteCodeElement = codeParser.getEnvironment().getTypeCache().get("org.ita.neutrino.testfiles.junit3parsertests.MockClass");
 		
 		assertEquals("Suite: code element", expectedSuiteCodeElement, suite.getCodeElement());
 	}
 
 	private void testSuiteFixtureParser() {
 		assertEquals("Suite: fixture list (size)", 2, suite.getFixtures().size());
 		assertEquals("Suite: fixture 0", "fixture0", suite.getFixtures().get(0).getName());
 		assertEquals("Suite: fixture 1", "fixture1", suite.getFixtures().get(1).getName());
 	}
 
 	private void testSuiteMethodParser() {
 		// Nesse caso sempre haverá apenas um método de setUp e tearDown.
 		assertEquals("Suite: before method list (size)", 1, suite.getBeforeMethodList().size());
		assertEquals("Suite: before method 0", "setup", suite.getBeforeMethodList().get(0).getName());
 		
 		assertEquals("Suite: after method list (size)", 1, suite.getAfterMethodList().size());
 		assertEquals("Suite: after method 0", "tearDown", suite.getAfterMethodList().get(0).getName());
 		
 		
 		assertEquals("Suite: test method list (size)", 2, suite.getTestMethodList().size());
 		
 		assertEquals("Suite: test method 0", "testNothing0", suite.getTestMethodList().get(0).getName());
 		assertEquals("Suite: test method 1", "testNothing1", suite.getTestMethodList().get(1).getName());
 	}
 
 	private void testBlockElementsParser() {
 		JUnitTestMethod testNothing0 = suite.getMethodByName("testNothing0");
 		
 		List<JUnitTestStatement> statementList = testNothing0.getStatements();
 		
 		assertEquals("StatementList: size", 2, statementList.size());
 		
 		
 		assertEquals("Action: classe", statementList.get(0).getClass(), JUnitAction.class);
 		
 		JUnitAction action = (JUnitAction) statementList.get(0);
 		
 		assertEquals("Action: valor", "action();\n", action.toString());
 		
 		
 		assertEquals("Assertion: classe", statementList.get(1).getClass(), JUnitAssertion.class);
 		
 		JUnitAssertion assertion = (JUnitAssertion) statementList.get(1);
 		
 		assertEquals("Assertion: valor", "assertTrue(\"Comment\",true);\n", assertion.toString());
 				
 		assertEquals("Assertion: comentário", "Comment", assertion.getExplanation());
 		
 	}
 
 }
