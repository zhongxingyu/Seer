 /*
  * Copyright 2013 Red Hat, Inc.
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package org.jboss.errai.idea.plugin.marshalling;
 
 import static com.intellij.psi.search.GlobalSearchScope.allScope;
 import static com.intellij.psi.search.searches.AnnotatedElementsSearch.searchPsiClasses;
 
 import com.intellij.lang.properties.IProperty;
 import com.intellij.lang.properties.PropertiesUtil;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.JavaPsiFacade;
 import com.intellij.psi.PsiAnnotation;
 import com.intellij.psi.PsiClass;
 import com.intellij.psi.PsiFile;
 import com.intellij.psi.search.GlobalSearchScope;
 import org.jboss.errai.idea.plugin.util.DefaultPolicy;
 import org.jboss.errai.idea.plugin.util.Types;
 import org.jboss.errai.idea.plugin.util.Util;
 
import java.util.Collections;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author Mike Brock
  */
 public class MarshallingUtil {
   public static Set<String> getConfiguredPortableTypes(Project project) {
     final Set<String> bindableTypes = new HashSet<String>();
 
     for (PsiFile file : Util.getAllErraiAppProperties(project)) {
       for (IProperty property
           : PropertiesUtil.findAllProperties(project,
           PropertiesUtil.getResourceBundle(file), "errai.marshalling.serializableTypes")) {
         final String value = property.getValue();
         if (value != null) {
           for (String s : value.split("\\s+")) {
             bindableTypes.add(s.trim());
           }
         }
       }
     }
 
     return bindableTypes;
   }
 
   public static Set<String> getAllClasspathMarshallers(Project project) {
     Set<String> exposed = new HashSet<String>();
 
     final JavaPsiFacade instance = JavaPsiFacade.getInstance(project);
     final GlobalSearchScope allScope = allScope(project);
 
     final PsiClass clientMarAnno = instance.findClass(Types.CLIENT_MARSHALLER, allScope);

    if (clientMarAnno == null) {
      return Collections.emptySet();
    }

     final PsiClass serverMarAnno = instance.findClass(Types.SERVER_MARSHALLER, allScope);
 
     for (PsiClass psiClass : searchPsiClasses(clientMarAnno, allScope(project))) {
       final PsiAnnotation element = Util.getAnnotationFromElement(psiClass, Types.CLIENT_MARSHALLER);
       element.findDeclaredAttributeValue("value");
 
       final String value = Util.getAttributeValue(element, "value", DefaultPolicy.NULL);
 
     //  System.out.println(value);
 
     }
 
     for (PsiClass psiClass : searchPsiClasses(serverMarAnno, allScope(project))) {
     }
 
     return exposed;
   }
 
 //  public static void extractType(final Set<String> exposed,
 //                                 final PsiClass marshallerClass,
 //                                 final Project project,
 //                                 final JavaPsiFacade instance,
 //                                 final GlobalSearchScope scope) {
 //
 //
 //    for (PsiClassType type : marshallerClass.getSuperTypes()) {
 //      if (Util.getErasedCanonicalText(type.getCanonicalText()).equals(Types.MARSHALLER)) {
 //        exposed.add(Util.getErasedTypeParam(project, type.getCanonicalText()).getQualifiedName());
 //      }
 //      else {
 //        final PsiClass superCls = instance.findClass(type.getClassName(), scope);
 //        if (Util.typeIsAssignableFrom(superCls, Types.MARSHALLER)) {
 //          extractType(exposed, marshallerClass, project, instance, scope);
 //        }
 //      }
 //    }
 //  }
 }
