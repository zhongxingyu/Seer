 package com.sdc.ast.expressions;
 
 import com.sdc.abstractLanguage.AbstractOperationPrinter;
 import com.sdc.ast.OperationType;
 
 import static com.sdc.ast.OperationType.*;
 import static com.sdc.ast.OperationType.EQ;
 import static com.sdc.ast.OperationType.NE;
 
 public abstract class PriorityExpression extends Expression {
     protected OperationType myType;
 
     public int getPriority(AbstractOperationPrinter operationPrinter) {
         return operationPrinter.getPriority(myType);
     }
 
     public OperationType getOperationType() {
         return myType;
     }
 
     public void setOperationType(final OperationType type) {
         this.myType = type;
     }
 
     public boolean isAssociative() {
         if (myType == ADD || myType == MUL || myType == AND || myType == OR
                 || myType == SQUARE_BRACKETS || myType == ARRAYLENGTH ||
                myType == BITWISE_AND || myType == BITWISE_OR || myType == BITWISE_XOR || myType == EQ || myType == NE ||
                isComplexIncrement()
                ) {
             return true;
         }
         return false;
     }

    private boolean isComplexIncrement() {
        return myType == BITWISE_OR_INC || myType == BITWISE_AND_INC || myType == BITWISE_XOR_INC ||
                myType == SHL_INC || myType == SHR_INC || myType == USHR_INC ||
                myType == ADD_INC || myType == MUL_INC || myType == DIV_INC || myType == SUB_INC || myType == REM_INC;
    }
 }
