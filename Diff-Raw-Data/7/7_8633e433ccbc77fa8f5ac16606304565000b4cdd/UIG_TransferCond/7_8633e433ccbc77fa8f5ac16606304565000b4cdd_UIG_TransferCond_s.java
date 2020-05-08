 package instructions;
 
 import assemblernator.IOFormat;
 import assemblernator.Instruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.OperandChecker;
 
 /**
  * @author Eric
  * @date Apr 14, 2012; 5:52:36 PM
  */
 public abstract class UIG_TransferCond extends Instruction {
 	String dest = "";
 	String src = "";
 	
 	/**
 	 * @author Eric
 	 * @date Apr 14, 2012; 5:52:36 PM
 	 */
 	@Override
 	public final boolean check(ErrorHandler hErr) {
 		boolean isValid = true;
 		//not enough operands
 		if(this.operands.size() > 2){
 			isValid=false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), ""), this.lineNum, -1);
			//just right amount of operands
 		}else if(this.operands.size() == 2){
 			//combo check
 			if(this.hasOperand("FR")){
 				src="FR";
 				//range checking
 				isValid = OperandChecker.isValidMem(this.getOperand("FR"));
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "FR", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("DM")){
 					dest="DM";
 					//range checking
 					isValid = OperandChecker.isValidMem(this.getOperand("DM"));
 					if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				}else{
 					isValid = false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "DM", "FR"), this.lineNum, -1);
 					
 				}
 			}else{
 				isValid = false;
 				hErr.reportError(makeError("instructionMissingOp","FR", this.getOpId()), this.lineNum, -1);
 				
 			}
			//to many operands
 		}else if (this.operands.size() == 3){
 			if(this.hasOperand("FR")){
 			src="FR";
 			//range checking
 			isValid = OperandChecker.isValidMem(this.getOperand("FR"));
 			if(!isValid) hErr.reportError(makeError("OORidxReg", "FR", this.getOpId()), this.lineNum, -1);
 				if (this.hasOperand("DM") && this.hasOperand("DX")) {
 				dest = "DMDX";
 				//range checking
 				isValid = OperandChecker.isValidMem(this.getOperand("DX"));
 				if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 				isValid = OperandChecker.isValidMem(this.getOperand("DM"));
 				if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				} else {
 				isValid = false;
 				hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "DM and DX", "FR"), this.lineNum, -1);
 				}
 			
 			}else{
 				isValid = false;
 				hErr.reportError(makeError("instructionMissingOp","FR", this.getOpId()), this.lineNum, -1);
 			}
 		}else{
 			isValid =false;
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 		}
 		
 	
 		return isValid;
 	
 }
 	
 }
