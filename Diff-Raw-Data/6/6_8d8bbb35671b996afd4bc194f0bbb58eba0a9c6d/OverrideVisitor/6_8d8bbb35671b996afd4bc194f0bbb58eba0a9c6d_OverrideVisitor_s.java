 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.netbeans.modules.javafx.editor.hints;
 
 import com.sun.javafx.api.tree.ClassDeclarationTree;
 import com.sun.javafx.api.tree.FunctionDefinitionTree;
 import com.sun.javafx.api.tree.JavaFXTreePathScanner;
 import com.sun.javafx.api.tree.Tree;
 import com.sun.tools.javac.code.Symbol.MethodSymbol;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import javax.lang.model.element.Element;
 import org.netbeans.api.javafx.source.CompilationInfo;
 
 /**
  *
  * @author karol harezlak
  */
 final class OverrideVisitor extends JavaFXTreePathScanner<Void, Void> {
 
     private CompilationInfo compilationInfo;
     private Map<Element, Collection<Tree>> classTrees;
     private Map<Element, List<MethodSymbol>> overriddenMethods;
     private boolean includeAnon = false;
 
     public OverrideVisitor(CompilationInfo compilationInfo,
             Map<Element, Collection<Tree>> classTrees,
             Map<Element, List<MethodSymbol>> overriddenMethods) {
 
         this.compilationInfo = compilationInfo;
         this.classTrees = classTrees;
         this.overriddenMethods = overriddenMethods;
     }
 
     public OverrideVisitor(CompilationInfo compilationInfo,
             Map<Element, Collection<Tree>> classTrees,
             Map<Element, List<MethodSymbol>> overriddenMethods,
             boolean incudeAnon) {
 
         this(compilationInfo, classTrees, overriddenMethods);
         this.includeAnon = incudeAnon;
     }
 
     @Override
     public Void visitClassDeclaration(ClassDeclarationTree node, Void v) {
         Element currentClass = compilationInfo.getTrees().getElement(getCurrentPath());
         if (currentClass != null && !currentClass.toString().contains("$anon")) { //NOI18N
             collectClasses(currentClass, node);
         } else if (includeAnon) {
             collectClasses(currentClass, node);
         }
         return super.visitClassDeclaration(node, v);
     }
 
     private void collectClasses(Element currentClass, ClassDeclarationTree node) {
         Collection<Tree> extendsList = classTrees.get(currentClass);
             if (extendsList == null) {
                 extendsList = new HashSet<Tree>();
             }
             extendsList.addAll(node.getSupertypeList());
             classTrees.put(currentClass, extendsList);
     }
 
   
     @Override
     public Void visitFunctionDefinition(FunctionDefinitionTree node, Void v) {
         if (node.toString().contains(" overridefunction ") || node.toString().contains(" override ")) { //NOI18N
             Element element = compilationInfo.getTrees().getElement(getCurrentPath());
             if (element != null) {
                 Element currentClass = element.getEnclosingElement();
                 if (element instanceof MethodSymbol) {
                     if (overriddenMethods.get(currentClass) == null) {
                         overriddenMethods.put(currentClass, new ArrayList<MethodSymbol>());
                     }
                     List<MethodSymbol> methods = overriddenMethods.get(currentClass);
                     methods.add((MethodSymbol) element);
                     overriddenMethods.put(currentClass, methods);
                 }
             }
         }
         return super.visitFunctionDefinition(node, v);
     }
 }
