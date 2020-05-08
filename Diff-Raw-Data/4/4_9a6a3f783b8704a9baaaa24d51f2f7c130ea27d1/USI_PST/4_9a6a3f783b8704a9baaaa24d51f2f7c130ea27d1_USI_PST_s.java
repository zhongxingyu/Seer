 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import static assemblernator.InstructionFormatter.formatOther;
 import static simulanator.Deformatter.breakDownOther;
 import simulanator.Machine;
 import simulanator.Deformatter.OpcodeBreakdown;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.Module.Value;
 import assemblernator.Module.Value.BitLocation;
 import assemblernator.OperandChecker;
 
 /**
  * The PST instruction.
  * 
  * @author Ratul Khosla
  * @date Apr 08, 2012; 08:26:19
  * @specRef S2
  */
 public class USI_PST extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "PST";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x32; // 0b110010
 
 	/** The static instance for this instruction. */
 	static USI_PST staticInstance = new USI_PST(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 	
 		boolean isValid = true;
 		//anything less than 2 operands is an error
 			if(this.operands.size() < 2){
 				//error
 				isValid = false;
 				hErr.reportError(makeError("tooFewOperandsIns", this.getOpId()), this.lineNum, -1);
 				//checks combo for 2 operands if no DR error if dr and no FL or FM error
 			}else if(this.operands.size() == 2){
 				if(this.hasOperand("DR")){
 					//error checking
 					Operand o = getOperandData("DR");
 					Value constantSize = module.evaluate(o.expression, false, BitLocation.Other, hErr, this,
 							o.valueStartPosition);
 					this.getOperandData("DR").value = constantSize;
 					isValid = OperandChecker.isValidReg(constantSize.value);
 					if(!isValid){
 						hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 						return isValid;
 					}
 					if(this.hasOperand("FL")){
 						//error checking
 						Operand o1 = getOperandData("FL");
 						Value constantSize1 = module.evaluate(o1.expression, false, BitLocation.Literal, hErr, this,
 								o1.valueStartPosition);
 						this.getOperandData("FL").value = constantSize1;
						isValid = OperandChecker.isValidLiteral(constantSize1.value,ConstantRange.RANGE_13_TC);
						if(!isValid) hErr.reportError(makeError("OORconstant", "FL", this.getOpId(),"-2^12", "2^12 - 1"), this.lineNum, -1);
 					}else if (this.hasOperand("FM")){
 						//error checking
 						Operand o1 = getOperandData("FM");
 						Value constantSize1 = module.evaluate(o1.expression, true, BitLocation.Address, hErr, this,
 								o1.valueStartPosition);
 						this.getOperandData("FM").value = constantSize1;
 						isValid = OperandChecker.isValidMem(constantSize1.value);
 						if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 					}else{
 						isValid = false;
 						hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "FM or FL", "DR"), this.lineNum, -1);
 					}
 				}else{
 					//error
 					isValid = false;
 					hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "DR"), this.lineNum, -1);
 				}
 				//checks combo of 3 operands no dr error if dr and no FX and FX error
 			}else if(this.operands.size() == 3){
 				if(this.hasOperand("DR")){
 					//error checking
 					Operand o = getOperandData("DR");
 					Value constantSize = module.evaluate(o.expression, false, BitLocation.Other, hErr, this,
 							o.valueStartPosition);
 					this.getOperandData("DR").value = constantSize;
 					isValid = OperandChecker.isValidReg(constantSize.value);
 					if(!isValid){
 						hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 						return isValid;
 					}
 					if (this.hasOperand("FM") && this.hasOperand("FX")){
 						//error checking
 						Operand o1 = getOperandData("FX");
 						Value constantSize1 = module.evaluate(o1.expression, false, BitLocation.Other , hErr, this,
 								o1.valueStartPosition);
 						this.getOperandData("FX").value = constantSize1;
 						isValid = OperandChecker.isValidIndex(constantSize1.value);
 						if(!isValid){
 							hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 							return isValid;
 						}
 						Operand o2 = getOperandData("FM");
 						Value constantSize2 = module.evaluate(o2.expression, true, BitLocation.Address,  hErr, this,
 								o2.valueStartPosition);
 						this.getOperandData("FM").value = constantSize2;
 						isValid = OperandChecker.isValidMem(constantSize2.value);
 						if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 					}else{
 						//error
 						isValid = false;
 						hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "FX and FM", "DR"), this.lineNum, -1);
 					}
 				}else{
 					//error
 					isValid = false;
 					hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "DR"), this.lineNum, -1);
 				}
 				//to many operands error
 			}else{
 				//error
 				isValid = false;
 				hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 			}
 				return isValid;
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return formatOther(this);
 	}
 
 	/** @see assemblernator.Instruction#execute(int, Machine) */
 	@Override public void execute(int instruction, Machine machine) {
 
 		OpcodeBreakdown brkdwn = breakDownOther(machine.instruction);
 		int wordOrig = brkdwn.readFromSource(machine);
 		int compareVal;
 		int poppedVal = machine.stack.pop();
 		
 		if(poppedVal == wordOrig) {
 			compareVal = 0;
 		} else if (poppedVal < wordOrig) {
 			compareVal = 2;
 		} else {
 			compareVal = 3;			
 		}
 		brkdwn.putToDest(compareVal, machine);
 	}
 
 	// =========================================================
 	// === Redundant code ======================================
 	// =========================================================
 	// === This code's the same in all instruction classes, ====
 	// === But Java lacks the mechanism to allow stuffing it ===
 	// === in super() where it belongs. ========================
 	// =========================================================
 
 	/**
 	 * @see Instruction
 	 * @return The static instance of this instruction.
 	 */
 	public static Instruction getInstance() {
 		return staticInstance;
 	}
 
 	/** @see assemblernator.Instruction#getOpId() */
 	@Override public String getOpId() {
 		return opId;
 	}
 
 	/** @see assemblernator.Instruction#getOpcode() */
 	@Override public int getOpcode() {
 		return opCode;
 	}
 
 	/** @see assemblernator.Instruction#getNewInstance() */
 	@Override public Instruction getNewInstance() {
 		return new USI_PST();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_PST(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_PST() {}
 
 
 }
 
