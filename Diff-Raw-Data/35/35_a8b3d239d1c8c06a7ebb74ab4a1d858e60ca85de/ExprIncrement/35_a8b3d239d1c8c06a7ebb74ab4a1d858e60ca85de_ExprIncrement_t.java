 package com.sdc.ast.expressions;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 import com.sdc.ast.OperationType;
 import com.sdc.ast.controlflow.Increment;
 import com.sdc.ast.expressions.identifiers.Variable;
 
 import static com.sdc.ast.OperationType.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Dmitrii.Pozdin
  * Date: 8/12/13
  * Time: 10:26 AM
  * To change this template use File | Settings | File Templates.
  */
 public class ExprIncrement extends PriorityExpression {
     private Variable myVariable;
     private Expression myIncrement;
     private boolean myIsIncrementSimple=false;
 
     public ExprIncrement(Increment increment){
         this(increment.getVariable(), increment.getIncrementExpression(), increment.getOperationType());
     }
 
     public ExprIncrement(final Variable variable, final Expression increment, final OperationType type) {
         myVariable = variable;
         myIncrement = increment;
         myType = type;
         switch (type) {
             case INC:
                 myType = INC;
                 myIsIncrementSimple=true;
                 break;
             case DEC:
                 myType = DEC;
                 myIsIncrementSimple=true;
                 break;
             case INC_REV:
                 myType = INC_REV;
                 myIsIncrementSimple=true;
                 break;
             case DEC_REV:
                 myType = DEC_REV;
                 myIsIncrementSimple=true;
                 break;
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
                 break;
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
                 break;
             case MUL:
                 myType = MUL_INC;
                 break;
             case DIV:
                 myType = DIV_INC;
                 break;
             case REM:
                 myType = REM_INC;
                 break;
             default:
                 break;
         }
     }
 
     public ExprIncrement(final Variable variable, final int increment) {
         myVariable = variable;
         myIncrement = new Constant(increment,false);
         if (increment == 1) {
             myType = INC;
         } else if (increment == -1) {
             myType = DEC;
         } else if (increment >=0) {
             myType = ADD_INC;
         } else if (increment <0) {
             myType = SUB_INC;
         }
     }
 
     public String getOperation(AbstractOperationPrinter operationPrinter) {
         switch (myType) {
             case INC:
                 return operationPrinter.getIncView();
             case DEC:
                 return operationPrinter.getDecView();
             case INC_REV:
                 return operationPrinter.getIncRevView();
             case DEC_REV:
                 return operationPrinter.getDecRevView();
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
 
     public Variable getVariable() {
         return myVariable;
     }
 
     public Expression getIncrementExpression() {
         return myIncrement;
     }
 
     public boolean IsIncrementSimple(){
         return myIsIncrementSimple;
     }
 
     @Override
     public String toString() {
         return "ExprIncrement{" +
                 "myVariable=" + myVariable +
                 ", myIncrement=" + myIncrement +
                 '}';
     }
 }
