 /*
  * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
  *
  * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
  *
  * The contents of this file are subject to the terms of either the GNU
  * General Public License Version 2 only ("GPL") or the Common
  * Development and Distribution License("CDDL") (collectively, the
  * "License"). You may not use this file except in compliance with the
  * License. You can obtain a copy of the License at
  * http://www.netbeans.org/cddl-gplv2.html
  * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
  * specific language governing permissions and limitations under the
  * License.  When distributing the software, include this License Header
  * Notice in each file and include the License file at
  * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
  * particular file as subject to the "Classpath" exception as provided
  * by Sun in the GPL Version 2 section of the License file that
  * accompanied this code. If applicable, add the following below the
  * License Header, with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
  * Contributor(s):
  *
  * Portions Copyrighted 2008 Sun Microsystems, Inc.
  */
 
 package org.netbeans.modules.javafx.editor.imports;
 
 import com.sun.javafx.api.tree.*;
 import com.sun.tools.javafx.tree.JFXTree;
 import org.netbeans.api.javafx.source.CompilationInfo;
 
 import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
 import javax.lang.model.type.TypeKind;
 import java.util.Collection;
import java.util.TreeSet;
 
 /**
  * @author Rastislav Komara (<a href="mailto:moonko@netbeans.orgm">RKo</a>)
  * @todo documentation
  */
 class IdentifierVisitor extends JavaFXTreeScanner<Collection<Element>, Collection<Element>> {
     private final CompilationInfo info;
     protected UnitTree cu;
    private final Collection<Name> variableNames = new TreeSet<Name>();
 
     IdentifierVisitor(CompilationInfo info) {
         this.info = info;
         cu = this.info.getCompilationUnit();
     }
 

    @Override
    public Collection<Element> visitVariable(VariableTree node, Collection<Element> elements) {
        variableNames.add(node.getName());
        return super.visitVariable(node, elements);
    }

     @Override
     public Collection<Element> visitIdentifier(IdentifierTree node, Collection<Element> elements) {
        if (variableNames.contains(node.getName())) {
            return elements;
        }
         Element element = toElement(node);
         if (element != null) {
             if ((element.asType().getKind() == TypeKind.PACKAGE)) {
                 JavaFXTreePath path = JavaFXTreePath.getPath(cu, node);
                 Tree parent = path.getParentPath().getLeaf();
                 if (parent.getJavaFXKind() == Tree.JavaFXKind.MEMBER_SELECT) {
                     elements.add(element);
                 }
             } else {
                 elements.add(element);
             }
         }
         return elements;
     }
 
 
     private Element toElement(Tree node) {
         return info.getTrees().getElement(JavaFXTreePath.getPath(cu, node));
     }
 
     @Override
     public Collection<Element> visitFunctionValue(FunctionValueTree node, Collection<Element> elements) {
         JavaFXTreePath path = JavaFXTreePath.getPath(cu, node);
         JFXTree tree = (JFXTree) path.getParentPath().getLeaf();
         if (tree.getGenType() == SyntheticTree.SynthType.COMPILED) {
             Element element = toElement(node.getType());
             if (element != null) {
                 elements.add(element);
             }
         }
         super.visitFunctionValue(node, elements);
         return elements;
 
     }
 }
