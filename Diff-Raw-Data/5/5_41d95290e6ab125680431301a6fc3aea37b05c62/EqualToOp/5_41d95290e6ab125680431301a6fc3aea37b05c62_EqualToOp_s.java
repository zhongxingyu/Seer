 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class EqualToOp extends ComparisonOp
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public EqualToOp(String strName)
     {
         super(strName);
     }
 
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public STO checkOperands(STO operand1, STO operand2)
     {
         STO resultSTO;
         Type o1Type = operand1.getType();
         Type o2Type = operand2.getType();
         
         boolean bothNumeric = o1Type.isNumeric() && o2Type.isNumeric();
         boolean bothBoolean = o1Type.isBool() && o2Type.isBool();
        boolean bothPointer = o1Type.isPtrGrp() && o2Type.isPtrGrp();
         boolean bothNullPtr = o1Type.isNullPtr() && o2Type.isNullPtr();
        boolean eitherPointer = o1Type.isPtrGrp() || o2Type.isPtrGrp();
         boolean eitherNullPtr = o1Type.isNullPtr() || o2Type.isNullPtr();
         
         // Check #1 - EqualTo - Both operands must be numeric or boolean
         // Check #17 -EqualTo - Both operands be numeric, boolean, pointer, or nullptr
         if(!bothNumeric && !bothBoolean && !bothPointer && !bothNullPtr) {
             // if either operand is a pointer type or nullptr use check 17 error
             if(eitherPointer || eitherNullPtr) {
                 return(new ErrorSTO(Formatter.toString(ErrorMsg.error17_Expr, this.getName(), o1Type.getName(), o2Type.getName())));
             } 
             return(new ErrorSTO(Formatter.toString(ErrorMsg.error1b_Expr, operand1.getType().getName(), this.getName(), operand2.getType().getName())));
         }
 
         if((operand1.isConst() && operand2.isConst()) || (o1Type.isNullPtr() && o2Type.isNullPtr())) {
             resultSTO = new ConstSTO("EqualToOp.checkOperands() Result", new BoolType());
         }
         else {
             resultSTO = new ExprSTO("EqualToOp.checkOperands() Result", new BoolType());
         }
 
 
         return resultSTO;
     }
 
     public STO doOperation(ConstSTO operand1, ConstSTO operand2, Type resultType)
     {
         Double value = 0.0;
         boolean b_value = true;
 
         if(operand1.getType().isFloat() || operand2.getType().isFloat()) {
             b_value = operand1.getFloatValue() == operand2.getFloatValue();
         }
 
         else if(operand1.getType().isNullPtr() && operand2.getType().isNullPtr()) {
             b_value = true;
         }
         else if(operand1.getType().isInt() && operand2.getType().isInt()) {
             b_value = operand1.getIntValue() == operand2.getIntValue();
         }
         else {
             b_value = operand1.getBoolValue() == operand2.getBoolValue();
         }
 
         if(b_value)
             value = new Double(1);
         else
             value = new Double(0);
 
         return new ConstSTO("EqualToOp.doOperation Result", resultType, value);
     }
 
     public boolean isEqualToOp()
     {
         return true;
     }
 }
