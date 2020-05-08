 package org.eclipse.dltk.core.model.binary;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.MementoModelElementUtil;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.NamedMember;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 
 /**
  * @since 2.0
  */
 public abstract class BinaryMember extends NamedMember {
 	private int flags;
 	private List<IModelElement> children = new ArrayList<IModelElement>();
 
 	public BinaryMember(ModelElement parent, String name) {
 		super(parent, name);
 	}
 
 	@Override
	public boolean exists() {
		return true;
	}

	@Override
 	protected char getHandleMementoDelimiter() {
 		return JEM_USER_ELEMENT;
 	}
 
 	@Override
 	public ISourceRange getSourceRange() throws ModelException {
 		return getSourceMapper().getSourceRange(this);
 	}
 
 	public SourceMapper getSourceMapper() {
 		IModelElement module = getAncestor(SOURCE_MODULE);
 		if (module instanceof BinaryModule) {
 			return ((BinaryModule) module).getSourceMapper();
 		}
 		return null;
 	}
 
 	@Override
 	public int getFlags() throws ModelException {
 		return flags;
 	}
 
 	void setFlags(int flags) {
 		this.flags = flags;
 	}
 
 	@Override
 	public IModelElement[] getChildren(IProgressMonitor monitor)
 			throws ModelException {
 		return children.toArray(new IModelElement[children.size()]);
 	}
 
 	public void addChild(IModelElement element) {
 		this.children.add(element);
 	}
 
 	public void removeChild(IModelElement element) {
 		this.children.remove(element);
 	}
 
 	public IModelElement getHandleFromMemento(String token,
 			MementoTokenizer memento, WorkingCopyOwner workingCopyOwner) {
 		switch (token.charAt(0)) {
 		case JEM_USER_ELEMENT:
 			return MementoModelElementUtil.getHandleFromMemento(memento, this,
 					workingCopyOwner);
 		}
 
 		return null;
 	}
 }
