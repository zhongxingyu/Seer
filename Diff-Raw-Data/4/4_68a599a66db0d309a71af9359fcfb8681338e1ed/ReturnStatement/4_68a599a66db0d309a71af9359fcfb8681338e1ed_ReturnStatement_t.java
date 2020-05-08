 package edu.lmu.cs.xlg.carlos.entities;
 
 /**
  * A return statement, which may or may not have an expression to return.
  */
 public class ReturnStatement extends Statement {
 
     private Expression returnExpression;
 
     public ReturnStatement(Expression returnExpression) {
         this.returnExpression = returnExpression;
     }
 
     public Expression getReturnExpression() {
         return returnExpression;
     }
 
     @Override
     public void analyze(AnalysisContext context) {
         if (context.getFunction() == null) {
             // At top-level, not inside any function
             context.error("return_outside_function");
 
         } else if (context.getFunction().getReturnType() == null) {
             // Inside a procedure, better not have a return expression
             if (returnExpression != null) {
                 context.error("return_value_not_allowed");
             }
 
         } else if (returnExpression == null) {
             // Inside a function without a return expression
             context.error("return_value_required");
 
         } else {
             // Returning something from a function, so typecheck
             returnExpression.analyze(context);
             returnExpression.assertAssignableTo(context.getFunction().getReturnType(), context,
                 "return_statement");
         }
     }
 
     @Override
     public Statement optimize() {
        if (returnExpression != null) {
            returnExpression = returnExpression.optimize();
        }
         return this;
     }
 }
