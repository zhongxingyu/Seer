 package instructions;
 
 import assemblernator.Instruction;
 import assemblernator.Module;
 
 /**
  * The NEWLC instruction.
  * 
  * @author Generate.java
  * @date Apr 08, 2012; 08:26:19
  * @specRef D2
  */
 public class USI_NEWLC extends Instruction {
 	/**
 	 * The operation identifier of this instruction; while comments should not
 	 * be treated as an instruction, specification says they must be included in
 	 * the user report. Hence, we will simply give this class a semicolon as its
 	 * instruction ID.
 	 */
 	private static final String opId = "NEWLC";
 
 	/** This instruction's identifying opcode. */
 	private static final int opCode = 0xFFFFFFFF; // This instruction doesn't have an opcode.
 
 	/** The static instance for this instruction. */
 	static USI_NEWLC staticInstance = new USI_NEWLC(true);
 
 	/** @see assemblernator.Instruction#getNewLC(int, Module) */
 	@Override public int getNewLC(int lc, Module mod) {
		int ownLC = (hasOperand("FC")?mod.evaluate(getOperand("FC")):mod.getAddress(getOperand("LR")));
 		this.lc = ownLC;
 		return ownLC;
 	}
 
 	/** @see assemblernator.Instruction#check() */
 	@Override public boolean check() {
 		return false; // TODO: IMPLEMENT
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
 		return new USI_NEWLC();
 	}
 
 	/**
 	 * Calls the Instance(String,int) constructor to track this instruction.
 	 * 
 	 * @param ignored
 	 *            Unused parameter; used to distinguish the constructor for the
 	 *            static instance.
 	 */
 	private USI_NEWLC(boolean ignored) {
 		super(opId, opCode);
 	}
 
 	/** Default constructor; does nothing. */
 	private USI_NEWLC() {}
 }
 
