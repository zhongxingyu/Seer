 package testParser;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.assertFalse;
 
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.util.List;
 
import models.CallGraph;
import models.Clazz;
import models.File;
import models.Mapping;
 import models.*;
 
 import org.junit.Test;
 
 import parser.Parser;
 
 public class TestParser {
 	@Test
 	public void testVisitorVisitPackage() {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Single package
 		String fileName = "src/test/A.java";
 		String rawFile = "package myPackage;\nclass A(){\n}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 		assertEquals(f.getFileName(), fileName);
 		assertEquals(f.getFilePackage(), "myPackage");
 
 		// Duplicate packages
 		rawFile = "package Hahaha;\npackage hohoho;\npackage MuHAHAHA;\nclass A(){}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		f = cg.containsFile(fileName);
 		assertEquals(f.getFilePackage(), "MuHAHAHA");
 
 		// Empty package
 		rawFile = "class A(){}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		f = cg.containsFile(fileName);
 		assertEquals(f.getFilePackage(), "");
 
 		// casesensitive packages
 		rawFile = "packaGe myPackage;\nclass B(){}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		f = cg.containsFile(fileName);
 		assertEquals(f.getFilePackage(), "");
 	}
 
 	@Test
 	public void testVisitorVisitImport() {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Single import
 		String fileName = "src/test/A.java";
 		String rawFile = "package myPackage;\nimport models.clazz;\nclass A(){\n}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 		List<String> imports = f.getFileImports();
 
 		assertEquals(imports.size(), 1);
 		assertEquals(imports.get(0), "models.clazz");
 
 		// multiple imports
 		rawFile = "package myPackage;\nimport models.clazz;\n\nimport models.*;import java.util.List;\nimport static org.junit.Assert.*;\nclass B(){\n}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		f = cg.containsFile(fileName);
 		imports = f.getFileImports();
 
 		assertEquals(imports.size(), 4);
 		assertTrue(imports.contains("models.clazz"));
 		assertTrue(imports.contains("models"));
 		assertTrue(imports.contains("java.util.List"));
 		assertTrue(imports.contains("org.junit.Assert"));
 
 		// empty import
 		rawFile = "package myPackage;\nclass C(){\n}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		f = cg.containsFile(fileName);
 		imports = f.getFileImports();
 		assertEquals(imports.size(), 0);
 	}
 
 	@Test
 	public void testVisitorVisitTypeDeclarationClass() {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Single class
 		String fileName = "src/test/A.java";
 		String rawFile = "package mypackage;\nimport models.clazz;\npublic class classA implements classAbase{\n}\n";
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 		List<Clazz> classes = f.getFileClazzes();
 
 		assertEquals(classes.size(), 1);
 		assertEquals(classes.get(0).getName(), "mypackage.classA");
 		assertEquals(classes.get(0).getFile().getFileName(), fileName);
 		assertFalse(classes.get(0).isInterface());
 		assertEquals(classes.get(0).getUnresolvedInterfaces().size(), 1);
 		assertEquals(classes.get(0).getUnresolvedInterfaces().get(0), "classAbase");
 
 		// Multiple class
 		rawFile = "package mypackage;\nimport models.clazz;\nprivate class classB(){\npublic class insideClassC{}\n};\n";
 		parser.parseFileFromString(fileName, rawFile);
 		f = cg.containsFile(fileName);
 		classes = f.getFileClazzes();
 
 		assertEquals(classes.size(), 2);
 		boolean classB = false;
 		boolean classC = false;
 		for (Clazz c : classes) {
 			if (c.getName().equals("mypackage.insideClassC") && !c.isInterface() && c.getFile().getFileName().equals(fileName))
 				classC = true;
 			if (c.getName().equals("mypackage.classB") && !c.isInterface() && c.getFile().getFileName().equals(fileName))
 				classB = true;
 		}
 		assertTrue(classC);
 		assertTrue(classB);
 	}
 	
 	@Test
 	public void testVisitorVisitTypeDeclarationClassUnresolvedSuperClass() {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Single class
 		String fileName = "src/test/classA.java";
 		String rawFile = "package mypackage;\n" +
 						 "import models.clazz;\n" +
 						 "public class classA extends classAbase{\n" +
 						 	"public class Achild extends AchildBase{\n" +
 						 		"public void doSomething(){}\n" +
 						 	"}\n" +
 						 	"public class Bchild extends BchildBase{\n" +
 					 			"public void doSomethingElse(){}\n" +
 					 		"}\n" +
 						 "}\n";
 		
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 		List<Clazz> classes = f.getFileClazzes();
 
 		assertEquals(classes.size(), 3);
 
 		boolean Bchild = false;
 		boolean Achild = false;
 		boolean classA = false;
 		for (Clazz c : classes) {
 			if (c.getName().equals("mypackage.classA") && c.getUnresolvedSuperClazz().equals("classAbase"))
 				classA = true;
 			if (c.getName().equals("mypackage.Achild") && c.getUnresolvedSuperClazz().equals("AchildBase"))
 				Achild = true;
 			if (c.getName().equals("mypackage.Bchild") && c.getUnresolvedSuperClazz().equals("BchildBase"))
 				Bchild = true;
 		}
 		assertTrue(classA);
 		assertTrue(Achild);
 		assertTrue(Bchild);
 	}
 	
 	@Test
 	public void testVisitorVisitTypeDeclarationClassUnresolvedInterface() {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Single class
 		String fileName = "src/test/classA.java";
 		String rawFile = "package mypackage;\n" +
 						 "import models.clazz;\n" +
 						 "public class classA implements A1, A2, A3{\n" +
 						 	"public class Achild implements Achild1, Achild2{\n" +
 						 		"public void doSomething(){}\n" +
 						 	"}\n" +
 						 "}\n";
 		
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 		List<Clazz> classes = f.getFileClazzes();
 
 		assertEquals(classes.size(), 2);
 
 		boolean classA = false;
 		boolean Achild = false;
 		for (Clazz c : classes) {
 			if (c.getName().equals("mypackage.classA") && c.getUnresolvedInterfaces().contains("A1")
 			 										   && c.getUnresolvedInterfaces().contains("A2")
 			 										   && c.getUnresolvedInterfaces().contains("A3"))
 				classA = true;
 			if (c.getName().equals("mypackage.Achild") && c.getUnresolvedInterfaces().contains("Achild1")
 													   && c.getUnresolvedInterfaces().contains("Achild2"))
 				Achild = true;
 		}
 		assertTrue(classA);
 		assertTrue(Achild);
 	}
 	
 	@Test
 	public void testVisitorVisitTypeDeclarationClassGenericType() {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Single class
 		String fileName = "src/test/classA.java";
 		String rawFile = "package mypackage;\n" +
 						 "import models.clazz;\n" +
 						 "public class classA<K, E> extends abstractA<K, E> implements mapA<K, E>{\n" +
 						 "public class classB<K>{\n}\n" +
 						 "}\n";
 		
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 		List<Clazz> classes = f.getFileClazzes();
 
 		assertEquals(classes.size(), 2);
 		assertEquals(classes.get(1).getGenericTypes().size(), 2);
 		assertTrue(classes.get(1).getGenericTypes().contains("K"));
 		assertTrue(classes.get(1).getGenericTypes().contains("E"));
 		assertEquals(classes.get(0).getGenericTypes().size(), 1);
 		assertTrue(classes.get(0).getGenericTypes().contains("K"));
 	}
 
 	@Test
 	public void testVisitorVisitMethodDeclaration() throws IOException {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Test classes from Agilefant
 		String fileName = "test/testFiles/TestVisitorFileMethods.java";
 		String rawFile = Parser.readFileToString(fileName);
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 		
 		// 31 methods
 		Clazz c = f.getFileClazzes().get(0);
 		List<models.Method> methods = c.getMethods();
 		assertEquals(methods.size(), 31);
 		
 		boolean m1 = false;
 		boolean m2 = false;
 		for(models.Method m : methods)
 		{
			if(m.getName().equals("testFiles.TestVisitorFileMethods.setHourEntryDAO(HourEntryDAO)"))
 				m1 = true;
			if(m.getName().equals("testFiles.TestVisitorFileMethods.logTaskEffort(int, HourEntry, Set<Integer>)"))
 				m2 = true;
 		}
 		
 		assertTrue(m1);
 		assertTrue(m2);
 		
 		// 6 variables
 		List<Mapping> variables = c.getVariables();
 		assertEquals(variables.size(), 6);
 		
 		boolean var1 = false;
 		boolean var2 = false;
 		boolean var3 = false;
 		boolean var4 = false;
 		boolean var5 = false;
 		boolean var6 = false;
 		
 		for(Mapping m : variables)
 		{
 			if(m.getType().equals("HourEntryDAO") && m.getVarName().equals("hourEntryDAO"))
 				var1 = true;
 			if(m.getType().equals("StoryBusiness") && m.getVarName().equals("storyBusiness"))
 				var2 = true;
 			if(m.getType().equals("TaskBusiness") && m.getVarName().equals("taskBusiness"))
 				var3 = true;
 			if(m.getType().equals("UserBusiness") && m.getVarName().equals("userBusiness"))
 				var4 = true;
 			if(m.getType().equals("BacklogBusiness") && m.getVarName().equals("backlogBusiness"))
 				var5 = true;
 			if(m.getType().equals("BacklogHourEntryDAO") && m.getVarName().equals("backlogHourEntryDAO"))
 				var6 = true;
 		}
 		
 		assertTrue(var1);
 		assertTrue(var2);
 		assertTrue(var3);
 		assertTrue(var4);
 		assertTrue(var5);
 		assertTrue(var6);
 	}
 	
 	@Test
 	public void testVisitorVisitExtendsInterface() throws IOException {
 		CallGraph cg = new CallGraph();
 		Parser parser = new Parser(cg);
 
 		// Single class
 		String fileName = "test/testFiles/TestVisitorFile.java";
 		String rawFile = Parser.readFileToString(fileName);
 		parser.parseFileFromString(fileName, rawFile);
 		File f = cg.containsFile(fileName);
 
 		// Test interfaces
 		List<Clazz> interfaces = f.getFileInterfaces();
 		assertEquals(interfaces.size(), 4);
 		boolean interface1 = false;
 		boolean interface2 = false;
 		boolean interface3 = false;
 		boolean groupInterface = false;
 		for (Clazz c : interfaces) {
 			if (c.getName().equals("testFiles.Interface1") && c.isInterface() && c.getUnresolvedInterfaces().isEmpty())
 				interface1 = true;
 			if (c.getName().equals("testFiles.Interface2") && c.isInterface() && c.getUnresolvedInterfaces().isEmpty())
 				interface2 = true;
 			if (c.getName().equals("testFiles.Interface3") && c.isInterface() && c.getUnresolvedInterfaces().isEmpty())
 				interface3 = true;
 			if (c.getName().equals("testFiles.GroupedInterface") && c.isInterface() &&
 														c.getUnresolvedInterfaces().contains("Interface1") &&
 														c.getUnresolvedInterfaces().contains("Interface2") &&
 														c.getUnresolvedInterfaces().contains("Interface3"))
 				groupInterface = true;
 		}
 		
 		assertTrue(interface1);
 		assertTrue(interface2);
 		assertTrue(interface3);
 		assertTrue(groupInterface);
 		
 		// Test Classes
 		List<Clazz> classes = f.getFileClazzes();
 		// This includes the interface class as well
 		assertEquals(classes.size(), 10);
 		boolean testVisitorFile = false;
 		boolean testVisitorFile_1 = false;
 		boolean testVisitorFile_2 = false;
 		boolean testVisitorFile_3 = false;
 		boolean testVisitorFile_3_1 = false;
 		boolean testVisitorFile_3_2 = false;
 		
 		for(Clazz c : classes)
 		{
 			if(c.getName().equals("testFiles.TestVisitorFile"))
 				testVisitorFile = true;
 			if(c.getName().equals("testFiles.testVisitorFile_1")&&
 					c.getUnresolvedSuperClazz().equals("TestVisitorFile")&&
 					c.getUnresolvedInterfaces().size() == 2 &&
 					c.getUnresolvedInterfaces().contains("GroupedInterface")&&
 					c.getUnresolvedInterfaces().contains("Interface1"))
 				testVisitorFile_1 = true;
 			if(c.getName().equals("testFiles.testVisitorFile_2")&&
 					c.getUnresolvedSuperClazz().equals("TestVisitorFile")&&
 					c.getUnresolvedInterfaces().size() == 1 &&
 					c.getUnresolvedInterfaces().contains("Interface2"))
 				testVisitorFile_2 = true;
 			if(c.getName().equals("testFiles.testVisitorFile_3")&&
 					c.getUnresolvedSuperClazz().equals("")&&
 					c.getUnresolvedInterfaces().size() == 1 &&
 					c.getUnresolvedInterfaces().contains("Interface3"))
 				testVisitorFile_3 = true;
 			if(c.getName().equals("testFiles.testVisitorFile_3_1")&&
 					c.getUnresolvedSuperClazz().equals("testVisitorFile_3")&&
 					c.getUnresolvedInterfaces().size() == 2 &&
 					c.getUnresolvedInterfaces().contains("Interface1") &&
 					c.getUnresolvedInterfaces().contains("Interface3"))
 				testVisitorFile_3_1 = true;
 			if(c.getName().equals("testFiles.testVisitorFile_3_2")&&
 					c.getUnresolvedSuperClazz().equals("testVisitorFile_3")&&
 					c.getUnresolvedInterfaces().size() == 1 &&
 					c.getUnresolvedInterfaces().contains("GroupedInterface"))
 				testVisitorFile_3_2 = true;
 		}
 		
 		assertTrue(testVisitorFile);
 		assertTrue(testVisitorFile_1);
 		assertTrue(testVisitorFile_2);
 		assertTrue(testVisitorFile_3);
 		assertTrue(testVisitorFile_3_1);
 		assertTrue(testVisitorFile_3_2);
 		
 	}
 	
 	
 }
