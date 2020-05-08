 //---------------------------------------------------------------------
 //
 //---------------------------------------------------------------------
 
 class ArithmeticOp extends BinaryOp
 {
     //---------------------------------------------------------------------
     //      Constructors
     //---------------------------------------------------------------------
     public 
     ArithmeticOp ()
     {
         super();
     }
     
     //---------------------------------------------------------------------
     //      Methods
     //---------------------------------------------------------------------
     public STO
     checkOperands(STO operand1, STO operand2)
     {
         STO newSTO;
 
         // Check #1 - Plus, Minus, Mul, Div - Both operands numeric
         // Check left operand to be numeric
         if(!operand1.getType().isNumeric())
         {
            return (new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr, operand1.getType().getName(), this.getName())));
         }
         // Check right operand to be numeric
         else if((!operand2.getType().isNumeric()))
         {
            return (new ErrorSTO(Formatter.toString(ErrorMsg.error1n_Expr, operand2.getType().getName(), this.getName())));
         }
         
         // Check successful, determine result type
         // Plus, Minus, Star, Slash - Int if both int, Float otherwise
         if(operand1.getType().isInt() && operand2.getType().isInt())
         {
             newSTO = new ExprSTO("DoBinaryOp Result", new IntType());
         }
         else
         {
             newSTO = new ExprSTO("DoBinaryOp Result", new FloatType());
         }
 
         return (newSTO);
     }
 }
