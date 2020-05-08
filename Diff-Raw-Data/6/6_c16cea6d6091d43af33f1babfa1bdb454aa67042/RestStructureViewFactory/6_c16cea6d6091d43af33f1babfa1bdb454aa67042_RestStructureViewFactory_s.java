 package com.jetbrains.rest.structureView;
 
 import org.jetbrains.annotations.NotNull;
 import com.intellij.ide.structureView.StructureViewBuilder;
 import com.intellij.ide.structureView.StructureViewModel;
 import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
 import com.intellij.lang.PsiStructureViewFactory;
 import com.intellij.psi.PsiFile;
 
 /**
  * User : catherine
  */
 public class RestStructureViewFactory implements PsiStructureViewFactory {
   @Override
   public StructureViewBuilder getStructureViewBuilder(final PsiFile psiFile) {
     return new TreeBasedStructureViewBuilder() {
 
       @Override
       @NotNull
      public StructureViewModel createStructureViewModel() {
         return new RestStructureViewModel(psiFile);
       }
     };
   }
 }
