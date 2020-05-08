 package de.skuzzle.polly.core.parser;
 
 import java.io.UnsupportedEncodingException;
 import java.nio.charset.Charset;
 
 import de.skuzzle.polly.core.parser.ast.Root;
 import de.skuzzle.polly.core.parser.ast.declarations.Namespace;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTTraversalException;
 import de.skuzzle.polly.core.parser.ast.visitor.ASTVisitor;
 import de.skuzzle.polly.core.parser.ast.visitor.DebugExecutionVisitor;
 import de.skuzzle.polly.core.parser.ast.visitor.ExecutionVisitor;
 import de.skuzzle.polly.core.parser.ast.visitor.ParentSetter;
 import de.skuzzle.polly.core.parser.ast.visitor.Unparser;
 import de.skuzzle.polly.core.parser.ast.visitor.resolving.TypeResolver;
 import de.skuzzle.polly.core.parser.problems.ProblemReporter;
 
 
 /**
  * This is the main class for accessing the most often used parser feature: evaluating
  * an input String. It parses the String, resolves all types and executes the input in
  * the context of a provided {@link Namespace}. Evaluation may be either successful or
  * fail. In the latter case, you can retrieve the Exception that caused the fail using
  * {@link #getLastError()}. If no exception occurred, you may retrieve the result using
  * {@link #getRoot()}.  
  * 
  * @author Simon Taddiken
  */
 public class Evaluator {
     
     private final static ExecutionVisitor getExecutor(Namespace rootNs, 
             Namespace workingNs, ProblemReporter reporter) {
         if (ParserProperties.should(ParserProperties.ENABLE_EXECUTION_DEBUGGING)) {
             return new DebugExecutionVisitor(rootNs, workingNs, reporter);
         } else {
             return new ExecutionVisitor(rootNs, workingNs, reporter);
         }
     }
 
     private final String input;
     private final String encoding;
     private final ProblemReporter reporter;
     private Root lastResult;
     private ASTTraversalException lastError;
     
     
     
     public Evaluator(String input, String encoding, ProblemReporter reporter) 
             throws UnsupportedEncodingException {
         if (!Charset.isSupported(encoding)) {
             throw new UnsupportedEncodingException(encoding);
         }
         this.reporter = reporter;
         this.input = input;
         this.encoding = encoding;
     }
     
     
     
     /**
      * Gets the input String which is parsed by this evaluator.
      * 
      * @return The input string.
      */
     public String getInput() {
         return this.input;
     }
     
     
     
     /**
      * Tries to evaluate the input that this instance was created with. Success of
      * evaluation can be queried using {@link #errorOccurred()}. If an error occurred,
      * the exception can be obtained using {@link #getLastError()}. If evaluation was
      * successful, the result can be obtained using {@link #getRoot()}. 
      * 
      * @param rootNs The namespace to which ne declarations will be stored.
      * @param workingNs The initial namespace to work with.
      */
     public void evaluate(Namespace rootNs, Namespace workingNs) {
         try {
             final InputParser parser = new InputParser(this.input, this.encoding, 
                 this.reporter);
             this.lastResult = parser.parse();
             
             if (this.lastResult == null) {
                 return;
             }
             
             // set parent attributes for all nodes
             final ASTVisitor parentSetter = new ParentSetter();
             this.lastResult.visit(parentSetter);
             
            final ProblemReporter reporter = this.reporter.subReporter(
                this.lastResult.getPosition());
            
             // resolve types
            TypeResolver.resolveAST(this.lastResult, workingNs, reporter);
             
             if (!this.reporter.hasProblems()) {
                final ASTVisitor executor = getExecutor(rootNs, workingNs, reporter);
                 this.lastResult.visit(executor);
             }
             
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException("This should not have happened", e);
         } catch (ASTTraversalException e) {
             this.lastError = e;
         }
     }
     
     
     
     public boolean errorOccurred() {
         return this.reporter.hasProblems();
     }
     
     
     
     public ASTTraversalException getLastError() {
         return this.lastError;
     }
     
     
     
     public Root getRoot() {
         /*if (this.errorOccurred()) {
             throw new IllegalStateException("no valid result available");
         }*/
         return this.lastResult;
     }
     
     
     
     public String unparse() {
         return Unparser.toString(this.getRoot());
     }
 }
