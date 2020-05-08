 /**
  * 
  */
 package org.eclipse.dltk.javascript.jdt.integration;
 
 import java.io.Reader;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.CompletionRequestor;
 import org.eclipse.dltk.core.IBuffer;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IForeignElement;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IPackageDeclaration;
 import org.eclipse.dltk.core.IProblemRequestor;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.javascript.reference.resolvers.IResolvableMember;
 import org.eclipse.dltk.internal.javascript.typeinference.FakeField;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMember;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jdt.ui.JavadocContentAccess;
 import org.eclipse.ui.PartInitException;
 
 final class JavaReferenceFakeField extends FakeField implements ISourceModule,IForeignElement,IResolvableMember{
 	
 
 	IJavaElement element;
 	
 	public IResource getResource(){
 		return element.getResource();
 	}
 	
 	JavaReferenceFakeField(ModelElement parent, String name,
 			int offset, int length,IJavaElement res) {
 		super(parent, name, offset, length);
 		this.element=res;
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
 
 	public ISourceModule getPrimary() {
 		return this;
 	}
 
 	public IType getType(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public IType[] getTypes() throws ModelException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ISourceModule getWorkingCopy(IProgressMonitor monitor)
 			throws ModelException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ISourceModule getWorkingCopy(WorkingCopyOwner owner,
 			IProblemRequestor problemRequestor, IProgressMonitor monitor)
 			throws ModelException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public boolean isPrimary() {
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
 
 	public void codeSelect() {
 		try {
 			JavaUI.openInEditor(element,true,true);
 		} catch (PartInitException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JavaModelException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
        }
 
 	public Reader getInfo(
 			boolean lookIntoParents, boolean lookIntoExternal) {
 		try {
 			Reader contentReader = JavadocContentAccess.getHTMLContentReader((IMember) element, true, true);
 			System.out.println(element);
 			return contentReader;
 		} catch (JavaModelException e) {
 			return null;
 		}
 		
 	}
 
 	public IType[] getAllTypes() throws ModelException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public char[] getSourceAsCharArray() throws ModelException {
 		String source =  getSource();
 		if( source != null ) {
 			return source.toCharArray();
 		}
 		return null;
 	}
 }
