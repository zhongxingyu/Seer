 package org.ita.testrefactoring.junitparser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ita.testrefactoring.abstracttestparser.TestMethod;
 import org.ita.testrefactoring.codeparser.Method;
import org.ita.testrefactoring.codeparser.MethodInvocationStatement;
 import org.ita.testrefactoring.codeparser.Statement;
 
 public class JUnitTestMethod extends TestMethod {
 
 	private JUnitTestSuite parent;
 	private List<JUnitTestStatement> statementList = new ArrayList<JUnitTestStatement>();
 	private Method element;
 
 	JUnitTestMethod() {
 
 	}
 
 	JUnitAction createAction(Statement statement) {
 		JUnitAction action = new JUnitAction();
 
 		action.setParent(this);
 
 		action.setCodeElement(statement);
 		
 		statementList.add(action);
 
 		return action;
 	}
 
	JUnitAssertion createAssertion(MethodInvocationStatement methodInvocation) {
 		JUnitAssertion assertion = new JUnitAssertion();
 
 		assertion.setParent(this);
 		
		assertion.setCodeElement(methodInvocation);
		
 		statementList.add(assertion);
 
 		return assertion;
 	}
 
 	@Override
 	public List<JUnitTestStatement> getStatements() {
 		return statementList;
 	}
 
 	@Override
 	public JUnitTestSuite getParent() {
 		return parent;
 	}
 
 	void setParent(JUnitTestSuite parent) {
 		this.parent = parent;
 	}
 
 	@Override
 	public Method getCodeElement() {
 		return element;
 	}
 
 	void setCodeElement(Method element) {
 		this.element = element;
 	}
 
 	@Override
 	public String getName() {
 		return element.getName();
 	}
 
 	@Override
 	public String toString() {
 		return getName();
 	}
 }
