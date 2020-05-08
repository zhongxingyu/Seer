 package com.antonzherdev.objd.reference;
 
 import com.antonzherdev.chain.*;
 import com.antonzherdev.objd.Dot;
 import com.antonzherdev.objd.ObjDUtil;
 import com.antonzherdev.objd.psi.*;
 import com.antonzherdev.objd.tp.PsiRef;
 import com.intellij.openapi.progress.ProcessCanceledException;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiNamedElement;
 import com.intellij.psi.PsiReferenceBase;
 import com.intellij.util.ArrayUtil;
 import com.intellij.util.IncorrectOperationException;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import static com.antonzherdev.chain.Chain.chain;
 import static com.antonzherdev.chain.Chain.unionChain;
 
 public class CallReference extends PsiReferenceBase<ObjDCallName> {
 
     public CallReference(@NotNull ObjDCallName element, TextRange textRange) {
         super(element, textRange);
     }
 
 
     @Nullable
     @Override
     public PsiElement resolve() {
         try {
             return getRefsChain(getElement())
                     .find(new B<PsiRef>() {
                         @Override
                         public Boolean f(PsiRef x) {
                             return x.getName().equals(getElement().getName());
                         }
                     })
                     .map(PsiRef.ELEMENT_F)
                     .getOrNull();
         } catch (ProcessCanceledException e) {
             return null;
         }
     }
 
     public static IChain<PsiRef> getRefsChain(PsiElement element) {
         Option<Dot> dot = ObjDUtil.getDot(element);
         if(dot.isDefined()) {
             return dot.get().getLeft().getTp().getRefsChain();
         }
         return ObjDUtil.availableClassesInFile((ObjDFile) element.getContainingFile())
                 .map(new F<ObjDClass, PsiNamedElement>() {
                     @Override
                     public PsiNamedElement f(ObjDClass objDClass) {
                         return objDClass.getClassName();
                     }
                 })
                .append(ObjDUtil.availableDefsInFile((ObjDFile) element.getContainingFile()))
                .append(
                         ObjDUtil.getClass(element).map(new F<ObjDClassStatement, IChain<PsiNamedElement>>() {
                             @Override
                             public IChain<PsiNamedElement> f(ObjDClassStatement x) {
                                 return ObjDUtil.classFields(x);
                             }
                         }).getOrElse(Chain.<PsiNamedElement>empty()))
                .append(vars(element))
                 .map(PsiRef.APPLY);
     }
 
     private static List<PsiNamedElement> vars(PsiElement element) {
         ArrayList<PsiNamedElement> items = new ArrayList<PsiNamedElement>();
         vars(items, element);
         return items;
     }
 
     private static void vars(ArrayList<PsiNamedElement> items, PsiElement element) {
         if(element instanceof ObjDExprVal) {
             items.add(((ObjDExprVal) element).getDefName());
         } else if(element instanceof ObjDDefStatement) {
             chain(((ObjDDefStatement) element).getDefParameterList()).map(new F<ObjDDefParameter,ObjDDefName>() {
                 @Override
                 public ObjDDefName f(ObjDDefParameter x) {
                     return x.getDefName();
                 }
             }).addAllTo(items);
             return;
         } else if(element instanceof ObjDExprLambda) {
             chain(((ObjDExprLambda) element).getLambdaParList()).map(new F<ObjDLambdaPar,ObjDDefName>() {
                 @Override
                 public ObjDDefName f(ObjDLambdaPar x) {
                     return x.getDefName();
                 }
             }).addAllTo(items);
         }  else if(element instanceof ObjDExprCallPostLambda) {
             chain(((ObjDExprCallPostLambda) element).getLambdaParList()).map(new F<ObjDLambdaPar,ObjDDefName>() {
                 @Override
                 public ObjDDefName f(ObjDLambdaPar x) {
                     return x.getDefName();
                 }
             }).addAllTo(items);
         } if(element instanceof ObjDCaseItem) {
             caseCond(items, ((ObjDCaseItem) element).getCaseCond());
         } else if(element instanceof ObjDFieldStatement) return;
 
         PsiElement p = element.getPrevSibling();
         if(p != null) {
             vars(items, p);
         } else {
             p = element.getParent();
             if(p != null) vars(items, p);
         }
     }
 
     private static void caseCond(ArrayList<PsiNamedElement> items, ObjDCaseCond caseCond) {
         if(caseCond instanceof ObjDCaseCondVal) {
             items.add(((ObjDCaseCondVal) caseCond).getDefName());
         } else if(caseCond instanceof ObjDCaseCondUnapply) {
             for (ObjDCaseCond x : ((ObjDCaseCondUnapply) caseCond).getCaseCondList()) {
                 caseCond(items, x);
             }
         }
     }
 
 
     @NotNull
     @Override
     public Object[] getVariants() {
         return ArrayUtil.EMPTY_OBJECT_ARRAY;
     }
 
     @SuppressWarnings("ConstantConditions")
     @Override
     public PsiElement handleElementRename(String newName) throws IncorrectOperationException {
         getElement().setName(newName);
         return getElement();
     }
 }
