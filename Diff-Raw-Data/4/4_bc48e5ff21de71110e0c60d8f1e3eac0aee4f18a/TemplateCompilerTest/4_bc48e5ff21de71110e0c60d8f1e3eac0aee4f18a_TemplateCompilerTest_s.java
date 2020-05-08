 /*******************************************************************************
  * Copyright (c) 2006-2012
  * Software Technology Group, Dresden University of Technology
  * DevBoost GmbH, Berlin, Amtsgericht Charlottenburg, HRB 140026
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *   Software Technology Group - TU Dresden, Germany;
  *   DevBoost GmbH - Berlin, Germany
  *      - initial API and implementation
  ******************************************************************************/
 package de.devboost.commenttemplate.test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 
 import javax.tools.JavaFileObject;
 
 import junit.framework.TestCase;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.emftext.language.java.JavaClasspath;
 import org.emftext.language.java.containers.CompilationUnit;
 import org.emftext.language.java.resource.JavaSourceOrClassFileResourceFactoryImpl;
 
 import de.devboost.commenttemplate.CommentTemplate;
 import de.devboost.commenttemplate.compiler.CommentTemplateCompiler;
 import de.devboost.commenttemplate.test.input.Template1Source;
 import de.devboost.onthefly_javac.CompilationResult;
 import de.devboost.onthefly_javac.OnTheFlyJavaCompiler;
 
 public class TemplateCompilerTest extends TestCase {
 
 	public void testLineSplitting() {
 		assertLineSplitting("\n ", new String[] {"", " "});
 		assertLineSplitting("\n\n", new String[] {"", "", ""});
 		assertLineSplitting("\n", new String[] {"", ""});
 
 		assertLineSplitting(" \n", new String[] {" ", ""});
 	}
 	
 	private void assertLineSplitting(String comment, String[] expected) {
 		List<String> lines = new CommentTemplateCompiler().split(comment);
 		assertEquals(Arrays.asList(expected), lines);
 	}
 	
 	public void testCommentSplitting() {
 		assertCommentSplitting("/*abc*/", new String[] {"/*abc*/"});
 		assertCommentSplitting("\n/*abc*/", new String[] {"\n/*abc*/"});
 		assertCommentSplitting("\n\t/*abc*/", new String[] {"\n\t/*abc*/"});
 		assertCommentSplitting("/*abc*//*def*/", new String[] {"/*abc*/","/*def*/"});
 	}
 
 	private void assertCommentSplitting(String text, String[] expected) {
 		List<String> comments = new CommentTemplateCompiler().splitTextToComments(text);
 		assertEquals(Arrays.asList(expected), comments);
 	}
 	
 	public void testRegex() {
 		assertEquals("\r\n\r\n", "\r\n\r\n\r\n".replaceAll("\\r\\n\\z", ""));
 		assertEquals("\n\n", "\n\n\n".replaceAll("\\n\\z", ""));
 	}
 
 	public void testTemplateCompilation() throws IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InstantiationException, FileNotFoundException, IOException {
 		
 		String templatePackage = Template1Source.class.getPackage().getName();
 		String pathToTemplates = "src" + File.separatorChar + templatePackage.replace('.', File.separatorChar);
 		File templateDir = new File(pathToTemplates);
 		File[] templateFiles = templateDir.listFiles(new FileFilter() {
 			
 			@Override
 			public boolean accept(File pathname) {
 				String name = pathname.getName();
 				return name.endsWith(".java");
 			}
 		});
 		
 		for (File templateFile : templateFiles) {
 			String templateFileName = templateFile.getName();
 			String templateClassName = templateFileName.replace(".java", "");
 			String compiledClassName = templateClassName.replace("Source", "");
 
 			ResourceSet rs = new ResourceSetImpl();
 			rs.getResourceFactoryRegistry().getExtensionToFactoryMap().put("java", new JavaSourceOrClassFileResourceFactoryImpl());
 			
 			JavaClasspath.get(rs).registerClassifier(CommentTemplate.class);
 			
 			Resource resource = rs.getResource(URI.createFileURI(templateFile.getAbsolutePath()), true);
 			assertFalse("Original resource must not be empty.", resource.getContents().isEmpty());
 			EList<Diagnostic> errors = resource.getErrors();
 			for (Diagnostic diagnostic : errors) {
 				System.out.println("Error: " + diagnostic.getMessage() + " at line " + diagnostic.getLine());
 			}
 			assertTrue("Resource must not contain errors.", errors.isEmpty());
 
			boolean success = new CommentTemplateCompiler().compile(resource);
 			assertTrue("Template must be compilable", success);
 			assertFalse("Original resource must not be empty after compilation.", resource.getContents().isEmpty());
 
 			CompilationUnit cu = (CompilationUnit) resource.getContents().get(0);
 			cu.getClassifiers().get(0).setName(compiledClassName);
 
 			ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 			resource.save(outputStream, null);
 			
 			String compiledSourceCode = outputStream.toString();
 			System.out.println("compiledSourceCode (" + templateFileName + ") =>" + compiledSourceCode + "<=");
 			
 			String className = templatePackage + "." + compiledClassName;
 			
 			OnTheFlyJavaCompiler compiler = new OnTheFlyJavaCompiler();
 			CompilationResult result = compiler.compile(className, compiledSourceCode);
 			boolean compiledSuccessfully = result.isSuccess();
 			if (!compiledSuccessfully) {
 				List<javax.tools.Diagnostic<? extends JavaFileObject>> compilationErrors = result.getDiagnosticsCollector().getDiagnostics();
 				for (javax.tools.Diagnostic<? extends JavaFileObject> compilationError : compilationErrors) {
 					System.out.println(compilationError.getMessage(Locale.ENGLISH));
 				}
 			}
 			assertTrue("Compilation must be successful.", compiledSuccessfully);
 			
 			Class<?> loadedClass = result.loadClass(className);
 			String generatedString = instantiateAndInvoke(loadedClass, "generate");
 			System.out.println("generatedString (" + templateFileName + ") =>" + generatedString + "<=");
 			String expectedResult = instantiateAndInvoke(loadedClass, "expectedResult");
 			System.out.println("expectedResult  (" + templateFileName + ") =>" + expectedResult + "<=");
 			List<Byte> generatedBytes = new ArrayList<Byte>();
 			for (Byte nextByte : generatedString.getBytes()) {
 				generatedBytes.add(nextByte);
 			}
 			List<Byte> expectedBytes = new ArrayList<Byte>();
 			for (Byte nextByte : expectedResult.getBytes()) {
 				expectedBytes.add(nextByte);
 			}
 			System.out.println("generatedString (" + templateFileName + ") =>" + generatedBytes + "<=");
 			System.out.println("expectedResult  (" + templateFileName + ") =>" + expectedBytes + "<=");
 			assertEquals("Unexpected generation result.", expectedResult, generatedString);
 		}
 
 		assertTrue("Found too few test templates", templateFiles.length >= 11);
 	}
 
 	private String instantiateAndInvoke(Class<?> loadedClass, String methodName)
 			throws InstantiationException, IllegalAccessException,
 			NoSuchMethodException, InvocationTargetException {
 		Object newInstance = loadedClass.newInstance();
 		Method method = loadedClass.getMethod(methodName, (Class<?>[]) null);
 		Object returnValue = method.invoke(newInstance, new Object[] {});
 		assertNotNull("Return value must not be null.", returnValue);
 		assertTrue("Return value must be a string.", returnValue instanceof String);
 		String generatedString = (String) returnValue;
 		return generatedString;
 	}
 }
