 package com.farpost.idea.beans.php.reflection;
 
 import com.farpost.idea.beans.php.Util;
 import com.intellij.codeInsight.completion.CompletionParameters;
 import com.intellij.codeInsight.completion.CompletionResultSet;
 import com.intellij.codeInsight.completion.PrefixMatcher;
 import com.intellij.codeInsight.lookup.LookupElement;
 import com.intellij.openapi.util.Pair;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiNamedElement;
 import com.jetbrains.php.PhpIndex;
 import com.jetbrains.php.completion.ClassUsageContext;
 import com.jetbrains.php.lang.psi.elements.PhpClass;
 import org.jetbrains.annotations.NotNull;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 /**
  * User: zolotov
  * Date: 12/4/12
  */
 @SuppressWarnings("All")
 public class PhpCompletionAdapter {
  private static Class completionUtilClass;
   private static Method addNamespacesMethod;
   private static Class phpLookupElementClass;
   private static Field phpLookupElementField;
 
   static {
     try {
      completionUtilClass = Class.forName("com.jetbrains.php.completion.PhpCompletionUtil");
       phpLookupElementClass = Class.forName("com.jetbrains.php.completion.PhpLookupElement");
       phpLookupElementField = phpLookupElementClass.getField("handler");
       addNamespacesMethod =
        completionUtilClass.getDeclaredMethod("addSubNamespaces", String.class, CompletionResultSet.class, PhpIndex.class);
     }
     catch (ClassNotFoundException e) {
       ReflectionUtil.error(e);
     }
     catch (NoSuchMethodException e) {
       ReflectionUtil.error(e);
     }
     catch (NoSuchFieldException e) {
       ReflectionUtil.error(e);
     }
   }
 
   private final PsiElement element;
 
   public PhpCompletionAdapter(@NotNull PsiElement element) {
     this.element = element;
   }
 
   public void addClassCompletions(CompletionParameters parameters, CompletionResultSet result) {
     final PhpIndex index = PhpIndex.getInstance(element.getProject());
     final Pair<String, String> nameAndNamespace = Util.getNameAndNamespace(element.getText());
     try {
       Collection<PsiNamedElement> variants = new HashSet<PsiNamedElement>();
       final String normalizedNamespace = Util.normalizeNamespace(nameAndNamespace.second);
       final PrefixMatcher prefixMatcher = result.getPrefixMatcher().cloneWithPrefix(nameAndNamespace.first);
       result = result.withPrefixMatcher(prefixMatcher);
       addNamespacesMethod.invoke(null, normalizedNamespace, result, index);
       boolean restrict = parameters.getInvocationCount() <= 1
                          && (!nameAndNamespace.second.isEmpty() || result.getPrefixMatcher().getPrefix().length() == 0);
       for (String className : index.getAllClassNames(prefixMatcher)) {
         Collection<PhpClass> classesByName = index.getClassesByName(className);
         if (restrict) {
           classesByName = index.filterByNamespace(classesByName, nameAndNamespace.second.isEmpty() ? null : normalizedNamespace);
         }
         variants.addAll(classesByName);
       }
       final ClassUsageContext classUsageContext = new ClassUsageContext(false);
       classUsageContext.setInInstanceof(true);
       final List<LookupElement> list = PhpVariantsUtilAdapter.getLookupItemsForClasses(variants, classUsageContext);
       for (LookupElement lookupElement : list) {
         phpLookupElementField.set(lookupElement, null);
       }
       result.addAllElements(list);
     }
     catch (InvocationTargetException e) {
       ReflectionUtil.error(e);
     }
     catch (IllegalAccessException e) {
       ReflectionUtil.error(e);
     }
   }
 }
