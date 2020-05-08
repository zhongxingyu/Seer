 package com.sdc.ast.controlflow;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 import com.sdc.ast.OperationType;
 import com.sdc.ast.expressions.identifiers.Variable;
 
 import static com.sdc.ast.OperationType.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Dmitrii.Pozdin
  * Date: 8/9/13
  * Time: 4:54 PM
  * To change this template use File | Settings | File Templates.
  */
 public class Increment extends Statement {
     private Variable myVariable;
     private int myIncrement;
     private OperationType myType;
 
     public Increment(Variable v, int increment) {
         super();
         myIncrement = increment;
         myVariable=v;
         myIncrement = Math.abs(increment);
         if (increment == 1) {
             myType = INC;
         } else if (increment == -1) {
             myType = DEC;
         } else if (increment >= 0) {
             myType = ADD_INC;
         } else if (increment < 0) {
             myType = SUB_INC;
         }
     }
 
     public Increment(Variable v, int increment, OperationType type) {
         myVariable=v;
         myIncrement = increment;
         switch (type) {
             case INC:
                 myIncrement = 1; //i.e. we ignore increment here
                 myType = INC;
                 return;
             case DEC:
                 myIncrement = 1; //i.e. we ignore increment here
                 myType = DEC;
                 return;
             case ADD_INC:
                 addType(increment);
                 return;
             case ADD:
                 addType(increment);
                 return;
             case SUB_INC:
                 subType(increment);
                 return;
             case SUB:
                 subType(increment);
                 return;
             case MUL_INC:
                 myType = MUL_INC;
                 return;
             case MUL:
                 myType = MUL_INC;
                 return;
             case DIV_INC:
                 myType = DIV_INC;
                 return;
             case DIV:
                 myType = DIV_INC;
                 return;
             case REM_INC:
                 myType = REM_INC;
                 return;
             case REM:
                 myType = REM_INC;
                 return;
             default:
                 return;
         }
 
     }
 
 
     public int getIncrement() {
         return myIncrement;
     }
 
     public int getSignIncrement() {
         return myIncrement * (myType == SUB_INC || myType == DEC ? -1 : 1);
     }
     public String getName() {
         return myVariable.getName();
     }
 
     public String getOperation() {
         switch (myType) {
             case INC:
                 return "++";
             case DEC:
                 return "--";
             case ADD_INC:
                 return " += " + myIncrement;
             case SUB_INC:
                 return " -= " + myIncrement;
             case MUL_INC:
                 return " *= " + myIncrement;
             case DIV:
                 return " /= " + myIncrement;
             case REM_INC:
                 return " %= " + myIncrement;
             default:
                 return "";
         }
     }
 
     public String getOperation(AbstractOperationPrinter operationPrinter) {
         switch (myType) {
             case INC:
                 return operationPrinter.getIncView();
             case DEC:
                 return operationPrinter.getDecView();
             case ADD_INC:
                 return operationPrinter.getAddIncView() + myIncrement;
             case SUB_INC:
                 return operationPrinter.getSubIncView() + myIncrement;
             case MUL_INC:
                 return operationPrinter.getMulIncView() + myIncrement;
             case DIV_INC:
                 return operationPrinter.getDivIncView() + myIncrement;
             case REM_INC:
                 return operationPrinter.getRemIncView() + myIncrement;
             default:
                 return "";
         }
     }
 
     public OperationType getType() {
         return myType;
     }
 
     public Variable getVariable() {
         return myVariable;
     }
 
     private void addType(int increment) {
         if (increment == 1) {
             myType = INC;
         } else if (increment == -1) {
             myType = DEC;
         } else if (increment > 0) {
             myType = ADD_INC;
         } else if (increment < 0) {
             myType = SUB_INC;
         }
         myIncrement = Math.abs(myIncrement);
     }
 
     private void subType(int increment) {
         if (increment == 1) {
             myType = DEC;
         } else if (increment == -1) {
             myType = INC;
         } else if (increment > 0) {
             myType = SUB_INC;
        } else if (increment < 0) {
            myType = ADD_INC;
         }
         myIncrement = Math.abs(myIncrement);
     }
 }
