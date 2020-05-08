 package com.jetbrains.rest.structureView;
 
 import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
 import com.intellij.ide.structureView.StructureViewBuilder;
 import com.intellij.ide.structureView.StructureViewModel;
 import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
 import com.intellij.lang.PsiStructureViewFactory;
import com.intellij.openapi.editor.Editor;
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
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
         return new RestStructureViewModel(psiFile);
       }
     };
   }
 }
