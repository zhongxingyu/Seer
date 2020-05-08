 package org.sonar.ide.idea.utils;
 
 import com.intellij.codeInspection.InspectionManager;
 import com.intellij.codeInspection.LocalQuickFix;
 import com.intellij.codeInspection.ProblemDescriptor;
 import com.intellij.codeInspection.ProblemHighlightType;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.psi.PsiDocumentManager;
 import com.intellij.psi.PsiElement;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.sonar.wsclient.services.Violation;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Evgeny Mandrikov
  */
 public final class InspectionUtils {
 
   /**
    * Hide utility-class constructor.
    */
   private InspectionUtils() {
   }
 
   @NotNull
   public static TextRange getTextRange(@NotNull Document document, int line) {
     int lineStartOffset = document.getLineStartOffset(line);
     int lineEndOffset = document.getLineEndOffset(line);
     return new TextRange(lineStartOffset, lineEndOffset);
   }
 
   @Nullable
   public static ProblemDescriptor[] buildProblemDescriptors(
       @Nullable Collection<Violation> violations,
       @NotNull InspectionManager manager,
       PsiElement element,
       boolean isOnTheFly
   ) {
     if (violations == null) {
       return null;
     }
 
     Project project = element.getProject();
     PsiDocumentManager documentManager = PsiDocumentManager.getInstance(project);
     Document document = documentManager.getDocument(element.getContainingFile());
     if (document == null) {
       return null;
     }
 
     List<ProblemDescriptor> problems = new ArrayList<ProblemDescriptor>();
 
     for (Violation violation : violations) {
       int line = violation.getLine() - 1;
 
       /*
       BLOCKER
       CRITICAL
       MAJOR
       MINOR
       INFO
       */
 
       problems.add(manager.createProblemDescriptor(
           element,
           getTextRange(document, line),
           getDescriptionTemplate(violation),
           ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
           isOnTheFly,
          LocalQuickFix.EMPTY_ARRAY
       ));
     }
     return problems.toArray(new ProblemDescriptor[problems.size()]);
   }
 
   public static String getDescriptionTemplate(Violation violation) {
     return violation.getRuleName() + " : " + violation.getMessage();
   }
 }
