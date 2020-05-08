 package instructions;
 import instructions.UIG_IO.OperandType;
 import assemblernator.IOFormat;
 import assemblernator.Instruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.OperandChecker;
 
 /**
  * @author Eric
  * @date Apr 14, 2012; 5:22:20 PM
  */
 public abstract class UIG_Arithmetic extends Instruction {
 
 	
 	String dest = "";
 	String src = "";
 
 	/**
 	 * @author Eric
 	 * @date Apr 14, 2012; 5:52:36 PM
 	 */
 	@Override
 	public final boolean check(ErrorHandler hErr) {
 		boolean isValid = true;
 //any size under two is invalid
 	if(this.operands.size() < 2){
 		isValid = false;
 		//checks all combinations for two operands if a combo is not found operands are invalid
 	}else if (this.operands.size() == 2){
 		//checks combos associated with DM
 		if(this.hasOperand("DM")){
 			dest="DM";
 			if(this.hasOperand("FR")){
 				src="FR";
 			}
			if(	this.hasOperand("FM")){
 				src="FM";
 			}
			if(	this.hasOperand("FL")){
 				src="FL";
 			}
 				else{
 				isValid = false;
 			}
 			//checks combos associated with DR
 		}else if (this.hasOperand("DR")){
 			dest="DR";
 			if(this.hasOperand("FR")){
 				src="FR";
 			}
 			else if(this.hasOperand("FM")){
 				src="FM";
 			}
 			else if(this.hasOperand("FL")){
 				src="FL";
 			}
 			else if (this.hasOperand("FX")){
 				src="FX";
 			}else{
 				isValid = false;
 			}
 			//checks combos associated with DX
 		}else if (this.hasOperand("DX")){
 			dest="DX";
 			if(this.hasOperand("FL")){
 				src="FL";
 			}
 			else if (this.hasOperand("FX") ){
 				src="FX";
 			}else{
 				isValid = false;
 			}
 			
 		}else{
 			isValid = false;
 		}
 		//checks all combinations for three operands instructions
 	}else if (this.operands.size() == 3){
 		//checks combos associated FR
 		if(this.hasOperand("FR")){
 			src="FR";
 			if(this.hasOperand("DM") && this.hasOperand("DX")){
 			dest="DMDX";
 			}
 			else{
 				isValid=false;
 			}
 			//checks combos associated DR
 		}else if(this.hasOperand("DR")){
 			dest="DR";
 			if(this.hasOperand("FX") && this.hasOperand("FM")){
 				src="FXFM";
 			}
 			else{
 				isValid=false;
 			}
 		}else{
 			isValid =false;
 		}
 		//more than three operands is invalid	
 	}else{
 		isValid = false;
 	}
 	
 		return isValid;
 		
 	}
 	
 
 	@Override
 	public final int[] assemble() {
 		String code = IOFormat.formatBinInteger(this.getOpcode(), 6);
 		if(dest == "DR"){
 			if(src=="FM" || src=="FL" || src=="FXFM"){
 				//format 0
 			}else{
 				//format 1
 			}
 		}else if(dest == "DX"){
 			// and so on
 		}
 		
 		
 		
 		return null;
 		
 		
 	}
 	}
 
 
