 package descent.internal.core.hierarchy;
 
 /**
  * This is the public entry point to resolve type hierarchies.
  *
  * When requesting additional types from the name environment, the resolver
  * accepts all forms (binary, source & compilation unit) for additional types.
  *
  * Side notes: Binary types already know their resolved supertypes so this
  * only makes sense for source types. Even though the compiler finds all binary
  * types to complete the hierarchy of a given source type, is there any reason
  * why the requestor should be informed that binary type X subclasses Y &
  * implements I & J?
  */
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 
 import descent.core.IClassFile;
 import descent.core.IJavaElement;
 import descent.core.IType;
 import descent.core.JavaModelException;
 import descent.core.dom.CompilationUnitResolver;
 import descent.core.dom.CompilationUnitResolver.ParseResult;
 import descent.internal.compiler.IProblemFactory;
 import descent.internal.compiler.env.ICompilationUnit;
 import descent.internal.compiler.env.IGenericType;
 import descent.internal.compiler.env.INameEnvironment;
 import descent.internal.compiler.parser.ASTDmdNode;
 import descent.internal.compiler.parser.BaseClass;
 import descent.internal.compiler.parser.BaseClasses;
 import descent.internal.compiler.parser.ClassDeclaration;
 import descent.internal.compiler.parser.Dsymbol;
