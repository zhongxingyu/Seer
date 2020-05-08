 package org.ita.neutrino.junitgenericparser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ita.neutrino.codeparser.Method;
 import org.ita.neutrino.codeparser.MethodInvocationStatement;
 import org.ita.neutrino.codeparser.Statement;
 import org.ita.neutrino.codeparser.Type;
 
 public class BlockParser {
 
 	private JUnitTestBattery battery;
 	private List<JUnitTestMethod> concreteMethodList;
 
 	public void setBattery(JUnitTestBattery battery) {
 		this.battery = battery;
 	}
 
 	public void parse() {
 		getAllConcreteMethods();
 
 		for (JUnitTestMethod method : concreteMethodList) {
 			parseBlock(method);
 		}
 	}
 
 	private void getAllConcreteMethods() {
 		concreteMethodList = new ArrayList<JUnitTestMethod>();
 
 		for (JUnitTestSuite suite : battery.getSuiteList()) {
 			// cp = codeParser
 			for (JUnitTestMethod tpBeforeMethod : suite.getBeforeMethodList()) {
 				Method cpBeforeMethod = tpBeforeMethod.getCodeElement();
 
 				if (!cpBeforeMethod.getNonAccessModifier().isAbstract()) {
 					concreteMethodList.add(tpBeforeMethod);
 				}
 			}
 
 			// tp = testParser
 			for (JUnitTestMethod tpTestMethod : suite.getTestMethodList()) {
 				Method cpTestMethod = tpTestMethod.getCodeElement();
 
 				if (!cpTestMethod.getNonAccessModifier().isAbstract()) {
 					concreteMethodList.add(tpTestMethod);
 				}
 			}
 
 			for (JUnitTestMethod tpAfterMethod : suite.getAfterMethodList()) {
 				Method cpAfterMethod = tpAfterMethod.getCodeElement();
 
 				if (!cpAfterMethod.getNonAccessModifier().isAbstract()) {
 					concreteMethodList.add(tpAfterMethod);
 				}
 
 			}
 		}
 
 	}
 
 	private void parseBlock(JUnitTestMethod method) {
 		for (Statement statement : method.getCodeElement().getBody().getStatementList()) {
 			parseStatement(method, statement);
 		}
 	}
 
 	private void parseStatement(JUnitTestMethod method, Statement statement) {
		if (statement instanceof MethodInvocationStatement) {
			MethodInvocationStatement methodInvocation = (MethodInvocationStatement) statement;

			JUnitTestStatement testStatement = null;
			
			if (isAssertion(methodInvocation)) {
				testStatement = method.createAssertion(methodInvocation);
			} else {
				testStatement = method.createAction(statement);
			}
			
			if (battery.getCodeElement().getSelection().getSelectedElement() == statement) {
				battery.getSelection().setSelectedFragment(testStatement);
			}
 		}
 	}
 
	private boolean isAssertion(MethodInvocationStatement methodInvocation) {
 		
 		Type methodType = methodInvocation.getCalledMethod().getParent();
 		String fullName = methodType.getQualifiedName() + "." + methodInvocation.getCalledMethod().getName();
 
 		if (fullName.equals("org.junit.Assert.assertArrayEquals")
 				|| fullName.equals("org.junit.Assert.assertEquals")
 				|| fullName.equals("org.junit.Assert.assertTrue")
 				|| fullName.equals("org.junit.Assert.assertFalse")
 				|| fullName.equals("org.junit.Assert.assertNull")
 				|| fullName.equals("org.junit.Assert.assertNotNull")
 				|| fullName.equals("org.junit.Assert.assertSame")
 				|| fullName.equals("org.junit.Assert.assertNotSame")
 				|| fullName.equals("org.junit.Assert.assertThat")
 
 				|| fullName.equals("junit.framework.Assert.assertEquals")
 				|| fullName.equals("junit.framework.Assert.assertTrue")
 				|| fullName.equals("junit.framework.Assert.assertFalse")
 				|| fullName.equals("junit.framework.Assert.assertNull")
 				|| fullName.equals("junit.framework.Assert.assertNotNull")
 				|| fullName.equals("junit.framework.Assert.assertSame")
 				|| fullName.equals("junit.framework.Assert.assertNotSame")) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 }
