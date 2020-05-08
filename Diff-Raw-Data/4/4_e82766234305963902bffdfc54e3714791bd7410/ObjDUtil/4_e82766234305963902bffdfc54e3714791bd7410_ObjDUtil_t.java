 package com.antonzherdev.objd;
 
 import com.antonzherdev.chain.*;
 import com.antonzherdev.objd.psi.*;
 import com.antonzherdev.objd.tp.ObjDTp;
 import com.intellij.lang.ASTNode;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.vfs.VirtualFile;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.PsiManager;
 import com.intellij.psi.PsiNamedElement;
 import com.intellij.psi.search.FileTypeIndex;
 import com.intellij.psi.search.GlobalSearchScope;
 import com.intellij.psi.tree.TokenSet;
 import com.intellij.util.indexing.FileBasedIndex;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import static com.antonzherdev.chain.Chain.chain;
 import static com.antonzherdev.chain.Chain.empty;
 
 public class ObjDUtil {
     public static Option<ObjDFile> findFile(Project project, final String fileName) {
         return getAllVirtualFiles(project).filter(new B<VirtualFile>() {
             @Override
             public Boolean f(VirtualFile virtualFile) {
                 return virtualFile.getNameWithoutExtension().equals(fileName);
             }
         }).map(toObjDFileF(project)).first();
     }
 
     public static IChain<ObjDFile> getAllFiles(final Project project) {
         return getAllVirtualFiles(project).map(toObjDFileF(project));
     }
 
     public static Option<ObjDClass> findKernelClass(Project project, final String name) {
         return getClassesInFile(findFile(project, name).getOrNull()).find(new B<ObjDClass>() {
             @Override
             public Boolean f(ObjDClass x) {
                 return x.getClassName().getName().equals(name);
             }
         });
     }
 
     private static F<VirtualFile, ObjDFile> toObjDFileF(final Project project) {
         return new F<VirtualFile, ObjDFile>() {
             @Override
             public ObjDFile f(VirtualFile virtualFile) {
                 return (ObjDFile) PsiManager.getInstance(project).findFile(virtualFile);
             }
         };
     }
 
     private static IChain<VirtualFile> getAllVirtualFiles(Project project) {
         return chain(FileBasedIndex.getInstance().getContainingFiles(FileTypeIndex.NAME,
                 ObjDFileType.INSTANCE, GlobalSearchScope.allScope(project)));
     }
 
     public static IChain<ObjDClassName> availableClassesInFile(PsiFile file) {
         return availableFiles(file)
                 .flatMap(new F<ObjDFile, IChain<ObjDClass>>() {
                     @Override
                     public IChain<ObjDClass> f(ObjDFile objDFile) {
                         return getClassesInFile(objDFile);
                     }
                 })
                 .map(new F<ObjDClass,ObjDClassName>() {
                     @Override
                     public ObjDClassName f(ObjDClass x) {
                         return x.getClassName();
                     }
                 });
     }
 
     public static IChain<ObjDFile> availableFiles(PsiFile file) {
         return chain(file.getNode().getChildren(TokenSet.create(ObjDTypes.IMPORT_STATEMENT)))
                 .map(new F<ASTNode,ObjDFile>() {
                     @Override
                     public ObjDFile f(ASTNode astNode) {
                         ObjDImportOdFile od = astNode.getPsi(ObjDImportStatement.class).getImportOdFile();
                         if(od == null) return null;
                         return (ObjDFile) od.getReference().resolve();
                     }
                 })
                 .prepend((ObjDFile)file)
                 .append(getKernelFiles(file.getProject()));
     }
 
     final static List<String> kernelFiles = Arrays.asList("ODEnum", "ODObject", "CNTuple", "CNOption", "CNList", "CNMap");
     private static IChain<ObjDFile> getKernelFiles(Project project) {
         return getAllVirtualFiles(project)
                 .filter(new B<VirtualFile>() {
                     @Override
                     public Boolean f(VirtualFile f) {
                         return kernelFiles.contains(f.getNameWithoutExtension());
                     }
                 })
                 .map(toObjDFileF(project));
     }
 
     public static IChain<ObjDClass> getClassesInFile(ObjDFile objDFile) {
         if(objDFile == null) return empty();
         return Chain.chain(objDFile.getNode().getChildren(TokenSet.create(ObjDTypes.CLASS_STATEMENT, ObjDTypes.TYPE_STATEMENT))).map(new F<ASTNode, ObjDClass>() {
             @Override
             public ObjDClass f(ASTNode astNode) {
                 return astNode.getPsi(ObjDClass.class);
             }
         });
     }
 
     public static IChain<ObjDDefStatement> getDefsInFile(ObjDFile objDFile) {
         return Chain.chain(objDFile.getNode().getChildren(TokenSet.create(ObjDTypes.DEF_STATEMENT))).map(new F<ASTNode,ObjDDefStatement>() {
             @Override
             public ObjDDefStatement f(ASTNode astNode) {
                 return astNode.getPsi(ObjDDefStatement.class);
             }
         });
     }
 
     public static IChain<ObjDFieldStatement> getValsInFile(ObjDFile objDFile) {
         return Chain.chain(objDFile.getNode().getChildren(TokenSet.create(ObjDTypes.FIELD_STATEMENT))).map(new F<ASTNode,ObjDFieldStatement>() {
             @Override
             public ObjDFieldStatement f(ASTNode astNode) {
                 return astNode.getPsi(ObjDFieldStatement.class);
             }
         });
     }
 
     public static Option<ObjDClassStatement> getClass(PsiElement element) {
         while(element != null) {
             if(element instanceof ObjDClassStatement) return Option.some((ObjDClassStatement)element);
             element = element.getParent();
         }
         return Option.none();
     }
 
     public static IChain<PsiNamedElement> classFields(ObjDClass cls) {
         if(cls == null) return empty();
 
         if(cls instanceof ObjDClassStatement) {
             ObjDClassStatement stm = (ObjDClassStatement) cls;
             return chain(stm.getClassConstructorFieldList()).map(new F<ObjDClassConstructorField, PsiNamedElement>() {
                 @Override
                 public PsiNamedElement f(ObjDClassConstructorField x) {
                     return x.getDefName();
                 }
             }).append(chain(stm.getClassBody() == null ? null : stm.getClassBody().getDefStatementList()).map(new F<ObjDDefStatement, PsiNamedElement>() {
                 @Override
                 public PsiNamedElement f(ObjDDefStatement x) {
                     return x.getDefName();
                 }
             })).append(chain(stm.getClassBody() == null ? null : stm.getClassBody().getFieldStatementList()).map(new F<ObjDFieldStatement, PsiNamedElement>() {
                 @Override
                 public PsiNamedElement f(ObjDFieldStatement x) {
                     return x.getDefName();
                 }
             })).append(chain(stm.getClassBody() == null ? null : stm.getClassBody().getEnumItemList()).map(new F<ObjDEnumItem, PsiNamedElement>() {
                 @Override
                 public PsiNamedElement f(ObjDEnumItem x) {
                     return x.getDefName();
                 }
             }))
             .append(enumSpecials(stm))
             .append(parentFields(stm));
         } else {
             return parentFields(cls);
         }
     }
 
     private static IChain<PsiNamedElement> enumSpecials(ObjDClassStatement stm) {
         return stm.isEnum() ? classFields(findKernelClass(stm.getProject(), "ODEnum").getOrNull()) : null;
     }
 
     private static IChain<PsiNamedElement> parentFields(ObjDClass stm) {
         List<ObjDClassExtends> classExtends = stm.getClassExtendsList();
         if(classExtends == null || classExtends.isEmpty()) {
             if(stm.getClassName().getName().equals("ODObject")) return empty();
             else return classFields(findKernelClass(stm.getProject(), "ODObject").getOrNull());
         }
 
         return chain(classExtends).flatMap(new F<ObjDClassExtends,IChain<PsiNamedElement>>() {
             @Override
             public IChain<PsiNamedElement> f(ObjDClassExtends objDClassExtends) {
                 PsiElement resolve = objDClassExtends.getDataTypeRef().getReference().resolve();
                 if(resolve == null) return empty();
                 return classFields((ObjDClassStatement) resolve.getParent());
             }
         });
     }
 
     public static boolean isAfterDot(PsiElement element) {
         return getDot(element).isDefined();
     }
 
     public static Option<Dot> getDot(final PsiElement element) {
         final PsiElement par = removeIndex(element.getParent());
 
         if(par.getParent() instanceof ObjDExprDot) {
            final ObjDExprDot dot = (ObjDExprDot) par.getParent();
             if(dot.getExprList().get(0) == par) return Option.none();
             return Option.<Dot>some(new Dot() {
                 @Override
                 public Left getLeft() {
                     return new Left() {
                         @Override
                         public ObjDTp getTp() {
                             Iterator<ObjDExpr> i = dot.getExprList().iterator();
                             ObjDExpr prev = null;
                             while (i.hasNext()) {
                                 ObjDExpr next = i.next();
                                 if(next == par) return prev.getTp();
                                 prev = next;
                             }
                             return new ObjDTp.Unknown("Error in dot");
                         }
                     };
                 }
 
                 @Override
                 public PsiElement getRight() {
                     return element;
                 }
             });
         } else {
             return Option.none();
         }
     }
 
     private static PsiElement removeIndex(PsiElement parent) {
         while(parent.getParent() instanceof ObjDExprIndex) {
             parent = parent.getParent();
         }
         return parent;
     }
 
     public static List<ObjDClassGeneric> getDeclaredGenerics(PsiElement element) {
         List<ObjDClassGeneric> ret = new ArrayList<ObjDClassGeneric>();
         while(element != null) {
            if(element instanceof ObjDClass) {
                ObjDClassGenerics classGenerics = ((ObjDClass) element).getClassGenerics();
                 if(classGenerics != null) ret.addAll(classGenerics.getClassGenericList());
             }
             if(element instanceof ObjDDefStatement) {
                 ObjDClassGenerics classGenerics = ((ObjDDefStatement) element).getClassGenerics();
                 if(classGenerics != null) ret.addAll(classGenerics.getClassGenericList());
             }
             element = element.getParent();
         }
         return ret;
     }
 
 }