import descent.internal.compiler.parser.ISignatureOptions;
 import descent.internal.compiler.parser.Module;
 import descent.internal.compiler.parser.Parser;
 import descent.internal.compiler.parser.SemanticContext;
 import descent.internal.compiler.parser.TemplateDeclaration;
 import descent.internal.compiler.parser.TemplateInstance;
 import descent.internal.compiler.parser.Type;
 import descent.internal.compiler.parser.TypeClass;
 import descent.internal.compiler.parser.TypeInstance;
 import descent.internal.core.BasicCompilationUnit;
 import descent.internal.core.InternalSignature;
 import descent.internal.core.JavaElement;
 import descent.internal.core.Openable;
 import descent.internal.core.util.Util;
 
 //TODO JDT Type Hierarchy NOW!
 public class HierarchyResolver {
 
 	private final INameEnvironment nameEnvironment;
 	private final Map options;
 	private final HierarchyBuilder builder;
 	private final IProblemFactory factory;
 	private TypeClass focusType;
 	private String focusTypeSignature;
 	private InternalSignature internalSignature;
 
 	public HierarchyResolver(INameEnvironment nameEnvironment, Map options, HierarchyBuilder builder, IProblemFactory factory) {
 		this.nameEnvironment = nameEnvironment;
 		this.options = options;
 		this.builder = builder;
 		this.factory = factory;
 		this.internalSignature = new InternalSignature(null);
 	}
 
 	public void resolve(IGenericType type) {
 		// TODO JDT Type Hierarchy NOW!
 	}
 
 	public void resolve(Openable[] openables, HashSet localTypes, IProgressMonitor monitor) {
 		try {
 			int openablesLength = openables.length;
 			ParseResult[] parsedUnits = new ParseResult[openablesLength];
 			descent.core.ICompilationUnit[] cus = new descent.core.ICompilationUnit[openablesLength];
 			
 			IType focus = this.builder.getType();
 			Openable focusOpenable = null;
 			if (focus != null) {
 				if (focus.isBinary()) {
 					focusOpenable = (Openable)focus.getClassFile();
 				} else {
 					focusOpenable = (Openable)focus.getCompilationUnit();
 				}
 			}
 			
 			// resolve
 			for (int i = 0; i < openablesLength; i++) {
 				Openable openable = openables[i];
 				descent.core.ICompilationUnit cu = (descent.core.ICompilationUnit)openable;
 				ICompilationUnit toParse = (ICompilationUnit) cu;
 				if (cu.getElementType() == IJavaElement.CLASS_FILE) {
 					IClassFile classFile = (IClassFile) cu;
 					IPath path = classFile.getPath();
 					char[] contents = Util.getFileContentsAsCharArray(path.toFile());
 					toParse = new BasicCompilationUnit(contents, toParse.getPackageName(), toParse.getFullyQualifiedName(), new String(toParse.getFileName()));
 				}
 				
 				ParseResult result = CompilationUnitResolver.parse(Parser.D1, toParse, options, false, false, true);
 				result.module.moduleName = cu.getFullyQualifiedName();
 				result.context = CompilationUnitResolver.resolve(result.module, cu.getJavaProject(), cu.getOwner(), result.encoder, true);
 				
 				parsedUnits[i] = result;
 				cus[i] = cu;
 				
 				if (openable.equals(focusOpenable)) {
 					searchFocusType(focus, result, cu);
 				}
 				
 				worked(monitor, 1);
 			}
 			
 			for (int i = 0; i < openablesLength; i++) {
 				connect(parsedUnits[i].module, parsedUnits[i].context, cus[i]);
 			}
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 		} finally {
 			// reset()
 		}
 	}
 
 	private void searchFocusType(IType focus, ParseResult result, descent.core.ICompilationUnit cu) throws JavaModelException {
 		Module module = result.module;
 		for(Object value : module.symtab.values()) {
 			if (value == null)
 				continue;
 			
 			Dsymbol sym = (Dsymbol) value;
 			ClassDeclaration clazz = getClassDeclaration(sym);
 			if (clazz != null) {
 				if (focus.equals(getJavaElement(clazz, cu))) {
 					focusType = getTypeClass(clazz.type);
					focusTypeSignature = focusType.getSignature(ISignatureOptions.None);
 					return;
 				}
 			}
 		}
 	}
 
 	private IJavaElement getJavaElement(ClassDeclaration clazz, descent.core.ICompilationUnit cu) throws JavaModelException {
 		IJavaElement element = clazz.getJavaElement();
 		if (element == null) {
 			element = internalSignature.binarySearch(cu, clazz.start,
 					clazz.start + clazz.length);
 		}
 		return element;
 	}
 
 	private void connect(Module module, SemanticContext context, descent.core.ICompilationUnit cu) throws JavaModelException {
 		for(Object value : module.symtab.values()) {
 			if (value == null)
 				continue;
 			
 			Dsymbol sym = (Dsymbol) value;
 			ClassDeclaration clazz = getClassDeclaration(sym);
 			if (clazz != null) {
 				TypeClass typeClass = getTypeClass(clazz.type);
 				connect(typeClass, cu);
 			}
 		}
 	}
 	
 	private void connect(TypeClass typeClass, descent.core.ICompilationUnit cu) throws JavaModelException  {
 		if (typeClass == null)
 			return;
 		
 		if (!subOrSuperOfFocus(typeClass)) {
 			return;
 		}
 		
 		ClassDeclaration clazz = typeClass.sym;
 		
 		IType typeHandle = (IType) getJavaElement(clazz, cu); 
 		IGenericType type = (IGenericType) ((JavaElement) typeHandle).getElementInfo();
 		IType superclassHandle = getSuperclassHandle(clazz, cu);
 		IType[] superinterfaceHandles = getSuperinterfaceHandles(clazz, cu);
 		
 		this.builder.connect(type, typeHandle, superclassHandle, superinterfaceHandles);
 		
 		BaseClasses baseclasses = clazz.baseclasses;
 		if (baseclasses != null) {
 			for (int i = 0, length = baseclasses.size(); i < length; i++) {
 				BaseClass superSome = (BaseClass) baseclasses.get(i);
 				TypeClass superType = getTypeClass(superSome.type);
 				connect(superType, cu);
 			} 
 		}
 	}
 
 	private IType getSuperclassHandle(ClassDeclaration clazz, descent.core.ICompilationUnit cu) throws JavaModelException {
 		BaseClasses baseclasses = clazz.baseclasses;
 		if (baseclasses != null) {
 			for (int i = 0, length = baseclasses.size(); i < length; i++) {
 				BaseClass superSome = (BaseClass) baseclasses.get(i);
 				TypeClass superType = getTypeClass(superSome.type);
 				if (superType != null && superType.sym != null && null == superType.sym.isInterfaceDeclaration()) {
 					return (IType) getJavaElement(superType.sym, cu);
 				}
 			} 
 		}
 		return null;
 	}
 	
 	private IType[] getSuperinterfaceHandles(ClassDeclaration clazz, descent.core.ICompilationUnit cu) throws JavaModelException {
 		List<IType> superinterfaces = new ArrayList<IType>(3);
 		BaseClasses baseclasses = clazz.baseclasses;
 		if (baseclasses != null) {
 			for (int i = 0, length = baseclasses.size(); i < length; i++) {
 				BaseClass superSome = (BaseClass) baseclasses.get(i);
 				TypeClass superType = getTypeClass(superSome.type);
 				if (superType != null && superType.sym != null && superType.sym.isInterfaceDeclaration() != null) {
 					superinterfaces.add((IType) getJavaElement(superType.sym, cu));
 				}
 			} 
 		}
 		return superinterfaces.toArray(new IType[superinterfaces.size()]);
 	}
 
 	private boolean subOrSuperOfFocus(TypeClass clazz) {
 		if (this.focusType == null) return true; // accept all types (case of hierarchy in a region)
 		
 		if (this.subTypeOfType(clazz, focusType, focusTypeSignature)) return true;
 		if (this.subTypeOfType(focusType, clazz, clazz.getSignature())) return true;
 		return false;
 	}
 
 	private boolean subTypeOfType(TypeClass subType, TypeClass typeBinding, String typeBindingSignature) {
 		if (typeBinding == null || subType == null) return false;
		if (subType == typeBinding || subType.getSignature(ISignatureOptions.None).equals(typeBindingSignature)) return true;
 		BaseClasses baseclasses = subType.sym.baseclasses;
 		if (baseclasses != null) {
 			for (int i = 0, length = baseclasses.size(); i < length; i++) {
 				BaseClass superSome = (BaseClass) baseclasses.get(i);
 				TypeClass typeClass = getTypeClass(superSome.type);
 				if (typeClass != null && this.subTypeOfType(typeClass, typeBinding, typeBindingSignature)) return true;
 			} 
 		}
 		return false;
 	}
 	
 	private ClassDeclaration getClassDeclaration(Dsymbol sym) {
 		if (sym.getNodeType() == ASTDmdNode.TEMPLATE_DECLARATION) {
 			TemplateDeclaration temp = (TemplateDeclaration) sym;
 			if (temp.wrapper) {
 				sym = temp.members.get(0);
 			}
 		}
 		if (sym.getNodeType() == ASTDmdNode.CLASS_DECLARATION || sym.getNodeType() == ASTDmdNode.INTERFACE_DECLARATION) {
 			ClassDeclaration clazz = (ClassDeclaration) sym;
 			return clazz;
 		}
 		return null;
 	}
 	
 	private TypeClass getTypeClass(Type type) {
 		if (type.getNodeType() == ASTDmdNode.TYPE_CLASS)
 			return (TypeClass) type;
 		if (type.getNodeType() == ASTDmdNode.TYPE_INSTANCE) {
 			TypeInstance inst = (TypeInstance) type;
 			TemplateInstance tempinst = inst.tempinst;
 			if (tempinst == null)
 				return null;
 			TemplateDeclaration tempdecl = tempinst.tempdecl;
 			if (tempdecl == null || !tempdecl.wrapper) {
 				return null;
 			}
 			Dsymbol sym = tempdecl.members.get(0);
 			return getTypeClass(sym.type());
 		}
 		return null;
 	}
 
 	protected void worked(IProgressMonitor monitor, int work) {
 		if (monitor != null) {
 			if (monitor.isCanceled()) {
 				throw new OperationCanceledException();
 			} else {
 				monitor.worked(work);
 			}
 		}
 	}
 	
 }
