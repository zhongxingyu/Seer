 package com.antonzherdev.objd.markers;
 
 import com.antonzherdev.chain.F;
 import com.antonzherdev.objd.ObjDUtil;
 import com.antonzherdev.objd.psi.*;
 import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
 import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
 import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
 import com.intellij.icons.AllIcons;
 import com.intellij.psi.PsiElement;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.Collection;
 import java.util.List;
 
 import static com.antonzherdev.chain.Chain.chain;
 
 public class ClassChildrenProvider extends RelatedItemLineMarkerProvider {
     @Override
     protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
         if(element instanceof ObjDClassStatement) {
             final ObjDClassName nmElement = ((ObjDClassStatement) element).getClassName();
             final String name = nmElement.getName();
            List<ObjDClass> classes = ObjDUtil.getAllClasses(element.getProject())
                     .filter(new F<ObjDClass, Boolean>() {
                         @Override
                         public Boolean f(ObjDClass objDClass) {
                             return chain(objDClass.getClassExtendsList())
                                     .find(new F<ObjDClassExtends, Boolean>() {
                                         @Override
                                         public Boolean f(ObjDClassExtends x) {
                                             ObjDDataTypeRef tp = x.getDataTypeRef();
                                             return name.equals(tp.getName()) && tp.getReference().resolve() == nmElement;
                                         }
                                     }).isDefined();
                         }
                     }).list();
             if(!classes.isEmpty()) {
                 NavigationGutterIconBuilder<PsiElement> builder =
                         NavigationGutterIconBuilder.create(AllIcons.Gutter.ImplementedMethod).
                                 setTargets(classes.toArray(new PsiElement[classes.size()])).
                                 setTooltipText("Is subclassed by");
                 result.add(builder.createLineMarkerInfo(element));
             }
         }
     }
 }
