 package com.sdc.ast.controlflow;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 import com.sdc.ast.OperationType;
 import com.sdc.ast.expressions.Constant;
 import com.sdc.ast.expressions.ExprIncrement;
 import com.sdc.ast.expressions.Expression;
 import com.sdc.ast.expressions.IntConstant;
 import com.sdc.ast.expressions.identifiers.Variable;
 
 import static com.sdc.ast.OperationType.*;
 
 public class Increment extends Statement {
     private Variable myVariable;
     private Expression myIncrement;
     private OperationType myType;
     private boolean myIsIncrementSimple = false;
 
     public Increment(Variable v, int increment) {
         super();
         myVariable = v;
         myIncrement = new Constant(Math.abs(increment), false);
         if (increment == 1) {
             myIsIncrementSimple = true;
             myType = INC;
         } else if (increment == -1) {
             myIsIncrementSimple = true;
             myType = DEC;
         } else if (increment >= 0) {
             myType = ADD_INC;
         } else if (increment < 0) {
             myType = SUB_INC;
         }
     }
 
     public Increment(ExprIncrement exprIncrement) {
         this(exprIncrement.getVariable(), exprIncrement.getIncrementExpression(), exprIncrement.getOperationType());
     }
 
     public Increment(Variable v, Expression increment, OperationType type) {
         myVariable = v;
         myIncrement = increment;
         myType = type;
         switch (type) {
             case INC:
                 myType = INC;
                 myIsIncrementSimple = true;
                 return;
             case DEC:
                 myType = DEC;
                 myIsIncrementSimple = true;
                 return;
             case INC_REV:
                 myType = INC;
                 myIsIncrementSimple = true;
                 return;
             case DEC_REV:
                 myType = DEC;
                 myIsIncrementSimple = true;
                 return;
             case ADD:
                 if (increment instanceof IntConstant && ((IntConstant) increment).isOne()) {
                     myType = INC;
                     myIsIncrementSimple = true;
                 } else if (increment instanceof IntConstant && ((IntConstant) increment).isMinusOne()) {
                     myType = DEC;
                     myIsIncrementSimple = true;
                 } else {
                     myType = ADD_INC;
                 }
                 return;
             case SUB:
                 if (increment instanceof IntConstant && ((IntConstant) increment).isOne()) {
                     myType = DEC;
                     myIsIncrementSimple = true;
                 } else if (increment instanceof IntConstant && ((IntConstant) increment).isMinusOne()) {
                     myType = INC;
                     myIsIncrementSimple = true;
                 } else {
                    myType = SUB_INC;
                 }
                 return;
             case MUL:
                 myType = MUL_INC;
                 return;
             case DIV:
                 myType = DIV_INC;
                 return;
             case REM:
                 myType = REM_INC;
                 return;
             default:
                 return;
         }
 
     }
 
 
     public Expression getIncrementExpression() {
         return myIncrement;
     }
 
 
     public String getName() {
         return myVariable.getName();
     }
 
     public String getOperation(AbstractOperationPrinter operationPrinter) {
         switch (myType) {
             case INC:
                 return operationPrinter.getIncView();
             case DEC:
                 return operationPrinter.getDecView();
             case ADD_INC:
                 return operationPrinter.getAddIncView();
             case SUB_INC:
                 return operationPrinter.getSubIncView();
             case MUL_INC:
                 return operationPrinter.getMulIncView();
             case DIV_INC:
                 return operationPrinter.getDivIncView();
             case REM_INC:
                 return operationPrinter.getRemIncView();
             default:
                 return "";
         }
     }
 
     public OperationType getOperationType() {
         return myType;
     }
 
     public Variable getVariable() {
         return myVariable;
     }
 
     public boolean IsIncrementSimple() {
         return myIsIncrementSimple;
     }

    public int getPriority(AbstractOperationPrinter operationPrinter){
        return operationPrinter.getPriority(myType);
    }
 }
