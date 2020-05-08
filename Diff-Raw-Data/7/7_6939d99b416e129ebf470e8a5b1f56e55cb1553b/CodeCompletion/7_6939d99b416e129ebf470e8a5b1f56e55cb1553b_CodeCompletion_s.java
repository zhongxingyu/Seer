 package org.eclipse.dltk.javascript.core.tests.contentassist;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import junit.framework.TestCase;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.env.ISourceModule;
 import org.eclipse.dltk.core.CompletionProposal;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IOpenable;
 import org.eclipse.dltk.core.IPackageDeclaration;
 import org.eclipse.dltk.core.IProblemRequestor;
 import org.eclipse.dltk.core.IScriptModel;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.ISearchableEnvironment;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.NameLookup;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 import org.eclipse.dltk.javascript.internal.core.codeassist.completion.JavaScriptCompletionEngine;
 import org.eclipse.dltk.utils.CorePrinter;
 
 public class CodeCompletion extends TestCase {
 
 	private final class TestModule extends ModelElement implements ISourceModule,org.eclipse.dltk.core.ISourceModule {
 
 		String content;
 		String elementName;
 		public TestModule(String string) {
 			super(null);
 			this.content = string;
 			this.elementName = "noname.js";
 		}
 
 		public TestModule(URL resource) {
 			super(null);
 			try {
 				StringBuffer bf = new StringBuffer();
 				DataInputStream str = new DataInputStream(resource.openStream());
 				this.elementName = new Path( resource.getPath() ).lastSegment();
 				ByteArrayOutputStream bs = new ByteArrayOutputStream();
 				while (str.available() >= 0) {
 					int k = str.read();
 					if (k == -1)
 						break;
 					else
 						bs.write(k);
 				}
 				this.content = new String(bs.toByteArray(), "UTF-8");
 			} catch (IOException e) {
 				throw new IllegalArgumentException();
 			}
 		}
 
 		public IModelElement getModelElement() {
 			return null;
 		}
 
 		public IPath getScriptFolder() {
 			return null;
 		}
 
 		public String getSourceContents() {
 			return content;
 		}
 
 		public char[] getContentsAsCharArray() {
 			return content.toCharArray();
 		}
 
 		public char[] getFileName() {
 			return "".toCharArray();
 		}
 
 		public void becomeWorkingCopy(IProblemRequestor problemRequestor,
 				IProgressMonitor monitor) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void commitWorkingCopy(boolean force, IProgressMonitor monitor)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void discardWorkingCopy() throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public IModelElement getElementAt(int position) throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IField getField(String string) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IField[] getFields() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IMethod getMethod(String name) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public WorkingCopyOwner getOwner() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IPackageDeclaration getPackageDeclaration(String name) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IPackageDeclaration[] getPackageDeclarations()
 				throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public org.eclipse.dltk.core.ISourceModule getPrimary() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public String getSource() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IType getType(String name) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IType[] getTypes() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public org.eclipse.dltk.core.ISourceModule getWorkingCopy(
 				IProgressMonitor monitor) throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public org.eclipse.dltk.core.ISourceModule getWorkingCopy(
 				WorkingCopyOwner owner, IProblemRequestor problemRequestor,
 				IProgressMonitor monitor) throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean isPrimary() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean isReadOnly() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean isWorkingCopy() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void reconcile(boolean forceProblemDetection,
 				WorkingCopyOwner owner, IProgressMonitor monitor)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public boolean exists() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public IModelElement getAncestor(int ancestorType) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IResource getCorrespondingResource() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public String getElementName() {
 			// TODO Auto-generated method stub
 			return elementName;
 		}
 
 		public int getElementType() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public String getHandleIdentifier() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IScriptModel getModel() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IOpenable getOpenable() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IModelElement getParent() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IPath getPath() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IModelElement getPrimaryElement() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IResource getResource() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IScriptProject getScriptProject() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IResource getUnderlyingResource() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean isStructureKnown() throws ModelException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public Object getAdapter(Class adapter) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IModelElement[] getChildren() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean hasChildren() throws ModelException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void close() throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public IBuffer getBuffer() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean hasUnsavedChanges() throws ModelException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean isConsistent() throws ModelException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean isOpen() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void makeConsistent(IProgressMonitor progress)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void open(IProgressMonitor progress) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void save(IProgressMonitor progress, boolean force)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public ISourceRange getSourceRange() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public void copy(IModelElement container, IModelElement sibling,
 				String rename, boolean replace, IProgressMonitor monitor)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void delete(boolean force, IProgressMonitor monitor)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void move(IModelElement container, IModelElement sibling,
 				String rename, boolean replace, IProgressMonitor monitor)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void rename(String name, boolean replace,
 				IProgressMonitor monitor) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void codeComplete(int offset, CompletionRequestor requestor)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void codeComplete(int offset, CompletionRequestor requestor,
 				WorkingCopyOwner owner) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public IModelElement[] codeSelect(int offset, int length)
 				throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IModelElement[] codeSelect(int offset, int length,
 				WorkingCopyOwner owner) throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		protected void closing(Object info) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		protected Object createElementInfo() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		protected void generateInfos(Object info, HashMap newElements,
 				IProgressMonitor pm) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public IModelElement getHandleFromMemento(String token,
 				MementoTokenizer memento, WorkingCopyOwner owner) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		protected char getHandleMementoDelimiter() {
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		public void printNode(CorePrinter output) {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public IType[] getAllTypes() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public char[] getSourceAsCharArray() throws ModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean isBuiltin() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public void codeComplete(int offset, CompletionRequestor requestor,
 				long timeout) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 
 		public void codeComplete(int offset, CompletionRequestor requestor,
 				WorkingCopyOwner owner, long timeout) throws ModelException {
 			// TODO Auto-generated method stub
 			
 		}
 	}
 
 	private final class TestCompletionRequetor extends CompletionRequestor {
 		LinkedList results;
 
 		public TestCompletionRequetor(LinkedList results2) {
 			this.results = results2;
 		}
 
 		public void accept(CompletionProposal proposal) {
 			results.add(proposal);
 		}
 	}
 
 	private final class NullEnvironment implements ISearchableEnvironment {
 //		public void findPackages(char[] prefix, ISearchRequestor requestor) {
 //			// TODO Auto-generated method stub
 //
 //		}
 
 //		public void findTypes(char[] prefix, boolean findMembers,
 //				boolean camelCaseMatch, ISearchRequestor storage) {
 //			// TODO Auto-generated method stub
 //
 //		}
 
 		public NameLookup getNameLookup() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public void cleanup() {
 			// TODO Auto-generated method stub
 
 		}
 
 //		public NameEnvironmentAnswer findType(char[][] compoundTypeName) {
 //			return null;
 //		}
 //
 //		public NameEnvironmentAnswer findType(char[] typeName,
 //				char[][] packageName) {
 //			return null;
 //		}
 
 		public boolean isPackage(char[][] parentPackageName, char[] packageName) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 	}
 
 	public JavaScriptCompletionEngine createEngine(LinkedList results) {
 		ISearchableEnvironment env = new NullEnvironment();
 		JavaScriptCompletionEngine engine = new JavaScriptCompletionEngine();
 		engine.setRequestor(new TestCompletionRequetor(results));
 		return engine;
 		
 //		throw new Error("Unimplemented, please fix");
 	}
 	
 	private int lastPositionInFile(String string, String moduleName) {
 		URL resource = this.getClass().getResource(moduleName);
 		DataInputStream str;
 		try {
 			str = new DataInputStream(resource.openStream());
 		} catch (IOException e1) {
 			return -1;
 		}
 		ByteArrayOutputStream bs = new ByteArrayOutputStream();
 		try {
 			while (str.available() >= 0) {
 				int k = str.read();
 				if (k == -1)
 					break;
 				else
 					bs.write(k);
 			}
 		} catch (IOException e) {
 			return -1;
 		}
 		String content = "";
 		try {
 			content = new String(bs.toByteArray(), "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			return -1;
 		}
 		
 		int position = content.lastIndexOf(string); 
 		if (position >= 0){
 			return position + string.length();
 		}
 		return -1;
 	}
 
 	private void compareNames(LinkedList results, String[] names) {
 		assertEquals(names.length,results.size());
 		Collections.sort(results,new Comparator(){
 
 			public int compare(Object arg0, Object arg1) {
 				CompletionProposal pr=(CompletionProposal) arg0;
 				CompletionProposal pr1=(CompletionProposal) arg1;
 				return new String(pr.getName()).compareTo(new String(pr1.getName()));
 			}
 			
 		});
 		Arrays.sort(names);
 		Iterator it=results.iterator();
 		int pos=0;
 		while (it.hasNext()){
 			CompletionProposal pr=(CompletionProposal) it.next();
 			assertEquals(names[pos],new String(pr.getName()));
 			pos++;
 		}
 	}
 	
 	private void basicTest(String mname,int position,String[] compNames){
 		LinkedList results = new LinkedList();
 		JavaScriptCompletionEngine c = createEngine(results);
 		c.setUseEngine(false);
 		c.complete(new TestModule(this.getClass().getResource(mname)), position,
 				0);
 		//assertEquals(2, results.size());
 		compareNames(results,compNames);
 	}
 
 	/**
 	 * dumb completion on keyword
 	 */
 	public void test0() {
 		LinkedList results = new LinkedList();
 		JavaScriptCompletionEngine c = createEngine(results);
 		c.setUseEngine(false);
 		c.complete(new TestModule(""), 0, 0);
 		assertEquals(0, results.size());
 	}
 
 	/**
 	 * dumb completion on function
 	 */
 	public void test1() {
 		LinkedList results = new LinkedList();
 		JavaScriptCompletionEngine c = createEngine(results);
 		c.setUseEngine(false);
 		c.complete(new TestModule(this.getClass().getResource("test1.js")), 0,
 				0);
 		assertEquals(0, results.size());
 	}
 	
 	/**
 	 * dumb completion on function
 	 */
 	public void test2() {
 		String[] names = new String[] { "firstVar", "secondVar" };
 		String module = "test2.js";
 		int position = lastPositionInFile("\n", module);
 		basicTest(module, position, names);
 		// basicTest("test2.js", 36, names);
 	}
 	
 	/**
 	 * dumb completion on function
 	 */
 	public void test3() {		
 		String[] names=new String[]{"world"};
 		String module = "test3.js";
 		int position = lastPositionInFile("firstVar.", module);
 		basicTest(module, position, names);		
 //		basicTest(module, 63, names);		
 	}
 	//104 ,temperature
 	/**
 	 * dumb completion on function
 	 */
 	public void test4() {
 		String[] names=new String[]{"temperature","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};	
 		String module = "test4.js";
 		int position = lastPositionInFile("firstVar.world.", module);
 		basicTest(module, position, names);		
 //		basicTest("test4.js", 100, names);		
 	}
 	
 	public void test5() {
 		String[] names=new String[]{"world","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		String[] names1=new String[]{"temperature","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		String module = "test5.js";
 		int positionFirst = lastPositionInFile("secondVar.", module);
 		basicTest(module, positionFirst, names);
 		int positionSecond = lastPositionInFile("world.", module);
 		basicTest(module, positionSecond, names1);
 //		basicTest(module, 120, names);
 //		basicTest(module, 126, names1);
 	}
 	
 	public void test6() {
 		String[] names=new String[]{"world","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		//String[] names1=new String[]{"temperature"};
 		String module = "test6.js";
 		int position = lastPositionInFile("world.", module);
 		basicTest(module, position, names);		
 //		basicTest(module, 158, names);
 		//basicTest(module, 126, names1);
 	}
 	public void test7() {
 		String[] names=new String[]{"world","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		//String[] names1=new String[]{"temperature"};
 		String module = "test7.js";
 		int position = lastPositionInFile("world.", module);
 		basicTest(module, position, names);		
 //		basicTest(module, 157, names);
 		//basicTest(module, 126, names1);
 	}
 	public void test8() {
 		String[] names=new String[]{"mission","target","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		//String[] names1=new String[]{"temperature"};
 		String module = "test8.js";
 		int position = lastPositionInFile("firstVar.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 124, names);
 		//basicTest(module, 126, names1);
 	}
 	public void test9() {
 		String[] names=new String[]{"toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		//String[] names1=new String[]{"temperature"};
 		String module = "test9.js";
 		int position = lastPositionInFile("firstVar.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 138, names);
 		//basicTest(module, 126, names1);
 	}
 	
 	public void test10() {
 		String[] names=new String[]{"mission","target","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		//String[] names1=new String[]{"temperature"};
 		String module = "test10.js";
 //		basicTest(module, 139, names);
 		int position = lastPositionInFile("secondVar.", module);
 		basicTest(module, position, names);
 	}
 	
 	public void test11() {
 		String[] names=new String[]{"firstVar","secondVar"};
 		String module = "test11.js";
 		int position = lastPositionInFile("{", module);
 		basicTest(module, position, names);
 //		basicTest(module, 56, names);
 	}
 	
 	public void test12() {
 		String[] names=new String[]{"p0","p1"};
 		String module = "test12.js";
 //		basicTest(module, 62, names);
 		int position = lastPositionInFile("p", module);
 		basicTest(module, position, names);
 	}
 	
 	public void test13() {
 		String[] names=new String[]{"element","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		String module = "test13.js";
 		int position = lastPositionInFile("firstVar.", module);
 		basicTest(module, position, names);
 	}
 	
 	public void test14() {
 		String[] names=new String[]{"element","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		String module = "test14.js";
 		int position = lastPositionInFile("firstVar.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 88, names);
 	}
 	
 	public void test15() {
 		String[] names=new String[]{"toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf"};
 		String module = "test15.js";
 		int position = lastPositionInFile("firstVar.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 104, names);
 	}
 	
 	public void test16() {
 		String[] names=new String[]{"xaml"};
 		String module = "test16.js";
 		int position = lastPositionInFile("x", module);
 		basicTest(module, position, names);
 //		basicTest(module, 86, names);
 	}
 	
 	public void test17() {
 		String[] names=new String[]{};
 		String module = "test17.js";
 		int position = lastPositionInFile("x", module);
 		basicTest(module, position, names);
 //		basicTest(module, 87, names);
 	}
 	
 	public void test18() {
 		String[] names=new String[]{"my"};
 		String module = "test18.js";
 		int position = lastPositionInFile("hello.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 120, names);
 	}
 	
 	public void test19() {
 		String[] names=new String[]{"toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf"};
 		String module = "test19.js";
 		int position = lastPositionInFile("hello.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 119, names);
 	}
 	
 	public void test20() {
 		String[] names=new String[]{"my","toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf"};
 		String module = "test20.js";
 		int position = lastPositionInFile("hello.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 123, names);
 	}
 	
 	public void test21() {
 		String[] names=new String[]{"favorite","toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf"};
 		String module = "test21.js";
 		int position = lastPositionInFile("hello.my.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 151, names);
 	}
 	
 	public void test22() {
 		String[] names=new String[]{"toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf"};
 		String module = "test22.js";
 		int position = lastPositionInFile("hello.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 147, names);
 	}
 	
 	public void test23() {
 		String[] names=new String[]{"my","olive"};
 		String module = "test23.js";
 		int position = lastPositionInFile("hello.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 160, names);
 	}
 	
 	public void test24() {
 		String[] names=new String[]{"age","toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf","wine"};
 		String module = "test24.js";
 		int position = lastPositionInFile("hello.olive.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 215, names);
 	}
 	
 	public void test25() {
 		String[] names=new String[]{"age","my","olive","toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf","wine"};
 
 		String module = "test25.js";
 		int position = lastPositionInFile("hello.olive.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 219, names);
 	}
 	
 	public void test26() {
 		String[] names=new String[]{"toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf","wine"};
 
 		String module = "test26.js";
 		int position = lastPositionInFile("hello.olive.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 256, names);
 	}
 	
 	public void test27() {
 		String[] names=new String[]{"olive"};
 		String module = "test27.js";
 		int position = lastPositionInFile("hello.o", module);
 		basicTest(module, position, names);
 //		basicTest(module, 131, names);
 	}
 	
 	public void test28() {
 		String[] names=new String[]{"toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf"};
 		String module = "test28.js";
 		int position = lastPositionInFile("hello.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 118, names);
 	}
 	
 	public void test29() {
 		String[] names=new String[]{"x"};
 		String module = "test29.js";
 		int position = lastPositionInFile("{", module);
 		basicTest(module, position, names);
 //		basicTest(module, 46, names);
 	}
 	
 	public void test30() {
 		String[] names=new String[]{"toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf","xsd"};
 		String module = "test30.js";
 		int position = lastPositionInFile("sz.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 79, names);
 	}
 	
 	public void test31() {
 		String[] names=new String[]{"x","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		String module = "test31.js";
 		int position = lastPositionInFile("node.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 107, names);
 	}
 	
 	public void test32() {
 		String[] names=new String[]{"x","toString","toSource","valueOf","charAt","charCodeAt","indexOf","lastIndexOf","split","substring","toLowerCase","toUpperCase","substr","concat","slice","bold","italics","fixed","strike","small","big","blink","sup","sub","fontsize","fontcolor","link","anchor","equals","equalsIgnoreCase","match","search","replace","localeCompare","toLocaleLowerCase","toLocaleUpperCase"};
 		String module = "test32.js";
 		int position = lastPositionInFile("node.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 110, names);
 	}
 	
 	public void test33() {
 		String[] names=new String[]{"object","objectVariable"};
 		String module = "test33.js";
 		int position = lastPositionInFile("{", module);
 		basicTest(module, position, names);
 //		basicTest(module, 65, names);
 	}
 	
 	public void test34() {
 		String[] names=new String[]{"x2"};
 		String module = "test34.js";
 		int position = lastPositionInFile("object.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 93, names);
 	}
 	
 	public void test35() {
 		String[] names=new String[]{"toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf","x"};
 		String module = "test35.js";
 		int position = lastPositionInFile("c.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 82, names);
 	}
 	
 	public void test36() {
 		String[] names=new String[]{"e"};
 		String module = "test36.js";
 		int position = lastPositionInFile("{", module);
 		basicTest(module, position, names);
 //		basicTest(module, 76, names);
 	}
 	
 	public void test37() {
 		String[] names=new String[]{"forward","hello","object"};
 		String module = "test37.js";
 		int position = lastPositionInFile("{", module);
 		basicTest(module, position, names);
 //		basicTest(module, 85, names);
 	}
 	
 	public void test38() {
 		String[] names=new String[]{"object"};
 		String module = "test38.js";
 		int position = lastPositionInFile("o", module);
 		basicTest(module, position, names);
 //		basicTest(module, 102, names);
 	}
 	
 	public void test39() {
 		String[] names=new String[]{"er","toExponential","toFixed","toLocaleString","toPrecision","toSource","toString","valueOf"};
 		String module = "test39.js";
 		int position = lastPositionInFile("eer.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 125, names);
 	}
 	
 	public void test40() {
 		String[] names=new String[]{"s"};
 		String module = "test40.js";
 		int position = lastPositionInFile("v.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 65, names);
 	}
 	
 	public void test41() {
 		String[] names=new String[]{"hello"};
 		String module = "test41.js";
 		int position = lastPositionInFile("er[0].", module);
 		basicTest(module, position, names);
 //		basicTest(module, 96, names);
 	}
 	
 	public void test42() {
 		String[] names=new String[]{"Hello","Mama"};
 		String module = "test42.js";
 		int position = lastPositionInFile("object.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 76, names);
 	}
 	
 	public void test43() {
 		String[] names=new String[]{};
 		String module = "test43.js";
 		int position = lastPositionInFile(";", module);
 		basicTest(module, position, names);
 //		basicTest(module, 45, names);
 	}
 	
 	public void test44() {
 		String[] names=new String[]{"main"};
 		String module = "test44.js";
 		int position = lastPositionInFile("object.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 95, names);
 	}
 	
 	public void REM_test45() {
 		String[] names=new String[]{"word","other_word"};
 		String module = "test45.js";
 		int position = lastPositionInFile("x.hello.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 139, names);
 	}
 	
 	public void test46() {
 		String[] names=new String[]{"aaa","baa","my","prototype"};
 		String module = "test46.js";
 		int position = lastPositionInFile("x.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 102, names);
 	}
 	
 	public void test47() {
 		String[] names=new String[]{"x","y"};
 		String module = "test47.js";
 		int position = lastPositionInFile("this.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 54, names);
 	}
 	
 	public void REM_test48() {
 		String[] names=new String[]{"erer"};
 		String module = "test48.js";
 		int position = lastPositionInFile("this.", module);
 		basicTest(module, position, names);
 //		basicTest(module, 105, names);
 	}
 }
