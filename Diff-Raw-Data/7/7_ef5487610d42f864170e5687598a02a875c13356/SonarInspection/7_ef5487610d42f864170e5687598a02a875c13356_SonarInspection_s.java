 package org.sonar.ide.idea.inspection;
 
 import com.intellij.codeInspection.*;
import com.intellij.openapi.diagnostic.Logger;
 import com.intellij.openapi.editor.Document;
 import com.intellij.openapi.project.Project;
 import com.intellij.openapi.util.TextRange;
 import com.intellij.psi.*;
 import org.jetbrains.annotations.Nls;
 import org.jetbrains.annotations.NotNull;
 import org.jetbrains.annotations.Nullable;
 import org.sonar.ide.idea.utils.ResourceUtils;
 import org.sonar.ide.idea.utils.SonarUtils;
 import org.sonar.ide.shared.ViolationsLoader;
 import org.sonar.wsclient.Sonar;
 import org.sonar.wsclient.services.Violation;
 
 import javax.swing.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * @author Evgeny Mandrikov
  */
 public class SonarInspection extends LocalInspectionTool {
  private static final Logger LOG = Logger.getInstance(SonarInspection.class.getName());
 
   @Nls
   @NotNull
   @Override
   public String getGroupDisplayName() {
     return "Sonar";
   }
 
   @Nls
   @NotNull
   @Override
   public String getDisplayName() {
     return "Violations";
   }
 
   @NotNull
   @Override
   public String getShortName() {
     return "Sonar-IDEA"; // TODO
   }
 
   @Override
   public String getStaticDescription() {
     return "Violations from Sonar (Checkstyle, PMD, Findbugs, ...)";
   }
 
   @Override
   public boolean isEnabledByDefault() {
     return true;
   }
 
   @Nullable
   @Override
   public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
     if (isOnTheFly) {
       return null;
     }
     if (!(file instanceof PsiJavaFile)) {
       return null;
     }
     LOG.debug("Running " + (isOnTheFly ? "on the fly" : "offline") + " inspection for " + file);
 
     PsiJavaFile javaFile = (PsiJavaFile) file;
     ViolationsLoader violationsLoader = new ViolationsLoader();
     Sonar sonar = SonarUtils.getSonar(file.getProject());
     ResourceUtils resourceUtils = new ResourceUtils();
     Collection<Violation> violations = violationsLoader.getViolations(sonar, resourceUtils.getResourceKey(javaFile));
 
     return buildProblemDescriptors(
         violations,
         manager,
         file,
         isOnTheFly
     );
   }
 
   @Override
   public JComponent createOptionsPanel() {
     LOG.debug("Create options panel");
     return super.createOptionsPanel();
   }
 
   @Override
   public PsiNamedElement getProblemElement(PsiElement psiElement) {
     PsiNamedElement result = super.getProblemElement(psiElement);
     LOG.debug("Get problem element for " + psiElement + " with result " + result);
     return result;
   }
 
   @Override
   public void inspectionStarted(LocalInspectionToolSession session) {
     LOG.debug("Inspection started");
     super.inspectionStarted(session);
   }
 
   @Override
   public void inspectionFinished(LocalInspectionToolSession session) {
     LOG.debug("Inspection finished");
     super.inspectionFinished(session);
   }
 
   @NotNull
   private TextRange getTextRange(@NotNull Document document, int line) {
     int lineStartOffset = document.getLineStartOffset(line);
     int lineEndOffset = document.getLineEndOffset(line);
     return new TextRange(lineStartOffset, lineEndOffset);
   }
 
   @Nullable
   private ProblemDescriptor[] buildProblemDescriptors(
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
 
   private String getDescriptionTemplate(Violation violation) {
     return violation.getRuleName() + " : " + violation.getMessage();
   }
 }
