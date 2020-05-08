 /*
  * Copyright 2013. Guidewire Software, Inc.
  */
 
 package inspections;
 
 import com.intellij.codeInsight.CodeInsightUtilBase;
 import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
 import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.editor.Editor;
 import com.intellij.openapi.project.Project;
 import com.intellij.psi.PsiElement;
 import com.intellij.psi.PsiFile;
 import com.intellij.util.IncorrectOperationException;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 
 /**
  */
 public class FooQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {
   private static final Logger LOG = Logger.getInstance( "#" + FooQuickFix.class.getName() );
 
   public FooQuickFix( PsiElement elem ) {
     super( elem );
   }
 
   @NotNull
   @Override
   public String getText() {
     return "Foo quick fix";
   }
 
   @Override
   @NotNull
   public String getFamilyName() {
     return "Do the Foo fix";
   }
 
   @Override
   public boolean isAvailable( @NotNull Project project,
                               @NotNull PsiFile file,
                               @NotNull PsiElement startElement,
                               @NotNull PsiElement endElement ) {
    //final GosuTypeLiteralImpl typeLit = (GosuTypeLiteralImpl)startElement;
     return startElement.isValid()
            && startElement.getManager().isInProject( startElement );
   }
 
   @Override
   public void invoke( @NotNull Project project,
                       @NotNull PsiFile file,
                       @Nullable("is null when called from inspection") Editor editor,
                       @NotNull PsiElement startElement,
                       @NotNull PsiElement endElement ) {
    //GosuTypeLiteralImpl typeLit = (GosuTypeLiteralImpl)startElement;
     if( !CodeInsightUtilBase.prepareFileForWrite( startElement.getContainingFile() ) ) {
       return;
     }
     try {
       file.getViewProvider().getDocument().replaceString( startElement.getTextOffset(),
                                                           startElement.getTextOffset() + startElement.getTextLength(),
                                                          "Killroy" );
     }
     catch( IncorrectOperationException e ) {
       LOG.error( e );
     }
   }
 }
