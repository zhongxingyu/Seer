 package domain.UmlClass;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 import java.util.ArrayList;
 import domain.UmlClass.AssociationType;
 
 public class TestUmlClassModel{
 
 	private UmlClassModel targetModel;
 	private UmlClassModel testModel;
 	private UmlClassModel anotherModel;
 	private UmlClassModel unassociatedModel;
 	private UmlClassModel testClass1a;
 	private UmlClassModel testClass1b;
 	private UmlClassModel testClass2;
 	private UmlClassModel testClass3;
 	private UmlClassModel testClass4;
 	private UmlClassModel testClass5;
 	private UmlClassModel testClass6;
 	private UmlClassModel testClass7;
 	private UmlClassModel testClass8;
 	private UmlClassModel testClass9;
 	private UmlClassModel testClass10a;
 	private UmlClassModel testClass10b;
 	private UmlClassModel testClass11a;
 	private UmlClassModel testClass11b;
 	private UmlClassModel testClass12a;
 	private UmlClassModel testClass12b;
 	private UmlClassModel testClass13;
 	
 	private UmlAttributeModel testAttribute;
 	private UmlAttributeModel anotherAttribute;
 	private UmlAttributeModel nonexistentAttribute;
 	private UmlAttributeModel testAttribute5;
 	private UmlMethodModel testMethod;
 	private UmlMethodModel anotherMethod;
 	private UmlMethodModel nonexistentMethod;
 	private UmlMethodModel testMethod4;
 	private UmlMethodModel testMethod5;
 	private UmlMethodModel testMethod6;
 	private UmlMethodModel testMethod7;
 	private UmlMethodModel testMethod13;
 	private UmlAttributeModel testAttribute8;
 	private UmlAttributeModel testAttribute9;
 	private UmlAttributeModel testAttribute13;
 	private ArrayList<UmlAttributeModel> testAttributeList5;
 	
 	@Before
 	public void setUp() throws Exception {
 		targetModel = new UmlClassModel(AccessModifier.Public, "targetModel");
 		testModel = new UmlClassModel();	
 		anotherModel = new UmlClassModel(AccessModifier.Public, "anotherModel");
 		unassociatedModel = new UmlClassModel();
 		testAttribute = new UmlAttributeModel();
 		anotherAttribute = new UmlAttributeModel(AccessModifier.Private, "anotherAttribute", "Object");
 		nonexistentAttribute = new UmlAttributeModel(AccessModifier.Private, "nonexistentAttribute", "Object");
 		testMethod = new UmlMethodModel();
 		anotherMethod = new UmlMethodModel(AccessModifier.Private, "Object", "anotherMethod", new ArrayList<UmlAttributeModel>());
 		nonexistentMethod = new UmlMethodModel(AccessModifier.Private, "Object", "nonexistentMethod", new ArrayList<UmlAttributeModel>());
 		
 		/*the following objects are used for testing the toString method
 		 * Naming convention: "test" + ObjectType + TestNumber
 		 * The ObjectType is Class, Method, or Attribute.
 		 * The TestNumber indicate for which test (see the comments in testToString) the Object is used.
 		 */
 		//testModels
 		testClass1a = new UmlClassModel();
 		testClass1b = new UmlClassModel(AccessModifier.Private, "CustomClassTest");
 		testClass2 = new UmlClassModel(AccessModifier.Public, "AbstractClassTest");
 		testClass3 = new UmlClassModel(AccessModifier.Public, "InterfaceTest");
 		testClass4 = new UmlClassModel(AccessModifier.Public, "PlainMethodTest");
 		testClass5 = new UmlClassModel(AccessModifier.Public, "MethodWithParamTest");
 		testClass6 = new UmlClassModel(AccessModifier.Public, "StaticMethodTest");
 		testClass7 = new UmlClassModel(AccessModifier.Public, "AbstractMethodTest");
 		testClass8 = new UmlClassModel(AccessModifier.Public, "NonStaticAttributeTest");
 		testClass9 = new UmlClassModel(AccessModifier.Public, "StaticAttributeTest");
 		testClass10a = new UmlClassModel(AccessModifier.Public, "AggregationTestClass");
 		testClass10b = new UmlClassModel(AccessModifier.Public, "AggregateObjects");
 		testClass11a = new UmlClassModel(AccessModifier.Public, "DependencyTest");
 		testClass11b = new UmlClassModel(AccessModifier.Public, "DependedOnClass");
 		testClass12a = new UmlClassModel(AccessModifier.Public, "InheritedFromTestClass");
 		testClass12b = new UmlClassModel(AccessModifier.Public, "InheritedClassTest");
 		testClass13 = new UmlClassModel(AccessModifier.Public, "MethodWithAttrTest");
 		
 		//testMethods
 		testMethod4 = new UmlMethodModel(AccessModifier.Private, "void", "plainMethod", new ArrayList<UmlAttributeModel>());
 		testMethod5 = new UmlMethodModel(AccessModifier.Private, "void", "methodWithParam", new ArrayList<UmlAttributeModel>());
 		testMethod6 = new UmlMethodModel(AccessModifier.Private, "void", "staticMethod", new ArrayList<UmlAttributeModel>());
 		testMethod7 = new UmlMethodModel(AccessModifier.Private, "void", "abstractMethod", new ArrayList<UmlAttributeModel>());
 		testMethod13 = new UmlMethodModel(AccessModifier.Private, "void", "methodWithAttr", new ArrayList<UmlAttributeModel>());
 		
 		//testAttributes
 		testAttribute5 = new UmlAttributeModel(AccessModifier.Private, "testParameter", "void");
 		testAttribute8 = new UmlAttributeModel(AccessModifier.Private, "testAttribute", "String");
 		testAttribute9 = new UmlAttributeModel(AccessModifier.Private, "testAttribute2", "String");
 		testAttribute13 = new UmlAttributeModel(AccessModifier.Private, "testAttribute13", "String");
 		
 		//testAttributeLists
 		testAttributeList5 = new ArrayList<UmlAttributeModel>();
 	}
 
 	@Test
 	public void testAddAssociation() {
 		// added some syntactic sugar to add/remove Associations
 		
 		//test adding one association
 		UmlAssociationModel assocA = testModel.addAssociation(targetModel.getName(), AssociationType.Inheritance);
 		assertTrue(assocA != null);
 		assertTrue(testModel.getAssociations().get(0).equals(assocA));
 		
 		//test adding duplicate association
 		assertTrue(testModel.addAssociation(targetModel.getName(), AssociationType.Inheritance) == null);
 		assertTrue(testModel.getAssociations().size() == 1);
 		
 		//test adding association with same target but different associationType
 		assertTrue(testModel.addAssociation(targetModel.getName(), AssociationType.Composition) == null);
 		assertTrue(testModel.getAssociations().size() == 1);
 			
 		//test adding a new association to the first one
 		UmlAssociationModel assocB = testModel.addAssociation(anotherModel.getName(), AssociationType.Inheritance);
 		assertTrue(assocB != null);
 		assertTrue(testModel.getAssociations().get(1).equals(assocB));		
 	}
 	
 	@Test
 	public void testRemoveAssociation() {
 		//add two associations in preparation for removal
 		UmlAssociationModel assocA = testModel.addAssociation(targetModel.getName(), AssociationType.Inheritance);
 		UmlAssociationModel assocB = testModel.addAssociation(anotherModel.getName(), AssociationType.Inheritance);
 		assertTrue(assocA != null);
 		assertTrue(assocB != null);
 		//remove an association
 		assertTrue(testModel.removeAssociation(targetModel).equals(assocA));
 		assertTrue(testModel.getAssociations().size() == 1 && testModel.getAssociations().get(0).equals(assocB));
 		//try to remove an association that doesn't exist
 		assertTrue(testModel.removeAssociation(unassociatedModel) == null);
 	}
 	
 	@Test
 	public void testHasInheritanceCycle() {
 		//test two classes with no associations
 		assertFalse(testModel.hasInheritanceCycle(targetModel));
 		//create non-inheritance relationship between two classes
 		targetModel.addAssociation(testModel.getName(), AssociationType.Dependency);
 		assertFalse(testModel.hasInheritanceCycle(targetModel));
 		//create inheritance relationship in target class to non-tested class
 		targetModel.addAssociation(anotherModel.getName(), AssociationType.Inheritance);
 		assertFalse(testModel.hasInheritanceCycle(targetModel));
 		//create inheritance relationship in target class to tested class
 		targetModel.removeAssociation(testModel);
 		targetModel.addAssociation(testModel.getName(), AssociationType.Inheritance);
 		assertTrue(testModel.hasInheritanceCycle(targetModel));
 	}
 	
 	@Test
 	public void testAddAttribute() {
 		//try to add one attribute
 		UmlAttributeModel attrA = new UmlAttributeModel();
 		assertTrue(testModel.addAttribute(attrA));
 		assertTrue(testModel.getAttributes().get(0).equals(attrA));
 		//try to add a duplicate attribute (throws exception)
 		assertFalse(testModel.addAttribute(new UmlAttributeModel()));
 	}
 	
 	@Test
 	public void testRemoveAttribute() {
 		//add attributes to set up
 		assertTrue(testModel.addAttribute(testAttribute));
 		assertTrue(testModel.addAttribute(anotherAttribute));
 		//remove an attribute
 		assertTrue(testModel.removeAttribute(testAttribute));
 		assertTrue(testModel.getAttributes().size() == 1 && testModel.getAttributes().get(0).equals(anotherAttribute));
 		//remove an attribute that doesn't exist (throws exception)
 		assertFalse(testModel.removeAttribute(nonexistentAttribute));
 	}
 	
 	@Test
 	public void testAddMethod() {
 		//try to add one method
 		UmlMethodModel mockMethod = new UmlMethodModel();
 		
 		assertTrue(testModel.addMethod(mockMethod));
 		assertTrue(testModel.getMethods().get(0).equals(mockMethod));
 		//try to add a duplicate method (throws exception)
 		assertFalse(testModel.addMethod(mockMethod));
 	}
 	
 	@Test
 	public void testRemoveMethod() {
 		//add methods to set up 
 		assertTrue(testModel.addMethod(testMethod));
 		assertTrue(testModel.addMethod(anotherMethod));
 		//remove an attribute
 		assertTrue(testModel.removeMethod(testMethod));
 		assertTrue(testModel.getMethods().size() == 1 && testModel.getMethods().get(0).equals(anotherMethod));
 		//remove an attribute that doesn't exist (throws exception)
 		assertFalse(testModel.removeMethod(nonexistentMethod));
 	}
 
 	@Test
 	public void testToString() {
 		// quick and dirty test to verify that mysterious [] characters are no longer displayed
 		testModel.addAssociation(targetModel.getName(), AssociationType.Inheritance);
 		UmlAttributeModel paramA = new UmlAttributeModel();
 		UmlAttributeModel paramB = new UmlAttributeModel();
 		ArrayList<UmlAttributeModel> params = new ArrayList<UmlAttributeModel>();
 		params.add(paramA);
 		params.add(paramB);		
 		testModel.addMethod(new UmlMethodModel(AccessModifier.Public, "Object", "TestMethod", params));
 		testModel.addAttribute(new UmlAttributeModel());
 		String codeSkeleton = testModel.toString();
 		assertTrue(!codeSkeleton.contains("["));
 		assertTrue(!codeSkeleton.contains("]"));
 		
 		//Test 1a: blank public class (with default name)
 		String codeSkel1a = testClass1a.toString();
 		assertEquals("public class newClass  {\n\n}\n", codeSkel1a);
 		
 		//Test 1b: blank private class (with custom name)
 		String codeSkel1b = testClass1b.toString();
 		assertEquals("private class CustomClassTest  {\n\n}\n", codeSkel1b);
 		
 		//Test 2: default abstract class
 		testClass2.setAbstractFlag(true);
 		String codeSkel2 = testClass2.toString();
 		assertEquals("public abstract class AbstractClassTest  {\n\n}\n", codeSkel2);
 		
 		//Test 3: default interface
 		testClass3.setInterfaceFlag(true);
 		String codeSkel3 = testClass3.toString();
 		assertEquals("public interface InterfaceTest  {\n\n}\n", codeSkel3);
 		
 		//Test 4: class with a non-static, non-abstract method
 		testClass4.addMethod(testMethod4);
 		String codeSkel4 = testClass4.toString();
 		assertEquals("public class PlainMethodTest  {\n\n\tprivate void plainMethod () {\n\n\t}\n}\n", codeSkel4);
 		
 		//Test 5: class with a method with a parameter
 		testAttributeList5.add(testAttribute5);
 		testMethod5.setParameters(testAttributeList5);
 		testClass5.addMethod(testMethod5);
 		String codeSkel5 = testClass5.toString();
 		assertEquals("public class MethodWithParamTest  {\n\n\tprivate void methodWithParam (void testParameter) {\n\n\t}\n}\n", codeSkel5);
 		
 		//Test 6: class with a static method
 		testMethod6.setStaticFlag(true);
 		testClass6.addMethod(testMethod6);
 		String codeSkel6 = testClass6.toString();
 		assertEquals("public class StaticMethodTest  {\n\n\tprivate static void staticMethod () {\n\n\t}\n}\n", codeSkel6);
 		
 		//Test 7: class with an abstract method
 		testMethod7.setAbstractFlag(true);
 		testClass7.addMethod(testMethod7);
 		String codeSkel7 = testClass7.toString();
 		assertEquals("public class AbstractMethodTest  {\n\n\tprivate abstract void abstractMethod () {\n\n\t}\n}\n", codeSkel7);
 		
 		//Test 8: class with a non-static attribute
 		testAttribute8.setStaticFlag(false);
 		testClass8.addAttribute(testAttribute8);
 		String codeSkel8 = testClass8.toString();
 		assertEquals("public class NonStaticAttributeTest  {\n\tprivate String testAttribute;\n\n}\n", codeSkel8);
 		
 		//Test 9: class with a static attribute
 		testAttribute9.setStaticFlag(true);
 		testClass9.addAttribute(testAttribute9);
 		String codeSkel9 = testClass9.toString();
 		assertEquals("public class StaticAttributeTest  {\n\tprivate static String testAttribute2;\n\n}\n", codeSkel9);
 		
		//TODO: Test 10: class with an aggregation association
 		testClass10b.addAssociation("AggregationTestClass", AssociationType.Aggregation);
 		String codeSkel10 = testClass10b.toString();
		assertEquals("public class AggregateObjects  {\n\n\tAggregationTestClass aggObject;\n\n}\n", codeSkel10);
 		
		//TODO: Test 11: class with a dependency association
 		testClass11b.addAssociation("DependedOnClass", AssociationType.Dependency);
 		String codeSkel11 = testClass11b.toString();
 		assertEquals("public class DependedOnClass dependent of DependedOnClass  {\n\n}\n", codeSkel11);
 		
 		//Test 12: class that inherits from another class
 		testClass12b.addAssociation("InheritedFromTestClass", AssociationType.Inheritance);
 		String codeSkel12 = testClass12b.toString();
 		assertEquals("public class InheritedClassTest extends InheritedFromTestClass  {\n\n}\n", codeSkel12);
 		
 		//Test 13: a class with both a method and an attribute
 		testClass13.addAttribute(testAttribute13);
 		testClass13.addMethod(testMethod13);
 		String codeSkel13 = testClass13.toString();
 		assertEquals("public class MethodWithAttrTest  {\n\tprivate String testAttribute13;\n\n" +
 				"\tprivate void methodWithAttr () {\n\n\t}\n}\n", codeSkel13);
 	}
 }
