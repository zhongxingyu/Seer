 package org.eclipse.dltk.javascript.core.tests.contentassist;
 
 import org.eclipse.dltk.compiler.env.IModuleSource;
 
 public class XMLCompletionTests extends AbstractCompletionTest {
 
 	public void test1() {
 		String[] names = concat(getMethodsOfXML(), "product", "customer");
 		IModuleSource module = createModule("test-xml1.js");
 		int position = lastPositionInFile("order.", module);
 		basicTest(module, position, names);
 	}
 
 	public void test2() {
 		String[] names = concat(getMethodsOfXML(), "@id", "@name");
 		IModuleSource module = createModule("test-xml2.js");
 		int position = lastPositionInFile("order.product.", module);
 		basicTest(module, position, names);
 	}
 
 	public void test3() {
 		String[] names = getMethodsOfXML().toArray(new String[0]);
 		IModuleSource module = createModule("test-xml3.js");
 		int position = lastPositionInFile("x[0].", module);
 		basicTest(module, position, names);
 	}
 }
