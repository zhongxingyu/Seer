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
  * The POP instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef S1
  */
 public class USI_POP extends AbstractInstruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "POP";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0x31; // 0b110001
 
 	/** The static instance for this instruction. */
 	static USI_POP staticInstance = new USI_POP(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
 		return lc+1;
 	}
 
 	/** @see assemblernator.Instruction#check(ErrorHandler, Module) */
 	@Override public boolean check(ErrorHandler hErr, Module module) {
 		boolean isValid = true;
 		Value value;
 		if(this.operands.size() > 2) {
 			hErr.reportError(makeError("extraOperandsIns", this.getOpId()), this.lineNum, -1);
 			isValid =  false;
 		} else if(this.operands.size() < 1) {
 			hErr.reportError(makeError("tooFewOperandsIns", this.getOpId()), this.lineNum, -1);
 		} else if(this.hasOperand("DR") && this.operands.size() == 1) {
 			value = module.evaluate(this.getOperand("DR"), false,BitLocation.Other, hErr, this, this.getOperandData("DR").valueStartPosition);
 			//range checking
 			isValid = OperandChecker.isValidReg(value.value);
 			if(!isValid) hErr.reportError(makeError("OORarithReg", "DR", this.getOpId()), this.lineNum, -1);
 			this.getOperandData("DR").value = value;
 		} else if(this.hasOperand("DM") && this.hasOperand("DX") && this.operands.size() == 2){
 			//range checking
 			value = module.evaluate(this.getOperand("DM"), true, BitLocation.Address, hErr, this, this.getOperandData("DM").valueStartPosition);
 			isValid = OperandChecker.isValidMem(value.value);
 			if(!isValid){
 				hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 				return isValid;
 			}
 			this.getOperandData("DM").value = value;
 			value = module.evaluate(this.getOperand("DX"), false, BitLocation.Other, hErr, this, this.getOperandData("DX").valueStartPosition);
 			isValid = OperandChecker.isValidIndex(value.value);
 			if(!isValid) hErr.reportError(makeError("OORidxReg", "DX", this.getOpId()), this.lineNum, -1);
 			this.getOperandData("DX").value = value;
 		} else if(this.hasOperand("DM") && this.operands.size() == 1) {
 			value = module.evaluate(this.getOperand("DM"), true, BitLocation.Address, hErr, this, this.getOperandData("DM").valueStartPosition);
 			isValid = OperandChecker.isValidMem(value.value);
 			if(!isValid) hErr.reportError(makeError("OORmemAddr", "DM", this.getOpId()), this.lineNum, -1);
 			this.getOperandData("DM").value = value;
 		} else{
 			//error
 			isValid = false;
 			hErr.reportError(makeError("instructionMissingOp", this.getOpId(), "{DM,DX}, {DR}, or {DM}"), this.lineNum, -1);
 			if(this.hasOperand("FR")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong",  this.getOpId(), "FR"), this.lineNum, -1);
 			}  else if(this.hasOperand("FC")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FC"), this.lineNum, -1);				
 			}  else if(this.hasOperand("FL")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FL"), this.lineNum, -1);				
 			} else if(this.hasOperand("FS")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "FS"), this.lineNum, -1);				
 			} else if(this.hasOperand("LR")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "LR"), this.lineNum, -1);				
 			} else if(this.hasOperand("FM")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong",  this.getOpId(),"FM"), this.lineNum, -1);				
 			} else if(this.hasOperand("EX")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong",  this.getOpId(),"EX"), this.lineNum, -1);				
 			} else if(this.hasOperand("NW")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "NW"), this.lineNum, -1);				
 			} else if(this.hasOperand("ST")){
 				isValid = false;
 				hErr.reportError(makeError("operandInsWrong", this.getOpId(), "ST"), this.lineNum, -1);				
 			}
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
 
 		int s= 0;
		if(!machine.stack.empty()) {
			s = machine.stack.pop();
			brkdwn.putToDest(s, machine);
		} else {
			machine.hErr.reportError(makeError("runEmptyStack"), machine.getLC(), -1);
		}
 		
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
 		return new USI_POP();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_POP(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_POP() {}
 }
 
