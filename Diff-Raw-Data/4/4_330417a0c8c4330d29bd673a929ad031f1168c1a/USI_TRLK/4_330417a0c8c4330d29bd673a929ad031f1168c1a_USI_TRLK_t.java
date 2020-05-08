 package instructions;
 
 import static assemblernator.ErrorReporting.makeError;
 import assemblernator.AbstractInstruction;
 import assemblernator.ErrorReporting.ErrorHandler;
 import assemblernator.Instruction;
 import assemblernator.Module;
 import assemblernator.OperandChecker;
 
 /**
  * The TRLK instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef JT5
  */
 public class USI_TRLK extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "TRLK";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x00000025; // 0b10010100000000000000000000000000
 
 	/** The static instance for this instruction. */
 	static USI_TRLK staticInstance = new USI_TRLK(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 	/**
 	 * The type of operand specifying the destination for this operation.
 	 */
 	String dest = "";
 	/**
 	 * The type of operand specifying the source for this operation.
 	 */
 	String src = "";
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		//operands less than two error
 		if (this.operands.size() < 2){
 			isValid=false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), ""), this.lineNum, -1);
 			//checks combos for 2 operands
 		}else if(this.operands.size() == 2){
 			if(this.hasOperand("DR")){
 				dest="DR";
 				//range check
 				Operand o = getOperandData("DR");
 				int constantSize = module.evaluate(o.expression, false, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("DR").value = constantSize;
 				isValid = OperandChecker.isValidReg(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("FM")){
 					src="FM";
 					//range check
 					Operand o1 = getOperandData("FM");
 					int constantSize1 = module.evaluate(o1.expression, true, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FM").value = constantSize1;
 					isValid = OperandChecker.isValidMem(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 					//dont know if this is need but can be cut out
 				}else if (this.hasOperand("FL")){
 					src="FL";
 					//range check
 					Operand o1 = getOperandData("FL");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FL").value = constantSize1;
					isValid = OperandChecker.isValidLiteral(constantSize1,ConstantRange.RANGE_ADDR);
					if(!isValid) hErr.reportError(makeError("OORmemAddr", "FL", this.getOpId()), this.lineNum, -1);
 				}else{
 					isValid=false;
 					hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "FM or FL"), this.lineNum, -1);
 				}
 			}else{
 				
 			}
 			//checks combos for 3 operands
 		}else if(this.operands.size() == 3){
 			if(this.hasOperand("DR")){
 				dest="DR";
 				//range check
 				Operand o = getOperandData("DR");
 				int constantSize = module.evaluate(o.expression, false, hErr, this,
 						o.valueStartPosition);
 				this.getOperandData("DR").value = constantSize;
 				isValid = OperandChecker.isValidReg(constantSize);
 				if(!isValid) hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 				if(this.hasOperand("FM") && this.hasOperand("FX")){
 					src="FMFX";
 					//range check
 					Operand o1 = getOperandData("FX");
 					int constantSize1 = module.evaluate(o1.expression, false, hErr, this,
 							o1.valueStartPosition);
 					this.getOperandData("FX").value = constantSize1;
 					isValid = OperandChecker.isValidIndex(constantSize1);
 					if(!isValid) hErr.reportError(makeError("OORidxReg", "FX", this.getOpId()), this.lineNum, -1);
 					Operand o2 = getOperandData("FM");
 					int constantSize2 = module.evaluate(o2.expression, true, hErr, this,
 							o2.valueStartPosition);
 					this.getOperandData("FM").value = constantSize2;
 					isValid = OperandChecker.isValidMem(constantSize2);
 					if(!isValid) hErr.reportError(makeError("OORmemAddr", "FM", this.getOpId()), this.lineNum, -1);
 				}else{
 					isValid=false;
 					hErr.reportError(makeError("operandInsNeedAdd", this.getOpId(), "FM and FX", "DR"), this.lineNum, -1);
 				}
 			}
 		}else{
 			isValid =false;
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 		}
 		
 		
 		return isValid; // TODO: IMPLEMENT
 	}
 
 	/** @see assemblernator.Instruction#assemble() */
 	@Override public int[] assemble() {
 		return null; // TODO: IMPLEMENT
 	}
 
 	/** @see assemblernator.Instruction#execute(int) */
 	@Override public void execute(int instruction) {
 		// TODO: IMPLEMENT
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
 		return new USI_TRLK();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_TRLK(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_TRLK() {}
 }
 
