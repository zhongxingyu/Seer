 package org.ita.testrefactoring.junitparser;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.ita.testrefactoring.codeparser.Annotation;
 import org.ita.testrefactoring.codeparser.Environment;
 import org.ita.testrefactoring.codeparser.Method;
 import org.ita.testrefactoring.codeparser.Type;
 import org.ita.testrefactoring.codeparser.TypeKind;
 
 /**
  * Responsável por localizar as Suites de testes e seus respectivos métodos.
  * 
  * @author Rafael Monico
  * 
  */
 class BatteryParser {
 
 	private enum TestMethodKind {
 		NOT_TEST_METHOD(null), BEFORE_METHOD("org.junit.Before"), TEST_METHOD("org.junit.Test"), AFTER_METHOD("org.junit.After");
 		
 		private String annotationName;
 
 		private TestMethodKind(String qualifiedAnnotationName) {
 			annotationName = qualifiedAnnotationName;
 		}
 		
 		String getAnnotationName() {
 			return annotationName;
 		}
 	}
 
 	private Environment environment;
 	private JUnitTestBattery battery;
 
 	public void setEnvironment(Environment environment) {
 		this.environment = environment;
 
 	}
 
 	public void setBattery(JUnitTestBattery battery) {
 		this.battery = battery;
 	}
 
 	public void parse() {
 		List<Type> knownTypes = getKnownTypesList();
 		
 		for (Type t : knownTypes) {
 			// Não sei se a classe será uma suite de teste nesse momento
 			JUnitTestSuite suite = null;
 			
 			for (Method m : t.getMethodList().values()) {
 				TestMethodKind methodKind = getTestMethodKind(m);
 				
 				if (methodKind == TestMethodKind.NOT_TEST_METHOD) {
 					continue;
 				}
 				
 				if (suite == null) {
 					suite = battery.createSuite(t);
 				}
 				
 				if (methodKind == TestMethodKind.BEFORE_METHOD) {
 					suite.createBeforeMethod(m);
 				} else if (methodKind == TestMethodKind.TEST_METHOD) {
 					suite.createTestMethod(m);
 				} else if (methodKind == TestMethodKind.AFTER_METHOD) {
 					suite.createAfterMethod(m);
 				}
 			}
 			
			if (suite != null) {
				battery.getSuiteList().add(suite);
			}
 		}
 	}
 
 	private List<Type> getKnownTypesList() {
 		List<Type> knownTypes = new ArrayList<Type>();
 		
 		for (Type t : environment.getTypeCache().values()) {
			if (t.getKind() != TypeKind.UNKNOWN) {
 				knownTypes.add(t);
 			}
 		}
 		
 		return knownTypes;
 	}
 	
 	private TestMethodKind getTestMethodKind(Method method) {
 		for (Annotation a : method.getAnnotations()) {
 			for (TestMethodKind kind : TestMethodKind.values()) {
 				if (a.getQualifiedName().equals(kind.getAnnotationName())) {
 					return kind;
 				}
 			}
 		}
 		
 		return TestMethodKind.NOT_TEST_METHOD;
 	}
 
 }
