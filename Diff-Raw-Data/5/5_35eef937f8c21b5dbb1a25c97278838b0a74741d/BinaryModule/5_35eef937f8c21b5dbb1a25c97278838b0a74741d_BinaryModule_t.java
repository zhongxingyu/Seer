 package org.eclipse.dltk.core.model.binary;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IExternalSourceModule;
 import org.eclipse.dltk.core.IField;
 import org.eclipse.dltk.core.IMethod;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelProvider;
 import org.eclipse.dltk.core.IProblemRequestor;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ISourceRange;
 import org.eclipse.dltk.core.IType;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.internal.core.AbstractSourceModule;
 import org.eclipse.dltk.internal.core.MementoModelElementUtil;
 import org.eclipse.dltk.internal.core.ModelElement;
 import org.eclipse.dltk.internal.core.ModelProviderManager;
 import org.eclipse.dltk.internal.core.OpenableElementInfo;
 import org.eclipse.dltk.internal.core.util.MementoTokenizer;
 import org.eclipse.dltk.utils.CorePrinter;
import org.eclipse.osgi.util.NLS;
 
 /**
  * @since 2.0
  */
 public class BinaryModule extends AbstractSourceModule implements
 		IBinaryModule, IExternalSourceModule, ISourceMapperProvider,
 		org.eclipse.dltk.compiler.env.ISourceModule {
 
 	protected BinaryModule(ModelElement parent, String name,
 			WorkingCopyOwner owner) {
 		super(parent, name, owner);
 	}
 
 	@Override
 	protected Object createElementInfo() {
 		return new BinaryModuleElementInfo();
 	}
 
 	@Override
 	protected IStatus validateSourceModule(IDLTKLanguageToolkit toolkit,
 			IResource resource) {
 		return Status.OK_STATUS;
 	}
 
 	@Override
 	protected IStatus validateSourceModule(IResource resource)
 			throws CoreException {
 		return Status.OK_STATUS;
 	}
 
 	@Override
 	public boolean exists() {
 		return true;
 	}
 
 	@Override
 	protected boolean buildStructure(OpenableElementInfo info,
 			IProgressMonitor pm, Map newElements, IResource underlyingResource)
 			throws ModelException {
 		final BinaryModuleElementInfo moduleInfo = (BinaryModuleElementInfo) info;
 
 		IBinaryElementParser binaryParser = DLTKLanguageManager
 				.getBinaryElementParser(this);
 		if (binaryParser == null) {
			DLTKCore.error(NLS.bind("Binary parser for {0} not found",
					getPath()));
 			return false;
 		}
 		BinaryModuleStructureRequestor requestor = new BinaryModuleStructureRequestor(
 				this, moduleInfo, this.getSourceMapper(), newElements);
 		binaryParser.setRequestor(requestor);
 		binaryParser.parseBinaryModule(this);
 
 		// We need to update children contents using model providers
 		List<IModelElement> childrenSet = new ArrayList<IModelElement>(
 				moduleInfo.getChildrenAsList());
 		// Call for extra model providers
 		IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(this);
 		IModelProvider[] providers = ModelProviderManager.getProviders(toolkit
 				.getNatureId());
 		if (providers != null) {
 			for (int i = 0; i < providers.length; i++) {
 				providers[i].provideModelChanges(this, childrenSet);
 			}
 		}
 		moduleInfo.setChildren(childrenSet);
 
 		return moduleInfo.isStructureKnown();
 	}
 
 	public IResource getResource() {
 		return null;
 	}
 
 	public SourceMapper getSourceMapper() {
 		IModelElement parent = getParent();
 		if (parent instanceof ISourceMapperProvider) {
 			return ((ISourceMapperProvider) parent).getSourceMapper();
 		}
 		return null;
 	}
 
 	public boolean isBinary() {
 		return true;
 	}
 
 	@Override
 	public boolean isReadOnly() {
 		return true;
 	}
 
 	@Override
 	protected char getHandleMementoDelimiter() {
 		return JEM_USER_ELEMENT;
 	}
 
 	@Override
 	public void printNode(CorePrinter output) {
 	}
 
 	public int getElementType() {
 		// TODO: Replace with BINARY_MODULE then full support of binary modules
 		// will be implemented.
 		return SOURCE_MODULE;
 	}
 
 	public String getSource() throws ModelException {
 		SourceMapper mapper = getSourceMapper();
 		if (mapper != null) {
 			String content = mapper.getSource(this);
 			if (content != null) {
 				return content;
 			}
 		}
 		return "//Binary source are not available";
 	}
 
 	public ISourceRange getSourceRange() throws ModelException {
 		SourceMapper mapper = getSourceMapper();
 		if (mapper != null) {
 			return mapper.getSourceRange(this);
 		}
 		return null;
 	}
 
 	public char[] getSourceAsCharArray() throws ModelException {
 		return getSource().toCharArray();
 	}
 
 	public boolean equals(Object obj) {
 		if (!(obj instanceof BinaryModule)) {
 			return false;
 		}
 		return super.equals(obj);
 	}
 
 	public void becomeWorkingCopy(IProblemRequestor problemRequestor,
 			IProgressMonitor monitor) throws ModelException {
 	}
 
 	public void commitWorkingCopy(boolean force, IProgressMonitor monitor)
 			throws ModelException {
 	}
 
 	public void discardWorkingCopy() throws ModelException {
 	}
 
 	public ISourceModule getPrimary() {
 		return this;
 	}
 
 	public ISourceModule getWorkingCopy(IProgressMonitor monitor)
 			throws ModelException {
 		return this;
 	}
 
 	public ISourceModule getWorkingCopy(WorkingCopyOwner owner,
 			IProblemRequestor problemRequestor, IProgressMonitor monitor)
 			throws ModelException {
 		return this;
 	}
 
 	public boolean isBuiltin() {
 		return false;
 	}
 
 	public boolean isPrimary() {
 		return true;
 	}
 
 	public boolean isWorkingCopy() {
 		return true;
 	}
 
 	public void reconcile(boolean forceProblemDetection,
 			WorkingCopyOwner owner, IProgressMonitor monitor)
 			throws ModelException {
 	}
 
 	public void copy(IModelElement container, IModelElement sibling,
 			String rename, boolean replace, IProgressMonitor monitor)
 			throws ModelException {
 	}
 
 	public void delete(boolean force, IProgressMonitor monitor)
 			throws ModelException {
 	}
 
 	public void move(IModelElement container, IModelElement sibling,
 			String rename, boolean replace, IProgressMonitor monitor)
 			throws ModelException {
 	}
 
 	public void rename(String name, boolean replace, IProgressMonitor monitor)
 			throws ModelException {
 	}
 
 	@Override
 	protected char[] getBufferContent() throws ModelException {
 		return getSource().toCharArray();
 	}
 
 	@Override
 	protected String getModuleType() {
 		return "Binary Source Module";
 	}
 
 	@Override
 	protected ISourceModule getOriginalSourceModule() {
 		return this;
 	}
 
 	@Override
 	protected String getNatureId() throws CoreException {
 		IDLTKLanguageToolkit lookupLanguageToolkit = lookupLanguageToolkit(getScriptProject());
 		if (lookupLanguageToolkit == null) {
 			return null;
 		}
 		return lookupLanguageToolkit.getNatureId();
 	}
 
 	public String getFileName() {
 		return this.getPath().toOSString();
 	}
 
 	@Override
 	public IPath getPath() {
 		return this.getParent().getPath().append(this.getElementName());
 	}
 
 	public InputStream getContents() throws CoreException {
 		return new ByteArrayInputStream(getSource().getBytes());
 	}
 
 	public IPath getFullPath() {
 		return getPath();
 	}
 
 	public String getName() {
 		return getPath().lastSegment();
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
 
 	@Override
 	public IType getType(String typeName) {
 		try {
 			IType[] types = getTypes();
 			for (IType type : types) {
 				if (type.getElementName().equals(typeName)) {
 					return type;
 				}
 			}
 		} catch (ModelException e) {
 		}
 		return new BinaryType(parent, typeName);
 	}
 
 	@Override
 	public IMethod getMethod(String selector) {
 		try {
 			IMethod[] methods = getMethods();
 			for (IMethod method : methods) {
 				if (method.getElementName().equals(selector)) {
 					return method;
 				}
 			}
 		} catch (ModelException e) {
 		}
 		return new BinaryMethod(parent, selector);
 	}
 
 	@Override
 	public IField getField(String fieldName) {
 		try {
 			IField[] fields = getFields();
 			for (IField field : fields) {
 				if (field.getElementName().equals(fieldName)) {
 					return field;
 				}
 			}
 		} catch (ModelException e) {
 		}
 		return new BinaryField(parent, fieldName);
 	}
 }
