 package org.eclipse.dltk.ruby.tests.assist;
 
 import java.util.ArrayList;
 
 import junit.framework.Test;
 
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.tests.model.AbstractModelCompletionTests;
 import org.eclipse.dltk.ruby.internal.core.codeassist.RubySelectionEngine;
 
 public class RubySelectionTests extends AbstractModelCompletionTests {
 	
 	//	Used only for testing checkSelection method
 	private static class ThinkRubySelectionEngine extends RubySelectionEngine {
 
 		public ThinkRubySelectionEngine() {
 			super(null, null, null);
 		}
 		
 		public boolean checkSelection (String source, int start, int end) {
 			return super.checkSelection(source, start, end);
 		}
 		
 		public int getActualStart ()  {
 			return this.actualSelectionStart;
 		}
 		
 		public int getActualEnd ()  {
 			return this.actualSelectionEnd;
 		}
 		
 	};
 
 	private static final String SELECTION_PROJECT = "Selection";
 	
 	private static final ThinkRubySelectionEngine thinkEngine = new ThinkRubySelectionEngine(); 
 
 	public RubySelectionTests(String name) {
 		super("org.eclipse.dltk.ruby.core.tests", name);
 	}
 
 	public void setUpSuite() throws Exception {
 		PROJECT = setUpScriptProject(SELECTION_PROJECT);
 
 		super.setUpSuite();
 	}
 
 	public static Test suite() {
 		return new Suite(RubySelectionTests.class);
 	}
 	
 	
 	
 	/*
 	 * 
 	 * ckeckSelection() method tests
 	 * 
 	 * 
 	 */
 	
 	public String getCheckrbSource ()  throws ModelException  {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 				"check.rb");
 		return cu.getSource();	
 	}
 
 	public void selectionPosChecker0(int start, int end, boolean expRes, int expStart, int expEnd) throws ModelException {
 		String source =  getCheckrbSource();
 		boolean res = thinkEngine.checkSelection(source, start, end);
 		assertEquals(expRes, res);
 		if (res) {
 			assertEquals(expStart, thinkEngine.getActualStart());
 			assertEquals(expEnd, thinkEngine.getActualEnd());
 		}
 	}
 	
 	public void selectionPosChecker1(String word) throws ModelException {
 		String source =  getCheckrbSource();
 		int start = source.indexOf(word);
 		assertTrue(start != -1);
 		int end = start - 1;
 		for (int i = 0; i < word.length(); i++) {
 			boolean res = thinkEngine.checkSelection(source, start + i, end + i);
 			assertEquals(true, res);
 			String sel = source.substring(thinkEngine.getActualStart(), thinkEngine.getActualEnd());
 			assertEquals(word, sel);
 		}
 	}
 	
 	public void testSelectionPosChecker0() throws ModelException {
 		selectionPosChecker0(0, 0, true, 0, 1);
 	}
 	
 	public void testSelectionPosChecker1() throws ModelException {
 		selectionPosChecker0(0, -1, true, 0, 3);
 	}
 	
 	public void testSelectionPosChecker2() throws ModelException {
 		selectionPosChecker1("FooClass");
 	}
 	
 	public void testSelectionPosChecker3() throws ModelException {
 		selectionPosChecker1("@a");
 	}
 	
 	public void testSelectionPosChecker4() throws ModelException {
 		selectionPosChecker1("@@a");
 	}
 	
 	public void testSelectionPosChecker5() throws ModelException {
 		selectionPosChecker1("foo1");
 	}
 	
 	
 	public void testSelectionPosChecker6() throws ModelException {
 		selectionPosChecker1("cool?");
 	}
 	
 	public void testSelectionPosChecker7() throws ModelException {
 		selectionPosChecker1("hot!");
 	}
 	
 	
 	public void testSelectionPosChecker8() throws ModelException {
 		selectionPosChecker1("**");
 	}
 
 	public void testSelectionPosChecker9() throws ModelException {
 		selectionPosChecker1("$a");
 	}
 	
 	public void testSelectionPosChecker10() throws ModelException {
 		selectionPosChecker1("print");
 	}
 	
 	public void testSelectionPosChecker11() throws ModelException {
 		selectionPosChecker1("times");
 	}
 	
 	public void testSelectionPosChecker12() throws ModelException {
 		String src = getCheckrbSource();
 		int pos = src.indexOf("5.*");
 		selectionPosChecker0(pos+2, pos+1, true, pos+2, pos+3);
 	}
 	
 	public void testSelectionPosChecker13() throws ModelException {
 		selectionPosChecker1("4242");
 	}
 	
 	public void testSelectionPosChecker15() throws ModelException {	
 		int pos = "def FooClass".length() + 5;
 		selectionPosChecker0(pos+2, pos+4, false, 0, 0);
 	}
 	
 	public void testSelectionPosChecker16() throws ModelException {	
 		int pos = "def FooClass".length() + 2;
 		selectionPosChecker0(pos+2, pos+1, false, 0, 0);
 	}
 	
 	public void testSelectionOnMethod() throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method1.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("cool_method");
 		
 		IModelElement[] elements = cu.codeSelect(start + 1, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IMethod method = cu.getType("Bar").getMethod("cool_method");
 		assertNotNull(method);
 		assertEquals(method, elements[0]);
 	}
 	
 	public void testSelectionOnClassDeclaraion() throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method1.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("Foo");
 		
 		IModelElement[] elements = cu.codeSelect(start + 1, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IType type = cu.getType("Foo");
 		assertNotNull(type);
 		assertEquals(type, elements[0]);
 	}
 	
 	public void testSelectionOnClassUsage() throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method1.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("Bar");
 		
 		IModelElement[] elements = cu.codeSelect(start + 1, 0);
 		assertNotNull(elements);
 		assertEquals(2, elements.length);
 //		IType type = cu.getType("Bar");
 //		assertNotNull(type);
 //		assertEquals(type, elements[0]);
 	}
 	
 	public void testSelectionOnMethodDeclaration() throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method1.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("doo");
 		
 		IModelElement[] elements = cu.codeSelect(start + 1, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IMethod method = cu.getType("Foo").getMethod("doo");
 		assertNotNull(method);
 		assertEquals(method, elements[0]);
 	}
 	
 	public void testSelectionOnMethod2() throws ModelException { // NIM = not in method
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method1.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("Bar.new.cool_method");
 		
 		IModelElement[] elements = cu.codeSelect(start + "Bar.new.".length() + 2, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IMethod method = cu.getType("Bar").getMethod("cool_method");
 		assertNotNull(method);
 		assertEquals(method, elements[0]);
 	}
 	
 	public void testSelectionOnLocalVariable() throws ModelException { // NIM = not in method
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method1.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("ff = Foo.new") + 1;
 		
 		IModelElement[] elements = cu.codeSelect(start, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		// TODO: require LocalVariable model element
 	}
 	
 	public void testSelectionOnLocalVariableMethodNIM() throws ModelException { // NIM = not in method
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method1.rb");
 		
 
 		String source = cu.getSource();
 		int start = source.indexOf("f.doo");
 		
 		IModelElement[] elements = cu.codeSelect(start + 3, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IMethod method = cu.getType("Foo").getMethod("doo");
 		assertNotNull(method);
 		assertEquals(method, elements[0]);
 	}
 	
 	public void testSelectionOnMethod2_1() throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method2.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("boz.dining_philosopher") + 5;
 		
 		IModelElement[] elements = cu.codeSelect(start, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IMethod method = cu.getType("Foo").getMethod("dining_philosopher");
 		assertNotNull(method);
 		assertEquals(method, elements[0]);
 	}
 	
 	public void testSelectionOnMethod2_2() throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method2.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("ultimate_answer") + 1;
 		
 		IModelElement[] elements = cu.codeSelect(start, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IMethod method = cu.getType("Foo").getMethod("ultimate_answer");
 		assertNotNull(method);
 		assertEquals(method, elements[0]);
 	}
 	
 	public void _testSelectionOnMethod3_2() throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src",
 		"selection_on_method3.rb");
 
 		String source = cu.getSource();
 		int start = source.indexOf("megathing") + 1;
 		
 		IModelElement[] elements = cu.codeSelect(start, 0);
 		assertNotNull(elements);
 		assertEquals(1, elements.length);
 		IMethod method = cu.getMethod("megathing");
 		assertNotNull(method);
 		assertEquals(method, elements[0]);
 	}
 	
 	
 	
 	
 	
 	
 	
 	//////
 	public void executeTest (String module, int where, ArrayList destinations) throws ModelException {
 		ISourceModule cu = getSourceModule(SELECTION_PROJECT, "src", module);
 				
 		IModelElement[] elements = cu.codeSelect(where, 0);
 		assertNotNull(elements);
 		assertEquals(destinations.size(), elements.length);
 		
 		for (int i = 0; i < elements.length; i++) {
 			assertNotNull(elements[i]);
 //			int pos = ((Integer)(destinations.get(i))).intValue();
 //			
 //			ISourceRange sourceRange = null;
 //			if (elements[i] instanceof SourceType) {
 //				SourceType sourceType = (SourceType) elements[i];
 //				sourceRange = sourceType.getNameRange();
 //			} else if (elements[i] instanceof SourceMethod) {
 //				SourceMethod sourceMethod = (SourceMethod)elements[i];
 //				sourceRange = sourceMethod.getNameRange();
 //			} else {
 //				sourceRange = ((ISourceReference)elements[i]).getSourceRange();
 //			}
 //			
 //			
 //			
 //		
 //			assertTrue(sourceRange.getOffset() <= pos);
 //			assertTrue(pos < sourceRange.getOffset() + sourceRange.getLength());
 		}
 		
 	}
 	
 	
 	
 	
 	
 }
