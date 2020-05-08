 package org.xtest.validation;
 
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.emf.common.util.Diagnostic;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.xtext.common.types.JvmField;
 import org.eclipse.xtext.common.types.JvmIdentifiableElement;
 import org.eclipse.xtext.common.types.JvmTypeReference;
 import org.eclipse.xtext.common.types.util.TypeConformanceComputer;
 import org.eclipse.xtext.common.types.util.TypeReferences;
 import org.eclipse.xtext.diagnostics.Severity;
 import org.eclipse.xtext.util.CancelIndicator;
 import org.eclipse.xtext.validation.CancelableDiagnostician;
 import org.eclipse.xtext.validation.Check;
 import org.eclipse.xtext.validation.CheckType;
 import org.eclipse.xtext.xbase.XAssignment;
 import org.eclipse.xtext.xbase.XExpression;
 import org.eclipse.xtext.xbase.typing.ITypeProvider;
 import org.xtest.XTestAssertException;
 import org.xtest.XTestEvaluationException;
 import org.xtest.XTestRunner;
 import org.xtest.XTestRunner.DontRunCheck;
 import org.xtest.preferences.PerFilePreferenceProvider;
 import org.xtest.preferences.RuntimePref;
 import org.xtest.results.XTestResult;
 import org.xtest.xTest.Body;
 import org.xtest.xTest.XAssertExpression;
 import org.xtest.xTest.XTestPackage;
 import org.xtest.xTest.impl.BodyImplCustom;
 
 import com.google.common.collect.HashMultimap;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 /**
  * Validator for xtest expression models. Validates that:
  * <ul>
  * <li>assert expressions have boolean return type
  * <li>assert/throws types are subclasses are throwable
  * <li><b>All unit tests pass</b> </ol>
  * 
  * @author Michael Barry
  */
 @Singleton
 @SuppressWarnings("restriction")
 public class XTestJavaValidator extends AbstractXTestJavaValidator {
     private static final int TEST_RUN_FAILURE_INDEX = Integer.MIN_VALUE;
     private final ThreadLocal<CancelIndicator> cancelIndicators = new ThreadLocal<CancelIndicator>();
     private final ThreadLocal<HashMultimap<Severity, EObject>> issues = new ThreadLocal<HashMultimap<Severity, EObject>>();
     @Inject
     private PerFilePreferenceProvider preferenceProvider;
     @Inject
     private XTestRunner runner;
     @Inject
     private TypeConformanceComputer typeConformanceComputer;
     @Inject
     private ITypeProvider typeProvider;
     @Inject
     private TypeReferences typeReferences;
 
     /**
      * Verifies that the "throws" type is a subclass of throwable or that the assert expression has
      * boolean return type.
      * 
      * @param assertExpression
      *            The assert expression to check
      */
     @Check
     public void checkAssertExpression(XAssertExpression assertExpression) {
         JvmTypeReference throws1 = assertExpression.getThrows();
         if (throws1 != null) {
             JvmTypeReference expected = typeReferences.getTypeForName(Throwable.class,
                     assertExpression);
             if (!typeConformanceComputer.isConformant(expected, throws1)) {
                 error("Throws expression must be a subclass of Throwable",
                         XTestPackage.Literals.XASSERT_EXPRESSION__THROWS);
             }
         } else {
             XExpression actual = assertExpression.getActual();
             if (actual != null) {
                 JvmTypeReference returnType = typeProvider.getCommonReturnType(actual, true);
                 JvmTypeReference expected = typeReferences.getTypeForName(Boolean.class,
                         assertExpression);
 
                 if (!typeConformanceComputer.isConformant(expected, returnType)) {
                     error("Assert expression must return a boolean",
                             XTestPackage.Literals.XASSERT_EXPRESSION__ACTUAL);
                 }
             }
         }
     }
 
     @Override
     @Check
     public void checkAssignment(XAssignment assignment) {
         JvmIdentifiableElement assignmentFeature = assignment.getFeature();
         if (!(assignmentFeature instanceof JvmField && ((JvmField) assignmentFeature).isFinal())) {
             super.checkAssignment(assignment);
         }
     }
 
     /**
      * Runs the unit test as long as the {@link CheckType} is not {@link DontRunCheck} and marks any
      * failed expressions.
      * 
      * @param main
      *            The xtest expression model to run.
      */
     public void doMagic(Body main) {
         if (!(getCheckMode() instanceof XTestRunner.DontRunCheck)) {
             CancelIndicator indicator = cancelIndicators.get();
             if (indicator == null) {
                 indicator = CancelIndicator.NullImpl;
             }
             XTestResult result = runner.run(main, indicator);
             markErrorsFromTest(result);
 
             if (preferenceProvider.get(main, RuntimePref.MARK_UNEXECUTED)) {
                 Set<XExpression> unexecutedExpressions = runner.getUnexecutedExpressions(main);
                 markUnexecuted(main, unexecutedExpressions);
             }
             result.setIssues(issues.get());
             getContext().put(XTestResult.KEY, result);
         }
     }
 
     /**
      * Invoke {@link #doMagic(Body)} while editing an Xtest file if
      * {@link RuntimePref#RUN_WHILE_EDITING} is true
      * 
      * @param main
      *            Body of the xtest file
      */
     @Check(CheckType.FAST)
     public void doMagicFast(Body main) {
         if (preferenceProvider.get(main, RuntimePref.RUN_WHILE_EDITING)) {
             doMagic(main);
         }
     }
 
     /**
      * Invoke {@link #doMagic(Body)} while building an Xtest file if
      * {@link RuntimePref#RUN_WHILE_EDITING} is false
      * 
      * @param main
      *            Body of the xtest file
      */
     @Check(CheckType.NORMAL)
     public void doMagicNormal(Body main) {
         if (!preferenceProvider.get(main, RuntimePref.RUN_WHILE_EDITING)) {
             doMagic(main);
         }
     }
 
     @Override
     protected Diagnostic createDiagnostic(Severity severity, String message, EObject object,
             EStructuralFeature feature, int index, String code, String... issueData) {
         // Hook into issue storing
         if (index != TEST_RUN_FAILURE_INDEX) {
             storeIssue(severity, object);
         }
         return super.createDiagnostic(severity, message, object, feature, index, code, issueData);
     }
 
     @Override
     protected Diagnostic createDiagnostic(Severity severity, String message, EObject object,
             int offset, int length, String code, String... issueData) {
         // Hook into issue storing
         storeIssue(severity, object);
         return super.createDiagnostic(severity, message, object, offset, length, code, issueData);
     }
 
     @Override
     protected boolean isResponsible(Map<Object, Object> context, EObject eObject) {
         cancelIndicators.set((CancelIndicator) context
                 .get(CancelableDiagnostician.CANCEL_INDICATOR));
        issues.set(HashMultimap.<Severity, EObject> create());
         return super.isResponsible(context, eObject);
     }
 
     /**
      * Marks the errors from the test
      * 
      * @param run
      *            The test result
      */
     private void markErrorsFromTest(XTestResult run) {
         if (run != null) {
             for (String error : run.getErrorMessages()) {
                 error(run.getQualifiedName() + ": " + error, run.getEObject(), null,
                         TEST_RUN_FAILURE_INDEX);
             }
             XTestAssertException assertException = run.getAssertException();
             if (assertException != null) {
                 XAssertExpression expression = assertException.getExpression();
                 error(run.getQualifiedName() + ": Assertion Failed", expression, null,
                         TEST_RUN_FAILURE_INDEX);
             }
             markEvaluationExceptions(run);
             for (XTestResult test : run.getSubTests()) {
                 markErrorsFromTest(test);
             }
         }
     }
 
     /**
      * Marks the evaluation exception on the line that generated it
      * 
      * @param run
      *            The test that failed
      */
     private void markEvaluationExceptions(XTestResult run) {
         XTestEvaluationException exception = run.getEvaluationException();
         if (exception != null) {
             Throwable cause = exception.getCause();
             XExpression expression = exception.getExpression();
             StringBuilder builder = new StringBuilder(run.getQualifiedName() + ": "
                     + cause.toString());
             for (StackTraceElement trace : cause.getStackTrace()) {
                 builder.append("\n");
                 builder.append(trace.toString());
             }
             error(builder.toString(), expression, null, TEST_RUN_FAILURE_INDEX);
         }
     }
 
     /**
      * Finds all expressions in {@code main} that are not contained in {@code executedExpressions}
      * 
      * @param main
      *            The top-level expression object
      * @param executedExpressions
      *            The set of evaluated expressions
      */
     private void markUnexecuted(Body main, Set<XExpression> unexecuted) {
         for (XExpression expression : unexecuted) {
             warning("Expression never reached", expression, null, 10);
         }
     }
 
     /**
      * Store validation issue into the top-level {@link Body} EObject
      * 
      * @param severity
      *            The severity of the issue
      * @param object
      *            The object with the issue
      */
     private void storeIssue(Severity severity, EObject object) {
         BodyImplCustom body = null;
         for (EObject cursor = object; cursor != null; cursor = cursor.eContainer()) {
             if (cursor instanceof BodyImplCustom) {
                 body = (BodyImplCustom) cursor;
             }
         }
         if (body != null && (severity == Severity.ERROR || severity == Severity.WARNING)) {
             issues.get().put(severity, object);
         }
     }
 }
