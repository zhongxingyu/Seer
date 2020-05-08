 package ast;
 
 import interpreter.ASTVisitor;
 
 /**
  * This class symbolizes a function call.
  */
 public class FunctionCall extends Expression {
     /**
      * function parameters
      */
     private Expression[] parameters;
     /**
      * function to be called
      */
    private Function function;
 
     /**
      * Constructor.
      *
      * @param function function to be called
      * @param parameters array of function parameters
      * @param position indicates the position of this element
      *                 in the original source code
      */
    public FunctionCall(Function function, Expression[] parameters,
                            Position position) {
         super(position);
         this.parameters = parameters;
         this.function = function;
 
     }
 
     @Override
     public void accept(ASTVisitor visitor) {
         visitor.visit(this);
     }
 
     /**
      * Returns the parameters of this function call.
      * @return function parameters
      */
     public Expression[] getParameters() {
         return parameters;
     }
 
     /**
      * Returns the function to be called.
      * @return function to be called
      */
     public Function getFunction() {
         return function;
     }
 }
